package nmea.computers;

import calc.GeoPoint;
import calc.GeomUtil;
// import calc.GreatCirclePoint;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.parser.Angle360;
import nmea.parser.GeoPos;
import nmea.parser.Speed;
import nmea.parser.StringParsers;
// import util.MercatorUtil;
import util.TextToSpeech;
import utils.TimeUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * AIS Manager. WiP.
 * Uses current position and AIS data to detect <u>possible collision threats</u>.
 * <br/>
 * Threats will end up in the cache, along with AIS targets.
 * <br/>
 * To be used as a custom computer:
 * <pre>
 * computers:
 *   - class: nmea.computers.AISManager
 *     properties: mux-configs/ais.mgr.properties
 * </pre>
 * Properties file like:
 * <pre>
 * # Properties of the AISManager Computer
 * minimum.distance=5
 * heading.fork.width=10
 *
 * # For test (big distance, big fork)
 * #minimum.distance=50
 * #heading.fork.width=90
 *
 * # collision.threat.callback=nmea.computers.SpeakingCallback
 * collision.threat.callback=default
 * </pre>
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '.ais'
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class AISManager extends Computer {

	private final static double DEFAULT_MINIMUM_DISTANCE = 20D;
	private double minimumDistance = DEFAULT_MINIMUM_DISTANCE;
	private final static double DEFAULT_COLLISION_THREAT_DISTANCE = 1;
	private double collisionThreatDistance = DEFAULT_COLLISION_THREAT_DISTANCE;

	private final AISParser aisParser = new AISParser();

	public AISManager(Multiplexer mux) {
		super(mux);
	}

	public Consumer<String> defaultCallback = s -> {
		System.out.println("---- C O L L I S I O N   T H R E A T ---");
		System.out.println(s);
		System.out.println("----------------------------------------");
	};
	private Consumer<String> collisionCallback = null;

	/**
	 * Find the shortest distance between two trajectories.
	 * <br/>
	 * Finding the roots of the first derivative of the formula of this difference is a bit tough...
	 * <br/>
	 * We will iterate.
	 * <br/>
	 * We start with the boat's position, and the AIS target's position.<br/>
	 * We measure the distance between those two positions.<br/>
	 * Now, as long as this distance is shrinking, we keep moving the two position, at their respective speed, in their respective heading.<br/>
	 * As soon as the distance between the two position starts to grow (or stops shrinking),
	 * we stop looping, and return the smallest distance found.<br/>
	 *
	 * @param position The boat's original position
	 * @param sog The boat's SOG
	 * @param cog The boat's COG
	 * @param targetPos The AIS target's original position
	 * @param targetSog The AIS target's SOG
	 * @param targetCog The AIS target's COG
	 * @return the shortest distance found, with an interval of 10 seconds
	 */
	private static double findCollision(GeoPos position, double sog, double cog, GeoPos targetPos, double targetSog, double targetCog) {
		return findCollision(position, sog, cog, targetPos, targetSog, targetCog, false);
	}
	private static double findCollision(GeoPos position, double sog, double cog, GeoPos targetPos, double targetSog, double targetCog, boolean verbose) {
		double originalDistance = GeomUtil.haversineNm(position.lat, position.lng, targetPos.lat, targetPos.lng);
		double smallestDist = originalDistance;
		double BETWEEN_LOOPS = 10d; // In seconds
		long nbLoops = 0;
		boolean keepLooping = true;
		while (keepLooping) {
			nbLoops += 1;
			// New boat position, 10 seconds later
			double dist = sog * (BETWEEN_LOOPS / 3_600d);
			GeoPoint pt = GeomUtil.deadReckoning(position.lat, position.lng, dist, cog);
			GeoPos newPos = new GeoPos(pt.getLatitude(), pt.getLongitude());

			// New target position, 10 seconds later
			dist = targetSog * (BETWEEN_LOOPS / 3_600d);
			pt = GeomUtil.deadReckoning(targetPos.lat, targetPos.lng, dist, targetCog);
			GeoPos newTargetPos = new GeoPos(pt.getLatitude(), pt.getLongitude());

			double newRange = GeomUtil.haversineNm(position.lat, position.lng, newTargetPos.lat, newTargetPos.lng);
			if (newRange < smallestDist) { // Still closing
				smallestDist = newRange;
				position = newPos;
				targetPos = newTargetPos;
				if (verbose) {
					long _now = (long)(nbLoops * BETWEEN_LOOPS * 1_000L);
					System.out.printf("Smallest distance is now %.03f nm - between (target) %s and (boat) %s (loop #%d, %s)\n",
							smallestDist,
							targetPos,
							position,
							nbLoops,
							TimeUtil.fmtDHMS(TimeUtil.msToHMS(_now)));
				}
			} else {
				keepLooping = false;
			}
		}
		return smallestDist;
	}

	/**
	 * Wait for AIS data (not cached), get the position from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void write(byte[] mess) {
		String sentence = new String(mess);
        // System.out.println(String.format("In AIS Computer, write method: %s", sentence));

		if (StringParsers.validCheckSum(sentence)) {
			if (sentence.startsWith(AISParser.AIS_PREFIX)) {
				try {
					AISParser.AISRecord aisRecord = aisParser.parseAIS(sentence);
					if (aisRecord != null) {
						// TODO Manage MMSI to ignore (like the one of the boat we're in...)
						if (aisRecord.getLatitude() != 0f && aisRecord.getLongitude() != 0f) {
							NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
							GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
							if (position != null) {
								double distToTarget = GeomUtil.haversineNm(position.lat, position.lng, aisRecord.getLatitude(), aisRecord.getLongitude());
								double bearingFromTarget = GeomUtil.bearingFromTo(aisRecord.getLatitude(), aisRecord.getLongitude(), position.lat, position.lng);
								// It's worth calculating
								if (distToTarget <= this.minimumDistance) {
									// double diffHeading = GeomUtil.bearingDiff(bearingFromTarget, aisRecord.getCog());
									String inRangeMessage = String.format("(%s) AISManager >> In range: [%s] (%.02f/%.02f nm), min dist: %.02f/%.02f",
											TimeUtil.getTimeStamp(),
											(aisRecord.getVesselName() != null ? aisRecord.getVesselName() : (aisRecord.getMMSI() != 0 ? aisRecord.getMMSI() : "")), // MMSI ?
											distToTarget,
											this.minimumDistance,
											distToTarget,
											this.collisionThreatDistance);
									System.out.println(inRangeMessage);
									if (false) {
										// A test
										String messToSpeak = String.format("Boat in range %.02f miles! %s", distToTarget, (aisRecord.getVesselName() != null ? aisRecord.getVesselName() : ""));
										TextToSpeech.speak(messToSpeak);
									}
									double sog = cache.get(NMEADataCache.SOG) != null ? ((Speed)cache.get(NMEADataCache.SOG)).getSpeed() : 0d;
									double cog = cache.get(NMEADataCache.COG) != null ? ((Angle360)cache.get(NMEADataCache.COG)).getAngle() : 0d;
									double targetSog = aisRecord.getSog();
									double targetCog = aisRecord.getCog();
									GeoPos targetPosition = new GeoPos(aisRecord.getLatitude(), aisRecord.getLongitude());
									double dist = findCollision(position, sog, cog, targetPosition, targetSog, targetCog);
									// Then warn
									if (dist < this.collisionThreatDistance) { // diffHeading < this.headingFork) { // Possible collision route (if you don't move)
										// Collision threat in the cache
										aisRecord.setCollisionThreat(new AISParser.CollisionThreat(distToTarget, bearingFromTarget, this.minimumDistance, dist));
										// Find vesselName if it exists
										String vesselName = aisRecord.getVesselName();
										if (vesselName == null) {
											// Look for record type 24 (Static Data Report)
											try {
												final Map<Integer, Map<Integer, AISParser.AISRecord>> aisMap = (Map<Integer, Map<Integer, AISParser.AISRecord>>)cache.get(NMEADataCache.AIS);
												Map<Integer, AISParser.AISRecord> mapOfTypes = aisMap.get(aisRecord.getMMSI());
												AISParser.AISRecord aisRecord2 = mapOfTypes.get(24);
												if (aisRecord2 != null) {
													vesselName = aisRecord2.getVesselName();
												}
											} catch (Exception ex) {
												System.err.println("Getting vessel name:");
												ex.printStackTrace();
											}
										}
										String warningText = String.format("!!! Possible collision threat with %s (%s), at %s / %s\n" +
														"\tdistance %.02f nm (min is %.02f)\n" +
														"\tBearing from target to current pos. %.02f\272\n" +
														"\tCOG Target: %.02f\n" +
														"\tSOG Target: %.02f\n" +
														"\tMin dist: %.03fnm",
												aisRecord.getMMSI(),
												vesselName != null ? vesselName.replace("@", " ").trim() : "-",
												GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
												distToTarget,
												this.minimumDistance,
												bearingFromTarget,
												aisRecord.getCog(),
												aisRecord.getSog(),
												dist);
										System.out.println(warningText);

										// Honk! Define a callback Consumer<String> (see 'speak' below), or just a signal (sent to a buzzer, a light, whatever).
										if (collisionCallback != null) {
											// A test
											String targetName = vesselName != null ? vesselName.replace("@", " ").trim() : "";
											if (targetName.length() == 0) {
												targetName = String.valueOf(aisRecord.getMMSI());
											}
											int bearingToTarget = (int)(180 + Math.round(bearingFromTarget));
											bearingToTarget %= 360;
//											String messageToSpeak = String.format("Possible collision threat with %s, %.02f miles in the %d.",
//													targetName,
//													distToTarget,
//													bearingToTarget);
											String messageToSpeak = String.format("AIS-COLLISION;%s;%.02f;%d;%f",
													targetName,
													distToTarget,
													bearingToTarget,
													dist);
											collisionCallback.accept(messageToSpeak);
  											// TextToSpeech.speak(messageToSpeak);
										}
									} else {
										aisRecord.setCollisionThreat(null);
									}
									// Push record back in the cache
									if (this.verbose) {
										System.out.printf("Pushing aisRecord in cache:\n%s\n", aisRecord.toString());
									}
									final Map<Integer, Map<Integer, AISParser.AISRecord>> aisMap = (Map<Integer, Map<Integer, AISParser.AISRecord>>)cache.get(NMEADataCache.AIS);
									Map<Integer, AISParser.AISRecord> mapOfTypes = aisMap.get(aisRecord.getMMSI());
									mapOfTypes.put(aisRecord.getMessageType(), aisRecord);
									cache.put(NMEADataCache.AIS, aisMap); // Back in.
								} else {
									if (this.verbose) {
										System.out.printf("For %s (%s): Comparing %s with %s / %s (%.04f / %.04f)\n" +
														"\tdistance %.02f nm (min is %.02f)\n" +
														"\tBearing from target to current pos. %.02f\272\n" +
														"\tCOG Target: %.02f\n" +
														"\t-> No threat found\n",
												aisRecord.getMMSI(),
												aisRecord.getVesselName(),
												position.toString(),
												GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
												aisRecord.getLatitude(),
												aisRecord.getLongitude(),
												distToTarget,
												this.minimumDistance,
												bearingFromTarget,
												aisRecord.getCog());
									}
								}
							}
						}
					}
				} catch (AISParser.AISException aisException) { // un-managed AIS type
					// Absorb
				}
			}
		}
	}

	@Override
	public void setActive(boolean active) {
		if (isVerbose()) {
			System.out.printf("Setting active to %b for %s (%s)\n", active, ((AISManager.AISComputerBean) this.getBean()).getType(), this.getClass().getName());
		}
		super.setActive(active);
		// Specific to some callback, as they run on their own...
		if (collisionCallback != null && (collisionCallback instanceof BufferedCollisionCallback ||
				collisionCallback instanceof BufferedCollisionSingletonCallback ||
				collisionCallback instanceof RESTClientCollisionCallback)) {
			if (isVerbose()) {
				System.out.printf("Setting BufferedCollision(*)Callback to %b\n", active);
			}
			if (collisionCallback instanceof BufferedCollisionCallback) {
				((BufferedCollisionCallback) collisionCallback).setActive(active);
			}
			if (collisionCallback instanceof BufferedCollisionSingletonCallback) {
				((BufferedCollisionSingletonCallback) collisionCallback).setActive(active);
			}
			if (collisionCallback instanceof RESTClientCollisionCallback) {
				((RESTClientCollisionCallback) collisionCallback).setActive(active);
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing AIS data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(DEFAULT_MINIMUM_DISTANCE)));
		this.collisionThreatDistance = Double.parseDouble(props.getProperty("collision.threat.distance", String.valueOf(DEFAULT_COLLISION_THREAT_DISTANCE)));
		this.verbose = "true".equals(props.getProperty("verbose"));
		String callback = props.getProperty("collision.threat.callback");
		if (callback != null) {
			if (callback.equals("default")) {
				this.collisionCallback = defaultCallback;
			} else {
				try {
//					Class<?> aConsumer = Class.forName(callback);
//					this.collisionCallback = (Consumer<String>) aConsumer.getDeclaredConstructor().newInstance();
					Class<?> aConsumer = Class.forName(callback);
					final Method[] methods = aConsumer.getMethods();
					if (Arrays.stream(methods).filter(m -> m.getName().equals("getInstance")).findFirst().isPresent()) {
						// A singleton
						this.collisionCallback = (Consumer<String>) aConsumer.getDeclaredMethod("getInstance").invoke(null);
					} else {
						this.collisionCallback = (Consumer<String>) aConsumer.getDeclaredConstructor().newInstance();
					}
					// Specific properties for this consumer ?
					final String cbProps = props.getProperty("collision.threat.callback.props");
					if (cbProps != null) {
						final Method cbMethod = aConsumer.getDeclaredMethod("setProperties", String.class);
						if (cbMethod != null) {
							cbMethod.invoke(this.collisionCallback, cbProps);
						} else {
							System.out.printf("No such method 'setProperties' in %s\n", aConsumer.getName());
						}
					}
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				} catch (IllegalAccessException iae) {
					iae.printStackTrace();
				} catch (InstantiationException ie) {
					ie.printStackTrace();
				} catch (NoSuchMethodException nsme) {
					nsme.printStackTrace();
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				}
			}
		}
		if (this.verbose) {
			System.out.println(String.format("Computer %s:\nVerbose: %s\nMinimum Distance: %.02f\nCollision Threat Distance: %.01f",
					this.getClass().getName(),
					this.verbose,
					this.minimumDistance,
					this.collisionThreatDistance));
		}
	}

	public static class AISComputerBean {
		private String cls;
		private final String type = "ais-computer";
		private boolean verbose;
		private boolean active;

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public boolean isVerbose() {
			return verbose;
		}
		public boolean isActive() {
			return active;
		}

		public AISComputerBean() {}  // This is for Jackson
		public AISComputerBean(AISManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
			this.active = instance.isActive();
		}
	}

	@Override
	public Object getBean() {
		return new AISComputerBean(this);
	}

	// For tests
	public static void main(String[] args) {
		GeoPos A = new GeoPos(47.677667, -3.135667);
		double sogA = 0.10;
		double cogA = 90.0;

		GeoPos B = new GeoPos(47.8, -3.135667);
		double sogB = 5.0;
		double cogB = 182.0;

		double dist = findCollision(A, sogA, cogA, B, sogB, cogB, true);
		System.out.printf("Done, smallest dist is %.03f nm\n", dist);
	}
}
package nmea.computers;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.parser.GeoPos;
import nmea.parser.StringParsers;
import util.TextToSpeech;
import utils.TimeUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * AIS Manager. WiP.
 * Uses current position and AIS data to detect <u>possible collision threats</u>.
 * <br/>
 * Does NOT put anything in the cache.
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

	public static class AISCollisionThreat {
		private int mmsi;
		private String vesselName;
		private GeoPos targetPos;
		private double distToTarget;
		private double bearingFromTarget;
		private double targetCOG;

		public AISCollisionThreat() {}

		public AISCollisionThreat mmsi(int mmsi) {
			this.mmsi = mmsi;
			return this;
		}
		public AISCollisionThreat vesselName(String vesselName) {
			this.vesselName = vesselName;
			return this;
		}
		public AISCollisionThreat targetPos(GeoPos targetPos) {
			this.targetPos = targetPos;
			return this;
		}
		public AISCollisionThreat distToTarget(double distToTarget) {
			this.distToTarget = distToTarget;
			return this;
		}
		public AISCollisionThreat bearingFromTarget(double bearingFromTarget) {
			this.bearingFromTarget = bearingFromTarget;
			return this;
		}
		public AISCollisionThreat targetCOG(double targetCOG) {
			this.targetCOG = targetCOG;
			return this;
		}
		public int getMmsi() {
			return mmsi;
		}

		public void setMmsi(int mmsi) {
			this.mmsi = mmsi;
		}

		public String getVesselName() {
			return vesselName;
		}

		public void setVesselName(String vesselName) {
			this.vesselName = vesselName;
		}

		public GeoPos getTargetPos() {
			return targetPos;
		}

		public void setTargetPos(GeoPos targetPos) {
			this.targetPos = targetPos;
		}

		public double getDistToTarget() {
			return distToTarget;
		}

		public void setDistToTarget(double distToTarget) {
			this.distToTarget = distToTarget;
		}

		public double getBearingFromTarget() {
			return bearingFromTarget;
		}

		public void setBearingFromTarget(double bearingFromTarget) {
			this.bearingFromTarget = bearingFromTarget;
		}

		public double getTargetCOG() {
			return targetCOG;
		}

		public void setTargetCOG(double targetCOG) {
			this.targetCOG = targetCOG;
		}
	}

	private final static double DEFAULT_MINIMUM_DISTANCE = 20D;
	private double minimumDistance = DEFAULT_MINIMUM_DISTANCE;
	private final static double DEFAULT_HEADING_FORK = 10;
	private double headingFork = DEFAULT_HEADING_FORK;

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
	 * Wait for AIS data (not cached), get the position from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
        // System.out.println(String.format("In AIS Computer, write method: %s", sentence));

		if (StringParsers.validCheckSum(sentence)) {
			if (sentence.startsWith(AISParser.AIS_PREFIX)) {
				try {
					AISParser.AISRecord aisRecord = aisParser.parseAIS(sentence);
					if (aisRecord != null) {
						if (aisRecord.getLatitude() != 0f && aisRecord.getLongitude() != 0f) {
							NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
							GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
							if (position != null) {
								double distToTarget = GeomUtil.haversineNm(position.lat, position.lng, aisRecord.getLatitude(), aisRecord.getLongitude());
								double bearingFromTarget = GeomUtil.bearingFromTo(aisRecord.getLatitude(), aisRecord.getLongitude(), position.lat, position.lng);
								// TODO Use the two speeds and headings (here and target). First degree equation solving.
								if (distToTarget <= this.minimumDistance) {
									double diffHeading = GeomUtil.bearingDiff(bearingFromTarget, aisRecord.getCog());
									String inRangeMessage = String.format("(%s) AISManager >> In range (%.02f/%.02f nm), diff heading: %.02f/%.02f", TimeUtil.getTimeStamp(), distToTarget, this.minimumDistance, diffHeading, this.headingFork);
									System.out.println(inRangeMessage);
									if (false) {
										// A test
										String messToSpeak = String.format("Boat in range %.02f miles! %s", distToTarget, (aisRecord.getVesselName() != null ? aisRecord.getVesselName() : ""));
										TextToSpeech.speak(messToSpeak);
									}
									if (diffHeading < this.headingFork) { // Possible collision route (if you don't move)
										// Collision threat in the cache
										aisRecord.setCollisionThreat(new AISParser.CollisionThreat(distToTarget, bearingFromTarget, this.minimumDistance));
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
														"\tCOG Target: %.02f",
												aisRecord.getMMSI(),
												vesselName.replace("@", " ").trim(),
												GeomUtil.decToSex(aisRecord.getLatitude(), GeomUtil.SWING, GeomUtil.NS),
												GeomUtil.decToSex(aisRecord.getLongitude(), GeomUtil.SWING, GeomUtil.EW),
												distToTarget,
												this.minimumDistance,
												bearingFromTarget,
												aisRecord.getCog());

										AISCollisionThreat collisionThreat = new AISCollisionThreat()
												.bearingFromTarget(bearingFromTarget)
												.mmsi(aisRecord.getMMSI())
												.vesselName(vesselName.replace("@", " ").trim())
												.targetPos(new GeoPos(aisRecord.getLatitude(), aisRecord.getLongitude()))
												.targetCOG(aisRecord.getCog());
										// TODO Send this to cache

										System.out.println(warningText);
										// TODO Honk! Define a callback Consumer<String> (see 'speak' below), or just a signal (sent to a buzzer, a light, whatever).
										if (collisionCallback != null) {
											// A test
											int bearingToTarget = (int)(180 + Math.round(bearingFromTarget));
											bearingToTarget %= 360;
											String messageToSpeak = String.format("Possible collision threat, %.02f miles in the %d.",
													distToTarget,
													bearingToTarget);
											collisionCallback.accept(messageToSpeak);
//											TextToSpeech.speak(messageToSpeak);
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
	public void close() {
		System.out.println("- Stop Computing AIS data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(DEFAULT_MINIMUM_DISTANCE)));
		this.headingFork = Double.parseDouble(props.getProperty("heading.fork.width", String.valueOf(DEFAULT_HEADING_FORK)));
		this.verbose = "true".equals(props.getProperty("verbose"));
		String callback = props.getProperty("collision.threat.callback");
		if (callback != null) {
			if (callback.equals("default")) {
				this.collisionCallback = defaultCallback;
			} else {
				try {
					Class<?> aConsumer = Class.forName(callback);
					this.collisionCallback = (Consumer<String>) aConsumer.getDeclaredConstructor().newInstance();
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
			System.out.println(String.format("Computer %s:\nVerbose: %s\nMinimum Distance: %.02f\nHeading Fork: %.01f",
					this.getClass().getName(),
					this.verbose,
					this.minimumDistance,
					this.headingFork));
		}
	}

	public static class AISComputerBean {
		private String cls;
		private final String type = "ais-computer";
		private boolean verbose;

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public AISComputerBean() {}  // This is for Jackson
		public AISComputerBean(AISManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new AISComputerBean(this);
	}
}

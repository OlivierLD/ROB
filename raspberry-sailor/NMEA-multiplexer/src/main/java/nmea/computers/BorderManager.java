package nmea.computers;

import algebra.SquareMatrix;
import algebra.SystemUtil;
import calc.GreatCirclePoint;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.parser.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Border Manager. WiP.
 * Uses current position and borders definition <u>possible collision threats</u>.
 * <br/>
 * Threats will end up in the cache.
 * <br/>
 * To be used as a custom computer:
 * <pre>
 * computers:
 *   - class: nmea.computers.BorderManager
 *     properties: mux-configs/border.mgr.properties
 * </pre>
 * Properties file like:
 * <pre>
 * # Properties of the BorderManager Computer
 * minimum.distance=0.5
 *
 * # For test (big distance, big fork)
 * #minimum.distance=50
 *
 * # border.threat.callback=nmea.computers.SpeakingCallback
 * border.threat.callback=default
 * </pre>
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '."borders-data"'
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class BorderManager extends Computer {

	final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));

	public static class BorderThreat {
		private double minDist;
		private String borderName;
		private Date date;
		private int segmentIdx;
		private double dist;

		public BorderThreat() {}

		public BorderThreat borderName(String name) {
			this.borderName = name;
			return this;
		}
		public BorderThreat date(Date date) {
			this.date = date;
			return this;
		}
		public BorderThreat segmentIdx(int idx) {
			this.segmentIdx = idx;
			return this;
		}
		public BorderThreat dist(double dist) {
			this.dist = dist;
			return this;
		}
		public BorderThreat minDist(double dist) {
			this.minDist = dist;
			return this;
		}
		public String getBorderName() {
			return borderName;
		}

		public void setBorderName(String borderName) {
			this.borderName = borderName;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getSegmentIdx() {
			return segmentIdx;
		}

		public void setSegmentIdx(int segmentIdx) {
			this.segmentIdx = segmentIdx;
		}

		public double getDist() {
			return dist;
		}
		public double getMinDist() {
			return minDist;
		}

		public void setDist(double dist) {
			this.dist = dist;
		}
	}

	private final static double DEFAULT_MINIMUM_DISTANCE = 20D;
	private double minimumDistance = DEFAULT_MINIMUM_DISTANCE;
	public BorderManager(Multiplexer mux) {
		super(mux);
	}

	public Consumer<String> defaultCallback = s -> {
		System.out.println("---- B O R D E R   T H R E A T ---");
		System.out.println(s);
		System.out.println("----------------------------------");
	};
	private Consumer<String> collisionCallback = null;

	private static class DistAndClosestPoint {
		double dist;  // In NM
		GeoPos closest; // In degrees
	}
	private static DistAndClosestPoint getProximity(GeoPos position, Marker markerOne, Marker markerTwo) {

		DistAndClosestPoint dacp = new DistAndClosestPoint();
		double distToSegment;
		if (true) {
			// Equation de la droite [markerOne, markerTwo], y = ax + b (where y: lat, x: lng)
			double deltaLng = markerTwo.getLongitude() - markerOne.getLongitude();
			double deltaLat = markerTwo.getLatitude() - markerOne.getLatitude();
			double coeffA = (deltaLng == 0.0) ? 0.0 : (deltaLat / deltaLng);
			// markerOne.lat = (coeffA * markerOne.lng) + b
			// => b = markerOne.lat - (coeffA * markerOne.lng)
			double coeffB = markerOne.getLatitude() - (coeffA * markerOne.getLongitude());
			// double coeffB = markerOne.getLongitude() - (coeffA * markerOne.getLatitude());
			if (false) {
				System.out.printf("-> From pos %f / %f, coeffA = %f, coeffB = %f, f(x) = %f\n",
						position.lat, position.lng, coeffA, coeffB, (coeffA * position.lng) + coeffB);
			}
			// Coeffs of the perpendicular to the segment, going through position
			double perpCoeffA = (coeffA != 0.0 ? - (1.0 / coeffA) : Double.MAX_VALUE); // If coeffA = 0 (horizontal line, x = a)
			double perpCoeffB = position.lat - (perpCoeffA * position.lng);

			if (false) {
				System.out.printf("CoeffA [%f], PerpCoeffA [%f], PerpCoeffB [%f]\n", coeffA, perpCoeffA, perpCoeffB);
			}
			// The closest point from "position" is the intersection of the two equations
			// y = coeffA x + coeffB
			// y = perpCoeffA x + perpCoeffB
			// where x = lng, y = lat
			// Matrix is
			// | 1.y     -coeffA.x |  |     coeffB |
			// | 1.y -perpCoeffA.x |  | perpCoeffB |
			SquareMatrix sqMat = new SquareMatrix(2);
			sqMat.setMatrixElements(new double [][] { {1.0, -coeffA}, {1.0, -perpCoeffA} });
			final double[] xy = SystemUtil.solveSystem(sqMat, new double[]{coeffB, perpCoeffB});
			double closestLat = xy[0];
			double closestLng = xy[1];
			try {
				dacp.closest = new GeoPos(closestLat, closestLng);
			} catch (Exception ex) {
				if (VERBOSE) {
					System.err.println(ex);
					System.err.printf("Between [%s] and [%s], from [%s]\n", markerOne, markerTwo, position);
					System.err.printf(" >> For Lat [%f], Lng [%f]\n", closestLat, closestLng);
					System.err.printf(" >> CoeffA [%f], PerpCoeffA [%f], PerpCoeffB [%f]\n", coeffA, perpCoeffA, perpCoeffB);
				}
				dacp.closest = null;
			}
			// System.out.printf("Closest point from %s is %s\n", position.toString(), dacp.closest.toString());
			distToSegment = Math.abs((coeffA * position.lng) - position.lat + coeffB) / (Math.sqrt((coeffA * coeffA) + 1)); // in degrees
			dacp.dist = distToSegment * 60;
		} else {  // Another method...
			// from https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
			double x0 = position.lng;
			double y0 = position.lat;
			double x1 = markerOne.getLongitude();
			double y1 = markerOne.getLatitude();
			double x2 = markerTwo.getLongitude();
			double y2 = markerTwo.getLatitude();
			distToSegment = Math.abs(((x2 - x1) * (y0 -y1)) - ((x0 - x1) * (y2 - y1))) / Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
			double distInNm = distToSegment * 60.0;
		}
		return dacp;
	}
	/**
	 * Wait for position data (not cached), get the perimeters/borders from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void write(byte[] mess) {
		String sentence = new String(mess);

		if (StringParsers.validCheckSum(sentence)) {

			// On position data only ? GLL, RMC ?
			if ("RMC".equals(sentence.substring(3, 6)) || "GLL".equals(sentence.substring(3, 6))) {

				NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
				GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
				UTCDate utcDate = (UTCDate) cache.get(NMEADataCache.GPS_DATE_TIME, true);
				if (position != null) {

					List<Border> borders = (List<Border>) cache.get(NMEADataCache.BORDERS_DATA);
					// See https://www.alloprof.qc.ca/fr/eleves/bv/mathematiques/la-distance-d-un-point-a-une-droite-dans-un-plan-m1315
					// final String collected = borders.stream().map(border -> "[" + border.getBorderName() + "]").collect(Collectors.joining(" "));
					// System.out.println("Borders: " + collected);

					// Distance to border's segments
					// Warning: considered as a CARTESIAN Plan!!
					List<List<DistAndClosestPoint>> distancesToBorders = new ArrayList<>();
					borders.forEach(border -> {
						List<DistAndClosestPoint> distancesToSegments = new ArrayList<>();
						final List<Marker> markerList = border.getMarkerList();
						for (int i=0; i< markerList.size() - 1; i++) {
							final Marker markerOne = markerList.get(i);
							final Marker markerTwo = markerList.get(i + 1);
							DistAndClosestPoint proximity = getProximity(position, markerOne, markerTwo);
							if (VERBOSE) {
								System.out.printf(">> From [%s], border [%s], segment %d, dist = %f\n", position.toString(), border.getBorderName(), i, proximity.dist);
							}
							distancesToSegments.add(proximity);
						}
						distancesToBorders.add(distancesToSegments);
					});
					boolean threatDetected = false; // Computation result.
					// compare to this.minimumDistance
					List<BorderThreat> threats = new ArrayList<>();
					List<String> threatMessages = new ArrayList<>();
					for (int border=0; border<distancesToBorders.size(); border++) {
						final List<DistAndClosestPoint> toBorders = distancesToBorders.get(border);
						String name = borders.get(border).getBorderName();
						for (int seg=0; seg<toBorders.size(); seg++) {
							DistAndClosestPoint dacp = toBorders.get(seg);
							double segDist = dacp.dist;
							if (VERBOSE) {
								System.out.printf(">>> For border [%s], segment %d, seg-dist is %f (min is %f)...\n", name, seg, segDist, this.minimumDistance);
							}
							if (segDist <= this.minimumDistance) { // Potential threat detected
								Marker markerOne = borders.get(border).getMarkerList().get(seg);
								Marker markerTwo = borders.get(border).getMarkerList().get(seg + 1);

								// WiP... TODO Cleanup the unused conditions...
								// Position between segment extremities - in lat, in lng
//								boolean conditionOne = ((position.lng <= Math.max(markerOne.getLongitude(), markerTwo.getLongitude()) && position.lng >= Math.min(markerOne.getLongitude(), markerTwo.getLongitude())) ||
//										(position.lat <= Math.max(markerOne.getLatitude(), markerTwo.getLatitude()) && position.lat >= Math.min(markerOne.getLatitude(), markerTwo.getLatitude())));
								// Closest point "IN" the segment !
								boolean conditionOne = false;
								if (dacp.closest != null) {
									conditionOne = ((dacp.closest.lng <= Math.max(markerOne.getLongitude(), markerTwo.getLongitude()) && dacp.closest.lng >= Math.min(markerOne.getLongitude(), markerTwo.getLongitude())) ||
											(dacp.closest.lat <= Math.max(markerOne.getLatitude(), markerTwo.getLatitude()) && dacp.closest.lat >= Math.min(markerOne.getLatitude(), markerTwo.getLatitude())));
								}
								// Distance from position to one (at least) extremity within the minimumDistance
								double distToOne = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(markerOne.getLatitude()), Math.toRadians(markerOne.getLongitude()))) * 60.0;
								double distToTwo = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(markerTwo.getLatitude()), Math.toRadians(markerTwo.getLongitude()))) * 60.0;
								boolean conditionTwo = (distToOne < this.minimumDistance || distToTwo < this.minimumDistance);

								boolean conditionThree = false;
								if (dacp.closest != null) {
									double distToClosest = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(dacp.closest.lat), Math.toRadians(dacp.closest.lng))) * 60.0;
									conditionThree = (distToClosest < this.minimumDistance);
								}
								// TODO? More condition could be added ?...

								if (conditionThree && conditionOne /* && conditionTwo */) {
									if (VERBOSE) {
										System.out.printf("\t>>> Border [%s], seg #%d, Detection granted (one: %f, two: %f).\n", name, seg, distToOne, distToTwo);
									}
									threatDetected = true;
									threatMessages.add(String.format("Threat detected at %s on border [%s], segment #%d, %f nm", utcDate, name, (seg + 1), segDist));
									threats.add(new BorderThreat().borderName(name).date(utcDate.getDate()).segmentIdx(seg + 1).dist(segDist).minDist(this.minimumDistance));
							    } else {
								    if (VERBOSE) {
									  System.out.printf("\t>>> Border [%s], seg #%d, Step Detection refused (one: %f, two: %f).\n", name, seg, distToOne, distToTwo);
								    }
							    }
							}
						}
					}

					// Threats in the cache
					cache.put(NMEADataCache.BORDERS_THREATS, threats);

					if (threatDetected) {
						String warningText = threatMessages.stream().collect(Collectors.joining("\n"));
						if (this.verbose) {
							System.out.println(warningText);
						}
						// TODO Honk! Define a callback Consumer<String> (see 'speak' below), or just a signal (sent to a buzzer, a light, whatever).
						if (collisionCallback != null) {
							// A test
							String messageToSpeak = warningText; // "Honk honk!!"; // With more data
							collisionCallback.accept(messageToSpeak);
							// TextToSpeech.speak(messageToSpeak);
						}
					}
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing Border data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(DEFAULT_MINIMUM_DISTANCE)));
		this.verbose = "true".equals(props.getProperty("verbose"));
		String callback = props.getProperty("border.threat.callback");
		if (callback != null) {
			if (callback.equals("default")) {
				this.collisionCallback = defaultCallback;
			} else {
				// Like Speaking callback, honk callback, led (blinking light) callback, etc...
				// The speaking callback might be a little too verbose...
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
			System.out.println(String.format("Border Computer %s:\n\tVerbose: %s\n\tMinimum Distance: %.02f",
					this.getClass().getName(),
					this.verbose,
					this.minimumDistance));
		}
	}

	public static class BorderComputerBean {
		private String cls;
		private final String type = "border-computer";
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

		public BorderComputerBean() {}  // This is for Jackson
		public BorderComputerBean(BorderManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new BorderComputerBean(this);
	}

	// For threat detection tests, debug.
	public static void main(String... args) {
		/*
		From N 47°38.73' / W 3°26.69' -> 47.645500° / - 3.444833°
		     N 47°38.85' / W 3°26.16' -> 47.645833° / - 3.436000°

	   - rank: 1
         latitude: 47.64561482699582
         longitude: -3.4445571899414067
       - rank: 2
         latitude: 47.643764382716846
         longitude: -3.4296226501464844
       - rank: 3
         latitude: 47.6338171216314
         longitude: -3.41451644897461
		 */
		final double MIN_DIST = 0.25; // in nm
		GeoPos position = new GeoPos(47.645833, -3.436000);
//		GeoPos position = new GeoPos(47.645500, -3.444833);
		Marker markerOne = new Marker(47.64561482699582, -3.4445571899414067, "-", "default");
		Marker markerTwo = new Marker(47.643764382716846, -3.4296226501464844, "-", "default");

		final DistAndClosestPoint dacp = getProximity(position, markerOne, markerTwo);
		double distInNm = dacp.dist;

		System.out.printf("Dist to segment: %f nm\n", distInNm);
		if (distInNm < MIN_DIST) {
			// Position between segment extremities - in lat, in lng
//			boolean conditionOne = ((position.lng <= Math.max(markerOne.getLongitude(), markerTwo.getLongitude()) && position.lng >= Math.min(markerOne.getLongitude(), markerTwo.getLongitude())) ||
//					(position.lat <= Math.max(markerOne.getLatitude(), markerTwo.getLatitude()) && position.lat >= Math.min(markerOne.getLatitude(), markerTwo.getLatitude())));
			// Closest point IN the segment !
			boolean conditionOne = ((dacp.closest.lng <= Math.max(markerOne.getLongitude(), markerTwo.getLongitude()) && dacp.closest.lng >= Math.min(markerOne.getLongitude(), markerTwo.getLongitude())) ||
					(dacp.closest.lat <= Math.max(markerOne.getLatitude(), markerTwo.getLatitude()) && dacp.closest.lat >= Math.min(markerOne.getLatitude(), markerTwo.getLatitude())));

			// Distance from position to one (at least) extremity within the minimumDistance
			double distToOne = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(markerOne.getLatitude()), Math.toRadians(markerOne.getLongitude()))) * 60.0;
			double distToTwo = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(markerTwo.getLatitude()), Math.toRadians(markerTwo.getLongitude()))) * 60.0;
			boolean conditionTwo = (distToOne < MIN_DIST || distToTwo < MIN_DIST); // Would not work, if in the middle of a looong segment.

			double distToClosest = new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)).gcDistanceBetween(new GreatCirclePoint(Math.toRadians(dacp.closest.lat), Math.toRadians(dacp.closest.lng))) * 60.0;
			boolean conditionThree = (distToClosest < MIN_DIST);

			if (conditionThree && conditionOne /* && conditionTwo */) {
				System.out.printf("\t>>> Detection granted\n");
				String.format("Threat detected\n");
			} else {
					System.out.printf("\t>>> Detection refused\n");
			}
		} else {
			System.out.println("No threat.");
		}
	}
}

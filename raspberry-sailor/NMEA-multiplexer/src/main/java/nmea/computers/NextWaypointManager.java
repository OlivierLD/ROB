package nmea.computers;

import algebra.SquareMatrix;
import algebra.SystemUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.parser.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Waypoint Manager. WiP.
 * <br/>
 * RMB (equivalent) Sentence will end up in the cache.
 * <br/>
 * To be used as a custom computer:
 * <pre>
 * computers:
 *   - class: nmea.computers.NextWaypointManager
 *     properties: Maybe ? TBD
 * </pre>
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '."rmb"' ?
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class NextWaypointManager extends Computer {

	final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));

	public NextWaypointManager(Multiplexer mux) {
		super(mux);
	}

	/**
	 * Wait for position data (not cached), get the next waypoint from the cache,
	 * and computes RMB data.
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

				Marker nextWayPoint = (Marker) cache.get(NMEADataCache.NEXT_WAYPOINT);

				if (position != null && nextWayPoint != null) {
					// Then compute. Great Circle
					double gcDistInNM = GreatCircle.getGCDistance(
							new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)),
							new GreatCirclePoint(Math.toRadians(nextWayPoint.getLatitude()), Math.toRadians(nextWayPoint.getLongitude()))
					);
//					double initialRouteAngle = GreatCircle.getInitialRouteAngleInDegrees(
//							new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)),
//							new GreatCirclePoint(Math.toRadians(nextWayPoint.getLatitude()), Math.toRadians(nextWayPoint.getLongitude()))
//					);
					GreatCircle gc = new GreatCircle();
					int brg;
					gc.setStart(new GreatCirclePoint(position.lat, position.lng));
					gc.setArrival(new GreatCirclePoint(nextWayPoint.getLatitude(), nextWayPoint.getLongitude()));
					double rlZ = gc.calculateRhumbLineRoute_degrees();
					brg = (int) Math.round(Math.toDegrees(rlZ));

					double dist = gcDistInNM;
					double route = brg; // initialRouteAngle;

					if (this.verbose) {
						System.out.printf("To WP %s, Found %.02f nm, in %.01f\272\n", nextWayPoint.getId(), dist, route);
					}

					// Build Data, put in cache
					cache.put(NMEADataCache.TO_WP, nextWayPoint.getId());
					cache.put(NMEADataCache.D2WP, new Distance(dist));
					cache.put(NMEADataCache.B2WP, new Angle360(route));

				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing WayPoints data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;
		this.verbose = "true".equals(props.getProperty("verbose"));
	}

	public static class WaypointComputerBean {
		private String cls;
		private final String type = "waypoint-computer";
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

		public WaypointComputerBean() {}  // This is for Jackson
		public WaypointComputerBean(NextWaypointManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
			this.active = instance.isActive();
		}
	}

	@Override
	public Object getBean() {
		return new WaypointComputerBean(this);
	}

	// For threat detection tests, debug.
	public static void main(String... args) {
		/*
		From N 47.677667 / -3.135667
		     N 47.677667 / -70.0
		*/
		GeoPos position = new GeoPos(47.677667, -3.135667); // From
		GeoPos wp       = new GeoPos(47.677667, -70.0);     // To

		double gcDistInNM = GreatCircle.getGCDistance(
				new GreatCirclePoint(Math.toRadians(position.lat), Math.toRadians(position.lng)),
				new GreatCirclePoint(Math.toRadians(wp.lat), Math.toRadians(wp.lng))
		);
//		double initialRouteAngle = GreatCircle.getInitialRouteAngleInDegrees(
//				new GreatCirclePoint(position.lat, position.lng),
//				new GreatCirclePoint(wp.lat, wp.lng)
//		);
		GreatCircle gc = new GreatCircle();
		int brg;
		gc.setStart(new GreatCirclePoint(position.lat, position.lng));
		gc.setArrival(new GreatCirclePoint(wp.lat, wp.lng));
		double rlZ = gc.calculateRhumbLineRoute_degrees();
		brg = (int) Math.round(Math.toDegrees(rlZ));

		double dist = gcDistInNM;
		double route = brg; // initialRouteAngle;

		System.out.printf("To WP, Found %.02f nm, in %.01f\272\n", dist, route);
	}
}
package util;

import calc.GeoPoint;
import calc.GreatCirclePoint;
import calc.GeomUtil;

public final class MercatorUtil {

	public MercatorUtil() {
	}

	/**
	 * Computes the Increasing Latitude. Mercator formula.
	 * Inc Lat = log(tan(PI / 4) + (lat / 2))
	 *
	 * @param lat in degrees
	 * @return Increasing Latitude, in degrees.
	 */
	public static double getIncLat(double lat) {
//		double angle = (Math.PI / 4D) + (Math.toRadians(lat) / 2D);
//		double tan = Math.tan(angle);
		double il = Math.log(Math.tan((Math.PI / 4D) + (Math.toRadians(lat) / 2D))); // Neperian log.
		return Math.toDegrees(il);
	}

	public static double getInvIncLat(double il) {
		double ret = Math.toRadians(il);
		ret = Math.exp(ret);
		ret = Math.atan(ret);
		ret -= (Math.PI / 4d); // 0.78539816339744828D;
		ret *= 2;
		ret = Math.toDegrees(ret);
		return ret;
	}

	public static GreatCirclePoint deadReckoning(GreatCirclePoint p, double d, double r) {
		return  new GreatCirclePoint(GeomUtil.deadReckoning(p.getL(), p.getG(), d, r));
	}

	/**
	 * @param l From latitude (in degrees)
	 * @param g From Longitude (in degrees)
	 * @param d	Distance (in nm)
	 * @param r	Heading (in degrees)
	 * @return the new position
	 */
	public static GreatCirclePoint deadReckoning(double l, double g, double d, double r) {
		return  new GreatCirclePoint(GeomUtil.deadReckoning(l, g, d, r));
	}

	// Ratio on *one* degree, that is the trick.
	public static double getIncLatRatio(double lat) {
		if (lat == 0d) {
			return 1d;
		} else {
			double bottom = lat - 1d;
			if (bottom < 0d) {
				bottom = 0d;
			}
			return ((lat - bottom) / (getIncLat(lat) - getIncLat(bottom)));
		}
	}

	// For tests
	public static void main(String[] args) {
		double fromLat = 47.677667;
		double fromLng = -3.135667;
		double dist = 10;
		double cog = 90;
		final GreatCirclePoint greatCirclePoint = deadReckoning(fromLat, fromLng, dist, cog);
		System.out.printf("New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePoint);

		double incLat = getIncLat(fromLat);
		System.out.printf("For lat %s, inc lat = %s\n",
				GeomUtil.decToSex(fromLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN),
				GeomUtil.decToSex(incLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN));

		fromLat = 45d;
		incLat = getIncLat(fromLat);
		System.out.printf("For lat %s, inc lat = %s\n",
				GeomUtil.decToSex(fromLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN),
				GeomUtil.decToSex(incLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN));

		fromLat = 0d;
		incLat = getIncLat(fromLat);
		System.out.printf("For lat %s, inc lat = %s\n",
				GeomUtil.decToSex(fromLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN),
				GeomUtil.decToSex(incLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN));


		double tan = Math.tan(
				(Math.PI / 4D) +
				(Math.toRadians(fromLat) / 2D)
		);
		double il = Math.log(tan); // Neperian log
		System.out.printf("For Lat %s, Tan = %f, IncLat, log(tan) = %f Rad, %s\n",
				GeomUtil.decToSex(fromLat, GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN),
				tan, il, GeomUtil.decToSex(Math.toDegrees(il), GeomUtil.UNICODE, GeomUtil.NS, GeomUtil.LEADING_SIGN));
	}
}

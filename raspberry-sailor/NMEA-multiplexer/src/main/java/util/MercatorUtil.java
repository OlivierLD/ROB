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
		return new GreatCirclePoint(GeomUtil.deadReckoning(p.getL(), p.getG(), d, r));
	}

	/**
	 * @param l From latitude (in degrees)
	 * @param g From Longitude (in degrees)
	 * @param d	Distance (in nm)
	 * @param r	Heading (in degrees)
	 * @return the new position
	 */
	public static GreatCirclePoint deadReckoning(double l, double g, double d, double r) {
		return new GreatCirclePoint(GeomUtil.deadReckoning(l, g, d, r));
	}

	/**
	 * @Deprecated, formula is not right. See #GeomUtil.deadReckoning. DO NOT USE !
	 *
	 * @param l From latitude (in degrees)
	 * @param g From Longitude (in degrees)
	 * @param d	Distance (in nm)
	 * @param r	Heading (in degrees)
	 * @return the new position
	 */
	public static GreatCirclePoint deadReckoningV1(double l, double g, double d, double r) {
		double deltaL = (d / 60D) * Math.cos(Math.toRadians(r));
		double l2 = l + deltaL;
		double lc1 = getIncLat(l);
		double lc2 = getIncLat(l2);
		double deltaLc = lc2 - lc1;
		// double deltaG = deltaLc * Math.tan(Math.toRadians(r));
//		double deltaLc = lc2 - lc1;
//		double deltaG = deltaLc * Math.tan(Math.toRadians(r));
		double deltaG = (d / 60d) * Math.sin(Math.toRadians(r));
		double g2 = g + deltaG;
		return new GreatCirclePoint(l2, g2);
	}

	// same as in JS, not too bad...
	public static GreatCirclePoint deadReckoningJS(double l, double g, double d, double r) {
		double radianDistance = Math.toRadians(d / 60d);
		double finalLat = (Math.asin((Math.sin(Math.toRadians(l)) * Math.cos(radianDistance)) +
				(Math.cos(Math.toRadians(l)) * Math.sin(radianDistance) * Math.cos(Math.toRadians(r)))));
		double finalLng = Math.toRadians(g) + Math.atan2(Math.sin(Math.toRadians(r)) * Math.sin(radianDistance) * Math.cos(Math.toRadians(l)),
				Math.cos(radianDistance) - Math.sin(Math.toRadians(l)) * Math.sin(finalLat));
		finalLat = Math.toDegrees(finalLat);
		finalLng = Math.toDegrees(finalLng);
		return new GreatCirclePoint(finalLat, finalLng);
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
		GreatCirclePoint greatCirclePoint = deadReckoning(fromLat, fromLng, dist, cog);
		System.out.printf("New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePoint);

		GreatCirclePoint greatCirclePointV1 = deadReckoningV1(fromLat, fromLng, dist, cog);
		System.out.printf("V1: New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePointV1);

		GreatCirclePoint greatCirclePointJS = deadReckoningJS(fromLat, fromLng, dist, cog);
		System.out.printf("JS: New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePointJS);

		cog = 45;
		greatCirclePoint = deadReckoning(fromLat, fromLng, dist, cog);
		System.out.printf("New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePoint);

		greatCirclePointV1 = deadReckoningV1(fromLat, fromLng, dist, cog);
		System.out.printf("V1: New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePointV1);

		greatCirclePointJS = deadReckoningJS(fromLat, fromLng, dist, cog);
		System.out.printf("JS: New pos, %.02f nm in the %.01f: %s\n", dist, cog, greatCirclePointJS);

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

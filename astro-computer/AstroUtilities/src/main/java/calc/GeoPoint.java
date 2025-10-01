package calc;

import java.io.Serializable;

public final class GeoPoint
		implements Serializable, Cloneable {
	double latitude;
	double longitude;

	public GeoPoint() {}

	public GeoPoint(double l, double g) {
		latitude = l;
		longitude = g;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

//	public double getL() {
//		return latitude;
//	}
//
//	public double getG() {
//		return longitude;
//	}
//
//	public void setL(double l) {
//		latitude = l;
//	}
//
//	public void setG(double g) {
//		longitude = g;
//	}

	public boolean equals(GeoPoint p) {
		String g = GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW);
		String gp = GeomUtil.decToSex(p.getLongitude(), GeomUtil.SHELL, GeomUtil.EW);
		String l = GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS);
		String lp = GeomUtil.decToSex(p.getLatitude(), GeomUtil.SHELL, GeomUtil.NS);
		return g.equals(gp) && l.equals(lp);
	}

	/**
	 * In nautical miles
	 *
	 * @param target target point
	 * @return distance in nm
	 */
	public double orthoDistanceBetween(GeoPoint target) {
		GreatCircle gc = new GreatCircle();
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getLatitude()), Math.toRadians(this.getLongitude())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getLatitude()), Math.toRadians(target.getLongitude())));
		gc.calculateGreatCircle(1);
		double d = Math.toDegrees(gc.getDistance());
		return d * 60D;
	}

	/**
	 * In nautical miles
	 *
	 * @param target target point
	 * @return distance in degrees
	 */
	public double gcDistanceBetween(GeoPoint target) {
		GreatCirclePoint from = new GreatCirclePoint(Math.toRadians(this.getLatitude()), Math.toRadians(this.getLongitude()));
		GreatCirclePoint to = new GreatCirclePoint(Math.toRadians(target.getLatitude()), Math.toRadians(target.getLongitude()));
		return GreatCircle.getGCDistanceInDegrees(from, to);
	}

	/**
	 * AKA Rhumbline. In nautical miles
	 *
	 * @param target target point
	 * @return distance
	 */
	public double loxoDistanceBetween(GeoPoint target) {
		GreatCircle gc = new GreatCircle();
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getLatitude()), Math.toRadians(this.getLongitude())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getLatitude()), Math.toRadians(target.getLongitude())));
		GreatCircle.RLData rlData = gc.calculateRhumbLine();
		return rlData.getdLoxo();
	}

	@Override
	public String toString() {
		// System.out.printf("GraphicsEnvironment.isHeadless(): %s\n", GraphicsEnvironment.isHeadless());
		// GraphicsEnvironment.isHeadless() is not enough to know what degree symbol to display... Swing Component, or console ?
		int degreeDisplayOption = // GraphicsEnvironment.isHeadless() ? GeomUtil.SHELL : GeomUtil.SWING;
								  GeomUtil.SHELL;
		return this.toString(degreeDisplayOption);
	}

	public String toString(int degreeDisplayOption) {
		return String.format("%s / %s",
				GeomUtil.decToSex(this.latitude, degreeDisplayOption, GeomUtil.NS),
				GeomUtil.decToSex(this.longitude, degreeDisplayOption, GeomUtil.EW));
	}

	public String toNumericalString() {
		return String.format("%f / %f", this.latitude, this.longitude);
	}

	public GeoPoint degreesToRadians() {
		return new GeoPoint(Math.toRadians(this.getLatitude()), Math.toRadians(this.getLongitude()));
	}

	public GeoPoint radiansToDegrees() {
		return new GeoPoint(Math.toDegrees(this.getLatitude()), Math.toDegrees(this.getLongitude()));
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("Something impossible just happened");
		}
	}
}
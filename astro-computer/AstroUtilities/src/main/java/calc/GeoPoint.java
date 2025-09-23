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

	public double getL() {
		return latitude;
	}

	public double getG() {
		return longitude;
	}

	public void setL(double l) {
		latitude = l;
	}

	public void setG(double g) {
		longitude = g;
	}

	public boolean equals(GeoPoint p) {
		String g = GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW);
		String gp = GeomUtil.decToSex(p.getG(), GeomUtil.SHELL, GeomUtil.EW);
		String l = GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS);
		String lp = GeomUtil.decToSex(p.getL(), GeomUtil.SHELL, GeomUtil.NS);
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
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getL()), Math.toRadians(target.getG())));
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
		GreatCirclePoint from = new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG()));
		GreatCirclePoint to = new GreatCirclePoint(Math.toRadians(target.getL()), Math.toRadians(target.getG()));
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
		gc.setStart(new GreatCirclePoint(Math.toRadians(this.getL()), Math.toRadians(this.getG())));
		gc.setArrival(new GreatCirclePoint(Math.toRadians(target.getL()), Math.toRadians(target.getG())));
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

	public GeoPoint degreesToRadians() {
		return new GeoPoint(Math.toRadians(this.getL()), Math.toRadians(this.getG()));
	}

	public GeoPoint radiansToDegrees() {
		return new GeoPoint(Math.toDegrees(this.getL()), Math.toDegrees(this.getG()));
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
package gribprocessing.utils;

import calc.GeoPoint;

/**
 * Designed to minimize the size of the chain of ancestors
 * TODO See how/if the bsp, date, hdg, tws, twd, twa are used or required in an isochron.
 */
public class RoutingIsochronPoint {

	private GeoPoint ancestor = null;
	private GeoPoint position = null;

	public RoutingIsochronPoint(GeoPoint pos) {
		this.position = pos;
	}

	public GeoPoint getAncestor() {
		return ancestor;
	}

	public void setAncestor(GeoPoint ip) {
		ancestor = ip;
	}

	public void setPosition(GeoPoint position) {
		this.position = position;
	}

	public GeoPoint getPosition() {
		return position;
	}
}

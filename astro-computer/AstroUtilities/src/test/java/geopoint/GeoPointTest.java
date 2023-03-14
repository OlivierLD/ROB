package geopoint;

import calc.GeoPoint;
import calc.GeomUtil;

public class GeoPointTest {
    public static void main(String... args) {
        GeoPoint pt = new GeoPoint(47.677667, -3.135667);
        System.out.printf("Pos is: %s\n", pt.toString(GeomUtil.SHELL));
    }
}

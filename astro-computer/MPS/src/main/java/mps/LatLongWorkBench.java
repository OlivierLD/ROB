package mps;

import calc.GeoPoint;
import calc.GeomUtil;

/**
 * See <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a> and/or
 * <a href="https://fr.wikipedia.org/wiki/Formule_de_haversine">Formule de Haversine</a>
 *
 * See also <a href="https://github.com/search5/latlon">LatLon</a>, it's in Python
 *
 * This code is here to make sure we find the same results as the ones found with <a href="https://github.com/search5/latlon">LatLon</a>.
 */
public class LatLongWorkBench {

    public static void main(String[] args) {

        GeoPoint honolulu = new GeoPoint(21.3, -157.8167);
        GeoPoint palmyra = new GeoPoint(5.8833, -162.0833);

        GeoPoint belz = new GeoPoint(47.677667, -3.135667);

        double distInKm = 8000;

        final GeoPoint arriving = GeomUtil.haversineInv(belz, distInKm / 1.852, 8.80);

        System.out.printf("Arriving %s\n", arriving.toString()); // Wahaha !

    }

}
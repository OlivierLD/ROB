package astro.plandessommets;

import calc.GeoPoint;
import calc.GeomUtil;

/**
 * See https://en.wikipedia.org/wiki/Haversine_formula and/or https://fr.wikipedia.org/wiki/Formule_de_haversine
 * See also https://github.com/search5/latlon it's in Python
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
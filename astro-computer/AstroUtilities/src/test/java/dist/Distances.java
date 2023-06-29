package dist;

import calc.GeomUtil;

public class Distances {

    public static void main(String... args) {

        // HaverSines
        double pointOneLat = 47.6600691664467;
        double pointOneLng = -3.2113552093505864;
        double pointTwoLat = 47.65532858402682;
        double pointTwoLng = -3.2104969024658208;
        double dToMarkKm = GeomUtil.haversineKm(pointOneLat, pointOneLng, pointTwoLat, pointTwoLng);
        double dToMarkNm = GeomUtil.haversineNm(pointOneLat, pointOneLng, pointTwoLat, pointTwoLng);

        System.out.printf("From [%f / %f] to [%f / %f], dist: %f km\n", pointOneLat, pointOneLng, pointTwoLat, pointTwoLng, dToMarkKm);
        System.out.printf("From [%f / %f] to [%f / %f], dist: %f nm\n", pointOneLat, pointOneLng, pointTwoLat, pointTwoLng, dToMarkNm);

        System.out.printf("Dist ratio: %f\n", (dToMarkKm / dToMarkNm));

        // -----------------------------------

        // Distance to a segment
        double extPointLat = 47.659;
        double extPointLng = -3.2106;
        // Equation de la droite [markerOne, markerTwo], y = ax + b
        double coeffA = (pointTwoLng == pointOneLng) ? 0.0 :
                (pointTwoLat - pointOneLat) / (pointTwoLng - pointOneLng);
        // markerOne.lat = (coeffA * markerOne.lng) + b
        // => b = markerOne.lat - (coeffA * markerOne.lng)
        double coeffB = pointOneLat - (coeffA * pointOneLng);

        // distance from position to the segment
        double distToSegment = Math.abs((coeffA * extPointLng) - extPointLat + coeffB) / (Math.sqrt((coeffA * coeffA) + 1)); // in degrees
        System.out.printf("(1) Dist to segment: %f deg, %f nm\n", distToSegment, distToSegment * 60.0);

        // From https://www.alloprof.qc.ca/fr/eleves/bv/mathematiques/la-distance-d-un-point-a-une-droite-dans-un-plan-m1315,
        // For verification
        coeffA = 3;
        coeffB = -4;
        extPointLng = 4;
        extPointLat = -1;
        distToSegment = Math.abs((coeffA * extPointLng) - extPointLat + coeffB) / (Math.sqrt((coeffA * coeffA) + 1)); // in degrees
        System.out.printf("(2) Dist to segment: %f units.\n", distToSegment);
    }
}

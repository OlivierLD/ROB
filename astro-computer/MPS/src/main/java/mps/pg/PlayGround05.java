package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import mps.MPSToolBox;

import java.util.*;

/**
 * Full stuff, implemented in a subroutine.
 * Start from the cones, and find the position.
 */
public class PlayGround05 {

    public static void main(String... args) {

        // Get to the body's coordinates at the given time
        AstroComputerV2 ac = new AstroComputerV2();
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now

        // A Test. The Sun, 2025-AUG-20 10:40:31 UTC, from 47º40.66'N / 3º08.14'W
        // We start from a known position to evaluate the observed altitude... (a trick)
        double userLatitude = 47.677667d;
        double userLongitude = -3.135667d;

        int year = 2025;
        int month = Calendar.AUGUST; //  7; // Aug
        int day = 20;
        int hours = 10;
        int minutes = 40;
        int seconds = 31;

        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month); // Aug
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, hours); // and not just HOUR !!!!
        date.set(Calendar.MINUTE,minutes);
        date.set(Calendar.SECOND, seconds);

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        // This could also come from the Almanacs
        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        CelestialDeadReckoning dr = MPSToolBox.calculateDR(sunGHA, sunDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        // double z = dr.getZ();
        double sunObsAlt = he; // Or from the sextant

        // This could also come from the Almanacs
        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();
        dr = MPSToolBox.calculateDR(moonGHA, moonDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        // double z = dr.getZ();
        double moonObsAlt = he; // Or from the sextant

        // Now, find the intersection of the two cones...
        List<GeoPoint> closests = resolve2Cones(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl,
                                               ac.getCalculationDateTime().getTime(), moonObsAlt, moonGHA, moonDecl,
                                       4, false);

        if (closests != null) {
            final double d = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            System.out.printf("Position between %s and %s, dist %.04f nm.\n", closests.get(0), closests.get(1), d);
        } else {
            System.out.println("Oops ! Not found...");
        }

        GeoPoint userPos = new GeoPoint(userLatitude, userLongitude);
        System.out.printf("-> For comparison, User pos is %s\n", userPos);
    }
    public static List<GeoPoint> resolve2Cones(Date firstTime, double firstObsAlt, double firstGHA, double firstDecl,
                                               Date secondTime, double secondObsAlt, double secondGHA, double secondDecl,
                                               int nbLoops, boolean verbose) {

        List<GeoPoint> result = null;

        double smallest = Double.MAX_VALUE;
        GeoPoint closestPointBody1 = null;
        GeoPoint closestPointBody2 = null;
        Double closestPointZBody1 = null;
        Double closestPointZBody2 = null;

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = 1d; // Will thus start with step = 0.1

        for (int loop=0; loop<nbLoops; loop++) {

            MPSToolBox.ConeDefinition coneBody1 = MPSToolBox.calculateCone(firstTime, firstObsAlt, firstGHA, firstDecl, "Body 1",
                    closestPointZBody1 == null ? fromZ : closestPointZBody1 - zStep,
                    closestPointZBody1 == null ? toZ : closestPointZBody1 + zStep,
                    zStep / 10d, false);
            MPSToolBox.ConeDefinition coneBody2 = MPSToolBox.calculateCone(secondTime, secondObsAlt, secondGHA, secondDecl, "Body 2",
                    closestPointZBody2 == null ? fromZ : closestPointZBody2 - zStep,
                    closestPointZBody2 == null ? toZ : closestPointZBody2 + zStep,
                    zStep / 10d, false);

            // Now, find the intersection of the two cones...
            for (MPSToolBox.ConePoint conePointBody1 : coneBody1.getCircle()) {
                for (MPSToolBox.ConePoint conePointBody2 : coneBody2.getCircle()) {
                    // GC distance from-to, use GeomUtil.haversineNm
                    double dist = GeomUtil.haversineNm(conePointBody1.getPoint(), conePointBody2.getPoint());
                    if (dist < smallest) {
                        smallest = dist;
                        closestPointBody1 = (GeoPoint) conePointBody1.getPoint().clone();
                        closestPointBody2 = (GeoPoint) conePointBody2.getPoint().clone();
                        closestPointZBody1 = conePointBody1.getZ();
                        closestPointZBody2 = conePointBody2.getZ();
                    }
                }
            }
            // End of loop #n
            if (verbose) {
                System.out.printf("Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                        loop + 1,
                        smallest,
                        closestPointZBody1,
                        closestPointBody1.toString(),
                        closestPointZBody2,
                        closestPointBody2.toString());
            }

            result = Arrays.asList(new GeoPoint[] {  // List.of not supported in Java8
                        closestPointBody1, closestPointBody2
                    });
            zStep /= 10.0; // For the next one
        }
        return result;
    }
}
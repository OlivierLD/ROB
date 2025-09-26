package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import mps.MPSToolBox;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Full stuff, Invoking method from MPSToolBox
 * Start from the cones, and find the position.
 * Observed Altitude, GHA and Decl calculated and sent from the main.
 */
public class PlayGround06 {

    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }
    public static void main(String... args) {

        // Date from = DURATION_FMT.parse(value);

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

        Date dateOne = ac.getCalculationDateTime().getTime();
        Date dateTwo = ac.getCalculationDateTime().getTime();

        System.out.println("------------------------------------------------");
        System.out.printf("Starting process with:\n" +
                          "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                          "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                          DURATION_FMT.format(dateOne),
                          GeomUtil.decToSex(sunObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                          GeomUtil.decToSex(sunGHA, GeomUtil.SHELL, GeomUtil.NONE),
                          GeomUtil.decToSex(sunDecl, GeomUtil.SHELL, GeomUtil.NS),
                          DURATION_FMT.format(dateTwo),
                          GeomUtil.decToSex(moonObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                          GeomUtil.decToSex(moonGHA, GeomUtil.SHELL, GeomUtil.NONE),
                          GeomUtil.decToSex(moonDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.println("------------------------------------------------");
        // Now, find the intersection of the two cones...
        List<GeoPoint> closests =
                MPSToolBox.resolve2Cones(dateOne, sunObsAlt, sunGHA, sunDecl,
                                         dateTwo, moonObsAlt, moonGHA, moonDecl,
                                         4, false, false);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            System.out.printf("1st : Position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);
            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.printf("2nd : Position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(3), d2);
        } else {
            System.out.println("Oops ! Not found...");
        }

        System.out.printf("-> For comparison, User pos is %s\n", new GeoPoint(userLatitude, userLongitude));
        System.out.println("------------------------------------------------");
    }
}
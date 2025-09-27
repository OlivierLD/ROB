package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import mps.MPSToolBox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Full stuff, Invoking method from MPSToolBox, for more than 2 bodies.
 * Start from the cones, and find the position.
 * GHA, Decl, and Observed Altitude are calculated in the main.
 * Observer's position is found at the end.
 */
public class PlayGround08 {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
     static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }
    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
     }

    /*
     * Point of reference, user's position
     * Used for Observed Altitude calculation
     * Should be the one found at the end.
     */
    static double userLatitude = 47.677667;
    static double userLongitude = -3.135667;

    public static void main(String... args) {

        AstroComputerV2 ac = new AstroComputerV2();

        /*
         * 26-Sep-2025 03:15:00 UTC
         */
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 2025);
        date.set(Calendar.MONTH, Calendar.SEPTEMBER);
        date.set(Calendar.DAY_OF_MONTH, 26);
        date.set(Calendar.HOUR_OF_DAY, 3); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 15);
        date.set(Calendar.SECOND, 0);

        System.out.printf("Calculation launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        double deltaT = ac.getDeltaT(); // Unused for now

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();

        final double saturnGHA = ac.getSaturnGHA();
        final double saturnDecl = ac.getSaturnDecl();

        final double jupiterGHA = ac.getJupiterGHA();
        final double jupiterDecl = ac.getJupiterDecl();

        // Star rigel = Star.getStar("Rigel"); // Case sensitive name
        ac.starPos("Rigel");
        final double rigelSHA = ac.getStarSHA("Rigel");
        final double rigelGHA = ac.getStarGHA("Rigel");
        final double rigelDecl = ac.getStarDec("Rigel");

        ac.starPos("Aldebaran");
        final double aldebaranSHA = ac.getStarSHA("Aldebaran");
        final double aldebaranGHA = ac.getStarGHA("Aldebaran");
        final double aldebaranDecl = ac.getStarDec("Aldebaran");

        CelestialDeadReckoning dr = MPSToolBox.calculateDR(sunGHA, sunDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double sunObsAlt = he; // Should be read (and corrected) from the sextant

        dr = MPSToolBox.calculateDR(moonGHA, moonDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        double moonObsAlt = he; // Should be read (and corrected) from the sextant

        dr = MPSToolBox.calculateDR(saturnGHA, saturnDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        double saturnObsAlt = he; // Should be read (and corrected) from the sextant

        dr = MPSToolBox.calculateDR(jupiterGHA, jupiterDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        double jupiterObsAlt = he; // Should be read (and corrected) from the sextant

        dr = MPSToolBox.calculateDR(rigelGHA, rigelDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        double rigelObsAlt = he; // Should be read (and corrected) from the sextant

        dr = MPSToolBox.calculateDR(aldebaranGHA, aldebaranDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        double aldebaranObsAlt = he; // Should be read (and corrected) from the sextant

        System.out.printf("Sun      :\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(sunObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(sunGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(sunDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.printf("Moon     :\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(moonObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(moonGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(moonDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.println("----------------------------------------------------");
        System.out.printf("Saturn   :\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(saturnObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(saturnGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(saturnDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.printf("Jupiter  :\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(jupiterObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(jupiterGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(jupiterDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.printf("Rigel    :\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(rigelObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(rigelGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(rigelDecl, GeomUtil.SHELL, GeomUtil.NS));
        System.out.printf("Aldebaran:\t ObsAlt: %s,\t GHA: %s,\t Decl: %s\n",
                GeomUtil.decToSex(aldebaranObsAlt, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(aldebaranGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(aldebaranDecl, GeomUtil.SHELL, GeomUtil.NS));

        List<MPSToolBox.ConesIntersection> conesIntersectionList = new ArrayList<>();

        double altOne = saturnObsAlt;
        double ghaOne = saturnGHA;
        double declOne = saturnDecl;
        Date dateOne = date.getTime();

        double altTwo = jupiterObsAlt;
        double ghaTwo = jupiterGHA;
        double declTwo = jupiterDecl;
        Date dateTwo = date.getTime();

        int nbIter = 4;
        boolean reverse = false;

        boolean verbose = false;

        if (verbose) {
            System.out.println("------------------------------------------------");
            System.out.printf("Starting resolve process with:\n" +
                            "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                            "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                    DURATION_FMT.format(dateOne),
                    GeomUtil.decToSex(altOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declOne, GeomUtil.SHELL, GeomUtil.NS),
                    DURATION_FMT.format(dateTwo),
                    GeomUtil.decToSex(altTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declTwo, GeomUtil.SHELL, GeomUtil.NS));
            System.out.println("------------------------------------------------");
        }

        // Now, find the intersection(s) of the two cones...
        List<GeoPoint> closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                dateTwo, altTwo, ghaTwo, declTwo,
                nbIter, reverse, verbose);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.println("Saturn & Jupiter");
            System.out.printf("After %d iterations:\n", nbIter);
            System.out.printf("1st position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);
            System.out.printf("2nd position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(2), d2);
            // For later
            conesIntersectionList.add(new MPSToolBox.ConesIntersection("Saturn", "Jupiter",
                    closests.get(0), closests.get(1),
                    closests.get(2), closests.get(3)));
        } else {
            System.out.println("Oops ! Not found...");
        }

        altOne = saturnObsAlt;
        ghaOne = saturnGHA;
        declOne = saturnDecl;

        altTwo = rigelObsAlt;
        ghaTwo = rigelGHA;
        declTwo = rigelDecl;

        if (verbose) {
            System.out.println("------------------------------------------------");
            System.out.printf("Starting resolve process with:\n" +
                            "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                            "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                    DURATION_FMT.format(dateOne),
                    GeomUtil.decToSex(altOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declOne, GeomUtil.SHELL, GeomUtil.NS),
                    DURATION_FMT.format(dateTwo),
                    GeomUtil.decToSex(altTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declTwo, GeomUtil.SHELL, GeomUtil.NS));
            System.out.println("------------------------------------------------");
        }

        // Now, find the intersection(s) of the two cones...
        closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                dateTwo, altTwo, ghaTwo, declTwo,
                nbIter, reverse, verbose);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.println("Saturn & Rigel");
            System.out.printf("After %d iterations:\n", nbIter);
            System.out.printf("1st position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);
            System.out.printf("2nd position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(2), d2);
            // For later
            conesIntersectionList.add(new MPSToolBox.ConesIntersection("Saturn", "Rigel",
                    closests.get(0), closests.get(1),
                    closests.get(2), closests.get(3)));
        } else {
            System.out.println("Oops ! Not found...");
        }

        altOne = rigelObsAlt;
        ghaOne = rigelGHA;
        declOne = rigelDecl;

        altTwo = jupiterObsAlt;
        ghaTwo = jupiterGHA;
        declTwo = jupiterDecl;

        if (verbose) {
            System.out.println("------------------------------------------------");
            System.out.printf("Starting resolve process with:\n" +
                            "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                            "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                    DURATION_FMT.format(dateOne),
                    GeomUtil.decToSex(altOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declOne, GeomUtil.SHELL, GeomUtil.NS),
                    DURATION_FMT.format(dateTwo),
                    GeomUtil.decToSex(altTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declTwo, GeomUtil.SHELL, GeomUtil.NS));
            System.out.println("------------------------------------------------");
        }

        // Now, find the intersection(s) of the two cones...
        closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                dateTwo, altTwo, ghaTwo, declTwo,
                nbIter, reverse, verbose);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.println("Rigel & Jupiter");
            System.out.printf("After %d iterations:\n", nbIter);
            System.out.printf("1st position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);
            System.out.printf("2nd position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(2), d2);
            // For later
            conesIntersectionList.add(new MPSToolBox.ConesIntersection("Rigel", "Jupiter",
                    closests.get(0), closests.get(1),
                    closests.get(2), closests.get(3)));
        } else {
            System.out.println("Oops ! Not found...");
        }

        altOne = rigelObsAlt;
        ghaOne = rigelGHA;
        declOne = rigelDecl;

        altTwo = aldebaranObsAlt;
        ghaTwo = aldebaranGHA;
        declTwo = aldebaranDecl;

        nbIter = 5;

        if (verbose) {
            System.out.println("------------------------------------------------");
            System.out.printf("Starting resolve process with:\n" +
                            "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                            "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                    DURATION_FMT.format(dateOne),
                    GeomUtil.decToSex(altOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaOne, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declOne, GeomUtil.SHELL, GeomUtil.NS),
                    DURATION_FMT.format(dateTwo),
                    GeomUtil.decToSex(altTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(ghaTwo, GeomUtil.SHELL, GeomUtil.NONE),
                    GeomUtil.decToSex(declTwo, GeomUtil.SHELL, GeomUtil.NS));
            System.out.println("------------------------------------------------");
        }

        // Now, find the intersection(s) of the two cones...
        closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                dateTwo, altTwo, ghaTwo, declTwo,
                nbIter, reverse, verbose);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.println("Rigel & Aldebaran");
            System.out.printf("After %d iterations:\n", nbIter);
            System.out.printf("1st position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);
            System.out.printf("2nd position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(2), d2);
            // For later
            conesIntersectionList.add(new MPSToolBox.ConesIntersection("Rigel", "Aldebaran",
                    closests.get(0), closests.get(1),
                    closests.get(2), closests.get(3)));
        } else {
            System.out.println("Oops ! Not found...");
        }

        // Now process all intersections...
        System.out.println("-----------------------------");
        System.out.printf("We have %d intersections to process:\n", conesIntersectionList.size());
        if (conesIntersectionList.size() > 1) {
            conesIntersectionList.forEach(ci -> System.out.printf("- Intersection between %s and %s\n", ci.getBodyOneName(), ci.getBodyTwoName()));
            MPSToolBox.ConesIntersection referenceIntersection = conesIntersectionList.get(0); // TODO Iterate on the reference as well ?...

            List<GeoPoint> candidates = new ArrayList<>();

            double criticalDist = 5.0; // TODO Fix that one !... Change it from 5 to 15, to see the impact.
            for (int i=1; i<conesIntersectionList.size(); i++) {
                MPSToolBox.ConesIntersection thatOne = conesIntersectionList.get(i);
                double distOneOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionOne());
                if (distOneOne < criticalDist) {
                    candidates.add(thatOne.getConeOneIntersectionOne());
                }
                double distOneTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionTwo());
                if (distOneTwo < criticalDist) {
                    candidates.add(thatOne.getConeOneIntersectionTwo());
                }
                double distTwoOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionOne());
                if (distTwoOne < criticalDist) {
                    candidates.add(thatOne.getConeOneIntersectionOne());
                }
                double distTwoTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionTwo());
                if (distTwoTwo < criticalDist) {
                    candidates.add(thatOne.getConeOneIntersectionTwo());
                }
            }
            // The result...
            System.out.println("-----------------------------");
            System.out.printf("%d candidate(s):\n", candidates.size());
            candidates.stream().forEach(pt -> {
                System.out.printf("\u2022 %s\n", pt);
            });
            System.out.println("-----------------------------");
            // An average ?
            double averageLat = candidates.stream().mapToDouble(p -> p.getLatitude()).average().getAsDouble();
            double averageLng = candidates.stream().mapToDouble(p -> p.getLongitude()).average().getAsDouble();
            GeoPoint avgPoint = new GeoPoint(averageLat, averageLng);
            System.out.printf("=> Average: %s\n", avgPoint);

            GeoPoint original = new GeoPoint(userLatitude, userLongitude);
            System.out.printf("=> Compare to original position: %s\n", original);

            System.out.printf("==> Difference/offset: %.02f nm\n", GeomUtil.haversineNm(original, avgPoint));

        } else {
            System.out.println("Not enough intersections to process...");
        }
        System.out.println("------- End of the story -------");
    }
}
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
 * Full stuff, Invoking method from MPSToolBox, for 2 bodies. Sun and Moo by default
 * Start from the cones, and find the position.
 * Observed Altitude, GHA and Decl can be received from the main (or calculated from it).
 * See input (CLI) parameters.
 */
public class PlayGround07 {

    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private final static String TIME_ONE = "--time-1:";
    private final static String GHA_ONE = "--gha-1:";
    private final static String DECL_ONE = "--decl-1:";
    private final static String ALT_ONE = "--alt-1:";
    private final static String TIME_TWO = "--time-2:";
    private final static String GHA_TWO = "--gha-2:";
    private final static String DECL_TWO = "--decl-2:";
    private final static String ALT_TWO = "--alt-2:";
    private final static String NB_ITERATIONS = "--nb-iter:";
    private final static String VERBOSE = "--verbose:";

    private static Double parseWithSign(String str) {
        return GeomUtil.sexToDec(str);
    }
    private static Double parseWithNoSign(String str) {
        String degrees = null;
        String minutes = null;
        if (str.contains(GeomUtil.DEGREE_SYMBOL)) {
            degrees = str.substring(0, str.indexOf(GeomUtil.DEGREE_SYMBOL));
            minutes = str.substring(str.indexOf(GeomUtil.DEGREE_SYMBOL) + 1);
        } else if (str.contains(GeomUtil.ALT_DEGREE_SYMBOL)) {
            degrees = str.substring(0, str.indexOf(GeomUtil.ALT_DEGREE_SYMBOL));
            minutes = str.substring(str.indexOf(GeomUtil.ALT_DEGREE_SYMBOL) + 1);
        } else if (str.contains(" ")) {
            degrees = str.substring(0, str.indexOf(" "));
            minutes = str.substring(str.indexOf(" ") + 1);
        }
        if (minutes != null && minutes.endsWith("'")) {
            minutes = minutes.substring(0, minutes.indexOf("'"));
        }
        Double d = GeomUtil.sexToDec(degrees, minutes);
        return d;
    }
    public static void main(String... args) {

        Date dateOne = null;
        Date dateTwo = null;
        Double ghaOne = null;
        Double declOne = null;
        Double altOne = null;
        Double ghaTwo = null;
        Double declTwo = null;
        Double altTwo = null;

        int nbIter = 4;
        boolean verbose = false;
        boolean cliPrmVerbose = "true".equals(System.getProperty("prm.verbose"));

        /*
         * CLI prms like
         * --time-1:2025-07-20T10:11:12 "--gha-1:339º17.40'" "--decl-1:N 12º16.80'" --time-2:2025-08-20T10:40:31 --alt-2:66º33.85' --gha-2:13º41.85' "--decl-2:N 25º46.13'"
         */
        if (args.length > 0) {
            for (String arg : args) {
                if (cliPrmVerbose) {
                    System.out.printf("Processing arg : [%s]\n", arg);
                }
                if (arg.startsWith(TIME_ONE)) {
                    String str = arg.substring(TIME_ONE.length()).trim();
                    try {
                        dateOne = DURATION_FMT.parse(str);
                    } catch (ParseException pe) {
                        pe.printStackTrace();
                    }
                } else if (arg.startsWith(GHA_ONE)) {
                    String str = arg.substring(GHA_ONE.length()).trim();
                    ghaOne = parseWithNoSign(str);
                    // System.out.printf("GHA 1: %.06f\n", ghaOne);
                } else if (arg.startsWith(DECL_ONE)) {
                    String str = arg.substring(DECL_ONE.length()).trim();
                    declOne = parseWithSign(str);
                    // System.out.printf("DECL 1: %.06f\n", declOne);
                } else if (arg.startsWith(ALT_ONE)) {
                    String str = arg.substring(ALT_ONE.length()).trim();
                    altOne = parseWithNoSign(str);
                    // System.out.printf("ALT 1: %.06f\n", altOne);
                } else if (arg.startsWith(TIME_TWO)) {
                    String str = arg.substring(TIME_TWO.length()).trim();
                    try {
                        dateTwo = DURATION_FMT.parse(str);
                    } catch (ParseException pe) {
                        pe.printStackTrace();
                    }
                } else if (arg.startsWith(GHA_TWO)) {
                    String str = arg.substring(GHA_TWO.length()).trim();
                    ghaTwo = parseWithNoSign(str);
                    // System.out.printf("GHA 2: %.06f\n", ghaTwo);
                } else if (arg.startsWith(DECL_TWO)) {
                    String str = arg.substring(DECL_TWO.length()).trim();
                    declTwo = parseWithSign(str);
                    // System.out.printf("DECL 2: %.06f\n", declTwo);
                } else if (arg.startsWith(ALT_TWO)) {
                    String str = arg.substring(ALT_TWO.length()).trim();
                    altTwo = parseWithNoSign(str);
                    // System.out.printf("ALT 2: %.06f\n", altTwo);
                } else if (arg.startsWith(NB_ITERATIONS)) {
                    String str = arg.substring(NB_ITERATIONS.length()).trim();
                    try {
                        nbIter = Integer.parseInt(str);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (arg.startsWith(VERBOSE)) {
                    String str = arg.substring(VERBOSE.length()).trim();
                    verbose = "true".equals(str);
                }
            }
        }

        if (dateOne == null || dateTwo == null ||
            ghaOne == null || declOne == null || altOne == null ||
            ghaTwo == null || declTwo == null || altTwo == null) {
            System.out.println("==> Note: Not enough values provided from CLI. Proceeding with default.");
            if (dateOne == null) {
                System.out.println("--date-1: is missing");
            }
            if (declOne == null) {
                System.out.println("--decl-1: is missing");
            }
            if (ghaOne == null) {
                System.out.println("--gha-1: is missing");
            }
            if (altOne == null) {
                System.out.println("--alt-1: is missing");
            }
            if (dateTwo == null) {
                System.out.println("--date-2: is missing");
            }
            if (declTwo == null) {
                System.out.println("--decl-2: is missing");
            }
            if (ghaTwo == null) {
                System.out.println("--gha-2: is missing");
            }
            if (altTwo == null) {
                System.out.println("--alt-2: is missing");
            }

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
            date.set(Calendar.MINUTE, minutes);
            date.set(Calendar.SECOND, seconds);

            ac.calculate(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    date.get(Calendar.MINUTE),
                    date.get(Calendar.SECOND),
                    true);

            // This could also come from the Almanacs
            ghaOne = ac.getSunGHA();
            declOne = ac.getSunDecl();

            CelestialDeadReckoning dr = MPSToolBox.calculateDR(ghaOne, declOne, userLatitude, userLongitude).calculate(); // All angles in degrees
            double he = dr.getHe();
            // double z = dr.getZ();
            altOne = he;

            // This could also come from the Almanacs
            ghaTwo = ac.getMoonGHA();
            declTwo = ac.getMoonDecl();
            dr = MPSToolBox.calculateDR(ghaTwo, declTwo, userLatitude, userLongitude).calculate(); // All angles in degrees
            he = dr.getHe();
            // double z = dr.getZ();
            altTwo = he; // Or from the sextant

            dateOne = ac.getCalculationDateTime().getTime();
            dateTwo = ac.getCalculationDateTime().getTime();
        } else {
            System.out.println("OK. Proceeding with user's input.");
        }

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

        List<MPSToolBox.ConesIntersection> conesIntersectionList = new ArrayList<>();

        // Now, find the intersection(s) of the two cones...
        List<GeoPoint> closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                                                           dateTwo, altTwo, ghaTwo, declTwo,
                                                           0.1d, nbIter, false, verbose);

        if (closests != null) {
            final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
            System.out.printf("1st Position between %s and %s, dist %.02f nm.\n", closests.get(0), closests.get(1), d1);

            final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
            System.out.printf("2nd Position between %s and %s, dist %.02f nm.\n", closests.get(2), closests.get(3), d2);

            conesIntersectionList.add(new MPSToolBox.ConesIntersection("BodyOne", "BodyTwo",
                    closests.get(0) , closests.get(1), closests.get(2), closests.get(3)));
        } else {
            System.out.println("Oops ! Not found...");
        }
        // System.out.printf("-> For comparison, User pos is %s\n", new GeoPoint(userLatitude, userLongitude));
    }
}
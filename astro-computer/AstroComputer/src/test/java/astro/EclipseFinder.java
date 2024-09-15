package astro;

import calc.calculation.AstroComputerV2;
import utils.TimeUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A test
 * Trying to find solar or lunar eclipses...
 */
public class EclipseFinder {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("EEE yyyy-MMM-dd HH:mm:ss 'UTC'");
    private final static SimpleDateFormat SDF_MONTH = new SimpleDateFormat("MMM yyyy");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private final static AstroComputerV2 astroComputerV2 = new AstroComputerV2();

    private final static int[] STEPS = { Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
    public enum Step {
        DAY(0),
        HOUR(1),
        MINUTE(2),
        SECOND(3);

        private final int index;

        Step(int i) {
            this.index = i;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum EclipseType {
        SOLAR,
        LUNAR
    }
    private final static boolean VERBOSE = false;

    private static double deltaGHA(double one, double two) {
        double diff = Math.abs(one - two);
        if (Math.abs(diff) > 180) {
            diff = Math.abs(diff - 360);
        }
        return Math.abs(diff);
    }

    private final static long SEC_PER_DAY = (24 * 3_600);
    private final static long MIN_PER_DAY = (24 * 60);
    private final static long HOUR_PER_DAY = (24);
    private final static double SUN_DIAM = 2 * 0.266667; // In degrees. And moon too...

    private static class MinimalConfig {
        private double deltaGHA;
        private double deltaDecl;
        private Calendar when;

        public MinimalConfig() {}

        public MinimalConfig(double deltaGHA, double deltaDecl) {
            this.deltaGHA = deltaGHA;
            this.deltaDecl = deltaDecl;
        }

        public double getDeltaGHA() {
            return deltaGHA;
        }

        public void setDeltaGHA(double deltaGHA) {
            this.deltaGHA = deltaGHA;
        }

        public double getDeltaDecl() {
            return deltaDecl;
        }

        public void setDeltaDecl(double deltaDecl) {
            this.deltaDecl = deltaDecl;
        }

        public Calendar getWhen() {
            return when;
        }

        public void setWhen(Calendar when) {
            this.when = (Calendar)((GregorianCalendar)when).clone();;
        }
    }
    private static void narrowSearch(Calendar startCal, EclipseType type) {
        final int delta = STEPS[Step.MINUTE.getIndex()];
        final long howLong = MIN_PER_DAY * 3; // Nb days. Depends on delta above

        Calendar localCal = (Calendar)((GregorianCalendar)startCal).clone();

        MinimalConfig minimalGHA  = new MinimalConfig(Double.MAX_VALUE, Double.MAX_VALUE);
        MinimalConfig minimalD = new MinimalConfig(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int i=0; i<howLong; i++) {
            // System.out.printf("Narrowing, now %s...\n", SDF_UTC.format(new Date(startCal.getTime().getTime())));
            astroComputerV2.calculate(
                    localCal.get(Calendar.YEAR),
                    localCal.get(Calendar.MONTH) + 1,
                    localCal.get(Calendar.DAY_OF_MONTH),
                    localCal.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    localCal.get(Calendar.MINUTE),
                    localCal.get(Calendar.SECOND));

            final double sunDecl = astroComputerV2.getSunDecl();
            final double moonDecl = astroComputerV2.getMoonDecl();
            final double sunGHA = astroComputerV2.getSunGHA();
            final double moonGHA = astroComputerV2.getMoonGHA();

            double deltaGHA = (type.equals(EclipseType.SOLAR)) ? deltaGHA(sunGHA, moonGHA) : deltaGHA(sunGHA, moonGHA + 180);
            double deltaDecl = (type.equals(EclipseType.SOLAR)) ? Math.abs(sunDecl - moonDecl) : Math.abs(sunDecl + moonDecl);

            if (deltaGHA < minimalGHA.getDeltaGHA()) {
                minimalGHA.setDeltaGHA(deltaGHA);
                minimalGHA.setDeltaDecl(deltaDecl);
                minimalGHA.setWhen(localCal);
            }
            if (deltaDecl < minimalD.getDeltaDecl()) {
                minimalD.setDeltaGHA(deltaGHA);
                minimalD.setDeltaDecl(deltaDecl);
                minimalD.setWhen(localCal);
            }
            // Delta GHA & Delta D below SUN_DIAM...
            if (deltaGHA < SUN_DIAM && deltaDecl < SUN_DIAM) {
                System.out.printf("\t\t--> GHA => \u03B4 GHA: %f, \u03B4 Decl: %f, At %s\n",
                        deltaGHA,
                        deltaDecl,
                        SDF_UTC.format(new Date(localCal.getTime().getTime())));
            }

            // Increment
            localCal.add(delta, 1);
        }
        System.out.printf("\tGHA => min \u03B4 GHA: %f, \u03B4 Decl: %f, At %s\n",  // U+0384: δ
                          minimalGHA.getDeltaGHA(),
                          minimalGHA.getDeltaDecl(),
                          SDF_UTC.format(new Date(minimalGHA.getWhen().getTime().getTime())));
        System.out.printf("\tDecl => \u03B4 GHA: %f, min \u03B4 Decl: %f, At %s\n",
                minimalD.getDeltaGHA(),
                minimalD.getDeltaDecl(),
                SDF_UTC.format(new Date(minimalD.getWhen().getTime().getTime())));
    }

    public static void main(String... args) {
        System.setProperty("deltaT", "AUTO");
//        System.setProperty("astro.verbose", "true");

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        int currentYear = date.get(Calendar.YEAR); // Or hard-code the date you want here
        System.out.printf("Year %d\n", currentYear);

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("Etc/UTC" /*"America/Los_Angeles"*/));
        cal.set(Calendar.YEAR, currentYear);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        System.out.println("Setting date to " + SDF_UTC.format(new Date(cal.getTime().getTime())) + ", let's go.");
        // For info...
        double deltaT = TimeUtil.getDeltaT(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
        System.out.printf("Will use DeltaT for [%04d-%02d]: %f\n", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, deltaT);

        long iterations = 0L;
        int stepIndex = Step.HOUR.getIndex(); // 2; // 3 is second; // Second, 0 is Day;... See above.
        long before = System.currentTimeMillis();
        while (cal.get(Calendar.YEAR) == currentYear) {
            iterations++;
            if (cal.get(Calendar.DAY_OF_MONTH) == 1 && cal.get(Calendar.HOUR_OF_DAY) == 0) {
                System.out.printf("- Now working on %s\n", SDF_MONTH.format(new Date(cal.getTime().getTime())));
            }
            astroComputerV2.calculate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND));

            final double sunDecl = astroComputerV2.getSunDecl();
            final double moonDecl = astroComputerV2.getMoonDecl();
            final double sunGHA = astroComputerV2.getSunGHA();
            final double moonGHA = astroComputerV2.getMoonGHA();
            double deltaGHA = deltaGHA(sunGHA, moonGHA);             // Solar
            double deltaGHA2 = deltaGHA(sunGHA, moonGHA + 180); // Lunar
            if (deltaGHA < 1) {
                // Check Declinations
                if (Math.abs(sunDecl - moonDecl) < 1.0) {
                    System.out.printf("%s, SOLAR eclipse (delta HA %f, delta D %f) ? Sun: D: %f, GHA: %f - Moon: D: %f, GHA: %f\n",
                            SDF_UTC.format(new Date(cal.getTime().getTime())),
                            deltaGHA, Math.abs(sunDecl - moonDecl),
                            sunDecl,
                            sunGHA,
                            moonDecl,
                            moonGHA);
                    // TODO Refine/narrow (Newton?) to sun/moon semi-diameters (16' = 0.266667°)
                    Calendar startCal = (Calendar)((GregorianCalendar)cal).clone();
                    startCal.add(Calendar.DAY_OF_MONTH, -1);
                    narrowSearch(startCal, EclipseType.SOLAR); // cal - 1 day
                }
            }
            if (deltaGHA2 < 1) {
                // Check Declinations
                if (Math.abs(sunDecl + moonDecl) < 1.0) {
                    System.out.printf("%s, LUNAR eclipse (delta HA %f, delta D %f) ? Sun: D: %f, GHA: %f - Moon: D: %f, GHA: %f\n",
                            SDF_UTC.format(new Date(cal.getTime().getTime())),
                            deltaGHA2, Math.abs(sunDecl + moonDecl),
                            sunDecl,
                            sunGHA,
                            moonDecl,
                            moonGHA);
                    // TODO Refine/narrow (Newton?) to sun/moon semi-diameters (16' = 0.266667°)
                    Calendar startCal = (Calendar)((GregorianCalendar)cal).clone();
                    startCal.add(Calendar.DAY_OF_MONTH, -1);
                    narrowSearch(startCal, EclipseType.LUNAR); // cal - 1 day
                }
            }
            if (VERBOSE) {
                System.out.println("\tDate is now " + SDF_UTC.format(new Date(cal.getTime().getTime())));
            }
            cal.add(STEPS[stepIndex], 1);
        }
        long after = System.currentTimeMillis();
        System.out.printf("Used %s iterations, in %s ms.\n",
                NumberFormat.getInstance().format(iterations),
                NumberFormat.getInstance().format(after - before));
    }
}

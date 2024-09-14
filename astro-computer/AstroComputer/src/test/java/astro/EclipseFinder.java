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
    private final static boolean VERBOSE = false;

    private static double deltaGHA(double one, double two) {
        double diff = Math.abs(one - two);
        if (Math.abs(diff) > 180) {
            diff = Math.abs(diff - 360);
        }
        return Math.abs(diff);
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
            double deltaGHA = deltaGHA(sunGHA, moonGHA);
            double deltaGHA2 = deltaGHA(sunGHA, moonGHA + 180);
            if (deltaGHA < 1) { // 0: Solar, 180, Lunar eclipse ?
                // Check Declinations
                if (Math.abs(sunDecl - moonDecl) < 1.0) {
                    // TODO Refine/narrow (Newton) to sun/moon semi-diameters
                    System.out.printf("%s, Solar eclipse (delta HA %f, delta D %f) ? Sun: D: %f, GHA: %f - Moon: D: %f, GHA: %f\n",
                            SDF_UTC.format(new Date(cal.getTime().getTime())),
                            deltaGHA, Math.abs(sunDecl - moonDecl),
                            sunDecl,
                            sunGHA,
                            moonDecl,
                            moonGHA);
                }
            }
            if (deltaGHA2 < 1) { // 0 or 180, solar or lunar eclipse
                // Check Declinations
                if (Math.abs(sunDecl + moonDecl) < 1.0) {
                    // TODO Refine/narrow (Newton) to sun/moon semi-diameters
                    System.out.printf("%s, Lunar eclipse (delta HA %f, delta D %f) ? Sun: D: %f, GHA: %f - Moon: D: %f, GHA: %f\n",
                            SDF_UTC.format(new Date(cal.getTime().getTime())),
                            deltaGHA2, Math.abs(sunDecl + moonDecl),
                            sunDecl,
                            sunGHA,
                            moonDecl,
                            moonGHA);
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

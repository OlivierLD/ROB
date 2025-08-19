package astro.plandessommets;

import calc.DeadReckoning;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Sun, Moon, Venus, Mars, Jupiter
 */
public class Context01 {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public static void main(String... args) {

        System.out.println("--------- S T A R T ----------");

        double latitude = 47.677667;
        double longitude = -3.135667;

        AstroComputerV2 ac = new AstroComputerV2();

        /*
         * 19-Aug-2025 12:58:25 UTC
         */
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 2025);
        date.set(Calendar.MONTH, 7); // Aug
        date.set(Calendar.DAY_OF_MONTH, 19);
        date.set(Calendar.HOUR_OF_DAY, 12); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 58);
        date.set(Calendar.SECOND, 25);

        System.out.printf("Calculation launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);
        double deltaT = ac.getDeltaT();

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();

        final double venusGHA = ac.getVenusGHA();
        final double venusDecl = ac.getVenusDecl();

        final double marsGHA = ac.getMarsGHA();
        final double marsDecl = ac.getMarsDecl();

        final double jupiterGHA = ac.getJupiterGHA();
        final double jupiterDecl = ac.getJupiterDecl();

        // Sun
        DeadReckoning dr = new DeadReckoning(sunGHA, sunDecl, latitude, longitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                        "Sun at GHA: %.04f\272, Decl: %.04f\272\n" +
                        "From position %s / %s\n" +
                        "Sun Obs Alt: %s - %.2f\272, Z: %.01f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                deltaT,
                sunGHA, sunDecl,
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                he,
                z);
        System.out.println();

        // Moon
        dr = new DeadReckoning(moonGHA, moonDecl, latitude, longitude).calculate(); // All angles in degrees
        he = dr.getHe();
        z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                          "Moon at GHA: %.04f\272, Decl: %.04f\272\n" +
                          "From position %s / %s\n" +
                          "Moon Obs Alt: %s - %.2f\272, Z: %.01f\272\n",
                            SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                            deltaT,
                            moonGHA, moonDecl,
                            GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                            GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                            GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            he,
                            z);
        System.out.println();

        // Venus
        dr = new DeadReckoning(venusGHA, venusDecl, latitude, longitude).calculate(); // All angles in degrees
        he = dr.getHe();
        z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                        "Venus at GHA: %.04f\272, Decl: %.04f\272\n" +
                        "From position %s / %s\n" +
                        "Venus Obs Alt: %s - %.2f\272, Z: %.01f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                deltaT,
                venusGHA, venusDecl,
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                he,
                z);
        System.out.println();

        // Mars
        dr = new DeadReckoning(marsGHA, marsDecl, latitude, longitude).calculate(); // All angles in degrees
        he = dr.getHe();
        z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                        "Mars at GHA: %.04f\272, Decl: %.04f\272\n" +
                        "From position %s / %s\n" +
                        "Mars Obs Alt: %s - %.2f\272, Z: %.01f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                deltaT,
                marsGHA, marsDecl,
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                he,
                z);
        System.out.println();

        // Jupiter
        dr = new DeadReckoning(jupiterGHA, jupiterDecl, latitude, longitude).calculate(); // All angles in degrees
        he = dr.getHe();
        z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                        "Jupiter at GHA: %.04f\272, Decl: %.04f\272\n" +
                        "From position %s / %s\n" +
                        "Jupiter Obs Alt: %s - %.2f\272, Z: %.01f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                deltaT,
                jupiterGHA, jupiterDecl,
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                he,
                z);
        System.out.println();

        System.out.println("----------- E N D ------------");
    }
}
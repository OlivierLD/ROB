package mps;

import calc.CelestialDeadReckoning;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * For Comparison with data used by Yves Robin-Jouan
 * Doc at https://www.aftopo.org/download.php?type=pdf&matricule=aHR0cHM6Ly93d3cuYWZ0b3BvLm9yZy93cC1jb250ZW50L3VwbG9hZHMvYXJ0aWNsZXMvcGRmL2FydGljbGUxNzYwNy5wZGY=
 * stored as plan.des.sommets.02.pdf
 * <br/>
 * There is a ~180&deg; difference with the GHAs... (see below in the code)
 * <br/>
 * WiP...
 */
public class ForComparison {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public static void main(String... args) {

        System.out.println("--------- S T A R T ----------");

        double latitude = GeomUtil.sexToDec("27", "39.13");
        double longitude = 0d;

        AstroComputerV2 ac = new AstroComputerV2();

        /*
         * Venus, 4-feb-1995, 23:40:31 UTC
         */
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 1995);
        date.set(Calendar.MONTH, 1); // Feb
        date.set(Calendar.DAY_OF_MONTH, 4);
        date.set(Calendar.HOUR_OF_DAY, 23); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 40);
        date.set(Calendar.SECOND, 31);

        System.out.printf("Calculation for Venus launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);
        double deltaT = ac.getDeltaT();

        double venusGHA = ac.getVenusGHA();
        final double venusDecl = ac.getVenusDecl();

        venusGHA = (180 + venusGHA) % 360; // Bizarre...

        CelestialDeadReckoning dr = new CelestialDeadReckoning(/*AstroComputerV2.ghaToLongitude*/(venusGHA), venusDecl, latitude, longitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s, (deltaT: %f s)\n" +
                          "Venus at GHA: %.04f\272 (Y.R-J: 39.7250\272), Decl: %.04f\272 (Y.R-J: -20.7950\272)\n" +
                          "From position %s / %s\n" +
                          "Venus Obs Alt: %s - %.2f\272 (Y.R-J: 28.9133\272), Z: %.01f\272\n",
                            SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                            deltaT,
                            venusGHA, venusDecl,
                            GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                            GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                            GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            he,
                            z);
        System.out.println();
        /*
         * Spica, 4-feb-1995, 23:29:15 UTC
         */
        // Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 1995);
        date.set(Calendar.MONTH, 1); // Feb
        date.set(Calendar.DAY_OF_MONTH, 4);
        date.set(Calendar.HOUR_OF_DAY, 23); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 29);
        date.set(Calendar.SECOND, 15);

        System.out.printf("Calculation for Spica launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);
        deltaT = ac.getDeltaT();

        // Star spica = Star.getStar("Spica"); // Case sensitive name
        ac.starPos("Spica");
        double spicaSHA = ac.getStarSHA("Spica");
        double spicaGHA = ac.getStarGHA("Spica");
        double spicaDEC = ac.getStarDec("Spica");

        // GHA star = SHA star + GHA aries
        // final double spicaGHA = ((360d - (15d * spica.getRa())) + ac.getAriesGHA()) % 360.0; // in [0, 360[
        // final double spicaDecl = spica.getDec();

        System.out.printf("- Spica SHA: %s, GHA: %s, Decl: %s (deltaT: %f s)\n",
                            GeomUtil.decToSex(spicaSHA, GeomUtil.SHELL, GeomUtil.NONE),
                            GeomUtil.decToSex(spicaGHA, GeomUtil.SHELL, GeomUtil.NONE),
                            GeomUtil.decToSex(spicaDEC, GeomUtil.SHELL, GeomUtil.NS),
                            deltaT);

        spicaGHA = (spicaGHA + 180) % 360; // Mmmmh.
        /*DeadReckoning*/ dr = new CelestialDeadReckoning(/*AstroComputerV2.ghaToLongitude*/(spicaGHA), spicaDEC, latitude, longitude).calculate(); // All angles in degrees
        he = dr.getHe();
        z = dr.getZ();

        System.out.printf("After Dead Reckoning: For %s,\n" +
                        "Spica at GHA: %.04f\272 (Y.R-J: 105.2350\272), Decl: %.04f\272 (Y.R-J: -11.1367\272) \n" +
                        "From position %s / %s\n" +
                        "Spica Obs Alt: %s - %.2f\272 (Y.R-J: 47.5633\272), Z: %.01f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                spicaGHA, spicaDEC,
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                he,
                z);

        System.out.println();
        // Spica, 2010-Nov-05 at 00:00:00 UTC
        // Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 2010);
        date.set(Calendar.MONTH, 10);
        date.set(Calendar.DAY_OF_MONTH, 5);
        date.set(Calendar.HOUR_OF_DAY, 0); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        System.out.printf("Calculation for Spica launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);
        deltaT = ac.getDeltaT();

        // /*Star*/ spica = Star.getStar("Spica"); // Case sensitive name
        ac.starPos("Spica");
        spicaSHA = ac.getStarSHA("Spica");
        spicaGHA = ac.getStarGHA("Spica");
        spicaDEC = ac.getStarDec("Spica");

        System.out.printf("- Spica %s SHA: %s, Decl: %s (deltaT: %f s)\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                GeomUtil.decToSex(spicaSHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(spicaDEC, GeomUtil.SHELL, GeomUtil.NS),
                deltaT);
        System.out.println();

        // Spica, 2025-Aug-01 at 00:00:00 UTC
        // Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, 2025);
        date.set(Calendar.MONTH, 7);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.HOUR_OF_DAY, 0); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        System.out.printf("Calculation for Spica launched for %s\n", SDF_UTC.format(date.getTime()));

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);
        deltaT = ac.getDeltaT();
        ac.starPos("Spica");
        spicaSHA = ac.getStarSHA("Spica");
        spicaGHA = ac.getStarGHA("Spica");
        spicaDEC = ac.getStarDec("Spica");

        System.out.printf("- Spica %s SHA: %s, Decl: %s (deltaT: %f s)\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                GeomUtil.decToSex(spicaSHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(spicaDEC, GeomUtil.SHELL, GeomUtil.NS),
                deltaT);

        System.out.println("----------- E N D ------------");
    }
}
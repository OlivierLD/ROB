package astro;

import calc.DeadReckoning;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * For the SRU and DR, compare with https://olivierld.github.io/web.stuff/astro/index_03.html
 */
public class PlanDesSommetsPlayground {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private double rhoE = 635677D; // Earth radius, in 100s of km. It's 6356.77 km.
    public static void main(String... args) {

        double latitude = 47.677667d;
        double longitude = -3.135667d;

        AstroComputerV2 ac = new AstroComputerV2();
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        DeadReckoning dr = new DeadReckoning(sunGHA, sunDecl, latitude, longitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double z = dr.getZ();
        final double refraction = DeadReckoning.getRefraction(he);

        final double eyeHeight = 1.8; // in meters
        final double horizonDip = DeadReckoning.getHorizonDip(eyeHeight);
        final double sunHp = ac.getSunHp() / 60d; // Here in minutes of arc. Returned in seconds of arc
        final double sunSd = ac.getSunSd() / 60d; // Here in minutes of arc. Returned in seconds of arc


        // Apparent Correction = (+/- SD) - Horizon Dip
        // Total Correction = App Correction - refr + hp.
        // Observed Altitude = Instr Alt + (SD - HorizonDip - refr + hp)
        double appCorrection = sunSd - horizonDip; // In minutes
        double totalCorrection = appCorrection - refraction + sunHp;

        System.out.printf("Apparent Correction: %.02f', Total Correction: %.02f'\n", appCorrection, totalCorrection);

        double instrAlt = he - (totalCorrection / 60d); // (-(sunSd / 60d) /* Lower Limb */ - (horizonDip / 60d) - (refraction / 60d) + (sunHp / 60d));

        System.out.printf("After Dead Reckoning: For %s,\n" +
                          "Obs Alt: %s, Instr Alt: %s,\n" +
                          "with sunSD: %f', sunHP: %f', refraction: %f', horizon dip: %f' \n",
                            SDF_UTC.format(date.getTime()),
                            GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            GeomUtil.decToSex(instrAlt, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            sunSd, sunHp, refraction, horizonDip);

//        double corr = SightReductionUtil.getAltitudeCorrection(7d,
//                2d,
//                0.1 / 60d,
//                16d / 60d,
//                SightReductionUtil.LOWER_LIMB,
//                false,
//                true);
        double corr = SightReductionUtil.getAltitudeCorrection(instrAlt,
                eyeHeight,
                sunHp / 60d, // In degrees
                sunSd / 60d,     // In degrees
                SightReductionUtil.LOWER_LIMB,
                false,
                true);
        System.out.println("Total Correction (SRU): " + (corr * 60d) + "' (minutes)");

        // Opposite of Dead Reckoning
        SightReductionUtil sightReductionUtil = new SightReductionUtil(sunGHA, sunDecl, latitude, longitude);
        sightReductionUtil.calculate();
        // He and Z, from body's position and observer's position
        he = sightReductionUtil.getHe();
        z = sightReductionUtil.getZ();

        System.out.printf("Obs Alt: %.02f\272 (%s), Z: %.01f\272 \n", he, GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(), z);
    }

}
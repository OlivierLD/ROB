package astro;

import calc.DeadReckoning;
import calc.GeomUtil;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * For the SRU and DR, compare with <a href="https://olivierld.github.io/web.stuff/astro/index_03.html">...</a>
 */
public class PlanDesSommetsPlayground {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    private final static double rhoE = 635677D; // Earth radius, in 100s of km. It's 6356.77 km.
    private final static double earthRadiusNM = (rhoE / 100d) / 1.852; // Earth radius, in nm.
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
        // Total Correction = App Correction - refraction + hp.
        // Observed Altitude = Instr Alt + (SD - HorizonDip - refraction + hp)
        double appCorrection = sunSd - horizonDip; // In minutes
        double totalCorrection = appCorrection - refraction + sunHp;

        System.out.printf("Apparent Correction: %.02f', Total Correction: %.02f'\n", appCorrection, totalCorrection);

        double instrAlt = he - (totalCorrection / 60d); // (-(sunSd / 60d) /* Lower Limb */ - (horizonDip / 60d) - (refraction / 60d) + (sunHp / 60d));

        System.out.printf("After Dead Reckoning: For %s,\n" +
                          "Sun at GHA: %.02f\272, Decl: %.02f\272 \n" +
                          " From position %s / %s\n" +
                          "Obs Alt: %s, Instr Alt: %s, Z: %.01f\272\n" +
                          "with sunSD: %f', sunHP: %f', refraction: %f', horizon dip: %f' \n",
                            SDF_UTC.format(date.getTime()),
                            sunGHA, sunDecl,
                            GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                            GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                            GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            GeomUtil.decToSex(instrAlt, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                            z,
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

        // Distance between Body's PG and Observer
        GreatCirclePoint from = new GreatCirclePoint(Math.toRadians(latitude), Math.toRadians(longitude));
        GreatCirclePoint to = new GreatCirclePoint(Math.toRadians(sunDecl), Math.toRadians(AstroComputerV2.ghaToLongitude(sunGHA)));

        System.out.println();
        final double distanceInNM = GreatCircle.getDistanceInNM(from, to);
        System.out.printf("From observer [%s / %s] to Sun [%s / %s], dist: %.02f'\n",
                GeomUtil.decToSex(Math.toDegrees(from.getL()), GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(Math.toDegrees(from.getG()), GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(Math.toDegrees(to.getL()), GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(Math.toDegrees(to.getG()), GeomUtil.SHELL, GeomUtil.EW),
                distanceInNM);

        // Find FS, distance from observer to summit.
        double FS = earthRadiusNM * (1 / Math.tan(Math.toRadians(he)));
        System.out.printf("FS, in nautical miles: %.02f'\n", FS);
        double coneDiameter = earthRadiusNM * Math.cos(Math.toRadians(he));
        System.out.printf("Cone diameter, in nautical miles: %.02f'\n", coneDiameter);
    }

}
package astro.plandessommets;

import calc.*;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * For the SRU and DR, compare with <a href="https://olivierld.github.io/web.stuff/astro/index_03.html">...</a>
 */
public class PlanDesSommetsPlayground {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
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
        if (true) { // Enforce date
            date.set(Calendar.YEAR, 2025);
            date.set(Calendar.MONTH, 7); // Aug
            date.set(Calendar.DAY_OF_MONTH, 20);
            date.set(Calendar.HOUR_OF_DAY, 10); // and not just HOUR !!!!
            date.set(Calendar.MINUTE, 40);
            date.set(Calendar.SECOND, 31);
        }

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        CelestialDeadReckoning dr = new CelestialDeadReckoning(sunGHA, sunDecl, latitude, longitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        double z = dr.getZ();

        System.out.printf("Original Context : at %s, Sun Decl %s, Sun GHA %s, from pos is %s / %s, seeing the Sun at altitude %s, in the %.02f\272\n",
                SDF_UTC.format(ac.getCalculationDateTime().getTime()),
                GeomUtil.decToSex(sunDecl, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(sunGHA, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                z);

        final double refraction = CelestialDeadReckoning.getRefraction(he);

        final double eyeHeight = 1.8; // in meters
        final double horizonDip = CelestialDeadReckoning.getHorizonDip(eyeHeight);
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

        double distInNM = (90.0 - he) * 60.0;

        System.out.printf("Obs Alt: %.02f\272 (%s), Z: %.01f\272, dist to Pg: %.02f nm\n", he, GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(), z, distInNM);

        // Compare GC & RhumbLine, S to N. Should be identical
        if (true) {
            System.out.println("----- Compare Great Circle and Rhumbline -----");
            GreatCirclePoint from = new GreatCirclePoint(Math.toRadians(latitude), Math.toRadians(longitude)); // Observer
            GreatCirclePoint to = new GreatCirclePoint(Math.toRadians(latitude + 10), Math.toRadians(longitude)); // Observer + full north
            final double distanceInNM = GreatCircle.getDistanceInNM(from, to);
            // final double initialRouteAngle = GreatCircle.getInitialRouteAngleInDegrees(from, to);
            final double initialRouteAngle = GreatCircle.getIRAInDegrees(from, to); // Should be 0 !
            assert (initialRouteAngle == 0.0);
            System.out.printf("-- GC: From [%s / %s] to  [%s / %s], dist: %.02f', IRA: %.02f\272\n",
                    GeomUtil.decToSex(Math.toDegrees(from.getL()), GeomUtil.SHELL, GeomUtil.NS),
                    GeomUtil.decToSex(Math.toDegrees(from.getG()), GeomUtil.SHELL, GeomUtil.EW),
                    GeomUtil.decToSex(Math.toDegrees(to.getL()), GeomUtil.SHELL, GeomUtil.NS),
                    GeomUtil.decToSex(Math.toDegrees(to.getG()), GeomUtil.SHELL, GeomUtil.EW),
                    distanceInNM, initialRouteAngle);

            double hdg = initialRouteAngle;
            // This is a rhumbline, prefer haversine.
            final GeoPoint geoPoint = GeomUtil.deadReckoning(latitude, longitude, distanceInNM, hdg);
            System.out.printf("-- RL: From [%s / %s], %.02f nm in the %.0f\272, pos is [%s / %s]\n",
                    GeomUtil.decToSex(latitude, GeomUtil.SHELL, GeomUtil.NS),
                    GeomUtil.decToSex(longitude, GeomUtil.SHELL, GeomUtil.EW),
                    distanceInNM, hdg,
                    GeomUtil.decToSex(geoPoint.getLatitude(), GeomUtil.SHELL, GeomUtil.NS),
                    GeomUtil.decToSex(geoPoint.getLongitude(), GeomUtil.SHELL, GeomUtil.EW));

            assert(Math.toDegrees(to.getL()) == geoPoint.getLatitude());
            assert(Math.toDegrees(to.getG()) == geoPoint.getLongitude());

            System.out.println("----------------------------------------------");
        }

        // Distance between Body's PG and Observer
        GreatCirclePoint from = new GreatCirclePoint(Math.toRadians(sunDecl), Math.toRadians(AstroComputerV2.ghaToLongitude(sunGHA))); // Body
        GreatCirclePoint to = new GreatCirclePoint(Math.toRadians(latitude), Math.toRadians(longitude)); // Observer

        System.out.println();
        final double distanceInNM = GreatCircle.getDistanceInNM(from, to); // Body to Observer
        assert(distInNM == distanceInNM);

        // final double initialRouteAngle = GreatCircle.getInitialRouteAngleInDegrees(from, to);
        final double initialRouteAngle = GreatCircle.getIRAInDegrees(from, to);

//        final double distanceInNM = GreatCircle.calculateRhumbLineDistance(from, to);
//        final double initialRouteAngle = Math.toDegrees(GreatCircle.calculateRhumbLineRoute(from, to));

        System.out.printf("From Sun [%s / %s (GHA: %s)] to the observer [%s / %s], dist: %.02f', IRA: %.02f\272\n",
                GeomUtil.decToSex(Math.toDegrees(from.getL()), GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(Math.toDegrees(from.getG()), GeomUtil.SHELL, GeomUtil.EW),
                GeomUtil.decToSex(sunGHA, GeomUtil.SHELL, GeomUtil.NONE),
                GeomUtil.decToSex(Math.toDegrees(to.getL()), GeomUtil.SHELL, GeomUtil.NS),
                GeomUtil.decToSex(Math.toDegrees(to.getG()), GeomUtil.SHELL, GeomUtil.EW),
                distanceInNM, initialRouteAngle);
        if (true) {
            final double reverseDistanceInNM = GreatCircle.getDistanceInNM(to, from);
            // final double reverseInitialRouteAngle = GreatCircle.getInitialRouteAngleInDegrees(to, from);
            final double reverseInitialRouteAngle = GreatCircle.getIRAInDegrees(to, from);

            final double reverseInitialRouteAngleV2 = GreatCircle.getInitialRouteAngleInDegreesV2(to, from);
            System.out.printf("Reverse: From the observer [%s / %s], to Sun [%s / %s (GHA: %s)], dist: %.02f', IRA: %.02f\272 (V2: %.02f\272)\n",
                    GeomUtil.decToSex(Math.toDegrees(to.getL()), GeomUtil.SHELL, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(Math.toDegrees(to.getG()), GeomUtil.SHELL, GeomUtil.EW).trim(),
                    GeomUtil.decToSex(Math.toDegrees(from.getL()), GeomUtil.SHELL, GeomUtil.NS).trim(),
                    GeomUtil.decToSex(Math.toDegrees(from.getG()), GeomUtil.SHELL, GeomUtil.EW).trim(),
                    GeomUtil.decToSex(sunGHA, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                    reverseDistanceInNM, reverseInitialRouteAngle, reverseInitialRouteAngleV2);
        }

        // Find FS, distance from observer to summit.
        double FS = earthRadiusNM * (1 / Math.tan(Math.toRadians(he)));
        System.out.printf("FS, in nautical miles: %.02f'\n", FS);
        double coneDiameter = earthRadiusNM * Math.cos(Math.toRadians(he));
        System.out.printf("Cone radius, in nautical miles: %.02f'\n", coneDiameter);

        // Find all the points seeing the Sun at the same altitude
        System.out.println("---- The Circle, Cone base ----");
        long ari = Math.round(initialRouteAngle);
        for (int i=0; i<360; i++) {
//            double hdg = ari + i;
//            while (hdg >= 360) {
//                hdg -= 360;
//            }
            double hdg = i;
            // The point of the circle. Use a Great Circle, not a RhumbLine... GreatCircle.dr needs (big) tune-up, use haversine.
            if (false) {
                // Use RL
                final GeoPoint drRL = GeomUtil.deadReckoning(new GeoPoint(sunDecl, AstroComputerV2.ghaToLongitude(sunGHA)), distanceInNM, hdg);

                GreatCirclePoint sunPt = new GreatCirclePoint(Math.toRadians(sunDecl), Math.toRadians(AstroComputerV2.ghaToLongitude(sunGHA))); // Body
                GreatCirclePoint circlePt = new GreatCirclePoint(Math.toRadians(drRL.getLatitude()), Math.toRadians(drRL.getLongitude())); // Observer, circle point
                // GC calc, for validation
                final double radiusInNM = GreatCircle.getDistanceInNM(sunPt, circlePt);
                // final double toTheObs = GreatCircle.getInitialRouteAngleInDegrees(sunPt, circlePt);
                final double toTheObs = GreatCircle.getIRAInDegrees(sunPt, circlePt);

                // Validation for the altitude
                SightReductionUtil sru = new SightReductionUtil(sunGHA, sunDecl, drRL.getLatitude(), drRL.getLongitude());
                sru.calculate();
                // He and Z, from body's position and observer's position
                he = sru.getHe();
                z = sru.getZ();

                System.out.printf("From %s / %s, %.02f nm in the %.0f\272, pos is %s / %s, seeing the Sun at altitude %s, in the %.02f\272 (GC: %.02f' in the %.02f\272)\n",
                        GeomUtil.decToSex(sunDecl, GeomUtil.SHELL, GeomUtil.NS),
                        GeomUtil.decToSex(AstroComputerV2.ghaToLongitude(sunGHA), GeomUtil.SHELL, GeomUtil.EW),
                        distanceInNM,
                        hdg,
                        GeomUtil.decToSex(drRL.getLatitude(), GeomUtil.SHELL, GeomUtil.NS),
                        GeomUtil.decToSex(drRL.getLongitude(), GeomUtil.SHELL, GeomUtil.EW),
                        GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                        z, radiusInNM, toTheObs);

            } else {
                // Use GC
                GeoPoint sunPos = new GeoPoint(sunDecl, AstroComputerV2.ghaToLongitude(sunGHA));
                // hdg = 335.29;
                final GeoPoint drGC = GeomUtil.haversineInv(sunPos, distanceInNM, hdg); // THE dr to use

                GreatCirclePoint sunPt = new GreatCirclePoint(Math.toRadians(sunDecl), Math.toRadians(AstroComputerV2.ghaToLongitude(sunGHA))); // Body
                GreatCirclePoint circlePt = new GreatCirclePoint(Math.toRadians(drGC.getL()), Math.toRadians(drGC.getG())); // Observer, circle point
                // GC calc, for validation
                // final double radiusInNM = GreatCircle.getDistanceInNM(sunPt, circlePt);
                final double radiusInNM = GeomUtil.haversineNm(sunPos.getL(), sunPos.getG(), drGC.getL(), drGC.getG()); // THE one to prefer
                // final double toTheObs = GreatCircle.getInitialRouteAngleInDegrees(sunPt, circlePt);
                final double toTheObs = GreatCircle.getIRAInDegrees(sunPt, circlePt);

                // Validation for the altitude
                SightReductionUtil sru = new SightReductionUtil(sunGHA, sunDecl, drGC.getL(), drGC.getG());
                sru.calculate();
                // He and Z, from body's position and observer's position
                he = sru.getHe();
                z = sru.getZ();

                System.out.printf("From Pg %s / %s, %.02f nm in the %.02f\272, pos on circle is %s / %s, seeing the Sun at altitude %s, in the %.02f\272 (GC: %.02f' in the %.02f\272)\n",
                        GeomUtil.decToSex(sunDecl, GeomUtil.SHELL, GeomUtil.NS).trim(),
                        GeomUtil.decToSex(AstroComputerV2.ghaToLongitude(sunGHA), GeomUtil.SHELL, GeomUtil.EW).trim(),
                        distanceInNM,
                        hdg,
                        GeomUtil.decToSex(drGC.getL(), GeomUtil.SHELL, GeomUtil.NS).trim(),
                        GeomUtil.decToSex(drGC.getG(), GeomUtil.SHELL, GeomUtil.EW).trim(),
                        GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE).trim(),
                        z, radiusInNM, toTheObs);
            }
        }
    }

}
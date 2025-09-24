package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mps.MPSToolBox;

import java.util.Calendar;
import java.util.TimeZone;

public class PlayGround01 {

    public static void main(String... args) {
        boolean jsonOutput = false;
        ObjectMapper mapper = new ObjectMapper();


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

        final double sunGHA = ac.getSunGHA();
        final double sunDecl = ac.getSunDecl();

        CelestialDeadReckoning dr = MPSToolBox.calculateDR(sunGHA, sunDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        double he = dr.getHe();
        // double z = dr.getZ();

        double sunObsAlt = he; // 49.37536262617141;

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = 0.1d;

        MPSToolBox.ConeDefinition sunCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl, "the Sun", fromZ, toZ, zStep, false);

        if (jsonOutput) {
            // Print circle values, in JSON
            // Pg of the body, summit of the cone...
            try {
                System.out.println("--------------------------------");
                System.out.printf("Producing an array of %d element(s)\n", sunCone.getCircle().size());
                String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sunCone);
                System.out.println(content);
                System.out.println("--------------------------------");
            } catch (JsonProcessingException jpe) {
                jpe.printStackTrace();
            }
        }

        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();
        dr = MPSToolBox.calculateDR(moonGHA, moonDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        // double z = dr.getZ();

        double moonObsAlt = he; // 41.46314233337399;
        MPSToolBox.ConeDefinition moonCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), moonObsAlt, moonGHA, moonDecl, "the Moon", fromZ, toZ, zStep, false);

        // Now, find the intersection of the two cones...
        double smallest = Double.MAX_VALUE;
        GeoPoint closestSunPoint = null;
        GeoPoint closestMoonPoint = null;
        Double closestSunPointZ = null;
        Double closestMoonPointZ = null;

        for (MPSToolBox.ConePoint sunPoint : sunCone.getCircle()) {
            for (MPSToolBox.ConePoint moonPoint : moonCone.getCircle()) {
                // GC distance from-to, use GeomUtil.haversineNm
                double dist =  GeomUtil.haversineNm(sunPoint.getPoint(), moonPoint.getPoint());
                if (false && sunPoint.getPoint().getLatitude() > 47 && sunPoint.getPoint().getLatitude() < 48 &&
                        sunPoint.getPoint().getLongitude() < -3 && sunPoint.getPoint().getLongitude() > -4 &&
                        moonPoint.getPoint().getLatitude() > 47 && moonPoint.getPoint().getLatitude() < 48 &&
                        moonPoint.getPoint().getLongitude() < -3 && moonPoint.getPoint().getLongitude() > -4) {
                    System.out.printf("Between (Sun circle) %s and (Moon circle) %s, Distance is %.04f nm \n", sunPoint.getPoint().toString(), moonPoint.getPoint().toString(), dist);
                }
                if (dist < smallest) {
                    smallest = dist;
                    closestSunPoint = (GeoPoint)sunPoint.getPoint().clone();
                    closestMoonPoint = (GeoPoint)moonPoint.getPoint().clone();
                    closestSunPointZ = sunPoint.getZ();
                    closestMoonPointZ = moonPoint.getZ();
                }
            }
        }
        // End of first loop !
        System.out.printf("Loop 1 - Smallest distance: %.04f nm, between (Sun circle, z: %.04f) %s and (Moon circle, z: %.04f) %s \n", smallest, closestSunPointZ, closestSunPoint.toString(), closestMoonPointZ, closestMoonPoint.toString());

        // Starting loop 2
        sunCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl, "the Sun", closestSunPointZ - zStep, closestSunPointZ + zStep, zStep / 10.0, false);
        moonCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), moonObsAlt, moonGHA, moonDecl, "the Moon", closestMoonPointZ - zStep, closestMoonPointZ + zStep, zStep / 10.0, false);

        zStep /= 10.0;

        for (MPSToolBox.ConePoint sunPoint : sunCone.getCircle()) {
            for (MPSToolBox.ConePoint moonPoint : moonCone.getCircle()) {
                double dist =  GeomUtil.haversineNm(sunPoint.getPoint(), moonPoint.getPoint());
                if (false && sunPoint.getPoint().getLatitude() > 47 && sunPoint.getPoint().getLatitude() < 48 &&
                        sunPoint.getPoint().getLongitude() < -3 && sunPoint.getPoint().getLongitude() > -4 &&
                        moonPoint.getPoint().getLatitude() > 47 && moonPoint.getPoint().getLatitude() < 48 &&
                        moonPoint.getPoint().getLongitude() < -3 && moonPoint.getPoint().getLongitude() > -4) {
                    System.out.printf("Between (Sun circle) %s and (Moon circle) %s, Distance is %.04f nm \n", sunPoint.getPoint().toString(), moonPoint.getPoint().toString(), dist);
                }
                if (dist < smallest) {
                    smallest = dist;
                    closestSunPoint = (GeoPoint)sunPoint.getPoint().clone();
                    closestMoonPoint = (GeoPoint)moonPoint.getPoint().clone();
                    closestSunPointZ = sunPoint.getZ();
                    closestMoonPointZ = moonPoint.getZ();
                }
            }
        }
        // End of second loop !
        System.out.printf("Loop 2 - Smallest distance: %.04f nm, between (Sun circle, z: %.04f) %s and (Moon circle, z: %.04f) %s \n", smallest, closestSunPointZ, closestSunPoint.toString(), closestMoonPointZ, closestMoonPoint.toString());

        // Starting loop 3
        sunCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl, "the Sun", closestSunPointZ - zStep, closestSunPointZ + zStep, zStep / 10.0, false);
        moonCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), moonObsAlt, moonGHA, moonDecl, "the Moon", closestMoonPointZ - zStep, closestMoonPointZ + zStep, zStep / 10.0, false);

        zStep /= 10.0;

        for (MPSToolBox.ConePoint sunPoint : sunCone.getCircle()) {
            for (MPSToolBox.ConePoint moonPoint : moonCone.getCircle()) {
                double dist =  GeomUtil.haversineNm(sunPoint.getPoint(), moonPoint.getPoint());
                if (false && sunPoint.getPoint().getLatitude() > 47 && sunPoint.getPoint().getLatitude() < 48 &&
                        sunPoint.getPoint().getLongitude() < -3 && sunPoint.getPoint().getLongitude() > -4 &&
                        moonPoint.getPoint().getLatitude() > 47 && moonPoint.getPoint().getLatitude() < 48 &&
                        moonPoint.getPoint().getLongitude() < -3 && moonPoint.getPoint().getLongitude() > -4) {
                    System.out.printf("Between (Sun circle) %s and (Moon circle) %s, Distance is %.04f nm \n", sunPoint.getPoint().toString(), moonPoint.getPoint().toString(), dist);
                }
                if (dist < smallest) {
                    smallest = dist;
                    closestSunPoint = (GeoPoint)sunPoint.getPoint().clone();
                    closestMoonPoint = (GeoPoint)moonPoint.getPoint().clone();
                    closestSunPointZ = sunPoint.getZ();
                    closestMoonPointZ = moonPoint.getZ();
                }
            }
        }
        // End of second loop !
        System.out.printf("Loop 3 - Smallest distance: %.04f nm, between (Sun circle, z: %.04f) %s and (Moon circle, z: %.04f) %s \n", smallest, closestSunPointZ, closestSunPoint.toString(), closestMoonPointZ, closestMoonPoint.toString());

        GeoPoint userPos = new GeoPoint(userLatitude, userLongitude);
        System.out.printf("-> For comparison, User pos is %s\n", userPos);

    }

}
package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import mps.MPSToolBox;

import java.util.*;

public class PlayGround03 {

    public static void main(String... args) {

        // Get to the body's coordinates at the given time
        AstroComputerV2 ac = new AstroComputerV2();
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now

        // A Test. The Sun, 2025-AUG-20 10:40:31 UTC

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

        System.out.printf("Sun expected observed altitude: %f, %s\n", sunObsAlt, GeomUtil.decToSex(sunObsAlt, GeomUtil.SWING, GeomUtil.NONE));

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = 90.0; // 0.1;

        MPSToolBox.ConeDefinition sunCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl, "the Sun", fromZ, toZ, zStep, true);

        double randomIndex = Math.random();
        int nbPts = sunCone.getCircle().size();
        final GeoPoint rndPoint = sunCone.getCircle().get((int) (randomIndex * nbPts)).getPoint();
        CelestialDeadReckoning rndDr = MPSToolBox.calculateDR(sunGHA, sunDecl, rndPoint.getLatitude(), rndPoint.getLongitude()).calculate(); // All angles in degrees
        double rndHe = rndDr.getHe();
        // double z = rndDr.getZ();
        System.out.printf("Sun random observed altitude: %f, %s\n", rndHe, GeomUtil.decToSex(rndHe, GeomUtil.SWING, GeomUtil.NONE));

        GeoPoint userPos = new GeoPoint(userLatitude, userLongitude);
        System.out.printf("-> For comparison, User pos is %s\n", userPos);

        // Original pos
        CelestialDeadReckoning finalDR = MPSToolBox.calculateDR(sunGHA, sunDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        double finalHe = finalDR.getHe();
        // double z = rndDr.getZ();
        System.out.printf("Sun final observed altitude: %f, %s\n", finalHe, GeomUtil.decToSex(finalHe, GeomUtil.SWING, GeomUtil.NONE));

    }

}
package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import mps.MPSToolBox;

import java.util.*;

public class PlayGround02 {

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

        System.out.printf("Sun expected observed altitude: %f\n", sunObsAlt);

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = 1.0; // 0.1;

        MPSToolBox.ConeDefinition sunCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), sunObsAlt, sunGHA, sunDecl, "the Sun", fromZ, toZ, zStep, false);

        double randomIndex = Math.random();
        int nbPts = sunCone.getCircle().size();
        final GeoPoint rndPoint = sunCone.getCircle().get((int) (randomIndex * nbPts)).getPoint();
        CelestialDeadReckoning rndDr = MPSToolBox.calculateDR(sunGHA, sunDecl, rndPoint.getLatitude(), rndPoint.getLongitude()).calculate(); // All angles in degrees
        double rndHe = rndDr.getHe();
        // double z = rndDr.getZ();
        System.out.printf("Sun random observed altitude: %f\n", rndHe);

        // TODO Honk if not the expected one (sunObsAlt).

        final double moonGHA = ac.getMoonGHA();
        final double moonDecl = ac.getMoonDecl();
        dr = MPSToolBox.calculateDR(moonGHA, moonDecl, userLatitude, userLongitude).calculate(); // All angles in degrees
        he = dr.getHe();
        // double z = dr.getZ();

        double moonObsAlt = he; // 41.46314233337399;
        MPSToolBox.ConeDefinition moonCone = MPSToolBox.calculateCone(ac.getCalculationDateTime().getTime(), moonObsAlt, moonGHA, moonDecl, "the Moon", fromZ, toZ, zStep, false);

        // Now, Create a list of pairs of points , with the distance between them.
        class PPD implements Comparable<PPD> {  // Pair of points and distance
            final MPSToolBox.ConePoint point1;
            final MPSToolBox.ConePoint point2;
            final double distance;

            public PPD(MPSToolBox.ConePoint point1, MPSToolBox.ConePoint point2, double distance) {
                this.point1 = point1;
                this.point2 = point2;
                this.distance = distance;
            }

            @Override
            public int compareTo(PPD other) {
                return Double.compare(this.distance, other.distance); // Sort by distance in ascending order
            }

        }

        List<PPD> theList = new ArrayList<>();
        for (MPSToolBox.ConePoint sunPoint : sunCone.getCircle()) {
            for (MPSToolBox.ConePoint moonPoint : moonCone.getCircle()) {
                double dist =  GeomUtil.haversineNm(sunPoint.getPoint(), moonPoint.getPoint()); // GC distance from-to
                theList.add(new PPD(sunPoint, moonPoint, dist));
            }
        }
        // List populated, now sort it by distance.
        Collections.sort(theList);

        // Display when distance < 50
        theList.stream()
                .filter(item -> item.distance < 50.0)
                .forEach(item -> {
                    System.out.printf("%f nm between %s and %s\n",
                            item.distance,
                            item.point1.getPoint(),
                            item.point2.getPoint());
                });

        GeoPoint userPos = new GeoPoint(userLatitude, userLongitude);
        System.out.printf("-> For comparison, User pos is %s\n", userPos);
    }

}
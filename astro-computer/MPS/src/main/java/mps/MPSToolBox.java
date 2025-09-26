package mps;

import calc.*;
import calc.calculation.AstroComputerV2;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * For the SRU and DR, compare with <a href="https://olivierld.github.io/web.stuff/astro/index_03.html">...</a>
 */
public class MPSToolBox {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
    static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    /*
     * If Earth is a perfect sphere:
     *    R = (360*60) / (2*PI) = 3,437.7467707849 nm
     *    R = 3,437.7467707849 * 1.852 = 6,366.7070194937 km
     */
    private final static double rhoE = 635677D; // Earth radius, in 100s of km. It's 6356.77 km.
    private final static double earthRadiusNM = (rhoE / 100d) / 1.852; // Earth radius, in nm.

    final public static class ConePoint {
        GeoPoint point; // Point of the circle
        double z;       // ICA from center to circle

        public ConePoint(GeoPoint p, double z) {
            super();
            this.point = p;
            this.z = z;
        }
        public GeoPoint getPoint() {
            return point;
        }

        public void setPoint(GeoPoint point) {
            this.point = point;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }
    }
    final public static class ConeDefinition {
        GeoPoint pg;
        double obsAlt;
        double earthCenterToConeSummit;
        String bodyName;
        String observationTime;
        List<ConePoint> circle;

        public GeoPoint getPg() {
            return pg;
        }

        public void setPg(GeoPoint pg) {
            this.pg = pg;
        }

        public double getObsAlt() {
            return obsAlt;
        }

        public double getEarthCenterToConeSummit() {
            return earthCenterToConeSummit;
        }
        public void setObsAlt(double obsAlt) {
            this.obsAlt = obsAlt;
        }

        public void setEarthCenterToConeSummit(double earthCenterToConeSummit) {
            this.earthCenterToConeSummit = earthCenterToConeSummit;
        }

        public String getBodyName() {
            return bodyName;
        }

        public void setBodyName(String bodyName) {
            this.bodyName = bodyName;
        }

        public String getObservationTime() {
            return observationTime;
        }

        public void setObservationTime(String observationTime) {
            this.observationTime = observationTime;
        }

        public List<ConePoint> getCircle() {
            return circle;
        }

        public void setCircle(List<ConePoint> circle) {
            this.circle = circle;
        }
    }

    public static CelestialDeadReckoning calculateDR(double gha, double dec, double userLatitude, double userLongitude) {
        CelestialDeadReckoning dr = new CelestialDeadReckoning(gha, dec, userLatitude, userLongitude).calculate();
        return dr;
    }

    public static ConeDefinition calculateCone(Date calculationTime,
                                               double obsAlt,
                                               double gha,
                                               double dec,
                                               String bodyName,
                                               boolean verbose) {
        return calculateCone(calculationTime, obsAlt, gha, dec, bodyName, 0d, 360d, 1d, verbose);
    }

    /**
     * Return the cone definition for a body defined by its GHA and DEC, and a given observed altitude.
     *
     * @param calculationTime Used for the ConeDefinition containing the requested data
     * @param obsAlt Observed altitude of the body
     * @param gha GHA of the body (obtained from almanacs)
     * @param dec DEV of the body (obtained from almanacs)
     * @param bodyName Name, used for the ConeDefinition containing the requested data
     * @param fromZ The Z (from Pg) used to start the iteration for the circle
     * @param toZ The Z (from Pg) used to finish the iteration for the circle
     * @param zStep The step for the iteration
     * @param verbose guess what !
     * @return the requested ConeDefinition
     */
    public static ConeDefinition calculateCone(Date calculationTime,
                                               double obsAlt,
                                               double gha,
                                               double dec,
                                               String bodyName,
                                               double fromZ,
                                               double toZ,
                                               double zStep,
                                               boolean verbose) {

        double distInNM = (90.0 - obsAlt) * 60.0;

        // Find MS, distance from observer to summit.
        double MS = earthRadiusNM * (1 / Math.tan(Math.toRadians(obsAlt)));
        if (verbose) {
            System.out.printf("MS (obs to summit), in nautical miles: %.02f'\n", MS);
        }
        double coneDiameter = earthRadiusNM * Math.cos(Math.toRadians(obsAlt));
        if (verbose) {
            System.out.printf("Cone radius, in nautical miles: %.02f'\n", coneDiameter);
        }

        double earthCenterToConeSummit = Math.sqrt((MS * MS) + (earthRadiusNM * earthRadiusNM));

        // Find all the points seeing the body at the same altitude
        if (verbose) {
            System.out.println("---- The Circle, Cone base ----");
        }

        ConeDefinition cd = new ConeDefinition();
        cd.circle = new ArrayList<>();
        cd.bodyName = bodyName;
        cd.obsAlt = obsAlt;
        cd.pg = new GeoPoint(dec, AstroComputerV2.ghaToLongitude(gha));
        cd.earthCenterToConeSummit = earthCenterToConeSummit;
        cd.observationTime = SDF_UTC.format(calculationTime);

        for (double z=fromZ; z<toZ; z += zStep) { // The steps and interval here !
            double hdg = z;
            GeoPoint bodyPos = new GeoPoint(dec, AstroComputerV2.ghaToLongitude(gha));
            final GeoPoint drGC = GeomUtil.haversineInv(bodyPos, distInNM, hdg); // THE dr to use
            // final GeoPoint drGC = GeomUtil.deadReckoning(bodyPos, distInNM, hdg);

            // altitude tests, reverse
            if (verbose) {
                // for 20-AUG-2025 10:40:31, GHA: 339°17.40', D: N 12°16.80', Obs Alt: 49°22.51'
                CelestialDeadReckoning cdr = calculateDR(gha, dec, drGC.getLatitude(), drGC.getLongitude());
                double he = cdr.getHe();
                System.out.printf("GHA: %s, D: %s \n", GeomUtil.decToSex(gha, GeomUtil.SWING, GeomUtil.NONE), GeomUtil.decToSex(dec, GeomUtil.SWING, GeomUtil.NS));
                System.out.printf("For obsAlt=%f (%s), he (from circle)=%f (%s)\n", obsAlt, GeomUtil.decToSex(obsAlt, GeomUtil.SWING, GeomUtil.NONE), he, GeomUtil.decToSex(he, GeomUtil.SWING, GeomUtil.NONE));
            }
            cd.circle.add(new ConePoint(drGC, hdg));
        }
        return cd;
    }

    /**
     * Find the intersection of two cones, defined by their Observed Altitude, GHA and Declination.
     * No user position involved here  - this is what we want to find.
     *
     * This uses a method close to the Newton's method.
     * We look for the points as close to each other as possible, starting from all the points of cone1, and checking for each of them
     * all the points of cone2, with a given step for Z.
     * Then we restrict the Z interval, and the Z step.
     * This as many times as required by the nbLoops parameter.
     *
     * @param firstTime Date when the firstObsAlt was taken
     * @param firstObsAlt Observed Altitude of the first body
     * @param firstGHA GHA of the first body (at firstTime)
     * @param firstDecl Decl of the first body (at firstTime)
     * @param secondTime Date when the secondObsAlt was taken
     * @param secondObsAlt Observed Altitude of the second body
     * @param secondGHA GHA of the second body (at secondTime)
     * @param secondDecl Decl of the second body (at secondTime)
     * @param nbLoops Number of recursions
     * @param verbose true or false
     * @return a List of the two GeoPoints (one on each cone) closest to each other.
     */
    public static List<GeoPoint> resolve2Cones(Date firstTime, double firstObsAlt, double firstGHA, double firstDecl,
                                               Date secondTime, double secondObsAlt, double secondGHA, double secondDecl,
                                               int nbLoops, boolean verbose) {

        List<GeoPoint> result = null;

        double smallest = Double.MAX_VALUE;
        GeoPoint closestPointBody1 = null;
        GeoPoint closestPointBody2 = null;
        Double closestPointZBody1 = null;
        Double closestPointZBody2 = null;

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = 1d; // Will thus start with step = 0.1

        for (int loop=0; loop<nbLoops; loop++) {

            MPSToolBox.ConeDefinition coneBody1 = MPSToolBox.calculateCone(firstTime, firstObsAlt, firstGHA, firstDecl, "Body 1",
                    closestPointZBody1 == null ? fromZ : closestPointZBody1 - zStep,
                    closestPointZBody1 == null ? toZ : closestPointZBody1 + zStep,
                    zStep / 10d, false);
            MPSToolBox.ConeDefinition coneBody2 = MPSToolBox.calculateCone(secondTime, secondObsAlt, secondGHA, secondDecl, "Body 2",
                    closestPointZBody2 == null ? fromZ : closestPointZBody2 - zStep,
                    closestPointZBody2 == null ? toZ : closestPointZBody2 + zStep,
                    zStep / 10d, false);

            // Now, find the intersection of the two cones...
            for (MPSToolBox.ConePoint conePointBody1 : coneBody1.getCircle()) {
                for (MPSToolBox.ConePoint conePointBody2 : coneBody2.getCircle()) {
                    // GC distance from-to, use GeomUtil.haversineNm
                    double dist = GeomUtil.haversineNm(conePointBody1.getPoint(), conePointBody2.getPoint());
                    if (dist < smallest) {
                        smallest = dist;
                        closestPointBody1 = (GeoPoint) conePointBody1.getPoint().clone();
                        closestPointBody2 = (GeoPoint) conePointBody2.getPoint().clone();
                        closestPointZBody1 = conePointBody1.getZ();
                        closestPointZBody2 = conePointBody2.getZ();
                    }
                }
            }
            // End of loop #n
            if (verbose) {
                System.out.printf("Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                        loop + 1,
                        smallest,
                        closestPointZBody1,
                        closestPointBody1.toString(),
                        closestPointZBody2,
                        closestPointBody2.toString());
            }

            result = Arrays.asList(new GeoPoint[] {  // List.of not supported in Java8
                    closestPointBody1, closestPointBody2
            });
            zStep /= 10.0; // For the next one
        }
        return result;
    }
}
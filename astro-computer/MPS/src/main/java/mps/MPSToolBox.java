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
    final public static class ConesIntersection {
        String bodyOneName;
        String bodyTwoName;

        GeoPoint coneOneIntersectionOne;
        GeoPoint coneOneIntersectionTwo;
        GeoPoint coneTwoIntersectionOne;
        GeoPoint coneTwoIntersectionTwo;

        public ConesIntersection() { // Required by ObjectMapper...
        }
        public ConesIntersection(String bodyOneName, String bodyTwoName,
                                 GeoPoint coneOneIntersectionOne, GeoPoint coneOneIntersectionTwo,
                                 GeoPoint coneTwoIntersectionOne, GeoPoint coneTwoIntersectionTwo) {
            this.bodyOneName = bodyOneName;
            this.bodyTwoName = bodyTwoName;
            this.coneOneIntersectionOne = coneOneIntersectionOne;
            this.coneOneIntersectionTwo = coneOneIntersectionTwo;
            this.coneTwoIntersectionOne = coneTwoIntersectionOne;
            this.coneTwoIntersectionTwo = coneTwoIntersectionTwo;
        }

        public String getBodyOneName() {
            return bodyOneName;
        }

        public void setBodyOneName(String bodyOneName) {
            this.bodyOneName = bodyOneName;
        }

        public String getBodyTwoName() {
            return bodyTwoName;
        }

        public void setBodyTwoName(String bodyTwoName) {
            this.bodyTwoName = bodyTwoName;
        }

        public GeoPoint getConeOneIntersectionOne() {
            return coneOneIntersectionOne;
        }

        public void setConeOneIntersectionOne(GeoPoint coneOneIntersectionOne) {
            this.coneOneIntersectionOne = coneOneIntersectionOne;
        }

        public GeoPoint getConeOneIntersectionTwo() {
            return coneOneIntersectionTwo;
        }

        public void setConeOneIntersectionTwo(GeoPoint coneOneIntersectionTwo) {
            this.coneOneIntersectionTwo = coneOneIntersectionTwo;
        }

        public GeoPoint getConeTwoIntersectionOne() {
            return coneTwoIntersectionOne;
        }

        public void setConeTwoIntersectionOne(GeoPoint coneTwoIntersectionOne) {
            this.coneTwoIntersectionOne = coneTwoIntersectionOne;
        }

        public GeoPoint getConeTwoIntersectionTwo() {
            return coneTwoIntersectionTwo;
        }

        public void setConeTwoIntersectionTwo(GeoPoint coneTwoIntersectionTwo) {
            this.coneTwoIntersectionTwo = coneTwoIntersectionTwo;
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
     * @param verbose guess what!
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

        for (double z=fromZ; (zStep > 0 && z < toZ) || (zStep < 0 && z > toZ); z += zStep) { // The steps and interval here !
            double hdg = z;
            // System.out.printf("Calculating cone point for Z=%.4f (zStep = %.4f)\n", hdg, zStep);
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

    private static List<ConePoint> intersectionDelegation(MPSToolBox.ConeDefinition coneBody1,
                                                          MPSToolBox.ConeDefinition coneBody2,
                                                          int loop,
                                                          double zStep,
                                                          boolean verbose) {

        List<ConePoint> result = null;

        double smallest = Double.MAX_VALUE;
        GeoPoint closestPointBody1 = null;
        GeoPoint closestPointBody2 = null;
        Double closestPointZBody1 = null;
        Double closestPointZBody2 = null;

        double smallestSecond = Double.MAX_VALUE;
        GeoPoint closestPointBody1Second = null;
        GeoPoint closestPointBody2Second = null;
        Double closestPointZBody1Second = null;
        Double closestPointZBody2Second = null;

        final double distMin = 3.0; // TODO Fix that 3...

        for (MPSToolBox.ConePoint conePointBody1 : coneBody1.getCircle()) {
            for (MPSToolBox.ConePoint conePointBody2 : coneBody2.getCircle()) {
                // GC distance from-to, use GeomUtil.haversineNm
                double dist = GeomUtil.haversineNm(conePointBody1.getPoint(), conePointBody2.getPoint());
                // For some tests..., to find the 2 intersections
                if (loop == 0 && dist < distMin) {
                    if (false) {
                        System.out.printf("Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                dist, zStep / 10d,
                                conePointBody1.getPoint(),
                                conePointBody1.getZ(),
                                conePointBody2.getPoint(),
                                conePointBody2.getZ());
                    }
                    // For loop 0, we'll need 2 smallest dist, identified by their Z
                    if (closestPointZBody1 != null && closestPointZBody2 != null) {
                        if (false) {
                            System.out.printf("DeltaZ_1 %.04f, DeltaZ_2 %.04f, compare to %.04f\n",
                                    Math.abs(conePointBody1.getZ() - closestPointZBody1),
                                    Math.abs(conePointBody2.getZ() - closestPointZBody2),
                                    (5 * zStep));
                        }
                        if (Math.abs(conePointBody1.getZ() - closestPointZBody1) > (5 * zStep) &&
                                Math.abs(conePointBody2.getZ() - closestPointZBody2) > (5 * zStep)) {
                            if (dist < smallestSecond) {
                                smallestSecond = dist;
                                closestPointBody1Second = (GeoPoint) conePointBody1.getPoint().clone();
                                closestPointBody2Second = (GeoPoint) conePointBody2.getPoint().clone();
                                closestPointZBody1Second = conePointBody1.getZ();
                                closestPointZBody2Second = conePointBody2.getZ();
                                if (verbose) {
                                    System.out.printf("2nd Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                            dist, zStep / 10d,
                                            conePointBody1.getPoint(),
                                            conePointBody1.getZ(),
                                            conePointBody2.getPoint(),
                                            conePointBody2.getZ());
                                    System.out.printf("-- (1st : between %s (Z=%.02f) and %s (Z=%.02f))\n",
                                            closestPointBody1.toString(),
                                            closestPointZBody1,
                                            closestPointBody2.toString(),
                                            closestPointZBody2);
                                }
                            }
                        }
                    }
                }
                if ((loop != 0) || (loop == 0 && closestPointZBody1Second == null && closestPointZBody2Second == null)) {
                    if (dist < smallest) {
                        smallest = dist;
                        closestPointBody1 = (GeoPoint) conePointBody1.getPoint().clone();
                        closestPointBody2 = (GeoPoint) conePointBody2.getPoint().clone();
                        closestPointZBody1 = conePointBody1.getZ();
                        closestPointZBody2 = conePointBody2.getZ();
                        if (verbose && dist < distMin && loop == 0) {
                            System.out.printf("1st Intersection: Found dist = %.03f, zStep=%.03f, between %s (Z=%.02f) and %s (Z=%.02f)\n",
                                    dist, zStep / 10d,
                                    conePointBody1.getPoint(),
                                    conePointBody1.getZ(),
                                    conePointBody2.getPoint(),
                                    conePointBody2.getZ());
                        }
                    }
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
            if (loop == 0) {
                System.out.printf("=> 2nd Intersection: Loop %d - Smallest distance: %.04f nm, between (first circle, z: %.04f) %s and (second circle, z: %.04f) %s \n",
                        loop + 1,
                        smallestSecond,
                        closestPointZBody1Second,
                        closestPointBody1Second.toString(),
                        closestPointZBody2Second,
                        closestPointBody2Second.toString());
            }
        }

        if (closestPointBody1 != null && closestPointBody2 != null) {
            result = new ArrayList<>();
            result.add(new ConePoint(closestPointBody1, closestPointZBody1));
            result.add(new ConePoint(closestPointBody2, closestPointZBody2));

            if (loop == 0 && closestPointBody1Second != null && closestPointBody2Second != null) {
                result.add(new ConePoint(closestPointBody1Second, closestPointZBody1Second));
                result.add(new ConePoint(closestPointBody2Second, closestPointZBody2Second));
            }
        }

        return result;
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
     * @param firstZStep Azimuth step to start with.
     * @param nbLoops Number of recursions
     * @param reverse Build the cones counterclockwise if true
     * @param verbose true or false
     * @return a List of the two GeoPoints (one on each cone) closest to each other.
     */
    public static List<GeoPoint> resolve2Cones(Date firstTime, double firstObsAlt, double firstGHA, double firstDecl,
                                               Date secondTime, double secondObsAlt, double secondGHA, double secondDecl,
                                               double firstZStep, int nbLoops, boolean reverse, boolean verbose) {

        List<GeoPoint> result = null;

        // double smallest = Double.MAX_VALUE;
        GeoPoint closestPointBody1 = null;
        GeoPoint closestPointBody2 = null;
        Double closestPointZBody1 = null;
        Double closestPointZBody2 = null;

        // double smallestSecond = Double.MAX_VALUE;
        GeoPoint closestPointBody1Second = null;
        GeoPoint closestPointBody2Second = null;
        Double closestPointZBody1Second = null;
        Double closestPointZBody2Second = null;

        double fromZ = 0d;
        double toZ = 360d;
        double zStep = firstZStep * 10d; // because divided by 10, even when starting the first loop.

        if (reverse) {
            fromZ = 360d;
            toZ = 0d;
            zStep *= -1;
        }

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
            List<ConePoint> geoPointsFirst = intersectionDelegation(coneBody1, coneBody2, loop, zStep / 10, verbose);
            closestPointBody1 = geoPointsFirst.get(0).getPoint();
            closestPointZBody1 = geoPointsFirst.get(0).getZ();
            closestPointBody2 = geoPointsFirst.get(1).getPoint();
            closestPointZBody2 = geoPointsFirst.get(1).getZ();

            if (loop == 0) { // Populate second ones
                if (geoPointsFirst.size() == 4) {
                    closestPointBody1Second = geoPointsFirst.get(2).getPoint();
                    closestPointZBody1Second = geoPointsFirst.get(2).getZ();
                    closestPointBody2Second = geoPointsFirst.get(3).getPoint();
                    closestPointZBody2Second = geoPointsFirst.get(3).getZ();
                } else {
                    System.err.printf("Ooops !!! Second intersection was not found ! Only %d point(s) available.\n", geoPointsFirst.size());
                }
            }

            // 2nd intersection ?
            if (loop > 0) { // Deal with 2nd intersection
                if (verbose) {
                    System.out.println("Dealing with second Intersection...");
                }
                MPSToolBox.ConeDefinition coneBody1Second = MPSToolBox.calculateCone(firstTime, firstObsAlt, firstGHA, firstDecl, "Body 1",
                        closestPointZBody1Second == null ? fromZ : closestPointZBody1Second - zStep,
                        closestPointZBody1Second == null ? toZ : closestPointZBody1Second + zStep,
                        zStep / 10d, false);
                MPSToolBox.ConeDefinition coneBody2Second = MPSToolBox.calculateCone(secondTime, secondObsAlt, secondGHA, secondDecl, "Body 2",
                        closestPointZBody2Second == null ? fromZ : closestPointZBody2Second - zStep,
                        closestPointZBody2Second == null ? toZ : closestPointZBody2Second + zStep,
                        zStep / 10d, false);
                List<ConePoint> geoPointsSecond = intersectionDelegation(coneBody1Second, coneBody2Second, loop, zStep / 10, verbose);
                closestPointBody1Second = geoPointsSecond.get(0).getPoint();
                closestPointZBody1Second = geoPointsSecond.get(0).getZ();
                closestPointBody2Second = geoPointsSecond.get(1).getPoint();
                closestPointZBody2Second = geoPointsSecond.get(1).getZ();
            }

            zStep /= 10.0; // For the next loop
        }

        result = new ArrayList<>();
        result.add(closestPointBody1);
        result.add(closestPointBody2);
        result.add(closestPointBody1Second);
        result.add(closestPointBody2Second);

        return result;
    }

    public static class MotEnoughIntersectionsException extends Exception {
        public MotEnoughIntersectionsException() {
            super();
        }
        public MotEnoughIntersectionsException(String message) {
            super(message);
        }
    }

    private final static double CRITICAL_DIST = 5.0; // TODO Fix/tweak that one !... Change it from 5 to 15, to see the impact.

    public static GeoPoint processIntersectionsList(List<MPSToolBox.ConesIntersection> conesIntersectionList,
                                                    boolean verbose) throws MotEnoughIntersectionsException {
        if (verbose) {
            System.out.printf("We have %d intersections to process.\n", conesIntersectionList.size());
        }
        if (conesIntersectionList.size() > 1) {
            if (verbose) {
                conesIntersectionList.forEach(ci -> System.out.printf("- Intersection between %s and %s\n", ci.getBodyOneName(), ci.getBodyTwoName()));
            }
            List<GeoPoint> candidates = new ArrayList<>();

            // TODO Needs improvements: Iterate on the reference as well...
            for (int ref = 0; ref < conesIntersectionList.size(); ref++) {

                if (verbose) {
                    System.out.printf("-- Ref: %d\n", ref);
                }
                MPSToolBox.ConesIntersection referenceIntersection = conesIntersectionList.get(ref);

                /*
                   Need to manage:
                        cone 1 - intersection 1 with cone 1 - intersection 1
                        cone 1 - intersection 1 with cone 1 - intersection 2
                        cone 1 - intersection 2 with cone 1 - intersection 1
                        cone 1 - intersection 2 with cone 1 - intersection 2

                        cone 2 - intersection 1 with cone 1 - intersection 1
                        cone 2 - intersection 1 with cone 1 - intersection 2
                        cone 2 - intersection 2 with cone 1 - intersection 1
                        cone 2 - intersection 2 with cone 1 - intersection 2

                        cone 1 - intersection 1 with cone 2 - intersection 1
                        cone 1 - intersection 1 with cone 2 - intersection 2
                        cone 1 - intersection 2 with cone 2 - intersection 1
                        cone 1 - intersection 2 with cone 2 - intersection 2

                        cone 2 - intersection 1 with cone 2 - intersection 1
                        cone 2 - intersection 1 with cone 2 - intersection 2
                        cone 2 - intersection 2 with cone 2 - intersection 1
                        cone 2 - intersection 2 with cone 2 - intersection 2

                        Good luck.

                 */

                for (int i = 0; i < conesIntersectionList.size(); i++) {
                    if (i != ref) {
                        if (verbose) {
                            System.out.printf("---- i: %d\n", i);
                        }
                        MPSToolBox.ConesIntersection thatOne = conesIntersectionList.get(i);

                        // Cartesian product !

                        // Cone 1 - Cone 1
                        double oneOneDistOneOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionOne(), oneOneDistOneOne);
                        }
                        if (oneOneDistOneOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionOne());
                        }
                        double oneOneDistOneTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeOneIntersectionTwo(), oneOneDistOneTwo);
                        }
                        if (oneOneDistOneTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionTwo());
                        }
                        double oneOneDistTwoOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionOne(), oneOneDistTwoOne);
                        }
                        if (oneOneDistTwoOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionOne());
                        }
                        double oneOneDistTwoTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeOneIntersectionTwo(), oneOneDistTwoTwo);
                        }
                        if (oneOneDistTwoTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionTwo());
                        }

                        // Cone 1 - Cone 2
                        double oneTwoDistOneOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeTwoIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeTwoIntersectionOne(), oneTwoDistOneOne);
                        }
                        if (oneTwoDistOneOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionOne());
                        }
                        double oneTwoDistOneTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeTwoIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionOne(), thatOne.getConeTwoIntersectionTwo(), oneTwoDistOneTwo);
                        }
                        if (oneTwoDistOneTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionTwo());
                        }
                        double oneTwoDistTwoOne = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeTwoIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeTwoIntersectionOne(), oneTwoDistTwoOne);
                        }
                        if (oneTwoDistTwoOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionOne());
                        }
                        double oneTwoDistTwoTwo = GeomUtil.haversineNm(referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeTwoIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeTwoIntersectionTwo(), oneTwoDistTwoTwo);
                        }
                        if (oneTwoDistTwoTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionTwo());
                        }

                        // Cone 2 - Cone 1
                        double twoOneDistOneOne = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionOne(), twoOneDistOneOne);
                        }
                        if (twoOneDistOneOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionOne());
                        }
                        double twoOneDistOneTwo = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionTwo(), twoOneDistOneTwo);
                        }
                        if (twoOneDistOneTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionTwo());
                        }
                        double twoOneDistTwoOne = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeOneIntersectionOne(), oneOneDistTwoOne);
                        }
                        if (twoOneDistTwoOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionOne());
                        }
                        double twoOneDistTwoTwo = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionTwo(), thatOne.getConeOneIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionTwo(), thatOne.getConeOneIntersectionTwo(), twoOneDistTwoTwo);
                        }
                        if (twoOneDistTwoTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeOneIntersectionTwo());
                        }

                        // Cone 2 - Cone 2
                        double twoTwoDistOneOne = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeTwoIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeTwoIntersectionOne(), twoTwoDistOneOne);
                        }
                        if (twoTwoDistOneOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionOne());
                        }
                        double twoTwoDistOneTwo = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeTwoIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionOne(), thatOne.getConeTwoIntersectionTwo(), twoTwoDistOneTwo);
                        }
                        if (twoTwoDistOneTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionTwo());
                        }
                        double twoTwoDistTwoOne = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionTwo(), thatOne.getConeTwoIntersectionOne());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeTwoIntersectionTwo(), thatOne.getConeTwoIntersectionOne(), twoTwoDistTwoOne);
                        }
                        if (twoTwoDistTwoOne < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionOne());
                        }
                        double twoTwoDistTwoTwo = GeomUtil.haversineNm(referenceIntersection.getConeTwoIntersectionTwo(), thatOne.getConeTwoIntersectionTwo());
                        if (verbose) {
                            System.out.printf("ref: %d, i: %d. Between %s and %s, dist = %f\n", ref, i, referenceIntersection.getConeOneIntersectionTwo(), thatOne.getConeTwoIntersectionTwo(), twoTwoDistTwoTwo);
                        }
                        if (twoTwoDistTwoTwo < CRITICAL_DIST) {
                            candidates.add(thatOne.getConeTwoIntersectionTwo());
                        }
                    }
                }
            }
            // The result...
            if (verbose) {
                System.out.println("-----------------------------");
                System.out.printf("%d candidate(s):\n", candidates.size());
                candidates.stream().forEach(pt -> {
                    System.out.printf("\u2022 %s\n", pt);
                });
                System.out.println("-----------------------------");
            }
            // An average ?
            double averageLat = candidates.stream().mapToDouble(GeoPoint::getLatitude).average().getAsDouble();
            double averageLng = candidates.stream().mapToDouble(GeoPoint::getLongitude).average().getAsDouble();
            GeoPoint avgPoint = new GeoPoint(averageLat, averageLng);
            if (verbose) {
                System.out.printf("=> Average: %s\n", avgPoint);
            }
            return avgPoint;
        } else {
            throw new  MotEnoughIntersectionsException(String.format("Not enough intersections to process. Need at least 2, got %d", conesIntersectionList.size()));
        }
    }
}
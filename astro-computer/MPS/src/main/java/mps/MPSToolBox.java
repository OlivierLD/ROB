package mps;

import calc.*;
import calc.calculation.AstroComputerV2;
import calc.calculation.SightReductionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     *
     * @param calculationTime
     * @param obsAlt
     * @param gha
     * @param dec
     * @param bodyName
     * @param fromZ
     * @param toZ
     * @param zStep
     * @param verbose
     * @return
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

        // Find FS, distance from observer to summit.
        double FS = earthRadiusNM * (1 / Math.tan(Math.toRadians(obsAlt)));
        if (verbose) {
            System.out.printf("FS (obs to summit), in nautical miles: %.02f'\n", FS);
        }
        double coneDiameter = earthRadiusNM * Math.cos(Math.toRadians(obsAlt));
        if (verbose) {
            System.out.printf("Cone radius, in nautical miles: %.02f'\n", coneDiameter);
        }

        double earthCenterToConeSummit = Math.sqrt((FS * FS) + (earthRadiusNM * earthRadiusNM));

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

}
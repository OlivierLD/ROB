package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import calc.calculation.nauticalalmanacV2.Star;
import mps.MPSToolBox;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Helper:
 * ---------
 * From an input file (name at CLI), like inputSample.03.txt
 * For given bodies
 * For a given position and UTC date (duration fmt)
 * Returns ObsAlt
 */
public class PlayGround10 {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
     static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }
    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
     }

    /**
     *
     * @param duration, format YYYY-MM-DDTHH:MI:SS
     *                         |    |  |  |  |  |
     *                         |    |  |  |  |  17
     *                         |    |  |  |  14
     *                         |    |  |  11
     *                         |    |  8
     *                         |    5
     *                         0
     * @return The corresponding UTC Date
     * @throws Exception when there is a problem...
     */
    private static Calendar parseDuration(String duration) throws Exception {
        int year = Integer.parseInt(duration.substring(0, 4));
        int month = Integer.parseInt(duration.substring(5, 7));
        int day = Integer.parseInt(duration.substring(8, 10));
        int hours = Integer.parseInt(duration.substring(11, 13));
        int minutes = Integer.parseInt(duration.substring(14, 16));
        int seconds = Integer.parseInt(duration.substring(17));

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month - 1);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, hours); // and not just HOUR !!!!
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, seconds);

        return date;
    }

    private static boolean validateBody(String body) {
        boolean ok;

        switch (body) {
            case "Sun":
            case "Moon":
            case "Venus":
            case "Mars":
            case "Jupiter":
            case "Saturn":
                ok = true;
                break;
            default:
                Star star = Star.getStar(body);
                ok = star != null;
                break;
        }
        return ok;
    }

    private static class BodyData {
        String bodyName;
        Date date;
        Double gha;
        Double decl;
        Double obsAlt;

        public BodyData() {
        }
        public BodyData(String bodyName, Date date, Double gha, Double decl, Double obsAlt) {
            this.bodyName = bodyName;
            this.date = date;
            this.gha = gha;
            this.decl = decl;
            this.obsAlt = obsAlt;
        }

        public String getBodyName() {
            return bodyName;
        }

        public void setBodyName(String bodyName) {
            this.bodyName = bodyName;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Double getGha() {
            return gha;
        }

        public void setGha(Double gha) {
            this.gha = gha;
        }

        public Double getDecl() {
            return decl;
        }

        public void setDecl(Double decl) {
            this.decl = decl;
        }

        public Double getObsAlt() {
            return obsAlt;
        }

        public void setObsAlt(Double obsAlt) {
            this.obsAlt = obsAlt;
        }
    }
    public static BodyData computeBodyData(String body, String dateDuration, GeoPoint position) throws Exception {

        BodyData bodyData = new BodyData();
        bodyData.setBodyName(body);
        // bodyData.setObsAlt(obsAlt);

        AstroComputerV2 ac = new AstroComputerV2();

        Calendar date = parseDuration(dateDuration);
        bodyData.setDate(date.getTime());
        System.out.printf("Calculation launched for %s\n", SDF_UTC.format(date.getTime()));

        if (!validateBody(body)) {
            throw new Exception(String.format("Invalid body [%s]", body));
        }

        ac.calculate(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND),
                true);

        // double deltaT = ac.getDeltaT(); // Unused for now

        double gha, decl;

        switch (body) {
            case "Sun":
                gha = ac.getSunGHA();
                decl = ac.getSunDecl();
                break;
            case "Moon":
                gha = ac.getMoonGHA();
                decl = ac.getMoonDecl();
                break;
            case "Venus":
                gha = ac.getVenusGHA();
                decl = ac.getVenusDecl();
                break;
            case "Mars":
                gha = ac.getMarsGHA();
                decl = ac.getMarsDecl();
                break;
            case "Jupiter":
                gha = ac.getJupiterGHA();
                decl = ac.getJupiterDecl();
                break;
            case "Saturn":
                gha = ac.getSaturnGHA();
                decl = ac.getSaturnDecl();
                break;
            default:
                ac.starPos(body);
                // final double sha = ac.getStarSHA(body);
                gha = ac.getStarGHA(body);
                decl = ac.getStarDec(body);
                break;
        }
        CelestialDeadReckoning dr = MPSToolBox.calculateDR(gha, decl, position.getLatitude(), position.getLongitude()).calculate(); // All angles in degrees
        double he = dr.getHe();
        bodyData.setObsAlt(he);
        System.out.printf("For %s at %s, ObsAlt should be %f (%s)\n", body, SDF_UTC.format(date.getTime()), he, GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE));

        bodyData.setGha(gha);
        bodyData.setDecl(decl);

        return bodyData;
    }

    public static void main(String... args) {

        List<BodyData> listBodyData = new ArrayList<>();

        if (args.length < 1) {
            throw new RuntimeException("Please provide the prm file name as 1st CLI prm.");
        } else {
            File inputFile = new File(args[0]);
            if (!inputFile.exists()) {
                throw new RuntimeException(String.format("File %s was not found", inputFile.getAbsolutePath()));
            } else {
                try {
                    GeoPoint userPosition = null;
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String line = "";
                    while (line != null) {
                        line = br.readLine();
                        if (line != null && !line.startsWith("#")) {
                            if (line.startsWith("Position ")) {
                                // Line like [Position Latitude=N 47 40.66' Longitude=N 3º08.14' ]
                                int latitudeOffset = line.indexOf("Latitude=");
                                int longitudeOffset = line.indexOf("Longitude=");
                                double latitude = GeomUtil.sexToDec(line.substring(latitudeOffset + "Latitude=".length(), longitudeOffset).trim());
                                double longitude = GeomUtil.sexToDec(line.substring(longitudeOffset + "Longitude=".length()).trim());
                                userPosition = new GeoPoint(latitude, longitude);
                            } else {
                                if (userPosition == null) {
                                    throw new RuntimeException("User Position not defined yet. Should be the first uncommented line.");
                                }
                                AtomicReference<String> body = new AtomicReference<>(null);
                                AtomicReference<String> date = new AtomicReference<>(null);

                                System.out.printf("Read line [%s]\n", line);
                                final String[] split = line.split(";");
                                Arrays.asList(split).forEach(el -> {
                                    String one = el.trim();
                                    final String[] nameValue = one.split("=");
                                    if (nameValue.length != 2) {
                                        System.out.printf("Oops! Pb with [%s]\n", el);
                                    } else {
                                        switch (nameValue[0]) {
                                            case "Body":
                                                System.out.printf("\tBody : [%s]\n", nameValue[1]);
                                                body.set(nameValue[1]);
                                                break;
                                            case "Date":
                                                System.out.printf("\tDate : [%s]\n", nameValue[1]);
                                                date.set(nameValue[1]);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });
                                // Process ?
                                if (body.get() == null || date.get() == null) {
                                    throw new RuntimeException(String.format("Missing or invalid value in line [%s]", line));
                                } else {
                                    try {
                                        BodyData bodyData = computeBodyData(body.get(), date.get(), userPosition);
                                        listBodyData.add(bodyData);
                                    } catch (Exception ex) {
                                        // ex.printStackTrace();
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        }
                    }
                    br.close();
                } catch (FileNotFoundException fnfe) {
                    throw new RuntimeException(fnfe);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        }

        // Good to go ?
        System.out.printf("We have %d bodies.\n", listBodyData.size());
        listBodyData.forEach(bd-> System.out.printf("%s:\t At %s\t ObsAlt: %s (%f),\t GHA: %s (%f),\t Decl: %s (%f)\n",
                                                    bd.getBodyName(),
                                                    SDF_UTC.format(bd.getDate()),
                                                    GeomUtil.decToSex(bd.getObsAlt(), GeomUtil.SHELL, GeomUtil.NONE),
                                                    bd.getObsAlt(),
                                                    GeomUtil.decToSex(bd.getGha(), GeomUtil.SHELL, GeomUtil.NONE),
                                                    bd.getGha(),
                                                    GeomUtil.decToSex(bd.getDecl(), GeomUtil.SHELL, GeomUtil.NS),
                                                    bd.getDecl()));


        System.out.println("------- End of the story -------");
    }
}
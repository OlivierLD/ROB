package mps.pg;

import calc.CelestialDeadReckoning;
import calc.GeoPoint;
import calc.GeomUtil;
import calc.calculation.AstroComputerV2;
import calc.calculation.nauticalalmanacV2.Star;
import mps.MPSToolBox;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Full stuff, Invoking method from MPSToolBox, for more than 2 bodies.
 * Input is taken from a text file, provided at runtime.
 * Start from the cones, and find the position.
 * GHA and Decl are calculated in the main, for the given time.
 * Observer's position is found at the end.
 */
public class PlayGround09 {

    private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss 'UTC'");
     static {
        SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }
    private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
     }

    /*
     * Point of reference, user's position. FOR TESTS ONLY!!
     * Used for Observed Altitude calculation
     * Should be the one found at the end.
     */
    static double userLatitude = 47.677667;
    static double userLongitude = -3.135667;

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
     * @throws Exception
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
        boolean ok = true;

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
    public static BodyData computeBodyData(String body, String dateDuration, double obsAlt) throws Exception {

        BodyData bodyData = new BodyData();
        bodyData.setBodyName(body);
        bodyData.setObsAlt(obsAlt);

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

        double deltaT = ac.getDeltaT(); // Unused for now

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
        if (true) {
            CelestialDeadReckoning dr = MPSToolBox.calculateDR(gha, decl, userLatitude, userLongitude).calculate(); // All angles in degrees
            double he = dr.getHe();
            System.out.printf("For %s at %s, ObsAlt should be %f (%s)\n", body, SDF_UTC.format(date.getTime()), he, GeomUtil.decToSex(he, GeomUtil.SHELL, GeomUtil.NONE));
        }
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
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String line = "";
                    while (line != null) {
                        line = br.readLine();
                        if (line != null && !line.startsWith("#")) {
                            AtomicReference<String> body = new AtomicReference<>(null);
                            AtomicReference<String> date = new AtomicReference<>(null);
                            AtomicReference<Double> alt = new AtomicReference<>(null);
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
//                                            try {
//                                                Calendar date = parseDuration(nameValue[1]);
//                                                System.out.printf("Date is %s\n", date.getTime());
//                                            } catch (Exception ex) {
//                                                ex.printStackTrace();
//                                            }
                                            break;
                                        case "ObsAlt":
                                            double obsAlt;
                                            if (nameValue[1].contains("º")) {
                                                obsAlt = GeomUtil.sexToDec(nameValue[1]);
                                            } else {
                                                obsAlt = Double.parseDouble(nameValue[1]);
                                            }
                                            System.out.printf("\tAlt : [%s] => [%f]\n", nameValue[1], obsAlt);
                                            alt.set(obsAlt);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            });
                            // Process ?
                            if (body.get() == null || date.get() == null || alt.get() == null) {
                                throw new RuntimeException(String.format("Missing or invalid value in line [%s]", line));
                            } else {
                                try {
                                    BodyData bodyData = computeBodyData(body.get(), date.get(), alt.get());
                                    listBodyData.add(bodyData);
                                } catch (Exception ex) {
                                    // ex.printStackTrace();
                                    throw new RuntimeException(ex);
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
        listBodyData.forEach(bd-> System.out.printf("%s:\t ObsAlt: %s (%f),\t GHA: %s (%f),\t Decl: %s (%f)\n",
                                                    bd.getBodyName(),
                                                    GeomUtil.decToSex(bd.getObsAlt(), GeomUtil.SHELL, GeomUtil.NONE),
                                                    bd.getObsAlt(),
                                                    GeomUtil.decToSex(bd.getGha(), GeomUtil.SHELL, GeomUtil.NONE),
                                                    bd.getGha(),
                                                    GeomUtil.decToSex(bd.getDecl(), GeomUtil.SHELL, GeomUtil.NS),
                                                    bd.getDecl()));


        List<MPSToolBox.ConesIntersection> conesIntersectionList = new ArrayList<>();

        final long before = System.currentTimeMillis();

        /// Cones and Co here. All permutations.
        int nbProcess = 0;
        for (int i=0; i<listBodyData.size(); i++) {
            for (int j=0; j<listBodyData.size(); j++) {
                if (i != j) {
                    System.out.printf("[%d, %d], %s and %s\n", i, j, listBodyData.get(i).getBodyName(), listBodyData.get(j).getBodyName());
                    String bodyOne = listBodyData.get(i).getBodyName();
                    double altOne = listBodyData.get(i).getObsAlt(); // saturnObsAlt;
                    double ghaOne = listBodyData.get(i).getGha();    // saturnGHA;
                    double declOne = listBodyData.get(i).getDecl();  // saturnDecl;
                    Date dateOne = listBodyData.get(i).getDate();    // date.getTime();

                    String bodyTwo = listBodyData.get(j).getBodyName();
                    double altTwo = listBodyData.get(j).getObsAlt(); // jupiterObsAlt;
                    double ghaTwo = listBodyData.get(j).getGha();    // jupiterGHA;
                    double declTwo = listBodyData.get(j).getDecl();  // jupiterDecl;
                    Date dateTwo = listBodyData.get(j).getDate();    // date.getTime();

                    int nbIter = 4;
                    boolean reverse = false;
                    boolean verbose = false;

                    if (verbose) {
                        System.out.println("------------------------------------------------");
                        System.out.printf("Starting resolve process with:\n" +
                                        "Time1: %s, Alt1: %s, GHA1: %s, Decl1: %s\n" +
                                        "Time2: %s, Alt2: %s, GHA2: %s, Decl2: %s\n",
                                DURATION_FMT.format(dateOne),
                                GeomUtil.decToSex(altOne, GeomUtil.SHELL, GeomUtil.NONE),
                                GeomUtil.decToSex(ghaOne, GeomUtil.SHELL, GeomUtil.NONE),
                                GeomUtil.decToSex(declOne, GeomUtil.SHELL, GeomUtil.NS),
                                DURATION_FMT.format(dateTwo),
                                GeomUtil.decToSex(altTwo, GeomUtil.SHELL, GeomUtil.NONE),
                                GeomUtil.decToSex(ghaTwo, GeomUtil.SHELL, GeomUtil.NONE),
                                GeomUtil.decToSex(declTwo, GeomUtil.SHELL, GeomUtil.NS));
                        System.out.println("------------------------------------------------");
                    }
                    // Ephemeris and Altitudes OK, let's proceed.
                    double firstZStep = 0.1d;  // More than 0.1 not good enough...

                    // Now, find the intersection(s) of the two cones...
                    List<GeoPoint> closests = MPSToolBox.resolve2Cones(dateOne, altOne, ghaOne, declOne,
                                                                       dateTwo, altTwo, ghaTwo, declTwo,
                                                                       firstZStep, nbIter, reverse, verbose);

                    if (closests != null) {
                        final double d1 = GeomUtil.haversineNm(closests.get(0), closests.get(1));
                        final double d2 = GeomUtil.haversineNm(closests.get(2), closests.get(3));
                        System.out.printf("%d - %s & %s\n", ++nbProcess, bodyOne, bodyTwo);
                        System.out.printf("After %d iterations:\n", nbIter);
                        System.out.printf("1st position between %s (%s) and %s (%s), dist %.02f nm.\n", closests.get(0), closests.get(0).toNumericalString(), closests.get(1), closests.get(1).toNumericalString(), d1);
                        System.out.printf("2nd position between %s (%s) and %s (%s), dist %.02f nm.\n", closests.get(2), closests.get(2).toNumericalString(), closests.get(2), closests.get(2).toNumericalString(), d2);
                        // For later
                        conesIntersectionList.add(new MPSToolBox.ConesIntersection(bodyOne, bodyTwo,
                                                                                   closests.get(0), closests.get(1),
                                                                                   closests.get(2), closests.get(3)));
                    } else {
                        System.out.println("Oops ! Not found...");
                    }

                }
            }
        }
        System.out.printf("End of permutations, %d intersections\n", conesIntersectionList.size());
        final long after = System.currentTimeMillis();
        System.out.println("-----------------------------");
        System.out.printf("Full Intersection Calculation took %s ms (System Time)\n", NumberFormat.getInstance().format(after - before));

        // Now process all intersections...
        System.out.println("-----------------------------");

        try {
            GeoPoint avgPoint = MPSToolBox.processIntersectionsList(conesIntersectionList, false);
            System.out.printf("Found (avg) intersection at %s\n", avgPoint);
            if (false) {
                GeoPoint original = new GeoPoint(userLatitude, userLongitude);
                System.out.printf("=> Compare to original position: %s\n", original);

                System.out.printf("==> Difference/offset: %.02f nm\n", GeomUtil.haversineNm(original, avgPoint));
            }
        } catch (MPSToolBox.NotEnoughIntersectionsException mei) {
            mei.printStackTrace();
        }
        System.out.println("------- End of the story -------");
    }
}
package util;
/**
 * This would help you to generate the files you'd need to come up
 * with your own polars.
 */

import nmea.parser.StringParsers;
import nmea.parser.TrueWind;
import nmea.parser.VHW;

import java.io.*;
import java.text.NumberFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Scan a log file (in NMEA), and
 * extract the BSP for TWA/TWS
 */
public class LogToPolarPoints {

    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));

    private final static String FROM_PREFIX = "--from:";
    private final static String FROM_ZIP_PREFIX = "--from-zip:";
    private final static String PATH_IN_ZIP_PREFIX = "--path-in-zip:";
    private final static String TO_PREFIX = "--to:";

    private final static boolean NO_ZERO_BSP = "true".equals(System.getProperty("skip.bsp.00", "true"));

    private static InputStream getZipInputStream(String zipName, String entryName)
            throws Exception {
        ZipInputStream zip = new ZipInputStream(new FileInputStream(zipName));
        InputStream is = null;
        boolean go = true;
        while (go) {
            ZipEntry ze = zip.getNextEntry();
            if (ze == null) {
                go = false;
            } else {
                if (ze.getName().equals(entryName)) {
                    is = zip;
                    go = false;
                }
            }
        }
//		if (is == null) {
//			throw new FileNotFoundException("Entry " + entryName + " not found in " + zipName);
//		}
        return is;
    }

    public static void processFiles(String in, String out) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(in));
        try (br) {
            processFiles(br, out);
        }
    }

    public static void processFiles(String in, String path, String out) throws Exception {

        final InputStream zipInputStream = getZipInputStream(in, path);
        BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream));
        try (br) {
            processFiles(br, out);
        }
    }
    /**
     * Looks for TW & BSP, reset on RMC
     *
     * @param br NMEA Data in
     * @param out name of the file to produce
     * @throws Exception when error.
     */
    public static void processFiles(BufferedReader br, String out) throws Exception {

        BufferedWriter bw = new BufferedWriter(new FileWriter(out));

        String line;
        long nbLineProcessed = 0;
        long nbBSP = 0;
        long nbTW = 0;
        long nbRMC = 0;
        long outputRecords = 0;

        double minBSP = Double.MAX_VALUE;
        double maxBSP = 0;
        int minTWA = 180;
        int maxTWA = 0;
        double minTWS = Double.MAX_VALUE;
        double maxTWS = 0;

        TrueWind refTW = null;
        VHW refBSP = null;

        bw.write("[\n");

        boolean keepReading = true;
        while (keepReading) {
            line = br.readLine();
            if (line == null) {
                keepReading = false;
            } else {
                nbLineProcessed += 1;
                if (line.startsWith("!AI")) { // Skip AIS data
                    if (VERBOSE) {
                        System.out.println("Skipping AIS line");
                    }
                } else {
                    try {
                        final StringParsers.ParsedData parsedData = StringParsers.autoParse(line);
                        // MWV (T or AW), VWR (AW), RMC (SOG, COG), VHW (BSP, HDM), HDG
                        switch (parsedData.getSentenceId()) {
                            case "MWV":
                                if (parsedData.getParsedData() instanceof TrueWind) {
                                    TrueWind tw = (TrueWind) parsedData.getParsedData();
                                    refTW = tw;
                                    if (VERBOSE) {
                                        System.out.printf("True Wind: %s\n", tw);
                                    }
                                    nbTW += 1;
                                }
                                break;
                            case "VWR":
                            case "RMC":
                                nbRMC += 1;
                                if (refBSP != null && refTW != null) {
                                    // Write here
                                    // System.out.printf("TW: %s, BSP: %f\n", refTW, refBSP.getBsp());
                                    int twa = refTW.getAngle();
                                    if (twa > 180) {
                                        twa = 360 - twa;
                                    }
                                    final double tws = refTW.getSpeed();
                                    final double bsp = refBSP.getBsp();
                                    minBSP = Math.min(minBSP, bsp);
                                    maxBSP = Math.max(maxBSP, bsp);
                                    minTWS = Math.min(minTWS, tws);
                                    maxTWS = Math.max(maxTWS, tws);
                                    minTWA = Math.min(minTWA, twa);
                                    maxTWA = Math.max(maxTWA, twa);

                                    if (bsp > 0 || !NO_ZERO_BSP) {
                                        String outputData = String.format("{ \"twa\": %d, \"tws\": %f, \"bsp\": %f },\n", twa, tws, bsp);
                                        outputRecords += 1;
                                        bw.write(outputData);
                                    }
                                }
                                if (refBSP != null && refTW != null) {
                                    refBSP = null;
                                    refTW = null;
                                }
                                break;
                            case "VHW":
                                VHW vhw = (VHW) parsedData.getParsedData();
                                refBSP = vhw;
                                if (VERBOSE) {
                                    System.out.printf("BSP: %f\n", vhw.getBsp());
                                }
                                nbBSP += 1;
                                break;
                            case "HDG":
                            default:
                                break;
                        }
                    } catch (Exception ex) {
                        System.err.printf("Error processing [%s] :\n", line);
                        ex.printStackTrace();
                    }
                }
            }
        }
        bw.write("]\n");
        bw.close();

        System.out.println("--- Summary ---");
        System.out.printf("Processed %s line%s.\n", NumberFormat.getInstance().format(nbLineProcessed), nbLineProcessed > 1 ? "s" : "");
        System.out.printf("BSP: %s records, TW: %s records, RMC: %s records, written %s records.\n",
                NumberFormat.getInstance().format(nbBSP),
                NumberFormat.getInstance().format(nbTW),
                NumberFormat.getInstance().format(nbRMC),
                NumberFormat.getInstance().format(outputRecords));
        System.out.printf("BSP in [%f, %f]\n", minBSP, maxBSP);
        System.out.printf("TWS in [%f, %f]\n", minTWS, maxTWS);
        System.out.printf("TWA in [%d, %d]\n", minTWA, maxTWA);
        System.out.println("---------------");
    }

    public static void main(String... args) {

        String inputFileName = null;
        String inputZipFileName = null;
        String pathInZip = null;
        String outputFileName = null;
        for (String arg : args) {
            if (arg.startsWith(FROM_PREFIX)) {
                inputFileName = arg.substring(FROM_PREFIX.length());
            } else if (arg.startsWith(TO_PREFIX)) {
                outputFileName = arg.substring(TO_PREFIX.length());
            } else if (arg.startsWith(FROM_ZIP_PREFIX)) {
                inputZipFileName = arg.substring(FROM_ZIP_PREFIX.length());
            } else if (arg.startsWith(PATH_IN_ZIP_PREFIX)) {
                pathInZip = arg.substring(PATH_IN_ZIP_PREFIX.length());
            } else {
                System.out.printf("Unmanaged CLI parameter [%s]\n", arg);
            }
        }
        String errorMessage = "";
        if (outputFileName == null) {
            errorMessage += "Missing outputFileName.";
        }
        if (inputFileName == null && (inputZipFileName == null || pathInZip == null)) {
            errorMessage += ((errorMessage.length() > 0 ? "\n" : "") + "Please provide an Input File Name, or a Zip Input File Name plus a Path In Zip.");
        }
        if (inputFileName != null && inputZipFileName != null) {
            errorMessage += ((errorMessage.length() > 0 ? "\n" : "") + "Ambiguous. Zip, or not ?");
        }
        if (errorMessage.length() > 0) {
            throw new RuntimeException(errorMessage);
        }

        System.out.println("--- Summary --");
        System.out.printf("- Will read [%s]\n", (inputFileName != null) ? inputFileName : String.format("%s - %s", inputFileName, pathInZip));
        System.out.printf("- Will produce [%s]\n", outputFileName);
        // Now let's go.
        try {
            if (inputFileName != null) {
                processFiles(inputFileName, outputFileName);
            } else {
                processFiles(inputZipFileName, pathInZip, outputFileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Done!");
    }
}

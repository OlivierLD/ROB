package util;
/**
 * This would help you to generate the files you'd need to come up
 * with your own polars.
 */

import nmea.parser.StringParsers;
import nmea.parser.TrueWind;
import nmea.parser.VHW;
import util.swing.SwingFrame;

import java.awt.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Scan a log file (in NMEA), and
 * extract the BSP for TWA/TWS
 *
 * Can produce json, xml, csv, txt outputs (based on the extension of the output file name)
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

    public final static class PolarTriplet {
        double bsp;
        double twa;
        double tws;

        public PolarTriplet() {
        }

        public PolarTriplet(double bsp, double twa, double tws) {
            this.bsp = bsp;
            this.twa = twa;
            this.tws = tws;
        }

        public PolarTriplet bsp(double bsp) {
            this.bsp = bsp;
            return this;
        }
        public PolarTriplet twa(double twa) {
            this.twa = twa;
            return this;
        }
        public PolarTriplet tws(double tws) {
            this.tws = tws;
            return this;
        }

        public double getBsp() {
            return bsp;
        }

        public void setBsp(double bsp) {
            this.bsp = bsp;
        }

        public double getTwa() {
            return twa;
        }

        public void setTwa(double twa) {
            this.twa = twa;
        }

        public double getTws() {
            return tws;
        }

        public void setTws(double tws) {
            this.tws = tws;
        }
    }

    enum SupportedExtension {
        JSON, CSV, XML, TXT
    }
    private static SupportedExtension extensionToUse = SupportedExtension.JSON;

    public static List<PolarTriplet> processFiles(String in, String out) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(in));
        try (br) {
            return processFiles(br, out);
        }
    }

    public static List<PolarTriplet> processFiles(String in, String path, String out) throws Exception {

        final InputStream zipInputStream = getZipInputStream(in, path);
        BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream));
        try (br) {
            return processFiles(br, out);
        }
    }
    /**
     * Looks for TW & BSP, reset on RMC
     *
     * @param br NMEA Data in
     * @param out name of the file to produce
     * @throws Exception when error.
     */
    public static List<PolarTriplet> processFiles(BufferedReader br, String out) throws Exception {

        if (out.endsWith(".json")) {
            extensionToUse = SupportedExtension.JSON;
        } else if (out.endsWith(".xml")) {
            extensionToUse = SupportedExtension.XML;
        } else if (out.endsWith(".csv")) {
            extensionToUse = SupportedExtension.CSV;
        } else {
            extensionToUse = SupportedExtension.TXT;
        }

        List<PolarTriplet> plList = new ArrayList<>();
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

        if (extensionToUse == SupportedExtension.JSON) {
            bw.write("[\n");
        } else if (extensionToUse == SupportedExtension.XML) {
            bw.write("<polar-points>\n");
        } else if (extensionToUse == SupportedExtension.CSV) {
            bw.write("twa;tws;bsp\n");
        }

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
                                        plList.add(new PolarTriplet(bsp, twa, tws));
                                        outputRecords += 1;

                                        if (extensionToUse == SupportedExtension.JSON) {
                                            String outputData = String.format("%s{ \"twa\": %d, \"tws\": %f, \"bsp\": %f }\n", (outputRecords > 1 ? "," : ""), twa, tws, bsp);
                                            bw.write(outputData);
                                        } else if (extensionToUse == SupportedExtension.XML) {
                                            String outputData = String.format("  <twa>%d</twa> <tws>%f</tws> <bsp>%f</bsp>\n", twa, tws, bsp);
                                            bw.write(outputData);
                                        } else if (extensionToUse == SupportedExtension.CSV) {
                                            String outputData = String.format("%d; %f; %f\n", twa, tws, bsp);
                                            bw.write(outputData);
                                        } else {
                                            String outputData = String.format("twa: %d, tws: %f, bsp: %f\n", twa, tws, bsp);
                                            bw.write(outputData);
                                        }
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
        if (extensionToUse == SupportedExtension.JSON) {
            bw.write("]\n");
        } else if (extensionToUse == SupportedExtension.XML) {
            bw.write("</polar-points>\n");
//        } else if (extensionToUse == SupportedExtension.CSV) {
//            bw.write("bsp;twa;tws\n");
        }


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

        return plList;
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
            List<PolarTriplet> plList = null;
            if (inputFileName != null) {
                plList = processFiles(inputFileName, outputFileName);
            } else {
                plList = processFiles(inputZipFileName, pathInZip, outputFileName);
            }
            // Display result, graphically
            try {
                SwingFrame frame = new SwingFrame(plList, SwingFrame.DataOption.POLARS);
                frame.setVisible(true);
                frame.plot();
            } catch (HeadlessException he) {
                System.out.println("Headless Exception. Try in a graphical desktop environment to visualize the data.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Done!");
    }
}

package grib.processing.util;


import chart.components.util.World;
import coreutilities.Utilities;
import grib.data.GribDate;
import grib.data.GribType;
import jgrib.*;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;
import util.GnlUtilities;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


public class GRIBUtil {
    public GRIBUtil() {
    }

    private static HashMap<GribDate, HashMap<GribType, Float[][]>> buildGRIBMap(String fName)
            throws Exception {
        GribFile gribFile = new GribFile(fName);
        return buildGRIBMap(gribFile);
    }

    private static HashMap<GribDate, HashMap<GribType, Float[][]>> buildGRIBMap(GribFile gribFile)
            throws Exception {
        HashMap<GribDate, HashMap<GribType, Float[][]>> gribDataMap = null;

//  GribPDSParamTable.turnOffJGRIBLogging();

        TimeZone tz = TimeZone.getTimeZone("127"); // "GMT + 0"
        //  TimeZone.setDefault(tz);
        GnlUtilities.SDF.setTimeZone(tz);

        gribDataMap = new HashMap<GribDate, HashMap<GribType, Float[][]>>();

        for (int i = 0; i < gribFile.getLightRecords().length; i++) {
            try {
                GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
                GribRecordPDS grpds = gr.getPDS(); // Headers and Data
                GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
                GribRecordBDS grbds = gr.getBDS();

//      System.out.println("GRBDS:\n" + grbds.toString());

                Date date = grpds.getGMTForecastTime().getTime();
                int width = grgds.getGridNX();
                int height = grgds.getGridNY();
                double stepX = grgds.getGridDX();
                double stepY = grgds.getGridDY();
                double top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
                double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
                double left = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
                double right = Math.max(grgds.getGridLon1(), grgds.getGridLon2());

                if (Math.abs(left - right) > 180) { // Swap
                    double tmp = left;
                    left = right;
                    right = tmp;
                }

                String type = grpds.getType();
                String description = grpds.getDescription();
                String unit = grpds.getUnit();

//      System.out.println("Type:" + type + ", " + description + ", " + unit);
//      System.out.println("Between " + grbds.getMinValue() + " and " + grbds.getMaxValue());

                GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

                Float[][] data = new Float[height][width];
                float val = 0F;
                for (int col = 0; col < width; col++) {
                    for (int row = 0; row < height; row++) {
                        try {
                            val = gr.getValue(col, row);
                            if (val > grbds.getMaxValue()) {
                                // then what?
//              System.out.println("Exceed for " + type + ", val:" + val + " min:" + grbds.getMinValue() + " max:" + grbds.getMaxValue());
                                assert "htsgw".equals(type);
                                val = 0f;
                            }
                            data[row][col] = val;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                HashMap<GribType, Float[][]> subMap = gribDataMap.get(date);
                if (subMap == null) {
                  subMap = new HashMap<GribType, Float[][]>();
                }
//        subMap = new HashMap<String, Float[][]>();
                subMap.put(new GribType(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
//      subMap.put(type, data);
                gribDataMap.put(gDate, subMap);
            } catch (NoValidGribException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotSupportedException e) {
                e.printStackTrace();
            }
        }
        return gribDataMap;
    }

    public static double[] getBoundaries(HashMap<GribDate, HashMap<GribType, Float[][]>> gdm) {
        double[] ret = null;
        if (gdm != null) {
            Set<GribDate> keys = gdm.keySet();
            Iterator<GribDate> iterator = keys.iterator();
            while (iterator.hasNext()) {
                GribDate first = iterator.next();
                ret = new double[]{first.getTop(), first.getBottom(), first.getLeft(), first.getRight()};
                break;
            }
        }
        return ret;
    }

    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private final static String FILE_HEADER_NB_DATE = "FileHeaderNbDate:";
    private final static String DATE_HEADER = "DateHeader:";
    private final static String DATE_HEADER_NB_TYPE = "DateHeaderNbType:";
    private final static String DATA_TYPE_HEADER = "DataTypeHeader:";
    private final static String DATA_TYPE_DIM = "DataTypeDim:";
    private final static String END_OF_GRIB_DATA = "EndOFGribData";
    private final static String CHART_HEADER = "ChartHeader:";
    private final static String END_OF_CHART_DATA = "EndOFChartData";

    private static void buildGRIBMap(String gribName, String asciiFileName)
            throws Exception {
        buildGRIBMap(gribName, asciiFileName, false);
    }

    /*
     * Turns a GRIB file is ascii, for use from an Applet or PApplet.
     */
    public static void buildGRIBMap(String gribName, String asciiFileName, boolean withChart)
            throws Exception {
        GribFile gribFile = new GribFile(gribName);
        buildGRIBMap(gribFile, asciiFileName, withChart);
    }

    public static void buildGRIBMap(GribFile gribFile, String asciiFileName, boolean withChart)
            throws Exception {
        HashMap<GribDate, HashMap<GribType, Float[][]>> map = buildGRIBMap(gribFile);

        File f = new File(asciiFileName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
//  System.out.println("Created " + asciiFileName);

        Set<GribDate> keys = map.keySet();
        GribDateComparator gdc = new GribDateComparator();
        List<GribDate> lkeys = new ArrayList<GribDate>(keys);
        Collections.sort(lkeys, gdc);
        bw.write(FILE_HEADER_NB_DATE + lkeys.size() + "\n");
        for (GribDate gd : lkeys) {
            bw.write(DATE_HEADER + SDF.format(gd.getGDate()) + "\n");
            HashMap<GribType, Float[][]> submap = map.get(gd);
            Set<GribType> subKeys = submap.keySet();
            List<GribType> lsubKeys = new ArrayList<GribType>(subKeys);
            bw.write(DATE_HEADER_NB_TYPE + lsubKeys.size() + "\n");
            GribTypeComparator gtc = new GribTypeComparator();
            Collections.sort(lsubKeys, gtc);
            for (GribType gt : lsubKeys) {
                bw.write(DATA_TYPE_HEADER + gt.getType() + "\n"); // + ", desc:" + gt.getDesc() + ", unit:" + gt.getUnit() + "\n");
                Float[][] data = submap.get(gt);
                bw.write(DATA_TYPE_DIM + data.length + " x " + data[0].length + "\n");
//      System.out.println("-- Data:" + data.length + " x " + data[0].length);
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[i].length; j++) {
                        bw.write(data[i][j] + " ");
                    }
                    bw.write("\n");
                }
            }
        }
        bw.write(END_OF_GRIB_DATA + "\n");

        // The Chart
        if (withChart) {
            double[] boundaries = getBoundaries(map);
            double topLat = boundaries[0];
            double bottomLat = boundaries[1];
            double leftLong = boundaries[2];
            double rightLong = boundaries[3];

            System.out.println("Boundaries: " + topLat + ", " + bottomLat + " and " + leftLong + ", " + rightLong);

            URL data = World.class.getResource("data.xml");
            DOMParser parser = new DOMParser();
            parser.parse(data);
            XMLDocument doc = parser.getDocument();
            String xPath = "//section[./point/Lat < " + Double.toString(topLat) + " and " +
                    "./point/Lat > " + Double.toString(bottomLat) + "]"; // " and " +
            //    "((./point/Lng mod 180) > " + Double.toString(rightLong) + " or " +
            //     "(./point/Lng mod 180) < " + Double.toString(leftLong) + ")]";
            System.out.println("XPath:" + xPath);
            NodeList sectionList = doc.selectNodes(xPath);
            System.out.println("Found " + sectionList.getLength() + " node(s)");
            if (sectionList.getLength() > 0) {
                bw.write(CHART_HEADER +
                        Double.toString(topLat) + " " +
                        Double.toString(bottomLat) + " " +
                        Double.toString(rightLong) + " " +
                        Double.toString(leftLong) + "\n");

                for (int i = 0; i < sectionList.getLength(); i++) {
                    XMLElement section = (XMLElement) sectionList.item(i);
                    NodeList pointList = section.selectNodes("./point");
                    StringBuffer bf = new StringBuffer();
                    for (int j = 0; j < pointList.getLength(); j++) {
                        XMLElement point = (XMLElement) pointList.item(j);
                        double lat = Double.parseDouble(point.getElementsByTagName("Lat").item(0).getFirstChild().getNodeValue());
                        double lng = Double.parseDouble(point.getElementsByTagName("Lng").item(0).getFirstChild().getNodeValue());
                        if (lng < -180d) {
                          lng = 360d + lng;
                        }
                        if (lat >= bottomLat && lat <= topLat && isBetween(leftLong, rightLong, lng)) { // Then write it
                            if (bf.length() > 0) {
                              bf.append("|");
                            }
                            bf.append(Double.toString(lat) + "," + Double.toString(lng));
                        }
                    }
                    if (bf.length() > 0) {
                      bw.write(bf.toString() + "\n");
                    }
                }
                bw.write(END_OF_CHART_DATA + "\n");
            }
        }
        System.out.println("Closing " + f.getAbsolutePath());
        bw.close();
    }

    public static void readAsciiGRIB(String asciiFileName) throws Exception {
        File f = new File(asciiFileName);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = "";
        boolean keepReading = true;
        while (keepReading) {
            line = br.readLine();
            if (line == null) {
              keepReading = false;
            } else {
                if (line.startsWith(FILE_HEADER_NB_DATE)) {
                    String nbd = line.substring(FILE_HEADER_NB_DATE.length());
                    System.out.println(nbd + " Date(s)");
                } else if (line.startsWith(DATE_HEADER)) {
                    String dateStr = line.substring(DATE_HEADER.length());
                    System.out.println(dateStr);
                } else if (line.startsWith(DATE_HEADER_NB_TYPE)) {
                    String nbTypes = line.substring(DATE_HEADER_NB_TYPE.length());
                    System.out.println(nbTypes);
                } else if (line.startsWith(DATA_TYPE_HEADER)) {
                    String type = line.substring(DATA_TYPE_HEADER.length());
                    System.out.println(type);
                } else if (line.startsWith(DATA_TYPE_DIM)) {
                    String dim = line.substring(DATA_TYPE_DIM.length());
                    System.out.println(dim);
                    StringTokenizer st = new StringTokenizer(dim, "x");

                    while (st.hasMoreTokens()) {
                        System.out.println("-> " + st.nextToken().trim());
                    }
                } else { // Data
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        System.out.println(st.nextToken().trim());
                    }
                }
            }
        }
        br.close();
    }

    private static boolean isBetween(double left, double right, double lng) {
        double r = right, l = left, longitude = lng;
        if (Utilities.sign(left) != Utilities.sign(right) && r < 0D) { // On each side of the anti-meridian
            r += 360D;
            if (longitude < 0) longitude += 360D;
        }
        return (longitude > l && longitude < r);
    }

    private static class GribDateComparator implements Comparator<GribDate> {
        public int compare(GribDate gd1, GribDate gd2) {
            return gd1.getGDate().compareTo(gd2.getGDate());
        }
    }

    private static class GribTypeComparator implements Comparator<GribType> {
        public int compare(GribType gt1, GribType gt2) {
            return gt1.getType().compareTo(gt2.getType());
        }
    }
}

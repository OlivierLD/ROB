package util;


import calc.GeoPoint;
import nmea.parser.StringParsers;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.TimeUtil;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GPXUtil {
    private final static boolean USE_SWING_DIALOG = "true".equals(System.getProperty("use-swing"));
    private final static String GPX_NS = "http://www.topografix.com/GPX/1/1";
    private static DOMParser parser;

    public static void createParser() {
        parser = new DOMParser();
        parser.setValidationMode(XMLParser.NONVALIDATING);
    }

    public static class DatedPoint {
        GeoPoint pt;
        Date date;

        public DatedPoint() {
        }

        public DatedPoint(GeoPoint pt, Date date) {
            this.pt = pt;
            this.date = date;
        }

        public GeoPoint getPt() {
            return pt;
        }

        public void setPt(GeoPoint pt) {
            this.pt = pt;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
    public static List<DatedPoint> parseGPXData(URL url, long from, long to) {
        if (parser == null) {
            createParser();
        }
        List<DatedPoint> ret = new ArrayList<>();
        XMLDocument gpx = null;

        synchronized (parser) {
            try {
                parser.parse(url);
                gpx = parser.getDocument();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (gpx != null) {
            try {
                NSResolver nsr = new NSResolver() {
                    public String resolveNamespacePrefix(String string) {
                        return GPX_NS;
                    }
                };
                boolean track = true;
                NodeList nl = gpx.selectNodes("//gpx:trkpt[./gpx:type = 'WPT']", nsr);
                if (nl.getLength() == 0) { // Then try a route (was a track)
                    nl = gpx.selectNodes("//gpx:rtept[./gpx:type = 'WPT']", nsr);
                    track = false;
                }
                if (nl.getLength() == 0) { // Then try trkpt without type (trkseg ?)
                    nl = gpx.selectNodes("//gpx:trkpt", nsr);
                    track = true;
                }
                for (int i = 0; i < nl.getLength(); i++) {
                    XMLElement xe = (XMLElement) nl.item(i);
                    double lat = Double.parseDouble(xe.getAttribute("lat"));
                    double lon = Double.parseDouble(xe.getAttribute("lon"));
                    long time = -1L;
                    try {
                        NodeList nodeList = xe.selectNodes("./gpx:time", nsr);
                        Node timeNode = nodeList.item(0);
                        String duration = timeNode.getFirstChild().getNodeValue(); // ((XMLElement)timeNode).getTextContent();
                        time = StringParsers.durationToDate(duration);
                        int tuOffset = TimeUtil.getGMTOffset();
                        time += (tuOffset * (3_600L * 1_000L));
                    } catch (Exception ex) {
                        System.out.println("Finding time:" + ex.toString());
                    }
                    Date date = null;
                    if (track) {
                        TimeZone tz = TimeZone.getDefault();
                        TimeZone.setDefault(TimeZone.getTimeZone("127"));
                        date = new Date(time);
                        // System.out.println("Date:" + new Date(time).toString());
                        TimeZone.setDefault(tz); // Reset
                    }
                    boolean add = !track || from <= -1L || time >= from;
                    if (track && add && to > -1L && time > to) {
                        add = false;
                    }
                    if (add) {
                        GeoPoint gp = new GeoPoint(lat, lon);
                        ret.add(new DatedPoint(gp, date));
                    }
                }
                if (USE_SWING_DIALOG) {
                    JOptionPane.showMessageDialog(null, "Added " + ret.size() + " points", "GPX Parsing", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.toString(), "GPX Parsing", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        return ret;
    }

    private final static int FIRST = 0;
    private final static int LAST = 1;

    public static Date getFirstDate(URL url) {
        return getDate(url, FIRST);
    }

    public static Date getLastDate(URL url) {
        return getDate(url, LAST);
    }

    public static Date getDate(URL url, int opt) {
        Date date = null;
        if (parser == null) {
            createParser();
        }
        XMLDocument gpx = null;

        synchronized (parser) {
            try {
                parser.parse(url);
                gpx = parser.getDocument();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (gpx != null) {
            try {
                NSResolver nsr = new NSResolver() {
                    public String resolveNamespacePrefix(String string) {
                        return GPX_NS;
                    }
                };
                String xpath = "//gpx:trkseg[position() = last()]//gpx:trkpt[position() = last()]/gpx:time";
                if (opt == FIRST) {
                    xpath = "//gpx:trkseg[position() = 1]//gpx:trkpt[position() = 1]/gpx:time";
                }
                NodeList nl = gpx.selectNodes(xpath, nsr);
                String strDate = "";
                strDate = ((XMLElement) nl.item(0)).getFirstChild().getNodeValue(); // .getTextContent();
                long time = StringParsers.durationToDate(strDate);
                int tuOffset = TimeUtil.getGMTOffset();
                time += (tuOffset * (3_600L * 1_000L));
                if (true) {
                    TimeZone tz = TimeZone.getDefault();
                    TimeZone.setDefault(TimeZone.getTimeZone("127"));
                    // System.out.println("Date:" + new Date(time).toString());
                    TimeZone.setDefault(tz); // Reset
                }
                date = new Date(time);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return date;
    }

    public static void main(String... args) {
        GPXUtil.createParser();
        try {
            String fName = "/sample-data/patrick/LeHavre-Fecamp-Final-20-06.gpx";
            //             "/sample-data/patrick/Dives-LeHavre01.gpx";
            String testData = String.format("%s%s", System.getProperty("user.dir"), fName);
            System.out.printf("Reading %s\n", testData);
            URL gpxURL = new File(testData).toURI().toURL();
            List<DatedPoint> algp = GPXUtil.parseGPXData(gpxURL, -1L, -1L);
            System.out.println("Returned " + algp.size() + " points.");

            TimeZone tz = TimeZone.getDefault(); // Original one
            TimeZone.setDefault(TimeZone.getTimeZone("127")); // GMT. Set at the default level

            System.out.printf("Between %s and %s\n", GPXUtil.getFirstDate(gpxURL), GPXUtil.getLastDate(gpxURL));

            TimeZone.setDefault(tz); // Reset
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
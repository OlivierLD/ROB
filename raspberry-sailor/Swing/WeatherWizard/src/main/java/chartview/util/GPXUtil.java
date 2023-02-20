package chartview.util;


import calc.GeoPoint;
import chartview.ctx.WWContext;
import nmea.parser.StringParsers;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
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
    private final static String GPX_NS = "http://www.topografix.com/GPX/1/1";

    public static List<GeoPoint> parseGPXData(URL url, long from, long to) {
        List<GeoPoint> ret = new ArrayList<>();
        DOMParser parser = WWContext.getInstance().getParser();
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
                for (int i = 0; i < nl.getLength(); i++) {
                    XMLElement xe = (XMLElement) nl.item(i);
                    double lat = Double.parseDouble(xe.getAttribute("lat"));
                    double lon = Double.parseDouble(xe.getAttribute("lon"));
                    long time = -1L;
                    try {
                        time = StringParsers.durationToDate(xe.selectNodes("./gpx:time", nsr).item(0).getTextContent());
                        int tuOffset = TimeUtil.getGMTOffset();
                        time += (tuOffset * (3_600L * 1_000L));
                    } catch (Exception ex) {
                        System.out.println("Finding time:" + ex.toString());
                    }
                    if (track) {
                        TimeZone tz = TimeZone.getDefault();
                        TimeZone.setDefault(TimeZone.getTimeZone("127"));
                        // System.out.println("Date:" + new Date(time).toString());
                        TimeZone.setDefault(tz); // Reset
                    }
                    boolean add = !track || from <= -1L || time >= from;
                    if (track && add && to > -1L && time > to) {
                        add = false;
                    }
                    if (add) {
                        GeoPoint gp = new GeoPoint(lat, lon);
                        ret.add(gp);
                    }
                }
                JOptionPane.showMessageDialog(null, "Added " + ret.size() + " points", "GPX Parsing", JOptionPane.INFORMATION_MESSAGE);
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
        DOMParser parser = WWContext.getInstance().getParser();
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
                strDate = ((XMLElement) nl.item(0)).getNodeValue(); // .getTextContent();
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
        try {
            String testData = "leg.02.gpx";
            URL gpxURL = new File(testData).toURI().toURL();
            List<GeoPoint> algp = GPXUtil.parseGPXData(gpxURL, -1L, -1L);
            System.out.println("Returned " + algp.size() + " points.");

            TimeZone tz = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("127"));
            System.out.println("Last Date:" + GPXUtil.getLastDate(gpxURL));
            TimeZone.setDefault(tz); // Reset
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

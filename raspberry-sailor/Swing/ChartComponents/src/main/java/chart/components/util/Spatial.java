package chart.components.util;

import calc.GeoPoint;
import chart.components.ui.ChartPanelInterface;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.net.URL;
import java.util.StringTokenizer;

//import org.w3c.dom.Node;

// Referenced classes of package chart.components.util:
//      GeoPoint

public final class Spatial {
    public enum Chart {
        WEST_COAST("US West Coast", "CaliforniaCoast.xml"),
        BAY_AREA("SF Bay Area", "bayArea.xml");

        @SuppressWarnings("compatibility:-7549378910685392414")
        public final static long serialVersionUID = 1L;

        private final String label;
        private final String resource;

        Chart(String label, String resource) {
            this.label = label;
            this.resource = resource;
        }

        public String label() {
            return this.label;
        }

        public String resource() {
            return this.resource;
        }
    }

    private static final String XPATH = "//Region/geometry//coordinates";

    private String fileName = "CaliforniaCoast.xml"; // Default
    private NodeList nl;

    public Spatial() {
        nl = null;
    }

    public Spatial(Chart chart) {
        nl = null;
        fileName = chart.resource();
    }

    public void drawChart(ChartPanelInterface cpi, Graphics gr) {
        drawChart(cpi, gr, 0, null);
    }

    public void drawChart(ChartPanelInterface cpi, Graphics gr, int sect, Color c) {
        Color origColor = cpi.getChartColor();
        double maxL = -Double.MAX_VALUE;
        double minL = Double.MAX_VALUE;
        double maxG = -Double.MAX_VALUE;
        double minG = Double.MAX_VALUE;
        try {
            if (nl == null) {
                URL data = this.getClass().getResource(fileName);
                DOMParser parser = new DOMParser();
                parser.parse(data);
                XMLDocument doc = parser.getDocument();
                nl = doc.selectNodes(XPATH);
            }
            for (int i = 0; i < nl.getLength(); i++) {
                gr.setColor(origColor);
                if (c != null && i == sect) {
                    gr.setColor(c);
                }
                XMLElement section = (XMLElement) nl.item(i);
                String bigCoordinateString = section.getFirstChild().getNodeValue();
                StringTokenizer st = new StringTokenizer(bigCoordinateString);
                Point previous = null;
                Point first = null;
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					double g = Double.parseDouble(token.substring(0, token.indexOf(',') - 1));
					double l = Double.parseDouble(token.substring(token.indexOf(',') + 1));
					if (g < minG) {
						minG = g;
					}
					if (g > maxG) {
						maxG = g;
					}
					if (l < minL) {
						minL = l;
					}
					if (l > maxL) {
						maxL = l;
					}
					Point gpt = cpi.getPanelPoint(l, g);
					if (previous != null && cpi.contains(new GeoPoint(l, g))) {
						gr.drawLine(previous.x, previous.y, gpt.x, gpt.y);
					}
					previous = gpt;
					if (first == null) {
						first = gpt;
					}
				}
                if (previous != null) {
                    gr.drawLine(first.x, first.y, previous.x, previous.y);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
//    System.out.println("Chart:");
//    System.out.println("From L:" + minL + " to " + maxL);
//    System.out.println("From G:" + minG + " to " + maxG);
    }
}

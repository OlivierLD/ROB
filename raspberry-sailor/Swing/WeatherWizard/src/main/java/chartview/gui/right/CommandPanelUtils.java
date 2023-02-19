package chartview.gui.right;

import calc.GeoPoint;
import calc.GeomUtil;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelInterface;
import chartview.ctx.WWContext;
import chartview.gui.toolbar.controlpanels.LoggingPanel;
import chartview.gui.util.dialog.FaxType;
import chartview.gui.util.dialog.PrintOptionsPanel;
import chartview.gui.util.dialog.TwoFilePanel;
import chartview.gui.util.dialog.places.PlacesTablePanel;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.gui.util.print.print.PrintUtilities;
import chartview.util.ImageUtil;
import chartview.util.RelativePath;
import chartview.util.SearchUtil;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;
import chartview.util.http.HTTPClient;
import chartview.util.http.HTTPClient.CannotWriteException;
import coreutilities.Utilities;
import jgrib.GribFile;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommandPanelUtils {
    private static Font dataFont = new Font("Tahoma", Font.BOLD, 12);
    private static Font titleFont = new Font("Tahoma", Font.BOLD, 12);
    private static final int ALT_WINDOW_HEADER_SIZE = 30;
    private static final int ALT_WINDOW_BORDER_SIZE = 5;
    private static final int ALT_WINDOW_DATA_OFFSET_SIZE = 2;
    private static final int ALT_WINDOW_MIN_HEIGHT = 100;
    private static final int ALT_WINDOW_MIN_WIDTH = 100;
    private static final int ALT_WINDOW_MIN_NUM_LINES = 8;
    private static final int ALT_WINDOW_TITLE_MIN_BASELINE = 20;
    private static final int ALT_WINDOW_TITLE_OFFSET = 10;

    private static final int ALT_WINDOW_3BUTTON_WIDTH = 80;

    private static int altTooltipX = 10;
    private static int altTooltipY = 10;
    private static int altTooltipW = ALT_WINDOW_MIN_WIDTH;
    private static int altTooltipH = ALT_WINDOW_MIN_HEIGHT;

    private final static ImageIcon closeImage = new ImageIcon(CommandPanelUtils.class.getResource("close.gif"));
    private final static ImageIcon zoomInImage = new ImageIcon(CommandPanelUtils.class.getResource("zoomexpand.gif"));
    private final static ImageIcon zoomOutImage = new ImageIcon(CommandPanelUtils.class.getResource("zoomshrink.gif"));
    private final static int buttonWidth = 15;

    public final static int CLOSE_IMAGE = 1;
    public final static int ZOOMEXPAND_IMAGE = 2;
    public final static int ZOOMSHRINK_IMAGE = 3;

    public enum BackGround {
        MERCATOR_GREENWICH_CENTERED("GREENWICH_CENTERED_MERCATOR_BG", "background/world.1.jpg"),
        MERCATOR_ANTIMERIDIAN_CENTERED("180_CENTERED_MERCATOR_BG", "background/world.2.jpg"),
        MERCATOR_NE_ATLANTIC("BG_MERCATOR_NE_ATLANTIC_ALIAS", "background/NEAtlantic.png"),
        CALIFORNIA_COAST_MAP("CALIFORNIA_COAST_MAP", "background/CalCoastMap.png"),
        CALIFORNIA_COAST_SAT("CALIFORNIA_COAST_SAT", "background/CalCoastSat.png");

        @SuppressWarnings("compatibility:8619290224382482962")
        public final static long serialVersionUID = 1L;

        private final String label;
        private final String resource;

        BackGround(String label, String resource) {
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

    public static void setDisplayAltTooltip(ChartPanel chartPanel, Graphics graphics, String winTitle, String dataString) {
        int imageWidth = 24;
        Color endColor = new Color(0.0f, 0.0f, 0.05f, 0.75f);
        Color startColor = new Color(0.0f, 0.0f, 0.75f, 0.25f);

        altTooltipW = ALT_WINDOW_MIN_WIDTH;
        altTooltipH = ALT_WINDOW_MIN_HEIGHT;
        // Measure dimensions, based on the title and the data to display.
        if (winTitle != null) {
            int strWidth = graphics.getFontMetrics(titleFont).stringWidth(winTitle);
            if ((strWidth + ALT_WINDOW_TITLE_OFFSET + ALT_WINDOW_3BUTTON_WIDTH) > altTooltipW) {
                altTooltipW = strWidth + ALT_WINDOW_TITLE_OFFSET + ALT_WINDOW_3BUTTON_WIDTH;
            }
        }

        if (dataString != null) {
            graphics.setFont(dataFont);
            String[] dataLine = dataString.split("\n");
            int strHeight = dataFont.getSize();
            int progressWidth = 0;
            for (String s : dataLine) {
                int strWidth = graphics.getFontMetrics(dataFont).stringWidth(s);
                if (strWidth > progressWidth) {
                    progressWidth = strWidth;
                }
            }
            if ((progressWidth + (2 * ALT_WINDOW_DATA_OFFSET_SIZE) + (2 * ALT_WINDOW_BORDER_SIZE)) > altTooltipW) {
                altTooltipW = (progressWidth + (2 * ALT_WINDOW_DATA_OFFSET_SIZE) + (2 * ALT_WINDOW_BORDER_SIZE));
            }
            int nl = dataLine.length;
            if (nl < ALT_WINDOW_MIN_NUM_LINES) {
                nl = ALT_WINDOW_MIN_NUM_LINES;
            }
            if (((nl * (strHeight + 2)) + ALT_WINDOW_HEADER_SIZE + (2 * ALT_WINDOW_BORDER_SIZE)) > altTooltipH) {
                altTooltipH = ((nl * (strHeight + 2)) + ALT_WINDOW_HEADER_SIZE + (2 * ALT_WINDOW_BORDER_SIZE));
            }
        }
        //  System.out.println("Repainting AltWin:" + altTooltipX + ", " + altTooltipY);

        int x = (int) chartPanel.getVisibleRect().getX();
        int y = (int) chartPanel.getVisibleRect().getY();

        GradientPaint gradient = new GradientPaint(x + altTooltipX, y + altTooltipY, startColor, x + altTooltipX + altTooltipH, y + altTooltipY + altTooltipW, endColor); // Diagonal, top-left to bottom-right
//  GradientPaint gradient = new GradientPaint(x + altTooltipX, x + altTooltipX + altTooltipH, startColor, y + altTooltipY + altTooltipW, y + altTooltipY, endColor); // Horizontal
//  GradientPaint gradient = new GradientPaint(x + altTooltipX, y + altTooltipY, startColor, x + altTooltipX, x + altTooltipX + altTooltipH, endColor); // vertical
//  GradientPaint gradient = new GradientPaint(x + altTooltipX, x + altTooltipX + altTooltipH, startColor, x + altTooltipX, y + altTooltipY, endColor); // vertical, upside down
        ((Graphics2D) graphics).setPaint(gradient);

        //  Color bgColor = new Color(0.0f, 0.0f, 0.75f, 0.55f);
        //  graphics.setColor(bgColor);
        graphics.fillRoundRect(x + altTooltipX, y + altTooltipY, altTooltipW, altTooltipH, 10, 10);

        int xi = altTooltipX + altTooltipW - (imageWidth);
        int yi = altTooltipY;
        graphics.drawImage(closeImage.getImage(), x + xi, y + yi, null);

        xi = altTooltipX + altTooltipW - (2 * imageWidth);
        yi = altTooltipY;
        graphics.drawImage(zoomInImage.getImage(), x + xi, y + yi, null);

        xi = altTooltipX + altTooltipW - (3 * imageWidth);
        yi = altTooltipY;
        graphics.drawImage(zoomOutImage.getImage(), x + xi, y + yi, null);

        // The data frame (area)
        int xs = x + altTooltipX + ALT_WINDOW_BORDER_SIZE;
        int ys = y + altTooltipY + ALT_WINDOW_HEADER_SIZE;
        graphics.setColor(new Color(1f, 1f, 1f, 0.5f));
        graphics.fillRoundRect(xs,
                ys,
                altTooltipW - (2 * ALT_WINDOW_BORDER_SIZE),
                altTooltipH - ((2 * ALT_WINDOW_BORDER_SIZE) + ALT_WINDOW_HEADER_SIZE),
                10, 10);

        chartPanel.setPositionToolTipEnabled(false);
        // Win Title here
        if (winTitle != null) {
            graphics.setFont(titleFont);
            graphics.setColor(Color.white);
            int baseLine = ALT_WINDOW_TITLE_MIN_BASELINE;
            if ((titleFont.getSize() + 2) > baseLine) {
                baseLine = titleFont.getSize() + 2;
            }
            graphics.drawString(winTitle, x + altTooltipX + ALT_WINDOW_TITLE_OFFSET, y + altTooltipY + baseLine);
        }
        // Draw Data Here
        if (dataString != null) {
            graphics.setFont(dataFont);
            String[] dataLine = dataString.split("\n");
            graphics.setColor(Color.blue);
            for (int i = 0; i < dataLine.length; i++) {
                graphics.drawString(dataLine[i],
                        xs + ALT_WINDOW_DATA_OFFSET_SIZE,
                        ys + dataFont.getSize());
                ys += (dataFont.getSize() + 2);
            }
        }
    }

    public static boolean isMouseInAltWindow(MouseEvent me, boolean displayAltTooltip, ChartPanel chartPanel) {
        boolean resp = false;
        if (displayAltTooltip) {
            int x = me.getX() - (int) chartPanel.getVisibleRect().getX(),
                    y = me.getY() - (int) chartPanel.getVisibleRect().getY();
            if (x > altTooltipX &&
                    y > altTooltipY &&
                    x < (altTooltipX + altTooltipW) &&
                    y < (altTooltipY + altTooltipH)) {
                resp = true;
            }
        }
        return resp;
    }

    public static int isMouseOnAltWindowButton(MouseEvent me, boolean displayAltTooltip, ChartPanel chartPanel) {
        int button = 0;
        if (!displayAltTooltip) {
            return button;
        }
        int x = me.getX() - (int) chartPanel.getVisibleRect().getX(),
                y = me.getY() - (int) chartPanel.getVisibleRect().getY();
        //  System.out.println("X:" + x + ", Y:" + y + " (winY:" + altTooltipY + ", winX:" + altTooltipX + ", winW:" + altTooltipW);
        if (y > altTooltipY + 7 && y < altTooltipY + 21) {
        }
        if (x < (altTooltipX + altTooltipW - 3) && x > (altTooltipX + altTooltipW - (3 + buttonWidth))) {
            //    System.out.println("Close");
            button = CLOSE_IMAGE;
        } else if (x < (altTooltipX + altTooltipW - 30) && x > (altTooltipX + altTooltipW - (30 + buttonWidth))) {
            //    System.out.println("Expand");
            button = ZOOMEXPAND_IMAGE;
        } else if (x < (altTooltipX + altTooltipW - 50) && x > (altTooltipX + altTooltipW - (50 + buttonWidth))) {
            //    System.out.println("Shrink");
            button = ZOOMSHRINK_IMAGE;
        }
        return button;
    }

    public static void setDataFont(Font dataFont) {
        CommandPanelUtils.dataFont = dataFont;
    }

    public static Font getDataFont() {
        return dataFont;
    }

    public static void setTitleFont(Font titleFont) {
        CommandPanelUtils.titleFont = titleFont;
    }

    public static Font getTitleFont() {
        return titleFont;
    }

    public static void setAltTooltipX(int altTooltipX) {
        CommandPanelUtils.altTooltipX = altTooltipX;
    }

    public static int getAltTooltipX() {
        return altTooltipX;
    }

    public static void setAltTooltipY(int altTooltipY) {
        CommandPanelUtils.altTooltipY = altTooltipY;
    }

    public static int getAltTooltipY() {
        return altTooltipY;
    }

    public static int getAltTooltipW() {
        return altTooltipW;
    }

    public static void setAltTooltipW(int w) {
        CommandPanelUtils.altTooltipW = w;
    }

    public static void setAltTooltipH(int h) {
        CommandPanelUtils.altTooltipH = h;
    }

    public static int getAltTooltipH() {
        return altTooltipH;
    }

    public static double resetDir(double dir) {
        dir = 180D - dir;
        while (dir > 360D) {
            dir -= 360D;
        }
        while (dir < 0D) {
            dir += 360D;
        }
        return dir;
    }

    public static void print(CommandPanel cp) {
        PrintOptionsPanel pop = new PrintOptionsPanel();
        int resp = JOptionPane.showConfirmDialog(cp, pop, WWGnlUtilities.buildMessage("insert-title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp == JOptionPane.OK_OPTION) {
            String title = pop.getTitle();
            Component view;
            if (pop.getAllCompositeOption()) {
                view = cp.getChartPanel();  // All the composite
            } else {
                view = cp.getChartPanelScrollPane().getViewport(); // Just visible part
            }
            if (title.trim().length() > 0) {
                PrintUtilities.printComponent(view, title, Color.black, new Font("Verdana", Font.BOLD | Font.ITALIC, 16), 10, 20);
            } else {
                PrintUtilities.printComponent(view);
            }
        }
    }

    public static int[] genImage(CommandPanel cp) {
        int[] ret = null;
        String fName = WWGnlUtilities.chooseFile(cp,
                JFileChooser.FILES_ONLY,
                new String[]{"jpg", "jpeg", "png"},
                WWGnlUtilities.buildMessage("image-files"),
                ".",
                WWGnlUtilities.buildMessage("save"),
                WWGnlUtilities.buildMessage("generate-image"));
        if (fName.trim().length() > 0) {
            String prefix = "";
            String suffix = "";
            if (!fName.trim().contains(".")) {
                JOptionPane.showMessageDialog(cp,
                        WWGnlUtilities.buildMessage("please-provide-extension"),
                        WWGnlUtilities.buildMessage("generate-image"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                prefix = fName.trim().substring(0, fName.trim().indexOf("."));
                suffix = fName.trim().substring(fName.trim().indexOf(".") + 1);
                if (!suffix.toLowerCase().equals("jpg") &&
                        !suffix.toLowerCase().equals("jpeg") &&
                        !suffix.toLowerCase().equals("png")) {
                    JOptionPane.showMessageDialog(cp,
                            WWGnlUtilities.buildMessage("bad-extension",
                                    new String[]{suffix.toLowerCase()}),
                            WWGnlUtilities.buildMessage("generate-image"),
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    ret = cp.getChartPanel().genImage(prefix, suffix.toLowerCase());
                    JOptionPane.showMessageDialog(cp,
                            WWGnlUtilities.buildMessage("file-generated",
                                    new String[]{fName}),
                            WWGnlUtilities.buildMessage("generate-image"),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        return ret;
    }

    public static void runStorageThread(CommandPanel cp,
                                        boolean update,
                                        String compositeName,
                                        GribHelper.GribConditionData wgd[]) {
        WWContext.getInstance().fireSetLoading(true, "Please wait, storing..."); // LOCALIZE
        XMLDocument storage = new XMLDocument();

        XMLElement root = (XMLElement) storage.createElement("storage");
        storage.appendChild(root);

        if (cp.getCurrentComment().length() > 0) {
            XMLElement compositeComment = (XMLElement) storage.createElement("composite-comment");
            root.appendChild(compositeComment);
            CDATASection cdata = storage.createCDATASection(cp.getCurrentComment());
            compositeComment.appendChild(cdata);
        }

        XMLElement faxCollection = (XMLElement) storage.createElement("fax-collection");
        root.appendChild(faxCollection);

        for (int i = 0; cp.getFaxImage() != null && i < cp.getFaxImage().length; i++) {
            XMLElement fax = (XMLElement) storage.createElement("fax");
            faxCollection.appendChild(fax);
            if (update) {
                fax.setAttribute("file", cp.getFaxImage()[i].fileName);
            } else {
                fax.setAttribute("file", RelativePath.getRelativePath(System.getProperty("user.dir").replace(File.separatorChar, '/'), cp.getFaxImage()[i].fileName).replace(File.separatorChar, '/'));
            }
            fax.setAttribute("color", WWGnlUtilities.colorToString(cp.getFaxImage()[i].color));
            if (cp.getWHRatio() != 1D) {
                fax.setAttribute("wh-ratio", Double.toString(cp.getWHRatio()));
            }
            fax.setAttribute("transparent", Boolean.toString(cp.getFaxImage()[i].transparent));
            fax.setAttribute("color-change", Boolean.toString(cp.getFaxImage()[i].colorChange));

            XMLElement faxScale = (XMLElement) storage.createElement("faxScale");
            fax.appendChild(faxScale);
            Text faxScaleText = storage.createTextNode("#text");
            faxScaleText.setNodeValue(Double.toString(cp.getFaxImage()[i].imageScale));
            faxScale.appendChild(faxScaleText);

            XMLElement faxXoffset = (XMLElement) storage.createElement("faxXoffset");
            fax.appendChild(faxXoffset);
            Text faxXoffsetText = storage.createTextNode("#text");
            faxXoffsetText.setNodeValue(Integer.toString(cp.getFaxImage()[i].imageHOffset));
            faxXoffset.appendChild(faxXoffsetText);

            XMLElement faxYoffset = (XMLElement) storage.createElement("faxYoffset");
            fax.appendChild(faxYoffset);
            Text faxYoffsetText = storage.createTextNode("#text");
            faxYoffsetText.setNodeValue(Integer.toString(cp.getFaxImage()[i].imageVOffset));
            faxYoffset.appendChild(faxYoffsetText);

            XMLElement faxRotation = (XMLElement) storage.createElement("faxRotation");
            fax.appendChild(faxRotation);
            Text faxRotationText = storage.createTextNode("#text");
            faxRotationText.setNodeValue(Double.toString(cp.getFaxImage()[i].imageRotationAngle));
            faxRotation.appendChild(faxRotationText);

            XMLElement faxOrigin = (XMLElement) storage.createElement("faxOrigin");
            fax.appendChild(faxOrigin);
            Text faxOriginText = storage.createTextNode("#text");
            faxOriginText.setNodeValue(cp.getFaxImage()[i].faxOrigin);
            faxOrigin.appendChild(faxOriginText);

            XMLElement faxTitle = (XMLElement) storage.createElement("faxTitle");
            fax.appendChild(faxTitle);
            Text faxTitleText = storage.createTextNode("#text");
            faxTitleText.setNodeValue(cp.getFaxImage()[i].faxTitle);
            faxTitle.appendChild(faxTitleText);
        }
        XMLElement grib = (XMLElement) storage.createElement("grib");

        // Obsolete - 13-Oct-2009
        //  grib.setAttribute("wind-only", Boolean.toString(windOnly));
        //  grib.setAttribute("with-contour", Boolean.toString(displayContourLines));

        grib.setAttribute("display-TWS-Data", Boolean.toString(cp.isDisplayTws()));
        grib.setAttribute("display-PRMSL-Data", Boolean.toString(cp.isDisplayPrmsl()));
        grib.setAttribute("display-500HGT-Data", Boolean.toString(cp.isDisplay500mb()));
        grib.setAttribute("display-WAVES-Data", Boolean.toString(cp.isDisplayWaves()));
        grib.setAttribute("display-TEMP-Data", Boolean.toString(cp.isDisplayTemperature()));
        grib.setAttribute("display-PRATE-Data", Boolean.toString(cp.isDisplayRain()));

        grib.setAttribute("display-TWS-contour", Boolean.toString(cp.isDisplayContourTWS()));
        grib.setAttribute("display-PRMSL-contour", Boolean.toString(cp.isDisplayContourPRMSL()));
        grib.setAttribute("display-500HGT-contour", Boolean.toString(cp.isDisplayContour500mb()));
        grib.setAttribute("display-WAVES-contour", Boolean.toString(cp.isDisplayContourWaves()));
        grib.setAttribute("display-TEMP-contour", Boolean.toString(cp.isDisplayContourTemp()));
        grib.setAttribute("display-PRATE-contour", Boolean.toString(cp.isDisplayContourPrate()));

        grib.setAttribute("display-TWS-3D", Boolean.toString(cp.isDisplay3DTws()));
        grib.setAttribute("display-PRMSL-3D", Boolean.toString(cp.isDisplay3DPrmsl()));
        grib.setAttribute("display-500HGT-3D", Boolean.toString(cp.isDisplay3D500mb()));
        grib.setAttribute("display-WAVES-3D", Boolean.toString(cp.isDisplay3DWaves()));
        grib.setAttribute("display-TEMP-3D", Boolean.toString(cp.isDisplay3DTemperature()));
        grib.setAttribute("display-PRATE-3D", Boolean.toString(cp.isDisplay3DRain()));

        grib.setAttribute("smooth", Integer.toString(cp.getSmooth()));
        grib.setAttribute("time-smooth", Integer.toString(cp.getTimeSmooth()));

        //  System.out.println("GRIB Request :" + gribRequest);
        //  System.out.println("GRIB File Name :" + gribFileName);
        grib.setAttribute("grib-request", cp.getGribRequest());
        root.appendChild(grib);

        //  System.out.println("GRIB File Name:" + gribFileName);
        boolean inLineGRIBContent = false;
        if (!update) {
            File gf = new File(cp.getGribFileName());
            if (!gf.exists()) {
                inLineGRIBContent = true;
            }
        }
        if (update || !inLineGRIBContent) {
            Text gribText = storage.createTextNode("#text");
            gribText.setNodeValue(cp.getGribFileName().replace(File.separatorChar, '/'));
            grib.appendChild(gribText);
        } else {
            grib.setAttribute("in-line", "true");
            grib.setAttribute("in-line-request", cp.getGribFileName());
            if (wgd != null) {
                try {
                    boolean inLineOption = false;
                    if (inLineOption) {
                        // Might generate OutOfMemoryError
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        XMLEncoder encoder = new XMLEncoder(baos);
                        // TODO Write down the GribFile
                        //            Object o = WWContext.getInstance().getGribFile();
                        //            if (o == null)
                        //              o = wgd;
                        encoder.writeObject(wgd);
                        encoder.close();
                        String gribContent = baos.toString();
                        StringReader sr = new StringReader(gribContent);
                        DOMParser parser = WWContext.getInstance().getParser();
                        XMLDocument doc = null;
                        synchronized (parser) {
                            parser.setValidationMode(DOMParser.NONVALIDATING);
                            parser.parse(sr);
                            doc = parser.getDocument();
                        }
                        Node docRoot = doc.getDocumentElement();
                        Node newNode = storage.adoptNode(docRoot);
                        grib.appendChild(newNode);
                    } else { // File approach
                        String gribDir = ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX].toString().split(File.pathSeparator)[0] + File.separator + "inline-requests";
                        String ilGribFileName = WWGnlUtilities.SDF.format(new Date()) + ".in-line-grb";
                        File dir = new File(gribDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        FileOutputStream fos = new FileOutputStream(new File(gribDir, ilGribFileName));
                        XMLEncoder encoder = new XMLEncoder(fos);
                        // TODO Write down the GribFile
                        //            Object o = WWContext.getInstance().getGribFile();
                        //            if (o == null)
                        //              o = wgd;
                        encoder.writeObject(wgd);
                        encoder.close();
                        Text gribText = storage.createTextNode("#text");
                        gribText.setNodeValue((gribDir + File.separator + ilGribFileName).replace(File.separatorChar, '/'));
                        grib.appendChild(gribText);
                    }
                } catch (Exception pe) {
                    pe.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (Exception ignore) {
                }
            }
        }
        if (cp.getGPXData() != null) {
            XMLElement gpxDataNode = (XMLElement) storage.createElement("gpx-data");
            root.appendChild(gpxDataNode);
            // Loop on the GPX Data content
            for (GeoPoint gpxPoint : cp.getGPXData()) {
                double l = gpxPoint.getL();
                double g = gpxPoint.getG();
                XMLElement gpxNode = (XMLElement) storage.createElement("gpx-point");
                gpxNode.setAttribute("lat", Double.toString(l));
                gpxNode.setAttribute("lng", Double.toString(g));
                gpxDataNode.appendChild(gpxNode);
            }
        }

        XMLElement projection = (XMLElement) storage.createElement("projection");
        root.appendChild(projection);
        switch (cp.getChartPanel().getProjection()) {
            case ChartPanel.ANAXIMANDRE:
                projection.setAttribute("type", CommandPanel.ANAXIMANDRE);
                break;
            case ChartPanel.POLAR_STEREOGRAPHIC:
                projection.setAttribute("type", CommandPanel.POLAR_STEREO);
                break;
            case ChartPanel.STEREOGRAPHIC:
                projection.setAttribute("type", CommandPanel.STEREO);
                break;
            case ChartPanel.LAMBERT:
                projection.setAttribute("type", CommandPanel.LAMBERT);
                projection.setAttribute("contact-parallel", Double.toString(cp.getChartPanel().getContactParallel()));
                break;
            case ChartPanel.CONIC_EQUIDISTANT:
                projection.setAttribute("type", CommandPanel.CONIC_EQU);
                projection.setAttribute("contact-parallel", Double.toString(cp.getChartPanel().getContactParallel()));
                break;
            case ChartPanel.GLOBE_VIEW:
                projection.setAttribute("type", CommandPanel.GLOBE);
                break;
            case ChartPanel.SATELLITE_VIEW:
                projection.setAttribute("type", CommandPanel.SATELLITE);
                projection.setAttribute("nadir-latitude", Double.toString(cp.getChartPanel().getSatelliteLatitude()));
                projection.setAttribute("nadir-longitude", Double.toString(cp.getChartPanel().getSatelliteLongitude()));
                projection.setAttribute("altitude", Double.toString(cp.getChartPanel().getSatelliteAltitude()));
                projection.setAttribute("opaque", Boolean.toString(!cp.getChartPanel().isTransparentGlobe()));
                break;
            case ChartPanel.MERCATOR:
            default:
                projection.setAttribute("type", CommandPanel.MERCATOR);
                break;
        }

        XMLElement north = (XMLElement) storage.createElement("north");
        root.appendChild(north);
        Text northText = storage.createTextNode("#text");
        northText.setNodeValue(Double.toString(cp.getNLat()));
        north.appendChild(northText);

        XMLElement south = (XMLElement) storage.createElement("south");
        root.appendChild(south);
        Text southText = storage.createTextNode("#text");
        southText.setNodeValue(Double.toString(cp.getSLat()));
        south.appendChild(southText);

        XMLElement east = (XMLElement) storage.createElement("east");
        root.appendChild(east);
        Text eastText = storage.createTextNode("#text");
        eastText.setNodeValue(Double.toString(cp.getELong()));
        east.appendChild(eastText);

        XMLElement west = (XMLElement) storage.createElement("west");
        root.appendChild(west);
        Text westText = storage.createTextNode("#text");
        westText.setNodeValue(Double.toString(cp.getWLong()));
        west.appendChild(westText);

        XMLElement chartwidth = (XMLElement) storage.createElement("chartwidth");
        root.appendChild(chartwidth);
        Text chartwidthText = storage.createTextNode("#text");
        chartwidthText.setNodeValue(Integer.toString(cp.getChartPanel().getWidth()));
        chartwidth.appendChild(chartwidthText);

        XMLElement chartheight = (XMLElement) storage.createElement("chartheight");
        root.appendChild(chartheight);
        Text chartheightText = storage.createTextNode("#text");
        chartheightText.setNodeValue(Integer.toString(cp.getChartPanel().getHeight()));
        chartheight.appendChild(chartheightText);

        // Show Chart
        boolean showChart = cp.isDrawChart();
        if (!showChart && !("yes".equals(System.getProperty("headless", "no")) ||
                "true".equals(System.getProperty("headless", "no")))) {
            int resp = JOptionPane.showConfirmDialog(cp,
                    "Chart will not be drawn (by default) for this composite.\nDo you confirm?", // LOCALIZE
                    WWGnlUtilities.buildMessage("store-composite"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (resp == JOptionPane.NO_OPTION) {
                showChart = true;
            }
        }
        XMLElement chartOpt = (XMLElement) storage.createElement("chart-opt");
        root.appendChild(chartOpt);
        chartOpt.setAttribute("show", showChart ? "yes" : "no");

        XMLElement scroll = (XMLElement) storage.createElement("scroll");
        root.appendChild(scroll);
        scroll.setAttribute("x", Integer.toString(cp.getChartPanelScrollPane().getViewport().getViewPosition().x));
        scroll.setAttribute("y", Integer.toString(cp.getChartPanelScrollPane().getViewport().getViewPosition().y));

        XMLElement faxOption = (XMLElement) storage.createElement("fax-option");
        root.appendChild(faxOption);
        faxOption.setAttribute("value", (cp.getCheckBoxPanelOption() == CommandPanel.CHECKBOX_OPTION ? "CHECKBOX" : "RADIOBUTTON"));

        // Boat position?
        if (cp.getBoatPosition() != null) {
            XMLElement boatLocation = (XMLElement) storage.createElement("boat-position");
            root.appendChild(boatLocation);
            boatLocation.setAttribute("lat", Double.toString(cp.getBoatPosition().getL()));
            boatLocation.setAttribute("lng", Double.toString(cp.getBoatPosition().getG()));
            boatLocation.setAttribute("hdg", Integer.toString(cp.getBoatHeading()));
        }

        // Hand drawing
        if (cp.getChartPanel().getHandDrawing() != null && cp.getChartPanel().getHandDrawing().size() > 0) {
            XMLElement handDrawing = (XMLElement) storage.createElement("hand-drawings");
            root.appendChild(handDrawing);
            List<ChartPanel.PointList<GeoPoint>> hd = cp.getChartPanel().getHandDrawing();
            int idx = 0;
            for (ChartPanel.PointList<GeoPoint> oneDrawing : hd) {
                XMLElement oneLine = (XMLElement) storage.createElement("hand-drawing-line");
                oneLine.setAttribute("color", WWGnlUtilities.colorToString(oneDrawing.getLineColor()));
                handDrawing.appendChild(oneLine);
                oneLine.setAttribute("idx", Integer.toString(++idx));
                for (GeoPoint gp : oneDrawing) {
                    XMLElement geoPoint = (XMLElement) storage.createElement("gp");
                    oneLine.appendChild(geoPoint);
                    geoPoint.setAttribute("lat", Double.toString(gp.getL()));
                    geoPoint.setAttribute("lng", Double.toString(gp.getG()));
                }
            }
        }

        WWContext.getInstance().fireInterruptProgress(); // Interrupt Progress

        String fileName = "";
        if (!update && compositeName == null) {
            fileName = WWGnlUtilities.chooseFile(cp,
                    JFileChooser.FILES_ONLY, // TODO Localize?
                    new String[]{"waz"}, // , "xml" },
                    "Composites",
                    ParamPanel.data[ParamData.COMPOSITE_ROOT_DIR][ParamData.VALUE_INDEX].toString(),
                    "Save",
                    "Save Composite File");
        } else {
            fileName = compositeName;
        }
        if (!update && fileName != null && fileName.trim().length() > 0) {
            boolean archiveRequired = false;
            String archiveName = null;
            fileName = Utilities.makeSureExtensionIsOK(fileName, new String[]{".xml", ".waz"}, ".waz");
            if (fileName.toUpperCase().endsWith(".WAZ")) { // Archive required
                archiveRequired = true;
                archiveName = fileName;
                fileName = fileName.substring(0, fileName.trim().length() - WWContext.WAZ_EXTENSION.length());
            }
            fileName = Utilities.makeSureExtensionIsOK(fileName, ".xml");  // On purpose. There is a boolean (archiveRequired)
            try {
                // Check file existence first
                File composite = new File(fileName);
                boolean ok = true;
                if (composite.exists()) {
                    int resp = JOptionPane.showConfirmDialog(cp,
                            WWGnlUtilities.buildMessage("composite-already-exist", new String[]{fileName}),
                            WWGnlUtilities.buildMessage("store-composite"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (resp == JOptionPane.NO_OPTION) {
                        ok = false;
                    }
                }
                if (ok) {
                    OutputStream os = new FileOutputStream(composite);
                    storage.print(os);
                    os.close();
                    if (!archiveRequired && "false".equals(System.getProperty("headless", "false"))) {
                        WWContext.getInstance().fireReloadCompositeTree();
                    }
                }
            } catch (Exception e) {
                WWContext.getInstance().fireExceptionLogging(e);
                e.printStackTrace();
            }
            if (archiveRequired) { // Archive here if necessary
                //    System.out.println("Archiving " + fileName);
                boolean autoDownloadAndSave = fileName != null && !update;
                for (int i = 0; cp.getFaxImage() != null && i < cp.getFaxImage().length; i++) {
                    if (cp.getFaxImage()[i].faxOrigin.startsWith(SearchUtil.SEARCH_PROTOCOL)) { // 2013-APR-14. TODO Idem for the GRIBs
                        autoDownloadAndSave = false;
                        break;
                    }
                }
                WWGnlUtilities.archiveComposite(fileName, autoDownloadAndSave, !cp.getGribRequest().startsWith(SearchUtil.SEARCH_PROTOCOL));
                WWContext.getInstance().fireReloadCompositeTree();
                WWContext.getInstance().fireReloadFaxTree();
                WWContext.getInstance().fireReloadGRIBTree();
                // Finally, reload composite from archive,
                // to have the faxes and GRIBs pointing in the archive
                WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("swapping-pointers"));
                cp.restoreComposite(archiveName);
            }
        } else if (update && fileName != null && fileName.trim().length() > 0) {
            // Update the composite.xml in the waz file
            if (fileName.toUpperCase().endsWith(".WAZ")) { // Archive required
                //      WWContext.getInstance().fireInterruptProgress();
                int resp = JOptionPane.showConfirmDialog(cp,
                        WWGnlUtilities.buildMessage("updating-composite", new String[]{fileName}),
                        WWGnlUtilities.buildMessage("store-composite"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    try {
                        storage.print(System.out);
                        WWGnlUtilities.updateComposite(fileName, storage);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // Finally
                    if ("false".equals(System.getProperty("headless", "false"))) {
                        WWContext.getInstance().fireReloadCompositeTree();
                    }
                }
            } else {
                System.out.println("Ooops!");
            }
        }
        WWContext.getInstance().fireSetCompositeFileName(fileName);
        WWContext.getInstance().fireSetLoading(false, "Please wait...");
    }

    public static void createFromPattern(String fileName,
                                         CommandPanel cp,
                                         int blurSharpOption) {
        try {
            DOMParser parser = WWContext.getInstance().getParser();
            synchronized (parser) {
                parser.setValidationMode(DOMParser.NONVALIDATING);
                URL patternURL = null;
                try {
                    if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
                        patternURL = new URL(fileName);
                    } else {
                        patternURL = new File(fileName).toURI().toURL();
                    }
                    parser.parse(patternURL);
                } catch (FileNotFoundException fnfe) // Possibly happens at startup?
                {
                    JOptionPane.showMessageDialog(cp, fnfe.toString(), "Loading Pattern", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Exception other) {
                    System.out.println("restoreFromPattern:" + other.toString());
                    other.printStackTrace();
                    // Dump the file
                    File f = new File(fileName);
                    if (f.exists()) {
                        try {
                            System.out.println(f.toURI().toString());
                        } catch (Exception e) {
                            System.out.println("...No URI " + e.toString());
                        }
                        try {
                            System.out.println(f.toURI().toURL().toString());
                        } catch (Exception e) {
                            System.out.println("...No URL " + e.toString());
                        }
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        String line = "";
                        boolean go = true;
                        while (go) {
                            line = br.readLine();
                            if (br == null) {
                                go = false;
                            } else {
                                System.out.println(line);
                            }
                        }
                    } else {
                        System.out.println("File [" + fileName + "] does not exist...");
                    }
                    System.out.println("Cancelling...");
                    WWContext.getInstance().fireInterruptProgress();
                    JOptionPane.showMessageDialog(cp, other.toString(), "Loading Pattern", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                XMLDocument doc = parser.getDocument();
                String projType = ((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("type");
                if (projType.equals(CommandPanel.MERCATOR)) {
                    cp.getChartPanel().setProjection(ChartPanel.MERCATOR);
                    WWContext.getInstance().fireSetProjection(ChartPanel.MERCATOR);
                } else if (projType.equals(CommandPanel.ANAXIMANDRE)) {
                    cp.getChartPanel().setProjection(ChartPanel.ANAXIMANDRE);
                    WWContext.getInstance().fireSetProjection(ChartPanel.ANAXIMANDRE);
                } else if (projType.equals(CommandPanel.LAMBERT)) {
                    cp.getChartPanel().setProjection(ChartPanel.LAMBERT);
                    double cpar = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("contact-parallel"));
                    cp.getChartPanel().setContactParallel(cpar);
                    WWContext.getInstance().fireSetProjection(ChartPanel.LAMBERT);
                    WWContext.getInstance().fireSetContactParallel(cpar);
                } else if (projType.equals(CommandPanel.GLOBE)) {
                    cp.getChartPanel().setProjection(ChartPanel.GLOBE_VIEW);
                    WWContext.getInstance().fireSetProjection(ChartPanel.GLOBE_VIEW);
                    // FIXME Globe parameters
                    //      StaticObjects.getInstance().fireSetGlobeParameters();
                } else if (projType.equals(CommandPanel.SATELLITE)) {
                    cp.getChartPanel().setProjection(ChartPanel.SATELLITE_VIEW);
                    WWContext.getInstance().fireSetProjection(ChartPanel.SATELLITE_VIEW);
                    double nl = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("nadir-latitude"));
                    double ng = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("nadir-longitude"));
                    double alt = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("altitude"));
                    boolean opaque = new Boolean(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("opaque")).booleanValue();
                    cp.getChartPanel().setSatelliteAltitude(alt);
                    cp.getChartPanel().setSatelliteLatitude(nl);
                    cp.getChartPanel().setSatelliteLongitude(ng);
                    cp.getChartPanel().setTransparentGlobe(!opaque);
                    WWContext.getInstance().fireSetSatelliteParameters(nl, ng, alt, opaque);
                } else if (projType.equals(CommandPanel.STEREO)) {
                    cp.getChartPanel().setProjection(ChartPanel.STEREOGRAPHIC);
                    WWContext.getInstance().fireSetProjection(ChartPanel.STEREOGRAPHIC);
                } else if (projType.equals(CommandPanel.POLAR_STEREO)) {
                    cp.getChartPanel().setProjection(ChartPanel.POLAR_STEREOGRAPHIC);
                    WWContext.getInstance().fireSetProjection(ChartPanel.POLAR_STEREOGRAPHIC);
                }
                double nLat = Double.parseDouble(doc.selectNodes("//north").item(0).getFirstChild().getNodeValue());
                double sLat = Double.parseDouble(doc.selectNodes("//south").item(0).getFirstChild().getNodeValue());
                double wLong = Double.parseDouble(doc.selectNodes("//west").item(0).getFirstChild().getNodeValue());
                double eLong = Double.parseDouble(doc.selectNodes("//east").item(0).getFirstChild().getNodeValue());
                cp.setNLat(nLat);
                cp.setSLat(sLat);
                cp.setELong(eLong);
                cp.setWLong(wLong);

                int w = Integer.parseInt(doc.selectNodes("//chartwidth").item(0).getFirstChild().getNodeValue());
                int h = Integer.parseInt(doc.selectNodes("//chartheight").item(0).getFirstChild().getNodeValue());

                int xScroll = 0, yScroll = 0;
                try {
                    xScroll = Integer.parseInt(((XMLElement) doc.selectNodes("//scroll").item(0)).getAttribute("x"));
                    yScroll = Integer.parseInt(((XMLElement) doc.selectNodes("//scroll").item(0)).getAttribute("y"));
                } catch (Exception ignore) {
                }

                try {
                    XMLElement faxOption = (XMLElement) (doc.selectNodes("//fax-option").item(0));
                    String opt = faxOption.getAttribute("value");
                    if (opt.equals("CHECKBOX")) {
                        cp.setCheckBoxPanelOption(CommandPanel.CHECKBOX_OPTION);
                    } else if (opt.equals("RADIOBUTTON")) {
                        cp.setCheckBoxPanelOption(CommandPanel.RADIOBUTTON_OPTION);
                    } else {
                        System.out.println("Unknown option [" + opt + "]");
                    }
                } catch (Exception ignore) {
                }

                if (cp.getChartPanel().getProjection() != ChartPanel.GLOBE_VIEW &&
                        cp.getChartPanel().getProjection() != ChartPanel.SATELLITE_VIEW) {
                    cp.getChartPanel().setWidthFromChart(nLat, sLat, wLong, eLong);
                }
                //    eLong = chartPanel.calculateEastG(nLat, sLat, wLong);
                cp.getChartPanel().setEastG(eLong);
                cp.getChartPanel().setWestG(wLong);
                cp.getChartPanel().setNorthL(nLat);
                cp.getChartPanel().setSouthL(sLat);

                cp.getChartPanel().setW(w);
                cp.getChartPanel().setH(h);
                cp.getChartPanel().setBounds(0, 0, w, h);

                // Show / Hide chart
                boolean drawChart = true;
                try {
                    drawChart = "yes".equals(((XMLElement) doc.selectNodes("//chart-opt").item(0)).getAttribute("show"));
                } catch (Exception ignore) {
                }
                cp.setDrawChart(drawChart);

                cp.getChartPanel().repaint();

                try {
                    NodeList faxes = doc.selectNodes("//fax-collection/fax");
                    cp.setFaxImage(new CommandPanel.FaxImage[faxes.getLength()]);
                    FaxType[] ft = new FaxType[faxes.getLength()];
                    for (int i = 0; i < faxes.getLength(); i++) {
                        XMLElement fax = (XMLElement) faxes.item(i);
                        // Dynamic pattern?
                        XMLElement dynamic = null;
                        try {
                            dynamic = (XMLElement) fax.selectNodes("dynamic-resource").item(0);
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                        String faxName = "";
                        String hintName = fax.getAttribute("hint");
                        if (dynamic != null) {
                            final String url = dynamic.getAttribute("url");
                            if (url.toUpperCase().startsWith("HTTP://") || url.toUpperCase().startsWith("HTTPS://")) {
                                boolean ok2go = WWContext.getInstance().isOnLine();
                                if (!ok2go) {
                                    //            String mess = GnlUtilities.buildMessage("you-are-not-on-line", new String[] { url });
                                    int resp = JOptionPane.showConfirmDialog(cp,
                                            WWGnlUtilities.buildMessage("you-are-not-on-line",
                                                    new String[]{url}),
                                            WWGnlUtilities.buildMessage("dynamic-pattern"),
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (resp == JOptionPane.YES_OPTION) {
                                        ok2go = true;
                                        WWContext.getInstance().setOnLine(true);
                                    }
                                }
                                if (ok2go) {
                                    WWContext.getInstance().fireProgressing("Loading Fax " + hintName);
                                    String dir = dynamic.getAttribute("dir");
                                    String prefix = dynamic.getAttribute("prefix");
                                    String pattern = dynamic.getAttribute("pattern");
                                    String ext = dynamic.getAttribute("extension");
                                    Date now = new Date();
                                    dir = WWGnlUtilities.translatePath(dir, now).replace('/', File.separatorChar);
                                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                                    faxName = dir + File.separator + prefix + sdf.format(now) + "." + ext;

                                    File faxDir = new File(dir);
                                    if (!faxDir.exists()) {
                                        faxDir.mkdirs();
                                    }
                                    try {
                                        WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{url}) + "\n", LoggingPanel.WHITE_STYLE);
                                        System.out.println("Loading " + faxName);
                                        HTTPClient.getChart(url, dir, faxName, true);
                                        if ("true".equals(System.getProperty("headless", "false"))) {
                                            WWContext.getInstance().fireReloadFaxTree();
                                        }
                                    } catch (CannotWriteException cwe) { // Case of a permission
                                        String message = cwe.getMessage();
                                        message += ("\nUser [" + System.getProperty("user.name") + "] seems not to have write access to " + faxDir);
                                        System.out.println(">>> *********************************");
                                        System.err.println(message);
                                        System.out.println(">>> *********************************");
                                        JOptionPane.showMessageDialog(cp, message, "Downloading fax", JOptionPane.WARNING_MESSAGE);
                                        cwe.printStackTrace();
                                    } catch (IIOException iioe) {
                                        System.out.println(String.format("    >>> Fax #%d not reachable...", i + 1));
                                        iioe.printStackTrace();
                                    } catch (Exception ex) {
                                        System.err.println("------------");
                                        ex.printStackTrace();
                                        System.err.println("------------");
                                        System.out.println("!!!!!!! >>> Loading fax, Exception is a " + ex.getClass().getName());
                                        // System.out.println("HTTPClient.getChart interrupted..., returning.");
                                        WWContext.getInstance().fireInterruptProgress();
                                        return;
                                    }
                                }
                            } else { // Non HTTP protocols
                                // Non http protocols!
                                if (url.startsWith(SearchUtil.SEARCH_PROTOCOL)) { // (local search, for SailMail and similar)
                                    // Parse Expression, like search:chartview.util.SearchUtil.findMostRecentFax(pattern, rootPath)
                                    faxName = SearchUtil.dynamicSearch(url);
                                    // System.out.println("For " + hintName + ", search: found [" + faxName + "]");
                                } else if (url.startsWith(WWContext.INTERNAL_RESOURCE_PREFIX) ||
                                        url.startsWith(WWContext.EXTERNAL_RESOURCE_PREFIX)) { // like Backgrounds
                                    String internStr = "";
                                    if (url.startsWith(WWContext.INTERNAL_RESOURCE_PREFIX)) {
                                        internStr = url.substring(WWContext.INTERNAL_RESOURCE_PREFIX.length());

                    //                  if (internStr.equals(WWContext.BG_MERCATOR_GREENWICH_CENTERED_ALIAS))
                    //                    internStr = WWContext.BG_MERCATOR_GREENWICH_CENTERED;
                    //                  else if (internStr.equals(WWContext.BG_MERCATOR_ANTIMERIDIAN_CENTERED_ALIAS))
                    //                    internStr = WWContext.BG_MERCATOR_ANTIMERIDIAN_CENTERED;
                    //                  else if (internStr.equals(WWContext.BG_MERCATOR_NE_ATLANTIC_ALIAS))
                    //                    internStr = WWContext.BG_MERCATOR_NE_ATLANTIC;

                                        for (BackGround bg : BackGround.values()) {
                                            if (internStr.equals(bg.label())) {
                                                //                    System.out.println("Looking for [" + bg.resource() + "]");
                                                URL resourceURL = CommandPanel.class.getResource(bg.resource());
                                                //                    System.out.println("URL is [" + resourceURL + "]");
                                                internStr = resourceURL.toString();
                                                break;
                                            }
                                        }
                                    } else if (url.startsWith(WWContext.EXTERNAL_RESOURCE_PREFIX)) {
                                        internStr = url.substring(WWContext.EXTERNAL_RESOURCE_PREFIX.length());
                                        try {
                                            /* URL testUrl = */
                                            new URL(internStr);
                                        } catch (Exception ex) {
                                            // try file
                                            try {
                                                internStr = new File(internStr).toURI().toURL().toString();
                                            } catch (Exception ex2) {
                                                System.err.println(ex2.toString());
                                            }
                                        }
                                    }
                                    URL intern = new URL(internStr);
                                    Image image = null;
                                    try {
                                        image = ImageIO.read(intern);
                                        File temp = File.createTempFile("resource.bg.", ".png");
                                        ImageIO.write(ImageUtil.toBufferedImage(image), "png", temp);
                                        faxName = temp.getAbsolutePath();
                                        temp.deleteOnExit();
                                    } catch (CannotWriteException cwe) { // Case of a permission
                                        String message = cwe.getMessage();
                                        message += ("\nUser [" + System.getProperty("user.name") + "] seems not to have write access...");
                                        JOptionPane.showMessageDialog(cp, message, "Download", JOptionPane.WARNING_MESSAGE);
                                        cwe.printStackTrace();
                                    } catch (Exception e) {
                                        System.err.println("For URL: [" + url + "] => [" + internStr + "]");
                                        WWContext.getInstance().fireExceptionLogging(e);
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else { // Non dynamic, prompt the user.
                            // Prompt for the file
                            String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.FAX_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
                            faxName = WWGnlUtilities.chooseFile(cp,
                                    JFileChooser.FILES_ONLY,
                                    new String[]{"gif", "jpg", "jpeg", "tif", "tiff", "png"},
                                    hintName,
                                    firstDir,
                                    WWGnlUtilities.SaveOrOpen.OPEN,
                                    WWGnlUtilities.buildMessage("open"),
                                    hintName,
                                    true);
                        }
                        // Fax identified, loading.
                        if (faxName.trim().length() > 0) {
                            Color c = WWGnlUtilities.buildColor(fax.getAttribute("color"));
                            if (false) {
                                System.out.println("For " + faxName + ", color=" + WWGnlUtilities.colorToString(c));
                            }
                            String strRatio = fax.getAttribute("wh-ratio");
                            String transparentStr = fax.getAttribute("transparent");
                            String colorChangeStr = fax.getAttribute("color-change");
                            if (strRatio.trim().length() > 0) {
                                try {
                                    cp.setWHRatio(Double.parseDouble(strRatio));
                                } catch (Exception ignore) {
                                    ignore.printStackTrace();
                                }
                            } else {
                                cp.setWHRatio(1D);
                            }
                            double imageScale = Double.parseDouble(fax.selectNodes("./faxScale").item(0).getFirstChild().getNodeValue());
                            int imageHOffset = Integer.parseInt(fax.selectNodes("./faxXoffset").item(0).getFirstChild().getNodeValue());
                            int imageVOffset = Integer.parseInt(fax.selectNodes("./faxYoffset").item(0).getFirstChild().getNodeValue());
                            double imageRotation = 0D;
                            try {
                                imageRotation = Double.parseDouble(fax.selectNodes("./faxRotation").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                                System.err.println("Rotation:" + ignore.getLocalizedMessage());
                            }
                            cp.getFaxImage()[i] = new CommandPanel.FaxImage();
                            cp.getFaxImage()[i].fileName = faxName;
                            cp.getFaxImage()[i].color = c;
                            cp.getFaxImage()[i].imageScale = imageScale;
                            cp.getFaxImage()[i].imageHOffset = imageHOffset;
                            cp.getFaxImage()[i].imageVOffset = imageVOffset;
                            cp.getFaxImage()[i].imageRotationAngle = imageRotation;
                            cp.getFaxImage()[i].comment = WWGnlUtilities.getHeader(faxName);
                            // int gmtOffset = 0;
                            // if (!cp.getFaxImage()[i].fileName.startsWith(WWContext.WAZ_PROTOCOL_PREFIX))
                            //   gmtOffset = TimeUtil.getLocalGMTOffset(); // Local
                            cp.getFaxImage()[i].created = (new File(faxName).lastModified()); // - (gmtOffset * 3600000L);
                            String faxOrigin = "";
                            try {
                                faxOrigin = ((XMLElement) fax.selectNodes("./dynamic-resource").item(0)).getAttribute("url");
                            } catch (Exception ignore) {
//                                ignore.printStackTrace();
                                System.err.printf("No FaxOrigin, ignoring: %s\n", ignore.toString());
                            }
                            cp.getFaxImage()[i].faxOrigin = faxOrigin;
                            if (cp.getFaxImage()[i].comment.equals(faxName)) {
                                String hint = fax.getAttribute("hint");
                                if (hint.trim().length() > 0) {
                                    cp.getFaxImage()[i].comment = hint;
                                    cp.getFaxImage()[i].faxTitle = hint;
                                }
                            }
                            if (cp.getFaxImage()[i].comment.trim().length() > 0 && (cp.getFaxImage()[i].faxTitle == null || cp.getFaxImage()[i].faxTitle.trim().length() == 0)) {
                                cp.getFaxImage()[i].faxTitle = cp.getFaxImage()[i].comment;
                            }
                            cp.getFaxImage()[i].show = true;
                            if (transparentStr == null || transparentStr.trim().length() == 0) {
                                cp.getFaxImage()[i].transparent = true;
                            } else {
                                cp.getFaxImage()[i].transparent = transparentStr.equals("true");
                            }
                            if (colorChangeStr == null || colorChangeStr.trim().length() == 0) {
                                cp.getFaxImage()[i].colorChange = true;
                            } else {
                                cp.getFaxImage()[i].colorChange = colorChangeStr.equals("true");
                            }

                            try {
                                if (cp.getFaxImage()[i].transparent) {
                                    if (cp.getFaxImage()[i].colorChange) {
                                        //   faxImage[i].faxImage = ImageUtil.makeTransparentImage(this, ImageUtil.readImage(faxName), c);
                                        //   faxImage[i].faxImage = ImageUtil.switchColorAndMakeColorTransparent(ImageUtil.readImage(faxName), Color.black, c, Color.white, blurSharpOption);
                                        Image faxImg = ImageUtil.readImage(faxName);
                                        if (ImageUtil.countColors(faxImg) > 2) {
                                            faxImg = ImageUtil.switchAnyColorAndMakeColorTransparent(faxImg, c, ImageUtil.mostUsedColor(faxImg), blurSharpOption);
                                        } else {
                                            faxImg = ImageUtil.switchColorAndMakeColorTransparent(faxImg, Color.black, c, Color.white, blurSharpOption);
                                        }
                                        cp.getFaxImage()[i].faxImage = faxImg;
                                    } else {
                                        cp.getFaxImage()[i].faxImage = ImageUtil.makeColorTransparent(ImageUtil.readImage(faxName), Color.white, blurSharpOption);
                                    }
                                } else {
                                    cp.getFaxImage()[i].faxImage = ImageUtil.readImage(faxName);
                                }
                            } catch (Exception e) {
                                WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("file-not-found", new String[]{faxName}) + "\n");
                                //  WWContext.getInstance().fireInterruptProgress();
                                //  return;
                            }
                            ft[i] = new FaxType(faxName, c, Boolean.valueOf(true), Boolean.valueOf(true), imageRotation, faxOrigin, faxName);
                            ft[i].setRank(i + 1);
                            ft[i].setComment(cp.getFaxImage()[i].comment);
                            cp.repaint();
                        } else {
                            return; // Bye!
                        }
                    }
                    cp.setCheckBoxes(ft);
                    WWContext.getInstance().fireFaxLoaded();
                    WWContext.getInstance().fireFaxesLoaded(ft);
                } catch (Exception ex) {
                    WWContext.getInstance().fireExceptionLogging(ex);
                    ex.printStackTrace();
                    cp.unsetFaxImage();
                }

                String gribRequest = "";
                try {
                    XMLElement gribNode = (XMLElement) doc.selectNodes("//grib").item(0);

                    String twsData = gribNode.getAttribute("display-TWS-Data");
                    if (twsData.trim().length() > 0) { // New version
                        cp.setDisplayTws("true".equals(twsData));
                        cp.setDisplayPrmsl("true".equals(gribNode.getAttribute("display-PRMSL-Data")));
                        cp.setDisplay500mb("true".equals(gribNode.getAttribute("display-500HGT-Data")));
                        cp.setDisplayWaves("true".equals(gribNode.getAttribute("display-WAVES-Data")));
                        cp.setDisplayTemperature("true".equals(gribNode.getAttribute("display-TEMP-Data")));
                        cp.setDisplayRain("true".equals(gribNode.getAttribute("display-PRATE-Data")));

                        cp.setDisplayContourTWS("true".equals(gribNode.getAttribute("display-TWS-contour")));
                        cp.setDisplayContourPRMSL("true".equals(gribNode.getAttribute("display-PRMSL-contour")));
                        cp.setDisplayContour500mb("true".equals(gribNode.getAttribute("display-500HGT-contour")));
                        cp.setDisplayContourWaves("true".equals(gribNode.getAttribute("display-WAVES-contour")));
                        cp.setDisplayContourTemp("true".equals(gribNode.getAttribute("display-TEMP-contour")));
                        cp.setDisplayContourPrate("true".equals(gribNode.getAttribute("display-PRATE-contour")));

                        cp.setDisplay3DTws("true".equals(gribNode.getAttribute("display-TWS-3D")));
                        cp.setDisplay3DPrmsl("true".equals(gribNode.getAttribute("display-PRMSL-3D")));
                        cp.setDisplay3D500mb("true".equals(gribNode.getAttribute("display-500HGT-3D")));
                        cp.setDisplay3DWaves("true".equals(gribNode.getAttribute("display-WAVES-3D")));
                        cp.setDisplay3DTemperature("true".equals(gribNode.getAttribute("display-TEMP-3D")));
                        cp.setDisplay3DRain("true".equals(gribNode.getAttribute("display-PRATE-3D")));
                    }
                    String wo = gribNode.getAttribute("wind-only"); // deprecated
                    if (wo.trim().length() > 0) {
                        cp.setWindOnly(Boolean.valueOf(wo));
                        if (!cp.isWindOnly()) {
                            cp.setDisplayPrmsl(true);
                            cp.setDisplay500mb(true);
                            cp.setDisplayWaves(true);
                            cp.setDisplayTemperature(true);
                            cp.setDisplayRain(true);
                        }
                    }
                    String wc = gribNode.getAttribute("with-contour"); // deprecated
                    if (wc.trim().length() > 0) {
                        cp.setDisplayContourLines(Boolean.valueOf(wc));
                    }
                    if (gribNode.selectNodes("dynamic-grib").getLength() == 0) {
                        String gribHint = gribNode.getFirstChild().getNodeValue();
                        if (gribHint.trim().length() > 0) {
                            String firstDir = ((ParamPanel.DataPath) ParamPanel.data[ParamData.GRIB_FILES_LOC][ParamData.VALUE_INDEX]).toString().split(File.pathSeparator)[0];
                            String grib = WWGnlUtilities.chooseFile(cp,
                                    JFileChooser.FILES_ONLY,
                                    new String[]{"grb", "grib"},
                                    gribHint,
                                    firstDir,
                                    WWGnlUtilities.SaveOrOpen.OPEN,
                                    WWGnlUtilities.buildMessage("open"),
                                    gribHint,
                                    false);
                            if (grib != null && grib.trim().length() > 0) {
                                String gribFileName = grib;
                                GribHelper.GribConditionData wgd[] = GribHelper.getGribData(gribFileName, true);
                                cp.setGribFileName(gribFileName);
                                cp.setGribData(wgd, gribFileName);
                            } else {
                                cp.setGribFileName("");
                            }
                        }
                    } else { // GRIB from Saildocs
                        WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("loading-grib"));
                        XMLElement saildocs = (XMLElement) gribNode.selectNodes("dynamic-grib").item(0);
                        String request = saildocs.getAttribute("request");
                        gribRequest = request;
                        if (gribRequest.startsWith(SearchUtil.SEARCH_PROTOCOL)) { // From the disc
                            // Parse Expression, like search:chartview.util.SearchUtil.findMostRecentFax(pattern, rootPath)
                            String gribFileName = SearchUtil.dynamicSearch(gribRequest);
                            // System.out.println("For " + hintName + ", search: found [" + gribFileName + "]");
                            if (gribFileName != null && gribFileName.trim().length() > 0) {
                                GribHelper.GribConditionData wgd[] = GribHelper.getGribData(gribFileName, true);
                                cp.setGribData(wgd, gribFileName);
                            } else {
                                gribFileName = "";
                            }
                            cp.setGribFileName(gribFileName);
                        } else { // From the Web
                            String gribDir = saildocs.getAttribute("dir");
                            String girbPrefix = saildocs.getAttribute("prefix");
                            String gribPattern = saildocs.getAttribute("pattern");
                            String gribExt = saildocs.getAttribute("extension");
                            Date now = new Date();
                            gribDir = WWGnlUtilities.translatePath(gribDir, now);
                            SimpleDateFormat sdf = new SimpleDateFormat(gribPattern);
                            String gribFileName = gribDir + File.separator + girbPrefix + sdf.format(now) + "." + gribExt;

                            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("loading2", new String[]{request}) + "\n", LoggingPanel.WHITE_STYLE);
                            File dir = new File(gribDir);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            try {
                                byte[] gribContent = HTTPClient.getGRIB(WWGnlUtilities.generateGRIBRequest(request), gribDir, gribFileName, true);
                                WWContext.getInstance().fireReloadGRIBTree();
                                GribHelper.GribConditionData wgd[] = GribHelper.getGribData(new ByteArrayInputStream(gribContent), request);
                                cp.setGribData(wgd, gribFileName);
                                cp.setGribFileName(gribFileName);
                            } catch (CannotWriteException cwe) { // Case of a permission
                                String message = cwe.getMessage();
                                message += ("\nUser [" + System.getProperty("user.name") + "] seems not to have write access to " + gribDir);
                                JOptionPane.showMessageDialog(cp, message, "Download", JOptionPane.WARNING_MESSAGE);
                                cwe.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                WWContext.getInstance().fireInterruptProgress();
                                return;
                            }
                        }
                    }
                    int smooth = 1;
                    int timeSmooth = 1;

                    try {
                        smooth = Integer.parseInt(gribNode.getAttribute("smooth"));
                    } catch (Exception ignore) {
                    }
                    try {
                        timeSmooth = Integer.parseInt(gribNode.getAttribute("time-smooth"));
                    } catch (Exception ignore) {
                    }
                    // GRIB Smoothing ?
                    WWContext.getInstance().fireGribSmoothing(smooth);
                    WWContext.getInstance().fireGribTimeSmoothing(timeSmooth);
                    // Broadcast those values
                    WWContext.getInstance().fireGribSmoothingValue(smooth);
                    WWContext.getInstance().fireGribTimeSmoothingValue(timeSmooth);
                } catch (Exception ex) {
                    //      StaticObjects.getInstance().fireLogging("No GRIB node...");
                    cp.unsetGribData();
                    gribRequest = "";
                }
                cp.setGribRequest(gribRequest);
                // Now done first
                //        chartPanel.setW(w);
                //        chartPanel.setH(h);
                //        chartPanel.setBounds(0,0,w,h);

                if (xScroll != 0 || yScroll != 0) {
                    cp.getChartPanelScrollPane().getViewport().setViewPosition(new Point(xScroll, yScroll));
                }
                // Done. Should we save it?
                System.out.println("-- Composite [" + fileName + "] created. Saving?");
                //      System.out.println("Default Composite: [" + ((ParamPanel.DataFile) ParamPanel.data[ParamData.LOAD_COMPOSITE_STARTUP][ParamData.VALUE_INDEX]).toString() + "]");
                boolean autoSaveDefaultComposite = ((String) ParamPanel.data[ParamData.AUTO_SAVE_DEFAULT_COMPOSITE][ParamData.VALUE_INDEX]).trim().length() > 0;
                String compositeName = ((ParamPanel.DataFile) ParamPanel.data[ParamData.LOAD_COMPOSITE_STARTUP][ParamData.VALUE_INDEX]).toString();
                String ca[] = compositeName.split(",");
                for (int i = 0; i < ca.length; i++) {
                    if (ca[i].equals(fileName) && autoSaveDefaultComposite) {
                        try {
                            System.out.println("-- Created from [" + fileName + "]. Saving!");
                            String compositeDir = ((ParamPanel.DataDirectory) ParamPanel.data[ParamData.COMPOSITE_ROOT_DIR][ParamData.VALUE_INDEX]).toString();
                            // Warning!! If the LOAD_COMPOSITE_STARTUP is an array, the AUTO_SAVE_DEFAULT_COMPOSITE must be an array too.
                            String bigPattern = ((String) ParamPanel.data[ParamData.AUTO_SAVE_DEFAULT_COMPOSITE][ParamData.VALUE_INDEX]);
                            String patterns[] = bigPattern.split(",");
                            String onePattern = "";
                            if (patterns.length != ca.length) {
                                onePattern = patterns[0];
                                System.out.println(">>> Warning: Using " + onePattern + " for " + ca[i] + ".");
                            } else {
                                onePattern = patterns[i];
                            }
                            String dir = "", prefix = "", pattern = "", suffix = "", ext = "";
                            String[] patternElements = onePattern.split("\\|");
                            if (patternElements.length == 4) { // Old version
                                dir = compositeDir + patternElements[0].trim(); // "/yyyy/MM-MMM";
                                prefix = patternElements[1].trim();             // "Auto_";
                                pattern = patternElements[2].trim();             // "yyyy_MM_dd_HH_mm_ss_z";
                                ext = patternElements[3].trim();                // "waz";
                            } else if (patternElements.length == 5) { // New version
                                dir = compositeDir + patternElements[0].trim(); // "/yyyy/MM-MMM";
                                prefix = patternElements[1].trim();             // "Auto_";
                                pattern = patternElements[2].trim();             // "yyyy_MM_dd_HH_mm_ss_z";
                                suffix = patternElements[3].trim();             // "_Pacific";
                                ext = patternElements[4].trim();                // "waz";
                            }
                            Date now = new Date();
                            dir = WWGnlUtilities.translatePath(dir, now).replace('/', File.separatorChar);
                            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                            String saveAsName = dir + File.separator + prefix + sdf.format(now) + suffix + "." + ext;

                            File faxDir = new File(dir);
                            if (!faxDir.exists()) {
                                faxDir.mkdirs();
                            }
                            cp.runStorageThread(false, saveAsName); // TODO Chain possible user-exit. FTP on a site, etc.
                            System.out.println("-- Saved as [" + saveAsName + "]");

                        } catch (Exception ex) {
                            String message = "Error: " + ex.getLocalizedMessage() + "\nfor [" +
                                    ((String) ParamPanel.data[ParamData.AUTO_SAVE_DEFAULT_COMPOSITE][ParamData.VALUE_INDEX]) + "]";
                            if ("true".equals(System.getProperty("headless", "false")) || "yes".equals(System.getProperty("headless", "false"))) {
                                System.out.println(message);
                            } else {
                                JOptionPane.showMessageDialog(cp, message, "Auto-save", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    public static void buildPlaces(CommandPanel cp) {
        String placesFileName = PlacesTablePanel.PLACES_FILE_NAME;
        DOMParser parser = WWContext.getInstance().getParser();
        try {
            XMLDocument doc = null;
            synchronized (parser) {
                parser.setValidationMode(DOMParser.NONVALIDATING);
                parser.parse(new File(placesFileName).toURI().toURL());
                doc = parser.getDocument();
            }
            NodeList place = doc.selectNodes("//place");
            List<GeoPoint> alPos = new ArrayList<>(place.getLength());
            List<String> alName = new ArrayList<>(place.getLength());
            List<Boolean> alShow = new ArrayList<>(place.getLength());
            for (int i = 0; i < place.getLength(); i++) {
                GeoPoint gp = null;
                XMLElement xe = (XMLElement) place.item(i);
                String placeName = xe.getAttribute("name");
                String show = xe.getAttribute("show");
                if (show.trim().length() == 0) {
                    show = "true";
                }
                String degL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("deg");
                String minL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("min");
                String sgnL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("sign");
                String degG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("deg");
                String minG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("min");
                String sgnG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("sign");
                double l = GeomUtil.sexToDec(degL, minL);
                if (sgnL.equals("S")) {
                    l = -l;
                }
                double g = GeomUtil.sexToDec(degG, minG);
                if (sgnG.equals("W")) {
                    g = -g;
                }
                gp = new GeoPoint(l, g);
                alPos.add(gp);
                alName.add(placeName);
                alShow.add(Boolean.valueOf(show));
            }
            cp.setGpa(/*(GeoPoint[])*/alPos.toArray(new GeoPoint[alPos.size()]));
            cp.setPtLabels(/*(String[])*/alName.toArray(new String[alName.size()]));
            cp.setShowPlacesArray(alShow.toArray(new Boolean[alShow.size()]));
        } catch (FileNotFoundException fne) {
            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("places-not-found") + "\n");
            System.out.println(WWGnlUtilities.buildMessage("places-not-found"));
        } catch (Exception ex) {
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
    }

    public static void buildSailMailStations(CommandPanel cp) {
        cp.setSma(new ArrayList<WWGnlUtilities.SailMailStation>());
        String placesFileName = WWContext.SAILMAIL_STATIONS;
        DOMParser parser = WWContext.getInstance().getParser();
        try {
            XMLDocument doc = null;
            synchronized (parser) {
                parser.setValidationMode(DOMParser.NONVALIDATING);
                parser.parse(new File(placesFileName).toURI().toURL());
                doc = parser.getDocument();
            }
            NodeList place = doc.selectNodes("//place");
            //    List<GeoPoint> alPos  = new ArrayList<GeoPoint>(place.getLength());
            //    List<String> alName = new ArrayList<String>(place.getLength());
            //    List<Boolean> alShow = new ArrayList<Boolean>(place.getLength());
            for (int i = 0; i < place.getLength(); i++) {
                GeoPoint gp = null;
                XMLElement xe = (XMLElement) place.item(i);
                String placeName = xe.getAttribute("name");
                String show = xe.getAttribute("show");
                if (show.trim().length() == 0) {
                    show = "true";
                }
                String degL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("deg");
                String minL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("min");
                String sgnL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("sign");
                String degG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("deg");
                String minG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("min");
                String sgnG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("sign");
                double l = GeomUtil.sexToDec(degL, minL);
                if (sgnL.equals("S")) {
                    l = -l;
                }
                double g = GeomUtil.sexToDec(degG, minG);
                if (sgnG.equals("W")) {
                    g = -g;
                }
                gp = new GeoPoint(l, g);
                WWGnlUtilities.SailMailStation sms = new WWGnlUtilities.SailMailStation(gp, placeName);
                cp.getSma().add(sms);
            }
        } catch (FileNotFoundException fne) {
            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("file-not-found", new String[]{placesFileName}) + "\n");
            System.out.println(WWGnlUtilities.buildMessage("file-not-found", new String[]{placesFileName}));
        } catch (Exception ex) {
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
    }

    public static void buildWeatherStations(CommandPanel cp) {
        cp.setWsta(new ArrayList<WWGnlUtilities.WeatherStation>());
        String placesFileName = WWContext.NOAA_STATIONS;
        DOMParser parser = WWContext.getInstance().getParser();
        try {
            XMLDocument doc = null;
            synchronized (parser) {
                parser.setValidationMode(DOMParser.NONVALIDATING);
                parser.parse(new File(placesFileName).toURI().toURL());
                doc = parser.getDocument();
            }
            NodeList place = doc.selectNodes("//place");
            //    List<GeoPoint> alPos  = new ArrayList<GeoPoint>(place.getLength());
            //    List<String> alName = new ArrayList<String>(place.getLength());
            //    List<Boolean> alShow = new ArrayList<Boolean>(place.getLength());
            for (int i = 0; i < place.getLength(); i++) {
                GeoPoint gp = null;
                XMLElement xe = (XMLElement) place.item(i);
                String placeName = xe.getAttribute("name");
                String show = xe.getAttribute("show");
                if (show.trim().length() == 0) {
                    show = "true";
                }
                String degL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("deg");
                String minL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("min");
                String sgnL = ((XMLElement) xe.getElementsByTagName("latitude").item(0)).getAttribute("sign");
                String degG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("deg");
                String minG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("min");
                String sgnG = ((XMLElement) xe.getElementsByTagName("longitude").item(0)).getAttribute("sign");
                double l = GeomUtil.sexToDec(degL, minL);
                if (sgnL.equals("S")) {
                    l = -l;
                }
                double g = GeomUtil.sexToDec(degG, minG);
                if (sgnG.equals("W")) {
                    g = -g;
                }
                gp = new GeoPoint(l, g);
                WWGnlUtilities.WeatherStation ws = new WWGnlUtilities.WeatherStation(gp, placeName);
                cp.getWsta().add(ws);
            }
        } catch (FileNotFoundException fne) {
            WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("file-not-found", new String[]{placesFileName}) + "\n");
            System.out.println(WWGnlUtilities.buildMessage("file-not-found", new String[]{placesFileName}));
        } catch (Exception ex) {
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
    }

    public static int restoreComposite(String fileName,
                                       String option,
                                       Pattern faxPattern,
                                       boolean withBoatAndTrack,
                                       CommandPanel cp,
                                       int blurSharpOption) {
        int nbComponents = 0;
        boolean fromArchive = false;
        ZipFile waz = null;
        XMLDocument doc = null;
        DOMParser parser = WWContext.getInstance().getParser();
        synchronized (parser) {
            parser.setValidationMode(DOMParser.NONVALIDATING);
            WWContext.getInstance().setCurrentComposite(fileName);
            try {
                if (fileName.endsWith(WWContext.WAZ_EXTENSION)) {
                    fromArchive = true;
                    waz = new ZipFile(fileName);
                    ZipEntry composite = waz.getEntry("composite.xml");
                    if (composite != null) {
                        InputStream is = waz.getInputStream(composite);
                        parser.parse(is);
                    } else {
                        System.out.println("composite.xml not found :(");
                    }
                } else if (fileName.endsWith(".xml")) {
                    if (!(new File(fileName).isDirectory())) {
                        parser.parse(new File(fileName).toURI().toURL());
                    } else {
                        JOptionPane.showMessageDialog(cp, fileName + " is a directory", "Restoring Composite", JOptionPane.ERROR_MESSAGE);
                        return 0;
                    }
                }
                doc = parser.getDocument();

                if (doc.selectNodes("//composite-comment").getLength() > 0) {
                    cp.setCurrentComment(Utilities.superTrim(doc.selectNodes("//composite-comment").item(0).getFirstChild().getNodeValue()));
                    WWContext.getInstance().fireLogging("Comment:" + cp.getCurrentComment() + "\n");
                } else {
                    WWContext.getInstance().fireLogging("Reseting Comment...\n");
                    cp.setCurrentComment("");
                }

                String projType = "";
                try {
                    projType = ((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("type");
                } catch (Exception ignore) {
                    projType = CommandPanel.MERCATOR;
                }
                if (projType.equals(CommandPanel.MERCATOR)) {
                    cp.getChartPanel().setProjection(ChartPanel.MERCATOR);
                    WWContext.getInstance().fireSetProjection(ChartPanel.MERCATOR);
                } else if (projType.equals(CommandPanel.ANAXIMANDRE)) {
                    cp.getChartPanel().setProjection(ChartPanel.ANAXIMANDRE);
                    WWContext.getInstance().fireSetProjection(ChartPanel.ANAXIMANDRE);
                } else if (projType.equals(CommandPanel.LAMBERT)) {
                    cp.getChartPanel().setProjection(cp.getChartPanel().LAMBERT);
                    double cpar = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("contact-parallel"));
                    cp.getChartPanel().setContactParallel(cpar);
                    WWContext.getInstance().fireSetProjection(cp.getChartPanel().LAMBERT);
                    WWContext.getInstance().fireSetContactParallel(cpar);
                } else if (projType.equals(CommandPanel.CONIC_EQU)) {
                    cp.getChartPanel().setProjection(cp.getChartPanel().CONIC_EQUIDISTANT);
                    double cpar = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("contact-parallel"));
                    cp.getChartPanel().setContactParallel(cpar);
                    WWContext.getInstance().fireSetProjection(cp.getChartPanel().CONIC_EQUIDISTANT);
                    WWContext.getInstance().fireSetContactParallel(cpar);
                } else if (projType.equals(CommandPanel.GLOBE)) {
                    cp.getChartPanel().setProjection(cp.getChartPanel().GLOBE_VIEW);
                    WWContext.getInstance().fireSetProjection(cp.getChartPanel().GLOBE_VIEW);
                    //      StaticObjects.getInstance().fireSetGlobeParameters();
                } else if (projType.equals(CommandPanel.STEREO)) {
                    cp.getChartPanel().setProjection(cp.getChartPanel().STEREOGRAPHIC);
                    WWContext.getInstance().fireSetProjection(cp.getChartPanel().STEREOGRAPHIC);
                } else if (projType.equals(CommandPanel.POLAR_STEREO)) {
                    cp.getChartPanel().setProjection(ChartPanelInterface.POLAR_STEREOGRAPHIC);
                    WWContext.getInstance().fireSetProjection(ChartPanelInterface.POLAR_STEREOGRAPHIC);
                } else if (projType.equals(CommandPanel.SATELLITE)) {
                    cp.getChartPanel().setProjection(ChartPanelInterface.SATELLITE_VIEW);
                    WWContext.getInstance().fireSetProjection(ChartPanelInterface.SATELLITE_VIEW);
                    double nl = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("nadir-latitude"));
                    double ng = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("nadir-longitude"));
                    double alt = Double.parseDouble(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("altitude"));
                    boolean opaque = new Boolean(((XMLElement) doc.selectNodes("//projection").item(0)).getAttribute("opaque")).booleanValue();
                    cp.getChartPanel().setSatelliteAltitude(alt);
                    cp.getChartPanel().setSatelliteLatitude(nl);
                    cp.getChartPanel().setSatelliteLongitude(ng);
                    cp.getChartPanel().setTransparentGlobe(!opaque);
                    WWContext.getInstance().fireSetSatelliteParameters(nl, ng, alt, opaque);
                }
                try {
                    cp.setNLat(Double.parseDouble(doc.selectNodes("//north").item(0).getFirstChild().getNodeValue()));
                } catch (Exception ex) {
                }
                try {
                    cp.setSLat(Double.parseDouble(doc.selectNodes("//south").item(0).getFirstChild().getNodeValue()));
                } catch (Exception ex) {
                }
                try {
                    cp.setWLong(Double.parseDouble(doc.selectNodes("//west").item(0).getFirstChild().getNodeValue()));
                } catch (Exception ex) {
                }
                try {
                    cp.setELong(Double.parseDouble(doc.selectNodes("//east").item(0).getFirstChild().getNodeValue()));
                } catch (Exception ex) {
                }
                int w = 0;
                try {
                    w = Integer.parseInt(doc.selectNodes("//chartwidth").item(0).getFirstChild().getNodeValue());
                } catch (Exception ex) {
                }
                int h = 0;
                try {
                    h = Integer.parseInt(doc.selectNodes("//chartheight").item(0).getFirstChild().getNodeValue());
                } catch (Exception ex) {
                }

                boolean showChart = true;
                try {
                    XMLElement chartOpt = (XMLElement) (doc.selectNodes("//chart-opt").item(0));
                    if (chartOpt != null) {
                        showChart = "yes".equals(chartOpt.getAttribute("show"));
                    }
                } catch (Exception ignore) {
                }
                cp.setDrawChart(showChart);

                int xScroll = 0, yScroll = 0;
                try {
                    XMLElement scroll = (XMLElement) (doc.selectNodes("//scroll").item(0));
                    if (scroll != null) {
                        xScroll = Integer.parseInt(scroll.getAttribute("x"));
                        yScroll = Integer.parseInt(scroll.getAttribute("y"));
                    }
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                    doc.print(System.err);
                }
                try {
                    XMLElement faxOption = (XMLElement) (doc.selectNodes("//fax-option").item(0));
                    if (faxOption != null) {
                        String opt = faxOption.getAttribute("value");
                        if (opt.equals("CHECKBOX")) {
                            cp.setCheckBoxPanelOption(CommandPanel.CHECKBOX_OPTION);
                        } else if (opt.equals("RADIOBUTTON")) {
                            cp.setCheckBoxPanelOption(CommandPanel.RADIOBUTTON_OPTION);
                        } else {
                            System.out.println("Unknown option [" + opt + "]");
                        }
                    } else
                        System.err.println("No //fax-option node in this document.");
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                    doc.print(System.err);
                }

                // Boat Position?
                if ((withBoatAndTrack || option.equals(TwoFilePanel.EVERY_THING)) && doc.selectNodes("//boat-position").getLength() == 1) {
                    XMLElement bp = (XMLElement) doc.selectNodes("//boat-position").item(0);
                    double l = Double.parseDouble(bp.getAttribute("lat"));
                    double g = Double.parseDouble(bp.getAttribute("lng"));
                    int hdg = Integer.parseInt(bp.getAttribute("hdg"));
                    cp.setBoatPosition(new GeoPoint(l, g));
                    cp.setBoatHeading(hdg);
                }

                if (cp.getChartPanel().getProjection() != ChartPanelInterface.GLOBE_VIEW &&
                        cp.getChartPanel().getProjection() != ChartPanelInterface.SATELLITE_VIEW) {
                    cp.getChartPanel().setWidthFromChart(cp.getNLat(), cp.getSLat(), cp.getWLong(), cp.getELong());
                }
                //    eLong = cp.getChartPanel().calculateEastG(nLat, sLat, wLong);
                cp.getChartPanel().setEastG(cp.getELong());
                cp.getChartPanel().setWestG(cp.getWLong());
                cp.getChartPanel().setNorthL(cp.getNLat());
                cp.getChartPanel().setSouthL(cp.getSLat());

                cp.getChartPanel().setW(w);
                cp.getChartPanel().setH(h);
                cp.getChartPanel().setBounds(0, 0, w, h);

                cp.getChartPanel().repaint();

                if (option.equals(TwoFilePanel.EVERY_THING) || option.equals(TwoFilePanel.JUST_FAXES)) {
                    try {
                        NodeList faxes = doc.selectNodes("//fax-collection/fax");
                        cp.setFaxImage(new CommandPanel.FaxImage[faxes.getLength()]);
                        FaxType[] ft = new FaxType[faxes.getLength()];
                        for (int i = 0; i < faxes.getLength(); i++) {
                            WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("restoring-fax", new String[]{Integer.toString(i + 1),
                                    Integer.toString(faxes.getLength())}));
                            XMLElement fax = (XMLElement) faxes.item(i);
                            String faxName = fax.getAttribute("file");
                            if (faxPattern != null) {
                                Matcher m = faxPattern.matcher(faxName);
                                if (!m.find()) {
                                    continue;
                                }
                            }
                            nbComponents++;
                            Color c = WWGnlUtilities.buildColor(fax.getAttribute("color"));
                            String strRatio = fax.getAttribute("wh-ratio");
                            if (strRatio.trim().length() > 0) {
                                try {
                                    cp.setWHRatio(Double.parseDouble(strRatio));
                                } catch (Exception ignore) {
                                    ignore.printStackTrace();
                                }
                            } else {
                                cp.setWHRatio(1D);
                            }
                            String strTransparent = fax.getAttribute("transparent");
                            String strColorChange = fax.getAttribute("color-change");
                            double imageScale = Double.parseDouble(fax.selectNodes("./faxScale").item(0).getFirstChild().getNodeValue());
                            int imageHOffset = Integer.parseInt(fax.selectNodes("./faxXoffset").item(0).getFirstChild().getNodeValue());
                            int imageVOffset = Integer.parseInt(fax.selectNodes("./faxYoffset").item(0).getFirstChild().getNodeValue());
                            double imageRotation = 0D;
                            try {
                                imageRotation = Double.parseDouble(fax.selectNodes("./faxRotation").item(0).getFirstChild().getNodeValue());
                            } catch (Exception ignore) {
                                ignore.printStackTrace();
                            }
                            // New items (15-sep-2009)
                            String faxTitle = "";
                            String faxOrigin = "";
                            try {
                                faxTitle = fax.selectNodes("./faxTitle").item(0).getFirstChild().getNodeValue();
                            } catch (Exception ignore) { /* ignore.printStackTrace(); */ }
                            try {
                                faxOrigin = fax.selectNodes("./faxOrigin").item(0).getFirstChild().getNodeValue();
                            } catch (Exception ignore) { /* ignore.printStackTrace(); */ }

                            cp.getFaxImage()[i] = new CommandPanel.FaxImage();
                            cp.getFaxImage()[i].fileName = faxName;
                            cp.getFaxImage()[i].color = c;
                            cp.getFaxImage()[i].imageScale = imageScale;
                            cp.getFaxImage()[i].imageHOffset = imageHOffset;
                            cp.getFaxImage()[i].imageVOffset = imageVOffset;
                            cp.getFaxImage()[i].imageRotationAngle = imageRotation;
                            cp.getFaxImage()[i].comment = WWGnlUtilities.getHeader(faxName);
//            int gmtOffset = 0;
//            if (!cp.getFaxImage()[i].fileName.startsWith(WWContext.WAZ_PROTOCOL_PREFIX))
//              gmtOffset = TimeUtil.getLocalGMTOffset(); // Local
                            cp.getFaxImage()[i].created = (new File(faxName).lastModified()); // - (gmtOffset * 3600000L);

                            cp.getFaxImage()[i].show = true;
                            if (strTransparent == null || strTransparent.trim().length() == 0) {
                                cp.getFaxImage()[i].transparent = true;
                            } else {
                                cp.getFaxImage()[i].transparent = strTransparent.equals("true");
                            }
                            if (strColorChange == null || strColorChange.trim().length() == 0) {
                                cp.getFaxImage()[i].colorChange = true;
                            } else {
                                cp.getFaxImage()[i].colorChange = strColorChange.equals("true");
                            }
                            cp.getFaxImage()[i].faxTitle = faxTitle;
                            cp.getFaxImage()[i].faxOrigin = faxOrigin;
                            try {
                                if (fromArchive) {
                                    if (faxName.startsWith(WWContext.WAZ_PROTOCOL_PREFIX)) {
                                        faxName = faxName.substring(WWContext.WAZ_PROTOCOL_PREFIX.length());
                                    }
                                    try {
                                        InputStream is = waz.getInputStream(waz.getEntry(faxName)); // Possible NPE
                                        cp.getFaxImage()[i].created = waz.getEntry(faxName).getTime();
                                        boolean tif = faxName.toUpperCase().endsWith(".TIFF") || faxName.toUpperCase().endsWith(".TIF");
                                        if (cp.getFaxImage()[i].transparent) {
                                            if (cp.getFaxImage()[i].colorChange) {
                                                //    faxImage[i].faxImage = ImageUtil.makeTransparentImage(this, ImageUtil.readImage(is, tif), c);
                                                // faxImage[i].faxImage = ImageUtil.switchColorAndMakeColorTransparent(ImageUtil.readImage(is, tif), Color.black, c, Color.white, blurSharpOption);
                                                Image faxImg = ImageUtil.readImage(is, tif); //ImageUtil.readImage(faxName);
                                                if (ImageUtil.countColors(faxImg) > 2) {
                                                    faxImg = ImageUtil.switchAnyColorAndMakeColorTransparent(faxImg, c, ImageUtil.mostUsedColor(faxImg), blurSharpOption);
                                                } else {
                                                    faxImg = ImageUtil.switchColorAndMakeColorTransparent(faxImg, Color.black, c, Color.white, blurSharpOption);
                                                }
                                                cp.getFaxImage()[i].faxImage = faxImg;
                                            } else {
                                                cp.getFaxImage()[i].faxImage = ImageUtil.makeColorTransparent(ImageUtil.readImage(is, tif), Color.white, blurSharpOption);
                                            }
                                        } else {
                                            cp.getFaxImage()[i].faxImage = ImageUtil.readImage(is, tif);
                                        }
                                        is.close();
                                    } catch (NullPointerException npe) {
                                        System.err.println(String.format("%s not found in archive.", faxName));
                                    }
                                } else {
                                    if (cp.getFaxImage()[i].transparent) {
                                        if (cp.getFaxImage()[i].colorChange) {
                                            //  faxImage[i].faxImage = ImageUtil.makeTransparentImage(this, ImageUtil.readImage(faxName), c);
                                            cp.getFaxImage()[i].faxImage = ImageUtil.switchColorAndMakeColorTransparent(ImageUtil.readImage(faxName), Color.black, c, Color.white, blurSharpOption);
                                        } else {
                                            cp.getFaxImage()[i].faxImage = ImageUtil.makeColorTransparent(ImageUtil.readImage(faxName), Color.white, blurSharpOption);
                                        }
                                    } else {
                                        cp.getFaxImage()[i].faxImage = ImageUtil.readImage(faxName);
                                    }
                                }
                            } catch (Exception e) {
                                WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("file-not-found", new String[]{faxName}) + "\n");
                                throw e;
                            }
                            ft[i] = new FaxType(faxName, c, Boolean.valueOf(true), Boolean.valueOf(true), imageRotation, faxOrigin, faxTitle, cp.getFaxImage()[i].colorChange);
                            ft[i].setRank(i + 1);
                            ft[i].setComment(cp.getFaxImage()[i].comment);
                            ft[i].setShow(true);
                            ft[i].setTransparent(true);
                            cp.repaint(); // Repaint between each fax
                        }
                        if (faxPattern == null) {
                            cp.setCheckBoxes(ft);
                            if (faxes.getLength() > 0) {
                                WWContext.getInstance().fireFaxLoaded();
                                WWContext.getInstance().fireFaxesLoaded(ft);
                            }
                            cp.setCheckBoxes(ft);
                        }
                    } catch (Exception ex) {
                        WWContext.getInstance().fireExceptionLogging(ex);
                        ex.printStackTrace();
                        cp.unsetFaxImage();
                    }
                }
                // TODO IF there is a GRIB...
                if (option.equals(TwoFilePanel.EVERY_THING) || option.equals(TwoFilePanel.JUST_GRIBS)) {
                    try {
                        XMLElement gribNode = (XMLElement) doc.selectNodes("//grib").item(0);

                        String twsData = gribNode.getAttribute("display-TWS-Data");
                        if (twsData.trim().length() > 0) { // New version
                            cp.setDisplayTws("true".equals(twsData));
                            cp.setDisplayPrmsl("true".equals(gribNode.getAttribute("display-PRMSL-Data")));
                            cp.setDisplay500mb("true".equals(gribNode.getAttribute("display-500HGT-Data")));
                            cp.setDisplayWaves("true".equals(gribNode.getAttribute("display-WAVES-Data")));
                            cp.setDisplayTemperature("true".equals(gribNode.getAttribute("display-TEMP-Data")));
                            cp.setDisplayRain("true".equals(gribNode.getAttribute("display-PRATE-Data")));

                            cp.setDisplayContourTWS("true".equals(gribNode.getAttribute("display-TWS-contour")));
                            cp.setDisplayContourPRMSL("true".equals(gribNode.getAttribute("display-PRMSL-contour")));
                            cp.setDisplayContour500mb("true".equals(gribNode.getAttribute("display-500HGT-contour")));
                            cp.setDisplayContourWaves("true".equals(gribNode.getAttribute("display-WAVES-contour")));
                            cp.setDisplayContourTemp("true".equals(gribNode.getAttribute("display-TEMP-contour")));
                            cp.setDisplayContourPrate("true".equals(gribNode.getAttribute("display-PRATE-contour")));

                            cp.setDisplay3DTws("true".equals(gribNode.getAttribute("display-TWS-3D")));
                            cp.setDisplay3DPrmsl("true".equals(gribNode.getAttribute("display-PRMSL-3D")));
                            cp.setDisplay3D500mb("true".equals(gribNode.getAttribute("display-500HGT-3D")));
                            cp.setDisplay3DWaves("true".equals(gribNode.getAttribute("display-WAVES-3D")));
                            cp.setDisplay3DTemperature("true".equals(gribNode.getAttribute("display-TEMP-3D")));
                            cp.setDisplay3DRain("true".equals(gribNode.getAttribute("display-PRATE-3D")));
                        }
                        String wo = gribNode.getAttribute("wind-only"); // deprecated
                        if (wo.trim().length() > 0) {
                            cp.setWindOnly(Boolean.valueOf(wo));
                        }
                        String wc = gribNode.getAttribute("with-contour"); // deprecated
                        if (wc.trim().length() > 0) {
                            cp.setDisplayContourLines(Boolean.valueOf(wc));
                        }
                        String inLine = gribNode.getAttribute("in-line");
                        if (inLine != null && inLine.trim().equals("true")) {
                            // Deserialize
                            //        String gribRequest = gribNode.getAttribute("in-line-request");
                            //        XMLElement gribContent = (XMLElement)gribNode.getChildrenByTagName("java").item(0);
                            //        StringWriter sw = new StringWriter();
                            //        gribContent.print(sw);

                            cp.setGribFileName(gribNode.getFirstChild().getNodeValue());
                            WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("restoring-grib"));
                            String displayFileName = cp.getGribFileName();
                            InputStream is = null;
                            if (fromArchive) {
                                if (cp.getGribFileName().startsWith(WWContext.WAZ_PROTOCOL_PREFIX)) {
                                    cp.setGribFileName(cp.getGribFileName().substring(WWContext.WAZ_PROTOCOL_PREFIX.length()));
                                }
                                is = waz.getInputStream(waz.getEntry(cp.getGribFileName()));
                            } else {
                                is = new FileInputStream(new File(cp.getGribFileName()));
                            }
                            //        XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(sw.toString().getBytes()));
                            XMLDecoder decoder = new XMLDecoder(is);
                            Object o = decoder.readObject();
                            if (o instanceof GribFile) {
                                GribFile gf = (GribFile) o;
                                WWContext.getInstance().setGribFile(gf);
                                List<GribHelper.GribConditionData> agcd = GribHelper.dumper(gf, displayFileName);
                                GribHelper.GribConditionData wgd[] = agcd.toArray(new GribHelper.GribConditionData[agcd.size()]);
                                cp.setGribData(wgd, displayFileName);
                            } else { // For backward compatibility. 27-Feb-2009
                                GribHelper.GribConditionData wgd[] = (GribHelper.GribConditionData[]) o;
                                //        setGribData(wgd, gribRequest);
                                cp.setGribData(wgd, displayFileName);
                            }
                        } else {
                            cp.setGribFileName(gribNode.getFirstChild().getNodeValue());
                            WWContext.getInstance().fireProgressing(WWGnlUtilities.buildMessage("restoring-grib"));
                            String displayFileName = cp.getGribFileName();
                            GribHelper.GribConditionData wgd[] = null;
                            if (fromArchive) {
                                if (cp.getGribFileName().startsWith(WWContext.WAZ_PROTOCOL_PREFIX)) {
                                    cp.setGribFileName(cp.getGribFileName().substring(WWContext.WAZ_PROTOCOL_PREFIX.length()));
                                }
                                synchronized (cp) {
                                    InputStream is = waz.getInputStream(waz.getEntry(cp.getGribFileName()));
                                    try {
                                        wgd = GribHelper.getGribData(is, cp.getGribFileName());
                                    } catch (RuntimeException rte) {
                                        String mess = rte.getMessage();
                                        //                  System.out.println("RuntimeException getMessage(): [" + mess + "]");
                                        if (mess.startsWith("DataArray (width) size mismatch")) {
                                            System.out.println(mess);
                                        } else {
                                            throw rte;
                                        }
                                    }
                                    is.close();
                                }
                            } else {
                                wgd = GribHelper.getGribData(cp.getGribFileName(), true);
                            }
                            cp.setGribData(wgd, displayFileName);
                        }
                        nbComponents++;

                        // GRIB Smoothing
                        int smooth = 1;
                        int timeSmooth = 1;

                        try {
                            smooth = Integer.parseInt(gribNode.getAttribute("smooth"));
                        } catch (Exception ignore) {
                        }
                        try {
                            timeSmooth = Integer.parseInt(gribNode.getAttribute("time-smooth"));
                        } catch (Exception ignore) {
                        }
                        // GRIB Smoothing ?
                        WWContext.getInstance().fireGribSmoothing(smooth);
                        WWContext.getInstance().fireGribTimeSmoothing(timeSmooth);
                        // Broadcast those values
                        WWContext.getInstance().fireGribSmoothingValue(smooth);
                        WWContext.getInstance().fireGribTimeSmoothingValue(timeSmooth);
                    } catch (Exception ex) {
                        //      StaticObjects.getInstance().fireLogging("No GRIB node...");
                        cp.unsetGribData();
                    }
                }
//      setWindOnly(windOnly);
                // Now done above.
                //        cp.getChartPanel().setW(w);
                //        cp.getChartPanel().setH(h);
                //        cp.getChartPanel().setBounds(0,0,w,h);

                // GPX Data?
                if (withBoatAndTrack || option.equals(TwoFilePanel.EVERY_THING)) {
                    try {
                        NodeList gpxList = doc.selectNodes("//gpx-data/gpx-point");
                        int nl = gpxList.getLength();
                        if (nl == 0) {
                            cp.setGPXData(null);
                        } else {
                            cp.setGPXData(new ArrayList<GeoPoint>(nl));
                            for (int i = 0; i < nl; i++) {
                                XMLElement gpx = (XMLElement) gpxList.item(i);
                                GeoPoint gp = new GeoPoint(Double.parseDouble(gpx.getAttribute("lat")),
                                        Double.parseDouble(gpx.getAttribute("lng")));
                                cp.getGPXData().add(gp);
                            }
                            nbComponents++;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // Hand drawing
                NodeList hd = doc.selectNodes("//hand-drawings/hand-drawing-line");
                if (hd != null && hd.getLength() > 0) {
                    List<ChartPanel.PointList<GeoPoint>> drawings = new ArrayList<ChartPanel.PointList<GeoPoint>>();
                    for (int i = 0; i < hd.getLength(); i++) {
                        XMLElement line = (XMLElement) hd.item(i);
                        Color c = Color.red;
                        String att = line.getAttribute("color");
                        if (att != null) {
                            c = WWGnlUtilities.buildColor(att);
                        }
                        ChartPanel.PointList<GeoPoint> oneLine = new ChartPanel.PointList<GeoPoint>(c);
                        drawings.add(oneLine);
                        NodeList pointList = line.selectNodes("gp");
                        for (int j = 0; j < pointList.getLength(); j++) {
                            XMLElement gp = (XMLElement) pointList.item(j);
                            GeoPoint point = new GeoPoint(Double.parseDouble(gp.getAttribute("lat")),
                                    Double.parseDouble(gp.getAttribute("lng")));
                            oneLine.add(point);
                        }
                    }
                    cp.getChartPanel().setHandDrawing(drawings);
                }

                if (xScroll != 0 || yScroll != 0) {
                    cp.getChartPanelScrollPane().getViewport().setViewPosition(new Point(xScroll, yScroll));
                }
            } catch (Exception e) {
                WWContext.getInstance().fireExceptionLogging(e);
                e.printStackTrace();
            }
        }
        return nbComponents;
    }

    public static GribHelper.GribConditionData[] getGribFromComposite(String fileName) {
        ZipFile waz = null;
        XMLDocument doc = null;
        GribHelper.GribConditionData wgd[] = null;
        DOMParser parser = WWContext.getInstance().getParser();
        synchronized (parser) {
            parser.setValidationMode(DOMParser.NONVALIDATING);
            try {
                if (fileName.endsWith(WWContext.WAZ_EXTENSION)) {
                    waz = new ZipFile(fileName);
                    ZipEntry composite = waz.getEntry("composite.xml");
                    if (composite != null) {
                        InputStream is = waz.getInputStream(composite);
                        parser.parse(is);
                    } else {
                        System.out.println("composite.xml not found :(");
                    }
                } else {
                    // This should not happen
                }
                doc = parser.getDocument();
                // TODO IF there is a GRIB...
                try {
                    XMLElement gribNode = (XMLElement) doc.selectNodes("//grib").item(0);

                    String gribFileName = gribNode.getFirstChild().getNodeValue().substring(WWContext.WAZ_PROTOCOL_PREFIX.length());
                    InputStream is = waz.getInputStream(waz.getEntry(gribFileName));
                    try {
                        wgd = GribHelper.getGribData(is, gribFileName);
                    } catch (RuntimeException rte) {
                        String mess = rte.getMessage();
//                  System.out.println("RuntimeException getMessage(): [" + mess + "]");
                        if (mess.startsWith("DataArray (width) size mismatch")) {
                            System.out.println(mess);
                        } else {
                            throw rte;
                        }
                    }
                    is.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return wgd;
    }
}

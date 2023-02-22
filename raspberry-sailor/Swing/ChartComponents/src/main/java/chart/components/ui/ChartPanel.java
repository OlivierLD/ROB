package chart.components.ui;

import calc.*;
import chart.components.ui.gui.DistancePanel;
import chart.components.util.GnlUtilities;
import chart.components.util.MercatorUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class ChartPanel extends JPanel
        implements Scrollable,
        ChartPanelInterface,
        EventListener,
        MouseListener,
        Cloneable,
        MouseMotionListener,
        Printable {
    private final static long serialVersionUID = 1L;

    private int projection;

    public static final int NS = 0;
    public static final int EW = 1;

    public static final int MOUSE_CLICKED = 0;
    public static final int MOUSE_DRAGGED = 1;
    public static final int MOUSE_ENTERED = 2;
    public static final int MOUSE_EXITED = 3;
    public static final int MOUSE_MOVED = 4;
    public static final int MOUSE_PRESSED = 5;
    public static final int MOUSE_RELEASED = 6;

    public static final int MOUSE_AWAY_FROM_EDGES = 0;
    public static final int MOUSE_CLOSE_TO_TOP = 1;
    public static final int MOUSE_CLOSE_TO_BOTTOM = 2;
    public static final int MOUSE_CLOSE_TO_LEFT = 3;
    public static final int MOUSE_CLOSE_TO_RIGHT = 4;

    private int mouseEdgeProximity = MOUSE_AWAY_FROM_EDGES;
    private final static int EDGE_PROXIMITY = 30;
    private boolean mouseEdgeProximityDetectionEnabled = true;

    private boolean mouseDraggedEnabled;
    private boolean enablePositionTooltip;
    private boolean enforceTooltip = false;
    private boolean cleanFirst;
    private boolean withScale = false;
    private boolean withLngLabels = true;
    private boolean withInvertedLabels = false;
    private boolean withGrid = true;

    public static final int MOUSE_DRAG_ZOOM = 0;
    public static final int MOUSE_DRAG_GRAB_SCROLL = 1;
    public static final int MOUSE_DRAW_ON_CHART = 2;
    public static final int MOUSE_DRAW_LINE_ON_CHART = 3;

    private int mouseDraggedType = MOUSE_DRAG_ZOOM;

    private boolean confirmDDZoom = false;

    private Color gridColor;
    private Color altGridColor; // Difference behind/forward

    private Color chartColor;
    private Color chartBackGround;
    private transient GradientPaint backGroundGradient;
    private Color postitTextColor;
    private Color postitBGColor;
    private Color lopColor;
    private Color lopLineColor;
    private Color ddRectColor;

    private Color drawColor = Color.red;
    private int drawThickness = 3;

    private Color videoTrackColor = Color.red;
    private int videoTrackThickness = 3;

    private Cursor openHandCursor = null;
    private Cursor closedHandCursor = null;
    private Cursor drawingCursor = null;
    private Cursor handPointingRightCursor = null;
    private Cursor handPointingLeftCursor = null;
    private Cursor handPointingUpCursor = null;
    private Cursor handPointingDownCursor = null;

    private Cursor crossHairCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    private int w;
    private int h;
    private int origW, origH;
    private double zoom;
    private int draggedFromX;
    private int draggedFromY;
    private boolean dragged;
    private Rectangle draggingRectangle;
    private transient List<Point> lineToDraw = null;
    private transient List<PointList<GeoPoint>> handDrawing = null;
    private transient PointList<GeoPoint> oneDrawing = null;
    private boolean plotHandDrawing = true;

    private double east;
    private double west;
    private double north;
    private double south;
    private double contactParallel;
    private double conic_ratio;
    private double conic_rotation;
    private int conicOffset_X;
    private int conicOffset_Y;
    private double globeView_ratio;
    private int globeViewOffset_X;
    private int globeViewOffset_Y;
    private double stereoView_ratio;
    private int stereoViewOffset_X;
    private int stereoViewOffset_Y;
    private double globeViewLngOffset = 0.0;
    private double globeViewRightLeftRotation = 0.0;
    private double globeViewForeAftRotation = 0.0;
    private boolean transparentGlobe = true; // Display front part only if false
    private boolean antiTransparentGlobe = true; // Display aft part only if false

    private transient GreatCircle gc = null;
    private double satelliteHorizonDiameter = 0D;

    private double vGrid;
    private double hGrid;
    private transient ChartPanelParentInterface parent;
    private double _north;
    private double _south;
    private double _east;
    private double _west;

    private static boolean printResize = true;

    private static final int AZIMUTH_LENGTH = 50;
    private DistancePanel dp = null;

    private double majorLatitudeTick = 0.25; // quarter degree: 15 miles
    private double minorLatitudeTick = (1d / 12d); // 5 miles

    public ChartPanel(ChartPanelParentInterface cppi) {
        this(cppi, 800, 600);
    }

    public ChartPanel(ChartPanelParentInterface cppi, int w, int h) {
        projection = ChartPanelInterface.MERCATOR;
        mouseDraggedEnabled = true;
        enablePositionTooltip = true;
        cleanFirst = true;
        gridColor = Color.BLACK;
        chartColor = Color.GRAY;
        chartBackGround = Color.white;
        backGroundGradient = null;
        postitTextColor = Color.black;
        postitBGColor = Color.white;
        lopColor = Color.black;
        lopLineColor = Color.MAGENTA;
        ddRectColor = Color.gray;
        this.origW = w; // 800;
        this.origH = h; // 600;
        zoom = 1.1D;
        draggedFromX = -1;
        draggedFromY = -1;
        dragged = false;
        draggingRectangle = null;
        lineToDraw = null;
        east = 0.0D;
        west = 0.0D;
        north = 0.0D;
        south = 0.0D;
        contactParallel = 0.0D;
        conic_ratio = 200D;
        conic_rotation = 0.0D;
        conicOffset_X = 0;
        conicOffset_Y = 0;
        globeView_ratio = 200D;
        globeViewOffset_X = 0;
        globeViewOffset_Y = 0;

        vGrid = 5D;
        hGrid = 5D;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        parent = cppi;
        this.w = w;
        this.h = h;
    }

    private void jbInit() throws Exception {
        setLayout(null);
        addMouseListener(this);
        addMouseMotionListener(this);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        setToolTipText("This bubble gives the position");

        this.setBackground(new Color(187, 220, 218));
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        String imgFileName = "PanOpenHand32x32.png";
        Image image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        openHandCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);
        imgFileName = "PanClosedHand32x32.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        closedHandCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);

//  imgFileName = "HandPointingRight.png";
        imgFileName = "right.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
//  Dimension dim = toolkit.getBestCursorSize(16, 16); // Still 32x32...
        handPointingRightCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);
//  imgFileName = "HandPointingLeft.png";
        imgFileName = "left.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        handPointingLeftCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);
//   imgFileName = "HandPointingUp.png";
        imgFileName = "up.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        handPointingUpCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);
//  imgFileName = "HandPointingDown.png";
        imgFileName = "down.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        handPointingDownCursor = toolkit.createCustomCursor(image, new Point(15, 15), imgFileName);

        imgFileName = "crayon.png";
        image = toolkit.getImage(ChartPanel.class.getResource(imgFileName));
        drawingCursor = toolkit.createCustomCursor(image, new Point(2, 30), imgFileName);
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setDimension(Dimension d) {
        this.w = d.width;
        this.h = d.height;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        int val = 1;
        if (orientation == 1)
            val = visibleRect.height / 100;
        else if (orientation == 0)
            val = visibleRect.width / 100;
        return val;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        int val = 1;
        if (orientation == 1)
            val = visibleRect.height / 10;
        else if (orientation == 0)
            val = visibleRect.width / 10;
        return val;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void setZoomFactor(double z) {
        zoom = z;
    }

    public double getZoomFactor() {
        return zoom;
    }

    public void zoomIn() {
        Rectangle visible = this.getVisibleRect();
        int centerX = visible.x + (visible.width / 2);
        int centerY = visible.y + (visible.height / 2);
        GeoPoint gp = this.getGeoPos(centerX, centerY);
        w *= zoom;
        h *= zoom;
        setPreferredSize(new Dimension(w, h));
        setSize(new Dimension(w, h));
        if (gp != null)
            positionTo(gp);
        repaint();
    }

    public void zoomOut() {
        Rectangle visible = this.getVisibleRect();
        int centerX = visible.x + (visible.width / 2);
        int centerY = visible.y + (visible.height / 2);
        GeoPoint gp = this.getGeoPos(centerX, centerY);
        w /= zoom;
        h /= zoom;
        setPreferredSize(new Dimension(w, h));
        setSize(new Dimension(w, h));
        if (gp != null) {
            positionTo(gp);
        }
        repaint();
    }

    public void applyZoom(double z) {
        w = (int) (origW * z);
        h = (int) (origH * z);
        repaint();
    }

    public double getEastG() {
        return east;
    }

    public double getWestG() {
        return west;
    }

    public double getNorthL() {
        return north;
    }

    public double getSouthL() {
        return south;
    }

    public void setEastG(double g) {
        east = g;
    }

    public void setWestG(double g) {
        west = g;
    }

    public void setNorthL(double l) {
        north = l;
    }

    public void setSouthL(double l) {
        south = l;
    }

    public void setContactParallel(double d) {
        contactParallel = d;
    }

    public double getContactParallel() {
        return contactParallel;
    }

    public void setVerticalGridInterval(double i) {
        vGrid = i;
    }

    public void setHorizontalGridInterval(double i) {
        hGrid = i;
    }

    public double getVerticalGridInterval() {
        return vGrid;
    }

    public double getHorizontalGridInterval() {
        return hGrid;
    }

    public void setCleanFirst(boolean b) {
        cleanFirst = b;
    }

    public boolean getCleanFirst() {
        return cleanFirst;
    }

    public void setGridColor(Color c) {
        gridColor = c;
    }

    public void setChartColor(Color c) {
        chartColor = c;
    }

    public void setChartBackGround(Color c) {
        chartBackGround = c;
    }

    public void setChartBackGround(GradientPaint bgg) {
        backGroundGradient = bgg;
    }

    public void setPostitTextColor(Color c) {
        postitTextColor = c;
    }

    public void setPostitBGColor(Color c) {
        postitBGColor = c;
    }

    public void setLopColor(Color c) {
        lopColor = c;
    }

    public void setLopLineColor(Color c) {
        lopLineColor = c;
    }

    public void setDdRectColor(Color c) {
        ddRectColor = c;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Color getChartColor() {
        return chartColor;
    }

    public Color getChartBackGround() {
        return chartBackGround;
    }

    public Color getPostitTextColor() {
        return postitTextColor;
    }

    public Color getPostitBGColor() {
        return postitBGColor;
    }

    public Color getLopColor() {
        return lopColor;
    }

    public Color getDdRectColor() {
        return ddRectColor;
    }

    public Color getLopLineColor() {
        return lopLineColor;
    }

    public void setPositionToolTipEnabled(boolean b) {
//  System.out.println((b?"Enabling":"Disabling") + " tooltip on chart");
        enablePositionTooltip = b;
    }

    public boolean isPositionToolTipEnabled() {
        if ((getProjection() == GLOBE_VIEW || getProjection() == SATELLITE_VIEW) && !enforceTooltip) {
            return false;
        } else {
            return enablePositionTooltip;
        }
    }

    public void setProjection(int i) {
        projection = i;
    }

    public int getProjection() {
        return projection;
    }

    private boolean playVideo = false;
    private boolean pauseVideo = false;
    private int videoStart = 0;
    private List array = null;
    private int currentVideoIndex = Integer.MIN_VALUE;
    private int videoIncrement = 1;

    public synchronized boolean isVideoPlaying() {
        return playVideo;
    }

    public synchronized boolean isVideoPaused() {
        return pauseVideo;
    }

    public int getCurrentVideoIndex() {
        return currentVideoIndex;
    }

    public void setCurrentVideoIndex(int i) {
        currentVideoIndex = i;
    }

    public void setVideoStart(int i) {
        videoStart = i;
    }

    public synchronized void stopVideo() {
        playVideo = false;
        pauseVideo = false;
        videoStart = 0;
        parent.videoCompleted();
        currentVideoIndex = Integer.MIN_VALUE;
    }

    public void pauseVideo() {
        pauseVideo = true;
    }

    private long videoSleep = 100L;

    public void setvideoSleep(long l) {
        videoSleep = l;
    }

    public void playVideo(final List al) {
        playVideo = true;
        pauseVideo = false;
        array = al;
        Thread videoThread = new Thread(() -> {
            // System.out.println("Starting video for " + al.size() + " points, at " + videoStart);
            for (int i = videoStart; i >= 0 && i < al.size() && playVideo; i += videoIncrement) {
                if (!pauseVideo) {
                    currentVideoIndex = i;
                    repaint();
                    try {
                        Thread.sleep(videoSleep);
                    } catch (InterruptedException ignore) {
                    }
                } else {
                    //     System.out.println("Pausing video at " + i);
                    videoStart = i;
                    break;
                }
            }
            if (!pauseVideo) {
                parent.videoCompleted();
                videoStart = 0;
                playVideo = false; // If it finishes by itself
            }
//        System.out.println("Video " + (pauseVideo?"paused":"completed"));
        });
        synchronized (videoThread) {
            videoThread.start();
        }
    }

    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (cleanFirst) {
            if (backGroundGradient != null)
                ((Graphics2D) g).setPaint(backGroundGradient);
            else
                g.setColor(chartBackGround);
            g.fillRect(0, 0, w, h);
        }
        g.setColor(gridColor);
        setPreferredSize(new Dimension(w, h));
        setSize(new Dimension(w, h));
        if ((getProjection() == ChartPanelInterface.GLOBE_VIEW ||
                getProjection() == ChartPanelInterface.SATELLITE_VIEW) && withGrid)
            redrawGrid(g);
        g.setColor(chartColor);
        parent.chartPanelPaintComponent(g);
        if (withGrid)
            redrawGrid(g);

        if (playVideo) {
            Point previous = null;
            Stroke originalStroke = null;
            if (g instanceof Graphics2D) {
                originalStroke = ((Graphics2D) g).getStroke();
                Stroke stroke = new BasicStroke(videoTrackThickness,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL);
                ((Graphics2D) g).setStroke(stroke);
            }
            g.setColor(videoTrackColor);
            for (int i = 0; i <= currentVideoIndex; i++) {
                Point pt = this.getPanelPoint(((GeoPoint) array.get(i)).getL(),
                        ((GeoPoint) array.get(i)).getG());
                if (previous != null)
                    g.drawLine(previous.x, previous.y, pt.x, pt.y);
                previous = pt;
            }
            parent.videoFrameCompleted(g, previous);
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setStroke(originalStroke);
            }
        }
        if (parent instanceof ChartPanelParentInterface_II) {
            ((ChartPanelParentInterface_II) parent).chartPanelPaintComponentAfter(g);
        }
        // Following ones always on top of everything
        if (dragged && draggingRectangle != null) {
            g.setColor(ddRectColor);
            g.drawRect(draggingRectangle.x,
                    draggingRectangle.y,
                    draggingRectangle.width,
                    draggingRectangle.height);
        }

        if (/* dragged && */ oneDrawing != null || handDrawing != null) {
            g.setColor(drawColor);
            Stroke originalStroke = null;
            if (g instanceof Graphics2D) {
                originalStroke = ((Graphics2D) g).getStroke();
                Stroke stroke = new BasicStroke(drawThickness,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL);
                ((Graphics2D) g).setStroke(stroke);
            }
            if (handDrawing != null && plotHandDrawing) {
                Iterator<PointList<GeoPoint>> iterator = handDrawing.iterator();
                while (iterator.hasNext()) {
                    PointList<GeoPoint> al = iterator.next();
                    g.setColor(al.getLineColor());
                    Iterator<GeoPoint> iterator2 = al.iterator();
                    GeoPoint previous = null;
                    while (iterator2.hasNext()) {
                        GeoPoint dpt = iterator2.next();
                        if (previous != null) {
                            Point f = getPanelPoint(previous.getL(), previous.getG());
                            Point t = getPanelPoint(dpt.getL(), dpt.getG());
                            g.drawLine(f.x, f.y, t.x, t.y);
                        }
                        previous = dpt;
                    }
                }
            }
            if (/*dragged && */oneDrawing != null && plotHandDrawing) // Current one
            {
                g.setColor(drawColor);
                Iterator iterator3 = oneDrawing.iterator();
                GeoPoint previous = null;
                while (iterator3.hasNext()) {
                    GeoPoint dpt = (GeoPoint) iterator3.next();
                    if (previous != null) {
                        Point f = getPanelPoint(previous.getL(), previous.getG());
                        Point t = getPanelPoint(dpt.getL(), dpt.getG());
                        g.drawLine(f.x, f.y, t.x, t.y);
                    }
                    previous = dpt;
                }
            }
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setStroke(originalStroke);
            }
        }
        if (dragged && lineToDraw != null) {
            Point p1 = lineToDraw.get(0);
            Point p2 = lineToDraw.get(1);
            Stroke originalStroke = null;
            if (g instanceof Graphics2D) {
                originalStroke = ((Graphics2D) g).getStroke();
                float miterLimit = 10F;
                float[] dashPattern = {10F};
                float dashPhase = 5F;
                Stroke stroke = new BasicStroke(1,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        miterLimit,
                        dashPattern,
                        dashPhase);
                ((Graphics2D) g).setStroke(stroke);
            }
            // Rhumbline
            g.setColor(Color.blue);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            // Great Circle
            GeoPoint from = getGeoPos(p1.x, p1.y);
            GeoPoint to = getGeoPos(p2.x, p2.y);
            // Calculate GC
            GeoPoint geoFrom = new GeoPoint(Math.toRadians(from.getL()), Math.toRadians(from.getG()));
            GeoPoint geoTo = new GeoPoint(Math.toRadians(to.getL()), Math.toRadians(to.getG()));
            if (gc == null) {
                gc = new GreatCircle();
            }
            gc.setStart(new GreatCirclePoint(geoFrom));
            gc.setArrival(new GreatCirclePoint(geoTo));
            gc.calculateRhumbLine();
            gc.calculateGreatCircle(20);
            Vector<GreatCircleWayPoint> route = gc.getRoute();
            g.setColor(Color.red);
            Enumeration<GreatCircleWayPoint> waypoints = route.elements();
            Point previous = null;
            while (waypoints.hasMoreElements()) {
                GreatCircleWayPoint gcwp = waypoints.nextElement();
                Point pp = getPanelPoint(new GeoPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG())));
                if (previous != null)
                    g.drawLine(previous.x, previous.y, pp.x, pp.y);
                previous = pp;
            }
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setStroke(originalStroke);
            }
            // Display data
            g.setColor(Color.black);
            // TODO Localize
            String one = "From " + from.toString() + "\nTo " + to.toString();
            String two = "Rhumbline:" + (Integer.toString((int) gc.calculateRhumbLineDistance())) + "', " + (Integer.toString((int) Math.toDegrees(gc.calculateRhumbLineRoute())) + "\272T");
            String three = "Great Circle:" + Integer.toString((int) (Math.toDegrees(gc.getDistance()) * 60.0)) + "'";
            postit(g, one + "\n" + two + "\n" + three, p2.x, p2.y, Color.blue, Color.white, 0.75f);
        }
    }

    public void undoLastHandDrawing() {
        if (handDrawing != null && handDrawing.size() > 0) {
            handDrawing.remove(handDrawing.size() - 1);
            repaint();
        }
    }

    public void redrawGrid(Graphics g) {
        adjustBoundaries();
        g.setColor(gridColor);
        switch (projection) {
            case ChartPanelInterface.ANAXIMANDRE:
            case ChartPanelInterface.MERCATOR:
                drawSquareGrid(g);
                break;
            case ChartPanelInterface.LAMBERT:
            case ChartPanelInterface.CONIC_EQUIDISTANT:
                drawConicGrid(g);
                break;
            case ChartPanelInterface.GLOBE_VIEW:
                drawSphericGrid(g);
                break;
            case ChartPanelInterface.SATELLITE_VIEW:
                drawSatelliteGrid(g);
                break;
            case ChartPanelInterface.STEREOGRAPHIC:
                drawStereoGrid(g);
                break;
            case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                drawPolarStereoGrid(g);
                break;
            default:
                System.out.println("What? from drawGrid");
                break;
        }
    }

    private void drawSquareGrid(Graphics g) {
        if (_north != _south && _east != _west) {
            double gAmpl; // = Math.abs(_east - _west);
            for (gAmpl = _east - _west; gAmpl < 0D; gAmpl += 360D) ;
            double lAmpl = 0.0D;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    lAmpl = Math.abs(_north - _south);
                    break;
                case ChartPanelInterface.MERCATOR:
                    lAmpl = Math.abs(MercatorUtil.getIncLat(_north) - MercatorUtil.getIncLat(_south));
                    break;
            }
            double graph2chartRatio = 1.0D;
            double gOrig;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                case ChartPanelInterface.MERCATOR:
                    graph2chartRatio = (double) w / gAmpl;
                    break;
                case ChartPanelInterface.LAMBERT:
                case ChartPanelInterface.CONIC_EQUIDISTANT:
                case ChartPanelInterface.GLOBE_VIEW:
                case ChartPanelInterface.SATELLITE_VIEW:
                default:
                    break;
            }
            gOrig = Math.ceil(_west);
            double gProgress = gOrig;
            if (gProgress % vGrid != 0D)
                gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
            boolean go = true;
            double __east = _east;
            if (gAmpl > 180D)
                __east += 360D;
            while (go) {
                int x = (int) ((gProgress - _west) * graph2chartRatio);
                g.drawLine(x, 0, x, h);
                if (withLngLabels) {
                    double labelProgress = gProgress;
                    if (labelProgress > 180) labelProgress -= 360;
                    String label = getLabel(labelProgress, ChartPanel.EW);
                    g.drawString(label, x, 10 + (withScale ? 10 : 0)); // Top
                    g.drawString(label, x, h - (5 + (withScale ? 10 : 0))); // Bottom
                }
                gProgress += vGrid;
                if (gProgress > __east)
                    go = false;
            }

            if (withScale) // Longitude scale
            {
                g.drawRect(1, 1, w - 1, h - 1);
                g.drawLine(0, 4, w, 4);
                g.drawLine(0, 8, w, 8);

                g.drawLine(0, h - 4, w, h - 4);
                g.drawLine(0, h - 8, w, h - 8);

                int prevx = 0;
                boolean black = false;
                for (double lng = _west; lng < __east; lng += 0.25) {
                    if (projection == ChartPanelInterface.MERCATOR) {
                        int x = (int) ((lng - _west) * graph2chartRatio);
                        if (lng != _west) {
                            if (black) {
                                g.drawLine(prevx, 2, x, 2);
                                g.drawLine(prevx, h - 2, x, h - 2);
                            }
                            g.drawLine(x, 0, x, 10);
                            g.drawLine(x, h, x, h - 10);
                        }
                        black = !black;
                        prevx = x;
                    }
                }
                for (double lng = _west; lng < _east; lng += (1.0 / 12.0)) {
                    if (projection == ChartPanelInterface.MERCATOR) {
                        int x = (int) ((lng - _west) * graph2chartRatio);
                        g.drawLine(x, 4, x, 8);
                        g.drawLine(x, h - 4, x, h - 8);
                    }
                }
            }

            double lOrig = Math.ceil(_south) - _south % hGrid;
            double incSouth = 0.0D;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    incSouth = _south;
                    break;
                case ChartPanelInterface.MERCATOR:
                    incSouth = MercatorUtil.getIncLat(_south);
                    break;
            }
            double lProgress = lOrig;
            if (lProgress % hGrid != (double) 0)
                lProgress = (double) ((int) (lProgress / hGrid) + 1) * hGrid;
            if (withScale) // Lat Scale
            {
                g.drawLine(4, 0, 4, h);
                g.drawLine(8, 0, 8, h);

                g.drawLine(w - 4, 0, w - 4, h);
                g.drawLine(w - 8, 0, w - 8, h);

                int prevy = 0;
                boolean black = false;
                for (double lat = _south; lat < _north; lat += majorLatitudeTick) // Every 10 miles
                {
                    if (projection == ChartPanelInterface.MERCATOR) {
                        double il = MercatorUtil.getIncLat(lat);
                        int y = h - (int) ((il - incSouth) * graph2chartRatio);
                        if (lat != _south) {
                            if (black) {
                                g.drawLine(2, prevy, 2, y);
                                g.drawLine(w - 2, prevy, w - 2, y);
                            }
                            g.drawLine(0, y, 10, y);
                            g.drawLine(w, y, w - 10, y);
                        }
                        black = !black;
                        prevy = y;
                    }
                }
                for (double lat = _south; lat < _north; lat += minorLatitudeTick) {
                    if (projection == ChartPanelInterface.MERCATOR) {
                        double il = MercatorUtil.getIncLat(lat);
                        int y = h - (int) ((il - incSouth) * graph2chartRatio);
                        g.drawLine(4, y, 8, y);
                        g.drawLine(w - 4, y, w - 8, y);
                    }
                }
            }
            go = true;
            while (go) {
                double incLProgress = 0.0D;
                switch (projection) {
                    case ChartPanelInterface.ANAXIMANDRE:
                        incLProgress = lProgress;
                        break;
                    case ChartPanelInterface.MERCATOR:
                        incLProgress = MercatorUtil.getIncLat(lProgress);
                        break;
                }
                int y = 0;
                switch (projection) {
                    case ChartPanelInterface.ANAXIMANDRE:
                        y = h - (int) ((incLProgress - incSouth) * ((double) h / lAmpl));
                        break;
                    case ChartPanelInterface.MERCATOR:
                        y = h - (int) ((incLProgress - incSouth) * graph2chartRatio);
                        break;
                }
                g.drawLine(0, y, w, y);
                String label = getLabel(lProgress, ChartPanel.NS);
                int strWidth = g.getFontMetrics(g.getFont()).stringWidth(label);
//      int strHeight = g.getFontMetrics(g.getFont()).getHeight();
                g.drawString(label, (withScale ? 9 : 1), y - 1);
                if (!withInvertedLabels)
                    g.drawString(label, w - (strWidth + (withScale ? 9 : 1)), y - 1);
                if (withInvertedLabels) {
                    if (g instanceof Graphics2D) {
                        Graphics2D g2 = (Graphics2D) g;
                        AffineTransform oldTx = g2.getTransform();

                        String invLabel = getLabel(-lProgress, ChartPanel.NS);
                        strWidth = g.getFontMetrics(g.getFont()).stringWidth(invLabel);

                        AffineTransform ct = AffineTransform.getTranslateInstance(strWidth + (withScale ? 9 : 1), y);
                        g2.transform(ct);

                        g2.transform(AffineTransform.getRotateInstance(Math.PI));
//          g2.drawString(invLabel, 0, -2);
                        g.drawString(invLabel, (strWidth + (2 * (withScale ? 9 : 1))) - w, -2);
                        g2.setTransform(oldTx);
                    }
                }
                lProgress += hGrid;
                if (lProgress > _north)
                    go = false;
            }
        } else {
            for (int i = w / 10; i < w; i += w / 10)
                g.drawLine(i, 0, i, h);
            for (int i = h / 10; i < h; i += h / 10)
                g.drawLine(0, i, w, i);
        }
    }

    private void drawConicGrid(Graphics g) {
        if (projection != ChartPanelInterface.LAMBERT && projection != ChartPanelInterface.CONIC_EQUIDISTANT) {
            System.out.println("Wow! What's that!?");
            return;
        }
        if (projection == ChartPanelInterface.LAMBERT || projection == ChartPanelInterface.CONIC_EQUIDISTANT) {
            if (contactParallel == 0.0D) {
                JOptionPane.showMessageDialog(this, "Contact parallel cannot be 0.0", "Lambert Projection", JOptionPane.ERROR_MESSAGE);
                System.out.println("Contact Parallel cannot be 0.0");
                return;
            }
        }
        double graph2chartRatio = 1.0D;

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double gOrig = Math.ceil(_west);
        double gProgress = gOrig;
        if (gProgress % vGrid != (double) 0)
            gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
        boolean go = true;
        while (go) {
            double[] xy = null;
            if (projection == ChartPanelInterface.LAMBERT)
                xy = calculateLambertCoordinates(Math.toRadians(_south), Math.toRadians(gProgress), Math.toRadians(contactParallel));
            if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                xy = calculateCECoordinates(Math.toRadians(_south), Math.toRadians(gProgress), Math.toRadians(contactParallel));
            double dx = xy[0];
            double dy = xy[1];
            if (dx < minX)
                minX = dx;
            if (dx > maxX)
                maxX = dx;
            if (dy < minY)
                minY = dy;
            if (dy > maxY)
                maxY = dy;
            if (projection == ChartPanelInterface.LAMBERT)
                xy = calculateLambertCoordinates(Math.toRadians(_north), Math.toRadians(gProgress), Math.toRadians(contactParallel));
            if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                xy = calculateCECoordinates(Math.toRadians(_north), Math.toRadians(gProgress), Math.toRadians(contactParallel));
            dx = xy[0];
            dy = xy[1];
            if (dx < minX)
                minX = dx;
            if (dx > maxX)
                maxX = dx;
            if (dy < minY)
                minY = dy;
            if (dy > maxY)
                maxY = dy;
            gProgress += vGrid;
            if (gProgress > _east)
                go = false;
        }
        double conicWidth = Math.abs(maxX - minX);
        double conicHeight = Math.abs(maxY - minY);
        conic_ratio = Math.min((double) w / conicWidth, (double) h / conicHeight);
        conicOffset_X = Math.abs((int) (conic_ratio * conicWidth) - w) / 2 - (int) (conic_ratio * minX);
        conicOffset_Y = (Math.abs((int) (conic_ratio * conicHeight) - h) / 2) - (int) (conic_ratio * minY);

        if (_north != _south && _east != _west) {
//    System.out.println("Determining the rotation angle");
//    System.out.println("New method:" + ((_east + _west) / 2));
            {
                double[] xy = null;
                if (projection == ChartPanelInterface.LAMBERT)
                    xy = calculateLambertCoordinates(Math.toRadians(_south), Math.toRadians(_west), Math.toRadians(contactParallel));
                if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                    xy = calculateCECoordinates(Math.toRadians(_south), Math.toRadians(_west), Math.toRadians(contactParallel));
                double dx = xy[0];
                int x1 = (int) Math.round(conic_ratio * dx);
                double dy = xy[1];
                int y1 = (int) Math.round(conic_ratio * dy);
                if (projection == ChartPanelInterface.LAMBERT)
                    xy = calculateLambertCoordinates(Math.toRadians(_south), Math.toRadians(_east), Math.toRadians(contactParallel));
                if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                    xy = calculateCECoordinates(Math.toRadians(_south), Math.toRadians(_east), Math.toRadians(contactParallel));
                dx = xy[0];
                int x2 = (int) Math.round(conic_ratio * dx);
                dy = xy[1];
                int y2 = (int) Math.round(conic_ratio * dy);
                int deltaX = x2 - x1;
                int deltaY = y2 - y1;
                double atan = (double) deltaY / (double) deltaX;
                conic_rotation = Math.atan(atan);
                if (deltaX < 0D)
                    conic_rotation += Math.PI;
                gOrig = Math.ceil(_west);
                gProgress = gOrig;
                if (gProgress % vGrid != (double) 0)
                    gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
                go = true;
                while (go) {
                    double[] xy_ = null;
                    if (projection == ChartPanelInterface.LAMBERT)
                        xy_ = calculateLambertCoordinates(Math.toRadians(_south), Math.toRadians(gProgress), Math.toRadians(contactParallel));
                    if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                        xy_ = calculateCECoordinates(Math.toRadians(_south), Math.toRadians(gProgress), Math.toRadians(contactParallel));
                    int x1_ = (int) Math.round(conic_ratio * xy_[0]);
                    int y1_ = (int) Math.round(conic_ratio * xy_[1]);
                    if (projection == ChartPanelInterface.LAMBERT)
                        xy_ = calculateLambertCoordinates(Math.toRadians(_north), Math.toRadians(gProgress), Math.toRadians(contactParallel));
                    if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                        xy_ = calculateCECoordinates(Math.toRadians(_north), Math.toRadians(gProgress), Math.toRadians(contactParallel));
                    int x2_ = (int) Math.round(conic_ratio * xy_[0]);
                    int y2_ = (int) Math.round(conic_ratio * xy_[1]);
                    Point pt1 = rotate(new Point(conicOffset_X + x1_, conicOffset_Y + y1_));
                    Point pt2 = rotate(new Point(conicOffset_X + x2_, conicOffset_Y + y2_));
                    g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                    gProgress += vGrid;
                    if (gProgress > _east)
                        go = false;
                }
                double lOrig = Math.ceil(_south) - _south % hGrid;
                double lProgress = lOrig;
                if (lProgress % hGrid != (double) 0)
                    lProgress = (double) ((int) (lProgress / hGrid) + 1) * hGrid;
                go = true;
                minX = Double.MAX_VALUE;
                maxX = -Double.MAX_VALUE;
                minY = Double.MAX_VALUE;
                maxY = -Double.MAX_VALUE;
                while (go) {
                    int previousX = Integer.MAX_VALUE;
                    int previousY = Integer.MAX_VALUE;
                    for (double d = _west; d <= _east; d += 5D) {
                        double[] xy_ = null;
                        if (projection == ChartPanelInterface.LAMBERT)
                            xy_ = calculateLambertCoordinates(Math.toRadians(lProgress), Math.toRadians(d), Math.toRadians(contactParallel));
                        if (projection == ChartPanelInterface.CONIC_EQUIDISTANT)
                            xy_ = calculateCECoordinates(Math.toRadians(lProgress), Math.toRadians(d), Math.toRadians(contactParallel));
                        double dx_ = xy_[0];
                        int x = (int) Math.round(conic_ratio * dx_);
                        double dy_ = xy_[1];
                        int y = (int) Math.round(conic_ratio * dy_);
                        if (dx < minX)
                            minX = dx;
                        if (dx > maxX)
                            maxX = dx;
                        if (dy < minY)
                            minY = dy;
                        if (dy > maxY)
                            maxY = dy;
                        if (previousX != Integer.MAX_VALUE && previousY != Integer.MAX_VALUE) {
                            Point pt1 = rotate(new Point(conicOffset_X + previousX, conicOffset_Y + previousY));
                            Point pt2 = rotate(new Point(conicOffset_X + x, conicOffset_Y + y));
                            g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                            if (lProgress == contactParallel)
                                g.drawOval(pt2.x - 1, pt2.y - 1, 2, 2);
                        }
                        previousX = x;
                        previousY = y;
                    }

                    lProgress += hGrid;
                    if (lProgress > _north)
                        go = false;
                }
            }
        } else {
            for (int i = w / 10; i < w; i += w / 10)
                g.drawLine(i, 0, i, h);

            for (int i = h / 10; i < h; i += h / 10)
                g.drawLine(0, i, w, i);
        }
    }

    private void drawSphericGrid(Graphics g) {
        if (projection != ChartPanelInterface.GLOBE_VIEW) {
            System.out.println("Wow!");
            return;
        }
        switch (projection) {
            case ChartPanelInterface.GLOBE_VIEW:
                double minX = Double.MAX_VALUE;
                double maxX = -Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxY = -Double.MAX_VALUE;

                double gOrig = Math.ceil(_west);
                double gProgress = gOrig;

                if (gProgress % vGrid != 0d)
                    gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
                boolean go = true;

//        double __south = _south + globeViewForeAftRotation;
//        double __north = _north - globeViewForeAftRotation;

                double __south = -90;
                double __north = 90;

                while (false && go) // TODO Remove that shit
                {
                    double dx = calculateGlobeViewXCoordinates(Math.toRadians(__south), Math.toRadians(gProgress));
                    double dy = calculateGlobeViewYCoordinates(Math.toRadians(__south), Math.toRadians(gProgress));
//        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;

                    dx = calculateGlobeViewXCoordinates(Math.toRadians(__north), Math.toRadians(gProgress));
                    dy = calculateGlobeViewYCoordinates(Math.toRadians(__north), Math.toRadians(gProgress));
//        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    gProgress += vGrid;
                    if (gProgress > _east)
                        go = false;
                }

                while (go) {
                    for (double _lat = __south; _lat <= __north; _lat += 5d) {
                        double dx = calculateGlobeViewXCoordinates(Math.toRadians(_lat), Math.toRadians(gProgress));
                        double dy = calculateGlobeViewYCoordinates(Math.toRadians(_lat), Math.toRadians(gProgress));
                        //        System.out.println("dx:" + dx + ", dy:" + dy);
                        if (dx < minX) minX = dx;
                        if (dx > maxX) maxX = dx;
                        if (dy < minY) minY = dy;
                        if (dy > maxY) maxY = dy;
                    }
                    gProgress += vGrid;
                    if (gProgress > _east)
                        go = false;
                }

                gOrig = Math.ceil(__south);
                double lProgress = gOrig;
                if (lProgress % hGrid != (double) 0)
                    lProgress = (double) ((int) (lProgress / hGrid) + 1) * hGrid;
                go = true;
                while (go) {
                    double dx = calculateGlobeViewXCoordinates(Math.toRadians(lProgress), Math.toRadians(_west));
                    double dy = calculateGlobeViewYCoordinates(Math.toRadians(lProgress), Math.toRadians(_west));
//        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    dx = calculateGlobeViewXCoordinates(Math.toRadians(lProgress), Math.toRadians(_east));
                    dy = calculateGlobeViewYCoordinates(Math.toRadians(lProgress), Math.toRadians(_east));
//        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    lProgress += hGrid;
                    if (lProgress > __north)
                        go = false;
                }
//      System.out.println("MinX:" + minX + ", MaxX:" + maxX + ", MinY:" + minY + ", MaxY:" + maxY);
                double opWidth = Math.abs(maxX - minX);
                double opHeight = Math.abs(maxY - minY);
                globeView_ratio = Math.min((double) w / opWidth, (double) h / opHeight);

                globeViewOffset_X = Math.abs((int) (globeView_ratio * opWidth) - w) / 2 - (int) (globeView_ratio * minX);
                globeViewOffset_Y = Math.abs((int) (globeView_ratio * opHeight) - h) / 2 - (int) (globeView_ratio * minY);

//      System.out.println("GV Ratio:" + globeView_ratio + ", GVoffsetX:" + globeViewOffset_X + ", GVoffsetY:" + globeViewOffset_Y);
                break;
            default:
                break;
        }

        double gstep = 10D; //Math.abs(_east - _west) / 60;
        double lstep = 10D;  //Math.abs(_north - _south) / 10;
        // Meridians
        for (double i = Math.min(_east, _west); i < Math.max(_east, _west); i += gstep) {
            Point previous = null;
            for (double j = Math.min(_south, _north) + (lstep / 5); j < Math.max(_south, _north); j += (lstep / 5)) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
        // Parallels
        for (double j = Math.min(_south, _north) + lstep; j < Math.max(_south, _north); j += lstep) {
            Point previous = null;
            for (double i = Math.min(_east, _west); i <= Math.max(_east, _west); i += gstep) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
    }

    private void drawSatelliteGrid(Graphics g) {
        if (projection != ChartPanelInterface.SATELLITE_VIEW) {
            System.out.println("Wow! What are we doing here?");
            return;
        }
        switch (projection) {
            case ChartPanelInterface.SATELLITE_VIEW:
                double minX = Double.MAX_VALUE;
                double maxX = -Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxY = -Double.MAX_VALUE;

                for (double lng = _west; lng < _east; lng += vGrid) {
                    for (double lat = _south; lat < _north; lat += hGrid) {
                        double xy[] = getSatelliteViewXY(lat, lng);
                        double dx = xy[0];
                        double dy = xy[1];
                        if (dx < minX) minX = dx;
                        if (dx > maxX) maxX = dx;
                        if (dy < minY) minY = dy;
                        if (dy > maxY) maxY = dy;
                    }
                }
//        System.out.println("MinX:" + minX + ", MaxX:" + maxX);
//        System.out.println("MinY:" + minY + ", MaxY:" + maxY);
                double opWidth = Math.abs(maxX - minX);
                double opHeight = Math.abs(maxY - minY);
                globeView_ratio = Math.min((double) w / opWidth, (double) h / opHeight);
//        System.out.println("Sat Ratio:" + globeView_ratio);

                globeViewOffset_X = (Math.abs((int) (globeView_ratio * opWidth) - w) / 2) - (int) (globeView_ratio * minX);
                globeViewOffset_Y = (Math.abs((int) (globeView_ratio * opHeight) - h) / 2) - (int) (globeView_ratio * minY);
                break;
            default:
                break;
        }

        double gstep = 5D; //Math.abs(_east - _west) / 60;
        double lstep = 5D;  //Math.abs(_north - _south) / 10;

//    System.out.println("Min Lat :" + Math.min(_south, _north));
//    System.out.println("Max Lat :" + Math.max(_south, _north));
//    System.out.println("Min Long:" + Math.min(_east, _west));
//    System.out.println("Min Long:" + Math.max(_east, _west));
        // Meridians
        for (double i = Math.min(_east, _west); i < Math.max(_east, _west); i += gstep) {
            Point previous = null;
            for (double j = Math.min(_south, _north); j < Math.max(_south, _north); j += lstep) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (!isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (!isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null) {
                        g.drawLine(previous.x, previous.y, p.x, p.y);
//          g.fillOval(p.x - 1, p.y - 1, 2, 2);
                    }
                    previous = p;
                }
            }
        }
        // Parallels
        for (double j = Math.min(_south, _north); j < Math.max(_south, _north); j += lstep) {
            Point previous = null;
            for (double i = Math.min(_east, _west); i <= Math.max(_east, _west); i += gstep) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (!isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (!isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null) {
                        g.drawLine(previous.x, previous.y, p.x, p.y);
//          g.fillOval(p.x - 1, p.y - 1, 2, 2);
                    }
                    previous = p;
                }
            }
        }
    }

    private void drawStereoGrid(Graphics g) {
        if (projection != ChartPanelInterface.STEREOGRAPHIC) {
            System.out.println("Wow! What are we doing here?");
            return;
        }
        switch (projection) {
            case ChartPanelInterface.STEREOGRAPHIC:
                double minX = Double.MAX_VALUE;
                double maxX = -Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxY = -Double.MAX_VALUE;

                double gOrig = Math.ceil(_west);
                double gProgress = gOrig;

                if (gProgress % vGrid != 0d)
                    gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
                boolean go = true;

//      double __south = _south + globeViewForeAftRotation;
//      double __north = _north - globeViewForeAftRotation;

                double __south = -90;
                double __north = 90;

                while (go) {
                    for (double _lat = __south; _lat <= __north; _lat += 5d) {
                        double[] xy = calculateStereoGraphicXYCoordinates(_lat, gProgress);
                        double dx = xy[0];
                        double dy = xy[1];
                        //        System.out.println("dx:" + dx + ", dy:" + dy);
                        if (dx < minX) minX = dx;
                        if (dx > maxX) maxX = dx;
                        if (dy < minY) minY = dy;
                        if (dy > maxY) maxY = dy;
                    }
                    gProgress += vGrid;
                    if (gProgress > _east)
                        go = false;
                }

                gOrig = Math.ceil(__south);
                double lProgress = gOrig;
                if (lProgress % hGrid != (double) 0)
                    lProgress = (double) ((int) (lProgress / hGrid) + 1) * hGrid;
                go = true;
                while (go) {
                    double[] xy = calculateStereoGraphicXYCoordinates(lProgress, _west);
                    double dx = xy[0];
                    double dy = xy[1];
                    //        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    xy = calculateStereoGraphicXYCoordinates(lProgress, _east);
                    dx = xy[0];
                    dy = xy[1];
                    //        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    lProgress += hGrid;
                    if (lProgress > __north)
                        go = false;
                }
//      System.out.println("Stereo - MinX:" + minX + ", MaxX:" + maxX + ", MinY:" + minY + ", MaxY:" + maxY);
                double opWidth = Math.abs(maxX - minX);
                double opHeight = Math.abs(maxY - minY);
                stereoView_ratio = Math.min((double) w / opWidth, (double) h / opHeight);
//      System.out.println("Width:" + opWidth + ", Height:" + opHeight);
//      stereoViewOffset_X = (int)(- minX);
//      stereoViewOffset_Y = (int)(- minY);
                stereoViewOffset_X = ((int) Math.abs((int) (stereoView_ratio * opWidth) - w) / 2) - (int) (stereoView_ratio * minX);
//      stereoViewOffset_Y = ((int)Math.abs((int)(stereoView_ratio * opHeight) - h) / 2) - (int)(stereoView_ratio * minY);
                stereoViewOffset_Y = ((int) Math.abs((int) (stereoView_ratio * opHeight) - h) / 2) + (int) (stereoView_ratio * maxY);
//      System.out.println("StereoView Ratio:" + stereoView_ratio + ", SVoffsetX:" + stereoViewOffset_X + ", SVoffsetY:" + stereoViewOffset_Y);
//      System.out.println("NewWidth:" + (opWidth * stereoView_ratio) + ", newHeight:" + (opHeight * stereoView_ratio));
                break;
            default:
                break;
        }

        double gstep = 10D; //Math.abs(_east - _west) / 60;
        double lstep = 10D;  //Math.abs(_north - _south) / 10;
        // Meridians
        for (double i = Math.min(_east, _west); i < Math.max(_east, _west); i += gstep) {
            Point previous = null;
            for (double j = Math.min(_south, _north) + (lstep / 5); j < Math.max(_south, _north); j += (lstep / 5)) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
        // Parallels
        for (double j = Math.min(_south, _north) + lstep; j < Math.max(_south, _north); j += lstep) {
            Point previous = null;
            for (double i = Math.min(_east, _west); i <= Math.max(_east, _west); i += gstep) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
    }

    private void drawPolarStereoGrid(Graphics g) {
        if (projection != ChartPanelInterface.POLAR_STEREOGRAPHIC) {
            System.out.println("Wow! What are we doing here?");
            return;
        }
        switch (projection) {
            case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                double minX = Double.MAX_VALUE;
                double maxX = -Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxY = -Double.MAX_VALUE;

                double gOrig = Math.ceil(_west);
                double gProgress = gOrig;

                if (gProgress % vGrid != 0d)
                    gProgress = (double) ((int) (gProgress / vGrid) + 1) * vGrid;
                boolean go = true;

                //      double __south = _south + globeViewForeAftRotation;
                //      double __north = _north - globeViewForeAftRotation;

                double __south = -90;
                double __north = 90;

                while (go) {
                    for (double _lat = __south; _lat <= __north; _lat += 5d) {
                        double[] xy = calculatePolarStereoGraphicXYCoordinates(_lat, gProgress);
                        double dx = xy[0];
                        double dy = xy[1];
                        //        System.out.println("dx:" + dx + ", dy:" + dy);
                        if (dx < minX) minX = dx;
                        if (dx > maxX) maxX = dx;
                        if (dy < minY) minY = dy;
                        if (dy > maxY) maxY = dy;
                    }
                    gProgress += vGrid;
                    if (gProgress > _east)
                        go = false;
                }

                gOrig = Math.ceil(__south);
                double lProgress = gOrig;
                if (lProgress % hGrid != (double) 0)
                    lProgress = (double) ((int) (lProgress / hGrid) + 1) * hGrid;
                go = true;
                while (go) {
                    double[] xy = calculatePolarStereoGraphicXYCoordinates(lProgress, _west);
                    double dx = xy[0];
                    double dy = xy[1];
                    //        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    xy = calculatePolarStereoGraphicXYCoordinates(lProgress, _east);
                    dx = xy[0];
                    dy = xy[1];
                    //        System.out.println("dx:" + dx + ", dy:" + dy);
                    if (dx < minX) minX = dx;
                    if (dx > maxX) maxX = dx;
                    if (dy < minY) minY = dy;
                    if (dy > maxY) maxY = dy;
                    lProgress += hGrid;
                    if (lProgress > __north)
                        go = false;
                }
                //      System.out.println("Stereo - MinX:" + minX + ", MaxX:" + maxX + ", MinY:" + minY + ", MaxY:" + maxY);
                double opWidth = Math.abs(maxX - minX);
                double opHeight = Math.abs(maxY - minY);
                stereoView_ratio = Math.min((double) w / opWidth, (double) h / opHeight);
                //      System.out.println("Width:" + opWidth + ", Height:" + opHeight);
                //      stereoViewOffset_X = (int)(- minX);
                //      stereoViewOffset_Y = (int)(- minY);
                stereoViewOffset_X = ((int) Math.abs((int) (stereoView_ratio * opWidth) - w) / 2) - (int) (stereoView_ratio * minX);
                stereoViewOffset_Y = 0; // ((int)Math.abs((int)(stereoView_ratio * opHeight) - h) / 2) - (int)(stereoView_ratio * minY);
                //    stereoViewOffset_Y = ((int)Math.abs((int)(stereoView_ratio * opHeight) - h) / 2) + (int)(stereoView_ratio * minY);
                //      System.out.println("StereoView Ratio:" + stereoView_ratio + ", SVoffsetX:" + stereoViewOffset_X + ", SVoffsetY:" + stereoViewOffset_Y);
                //      System.out.println("NewWidth:" + (opWidth * stereoView_ratio) + ", newHeight:" + (opHeight * stereoView_ratio));
                break;
            default:
                break;
        }

        double gstep = 10D; //Math.abs(_east - _west) / 60;
        double lstep = 10D;  //Math.abs(_north - _south) / 10;
        // Meridians
        for (double i = Math.min(_east, _west); i < Math.max(_east, _west); i += gstep) {
            Point previous = null;
            for (double j = Math.min(_south, _north) + (lstep / 5); j < Math.max(_south, _north); j += (lstep / 5)) {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
        // Parallels
        for (double j = Math.min(_south, _north) + lstep; j < Math.max(_south, _north); j += lstep) {
            Point previous = null;
            for (double i = Math.min(_east, _west); i <= Math.max(_east, _west); i += (gstep / 10)) // 10, for a smooth circle.
            {
                Point p = getPanelPoint(j, i);
                boolean thisPointIsBehind = isBehind(j, i - globeViewLngOffset);
                if (altGridColor != null) {
                    if (thisPointIsBehind)
                        g.setColor(altGridColor);
                    else
                        g.setColor(gridColor);
                }
                if (projection == ChartPanelInterface.GLOBE_VIEW && !isTransparentGlobe() && thisPointIsBehind)
                    previous = null;
                else if (projection == ChartPanelInterface.GLOBE_VIEW && !isAntiTransparentGlobe() && !thisPointIsBehind)
                    previous = null;
                else {
                    if (previous != null)
                        g.drawLine(previous.x, previous.y, p.x, p.y);
                    previous = p;
                }
            }
        }
    }

    private Point rotate(Point pt) {
        Point ret = null;
        double angle = 0.0;
        if (projection == ChartPanelInterface.LAMBERT || projection == ChartPanelInterface.CONIC_EQUIDISTANT)
            angle = conic_rotation;
        else if (projection == ChartPanelInterface.GLOBE_VIEW) // TODO What do we need this for?
            angle = Math.toRadians(globeViewRightLeftRotation);
        else if (projection == ChartPanelInterface.SATELLITE_VIEW)
            angle = Math.toRadians(satLat);
        int deltaX = w / 2 - pt.x;
        int deltaY = h / 2 - pt.y;
        int newX = (int) ((double) deltaX * Math.cos(angle) + (double) deltaY * Math.sin(angle));
        int newY = (int) ((double) (-deltaX) * Math.sin(angle) + (double) deltaY * Math.cos(angle));
        ret = new Point(w / 2 - newX, h / 2 - newY);
        return ret;
    }

    private Point rotateBack(Point pt) {
        Point ret = null;
        int deltaX = w / 2 - pt.x;
        int deltaY = h / 2 - pt.y;
        int newX = (int) ((double) deltaX * Math.cos(-conic_rotation) + (double) deltaY * Math.sin(-conic_rotation));
        int newY = (int) ((double) (-deltaX) * Math.sin(-conic_rotation) + (double) deltaY * Math.cos(-conic_rotation));
        ret = new Point(w / 2 - newX, h / 2 - newY);
        return ret;
    }

    private double[] calculateLambertCoordinates(double lat, double lng, double pc) {
        double x = calculateLambertXCoordinates(lat, lng, pc);
        double y = calculateLambertYCoordinates(lat, lng, pc);
        return new double[]{x, y};
    }

    private double calculateLambertXCoordinates(double lat, double lng, double pc) {
        double d = (Math.cos(lat) * Math.sin(lng * Math.sin(pc))) / (Math.sin(pc) * Math.cos(lat - pc));
        return d;
    }

    private double calculateLambertYCoordinates(double lat, double lng, double pc) {
        double d = (Math.cos(lat) * Math.cos(lng * Math.sin(pc))) / (Math.sin(pc) * Math.cos(lat - pc));
        return d;
    }

    private double[] calculateCECoordinates(double lat, double lng, double cp) {
        double den = Math.sin(cp) * Math.cos(lat - cp);

        double x = (Math.cos(lat) * Math.sin(lng * Math.sin(cp))) / den;
        double y = (Math.cos(lat) * Math.cos(lng * Math.sin(cp))) / den;

        return new double[]{x, y};
    }

    /**
     * alpha, then beta
     *
     * @param lat
     * @param lng
     * @return x, y, z. Cartesian coordinates.
     */
    private double[] rotateBothWays(double lat, double lng) {
        double x = Math.cos(lat) * Math.sin(lng);
        double y = Math.sin(lat);
        double z = Math.cos(lat) * Math.cos(lng);

        // Left Right, ca fout le bordel.

        double alfa = Math.toRadians(globeViewRightLeftRotation); // in plan (x, y), z unchanged, earth inclination on its axis
        double beta = Math.toRadians(globeViewForeAftRotation);   // in plan (y, z), x unchanged, latitude of the eye
        /*
         *                      |  cos a -sin a  0 |  a > 0 : counter clockwise
         * Rotation plan x, y:  |  sin a  cos a  0 |
         *                      |    0     0     1 |
         *
         *                      | 1    0      0    |  b > 0 : towards user
         * Rotation plan y, z:  | 0  cos b  -sin b |
         *                      | 0  sin b   cos b |
         *
         *  | x |   | cos a -sin a  0 |   | 1   0      0    |   | x |   |  cos a  (-sin a * cos b) (sin a * sin b) |
         *  | y | * | sin a  cos a  0 | * | 0  cos b -sin b | = | y | * |  sin a  (cos a * cos b) (-cos a * sin b) |
         *  | z |   |  0      0     1 |   | 0  sin b  cos b |   | z |   |   0          sin b           cos b       |
         */

        // All in once
        double _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa) * Math.cos(beta)) + (z * Math.sin(alfa) * Math.sin(beta));
        double _y = (x * Math.sin(alfa)) + (y * Math.cos(alfa) * Math.cos(beta)) - (z * Math.cos(alfa) * Math.sin(beta));
        double _z = (y * Math.sin(beta)) + (z * Math.cos(beta));

//    double _x = (x * Math.cos(alfa)) + (y * Math.sin(alfa));
//    double _y = ((-x) * Math.sin(alfa) * Math.cos(beta)) + (y * Math.cos(alfa) * Math.cos(beta)) + (z * Math.sin(beta));
//    double _z = (x * Math.sin(alfa) * Math.sin(beta)) - (y * Math.cos(beta) * Math.sin(beta)) + (z * Math.cos(beta));

        // One by one
        if (false) {
            _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa));
            _y = (x * Math.sin(alfa)) + (y * Math.cos(alfa));
            _z = z;

            _x = (_x);
            _y = (_y * Math.cos(beta)) - (_z * Math.sin(beta));
            _z = (_y * Math.sin(beta)) + (_z * Math.cos(beta));
        }
        return new double[]{_x, _y, _z};
    }

    /**
     * beta then alfa
     *
     * @param lat
     * @param lng
     * @return
     */
    private double[] rotateBothWays_stby(double lat, double lng) {
        double x = Math.cos(lat) * Math.sin(lng);
        double y = Math.sin(lat);
        double z = Math.cos(lat) * Math.cos(lng);

        double alfa = Math.toRadians(globeViewRightLeftRotation);
        double beta = Math.toRadians(globeViewForeAftRotation);
        /*
         *  | y |   |  cos a  -sin a  0 |   | 1   0      0    |
         *  | x | * |  sin a   cos a  0 | * | 0  cos b -sin b |
         *  | z |   |   0       0     1 |   | 0  sin b  cos b |
         */
        double _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa));
        double _y = (Math.cos(beta) * ((x * Math.sin(alfa)) + (y * Math.cos(alfa)))) - (z * Math.sin(beta));
        double _z = (Math.sin(beta) * ((x * Math.sin(alfa)) + (y * Math.cos(alfa)))) + (z * Math.cos(beta));

        //    double _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa) * Math.cos(beta)) + (z * Math.sin(alfa) * Math.sin(beta));
        //    double _y = (x * Math.sin(alfa)) + (y * Math.cos(alfa) * Math.cos(beta)) - (z * Math.cos(alfa) * Math.sin(beta));
        //    double _z = (y * Math.sin(beta)) + (z * Math.cos(beta));

        //    double _x =  (x   * Math.cos(alfa)) + (y * Math.sin(alfa));
        //    double _y = ((-x) * Math.sin(alfa) * Math.cos(beta)) + (y * Math.cos(alfa) * Math.cos(beta)) + (z * Math.sin(beta));
        //    double _z =  (x   * Math.sin(alfa) * Math.sin(beta)) - (y * Math.cos(alfa) * Math.sin(beta)) + (z * Math.cos(beta));

        // beta, then alfa
        //    double _x = (x * Math.cos(alfa)) - (Math.sin(alfa) * ((y * Math.cos(beta)) - (z * Math.sin(beta))));
        //    double _y = (x * Math.sin(alfa)) - (Math.cos(alfa) * ((y * Math.cos(beta)) - (z * Math.sin(beta))));
        //    double _z = (y * Math.sin(beta)) + (z * Math.cos(beta));

        //    double _x = (x * Math.cos(alfa)) - (Math.sin(alfa) * ((y * Math.cos(beta)) + (z * Math.sin(beta))));
        //    double _y = (x * Math.sin(alfa)) + (Math.cos(alfa) * ((y * Math.cos(beta)) + (z * Math.sin(beta))));
        //    double _z = ((-y) * Math.sin(beta)) + (z * Math.cos(beta));

        return new double[]{_x, _y, _z};
    }

    private double calculateGlobeViewXCoordinates(double lat,
                                                  double lng) {
        return rotateBothWays(lat, lng)[0];
    }

    private double calculateGlobeViewYCoordinates(double lat,
                                                  double lng) {
        return rotateBothWays(lat, lng)[1];
    }

    private double calculateGlobeViewZCoordinates(double lat,
                                                  double lng) {
        return rotateBothWays(lat, lng)[2];
    }

    private static final double R = 635677D / 2000D;

    private double[] calculateStereoGraphicXYCoordinates(double lat,
                                                         double lng) {
        double[] xy = new double[2];
        double lambdaZero = (west + east) / 2;
        double phiOne = (north + south) / 2;
        double k = (2 * R) / (1 + (Math.sin(Math.toRadians(phiOne)) * Math.sin(Math.toRadians(lat))) +
                (Math.cos(Math.toRadians(phiOne)) * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lng - lambdaZero))));
        double x = k * Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lng - lambdaZero));
        double y = k * ((Math.cos(Math.toRadians(phiOne)) * Math.sin(Math.toRadians(lat))) -
                (Math.sin(Math.toRadians(phiOne)) * Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lng - lambdaZero))));

        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    private double[] calculatePolarStereoGraphicXYCoordinates(double lat,
                                                              double lng) {
        double[] xy = new double[2];
        double lambdaZero = (west + east) / 2;
//  double phiOne     = (north + south) / 2;

        double phi = lat;
        if (phi > 0)
            phi = 90 - phi;
        else
            phi = -90 - phi;

        double x = -(2 * R) * Math.tan(Math.PI - Math.toRadians(phi / 2)) * Math.sin(Math.toRadians(lng - lambdaZero));
        double y = (2 * R) * Math.tan(Math.PI - Math.toRadians(phi / 2)) * Math.cos(Math.toRadians(lng - lambdaZero));

        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    /*
     * For Satellite view:
     *
     * thetaP : longitude of the projection
     * alfa   : latitude of interest
     * theta  : longitude of interest
     * rhoS   : altitude of the satellite (100s of km)
     * rhoE   : Earth diameter (100s of km). 6356.77 km
     *
     * F = cos(alfa) * cos(theta - thetaP)
     * R = rhoE / rhoS
     * C = rhoS - (rhoE * F)
     * X = (rhoS * rhoE * sin(theta - thetaP) * cos(alfa)) / C
     * Y = (rhoS * rhoE * sin(alfa)) / C
     */
    private double rhoE = 635677D; // Earth radius
    private double rhoS = 1600000D; // 16 000 km -- Satellite radius (not altitude)
    private double thetaP = 0D;  // Satellite Longitude
    private double satLat = 0D;  // Satellite latitude

    private static double[] rotation(double[] col,
                                     double[][] matrix) {
        double _x = 0D, _y = 0D, _z = 0D;
        _x = (col[0] * matrix[0][0]) + (col[1] * matrix[0][1]) + (col[2] * matrix[0][2]);
        _y = (col[0] * matrix[1][0]) + (col[1] * matrix[1][1]) + (col[2] * matrix[1][2]);
        _z = (col[0] * matrix[2][0]) + (col[1] * matrix[2][1]) + (col[2] * matrix[2][2]);

        return new double[]{_x, _y, _z};
    }

    private double[] getSatelliteViewXY(double lat, double lng) {
        // Rotation for satLat. Plan yz.
        double latitude = Math.toRadians(satLat);
        double longitude = Math.toRadians(-thetaP);

        double _lng = lng - thetaP;

        double _x = Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(_lng));
        double _y = Math.sin(Math.toRadians(lat));
        double _z = Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(_lng));
        /*
         *                      | 1    0       0   |  b > 0 : towards user
         * Rotation plan y, z:  | 0  cos b  -sin b |
         *                      | 0  sin b   cos b |
         *
         *                      | cos t  0 -sin t |  t > 0 : counterclockwise
         * Rotation plan x, z:  |   0    1    0   |
         *                      | sin t  0  cos t |
         *
         */
        double[] rotated = null;
        double newX = _x;
        double newY = _y;
        double newZ = _z;

        // rotation yz (for stellite latitude)
        if (true) {
            rotated = rotation(new double[]{newX, newY, newZ},
                    new double[][]{{1D, 0D, 0D},
                            {0D, Math.cos(latitude), -Math.sin(latitude)},
                            {0D, Math.sin(latitude), Math.cos(latitude)}});
            newX = rotated[0];
            newY = rotated[1];
            newZ = rotated[2];
        }
        // rotation xz (for satellite longitude)
        if (false) {
            rotated = rotation(new double[]{newX, newY, newZ},
                    new double[][]{{Math.cos(longitude), 0D, -Math.sin(longitude)},
                            {0D, 1D, 0D},
                            {Math.sin(longitude), 0D, Math.cos(longitude)}});
            newX = rotated[0];
            newY = rotated[1];
            newZ = rotated[2];
        }

        double newL = Math.toDegrees(Math.asin(newY));
        double newG = 0D;
        if (newZ == 0D)
            newG = 0D;
        else
            newG = Math.toDegrees(Math.atan(newX / newZ));

        if ((newZ < 0D)) // && newX > 0D) || (newZ > 0D && newX < 0D))
        {
            while (_lng < -180D) _lng += 360D;
            while (_lng > 180D) _lng -= 360D;
            if (_lng < 0D) {
                if (newG > 0D) newG -= 180D;
                else newG = -180D - newG;
            }
            if (_lng > 0D) {
                if (newG > 0D) newG = 180D - newG;
                else newG += 180D;
            }
        }
        newG += thetaP;
//    newL = lat;
//    newG = lng;

        // Begin here
        double alfa = (newL);
        double f = Math.cos(Math.toRadians(alfa)) * Math.cos(Math.toRadians(newG - thetaP));
//  double r = rhoE / rhoS; // Unused
        double c = rhoS - (rhoE * f);
        double x = rhoS * rhoE * Math.sin(Math.toRadians(newG - thetaP)) * Math.cos(Math.toRadians(alfa)) / c;
        double y = rhoS * rhoE * Math.sin(Math.toRadians(alfa)) / c;

        return new double[]{x, y};
    }

    public boolean isBehind(double lat, double lng) {
        if (projection == ChartPanelInterface.GLOBE_VIEW)
            return (calculateGlobeViewZCoordinates(Math.toRadians(lat), Math.toRadians(lng)) < 0.0);
        else if (projection == ChartPanelInterface.SATELLITE_VIEW) {
            // Angular distance between the point and the nadir of the satellite must
            // be smaller than the satellite semi eye width
            double dist2Nadir = getDistFromNadir(lat, lng);
            return (dist2Nadir > satelliteHorizonDiameter);
        } else
            return false;
    }

    /**
     * Retourne le demi angle sous lequel le satellite voit la Terre
     *
     * @return in degrees
     */
    private double getSatelliteSemiEyeWidth() {
        double ret = 0D;

        ret = Math.toDegrees(Math.acos(rhoE / rhoS));
        return ret;
    }

    /**
     * Calculate the distance between the nadir of the
     * satellite and the visible horizon
     *
     * @return in nautical miles
     */
    private double getSatelliteCircleRadius() {
        GeoPoint satNadir = new GeoPoint(Math.toRadians(satLat), Math.toRadians(thetaP));
        double pointOnTheCircleLat = satLat + getSatelliteSemiEyeWidth();
        if (pointOnTheCircleLat > 90D || pointOnTheCircleLat < -90D)
            pointOnTheCircleLat = satLat - getSatelliteSemiEyeWidth();
        GeoPoint onTheCircle = new GeoPoint(Math.toRadians(pointOnTheCircleLat), Math.toRadians(thetaP));
        if (gc == null) {
            gc = new GreatCircle();
        }
        gc.setStart(new GreatCirclePoint(satNadir));
        gc.setArrival(new GreatCirclePoint(onTheCircle));
        gc.calculateGreatCircle(1);
        double dist = Math.toDegrees(gc.getDistance()) * 60.0;

        return dist;
    }

    /**
     * Distance from the point of interest and satellite nadir
     *
     * @param lat Latitude of interest
     * @param lng Longitude of interest
     * @return in nautical miles
     */
    private double getDistFromNadir(double lat, double lng) {
        GeoPoint satNadir = new GeoPoint(Math.toRadians(satLat), Math.toRadians(thetaP));
        GeoPoint thePoint = new GeoPoint(Math.toRadians(lat), Math.toRadians(lng));
        if (gc == null)
            gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(satNadir));
        gc.setArrival(new GreatCirclePoint(thePoint));
        gc.calculateGreatCircle(1);
        double dist = Math.toDegrees(gc.getDistance()) * 60.0;

        return dist;
    }

    private void adjustBoundaries() {
        _north = north;
        _south = south;
        _east = east;
        _west = west;
//  if (sign(_north) == sign(_south));
        if (sign(_east) != sign(_west) && sign(_east) == -1)
            _west = _west - 360D;
    }

    private static int sign(double d) {
        int s = 0;
        if (d > 0.0D)
            s = 1;
        if (d < 0.0D)
            s = -1;
        return s;
    }

    private static String getLabel(double v, int orientation) {
        String label = "";
//  DecimalFormat df = new DecimalFormat("##0.00");
        if (Math.abs(v) > 180D)
            if (v < 0.0D)
                v = 360D + v;
            else
                v = 360D - v;

        if (((int) v) == v) // No minutes
        {
            label = Integer.toString((int) Math.abs(v)) + '\272';
            if (v > 0 && Math.abs(v) < 180.0) {
                label += (orientation == ChartPanel.NS ? "N" : "E");
            } else if (v < 0 && Math.abs(v) < 180.0)
                label += (orientation == ChartPanel.NS ? "S" : "W");
        } else {
            label = GeomUtil.decToSex(v, GeomUtil.SWING, (orientation == ChartPanel.NS ? GeomUtil.NS : GeomUtil.EW), true);
        }

//    label = df.format(Math.abs(v));
//    if (v < 0.0D)
//    {
//      if (orientation == 0)
//        label = "S " + label;
//      else
//        label = "W " + label;
//    } else
//    if (orientation == 0)
//      label = "N " + label;
//    else
//      label = "E " + label;
        return label;
    }

    public Point getPanelPoint(GeoPoint gp) {
        return getPanelPoint(gp.getL(), gp.getG());
    }

    /**
     * From Geographical Position to location on the JPanel
     *
     * @param lat in decimal degrees
     * @param lng in decimal degrees
     * @return the position on the JPanel displaying the chart
     */

    public Point getPanelPoint(double lat, double lng) {
        Point pt = null;
        adjustBoundaries();
        if (_north != _south && _east != _west) {
            double gAmpl; // = Math.abs(_east - _west);
            for (gAmpl = _east - _west; gAmpl < 0D; gAmpl += 360D) ;
            double graph2chartRatio = (double) w / gAmpl;
            double _lng = lng;
            if (Math.abs(_west) > 180D && sign(_lng) != sign(_west) && sign(_lng) > 0) {
                _lng -= 360D;
            }
            if (gAmpl > 180D && _lng < 0D && _west > 0D) {
                _lng += 360D;
            }
            if (gAmpl > 180D && _lng >= 0D && _west > 0D && _lng < _east) {
                _lng += (_west + (gAmpl - _east));
            }
            int x = 0;
            double[] xy = null;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                case ChartPanelInterface.MERCATOR:
//        x = (int)(Math.abs(_lng - _west) * graph2chartRatio);
                    x = (int) ((_lng - _west) * graph2chartRatio);
                    break;
                case ChartPanelInterface.LAMBERT:
                    x = (int) Math.round(conic_ratio * calculateLambertXCoordinates(Math.toRadians(lat), Math.toRadians(_lng), Math.toRadians(contactParallel)));
                    x = conicOffset_X + x;
                    break;
                case ChartPanelInterface.CONIC_EQUIDISTANT:
                    double[] ald = calculateCECoordinates(Math.toRadians(lat), Math.toRadians(_lng), Math.toRadians(contactParallel));
                    x = conicOffset_X + (int) Math.round(conic_ratio * ald[0]);
                    break;
                case ChartPanelInterface.GLOBE_VIEW:
                    x = (int) Math.round(globeView_ratio * calculateGlobeViewXCoordinates(Math.toRadians(lat), Math.toRadians(_lng - globeViewLngOffset)));
                    x += globeViewOffset_X;
                    break;
                case ChartPanelInterface.SATELLITE_VIEW:
                    xy = getSatelliteViewXY(lat, lng);
                    x = (int) Math.round(globeView_ratio * xy[0]);
                    x += globeViewOffset_X;
//        x = (int)xy[0];
                    break;
                case ChartPanelInterface.STEREOGRAPHIC:
                    xy = calculateStereoGraphicXYCoordinates(lat, lng);
                    x = (int) Math.round(stereoView_ratio * xy[0]);
                    x += stereoViewOffset_X;
                    break;
                case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                    xy = calculatePolarStereoGraphicXYCoordinates(lat, lng);
                    x = (int) Math.round(stereoView_ratio * xy[0]);
                    x += stereoViewOffset_X;
                    if (south < 0) { // Southern hemisphere
                        x = w - x;
                    }
                    break;
            }
            double incSouth = 0.0D;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    incSouth = _south;
                    break;
                case ChartPanelInterface.MERCATOR:
                    incSouth = MercatorUtil.getIncLat(_south);
                    break;
                case ChartPanelInterface.LAMBERT:
                    incSouth = (int) Math.round(conic_ratio * calculateLambertYCoordinates(Math.toRadians(_south), Math.toRadians(_lng), Math.toRadians(contactParallel)));
                    break;
                case ChartPanelInterface.CONIC_EQUIDISTANT:
                    double[] ald = calculateCECoordinates(Math.toRadians(_south), Math.toRadians(_lng), Math.toRadians(contactParallel));
                    incSouth = (int) Math.round(conic_ratio * ald[1]);
                    break;
                case ChartPanelInterface.GLOBE_VIEW:
                    incSouth = (int) Math.round(globeView_ratio * calculateGlobeViewYCoordinates(Math.toRadians(lat), Math.toRadians(_lng - globeViewLngOffset)));
                    break;
                case ChartPanelInterface.SATELLITE_VIEW:
                    incSouth = (int) Math.round(globeView_ratio * xy[1]);
//        incSouth = xy[1];
                    break;
                case ChartPanelInterface.STEREOGRAPHIC:
                    xy = calculateStereoGraphicXYCoordinates(lat, lng);
                    incSouth = (int) Math.round(stereoView_ratio * xy[1]);
                    break;
                case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                    xy = calculatePolarStereoGraphicXYCoordinates(lat, lng);
                    incSouth = (int) Math.round(stereoView_ratio * xy[1]);
                    break;
            }
            double incLat = 0.0D;
            int y = 0;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    incLat = lat;
                    y = h - (int) ((incLat - incSouth) * ((double) h / (north - south)));
                    break;
                case ChartPanelInterface.MERCATOR:
                    incLat = MercatorUtil.getIncLat(lat);
                    y = h - (int) ((incLat - incSouth) * graph2chartRatio);
                    break;
                case ChartPanelInterface.LAMBERT:
                    incLat = conic_ratio * calculateLambertYCoordinates(Math.toRadians(lat), Math.toRadians(_lng), Math.toRadians(contactParallel));
                    y = (int) Math.round(incLat) + conicOffset_Y;
                    break;
                case ChartPanelInterface.CONIC_EQUIDISTANT:
                    double[] ald = calculateCECoordinates(Math.toRadians(lat), Math.toRadians(_lng), Math.toRadians(contactParallel));
                    y = (int) Math.round(conic_ratio * ald[1]) + conicOffset_Y;
                    break;
                case ChartPanelInterface.GLOBE_VIEW:
                    y = (int) Math.round(globeView_ratio * calculateGlobeViewYCoordinates(Math.toRadians(lat), Math.toRadians(_lng - globeViewLngOffset)));
                    y = globeViewOffset_Y - y;
                    break;
                case ChartPanelInterface.SATELLITE_VIEW:
//        y = h - (int)(globeViewOffset_Y - (int)incSouth);
                    y = (int) (globeViewOffset_Y - (int) incSouth);
                    break;
                case ChartPanelInterface.STEREOGRAPHIC:
                    incLat = lat;
                    y = (int) (stereoViewOffset_Y - (int) incSouth);
                    break;
                case ChartPanelInterface.POLAR_STEREOGRAPHIC:
                    incLat = lat;
                    y = (int) (stereoViewOffset_Y - (int) incSouth);
                    if (south < 0) { // Southern hemisphere
                        y = h + y;
                    }
                    break;
            }
            pt = new Point(x, y);
            if (projection != ChartPanelInterface.GLOBE_VIEW &&
                    projection != ChartPanelInterface.SATELLITE_VIEW) {
                pt = rotate(pt);
            }
        }
        return pt;
    }

    public GeoPoint getGeoPos(int x, int y) {
        GeoPoint gp = null;
        double l = 0.0D;
        double g = 0.0D;
        adjustBoundaries();
        if (_north != _south && _east != _west) {
            double gAmpl; // = Math.abs(_east - _west);
            for (gAmpl = _east - _west; gAmpl < 0D; gAmpl += 360D) ;
            double lAmpl = 0.0D;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    lAmpl = Math.abs(_north - _south);
                    break;
                case ChartPanelInterface.MERCATOR:
                    lAmpl = Math.abs(MercatorUtil.getIncLat(_north) - MercatorUtil.getIncLat(_south));
                    break;
            }
            double graph2chartRatio = (double) w / gAmpl;
            switch (projection) {
                default:
                case ChartPanelInterface.GLOBE_VIEW:
                case ChartPanelInterface.SATELLITE_VIEW:
                case ChartPanelInterface.STEREOGRAPHIC: // TODO Sure?
                    break;
                case ChartPanelInterface.POLAR_STEREOGRAPHIC: // See http://kartoweb.itc.nl/geometrics/Map%20projections/body.htm
                    double lambdaZero = (west + east) / 2;
                    double _x = (x - stereoViewOffset_X) / stereoView_ratio;
                    double _y = (y - stereoViewOffset_Y) / stereoView_ratio;
                    g = lambdaZero + Math.toDegrees(Math.atan(_x / (_y)));
                    if (g < -180D) {
                        g += 360D;
                    }
                    if (g > 180D) {
                        g -= 360;
                    }
                    break;
                case ChartPanelInterface.ANAXIMANDRE:
                case ChartPanelInterface.MERCATOR:
                    g = (double) x / graph2chartRatio + _west;
                    if (g < -180D) {
                        g += 360D;
                    }
                    if (g > 180D) {
                        g -= 360;
                    }
                    break;
                case ChartPanelInterface.LAMBERT:
                    // TODO Fix it (G)
//        System.out.println("Lamber Rotation:" + Math.toDegrees(lambert_rotation));
                    Point rotated = rotateBack(new Point(x, y));
                    int deltaX = rotated.x - conicOffset_X;
                    int deltaY = rotated.y - conicOffset_Y;
                    double angleWithCenter = Math.toDegrees(Math.atan((double) deltaX / (double) deltaY));
                    if (deltaY < 0) {
                        if (deltaX > 0) {
                            angleWithCenter += 180D;
                        } else {
                            angleWithCenter -= 180D;
                        }
                    }
                    if (contactParallel < 0.0D) {
                        angleWithCenter += 180D;
                    }
//        System.out.println("Angle With Center:" + angleWithCenter + " [deltaX:" + deltaX + ", deltaY:" + deltaY + "]");
                    g = angleWithCenter / Math.sin(Math.toRadians(contactParallel));
                    while (g > 180D) {
                        g -= 360D;
                    }
                    while (g < -180D) {
                        g += 360D;
                    }
                    break;
            }
            double incSouth = 0.0D;
            switch (projection) {
                case ChartPanelInterface.ANAXIMANDRE:
                    incSouth = _south;
                    break;
                case ChartPanelInterface.MERCATOR:
                    incSouth = MercatorUtil.getIncLat(_south);
                    break;
            }
            double incLat = (double) (h - y) / graph2chartRatio + incSouth;
            l = 0.0D;
            switch (projection) {
                default:
                    // return null
                    break;
                case ChartPanelInterface.ANAXIMANDRE:
                    incLat = (double) (h - y) / ((double) h / lAmpl) + incSouth;
                    l = incLat;
                    break;
                case ChartPanelInterface.MERCATOR:
                    l = MercatorUtil.getInvIncLat(incLat);
                    break;
                case ChartPanelInterface.LAMBERT:
                    Point rotated = rotateBack(new Point(x, y));
                    int deltaX = rotated.x - conicOffset_X;
                    int deltaY = rotated.y - conicOffset_Y;
                    double dist2center = Math.sqrt(Math.pow(deltaX, 2D) + Math.pow(deltaY, 2D));
                    double qte = (1.0D - Math.cos(Math.toRadians(Math.abs(contactParallel))) * (dist2center / conic_ratio) * Math.sin(Math.toRadians(Math.abs(contactParallel)))) / ((dist2center / conic_ratio) * Math.pow(Math.sin(Math.toRadians(Math.abs(contactParallel))), 2D));
                    if (contactParallel < 0D) {
                        qte *= -1D;
                    }
                    l = Math.toDegrees(Math.atan(qte));
                    break;
                case ChartPanelInterface.POLAR_STEREOGRAPHIC: // TODO See http://kartoweb.itc.nl/geometrics/Map%20projections/body.htm
                    double _x = (x - stereoViewOffset_X) / stereoView_ratio;
                    double _y = (y - stereoViewOffset_Y) / stereoView_ratio;
                    l = 90 - Math.toDegrees(2 * Math.atan(Math.sqrt(_x * _x + _y * _y) / (2 * R)));
                    break;
            }
            gp = new GeoPoint(l, g);
        }
        return gp;
    }

    public boolean contains(GeoPoint gp) {
        boolean ret = false;
        if (gp.getL() > south && gp.getL() < north) {
            if (west < east) {
                if (gp.getG() < east && gp.getG() > west) {
                    ret = true;
                }
//        else
//          System.out.println(gp.getG() + " not between " + west + " and " + east);
            } else if (east < 0.0D) {
                double _east_ = 360D + east;
                double _lng_ = gp.getG();
                if (_lng_ < 0.0D) {
                    _lng_ += 360D;
                }
                if (_lng_ < _east_ && _lng_ > west) {
                    ret = true;
                }
            } else if (gp.getG() > east || gp.getG() < west) { // Not sure...
                ret = true;
            }
        }
        return ret;
    }

    public void postit(Graphics g, String s, int x, int y, Color bgcolor) {
        postit(g, s, x, y, bgcolor, null, null);
    }

    public void postit(Graphics g, String s, int x, int y, Color bgcolor, Color fgcolor, Float transp) {
        int bevel = 2;
        int postitOffset = 5;

        int startX = x;
        int startY = y;

        Color origin = g.getColor();
        g.setColor(postitTextColor);
        Font f = g.getFont();
        int nbCr = 0;
        int crOffset;
        for (crOffset = 0; (crOffset = s.indexOf("\n", crOffset) + 1) > 0; ) {
            nbCr++;
        }

        String txt[] = new String[nbCr + 1];
        int i = 0;
        crOffset = 0;
        for (i = 0; i < nbCr; i++) {
            txt[i] = s.substring(crOffset, (crOffset = s.indexOf("\n", crOffset) + 1) - 1);
        }
        txt[i] = s.substring(crOffset);
        int strWidth = 0;
        for (i = 0; i < nbCr + 1; i++) {
            if (g.getFontMetrics(f).stringWidth(txt[i]) > strWidth) {
                strWidth = g.getFontMetrics(f).stringWidth(txt[i]);
            }
        }
        Color c = g.getColor(); // postitTextColor
        g.setColor(bgcolor);
        if (g instanceof Graphics2D) {
            // Transparency
            Graphics2D g2 = (Graphics2D) g;
            float alpha = (transp != null ? transp.floatValue() : 0.3f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        // left or right, up or down...
        Point topRightExtremity = new Point(x + postitOffset + strWidth + (2 * bevel), (y - f.getSize()) + 1);
        Point bottomRightExtremity = new Point(x + postitOffset + strWidth + (2 * bevel), (nbCr + 1) * f.getSize());
        Point bottomLeftExtremity = new Point(x, (nbCr + 1) * f.getSize());

        if (!this.getVisibleRect().contains(topRightExtremity) && !this.getVisibleRect().contains(bottomRightExtremity)) {
            // This display left
            startX = x - strWidth - (2 * bevel) - (2 * postitOffset);
        }
        if (!this.getVisibleRect().contains(bottomLeftExtremity)) {
            // This display up
//    startY = y - ((nbCr + 1) * f.getSize());
            startY = y - ((nbCr) * f.getSize());
//    System.out.println("Up, y [" + y + "] becomes [" + startY + "]");
        }

        g.fillRect(startX + postitOffset, (startY - f.getSize()) + 1, strWidth + (2 * bevel), (nbCr + 1) * f.getSize());
        if (g instanceof Graphics2D) {
            // Reset Transparency
            Graphics2D g2 = (Graphics2D) g;
            float alpha = 1.0f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        if (fgcolor != null) {
            g.setColor(fgcolor);
        } else {
            g.setColor(c);
        }
        for (i = 0; i < nbCr + 1; i++) {
            g.drawString(txt[i], startX + bevel + postitOffset, startY + (i * f.getSize()));
        }
        g.setColor(origin);
    }

    public void bubble(Graphics g, String s, int x, int y, Color bgcolor, Color fgcolor, Float transp) {
        int bevel = 2;
        int postitOffset = 5;

        final int X_OFFSET = -50;
        final int Y_OFFSET = -50;

        int startX = x + X_OFFSET;
        int startY = y + Y_OFFSET;

        Color origin = g.getColor();
        g.setColor(postitTextColor);
        Font f = g.getFont();
        int nbCr = 0;
        int crOffset;
        for (crOffset = 0; (crOffset = s.indexOf("\n", crOffset) + 1) > 0; ) {
            nbCr++;
        }
        String txt[] = new String[nbCr + 1];
        int i = 0;
        crOffset = 0;
        for (i = 0; i < nbCr; i++) {
            txt[i] = s.substring(crOffset, (crOffset = s.indexOf("\n", crOffset) + 1) - 1);
        }
        txt[i] = s.substring(crOffset);
        int strWidth = 0;
        for (i = 0; i < nbCr + 1; i++) {
            if (g.getFontMetrics(f).stringWidth(txt[i]) > strWidth) {
                strWidth = g.getFontMetrics(f).stringWidth(txt[i]);
            }
        }
        Color c = g.getColor(); // postitTextColor
        g.setColor(bgcolor);
        if (g instanceof Graphics2D) {
            // Transparency
            Graphics2D g2 = (Graphics2D) g;
            float alpha = (transp != null ? transp.floatValue() : 0.3f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        // left or right, up or down...
        Point topRightExtremity = new Point(X_OFFSET + x + postitOffset + strWidth + (2 * bevel),
                Y_OFFSET + (y - f.getSize()) + 1);
        Point bottomRightExtremity = new Point(X_OFFSET + x + postitOffset + strWidth + (2 * bevel),
                Y_OFFSET + (nbCr + 1) * f.getSize());
        Point bottomLeftExtremity = new Point(X_OFFSET + x,
                Y_OFFSET + (nbCr + 1) * f.getSize());

        if (!this.getVisibleRect().contains(topRightExtremity) && !this.getVisibleRect().contains(bottomRightExtremity)) {
            // This display left
            startX = X_OFFSET + x - strWidth - (2 * bevel) - (2 * postitOffset);
        }
        if (!this.getVisibleRect().contains(bottomLeftExtremity)) {
            // This display up
            //    startY = y - ((nbCr + 1) * f.getSize());
            startY = Y_OFFSET + y - ((nbCr) * f.getSize());
            //    System.out.println("Up, y [" + y + "] becomes [" + startY + "]");
        }

        g.fillRect(startX + postitOffset,
                (startY - f.getSize()) + 1,
                strWidth + (2 * bevel),
                (nbCr + 1) * f.getSize());
        // La pointe
        Polygon polygon = new Polygon(new int[]{x,
                bottomLeftExtremity.x + ((bottomRightExtremity.x - bottomLeftExtremity.x) / 2) - 10,
                bottomLeftExtremity.x + ((bottomRightExtremity.x - bottomLeftExtremity.x) / 2) + 10},
                new int[]{y,
                        (((startY - f.getSize()) + 1) + (((nbCr + 1) * f.getSize()) / 2)),
                        (((startY - f.getSize()) + 1) + (((nbCr + 1) * f.getSize()) / 2))},
                3);
        g.fillPolygon(polygon);
        if (g instanceof Graphics2D) {
            // Reset Transparency
            Graphics2D g2 = (Graphics2D) g;
            float alpha = 1.0f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        if (fgcolor != null) {
            g.setColor(fgcolor);
        } else {
            g.setColor(c);
        }
        for (i = 0; i < nbCr + 1; i++) {
            g.drawString(txt[i], startX + bevel + postitOffset, startY + (i * f.getSize()));
        }
        g.setColor(origin);
    }

    public void plotLOP(Graphics g, GeoPoint gp, double azimuth, double intercept, String bodyName) {
        Point pt = getPanelPoint(gp.getL(), gp.getG());
        g.setColor(lopColor);
        g.drawLine(pt.x, pt.y, pt.x, pt.y);
        g.drawOval(pt.x - 2, pt.y - 2, 4, 4);
        Point extr = new Point((int) ((double) pt.x + (double) 50 * Math.sin(Math.toRadians(azimuth))), (int) ((double) pt.y - (double) 50 * Math.cos(Math.toRadians(azimuth))));
        g.drawLine(pt.x, pt.y, extr.x, extr.y);
        postit(g, bodyName, extr.x, extr.y, postitBGColor);
        extr = new Point((int) ((double) pt.x - (double) 50 * Math.sin(Math.toRadians(azimuth))), (int) ((double) pt.y + (double) 50 * Math.cos(Math.toRadians(azimuth))));
        g.drawLine(pt.x, pt.y, extr.x, extr.y);
        GeoPoint x = MercatorUtil.deadReckoning(gp.getL(), gp.getG(), intercept, azimuth);
        g.setColor(lopLineColor);
        pt = getPanelPoint(x.getL(), x.getG());
        g.drawLine(pt.x, pt.y, pt.x, pt.y);
        g.drawOval(pt.x - 2, pt.y - 2, 4, 4);
        extr = new Point((int) ((double) pt.x - (double) 50 * Math.sin(Math.toRadians(azimuth + (double) 90))), (int) ((double) pt.y + (double) 50 * Math.cos(Math.toRadians(azimuth + (double) 90))));
        g.drawLine(pt.x, pt.y, extr.x, extr.y);
        extr = new Point((int) ((double) pt.x - (double) 50 * Math.sin(Math.toRadians(azimuth - (double) 90))), (int) ((double) pt.y + (double) 50 * Math.cos(Math.toRadians(azimuth - (double) 90))));
        g.drawLine(pt.x, pt.y, extr.x, extr.y);
    }

    private void scrollOnMouseEvent(MouseEvent e) {
        int x = this.getVisibleRect().x;
        int y = this.getVisibleRect().y;
        int increment = 1;
        if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0 ||
                (e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            increment = 10;
        }
        if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0) {
            increment *= 2;
        }

        if (mouseEdgeProximity == MOUSE_CLOSE_TO_TOP) {
//    System.out.println("Scrolling Up...");
            this.scrollRectToVisible(new Rectangle(x, y - increment, this.getVisibleRect().width, this.getVisibleRect().height));
        } else if (mouseEdgeProximity == MOUSE_CLOSE_TO_BOTTOM) {
//    System.out.println("Scrolling Down...");
            this.scrollRectToVisible(new Rectangle(x, y + increment, this.getVisibleRect().width, this.getVisibleRect().height));
        } else if (mouseEdgeProximity == MOUSE_CLOSE_TO_LEFT) {
//    System.out.println("Scrolling Left...");
            this.scrollRectToVisible(new Rectangle(x - increment, y, this.getVisibleRect().width, this.getVisibleRect().height));
        } else if (mouseEdgeProximity == MOUSE_CLOSE_TO_RIGHT) {
//    System.out.println("Scrolling Right...");
            this.scrollRectToVisible(new Rectangle(x + increment, y, this.getVisibleRect().width, this.getVisibleRect().height));
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (parent.onEvent(e, ChartPanel.MOUSE_CLICKED)) {
            if (mouseEdgeProximity != MOUSE_AWAY_FROM_EDGES) {
                scrollOnMouseEvent(e);
            }
        }
        e.consume();
//  String mess = "Pos:" + Integer.toString(e.getX()) + ", " + Integer.toString(e.getY());
        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_CLICKED);
    }

    public void mousePressed(MouseEvent e) {
        parent.onEvent(e, ChartPanel.MOUSE_PRESSED);

        if (mouseDraggedType == MOUSE_DRAG_GRAB_SCROLL)
            this.setCursor(closedHandCursor);
        else if (mouseDraggedType == MOUSE_DRAW_LINE_ON_CHART)
            this.setCursor(crossHairCursor);
        if (parent.onEvent(e, ChartPanel.MOUSE_PRESSED)) {
            draggedFromX = e.getX();
            draggedFromY = e.getY();
        }

        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_PRESSED);
    }

    public boolean getDragged() {
        return dragged;
    }

    public void setDragged(boolean b) {
        dragged = b;
    }

    public int getDraggedFromX() {
        return draggedFromX;
    }

    public int getDraggedFromY() {
        return draggedFromY;
    }

    public void setDraggedFromX(int i) {
        draggedFromX = i;
    }

    public void setDraggedFromY(int i) {
        draggedFromY = i;
    }

    /* TODO Change the order for throwing the exception
     * Calculate the difference beetween the geographical position,
     * and get the zoom from it.
     **/
    public void zoomToZone(GeoPoint geoTopLeft,
                           GeoPoint geoBottomRight)
            throws NotOnChartException,
            ZoneTooSmallException {
//  System.out.println("Zooming to zone:" + geoTopLeft.toString() + " and " + geoBottomRight.toString());
        boolean zoneSizeOK = false;
        int nbTest = 0, maxTest = 100;
        Point topLeftOnThePanel = null, bottomRightOnThePanel = null;
        while (!zoneSizeOK && nbTest < maxTest) {
            topLeftOnThePanel = this.getPanelPoint(geoTopLeft.getL(),
                    geoTopLeft.getG());
            bottomRightOnThePanel = this.getPanelPoint(geoBottomRight.getL(),
                    geoBottomRight.getG());
            if (topLeftOnThePanel.x != bottomRightOnThePanel.x &&
                    topLeftOnThePanel.y != bottomRightOnThePanel.y) {
                int deltaX = Math.abs(topLeftOnThePanel.x - bottomRightOnThePanel.x);
                int deltaY = Math.abs(topLeftOnThePanel.y - bottomRightOnThePanel.y);
                if (Math.max(deltaX, deltaY) > 100)
                    zoneSizeOK = true;
            } else {
                bottomRightOnThePanel = new Point(topLeftOnThePanel.x + 10, topLeftOnThePanel.y + 10);
            }
            nbTest++;
            if (!zoneSizeOK) {
//      System.out.println("Zoom-zoom!");
                this.zoomIn();
            }
        }
//  if (zoneSizeOK)
        {
            double ratio = Math.min((double) this.getVisibleRect().width / (double) Math.abs(topLeftOnThePanel.x - bottomRightOnThePanel.x),
                    (double) this.getVisibleRect().height / (double) Math.abs(topLeftOnThePanel.y - bottomRightOnThePanel.y));
//    System.out.println("Ratio:" + ratio);
            if (ratio == 1d)
                return; // TASK Make sure that's right
            double oldZoom = getZoomFactor();
            setZoomFactor(ratio);
            parent.zoomFactorHasChanged(ratio);
            zoomIn();
            setZoomFactor(oldZoom);
//    parent.zoomFactorHasChanged(oldZoom);

            double middleLat = ((geoTopLeft.getL() + geoBottomRight.getL()) / 2.0);
            double middleLong = ((geoTopLeft.getG() + geoBottomRight.getG()) / 2.0);
            if (sign(geoTopLeft.getG()) != sign(geoBottomRight.getG())) {
                double delta = Math.abs(geoTopLeft.getG() + geoBottomRight.getG());
                middleLong = geoTopLeft.getG() + (delta / 2);
                if (middleLong > 180)
                    middleLong -= 360;
            }
            Point middle = getPanelPoint(middleLat, middleLong);
            int _w = getVisibleRect().width;
            int _h = getVisibleRect().height;
            Point topLeft = new Point(middle.x - (_w / 2),
                    middle.y - (_h / 2));

            // Scroll it in position
//    Point topLeft = getPanelPoint(nLat, wLong);
//    System.out.println("Current Chart Panel, w:" + this.getWidth() + ", h:" + this.getHeight());
//    System.out.println("Scrolling to " + topLeft.x + "," + topLeft.y + " and " + this.getVisibleRect().width + ", " + this.getVisibleRect().height);
            this.scrollRectToVisible(new Rectangle(topLeft.x, topLeft.y, this.getVisibleRect().width, this.getVisibleRect().height));
            if (topLeft.x < 0 || topLeft.x > this.getWidth() ||
                    topLeft.y < 0 || topLeft.y > this.getHeight()) {
                throw new NotOnChartException("Requested zoom position not on the choosen chart:" +
                        "\nx:" + Integer.toString(topLeft.x) +
                        ", y:" + Integer.toString(topLeft.y) +
                        "\nfor w:" + Integer.toString(this.getWidth()) +
                        " and h:" + Integer.toString(this.getHeight()));
            }
        }
//  else
//  {
        // Try to zoom here...
//    throw new RuntimeException("ChartPanel.zoomToZone not ready...");
//  }
        if (!zoneSizeOK) {
            throw new ZoneTooSmallException("Zone Too Small Exception:" +
                    "\ntop left:" + geoTopLeft.toString() + ", bottom right:" + geoBottomRight.toString() +
                    "\nfor w:" + Integer.toString(this.getWidth()) + " and h:" + Integer.toString(this.getHeight()));
        }
    }

    public void positionTo(GeoPoint centerPoint) {
        Point middle = getPanelPoint(centerPoint.getL(), centerPoint.getG());
        int _w = getVisibleRect().width;
        int _h = getVisibleRect().height;
        Point topLeft = new Point(middle.x - (_w / 2),
                middle.y - (_h / 2));
        // Scroll it in position
        this.scrollRectToVisible(new Rectangle(topLeft.x, topLeft.y, this.getVisibleRect().width, this.getVisibleRect().height));
    }

    public void mouseReleased(MouseEvent e) {
        if (mouseEdgeProximity == MOUSE_AWAY_FROM_EDGES) {
            this.setCursor(defaultCursor);
        }
        if (parent.onEvent(e, ChartPanel.MOUSE_RELEASED)) { // returning false disables default behavior
            if (dragged && (mouseDraggedType == MOUSE_DRAG_ZOOM ||
                    mouseDraggedType == MOUSE_DRAW_LINE_ON_CHART) &&
                    (draggedFromX - e.getX()) != 0 &&
                    (draggedFromY - e.getY()) != 0) {
                GeoPoint from = getGeoPos(Math.min(draggedFromX, e.getX()), Math.min(draggedFromY, e.getY()));
                GeoPoint to = getGeoPos(Math.max(draggedFromX, e.getX()), Math.max(draggedFromY, e.getY()));
                double nLat = Math.max(from.getL(), to.getL());
                double wLong = Math.min(from.getG(), to.getG());
                double sLat = Math.min(from.getL(), to.getL());
                double eLong = Math.max(from.getG(), to.getG());
                // If lng signs are different
                if (sign(wLong) != sign(eLong)) {
                    if (Math.abs(wLong - eLong) > 180D) {
                        // then swap
                        double d = eLong;
                        eLong = wLong;
                        wLong = d;
                    }
                }
                try {
                    GeoPoint topLeft = new GeoPoint(nLat, wLong);
                    GeoPoint bottomRight = new GeoPoint(sLat, eLong);
                    boolean goZoom = true;
                    if (confirmDDZoom && mouseDraggedType == MOUSE_DRAG_ZOOM) {
                        JComponent jcp = this;
                        if (parent instanceof JComponent)
                            jcp = (JComponent) parent;
                        int resp = JOptionPane.showConfirmDialog(jcp,
                                GnlUtilities.buildMessage("confirm-ddz", new String[]{topLeft.toString(), bottomRight.toString()}),
                                "Drag & Drop zoom",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (resp != JOptionPane.OK_OPTION)
                            goZoom = false;
                    }
                    if (mouseDraggedType == MOUSE_DRAW_LINE_ON_CHART) {
                        from = getGeoPos(draggedFromX, draggedFromY);
                        to = getGeoPos(e.getX(), e.getY());
                        goZoom = false;
                        // Calculate distance between from and to
                        GeoPoint geoFrom = new GeoPoint(Math.toRadians(from.getL()), Math.toRadians(from.getG()));
                        GeoPoint geoTo = new GeoPoint(Math.toRadians(to.getL()), Math.toRadians(to.getG()));
                        if (gc == null)
                            gc = new GreatCircle();
                        gc.setStart(new GreatCirclePoint(geoFrom));
                        gc.setArrival(new GreatCirclePoint(geoTo));
                        gc.calculateGreatCircle(1);
                        double dist = Math.toDegrees(gc.getDistance()) * 60.0;
                        //          String mess = "Distance between " + from.toString() + "\nand " + to.toString() + "\n" +
                        //                        "GC: " + Integer.toString((int)dist) + " nm\n";
                        gc.calculateRhumbLine();
                        //          mess += ("Rhumbline: " + Integer.toString((int)gc.getRhumbLineDistance()) + " nm\n" +
                        //                   "Bearing: "   + Integer.toString((int)Math.toDegrees(gc.getRhumbLineRoute())) + "\272T");
                        if (dp == null)
                            dp = new DistancePanel();
                        dp.setGcDist(dist);
                        dp.setRlDist(gc.calculateRhumbLineDistance());

                        dp.setFrom(from.toString());
                        dp.setTo(to.toString());
                        dp.setGCValue(Integer.toString((int) dist) + " nm");
                        dp.setRhumblineValue(Integer.toString((int) gc.calculateRhumbLineDistance()) + " nm");
                        dp.setBearingValue(Integer.toString((int) Math.toDegrees(gc.calculateRhumbLineRoute())) + "\272T");

                        dp.computeTime();

                        // Show result
                        JOptionPane.showMessageDialog(this,
                                dp,
                                GnlUtilities.buildMessage("dist-bear", null),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    if (goZoom) {
                        zoomToZone(topLeft,     // top left
                                bottomRight);// bottom right
                        parent.chartDDZ(topLeft.getL(), bottomRight.getL(), topLeft.getG(), bottomRight.getG());
                    }
                } catch (ChartPanel.ZoneTooSmallException ztse) {
                    System.err.println(ztse.getMessage());
                    //      ztse.printStackTrace();
                } catch (ChartPanel.NotOnChartException noce) {
                    System.err.println(noce.getMessage());
                    //      noce.printStackTrace();
                }
                this.setCursor(defaultCursor);
            }
            if (dragged && mouseDraggedType == MOUSE_DRAW_ON_CHART) {
                if (handDrawing == null)
                    handDrawing = new ArrayList<PointList<GeoPoint>>(10);
                handDrawing.add(oneDrawing);
                oneDrawing = null;
            }
        }

        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_RELEASED);

        draggedFromX = draggedFromY = -1;
        dragged = false;
        draggingRectangle = null;
        lineToDraw = null;
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
        parent.onEvent(e, ChartPanel.MOUSE_ENTERED);
        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_ENTERED);
    }

    public void mouseExited(MouseEvent e) {
        parent.onEvent(e, ChartPanel.MOUSE_EXITED);
        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_EXITED);
    }

    public void setMouseDraggedEnabled(boolean b) {
        mouseDraggedEnabled = b;
    }

    public boolean getMouseDraggedEnabled() {
        return mouseDraggedEnabled;
    }

    public int getMouseDraggedType() {
        return mouseDraggedType;
    }

    public void setMouseDraggedType(int mdt) {
        mouseDraggedType = mdt;
        if (mouseDraggedType == MOUSE_DRAG_ZOOM)
            defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        else if (mouseDraggedType == MOUSE_DRAW_ON_CHART)
            defaultCursor = drawingCursor;
        else if (mouseDraggedType == MOUSE_DRAW_LINE_ON_CHART)
            defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        else
            defaultCursor = openHandCursor;
        this.setCursor(defaultCursor);
    }

    // TODO Manage the DRAW_ON_CHART feature. Color, thickness, points.
    public void mouseDragged(MouseEvent e) {
//  if (mouseDraggedEnabled && ((e.getModifiers() & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == 0))
        if (mouseDraggedEnabled && mouseDraggedType == MOUSE_DRAG_ZOOM) {
            if (parent.onEvent(e, ChartPanel.MOUSE_DRAGGED)) {
                dragged = true;
                int _x = Math.min(draggedFromX, e.getX());
                int _y = Math.min(draggedFromY, e.getY());
                int _w = Math.abs(draggedFromX - e.getX());
                int _h = Math.abs(draggedFromY - e.getY());
                draggingRectangle = new Rectangle(_x, _y, _w, _h);
                repaint();
            }
        } else if (mouseDraggedEnabled && mouseDraggedType == MOUSE_DRAW_LINE_ON_CHART) {
            this.setCursor(crossHairCursor);
            if (parent.onEvent(e, ChartPanel.MOUSE_DRAGGED)) {
                dragged = true;
                int x1 = draggedFromX;
                int y1 = draggedFromY;
                int x2 = e.getX();
                int y2 = e.getY();
                lineToDraw = new ArrayList<Point>(2);
                lineToDraw.add(new Point(x1, y1));
                lineToDraw.add(new Point(x2, y2));
                repaint();
            }
        }
//  if ((e.getModifiers() & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) // Alt + Click: Grab Scroll
        if (mouseDraggedEnabled && mouseDraggedType == MOUSE_DRAG_GRAB_SCROLL) {
            this.setCursor(closedHandCursor);
            int x = this.getVisibleRect().x - (e.getX() - draggedFromX);
            int y = this.getVisibleRect().y - (e.getY() - draggedFromY);
            this.scrollRectToVisible(new Rectangle(x, y, this.getVisibleRect().width, this.getVisibleRect().height));
//    this.setCursor(defaultCursor);
        }
        // MOUSE_DRAW_ON_CHART
        if (mouseDraggedEnabled && mouseDraggedType == MOUSE_DRAW_ON_CHART) {
            this.setCursor(drawingCursor);
            if (parent.onEvent(e, ChartPanel.MOUSE_DRAGGED)) {
                dragged = true;
                if (oneDrawing == null) {
                    oneDrawing = new PointList<GeoPoint>(10);
                    oneDrawing.setLineColor(drawColor);
                }
                GeoPoint dpt = getGeoPos(e.getX(), e.getY());
                //    System.out.println("Point " + dpt.toString());
                oneDrawing.add(dpt);
                repaint();
            }
        }
        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_DRAGGED);
    }

    public void mouseMoved(MouseEvent e) {
        try {
            String toAdd = null;
            if (parent != null) {
                if (parent.onEvent(e, ChartPanel.MOUSE_MOVED)) {
                    toAdd = parent.getMessForTooltip();
                    if (mouseEdgeProximityDetectionEnabled) {
                        Dimension vrdim = this.getVisibleRect().getSize();
//            System.out.println( "Position:" + e.getX() + ", " + e.getY() + " (on " + this.getWidth() + " x " + this.getHeight() + ")" +
//                               " Vis Rect: x=" + this.getVisibleRect().x + ", y=" + this.getVisibleRect().y +
//                                         " w=" + vrdim.width + ", h=" + vrdim.height);
                        int mouseBefore = mouseEdgeProximity;

                        if (e.getX() <= (this.getVisibleRect().x + EDGE_PROXIMITY) && Math.abs(e.getX()) > EDGE_PROXIMITY) // Close to the left
                        {
                            mouseEdgeProximity = MOUSE_CLOSE_TO_LEFT;
                            this.setCursor(handPointingLeftCursor);
                        } else if (e.getY() <= (this.getVisibleRect().y + EDGE_PROXIMITY) && Math.abs(e.getY()) > EDGE_PROXIMITY) {
                            mouseEdgeProximity = MOUSE_CLOSE_TO_TOP;
                            this.setCursor(handPointingUpCursor);
                        } else if (e.getX() >= (this.getVisibleRect().x + vrdim.width - EDGE_PROXIMITY) && Math.abs(this.getWidth() - e.getX()) > EDGE_PROXIMITY) {
                            mouseEdgeProximity = MOUSE_CLOSE_TO_RIGHT;
                            this.setCursor(handPointingRightCursor);
                        } else if (e.getY() >= (this.getVisibleRect().y + vrdim.height - EDGE_PROXIMITY) && Math.abs(this.getHeight() - e.getY()) > EDGE_PROXIMITY) {
                            mouseEdgeProximity = MOUSE_CLOSE_TO_BOTTOM;
                            this.setCursor(handPointingDownCursor);
                        } else {
                            mouseEdgeProximity = MOUSE_AWAY_FROM_EDGES;
                            if (mouseBefore != MOUSE_AWAY_FROM_EDGES)
                                this.setCursor(defaultCursor);
                        }
//          System.out.println("- MouseEdgeProximity:" + mouseEdgeProximity);
                    }
                }
            }
            e.consume();
            if (enablePositionTooltip &&
                    ((getProjection() != ChartPanelInterface.GLOBE_VIEW &&
                            getProjection() != ChartPanelInterface.SATELLITE_VIEW) || enforceTooltip)) {
                int x = e.getX();
                int y = e.getY();
                GeoPoint gp = getGeoPos(x, y);
                if (gp != null) {
                    String mess = "<html>" + (enforceTooltip ? "" : GeomUtil.decToSex(gp.getL(), GeomUtil.SWING, GeomUtil.NS) + "<br>" +
                            GeomUtil.decToSex(gp.getG(), GeomUtil.SWING, GeomUtil.EW)) +
                            (toAdd == null ? "" : ((enforceTooltip ? "" : "<br>") + toAdd)) +
                            "<html>";
                    setToolTipText(mess);
                }
            } else {
                setToolTipText(null);
            }
        } catch (Exception ex) {
            System.out.println("MouseMoved:" + ex.getMessage());
            ex.printStackTrace();
        }
        if (parent instanceof ChartPanelParentInterface_II)
            ((ChartPanelParentInterface_II) parent).afterEvent(e, ChartPanel.MOUSE_MOVED);
    }

    public void setDefaultCursor() {
        this.setCursor(defaultCursor);
    }

    public double calculateEastG(double nLat, double sLat, double wLong) {
        double deltaIncLat = 0.0D;
        switch (projection) {
            case ChartPanelInterface.ANAXIMANDRE:
            case ChartPanelInterface.GLOBE_VIEW:
                deltaIncLat = nLat - sLat;
                break;
            case ChartPanelInterface.MERCATOR:
                deltaIncLat = MercatorUtil.getIncLat(nLat) - MercatorUtil.getIncLat(sLat);
                break;
        }
        double graphicRatio = (double) w / (double) h;
        double deltaG = deltaIncLat * graphicRatio;
        double eLong = wLong + deltaG;
        if (eLong > 180D)
            eLong -= 360D;
        return eLong;
    }

    public void setWidthFromChart(double nLat, double sLat, double wLong, double eLong) {
        double deltaIncLat = 0.0D;
        switch (projection) {
//      case ChartPanelInterface.LAMBERT:
//        return;
            case ChartPanelInterface.ANAXIMANDRE:
                deltaIncLat = nLat - sLat;
                break;
            case ChartPanelInterface.MERCATOR:
                deltaIncLat = MercatorUtil.getIncLat(nLat) - MercatorUtil.getIncLat(sLat);
                break;
        }
        double deltaG; // = eLong - wLong;
        for (deltaG = eLong - wLong; deltaG < 0D; deltaG += 360D) ;
        double graphicRatio = deltaG / deltaIncLat;

        if (projection != ChartPanelInterface.ANAXIMANDRE &&
                projection != ChartPanelInterface.MERCATOR)
            graphicRatio = 1d;

//  System.out.println("FYI: GraphicRatio:" + graphicRatio);
        graphicRatio = Math.max(graphicRatio, 0.45d); // To limit the excess of Increaling Latitude (over 80 degrees...)

        w = (int) ((double) h * graphicRatio);
        setPreferredSize(new Dimension(w, h));
    }

    public RenderedImage createChartImage(int w, int h) {
        int width = w;
        int height = h;

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        this.paintComponent((Graphics) g2d);
        // Graphics context no longer needed so dispose it
        g2d.dispose();

        return bufferedImage;
    }


    public int[] genImage(File f, String ext) {
        return genImage(f, ext, w, h);
    }

    public int[] genImage(File f, String ext, int width, int height) {
//  int w = this.getWidth();
//  int h = this.getHeight();
        RenderedImage rendImage = createChartImage(width, height);
        // Write generated image to a file
        try {
            OutputStream os = new FileOutputStream(f);
            ImageIO.write(rendImage, ext, os);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//  return new int[] { this.getWidth(), this.getHeight() };
//  return new int[] { w, h };
        return new int[]{width, height};
    }

    /**
     * @param fName with no extension
     * @param ext   extension: jpg, png
     */
    public int[] genImage(String fName, String ext) {
        int[] ret = null;
        // Write generated image to a file
        try {
            String fullFileName = fName + "." + ext;
            File f = new File(fullFileName);
            ret = genImage(f, ext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if (pageIndex > 0)
            return Printable.NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
//  System.out.println("ImageableX:" + pf.getImageableX() + ", ImageableY:" + pf.getImageableY());
        g2d.translate(pf.getImageableX(), pf.getImageableY());
//  g2d.translate(0, 0);

        if (printResize) {
            // Will fit the printable page
            double zf = Math.max((double) w / pf.getImageableWidth(),
                    (double) h / pf.getImageableHeight());
            double z = getZoomFactor();
            setZoomFactor(zf);
            zoomOut();
            setZoomFactor(z); // reset
        }
        paintComponent(g);
        return Printable.PAGE_EXISTS;
    }

    public void printGraphics() {
        printGraphics(-1D, -1D);
    }

    public void printGraphics(double width, double height) {
        PrinterJob pJob = PrinterJob.getPrinterJob();
        PageFormat pf = pJob.defaultPage();

        if (width != -1 && height != -1) {
            Paper paper = new Paper();
//    paper.setSize((width + 144D), (height + 144D));  // Unit is 72nd of inch...
//    paper.setImageableArea(72D, 72D, (width), (height));
            // Inverted W & H. TODO See with a min/max...
            paper.setSize((height + 144D), (width + 144D));  // Unit is 72nd of inch..., 1 pixel generally.
            paper.setImageableArea(72D, 72D, (height), (width));
            pf.setPaper(paper);
        }

//  Paper paper = pf.getPaper();
//  System.out.println("Paper W:" + paper.getWidth());
//  System.out.println("Paper H:" + paper.getHeight());

//  if (printResize)
        {
            if (this.getW() > this.getH())
                pf.setOrientation(PageFormat.LANDSCAPE);
            else
                pf.setOrientation(PageFormat.PORTRAIT);
        }

        pJob.setPrintable(this, pf); // All the trick!
        try {
            if (pJob.printDialog())
                pJob.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWithScale(boolean withScale) {
        this.withScale = withScale;
    }

    public boolean isWithScale() {
        return withScale;
    }

    public void setWithLngLabels(boolean withLngLabels) {
        this.withLngLabels = withLngLabels;
    }

    public boolean isWithLngLabels() {
        return withLngLabels;
    }

    public void setWithInvertedLabels(boolean withInvertedLabels) {
        this.withInvertedLabels = withInvertedLabels;
    }

    public boolean isWithInvertedLabels() {
        return withInvertedLabels;
    }

    public void setVideoIncrement(int videoIncrement) {
        this.videoIncrement = videoIncrement;
    }

    public void setVideoTrackColor(Color videoTrackColor) {
        this.videoTrackColor = videoTrackColor;
    }

    public void setVideoTrackThickness(int videoTrackThickness) {
        this.videoTrackThickness = videoTrackThickness;
    }

    public int getVideoIncrement() {
        return videoIncrement;
    }

    public void setGlobeViewLngOffset(double orthoPerspLngOffset) {
        this.globeViewLngOffset = orthoPerspLngOffset;
    }

    public double getGlobeViewLngOffset() {
        return globeViewLngOffset;
    }

    public void setTransparentGlobe(boolean b) {
        this.transparentGlobe = b;
        if (!b) this.antiTransparentGlobe = true;
    }

    public boolean isTransparentGlobe() {
        return transparentGlobe;
    }

    public void setAntiTransparentGlobe(boolean b) {
        this.antiTransparentGlobe = b;
        if (!b) this.transparentGlobe = true;
    }

    public boolean isAntiTransparentGlobe() {
        return antiTransparentGlobe;
    }

    public void setGlobeViewRightLeftRotation(double satViewRightLeftRotation) {
        this.globeViewRightLeftRotation = satViewRightLeftRotation;
    }

    public double getGlobeViewRightLeftRotation() {
        return globeViewRightLeftRotation;
    }

    public void setGlobeViewForeAftRotation(double satViewForeAftRotation) {
        this.globeViewForeAftRotation = satViewForeAftRotation;
    }

    public double getGlobeViewForeAftRotation() {
        return globeViewForeAftRotation;
    }

    public void setWithGrid(boolean withGrid) {
        this.withGrid = withGrid;
    }

    public boolean isWithGrid() {
        return withGrid;
    }

    public void setPrintResize(boolean pr) {
        printResize = pr;
    }

    public boolean isPrintResize() {
        return printResize;
    }

    public void setDrawColor(Color drawColor) {
        this.drawColor = drawColor;
    }

    public Color getDrawColor() {
        return drawColor;
    }

    public void setDrawThickness(int drawThickness) {
        this.drawThickness = drawThickness;
    }

    public int getDrawThickness() {
        return drawThickness;
    }

    public void setHandDrawing(List<PointList<GeoPoint>> handDrawing) {
        this.handDrawing = handDrawing;
        if (handDrawing == null)
            oneDrawing = null;
    }

    public List<PointList<GeoPoint>> getHandDrawing() {
        return handDrawing;
    }

    public void setOneDrawing(PointList<GeoPoint> oneDrawing) {
        this.oneDrawing = oneDrawing;
    }

    public List<GeoPoint> getOneDrawing() {
        return oneDrawing;
    }

    public void setSatelliteAltitude(double rhoS) // in km
    {
        this.rhoS = rhoS * 100D;
        this.satelliteHorizonDiameter = getSatelliteCircleRadius();
    }

    public double getSatelliteAltitude() // returns in KM
    {
        return rhoS / 100D; // Stored in 100s of km
    }

    public void setSatelliteLongitude(double thetaP) {
        this.thetaP = thetaP;
    }

    public double getSatelliteLongitude() {
        return thetaP;
    }

    public void setSatelliteLatitude(double satLat) {
        this.satLat = satLat;
    }

    public double getSatelliteLatitude() {
        return satLat;
    }

    public void setConfirmDDZoom(boolean confirmDDZoom) {
        this.confirmDDZoom = confirmDDZoom;
    }

    public boolean isConfirmDDZoom() {
        return confirmDDZoom;
    }

    public void setEnforceTooltip(boolean enforceTooltip) {
        this.enforceTooltip = enforceTooltip;
    }

    public boolean isEnforceTooltip() {
        return enforceTooltip;
    }

    public void setMajorLatitudeTick(double majorLatitudeTick) {
        this.majorLatitudeTick = majorLatitudeTick;
    }

    public void setMinorLatitudeTick(double minorLatitudeTick) {
        this.minorLatitudeTick = minorLatitudeTick;
    }

    public void setAltGridColor(Color altgridColor) {
        this.altGridColor = altgridColor;
    }

    public Color getAltGridColor() {
        return altGridColor;
    }

    public int getMouseEdgeProximity() {
        return this.mouseEdgeProximity;
    }

    public void setMouseEdgeProximityDetectionEnabled(boolean mouseEdgeProximityDetectionEnabled) {
        this.mouseEdgeProximityDetectionEnabled = mouseEdgeProximityDetectionEnabled;
    }

    public boolean isMouseEdgeProximityDetectionEnabled() {
        return mouseEdgeProximityDetectionEnabled;
    }

    public void setPlotHandDrawing(boolean plotHandDrawing) {
        this.plotHandDrawing = plotHandDrawing;
    }

    public boolean isPlotHandDrawing() {
        return plotHandDrawing;
    }

    public class NotOnChartException extends Exception {
        public NotOnChartException(String str) {
            super(str);
        }
    }

    public class ZoneTooSmallException extends Exception {
        public ZoneTooSmallException(String str) {
            super(str);
        }
    }

    // For tests
    public static void main(String... args) {
        ChartPanel c = new ChartPanel(null);

        c.setGlobeViewLngOffset(0D);
        c.setGlobeViewForeAftRotation(0D); // beta

        double lat = Math.toRadians(90D);
        double lng = Math.toRadians(0D);
        DecimalFormat df = new DecimalFormat("##0.000");

        c.setGlobeViewRightLeftRotation(90D);    // alfa
        double[] da = c.rotateBothWays(lat, lng);
        System.out.println("For 90 Returned:" + df.format(da[0]) + ", " + df.format(da[1]) + ", " + df.format(da[2]));

        c.setGlobeViewRightLeftRotation(0D);
        da = c.rotateBothWays(lat, lng);
        System.out.println("For 0 Returned :" + df.format(da[0]) + ", " + df.format(da[1]) + ", " + df.format(da[2]));

        c.setGlobeViewRightLeftRotation(45D);
        da = c.rotateBothWays(lat, lng);
        System.out.println("For 45 Returned:" + df.format(da[0]) + ", " + df.format(da[1]) + ", " + df.format(da[2]));

        double d = c.calculateGlobeViewXCoordinates(lat, lng);
        System.out.println("For 45 Returned X:" + df.format(d));
        d = c.calculateGlobeViewYCoordinates(lat, lng);
        System.out.println("For 45 Returned Y:" + df.format(d));
        d = c.calculateGlobeViewZCoordinates(lat, lng);
        System.out.println("For 45 Returned Z:" + df.format(d));
    }

    public static void main3(String... args) {
        ChartPanel cp = new ChartPanel(null);
        double semiAngle = cp.getSatelliteSemiEyeWidth();
        System.out.println("Calculated: " + semiAngle);

        cp.setSatelliteLatitude(10D);
        double[] xy = cp.getSatelliteViewXY(10D, 10D);
    }

    public static void main_(String... args) {
        double[] rotated = null;
        double newX = 1;
        double newY = 1;
        double newZ = 1;

        double angle = Math.toRadians(45D);
        double theta = Math.toRadians(45D);

        // rotation yz (for satellite latitude)
        if (false) {
            rotated = rotation(new double[]{newX, newY, newZ},
                    new double[][]{{1D, 0D, 0D},
                            {0D, Math.cos(angle), -Math.sin(angle)},
                            {0D, Math.sin(angle), Math.cos(angle)}});
            newX = rotated[0];
            newY = rotated[1];
            newZ = rotated[2];
        }
        System.out.println("X:" + newX + ", Y:" + newY + ", newZ:" + newZ);
        // rotation xz (for satellite longitude)
        if (true) {
            rotated = rotation(new double[]{newX, newY, newZ},
                    new double[][]{{Math.cos(theta), 0D, -Math.sin(theta)},
                            {0D, 1D, 0D},
                            {Math.sin(theta), 0D, Math.cos(theta)}});
            newX = rotated[0];
            newY = rotated[1];
            newZ = rotated[2];
        }
        System.out.println("X:" + newX + ", Y:" + newY + ", newZ:" + newZ);
    }

    public static class PointList<T> extends ArrayList<T> {

        private final static long serialVersionUID = 1L;

        private Color lineColor = Color.red;

        public PointList(Collection<T> c) {
            super(c);
        }

        public PointList(int initialCapacity) {
            super(initialCapacity);
        }

        public PointList() {
            super();
        }

        public PointList(Color c) {
            super();
            this.lineColor = c;
        }

        public void setLineColor(Color lineColor) {
            this.lineColor = lineColor;
        }

        public Color getLineColor() {
            return lineColor;
        }
    }
}

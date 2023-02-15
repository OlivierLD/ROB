package weatherwizard.currentuserexits;

import calc.GeoPoint;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelInterface;
import chartview.ctx.WWContext;
import chartview.gui.right.CommandPanel;
import chartview.util.UserExitException;
import chartview.util.UserExitInterface;
import chartview.util.grib.GRIBDataUtil;
import chartview.util.grib.GribHelper;
import coreutilities.Utilities;
import currentdustlet.DustletPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GRIBCurrentDustlet
        implements UserExitInterface {
    private final static int FULL_CHART_DISPLAY = 0;
    private final static int VISIBLE_PART_ONLY_DISPLAY = 1;

    private int displayOption = FULL_CHART_DISPLAY;
    private final static NumberFormat NF = NumberFormat.getInstance(Locale.ENGLISH); // Enforce English for Dustlets


    private List<String> feedback = null;
    private GribHelper.GribConditionData[] wgd = null;
    private ChartPanel chartPanel = null;
    private CommandPanel cp = null;
    private int gribIndex = 0;
    private String dustletBGImage = "";
    private int motes = 50_000;
    private int wsFactor = 50; // Current speed factor
    private long betweenLoops = 2_000L;
    private DustletPanel dustletPanel = null;

    private GeoPoint topLeft = null;
    private GeoPoint bottomRight = null;

    private DustletThread dustletThread = null;

    public GRIBCurrentDustlet() {
        super();
    }

    public boolean isAvailable(CommandPanel commandPanel, WWContext ctx) {
        boolean available = true;
        //  if (ctx.getGribFile() == null)
        //    available = false;
        if (ctx.getCurrentGribFileName() == null || ctx.getCurrentGribFileName().trim().length() == 0) {
            available = false;
        } else {
            if (!commandPanel.isThereCurrent()) {
                available = false;
            }
        }
        return available;
    }

    private void showDustlet(final Thread parent, String gribFile) {
        if (gribFile != null) {
            try {
                wgd = GribHelper.getGribData(gribFile);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
        feedback = new ArrayList<>(1);
        try {
            if (displayOption == VISIBLE_PART_ONLY_DISPLAY) {
                Rectangle visibleRect = chartPanel.getVisibleRect();
                motes = (visibleRect.width * visibleRect.height) / 25;
                double topLeftX = visibleRect.getX();
                double topLeftY = visibleRect.getY();
                topLeft = chartPanel.getGeoPos((int) topLeftX, (int) topLeftY);
                // System.out.println("TopLeft is at " + topLeft.toString());
                bottomRight = chartPanel.getGeoPos((int) topLeftX + visibleRect.width,
                        (int) topLeftY + visibleRect.height);
                // System.out.println("BottomRight is at " + bottomRight.toString());
            } else {
                motes = (chartPanel.getWidth() * chartPanel.getHeight()) / 25; // TODO chartPanbel == null ?
            }
            int[] dim = generateCurrentDustletFile(wsFactor);
            int[] imgdim = generateDustletBackground(); // temp / grib.png
//    System.out.println("Creating DustletPanel...");
            dustletPanel = new DustletPanel(this,
                    dustletBGImage,
                    imgdim[0],
                    imgdim[1],
                    dim[0],
                    dim[1],
                    motes,
                    wsFactor);
            dustletPanel.stop();
            // First date
            dustletPanel.setDustletDate(wgd[gribIndex].getDate());
//    System.out.println("...Done");
            JFrame frame = new JFrame("GRIB Current Dustlet");
            frame.getContentPane().add(dustletPanel);
            frame.setSize(new Dimension(imgdim[0] + 50,
                    imgdim[1] + 50));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
            frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    e.getComponent().setVisible(false);
                    if (dustletPanel != null)
                        dustletPanel.stop();
                    if (dustletThread != null)
                        dustletThread.stopLooping();
//          System.out.println("Notifying...");
                    feedback.add("Success!");
                    synchronized (parent) {
                        parent.notify();
                    }
                }
            });
            frame.setVisible(true);
            dustletPanel.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            feedback.add(ex.toString());
        }
    }

    private final static int FRAME_BEVEL = 50;

    private int[] generateDustletBackground() {
        int[] dim = null;
        {
            try {
                Utilities.makeSureTempExists();
                if (chartPanel != null) {
//        System.out.println("Before - Size:" + chartPanel.getSize().getWidth() + "x" + chartPanel.getSize().getHeight());
                    int nbZoomOut = 0;
                    // Original config, to restore it after
                    int origProj = chartPanel.getProjection();
//        chartPanel.setProjection(ChartPanelInterface.MERCATOR);
                    chartPanel.setProjection(ChartPanelInterface.ANAXIMANDRE);
                    chartPanel.repaint();

                    Dimension origDimension = chartPanel.getSize();
                    int origW = origDimension.width, origH = origDimension.height;
                    if (displayOption == VISIBLE_PART_ONLY_DISPLAY) {
                        Rectangle r = chartPanel.getVisibleRect();
                        origDimension = new Dimension(r.width, r.height);
                    }
//        System.out.println("After (ANAXIMANDRE)- Size:" + origDimension.getWidth() + "x" + origDimension.getHeight());
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    if ((origDimension.getWidth() + FRAME_BEVEL) > screenSize.getWidth() ||
                            (origDimension.getHeight() + FRAME_BEVEL) > screenSize.getHeight()) {
                        JOptionPane.showMessageDialog(cp, "Panel is bigger than the screen,\nRe-adjusting...", "Dustlet", JOptionPane.WARNING_MESSAGE);
                        boolean tooBig = true;
                        while (tooBig) {
                            chartPanel.zoomOut();
                            chartPanel.repaint();
                            nbZoomOut++;
                            origDimension = chartPanel.getSize();
                            if ((origDimension.getWidth() + FRAME_BEVEL) <= screenSize.getWidth() &&
                                    (origDimension.getHeight() + FRAME_BEVEL) <= screenSize.getHeight())
                                tooBig = false;
                        }
                    }
//        System.out.println("After (resize)- Size:" + origDimension.getWidth() + "x" + origDimension.getHeight());
                    boolean chart = cp.isDrawChart();
                    boolean grid = chartPanel.isWithGrid();
                    Color bg = chartPanel.getChartBackGround();
                    Color cc = chartPanel.getChartColor();
                    Color gc = chartPanel.getGridColor();

                    double north = chartPanel.getNorthL();
                    double south = chartPanel.getSouthL();
                    double west = chartPanel.getWestG();
                    double east = chartPanel.getEastG();

                    double newN = wgd[gribIndex].getNLat();
                    double newS = wgd[gribIndex].getSLat();
                    double newW = wgd[gribIndex].getWLng();
                    double newE = wgd[gribIndex].getELng();

                    if (displayOption == VISIBLE_PART_ONLY_DISPLAY) {
                        newN = topLeft.getL();
                        newS = bottomRight.getL();
                        newW = topLeft.getG();
                        newE = bottomRight.getG();
//          System.out.println("Chart boundaries:" + newN + ", " + newS + " and " + newW + ", " + newE);
                    }

                    boolean displayGrib = cp.isDrawGRIB();

                    chartPanel.setNorthL(newN);
                    chartPanel.setSouthL(newS);
                    chartPanel.setWestG(newW);
                    chartPanel.setEastG(newE);

                    if (displayOption == VISIBLE_PART_ONLY_DISPLAY) {
                        chartPanel.setW(origDimension.width);
                        chartPanel.setH(origDimension.height);
                    } else
                        chartPanel.setWidthFromChart(newN, newS, newW, newE);

//        Dimension newDimension = chartPanel.getSize();
//        System.out.println("After (new width)- Size:" + newDimension.getWidth() + "x" + newDimension.getHeight());

                    chartPanel.setChartBackGround(Color.black); // (Color)ParamPanel.data[ParamData.DUSTLET_CHART_BG][1]);
                    chartPanel.setChartColor(Color.white);      // (Color)ParamPanel.data[ParamData.DUSTLET_CHART_LINES][1]);
                    chartPanel.setGridColor(Color.lightGray);   // (Color)ParamPanel.data[ParamData.DUSTLET_CHART_GRID][1]);

                    // Hide faxes.
                    CommandPanel.FaxImage[] faxImage = cp.getFaxImage();
                    boolean[] showFax = new boolean[faxImage == null ? 0 : faxImage.length];
                    for (int i = 0; faxImage != null && i < faxImage.length; i++) {
                        showFax[i] = faxImage[i].show;
                        faxImage[i].show = false;
                    }
                    // Hide Grib
                    cp.setDrawGRIB(false);
                    // Chart, and Grid
                    cp.setDrawChart(true);
                    chartPanel.setWithGrid(true);
                    if (displayOption == VISIBLE_PART_ONLY_DISPLAY)
                        chartPanel.repaint();

                    File tempFile = File.createTempFile("grib", ".png", new File("temp"));
                    dustletBGImage = tempFile.toString();
                    if (displayOption == FULL_CHART_DISPLAY)
                        dim = chartPanel.genImage(tempFile,
                                "png");
                    else
                        dim = chartPanel.genImage(tempFile,
                                "png",
                                origDimension.width,
                                origDimension.height);

//        System.out.println("After (genImage)- Size:" + dim[0] + "x" + dim[1]);

                    //      dim = chartPanel.genImage("temp" + File.separator + "grib", "png");
                    // make it transparent
//          if (false)
//          {
//            Image img =ImageUtil. makeTransparentImage(null, ImageUtil.readImage("temp" + File.separator + "grib.png"), Color.cyan);
//            ImageIO.write(ImageUtil.toBufferedImage(img), "png", new File("temp" + File.separator + "grib.png"));
//          }

                    // reset to original values
                    if (nbZoomOut > 0) {
                        for (int i = 0; i < nbZoomOut; i++)
                            chartPanel.zoomIn();
                    }
                    chartPanel.setProjection(origProj);
                    cp.setDrawChart(chart);
                    chartPanel.setWithGrid(grid);

                    chartPanel.setChartBackGround(bg);
                    chartPanel.setChartColor(cc);
                    chartPanel.setGridColor(gc);
                    for (int i = 0; faxImage != null && i < faxImage.length; i++)
                        faxImage[i].show = showFax[i];
                    chartPanel.setNorthL(north);
                    chartPanel.setSouthL(south);
                    chartPanel.setWestG(west);
                    chartPanel.setEastG(east);

                    chartPanel.setW(origW);
                    chartPanel.setH(origH);
                    chartPanel.setWidthFromChart(north, south, west, east);
                    cp.setDrawGRIB(displayGrib);

                    chartPanel.repaint();
                } else
                    System.out.println("ChartPanel is null...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dim;
    }

    private int[] generateCurrentDustletFile(int value) {
        // New stuff: Generate data file
        GribHelper.GribConditionData gribData = null;
        int[] dim = null;
        if (wgd != null) {
            gribData = wgd[gribIndex];
            if (gribData != null) {
                System.out.println("Generating Dustlet for index " + gribIndex);
                String dustletDataFileName = "temp" + File.separator + "grib.dust";
                try {
                    dim = generateDustletFile(gribData, dustletDataFileName, value);
                    System.out.println("Dimensions of " + dustletDataFileName + "=" + dim[0] + "x" + dim[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dim;
    }

    private int[] generateDustletFile(GribHelper.GribConditionData gribData,
                                            String fileName,
                                            int value) throws Exception {
        int width = 0,
                height = 0;
        if (gribData.getGribPointData() == null) {
            System.out.println("gribData.getGribPointData() is null");
            throw new RuntimeException("gribData.getGribPointData() is null");
        }
        try {
            dustletPanel.setDustletDate(gribData.getDate());
        } catch (Exception ignore) {
            System.out.println(ignore.toString());
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
//  System.out.println("Writing to " + fileName);
        // Xs
        //  for (int h=0; h<gribData.getGribPointData().length; h++)
        for (int h = gribData.getGribPointData().length - 1; h >= 0; h--) {
            String line = "";
            for (int w = 0; w < gribData.getGribPointData()[h].length; w++) {
                if (displayOption == FULL_CHART_DISPLAY ||
                        (displayOption == VISIBLE_PART_ONLY_DISPLAY &&
                                gribData.getGribPointData()[h][w].getLat() <= topLeft.getL() &&
                                gribData.getGribPointData()[h][w].getLat() >= bottomRight.getL()) &&
                                GRIBDataUtil.isBetween(topLeft.getG(), bottomRight.getG(), gribData.getGribPointData()[h][w].getLng())) {
                    float x = gribData.getGribPointData()[h][w].getUOgrd();
                    line += (NF.format(value * x) + " ");
                }
            }
            if (line.trim().length() > 0) {
//      System.out.println("Line[" + line + "]");
                bw.write(line + "\n");
                width = line.trim().split(" ").length;
            }
        }
        // Ys
        //  for (int h=0; h<gribData.getGribPointData().length; h++)
        for (int h = gribData.getGribPointData().length - 1; h >= 0; h--) {
            String line = "";
            for (int w = 0; w < gribData.getGribPointData()[h].length; w++) {
                if (displayOption == FULL_CHART_DISPLAY ||
                        (displayOption == VISIBLE_PART_ONLY_DISPLAY &&
                                gribData.getGribPointData()[h][w].getLat() <= topLeft.getL() &&
                                gribData.getGribPointData()[h][w].getLat() >= bottomRight.getL()) &&
                                GRIBDataUtil.isBetween(topLeft.getG(), bottomRight.getG(), gribData.getGribPointData()[h][w].getLng())) {
                    float y = gribData.getGribPointData()[h][w].getVOgrd();
                    line += (NF.format(value * y) + " ");
                }
            }
            if (line.trim().length() > 0) {
//      System.out.println("Line[" + line + "]");
                bw.write(line + "\n");
                height++; // = line.trim().split(" ").length;
            }
        }
        bw.flush();
        bw.close();

        System.out.println("Generated [" + fileName + "]");
        System.out.println("Grib:" + gribData.getGribPointData()[0].length + " x " + gribData.getGribPointData().length);
        System.out.println("Dust:" + width + " x " + height);
        System.out.println("-----------------------");

        return new int[]{
                width, // gribData.getGribPointData()[0].length, // w
                height // gribData.getGribPointData().length     // h
        };
    }

    public void forward() {
        if (wgd != null) {
            gribIndex += 1;
            if (gribIndex >= wgd.length) {
                gribIndex = 0;
            }
            generateCurrentDustletFile(wsFactor);
        }
    }

    public void backward() {
        if (wgd != null) {
            gribIndex -= 1;
            if (gribIndex < 0) {
                gribIndex = (wgd.length - 1);
            }
            generateCurrentDustletFile(wsFactor);
        }
    }

    public void startAnimation(int speed) {
        dustletThread = new DustletThread("temp" + File.separator + "grib.dust",
                speed * 1_000L,
                wgd,
                wsFactor);
        dustletThread.start();
    }

    public void stopAnimation() {
        if (dustletThread != null) {
            dustletThread.stopLooping();
        }
        dustletThread = null;
    }

    public void setWindScale(int i) {
        this.wsFactor = i;
    }

    public void setBetweenLoops(int i) {
        betweenLoops = 1000L * i;
    }

    public boolean userExitTask(CommandPanel commandPanel, WWContext ctx)
            throws UserExitException {
        wgd = commandPanel.getGribData();
        cp = commandPanel;
        chartPanel = cp.getChartPanel();

        // Visible part, or full chart?
        ChoicePanel chp = new ChoicePanel();
        JOptionPane.showMessageDialog(cp, chp, "What to Display", JOptionPane.QUESTION_MESSAGE);
        if (chp.isFullChartSelected()) {
            System.out.println("Display Full Chart");
            displayOption = FULL_CHART_DISPLAY;
        } else {
            System.out.println("Display Visible part only");
            displayOption = VISIBLE_PART_ONLY_DISPLAY;
        }

        boolean ok = true;
        final Thread me = Thread.currentThread();
        Thread worker = new Thread() {
            public void run() {
                showDustlet(me, null);
            }
        };
        worker.start();
        synchronized (me) {
            try {
                me.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return ok;
    }

    public List<String> getFeedback() {
        if (dustletPanel != null)
            dustletPanel.stop();
        return feedback;
    }

    class DustletThread extends Thread {
        boolean keepLooping = true;
        String dustletFileName = "";
        GribHelper.GribConditionData[] wgd = null;
        int value = 1;

        public DustletThread(String fName,
                             long loop,
                             GribHelper.GribConditionData[] wgd,
                             int value) {
            super();
            dustletFileName = fName;
            betweenLoops = loop;
            this.wgd = wgd;
            this.value = value;
        }

        public void stopLooping() {
            keepLooping = false;
        }

        public void run() {
            while (keepLooping) {
                for (int i = 0; i < wgd.length && keepLooping; i++) {
                    GribHelper.GribConditionData gribData = null;
                    gribData = wgd[i];
                    if (gribData != null) {
                        try {
                            generateDustletFile(gribData, dustletFileName, value);
                            Thread.sleep(betweenLoops);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // For standalone tests
//  public static void main(String... args) throws Exception
//  {
//    GRIBDustlet gb = new GRIBDustlet();
//    String gribFileName = "GRIB_2009_02_25_Sample.grb";
//    URL gribURL = GRIBDustlet.class.getResource(gribFileName);
//    System.out.println("GRIB:" + gribURL.getFile());
//    gb.showDustlet(Thread.currentThread(), gribURL.getFile());
//  }  
}

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CommandPanel.java

package examples.northamerica;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel extends JPanel
        implements ChartPanelParentInterface {
    public void zoomFactorHasChanged(double d) {
    }

    public void chartDDZ(double top, double bottom, double left, double right) {
    }

    class VideoThread extends Thread {
        public void run() {
            videoElement = 0;
            while (videoOn) {
                try {
                    Thread.sleep(750L);
                } catch (Exception exception) {
                }
                chartPanel.repaint();
            }
            System.out.println("VideoThread Done.");
        }

        public void setParent(CommandPanel cp) {
            parent = cp;
        }

        CommandPanel parent;

        VideoThread() {
            parent = null;
        }
    }


    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        videoOn = false;
        videoElement = 0;
        gcElement = 0;
        from = null;
        to = null;
        travelButton = new JButton();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> jButton1_actionPerformed(e));
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> jButton2_actionPerformed(e));
        travelButton.setText("Travel");
        travelButton.addActionListener(e -> travelButton_actionPerformed(e));
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        bottomPanel.add(travelButton, null);
        add(bottomPanel, BorderLayout.SOUTH);
        double nLat = 60D;
        double sLat = -5D;
        double wLong = -160D;
        double eLong = -40D;
        chartPanel.setWidthFromChart(nLat, sLat, wLong, eLong);
        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(20D);
        chartPanel.setGridColor(Color.gray);
        chartPanel.setChartColor(Color.black);
        chartPanel.setMouseDraggedEnabled(false);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    public void chartPanelPaintComponent(Graphics gr) {
        gr.setColor(chartPanel.getChartColor());
        long before = System.currentTimeMillis();
        World.drawChart(chartPanel, gr);
        long after = System.currentTimeMillis();
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D) {
            g2d = (Graphics2D) gr;
            Stroke stroke = new BasicStroke(3F, 0, 2);
            g2d.setStroke(stroke);
        }
        double ls = GeomUtil.sexToDec("37", "56");
        double gs = -GeomUtil.sexToDec("123", "4");
        double lf = GeomUtil.sexToDec("25", "00");
        double gf = -GeomUtil.sexToDec("117", "00");
        double distance = 0.0D;
        int seg = 0;
        Point gp = chartPanel.getPanelPoint(ls, gs);
        chartPanel.postit(gr, "San Francisco\nSeptember", gp.x, gp.y, Color.yellow);
        if (g2d != null) {
            Color c = g2d.getColor();
            g2d.setColor(Color.red);
            g2d.drawOval(gp.x - 10, gp.y - 10, 20, 20);
            g2d.setColor(c);
        }
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, null, distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("10", "17");
        gf = -GeomUtil.sexToDec("109", "13");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Clipperton", distance);
        ls = lf;
        gs = gf;
        lf = -GeomUtil.sexToDec("1", "00");
        gf = -GeomUtil.sexToDec("91", "00");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Galapagos", distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("8", "40");
        gf = -GeomUtil.sexToDec("79", "36");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Panama\nDecember", distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("15", "00");
        gf = -GeomUtil.sexToDec("70", "00");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, null, distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("12", "05");
        gf = -GeomUtil.sexToDec("61", "52");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Petites Antilles\nFeb-March", distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("16", "37");
        gf = -GeomUtil.sexToDec("61", "43");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, null, distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("22", "22");
        gf = -GeomUtil.sexToDec("74", "00");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Bahamas", distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("32", "15");
        gf = -GeomUtil.sexToDec("64", "45");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Bermuda", distance);
        ls = lf;
        gs = gf;
        lf = GeomUtil.sexToDec("43", "21");
        gf = -GeomUtil.sexToDec("69", "45");
        distance = drawSegment(gr, seg++, ls, gs, lf, gf, "Maine\nMay-June", distance);
        if (videoOn) {
            videoElement++;
            if (videoElement > 20 * seg) {
              videoOn = false;
            }
        }
    }

    private double drawSegment(Graphics gr, int segNum, double ls, double gs, double lf, double gf, String postit, double distance) {
        double ret = distance;
        Graphics2D g2d = null;
        if (gr instanceof Graphics2D) {
          g2d = (Graphics2D) gr;
        }
        if (!videoOn || videoOn && videoElement > 20 * segNum) {
            if (postit != null) {
                Point gp = chartPanel.getPanelPoint(lf, gf);
                if (g2d != null) {
                    Color c = g2d.getColor();
                    g2d.setColor(Color.red);
                    g2d.drawOval(gp.x - 10, gp.y - 10, 20, 20);
                    g2d.setColor(c);
                }
                chartPanel.postit(gr, postit, gp.x, gp.y, Color.yellow);
            }
            if (!videoOn || videoOn && videoElement >= 20 * (segNum + 1)) {
              ret += plotGreatCircle(gr, ls, gs, lf, gf, false);
            } else if (videoOn) {
              if (gcElement >= 20) {
                gcElement = 0;
              } else {
                plotGreatCircle(gr, ls, gs, lf, gf, false, gcElement++);
              }
            }
        }
        return ret;
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf, double gf) {
        drawRhumbLine(g, ls, gs, lf, gf, null, 0x80000000);
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf, double gf, Date date) {
        drawRhumbLine(g, ls, gs, lf, gf, date, 0x80000000);
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf, double gf, int ptNum) {
        drawRhumbLine(g, ls, gs, lf, gf, null, ptNum);
    }

    private void drawRhumbLine(Graphics g, double ls, double gs, double lf, double gf, Date date, int ptNum) {
        Point start = chartPanel.getPanelPoint(ls, gs);
        Point finish = chartPanel.getPanelPoint(lf, gf);
        if (start != null && finish != null) {
            boolean borderCross = false;
            if (Math.abs(finish.x - start.x) > chartPanel.getWidth() / 2) {
                borderCross = true;
                if (start.x > finish.x) {
                    finish.x += chartPanel.getWidth();
                    g.drawLine(start.x, start.y, finish.x, finish.y);
                    finish.x -= chartPanel.getWidth();
                    start.x -= chartPanel.getWidth();
                } else if (finish.x > start.x) {
                    start.x += chartPanel.getWidth();
                    g.drawLine(start.x, start.y, finish.x, finish.y);
                    start.x -= chartPanel.getWidth();
                    finish.x -= chartPanel.getWidth();
                }
            }
            g.drawLine(start.x, start.y, finish.x, finish.y);
        }
    }

    private double plotGreatCircle(Graphics g, double ls, double gs, double lf, double gf) {
        return plotGreatCircle(g, ls, gs, lf, gf, true);
    }

    private double plotGreatCircle(Graphics g, double ls, double gs, double lf, double gf, boolean plot) {
        return plotGreatCircle(g, ls, gs, lf, gf, plot, 0x80000000);
    }

    private double plotGreatCircle(Graphics g, double ls, double gs, double lf, double gf, boolean plot, int maxPlot) {
        double distance = 0.0D;
        g.setColor(Color.blue);
        GreatCircle gc = new GreatCircle();
        gc.setStart(new GreatCirclePoint(ls, gs));
        gc.setArrival(new GreatCirclePoint(lf, gf));
        gc.calculateGreatCircle(20);
        distance = gc.getDistance();
        Vector route = gc.getRoute();
        Enumeration enumeration = route.elements();
        GreatCircleWayPoint previous = null;
        for (int currPlot = 0; enumeration.hasMoreElements() && (maxPlot == 0x80000000 || maxPlot > 0x80000000 && currPlot < maxPlot); ) {
            GreatCircleWayPoint gcwp = (GreatCircleWayPoint) enumeration.nextElement();
            Point b = chartPanel.getPanelPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG()));
            if (plot) {
              g.drawOval(b.x - 2, b.y - 2, 4, 4);
            }
            currPlot++;
            if (previous != null) {
                Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
                g.drawLine(a.x, a.y, b.x, b.y);
            }
            previous = gcwp;
        }

        return distance;
    }

    public boolean onEvent(EventObject e, int type) {
        if (type == 0) {
            MouseEvent me = (MouseEvent) e;
            int x = me.getX();
            int y = me.getY();
            if (to != null) {
              from = to = null;
            }
            GeoPoint gp = chartPanel.getGeoPos(x, y);
            if (from == null) {
                from = gp;
            } else {
                to = gp;
                GreatCircle gc = new GreatCircle();
                gc.setStart(new GreatCirclePoint(from));
                gc.setArrival(new GreatCirclePoint(to));
                gc.calculateGreatCircle(20);
                double gcDist = gc.getDistance();
                // gc.calculateRhumbLine();
                double rlDist = gc.calculateRhumbLineDistance();
                double rlZ = gc.calculateRhumbLineRoute();
                DecimalFormat df = new DecimalFormat("##0.00");
                String mess = "GC:" + df.format(Math.toDegrees(gcDist * 60D)) + "'\n" + "RL:" + df.format(rlDist) + "'\n" + " Z:" + df.format(Math.toDegrees(rlZ)) + " true";
                JOptionPane.showMessageDialog(this, mess, "Route", 1);
            }
        }
        return true;
    }

    private void travelButton_actionPerformed(ActionEvent e) {
        videoOn = !videoOn;
        if (videoOn) {
            System.out.println("Video Started");
            VideoThread videoThread = new VideoThread();
            videoThread.setName("VideoThread");
            videoThread.setParent(this);
            videoThread.start();
        } else {
            System.out.println("Video Stopped");
        }
    }

    public String getMessForTooltip() {
        return null;
    }

    public boolean replaceMessForTooltip() {
        return false;
    }

    public void videoCompleted() {
    }

    public void videoFrameCompleted(Graphics g, Point p) {
    }

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private static final int NB_PT_IN_GREAT_CIRCLE = 20;
    boolean videoOn;
    int videoElement;
    int gcElement;
    GeoPoint from;
    GeoPoint to;
    private JButton travelButton;
}

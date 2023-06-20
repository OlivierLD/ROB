package examples.atlantic;

import calc.*;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel extends JPanel
  implements ChartPanelParentInterface
{

  public CommandPanel()
  {
    borderLayout1 = new BorderLayout();
    jScrollPane1 = new JScrollPane();
    chartPanel = new ChartPanel(this);
    bottomPanel = new JPanel();
    zoomInButton = new JButton();
    zoomOutButton = new JButton();
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    setLayout(borderLayout1);
    this.setBackground(new Color(174, 220, 211));
    zoomInButton.setText("Zoom In");
    zoomInButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }

    });
    zoomOutButton.setText("Zoom Out");
    zoomOutButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }

    });
    jScrollPane1.getViewport().add(chartPanel, null);
    add(jScrollPane1, "Center");
    bottomPanel.add(zoomInButton, null);
    bottomPanel.add(zoomOutButton, null);
    add(bottomPanel, "South");
    double nLat = 67D;
    double sLat = -60D;
    double wLong = -100D;
    double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);
    chartPanel.setEastG(eLong);
    chartPanel.setWestG(wLong);
    chartPanel.setNorthL(nLat);
    chartPanel.setSouthL(sLat);
    chartPanel.setHorizontalGridInterval(10D);
    chartPanel.setVerticalGridInterval(10D);
  }

  private void jButton1_actionPerformed(ActionEvent e)
  {
    chartPanel.zoomIn();
  }

  private void jButton2_actionPerformed(ActionEvent e)
  {
    chartPanel.zoomOut();
  }

  public void chartPanelPaintComponent(Graphics gr)
  {
    World.drawChart(chartPanel, gr);
    double ls = GeomUtil.sexToDec("49", "40");
    double gs = -GeomUtil.sexToDec("6", "34");
    double lf = GeomUtil.sexToDec("40", "30");
    double gf = -GeomUtil.sexToDec("69", "15");
    Point gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "English Channel", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "New York", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    GreatCircle gc = plotGreatCircle(gr, ls, gs, lf, gf);
    double dist = Math.toDegrees(gc.getDistance()) * 60D;
    final GreatCircle.RLData rlData = gc.calculateRhumbLine();
    double rl = gc.calculateRhumbLineDistance();
    System.out.println("From English Channel to New York, great circle:" + Math.round(dist) + " nm");
    System.out.println("                                     rhumbline:" + Math.round(rl) + " nm");
    ls = GeomUtil.sexToDec("49", "40");
    gs = -GeomUtil.sexToDec("6", "34");
    lf = GeomUtil.sexToDec("14", "15");
    gf = -GeomUtil.sexToDec("61", "12");
    gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "English Channel", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "West Indies", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    plotGreatCircle(gr, ls, gs, lf, gf);
    ls = GeomUtil.sexToDec("49", "40");
    gs = -GeomUtil.sexToDec("6", "34");
    lf = GeomUtil.sexToDec("40", "0");
    gf = GeomUtil.sexToDec("9", "0");
    gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "English Channel", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "Corsica", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    plotGreatCircle(gr, ls, gs, lf, gf);
    ls = GeomUtil.sexToDec("49", "40");
    gs = -GeomUtil.sexToDec("6", "34");
    lf = -GeomUtil.sexToDec("56", "0");
    gf = -GeomUtil.sexToDec("67", "30");
    gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "English Channel", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "Horn", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    plotGreatCircle(gr, ls, gs, lf, gf);
    ls = GeomUtil.sexToDec("40", "0");
    gs = GeomUtil.sexToDec("9", "0");
    lf = -GeomUtil.sexToDec("56", "0");
    gf = -GeomUtil.sexToDec("67", "30");
    gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "Corsica", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "Horn", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    plotGreatCircle(gr, ls, gs, lf, gf);
    ls = GeomUtil.sexToDec("40", "30");
    gs = -GeomUtil.sexToDec("69", "15");
    lf = -GeomUtil.sexToDec("34", "22");
    gf = GeomUtil.sexToDec("18", "23");
    gp = chartPanel.getPanelPoint(ls, gs);
    chartPanel.postit(gr, "New York", gp.x, gp.y, Color.yellow);
    gp = chartPanel.getPanelPoint(lf, gf);
    chartPanel.postit(gr, "Good Hope", gp.x, gp.y, Color.yellow);
    drawRhumbLine(gr, ls, gs, lf, gf);
    plotGreatCircle(gr, ls, gs, lf, gf);
  }

  private void drawRhumbLine(Graphics g, double ls, double gs, double lf, 
      double gf)
  {
    g.setColor(Color.red);
    Point start = chartPanel.getPanelPoint(ls, gs);
    Point finish = chartPanel.getPanelPoint(lf, gf);
    if(start != null && finish != null)
      g.drawLine(start.x, start.y, finish.x, finish.y);
  }

  private GreatCircle plotGreatCircle(Graphics g, double ls, double gs, double lf, 
      double gf)
  {
    g.setColor(Color.blue);
    GreatCircle gc = new GreatCircle();
    gc.setStart(new GreatCirclePoint(Math.toRadians(ls), Math.toRadians(gs)));
    gc.setArrival(new GreatCirclePoint(Math.toRadians(lf), Math.toRadians(gf)));
    gc.calculateGreatCircle(20);
    Vector route = gc.getRoute();
    Enumeration enumeration = route.elements();
    GreatCircleWayPoint gcwp;
    for(GreatCircleWayPoint previous = null; enumeration.hasMoreElements(); previous = gcwp)
    {
      gcwp = (GreatCircleWayPoint)enumeration.nextElement();
      Point b = chartPanel.getPanelPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG()));
      g.drawOval(b.x - 2, b.y - 2, 4, 4);
      if(previous != null)
      {
        Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
        g.drawLine(a.x, a.y, b.x, b.y);
      }
    }

    return gc;
  }

  public boolean onEvent(EventObject eventobject, int i)
  {
    return true;
  }

  public String getMessForTooltip()
  {
    return null;
  }

  public boolean replaceMessForTooltip()
  {
    return false;
  }

  public void videoCompleted() {}
  public void videoFrameCompleted(Graphics g, Point p) {}

  private BorderLayout borderLayout1;
  private JScrollPane jScrollPane1;
  private ChartPanel chartPanel;
  private JPanel bottomPanel;
  private JButton zoomInButton;
  private JButton zoomOutButton;


  public void zoomFactorHasChanged(double d)
  {
  }

  public void chartDDZ(double top, double bottom, double left, double right)
  {
  }
}

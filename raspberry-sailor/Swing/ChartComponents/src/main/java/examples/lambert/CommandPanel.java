package examples.lambert;

import calc.GeoPoint;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import calc.GreatCircleWayPoint;
import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

public class CommandPanel 
     extends JPanel
  implements ChartPanelParentInterface
{
  private BorderLayout borderLayout1;
  private JScrollPane jScrollPane1;
  private ChartPanel chartPanel;
  private JPanel bottomPanel;
  private JButton zoomInButton;
  private JButton zoomOutButton;

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
    add(jScrollPane1, BorderLayout.CENTER);
    bottomPanel.add(zoomInButton, null);
    bottomPanel.add(zoomOutButton, null);
    add(bottomPanel, BorderLayout.SOUTH);
    
    double nLat = 0D, sLat = 0D, wLong = 0D, eLong = 0D, cp = 0D;
    
    boolean one = false;
    boolean two = false;
    boolean three = false;
    boolean four = false;
    boolean five = false;
    boolean six = false;
    boolean seven = false;
    boolean eight = true;
    
    if (one) // Good, but upside down
    {
      nLat  =  +10D;
      sLat  =  -89D;
      wLong =   90D; // 110D
      eLong = -150D; // -160D
      cp = -45D;
    }    
    if (two) // Pb - Longitude all wrong.
    {
      nLat  =    0D;
      sLat  =  -60D;
      wLong =   80D;
      eLong =  180D;
      cp = -25D;
      
      System.out.println("Middle axis:" + ((wLong + eLong) / 2));
    }
    if (three) // Good but upside down
    {
      nLat  =    0D;
      sLat  =  -60D;
      wLong =   80D;
      eLong = -170D;
      cp = -25D;
    }
    if (four) // Bad, Longitude screwed
    {
      nLat  =    0D;
      sLat  =  -50D;
      wLong =   80D;
      eLong =  170D;
      cp = -25D;
    }
    if (five) // Good
    {
      nLat  =     0D;
      sLat  =   -60D;
      wLong =  -170D;
      eLong =     0D;
      cp = -25D;
    }
    if (six) // Good
    {
      nLat  =     0D;
      sLat  =   -60D;
      wLong =   170D;
      eLong =   -20D;
      cp = -25D;
    }
    if (seven) // Good
    {
      nLat  =  +60D;
      sLat  =  -10D;
      wLong = -150D;
      eLong =    0D;
      cp = 20D;
    }
    if (eight) // Good
    {
      nLat  =   60D;
      sLat  =   10D;
      wLong =  160D;
      eLong =  -90D;
      cp = 60D;
    }
//  double eLong = chartPanel.calculateEastG(nLat, sLat, wLong);

    chartPanel.setProjection(ChartPanel.LAMBERT);
    chartPanel.setContactParallel(cp);

    chartPanel.setEastG(eLong);
    chartPanel.setWestG(wLong);
    chartPanel.setNorthL(nLat);
    chartPanel.setSouthL(sLat);
    chartPanel.setHorizontalGridInterval(10D);
    chartPanel.setVerticalGridInterval(10D);
    chartPanel.setWithScale(false);
    chartPanel.setMouseDraggedEnabled(true);
    chartPanel.setMouseDraggedType(ChartPanel.MOUSE_DRAG_ZOOM);
    chartPanel.setPositionToolTipEnabled(true);
    
  }

  private void jButton1_actionPerformed(ActionEvent e)
  {
    chartPanel.zoomIn();
  }

  private void jButton2_actionPerformed(ActionEvent e)
  {
    chartPanel.zoomOut();
  }

  GeoPoint from = null;
  GeoPoint to   = null;
  
  public void chartPanelPaintComponent(Graphics gr)
  {
    Graphics2D g2d = null;
    if (gr instanceof Graphics2D)
      g2d = (Graphics2D)gr;
    World.drawChart(chartPanel, gr);
  }

  private void drawRhumbLine(Graphics2D g, double ls, double gs, double lf, 
      double gf)
  {
    g.setColor(Color.red);
    g.setStroke(new BasicStroke(1.0F));
    Point start = chartPanel.getPanelPoint(ls, gs);
    Point finish = chartPanel.getPanelPoint(lf, gf);
    if(start != null && finish != null)
      g.drawLine(start.x, start.y, finish.x, finish.y);
  }

  private void drawRhumbLine(Graphics g, 
                             double ls, 
                             double gs, 
                             double lf, 
                             double gf)
  {
    g.setColor(Color.red);
    Point start = chartPanel.getPanelPoint(ls, gs);
    Point finish = chartPanel.getPanelPoint(lf, gf);
    if(start != null && finish != null)
      g.drawLine(start.x, start.y, finish.x, finish.y);
  }

  private void plotGreatCircle(Graphics g, 
                               double ls, 
                               double gs, 
                               double lf, 
                               double gf)
  {
    plotGreatCircle(g, ls, gs, lf, gf, false);
  }
  private void plotGreatCircle(Graphics g, 
                               double ls, 
                               double gs, 
                               double lf, 
                               double gf, 
                               boolean print)
  {
    g.setColor(Color.blue);
    GreatCircle gc = new GreatCircle();
    gc.setStart(new GreatCirclePoint(Math.toRadians(ls), Math.toRadians(gs)));
    gc.setArrival(new GreatCirclePoint(Math.toRadians(lf), Math.toRadians(gf)));
    gc.calculateGreatCircle(20);
    if (print)
      System.out.println("Distance:" + Math.toDegrees(gc.getDistance()) * 60 + " nm");
    Vector route = gc.getRoute();
    Enumeration enumeration = route.elements();
    GreatCircleWayPoint gcwp;
    for(GreatCircleWayPoint previous = null; enumeration.hasMoreElements(); previous = gcwp)
    {
      gcwp = (GreatCircleWayPoint)enumeration.nextElement();
      Point b = chartPanel.getPanelPoint(Math.toDegrees(gcwp.getPoint().getL()), Math.toDegrees(gcwp.getPoint().getG()));
//    g.drawOval(b.x - 2, b.y - 2, 4, 4);
      if (previous != null)
      {
        Point a = chartPanel.getPanelPoint(Math.toDegrees(previous.getPoint().getL()), Math.toDegrees(previous.getPoint().getG()));
        g.drawLine(a.x, a.y, b.x, b.y);
      }
    }
  }

  public boolean onEvent(EventObject e, int type)
  {
    if (type == ChartPanel.MOUSE_CLICKED)
    {
      if (from == null)
        from = chartPanel.getGeoPos(((MouseEvent)e).getX(), ((MouseEvent)e).getY()); 
      else
        to = chartPanel.getGeoPos(((MouseEvent)e).getX(), ((MouseEvent)e).getY()); 
      chartPanel.repaint();
    }
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
  public void zoomFactorHasChanged(double d) {}

  public void chartDDZ(double top, double bottom, double left, double right)
  {
  }
}

package chartview.gui.toolbar.controlpanels.station;

import chartview.ctx.WWContext;

import chartview.util.WWGnlUtilities;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.GradientPaint;
import java.awt.Graphics;

import java.awt.Graphics2D;

import java.awt.Point;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.geom.Arc2D;

import javax.swing.JPanel;


public class WindVanePanel
  extends JPanel
{
  private int windDir = 0;
  
  public WindVanePanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout( null );
    this.setSize(new Dimension(224, 224));
  }
  
  public void paintComponent(Graphics g)
  {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);      
    Point center = new Point(this.getWidth() / 2, this.getWidth() / 2);
    Graphics2D g2d = (Graphics2D)g;
    // Background
//  g2d.setColor(Color.black); 
    if (false)
    {
      Color startColor = Color.black; // new Color(255, 255, 255);
      Color endColor   = Color.gray; // new Color(102, 102, 102);
      GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      (g2d).setPaint(gradient);
  //  g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
      g2d.fillOval(0, 0, this.getWidth(), this.getHeight());
    }
    if (true)
    {
      int rad = Math.min(this.getWidth(), this.getHeight()) / 2;
      drawGlossyCircularDisplay((Graphics2D)g, center, rad, Color.lightGray, Color.black, 1f);
    }    
    // Starboard, Port
    float alpha = 0.3f;
    ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Stroke origStroke = ((Graphics2D)g).getStroke();
    int edgeWidth = 10;
    Stroke stroke =  new BasicStroke(edgeWidth, 
                                     BasicStroke.CAP_BUTT,
                                     BasicStroke.JOIN_BEVEL);
    ((Graphics2D)g).setStroke(stroke);  
    g.setColor(Color.green);
    // The origin of the angles is on the right (East). They turn counter-clockwise.
    double radius = (Math.min(this.getWidth(),this.getHeight()) - edgeWidth - 2) / 2d;
    Shape starBoardSide = new Arc2D.Float((float)((this.getWidth() / 2) - radius),  // x
                                          (float)((this.getHeight() / 2) - radius), // y
                                          (float)(2 * radius),                      // w
                                          (float)(2 * radius),                      // h
                                          -80f,                                     // start
                                          160f,                                     // extent
                                          Arc2D.OPEN);
    ((Graphics2D) g).draw(starBoardSide);
    g.setColor(Color.red);
    Shape portSide      = new Arc2D.Float((float)((this.getWidth() / 2) - radius),
                                          (float)((this.getHeight() / 2) - radius),
                                          (float)(2 * radius), 
                                          (float)(2 * radius), 
                                          100f, 
                                          160f,
                                          Arc2D.OPEN);
    ((Graphics2D) g).draw(portSide);
    
    ((Graphics2D)g).setStroke(origStroke);  
    ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    // Boat
    g2d.setColor(Color.white);
    int boatLength = this.getHeight() - 30;
    WWGnlUtilities.drawBoat(g2d, 
                           Color.white, 
                           center, 
                           boatLength, 
                           0,
                           1.0f);
    WWGnlUtilities.drawTWAOverBoat(g2d, 
                                   (this.getWidth() / 2) - 5, 
                                   center,
                                   windDir);
  }

  public void setWindDir(int windDir)
  {
    this.windDir = windDir;
  }

  public int getWindDir()
  {
    return windDir;
  }
  private static void drawGlossyCircularDisplay(Graphics2D g2d, Point center, int radius, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);
    g2d.fillOval(center.x - radius, center.y - radius, 2 * radius, 2 * radius);

    Point gradientOrigin = new Point(center.x - radius,
                                     center.y - radius);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (2 * radius / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    g2d.fillOval((int)(center.x - (radius * 0.90)), 
                 (int)(center.y - (radius * 0.95)), 
                 (int)(2 * radius * 0.9), 
                 (int)(2 * radius * 0.95));
  }
}

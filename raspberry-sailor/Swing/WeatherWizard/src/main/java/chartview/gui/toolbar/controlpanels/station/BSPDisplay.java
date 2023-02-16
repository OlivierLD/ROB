package chartview.gui.toolbar.controlpanels.station;

import chartview.util.WWGnlUtilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class BSPDisplay
  extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel bspLabel = new JLabel();
  private float bsp = 0F;  

  public BSPDisplay()
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
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(103, 42));
    this.setBackground(Color.black);
    bspLabel.setText("00.00");
    bspLabel.setForeground(Color.green);
    bspLabel.setFont(new Font("Courier New", Font.BOLD, 18)); // For default, at design time
    Font digiFont = null;
    try { digiFont = WWGnlUtilities.tryToLoadFont("ds-digi.ttf", this); }
    catch (Exception ex) { System.err.println(ex.getMessage()); }
    if (digiFont == null)
      digiFont = new Font("Courier New", Font.BOLD, 18);
    else
      digiFont = digiFont.deriveFont(Font.BOLD, 18);
    bspLabel.setFont(digiFont);
    this.add(bspLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                    GridBagConstraints.CENTER, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 0), 0, 0));
  }

  public void setBsp(float bsp)
  {
    this.bsp = bsp;
    bspLabel.setText(WWGnlUtilities.XXX12.format(bsp));
  }

  public float getBsp()
  {
    return bsp;
  }
  
  public void paintComponent(Graphics gr)
  {
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);      
    if (false)
    {
      Color startColor = Color.black; // new Color(255, 255, 255);
      Color endColor   = Color.gray; // new Color(102, 102, 102);
      GradientPaint gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      ((Graphics2D)gr).setPaint(gradient);    
      int w = this.getWidth();
      int h = this.getHeight();
      gr.fillRect(0, 0, w, h);
    }
    if (true)
    {
      drawGlossyRectangularDisplay((Graphics2D)gr, 
                                   new Point(0, 0), 
                                   new Point(this.getWidth(), this.getHeight()), 
                                   Color.gray, 
                                   Color.black, 
                                   1f);
    }
  }

  private static void drawGlossyRectangularDisplay(Graphics2D g2d, Point topLeft, Point bottomRight, Color lightColor, Color darkColor, float transparency)
  {
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2d.setPaint(null);

    g2d.setColor(darkColor);

    int width  = bottomRight.x - topLeft.x;
    int height = bottomRight.y - topLeft.y;

    g2d.fillRoundRect(topLeft.x , topLeft.y, width, height, 10, 10);

    Point gradientOrigin = new Point(0, //topLeft.x + (width) / 2,
                                     0);
    GradientPaint gradient = new GradientPaint(gradientOrigin.x, 
                                               gradientOrigin.y, 
                                               lightColor, 
                                               gradientOrigin.x, 
                                               gradientOrigin.y + (height / 3), 
                                               darkColor); // vertical, light on top
    g2d.setPaint(gradient);
    int offset = (int)(width * 0.025);
    int arcRadius = 5;
    g2d.fillRoundRect(topLeft.x + offset, topLeft.y + offset, (width - (2 * offset)), (height - (2 * offset)), 2 * arcRadius, 2 * arcRadius); 
  }
}

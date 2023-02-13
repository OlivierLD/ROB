package chartview.gui.util.panels;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;

import chartview.gui.toolbar.controlpanels.ControlPane;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import java.awt.RenderingHints;

import java.text.DecimalFormat;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GRIBVisualPanel extends JPanel // TransparentPanel 
{
  private float truewindspeed   = 0f;
  private int   truewinddir     = 0;
  private float prmslValue      = 0f;
  private float hgt500Value     = 0f;
  private float waveHeightValue = 0f;
  private float tempValue       = 0f;
  private float prateValue      = 0f;              
  
  public GRIBVisualPanel()
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

  private void jbInit() throws Exception
  {
    this.setLayout(null);
    this.setOpaque(true);
    this.setSize(new Dimension(400, 250));
    this.setPreferredSize(new Dimension(ControlPane.WIDTH, 250));

    WWContext.getInstance().addApplicationListener(new ApplicationEventListener()
     {
        public String toString()
        {
          return "from GribVisualPanel.";
        }
        public void setGRIBWindValue(int twd, float tws)
        {
          truewindspeed = tws;
          truewinddir = twd;
          repaint();
        }

        public void setGRIBPRMSLValue(float prmsl)
        {
          prmslValue = prmsl;
          repaint();
        }

        public void setGRIB500HGTValue(float hgt500)
        {
          hgt500Value = hgt500;
          repaint();
        }

        public void setGRIBWaveHeight(float wh)
        {
          waveHeightValue = wh;
          repaint();
        }

        public void setGRIBTemp(float t)
        {
          tempValue = t;
          repaint();
        }

        public void setGRIBprate(float prate)
        {
          prateValue = prate;
          repaint();
        }

        public void setGRIBData(int twd, float tws, float prmsl, float hgt500, float wh, float t, float prate)
        {
          truewindspeed = tws;
          truewinddir = twd;
          prmslValue = prmsl;
          hgt500Value = hgt500;
          waveHeightValue = wh;
          tempValue = t;
          prateValue = prate;
          repaint();
        }
      });
  }

  private final static int BORDER_TICKNESS = 5;
  private final static int COLOR_OPACITY   = 200; // 0-255
  private final static int FONT_SIZE       = 8;
  
  private static int tempUnit = Integer.parseInt(((ParamPanel.TemperatureUnitList)(ParamPanel.data[ParamData.TEMPERATURE_UNIT][ParamData.VALUE_INDEX])).getStringIndex());

  private final static DecimalFormat[] FMTS = { 
                                                new DecimalFormat("##0.0 'kts'"), 
                                                new DecimalFormat("###0 'mb'"), // Rounded 
                                                new DecimalFormat("##0 'm'"), 
                                                new DecimalFormat("##0.0 'm'"), 
                                                new DecimalFormat("##0'" + ParamPanel.TemperatureUnitList.getLabel(tempUnit) + "'"), 
                                                new DecimalFormat("##0.0 'mm/h'"),
                                              };
  private final static DecimalFormat DIR_FMT = new DecimalFormat("##0'\272'");
  private final static Color GRIB_DATA_TEXT_COLOR = Color.green; // Color.blue;
  
  private final static int TWS         = 0;
  private final static int PRMSL       = 1;
  private final static int HGT500      = 2;
  private final static int WAVES       = 3;
  private final static int TEMPERATURE = 4;
  private final static int PRATE       = 5;
  
  public void paintComponent(Graphics g)
  {
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);    
    int xOffset = BORDER_TICKNESS, 
        yOffset = BORDER_TICKNESS;
    int w = this.getWidth() - (2 * BORDER_TICKNESS);
    int h = (this.getHeight() - (2 * BORDER_TICKNESS)) / 2;
//  super.paintComponent(g);
    
    GradientPaint gradient = new GradientPaint(0, this.getHeight(), Color.darkGray, 0, 0, Color.black); // vertical, upside down
    ((Graphics2D)g).setPaint(gradient);
//  g.setColor(Color.darkGray);
    g.fillRect(0, 0, this.getWidth(), this.getHeight());
    
    Font origFont = g.getFont();
    Font smallFont = new Font(origFont.getName(), Font.BOLD /*origFont.getStyle()*/, FONT_SIZE);
    g.setFont(smallFont);

    for (int i=0; i<6; i++)
    {
      int displayValue = 0;
      float currValue = 0;
      String displayString = "";
      Color startColor = null; // bottom (low)
      Color endColor   = null; // top    (high)
      String label = "";
      switch (i)
      {
        case TWS: // TWS
          label = "TWS";
          currValue = truewindspeed;
          displayValue = (int) (currValue * ((float)h / 70f)); // [0, 70]
          startColor = new Color(193, 216, 217, COLOR_OPACITY);
          endColor   = new Color(  0,   0, 128, COLOR_OPACITY);
          break;
        case PRMSL: // PRMSL
          label = "PRMSL";
          currValue = prmslValue / 100f;
//        System.out.println("PRMSL:" + prmslValue);
          displayValue = (int) ((currValue - 950f) * ((float)h / 100f)); // [950, 1050]
          startColor = new Color(255,   0,   0, COLOR_OPACITY);
          endColor   = new Color(255, 128, 192, COLOR_OPACITY);
          break;
        case HGT500: // HGT500
          label = "HGT500";
          currValue = hgt500Value;
          displayValue = (int) ((currValue - 4500f) * ((float)h / 1500f)); // [4500, 6000]
//        System.out.println("HGT500:" + hgt500Value);
          endColor   = new Color(  0, 255, 255, COLOR_OPACITY);
          startColor = new Color(  0,   0, 255, COLOR_OPACITY);
          break;
        case WAVES: // WAVES
          label = "WAVES";
//        System.out.println("Waves Height:" + waveHeightValue);
          currValue = waveHeightValue / 100f;
          displayValue = (int) (currValue * ((float)h / 10f)); // [0, 10]
          startColor = new Color(128, 255, 128, COLOR_OPACITY);
          endColor   = new Color(  0, 128,   0, COLOR_OPACITY);
          break;
        case TEMPERATURE: // TEMP
          label = "TEMP";
          startColor = new Color(128, 255, 255, COLOR_OPACITY);
          endColor   = new Color(255,   0,   0, COLOR_OPACITY);
          currValue = (float)WWGnlUtilities.convertTemperatureFromKelvin(tempValue, ParamPanel.TemperatureUnitList.CELCIUS); // tempUnit);
//        System.out.println("TEMP:" + tempValue);
          displayValue = (int) ((currValue + 50f) * ((float)h / 100f)); // [-50, 50]
          break;
        case PRATE: // PRATE
//        System.out.println("PRATE:" + prateValue);
          label = "PRATE";
          currValue = prateValue * 3600f;
          displayValue = (int) (currValue * ((float)h / 20f)); // [0, 20]
          startColor = new Color(255, 255, 255, COLOR_OPACITY);
          endColor   = new Color(  0,   0,   0, COLOR_OPACITY);
          break;
        default:
          break;
      }
      if (displayValue == 0F)
        continue;
      
      // Label
      g.setColor(Color.lightGray);
      int labelXCenterOffset = xOffset + (i * (w / 6)) + (w / 12);
      int labelLength = g.getFontMetrics().stringWidth(label);
      int labelY = g.getFont().getSize() + 1;
      g.drawString(label, labelXCenterOffset - (labelLength / 2) + 1, labelY + 1);
      g.setColor(Color.white);
      g.drawString(label, labelXCenterOffset - (labelLength / 2), labelY);
      
   /* GradientPaint */ gradient = new GradientPaint(0, this.getHeight(), startColor, 0, 0, endColor); // vertical, upside down
      ((Graphics2D)g).setPaint(gradient);
      g.fillRect(xOffset + (i * (w / 6)), h - yOffset - displayValue, w / 6, displayValue);
      g.setColor(startColor);      
      g.drawRect(xOffset + (i * (w / 6)), h - yOffset - displayValue, w / 6, displayValue);
      g.setColor(GRIB_DATA_TEXT_COLOR);
      if (i == TEMPERATURE)
        displayString = FMTS[i].format(WWGnlUtilities.convertTemperatureFromCelcius(currValue, tempUnit));
      else
        displayString = FMTS[i].format(currValue);
      int l = g.getFontMetrics(smallFont).stringWidth(displayString);
      int x = xOffset + (i * (w / 6)) + (w / (2 * 6)) - (l / 2);
      int y = h - yOffset - 5;
      g.drawString(displayString, x, y);
      if (i == TWS)
      {
        displayString = "F " + Integer.toString(WWGnlUtilities.getBeaufort(truewindspeed));
        l = g.getFontMetrics(smallFont).stringWidth(displayString);
        x = xOffset + (i * (w / 6)) + (w / (2 * 6)) - (l / 2);
        y = h - yOffset - 15;
        g.drawString(displayString, x, y);
      }
    }

    // Wind dir - Display
    int displayCenterX = this.getWidth() / 4;
    int displayCenterY = 3 * this.getHeight() / 4;
    int displayRadius = Math.min(this.getWidth() / 4, this.getHeight() / 4) - BORDER_TICKNESS;
    int displayBackGroundOpacity = 125;
    if (true)
    {
      g.setColor(Color.lightGray);
      g.fillOval(displayCenterX - displayRadius,
                 displayCenterY - displayRadius,
                 2 * displayRadius,
                 2 * displayRadius);
      
      WWGnlUtilities.drawGlossyCircularDisplay((Graphics2D)g, 
                                               new Point(displayCenterX, displayCenterY), 
                                               (displayRadius - 4), 
                                               Color.lightGray, 
                                               Color.black, // Color.darkGray, 
                                               1f);
    }
    else
    {
      // Display shadow
      g.setColor(new Color(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue(), displayBackGroundOpacity));
      g.fillOval(displayCenterX - displayRadius + 3,
                 displayCenterY - displayRadius + 3,
                 2 * displayRadius,
                 2 * displayRadius);
      // Display
      g.setColor(new Color(0, 0, 0, displayBackGroundOpacity));
      g.fillOval(displayCenterX - displayRadius,
                 displayCenterY - displayRadius,
                 2 * displayRadius,
                 2 * displayRadius);
      g.setColor(new Color(255, 255, 255, displayBackGroundOpacity));
      g.fillOval(displayCenterX - (displayRadius - 4),
                 displayCenterY - (displayRadius - 4),
                 2 * (displayRadius - 4),
                 2 * (displayRadius - 4));
    }
    // Rose
    int externalCircleRadius = (int)((double)displayRadius * 0.95);
    int internalCircleRadius = (int)((double)displayRadius * 0.25);
    
//  g.setColor(new Color(0, 0, 0, displayBackGroundOpacity));
    g.setColor(Color.white);
    
    for (int i = 0; i < 360; i += 10) // One tick every 10 degrees
    {
      int x1 = displayCenterX + (int) ((externalCircleRadius - 10) * Math.cos(Math.toRadians(i)));
      int y1 = displayCenterY + (int) ((externalCircleRadius - 10) * Math.sin(Math.toRadians(i)));
      int x2 = displayCenterX + (int) ((externalCircleRadius) * Math.cos(Math.toRadians(i)));
      int y2 = displayCenterY + (int) ((externalCircleRadius) * Math.sin(Math.toRadians(i)));
      g.drawLine(x1, y1, x2, y2);
    }

    for (int d=0; false && d<360; d+=90) // Kind of a rose. Sux.
    {
      Point one   = new Point(displayCenterX, 
                              displayCenterY);
      Point two   = new Point((int)(displayCenterX + (Math.sin(Math.toRadians(d - 45)) * internalCircleRadius)), 
                              (int)(displayCenterY + (Math.cos(Math.toRadians(d - 45)) * internalCircleRadius)));
      Point three   = new Point((int)(displayCenterX + (Math.sin(Math.toRadians(d)) * externalCircleRadius)), 
                                (int)(displayCenterY  + (Math.cos(Math.toRadians(d)) * externalCircleRadius)));
      g.fillPolygon(new Polygon(new int[] {one.x, two.x, three.x},
                                new int[] {one.y, two.y, three.y},
                                3));      
    }
    int fontSize = h / 10;
    Font cardFont = new Font(origFont.getName(), Font.BOLD /* origFont.getStyle()*/, fontSize);
    g.setFont(cardFont);
    
    if (false) // NSEW
    {
      g.setColor(Color.white);
      String card = "N";
      int l = g.getFontMetrics(cardFont).stringWidth(card);
      int x = displayCenterX - (l / 2);
      int y = displayCenterY - externalCircleRadius + (fontSize / 2);
      g.drawString(card, x, y);
      g.setColor(Color.darkGray);
      g.drawString(card, x + 2, y + 2);
      g.setColor(Color.white);
      card = "S";
      l = g.getFontMetrics(cardFont).stringWidth(card);
      x = displayCenterX - (l / 2);
      y = displayCenterY + externalCircleRadius + (fontSize / 2);
      g.drawString(card, x, y);
      g.setColor(Color.darkGray);
      g.drawString(card, x + 2, y + 2);
      g.setColor(Color.white);
      card = "W";
      l = g.getFontMetrics(cardFont).stringWidth(card);
      x = displayCenterX - externalCircleRadius - (l / 2);
      y = displayCenterY + (fontSize / 2);
      g.drawString(card, x, y);
      g.setColor(Color.darkGray);
      g.drawString(card, x + 2, y + 2);
      g.setColor(Color.white);
      card = "E";
      l = g.getFontMetrics(cardFont).stringWidth(card);
      x = displayCenterX + externalCircleRadius - (l / 2);
      y = displayCenterY + (fontSize / 2);
      g.drawString(card, x, y);
      g.setColor(Color.darkGray);
      g.drawString(card, x + 2, y + 2);
    }
//  g.setColor(Color.white);
    g.setColor(Color.green);
    String twd = "TWD";
    int labelLength = g.getFontMetrics().stringWidth(twd);
    int labelY = g.getFont().getSize() + 1;
//  g.drawString(twd, displayCenterX - (labelLength / 2) + 2, displayCenterY - (externalCircleRadius / 2) + 2);
//  g.setColor(Color.darkGray);
    g.drawString(twd, displayCenterX - (labelLength / 2), displayCenterY - (externalCircleRadius / 2));
    
    // Actual direction (value)
//  g.setColor(Color.white);
    g.setColor(Color.green);
    twd = Integer.toString(truewinddir) + "\272";
    labelLength = g.getFontMetrics().stringWidth(twd);
    labelY = g.getFont().getSize() + 1;
//  g.drawString(twd, displayCenterX - (labelLength / 2) + 2, displayCenterY + (externalCircleRadius / 2) + 2);
//  g.setColor(Color.darkGray);
    g.drawString(twd, displayCenterX - (labelLength / 2), displayCenterY + (externalCircleRadius / 2));
    
    // Wind dir - Hand
    int handEndX = displayCenterX + (int)((displayRadius - 8) * Math.sin(Math.toRadians(truewinddir)));
    int handEndY = displayCenterY - (int)((displayRadius - 8) * Math.cos(Math.toRadians(truewinddir)));;
    internalCircleRadius = (int)((double)displayRadius * 0.10);
    Point pOne = new Point((int)(displayCenterX + (Math.sin(Math.toRadians(truewinddir - 70)) * internalCircleRadius)), 
                           (int)(displayCenterY - (Math.cos(Math.toRadians(truewinddir - 70)) * internalCircleRadius)));
    Point pTwo = new Point((int)(displayCenterX + (Math.sin(Math.toRadians(truewinddir + 70)) * internalCircleRadius)), 
                           (int)(displayCenterY - (Math.cos(Math.toRadians(truewinddir + 70)) * internalCircleRadius)));    
    // Hand Shadow
    if (false)
    {
      g.setColor(Color.gray);
//    g.drawLine(displayCenterX + 3, displayCenterY + 3, handEndX + 3, handEndY + 3);    
      g.fillPolygon(new Polygon(new int[] { pOne.x + 3, handEndX + 3, pTwo.x + 3, displayCenterX + 3 },
                                new int[] { pOne.y + 3, handEndY + 3, pTwo.y + 3, displayCenterY + 3 },
                                4));
    }
    // Hand
    Polygon hand = new Polygon(new int[] { pOne.x, handEndX, pTwo.x, displayCenterX },
                              new int[] { pOne.y, handEndY, pTwo.y, displayCenterY },
                              4);
    g.setColor(Color.cyan);
    g.drawPolygon(hand);
    g.setColor(new Color(Color.blue.getRed(), Color.blue.getGreen(), Color.blue.getBlue(), COLOR_OPACITY));
    ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
//  g.drawLine(displayCenterX, displayCenterY, handEndX, handEndY);    
    g.fillPolygon(hand);
    // Center
    g.setColor(new Color(0, 0, 0, displayBackGroundOpacity));
    g.fillOval(displayCenterX - 4,
               displayCenterY - 4,
               2 * 4,
               2 * 4);
    ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    
    // Text data
    Font dataFont = new Font("Courier", Font.BOLD /* origFont.getStyle() */, 10);
    g.setFont(dataFont);
    g.setColor(GRIB_DATA_TEXT_COLOR);

    int startX = (w / 2) + BORDER_TICKNESS;
    int startY = (this.getHeight() / 2) + FONT_SIZE;
    g.drawString("TWS    " + FMTS[0].format(truewindspeed), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("TWD    " + DIR_FMT.format(truewinddir), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("PRMSL  " + FMTS[1].format(prmslValue / 100f), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("500HGT " + FMTS[2].format(hgt500Value), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("WAVES  " + FMTS[3].format(waveHeightValue / 100f), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("TEMP   " + FMTS[4].format(WWGnlUtilities.convertTemperatureFromKelvin(tempValue, tempUnit)), startX, startY);
    startY += (FONT_SIZE * 1.2);
    g.drawString("PRATE  " + FMTS[5].format(prateValue * 3600f), startX, startY);
    startY += (FONT_SIZE * 1.2);

    g.setFont(g.getFont().deriveFont(24f));
    startY += (g.getFont().getSize() * 1.2);
    g.setColor(Color.cyan);
    g.drawString("Twd " + DIR_FMT.format(truewinddir), startX, startY);
    
    g.setFont(origFont);
  }
  
  public static void main1(String[] args)
  {
    float f = 12.3654654f;
    for (int i=0; i<FMTS.length; i++)
      System.out.println(FMTS[i].format(f));
  }

}

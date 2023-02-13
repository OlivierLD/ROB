package chartview.util;

import calc.GeoPoint;

import chart.components.ui.ChartPanel;

import chartview.ctx.WWContext;

import chartview.gui.right.CommandPanel;
import chartview.gui.util.tree.JTreeFilePanel;

import java.awt.Color;
import java.awt.Image;

import java.awt.Point;

import java.io.File;

import javax.swing.JOptionPane;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;

import org.w3c.dom.NodeList;

public class DynFaxUtil
{
  public static PreDefFax findPredefFax(String urlStr)
  {
    PreDefFax pdf = null;
    DOMParser parser = WWContext.getInstance().getParser();
    synchronized (parser)
    {
      try
      {
        parser.setValidationMode(XMLParser.NONVALIDATING);
        parser.parse(new File(JTreeFilePanel.PDF_URL).toURI().toURL());
        XMLDocument doc = parser.getDocument();
        String xpath = "//dyn-fax[./@origin = '" + urlStr + "']";
        NodeList nl = doc.selectNodes(xpath);
        XMLElement xe = (XMLElement)nl.item(0);
        Color c = null;
        try 
        { 
          String str = xe.getAttribute("color");
          c = WWGnlUtilities.buildColor(str);
        } 
        catch (Exception ex) { }
        pdf = new PreDefFax(xe.getAttribute("title"),
                            xe.getAttribute("origin"),
                            Double.parseDouble(xe.getAttribute("rotation")),
                            Double.parseDouble(xe.getAttribute("top")),
                            Double.parseDouble(xe.getAttribute("bottom")),
                            Double.parseDouble(xe.getAttribute("left")),
                            Double.parseDouble(xe.getAttribute("right")),
                            "true".equals(xe.getAttribute("transparent")),
                            "true".equals(xe.getAttribute("change-color")),
                            c);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return pdf;
  }
  
  public static CommandPanel.FaxImage getFaxImage(PreDefFax pdf, Image img, String fileSystemLocation, ChartPanel chartPanel)
  {
    CommandPanel.FaxImage fi = new CommandPanel.FaxImage();
    Point topLeft     = chartPanel.getPanelPoint(pdf.getTop(), pdf.getLeft());
    Point bottomRight = chartPanel.getPanelPoint(pdf.getBottom(), pdf.getRight());
    int imgWidth  = img.getWidth(null);
    int imgHeight = img.getHeight(null);
    
    if (pdf.getRotation() != 0)
    {
      int left = topLeft.x;
      int right = bottomRight.x;
      int top = topLeft.y;
      int bottom = bottomRight.y;
      
      int xFaxCenter = left + ((right - left) / 2);
      int yFaxCenter = top + ((bottom - top) / 2);
      // Rotation centree sur le centre du fax.
      Point center = new Point(xFaxCenter, yFaxCenter);
      Point rotatedOne = WWGnlUtilities.rotate(new Point(right, 
                                                         top), 
                                               center,
                                               pdf.getRotation());            
      Point rotatedTwo = WWGnlUtilities.rotate(new Point(left, 
                                                         bottom), 
                                               center,
                                               pdf.getRotation());
      
      top    = rotatedOne.y; // top
      left   = rotatedOne.x; // left
      bottom = rotatedTwo.y; // bottom
      right  = rotatedTwo.x; // right
//    System.out.println("Top:" + top + ", bottom:" + bottom + ", left:" + left + ", right:" + right);
      topLeft = new Point(left, top);
      bottomRight = new Point(right, bottom);
    }    
    double ratio = ((double)(bottomRight.x - topLeft.x)) / (double)imgWidth;
    if (ratio < 0)
    {
      JOptionPane.showMessageDialog(null, 
                                    "Chart boundaries not suitable for this fax...", // LOCALIZE 
                                    "Pre-defined fax", 
                                    JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    // Offsets
    int pixelXOffset = topLeft.x;
    int pixelYOffset = topLeft.y;    
    
    fi.faxImage = img;
    fi.fileName = fileSystemLocation;
    fi.faxTitle = pdf.getTitle();
    fi.imageScale = ratio;
    fi.colorChange = pdf.changeColor();
    fi.color = (pdf.getColor()!=null?pdf.getColor():Color.black);
    fi.imageRotationAngle = pdf.getRotation();
    fi.comment = "Pre-defined fax:" + pdf.getTitle();
    fi.show = true;
    fi.transparent = pdf.isTransparent();
    fi.faxOrigin = pdf.getOrigin();
    fi.imageHOffset = (int)(pixelXOffset / ratio);
    fi.imageVOffset = (int)(pixelYOffset / ratio);
    
    return fi;
  }
  
  public static class PreDefFax
  {
    private String title;
    private String origin;
    private double rotation;
    private double top;
    private double bottom;
    private double left;
    private double right;
    private boolean transparent;
    private boolean changeColor;
    private Color color;
    
    public PreDefFax(String title,
                     String origin,
                     double rotation,
                     double top,
                     double bottom,
                     double left,
                     double right,
                     boolean transparent,
                     boolean changeColor,
                     Color color)
    {
      this.title = title;
      this.origin = origin;
      this.rotation = rotation;
      this.top = top;
      this.bottom = bottom;
      this.left = left;
      this.right = right;
      this.transparent = transparent;
      this.changeColor = changeColor;
      this.color = color;
    }

    public String getTitle()
    {
      return title;
    }

    public String getOrigin()
    {
      return origin;
    }

    public double getRotation()
    {
      return rotation;
    }

    public void setTop(double top)
    {
      this.top = top;
    }

    public double getTop()
    {
      return top;
    }

    public void setBottom(double bottom)
    {
      this.bottom = bottom;
    }

    public double getBottom()
    {
      return bottom;
    }

    public void setLeft(double left)
    {
      this.left = left;
    }

    public double getLeft()
    {
      return left;
    }

    public void setRight(double right)
    {
      this.right = right;
    }

    public double getRight()
    {
      return right;
    }

    public void setTransparent(boolean transparent)
    {
      this.transparent = transparent;
    }

    public boolean isTransparent()
    {
      return transparent;
    }
    
    public String toString()
    {
      GeoPoint topLeft     = new GeoPoint(getTop(), getLeft());
      GeoPoint bottomRight = new GeoPoint(getBottom(), getRight());
      
      String str = getTitle() + "\n" +
                   getOrigin() + "\n" + 
                   topLeft.toString() + " to " + 
                   bottomRight.toString() + "\n" + 
                   "rotation:" + getRotation() + "\n" + 
                   "transparent:" + isTransparent() + "\n" +
                   "change color:" + changeColor();
      
      return str;
    }

    public boolean changeColor()
    {
      return changeColor;
    }

    public Color getColor()
    {
      return color;
    }
  }
}

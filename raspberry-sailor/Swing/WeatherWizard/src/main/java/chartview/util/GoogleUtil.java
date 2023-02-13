package chartview.util;

import calc.GeoPoint;

import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelInterface;

import chartview.ctx.WWContext;

import chartview.gui.right.CommandPanel;

import chartview.util.grib.GRIBDataUtil;

import chartview.util.grib.GribHelper;

import coreutilities.Utilities;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class GoogleUtil
{
  public final static int OPTION_MAP   = 0;
  public final static int OPTION_EARTH = 1;
  
  public static void generateGoogleFile(ChartPanel chartPanel, 
                                        int option, 
                                        CommandPanel.FaxImage[] faxImage, 
                                        CommandPanel caller, 
                                        boolean coloredWind, 
                                        String gribFileName, 
                                        boolean drawGRIB, 
                                        GribHelper.GribConditionData[] wgd,
                                        int gribIndex,
                                        JComboBox displayComboBox,
                                        boolean displayAltTooltip)
  {
    try
    {
      // Tooltip Option
      boolean dpt = chartPanel.isPositionToolTipEnabled();
      chartPanel.setPositionToolTipEnabled(false);
      Utilities.makeSureTempExists();
      WWContext.getInstance().fireSetStatus(WWGnlUtilities.buildMessage("generating-google"));
      WWContext.getInstance().fireSetLoading(true, WWGnlUtilities.buildMessage("generating"));
      ZipOutputStream kmz = null;
      String kmzFileName = "temp" + File.separator + WWGnlUtilities.SDF_.format(new Date()) + ".kmz";
      if (option == OPTION_EARTH)              
        kmz = new ZipOutputStream(new FileOutputStream(kmzFileName)); 
      // Calculate center of the chart
      double centerL = 0D, centerG = 0D;
      for (int i=0; faxImage!=null && i<faxImage.length; i++)
      {
        double fScale = faxImage[i].imageScale;
        int topLeftX = (int)(faxImage[i].imageHOffset * fScale);
        int topLeftY = (int)(faxImage[i].imageVOffset * fScale);
        int width = faxImage[i].faxImage.getWidth(null);
        int height = faxImage[i].faxImage.getHeight(null);
        GeoPoint topLeft = chartPanel.getGeoPos(topLeftX, topLeftY);
        GeoPoint bottomRight = chartPanel.getGeoPos((int)(topLeftX + (width * fScale)),
                                                    (int)(topLeftY + (height * fScale)));
        centerL += topLeft.getL();                                                            
        centerL += bottomRight.getL();   
        double left = topLeft.getG();
        if (bottomRight.getG() < left)
          left -= 360D;
        centerG += left;
        centerG += bottomRight.getG();
      }     
      if (faxImage != null)
      {
        centerL /= (2 * faxImage.length);
        centerG /= (2 * faxImage.length);
      }
      BufferedWriter bw = null;
      if (option == OPTION_MAP) 
        bw = new BufferedWriter(new FileWriter("temp" + File.separator + "googlefax.js"));
      else
        bw = new BufferedWriter(new FileWriter("temp" + File.separator + "doc.kml")); 
      
      if (option == OPTION_MAP) 
      {
        bw.write("centerlatitude=" + Double.toString(centerL) + ";\n" + 
                 "centerlongitude=" + Double.toString(centerG) + ";\n\n");
        bw.write("var faxarray = new Array(\n");
      }
      else
      {
        bw.write(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n" + 
        "  <Document>\n" + 
        "    <name>Weather Data, " + WWGnlUtilities.SDF_2.format(new Date()) + "</name>\n" +
        "    <open>1</open>\n");
        
      }
      boolean first = true;
      for (int i=0; faxImage!=null && i<faxImage.length; i++)
      {                
        if (!faxImage[i].show)
          continue; // Visible ones only
        if (Math.abs(faxImage[i].imageRotationAngle) % 90 != 0D)
    //              if (Math.abs(faxImage[i].imageRotationAngle) % 180 != 0D)
        {
          JOptionPane.showMessageDialog(caller, 
                                        WWGnlUtilities.buildMessage("google-90"), 
                                        WWGnlUtilities.buildMessage("google-generation"), 
                                        JOptionPane.WARNING_MESSAGE);
    //    JOptionPane.showMessageDialog(null, "Only multiples of 180 degrees \nare supported for Google Data rotation angles (for now)", "Google Data Generation", JOptionPane.WARNING_MESSAGE);
          continue;
        }
        double fScale = faxImage[i].imageScale;
        int topLeftX = (int)(faxImage[i].imageHOffset * fScale);
        int topLeftY = (int)(faxImage[i].imageVOffset * fScale);
        int width = faxImage[i].faxImage.getWidth(null);
        int height = faxImage[i].faxImage.getHeight(null);
        if (Math.abs(faxImage[i].imageRotationAngle) % 180 == 90D)
        {
          height = faxImage[i].faxImage.getWidth(null);
          width  = faxImage[i].faxImage.getHeight(null);
        }
        GeoPoint topLeft = chartPanel.getGeoPos(topLeftX, topLeftY);
    //  System.out.println("Top Left Corner    :" + topleft.toString());
        GeoPoint bottomRight = chartPanel.getGeoPos((int)(topLeftX + (width * fScale)),
                                                    (int)(topLeftY + (height * fScale)));
    //  System.out.println("Bottom Right Corner:" + bottomRight.toString());
        // If rotation...
        if (faxImage[i].imageRotationAngle != 0D)
        {
          // Translate...
          double tx = (((width * fScale)/2) - ((height * fScale)/2));
          double ty = (((height * fScale)/2) - ((width * fScale)/2));
          topLeft = chartPanel.getGeoPos((int)(topLeftX - tx), 
                                         (int)(topLeftY - ty));
          bottomRight = chartPanel.getGeoPos((int)(topLeftX + (width * fScale) - tx),
                                             (int)(topLeftY + (height * fScale) - ty));
        }
        
        String faxName = faxImage[i].fileName;
        try
        {
          String anaxFileName = WWGnlUtilities.DF2.format(i);
          Color c = faxImage[i].color;
          Image faximg = null;
          if (faxName.startsWith(WWContext.WAZ_PROTOCOL_PREFIX))
          {
            if (!WWContext.getInstance().getCurrentComposite().endsWith(WWContext.WAZ_EXTENSION))
            {
              // Reject
              JOptionPane.showMessageDialog(caller, 
                                            WWGnlUtilities.buildMessage("waz-incompatible"), 
                                            WWGnlUtilities.buildMessage("google-generation"), 
                                            JOptionPane.ERROR_MESSAGE);
              return;
            }
            ZipFile waz = new ZipFile(WWContext.getInstance().getCurrentComposite());
            if (faxName.startsWith(WWContext.WAZ_PROTOCOL_PREFIX))
              faxName = faxName.substring(WWContext.WAZ_PROTOCOL_PREFIX.length());
            InputStream is = waz.getInputStream(waz.getEntry(faxName));
            boolean tif = faxName.toUpperCase().endsWith(".TIFF") || faxName.toUpperCase().endsWith(".TIF");
            faximg = ImageUtil.readImage(is, tif);
          }
          else
            faximg = ImageUtil.readImage(faxName);
          
          if (caller.getProjection() == ChartPanel.MERCATOR)
          {
            BufferedImage image = ImageUtil.mercatorToAnaximandre(ImageUtil.toBufferedImage(faximg, Math.toRadians(faxImage[i].imageRotationAngle)), 
                                                                  Color.black, 
                                                                  topLeft.getL(), 
                                                                  bottomRight.getL(), 
                                                                  topLeft.getG(), 
                                                                  bottomRight.getG());
       //   Image img = ImageUtil.makeTransparentImage(null, Toolkit.getDefaultToolkit().createImage(image.getSource()), c);
            Image img = ImageUtil.switchColorAndMakeColorTransparent(Toolkit.getDefaultToolkit().createImage(image.getSource()), Color.black, c, Color.white, caller.getBlurSharpOption());

            ImageIO.write(ImageUtil.toBufferedImage(img), "png", new File("temp" + File.separator + anaxFileName + ".png"));
          }
          else if (caller.getProjection() == ChartPanel.ANAXIMANDRE)
          {
            Image img = null; // ImageUtil.makeTransparentImage(null, faximg, null);
            img = ImageUtil.makeColorTransparent(faximg, Color.white, caller.getBlurSharpOption());
            ImageIO.write(ImageUtil.toBufferedImage(img), "png", new File("temp" + File.separator + anaxFileName + ".png"));
          }
          if (option == OPTION_MAP)
            bw.write((first?"":",\n") + "  {name:\"" + ("temp/" + anaxFileName + ".png") + "\",\n" +
                     "   top:" + Double.toString(topLeft.getL()) + ",\n" +
                     "   bottom:" + Double.toString(bottomRight.getL()) + ",\n" +
                     "   left:" + Double.toString(topLeft.getG()) + ",\n" +
                     "   right:" + Double.toString(bottomRight.getG()) + "\n" +  
                     "  }\n");
          else // Earth
          {
            // Write the kmz file
            ZipEntry ze = new ZipEntry("files/" + (anaxFileName + ".png"));                     
            kmz.putNextEntry(ze);
            FileInputStream fin = new FileInputStream("temp" + File.separator + anaxFileName + ".png");
            Utilities.copy(fin, kmz);
            kmz.closeEntry();
            fin.close();
            
            // then the data
            double right = bottomRight.getG();
            double left  = topLeft.getG();
            if (right < left)
              left -= 360D;
            bw.write(
            "    <GroundOverlay>\n" + 
            "      <name>Fax " + (anaxFileName + ".png") + "</name>\n" + 
            "      <Icon>\n" + 
            "        <href>files/" + (anaxFileName + ".png") + "</href>\n" + 
            "        <viewBoundScale>0.75</viewBoundScale>\n" + 
            "      </Icon>\n" + 
            "      <LatLonBox>\n" + 
            "        <north>" + Double.toString(topLeft.getL()) + "</north>\n" + 
            "        <south>" + Double.toString(bottomRight.getL()) + "</south>\n" + 
            "        <east>" + Double.toString(right) + "</east>\n" + 
            "        <west>" + Double.toString(left) + "</west>\n" + 
            "      </LatLonBox>\n" + 
            "    </GroundOverlay>\n");
          }
          if (first)
            first = false;
        }
        catch (Exception ex)
        {
          JOptionPane.showMessageDialog(caller, ex.toString(), WWGnlUtilities.buildMessage("writing-anaximandre"), JOptionPane.ERROR_MESSAGE);
        }
      }
      if (gribFileName.trim().length() > 0  && drawGRIB)
      {
        JOptionPane.showMessageDialog(caller, 
                                      WWGnlUtilities.buildMessage("google-no-grib"),
                                      "Google Map & Google Earth", 
                                      JOptionPane.WARNING_MESSAGE);
      }
      if (false && gribFileName.trim().length() > 0  && drawGRIB)
      {
        // Original config
        int origProj = chartPanel.getProjection();
        chartPanel.setProjection(ChartPanelInterface.ANAXIMANDRE);
        boolean[] showFax = new boolean[faxImage.length];
        for (int i=0; faxImage!=null && i<faxImage.length; i++)
        {
          showFax[i] = faxImage[i].show;
          faxImage[i].show = false;
        }
        boolean chart = caller.isDrawChart();
        boolean grid  = chartPanel.isWithGrid();
        Color bg = chartPanel.getChartBackGround();
        chartPanel.setChartBackGround(Color.white);
        // No Chart, no Grid
        caller.setDrawChart(false);
        chartPanel.setWithGrid(false);
        coloredWind = false; // Wind displayed in black
        // Make it at least 2000 pixelswide
        int cpw = chartPanel.getWidth();
        double newZF = -1D;
        double origZF = chartPanel.getZoomFactor();
        if (cpw < 2000)
          newZF = 2000D / (double)cpw;

        if (newZF != -1D)
        {
          chartPanel.setZoomFactor(newZF);
          chartPanel.zoomIn();
        }
    //              chartPanel.repaint();
        chartPanel.genImage("temp" + File.separator + "grib", "png");
        boolean withTemp = wgd[gribIndex].temp;
        if (withTemp)
        {
          String dataOption = (String)displayComboBox.getSelectedItem();
          if (dataOption == null) dataOption = "WIND";
          String currentOption = dataOption;
          dataOption = "AIRTMP";
          displayComboBox.setSelectedItem(dataOption);
          chartPanel.repaint();
          chartPanel.genImage("temp" + File.separator + "temp", "png");
          displayComboBox.setSelectedItem(currentOption);
          chartPanel.repaint();
        }
        
        // NOT the GRIB size, but the Chart size
        double n = chartPanel.getNorthL();
        double s = chartPanel.getSouthL();
        double e = chartPanel.getEastG();
        double w = chartPanel.getWestG();
        
        // make it transparent
        try
        {
          @SuppressWarnings("deprecation")
          Image img = ImageUtil. makeTransparentImage(null, ImageUtil.readImage("temp" + File.separator + "grib.png"), Color.cyan);
          ImageIO.write(ImageUtil.toBufferedImage(img), "png", new File("temp" + File.separator + "grib.png"));
        }
        catch (Exception imgEx)
        {
          imgEx.printStackTrace();                  
        }
        
        // reset
        chartPanel.setProjection(origProj);
        for (int i=0; faxImage!=null && i<faxImage.length; i++)
          faxImage[i].show = showFax[i];
        caller.setDrawChart(chart);
        chartPanel.setWithGrid(grid);
        coloredWind = true;
        if (newZF != -1D)
        {
          chartPanel.zoomOut();
          chartPanel.setZoomFactor(origZF);
        }
        chartPanel.setChartBackGround(bg);
        chartPanel.repaint();
        // write the Google info
        if (option == OPTION_MAP)
        {
          if (faxImage.length > 0)
            bw.write(",\n");
          bw.write("  {name:\"temp/grib.png\",\n" +
                   "   top:" + Double.toString(n) + ",\n" +
                   "   bottom:" + Double.toString(s) + ",\n" +
                   "   left:" + Double.toString(w) + ",\n" +
                   "   right:" + Double.toString(e) + "\n" +  
                   "  }\n");
        }
        else
        {
          // Write the kmz file
          ZipEntry ze = new ZipEntry("files/grib.png");                     
          kmz.putNextEntry(ze);
          FileInputStream fin = new FileInputStream("temp" + File.separator + "grib.png");
          Utilities.copy(fin, kmz);
          kmz.closeEntry();
          fin.close();

          if (withTemp)
          {
            ze = new ZipEntry("files/temp.png");                     
            kmz.putNextEntry(ze);
            fin = new FileInputStream("temp" + File.separator + "temp.png");
            Utilities.copy(fin, kmz);
            kmz.closeEntry();
            fin.close();
          }
    
          double right = e;
          double left  = w;
          if (right < left)
            left -= 360D;
          bw.write(
          "    <GroundOverlay>\n" + 
          "      <name>GRIB Wind</name>\n" + 
          "      <Icon>\n" + 
          "        <href>files/grib.png</href>\n" + 
          "        <viewBoundScale>0.75</viewBoundScale>\n" + 
          "      </Icon>\n" + 
          "      <LatLonBox>\n" + 
          "        <north>" + Double.toString(n) + "</north>\n" + 
          "        <south>" + Double.toString(s) + "</south>\n" + 
          "        <east>" + Double.toString(right) + "</east>\n" + 
          "        <west>" + Double.toString(left) + "</west>\n" + 
          "      </LatLonBox>\n" + 
          "    </GroundOverlay>\n");
          if (withTemp)
          {
            bw.write(
            "    <GroundOverlay>\n" + 
            "      <name>GRIB Temperature</name>\n" + 
            "      <visibility>0</visibility>\n" +
            "      <Icon>\n" + 
            "        <href>files/temp.png</href>\n" + 
            "        <viewBoundScale>0.75</viewBoundScale>\n" + 
            "      </Icon>\n" + 
            "      <LatLonBox>\n" + 
            "        <north>" + Double.toString(n) + "</north>\n" + 
            "        <south>" + Double.toString(s) + "</south>\n" + 
            "        <east>" + Double.toString(right) + "</east>\n" + 
            "        <west>" + Double.toString(left) + "</west>\n" + 
            "      </LatLonBox>\n" + 
            "    </GroundOverlay>\n");
          }
        }              
      }
      if (option == OPTION_MAP) 
      {
        bw.write(");\n");
        if (faxImage.length == 0)
        {
          double n = chartPanel.getNorthL();
          double s = chartPanel.getSouthL();
          double e = chartPanel.getEastG();
          double w = chartPanel.getWestG();
          double left = w;
          if (e < left) left -= 360D;                    
          bw.write("\ncenterlatitude=" + Double.toString((n + s) / 2D) + ";\n" + 
                     "centerlongitude=" + Double.toString((e + left) / 2D) + ";\n");                    
        }
      }
      else
      {
        // GRIB Data for Google Earth
        if (wgd != null)
        {
          int arraysize = 0;
          if (wgd[gribIndex].hgt) arraysize++;
          if (wgd[gribIndex].prmsl) arraysize++;
          if (wgd[gribIndex].wave) arraysize++;
    //    if (wgd[gribIndex].temp) arraysize++;
          int[] options = new int[arraysize];
          arraysize = 0;
          if (wgd[gribIndex].hgt) options[arraysize++] = GRIBDataUtil.TYPE_500MB;
          if (wgd[gribIndex].prmsl) options[arraysize++] = GRIBDataUtil.TYPE_PRMSL;
          if (wgd[gribIndex].wave) options[arraysize++] = GRIBDataUtil.TYPE_WAVE;
    //    if (wgd[gribIndex].temp) options[arraysize++]  = GRIBDataUtil.TYPE_TMP;
          GRIBDataUtil.generateKMLGRIBFile(wgd[gribIndex], options, chartPanel, bw);
        }                             
        
        bw.write(
        "  </Document>\n" + 
        "</kml>");
      }
      // TODO Boat Position & Track in Google
      if (option == OPTION_MAP)
      {
      }
      else if (option == OPTION_EARTH)
      {
      }
      
      bw.close();
      if (option == OPTION_EARTH)
      {
        ZipEntry ze = new ZipEntry("doc.kml");                 
        kmz.putNextEntry(ze);
        FileInputStream fin = new FileInputStream("temp" + File.separator + "doc.kml");
        Utilities.copy(fin, kmz);
        kmz.closeEntry();
        fin.close();
        
        kmz.close();                
      }

      WWContext.getInstance().fireSetStatus(WWGnlUtilities.buildMessage("google-ready"));
      WWContext.getInstance().fireSetLoading(false, WWGnlUtilities.buildMessage("generating"));
      if (option == OPTION_MAP)
      {
        // Now start goolemap
        try
        { Utilities.openInBrowser("googlefax.html"); } 
        catch (Exception exception) 
        {
          WWContext.getInstance().fireExceptionLogging(exception);
          exception.printStackTrace(); 
        }                
      }
      else
      {
        String os = System.getProperty("os.name");
        if (os.indexOf("Windows") > -1)
          Runtime.getRuntime().exec("cmd /k start " + kmzFileName);
        else
        {
          String mess = "OS [" + os + "] not supported yet...\nStart " + kmzFileName + " by hand";
          System.out.println(mess);
          JOptionPane.showMessageDialog(caller, mess, "Google", JOptionPane.WARNING_MESSAGE);
        }
      }
      WWContext.getInstance().fireSetStatus("Ready"); 
      chartPanel.setPositionToolTipEnabled(dpt);
      if (displayAltTooltip)
        caller.repaint();
    }
    catch (Exception e)
    {
      WWContext.getInstance().fireExceptionLogging(e);
      e.printStackTrace();
    }    
  }
}

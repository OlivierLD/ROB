package chartview.util.grib;


import calc.GeoPoint;

import chart.components.ui.ChartPanel;
import chart.components.util.World;

import chartview.ctx.WWContext;

import chartview.gui.right.wire.ObjMaker;

import chartview.util.WWGnlUtilities;

import coreutilities.Utilities;

import java.awt.Point;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordPDS;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;

import org.w3c.dom.Text;


public class GRIBDataUtil
{
  public final static int TYPE_500MB = 0;
  public final static int TYPE_PRMSL = 1;
  public final static int TYPE_TMP   = 2;
  public final static int TYPE_WAVE  = 3;
  public final static int TYPE_RAIN  = 4;
  public final static int TYPE_TWS   = 5; // That one involves calculation
  public final static int TYPE_CURRENT = 6;

  /* Must ne in sync with the previous final statics */
  public final static String[] DATA_NAME = new String[] { 
                                                          "500mb", 
                                                          "prmsl", 
                                                          "temperature", 
                                                          "waves", 
                                                          "rain", 
                                                          "tws" 
                                                        };
  
  public final static double[] getWindSpeedBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          float x = gribData.getGribPointData()[h][w].getU();
          float y = gribData.getGribPointData()[h][w].getV();
          double speed = getGRIBWindSpeed(x, y); // Already adjusted
  
          if (speed < minValue) minValue = speed;
          if (speed > maxValue) maxValue = speed;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
    
  public final static double[] getCurrentSpeedBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          float x = gribData.getGribPointData()[h][w].getUOgrd();
          float y = gribData.getGribPointData()[h][w].getVOgrd();
          double speed = getGRIBWindSpeed(x, y);
  
          if (speed < minValue) minValue = speed;
          if (speed > maxValue) maxValue = speed;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
  
  public final static double getGRIBWindSpeed(float x, float y)
  {
    double tws = Math.sqrt((x * x) + (y * y));
    tws *= 3.600D; // m/s to km/h
    tws /= 1.852D; // km/h to knots
    return GribHelper.adjustWindSpeed((float)tws);
  }
  
  public final static double[] getPRMSLBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double val = gribData.getGribPointData()[h][w].getPrmsl() / 100D;
  
          if (val < minValue) minValue = val;
          if (val > maxValue) maxValue = val;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
  
  public final static double[] get500MbBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double val = gribData.getGribPointData()[h][w].getHgt();
  
          if (val < minValue) minValue = val;
          if (val > maxValue) maxValue = val;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
  
  public final static double[] getAirTempBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double val = gribData.getGribPointData()[h][w].getAirtmp() - 273D;
  
          if (val < minValue) minValue = val;
          if (val > maxValue) maxValue = val;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
  
  public final static double[] getWaveHgtBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double val = gribData.getGribPointData()[h][w].getWHgt() / 100D;
  
          if (val < minValue) minValue = val;
          if (val > maxValue) maxValue = val;
        }
      }
    }
    return new double[] { minValue, maxValue };
  }
  
  public final static double[] getRainBoundaries(GribHelper.GribConditionData gribData)
  {
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double val = gribData.getGribPointData()[h][w].getRain() * 3_600D;
  
          if (val < minValue) minValue = val;
          if (val > maxValue) maxValue = val;
        }
      }
    }
//  System.out.println("Rain boundaries:" + minValue + "/" + maxValue);
    return new double[] { minValue, maxValue };
  }
  
  public final static List<List<List<GeoPoint>>> generateIsoTWS(GribHelper.GribConditionData gribData, int[] va)
  { return generateIsoCurves(gribData, va, TYPE_TWS); }
  
  public final static List<List<List<GeoPoint>>> generateIsobars(GribHelper.GribConditionData gribData, int[] va)
  { return generateIsoCurves(gribData, va, TYPE_PRMSL); }

  public final static List<List<List<GeoPoint>>> generateIso500(GribHelper.GribConditionData gribData, int[] va)
  { return generateIsoCurves(gribData, va, TYPE_500MB); }
  
  public final static List<List<List<GeoPoint>>> generateIsotherm(GribHelper.GribConditionData gribData, int[] va)
  { return generateIsoCurves(gribData, va, TYPE_TMP); }
  
  public final static List<List<List<GeoPoint>>> generateIsowaves(GribHelper.GribConditionData gribData, int[] va)
  { return generateIsoCurves(gribData, va, TYPE_WAVE); }
  
  public final static List<List<List<GeoPoint>>> generateIsorain(GribHelper.GribConditionData gribData, int[] va)
  { 
    double[] dva = new double[va.length];
    for (int i=0; i<va.length; i++)
      dva[i] = ((double)va[i] / 1E4);
    return generateIsoCurves(gribData, dva, TYPE_RAIN); 
  }
  
  private final static List<List<List<GeoPoint>>> generateIsoCurves(GribHelper.GribConditionData gribData, int[] va, int dataType)
  {
    double[] dva = new double[va.length];
    for (int i=0; i<va.length; i++)
      dva[i] = (double)va[i];
    return generateIsoCurves(gribData, dva, dataType);
  }
  
  private final static List<List<List<GeoPoint>>> generateIsoCurves(GribHelper.GribConditionData gribData, double[] va, int dataType)
  {
    List<List<List<GeoPoint>>> alalalgp = new ArrayList<List<List<GeoPoint>>>();
    for (int i=0; i<va.length; i++)
    {
  //  long before = System.currentTimeMillis();
      alalalgp.add(detect(va[i], gribData, dataType));
  //  long after = System.currentTimeMillis();
  //  System.out.println("Detection + Binding done in " + Long.toString(after - before) + " ms for " + Integer.toString(va[i]));
    }
    return alalalgp;
  }
  
  private static List<List<GeoPoint>> detect(double val2detect, GribHelper.GribConditionData gribData, int dataType)
  {
    List<List<GeoPoint>> alalgp = new ArrayList<List<GeoPoint>>();
    List<GeoPoint> iso = new ArrayList<GeoPoint>();
    
    // Detect
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length - 1; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length - 1; w++)
      {
        if (gribData.getGribPointData()[h][w] != null &&
            gribData.getGribPointData()[h][w + 1] != null &&
            gribData.getGribPointData()[h + 1][w + 1] != null &&
            gribData.getGribPointData()[h + 1][w] != null)
        {
          double lat = gribData.getGribPointData()[h][w].getLat();
          double lng = gribData.getGribPointData()[h][w].getLng();
          
          double[][] around = valuesAround(dataType, gribData, w, h); 
          double[] ptOffset = isWithin(val2detect, around) ;
          if (ptOffset != null)
          {
            // Get the cell size
            double newLat = lat + (ptOffset[0] * gribData.getStepY());
            double newLng = lng + (ptOffset[1] * gribData.getStepX());
            iso.add(new GeoPoint(newLat, newLng));
          }
        }
      }
    }
    // Bind
    boolean proceed = true;
    while (proceed)
    {
      if (iso.size() == 0) 
        proceed = false;
      else
      {
        GeoPoint p = iso.get(0);
        List<GeoPoint> is = new ArrayList<GeoPoint>();
        alalgp.add(is);
        is.add(p);
        iso.remove(p);
        boolean closestFound = true;
        while (closestFound)
        {
          // Look into iso for the point the closest to p, if its distance is lower that maxDist
          double maxDist = 2d * Math.max(gribData.getStepX(), gribData.getStepY()) * 60d; // in miles.
          GeoPoint close = getClosest(p, iso, maxDist); 
          if (close != null)
          {
            is.add(close);
            iso.remove(close);
            p = close;
          }
          else
            closestFound = false;
        }
//      System.out.println("Island with " + is.size() + " point(s).");
      }
    }    
    // Return
    return alalgp;
  }
  
  private static double[][] valuesAround(int dataType, GribHelper.GribConditionData gribData, int w, int h)
  {
    double value      = -Double.MAX_VALUE,
           nextValue1 = -Double.MAX_VALUE,
           nextValue2 = -Double.MAX_VALUE,
           nextValue3 = -Double.MAX_VALUE;
    
    switch (dataType)
    {
      case TYPE_TWS:
        value = getGRIBWindSpeed(gribData.getGribPointData()[h][w].getU(),
                                 gribData.getGribPointData()[h][w].getV());
        nextValue1 = getGRIBWindSpeed(gribData.getGribPointData()[h][w+1].getU(),
                                      gribData.getGribPointData()[h][w+1].getV());
        nextValue2 = getGRIBWindSpeed(gribData.getGribPointData()[h+1][w+1].getU(),
                                      gribData.getGribPointData()[h+1][w+1].getV());
        nextValue3 = getGRIBWindSpeed(gribData.getGribPointData()[h+1][w].getU(),
                                      gribData.getGribPointData()[h+1][w].getV());
        break;
      case TYPE_PRMSL:
        value = gribData.getGribPointData()[h][w].getPrmsl() / 100D;          
        nextValue1 = gribData.getGribPointData()[h][w+1].getPrmsl() / 100D;
        nextValue2 = gribData.getGribPointData()[h+1][w+1].getPrmsl() / 100D;
        nextValue3 = gribData.getGribPointData()[h+1][w].getPrmsl() / 100D;
        break;
      case TYPE_500MB:
        value = gribData.getGribPointData()[h][w].getHgt();          
        nextValue1 = gribData.getGribPointData()[h][w+1].getHgt();
        nextValue2 = gribData.getGribPointData()[h+1][w+1].getHgt();
        nextValue3 = gribData.getGribPointData()[h+1][w].getHgt();
        break;
      case TYPE_RAIN:
        value = gribData.getGribPointData()[h][w].getRain();          
        nextValue1 = gribData.getGribPointData()[h][w+1].getRain();
        nextValue2 = gribData.getGribPointData()[h+1][w+1].getRain();
        nextValue3 = gribData.getGribPointData()[h+1][w].getRain();
        break;
      case TYPE_TMP:
        value = gribData.getGribPointData()[h][w].getAirtmp() - 273D;          
        nextValue1 = gribData.getGribPointData()[h][w+1].getAirtmp() - 273D;
        nextValue2 = gribData.getGribPointData()[h+1][w+1].getAirtmp() - 273D;
        nextValue3 = gribData.getGribPointData()[h+1][w].getAirtmp() - 273D;
        break;
      case TYPE_WAVE:
        value = gribData.getGribPointData()[h][w].getWHgt() / 100D;          
        nextValue1 = gribData.getGribPointData()[h][w+1].getWHgt() / 100D;
        nextValue2 = gribData.getGribPointData()[h+1][w+1].getWHgt() / 100D;
        nextValue3 = gribData.getGribPointData()[h+1][w].getWHgt() / 100D;
        break;
      default:
        System.out.println("valuesAround, type " + dataType + " not found!!");
        break;
    }
    return new double[][] { { value, nextValue1 }, { nextValue2, nextValue3 } };
  }
  
  private static double[] isWithin(double value, double[][] around)
  {
    double[] coordinates = null;
    assert around.length == 2 && around[0].length == 2;
        
    if (value == around[0][0])
      coordinates = new double[] {0f, 0f};
    else
    {
      if (around[0][0] < value && ((around[0][1] != -Double.MAX_VALUE && around[0][1] > value) ||
                                   (around[1][1] != -Double.MAX_VALUE && around[1][1] > value) ||
                                   (around[1][0] != -Double.MAX_VALUE && around[1][0] > value)))
      {
        double x = 0f, y = 0f;
        double under = value - around[0][0];
        if (around[1][0] != -Double.MAX_VALUE && around[1][0] > value)
        {
          double over  = around[1][0] - value;
          x = under / (under + over);
        }
        else if (around[0][1] != -Double.MAX_VALUE && around[0][1] > value)
        {
          double over  = around[0][1] - value;
          y = under / (under + over);
        }
        else if (around[1][1] != -Double.MAX_VALUE && around[1][1] > value)
        {
          double over  = around[1][1] - value;
          x = 0.707f * under / (under + over);
          y = x;
        }
        coordinates = new double[] {x, y};
      }
      else if (around[0][0] > value && ((around[0][1] != -Double.MAX_VALUE && around[0][1] < value) ||
                                        (around[1][1] != -Double.MAX_VALUE && around[1][1] < value) ||
                                        (around[1][0] != -Double.MAX_VALUE && around[1][0] < value)))
      {
        double x = 0f, y = 0f;
        double over  = around[0][0] - value;
        if (around[1][0] != -Double.MAX_VALUE && around[1][0] < value)
        {
          double under = value - around[1][0];
          x = over / (under + over);
        }
        else if (around[0][1] != -Double.MAX_VALUE && around[0][1] < value)
        {
          double under = value - around[0][1];
          y = over / (under + over);
        }
        else if (around[1][1] != -Double.MAX_VALUE && around[1][1] < value)
        {
          double under = value - around[1][1];
          x = 0.707f * over / (under + over);
          y = x;
        }
        coordinates = new double[] {x, y};
      }
    }    
    return coordinates;
  }
  
  private static GeoPoint getClosest(GeoPoint pt, List<GeoPoint> al, double maxDist)
  {
    GeoPoint p = null;
    double min = maxDist;
    for (GeoPoint point : al)
    {
      double dist = getDistance(pt, point);
      if (dist < min)
      {
        p = point;
        min = dist;
      }      
    }  
//  if (p != null) System.out.println("Min Dist = " + min);
    return p;
  }

  // That one is the bottleneck. ortho needs improvements.
  private static double getDistance(GeoPoint a, GeoPoint b)
  {
    // Loxo
//  return a.loxoDistanceBetween(b);
    // Ortho
//  return a.orthoDistanceBetween(b);
    // GC
    return a.gcDistanceBetween(b);
    // Pythagore
//  return Math.sqrt(((a.getG() - b.getG()) * (a.getG() - b.getG())) + 
//                   ((a.getL() - b.getL()) * (a.getL() - b.getL())));
  }
  
  public final static boolean thereIsVariation(GribFile gribFile, int option)
  {
    boolean ok = false;
    for (int i = 0; !ok && i < gribFile.getLightRecords().length; i++)
    {        
      try
      {
        GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
        GribRecordPDS grpds = gr.getPDS(); // Headers and Data
        GribRecordBDS grbds = gr.getBDS(); 
        String type = grpds.getType();
//      System.out.println("type:" + type);
//        if (option == TYPE_TWS)
//          throw new RuntimeException("Type TWS Not supported in this method");
        if (option == TYPE_TWS && (type.equals("ugrd") || type.equals("vgrd")))
        {
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_500MB && type.equals("hgt"))
        {
          // float min = grbds.getMinValue();
          // float max = grbds.getMaxValue();
          // ok = (max - min) > 0f;
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_PRMSL && type.equals("prmsl"))
        {
          // float min = grbds.getMinValue();
          // float max = grbds.getMaxValue();
          // ok = (max - min) > 0f;
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_TMP && type.equals("tmp"))
        {
          // float min = grbds.getMinValue();
          // float max = grbds.getMaxValue();
          // ok = (max - min) > 0f;
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_WAVE && type.equals("htsgw"))
        {
          // float min = grbds.getMinValue();
          // float max = grbds.getMaxValue();
          // ok = (max - min) > 0f;
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_RAIN && type.equals("prate"))
        {
          // float min = grbds.getMinValue();
          // float max = grbds.getMaxValue();
          // ok = (max - min) > 0f;
          ok = !grbds.getIsConstant();
        }
        else if (option == TYPE_CURRENT && (type.equals("uogrd") || type.equals("vogrd")))
        {
          ok = !grbds.getIsConstant();
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    return ok;
  }
  
  public final static boolean thereIsVariation(GribHelper.GribConditionData gribData, int option)
  {
    boolean ok = false;
    double prevVal = 0D;
    double value   = 0D;
    boolean first = true;
    
    for (int h=0; !ok && gribData.getGribPointData() != null && h<gribData.getGribPointData().length; h++)
    {
      for (int w=0; !ok && w<gribData.getGribPointData()[h].length; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          switch (option)
          {
            case TYPE_500MB:
              value = gribData.getGribPointData()[h][w].getHgt();
              break;
            case TYPE_PRMSL:
              value = gribData.getGribPointData()[h][w].getPrmsl();
              break;
            case TYPE_TMP:
              value = gribData.getGribPointData()[h][w].getAirtmp();
              break;
            case TYPE_WAVE:
              value = gribData.getGribPointData()[h][w].getWHgt();
              break;
            case TYPE_RAIN:
              value = gribData.getGribPointData()[h][w].getRain();
              break;
            default:
              break;
          }
          if (!first) // Not the first one
          {
            if (value != prevVal)
              ok = true;
          }
          first = false;
          prevVal = value;
        }
      }
    }    
    return ok;
  }
  
  public final static double[] generate3dFile(GribHelper.GribConditionData gribData,
                                              int option,
                                              double scaleFactor,
                                              double valueFactor,
                                              ChartPanel chartPanel)
  {
//  System.out.println("Option: " + option + ", Value Factor:" + valueFactor);
    boolean ok = true;
    
//  System.out.println("3D Generation: h=" + gribData.getGribPointData().length + ", w=" + gribData.getGribPointData()[0].length);
    
    double minValue = Integer.MAX_VALUE;
    double maxValue = Integer.MIN_VALUE;
    for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length && ok; h++)
    {
      for (int w=0; w<gribData.getGribPointData()[h].length && ok; w++)
      {
        if (gribData.getGribPointData()[h][w] != null)
        {
          double value = 0D;
          if (option == TYPE_500MB)
            value = gribData.getGribPointData()[h][w].getHgt();
          else if (option == TYPE_PRMSL)
            value = gribData.getGribPointData()[h][w].getPrmsl() / 10D;
          else if (option == TYPE_TMP)
            value = gribData.getGribPointData()[h][w].getAirtmp();
          else if (option == TYPE_WAVE)
          {
            double wh = gribData.getGribPointData()[h][w].getWHgt();
            if (wh > 3_000 || wh < 0)
              wh = 0.01;
            value = wh / 10D;
          }
          else if (option == TYPE_RAIN)
            value = gribData.getGribPointData()[h][w].getRain() * 3_600D;
          else if (option == TYPE_TWS)
            value = getGRIBWindSpeed(gribData.getGribPointData()[h][w].getU(),
                                     gribData.getGribPointData()[h][w].getV());
  //      value *= valueFactor;          
          if (value == 0 && option != TYPE_WAVE && option != TYPE_RAIN && option != TYPE_TWS)
            ok = false;
          else
          {
            if (value < minValue) minValue = value;
            if (value > maxValue) maxValue = value;
          }
        }
      }
    }
    if (minValue == 0 && maxValue == 0)
      ok = false; // that's for tws, waves and rain
      
    gribData.wind = true;
    if (option == TYPE_500MB)
      gribData.hgt = ok;
    else if (option == TYPE_PRMSL)
      gribData.prmsl = ok;
    else if (option == TYPE_TMP)
      gribData.temp = ok;
    else if (option == TYPE_WAVE)
      gribData.wave = ok;
    else if (option == TYPE_RAIN)
      gribData.rain = ok;
//  else if (option == TYPE_TWS)
//    gribData.wind = ok;
    
    if (ok)
      WWContext.getInstance().fireLogging("Value of " + DATA_NAME[option] + " between " + minValue + " and " + maxValue);
    else
      WWContext.getInstance().fireLogging("No value for " + DATA_NAME[option]);
    WWContext.getInstance().fireLogging("\n");

    XMLDocument xmlDoc = null;
    XMLElement root    = null;
    if (ok)
    {
      xmlDoc = new XMLDocument();
      root = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "data");
      root.setAttribute("name", DATA_NAME[option]);
      xmlDoc.appendChild(root);
      // buttocks, NS
      XMLElement _buttocks = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "buttocks");
      root.appendChild(_buttocks);
      //        System.out.println("E-W:0 to " + gribData.getWindPointData().length);
      for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length && ok; h++)
      {
        XMLElement _north2south = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "buttock");
        _buttocks.appendChild(_north2south);
        XMLElement part = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "part");
        part.setAttribute("id", "1"); // yo!
        _north2south.appendChild(part);
        boolean yIsSet = false;
        for (int w=0; w<gribData.getGribPointData()[h].length && ok; w++)
        {
          if (gribData.getGribPointData()[h][w] != null)
          {
            double lat = gribData.getGribPointData()[h][w].getLat();
            double lng = gribData.getGribPointData()[h][w].getLng();
            Point pp = chartPanel.getPanelPoint(lat, lng);
            if (pp.x < 0 || pp.x > chartPanel.getWidth() ||
                pp.y < 0 || pp.y > chartPanel.getHeight())
              continue;
              
            double value = 0;
            if (option == TYPE_500MB)
              value = gribData.getGribPointData()[h][w].getHgt();
            else if (option == TYPE_PRMSL)
              value = gribData.getGribPointData()[h][w].getPrmsl() / 10D;
            else if (option == TYPE_TMP)
              value = gribData.getGribPointData()[h][w].getAirtmp();
            else if (option == TYPE_RAIN)
              value = gribData.getGribPointData()[h][w].getRain() * 3_600D;
            else if (option == TYPE_WAVE)
            {
              double wh = gribData.getGribPointData()[h][w].getWHgt();
              if (wh > 3_000 || wh < 0)
                wh = 0.01;
              value = wh / 10D;
            }
            else if (option == TYPE_TWS)
              value = getGRIBWindSpeed(gribData.getGribPointData()[h][w].getU(),
                                       gribData.getGribPointData()[h][w].getV());
            if (value == 0 && option != TYPE_WAVE && option != TYPE_RAIN && option != TYPE_TWS)
              ok = false;
            else
            {
              if (!yIsSet)
              {
                _north2south.setAttribute("y", Integer.toString((int)(pp.y * scaleFactor))); // was h
                 yIsSet = true;
              }
              XMLElement plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
              plot.setAttribute("id", Integer.toString(w));
              part.appendChild(plot);
              XMLElement x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
              XMLElement z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
              plot.appendChild(x);
              plot.appendChild(z);
              Text txt = xmlDoc.createTextNode("txt#");
              txt.setNodeValue(Integer.toString((int)(pp.x * scaleFactor))); // was w
              
    //        System.out.println("For w:" + w + ", x:" + ((int)(pp.x * scaleFactor)));
              
              x.appendChild(txt);
              txt = xmlDoc.createTextNode("txt#");
              txt.setNodeValue(WWGnlUtilities.BIG_DOUBLE.format((-(value - minValue) * valueFactor) / 2));
              z.appendChild(txt);
  
              // For GoogleEarth:
  //            if (option == TYPE_500MB)
  //              System.out.println(Double.toString(lng) + "," + Double.toString(lat) + "," + Double.toString(value));
  
            }
          }
        }
        if (part.getChildNodes().getLength() == 0) // remove
        {
          _north2south.removeChild(part);
          _buttocks.removeChild(_north2south); // There is only one part...
        }
      }
    }
    // forms, EW
    if (ok)
    {
    //System.out.println("N-S:0 to " + gribData.getWindPointData()[0].length);
      XMLElement _forms = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "forms");
      root.appendChild(_forms);
      for (int w=0; gribData.getGribPointData() != null && w<gribData.getGribPointData()[0].length; w++)
      {
        XMLElement _east2west = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "form");
        _forms.appendChild(_east2west);
        
        boolean xIsSet = false;
        for (int h=0; h<gribData.getGribPointData().length; h++)
        {
          if (gribData.getGribPointData()[h][w] != null)
          {
            double lat = gribData.getGribPointData()[h][w].getLat();
            double lng = gribData.getGribPointData()[h][w].getLng();
            Point pp = chartPanel.getPanelPoint(lat, lng);
            if (pp.x < 0 || pp.x > chartPanel.getWidth() ||
                pp.y < 0 || pp.y > chartPanel.getHeight())
              continue;
              
            if (!xIsSet)
            {
              _east2west.setAttribute("x", Integer.toString((int)(pp.x * scaleFactor))); // was w
               xIsSet = true;
            }  
            double value = 0;
            if (option == TYPE_500MB)
              value = gribData.getGribPointData()[h][w].getHgt();
            else if (option == TYPE_PRMSL)
              value = gribData.getGribPointData()[h][w].getPrmsl() / 10D; 
            else if (option == TYPE_TMP)
              value = gribData.getGribPointData()[h][w].getAirtmp();
            else if (option == TYPE_RAIN)
              value = gribData.getGribPointData()[h][w].getRain() * 3_600D;
            else if (option == TYPE_WAVE)
            {
              double wh = gribData.getGribPointData()[h][w].getWHgt();
              if (wh > 3_000 || wh < 0)
                wh = 0.01;
              value = wh / 10D;
            }
            else if (option == TYPE_TWS)
              value = getGRIBWindSpeed(gribData.getGribPointData()[h][w].getU(),
                                       gribData.getGribPointData()[h][w].getV());
              
            XMLElement plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
            plot.setAttribute("id", Integer.toString(h));
            _east2west.appendChild(plot);
            XMLElement y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
            XMLElement z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
            plot.appendChild(y);
            plot.appendChild(z);
            Text txt = xmlDoc.createTextNode("txt#");
            txt.setNodeValue(Integer.toString((int)(pp.y * scaleFactor))); // was h
            y.appendChild(txt);
            txt = xmlDoc.createTextNode("txt#");
            txt.setNodeValue(WWGnlUtilities.BIG_DOUBLE.format((-(value - minValue) * valueFactor) / 2));
            z.appendChild(txt);
          }
        }
        if (_east2west.getChildNodes().getLength() == 0)
        {
          _forms.removeChild(_east2west);
        }
      }
    }
    // Push it out
    if (ok)
    {
      try
      {
        Utilities.makeSureTempExists();
        xmlDoc.print(new FileOutputStream(new File("temp" + File.separator + DATA_NAME[option] + ".xml")));
        ObjMaker.generate("temp" + File.separator + DATA_NAME[option] + ".xml", false);
        Point _n = new Point((int)(chartPanel.getWidth() * scaleFactor) / 2, 0);
        Point _s = new Point((int)(chartPanel.getWidth()  * scaleFactor) / 2, 
                             (int)(chartPanel.getHeight() * scaleFactor));
        Point _w = new Point(0, (int)(chartPanel.getHeight() * scaleFactor) / 2);
        Point _e = new Point((int)(chartPanel.getWidth() * scaleFactor), 
                             (int)(chartPanel.getHeight() * scaleFactor) / 2);
        List<Point> labelPts = new ArrayList<Point>(4);
        labelPts.add(_n);
        labelPts.add(_s);
        labelPts.add(_w);
        labelPts.add(_e);
        if (option == TYPE_500MB)
          WWContext.getInstance().fireNew500mbObj(labelPts);
        else if (option == TYPE_PRMSL)
          WWContext.getInstance().fireNewPrmslObj(labelPts);
        else if (option == TYPE_TMP)
          WWContext.getInstance().fireNewTmpObj(labelPts);
        else if (option == TYPE_WAVE)
          WWContext.getInstance().fireNewWaveObj(labelPts);
        else if (option == TYPE_RAIN)
          WWContext.getInstance().fireNewRainObj(labelPts);
        else if (option == TYPE_TWS)
          WWContext.getInstance().fireNewTWSObj(labelPts);          
      }
      catch (IOException e) {}
    }
    else
    {
      if (option == TYPE_500MB)
        WWContext.getInstance().fireNo500mbObj();
      else if (option == TYPE_PRMSL)
        WWContext.getInstance().fireNoPrmslObj();
      else if (option == TYPE_TMP)
        WWContext.getInstance().fireNoTmpObj();
      else if (option == TYPE_WAVE)
        WWContext.getInstance().fireNoWaveObj();
      else if (option == TYPE_RAIN)
        WWContext.getInstance().fireNoRainObj();
      else if (option == TYPE_TWS)
        WWContext.getInstance().fireNoTWSObj();
    }
    if (option == TYPE_500MB)
    {
      minValue *= 10D;
      maxValue *= 10D;
    }
    return new double[] { minValue, maxValue };
  }

  public final static void generateChart3dFile(GribHelper.GribConditionData gribData,
                                               double scaleFactor,
                                               ChartPanel chartPanel)
  { 
    XMLDocument xmlDoc = null;
    XMLElement root    = null;

    xmlDoc = new XMLDocument();
    root = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "data");
    root.setAttribute("name", "chart");
    xmlDoc.appendChild(root);

    WWContext.getInstance().getParser().setValidationMode(XMLParser.NONVALIDATING);
    List<List<Point>> chartPoints = World.getChartPoints(chartPanel, WWContext.getInstance().getParser());
    if (chartPoints != null && chartPoints.size() > 0)
    {
      XMLElement modules = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "modules");
      root.appendChild(modules);
      // A frame around the chart, sized after the GRIB
      {
        XMLElement frameModule = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "module");
        modules.appendChild(frameModule);
        frameModule.setAttribute("name", "chart-frame");
        frameModule.setAttribute("symetric", "no");
        double n = gribData.getNLat();
        double s = gribData.getSLat();
        double w = gribData.getWLng();
        double e = gribData.getELng();
        Point topLeft  = chartPanel.getPanelPoint(n, w);
        Point topRight = chartPanel.getPanelPoint(n, e);
        Point bottomLeft = chartPanel.getPanelPoint(s, w);
        Point bottomRight = chartPanel.getPanelPoint(s, e);
        
        XMLElement plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
        plot.setAttribute("id", Integer.toString(1));
        frameModule.appendChild(plot);
        XMLElement x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
        XMLElement y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
        XMLElement z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
        plot.appendChild(x);
        plot.appendChild(y);
        plot.appendChild(z);
        Text txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topLeft.x * scaleFactor)));
        x.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topLeft.y * scaleFactor)));
        y.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue("0.0");
        z.appendChild(txt);

        plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
        plot.setAttribute("id", Integer.toString(2));
        frameModule.appendChild(plot);
        x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
        y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
        z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
        plot.appendChild(x);
        plot.appendChild(y);
        plot.appendChild(z);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topRight.x * scaleFactor)));
        x.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topRight.y * scaleFactor)));
        y.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue("0.0");
        z.appendChild(txt);

        plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
        plot.setAttribute("id", Integer.toString(3));
        frameModule.appendChild(plot);
        x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
        y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
        z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
        plot.appendChild(x);
        plot.appendChild(y);
        plot.appendChild(z);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(bottomRight.x * scaleFactor)));
        x.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(bottomRight.y * scaleFactor)));
        y.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue("0.0");
        z.appendChild(txt);

        plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
        plot.setAttribute("id", Integer.toString(4));
        frameModule.appendChild(plot);
        x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
        y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
        z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
        plot.appendChild(x);
        plot.appendChild(y);
        plot.appendChild(z);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(bottomLeft.x * scaleFactor)));
        x.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(bottomLeft.y * scaleFactor)));
        y.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue("0.0");
        z.appendChild(txt);

        // Close the frame
        plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
        plot.setAttribute("id", Integer.toString(5));
        frameModule.appendChild(plot);
        x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
        y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
        z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
        plot.appendChild(x);
        plot.appendChild(y);
        plot.appendChild(z);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topLeft.x * scaleFactor)));
        x.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue(Integer.toString((int)(topLeft.y * scaleFactor)));
        y.appendChild(txt);
        txt = xmlDoc.createTextNode("txt#");
        txt.setNodeValue("0.0");
        z.appendChild(txt);
      }
      // One module per chart section         
      Iterator<List<Point>> iterator = chartPoints.iterator();
      int sectionIdx = 0;
      while (iterator.hasNext())
      {
        List section = iterator.next();
        if (section.size() > 0)
        {
          XMLElement module = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "module");
          modules.appendChild(module);
          module.setAttribute("name", Integer.toString(++sectionIdx));
          module.setAttribute("symetric", "no");
          Iterator sectionIterator = section.iterator();
          int ptIdx = 0;
          Point prevPt = null;
          
          double min = Double.MAX_VALUE, max = -min;
          double sum = 0;
          boolean avoidTooLongStrokes = true;
          while (sectionIterator.hasNext())
          {              
            Point pt = (Point)sectionIterator.next();
            // Avoid too long strokes
            if (avoidTooLongStrokes)
            {
              if (prevPt != null)
              {
                double dist = pt.distance(prevPt.x, prevPt.y);
                sum += dist;
//              if (dist > max) max = dist;
//              if (dist < min) min = dist;
                max = Math.max(max, dist);
                min = Math.min(min, dist);
                if (dist > 75D)
                {
                  prevPt = pt;
                  continue;
                }
              }
            }
            XMLElement plot = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "plot");
            plot.setAttribute("id", Integer.toString(++ptIdx));
            module.appendChild(plot);
            XMLElement x = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "x");
            XMLElement y = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "y");
            XMLElement z = (XMLElement)xmlDoc.createElementNS(ObjMaker.NAMESPACE, "z");
            plot.appendChild(x);
            plot.appendChild(y);
            plot.appendChild(z);
            Text txt = xmlDoc.createTextNode("txt#");
            txt.setNodeValue(Integer.toString((int)(pt.x * scaleFactor)));
            x.appendChild(txt);
            txt = xmlDoc.createTextNode("txt#");
            txt.setNodeValue(Integer.toString((int)(pt.y * scaleFactor)));
            y.appendChild(txt);
            txt = xmlDoc.createTextNode("txt#");
            txt.setNodeValue("0.0");
            z.appendChild(txt);
            prevPt = pt;
          }
//          System.out.println("Min:" + min + ", Max: " + max + ", Avg:" + (sum/(double)ptIdx));
        }
      }
    }            

    try
    {
      Utilities.makeSureTempExists();
      xmlDoc.print(new FileOutputStream(new File("temp" + File.separator + "chart.xml")));
      ObjMaker.generate("temp" + File.separator + "chart.xml", false);
      Point _n = new Point((int)(chartPanel.getWidth() * scaleFactor) / 2, 0);
      Point _s = new Point((int)(chartPanel.getWidth()  * scaleFactor) / 2, 
                           (int)(chartPanel.getHeight() * scaleFactor));
      Point _w = new Point(0, (int)(chartPanel.getHeight() * scaleFactor) / 2);
      Point _e = new Point((int)(chartPanel.getWidth() * scaleFactor), 
                           (int)(chartPanel.getHeight() * scaleFactor) / 2);
      List<Point> labelPts = new ArrayList<Point>(4);
      labelPts.add(_n);
      labelPts.add(_s);
      labelPts.add(_w);
      labelPts.add(_e);
    }
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }

  private final static int VALUE_FACTOR = 2500;
  
  private final static String[] KML_STYLE = new String[] { "purpleLine",
                                                           "yellowLine",
                                                           "blueLine",
                                                           "redLine",
                                                           "blackLine" };

  public final static void generateKMLGRIBFile(GribHelper.GribConditionData gribData,
                                               int[] options,
                                               ChartPanel chartPanel,
                                               BufferedWriter kmlwriter) throws Exception
  {
    boolean ok = true;
    
    double chartn = chartPanel.getNorthL();
    double charts = chartPanel.getSouthL();
    double charte = chartPanel.getEastG();
    double chartw = chartPanel.getWestG();    
    
    kmlwriter.write(
    "    <Style id=\"purpleLine\">\n" + 
    "      <LineStyle>\n" + 
    "        <color>7fff00ff</color>\n" + 
    "      </LineStyle>\n" + 
    "    </Style>\n" + 
    "    <Style id=\"yellowLine\">\n" + 
    "      <LineStyle>\n" + 
    "        <color>7f00ffff</color>\n" + 
    "      </LineStyle>\n" + 
    "    </Style>\n" + 
    "    <Style id=\"blackLine\">\n" + 
    "      <LineStyle>\n" + 
    "        <color>87000000</color>\n" + 
    "      </LineStyle>\n" + 
    "    </Style>\n" + 
    "    <Style id=\"redLine\">\n" + 
    "      <LineStyle>\n" + 
    "        <color>ff0000ff</color>\n" + 
    "      </LineStyle>\n" + 
    "    </Style>\n" + 
    "    <Style id=\"blueLine\">\n" + 
    "      <LineStyle>\n" + 
    "        <color>ffff0000</color>\n" + 
    "      </LineStyle>\n" + 
    "    </Style>\n");

    for (int i=0; i<options.length; i++)
    {
      int option = options[i];
      double minValue = Integer.MAX_VALUE;
      double maxValue = Integer.MIN_VALUE;
      for (int h=0; gribData.getGribPointData() != null && h<gribData.getGribPointData().length && ok; h++)
      {
        for (int w=0; w<gribData.getGribPointData()[h].length && ok; w++)
        {
          double lat = gribData.getGribPointData()[h][w].getLat();
          double lng = gribData.getGribPointData()[h][w].getLng();
  
          if (lat > chartn || lat < charts || !isBetween(chartw, charte, lng))
            continue;
  
          double value = 0D;
          if (option == TYPE_500MB)
            value = gribData.getGribPointData()[h][w].getHgt();
          else if (option == TYPE_PRMSL)
            value = gribData.getGribPointData()[h][w].getPrmsl() / 10D;
          else if (option == TYPE_TMP)
            value = gribData.getGribPointData()[h][w].getAirtmp();
          else if (option == TYPE_RAIN)
            value = gribData.getGribPointData()[h][w].getRain();
          else if (option == TYPE_WAVE)
          {
            double wh = gribData.getGribPointData()[h][w].getWHgt();
            if (wh > 3000 || wh < 0)
              wh = 0.01;
            value = wh / 10D;
          }
          if (value == 0 && option != TYPE_WAVE)
            ok = false;
          else
          {
            if (value < minValue) minValue = value;
            if (value > maxValue) maxValue = value;
          }
        }
      }
      if (minValue == 0 && maxValue == 0)
        ok = false; // that's for the waves
        
      gribData.wind = true;
      if (option == TYPE_500MB)
        gribData.hgt = ok;
      else if (option == TYPE_PRMSL)
        gribData.prmsl = ok;
      else if (option == TYPE_TMP)
        gribData.temp = ok;
      else if (option == TYPE_WAVE)
        gribData.wave = ok;
      else if (option == TYPE_RAIN)
        gribData.rain = ok;
      
      if (ok)
        WWContext.getInstance().fireLogging("Value of " + DATA_NAME[option] + " between " + minValue + " and " + maxValue);
      else
        WWContext.getInstance().fireLogging("No value for " + DATA_NAME[option]);
  
      if (ok)
      {
        kmlwriter.write(
        "    <Folder id=\"" + DATA_NAME[option] + "\">\n" +
        "      <name>" + DATA_NAME[option] + "</name>\n" + 
        "      <open>0</open>\n" + 
        "      <visibility>0</visibility>\n");
        for (int w=0; gribData.getGribPointData() != null && w<gribData.getGribPointData()[0].length - 1; w++)
        {
          for (int h=0; h<gribData.getGribPointData().length - 1; h++)
          {
            double lat1 = gribData.getGribPointData()[h][w].getLat();
            double lng1 = gribData.getGribPointData()[h][w].getLng();
            
            if (lat1 > chartn || lat1 < charts || !isBetween(chartw, charte, lng1))
              continue;
            
            double lat2 = gribData.getGribPointData()[h][w+1].getLat();
            double lng2 = gribData.getGribPointData()[h][w+1].getLng();
            
            double lat3 = gribData.getGribPointData()[h+1][w+1].getLat();
            double lng3 = gribData.getGribPointData()[h+1][w+1].getLng();
            
            double lat4 = gribData.getGribPointData()[h+1][w].getLat();
            double lng4 = gribData.getGribPointData()[h+1][w].getLng();
            
            double value1 = 0;
            double value2 = 0;
            double value3 = 0;
            double value4 = 0;
            if (option == TYPE_500MB)
            {
              value1 = gribData.getGribPointData()[h][w].getHgt();
              value2 = gribData.getGribPointData()[h][w+1].getHgt();
              value3 = gribData.getGribPointData()[h+1][w+1].getHgt();
              value4 = gribData.getGribPointData()[h+1][w].getHgt();
            }
            else if (option == TYPE_PRMSL)
            {
              value1 = gribData.getGribPointData()[h][w].getPrmsl() / 10D; 
              value2 = gribData.getGribPointData()[h][w+1].getPrmsl() / 10D;
              value3 = gribData.getGribPointData()[h+1][w+1].getPrmsl() / 10D;
              value4 = gribData.getGribPointData()[h+1][w].getPrmsl() / 10D;
            }
            else if (option == TYPE_TMP)
            {
              value1 = gribData.getGribPointData()[h][w].getAirtmp();
              value2 = gribData.getGribPointData()[h][w+1].getAirtmp();
              value3 = gribData.getGribPointData()[h+1][w+1].getAirtmp();
              value4 = gribData.getGribPointData()[h+1][w].getAirtmp();
            }
            else if (option == TYPE_RAIN)
            {
              value1 = gribData.getGribPointData()[h][w].getRain();
              value2 = gribData.getGribPointData()[h][w+1].getRain();
              value3 = gribData.getGribPointData()[h+1][w+1].getRain();
              value4 = gribData.getGribPointData()[h+1][w].getRain();
            }
            else if (option == TYPE_WAVE)
            {
              double wh = gribData.getGribPointData()[h][w].getWHgt();
              if (wh > 3000 || wh < 0) wh = 0.01;
              value1 = wh;
              wh = gribData.getGribPointData()[h][w+1].getWHgt();
              if (wh > 3000 || wh < 0) wh = 0.01;
              value2 = wh;
              wh = gribData.getGribPointData()[h+1][w+1].getWHgt();
              if (wh > 3000 || wh < 0) wh = 0.01;
              value3 = wh;
              wh = gribData.getGribPointData()[h+1][w].getWHgt();
              if (wh > 3000 || wh < 0) wh = 0.01;
              value4 = wh;
            }
            kmlwriter.write(
            "      <Placemark>\n" + 
            "        <name>" + DATA_NAME[option] + "</name>\n" + 
            "        <visibility>0</visibility>\n" + 
            "        <description>" + DATA_NAME[option] + " cell element</description>\n" + 
            "        <styleUrl>#" + KML_STYLE[i] + "</styleUrl>\n" +         
            "        <LineString>\n" + 
            "          <tessellate>1</tessellate>\n" + 
            "          <altitudeMode>relativeToGround</altitudeMode>\n" + 
            "          <coordinates> " + Double.toString(lng1) + "," + Double.toString(lat1) + "," + Integer.toString((int)(value1 - minValue) * VALUE_FACTOR) + "\n" + 
            "            " + Double.toString(lng2) + "," + Double.toString(lat2) + "," + Integer.toString((int)(value2 - minValue) * VALUE_FACTOR) + "\n" + 
            "            " + Double.toString(lng3) + "," + Double.toString(lat3) + "," + Integer.toString((int)(value3 - minValue) * VALUE_FACTOR) + "\n" + 
            "            " + Double.toString(lng4) + "," + Double.toString(lat4) + "," + Integer.toString((int)(value4 - minValue) * VALUE_FACTOR) + "\n" + 
            "            " + Double.toString(lng1) + "," + Double.toString(lat1) + "," + Integer.toString((int)(value1 - minValue) * VALUE_FACTOR) + " </coordinates>\n" + 
            "        </LineString>\n" +
            "     </Placemark>\n");
          }
        }
        kmlwriter.write("    </Folder>\n");

      }
    }
  }

  public static boolean isBetween(double left, double right, double lng)
  {
    double r = right, l = left, longitude = lng;
    if (Utilities.sign(left) != Utilities.sign(right) && r < 0D) // On each side of the anti-meridian
    {
      r += 360D;
      if (longitude < 0) longitude += 360D;
    }
    return (longitude > l && longitude < r);
  }
}

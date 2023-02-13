package chartview.util;

import java.text.DecimalFormat;
import calc.GeomUtil;

public class XSLUtil
{
  private final static DecimalFormat df4 = new DecimalFormat("###0.0000");
  private final static DecimalFormat df3 = new DecimalFormat("###0.000");
  private final static DecimalFormat df2 = new DecimalFormat("###0.00");
  private final static DecimalFormat df1 = new DecimalFormat("###0.0");
  
  private final static DecimalFormat dfi2 = new DecimalFormat("00");
  private final static DecimalFormat dfi3 = new DecimalFormat("000");

  public final static int absInt(double d)
  {
    return (int)Math.abs(d);
  }
  
  public final static double decimalPartInMinutes(double d)
  {
    double min = Math.abs(d) - (int)Math.abs(d);
    return min * 100d * (6d / 10d);
  }
  
  public final static String formatI2(double d)
  {
    return dfi2.format(d);
  }
  public final static String formatI3(double d)
  {
    return dfi3.format(d);
  }
  public final static String formatX4(double d)
  {
    return df4.format(d);
  }
  public final static String formatX3(double d)
  {
    return df3.format(d);
  }
  public final static String formatX2(double d)
  {
    return df2.format(d);
  }
  public final static String formatX1(double d)
  {
    return df1.format(d);
  }
  public final static String toNbsp(String s)
  {
    return s.replaceAll(" ", "&nbsp;");
  }
  public final static String initCap(String s)
  {
    String str = "";
    try
    {
      str = s.toLowerCase();
      char[] ca = str.toCharArray();
      char c = new String(new char[] { ca[0] }).toUpperCase().toCharArray()[0];
      ca[0] = c;
      str = new String(ca);
    }
    catch (Exception ignore) {}
    
    return str;
  }
  
  public final static String initAllCap(String s)
  {
    String str = "";
    try
    {
      str = s.toLowerCase();
      char[] ca = str.toCharArray();
      for (int i=0; i<ca.length; i++)
      {
        if (i == 0 || ca[i-1] == ' ')
          ca[i] = new String(new char[] { ca[i] }).toUpperCase().toCharArray()[0]; 
      }
      str = new String(ca);
    }
    catch (Exception ignore) {}    
    return str;
  }
  
  public final static String decToSex(double d, int a, int b)
  {
    return GeomUtil.decToSex(d, a, b);  
  }
  public final static String decToSex(double d, int a, int b, int c)
  {
    return GeomUtil.decToSex(d, a, b, c);  
  }
  public final static String decToSexTrunc(double d, int a, int b)
  {
    return GeomUtil.decToSex(d, a, b, true);  
  }
  
  public final static String sexToDec(String deg, String min, String sgn)
  {
//  System.out.println("Deg:" + deg + " Min:" + min + ", " + sgn);
    double d = GeomUtil.sexToDec(deg, min);
    if (sgn.equals("S") || sgn.equals("W"))
      d *= -1;
    return Double.toString(d);
  }
  
  public static void main(String... args)
  {
    System.out.println(initCap("akeu coucou"));
    System.out.println(initAllCap("akeu coucou larigou"));
    System.out.println(decimalPartInMinutes(12.25));
  }

}

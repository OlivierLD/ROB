package main;

import chartview.gui.util.param.ParamPanel;

import chartview.routing.polars.PolarHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PolarTester
{
  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
  public static String userInput(String prompt)
  {
    String retString = "";
    System.err.print(prompt);
    try
    {
      retString = stdin.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception) 
      {
        exception.printStackTrace();
      }
    }
    return retString;
  }

  public static void main(String... args)
  {
    new ParamPanel();
//  refreshCoeffs();

    System.out.println("Type Q to exit");
    System.out.println("--------------");
    boolean go = true;
    while (go)
    {
      boolean ok = true;
      String twsStr = userInput("TWS (double) : ");
      if (twsStr.equalsIgnoreCase("Q"))
      {
        go = false;
      }
      else
      {
        double tws = 0D;
        try { tws = Double.parseDouble(twsStr); } catch (Exception ex) { ok = false; }
        if (ok)
        {
          String twaStr = userInput("TWA (int)    : ");
          int twa = 0;
          try { twa = Integer.parseInt(twaStr); } catch (Exception ex) { ok = false; }
          if (ok)
          {
            double bsp = PolarHelper.getSpeed(tws, (double)twa);
            System.out.println("BSP: " + Double.toString(bsp));          
          }
        }
      }
    }
  }
}

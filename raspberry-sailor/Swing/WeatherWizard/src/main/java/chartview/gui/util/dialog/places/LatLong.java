package chartview.gui.util.dialog.places;

import chartview.ctx.WWContext;

import java.text.DecimalFormat;

import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public abstract class LatLong
{
  public final static int LAT  = 0;
  public final static int LONG = 1;

  int type = 0;
  int deg = 0;
  double min = 0.0;
  String nsew = "N";
  double value = 0.0;

  public LatLong(String str, int type) throws Exception
  {
    if (type != LAT && type != LONG)
      throw new Exception("Bad type");
    else
      this.type = type;
      
    // Parse the String here
    StringTokenizer st = new StringTokenizer(str, " ");
    
    int idx = 0;
    if (st.countTokens() != 3)
    {
      JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), "Bad format: use 12 34.56 N\ninstead of [" + str + "]", "Parsing", JOptionPane.ERROR_MESSAGE);
      throw new Exception("Bat format for " + str);
    }
    while (st.hasMoreElements())
    {
      String s = st.nextToken();
      switch (idx)
      {
        case 0:
          try
          {
            this.deg = Integer.parseInt(s);
          }
          catch (Exception e)
          {
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), "Bad degrees: use 12 34.56 N", "Parsing", JOptionPane.ERROR_MESSAGE);
            throw e;
          }
          break;
        case 1:
          try
          {
            this.min = Double.parseDouble(s);
          }
          catch (Exception e)
          {
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), "Bad minutes: use 12 34.56 N", "Parsing", JOptionPane.ERROR_MESSAGE);
            throw e;
          }
          break;
        case 2:
          if ((type == LAT && !s.equals("N") && !s.equals("S")) ||
              (type == LONG && !s.equals("E") && !s.equals("W")))
            throw new Exception("Bad sign: [" + s + "]");
          else
            nsew = s;
          break;
        default:
          break;
      }
      idx++;
    }
    this.value = deg + (min / 60);
    if (nsew.equals("S") || nsew.equals("W"))
      this.value *= (-1.0);
  }
  public String toString()
  { 
    DecimalFormat dm = new DecimalFormat("#0.##");
    return Integer.toString(deg) + " " + dm.format(min) + " " + nsew; 
  }
  public double getValue()
  { return this.value; }
}

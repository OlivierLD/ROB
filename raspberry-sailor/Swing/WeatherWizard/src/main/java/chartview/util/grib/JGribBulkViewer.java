package chartview.util.grib;

import chartview.util.WWGnlUtilities;

import chartview.util.grib.panel.BulkGribPanel;

import chartview.util.grib.panel.GribDatePanel;

import chartview.util.grib.panel.OneGRIBTablePanel;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jgrib.GribFile;
import jgrib.GribPDSParamTable;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

public class JGribBulkViewer
{
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm");
  public JGribBulkViewer(String fName) throws Exception
  {
    GribFile gribFile = new GribFile(fName);

    TimeZone tz = TimeZone.getTimeZone("127"); // "GMT + 0"
//  TimeZone.setDefault(tz);
    WWGnlUtilities.SDF.setTimeZone(tz);
    
    HashMap<GribDate, HashMap<Type, Float[][]>> bigmap = new HashMap<GribDate, HashMap<Type, Float[][]>>();
    
    for (int i=0; i<gribFile.getLightRecords().length; i++)
    {        
      try
      {
        GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
        GribRecordPDS grpds = gr.getPDS(); // Headers and Data         
        GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
        GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

        Date date     = grpds.getGMTForecastTime().getTime(); 
        int width     = grgds.getGridNX();
        int height    = grgds.getGridNY();
        double stepX  = grgds.getGridDX();
        double stepY  = grgds.getGridDY();
        double top    = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
        double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
        double left   = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
        double right  = Math.max(grgds.getGridLon1(), grgds.getGridLon2());
           
        String type        = grpds.getType();
        String description = grpds.getDescription();   
        String unit        = grpds.getUnit();

        GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);
          
        Float[][] data = new Float[height][width];
        float val = 0F;
        for (int col=0; col<width; col++)
        {
          for (int row=0; row<height; row++)
          {
            try
            {
              val = gr.getValue(col, row);
              data[row][col] = val;
            }
            catch (Exception ex)
            {
              ex.printStackTrace();
            }
          }
        }
        HashMap<Type, Float[][]> subMap = bigmap.get(gDate);
        if (subMap == null)
          subMap = new HashMap<Type, Float[][]>();
        subMap.put(new Type(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
        bigmap.put(gDate, subMap);
      }
      catch (NoValidGribException e)
      {
        e.printStackTrace();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      catch (NotSupportedException e)
      {
        e.printStackTrace();
      }
    }
    BulkGribPanel bgp = new BulkGribPanel();
    
    SortedSet<GribDate> ss = new TreeSet<GribDate>(bigmap.keySet());
    for (GribDate d : ss)
    {
      // New Date Tab
      GribDatePanel datePanel = new GribDatePanel();
      bgp.getMainTabbedPane().add(SDF.format(d.getGDate()), datePanel);
      // datePanel : more info
      datePanel.setWidth(d.getWidth());
      datePanel.setHeight(d.getHeight());
      datePanel.setStepX(d.getStepx());
      datePanel.setStepY(d.getStepy());
      datePanel.setTop(d.getTop());
      datePanel.setBottom(d.getBottom());
      datePanel.setLeft(d.getLeft());
      datePanel.setRight(d.getRight());
      
      HashMap<Type, Float[][]> dMap = bigmap.get(d);
      SortedSet<Type> type4date = new TreeSet<Type>(dMap.keySet());
//    System.out.println(d.getGDate().toString() + " : " + type4date.size() + " type(s)");
      for (Type t : type4date)
      {
        // New type tab in this date tab
//      System.out.print("  For " + d + " and type " + t + " (" + t.getDesc() + ", " + t.getUnit() + ")");
        Float[][] data = dMap.get(t);
//      System.out.println(data.length + "x" + data[0].length);
        OneGRIBTablePanel ogtp = new OneGRIBTablePanel();        
        ogtp.setData(data);
        ogtp.setMin(t.getMin());
        ogtp.setMax(t.getMax());
          
        ogtp.setText(t.getDesc() + ", " + t.getUnit() + ". min:" + Float.toString(t.getMin()) + ", max:" + Float.toString(t.getMax()));
        datePanel.getDateTabbedPane().add(t.getType(), ogtp);
      }
    }
    
    JFrame frame = new JFrame("Bulk GRIB Data");
    frame.getContentPane().add(bgp);
    frame.setSize(new Dimension(800, 600));
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if(frameSize.height > screenSize.height) frameSize.height = screenSize.height;
    if(frameSize.width > screenSize.width) frameSize.width = screenSize.width;
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2); 
    frame.addWindowListener(new WindowAdapter() 
      {
        public void windowClosing(WindowEvent e)
        {
          System.exit(0);
        }
      });
    frame.setVisible(true);

//  JOptionPane.showMessageDialog(Context.getInstance().getMasterTopFrame(), bgp, "Bulk GRIB Data", JOptionPane.PLAIN_MESSAGE);
  }

  class Type implements Comparable
  {
    private String type;
    private String desc;
    private String unit;
    
    private float min, max;
    
    public Type(String t, String d, String u, float min, float max)
    {
      type = t;
      desc = d;
      unit = u;
      this.min = min;
      this.max = max;
    }
    
    public String toString() { return type; }

    public void setType(String type)
    {
      this.type = type;
    }

    public String getType()
    {
      return type;
    }

    public int compareTo(Object o)
    {
      return this.type.compareTo(o.toString());
    }

    public void setDesc(String desc)
    {
      this.desc = desc;
    }

    public String getDesc()
    {
      return desc;
    }

    public void setUnit(String unit)
    {
      this.unit = unit;
    }

    public String getUnit()
    {
      return unit;
    }

    public void setMin(float min)
    {
      this.min = min;
    }

    public float getMin()
    {
      return min;
    }

    public void setMax(float max)
    {
      this.max = max;
    }

    public float getMax()
    {
      return max;
    }
  }
  
  
  class GribDate extends Date
  {
    private Date date;
    private int height;
    private int width;
    private double stepx;
    private double stepy;
    private double top, bottom, left, right;

    public GribDate(Date d, int h, int w, double x, double y, double t, double b, double l, double r)
    {
      super(d.getTime());
      this.date = d;
      this.height = h;
      this.width = w;
      this.stepx = x;
      this.stepy = y;
      this.left = l;
      this.right = r;
      this.top = t;
      this.bottom = b;
    }

    public void setGDate(Date date)
    {
      this.date = date;
    }

    public Date getGDate()
    {
      return date;
    }

    public void setHeight(int height)
    {
      this.height = height;
    }

    public int getHeight()
    {
      return height;
    }

    public void setWidth(int width)
    {
      this.width = width;
    }

    public int getWidth()
    {
      return width;
    }

    public void setStepx(double stepx)
    {
      this.stepx = stepx;
    }

    public double getStepx()
    {
      return stepx;
    }

    public void setStepy(double stepy)
    {
      this.stepy = stepy;
    }

    public double getStepy()
    {
      return stepy;
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
  }
}

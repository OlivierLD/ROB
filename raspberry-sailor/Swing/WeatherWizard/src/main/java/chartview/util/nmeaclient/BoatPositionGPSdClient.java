package chartview.util.nmeaclient;

import calc.GeoPoint;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

import nmea.server.datareader.CustomNMEAClient;

import ocss.gpsd.GPSdClient;
import ocss.gpsd.GPSdClientInterface;
import ocss.gpsd.GPSdUtils;

import ocss.nmea.parser.RMC;

public class BoatPositionGPSdClient implements BoatPositionClient, GPSdClientInterface
{
  WWGnlUtilities.BoatPosition bPos = null;
  private boolean ok = true;
  private Throwable problemCause = null;
  private static GPSdClient client = null;
  
  public BoatPositionGPSdClient()
  {
    final String gpsdPortNumber = (ParamPanel.data[ParamData.GPSD_PORT][ParamData.VALUE_INDEX]).toString();
    try
    {
      client = new GPSdClient(this, 
                              (ParamPanel.data[ParamData.NMEA_HOST][ParamData.VALUE_INDEX]).toString(),
                              Integer.parseInt(gpsdPortNumber), 
                              "true".equals(System.getProperty("gpsd.verbose", "false"))); // Verbose
    }
    catch (Exception e)
    {
      manageError(e);
    }
    
    Thread t = new Thread("gpsd-port-reader")
      {
        public void run()
        {
          while (bPos == null)
          {
            try { Thread.sleep(1000L); } 
            catch (Exception ex) 
            {
              System.err.println("Reading GPSd port:" + gpsdPortNumber + ":");
              ex.printStackTrace();
            }
          }
          try { client.closeClient(); } catch (Exception ex) {}
        }
      };
    t.start();
  }
  
  public void tpvRead(String data)
  {
    try 
    { 
      RMC rmc = GPSdUtils.parseTPV(data);
      bPos = new WWGnlUtilities.BoatPosition(new GeoPoint(rmc.getGp().lat, rmc.getGp().lng), (int)Math.round(rmc.getCog()));
    } 
    catch (GPSdUtils.GPSDException gpsde) 
    { 
      manageError(gpsde);
    }
  }

  public void setBoatPosition(WWGnlUtilities.BoatPosition bp)
  {
    bPos = bp;  
  }
  
  public WWGnlUtilities.BoatPosition getBoatPosition()
  {
    return bPos;
  }
  
  public void manageError(Throwable t) 
  {
    System.out.println("GPSD Error!!!:" + t.toString());
    problemCause = t;
    ok = false;
  }
  
  public boolean allIsOk()
  {
    return ok;
  }
  
  public Throwable getProblemCause()
  {
    return problemCause;
  }
}

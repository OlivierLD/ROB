package chartview.util.nmeaclient;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

import nmea.server.datareader.CustomNMEAClient;

public class BoatPositionUDPClient implements BoatPositionClient
{
  WWGnlUtilities.BoatPosition bPos = null;
  private boolean ok = true;
  private Throwable problemCause = null;
  
  public BoatPositionUDPClient()
  {
    final String udpPortNumber = (ParamPanel.data[ParamData.UDP_PORT][ParamData.VALUE_INDEX]).toString();
    final WW_NMEAReader reader = new WW_NMEAReader(false, this, CustomNMEAClient.UDP_OPTION, udpPortNumber, (ParamPanel.data[ParamData.NMEA_HOST][ParamData.VALUE_INDEX]).toString());     
//  reader.setParent(this);
    
    Thread t = new Thread("udp-port-reader")
      {
        public void run()
        {
          while (bPos == null)
          {
            try { Thread.sleep(1000L); } 
            catch (Exception ex) 
            {
              System.err.println("Reading UDP port:" + udpPortNumber + ":");
              ex.printStackTrace();
            }
          }
          try { reader.stopReader(); } catch (Exception ex) { System.err.println("UDP:" + ex.toString()); }
        }
      };
    t.start();
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
    System.out.println("UDP Error!!!:" + t.toString());
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

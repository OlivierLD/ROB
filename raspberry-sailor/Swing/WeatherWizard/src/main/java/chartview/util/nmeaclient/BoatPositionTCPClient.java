package chartview.util.nmeaclient;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

import nmea.server.datareader.CustomNMEAClient;

public class BoatPositionTCPClient implements BoatPositionClient
{
  WWGnlUtilities.BoatPosition bPos = null;
  private boolean ok = true;
  private Throwable problemCause = null;
  
  public BoatPositionTCPClient(boolean inThread)
  {
    final String tcpPortNumber = (ParamPanel.data[ParamData.TCP_PORT][ParamData.VALUE_INDEX]).toString();
    final WW_NMEAReader reader = new WW_NMEAReader(true, // verbose
                                                   this,  // Will be used to get th eposition
                                                   CustomNMEAClient.TCP_OPTION, 
                                                   tcpPortNumber, 
                                                   (ParamPanel.data[ParamData.NMEA_HOST][ParamData.VALUE_INDEX]).toString());     
//  reader.setParent(this);    
    if (inThread)
    {
      Thread t = new Thread("tcp-port-reader")
        {
          public void run()
          {
            readLoop(tcpPortNumber, reader);
          }
        };
      t.start();
    }
    else
      readLoop(tcpPortNumber, reader);
  }

  private void readLoop(String tcpPortNumber, WW_NMEAReader reader)
  {
    int i = 0;
    while (bPos == null && (i++ < 5))
    {
      System.out.println("Boat position TCP Client, looping (" + i + ").");
      try { Thread.sleep(1000L); } 
      catch (Exception ex) 
      {
        System.err.println("Reading TCP port:" + tcpPortNumber + ":");
        ex.printStackTrace();
      }
    }
    System.out.println("TCP Reading loop exited with boat position [" + (bPos == null?"": "not ") + "null]");
    try 
    { 
      reader.stopReader(); 
    } 
    catch (Exception ex) { ex.printStackTrace(); }    
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
    System.out.println("TCP Error!!!:" + t.toString());
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

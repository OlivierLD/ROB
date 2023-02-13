package chartview.util.nmeaclient;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import chartview.util.WWGnlUtilities;

public class BoatPositionSerialClient implements BoatPositionClient
{
  WWGnlUtilities.BoatPosition bPos = null;
  private boolean ok = true;
  private Throwable problemCause = null;
  
  public BoatPositionSerialClient()
  {
    final String serialPortName = (ParamPanel.data[ParamData.SERIAL_PORT][ParamData.VALUE_INDEX]).toString();
    final WW_NMEAReader reader = new WW_NMEAReader(false, this, serialPortName, 4800); 
//  reader.setParent(this);
    
    Thread t = new Thread("serial-port-reader")
      {
        public void run()
        {
          while (bPos == null)
          {
            try { Thread.sleep(1000L); } 
            catch (Exception ex) 
            {
              System.err.println("Reading Serial port:" + serialPortName + ":");
              ex.printStackTrace();
            }
          }
          try { reader.stopReader(); } catch (Exception ex) {}
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
    System.out.println("Serial Error!!!:" + t.toString());
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

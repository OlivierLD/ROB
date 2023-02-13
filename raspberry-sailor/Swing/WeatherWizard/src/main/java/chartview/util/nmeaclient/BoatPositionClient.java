package chartview.util.nmeaclient;

import chartview.util.WWGnlUtilities;

public interface BoatPositionClient
{
  public void setBoatPosition(WWGnlUtilities.BoatPosition bp);    
  public WWGnlUtilities.BoatPosition getBoatPosition();
  public void manageError(Throwable t);
  public boolean allIsOk();
  public Throwable getProblemCause();
}

package chartview.util.nmeaclient;

import chartview.util.WWGnlUtilities;

public interface BoatPositionClient {
    void setBoatPosition(WWGnlUtilities.BoatPosition bp);

    WWGnlUtilities.BoatPosition getBoatPosition();

    void manageError(Throwable t);

    boolean allIsOk();

    Throwable getProblemCause();
}

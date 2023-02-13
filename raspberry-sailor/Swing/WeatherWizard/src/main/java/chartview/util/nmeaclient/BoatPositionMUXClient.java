package chartview.util.nmeaclient;

import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;

public class BoatPositionMUXClient implements BoatPositionClient {
    WWGnlUtilities.BoatPosition bPos = null;
    private boolean ok = true;
    private Throwable problemCause = null;

    public BoatPositionMUXClient() {
    }

    public void setBoatPosition(WWGnlUtilities.BoatPosition bp) {
        bPos = bp;
    }

    public WWGnlUtilities.BoatPosition getBoatPosition() {
        return bPos;
    }

    public void manageError(Throwable t) {
        System.out.println("Dummy Error!!!:" + t.toString());
        problemCause = t;
        ok = false;
    }

    public boolean allIsOk() {
        return ok;
    }

    public Throwable getProblemCause() {
        return problemCause;
    }
}

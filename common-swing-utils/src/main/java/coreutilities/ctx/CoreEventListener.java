package coreutilities.ctx;

import java.util.EventListener;
import java.util.List;

public abstract class CoreEventListener implements EventListener {
    public void updateCompleted(List<String> fList) {}
    public void networkOk(boolean b) {}
    public void headingHasChanged(int hdg) {}
}

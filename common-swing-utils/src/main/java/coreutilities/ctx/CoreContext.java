package coreutilities.ctx;

import java.util.ArrayList;
import java.util.List;

public class CoreContext {
    private static CoreContext context = null;
    private List<CoreEventListener> applicationListeners = null;

    private CoreContext() {
        applicationListeners = new ArrayList<CoreEventListener>(2); // 2: Initial Capacity
    }

    public static synchronized CoreContext getInstance() {
        if (context == null)
            context = new CoreContext();
        return context;
    }

    public void release() {
        context = null;
        System.gc();
    }

    public List<CoreEventListener> getListeners() {
        return applicationListeners;
    }

    public synchronized void addApplicationListener(CoreEventListener l) {
        if (!this.getListeners().contains(l)) {
            this.getListeners().add(l);
            //  System.out.println("Now having " + Integer.toString(this.getListeners().size()) + " listener(s)");
        }
    }

    public synchronized void removeApplicationListener(CoreEventListener l) {
        this.getListeners().remove(l);
    }

    public void fireUpdateCompleted(List<String> fList) {
        for (int i = 0; i < this.getListeners().size(); i++) {
            CoreEventListener l = this.getListeners().get(i);
            l.updateCompleted(fList);
        }
    }

    public void fireNetworkOk(boolean b) {
        for (int i = 0; i < this.getListeners().size(); i++) {
            CoreEventListener l = this.getListeners().get(i);
            l.networkOk(b);
        }
    }

    public void fireHeadingHasChanged(int hdg) {
        for (int i = 0; i < this.getListeners().size(); i++) {
            CoreEventListener l = this.getListeners().get(i);
            l.headingHasChanged(hdg);
        }
    }
}

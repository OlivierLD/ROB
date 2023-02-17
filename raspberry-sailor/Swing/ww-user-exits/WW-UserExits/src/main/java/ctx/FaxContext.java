package ctx;

import oracle.xml.parser.v2.DOMParser;

import java.util.ArrayList;
import java.util.List;

public class FaxContext {
    private static FaxContext context = null;
    private transient List<FaxEventListener> applicationListeners = null;
    public DOMParser parser = null;

    private FaxContext() {
        applicationListeners = new ArrayList<FaxEventListener>(2); // 2: Initial Capacity
        parser = new DOMParser();
    }

    public static synchronized FaxContext getInstance() {
        if (context == null) {
            context = new FaxContext();
        }
        return context;
    }

    public void release() {
        context = null;
        parser = null;
        System.gc();
    }

    public List<FaxEventListener> getListeners() {
        return applicationListeners;
    }

    public synchronized void addApplicationListener(FaxEventListener l) {
        if (!this.getListeners().contains(l)) {
            this.getListeners().add(l);
            //    System.out.println("Now having " + Integer.toString(this.getListeners().size()) + " listener(s)");
        }
    }

    public synchronized void removeApplicationListener(FaxEventListener l) {
        this.getListeners().remove(l);
    }

    public void fireUpdatePrms(float z, float eyeD) {
        for (int i = 0; i < this.getListeners().size(); i++) {
            FaxEventListener l = this.getListeners().get(i);
            l.updatePrms(z, eyeD);
        }
    }
}

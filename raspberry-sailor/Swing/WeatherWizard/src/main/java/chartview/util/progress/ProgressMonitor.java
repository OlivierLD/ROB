package chartview.util.progress;

import chartview.ctx.WWContext;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ProgressMonitor // implements Serializable
{
    // current is not used for now.
    private int total, current = -1;
    private final boolean indeterminate;
    private String status;

    //private transient Vector<ChangeListener> listeners = new Vector<ChangeListener>();
    private final List<ChangeListener> listeners = new ArrayList<>();
    private List<ChangeListener> cll = Collections.synchronizedList(listeners);
    private final ChangeEvent ce; // = new ChangeEvent(this);

    public ProgressMonitor(int total, boolean indeterminate) {
        //  System.out.println("ProgressMonitor Constructor, total:" + total);
        this.total = total;
        this.indeterminate = indeterminate;
        this.ce = new ChangeEvent(this);
    }

    public int getTotal() {
        return total;
    }

    public void start(String status) {
        if (current != -1) {
			throw new IllegalStateException("not started yet");
		}
        this.status = status;
        current = 0;
        synchronized (this) {
            // cll = Collections.synchronizedList(listeners);
            // synchronized (cll)
            {
                fireChangeEvent();
            }
        }
    }

    public int getCurrent() {
        return current;
    }

    public String getStatus() {
        return status;
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }

    public void setCurrent(String status, int current) {
        if (current == -1) {
			throw new IllegalStateException("not started yet");
		} else if (WWContext.getDebugLevel() >= 3) {
			System.out.println("Setting current to " + current);
		}

        synchronized (this) {
            this.current = current;
            if (status != null) {
				this.status = status;
			}
            //    cll = Collections.synchronizedList(listeners);
            //    synchronized (cll)
            {
                fireChangeEvent();
            }
        }
    }

    public synchronized void addChangeListener(final ChangeListener listener) {
        Thread t = new Thread("listener-adder") {
            public void run() {
                if (WWContext.getDebugLevel() >= 3) {
					System.out.println("*** in ProgressMonitor: Adding a " + listener.getClass().getName() + " (" + listener.toString() + ") " + "...addChangeListeners   :" + listeners.size()
							+ " item(s)");
				}
                synchronized (cll) {
                    cll = Collections.synchronizedList(listeners);
                    //    listeners.add(listener);
                    cll.add(listener);
                    //    System.out.println("in ProgressMonitor: Adding a " + listener.getClass().getName() + " (" + listener.toString() + ") " + "...addChangeListeners   :" + listeners.size() + " item(s)");
                }
            }
        };
        t.start();
    }

    public synchronized void removeChangeListener(final ChangeListener listener) {
        Thread t = new Thread("listener-remover") {
            public void run() {
                if (WWContext.getDebugLevel() >= 3) {
					System.out.println("*** in ProgressMonitor: Remove request a " + listener.getClass().getName() + " (" + listener.toString() + ") ...removeChangeListeners:" + listeners.size()
							+ " item(s)");
				}
                synchronized (cll) {
                    cll = Collections.synchronizedList(listeners);
                    cll.remove(listener);
                    //    System.out.println("in ProgressMonitor: Removing a " + listener.getClass().getName() + " (" + listener.toString() + ") ...removeChangeListeners:" + listeners.size() + " item(s)");
                }
                if (WWContext.getDebugLevel() >= 3) {
					System.out.println("*** RemoveChangeListener completed");
				}
            }
        };
        t.start();
    }

    public synchronized void removeAllListeners() {
        Thread t = new Thread("all-listener-remover") {
            public void run() {
                synchronized (cll) {
                    cll = Collections.synchronizedList(listeners);
                    while (cll.size() > 0)
                        cll.remove(0);
                }
            }
        };
        t.start();
    }

    private synchronized void fireChangeEvent() {
        /* Java bug? (ConcurrentModificationException) */

        Thread t = new Thread("event-firer") {
            public void run() {
                int index = 0;
                int lsnrSize = 0;
                cll = Collections.synchronizedList(listeners);
                synchronized (cll) {
                    lsnrSize = cll.size();
                    if (WWContext.getDebugLevel() >= 3) {
						System.out.println("---- fireChangeEvent requested on " + lsnrSize + " ChangeListener(s)");
					}
                    try {
                        for (ChangeListener cl : cll) {
                            if (WWContext.getDebugLevel() >= 3) {
								System.out.println("       ChangeListener[" + index + "] is " + (cl != null ? "not " : "") + "null");
							}
                            if (cl != null) {
                                cl.stateChanged(ce);
                                if (WWContext.getDebugLevel() >= 3) {
									System.out.println("     ... stateChanged on ChangeListener[" + index + "] OK.");
								}
                            }
                            index++;
                        }
                        if (WWContext.getDebugLevel() >= 3) {
							System.out.println("---- fireChangeEvent Completed on " + lsnrSize + " listener(s).");
						}
                    } catch (ConcurrentModificationException cme) {
                        System.err.println("!! Index " + index + "/" + lsnrSize + ", ConcurrentModificationException (2) in ProgressMonitor, Ooops!");
                        System.err.println("-------------------------");
                        cme.printStackTrace();
                        System.err.println("-------------------------");
                    }
                }
            }
        };
        t.start();
    }
}

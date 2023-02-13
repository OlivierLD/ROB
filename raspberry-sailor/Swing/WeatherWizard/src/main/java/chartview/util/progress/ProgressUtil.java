package chartview.util.progress;

import chartview.ctx.WWContext;

import chartview.gui.AdjustFrame;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressUtil
{
  static class MonitorListener
    implements ChangeListener, ActionListener
  {
    ProgressMonitor monitor;
    Window owner;
    Timer timer;
    boolean showInterruptButton = true;
    String buttonLabel = null;

    public MonitorListener(Window owner, ProgressMonitor monitor, boolean showButton)
    {
      this(owner, monitor, showButton, "hide");
    }
    
    public MonitorListener(Window owner, ProgressMonitor monitor, boolean showButton, String label)
    {
      synchronized (monitor)
      {
        this.owner = owner;
        this.monitor = monitor;
        this.showInterruptButton = showButton;
        this.buttonLabel = label;
      }
    }

    public MonitorListener(Window owner, ProgressMonitor monitor)
    {
      this(owner, monitor, true);
    }

    public String toString()
    {
      return "from ProgressUtil, MonitorListener";
    }
    
    public void stateChanged(ChangeEvent ce)
    {
      ProgressMonitor monitor = (ProgressMonitor)ce.getSource();
      synchronized (monitor)
      {
        if (monitor.getCurrent() != monitor.getTotal())
        {
          if (timer == null)
          {
//          System.out.println("... Creating Timer.");
            int millisecondToWait = 500; // Will show the dialog after this amount of time, if not completed...
            timer = new Timer(millisecondToWait, this);
            timer.setRepeats(false);
            timer.start();
          }
        }
        else
        {
          if (timer != null && timer.isRunning())
            timer.stop();
          monitor.removeChangeListener(this);
        }
      }
    }

    public void actionPerformed(ActionEvent e)
    {
//    synchronized (monitor) // That one generate some kind of dead lock...
      {
        monitor.removeChangeListener(this);
        ProgressDialog dlg = owner instanceof Frame ? 
                                new ProgressDialog((Frame) owner, monitor, showInterruptButton, buttonLabel) : 
                                new ProgressDialog((Dialog) owner, monitor, showInterruptButton, buttonLabel);
        dlg.pack();
        dlg.setLocationRelativeTo(((AdjustFrame)WWContext.getInstance().getMasterTopFrame()));
        dlg.setVisible(true);
      }
    }
  }

  public static ProgressMonitor createModalProgressMonitor(Component owner, int total, boolean indeterminate)
  {
    return createModalProgressMonitor(owner, total, indeterminate, true);
  }
  
  public static ProgressMonitor createModalProgressMonitor(Component owner, int total, boolean indeterminate, boolean showInterrupt)
  {
    return createModalProgressMonitor(owner, total, indeterminate, showInterrupt, "hide");
  }

  public static ProgressMonitor createModalProgressMonitor(Component owner, int total, boolean indeterminate, boolean showInterrupt, String buttonLabel)
  {
    ProgressMonitor monitor = new ProgressMonitor(total, indeterminate);
    Window window = owner instanceof Window ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
    monitor.addChangeListener(new MonitorListener(window, monitor, showInterrupt, buttonLabel));
    return monitor;
  }
}


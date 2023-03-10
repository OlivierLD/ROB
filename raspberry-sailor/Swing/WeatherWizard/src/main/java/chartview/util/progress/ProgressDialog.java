package chartview.util.progress;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;


public class ProgressDialog
        extends JDialog
        implements ChangeListener {
    private final JLabel statusLabel = new JLabel();
    private JProgressBar progressBar = new JProgressBar();
    private ProgressMonitor monitor;
    private boolean showButton = true;

    public ProgressDialog(Frame owner, ProgressMonitor monitor, boolean showButton)
            throws HeadlessException {
        this(owner, monitor, showButton, "hide");
    }

    public ProgressDialog(Frame owner, ProgressMonitor monitor, boolean showButton, String bcl)
            throws HeadlessException {
        super(owner, "Progress", true);
        this.showButton = showButton;
        init(monitor, bcl);
    }

    public ProgressDialog(Frame owner, ProgressMonitor monitor)
            throws HeadlessException {
        this(owner, monitor, true);
    }

    public ProgressDialog(Dialog owner, ProgressMonitor monitor)
            throws HeadlessException {
        super(owner);
        init(monitor);
    }

    public ProgressDialog(Dialog owner, ProgressMonitor monitor, boolean showButton)
            throws HeadlessException {
        this(owner, monitor, showButton, "hide");
    }

    public ProgressDialog(Dialog owner, ProgressMonitor monitor, boolean showButton, String bcl)
            throws HeadlessException {
        super(owner);
        this.showButton = showButton;
        init(monitor, bcl);
    }

    public String toString() {
        return "JDialog ProgressDialog";
    }

    private void init(ProgressMonitor monitor) {
        init(monitor, "hide");
    }

    private void init(ProgressMonitor monitor, String buttonCodeLabel) {
        synchronized (this) {
            this.monitor = monitor;

            progressBar = new JProgressBar(0, monitor.getTotal());
            progressBar.setPreferredSize(new Dimension(300, 20));
            if (monitor.isIndeterminate())
                progressBar.setIndeterminate(true);
            else
                progressBar.setValue(monitor.getCurrent());
            statusLabel.setText(monitor.getStatus());

            JPanel contents = (JPanel) getContentPane();
            contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contents.setLayout(new BorderLayout());
            contents.add(statusLabel, BorderLayout.NORTH);
            contents.add(progressBar, BorderLayout.CENTER);
            if (showButton) {
                JPanel bottomPanel = new JPanel();
                JButton interruptButton = new JButton(WWGnlUtilities.buildMessage(buttonCodeLabel));
                bottomPanel.add(interruptButton, null);
                contents.add(bottomPanel, BorderLayout.SOUTH);
                interruptButton.addActionListener(e -> {
                    //  System.out.println("Canceling");
                    WWContext.getInstance().fireInterruptProgress();
                    removeMe();
                    WWContext.getInstance().removeApplicationListener(WWContext.getInstance().getAel4monitor());
                    dispose();
                });
            }
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            synchronized (this) {
//      synchronized (this.monitor)
                {
                    this.monitor.addChangeListener(this);
                }
            }
        }
    }

    private void removeMe() {
        this.monitor.removeChangeListener(this);
    }

    public void stateChanged(final ChangeEvent ce) {
        // to ensure EDT thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> stateChanged(ce));
            return;
        }

        if (monitor.getCurrent() != monitor.getTotal()) // Not finished yet
        {
            statusLabel.setText(monitor.getStatus());
            if (!monitor.isIndeterminate())
                progressBar.setValue(monitor.getCurrent());
        } else {
            this.monitor.removeChangeListener(this); // Finished
            WWContext.getInstance().removeApplicationListener(WWContext.getInstance().getAel4monitor());
            dispose();
        }
    }
}

package weatherwizard.userexits;

import chartadjustmentuserexits.PAppletFrame;
import chartview.ctx.WWContext;
import chartview.gui.right.CommandPanel;
import chartview.util.UserExitException;
import chartview.util.UserExitInterface;
import grib.processing.util.GRIBUtil;
import jgrib.GribFile;
import processing.core.PApplet;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class GRIBProcessing
        implements UserExitInterface {
    private List<String> feedback = null;
    private PAppletFrame frame = null;

    public GRIBProcessing() {
        super();
    }

    public boolean isAvailable(CommandPanel commandPanel, WWContext ctx) {
        boolean available = true;
        if (ctx.getGribFile() == null) {
          available = false;
        }
        return available;
    }

    public boolean userExitTask(CommandPanel cp, final WWContext ctx)
            throws UserExitException {
        // 1 - Generate file
        GribFile gribFile = ctx.getGribFile();
        try {
            GRIBUtil.buildGRIBMap(gribFile, "grib.txt", true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2 - Display PApplet
        boolean ok = true;
        if (false) {
            // Standalone, closes the application when shut down.
            PApplet.main(new String[] {"--bgcolor=#ece9d8", "chartadjustmentuserexits.ProcessingGRIBPApplet"});
        } else {
            frame = new PAppletFrame();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.stopPApplet();
                    e.getComponent().setVisible(false);
        //          System.out.println("Notifying...");
                    if (feedback != null) {
                        feedback.add("Success!");
                    }
        //          synchronized (parent) { parent.notify(); }
                }
            });
        //  frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            frame.setVisible(true);
        }
        return ok;
    }

    public List<String> getFeedback() {
        return feedback;
    }
}
package weatherwizard.userexits;

import chartview.ctx.WWContext;
import chartview.gui.right.CommandPanel;
import chartview.util.ImageUtil;
import chartview.util.UserExitException;
import chartview.util.UserExitInterface;
import chartview.util.grib.GribHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows how to use the UserExit feature to write on the file system the
 * images reworked by the Weather Wizard program (made transparent, with the black
 * replaced by a color).
 */
public class SimpleSample
        implements UserExitInterface {
    List<String> feedback = null;

    public SimpleSample() {
        super();
    }

    public boolean isAvailable(CommandPanel commandPanel, WWContext ctx) {
        boolean available = true;
        if (commandPanel.getFaxImage() == null) {
          available = false;
        }
        return available;
    }

    public boolean userExitTask(CommandPanel cp, WWContext ctx)
            throws UserExitException {
        boolean ok = true;
        feedback = new ArrayList<String>(1);
        try {
            GribHelper.GribConditionData[] gribData = cp.getGribData();
            System.out.println("GribData:" + gribData.length + " frame(s).");

            CommandPanel.FaxImage[] fi = cp.getFaxImage();
            System.out.println("Faxes:" + fi.length + " image(s)");
            for (int i = 0; i < fi.length; i++) {
                System.out.println(" - " + fi[i].fileName);
                // Saving the image
                Image img = fi[i].faxImage;
                try {
                    ImageUtil.writeImageToFile(img, "png", "C:\\Documents and Settings\\olediour\\Desktop\\" + "img" + i + ".png");
                } catch (Exception ex) {
                    System.err.println("Writing image file:");
                    ex.printStackTrace();
                    ok = false;
                    feedback.add(ex.toString());
                }
            }
            if (ok) {
                feedback.add("Successfully generated " + fi.length + " images.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
            feedback.add(ex.toString());
        }
        return ok;
    }

    public List<String> getFeedback() {
        return feedback;
    }
}

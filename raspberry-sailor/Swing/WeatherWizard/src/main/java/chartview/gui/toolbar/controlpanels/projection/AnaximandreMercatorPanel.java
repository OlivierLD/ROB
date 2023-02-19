package chartview.gui.toolbar.controlpanels.projection;

import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class AnaximandreMercatorPanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JLabel jLabel1 = new JLabel();

    public AnaximandreMercatorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        jLabel1.setText(WWGnlUtilities.buildMessage("nothing-specific"));
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
        jLabel1.setEnabled(false);
        this.add(jLabel1, BorderLayout.CENTER);
    }
}

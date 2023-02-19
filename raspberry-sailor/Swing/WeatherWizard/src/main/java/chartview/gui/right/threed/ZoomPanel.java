package chartview.gui.right.threed;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class ZoomPanel
        extends JPanel {
    private final JLabel jLabel1 = new JLabel();
    private final JTextField zoomTextField = new JTextField();

    public ZoomPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        jLabel1.setText("Zoom:");
        zoomTextField.setText("1.0");
        zoomTextField.setSize(new Dimension(40, 20));
        zoomTextField.setPreferredSize(new Dimension(40, 20));
        zoomTextField.setHorizontalAlignment(JTextField.CENTER);
        zoomTextField.addActionListener(e -> zoomTextField_actionPerformed(e));
        this.add(jLabel1, null);
        this.add(zoomTextField, null);

        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public String toString() {
                return "from ZoomPanel.";
            }

            public void setZoom3D(double d) {
                zoomTextField.setText(Double.toString(d));
            }
        });
    }

    private void zoomTextField_actionPerformed(ActionEvent e) {
        try {
            double d = Double.parseDouble(zoomTextField.getText());
            WWContext.getInstance().fireZoom3D(d);
        } catch (Exception ex) {
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
    }
}
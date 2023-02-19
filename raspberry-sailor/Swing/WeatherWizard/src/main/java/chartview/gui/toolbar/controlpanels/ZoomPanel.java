package chartview.gui.toolbar.controlpanels;

import chartview.ctx.WWContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class ZoomPanel
        extends JPanel {
    private final ButtonCommandPanel parent;
    private final JButton zoomOutButton = new JButton();
    private final JButton zoomInButton = new JButton();

    protected boolean shiftDown = false;

    public ZoomPanel(ButtonCommandPanel bcp) {
        parent = bcp;
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    private void jbInit() {
        //  jButton1.setText("jButton1");
        this.setSize(new Dimension(60, 30));
        this.setPreferredSize(new Dimension(60, 30));
        this.setMinimumSize(new Dimension(60, 30));
        this.setMaximumSize(new Dimension(60, 30));
        zoomOutButton.setIcon(new ImageIcon(this.getClass().getResource("img/zoomout.gif")));
        // jButton2.setText("jButton2");
        zoomOutButton.setPreferredSize(new Dimension(24, 24));
        zoomOutButton.setBorderPainted(false);
        zoomOutButton.setMaximumSize(new Dimension(24, 24));
        zoomOutButton.setMinimumSize(new Dimension(24, 24));
        zoomOutButton.setMargin(new Insets(1, 1, 1, 1));
        zoomOutButton.addActionListener(this::zoomOutButton_actionPerformed);
        zoomOutButton.setToolTipText("Shift: x1.1");
        zoomInButton.setIcon(new ImageIcon(this.getClass().getResource("img/zoomin.gif")));
        zoomInButton.setPreferredSize(new Dimension(24, 24));
        zoomInButton.setBorderPainted(false);
        zoomInButton.setMaximumSize(new Dimension(24, 24));
        zoomInButton.setMinimumSize(new Dimension(24, 24));
        zoomInButton.setMargin(new Insets(1, 1, 1, 1));
        zoomInButton.addActionListener(this::zoomInButton_actionPerformed);
        zoomInButton.setToolTipText("Shift: x1.1");
        this.add(zoomOutButton, null);
        this.add(zoomInButton, null);
    }

    private void zoomOutButton_actionPerformed(ActionEvent e) {
        shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
        parent.zoomOut();
    }

    private void zoomInButton_actionPerformed(ActionEvent e) {
        shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0);
        parent.zoomIn();
    }

    public void setEnabled(boolean b) {
        zoomInButton.setEnabled(b);
        zoomOutButton.setEnabled(b);
    }
}

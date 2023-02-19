package chartview.gui.right;


import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;


public abstract class CompositeTabComponent
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JLabel tabTitleLabel = new JLabel();
    private final JLabel closeTabButton = new JLabel();

    private String tabTitle = "Composite";
    private String iconLocation = ".";

    public CompositeTabComponent(String tt, String iconLocation) {
        this.tabTitle = tt;
        this.iconLocation = iconLocation;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit()
            throws Exception {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(200, 25));
        tabTitleLabel.setText(tabTitle);
//  try { System.out.println("Resource:" + this.getClass().getResource(".")); } catch (Exception ignore) {}
        URL imgLoc = this.getClass().getResource(iconLocation);
        if (imgLoc != null) {
          closeTabButton.setIcon(new ImageIcon(imgLoc));
        } else {
          closeTabButton.setText("X");
        }
        closeTabButton.setPreferredSize(new Dimension(24, 24));
        closeTabButton.setToolTipText(WWGnlUtilities.buildMessage("close"));
        closeTabButton.setEnabled(false);
        closeTabButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeTabButton_mouseEntered(e);
            }

            public void mouseExited(MouseEvent e) {
                closeTabButton_mouseExited(e);
            }

            public void mouseClicked(MouseEvent e) {
                onClose();
            }
        });
        this.add(tabTitleLabel, BorderLayout.WEST);
        this.add(new JLabel(" "), BorderLayout.CENTER);
        this.add(closeTabButton, BorderLayout.EAST);
    }

    public void setTabTitle(String s) {
        this.tabTitle = s;
        this.tabTitleLabel.setText(s);
    }

    public abstract void onClose();

    public abstract boolean ok2Close();

    private void closeTabButton_mouseEntered(MouseEvent e) {
        if (ok2Close()) {
          closeTabButton.setEnabled(true);
        }
    }

    private void closeTabButton_mouseExited(MouseEvent e) {
        closeTabButton.setEnabled(false);
    }
}

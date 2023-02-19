package chartview.gui.util.dialog;

import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO Remove it?
public class UpdatePanel
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JLabel topLabel = new JLabel();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JPanel bottomPanel = new JPanel();
    private final JPanel centerBottomPanel = new JPanel();
    private final JPanel bottomBottomPanel = new JPanel();
    private final JTextArea jTextArea = new JTextArea();
    private final JLabel bottomLeft = new JLabel();
    private final JLabel bottomRight = new JLabel();
    private final BorderLayout borderLayout2 = new BorderLayout();
    private final BorderLayout borderLayout3 = new BorderLayout();
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel bottomLabel1 = new JLabel();
    private final JLabel bottomLabel2 = new JLabel();

    public UpdatePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        topLabel.setText(" - ");
        jScrollPane1.setMaximumSize(new Dimension(400, 240));
        jScrollPane1.setPreferredSize(new Dimension(400, 239));
        jScrollPane1.setMinimumSize(new Dimension(400, 239));
        bottomPanel.setLayout(borderLayout3);
        centerBottomPanel.setLayout(gridBagLayout1);
        bottomBottomPanel.setLayout(borderLayout2);
        jTextArea.setEditable(false);
        jTextArea.setBackground(SystemColor.control);
        bottomLeft.setText(WWGnlUtilities.buildMessage("check-update-nature"));
        bottomRight.setText("<html><u>The Weather Wizard History Page</u></html>");
        bottomRight.setForeground(Color.blue);
        bottomRight.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                url_mouseClicked(e);
            }

            public void mouseEntered(MouseEvent e) {
                bottomRight.setForeground(WWGnlUtilities.PURPLE);
                bottomRight.repaint();
            }

            public void mouseExited(MouseEvent e) {
                bottomRight.setForeground(Color.blue);
                bottomRight.repaint();
            }
        });

        bottomLabel1.setText(" ");
        bottomLabel2.setText(" ");
        this.add(topLabel, BorderLayout.NORTH);
        jScrollPane1.getViewport().add(jTextArea, null);
        this.add(jScrollPane1, BorderLayout.CENTER);
        bottomBottomPanel.add(bottomLeft, BorderLayout.WEST);
        bottomBottomPanel.add(bottomRight, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        centerBottomPanel.add(bottomLabel1,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        centerBottomPanel.add(bottomLabel2,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 5, 0), 0, 0));
        bottomPanel.add(centerBottomPanel, BorderLayout.CENTER);
        bottomPanel.add(bottomBottomPanel, BorderLayout.SOUTH);
    }

    private void url_mouseClicked(MouseEvent e) {
//  try { Utilities.openInBrowser("http://donpedro.lediouris.net/software/structure/datafiles/news/index.html"); } 
        try {
            Utilities.openInBrowser("http://code.google.com/p/weatherwizard/wiki/WWHistory?ts=1337007410&updated=WWHistory");
        } catch (Exception ignore) {
        }
    }

    public void setFileList(String str) {
        jTextArea.setText(str);
    }

    public void setTopLabel(String str) {
        topLabel.setText(str);
    }

    public void setBottomLabel1(String str) {
        bottomLabel1.setText(str);
    }

    public void setBottomLabel2(String str) {
        bottomLabel2.setText(str);
    }
}

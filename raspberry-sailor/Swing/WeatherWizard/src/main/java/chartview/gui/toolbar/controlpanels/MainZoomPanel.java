package chartview.gui.toolbar.controlpanels;


import chartview.ctx.WWContext;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class MainZoomPanel
        extends JPanel {
    private boolean centerPanelVisible = true;

    private final JPanel centerPanel = new JPanel();

    private final JLabel zoomFactorLabel = new JLabel();
    private final JLabel lngIncrementLabel = new JLabel();
    private final JTextField zfTextField = new JTextField();
    private final JTextField lgIncTextField = new JTextField();

    private final JLabel faxIncrLabel = new JLabel();
    private final JTextField faxIncTextField = new JTextField();

    public MainZoomPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
//  this.remove(centerPanel);
        this.repaint();
    }

    private void jbInit() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 90));
        this.setMinimumSize(new Dimension(ControlPane.WIDTH, 90));
        this.setSize(new Dimension(ControlPane.WIDTH, 90));
        centerPanel.setLayout(new GridBagLayout());
        if (centerPanelVisible) {
          this.add(centerPanel, BorderLayout.CENTER);
        }
        // this.setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT_COLLAPSED));
        zoomFactorLabel.setText(WWGnlUtilities.buildMessage("zoom-factor"));
        lngIncrementLabel.setText(WWGnlUtilities.buildMessage("lg-incr"));
        zfTextField.setPreferredSize(new Dimension(40, 20));
        zfTextField.setHorizontalAlignment(JTextField.CENTER);
        lgIncTextField.setPreferredSize(new Dimension(40, 20));
        lgIncTextField.setToolTipText(WWGnlUtilities.buildMessage("in-degrees"));
        lgIncTextField.setHorizontalAlignment(JTextField.CENTER);
        faxIncrLabel.setText(WWGnlUtilities.buildMessage("fax-incr"));
        faxIncrLabel.setToolTipText(WWGnlUtilities.buildMessage("in-pixels"));
        faxIncTextField.setPreferredSize(new Dimension(40, 20));
        faxIncTextField.setHorizontalAlignment(JTextField.CENTER);
        faxIncTextField.setToolTipText(WWGnlUtilities.buildMessage("in-pixels"));
        centerPanel.add(zoomFactorLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        centerPanel.add(lngIncrementLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        centerPanel.add(zfTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        centerPanel.add(lgIncTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        centerPanel.add(faxIncrLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        centerPanel.add(faxIncTextField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

//  this.setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT_COLLAPSED));
//  this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT_COLLAPSED));

        this.validate();
    }

    public void setLatLongInc(double d) {
        lgIncTextField.setText(Double.toString(d));
    }

    public void setFaxInc(int i) {
        faxIncTextField.setText(Integer.toString(i));
    }

    public void setZoomFactor(double d) {
        zfTextField.setText(Double.toString(d));
    }

    public double getZoomFactor() throws NumberFormatException {
        return Double.parseDouble(zfTextField.getText());
    }

    public double getLatLongInc() throws Exception {
        return Double.parseDouble(lgIncTextField.getText());
    }

    public int getFaxInc() throws Exception {
        return Integer.parseInt(faxIncTextField.getText());
    }
}

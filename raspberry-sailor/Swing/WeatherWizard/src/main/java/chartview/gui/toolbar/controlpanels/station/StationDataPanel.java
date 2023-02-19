package chartview.gui.toolbar.controlpanels.station;

import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;


public class StationDataPanel
        extends JPanel {
    private final GridBagLayout gridBagLayout1 = new GridBagLayout();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel twdLabel = new JLabel();
    private final JLabel jLabel3 = new JLabel();
    private final JLabel twsLabel = new JLabel();
    private final JLabel jLabel5 = new JLabel();
    private final JLabel twaLabel = new JLabel();
    private final JLabel jLabel2 = new JLabel();
    private final JLabel bspLabel = new JLabel();
    private final JLabel jLabel4 = new JLabel();
    private final JLabel jLabel6 = new JLabel();
    private final JLabel awsLabel = new JLabel();
    private final JLabel awaLabel = new JLabel();

    public StationDataPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(gridBagLayout1);
        this.setSize(new Dimension(60, 149));
        jLabel1.setText("TWS");
        jLabel1.setFont(new Font("Tahoma", Font.BOLD, 10));
        twsLabel.setText("00");
        jLabel3.setText("TWD");
        jLabel3.setFont(new Font("Tahoma", Font.BOLD, 10));
        twdLabel.setText("00");
        jLabel5.setText("TWA");
        jLabel5.setFont(new Font("Tahoma", Font.BOLD, 10));
        twaLabel.setText("00");
        jLabel2.setText("BSP");
        jLabel2.setFont(new Font("Tahoma", Font.BOLD, 10));
        bspLabel.setText("00.00");
        jLabel4.setText("AWS");
        jLabel4.setFont(new Font("Tahoma", Font.BOLD, 10));
        jLabel6.setText("AWA");
        jLabel6.setFont(new Font("Tahoma", Font.BOLD, 10));
        awsLabel.setText("00");
        awaLabel.setText("00");
        this.add(jLabel1,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(twsLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(twdLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel5,
                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(twaLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2,
                new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));
        this.add(bspLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 3, 0, 0), 0, 0));
        this.add(jLabel6, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 3, 0, 0), 0, 0));
        this.add(awsLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 3, 0, 0), 0, 0));
        this.add(awaLabel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 3, 0, 0), 0, 0));
    }

    public void setTWS(float tws) {
        twsLabel.setText(WWGnlUtilities.DF2.format(tws));
    }

    public void setAWS(float aws) {
        awsLabel.setText(WWGnlUtilities.DF2.format(aws));
    }

    public void setTWD(int twd) {
        twdLabel.setText(Integer.toString(twd));
    }

    public void setTWA(int twa) {
        if (twa < 0) {
          twa += 360;
        }
        // There is a minus sign on the side the wind comes from
        if (twa > 0 && twa < 180) {
          twaLabel.setText(Integer.toString(twa) + " -");
        } else if (twa > 180 && twa < 360) {
          twaLabel.setText("- " + Integer.toString(360 - twa));
        } else if (twa == 0) {
          twaLabel.setText("0");
        } else if (twa == 180) {
          twaLabel.setText("180");
        } else {
          twaLabel.setText(Integer.toString(twa));
        }
    }

    public void setAWA(int awa) {
        if (awa < 0) {
          awa += 360;
        }
        // There is a minus sign on the side the wind comes from
        if (awa > 0 && awa < 180) {
          awaLabel.setText(Integer.toString(awa) + " -");
        } else if (awa > 180 && awa < 360) {
          awaLabel.setText("- " + Integer.toString(360 - awa));
        } else if (awa == 0) {
          awaLabel.setText("0");
        } else if (awa == 180) {
          awaLabel.setText("180");
        } else {
          awaLabel.setText(Integer.toString(awa));
        }
    }

    public void setBSP(float bsp) {
        bspLabel.setText(WWGnlUtilities.XX22.format(bsp));
    }
}

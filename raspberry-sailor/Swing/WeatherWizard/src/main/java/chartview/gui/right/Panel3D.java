package chartview.gui.right;

import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.right.threed.ThreeDPanel;
import chartview.gui.right.threed.ZoomPanel;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Panel3D
        extends JPanel {
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final ThreeDPanel threeDPanel = new ThreeDPanel(null,
            1.0f,
            new Color(240, 240, 245),
            (Color) ParamPanel.data[ParamData.CHART_COLOR][ParamData.VALUE_INDEX],
            Color.blue, // Text (N, S, E, W)
            Color.red); // Other points (not used for now)
    private final ZoomPanel zoompanel = new ZoomPanel();
    private final JCheckBox twsCheckBox = new JCheckBox();
    private final JCheckBox prmslCheckBox = new JCheckBox();
    private final JCheckBox hgt500CheckBox = new JCheckBox();
    private final JCheckBox wavesCheckBox = new JCheckBox();
    private final JCheckBox temperatureCheckBox = new JCheckBox();
    private final JCheckBox rainCheckBox = new JCheckBox();

    private final transient ApplicationEventListener ael = new ApplicationEventListener() {
        public String toString() {
            return "from Panel3D.";
        }

        public void newTWSObj(List<Point> al) {
            twsCheckBox.setEnabled(true);
        }

        public void new500mbObj(List<Point> al) {
            hgt500CheckBox.setEnabled(true);
        }

        public void newPrmslObj(List<Point> al) {
            prmslCheckBox.setEnabled(true);
        }

        public void newTmpObj(List<Point> al) {
            temperatureCheckBox.setEnabled(true);
        }

        public void newWaveObj(List<Point> al) {
            wavesCheckBox.setEnabled(true);
        }

        public void newRainObj(List<Point> al) {
            rainCheckBox.setEnabled(true);
        }

        public void noTWSObj() {
            twsCheckBox.setSelected(false);
            twsCheckBox.setEnabled(false);
        }

        public void no500mbObj() {
            hgt500CheckBox.setSelected(false);
            hgt500CheckBox.setEnabled(false);
        }

        public void noPrmslObj() {
            prmslCheckBox.setSelected(false);
            prmslCheckBox.setEnabled(false);
        }

        public void noTmpObj() {
            temperatureCheckBox.setSelected(false);
            temperatureCheckBox.setEnabled(false);
        }

        public void noWaveObj() {
            wavesCheckBox.setSelected(false);
            wavesCheckBox.setEnabled(false);
        }

        public void noRainObj() {
            rainCheckBox.setSelected(false);
            rainCheckBox.setEnabled(false);
        }
    };

    public Panel3D() {
        WWContext.getInstance().addApplicationListener(ael);

        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    public void removeListener() {
        WWContext.getInstance().removeApplicationListener(ael);
    }

    private void jbInit() {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(620, 361));
        twsCheckBox.setText("tws");
        twsCheckBox.setEnabled(false);
        twsCheckBox.addActionListener(this::twsCheckBox_actionPerformed);
        prmslCheckBox.setText("prmsl");
        prmslCheckBox.setEnabled(false);
        prmslCheckBox.addActionListener(this::prmslCheckBox_actionPerformed);
        hgt500CheckBox.setText("500mb");
        hgt500CheckBox.setEnabled(false);
        hgt500CheckBox.addActionListener(this::hgt500CheckBox_actionPerformed);
        wavesCheckBox.setText("waves");
        wavesCheckBox.setEnabled(false);
        wavesCheckBox.addActionListener(this::wavesCheckBox_actionPerformed);
        temperatureCheckBox.setText("temperature");
        temperatureCheckBox.setEnabled(false);
        temperatureCheckBox.addActionListener(this::temperatureCheckBox_actionPerformed);
        rainCheckBox.setText("precipitation");
        rainCheckBox.setEnabled(false);
        rainCheckBox.addActionListener(this::rainCheckBox_actionPerformed);
        this.add(threeDPanel, BorderLayout.CENTER);
        zoompanel.add(twsCheckBox, null);
        zoompanel.add(prmslCheckBox, null);
        zoompanel.add(hgt500CheckBox, null);
        zoompanel.add(wavesCheckBox, null);
        zoompanel.add(temperatureCheckBox, null);
        zoompanel.add(rainCheckBox, null);
        this.add(zoompanel, BorderLayout.SOUTH);
    }

    public ThreeDPanel getThreeDPanel() {
        return threeDPanel;
    }

    private void twsCheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireTWSDisplayed(twsCheckBox.isSelected());
    }

    private void prmslCheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().firePRMSLDisplayed(prmslCheckBox.isSelected());
    }

    private void hgt500CheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fire500MBDisplayed(hgt500CheckBox.isSelected());
    }

    private void wavesCheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireWAVESDisplayed(wavesCheckBox.isSelected());
    }

    private void temperatureCheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireTEMPDisplayed(temperatureCheckBox.isSelected());
    }

    private void rainCheckBox_actionPerformed(ActionEvent e) {
        WWContext.getInstance().fireRAINDisplayed(rainCheckBox.isSelected());
    }
}
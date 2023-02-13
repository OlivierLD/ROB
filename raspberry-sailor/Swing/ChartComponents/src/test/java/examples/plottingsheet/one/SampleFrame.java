package examples.plottingsheet.one;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {
    private BorderLayout borderLayout;
    private PlottingSheetImpl psi;

    public SampleFrame() {
        borderLayout = new BorderLayout();
        psi = new PlottingSheetImpl(this);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        getContentPane().setLayout(borderLayout);
        setSize(new Dimension(600, 600));
        setTitle("Plotting Sheet");
        getContentPane().add(psi, BorderLayout.CENTER);
    }
}

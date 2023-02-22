package examples.mercatorscale;

import javax.swing.*;
import java.awt.*;

public class SampleFrame extends JFrame {
	private final BorderLayout borderLayout;
	private final PlottingSheetImpl psi;

	public SampleFrame() {
		borderLayout = new BorderLayout();
		psi = new PlottingSheetImpl(this);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() {
		getContentPane().setLayout(borderLayout);
		setSize(new Dimension(620, 720));
		setTitle("Plotting Sheet");
		getContentPane().add(psi, BorderLayout.CENTER);
	}
}

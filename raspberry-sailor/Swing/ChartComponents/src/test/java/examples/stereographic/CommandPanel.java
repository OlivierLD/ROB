package examples.stereographic;

import chart.components.ui.ChartPanel;
import chart.components.ui.ChartPanelInterface;
import chart.components.ui.ChartPanelParentInterface;
import chart.components.util.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EventObject;
//import javax.swing.JViewport;


public class CommandPanel extends JPanel
        implements ChartPanelParentInterface {

    private BorderLayout borderLayout1;
    private JScrollPane jScrollPane1;
    private ChartPanel chartPanel;
    private JPanel bottomPanel;
    private JButton zoomInButton;
    private JButton zoomOutButton;

    public CommandPanel() {
        borderLayout1 = new BorderLayout();
        jScrollPane1 = new JScrollPane();
        chartPanel = new ChartPanel(this);
        bottomPanel = new JPanel();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        setLayout(borderLayout1);
        this.setBackground(new Color(174, 220, 211));
        zoomInButton.setText("Zoom In");
        zoomInButton.addActionListener(e -> jButton1_actionPerformed(e));
        zoomOutButton.setText("Zoom Out");
        zoomOutButton.addActionListener(e -> jButton2_actionPerformed(e));
        jScrollPane1.getViewport().add(chartPanel, null);
        add(jScrollPane1, BorderLayout.CENTER);
        bottomPanel.add(zoomInButton, null);
        bottomPanel.add(zoomOutButton, null);
        add(bottomPanel, BorderLayout.SOUTH);
//    double nLat = 120D;
//    double sLat = -120D;
//    double wLong = -110D;
//    double eLong = 115D;

        // Big one, from top to bottom
        double nLat = 120D;
        double sLat = -120D;
        double wLong = -130D;
        double eLong = 100D;

//    double nLat = 90D;
//    double sLat = 25D;
//    double wLong = -110D;
//    double eLong = 45D;

        chartPanel.setGridColor(Color.red);
        chartPanel.setChartColor(Color.blue);

        chartPanel.setEastG(eLong);
        chartPanel.setWestG(wLong);
        chartPanel.setNorthL(nLat);
        chartPanel.setSouthL(sLat);
        chartPanel.setHorizontalGridInterval(10D);
        chartPanel.setVerticalGridInterval(10D);

        chartPanel.setProjection(ChartPanelInterface.STEREOGRAPHIC);
    }

    private void jButton1_actionPerformed(ActionEvent e) {
        chartPanel.zoomIn();
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        chartPanel.zoomOut();
    }

    public void chartPanelPaintComponent(Graphics gr) {
        World.drawChart(chartPanel, gr);
    }

    public boolean onEvent(EventObject eventobject, int i) {
        return true;
    }

    public String getMessForTooltip() {
        return null;
    }

    public boolean replaceMessForTooltip() {
        return false;
    }

    public void videoCompleted() {
    }

    public void videoFrameCompleted(Graphics g, Point p) {
    }

    public void zoomFactorHasChanged(double d) {
    }

    public void chartDDZ(double top, double bottom, double left, double right) {
    }
}

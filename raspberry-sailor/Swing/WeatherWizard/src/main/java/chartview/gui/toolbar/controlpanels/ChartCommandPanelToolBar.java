package chartview.gui.toolbar.controlpanels;


import chart.components.ui.ChartPanel;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.gui.right.CommandPanel;
import chartview.gui.right.CompositeTabbedPane;
import chartview.gui.util.param.ParamData;
import chartview.gui.util.param.ParamPanel;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Date;


public class ChartCommandPanelToolBar
        extends JToolBar {
    private final JButton zoomInButton = new JButton();
    private final JButton zoomOutButton = new JButton();
    private final JButton reloadButton = new JButton();
    private final JButton resetZoomButton = new JButton();

    private final JPanel extraComponentHolder = new JPanel(new BorderLayout());
    private final JPanel radioButtonHolder = new JPanel(new FlowLayout());
    private final JPanel expandCollapseHolder = new JPanel(new FlowLayout());

    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JRadioButton ddRadioButton = new JRadioButton();
    private final JRadioButton grabRadioButton = new JRadioButton();
    private final JRadioButton crossRadioButton = new JRadioButton();
    private final JRadioButton pencilRadioButton = new JRadioButton();
    private final JRadioButton arrowRadioButton = new JRadioButton();

    private final JCheckBox documentDate = new JCheckBox();
    private final JButton nowGRIBButton = new JButton();
    private final JButton routingButton = new JButton();

    private final JButton expandCollapseControlButton = new JButton();
    private final JButton scrollThruOpenTabsButton = new JButton();
    private boolean controlExpanded = (Boolean) ParamPanel.data[ParamData.EXPAND_CONTROLS_BY_DEFAULT][ParamData.VALUE_INDEX];

    public static final int DD_ZOOM = 0;
    public static final int GRAB_SCROLL = 1;
    public static final int CROSS_HAIR_CURSOR = 2;
    public static final int PENCIL_CURSOR = 3;
    public static final int REGULAR_CURSOR = 4; // Keep this one the last one

    private int grab = DD_ZOOM;

    private final transient ApplicationEventListener ael = new ApplicationEventListener() {
        public String toString() {
            return "from ChartCommandPanelToolBar.";
        }

        public void toggleGrabScroll(int shape) {
            grab = shape;
            switch (shape) {
                case DD_ZOOM:
                    ddRadioButton.setSelected(true);
                    break;
                case GRAB_SCROLL:
                    grabRadioButton.setSelected(true);
                    break;
                case CROSS_HAIR_CURSOR:
                    crossRadioButton.setSelected(true);
                    break;
                case PENCIL_CURSOR:
                    pencilRadioButton.setSelected(true);
                    break;
                case REGULAR_CURSOR:
                    arrowRadioButton.setSelected(true);
                    break;
                default:
                    break;
            }
        }

        public void setOpenTabNum(int i) {
            scrollThruOpenTabsButton.setEnabled(i > 1);
        }

        public void gribLoaded() {
            setGRIBButtons();
        }

        public void clickOnChart() {
            setGRIBButtons();
        }
    };

    public ChartCommandPanelToolBar() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
        this.repaint();
    }

    public void removeListener() {
        WWContext.getInstance().removeApplicationListener(ael);
    }

    private void jbInit() {
        WWContext.getInstance().addApplicationListener(ael);
        zoomInButton.setIcon(new ImageIcon(this.getClass().getResource("img/zoomin.gif")));
        zoomInButton.setToolTipText(WWGnlUtilities.buildMessage("fax-zoom-in"));
        zoomInButton.setActionCommand("zoomIn");
        zoomInButton.setPreferredSize(new Dimension(24, 24));
        zoomInButton.setBorderPainted(false);
        zoomInButton.addActionListener(this::zoomInButton_actionPerformed);
        zoomOutButton.setIcon(new ImageIcon(this.getClass().getResource("img/zoomout.gif")));
        zoomOutButton.setToolTipText(WWGnlUtilities.buildMessage("fax-zoom-out"));
        zoomOutButton.setPreferredSize(new Dimension(24, 24));
        zoomOutButton.setBorderPainted(false);
        zoomOutButton.addActionListener(this::zoomOutButton_actionPerformed);
        reloadButton.setIcon(new ImageIcon(this.getClass().getResource("img/refresh.png")));
        reloadButton.setToolTipText(WWGnlUtilities.buildMessage("load-reload-defaut") + " (Ctrl+R)");
        reloadButton.setPreferredSize(new Dimension(24, 24));
        reloadButton.setBorderPainted(false);
        final String compositeName = ((ParamPanel.DataFile) ParamPanel.data[ParamData.LOAD_COMPOSITE_STARTUP][ParamData.VALUE_INDEX]).toString();
        reloadButton.setEnabled(compositeName.trim().length() > 0); // TODO Event, if that name is modified
        reloadButton.addActionListener(e -> WWContext.getInstance().fireLoadDynamicComposite(compositeName));
        resetZoomButton.setIcon(new ImageIcon(this.getClass().getResource("img/reset.gif")));
        resetZoomButton.setToolTipText(WWGnlUtilities.buildMessage("reset-zoom"));
        resetZoomButton.setPreferredSize(new Dimension(24, 24));
        resetZoomButton.setBorderPainted(false);
        //  resetZoomButton.setEnabled(compositeName.trim().length() > 0);
        resetZoomButton.addActionListener(e -> {
            JFrame jf = WWContext.getInstance().getMasterTopFrame();
            // System.out.println("MasterTopFrame is a " + jf.getClass().getName());
            CommandPanel cp = ((CompositeTabbedPane) ((AdjustFrame) jf).getMasterTabPane().getSelectedComponent()).getCommandPanel();
            ChartPanel chartPanel = cp.getChartPanel();
            chartPanel.setH(cp.getHeight());
            chartPanel.setWidthFromChart(chartPanel.getNorthL(), chartPanel.getSouthL(), chartPanel.getWestG(), chartPanel.getEastG());
            chartPanel.repaint();
        });

        expandCollapseControlButton.setIcon(new ImageIcon(this.getClass().getResource("img/monitors.png")));
        expandCollapseControlButton.setToolTipText(WWGnlUtilities.buildMessage("collapse"));
        expandCollapseControlButton.setPreferredSize(new Dimension(24, 24));
        expandCollapseControlButton.setBorderPainted(false);
        expandCollapseControlButton.addActionListener(e -> {
            controlExpanded = !controlExpanded;
            if (controlExpanded) {
              expandCollapseControlButton.setToolTipText(WWGnlUtilities.buildMessage("collapse"));
            } else {
              expandCollapseControlButton.setToolTipText(WWGnlUtilities.buildMessage("expand"));
            }
            expandCollapseButton_actionPerformed(e);
        });

        scrollThruOpenTabsButton.setIcon(new ImageIcon(this.getClass().getResource("img/clustering.png")));
        scrollThruOpenTabsButton.setToolTipText(WWGnlUtilities.buildMessage("scroll-thru-tabs"));
        scrollThruOpenTabsButton.setPreferredSize(new Dimension(24, 24));
        scrollThruOpenTabsButton.setBorderPainted(false);
        scrollThruOpenTabsButton.setEnabled(false);
        scrollThruOpenTabsButton.addActionListener(e -> WWContext.getInstance().fireScrollThroughTabs());

        this.add(zoomInButton);
        this.add(zoomOutButton);
        this.add(reloadButton);
        //  this.add(resetZoomButton);
        this.add(routingButton, null);
        this.add(nowGRIBButton, null);
        routingButton.setEnabled(false);
        nowGRIBButton.setEnabled(false);

        this.add(extraComponentHolder);
        extraComponentHolder.add(radioButtonHolder, BorderLayout.WEST);
        expandCollapseHolder.add(scrollThruOpenTabsButton);
        expandCollapseHolder.add(expandCollapseControlButton);
        extraComponentHolder.add(expandCollapseHolder, BorderLayout.EAST);

        radioButtonHolder.add(ddRadioButton, null);
        radioButtonHolder.add(grabRadioButton, null);
        radioButtonHolder.add(crossRadioButton, null);
        radioButtonHolder.add(pencilRadioButton, null);
        radioButtonHolder.add(arrowRadioButton, null);

        radioButtonHolder.add(documentDate, null);

        buttonGroup.add(ddRadioButton);
        buttonGroup.add(grabRadioButton);
        buttonGroup.add(crossRadioButton);
        buttonGroup.add(pencilRadioButton);
        buttonGroup.add(arrowRadioButton);

        ddRadioButton.setSelected(true);
        grabRadioButton.setSelected(false);
        crossRadioButton.setSelected(false);
        pencilRadioButton.setSelected(false);
        arrowRadioButton.setSelected(false);

        ddRadioButton.setText("<html><img src='" + this.getClass().getResource("img/ddz.png").toString() + "'></html>");
        grabRadioButton.setText("<html><img src='" + this.getClass().getResource("img/grab.png").toString() + "'></html>");
        crossRadioButton.setText("<html><img src='" + this.getClass().getResource("img/ch.png").toString() + "'></html>");
        pencilRadioButton.setText("<html><img src='" + ChartPanel.class.getResource("crayon.16x16.png").toString() + "'></html>");
        arrowRadioButton.setText("<html><img src='" + this.getClass().getResource("img/arrow.png").toString() + "'></html>");

        documentDate.setText(WWGnlUtilities.buildMessage("with-date"));
        documentDate.setToolTipText(WWGnlUtilities.buildMessage("with-date-tt"));

//  nowGRIBButton.setText("GRIB Now");
        nowGRIBButton.setIcon(new ImageIcon(this.getClass().getResource("img/down.png")));
        nowGRIBButton.setToolTipText("Position the GRIB on current date"); // TRANSLATE
        nowGRIBButton.setPreferredSize(new Dimension(24, 24));
        nowGRIBButton.setBorderPainted(false);

        routingButton.setIcon(new ImageIcon(this.getClass().getResource("img/navigation.png")));
        routingButton.setToolTipText(WWGnlUtilities.buildMessage("routing"));
        routingButton.setPreferredSize(new Dimension(24, 24));
        routingButton.setBorderPainted(false);
        routingButton.addActionListener(e -> {
            // System.out.println("Routing!!!");
            ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().calculateRouting();
        });

        ddRadioButton.setToolTipText(WWGnlUtilities.buildMessage("set-to-dd"));
        grabRadioButton.setToolTipText(WWGnlUtilities.buildMessage("set-to-gs"));
        crossRadioButton.setToolTipText(WWGnlUtilities.buildMessage("set-to-ch"));
        arrowRadioButton.setToolTipText(WWGnlUtilities.buildMessage("set-to-rp"));
        pencilRadioButton.setToolTipText(WWGnlUtilities.buildMessage("set-to-pencil"));

        ddRadioButton.addActionListener(e -> newCursorSelected());
        grabRadioButton.addActionListener(e -> newCursorSelected());
        crossRadioButton.addActionListener(e -> newCursorSelected());
        arrowRadioButton.addActionListener(e -> newCursorSelected());
        pencilRadioButton.addActionListener(e -> newCursorSelected());

        documentDate.addActionListener(e -> WWContext.getInstance().fireSetWithCompositeDocumentDate(documentDate.isSelected()));

        nowGRIBButton.addActionListener(e -> {
            int gribIndex = 0;
            GribHelper.GribConditionData[] currentGRIB = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getGribData();
            Date now = new Date();
            if (currentGRIB != null) {
                for (GribHelper.GribConditionData gribData : currentGRIB) {
                    Date gribDate = gribData.getDate();
                    if (gribDate.after(now)) {
                        gribIndex--;
                        break;
                    } else {
                      gribIndex++;
                    }
                }
            }
            WWContext.getInstance().fireGribIndex(gribIndex < 0 ? 0 : gribIndex);
        });
        this.validate();
    }

    private final static double SHIFT_ZOOM_FACTOR = 1.1;

    private void zoomInButton_actionPerformed(ActionEvent e) {
        double scale = 1;
        int mask = e.getModifiers();
        if ((mask & MouseEvent.SHIFT_MASK) != 0) {
          scale = SHIFT_ZOOM_FACTOR;
        }
        //  System.out.println("ZoomIn requested");
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.allLayerZoomIn(scale);
        }
    }

    private void zoomOutButton_actionPerformed(ActionEvent e) {
        double scale = 1;
        int mask = e.getModifiers();
        if ((mask & MouseEvent.SHIFT_MASK) != 0) {
          scale = SHIFT_ZOOM_FACTOR;
        }
        //  System.out.println("ZoomOut requested");
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.allLayerZoomOut(scale);
        }
    }

    private void expandCollapseButton_actionPerformed(ActionEvent e) {
        //  System.out.println("ZoomOut requested");
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.collapseExpandToolBar(controlExpanded);
        }
    }

    private void newCursorSelected() {
        if (ddRadioButton.isSelected()) {
          grab = DD_ZOOM;
        } else if (grabRadioButton.isSelected()) {
          grab = GRAB_SCROLL;
        } else if (crossRadioButton.isSelected()) {
          grab = CROSS_HAIR_CURSOR;
        } else if (arrowRadioButton.isSelected()) {
          grab = REGULAR_CURSOR;
        } else if (pencilRadioButton.isSelected()) {
          grab = PENCIL_CURSOR;
        }
        WWContext.getInstance().fireSetCursor(grab);
    }

    public void paintComponent(Graphics g) {
//  System.out.println("PaintComponent invoked on toolbar");     
        setGRIBButtons();
    }

    public void setGRIBButtons() {
        GribHelper.GribConditionData[] currentGRIB = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getGribData();
        boolean displayButton = (currentGRIB != null);
        if (((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getFrom() != null &&
                ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel().getTo() != null &&
                currentGRIB != null) {
          routingButton.setEnabled(true);
        } else {
          routingButton.setEnabled(false);
        }
        Date now = new Date();
        if (currentGRIB != null) {
            displayButton = false;
            for (GribHelper.GribConditionData gribData : currentGRIB) {
                Date gribDate = gribData.getDate();
//      System.out.println("GRIB Date:" + gribDate.toString() + ", now is " + now.toString());  
                if (gribDate.after(now)) {
                  displayButton = true;
                }
            }
        }
        nowGRIBButton.setEnabled(displayButton);
    }
}

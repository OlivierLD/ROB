package chartview.gui.toolbar.controlpanels;


import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.util.panels.GRIBVisualPanel;
import chartview.routing.enveloppe.custom.RoutingPoint;
import chartview.routing.enveloppe.custom.RoutingUtil;
import chartview.util.WWGnlUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ControlPane
        extends JPanel {
    public final static int WIDTH = 220;

    JPanel componentHolder = new JPanel(new GridBagLayout());

    private final MainZoomPanel mainZoomPanel = new MainZoomPanel();
    private final SelectFaxPanel selectFaxPanel = new SelectFaxPanel();

    private final ButtonCommandPanel imageCommandPanel = new ButtonCommandPanel() {
        public void fireUp() {
            imageUp();
        }

        public void fireDown() {
            imageDown();
        }

        public void fireLeft() {
            imageLeft();
        }

        public void fireRight() {
            imageRight();
        }

        public void fireZoomIn() {
            imageZoomin();
        }

        public void fireZoomOut() {
            imageZoomout();
        }
    };


    private final ButtonCommandPanel chartCommandPanel = new ButtonCommandPanel() {
        public void fireUp() {
            chartUp();
        }

        public void fireDown() {
            chartDown();
        }

        public void fireLeft() {
            chartLeft();
        }

        public void fireRight() {
            chartRight();
        }

        public void fireZoomIn() {
            chartZoomin();
        }

        public void fireZoomOut() {
            chartZoomout();
        }
    };
    private final ProjectionPanel projectionPanel = new ProjectionPanel();
    private final RotationPanel rotationPanel = new RotationPanel();
    private final GribPanel gribPanel = new GribPanel();
    private final LoggingPanel loggingPanel = new LoggingPanel();
    private final FaxPreviewPanel faxPreviewPanel = new FaxPreviewPanel();
    private final RoutingPanel routingPanel = new RoutingPanel();
    private final GRIBVisualPanel gribVisualPanel = new GRIBVisualPanel();
    private final ThumbnailPanel thumbnailPanel = new ThumbnailPanel();

    private final JPanel chartControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel faxControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel gribControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel projectionControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel routingControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel faxPreviewControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel logControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel gribVisualControlPanelHolder = new JPanel(new BorderLayout());
    private final JPanel thumbnailControlPanelHolder = new JPanel(new BorderLayout());

    private SingleControlPane faxControl = null;
    private SingleControlPane chartControl = null;
    private SingleControlPane gribControl = null;
    private SingleControlPane projectionControl = null;
    private SingleControlPane faxPreviewControl = null;
    private SingleControlPane routingPreviewControl = null;
    private SingleControlPane longControl = null;
    private SingleControlPane gribVisualControl = null;
    private SingleControlPane thumbnailControl = null;

    private final transient ApplicationEventListener ael = new ApplicationEventListener() {
        public String toString() {
            return "from ChartControlPane.";
        }

        public void faxLoaded() {
            imageCommandPanel.setEnabled(true);
            selectFaxPanel.setEnabled(true);
            rotationPanel.setEnabled(true);
            faxControl.setEnabled(true);
        }

        public void faxUnloaded() {
            imageCommandPanel.setEnabled(false);
            selectFaxPanel.setEnabled(false);
            rotationPanel.setEnabled(false);
            faxControl.setEnabled(false);
        }

        public void gribLoaded() {
            gribControl.setEnabled(true);
            gribVisualControl.setEnabled(true);
        }

        public void gribUnloaded() {
            gribControl.setEnabled(false);
            WWContext.getInstance().setGribFile(null);
            gribVisualControl.setEnabled(false);
        }

        public void routingAvailable(boolean b, List<RoutingPoint> bestRoute) {
//          System.out.println("Routing is " + (b?"":"not ") + "available");
            routingPreviewControl.setEnabled(b);
            routingPanel.setBestRoute(bestRoute, RoutingUtil.REAL_ROUTING);
        }

        public void routingForecastAvailable(boolean b, List<RoutingPoint> route) {
            routingPreviewControl.setEnabled(b);
            routingPanel.setBestRoute(route, RoutingUtil.WHAT_IF_ROUTING);
        }
    };

    public ControlPane() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {
        this.setLayout(new BorderLayout());
//  this.setSize(new Dimension(238, 300));

        int panelIdx = 0;
        componentHolder.add(mainZoomPanel, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Chart Controls
        JPanel intermediatePanel = new JPanel(new GridBagLayout());
        intermediatePanel.add(chartCommandPanel, new GridBagConstraints(0, 0, 0, 0, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

//  chartControlPanelHolder.add(chartCommandPanel, BorderLayout.CENTER);
        chartControlPanelHolder.add(intermediatePanel, BorderLayout.CENTER);
        chartControl = new SingleControlPane(WWGnlUtilities.buildMessage("chart-control"), chartControlPanelHolder, false);
        componentHolder.add(chartControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Projection Controls
        projectionControlPanelHolder.add(projectionPanel, BorderLayout.CENTER);
        projectionControl = new SingleControlPane(WWGnlUtilities.buildMessage("projection-control"), projectionControlPanelHolder, false);
        componentHolder.add(projectionControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Fax Controls
        JPanel intermediateFaxPanel = new JPanel();
        intermediateFaxPanel.setLayout(new BorderLayout());
        intermediateFaxPanel.add(selectFaxPanel, BorderLayout.NORTH);
        selectFaxPanel.setEnabled(false);
        imageCommandPanel.setSize(110, 80);
        JPanel imageCommandPanelHolder = new JPanel();
        imageCommandPanelHolder.setLayout(new GridBagLayout());
        imageCommandPanelHolder.add(imageCommandPanel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        intermediateFaxPanel.add(imageCommandPanelHolder, BorderLayout.CENTER);
        faxControlPanelHolder.add(intermediateFaxPanel, BorderLayout.CENTER);
        imageCommandPanel.setEnabled(false);
        rotationPanel.setEnabled(false);
        faxControlPanelHolder.add(rotationPanel, BorderLayout.SOUTH);

        faxControl = new SingleControlPane(WWGnlUtilities.buildMessage("fax-control"), faxControlPanelHolder, false);
        componentHolder.add(faxControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        faxControl.setEnabled(false);

        // Fax Preview
        faxPreviewControlPanelHolder.add(faxPreviewPanel, BorderLayout.CENTER);
        faxPreviewControl = new SingleControlPane(WWGnlUtilities.buildMessage("fax-preview"), faxPreviewControlPanelHolder, false);
        componentHolder.add(faxPreviewControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // GRIB Controls
        gribControlPanelHolder.add(gribPanel, BorderLayout.CENTER);
        gribControl = new SingleControlPane(WWGnlUtilities.buildMessage("grib-control"), gribControlPanelHolder, false);
        componentHolder.add(gribControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        gribControl.setEnabled(false);

        // GRIB Visual Controls
        gribVisualControlPanelHolder.add(gribVisualPanel, BorderLayout.CENTER);
        gribVisualControl = new SingleControlPane(WWGnlUtilities.buildMessage("grib-visual-control"), gribVisualControlPanelHolder, false) {
            @Override
            protected void onClickOnControl(boolean displayingData) {
//        System.out.println("GRIB Details at the mouse are " + (displayingData?"visible":"not displayed"));
                WWContext.getInstance().fireGribAtTheMouseisShowing(displayingData);
            }
        };
        componentHolder.add(gribVisualControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        gribVisualControl.setEnabled(false);

        // Routing Controls
        routingControlPanelHolder.add(routingPanel, BorderLayout.CENTER);
        routingPreviewControl = new SingleControlPane(WWGnlUtilities.buildMessage("routing-details"), routingControlPanelHolder, false);
        componentHolder.add(routingPreviewControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        routingPreviewControl.setEnabled(false);

        // Logging Controls
        logControlPanelHolder.add(loggingPanel, BorderLayout.CENTER);
        longControl = new SingleControlPane(WWGnlUtilities.buildMessage("log-control"), logControlPanelHolder, false);
        componentHolder.add(longControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Thumbnail
        thumbnailControlPanelHolder.add(thumbnailPanel, BorderLayout.CENTER);
        thumbnailControl = new SingleControlPane(WWGnlUtilities.buildMessage("thumbnail"), thumbnailControlPanelHolder, false);
        componentHolder.add(thumbnailControl, new GridBagConstraints(0, panelIdx++, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // Done with the toolbar
        this.add(componentHolder, BorderLayout.NORTH);

        WWContext.getInstance().addApplicationListener(ael);
    }

    public void removeListener() {
        WWContext.getInstance().removeApplicationListener(ael);
    }

    public MainZoomPanel getMainZoomPanel() {
        return mainZoomPanel;
    }

    private void imageUp() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageUp(imageCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void imageDown() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageDown(imageCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void imageLeft() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageLeft(imageCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void imageRight() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageRight(imageCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void imageZoomin() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageZoomin(imageCommandPanel.shiftDown ? 1.1 : 1);
        }
    }

    private void imageZoomout() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.imageZoomout(imageCommandPanel.shiftDown ? 1.1 : 1);
        }
    }

    private void chartUp() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartUp(chartCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void chartDown() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartDown(chartCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void chartLeft() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartLeft(chartCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void chartRight() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartRight(chartCommandPanel.shiftDown ? 10 : 1);
        }
    }

    private void chartZoomin() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartZoomin(chartCommandPanel.shiftDown ? 1.1 : 1);
        }
    }

    private void chartZoomout() {
        for (int i = 0; i < WWContext.getInstance().getListeners().size(); i++) {
            ApplicationEventListener l = WWContext.getInstance().getListeners().get(i);
            l.chartZoomout(chartCommandPanel.shiftDown ? 1.1 : 1);
        }
    }

    public ProjectionPanel getProjectionPanel() {
        return projectionPanel;
    }
}

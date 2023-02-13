package chartview.gui.toolbar.controlpanels;

import calc.GeoPoint;
import calc.GreatCircle;
import calc.GreatCirclePoint;
import chartview.ctx.ApplicationEventListener;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.gui.right.CommandPanel;
import chartview.gui.toolbar.controlpanels.station.BSPDisplay;
import chartview.gui.toolbar.controlpanels.station.StationDataPanel;
import chartview.gui.toolbar.controlpanels.station.WindGaugePanel;
import chartview.gui.toolbar.controlpanels.station.WindVanePanel;
import chartview.routing.enveloppe.custom.RoutingPoint;
import chartview.routing.enveloppe.custom.RoutingUtil;
import chartview.util.WWGnlUtilities;
import coreutilities.Utilities;
import coreutilities.gui.HeadingPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("serial")
public class RoutingPanel extends JPanel {
    private int current_routing_mode = -1;

    private int twa = 0;
    private int twd = 0;
    private float tws = 0F;
    private int hdg = 0;
    private float bsp = 0.0F;
    private Date date = null;
    private transient GeoPoint position = null;

    private List<RoutingPoint> bestRoute = null;

    private GridBagLayout gridBagLayoutOne = new GridBagLayout();
    private GridBagLayout gridBagLayoutTwo = new GridBagLayout();

    private JRadioButton trueWindRadioButton = new JRadioButton(WWGnlUtilities.buildMessage("true-wind"));
    private JRadioButton appWindRadioButton = new JRadioButton(WWGnlUtilities.buildMessage("app-wind"));
    private JPanel trueAppPanel = new JPanel();
    private ButtonGroup group = new ButtonGroup();

    private WindVanePanel windVanePanel = new WindVanePanel();
    private WindGaugePanel windGaugePanel = new WindGaugePanel();
    private StationDataPanel stationDataPanel = new StationDataPanel();
    private BSPDisplay bspDisplay = new BSPDisplay();
    private HeadingPanel headingPanel = new HeadingPanel(true); // with glossy effect

    private JTabbedPane tabbedPane = new JTabbedPane();
    private JPanel stepPanel = new JPanel();
    private JPanel summaryPanelHolder = new JPanel();
    private JPanel summaryPanel = new JPanel();

    private JLabel dateLabel = new JLabel();
    private JSlider routeSlider = new JSlider();
    private JLabel positionLabel = new JLabel();

    private JLabel fromPositionLabel = new JLabel(WWGnlUtilities.buildMessage("from"));
    private JLabel toPositionLabel = new JLabel(WWGnlUtilities.buildMessage("to"));
    private JLabel fromDateLabel = new JLabel(WWGnlUtilities.buildMessage("from"));
    private JLabel toDateLabel = new JLabel(WWGnlUtilities.buildMessage("to"));
    private JLabel duration = new JLabel(WWGnlUtilities.buildMessage("duration"));
    private JLabel bspRangeLabel = new JLabel(WWGnlUtilities.buildMessage("bsp-range"));
    private JLabel twsRangeLabel = new JLabel(WWGnlUtilities.buildMessage("tws-range"));
    private JLabel twaRangeLabel = new JLabel(WWGnlUtilities.buildMessage("twa-range"));

    int routeSliderValue = 1;
    private JLabel gcLabel = new JLabel();
    private JLabel actualDistLabel = new JLabel();
    private JButton prevButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton syncGribButton = new JButton();
    private JCheckBox autoSyncCheckBox = new JCheckBox();
    private JPanel bottomPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JSeparator jSeparator1 = new JSeparator();
    private JSeparator jSeparator2 = new JSeparator();
    private JSeparator jSeparator3 = new JSeparator();
    private JLabel elapsedLabel = new JLabel();
    private JSeparator jSeparator4 = new JSeparator();

    private JCheckBox showIsochrons = new JCheckBox(WWGnlUtilities.buildMessage("show-isochrones"));
    private JCheckBox showRoute = new JCheckBox(WWGnlUtilities.buildMessage("show-best-route"));
    private JCheckBox showLabels = new JCheckBox(WWGnlUtilities.buildMessage("routing-labels"));

    private JButton saveButton = new JButton("Save routing as...");

    public RoutingPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(400, 340));
        this.setPreferredSize(new Dimension(ControlPane.WIDTH, 340));
        this.add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.add(WWGnlUtilities.buildMessage("summary"), summaryPanelHolder);
        tabbedPane.add(WWGnlUtilities.buildMessage("steps"), stepPanel);
        summaryPanelHolder.setLayout(new BorderLayout());
        summaryPanelHolder.add(summaryPanel, BorderLayout.NORTH);

        stepPanel.setLayout(gridBagLayoutOne);
        stepPanel.setSize(new Dimension(395, 300));
        stepPanel.setPreferredSize(new Dimension(190, 300));
        summaryPanel.setLayout(gridBagLayoutTwo);

        group.add(trueWindRadioButton);
        group.add(appWindRadioButton);
        trueWindRadioButton.setSelected(true);
        trueWindRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                trueWindRadioButton_actionPerformed(e);
            }
        });
        appWindRadioButton.setSelected(false);
        appWindRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                appWindRadioButton_actionPerformed(e);
            }
        });
        trueAppPanel.add(trueWindRadioButton);
        trueAppPanel.add(appWindRadioButton);

        windVanePanel.setPreferredSize(new Dimension(80, 80));
        windGaugePanel.setPreferredSize(new Dimension(30, 100));
        headingPanel.setPreferredSize(new Dimension(190, 30));
        headingPanel.setSize(new Dimension(190, 30));
        dateLabel.setText(WWGnlUtilities.buildMessage("grib-date"));

        routeSlider.setToolTipText(WWGnlUtilities.buildMessage("grib-frame-index"));
        routeSlider.setMinimum(1);
        routeSlider.setMaximum(10);
        routeSlider.setValue(1);
        routeSlider.setPreferredSize(new Dimension(190, 40));
        routeSlider.setSize(new Dimension(190, 27));
        routeSlider.setSnapToTicks(true);
        routeSlider.setPaintTicks(true);
        routeSlider.setMajorTickSpacing(1);
        routeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                JSlider slider = (JSlider) evt.getSource();

                if (!slider.getValueIsAdjusting()) {
                    int sv = slider.getValue();
                    if (sv != routeSliderValue) {
                        routeSliderValue = sv;
                        //            System.out.println("Slider Value:" + routeSliderValue);
                        updateData();
                    }
                }
            }
        });
        positionLabel.setText(WWGnlUtilities.buildMessage("position"));
        gcLabel.setText(WWGnlUtilities.buildMessage("gc"));
        actualDistLabel.setText(WWGnlUtilities.buildMessage("actual"));
//  prevButton.setText("<");
        prevButton.setIcon(new ImageIcon(this.getClass().getResource("img/previous.png")));
        prevButton.setToolTipText(WWGnlUtilities.buildMessage("previous-step"));
        prevButton.setFont(new Font("Courier New", 0, 10));
        prevButton.setPreferredSize(new Dimension(22, 18));
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prevButton_actionPerformed(e);
            }
        });
//  nextButton.setText(">");
        nextButton.setIcon(new ImageIcon(this.getClass().getResource("img/next.png")));
        nextButton.setFont(new Font("Courier New", 0, 10));
        nextButton.setPreferredSize(new Dimension(22, 18));
        nextButton.setToolTipText(WWGnlUtilities.buildMessage("next-step"));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton_actionPerformed(e);
            }
        });
//  syncGribButton.setText("S");
        syncGribButton.setIcon(new ImageIcon(this.getClass().getResource("img/action.png")));
        syncGribButton.setFont(new Font("Courier New", 0, 10));
        syncGribButton.setPreferredSize(new Dimension(22, 18));
        syncGribButton.setToolTipText(WWGnlUtilities.buildMessage("synchronize-with-grib"));
        syncGribButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                syncGribButton_actionPerformed(e);
            }
        });
        autoSyncCheckBox.setText("AutoSync");
        autoSyncCheckBox.setActionCommand("autoSyncCheckBox");
        autoSyncCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autoSyncCheckBox_actionPerformed(e);
            }
        });
        bottomPanel.setLayout(gridBagLayout1);
        elapsedLabel.setText("ELapsed: 0");
        stepPanel.add(windVanePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                new Insets(0, 2, 0, 0), 0, 0));
        stepPanel.add(bspDisplay, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL,
                new Insets(0, 2, 0, 0), 0, 0));
        stepPanel.add(windGaugePanel, new GridBagConstraints(1, 1, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 5, 0, 0), 0, 0));

        stepPanel.add(stationDataPanel, new GridBagConstraints(2, 1, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 5, 0, 0), 0, 0));

        stepPanel.add(headingPanel, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 0, 0, 0), 0, 0));
        stepPanel.add(dateLabel, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 0, 0, 0), 0, 0));
        stepPanel.add(positionLabel, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        stepPanel.add(routeSlider, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));

        bottomPanel.add(prevButton,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 2), 0, 0));
        bottomPanel.add(nextButton,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 2), 0, 0));
        bottomPanel.add(syncGribButton,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 2), 0, 0));
        bottomPanel.add(autoSyncCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        stepPanel.add(bottomPanel, new GridBagConstraints(0, 9, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        stepPanel.add(elapsedLabel, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        stepPanel.add(trueAppPanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(new JLabel(WWGnlUtilities.buildMessage("from")),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(10, 2, 0, 0), 0, 0));
        summaryPanel.add(fromPositionLabel,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(10, 2, 0, 0), 0, 0));
        summaryPanel.add(new JLabel(WWGnlUtilities.buildMessage("to")),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(toPositionLabel,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(new JLabel(WWGnlUtilities.buildMessage("from")),
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(fromDateLabel,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(new JLabel(WWGnlUtilities.buildMessage("to")),
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(toDateLabel,
                new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));

        summaryPanel.add(duration,
                new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        2,
                        0,
                        0),
                        0, 0));


        summaryPanel.add(bspRangeLabel,
                new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(twsRangeLabel,
                new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(twaRangeLabel,
                new GridBagConstraints(0, 9, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(gcLabel,
                new GridBagConstraints(0, 11, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                        2,
                        0,
                        0),
                        0, 0));
        summaryPanel.add(actualDistLabel,
                new GridBagConstraints(0, 12, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 2, 0, 0), 0, 0));
        summaryPanel.add(jSeparator1,
                new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(jSeparator2,
                new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(jSeparator3,
                new GridBagConstraints(0, 10, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(jSeparator4,
                new GridBagConstraints(0, 13, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(showIsochrons,
                new GridBagConstraints(0, 14, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(showRoute,
                new GridBagConstraints(0, 15, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
        summaryPanel.add(showLabels,
                new GridBagConstraints(0, 16, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));

        summaryPanel.add(saveButton,
                new GridBagConstraints(0, 17, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));

        showIsochrons.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CommandPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel();
                cp.setDrawIsochrons(showIsochrons.isSelected());
                cp.getChartPanel().repaint();
            }
        });
        showRoute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CommandPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel();
                cp.setDrawBestRoute(showRoute.isSelected());
                showLabels.setEnabled(showRoute.isSelected());
                cp.getChartPanel().repaint();
            }
        });
        showLabels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CommandPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel();
                cp.setPostitOnRoute(showLabels.isSelected());
                cp.getChartPanel().repaint();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Re-suggest to save the routing
                CommandPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel();
                RoutingUtil.outputRouting(cp, cp.getFrom(), cp.getTo(), cp.getClosestPoint(), cp.getAllCalculatedIsochrons(), true);
            }
        });
        this.validate();

        WWContext.getInstance().addApplicationListener(new ApplicationEventListener() {
            public void setDisplayBestRoute(boolean b) {
                showRoute.setSelected(b);
                showLabels.setEnabled(showRoute.isSelected());
            }

            public void setDisplayRoutingLabels(boolean b) {
                showLabels.setSelected(b);
            }

            public void setDisplayIsochrons(boolean b) {
                showIsochrons.setSelected(b);
            }
        });
    }

    public void paintComponent(Graphics gr) {
        CommandPanel cp = ((AdjustFrame) WWContext.getInstance().getMasterTopFrame()).getCommandPanel();
        showIsochrons.setSelected(cp.isDrawIsochrons());
        showRoute.setSelected(cp.isDrawBestRoute());
        showLabels.setEnabled(showRoute.isSelected());
        showLabels.setSelected(showRoute.isSelected() && cp.isPostitOnRoute());
        saveButton.setEnabled(showRoute.isSelected());
    }

    private void updateData() {
        if (routeSliderValue == 1)
            prevButton.setEnabled(false);
        else
            prevButton.setEnabled(true);

        if (routeSliderValue == routeSlider.getMaximum())
            nextButton.setEnabled(false);
        else
            nextButton.setEnabled(true);

        RoutingPoint rp = null;
        RoutingPoint ic = null; // Isochron Center
        try {
            int r = bestRoute.size() - routeSliderValue;
            rp = bestRoute.get(r);
            if (r == 0) // Last one
                ic = rp;
            else
                ic = bestRoute.get(r - 1);
            setRouteData(rp, ic, current_routing_mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (autoSyncCheckBox.isSelected())
            syncGribButton_actionPerformed(null);
    }

    public void setAwa(int awa) {
        if (appWindRadioButton.isSelected())
            windVanePanel.setWindDir(awa);
        stationDataPanel.setAWA(awa);
    }

    public void setAws(float aws) {
        if (appWindRadioButton.isSelected())
            windGaugePanel.setTws(aws);
        stationDataPanel.setAWS(aws);
    }

    public void setTwa(int twa) {
        this.twa = twa;
        if (trueWindRadioButton.isSelected())
            windVanePanel.setWindDir(twa);
        stationDataPanel.setTWA(twa);
    }

    public int getTwa() {
        return twa;
    }

    public void setTwd(int twd) {
        this.twd = twd;
        stationDataPanel.setTWD(twd);
    }

    public int getTwd() {
        return twd;
    }

    public void setTws(float tws) {
        this.tws = tws;
        if (trueWindRadioButton.isSelected())
            windGaugePanel.setTws(tws);
        stationDataPanel.setTWS(tws);
    }

    public float getTws() {
        return tws;
    }

    public void setHdg(int hdg) {
        this.hdg = hdg;
        try {
            headingPanel.setHdg(hdg);
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    public int getHdg() {
        return hdg;
    }

    public void setDate(Date date) {
        this.date = date;
        if (date != null) {
            WWGnlUtilities.SDF_UT.setTimeZone(TimeZone.getTimeZone("127"));
            dateLabel.setText(WWGnlUtilities.SDF_UT_bis.format(date));
            int[] elapsed = evaluateDaysHours(fromDate, date);
            String mess = "";
            if (elapsed[1] > 0)
                mess = WWGnlUtilities.buildMessage("duration", new String[]{Integer.toString(elapsed[0]),
                        Integer.toString(elapsed[1])});
            else
                mess = WWGnlUtilities.buildMessage("small.duration", new String[]{Integer.toString(elapsed[0])});
            elapsedLabel.setText(mess);
        } else
            dateLabel.setText("");
    }

    public Date getDate() {
        return date;
    }

    public void setPosition(GeoPoint position) {
        this.position = position;
        positionLabel.setText(position.toString());
    }

    public GeoPoint getPosition() {
        return position;
    }

    public void setBsp(float bsp) {
        this.bsp = bsp;
        stationDataPanel.setBSP(bsp);
        bspDisplay.setBsp(bsp);
    }

    public float getBsp() {
        return bsp;
    }

    Date fromDate = null;
    Date toDate = null;

    public void setBestRoute(List<RoutingPoint> bestRoute, int routingType) {
        // GreatCircle gc = WWContext.getInstance().getGreatCircle();

        this.bestRoute = bestRoute;
//  routeSlider.setMinimum(1);
        if (bestRoute != null) {
            routeSlider.setMaximum(bestRoute.size());
            routeSlider.setValue(1);
            prevButton.setEnabled(false);
            RoutingPoint rp = null;
            RoutingPoint ic = null; // Isochron Center

            // Generate summary
            double minTWS = Double.MAX_VALUE, maxTWS = -Double.MAX_VALUE;
            double minBSP = Double.MAX_VALUE, maxBSP = -Double.MAX_VALUE;

            int minTWA = Integer.MAX_VALUE, maxTWA = Integer.MIN_VALUE;

            double actualDistance = 0D;
            GeoPoint prevPos = null;

            current_routing_mode = routingType;

            Iterator<RoutingPoint> iterator = bestRoute.iterator();
            while (iterator.hasNext()) {
                rp = iterator.next();
                if (rp.getAncestor() != null) // Not for the first one
                {
                    double bsp = rp.getBsp();
                    double tws = rp.getTws();
                    int twa = Math.abs(rp.getTwa());
                    if (twa > 180) twa = 360 - twa;

                    if (bsp < minBSP) minBSP = bsp;
                    if (bsp > maxBSP) maxBSP = bsp;
                    if (tws < minTWS) minTWS = tws;
                    if (tws > maxTWS) maxTWS = tws;
                    if (twa > maxTWA) maxTWA = twa;
                    if (twa < minTWA) minTWA = twa;
                }
                if (prevPos != null) {
                  actualDistance += GreatCircle.getDistanceInNM(new GreatCirclePoint(prevPos), new GreatCirclePoint(rp.getPosition()));
                }
                prevPos = rp.getPosition();
            }
            fromDate = bestRoute.get(bestRoute.size() - 1).getDate();
            toDate = bestRoute.get(0).getDate();
            int[] elapsed = evaluateDaysHours(fromDate, toDate);
            int nbDay = elapsed[0];
            int nbHour = elapsed[1];

            GeoPoint fromPos = bestRoute.get(bestRoute.size() - 1).getPosition();
            GeoPoint toPos = bestRoute.get(0).getPosition();

            fromPositionLabel.setText(fromPos.toString());
            toPositionLabel.setText(toPos.toString());
            fromDateLabel.setText(WWGnlUtilities.SDF_UT_day.format(fromDate));
            toDateLabel.setText(WWGnlUtilities.SDF_UT_day.format(toDate));
            duration.setText(WWGnlUtilities.buildMessage("duration", new String[]{Integer.toString(nbDay), Integer.toString(nbHour)}));
            bspRangeLabel.setText(WWGnlUtilities.buildMessage("bsp-range", new String[]{WWGnlUtilities.XXX12.format(minBSP), WWGnlUtilities.XXX12.format(maxBSP)}));
            twsRangeLabel.setText(WWGnlUtilities.buildMessage("tws-range", new String[]{WWGnlUtilities.XXX12.format(minTWS), WWGnlUtilities.XXX12.format(maxTWS)}));
            twaRangeLabel.setText(WWGnlUtilities.buildMessage("twa-range", new String[]{Integer.toString(minTWA), Integer.toString(maxTWA)}));

            // from-to
            double gcDist = GreatCircle.getDistanceInNM(new GreatCirclePoint(fromPos), new GreatCirclePoint(toPos));
            gcLabel.setText(WWGnlUtilities.buildMessage("gc", new String[]{WWGnlUtilities.XXX12.format(gcDist)}));
            actualDistLabel.setText(WWGnlUtilities.buildMessage("actual", new String[]{WWGnlUtilities.XXX12.format(actualDistance)}));

            try {
                int r = bestRoute.size() - 1;
                rp = bestRoute.get(r); // The first one
                if (r == 0) // Last one
                    ic = rp;
                else
                    ic = bestRoute.get(r - 1);
                setRouteData(rp, ic, routingType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int[] evaluateDaysHours(Date from, Date to) {
        long elapsed = to.getTime() - from.getTime();
        final long NBMSEC_PER_HOUR = 3600 * 1000;
        final long NBMSEC_PER_DAY = 24 * NBMSEC_PER_HOUR;
        int nbDay = (int) (elapsed / NBMSEC_PER_DAY);
        int nbHour = (int) Math.round(((double) (elapsed - (nbDay * NBMSEC_PER_DAY)) / (double) NBMSEC_PER_HOUR));
        return new int[]{nbDay, nbHour};
    }

    private void setRouteData(RoutingPoint rp, RoutingPoint center, int routingType) {
        try {
            double bsp = (routingType == RoutingUtil.REAL_ROUTING ? center.getBsp() : rp.getBsp());
            Date date = rp.getDate();
            GeoPoint position = rp.getPosition();
            int twa = (routingType == RoutingUtil.REAL_ROUTING ? -center.getTwa() : -rp.getTwa());
            double tws = (routingType == RoutingUtil.REAL_ROUTING ? center.getTws() : rp.getTws());
            int twd = (routingType == RoutingUtil.REAL_ROUTING ? center.getTwd() : rp.getTwd());
            int hdg = (routingType == RoutingUtil.REAL_ROUTING ? center.getHdg() : rp.getHdg());
            // AW Calculation
            int awa = 0;
            double aws = 0D;

            double base = bsp + (tws * Math.cos(Math.toRadians(twa)));
            double hauteur = tws * Math.sin(Math.toRadians(twa));
            aws = Math.sqrt((base * base) + (hauteur * hauteur));
            int _twa = twa;
            if (_twa < -180)
                _twa += 360;
            if (_twa > 180)
                _twa -= 360;

            setAws((float) aws);
            if (base != 0) {
                int _awa = (int) Math.toDegrees(Math.atan(hauteur / base));
                awa = _awa;
                if (base < 0) {
                    awa = 180 - _awa;
                }
            } else {
                awa = 90;
                if (Math.sin(Math.toRadians(twa)) < 0)
                    awa = -90;
            }
            if (awa > 180)
                awa = awa - 360;
            if (Utilities.sign(_twa) != Utilities.sign(awa))
                awa = -awa;
            setAwa(awa);

//    System.out.println("TWA:" + twa + ", TWS:" + tws + " (_twa:" + _twa + ")");
//    System.out.println("AWA:" + awa + ", AWS:" + aws);
//    System.out.println("=======================");
            setBsp((float) bsp);
            setPosition(position);
            setTwa(twa);
            setTwd(twd);
            setTws((float) tws);
            setDate(date);
            setHdg(hdg);
            setAwa(awa);
            setAws((float) aws);
            WWContext.getInstance().firePlotBoatAt(date, position, hdg);

            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RoutingPoint> getBestRoute() {
        return bestRoute;
    }

    private void prevButton_actionPerformed(ActionEvent e) {
        routeSliderValue -= 1;
        routeSlider.setValue(routeSliderValue);
        updateData();
        if (autoSyncCheckBox.isSelected())
            syncGribButton_actionPerformed(e);
    }

    private void nextButton_actionPerformed(ActionEvent e) {
        routeSliderValue += 1;
        routeSlider.setValue(routeSliderValue);
        updateData();
        if (autoSyncCheckBox.isSelected())
            syncGribButton_actionPerformed(e);
    }

    private void syncGribButton_actionPerformed(ActionEvent e) {
//  System.out.println("Synchronizing with GRIB for " + this.date.toString());
        WWContext.getInstance().fireSyncGribWithDate(date);
    }

    private void autoSyncCheckBox_actionPerformed(ActionEvent e) {
        syncGribButton.setEnabled(!autoSyncCheckBox.isSelected());
    }

    private void trueWindRadioButton_actionPerformed(ActionEvent e) {
        updateData();
    }

    private void appWindRadioButton_actionPerformed(ActionEvent e) {
        updateData();
    }
}

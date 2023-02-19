package chartview.gui.right;

import calc.GeoPoint;
import chart.components.ui.ChartPanelInterface;
import chartview.ctx.WWContext;
import chartview.gui.util.dialog.WHRatioPanel;
import chartview.gui.util.dialog.WayPointTablePanel;
import chartview.util.UserExitAction;
import chartview.util.UserExitException;
import chartview.util.UserExitInterface;
import chartview.util.WWGnlUtilities;
import chartview.util.grib.GribHelper;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class CommandPanelPopup
        extends JPopupMenu
        implements ActionListener, PopupMenuListener {
    private final CommandPanel parent;

    private transient List<UserExitAction> userExitList = null;

    private final JCheckBoxMenuItem showPrintablePageSize;

    private final JMenu compositeMenu;
    private final JMenuItem removeComposite;
    private final JMenuItem setupComposite;

    private final JMenuItem removeFax;
    private final JMenuItem setFaxWHRatio;
    private final JMenuItem removeGRIB;

//    private final JMenu menuGoogle;
//    private final JMenuItem menuGoogleMap;
//    private final JMenuItem menuGoogleEarth;

    private final JCheckBoxMenuItem plotNadir;

    private final JMenu GRIBnWindDisplayMenu;

    private final JMenuItem gribDetails;
    //  private JMenuItem spotHere;
    private final JRadioButtonMenuItem smallDot;
    private final JRadioButtonMenuItem heavyDot;
    private final JRadioButtonMenuItem background;
    private final JCheckBoxMenuItem displayWindSpeedValueMenuItem;
    private final JCheckBoxMenuItem useThickWindMenuItem;

    private final JMenuItem windBaseColor;
    private final JCheckBoxMenuItem gribTransparency;
    private final JCheckBoxMenuItem colorRangeForWindSpeed;
    private final JCheckBoxMenuItem gribSlicing;

    private final JMenu chartMenu;
    private final JRadioButtonMenuItem tooltip;
    private final JRadioButtonMenuItem notooltip;
    private final JRadioButtonMenuItem tooltipwin;
    private final JMenuItem chartColor;
    private final JMenuItem gridColor;
    private final JMenuItem chartBgColor;
    private final JMenuItem removeLastDrawing;
    private final JMenuItem chooseDrawingColor;

    private final JCheckBoxMenuItem showChart;
    private final JCheckBoxMenuItem showPlaces;
    private final JCheckBoxMenuItem showSailMail;
    private final JCheckBoxMenuItem showGrid;
    private final JCheckBoxMenuItem clickScroll;

    private final JMenu routingMenu;
    private final JCheckBoxMenuItem showIsochrons;
    private final JCheckBoxMenuItem showBestRoute;
    private final JCheckBoxMenuItem showRoutingLabels;
    private final JMenuItem interruptRouting;
    private final JMenuItem eraseRoutingBoat;
    private final JMenuItem erasePositionBoat;
    private final JMenuItem removeRouting;

    private final JMenuItem eraseFlags;
    private final JCheckBoxMenuItem insertRoutingWayPointsMenuItem;
    private final JMenuItem editRoutingWayPoints;

    private final JMenu userExitMenu;

    private final ButtonGroup group = new ButtonGroup();
    private final ButtonGroup group2 = new ButtonGroup();

    private final String SHOW_PRINTABLE_PAGE_SIZE = WWGnlUtilities.buildMessage("show-printable-page-size");

    private final String COMPOSITE_MENU = WWGnlUtilities.buildMessage("composite-options-menu");
    private final String REMOVE_COMPOSITE = WWGnlUtilities.buildMessage("remove-composite");
    private final String SETUP_COMPOSITE = WWGnlUtilities.buildMessage("set-composite");

    private final String REMOVE_FAX = WWGnlUtilities.buildMessage("remove-fax");
    private final String FAX_WH_RATIO = WWGnlUtilities.buildMessage("set-fax-ratio");
    private final String REMOVE_GRIB = WWGnlUtilities.buildMessage("remove-grib");

//    private final String VIEW_GOOGLE = WWGnlUtilities.buildMessage("view-google");
//    private final String GOOGLE_MAP = WWGnlUtilities.buildMessage("view-google-map");
//    private final String GOOGLE_EARTH = WWGnlUtilities.buildMessage("view-google-earth");

    private final String PLOT_NADIR = WWGnlUtilities.buildMessage("plot-nadir");

    private final String WIND_AND_GRIB_MENU = WWGnlUtilities.buildMessage("wind-and-grib-optiopns-menu");
    private final String GRIB_DETAILS = WWGnlUtilities.buildMessage("grib-details");
    //  private final String SPOT_HERE = WWGnlUtilities.buildMessage("spot-here");
    private final String SMALL_DOT = WWGnlUtilities.buildMessage("small-dot");
    private final String HEAVY_DOT = WWGnlUtilities.buildMessage("heavy-dot");
    private final String BACKGROUND = WWGnlUtilities.buildMessage("background");
    private final String WIND_BASE = WWGnlUtilities.buildMessage("change-wind-base");
    private final String TRANSPARENT_GRIB = WWGnlUtilities.buildMessage("use-wind-color");
    private final String COLOR_RANGE = WWGnlUtilities.buildMessage("color-range");
    private final String GRIB_SLICING = WWGnlUtilities.buildMessage("grib-slicing");

    private final String DISPLAYWINDSPEED = WWGnlUtilities.buildMessage("display-wind-speed");
    private final String USE_THICK_WIND = WWGnlUtilities.buildMessage("use-thick-wind");

    private final String CHART_MENU = WWGnlUtilities.buildMessage("chart-options-menu");
    private final String TOOLTIP = WWGnlUtilities.buildMessage("tooltip-on-chart");
    private final String NO_TOOLTIP = WWGnlUtilities.buildMessage("no-tooltip");
    private final String TOOLTIP_WINDOW = WWGnlUtilities.buildMessage("tooltip-window");
    private final String CHART_COLOR = WWGnlUtilities.buildMessage("change-chart-color");
    private final String GRID_COLOR = WWGnlUtilities.buildMessage("change-grid-color");
    private final String CHART_BG_COLOR = WWGnlUtilities.buildMessage("change-chart-bg-color");
    private final String SHOW_GRID = WWGnlUtilities.buildMessage("show-grid");
    private final String SHOW_CHART = WWGnlUtilities.buildMessage("show-chart");
    private final String SHOW_PLACES = WWGnlUtilities.buildMessage("show-places");
    private final String SHOW_SAILMAIL = WWGnlUtilities.buildMessage("show-sailmail");
    private final String REMOVE_LAST_DRAWING = WWGnlUtilities.buildMessage("remove-last-drawing");
    private final String CHOOSE_DRAWING_COLOR = WWGnlUtilities.buildMessage("choose-drawing-color");

    private final String ROUTING_MENU = WWGnlUtilities.buildMessage("routing-options-menu");
    private final String SHOW_ISOCHRONS = WWGnlUtilities.buildMessage("show-isochrones");
    private final String SHOW_BEST_ROUTE = WWGnlUtilities.buildMessage("show-best-route");
    private final String SHOW_ROUTING_LABELS = WWGnlUtilities.buildMessage("routing-labels");
    private final String INTERRUPT_ROUTING = WWGnlUtilities.buildMessage("interrupt-routing");
    private final String REMOVE_ROUTING = WWGnlUtilities.buildMessage("remove-routing");
    private final String ERASE_ROUTING_BOAT = WWGnlUtilities.buildMessage("erase-routing-boat");
    private final String ERASE_POSITION_BOAT = WWGnlUtilities.buildMessage("erase-position-boat");
    private final String CLICK_SCROLL = WWGnlUtilities.buildMessage("click-scroll");

    private final String DROP_FLAGS = WWGnlUtilities.buildMessage("drop-flags");
    private final String INSERT_ROUTING_WP = WWGnlUtilities.buildMessage("insert-routing-wp");
    private final String EDIT_ROUTING_WP = WWGnlUtilities.buildMessage("edit-routing-wp");

    private int _x = 0, _y = 0;

    public CommandPanelPopup(CommandPanel ccp, int x, int y) {
        super();
        this.parent = ccp;
        this._x = x;
        this._y = y;

        this.setBackground(Color.white);

        showPrintablePageSize = new JCheckBoxMenuItem(SHOW_PRINTABLE_PAGE_SIZE);
        this.add(showPrintablePageSize);
        showPrintablePageSize.addActionListener(this);
        showPrintablePageSize.setSelected(parent.isDisplayPageSize());
        showPrintablePageSize.setBackground(Color.white);
        showPrintablePageSize.setIcon(new ImageIcon(this.getClass().getResource("print.png")));

        compositeMenu = new JMenu(COMPOSITE_MENU);
        compositeMenu.setIcon(new ImageIcon(this.getClass().getResource("composite.png")));
        this.add(compositeMenu);

        removeComposite = new JMenuItem(REMOVE_COMPOSITE);
        removeComposite.setIcon(new ImageIcon(this.getClass().getResource("remove_composite.png")));
        compositeMenu.add(removeComposite);
        removeComposite.addActionListener(this);
        removeComposite.setBackground(Color.white);

        setupComposite = new JMenuItem(SETUP_COMPOSITE);
        setupComposite.setIcon(new ImageIcon(this.getClass().getResource("composite.png")));
        compositeMenu.add(setupComposite);
        setupComposite.addActionListener(this);
        setupComposite.setBackground(Color.white);

//        menuGoogle = new JMenu(VIEW_GOOGLE);
//        compositeMenu.add(menuGoogle);
//        menuGoogle.setBackground(Color.white);
//        menuGoogle.setIcon(new ImageIcon(this.getClass().getResource("google.png")));

//        menuGoogleMap = new JMenuItem(GOOGLE_MAP);
//        menuGoogle.add(menuGoogleMap);
//        menuGoogleMap.addActionListener(this);
//        menuGoogleMap.setBackground(Color.white);
//        menuGoogleMap.setIcon(new ImageIcon(this.getClass().getResource("google.png")));

//        menuGoogleEarth = new JMenuItem(GOOGLE_EARTH);
//        menuGoogle.add(menuGoogleEarth);
//        menuGoogleEarth.addActionListener(this);
//        menuGoogleEarth.setBackground(Color.white);
//        menuGoogleEarth.setIcon(new ImageIcon(this.getClass().getResource("ge.png")));

        removeFax = new JMenuItem(REMOVE_FAX);
        removeFax.setIcon(new ImageIcon(this.getClass().getResource("remove_fax.png")));
        compositeMenu.add(removeFax);
        removeFax.addActionListener(this);
        removeFax.setBackground(Color.white);

        setFaxWHRatio = new JMenuItem(FAX_WH_RATIO);
        compositeMenu.add(setFaxWHRatio);
        setFaxWHRatio.addActionListener(this);
        setFaxWHRatio.setBackground(Color.white);

        removeGRIB = new JMenuItem(REMOVE_GRIB);
        removeGRIB.setIcon(new ImageIcon(this.getClass().getResource("remove_grib.png")));
        compositeMenu.add(removeGRIB);
        removeGRIB.addActionListener(this);
        removeGRIB.setBackground(Color.white);

//  this.add(new JSeparator());

        plotNadir = new JCheckBoxMenuItem(PLOT_NADIR);
        this.add(plotNadir);
        plotNadir.setSelected(parent.isPlotNadir());
        plotNadir.addActionListener(this);
        plotNadir.setBackground(Color.white);

//  this.add(new JSeparator());

        GRIBnWindDisplayMenu = new JMenu(WIND_AND_GRIB_MENU);
        GRIBnWindDisplayMenu.setIcon(new ImageIcon(this.getClass().getResource("grib.png")));
        this.add(GRIBnWindDisplayMenu);
        gribDetails = new JRadioButtonMenuItem(GRIB_DETAILS);
        GRIBnWindDisplayMenu.add(gribDetails);
        gribDetails.addActionListener(this);
        gribDetails.setBackground(Color.white);
        gribDetails.setIcon(new ImageIcon(this.getClass().getResource("grib.png")));

//    spotHere = new JMenuItem(SPOT_HERE);
//    GRIBnWindDisplayMenu.add(spotHere);
//    spotHere.addActionListener(this);
//    spotHere.setBackground(Color.white);
//    spotHere.setIcon(new ImageIcon(this.getClass().getResource("anchor.png")));

        smallDot = new JRadioButtonMenuItem(SMALL_DOT);
        GRIBnWindDisplayMenu.add(smallDot);
        smallDot.addActionListener(this);
        smallDot.setBackground(Color.white);
        heavyDot = new JRadioButtonMenuItem(HEAVY_DOT);
        GRIBnWindDisplayMenu.add(heavyDot);
        heavyDot.addActionListener(this);
        heavyDot.setBackground(Color.white);
        background = new JRadioButtonMenuItem(BACKGROUND);
        GRIBnWindDisplayMenu.add(background);
        background.addActionListener(this);
        background.setBackground(Color.white);
        windBaseColor = new JMenuItem(WIND_BASE);
        GRIBnWindDisplayMenu.add(windBaseColor);
        windBaseColor.addActionListener(this);
        windBaseColor.setBackground(Color.white);
        gribTransparency = new JCheckBoxMenuItem(TRANSPARENT_GRIB);
        GRIBnWindDisplayMenu.add(gribTransparency);
        gribTransparency.addActionListener(this);
        gribTransparency.setSelected(WWContext.getInstance().getUseGRIBWindSpeedTransparency());
        gribTransparency.setBackground(Color.white);

        colorRangeForWindSpeed = new JCheckBoxMenuItem(COLOR_RANGE);
        GRIBnWindDisplayMenu.add(colorRangeForWindSpeed);
        colorRangeForWindSpeed.addActionListener(this);
        colorRangeForWindSpeed.setSelected(WWContext.getInstance().getUseColorRangeForWindSpeed());
        colorRangeForWindSpeed.setBackground(Color.white);

        displayWindSpeedValueMenuItem = new JCheckBoxMenuItem(DISPLAYWINDSPEED);
        GRIBnWindDisplayMenu.add(displayWindSpeedValueMenuItem);
        displayWindSpeedValueMenuItem.setSelected(parent.isDisplayWindSpeedValue());
        displayWindSpeedValueMenuItem.addActionListener(this);
        displayWindSpeedValueMenuItem.setBackground(Color.white);

        useThickWindMenuItem = new JCheckBoxMenuItem(USE_THICK_WIND);
        GRIBnWindDisplayMenu.add(useThickWindMenuItem);
        useThickWindMenuItem.setSelected(parent.isUseThickWind());
        useThickWindMenuItem.addActionListener(this);
        useThickWindMenuItem.setBackground(Color.white);

        gribSlicing = new JCheckBoxMenuItem(GRIB_SLICING);
        GRIBnWindDisplayMenu.add(gribSlicing);
        gribSlicing.setSelected(parent.isEnableGRIBSlice());
        gribSlicing.addActionListener(this);
        gribSlicing.setBackground(Color.white);

//  this.add(new JSeparator());

        chartMenu = new JMenu(CHART_MENU);
        this.add(chartMenu);

        chooseDrawingColor = new JMenuItem(CHOOSE_DRAWING_COLOR);
        chartMenu.add(chooseDrawingColor);
        chooseDrawingColor.addActionListener(this);
        chooseDrawingColor.setBackground(Color.white);

        removeLastDrawing = new JMenuItem(REMOVE_LAST_DRAWING);
        if (ccp.getChartPanel().getHandDrawing() != null && ccp.getChartPanel().getHandDrawing().size() > 0) {
            chartMenu.add(removeLastDrawing);
        }
        removeLastDrawing.addActionListener(this);
        removeLastDrawing.setBackground(Color.white);

        chartColor = new JMenuItem(CHART_COLOR);
        chartMenu.add(chartColor);
        chartColor.addActionListener(this);
        chartColor.setBackground(Color.white);
        gridColor = new JMenuItem(GRID_COLOR);
        chartMenu.add(gridColor);
        gridColor.addActionListener(this);
        gridColor.setBackground(Color.white);
        chartBgColor = new JMenuItem(CHART_BG_COLOR);
        chartMenu.add(chartBgColor);
        chartBgColor.addActionListener(this);
        chartBgColor.setBackground(Color.white);

        group.add(smallDot);
        group.add(heavyDot);
        group.add(background);

        smallDot.setSelected(true);
        heavyDot.setSelected(parent.drawHeavyDot);
        background.setSelected(parent.drawWindColorBackground);

        removeFax.setEnabled(parent.thereIsFax2Display());

        if (parent.thereIsGRIB2Display()) {
            removeGRIB.setEnabled(true);
            smallDot.setEnabled(true);
            heavyDot.setEnabled(true);
            background.setEnabled(true);
            displayWindSpeedValueMenuItem.setEnabled(true);
            useThickWindMenuItem.setEnabled(true);
            windBaseColor.setEnabled(true);
            gribSlicing.setEnabled(true);
        } else {
            removeGRIB.setEnabled(false);
            smallDot.setEnabled(false);
            heavyDot.setEnabled(false);
            background.setEnabled(false);
            displayWindSpeedValueMenuItem.setEnabled(false);
            useThickWindMenuItem.setEnabled(false);
            windBaseColor.setEnabled(false);
            gribSlicing.setEnabled(false);
        }

        chartMenu.add(new JSeparator());

//  boolean altWin = parent.isDisplayAltTooltip();
//  boolean posTooltip = parent.chartPanel.isPositionToolTipEnabled();

        String ttOption = System.getProperty("tooltip.option", "on-chart");
        tooltip = new JRadioButtonMenuItem(TOOLTIP);
        chartMenu.add(tooltip);
//  tooltip.setSelected(posTooltip && !altWin);
        tooltip.setSelected("on-chart".equals(ttOption));
        tooltip.setBackground(Color.white);
        tooltip.addActionListener(this);

        notooltip = new JRadioButtonMenuItem(NO_TOOLTIP);
        chartMenu.add(notooltip);
//  notooltip.setSelected(!posTooltip);
        notooltip.setSelected("none".equals(ttOption));
        notooltip.setBackground(Color.white);
        notooltip.addActionListener(this);

        tooltipwin = new JRadioButtonMenuItem(TOOLTIP_WINDOW);
        chartMenu.add(tooltipwin);
//  tooltipwin.setSelected(posTooltip && altWin);
        tooltipwin.setSelected("tt-window".equals(ttOption));
        tooltipwin.setBackground(Color.white);
        tooltipwin.addActionListener(this);

        group2.add(tooltip);
        group2.add(notooltip);
        group2.add(tooltipwin);

        chartMenu.add(new JSeparator());

        showChart = new JCheckBoxMenuItem(SHOW_CHART);
        chartMenu.add(showChart);
        showChart.setSelected(parent.isDrawChart());
        showChart.addActionListener(this);
        showChart.setBackground(Color.white);
        showPlaces = new JCheckBoxMenuItem(SHOW_PLACES);
        chartMenu.add(showPlaces);
        showPlaces.setSelected(parent.isDrawChart() && parent.isShowPlaces());
        showPlaces.addActionListener(this);
        showPlaces.setBackground(Color.white);
        showSailMail = new JCheckBoxMenuItem(SHOW_SAILMAIL);
        chartMenu.add(showSailMail);
        showSailMail.setSelected(parent.isDrawChart() && parent.isShowSMStations());
        showSailMail.addActionListener(this);
        showSailMail.setBackground(Color.white);
        showGrid = new JCheckBoxMenuItem(SHOW_GRID);
        chartMenu.add(showGrid);
        showGrid.setSelected(parent.chartPanel.isWithGrid());
        showGrid.setBackground(Color.white);
        showGrid.addActionListener(this);

        clickScroll = new JCheckBoxMenuItem(CLICK_SCROLL);
        chartMenu.add(clickScroll);
        clickScroll.setSelected(parent.chartPanel.isMouseEdgeProximityDetectionEnabled());
        clickScroll.setBackground(Color.white);
        clickScroll.addActionListener(this);

        routingMenu = new JMenu(ROUTING_MENU);
        routingMenu.setIcon(new ImageIcon(this.getClass().getResource("navigation.png")));
        this.add(routingMenu);
        showIsochrons = new JCheckBoxMenuItem(SHOW_ISOCHRONS);
        routingMenu.add(showIsochrons);
        showIsochrons.setSelected(parent.isDrawIsochrons());
        showIsochrons.setEnabled(parent.routingMode);
        showIsochrons.setBackground(Color.white);
        showIsochrons.addActionListener(this);
        showBestRoute = new JCheckBoxMenuItem(SHOW_BEST_ROUTE);
        routingMenu.add(showBestRoute);
        showBestRoute.setSelected(parent.isDrawBestRoute());
        showBestRoute.setEnabled(parent.routingMode);
        showBestRoute.setBackground(Color.white);
        showBestRoute.addActionListener(this);

        showRoutingLabels = new JCheckBoxMenuItem(SHOW_ROUTING_LABELS);
        routingMenu.add(showRoutingLabels);
        showRoutingLabels.setSelected(parent.isPostitOnRoute());
        showRoutingLabels.setEnabled(parent.routingMode);
        showRoutingLabels.setBackground(Color.white);
        showRoutingLabels.addActionListener(this);
        showRoutingLabels.setIcon(new ImageIcon(this.getClass().getResource("label.png")));

//  this.add(new JSeparator());

        interruptRouting = new JMenuItem(INTERRUPT_ROUTING);
        routingMenu.add(interruptRouting);
        interruptRouting.setEnabled(parent.routingOnItsWay);
        interruptRouting.setBackground(Color.white);
        interruptRouting.addActionListener(this);

        eraseRoutingBoat = new JMenuItem(ERASE_ROUTING_BOAT);
        routingMenu.add(eraseRoutingBoat);
        eraseRoutingBoat.setEnabled(parent.isRoutingBoatDisplayed());
        eraseRoutingBoat.setBackground(Color.white);
        eraseRoutingBoat.addActionListener(this);

        erasePositionBoat = new JMenuItem(ERASE_POSITION_BOAT);
        this.add(erasePositionBoat);
        erasePositionBoat.setEnabled(parent.isBoatPositionDisplayed());
        erasePositionBoat.setBackground(Color.white);
        erasePositionBoat.addActionListener(this);

        removeRouting = new JMenuItem(REMOVE_ROUTING);
        removeRouting.setIcon(new ImageIcon(this.getClass().getResource("remove_file.png")));
        routingMenu.add(removeRouting);
        removeRouting.setEnabled(parent.allCalculatedIsochrons != null);
        removeRouting.setBackground(Color.white);
        removeRouting.addActionListener(this);

        this.add(new JSeparator());

        eraseFlags = new JMenuItem(DROP_FLAGS);
//  eraseFlags.setIcon(new ImageIcon(this.getClass().getResource("greenflag.png")));
        eraseFlags.setIcon(new ImageIcon(this.getClass().getResource("pushpin_16x16.png")));
        this.add(eraseFlags);
        eraseFlags.setEnabled(parent.from != null);
        eraseFlags.setBackground(Color.white);
        eraseFlags.addActionListener(this);

        insertRoutingWayPointsMenuItem = new JCheckBoxMenuItem(INSERT_ROUTING_WP);
//  insertRoutingWayPointsMenuItem.setIcon(new ImageIcon(this.getClass().getResource("greenflag.png")));
        insertRoutingWayPointsMenuItem.setSelected(parent.insertRoutingWP);
        this.add(insertRoutingWayPointsMenuItem);
        insertRoutingWayPointsMenuItem.setEnabled(parent.from != null && parent.to != null);
        insertRoutingWayPointsMenuItem.setBackground(Color.white);
        insertRoutingWayPointsMenuItem.addActionListener(this);

        editRoutingWayPoints = new JMenuItem(EDIT_ROUTING_WP);
//  editRoutingWayPoints.setIcon(new ImageIcon(this.getClass().getResource("greenflag.png")));
        editRoutingWayPoints.setIcon(new ImageIcon(this.getClass().getResource("pushpin_16x16.png")));
        this.add(editRoutingWayPoints);
        editRoutingWayPoints.setEnabled(parent.intermediateRoutingWP != null && parent.intermediateRoutingWP.size() > 0);
        editRoutingWayPoints.setBackground(Color.white);
        editRoutingWayPoints.addActionListener(this);

        this.add(new JSeparator());

        userExitMenu = new JMenu("User Exit");
        this.add(userExitMenu);
        userExitMenu.setBackground(Color.white);

        // Read user-exit config file
        DOMParser parser = WWContext.getInstance().getParser();
        XMLDocument userExit = null;
        synchronized (parser) {
            try {
                parser.setValidationMode(XMLParser.NONVALIDATING);
                parser.parse(new File(WWGnlUtilities.USEREXITS_FILE_NAME).toURI().toURL());
                userExit = parser.getDocument();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (userExit != null) {
            try {
                NodeList nl = userExit.selectNodes("/ww-user-exit/*");
                if (nl != null && nl.getLength() > 0) {
                    userExitList = new ArrayList<>(1);
                }
                buildUserExitMenus(nl, userExitMenu);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Creating UserExits menu:" + ex.toString(), "UserExit Menu", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JMenuItem nullUserExit = new JMenuItem("<null>");
            userExitMenu.add(nullUserExit);
            nullUserExit.setBackground(Color.white);
//    nullUserExit.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals(REMOVE_COMPOSITE)) {
            parent.removeComposite();
      /*
      WWContext.getInstance().setCurrentComposite("");
      parent.unsetFaxImage();
      parent.unsetGribData();
      parent.setNLat(65D);
      parent.setSLat(-65D);
      parent.setWLong(-180D);
      parent.setELong(180D);
      parent.applyBoundariesChanges();
      parent.faxImage = null;
      parent.wgd = null;
      parent.gribFileName = "";
      parent.chartPanel.repaint();
      */
        }
        if (event.getActionCommand().equals(SHOW_PRINTABLE_PAGE_SIZE)) {
            parent.setDisplayPageSize(showPrintablePageSize.isSelected());
        }
        if (event.getActionCommand().equals(SETUP_COMPOSITE)) {
            WWContext.getInstance().fireSetCompositeRequested();
        }
        if (event.getActionCommand().equals(REMOVE_FAX)) {
            int nbf = (parent.getFaxes() != null ? parent.getFaxes().length : 0);
            if (nbf > 0) {
                String mess = WWGnlUtilities.buildMessage("really-remove") + " ";
                if (nbf == 1) {
                    mess += WWGnlUtilities.buildMessage("this-fax");
                } else {
                    mess += WWGnlUtilities.buildMessage("those-faxes", new String[]
                            {Integer.toString(nbf)});
                }
                int resp =
                        JOptionPane.showConfirmDialog(parent, mess, WWGnlUtilities.buildMessage("remove-faxes"), JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    parent.unsetFaxImage();
                    parent.chartPanel.repaint();
                }
            }
        }
//        if (event.getActionCommand().equals(GOOGLE_MAP)) {
//            WWContext.getInstance().fireGoogleMapRequested();
//        }
//        if (event.getActionCommand().equals(GOOGLE_EARTH)) {
//            WWContext.getInstance().fireGoogleEarthRequested();
//        }
        if (event.getActionCommand().equals(FAX_WH_RATIO)) {
            WHRatioPanel whrp = new WHRatioPanel();
            whrp.setRatio(parent.whRatio);
            int resp =
                    JOptionPane.showConfirmDialog(parent, whrp, WWGnlUtilities.buildMessage("fax-height-width-ratio"), JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
            if (resp == JOptionPane.OK_OPTION) {
                try {
                    parent.setWHRatio(whrp.getRatio()) /* 0.9375 */;
                } catch (Exception ex) {
                    WWContext.getInstance().fireLogging("Oops!\n");
                }
                parent.chartPanel.repaint();
            }
        } else if (event.getActionCommand().equals(REMOVE_GRIB)) {
            parent.unsetGribData();
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(PLOT_NADIR)) {
            parent.setPlotNadir(plotNadir.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(GRIB_DETAILS)) {
            String mess = "Contains " + parent.getGribData().length + " frame(s)";
            if (WWContext.getInstance().getGribFile() != null) {
                GribHelper.displayGRIBDetails(WWContext.getInstance().getGribFile(), mess);
            } else {
                System.out.println("No GribFile...");
            }
        } else if (event.getActionCommand().equals(SMALL_DOT)) {
            parent.drawHeavyDot = false;
            parent.drawWindColorBackground = false;
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(HEAVY_DOT)) {
            parent.drawHeavyDot = true;
            parent.drawWindColorBackground = false;
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(BACKGROUND)) {
            parent.drawHeavyDot = false;
            parent.drawWindColorBackground = true;
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(WIND_BASE)) {
            Color orig = CommandPanel.initialGribWindBaseColor;
            Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("change-wind-base"), orig);
            if (newColor != null) {
                CommandPanel.initialGribWindBaseColor = newColor;
                // parent.chartPanel.repaint();
            }
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(DISPLAYWINDSPEED)) {
            parent.setDisplayWindSpeedValue(displayWindSpeedValueMenuItem.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(USE_THICK_WIND)) {
            parent.setUseThickWind(useThickWindMenuItem.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(GRIB_SLICING)) {
            parent.setEnableGRIBSlice(!parent.isEnableGRIBSlice());
        } else if (event.getActionCommand().equals(TRANSPARENT_GRIB)) {
            Boolean bool = WWContext.getInstance().getUseGRIBWindSpeedTransparency();
            WWContext.getInstance().setUseGRIBWindSpeedTransparency(!bool);
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(COLOR_RANGE)) {
            Boolean bool = WWContext.getInstance().getUseColorRangeForWindSpeed();
            WWContext.getInstance().setUseColorRangeForWindSpeed(!bool);
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(TOOLTIP)) {
            parent.chartPanel.setPositionToolTipEnabled(!notooltip.isSelected());
            parent.setDisplayAltTooltip(tooltipwin.isSelected());
            if (tooltip.isSelected()) {
                System.setProperty("tooltip.option", "on-chart");
            }
        } else if (event.getActionCommand().equals(NO_TOOLTIP)) {
            parent.chartPanel.setPositionToolTipEnabled(!notooltip.isSelected());
            parent.setDisplayAltTooltip(!notooltip.isSelected());
            if (notooltip.isSelected()) {
                System.setProperty("tooltip.option", "none");
            }
        } else if (event.getActionCommand().equals(TOOLTIP_WINDOW)) {
            parent.chartPanel.setPositionToolTipEnabled(!notooltip.isSelected());
            parent.setDisplayAltTooltip(tooltipwin.isSelected());
            if (tooltipwin.isSelected()) {
                System.setProperty("tooltip.option", "tt-window");
            }
        } else if (event.getActionCommand().equals(CHART_COLOR)) {
            Color orig = parent.chartPanel.getChartColor();
            Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("set-chart-color"), orig);
            if (newColor != null) {
                parent.chartPanel.setChartColor(newColor);
                parent.chartPanel.repaint();
            }
        } else if (event.getActionCommand().equals(GRID_COLOR)) {
            Color orig = parent.chartPanel.getGridColor();
            Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("set-grid-color"), orig);
            if (newColor != null) {
                parent.chartPanel.setGridColor(newColor);
                parent.chartPanel.repaint();
            }
        } else if (event.getActionCommand().equals(CHART_BG_COLOR)) {
            Color orig = parent.chartPanel.getChartBackGround();
            Color newColor = JColorChooser.showDialog(this, WWGnlUtilities.buildMessage("set-chart-background-color"), orig);
            if (newColor != null) {
                parent.chartPanel.setChartBackGround(newColor);
                parent.chartPanel.repaint();
            }
        } else if (event.getActionCommand().equals(REMOVE_LAST_DRAWING)) {
            parent.chartPanel.undoLastHandDrawing();
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(CHOOSE_DRAWING_COLOR)) {
            Color c = JColorChooser.showDialog(this, "Drawing Color", parent.chartPanel.getDrawColor());
            if (c != null) {
                parent.chartPanel.setDrawColor(c);
                parent.chartPanel.repaint();
            }
        } else if (event.getActionCommand().equals(SHOW_CHART)) {
            parent.setDrawChart(showChart.isSelected());
            if (parent.compositeCheckBox != null && parent.compositeCheckBox.length >= 2) {
                parent.compositeCheckBox[parent.compositeCheckBox.length - 2].setSelected(showChart.isSelected());
            }
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_PLACES)) {
            parent.setShowPlaces(showPlaces.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_SAILMAIL)) {
            parent.setShowSMStations(showSailMail.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_GRID)) {
            parent.chartPanel.setWithGrid(showGrid.isSelected());
            if (parent.compositeCheckBox != null && parent.compositeCheckBox.length >= 2) {
                parent.compositeCheckBox[parent.compositeCheckBox.length - 1].setSelected(showGrid.isSelected());
            }
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(CLICK_SCROLL)) {
            parent.chartPanel.setMouseEdgeProximityDetectionEnabled(clickScroll.isSelected());
//    parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_ISOCHRONS)) {
            parent.setDrawIsochrons(showIsochrons.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_BEST_ROUTE)) {
            parent.setDrawBestRoute(showBestRoute.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(SHOW_ROUTING_LABELS)) {
            parent.setPostitOnRoute(showRoutingLabels.isSelected());
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(INTERRUPT_ROUTING)) {
            parent.interruptRouting();
        } else if (event.getActionCommand().equals(ERASE_ROUTING_BOAT)) {
            parent.eraseRoutingBoat();
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(ERASE_POSITION_BOAT)) {
            parent.resetBoatPosition();
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(REMOVE_ROUTING)) {
            parent.shutOffRouting();
            parent.insertRoutingWP = false;
            parent.intermediateRoutingWP = null;
            parent.chartPanel.repaint();
        } else if (event.getActionCommand().equals(DROP_FLAGS)) {
            boolean drop = true;
            // If routing exists?
            if (parent.getAllCalculatedIsochrons() != null && parent.getAllCalculatedIsochrons().size() > 0) {
                int resp = JOptionPane.showConfirmDialog(this,
                        WWGnlUtilities.buildMessage("get-rid-of-routing"),
                        WWGnlUtilities.buildMessage("routing"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                drop = (resp == JOptionPane.YES_OPTION);
            }
            if (drop) {
                parent.shutOffRouting();
                parent.insertRoutingWP = false;
                parent.intermediateRoutingWP = null;
                parent.chartPanel.repaint();
                WWContext.getInstance().fireClickOnChart();
            }
        } else if (event.getActionCommand().equals(INSERT_ROUTING_WP)) {
            parent.insertRoutingWP = insertRoutingWayPointsMenuItem.isSelected();
        } else if (event.getActionCommand().equals(EDIT_ROUTING_WP)) {
            // Reorder, delete
            List<GeoPoint> aliwp = new ArrayList<>(parent.intermediateRoutingWP.size() + 1);
            aliwp.addAll(parent.intermediateRoutingWP);
            aliwp.add(parent.to);
            WayPointTablePanel wptp = new WayPointTablePanel(aliwp);
            wptp.setTopLabel(WWGnlUtilities.buildMessage("from-2", new String[]{parent.from.toString()}));
            int resp = JOptionPane.showConfirmDialog(this, wptp, "Way Point(s)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (resp == JOptionPane.OK_OPTION) {
                aliwp = wptp.getData();
                parent.to = aliwp.get(aliwp.size() - 1); // Destination
                parent.intermediateRoutingWP = new ArrayList<>(aliwp.size() - 1);
                for (int i = 0; i < aliwp.size() - 1; i++) {
                    parent.intermediateRoutingWP.add(aliwp.get(i));
                }
                WWContext.getInstance().fireHighlightWayPoint(null); // Will do the repaint
//      parent.chartPanel.repaint();
            } else {
                WWContext.getInstance().fireHighlightWayPoint(null);
            }
        } else if (isUserExitAction(event.getActionCommand())) {
            /* boolean ok = */
            manageUserExitAction(event.getActionCommand());
        }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    public void show(Component c, int x, int y) {
        super.show(c, x, y);
        _x = x;
        _y = y;

        plotNadir.setEnabled(parent.getProjection() == ChartPanelInterface.GLOBE_VIEW ||
                parent.getProjection() == ChartPanelInterface.SATELLITE_VIEW);

        if (parent.thereIsFax2Display() || parent.thereIsGRIB2Display()) {
            removeComposite.setEnabled(true);
//            menuGoogle.setEnabled(true);
            // setupComposite.setEnabled(true);
        } else {
            removeComposite.setEnabled(false);
//            menuGoogle.setEnabled(false);
            // setupComposite.setEnabled(false);
        }

        if (parent.thereIsFax2Display()) {
            removeFax.setEnabled(true);
            // setupComposite.setEnabled(true);
            setFaxWHRatio.setEnabled(true);
        } else {
            removeFax.setEnabled(false);
            // setupComposite.setEnabled(false);
            setFaxWHRatio.setEnabled(false);
        }

        if (parent.thereIsGRIB2Display()) {
            removeGRIB.setEnabled(true);
            gribDetails.setEnabled(true);
            // spotHere.setEnabled(true);
            smallDot.setEnabled(true);
            heavyDot.setEnabled(true);
            background.setEnabled(true);
        } else {
            removeGRIB.setEnabled(false);
            gribDetails.setEnabled(false);
            // spotHere.setEnabled(false);
            smallDot.setEnabled(false);
            heavyDot.setEnabled(false);
            background.setEnabled(false);
        }

        showIsochrons.setSelected(parent.isDrawIsochrons());
        showIsochrons.setEnabled(parent.routingMode);

        showBestRoute.setSelected(parent.isDrawBestRoute());
        showBestRoute.setEnabled(parent.routingMode);

        interruptRouting.setEnabled(parent.routingOnItsWay);
        eraseRoutingBoat.setEnabled(parent.isRoutingBoatDisplayed());
        removeRouting.setEnabled(parent.allCalculatedIsochrons != null && parent.allCalculatedIsochrons.size() > 0);
        eraseFlags.setEnabled(parent.from != null);
    }

    private void buildUserExitMenus(NodeList nl, JMenu userExitParent) {
        try {
            for (int i = 0; i < nl.getLength(); i++) {
                XMLElement elmt = (XMLElement) nl.item(i);
                if (elmt.getNodeName().equals("user-exit")) {
                    UserExitAction uea = new UserExitAction();
                    uea.setRnk(Integer.parseInt(elmt.getAttribute("id")));
                    uea.setLabel(elmt.selectNodes("./label").item(0).getFirstChild().getNodeValue());
                    uea.setAction(elmt.selectNodes("./action").item(0).getFirstChild().getNodeValue());
                    uea.setSync("true".equals(elmt.getAttribute("sync")));
                    uea.setAck("true".equals(elmt.getAttribute("ack")));
                    uea.setTip("");
                    try {
                        uea.setTip(elmt.selectNodes("./comment").item(0).getFirstChild().getNodeValue().trim());
                    } catch (Exception ignore) {
                        System.out.println("Comment node missing...");
                    }
                    userExitList.add(uea);
                    JMenuItem ueMenuItem = new JMenuItem(uea.getLabel());
                    ueMenuItem.setToolTipText(uea.getTip());
                    ueMenuItem.setBackground(Color.white);
                    ueMenuItem.addActionListener(this);
                    boolean isAvailable = isUEAvailable(uea);
                    ueMenuItem.setEnabled(isAvailable);

                    userExitParent.add(ueMenuItem);
                } else if (elmt.getNodeName().equals("sub-menu")) {
                    String label = elmt.getAttribute("label");
                    JMenu subMenu = new JMenu(label);
                    userExitParent.add(subMenu);
                    NodeList subList = elmt.selectNodes("./*");
                    buildUserExitMenus(subList, subMenu);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isUserExitAction(String actionCommand) {
        boolean isIn = false;
        for (UserExitAction uea : userExitList) {
            if (uea.getLabel().equals(actionCommand)) {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    private boolean isUEAvailable(UserExitAction uea) {
        boolean b = false;
        try {
            Class taskClass = Class.forName(uea.getAction());
            Object taskObject = taskClass.getDeclaredConstructor().newInstance();
            if (taskObject instanceof UserExitInterface) {
                try {
                    UserExitInterface uei = (UserExitInterface) taskObject;
                    b = uei.isAvailable(parent, WWContext.getInstance());
                } catch (Exception otherException) {
                    JOptionPane.showMessageDialog(parent, otherException.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Message
                JOptionPane.showMessageDialog(parent, uea.getAction() + " must implement UserExitInterface.", "User Exit", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoSuchMethodException | InvocationTargetException ex) {
            JOptionPane.showMessageDialog(parent, ex.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(parent, ex.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
        } catch (InstantiationException ie) {
            JOptionPane.showMessageDialog(parent, ie.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalAccessException iae) {
            JOptionPane.showMessageDialog(parent, iae.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
        }
        return b;
    }

    private boolean manageUserExitAction(String actionCommand) {
        boolean ok = true;
        UserExitAction task = null;
        for (UserExitAction uea : userExitList) {
            if (uea.getLabel().equals(actionCommand)) {
                task = uea;
                break;
            }
        }
        if (task == null) {
            ok = false; // TASK More details?...
        } else {
            try {
                Class taskClass = Class.forName(task.getAction());
                final Object taskObject = taskClass.getDeclaredConstructor().newInstance();
                if (taskObject instanceof UserExitInterface) {
                    if (task.isSync()) {
                        executeUserExit((UserExitInterface) taskObject, task.isAck());
                    } else { // Thread
                        final UserExitAction t = task;
                        Thread ueThread = new Thread("user-exit-thread") {
                            public void run() {
                                executeUserExit((UserExitInterface) taskObject, t.isAck());
                            }
                        };
                        ueThread.start();
                    }
                } else {
                    // Message
                    JOptionPane.showMessageDialog(parent, task.getAction() + " must implement UserExitInterface.", "User Exit", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NoSuchMethodException | InvocationTargetException ex) {
                JOptionPane.showMessageDialog(parent, ex.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(parent, ex.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
            } catch (InstantiationException ie) {
                JOptionPane.showMessageDialog(parent, ie.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalAccessException iae) {
                JOptionPane.showMessageDialog(parent, iae.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
            }
        }
        return ok;
    }

    private void executeUserExit(UserExitInterface uei, boolean ack) {
        try {
            boolean execOk = uei.userExitTask(parent, WWContext.getInstance());
            if (ack) {
                List<String> feedback = uei.getFeedback();
                String message = "";
                if (feedback != null) {
                    for (String s : feedback) {
                        message += ((message.length() == 0 ? "" : "\n") + s);
                    }
                }
                if (execOk) {
                    message = "User Exit Execution OK:\n" + message;
                    JOptionPane.showMessageDialog(parent, message, "UserExit Execution", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    message = "User Exit Execution Problem:\n" + message;
                    JOptionPane.showMessageDialog(parent, message, "UserExit Execution", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (UserExitException ueex) {
            JOptionPane.showMessageDialog(parent, ueex.toString(), "User Exit Action", JOptionPane.ERROR_MESSAGE);
            ueex.printStackTrace();
        }
    }
}

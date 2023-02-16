package chartview.gui.util.param;

import chartview.ctx.JTableFocusChangeListener;
import chartview.ctx.WWContext;
import chartview.gui.AdjustFrame;
import chartview.gui.util.dialog.places.Latitude;
import chartview.gui.util.dialog.places.Longitude;
import chartview.gui.util.dialog.places.Position;
import chartview.gui.util.param.widget.*;
import chartview.util.WWGnlUtilities;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.*;


public final class ParamPanel
        extends JPanel {
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JPanel centerPane = new JPanel();
    private JLabel fileNameLabel = new JLabel();

    private JEditorPane helpTextArea = new JEditorPane();
    private JScrollPane textAreaScrollPane = new JScrollPane(helpTextArea);

    private final static String KEY = WWGnlUtilities.buildMessage("name");
    private final static String VALUE = WWGnlUtilities.buildMessage("value");

    private final static String[] names = {KEY, VALUE};

    private transient TableModel dataModel;

    public static Object[][] data = null; // new Object[ParamData.labels.length][names.length];
    private transient Object[][] localData = new Object[0][0];

    private JTable table;
    private JScrollPane scrollPane;
    private BorderLayout borderLayout2 = new BorderLayout();
    private JLabel titleLabel = new JLabel();

    public ParamPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            WWContext.getInstance().fireExceptionLogging(e);
            e.printStackTrace();
        }
    }

    /**
     * Factory settings
     */
    private static void initTableValues() {
        for (int i = 0; i < ParamData.NB_PRFERENCES; i++)
            onePreference(i);
    }

    private static void onePreference(int prefId) {
        data[prefId][ParamData.NAME_INDEX] = new ParamDisplayLabel(ParamData.getLabels()[prefId], ParamData.getHelptext()[prefId]);
        data[prefId][ParamData.VALUE_INDEX] = oneFactorySetting(prefId);
    }

    private static Object oneFactorySetting(int settingID) {
        Object it = null;
        switch (settingID) {
            case ParamData.CHART_COLOR:
                it = Color.red;
                break;
            case ParamData.GRID_COLOR:
                it = Color.blue;
                break;
            case ParamData.NMEA_SERVER_URL:
                it = "http://localhost:6666";
                break;
            case ParamData.NMEA_POLLING_FREQ:
                it = new Integer(60);
                break;
            case ParamData.NMEA_FALLBACK_TIMEOUT:
                it = new Integer(60);
                break;
            case ParamData.POLAR_FILE_LOC:
                it = new DataFile(new String[]{"xml", "polar-coeff"}, WWGnlUtilities.buildMessage("polars"), "." + File.separator + "polars" + File.separator + "polars.xml");
                break;
            case ParamData.ROUTING_STEP:
                it = new Integer(10);
                break;
            case ParamData.DEFAULT_FONT:
                it = new Font("Arial", Font.PLAIN, 12) {
                    @Override
                    public String toString() {
                        return FontPanel.fontToString(this);
                    }
                };
                break;
            case ParamData.ROUTING_FORK_WIDTH:
                it = new Integer(50);
                break;
            case ParamData.ROUTING_FROM_CURR_LOC:
                it = Boolean.FALSE;
                break;
            case ParamData.ROUTING_TIME_INTERVAL:
                it = new Double(6.0);
                break;
            case ParamData.GRIB_FILES_LOC:
//      it = new DataDirectory(GnlUtilities.buildMessage("grib-files-button"), "." + File.separator + "gribDir");
                it = new DataPath("." + File.separator + "gribDir");
                break;
            case ParamData.FAX_FILES_LOC:
//      it = new DataDirectory(GnlUtilities.buildMessage("faxes-button"), "." + File.separator + "faxDir");
                it = new DataPath("." + File.separator + "faxDir");
                break;
            case ParamData.COMPOSITE_ROOT_DIR:
                it = new DataDirectory(WWGnlUtilities.buildMessage("composite-button"), "." + File.separator + "compositeDir");
                break;
            case ParamData.CHART_LINE_THICK:
                it = new Integer(3);
                break;
            case ParamData.FAX_TRANSPARENCY:
                it = new Float(0.5);
                break;
            case ParamData.CHART_BG_COLOR:
                it = Color.white;
                break;
            case ParamData.GRIB_WIND_COLOR:
                it = Color.red;
                break;
            case ParamData.GRIB_CURRENT_COLOR:
                it = Color.cyan;
                break;
            case ParamData.PATTERN_DIR:
                it = new DataDirectory(WWGnlUtilities.buildMessage("pattern-button"), "." + File.separator + "patterns");
                break;
            case ParamData.PRMSL_CONTOUR:
                it = Color.red;
                break;
            case ParamData.MB500_CONTOUR:
                it = Color.magenta;
                break;
            case ParamData.WAVES_CONTOUR:
                it = Color.green;
                break;
            case ParamData.TEMP_CONTOUR:
                it = Color.orange;
                break;
            case ParamData.PRATE_CONTOUR:
                it = Color.lightGray;
                break;
            case ParamData.LOOK_AND_FEEL:
                it = new ListOfLookAndFeel("Metal"); // Unused
                break;
            case ParamData.AUTO_UPDATES:
                it = Boolean.FALSE;
                break;
            case ParamData.SHOW_NOTIFICATIONS:
                it = Boolean.TRUE;
                break;
            case ParamData.CONFIRM_DD_ZOOM:
                it = Boolean.FALSE;
                break;
            case ParamData.AVOID_TWS_GT:
                it = new Integer(-1);
                break;
            case ParamData.AVOID_TWA_LT:
                it = new Integer(-1);
                break;
            case ParamData.STOP_ROUTING_ON_EXHAUSTED_GRIB:
                it = Boolean.FALSE;
                break;
            case ParamData.INTERVAL_BETWEEN_ISOBARS:
                it = new Integer(4);
                break;
            case ParamData.ISO_TWS_LIST:
                it = new ContourLinesList("10, 20, 30, 40, 60");
                break;
            case ParamData.ISOBARS_LIST:
                it = new ContourLinesList("984, 988, 992, 996, 1000, 1004, 1008, [1012], 1016, 1020, 1024");
                break;
            case ParamData.ISOHEIGHT500_LIST:
                it = new ContourLinesList("4980, 5040, 5100, 5160, 5220, 5280, 5340, 5400, 5460, 5520, 5580, [5640], 5700, 5760, 5820, 5880, 5940, 6000, 6060");
                break;
            case ParamData.ISOHEIGHTWAVES_LIST:
                it = new ContourLinesList("1, 2, 3, 4, 5, 6, 7, 8, 9, 10");
                break;
            case ParamData.ISOTEMP_LIST:
                it = new ContourLinesList("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30");
                break;
            case ParamData.ISOPRATE_LIST:
                it = new ContourLinesList("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25");
                break;
            case ParamData.POLAR_SPEED_FACTOR:
                it = new Double(1D);
                break;
            case ParamData.CONFIRM_ON_EXIT:
                it = Boolean.TRUE;
                break;
            case ParamData.CONFIRM_COMMENT:
                it = Boolean.TRUE;
                break;
            case ParamData.USE_TRANSPARENT_GRIB_WIND:
                it = Boolean.TRUE;
                break;
            case ParamData.COLOR_RANGE:
                it = Boolean.FALSE;
                break;
            case ParamData.PREFERRED_WIND_DISPLAY:
                it = new WindOptionList(0);
                break;
            case ParamData.LOAD_COMPOSITE_STARTUP:
                it = new DataFile(new String[]{"ptrn"}, WWGnlUtilities.buildMessage("pattern-button"), "");
                break;
            case ParamData.GPS_BOAT_COLOR:
                it = Color.cyan;
                break;
            case ParamData.DEFAULT_ZOOM_VALUE:
                it = new Double(1.1);
                break;
            case ParamData.DEFAULT_CHART_INC_VALUE:
                it = new Double(1.0);
                break;
            case ParamData.DEFAULT_FAX_INC_VALUE:
                it = new Integer(10);
                break;
            case ParamData.DD_ZOOM_COLOR:
                it = Color.green;
                break;
            case ParamData.DISPLAY_WIND_WITH_COLOR_WIND_RANGE:
                it = Boolean.FALSE;
                break;
            case ParamData.DEFAULT_FAX_BLUR:
                it = new FaxBlurList(0);
                break;
            case ParamData.CLICK_SCROLL:
                it = Boolean.TRUE;
                break;
            case ParamData.OLD_ISOCHRONS_COLOR:
                it = Color.lightGray;
                break;
            case ParamData.SHOW_ROUTING_LABELS:
                it = Boolean.TRUE;
                break;
            case ParamData.SHOW_ISOCHRONS:
                it = Boolean.TRUE;
                break;
            case ParamData.SERIAL_PORT:
                it = new ListOfSerialPorts("COM1");
                break;
            case ParamData.TCP_PORT:
                it = "7001";
                break;
            case ParamData.UDP_PORT:
                it = "8001";
                break;
            case ParamData.GPSD_PORT:
                it = "2947";
                break;
            case ParamData.NMEA_HOST:
                it = "localhost";
                break;
            case ParamData.ROUTING_OUTPUT_FLAVOR:
                it = new RoutingOutputList(0);
                break;
            case ParamData.ROUTE_COLOR:
                it = Color.blue;
                break;
            case ParamData.ROUTING_BOAT_COLOR:
                it = Color.red;
                break;
            case ParamData.TWS_COLOR_IN_ROUTING:
                it = Color.blue;
                break;
            case ParamData.PRMSL_COLOR_IN_ROUTING:
                it = Color.red;
                break;
            case ParamData.HGT500_COLOR_IN_ROUTING:
                it = Color.cyan;
                break;
            case ParamData.WAVES_COLOR_IN_ROUTING:
                it = Color.green;
                break;
            case ParamData.AIRTMP_COLOR_IN_ROUTING:
                it = Color.black;
                break;
            case ParamData.RAIN_COLOR_IN_ROUTING:
                it = new Color(204, 204, 204); // Color.gray;
                break;
            case ParamData.BSP_COLOR_IN_ROUTING:
                it = Color.orange;
                break;
            case ParamData.AUTO_SAVE_DEFAULT_COMPOSITE:
                it = "";
                break;
            case ParamData.RELOAD_DEFAULT_COMPOSITE_INTERVAL:
                it = new Integer(0);
                break;
            case ParamData.GRAY_PANEL_OPTION:
                it = new GrayPanelOptionList(AdjustFrame.GRAY_PANEL_NO_FADE_OPTION);
                break;
            case ParamData.GRAY_PANEL_OPACITY:
                it = new Float(0.75);
                break;
            case ParamData.WAIT_ON_STARTUP:
                it = new Integer(30);
                break;
            case ParamData.EXPAND_CONTROLS_BY_DEFAULT:
                it = Boolean.TRUE;
                break;
            case ParamData.TEMPERATURE_UNIT:
                it = new TemperatureUnitList(0);
                break;
            case ParamData.ANEMOMETER_HAND_OPTION:
                it = new AnemometerHandOptionList(AnemometerHandOptionList.ARROW_HAND_OPTION);
                break;
            case ParamData.PLAY_SOUND_ON_JOB_COMPLETION:
                //    it = new SoundFile(new String[] {"wav", "ogg"}, WWGnlUtilities.buildMessage("sounds"), "." + File.separator + "sounds" + File.separator + "gong.wav");
                it = new SoundFile(new String[]{"wav", "ogg"}, WWGnlUtilities.buildMessage("sounds"), "");
                break;
            case ParamData.TRY_TO_AVOID_LAND:
                it = Boolean.FALSE;
                break;
            case ParamData.STOP_ROUTING_WHEN_CLOSER_TO:
                it = new Double(25.0);
                break;
            case ParamData.GRIB_TWS_COEFF:
                it = new Double(1D);
                break;
            case ParamData.EPHEMERIS_POSITION:
                try {
                    it = new Position(new Latitude("38 00.00 N"), new Longitude("122 00.00 W"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            case ParamData.TIDE_STATION_NAME:
                it = "Ocean Beach";
                break;
            case ParamData.EPHEMERIS_EMAIL_LIST:
                it = "olivier@lediouris.net";
                break;
            case ParamData.EPHEMERIS_EMAIL_TZ:
                it = new ListOfTimeZones("America/Los_Angeles");
                break;
            case ParamData.EPHEMERIS_EMAIL_PROVIDER:
                it = "";
                break;
            default:
                break;
        }
        return it;
    }

    /**
     * From Config file
     */
    public static void setUserValues() {
        if (data == null) {
            data = new Object[ParamData.getLabels().length][names.length];
            try {
                FileInputStream fis = new FileInputStream(ParamData.PARAM_FILE_NAME);
                DOMParser parser = WWContext.getInstance().getParser();
                synchronized (parser) {
                    parser.setValidationMode(XMLParser.NONVALIDATING);
                    parser.parse(fis);
                    XMLDocument doc = parser.getDocument();
                    for (int i = 0; i < ParamData.getLabels().length; i++) {
                        NodeList nl = doc.selectNodes("/app-parameters/param[@id='" + Integer.toString(i) + "']");
                        try {
                            data[i][ParamData.NAME_INDEX] = new ParamDisplayLabel(ParamData.getLabels()[i], ParamData.getHelptext()[i]);
                            String s = "";
                            if (nl.item(0) != null && nl.item(0).getFirstChild() != null)
                                s = nl.item(0).getFirstChild().getNodeValue();
                            else {
                                onePreference(i);
                                continue;
                            }
                            if (i == ParamData.CHART_COLOR ||                    // Colors
                                    i == ParamData.GRID_COLOR ||
                                    i == ParamData.CHART_BG_COLOR ||
                                    i == ParamData.GRIB_WIND_COLOR ||
                                    i == ParamData.GRIB_CURRENT_COLOR ||
                                    i == ParamData.PRMSL_CONTOUR ||
                                    i == ParamData.MB500_CONTOUR ||
                                    i == ParamData.WAVES_CONTOUR ||
                                    i == ParamData.TEMP_CONTOUR ||
                                    i == ParamData.PRATE_CONTOUR ||
                                    i == ParamData.GPS_BOAT_COLOR ||
                                    i == ParamData.DD_ZOOM_COLOR ||
                                    i == ParamData.OLD_ISOCHRONS_COLOR ||
                                    i == ParamData.ROUTE_COLOR ||
                                    i == ParamData.ROUTING_BOAT_COLOR ||
                                    i == ParamData.TWS_COLOR_IN_ROUTING ||
                                    i == ParamData.PRMSL_COLOR_IN_ROUTING ||
                                    i == ParamData.HGT500_COLOR_IN_ROUTING ||
                                    i == ParamData.WAVES_COLOR_IN_ROUTING ||
                                    i == ParamData.AIRTMP_COLOR_IN_ROUTING ||
                                    i == ParamData.RAIN_COLOR_IN_ROUTING ||
                                    i == ParamData.BSP_COLOR_IN_ROUTING)
                                data[i][ParamData.VALUE_INDEX] = WWGnlUtilities.buildColor(s);
                            else if (i == ParamData.ROUTING_STEP ||              // Integers
                                    i == ParamData.ROUTING_FORK_WIDTH ||
                                    i == ParamData.TCP_PORT ||
                                    i == ParamData.UDP_PORT ||
                                    i == ParamData.GPSD_PORT ||
                                    i == ParamData.NMEA_POLLING_FREQ ||
                                    i == ParamData.NMEA_FALLBACK_TIMEOUT ||
                                    i == ParamData.CHART_LINE_THICK ||
                                    i == ParamData.AVOID_TWS_GT ||
                                    i == ParamData.AVOID_TWA_LT ||
                                    i == ParamData.INTERVAL_BETWEEN_ISOBARS ||
                                    i == ParamData.DEFAULT_FAX_INC_VALUE ||
                                    i == ParamData.RELOAD_DEFAULT_COMPOSITE_INTERVAL ||
                                    i == ParamData.WAIT_ON_STARTUP)
                                data[i][ParamData.VALUE_INDEX] = new Integer(s);
                            else if (i == ParamData.DEFAULT_FONT)
                                data[i][ParamData.VALUE_INDEX] = FontPanel.stringToFont(s);
                            else if (i == ParamData.ROUTING_TIME_INTERVAL ||     // Doubles
                                    i == ParamData.POLAR_SPEED_FACTOR ||
                                    i == ParamData.DEFAULT_ZOOM_VALUE ||
                                    i == ParamData.DEFAULT_CHART_INC_VALUE ||
                                    i == ParamData.STOP_ROUTING_WHEN_CLOSER_TO ||
                                    i == ParamData.GRIB_TWS_COEFF)
                                data[i][ParamData.VALUE_INDEX] = new Double(s);
                            else if (i == ParamData.ROUTING_FROM_CURR_LOC ||     // Booleans
                                    i == ParamData.AUTO_UPDATES ||
                                    i == ParamData.SHOW_NOTIFICATIONS ||
                                    i == ParamData.CONFIRM_DD_ZOOM ||
                                    i == ParamData.STOP_ROUTING_ON_EXHAUSTED_GRIB ||
                                    i == ParamData.CONFIRM_ON_EXIT ||
                                    i == ParamData.CONFIRM_COMMENT ||
                                    i == ParamData.USE_TRANSPARENT_GRIB_WIND ||
                                    i == ParamData.COLOR_RANGE ||
                                    i == ParamData.DISPLAY_WIND_WITH_COLOR_WIND_RANGE ||
                                    i == ParamData.CLICK_SCROLL ||
                                    i == ParamData.SHOW_ROUTING_LABELS ||
                                    i == ParamData.SHOW_ISOCHRONS ||
                                    i == ParamData.EXPAND_CONTROLS_BY_DEFAULT ||
                                    i == ParamData.TRY_TO_AVOID_LAND)
                                data[i][ParamData.VALUE_INDEX] = new Boolean(s);
                            else if (i == ParamData.POLAR_FILE_LOC)              // DataFiles, Polars
                                data[i][ParamData.VALUE_INDEX] = new DataFile(new String[]{"xml", "polar-coeff"}, "Polars", s);
                            else if (i == ParamData.LOAD_COMPOSITE_STARTUP)      // DataFiles, Patterns
                                data[i][ParamData.VALUE_INDEX] = new DataFile(new String[]{"ptrn"}, "Patterns", s);
                            else if (i == ParamData.COMPOSITE_ROOT_DIR)               // DataDirectories
                                data[i][ParamData.VALUE_INDEX] = new DataDirectory(WWGnlUtilities.buildMessage("composite-button"), s);
                            else if (i == ParamData.FAX_FILES_LOC)
                                data[i][ParamData.VALUE_INDEX] = new DataPath(s);
                            else if (i == ParamData.GRIB_FILES_LOC)
                                data[i][ParamData.VALUE_INDEX] = new DataPath(s);
                            else if (i == ParamData.PATTERN_DIR)
                                data[i][ParamData.VALUE_INDEX] = new DataDirectory(WWGnlUtilities.buildMessage("pattern-button"), s);
                            else if (i == ParamData.FAX_TRANSPARENCY ||
                                    i == ParamData.GRAY_PANEL_OPACITY)          // Float
                                data[i][ParamData.VALUE_INDEX] = new Float(s);
                            else if (i == ParamData.LOOK_AND_FEEL)               // Look and Feel (list)
                                data[i][ParamData.VALUE_INDEX] = new ListOfLookAndFeel(s);
                            else if (i == ParamData.PREFERRED_WIND_DISPLAY)      // Wind Display Options (list)
                                data[i][ParamData.VALUE_INDEX] = new WindOptionList(Integer.parseInt(s));
                            else if (i == ParamData.ISOBARS_LIST ||              // Contour Lines
                                    i == ParamData.ISOHEIGHT500_LIST ||
                                    i == ParamData.ISOHEIGHTWAVES_LIST ||
                                    i == ParamData.ISOTEMP_LIST ||
                                    i == ParamData.ISO_TWS_LIST ||
                                    i == ParamData.ISOPRATE_LIST)
                                data[i][ParamData.VALUE_INDEX] = new ContourLinesList(s);
                            else if (i == ParamData.DEFAULT_FAX_BLUR)
                                data[i][ParamData.VALUE_INDEX] = new FaxBlurList(Integer.parseInt(s));
                            else if (i == ParamData.GRAY_PANEL_OPTION)
                                data[i][ParamData.VALUE_INDEX] = new GrayPanelOptionList(Integer.parseInt(s));
                            else if (i == ParamData.SERIAL_PORT)
                                data[i][ParamData.VALUE_INDEX] = new ListOfSerialPorts(s);
                            else if (i == ParamData.ROUTING_OUTPUT_FLAVOR)
                                data[i][ParamData.VALUE_INDEX] = new RoutingOutputList(Integer.parseInt(s));
                            else if (i == ParamData.TEMPERATURE_UNIT)
                                data[i][ParamData.VALUE_INDEX] = new TemperatureUnitList(Integer.parseInt(s));
                            else if (i == ParamData.ANEMOMETER_HAND_OPTION)
                                data[i][ParamData.VALUE_INDEX] = new AnemometerHandOptionList(Integer.parseInt(s));
                            else if (i == ParamData.PLAY_SOUND_ON_JOB_COMPLETION)              // DataFiles, Sound
                            {
//              if (s != null && s.trim().length() > 0)
                                data[i][ParamData.VALUE_INDEX] = new SoundFile(new String[]{"wav", "ogg"}, "Sounds", s);
                            } else if (i == ParamData.EPHEMERIS_POSITION)
                                data[i][ParamData.VALUE_INDEX] = new Position(s);
                            else if (i == ParamData.EPHEMERIS_EMAIL_TZ)
                                data[i][ParamData.VALUE_INDEX] = new ListOfTimeZones(s);
                            else                                               // Strings
                                data[i][ParamData.VALUE_INDEX] = s;
                        } catch (Exception ex) {
                            data[i][ParamData.VALUE_INDEX] = oneFactorySetting(i);
                        }
                    }
                    fis.close();
                }
            } catch (Exception e) {
                initTableValues();
//      if (StaticObjects.getInstance().verboseLevel >= StaticObjects.VERBOSE) 
//        System.out.println("Defaulting values");
//      e.printStackTrace();
            }
        }
    }

    // By Category
    private final static int DISPLAY_COLOR_PRM = 0;
    private final static int DISPLAY_OTHER_PRM = 1;
    private final static int ROUTING_PRM = 2;
    private final static int MISC_PRM = 3;
    private final static int HEADLESS_PRM = 4;

    private int currentCategoryIndex = -1;

    private static Object[] categoryIndexes = new Object[]
            {new int[] // Colors
                    {ParamData.CHART_COLOR,
                            ParamData.GRID_COLOR,
                            ParamData.CHART_BG_COLOR,
                            ParamData.GRIB_WIND_COLOR,
                            ParamData.GRIB_CURRENT_COLOR,
                            ParamData.USE_TRANSPARENT_GRIB_WIND,
                            ParamData.COLOR_RANGE,
                            ParamData.PRMSL_CONTOUR,
                            ParamData.MB500_CONTOUR,
                            ParamData.WAVES_CONTOUR,
                            ParamData.TEMP_CONTOUR,
                            ParamData.PRATE_CONTOUR,
                            ParamData.GPS_BOAT_COLOR,
                            ParamData.DD_ZOOM_COLOR,
                            ParamData.OLD_ISOCHRONS_COLOR,
                            ParamData.ROUTE_COLOR,
                            ParamData.ROUTING_BOAT_COLOR,
                            ParamData.TWS_COLOR_IN_ROUTING,
                            ParamData.PRMSL_COLOR_IN_ROUTING,
                            ParamData.HGT500_COLOR_IN_ROUTING,
                            ParamData.WAVES_COLOR_IN_ROUTING,
                            ParamData.AIRTMP_COLOR_IN_ROUTING,
                            ParamData.RAIN_COLOR_IN_ROUTING,
                            ParamData.BSP_COLOR_IN_ROUTING},
                    new int[] // Display
                            {ParamData.CHART_LINE_THICK,
                                    ParamData.FAX_TRANSPARENCY,
                                    ParamData.DEFAULT_FONT,
                                    ParamData.PREFERRED_WIND_DISPLAY,
                                    ParamData.DISPLAY_WIND_WITH_COLOR_WIND_RANGE,
                                    ParamData.DEFAULT_FAX_BLUR,
                                    ParamData.CLICK_SCROLL,
                                    ParamData.WAIT_ON_STARTUP,
                                    ParamData.EXPAND_CONTROLS_BY_DEFAULT,
                                    ParamData.TEMPERATURE_UNIT},
                    new int[] // Routing, NMEA
                            {ParamData.NMEA_SERVER_URL,
                                    ParamData.SERIAL_PORT,
                                    ParamData.TCP_PORT,
                                    ParamData.UDP_PORT,
                                    ParamData.GPSD_PORT,
                                    ParamData.NMEA_HOST,
                                    ParamData.NMEA_POLLING_FREQ,
                                    ParamData.NMEA_FALLBACK_TIMEOUT,
                                    ParamData.POLAR_FILE_LOC,
                                    ParamData.ROUTING_STEP,
                                    ParamData.ROUTING_FORK_WIDTH,
                                    ParamData.ROUTING_TIME_INTERVAL,
                                    ParamData.ROUTING_FROM_CURR_LOC,
                                    ParamData.AVOID_TWS_GT,
                                    ParamData.AVOID_TWA_LT,
                                    ParamData.STOP_ROUTING_ON_EXHAUSTED_GRIB,
                                    ParamData.POLAR_SPEED_FACTOR,
                                    ParamData.SHOW_ROUTING_LABELS,
                                    ParamData.SHOW_ISOCHRONS,
                                    ParamData.ROUTING_OUTPUT_FLAVOR,
                                    ParamData.ANEMOMETER_HAND_OPTION,
                                    ParamData.TRY_TO_AVOID_LAND,
                                    ParamData.STOP_ROUTING_WHEN_CLOSER_TO,
                                    ParamData.GRIB_TWS_COEFF},
                    new int[] // Misc
                            {ParamData.GRIB_FILES_LOC,
                                    ParamData.FAX_FILES_LOC,
                                    ParamData.COMPOSITE_ROOT_DIR,
                                    ParamData.PATTERN_DIR,
                                    ParamData.AUTO_UPDATES,
                                    ParamData.SHOW_NOTIFICATIONS,
                                    ParamData.CONFIRM_DD_ZOOM,
                                    ParamData.INTERVAL_BETWEEN_ISOBARS,
                                    ParamData.ISOBARS_LIST,
                                    ParamData.ISOHEIGHT500_LIST,
                                    ParamData.ISOHEIGHTWAVES_LIST,
                                    ParamData.ISOTEMP_LIST,
                                    ParamData.ISOPRATE_LIST,
                                    ParamData.ISO_TWS_LIST,
                                    ParamData.CONFIRM_ON_EXIT,
                                    ParamData.LOAD_COMPOSITE_STARTUP,
                                    ParamData.AUTO_SAVE_DEFAULT_COMPOSITE,
                                    ParamData.RELOAD_DEFAULT_COMPOSITE_INTERVAL,
                                    ParamData.DEFAULT_ZOOM_VALUE,
                                    ParamData.DEFAULT_CHART_INC_VALUE,
                                    ParamData.DEFAULT_FAX_INC_VALUE,
                                    ParamData.GRAY_PANEL_OPTION,
                                    ParamData.GRAY_PANEL_OPACITY,
                                    ParamData.PLAY_SOUND_ON_JOB_COMPLETION,
                                    ParamData.CONFIRM_COMMENT},
                    new int[]
                            { // Headless
                                    ParamData.EPHEMERIS_POSITION,
                                    ParamData.TIDE_STATION_NAME,
                                    ParamData.EPHEMERIS_EMAIL_LIST,
                                    ParamData.EPHEMERIS_EMAIL_TZ,
                                    ParamData.EPHEMERIS_EMAIL_PROVIDER
                            }
            };

    private Object[][] mkDataArray(int idx) {
        int len = ((int[]) categoryIndexes[idx]).length;
        Object[][] oa = new Object[len][2];
        for (int i = 0; i < len; i++) {
            int index = ((int[]) categoryIndexes[idx])[i];
            try {
                Object cloned = null;
                if (data[index][ParamData.VALUE_INDEX] == null)
                    data[index][ParamData.VALUE_INDEX] = oneFactorySetting(index);
                if (data[index][ParamData.VALUE_INDEX] instanceof Color)
                    cloned = new Color(((Color) data[index][ParamData.VALUE_INDEX]).getRed(),
                            ((Color) data[index][ParamData.VALUE_INDEX]).getGreen(),
                            ((Color) data[index][ParamData.VALUE_INDEX]).getBlue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof Integer)
                    cloned = new Integer(((Integer) data[index][ParamData.VALUE_INDEX]).intValue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof Double)
                    cloned = new Double(((Double) data[index][ParamData.VALUE_INDEX]).doubleValue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof Float)
                    cloned = new Float(((Float) data[index][ParamData.VALUE_INDEX]).floatValue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof String)
                    cloned = new String(data[index][ParamData.VALUE_INDEX].toString());
                else if (data[index][ParamData.VALUE_INDEX] instanceof Boolean)
                    cloned = ((Boolean) data[index][ParamData.VALUE_INDEX]).booleanValue();
                else if (data[index][ParamData.VALUE_INDEX] instanceof SoundFile)
                    cloned = new SoundFile(((SoundFile) data[index][ParamData.VALUE_INDEX]).getFileExt(),
                            ((SoundFile) data[index][ParamData.VALUE_INDEX]).getDesc(),
                            ((SoundFile) data[index][ParamData.VALUE_INDEX]).getValue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof DataFile)
                    cloned = new DataFile(((DataFile) data[index][ParamData.VALUE_INDEX]).getFileExt(),
                            ((DataFile) data[index][ParamData.VALUE_INDEX]).getDesc(),
                            ((DataFile) data[index][ParamData.VALUE_INDEX]).getValue());
                else if (data[index][ParamData.VALUE_INDEX] instanceof DataDirectory)
                    cloned = new DataDirectory(((DataDirectory) data[index][ParamData.VALUE_INDEX]).desc,
                            ((DataDirectory) data[index][ParamData.VALUE_INDEX]).value);
                else if (data[index][ParamData.VALUE_INDEX] instanceof ListOfLookAndFeel)
                    cloned = new ListOfLookAndFeel(((ListOfLookAndFeel) data[index][ParamData.VALUE_INDEX]).currentValue);
                else if (data[index][ParamData.VALUE_INDEX] instanceof AnemometerHandOptionList)
                    cloned = new AnemometerHandOptionList(((AnemometerHandOptionList) data[index][ParamData.VALUE_INDEX]).getCurrentIndex());
                else if (data[index][ParamData.VALUE_INDEX] instanceof TemperatureUnitList)
                    cloned = new TemperatureUnitList(((TemperatureUnitList) data[index][ParamData.VALUE_INDEX]).getCurrentIndex());
                else if (data[index][ParamData.VALUE_INDEX] instanceof ContourLinesList)
                    cloned = new ContourLinesList(((ContourLinesList) data[index][ParamData.VALUE_INDEX]).toString());
                else if (data[index][ParamData.VALUE_INDEX] instanceof Font) {
                    Font f = (Font) data[index][ParamData.VALUE_INDEX];
                    cloned = new Font(f.getName(), f.getStyle(), f.getSize());
                } else {
//        WWContext.getInstance().fireLogging("Cloning a [" + (data[index][ParamData.VALUE_INDEX]).getClass().getName() + "] is not supported.");
//        System.out.println("Cloning a [" + (data[index][ParamData.VALUE_INDEX]).getClass().getName() + "] is not supported.");
                    cloned = data[index][ParamData.VALUE_INDEX];
                }
                oa[i] = new Object[]{data[index][ParamData.NAME_INDEX], cloned};
            } catch (Exception ex) {
                WWContext.getInstance().fireExceptionLogging(ex);
                ex.printStackTrace();
            }
        }
        return oa;
    }

    public void setDisplayColorPrm() {
        setObject(mkDataArray(DISPLAY_COLOR_PRM));
        currentCategoryIndex = DISPLAY_COLOR_PRM;
    }

    public void setDisplayOtherPrm() {
        setObject(mkDataArray(DISPLAY_OTHER_PRM));
        currentCategoryIndex = DISPLAY_OTHER_PRM;
    }

    public void setRoutingPrm() {
        setObject(mkDataArray(ROUTING_PRM));
        currentCategoryIndex = ROUTING_PRM;
    }

    public void setMiscPrm() {
        setObject(mkDataArray(MISC_PRM));
        currentCategoryIndex = MISC_PRM;
    }

    public void setHeadlessPrm() {
        setObject(mkDataArray(HEADLESS_PRM));
        currentCategoryIndex = HEADLESS_PRM;
    }

    public void updateData() {
        // Update data
        if (currentCategoryIndex > -1) {
            for (int i = 0; i < localData.length; i++) {
                String before = "";
                try {
                    before = (data[((int[]) categoryIndexes[currentCategoryIndex])[i]][ParamData.VALUE_INDEX]).toString();
                } catch (NullPointerException npe) {
                }
                if (localData[i][ParamData.VALUE_INDEX] == null)
                    continue;
                String after = localData[i][ParamData.VALUE_INDEX].toString();
                if (!before.equals(after)) {
                    boolean ok2go = true;
                    int currentIndex = ((int[]) categoryIndexes[currentCategoryIndex])[i];
                    if (currentIndex == ParamData.ROUTING_STEP ||
                            currentIndex == ParamData.ROUTING_FORK_WIDTH ||
                            currentIndex == ParamData.NMEA_POLLING_FREQ ||
                            currentIndex == ParamData.NMEA_FALLBACK_TIMEOUT ||
                            currentIndex == ParamData.CHART_LINE_THICK ||
                            currentIndex == ParamData.AVOID_TWS_GT ||
                            currentIndex == ParamData.AVOID_TWA_LT ||
                            currentIndex == ParamData.DEFAULT_FAX_INC_VALUE ||
                            currentIndex == ParamData.RELOAD_DEFAULT_COMPOSITE_INTERVAL ||
                            currentIndex == ParamData.WAIT_ON_STARTUP) // The int values
                    {
                        try { /* int x = */
                            Integer.parseInt(after);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    e.getMessage(),
                                    WWGnlUtilities.buildMessage("modifying-parameters"),
                                    JOptionPane.ERROR_MESSAGE);
                            ok2go = false;
                        }
                    }
                    if (currentIndex == ParamData.EPHEMERIS_POSITION) // Position
                    {
                        try {
                            Position p = new Position(after);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                    ex.getMessage(),
                                    WWGnlUtilities.buildMessage("modifying-parameters"),
                                    JOptionPane.ERROR_MESSAGE);
                            ok2go = false;
                        }
                    }
                    if (currentIndex == ParamData.ROUTING_TIME_INTERVAL || // The double values
                            currentIndex == ParamData.POLAR_SPEED_FACTOR ||
                            currentIndex == ParamData.DEFAULT_ZOOM_VALUE ||
                            currentIndex == ParamData.DEFAULT_CHART_INC_VALUE ||
                            currentIndex == ParamData.STOP_ROUTING_WHEN_CLOSER_TO ||
                            currentIndex == ParamData.GRIB_TWS_COEFF) {
                        try { /* double d = */
                            Double.parseDouble(after);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    e.getMessage(), WWGnlUtilities.buildMessage("modifying-parameters"),
                                    JOptionPane.ERROR_MESSAGE);
                            ok2go = false;
                        }
                    }
                    if (currentIndex == ParamData.FAX_TRANSPARENCY ||
                            currentIndex == ParamData.GRAY_PANEL_OPACITY) // The float values
                    {
                        try {
                            float f = Float.parseFloat(after);
                            // Specific to some line:
                            if (currentIndex == ParamData.FAX_TRANSPARENCY ||
                                    currentIndex == ParamData.GRAY_PANEL_OPACITY) {
                                if (f < 0.0f || f > 1.0f) {
                                    JOptionPane.showMessageDialog(this,
                                            WWGnlUtilities.buildMessage("fax-transparency"),
                                            WWGnlUtilities.buildMessage("modifying-parameters"),
                                            JOptionPane.ERROR_MESSAGE);
                                    ok2go = false;
                                }
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    e.getMessage(), WWGnlUtilities.buildMessage("modifying-parameters"),
                                    JOptionPane.ERROR_MESSAGE);
                            ok2go = false;
                        }
                    } else if (currentIndex == ParamData.CHART_COLOR ||
                            currentIndex == ParamData.GRID_COLOR ||
                            currentIndex == ParamData.CHART_BG_COLOR ||
                            currentIndex == ParamData.GRIB_WIND_COLOR ||
                            currentIndex == ParamData.GRIB_CURRENT_COLOR ||
                            currentIndex == ParamData.PRMSL_CONTOUR ||
                            currentIndex == ParamData.MB500_CONTOUR ||
                            currentIndex == ParamData.WAVES_CONTOUR ||
                            currentIndex == ParamData.TEMP_CONTOUR ||
                            currentIndex == ParamData.PRATE_CONTOUR ||
                            currentIndex == ParamData.GPS_BOAT_COLOR ||
                            currentIndex == ParamData.DD_ZOOM_COLOR ||
                            currentIndex == ParamData.OLD_ISOCHRONS_COLOR ||
                            currentIndex == ParamData.ROUTE_COLOR ||
                            currentIndex == ParamData.ROUTING_BOAT_COLOR ||
                            currentIndex == ParamData.TWS_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.PRMSL_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.HGT500_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.WAVES_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.AIRTMP_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.RAIN_COLOR_IN_ROUTING ||
                            currentIndex == ParamData.BSP_COLOR_IN_ROUTING)            // The colors(s)
                    {
//            try { Color c = new Color(after); }
//            catch (Exception e) 
//            { 
//              JOptionPane.showMessageDialog(this, 
//                                            e.getMessage(), 
//                                            "Modifying Parameters", 
//                                            JOptionPane.ERROR_MESSAGE);
//              ok2go = false; 
//            }    
                    } else if (currentIndex == ParamData.ROUTING_FROM_CURR_LOC || // The boolean(s)
                            currentIndex == ParamData.AUTO_UPDATES ||
                            currentIndex == ParamData.SHOW_NOTIFICATIONS ||
                            currentIndex == ParamData.CONFIRM_DD_ZOOM ||
                            currentIndex == ParamData.STOP_ROUTING_ON_EXHAUSTED_GRIB ||
                            currentIndex == ParamData.USE_TRANSPARENT_GRIB_WIND ||
                            currentIndex == ParamData.COLOR_RANGE ||
                            currentIndex == ParamData.DISPLAY_WIND_WITH_COLOR_WIND_RANGE ||
                            currentIndex == ParamData.CLICK_SCROLL ||
                            currentIndex == ParamData.EXPAND_CONTROLS_BY_DEFAULT ||
                            currentIndex == ParamData.TRY_TO_AVOID_LAND) {
                        try { /* boolean b = */
                            new Boolean(after);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this,
                                    e.getMessage(),
                                    WWGnlUtilities.buildMessage("modifying-parameters"),
                                    JOptionPane.ERROR_MESSAGE);
                            ok2go = false;
                        }
                    }
//        if (currentIndex == ParamData.PLAY_SOUND_ON_JOB_COMPLETION)
//        {
                    // Play it?
//          final String soundName = ((ParamPanel.SoundFile) ParamPanel.data[ParamData.PLAY_SOUND_ON_JOB_COMPLETION][ParamData.VALUE_INDEX]).toString();
//        }
                    // Ok to go
                    if (ok2go) {
                        data[((int[]) categoryIndexes[currentCategoryIndex])[i]][ParamData.NAME_INDEX] = localData[i][ParamData.NAME_INDEX];
                        data[((int[]) categoryIndexes[currentCategoryIndex])[i]][ParamData.VALUE_INDEX] = localData[i][ParamData.VALUE_INDEX];
//          StaticObjects.getInstance().fireParametersHaveChanged();
                        saveParameters();
                        // Repaint or any action required?
                        if (currentIndex == ParamData.CHART_COLOR)
                            WWContext.getInstance().fireChartLineColorChanged((Color) ParamPanel.data[ParamData.CHART_COLOR][ParamData.VALUE_INDEX]);
                        if (currentIndex == ParamData.GRID_COLOR)
                            WWContext.getInstance().fireGridColorChanged((Color) ParamPanel.data[ParamData.GRID_COLOR][ParamData.VALUE_INDEX]);
                        if (currentIndex == ParamData.CHART_BG_COLOR)
                            WWContext.getInstance().fireChartBackgroundColorChanged((Color) ParamPanel.data[ParamData.CHART_BG_COLOR][ParamData.VALUE_INDEX]);
                        if (currentIndex == ParamData.DD_ZOOM_COLOR)
                            WWContext.getInstance().fireDDZColorChanged((Color) ParamPanel.data[ParamData.DD_ZOOM_COLOR][ParamData.VALUE_INDEX]);
                        if (currentIndex == ParamData.CHART_LINE_THICK)
                            WWContext.getInstance().fireChartLineThicknessChanged(((Integer) ParamPanel.data[ParamData.CHART_LINE_THICK][ParamData.VALUE_INDEX]).intValue());
                        if (currentIndex == ParamData.FAX_TRANSPARENCY ||
                                currentIndex == ParamData.GRIB_WIND_COLOR ||
                                currentIndex == ParamData.GRIB_CURRENT_COLOR ||
                                currentIndex == ParamData.PRMSL_CONTOUR ||
                                currentIndex == ParamData.MB500_CONTOUR ||
                                currentIndex == ParamData.WAVES_CONTOUR ||
                                currentIndex == ParamData.TEMP_CONTOUR ||
                                currentIndex == ParamData.PRATE_CONTOUR ||
                                currentIndex == ParamData.USE_TRANSPARENT_GRIB_WIND ||
                                currentIndex == ParamData.COLOR_RANGE ||
                                currentIndex == ParamData.DISPLAY_WIND_WITH_COLOR_WIND_RANGE ||
                                currentIndex == ParamData.GPS_BOAT_COLOR ||
                                currentIndex == ParamData.OLD_ISOCHRONS_COLOR ||
                                currentIndex == ParamData.ROUTE_COLOR ||
                                currentIndex == ParamData.ROUTING_BOAT_COLOR)
                            WWContext.getInstance().fireChartRepaint();
                        if (currentIndex == ParamData.LOOK_AND_FEEL) // TASK Remove, unused
                            WWContext.getInstance().fireLookAndFeelChanged(((ListOfLookAndFeel) ParamPanel.data[ParamData.LOOK_AND_FEEL][ParamData.VALUE_INDEX]).currentValue);
                        if (currentIndex == ParamData.CONFIRM_DD_ZOOM)
                            WWContext.getInstance().fireDDZoomConfirmChanged(((Boolean) ParamPanel.data[ParamData.CONFIRM_DD_ZOOM][ParamData.VALUE_INDEX]).booleanValue());
                    }
                }
            }
        }
    }

    // When a category is selected in the left pane
    public void setObject(Object oaa) {
        updateData();
        localData = (Object[][]) oaa;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        table.repaint();
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(181, 300));
//  parent.setSize(new Dimension(400, 378));
        bottomPanel.setLayout(new BorderLayout());
        centerPane.setLayout(borderLayout2);
        fileNameLabel.setText(" ");
        helpTextArea.setPreferredSize(new Dimension(20, 60));
        helpTextArea.setEditable(false);
        helpTextArea.setText("...");
        helpTextArea.setBackground(new Color(247, 255, 196));
        helpTextArea.setFont(new Font("Tahoma", 0, 10));
        titleLabel.setText(WWGnlUtilities.buildMessage("application-parameters"));
        topPanel.add(fileNameLabel, null);
        topPanel.add(titleLabel, null);
        this.add(topPanel, BorderLayout.NORTH);
        bottomPanel.add(textAreaScrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(centerPane, BorderLayout.CENTER);

        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        if (info != null) {
            lnfValues = new String[info.length];
            for (int i = 0; i < info.length; i++) {
                lnfValues[i] = info[i].getName();
            }
        }
        initTable();
        SelectionListener listener = new SelectionListener(table);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        setUserValues();
    }

    private void initTable() {
        dataModel = new DefaultTableModel() // AbstractTableModel()
        {
            @Override
            public int getColumnCount() {
                return names.length;
            }

            @Override
            public int getRowCount() {
                return localData.length;
            }

            @Override
            public Object getValueAt(int row, int col) {
                return localData[row][col];
            }

            @Override
            public String getColumnName(int column) {
                return names[column];
            }

            @Override
            public Class getColumnClass(int c) {
                if (c == 1) {
                    System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
                    System.out.println("  Value is [" + getValueAt(0, c).toString() + "]");
                }
                return getValueAt(0, c).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return (col == 1);
            } // Second column only

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                try {
                    localData[row][column] = aValue;
                } catch (Exception ex) {
                    System.err.println("Setting value row " + row + ", column " + column + " : [" + aValue + "]");
                    System.err.println(ex.getLocalizedMessage());
                }
            }
        };
        table = new JTable(dataModel) {
            /* For the tooltip text */
            public Component prepareRenderer(TableCellRenderer renderer,
                                             int rowIndex,
                                             int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    try {
                        Object o = getValueAt(rowIndex, vColIndex);
//          System.out.println("Object is " + (o==null?"null":("a " + o.getClass().getName())));
                        if (o != null)
                            jc.setToolTipText(o.toString());
                    } catch (Exception ex) {
                        System.err.println("From ParamPanel:" + ex.getMessage());
                        WWContext.getInstance().fireExceptionLogging(ex);
                        ex.printStackTrace();
                    }
                }
                return c;
            }
        };
        // Set a specific #Editor for a special column/line cell

//  table.getColumn(KEY).setCellRenderer(new ValueTableCellRenderer());

        TableColumn secondColumn = table.getColumn(VALUE);
        secondColumn.setCellEditor(new ParamEditor());
        secondColumn.setCellRenderer(new SpecialTableCellRenderer());
//  secondColumn.setCellRenderer(new CustomTableCellRenderer());

        scrollPane = new JScrollPane(table);
        centerPane.add(scrollPane, BorderLayout.CENTER);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new JTableFocusChangeListener(table));
    }

    
    public class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            System.out.println("TableCellRenderer is a " + comp.getClass().getName());
            return comp;
        }
    }

    
    public class SpecialTableCellRenderer
            extends JLabel
            implements TableCellRenderer {
        private final transient List<Color> rowColors = Arrays.asList(Color.WHITE, Color.LIGHT_GRAY);

        private transient Object curValue = null;
        private transient Color bgColor = null;

        private boolean bold = false;

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
//    bold = (column == 1);
            if (value == null) {
                curValue = table.getValueAt(row, column);
                String lineName = "";
                if (column == 1)
                    lineName = table.getValueAt(row, 0).toString();
                System.out.println("Defaulting " + ((lineName != null && lineName.trim().length() > 0) ? "[" + lineName + "]" : "") + " to curValue [" + (curValue == null ? "null" : curValue.getClass().getName()) + "]");
                curValue = "";
            } else
                curValue = value;

            if (!isSelected) {
//      System.out.println("ROW Idx:" + (row % 2));
                bgColor = rowColors.get(row % 2);
//      this.setBackground(rowColors.get(row % 2)); 
//      ((AbstractTableModel)table.getModel()).fireTableRowsUpdated(row, row);
            }
            return this;
        }

        public void paintComponent(Graphics g) {
            if (curValue instanceof Color) {
                if (curValue != null) {
                    if (false && bgColor != null) {
                        Color c = g.getColor();
                        g.setColor(bgColor);
                        g.fillRect(0, 0, this.getWidth(), this.getHeight());
                        g.setColor(c);
                    }
                    g.setColor((Color) curValue);
                }
//      g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);        
                g.fillRect(2, 2, Math.min(getWidth() - 5, 20), getHeight() - 5);
                g.setColor(Color.black);
                g.drawRect(2, 2, Math.min(getWidth() - 5, 20), getHeight() - 5);
            } else if (curValue instanceof Font) {
                Font f = (Font) curValue;
                if (f.getSize() == 0)
                    f = f.deriveFont(12f);
                g.setFont(f);
                g.drawString("Default Swing Font", 1, getHeight() - 1);
            } else {
                if (curValue != null) {
                    System.out.println("curValue is a " + curValue.getClass().getName());
                    System.out.println("-> Value is " + curValue.toString());
//        g.drawString((String)curValue, 1, getHeight() - 1);
                    if (false && bgColor != null) {
                        Color c = g.getColor();
                        g.setColor(bgColor);
                        g.fillRect(0, 0, this.getWidth(), this.getHeight());
                        g.setColor(c);
                    }
                    if (bold)
                        g.setFont(g.getFont().deriveFont(Font.BOLD, g.getFont().getSize()));
                    g.drawString(curValue.toString(), 1, getHeight() - 1);
                }
//        else
//        {
//          System.out.println("Not a color, but null");
//        }
            }
        }
    }

    protected static String[] lnfValues = null; // Populated in the constructor

    
    public class ParamEditor
            extends JComponent
            implements TableCellEditor {
        JComponent componentToApply;
        protected transient Vector<CellEditorListener> listeners;
        protected transient Object originalValue;
        JComboBox lnfList = new JComboBox(lnfValues); // Should not be used
        WindOptionComboBox wdoCombo = new WindOptionComboBox();
        FaxBlurListComboBox blurCombo = new FaxBlurListComboBox();
        GrayPanelOptionListComboBox gpOptionCombo = new GrayPanelOptionListComboBox();
        TemperatureUnitListComboBox tempUnitCombo = new TemperatureUnitListComboBox();
        AnemometerHandOptionListComboBox anemoCombo = new AnemometerHandOptionListComboBox();
        JComboBox serialPortList = new JComboBox(new String[] { "Empty Serial Port list"}); // TODO Remove this  SerialPortList.listSerialPorts());
        JComboBox timeZonesList = new JComboBox(TimeZone.getAvailableIDs());
        RoutingOptionComboBox roCombo = new RoutingOptionComboBox();

        public ParamEditor() {
            super();
            listeners = new Vector<CellEditorListener>();
        }

        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            if (value == null) {
                System.out.print("New value is null");
                if (originalValue != null)
                    value = originalValue;
                else
                    System.out.print(", original too");
                System.out.println(".");
            }
            originalValue = value;
            if (column == 1 && value instanceof Color) {
                componentToApply = new ColorPickerCellEditor((Color) value);
            } else if (column == 1 && value instanceof SoundFile) {
                SoundFile sf = (SoundFile) value;
                componentToApply = new FilePickerCellEditor(sf, sf.getFileExt(), sf.getDesc());
            } else if (column == 1 && value instanceof DataFile) {
                DataFile df = (DataFile) value;
                componentToApply = new FilePickerCellEditor(df, df.getFileExt(), df.getDesc());
            } else if (column == 1 && value instanceof DataDirectory) {
                DataDirectory dd = (DataDirectory) value;
                componentToApply = new DirectoryPickerCellEditor(dd, dd.getDesc());
            } else if (column == 1 && value instanceof DataPath) {
                DataPath dp = (DataPath) value;
                componentToApply = new FieldPlusPathPicker(dp);
            } else if (column == 1 && value instanceof ContourLinesList) {
                ContourLinesList cll = (ContourLinesList) value;
                componentToApply = new FieldPlusLOVPicker(cll);
            } else if (column == 1 && value instanceof Font) {
//      String font = FontPanel.fontToString((Font)value);
                componentToApply = new FieldPlusFontPicker((Font) value);
            } else if (column == 1 && value instanceof Boolean) {
                componentToApply = new BooleanCellEditor((Boolean) value);
            } else if (column == 1 && value instanceof ListOfLookAndFeel) {
                componentToApply = lnfList;
                lnfList.setSelectedItem(value);
            } else if (column == 1 && value instanceof WindOptionList) {
                componentToApply = wdoCombo;
                wdoCombo.setSelectedItem(((WindOptionList) value).getCurrentValue());
            } else if (column == 1 && value instanceof RoutingOutputList) {
                componentToApply = roCombo;
                roCombo.setSelectedItem(((RoutingOutputList) value).getCurrentValue());
            } else if (column == 1 && value instanceof FaxBlurList) {
                componentToApply = blurCombo;
                blurCombo.setSelectedItem(((FaxBlurList) value).getCurrentValue());
            } else if (column == 1 && value instanceof GrayPanelOptionList) {
                componentToApply = gpOptionCombo;
                gpOptionCombo.setSelectedItem(((GrayPanelOptionList) value).getCurrentValue());
            } else if (column == 1 && value instanceof TemperatureUnitList) {
                componentToApply = tempUnitCombo;
                tempUnitCombo.setSelectedItem(((TemperatureUnitList) value).getCurrentValue());
            } else if (column == 1 && value instanceof AnemometerHandOptionList) {
                componentToApply = anemoCombo;
                anemoCombo.setSelectedItem(((AnemometerHandOptionList) value).getCurrentValue());
            } else if (column == 1 && value instanceof ListOfSerialPorts) {
                componentToApply = serialPortList;
                serialPortList.setSelectedItem(value);
            } else if (column == 1 && value instanceof ListOfTimeZones) {
                componentToApply = timeZonesList;
                timeZonesList.setSelectedItem(value);
            } else // The rest, Strings.
            {
                if (value != null) {
                    WWContext.getInstance().fireLogging("At row " + row + ", value is a " + value.getClass().getName());
                    componentToApply = new JTextField(value.toString());
                } else
                    System.out.println("Null value in the ParamEditor");
            }
            return componentToApply;
        }

        public Object getCellEditorValue() {
//    System.out.println("getCellEditorValue invoked");
            if (componentToApply instanceof JTextField) {
                if (originalValue instanceof String)
                    return ((JTextField) componentToApply).getText();
                else if (originalValue instanceof Integer)
                    return new Integer(((JTextField) componentToApply).getText());
                else if (originalValue instanceof Double)
                    return new Double(((JTextField) componentToApply).getText());
                else if (originalValue instanceof Float)
                    return new Float(((JTextField) componentToApply).getText());
                else if (originalValue instanceof Position)
                    return new Position(((JTextField) componentToApply).getText());
                else
                    return null;
            } else if (componentToApply instanceof ColorPickerCellEditor) {
                Color c = (Color) ((ColorPickerCellEditor) componentToApply).getCellEditorValue();
                if (c != null)
                    return c;
                else {
//        System.out.println("Original Value is a " + originalValue.getClass().getName());
                    return (Color) originalValue;
                }
            } else if (componentToApply instanceof FieldPlusLOVPicker) {
                ContourLinesList cll = (ContourLinesList) ((FieldPlusLOVPicker) componentToApply).getCellEditorValue();
                if (cll != null)
                    return cll;
                else {
                    //  System.out.println("Original Value is a " + originalValue.getClass().getName());
                    return (ContourLinesList) originalValue;
                }
            } else if (componentToApply instanceof FieldPlusFontPicker) {
                Object obj = ((FieldPlusFontPicker) componentToApply).getCellEditorValue();
                if (obj instanceof Font) // FIXME Fix that shit...
                    return (Font) obj;
                else if (obj instanceof String) {
                    String font = (String) ((FieldPlusFontPicker) componentToApply).getCellEditorValue();
                    if (font != null)
                        return FontPanel.stringToFont(font);
                    else
                        return null;
                } else {
                    //  System.out.println("Original Value is a " + originalValue.getClass().getName());
                    return (Font) originalValue;
                }
            } else if (componentToApply instanceof FilePickerCellEditor) {
                return (/*(DataFile)*/((FilePickerCellEditor) componentToApply).getCellEditorValue()); // .toString();
            } else if (componentToApply instanceof DirectoryPickerCellEditor) {
                return (DataDirectory) ((DirectoryPickerCellEditor) componentToApply).getCellEditorValue(); // .toString();
            } else if (componentToApply instanceof FieldPlusPathPicker) {
                System.out.println("==> Path Value:" + ((ParamPanel.DataPath) ((FieldPlusPathPicker) componentToApply).getCellEditorValue()).toString());
                return (DataPath) ((FieldPlusPathPicker) componentToApply).getCellEditorValue(); // .toString();
            } else if (componentToApply instanceof BooleanCellEditor) {
                return ((Boolean) ((BooleanCellEditor) componentToApply).getCellEditorValue());
            } else if (componentToApply instanceof TemperatureUnitListComboBox) {
                String s = (String) ((TemperatureUnitListComboBox) componentToApply).getSelectedItem();
                Integer i = null;
                for (Integer k : TemperatureUnitList.getMap().keySet()) {
                    if (TemperatureUnitList.getMap().get(k).equals(s)) {
                        i = k;
                        break;
                    }
                }
                return (new TemperatureUnitList(i.intValue()));
            } else if (componentToApply instanceof AnemometerHandOptionListComboBox) {
                String s = (String) ((AnemometerHandOptionListComboBox) componentToApply).getSelectedItem();
                Integer i = null;
                for (Integer k : AnemometerHandOptionList.getMap().keySet()) {
                    if (AnemometerHandOptionList.getMap().get(k).equals(s)) {
                        i = k;
                        break;
                    }
                }
                return (new AnemometerHandOptionList(i.intValue()));
            } else if (componentToApply instanceof FaxBlurListComboBox) {
                String s = (String) ((FaxBlurListComboBox) componentToApply).getSelectedItem();
                Integer i = null;
                for (Integer k : FaxBlurList.getMap().keySet()) {
                    if (FaxBlurList.getMap().get(k).equals(s)) {
                        i = k;
                        break;
                    }
                }
                return (new FaxBlurList(i.intValue()));
            } else if (componentToApply instanceof GrayPanelOptionListComboBox) {
                String s = (String) ((GrayPanelOptionListComboBox) componentToApply).getSelectedItem();
                Integer i = null;
                for (Integer k : GrayPanelOptionList.getMap().keySet()) {
                    if (GrayPanelOptionList.getMap().get(k).equals(s)) {
                        i = k;
                        break;
                    }
                }
                return (new GrayPanelOptionList(i.intValue()));
            } else if (componentToApply instanceof WindOptionComboBox) {
                String s = (String) ((WindOptionComboBox) componentToApply).getSelectedItem();
                int idx = -1;
                for (int i = 0; i < WindOptionList.getMap().length; i++) {
                    if (WindOptionList.getMap()[i].equals(s)) {
                        idx = i;
                        break;
                    }
                }
                // Legacy
                if (idx == -1)
                    idx = 0;
                return (new WindOptionList(idx));
            } else if (componentToApply instanceof RoutingOptionComboBox) {
                String s = (String) ((RoutingOptionComboBox) componentToApply).getSelectedItem();
                int idx = -1;
                for (int i = 0; i < RoutingOutputList.getMap().length; i++) {
                    if (RoutingOutputList.getMap()[i].equals(s)) {
                        idx = i;
                        break;
                    }
                }
                // Legacy
                if (idx == -1)
                    idx = 0;
                return (new RoutingOutputList(idx));
            } else if (componentToApply instanceof JComboBox) // Too vague...
            {
                if (originalValue instanceof ListOfSerialPorts)
                    return new ListOfSerialPorts((String) ((JComboBox) componentToApply).getSelectedItem());
                else if (originalValue instanceof ListOfTimeZones)
                    return new ListOfTimeZones((String) ((JComboBox) componentToApply).getSelectedItem());
                else // Assume Look and Feel... Not granted
                {
                    System.out.println("Warning!!! Unproperly managed ComboBox for :" + originalValue.getClass().getName());
                    return (new ListOfLookAndFeel((String) ((JComboBox) componentToApply).getSelectedItem()));
                }
            } else {
                WWContext.getInstance().fireLogging("ParamPanel.getCellEditorValue : Null!! [" + (componentToApply != null ? componentToApply.getClass().getName() : " null") + "]");
                return null;
            }
//    return null;
        }

        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        public void addCellEditorListener(CellEditorListener l) {
            listeners.addElement(l);
        }

        public void removeCellEditorListener(CellEditorListener l) {
            listeners.removeElement(l);
        }

        protected void fireEditingCanceled() {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = listeners.size(); i >= 0; i--)
                listeners.elementAt(i).editingCanceled(ce);
        }

        protected void fireEditingStopped() {
            ChangeEvent ce = new ChangeEvent(this);
            for (int i = (listeners.size() - 1); i >= 0; i--)
                listeners.elementAt(i).editingStopped(ce);
        }
    }

    public int[] getSelectRows() {
        return table.getSelectedRows();
    }

    private Object[][] addLineInTable(String k,
                                      String v) {
        return addLineInTable(k, v, data);
    }

    private Object[][] addLineInTable(String k,
                                      String v,
                                      Object[][] d) {
        int len = 0;
        if (d != null)
            len = d.length;
        Object[][] newData = new Object[len + 1][names.length];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < names.length; j++)
                newData[i][j] = d[i][j];
        }
        newData[len][ParamData.NAME_INDEX] = k;
        newData[len][ParamData.VALUE_INDEX] = v;
//  System.out.println("Adding " + k + ":" + v);
        return newData;
    }

    public static void saveParameters() {
        WWContext.getInstance().fireLogging(WWGnlUtilities.buildMessage("saving-user-config"));
        XMLDocument doc = new XMLDocument();
        Element elem = doc.createElement("app-parameters");
        doc.appendChild(elem);
        for (int i = 0; i < data.length; i++) {
            Element param = doc.createElement("param");
            elem.appendChild(param);
            param.setAttribute("id", Integer.toString(i));
            Text val = doc.createTextNode("text#");
            Object valueObject = data[i][ParamData.VALUE_INDEX];
            if (valueObject != null) {
                if (valueObject instanceof Color) {
                    Color c = (Color) valueObject;
                    val.setNodeValue(WWGnlUtilities.colorToString(c));
                } else if (valueObject instanceof TemperatureUnitList) {
                    TemperatureUnitList tul = (TemperatureUnitList) valueObject;
                    val.setNodeValue(tul.getStringIndex());
                } else if (valueObject instanceof FaxBlurList) {
                    FaxBlurList fbl = (FaxBlurList) valueObject;
                    val.setNodeValue(fbl.getStringIndex());
                } else if (valueObject instanceof GrayPanelOptionList) {
                    GrayPanelOptionList gpol = (GrayPanelOptionList) valueObject;
                    val.setNodeValue(gpol.getStringIndex());
                } else if (valueObject instanceof AnemometerHandOptionList) {
                    AnemometerHandOptionList ahol = (AnemometerHandOptionList) valueObject;
                    val.setNodeValue(ahol.getStringIndex());
                } else if (valueObject instanceof RoutingOutputList) {
                    RoutingOutputList rol = (RoutingOutputList) valueObject;
                    val.setNodeValue(rol.getStringIndex());
                } else if (valueObject instanceof WindOptionList) {
                    WindOptionList wol = (WindOptionList) valueObject;
                    val.setNodeValue(wol.getStringIndex());
                } else if (valueObject instanceof Font) {
                    val.setNodeValue(FontPanel.fontToString((Font) valueObject));
                } else {
                    val.setNodeValue(valueObject.toString());
//        System.out.println("Saving:" + valueObject.toString());
                }
            } else {
                if (i != ParamData.PLAY_SOUND_ON_JOB_COMPLETION)
                    JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), WWGnlUtilities.buildMessage("null-value", new String[]{ParamData.getLabels()[i] /* Integer.toString(i) */}));
                val.setNodeValue("");
            }
            param.appendChild(val);
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(ParamData.PARAM_FILE_NAME);
            doc.print(os);
            os.flush();
            os.close();
//    parent.setParameterChanged(false); // reset
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(WWContext.getInstance().getMasterTopFrame(), ex.toString(), WWGnlUtilities.buildMessage("writing-parameters"), JOptionPane.ERROR_MESSAGE);
            WWContext.getInstance().fireExceptionLogging(ex);
            ex.printStackTrace();
        }
    }

    public static class DataFile {
        private String[] fileExt;
        private String desc;
        private String value;

        public DataFile(String[] sa, String s, String str) {
            fileExt = sa;
            desc = s;
            value = str;
        }

        public String[] getFileExt() {
            return fileExt;
        }

        public String getDesc() {
            return desc;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return value;
        }

        public void setValue(String s) {
            value = s;
        }
    }

    public static class SoundFile extends DataFile {
        public SoundFile(String[] sa, String s, String str) {
            super(sa, s, str);
        }
    }

    public static class DataDirectory {
        private String desc;
        private String value;

        public DataDirectory(String description, String dirValue) {
            desc = description;
            value = dirValue;
        }

        public String getDesc() {
            return desc;
        }

        public String toString() {
            return value;
        }

        public void setValue(String s) {
            value = s;
        }
    }

    public static class DataPath {
        private DataDirectory[] path = null;

        public DataPath(String s) {
            this.setValue(s);
        }

        public DataPath(DataDirectory[] dd) {
            this.path = dd;
        }

        public DataDirectory[] getPath() {
            return path;
        }

        public void setValue(String s) {
            // Parse the String, build the array
            String[] data = s.split(File.pathSeparator);
            if (data != null) {
                path = new DataDirectory[data.length];
                for (int i = 0; i < data.length; i++)
                    path[i] = new DataDirectory("DataPath", data[i]);
            }
        }

        public String toString() {
            String str = "";
            for (int i = 0; path != null && i < path.length; i++) {
                str += ((str.length() > 0 ? File.pathSeparator : "") + path[i].toString());
            }
            return str;
        }
    }

    public static class ContourLinesList {
        private String value;

        public ContourLinesList(String s) {
            this.value = s;
        }

        public void setValue(String str) {
            this.value = str;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            return getValue();
        }

        public int[] getIntValues() {
            List<Integer> al = new ArrayList<Integer>();
            StringTokenizer strtokContourLines = new StringTokenizer(value, ",");
            while (strtokContourLines.hasMoreTokens()) {
                String tok = strtokContourLines.nextToken().trim();
                boolean b = tok.startsWith("[") && tok.endsWith("]");
                if (b)
                    tok = tok.substring(1, tok.length() - 1);
                al.add(new Integer(tok));
            }
            int[] ret = new int[al.size()];
            Iterator<Integer> iterator = al.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Integer integer = iterator.next();
                ret[i++] = integer.intValue();
            }
            return ret;
        }

        public int[] getBoldIndexes() {
            List<Integer> al = new ArrayList<Integer>();
            StringTokenizer strtokContourLines = new StringTokenizer(value, ",");
            int idx = 0;
            while (strtokContourLines.hasMoreTokens()) {
                String tok = strtokContourLines.nextToken().trim();
                boolean b = tok.startsWith("[") && tok.endsWith("]");
                if (b)
                    al.add(new Integer(idx));
                idx++;
            }
            int[] ret = new int[al.size()];
            Iterator<Integer> iterator = al.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Integer integer = iterator.next();
                ret[i++] = integer.intValue();
            }

            return ret;
        }
    }

    public static class ListOfLookAndFeel extends ListOfValues {
        public ListOfLookAndFeel(String str) {
            super(str);
        }
    }

    public static class WindOptionList extends ListOfValues {
        private static String[] map = {WWGnlUtilities.buildMessage("small-dot"),
                WWGnlUtilities.buildMessage("heavy-dot"),
                WWGnlUtilities.buildMessage("background")};

        public final static int SMALL_DOT = 0;
        public final static int HEAVY_DOT = 1;
        public final static int BACKGROUND = 2;

        public WindOptionList(int key) {
            super.setCurrentValue(map[key]);
        }

        public static String[] getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (int i = 0; i < map.length; i++) {
                if (map[i].equals(s)) {
                    str = Integer.toString(i);
                    break;
                }
            }
            return str;
        }
    }

    public static class RoutingOutputList extends ListOfValues {
        private static String[] map = {"CSV",
                "GPX",
                "Text",
                WWGnlUtilities.buildMessage("ask-user-every-time"),
                "KML",
                "json",
                WWGnlUtilities.buildMessage("no-output")};

        public final static int CSV = 0;
        public final static int GPX = 1;
        public final static int TXT = 2;
        public final static int ASK = 3;
        public final static int KML = 4;
        public final static int JSON = 5;
        public final static int NONE = 6;

        public RoutingOutputList(int key) {
            super.setCurrentValue(map[key]);
        }

        public static String[] getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (int i = 0; i < map.length; i++) {
                if (map[i].equals(s)) {
                    str = Integer.toString(i);
                    break;
                }
            }
            return str;
        }
    }

    public static class TemperatureUnitList extends ListOfValues {
        public final static int CELCIUS = 0;
        public final static int FAHRENHEIT = 1;
        public final static int KELVIN = 2;

        private static HashMap<Integer, String> map = new HashMap<Integer, String>(3);

        public TemperatureUnitList(int key) {
            map.put(CELCIUS, "\272C");
            map.put(FAHRENHEIT, "\272F");
            map.put(KELVIN, "\272K");

            super.setCurrentValue(map.get(key));
        }

        public int getCurrentIndex() {
            int idx = -1;
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(getCurrentValue())) {
                    idx = k.intValue();
                    break;
                }
            }
            return idx;
        }

        public static HashMap<Integer, String> getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(s)) {
                    str = k.toString();
                    break;
                }
            }
            return str;
        }

        public static String getLabel(int idx) {
            return map.get(new Integer(idx));
        }
    }

    public static class AnemometerHandOptionList extends ListOfValues {
        public final static int SIMPLE_HAND_OPTION = 1;
        public final static int ARROW_HAND_OPTION = 2;
        public final static int BIG_HAND_OPTION = 3;

        private static HashMap<Integer, String> map = new HashMap<Integer, String>(3);

        public AnemometerHandOptionList(int key) {
            map.put(SIMPLE_HAND_OPTION, WWGnlUtilities.buildMessage("simple-hand"));
            map.put(ARROW_HAND_OPTION, WWGnlUtilities.buildMessage("short-arrow"));
            map.put(BIG_HAND_OPTION, WWGnlUtilities.buildMessage("long-arrow"));

            super.setCurrentValue(map.get(key));
        }

        public int getCurrentIndex() {
            int idx = -1;
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(getCurrentValue())) {
                    idx = k.intValue();
                    break;
                }
            }
            return idx;
        }

        public static HashMap<Integer, String> getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(s)) {
                    str = k.toString();
                    break;
                }
            }
            return str;
        }

        public static String getLabel(int idx) {
            return map.get(new Integer(idx));
        }
    }

    public static class FaxBlurList extends ListOfValues {
        private static HashMap<Integer, String> map = new HashMap<Integer, String>(3);

        public FaxBlurList(int key) {
            map.put(-1, WWGnlUtilities.buildMessage("blur"));
            map.put(0, WWGnlUtilities.buildMessage("default"));
            map.put(1, WWGnlUtilities.buildMessage("sharp"));

            super.setCurrentValue(map.get(key));
        }

        public static HashMap<Integer, String> getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(s)) {
                    str = k.toString();
                    break;
                }
            }
            return str;
        }
    }

    public static class GrayPanelOptionList extends ListOfValues {
        private static Map<Integer, String> map = new LinkedHashMap<Integer, String>(3);

        public GrayPanelOptionList(int key) {
            map.put(AdjustFrame.GRAY_PANEL_NOPANEL_OPTION, WWGnlUtilities.buildMessage("no-gray-panel"));
            map.put(AdjustFrame.GRAY_PANEL_NO_FADE_OPTION, WWGnlUtilities.buildMessage("no-fade"));
            map.put(AdjustFrame.GRAY_PANEL_SHIFT_DOWN_OPTION, WWGnlUtilities.buildMessage("shift-down"));
            map.put(AdjustFrame.GRAY_PANEL_FADE_OPTION, WWGnlUtilities.buildMessage("fade-option"));
            map.put(AdjustFrame.GRAY_PANEL_SECTOR_OPTION, WWGnlUtilities.buildMessage("circle-whipe"));

            super.setCurrentValue(map.get(key));
        }

        public static Map<Integer, String> getMap() {
            return map;
        }

        public String getStringIndex() {
            String str = "";
            String s = super.getCurrentValue();
            for (Integer k : map.keySet()) {
                if (map.get(k).equals(s)) {
                    str = k.toString();
                    break;
                }
            }
            return str;
        }
    }

    public class SelectionListener
            implements ListSelectionListener {
        JTable table;

        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Object o = localData[selectedRow][ParamData.NAME_INDEX];
                helpTextArea.setText(((ParamDisplayLabel) o).getHelp());
            } else {
                helpTextArea.setText("");
            }
        }
    }

    private static class ParamDisplayLabel {
        private String label = "";
        private String help = "";

        public ParamDisplayLabel() {
        }

        public ParamDisplayLabel(String label) {
            this.label = label;
            this.help = label;
        }

        public ParamDisplayLabel(String label, String help) {
            this.label = label;
            this.help = help;
        }

        public String getLabel() {
            return label;
        }

        public String getHelp() {
            return help;
        }

        public String toString() {
            return getLabel();
        }
    }

    
    private static class TemperatureUnitListComboBox extends JComboBox {
        public TemperatureUnitListComboBox() {
            super();
            this.removeAllItems();
            for (Integer key : TemperatureUnitList.getMap().keySet()) {
                this.addItem(TemperatureUnitList.getMap().get(key));
            }
        }
    }

    
    private static class AnemometerHandOptionListComboBox extends JComboBox {
        public AnemometerHandOptionListComboBox() {
            super();
            this.removeAllItems();
            for (Integer key : AnemometerHandOptionList.getMap().keySet()) {
                this.addItem(AnemometerHandOptionList.getMap().get(key));
            }
        }
    }

    
    private static class GrayPanelOptionListComboBox extends JComboBox {
        public GrayPanelOptionListComboBox() {
            super();
            this.removeAllItems();
            for (Integer key : GrayPanelOptionList.getMap().keySet()) {
                this.addItem(GrayPanelOptionList.getMap().get(key));
            }
        }
    }

    
    private static class FaxBlurListComboBox extends JComboBox {
        public FaxBlurListComboBox() {
            super();
            this.removeAllItems();
            for (Integer key : FaxBlurList.getMap().keySet()) {
                this.addItem(FaxBlurList.getMap().get(key));
            }
        }
    }

    
    private static class WindOptionComboBox extends JComboBox {
        public WindOptionComboBox() {
            super();
            this.removeAllItems();
            for (int i = 0; i < WindOptionList.getMap().length; i++) {
                this.addItem(WindOptionList.getMap()[i]);
            }
        }
    }

    
    private static class RoutingOptionComboBox extends JComboBox {
        public RoutingOptionComboBox() {
            super();
            this.removeAllItems();
            for (int i = 0; i < RoutingOutputList.getMap().length; i++) {
                this.addItem(RoutingOutputList.getMap()[i]);
            }
        }
    }
}
package chartview.gui.util.param;

import chartview.util.WWGnlUtilities;

import java.io.File;

public class ParamData {
    public final static String PARAM_FILE_NAME = "config" + File.separator + "app-properties.xml";

    public final static int NAME_INDEX = 0;
    public final static int VALUE_INDEX = 1;

    // TODO Replace with an Enumeration?
    public final static int CHART_COLOR = 0;
    public final static int GRID_COLOR = 1;
    public final static int NMEA_SERVER_URL = 2;
    public final static int NMEA_POLLING_FREQ = 3;
    public final static int POLAR_FILE_LOC = 4;
    public final static int ROUTING_STEP = 5;
    public final static int ROUTING_FORK_WIDTH = 6;
    public final static int ROUTING_TIME_INTERVAL = 7;
    public final static int ROUTING_FROM_CURR_LOC = 8;
    public final static int GRIB_FILES_LOC = 9;
    public final static int FAX_FILES_LOC = 10;
    public final static int COMPOSITE_ROOT_DIR = 11;
    public final static int CHART_LINE_THICK = 12;
    public final static int FAX_TRANSPARENCY = 13;
    public final static int CHART_BG_COLOR = 14;
    public final static int GRIB_WIND_COLOR = 15;
    public final static int PATTERN_DIR = 16;
    public final static int PRMSL_CONTOUR = 17;
    public final static int MB500_CONTOUR = 18;
    public final static int WAVES_CONTOUR = 19;
    public final static int TEMP_CONTOUR = 20;
    public final static int LOOK_AND_FEEL = 21;
    public final static int AUTO_UPDATES = 22;
    public final static int CONFIRM_DD_ZOOM = 23;
    public final static int AVOID_TWS_GT = 24;
    public final static int AVOID_TWA_LT = 25;
    public final static int STOP_ROUTING_ON_EXHAUSTED_GRIB = 26;
    public final static int INTERVAL_BETWEEN_ISOBARS = 27;
    public final static int ISOBARS_LIST = 28;
    public final static int ISOHEIGHT500_LIST = 29;
    public final static int ISOHEIGHTWAVES_LIST = 30;
    public final static int ISOTEMP_LIST = 31;
    public final static int POLAR_SPEED_FACTOR = 32;
    public final static int CONFIRM_ON_EXIT = 33;
    public final static int USE_TRANSPARENT_GRIB_WIND = 34;
    public final static int COLOR_RANGE = 35;
    public final static int PREFERRED_WIND_DISPLAY = 36;
    public final static int LOAD_COMPOSITE_STARTUP = 37;
    public final static int GPS_BOAT_COLOR = 38;
    public final static int ISO_TWS_LIST = 39;
    public final static int DEFAULT_ZOOM_VALUE = 40;
    public final static int DEFAULT_CHART_INC_VALUE = 41;
    public final static int DEFAULT_FAX_INC_VALUE = 42;
    public final static int DD_ZOOM_COLOR = 43;
    public final static int DISPLAY_WIND_WITH_COLOR_WIND_RANGE = 44;
    public final static int DEFAULT_FAX_BLUR = 45;
    public final static int ISOPRATE_LIST = 46;
    public final static int PRATE_CONTOUR = 47;
    public final static int CLICK_SCROLL = 48;
    public final static int OLD_ISOCHRONS_COLOR = 49;
    public final static int SHOW_ROUTING_LABELS = 50;
    public final static int SHOW_ISOCHRONS = 51;
    public final static int SERIAL_PORT = 52;
    public final static int ROUTING_OUTPUT_FLAVOR = 53;
    public final static int TCP_PORT = 54;
    public final static int UDP_PORT = 55;
    public final static int ROUTE_COLOR = 56;
    public final static int ROUTING_BOAT_COLOR = 57;
    public final static int TWS_COLOR_IN_ROUTING = 58;
    public final static int PRMSL_COLOR_IN_ROUTING = 59;
    public final static int HGT500_COLOR_IN_ROUTING = 60;
    public final static int WAVES_COLOR_IN_ROUTING = 61;
    public final static int AIRTMP_COLOR_IN_ROUTING = 62;
    public final static int RAIN_COLOR_IN_ROUTING = 63;
    public final static int BSP_COLOR_IN_ROUTING = 64;
    public final static int SHOW_NOTIFICATIONS = 65;
    public final static int GRIB_CURRENT_COLOR = 66;
    public final static int AUTO_SAVE_DEFAULT_COMPOSITE = 67;
    public final static int RELOAD_DEFAULT_COMPOSITE_INTERVAL = 68;
    public final static int GRAY_PANEL_OPTION = 69;
    public final static int GRAY_PANEL_OPACITY = 70;
    public final static int WAIT_ON_STARTUP = 71;
    public final static int EXPAND_CONTROLS_BY_DEFAULT = 72;
    public final static int TEMPERATURE_UNIT = 73;
    public final static int NMEA_HOST = 74;
    public final static int GPSD_PORT = 75;
    public final static int ANEMOMETER_HAND_OPTION = 76;
    public final static int PLAY_SOUND_ON_JOB_COMPLETION = 77;
    public final static int TRY_TO_AVOID_LAND = 78;
    public final static int STOP_ROUTING_WHEN_CLOSER_TO = 79;
    public final static int NMEA_FALLBACK_TIMEOUT = 80;
    public final static int GRIB_TWS_COEFF = 81;
    public final static int DEFAULT_FONT = 82;
    public final static int CONFIRM_COMMENT = 83;
    public final static int PLAY_SOUND_WHEN_LEAVING = 84;
    // Headless, TODO to remove
//    public final static int EPHEMERIS_POSITION = 84;
//    public final static int TIDE_STATION_NAME = 85;
//    public final static int EPHEMERIS_EMAIL_LIST = 86;
//    public final static int EPHEMERIS_EMAIL_TZ = 87;
//    public final static int EPHEMERIS_EMAIL_PROVIDER = 88;

    public final static int NB_PREFERENCES = 85; // 89;

    private final static String[] labels =
            {
                    WWGnlUtilities.buildMessage("chart-line-color"),
                    WWGnlUtilities.buildMessage("grid-color"),
                    WWGnlUtilities.buildMessage("server-url"),
                    WWGnlUtilities.buildMessage("polling-frequency"),
                    WWGnlUtilities.buildMessage("polar-location"),
                    WWGnlUtilities.buildMessage("routing-step"),
                    WWGnlUtilities.buildMessage("routing-fork"),
                    WWGnlUtilities.buildMessage("routing-interval"),
                    WWGnlUtilities.buildMessage("start-routing"),
                    WWGnlUtilities.buildMessage("grib-location"),
                    WWGnlUtilities.buildMessage("fax-location"),
                    WWGnlUtilities.buildMessage("composite-location"),
                    WWGnlUtilities.buildMessage("chart-thickness"),
                    WWGnlUtilities.buildMessage("fax-transp"),
                    WWGnlUtilities.buildMessage("chart-background"),
                    WWGnlUtilities.buildMessage("wind-color"),
                    WWGnlUtilities.buildMessage("pattern-location"),
                    WWGnlUtilities.buildMessage("prmsl-color"),
                    WWGnlUtilities.buildMessage("500-color"),
                    WWGnlUtilities.buildMessage("waves-color"),
                    WWGnlUtilities.buildMessage("temp-color"),
                    WWGnlUtilities.buildMessage("lnf"),
                    WWGnlUtilities.buildMessage("auto-download"),
                    WWGnlUtilities.buildMessage("confirm-ddz"),
                    WWGnlUtilities.buildMessage("avoid-tws-gt"),
                    WWGnlUtilities.buildMessage("avoid-twa-lt"),
                    WWGnlUtilities.buildMessage("stop-routing"),
                    WWGnlUtilities.buildMessage("isobars-interval"),
                    WWGnlUtilities.buildMessage("isobars-list"),
                    WWGnlUtilities.buildMessage("iso-500-list"),
                    WWGnlUtilities.buildMessage("iso-waves-list"),
                    WWGnlUtilities.buildMessage("iso-temp-list"),
                    WWGnlUtilities.buildMessage("polar-speed-factor"),
                    WWGnlUtilities.buildMessage("confirm-exit"),
                    WWGnlUtilities.buildMessage("use-wind-color"),
                    WWGnlUtilities.buildMessage("color-range"),
                    WWGnlUtilities.buildMessage("preferred-wind-display-option"),
                    WWGnlUtilities.buildMessage("load-composite-at-startup"),
                    WWGnlUtilities.buildMessage("gps-boat-color"),
                    WWGnlUtilities.buildMessage("iso-tws-list"),
                    WWGnlUtilities.buildMessage("default-zoom-value"),
                    WWGnlUtilities.buildMessage("default-chart-inc-value"),
                    WWGnlUtilities.buildMessage("default-fax-inc-value"),
                    WWGnlUtilities.buildMessage("ddz-color"),
                    WWGnlUtilities.buildMessage("display-wind-with-color-wind-range"),
                    WWGnlUtilities.buildMessage("default-blur-for-faxes"),
                    WWGnlUtilities.buildMessage("iso-prate-list"),
                    WWGnlUtilities.buildMessage("prate-color"),
                    WWGnlUtilities.buildMessage("click-scroll"),
                    WWGnlUtilities.buildMessage("old-isochrons-color"),
                    WWGnlUtilities.buildMessage("show-routing-labels-by-default"),
                    WWGnlUtilities.buildMessage("show-isochrons-by-default"),
                    WWGnlUtilities.buildMessage("nmea-serial-port"),
                    WWGnlUtilities.buildMessage("routing-output"),
                    WWGnlUtilities.buildMessage("nmea-tcp-port"),
                    WWGnlUtilities.buildMessage("nmea-udp-port"),
                    WWGnlUtilities.buildMessage("route-color"),
                    WWGnlUtilities.buildMessage("routing-boat-color"),
                    WWGnlUtilities.buildMessage("tws-color-in-routing"),
                    WWGnlUtilities.buildMessage("prmsl-color-in-routing"),
                    WWGnlUtilities.buildMessage("hgt500-color-in-routing"),
                    WWGnlUtilities.buildMessage("waves-color-in-routing"),
                    WWGnlUtilities.buildMessage("airtmp-color-in-routing"),
                    WWGnlUtilities.buildMessage("rain-color-in-routing"),
                    WWGnlUtilities.buildMessage("bsp-color-in-routing"),
                    WWGnlUtilities.buildMessage("show-notifications"),
                    WWGnlUtilities.buildMessage("current-color"),
                    WWGnlUtilities.buildMessage("auto-save-pattern"),
                    WWGnlUtilities.buildMessage("auto-reload-interval"),
                    WWGnlUtilities.buildMessage("shift-down-gray-panel"),
                    WWGnlUtilities.buildMessage("gray-panel-opacity"),
                    WWGnlUtilities.buildMessage("startup-wait"),
                    WWGnlUtilities.buildMessage("expand-control-by-default"),
                    WWGnlUtilities.buildMessage("temperature-unit"),
                    WWGnlUtilities.buildMessage("nmea-hostname"),
                    WWGnlUtilities.buildMessage("gpsd-port"),
                    WWGnlUtilities.buildMessage("anemometer-hand-option"),
                    WWGnlUtilities.buildMessage("play-sound-on-job-completion"),
                    WWGnlUtilities.buildMessage("avoid-land"),
                    WWGnlUtilities.buildMessage("routing-completed-below"),
                    WWGnlUtilities.buildMessage("nmea-fallback-timeout"),
                    WWGnlUtilities.buildMessage("grib-tws-coeff"),
                    WWGnlUtilities.buildMessage("default-font-size"),
                    WWGnlUtilities.buildMessage("ask-confirm-comment"),
                    WWGnlUtilities.buildMessage("play-sound-when-leaving") /*,
                    WWGnlUtilities.buildMessage("ephemeris-position"),
                    WWGnlUtilities.buildMessage("ephemeris-tide-station"),
                    WWGnlUtilities.buildMessage("ephemeris-email-list"),
                    WWGnlUtilities.buildMessage("ephemeris-email-tz"),
                    WWGnlUtilities.buildMessage("ephemeris-email-provider") */
            };

    private final static String[] helptext =
            {
                    WWGnlUtilities.buildMessage("help-chart-line-color"),
                    WWGnlUtilities.buildMessage("help-grid-color"),
                    WWGnlUtilities.buildMessage("help-server-url"),
                    WWGnlUtilities.buildMessage("help-polling-frequency"),
                    WWGnlUtilities.buildMessage("help-polar-location"),
                    WWGnlUtilities.buildMessage("help-routing-step"),
                    WWGnlUtilities.buildMessage("help-routing-fork"),
                    WWGnlUtilities.buildMessage("help-routing-interval"),
                    WWGnlUtilities.buildMessage("help-start-routing"),
                    WWGnlUtilities.buildMessage("help-grib-location"),
                    WWGnlUtilities.buildMessage("help-fax-location"),
                    WWGnlUtilities.buildMessage("help-composite-location"),
                    WWGnlUtilities.buildMessage("help-chart-thickness"),
                    WWGnlUtilities.buildMessage("help-fax-transp"),
                    WWGnlUtilities.buildMessage("help-chart-background"),
                    WWGnlUtilities.buildMessage("help-wind-color"),
                    WWGnlUtilities.buildMessage("help-pattern-location"),
                    WWGnlUtilities.buildMessage("help-prmsl-color"),
                    WWGnlUtilities.buildMessage("help-500-color"),
                    WWGnlUtilities.buildMessage("help-waves-color"),
                    WWGnlUtilities.buildMessage("help-temp-color"),
                    WWGnlUtilities.buildMessage("help-lnf"),
                    WWGnlUtilities.buildMessage("help-auto-download"),
                    WWGnlUtilities.buildMessage("help-confirm-ddz"),
                    WWGnlUtilities.buildMessage("help-avoid-tws-gt"),
                    WWGnlUtilities.buildMessage("help-avoid-twa-lt"),
                    WWGnlUtilities.buildMessage("help-stop-routing"),
                    WWGnlUtilities.buildMessage("help-isobars-interval"),
                    WWGnlUtilities.buildMessage("help-isobars-list"),
                    WWGnlUtilities.buildMessage("help-iso-500-list"),
                    WWGnlUtilities.buildMessage("help-iso-waves-list"),
                    WWGnlUtilities.buildMessage("help-iso-temp-list"),
                    WWGnlUtilities.buildMessage("help-polar-speed-factor"),
                    WWGnlUtilities.buildMessage("help-confirm-exit"),
                    WWGnlUtilities.buildMessage("help-use-wind-color"),
                    WWGnlUtilities.buildMessage("help-color-range"),
                    WWGnlUtilities.buildMessage("help-preferred-wind-display-option"),
                    WWGnlUtilities.buildMessage("help-load-composite-at-startup"),
                    WWGnlUtilities.buildMessage("help-gps-boat-color"),
                    WWGnlUtilities.buildMessage("help-iso-tws-list"),
                    WWGnlUtilities.buildMessage("help-default-zoom-value"),
                    WWGnlUtilities.buildMessage("help-default-chart-inc-value"),
                    WWGnlUtilities.buildMessage("help-default-fax-inc-value"),
                    WWGnlUtilities.buildMessage("help-ddz-color"),
                    WWGnlUtilities.buildMessage("help-display-wind-with-color-wind-range"),
                    WWGnlUtilities.buildMessage("help-default-blur-for-faxes"),
                    WWGnlUtilities.buildMessage("help-iso-prate-list"),
                    WWGnlUtilities.buildMessage("help-prate-color"),
                    WWGnlUtilities.buildMessage("help-click-scroll"),
                    WWGnlUtilities.buildMessage("help-old-isochrons-color"),
                    WWGnlUtilities.buildMessage("help-show-routing-labels-by-default"),
                    WWGnlUtilities.buildMessage("help-show-isochrons-by-default"),
                    WWGnlUtilities.buildMessage("help-nmea-serial-port"),
                    WWGnlUtilities.buildMessage("help-routing-output"),
                    WWGnlUtilities.buildMessage("help-nmea-tcp-port"),
                    WWGnlUtilities.buildMessage("help-nmea-udp-port"),
                    WWGnlUtilities.buildMessage("help-route-color"),
                    WWGnlUtilities.buildMessage("help-routing-boat-color"),
                    WWGnlUtilities.buildMessage("help-tws-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-prmsl-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-hgt500-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-waves-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-airtmp-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-rain-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-bsp-color-in-routing"),
                    WWGnlUtilities.buildMessage("help-show-notifications"),
                    WWGnlUtilities.buildMessage("help-current-color"),
                    WWGnlUtilities.buildMessage("help-auto-save-pattern"),
                    WWGnlUtilities.buildMessage("help-auto-reload-interval"),
                    WWGnlUtilities.buildMessage("help-shift-down-gray-panel"),
                    WWGnlUtilities.buildMessage("help-gray-panel-opacity"),
                    WWGnlUtilities.buildMessage("help-startup-wait"),
                    WWGnlUtilities.buildMessage("help-expand-control-by-default"),
                    WWGnlUtilities.buildMessage("temperature-unit-help"),
                    WWGnlUtilities.buildMessage("help-nmea-hostname"),
                    WWGnlUtilities.buildMessage("help-gpsd-port"),
                    WWGnlUtilities.buildMessage("help-anemometer-hand-option"),
                    WWGnlUtilities.buildMessage("help-play-sound-on-job-completion"),
                    WWGnlUtilities.buildMessage("help-avoid-land"),
                    WWGnlUtilities.buildMessage("help-routing-completed-below"),
                    WWGnlUtilities.buildMessage("help-nmea-fallback-timeout"),
                    WWGnlUtilities.buildMessage("help-grib-tws-coeff"),
                    WWGnlUtilities.buildMessage("help-default-font-size"),
                    WWGnlUtilities.buildMessage("help-ask-confirm-comment"),
                    WWGnlUtilities.buildMessage("help-play-sound-when-leaving") /*,
                    WWGnlUtilities.buildMessage("help-ephemeris-position"),
                    WWGnlUtilities.buildMessage("help-ephemeris-tide-station"),
                    WWGnlUtilities.buildMessage("help-ephemeris-email-list"),
                    WWGnlUtilities.buildMessage("help-ephemeris-email-tz"),
                    WWGnlUtilities.buildMessage("help-ephemeris-email-provider") */
            };

    public static String[] getLabels() {
        return labels.clone();
    }

    public static String[] getHelptext() {
        return helptext.clone();
    }
}

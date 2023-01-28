package nmea.forwarders;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.delegate.DelegateConsumer;
import nmea.mux.context.Context;
import nmea.parser.*;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * This is an example of a <b>transformer</b>.
 * <br>
 * To be used with other apps.
 * <p>
 * It auto-scrolls across available values, if display.time (-Ddisplay.time) is greater than 0
 * <p>
 * TODO A Consumer<String> to forward the formatted data
 */
public class NMEAtoTextProcessor implements Forwarder {
    private boolean keepWorking = true;

    private final static SimpleDateFormat SDF_DATE = new SimpleDateFormat("E dd MMM yyyy");
    private final static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss Z");
    private final static SimpleDateFormat SDF_TIME_NO_Z = new SimpleDateFormat("HH:mm:ss");

    static {
        SDF_DATE.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        SDF_TIME.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
        SDF_TIME_NO_Z.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    private final static SimpleDateFormat LOCAL_SDF_DATE = new SimpleDateFormat("E dd MMM yyyy");
    private final static SimpleDateFormat LOCAL_SDF_TIME = new SimpleDateFormat("HH:mm:ss Z");

    private static class CacheBean {
        private long gpsTime;
        private long gpsDateTime;

        private String wp;
        private double d2wp;
        private int b2wp;
        private double xte;

        private double lat;
        private double lng;
        private String pos;

        private boolean rmcOk;

        private long gpsSolarDate;

        private double log;
        private double dayLog;
        private int cog = -1;     // -1: not initialized yet
        private double sog = -1d; // -1: not initialized yet

        private int awa;
        private double aws;
        private double dbt;
        private int hdg;

        private double bsp;

        private double wtemp;
        private double atemp;

        private double D;
        private double d;
        private double W;

        private double leeway;
        private int cmg;

        private double tws;
        private int twa;
        private int twd;

        private int cdr;
        private double csp;

        private double prmsl;
        private double hum;
    }

    private static NMEAtoTextProcessor instance = null;
    protected boolean externallyOwned = false;

    protected boolean verbose = false;

    // Matching with the following values done in #setProperties below.
    private final static int TWD_OPTION = 0; // True Wind Direction
    private final static int BSP_OPTION = 1; // Boat Speed
    private final static int TWS_OPTION = 2; // True Wind Speed
    private final static int TWA_OPTION = 3; // True Wind Angle
    private final static int AWA_OPTION = 4; // Apparent Wind Angle
    private final static int AWS_OPTION = 5; // Apparent Wind Speed
    private final static int ATP_OPTION = 6; // Air Temperature
    private final static int WTP_OPTION = 7; // Water Temperature
    private final static int COG_OPTION = 8; // Course Over Ground
    private final static int SOG_OPTION = 9; // Speed Over Ground
    private final static int HDG_OPTION = 10; // Heading
    private final static int POS_OPTION = 11; // Position
    private final static int DBT_OPTION = 12; // Depth Below Transducer
    private final static int HUM_OPTION = 13; // Relative Humidity
    private final static int CUR_OPTION = 14; // Current. Speed and Direction
    private final static int PRS_OPTION = 15; // Atmospheric Pressure (PRMSL).
    private final static int GPS_OPTION = 16; // GPS Date & Time
    private final static int SOL_OPTION = 17; // SOLAR Date & Time
    // etc...
    private final static int SYS_OPTION = 18;

    private static final List<Integer> optionList = new ArrayList<>();
//	{
//					TWD_OPTION, // True Wind Direction
//					BSP_OPTION, // Boat Speed
//					TWS_OPTION, // True Wind Speed
//					TWA_OPTION, // True Wind Angle
//					AWA_OPTION, // Apparent Wind Angle
//					AWS_OPTION, // Apparent Wind Speed
//					ATP_OPTION, // Air Temperature
//					WTP_OPTION, // Water Temperature
//					COG_OPTION, // Course Over Ground
//					SOG_OPTION, // Speed Over Ground
//					HDG_OPTION, // Heading
//					POS_OPTION, // Position
//					DBT_OPTION, // Depth Below Transducer
//					HUM_OPTION, // Relative Humidity
//					CUR_OPTION, // Current. Speed and Direction
//					PRS_OPTION, // Atmospheric Pressure (PRMSL).
//          		GPS_OPTION  // GPS Date & Time
//          		SOL_OPTION  // SOLAR Date & Time
//          . . .
//	};

    private int currentOption = TWD_OPTION; // 0;

    private long scrollWait = 5_000L;

    enum SpeedUnit {
        KNOTS, KMH, MPH, MS
    }

    SpeedUnit speedUnit = SpeedUnit.KNOTS;


    private final Consumer<List<String>> DEFAULT_DISPLAY_CONSUMER = (dataList) -> {
        dataList.forEach(line -> System.out.println(line));
        System.out.println("---------------");
    };

	private Consumer<List<String>> displayConsumer = DEFAULT_DISPLAY_CONSUMER; // TODO Setter

	public void setDisplayConsumer(Consumer<List<String>> displayConsumer) {
		this.displayConsumer = displayConsumer;
	}

	public static NMEAtoTextProcessor getInstance() {
        return instance;
    }

    public void setExternallyOwned(boolean b) { // TODO Do it for other screen forwarders
        externallyOwned = b;
    }

    public NMEAtoTextProcessor() throws Exception {

        instance = this;

        int nbTry = 0;
        boolean ok = false;
        while (!ok) {
            // Make sure the cache has been initialized.
            if (ApplicationContext.getInstance().getDataCache() == null) {
                if (nbTry < 10) {
                    try {
                        Thread.sleep(1_000L);
                    } catch (Exception ex) {
                    }
                    nbTry++;
                } else {
                    throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
                }
            } else {
                ok = true;
            }
        }

        final String REST_CLIENT_EVENT_NAME = "change-speed-unit";
        final String SPEED_UNIT = "speed-unit";

        Context.getInstance().addTopicListener(new Context.TopicListener(REST_CLIENT_EVENT_NAME) {
            /**
             * Speed Unit can be changed (from a client, on the server) with a REST call:
             * POST /mux/events/change-speed-unit with a payload like
             * { "speed-unit": "kmh" }
             *
             * @param topic   <code>change-speed-unit</code> for this to work, or a regex matching it.
             * @param payload one of { "speed-unit": "kmh" }, { "speed-unit": "mph" }, { "speed-unit": "ms" }, or { "speed-unit": "kts" }
             */
            @Override
            @SuppressWarnings("unchecked")
            public void topicBroadcast(String topic, Object payload) {
//			System.out.println("Topic:" + topic + ", payload:" + payload);
                if (payload instanceof Map) {
                    Map<String, Object> map = (Map) payload;
                    Object unit = map.get(SPEED_UNIT);
                    if (unit != null) {
                        if (verbose) {
                            System.out.println("Changing Speed Unit to " + unit.toString());
                        }
                        switch (unit.toString()) {
                            case "kmh":
                                speedUnit = SpeedUnit.KMH;
                                break;
                            case "ms":
                                speedUnit = SpeedUnit.MS;
                                break;
                            case "mph":
                                speedUnit = SpeedUnit.MPH;
                                break;
                            case "kts":
                                speedUnit = SpeedUnit.KNOTS;
                                break;
                            default:
                                System.err.printf("Un-managed speed unit [%s]\n", unit.toString());
                                break;
                        }
                    } else {
                        System.err.println("Expected member [speed-unit] not found in the payload.");
                    }
                } else {
                    System.err.printf("Un-expected payload type [%s]\n", payload.getClass().getName());
                }
            }
        });
    }

    protected void initPartOne() {
        try {
            // NOOP
        } catch (Throwable error) {
            System.err.println("--- Instantiating Processor ---");
            error.printStackTrace(); // See the actual problem..., you never know.
            System.err.println("-----------------------------");
        }
    }

    protected void initPartTwo() {
        Thread scrollThread = new Thread("ScrollThread") { // if scrollWait > 0
            public void run() {
                while (keepWorking && scrollWait > 0) {
                    try {
                        Thread.sleep(scrollWait);
                    } catch (Exception ignore) {
                    }
                    // Scroll Display
//					onButtonPressed();
					currentOption++;
					if (currentOption >= optionList.size()) {
						currentOption = 0;
					}
				}
            }
        };
        scrollThread.start();

        // This is the loop providing the data to display, from the cache (when needed)
        Thread cacheThread = new Thread("SSD1306Processor CacheThread") {
            public void run() {
                while (keepWorking) {
                    NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
                    // Populate bean
                    CacheBean bean = new CacheBean();
                    if (cache != null) {
                        Object bsp = cache.get(NMEADataCache.BSP);
                        if (bsp != null) {
                            bean.bsp = ((Speed) bsp).getValue();
                        }
                        Object wtemp = cache.get(NMEADataCache.WATER_TEMP);
                        if (wtemp != null) {
                            bean.wtemp = ((Temperature) wtemp).getValue();
                        }
                        Object atemp = cache.get(NMEADataCache.AIR_TEMP);
                        if (atemp != null) {
                            bean.atemp = ((Temperature) atemp).getValue();
                        }
                        Object gpstime = cache.get(NMEADataCache.GPS_TIME);
                        if (gpstime != null) {
                            bean.gpsTime = ((UTCTime) gpstime).getValue().getTime();
                        }
                        Object gpsdatetime = cache.get(NMEADataCache.GPS_DATE_TIME);
                        if (gpsdatetime != null) {
                            bean.gpsDateTime = ((UTCDate) gpsdatetime).getValue().getTime();
                        }
                        Object nextwp = cache.get(NMEADataCache.TO_WP);
                        if (nextwp != null) {
                            bean.wp = (String) nextwp;
                        }
                        Object d2wp = cache.get(NMEADataCache.D2WP);
                        if (d2wp != null) {
                            bean.d2wp = ((Distance) d2wp).getValue();
                        }
                        Object cog = cache.get(NMEADataCache.COG);
                        if (cog != null && ((Angle360) cog).getValue() != -1) {
                            bean.cog = (int) Math.round(((Angle360) cog).getValue());
                        }
                        Object sog = cache.get(NMEADataCache.SOG);
                        if (sog != null) {
                            bean.sog = ((Speed) sog).getValue();
                        }
                        Object leeway = cache.get(NMEADataCache.LEEWAY);
                        if (leeway != null) {
                            bean.leeway = ((Angle180LR) leeway).getValue();
                        }
                        Object aws = cache.get(NMEADataCache.AWS);
                        if (aws != null) {
                            bean.aws = ((Speed) aws).getValue();
                        }
                        Object tws = cache.get(NMEADataCache.TWS);
                        if (tws != null) {
                            bean.tws = ((Speed) tws).getValue();
                        }
                        Object awa = cache.get(NMEADataCache.AWA);
                        if (awa != null) {
                            bean.awa = (int) Math.round(((Angle180) awa).getValue());
                        }
                        Object twa = cache.get(NMEADataCache.TWA);
                        if (twa != null) {
                            bean.twa = (int) Math.round(((Angle180) twa).getValue());
                        }
                        Object twd = cache.get(NMEADataCache.TWD);
                        if (twd != null) {
                            bean.twd = (int) Math.round(((Angle360) twd).getValue());
                        }
                        Object pos = cache.get(NMEADataCache.POSITION);
                        if (pos != null) {
                            GeoPos geopos = (GeoPos) pos;
                            bean.lat = geopos.lat;
                            bean.lng = geopos.lng;
                        }
                        Object decl = cache.get(NMEADataCache.DECLINATION);
                        if (decl != null) {
                            bean.D = ((Angle180EW) decl).getValue();
                        }
                        Object dev = cache.get(NMEADataCache.DEVIATION);
                        if (dev != null) {
                            bean.d = ((Angle180EW) dev).getValue();
                        }
                        Object w = cache.get(NMEADataCache.VARIATION);
                        if (w != null) {
                            bean.W = ((Angle180EW) w).getValue();
                        }
                        Object hdg = cache.get(NMEADataCache.HDG_COMPASS);
                        if (hdg != null) {
                            bean.hdg = (int) Math.round(((Angle360) hdg).getValue());  // Compass Heading. TODO Variation like below
                        } else {
                            hdg = cache.get(NMEADataCache.HDG_MAG);
                            if (hdg != null) {
                                double declination = 0d;
                                double deviation = 0d;
                                if (decl != null) {
                                    declination = ((Angle180EW) decl).getValue();
                                } else {
                                    Object defaultDecl = cache.get(NMEADataCache.DEFAULT_DECLINATION);
                                    if (defaultDecl != null) {
                                        // System.out.println("Default Declination is a " + defaultDecl.getClass().getName());
                                        declination = ((Angle180EW) defaultDecl).getValue();
                                    }
                                }
                                if (dev != null) {
                                    deviation = ((Angle180EW) dev).getValue();
                                }
                                int hdt = (int) Math.round(((Angle360) hdg).getValue() + (declination + deviation));
                                while (hdt >= 360) {
                                    hdt -= 360;
                                }
                                while (hdt < 0) {
                                    hdt += 360;
                                }
                                bean.hdg = hdt;
                            }
                        }
                        Object cmg = cache.get(NMEADataCache.CMG);
                        if (cmg != null) {
                            bean.cmg = (int) Math.round(((Angle360) cmg).getValue());
                        }
                        Object vdr = cache.get(NMEADataCache.VDR_CURRENT);
                        if (vdr != null) {
                            bean.cdr = ((Current) vdr).angle;
                            bean.csp = ((Current) vdr).speed;
                        }
                        Object xte = cache.get(NMEADataCache.XTE);
                        if (xte != null) {
                            bean.xte = ((Distance) xte).getValue();
                        }
                        Object b2wp = cache.get(NMEADataCache.B2WP);
                        if (b2wp != null) {
                            bean.b2wp = (int) Math.round(((Angle360) b2wp).getValue());
                        }
                        Object dbt = cache.get(NMEADataCache.DBT);
                        if (dbt != null) {
                            bean.dbt = ((Depth) dbt).getValue();
                        }
                        Object solarDate = cache.get(NMEADataCache.GPS_SOLAR_TIME);
                        if (solarDate != null) {
                            bean.gpsSolarDate = ((SolarDate) solarDate).getValue().getTime();
                        }
                        Object log = cache.get(NMEADataCache.LOG);
                        if (log != null) {
                            bean.log = ((Distance) log).getValue();
                        }
                        Object dayLog = cache.get(NMEADataCache.DAILY_LOG);
                        if (dayLog != null) {
                            bean.dayLog = ((Distance) dayLog).getValue();
                        }
                        Object prmsl = cache.get(NMEADataCache.BARO_PRESS);
                        if (prmsl != null) {
                            bean.prmsl = ((Pressure) prmsl).getValue();
                        }
                        Object hum = cache.get(NMEADataCache.RELATIVE_HUMIDITY);
                        if (hum != null) {
                            bean.hum = (Double) hum;
                        }
                        // rmcOk
                        Object rmcStatus = cache.get(NMEADataCache.RMC_STATUS);
                        if (rmcStatus != null) {
                            bean.rmcOk = (Boolean) rmcStatus;
                        } else {
                            bean.rmcOk = false;
                        }
                    }
                    // Transformer's specific job.
                    // Do see how optionList is populated from the properties.
                    if (!optionList.isEmpty() && !externallyOwned) {
                        int toDisplay = optionList.get(currentOption);
                        switch (toDisplay) {
                            case TWD_OPTION:
                                displayAngleAndValue("TWD ", bean.twd);
                                break;
                            case BSP_OPTION:
                                displaySpeed("BSP ", bean.bsp);
                                break;
                            case TWS_OPTION:
                                displaySpeed("TWS ", bean.tws);
                                break;
                            case TWA_OPTION:
                                displayAngleAndValue("TWA ", bean.twa);
                                break;
                            case AWA_OPTION:
                                displayAngleAndValue("AWA ", bean.awa);
                                break;
                            case AWS_OPTION:
                                displaySpeed("AWS ", bean.aws);
                                break;
                            case ATP_OPTION:
                                displayTemp("AIR ", bean.atemp);
                                break;
                            case WTP_OPTION:
                                displayTemp("WATER ", bean.wtemp);
                                break;
                            case COG_OPTION:
                                if (bean.cog != -1) {
                                    displayAngleAndValue("COG ", bean.cog);
                                } else {
                                    displayDummyValue("COG");
                                }
                                break;
                            case SOG_OPTION:
                                if (bean.sog != -1) {
                                    displaySpeed("SOG ", bean.sog);
                                } else {
                                    displayDummyValue("SOG");
                                }
                                break;
                            case HDG_OPTION:
                                displayAngleAndValue("HDG ", bean.hdg);
                                break;
                            case DBT_OPTION:
                                displayValue("DBT ", " m", bean.dbt);
                                break;
                            case HUM_OPTION:
                                displayValue("HUM ", " %", bean.hum);
                                break;
                            case CUR_OPTION:
                                displayCurrent(bean.cdr, bean.csp);
                                break;
                            case POS_OPTION:
                                displayPos(bean.lat, bean.lng, bean.rmcOk);
                                break;
                            case GPS_OPTION:
                                displayDateTime(bean.gpsDateTime);
                                break;
                            case SOL_OPTION:
                                displaySolarDateTime(bean.gpsSolarDate);
                                break;
                            case PRS_OPTION:
                                displayPRMSL(bean.prmsl);
                                break;
							case SYS_OPTION:
								displaySystemDateTime(System.currentTimeMillis()); // Not from the cache, obviously.
								break;
                            default:
                                break;
                        }
                    }
                    try {
                        Thread.sleep(1_000L);
                    } catch (Exception ex) {
                    }
                }
                System.out.println("Cache thread completed.");
            }
        };
        cacheThread.start();
    }

    @Override
    public void init() {
        initPartOne();
        initPartTwo();
    }

    private final static NumberFormat ANGLE_MASK = new DecimalFormat("000");

    private void displayAngleAndValue(String label, int value) {
        try {
			List<String> screen = new ArrayList<>();

			screen.add(label);
			screen.add(ANGLE_MASK.format(value) + "\u00b0");

            // Display
			displayConsumer.accept(screen);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private final static NumberFormat _22 = new DecimalFormat("00.00");
    private final static NumberFormat _X1 = new DecimalFormat("#0.0");

    private void displayValue(String label, String unit, double value) {
        try {
			List<String> screen = new ArrayList<>();

			screen.add(label);
			screen.add(_22.format(value) + unit);

            // Display
			displayConsumer.accept(screen);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void displaySpeed(String label, double value) {
        String unit = " kts";
        double speedFactor = 1D;
        switch (speedUnit) {
            case KMH:
                unit = " km/h";
                speedFactor = 1.852;
                break;
            case MPH:
                unit = " mph";
                speedFactor = 1.151;
                break;
            case MS:
                unit = " m/s";
                speedFactor = 0.514444;
                break;
            case KNOTS:
            default:
                break;
        }
        displayValue(label, unit, value * speedFactor);
    }

    private void displayTemp(String label, double value) {
        displayValue(label, "\u00b0C", value);
    }

    private void displayPos(double lat, double lng, boolean rmcStatus) {
        String latitude = GeomUtil.decToSex(lat, GeomUtil.NO_DEG, GeomUtil.NS, GeomUtil.TRAILING_SIGN).replaceFirst(" ", "\u00b0");
        String longitude = GeomUtil.decToSex(lng, GeomUtil.NO_DEG, GeomUtil.EW, GeomUtil.TRAILING_SIGN).replaceFirst(" ", "\u00b0");
        try {
			List<String> screen = new ArrayList<>();
            if (rmcStatus) {
                screen.add("POSITION");
				screen.add(latitude);
				screen.add(longitude);
            } else {
				screen.add("POSITION:");
                screen.add("RMC not ready yet!");
            }
            // Display
            displayConsumer.accept(screen);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void displayDateTime(long gpsDateTime) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        cal.setTimeInMillis(gpsDateTime);
        Date gps = cal.getTime();
        try {
			List<String> screen = new ArrayList<>();

			screen.add("GPS Date and Time");
			screen.add(SDF_DATE.format(gps));
			screen.add(SDF_TIME.format(gps));

            // Display
            displayConsumer.accept(screen);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Make sure the cache is fed using EoT, see System property calculate.solar.with.eot
    private void displaySolarDateTime(long solarTime) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
        cal.setTimeInMillis(solarTime);
        Date solar = cal.getTime();
        try {
			List<String> screen = new ArrayList<>();

			screen.add("SOLAR Date and Time");
            screen.add(SDF_DATE.format(solar));
            screen.add(SDF_TIME_NO_Z.format(solar));

            // Display
            displayConsumer.accept(screen);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void displaySystemDateTime(long sysEpoch) {
        Date sysDate = new Date(sysEpoch);
        try {
			List<String> screen = new ArrayList<>();

            screen.add("SYSTEM Date and Time");
			screen.add(LOCAL_SDF_DATE.format(sysDate));
			screen.add(LOCAL_SDF_TIME.format(sysDate));

            // Display
            displayConsumer.accept(screen);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private void displayCurrent(int dir, double speed) {
        String direction = "CURRENT DIR " + String.valueOf(dir) + "\u00b0";
        String speedStr = "CURRENT SPEED " + _22.format(speed) + " kts";
        try {
			List<String> screen = new ArrayList<>();

			screen.add(direction);
            screen.add(speedStr);

            // Display
            displayConsumer.accept(screen);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void displayPRMSL(double value) {
        try {
			List<String> screen = new ArrayList<>();

			screen.add("PRMSL");
            screen.add(_X1.format(value) + " mb");

			// Display
			displayConsumer.accept(screen);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private int status = 0;
    private final static String[] DUMMY_DATA_PREFIX = {"|", "/", "-", "\\"};

    private void displayDummyValue(String label) {
        try {
			List<String> screen = new ArrayList<>();

			screen.add(label);
            screen.add(DUMMY_DATA_PREFIX[status] + "No Data");
            status = (status + 1) % DUMMY_DATA_PREFIX.length;
            // Display
			displayConsumer.accept(screen);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void write(byte[] message) {
        // Nothing is done here. It is replaced by the Thread in the constructor, in init -> initPartTwo
    }

    @Override
    public void close() {
        System.out.println("- Stop writing to " + this.getClass().getName());
        try {
            // Stop Cache thread
            keepWorking = false;
            try {
                Thread.sleep(2_000L);
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public static class NMEAtoTextBean {
        private final String cls; // Class
        private final String type = "nmea-to-text";

        public NMEAtoTextBean(NMEAtoTextProcessor instance) {
            cls = instance.getClass().getName();
        }
    }

    @Override
    public Object getBean() {
        return new NMEAtoTextBean(this);
    }

    @Override
    public void setProperties(Properties props) {
		
		// Consumer !
		final String dataConsumer = props.getProperty("data.consumer");
		if (dataConsumer != null) {
			try {
				// DelegateConsumer, and its properties
				Object delegateConsumer = Class.forName(dataConsumer).getDeclaredConstructor().newInstance();
				if (delegateConsumer instanceof  DelegateConsumer) {
                    DelegateConsumer dg = (DelegateConsumer)delegateConsumer;
                    String propFileName = props.getProperty("consumer.properties");
                    if (propFileName != null) {
                        try {
                            Properties properties = new Properties();
                            properties.load(new FileReader(propFileName));
                            dg.setProperties(properties);
                        } catch (Exception ex) {
                            System.err.printf("Error reading DelegateConsumer's properties: [%s]\n", propFileName);
                            ex.printStackTrace();
                        }
                    }
					this.setDisplayConsumer(dg.getConsumer()); // Overrides the default one.
				} else {
                    System.err.println("Wrong DelegateConsumer class [%s], expected [DelegateConsumer]");
                }
			} catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException nsme) {
				nsme.printStackTrace();
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
		}

		String betweenLoops = props.getProperty("display.time", "5"); // Works if greater than 0
        try {
            scrollWait = Long.parseLong(betweenLoops) * 1_000L;
        } catch (NumberFormatException nfe) {
            System.err.println("Using default value for display wait time");
        }
        verbose = "true".equals(props.getProperty("screen.verbose", "false"));

        // Data to display on the small screen
        String csv = props.getProperty("to.display", "");
		if (verbose) {
			System.out.printf("To Display: %s\n", csv);
		}
        if (!csv.trim().isEmpty()) {
            Arrays.stream(csv.trim().split(",")).forEach(id -> {
                switch (id) {
                    case "POS": // Position
                        optionList.add(POS_OPTION);
                        break;
                    case "GPS": // GPS Date & Time
                        optionList.add(GPS_OPTION);
                        break;
                    case "SOL": // Solar Date & Time
                        optionList.add(SOL_OPTION);
                        break;
                    case "SYS": // System Date & Time
                        optionList.add(SYS_OPTION);
                        break;
                    case "BSP":
                        optionList.add(BSP_OPTION);
                        speedUnit = SpeedUnit.KNOTS;
                        break;
                    case "SOG": // KMH, MPH Speed in knots, km/h or mph
                        optionList.add(SOG_OPTION);
                        speedUnit = SpeedUnit.KNOTS;
                        break;
                    case "KMH": // KMH, MPH Speed in knots, km/h or mph
                        optionList.add(SOG_OPTION);
                        speedUnit = SpeedUnit.KMH;
                        break;
                    case "MPH": // KMH, MPH Speed in knots, km/h or mph
                        optionList.add(SOG_OPTION);
                        speedUnit = SpeedUnit.MPH;
                        break;
                    case "MS": // SOG, in KMH, MPH Speed in knots, km/h or mph
                        optionList.add(SOG_OPTION);
                        speedUnit = SpeedUnit.MS;
                        break;
                    case "COG": // Course Over Ground
                        optionList.add(COG_OPTION);
                        break;
                    case "HDG": // Heading
                        optionList.add(HDG_OPTION);
                        break;
                    case "TWD": // True Wind Direction
                        optionList.add(TWD_OPTION);
                        break;
                    case "TWS": // - True Wind Speed
                        optionList.add(TWS_OPTION);
                        break;
                    case "TWA": // - True Wind Angle
                        optionList.add(TWA_OPTION);
                        break;
                    case "AWS": // - Apparent Wind Speed
                        optionList.add(AWS_OPTION);
                        break;
                    case "AWA": // - Apparent Wind Angle
                        optionList.add(AWA_OPTION);
                        break;
                    case "WTP": // - Water Temperature
                        optionList.add(WTP_OPTION);
                        break;
                    case "ATP": // - Air Temperature
                        optionList.add(ATP_OPTION);
                        break;
                    case "PML": // - Pressure at Mean Sea Level
                        optionList.add(PRS_OPTION);
                        break;
                    case "HUM": // - Humidity
                        optionList.add(HUM_OPTION);
                        break;
                    case "DBT": // - Depth
                        optionList.add(DBT_OPTION);
                        break;
                    case "CUR": // - Current. Speed and Direction
                        optionList.add(CUR_OPTION);
                        break;
                    case "PCH": // Pitch
                    case "ROL": // Roll
                    default:
                        System.out.printf("[%s] not implemented yet.\n", id);
                        break;
                }
            });
        }
    }
}

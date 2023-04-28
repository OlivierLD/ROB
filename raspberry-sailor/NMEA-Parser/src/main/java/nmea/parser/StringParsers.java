package nmea.parser;

import calc.GeomUtil;

import java.io.PrintStream;
import java.text.NumberFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic form is of a sentence is:
 * <pre>$&lt;talker ID&gt;&lt;sentence ID,&gt;[parameter 1],[parameter 2],...[&lt;*checksum&gt;]&lt;CR&gt;&lt;LF&gt; (\r\n)</pre>
 * <br>
 * Available parsers:
 * <ol>
 *   <li>AAM (Waypoint Arrival Alarm)</li>
 *   <li>APB (Heading/Track Controller (Autopilot) Sentence "B")</li>
 *   <li>BAT (battery status, NOT standard)</li>
 *   <li>BOD (Bearing - Origin to Destination)</li>
 *   <li>BWC, through BWx (Bearing &amp; Distance to Waypoint)</li>
 *   <li>BWR, through BWx (Bearing &amp; Distance to Waypoint - Rhumb Line)</li>
 *   <li>DBT (Depth Below Transducer)</li>
 *   <li>DBS (Depth Below Surface)</li>
 *   <li>DPT (Depth)</li>
 *   <li>GBS (GPS Satellite Fault Detection)</li>
 *   <li>GGA (GPS Data)</li>
 *   <li>GLL (Geographical Latitude Longitude)</li>
 *   <li>GSA (GPS Satellites Data)</li>
 *   <li>GSV (GPS Detailed satellites data)</li>
 *   <li>HDM (Heading, Magnetic)</li>
 *   <li>HDT (Heading, True)</li>
 *   <li>MDA (Meteorological Composite)</li>
 *   <li>MMB (Atmospheric Pressure)</li>
 *   <li>MTA (Air Temperature)</li>
 *   <li>MTW (Water Temperature)</li>
 *   <li>MWD ((True) Wind Direction and Speed)</li>
 *   <li>MWV (Wind Speed and Angle)</li>
 *   <li>RMB (Recommended Minimum, version B)</li>
 *   <li>RMC (Recommended Minimum, version C)</li>
 *   <li>SSD (Ship Static Data)</li>
 *   <li>STD (Not standard, STarteD)</li>
 *   <li>TXT (Text)</li>
 *   <li>VDR (Current Speed and Direction)</li>
 *   <li>VHW (Water, Heading and Speed)</li>
 *   <li>VLW (Distance Travelled through Water)</li>
 *   <li>VPW (Speed - Measured Parallel to Wind, VMG)</li>
 *   <li>VTG (Track Made Good and Ground Speed)</li>
 *   <li>VSD (Voyage Static Data)</li>
 *   <li>VWR (Relative Wind Speed and Angle)</li>
 *   <li>VWT (True Wind Speed and Angle - obsolete)</li>
 *   <li>WCV (Waypoint Closure Velocity)</li>
 *   <li>XDR (Transducers Measurement, Various Sensors)</li>
 *   <li>XTE (Cross Track Error)</li>
 *   <li>ZDA (UTC Date and Time)</li>
 * </ol>
 * See {@link StringParsers.Dispatcher}, {@link #listDispatchers(PrintStream)}
 * <br>
 * Good source: https://gpsd.gitlab.io/gpsd/NMEA.html
 *    Also see: https://www.plaisance-pratique.com/IMG/pdf/NMEA0183-2.pdf
 *
 * TODO: getters for all public static classes ?...
 *
 * More output with -Dnmea.parser.verbose=true
 */
public class StringParsers {

	/*
	Code Units (Comments) below:
	- STRING-PARSERS
	- UTILITIES
	- AUTO-PARSE
	 */

	public final static String NMEA_EOS = "\r\n";
	public final static int MIN_NMEA_LENGTH = 6;

	public final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

	static {
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	private static Map<Integer, SVData> gsvMap = null;

  	/*
  	 * STRING-PARSERS
  	 * StringParsers starting here
  	 */

	public static List<StringGenerator.XDRElement> parseXDR(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		List<StringGenerator.XDRElement> lxdr = new ArrayList<>();
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		if ((sa.length - 1) % 4 != 0) { // Mismatch
			System.out.println("XDR String invalid (" + sa.length + " element(s) found, expected a multiple of 4)");
			return lxdr;
		}
		for (int i = 1; i < sa.length; i += 4) {
			String type = sa[i];
			String valStr = sa[i + 1];
			String unit = sa[i + 2];
			String tname = sa[i + 3];
			// Valid unit and type
			boolean foundType = false;
			boolean foundUnit = false;
			for (StringGenerator.XDRTypes xdrt : StringGenerator.XDRTypes.values()) {
				if (xdrt.type().equals(type)) {
					foundType = true;
					if (xdrt.unit().equals(unit)) {
						foundUnit = true;
						try {
							if (!valStr.trim().isEmpty()) {
								double value = Double.parseDouble(valStr);
								lxdr.add(new StringGenerator.XDRElement(xdrt, value, tname));
							}
						} catch (NumberFormatException nfe) {
							if (!valStr.trim().isEmpty()) {
								throw new RuntimeException(nfe);
							}
						}
						break;
					}
				}
			}
			if (!foundType) {
				System.out.println("Unknown XDR type [" + type + "], in [" + sentence + "]");
				return lxdr;
			}
			if (!foundUnit) {
				System.out.println("Invalid XDR unit [" + unit + "] for type [" + type + "], in [" + sentence + "]");
				return lxdr;
			}
		}
		return lxdr;
	}

	public static class MDA {
		public Double pressInch = null;
		public Double pressBar = null;
		public Double airT = null;
		public Double waterT = null;
		public Double relHum = null;
		public Double absHum = null;
		public Double dewC = null;
		public Double windDirT = null;
		public Double windDirM = null;
		public Double windSpeedK = null;
		public Double windSpeedMS = null;
	}

	/**
	 * MDA Meteorological Composite
	 * <pre>
	 * $--MDA,x.x,I,x.x,B,x.x,C,x.x,C,x.x,x.x,x.x,C,x.x,T,x.x,M,x.x,N,x.x,M*hh&lt;CR&gt;&lt;LF&gt;
	 *        |     |     |     |     |   |   |     |     |     |     |
	 *        |     |     |     |     |   |   |     |     |     |     19-Wind speed, m/s
	 *        |     |     |     |     |   |   |     |     |     17-Wind speed, knots
	 *        |     |     |     |     |   |   |     |     15-Wind dir Mag
	 *        |     |     |     |     |   |   |     13-Wind dir, True
	 *        |     |     |     |     |   |   11-Dew Point C
	 *        |     |     |     |     |   10-Absolute hum %
	 *        |     |     |     |     9-Relative hum %
	 *        |     |     |     7-Water temp in Celsius
	 *        |     |     5-Air Temp in Celsius  |
	 *        |     3-Pressure in Bars
	 *        1-Pressure in inches
	 *
	 * Example: $WIMDA,29.4473,I,0.9972,B,17.2,C,,,,,,,,,,,,,,*3E
	 * </pre>
	 * @param sentence The one to parse
	 * @return The result
	 */
	public static MDA parseMDA(String sentence) {
		final int PRESS_INCH = 1;
		final int PRESS_BAR = 3;
		final int AIR_T = 5;
		final int WATER_T = 7;
		final int REL_HUM = 9;
		final int ABS_HUM = 10;
		final int DEW_P_C = 11;
		final int WD_T = 13;
		final int WD_M = 15;
		final int WS_KNOTS = 17;
		final int WS_MS = 19;

		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}

		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		MDA mda = new MDA();
		for (int i = 0; i < sa.length; i++) {
			//  System.out.println(sa[i]);
			if (/*i % 2 == 1 &&*/ !sa[i].trim().isEmpty()) {
				double d = 0;
				try {
					d = Double.parseDouble(sa[i]);
					switch (i) {
						case PRESS_INCH:
							mda.pressInch = d;
							break;
						case PRESS_BAR:
							mda.pressBar = d;
							break;
						case AIR_T:
							mda.airT = d;
							break;
						case WATER_T:
							mda.waterT = d;
							break;
						case REL_HUM:
							mda.relHum = d;
							break;
						case ABS_HUM:
							mda.absHum = d;
							break;
						case DEW_P_C:
							mda.dewC = d;
							break;
						case WD_T:
							mda.windDirT = d;
							break;
						case WD_M:
							mda.windDirM = d;
							break;
						case WS_KNOTS:
							mda.windSpeedK = d;
							break;
						case WS_MS:
							mda.windSpeedMS = d;
							break;
						default:
							break;
					}
				} catch (NumberFormatException nfe) {
					// Oops
				}
			}
		}
		return mda;
	}

	/**
	 * MMB Atmospheric pressure
	 * <pre>
	 * Structure is $IIMMB,29.9350,I,1.0136,B*7A
	 *                     |       | |      |
	 *                     |       | |      Bars
	 *                     |       | Pressure in Bars
	 *                     |       Inches of Hg
	 *                     Pressure in inches of Hg
	 * </pre>
	 * @param sentence the one to parse
	 * @return Pressure in Mb / hPa
	 */
	public static double parseMMB(String sentence) {
		final int PR_IN_HG = 1;
		final int PR_BARS = 3;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		double d = 0d;
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			d = Double.parseDouble(sa[PR_BARS]);
			d *= 1_000d;
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return d;
	}

	/**
	 * MTA Air Temperature
	 * <pre>
	 * Structure is $IIMTA,020.5,C*30
	 *                     |     |
	 *                     |     Celsius
	 *                     Temperature in Celsius
	 * </pre>
	 * @param sentence the one to parse
	 * @return Temperature in Celsius
	 */
	public static double parseMTA(String sentence) {
		final int TEMP_CELSIUS = 1;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		double d = 0d;
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			d = Double.parseDouble(sa[TEMP_CELSIUS]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return d;
	}

	/**
	 * VDR Current Speed and Direction
	 * <pre>
	 * Structure is $IIVDR,00.0,T,00.0,M,00.0,N*XX
	 *                     |    | |    | |    |
	 *                     |    | |    | |    Knots
	 *                     |    | |    | Speed
	 *                     |    | |    Mag.
	 *                     |    | Magnetic Dir
	 *                     |    True
	 *                     True Dir
	 * </pre>
	 * @param sentence the one to parse
	 * @return The current from the string
	 */
	public static Current parseVDR(String sentence) {
		final int DIR = 1;
		final int SPEED = 5;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		Current current = null;
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			double speed = Double.parseDouble(sa[SPEED]);
			float dir = Float.parseFloat(sa[DIR]);
			current = new Current(Math.round(dir), speed);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		return current;
	}

	/**
	 * Battery voltage
	 * NOT STANDARD !!!
	 * <pre>
	 * Structure is $XXBAT,14.82,V,1011,98*20
	 *                     |     | |    |
	 *                     |     | |    Volume [0..100]
	 *                     |     | ADC [0..1023]
	 *                     |     Volts
	 *                     Voltage
	 * </pre>
	 * @param sentence the one to parse
	 * @return The tension in volts
	 */
	public static float parseBAT(String sentence) {
		final int VOLTAGE = 1;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		float v = -1f;
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			v = Float.parseFloat(sa[VOLTAGE]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return v;
	}

	/**
	 * Cache age
	 * NOT STANDARD !!!
	 * <pre>
	 * Structure is $XXSTD,77672*5C
	 *                     |
	 *                     Cache Age in ms
	 * </pre>
	 * @param sentence the one to parse
	 * @return The cache age, in milliseconds
	 */
	public static long parseSTD(String sentence) {
		final int VALUE = 1;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		long age = 0L;
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			age = Long.parseLong(sa[VALUE]);
		} catch (NumberFormatException nfe) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				nfe.printStackTrace();
			}
		}
		return age;
	}

	private static List<String> gsvData = new ArrayList<>();
	public static List<String> getGSVList() {
		return gsvData;
	}
	/**
	 * GSV Detailed Satellite data
	 * <pre>
	 * Structure is $GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
	 *                     | | |  |  |  |   |  |            |            |
	 *                     | | |  |  |  |   |  |            |            10 - Fourth SV...
	 *                     | | |  |  |  |   |  |            9 - Third SV...
	 *                     | | |  |  |  |   |  8 - Second SV...
	 *                     | | |  |  |  |   7 - SNR (0-99 db)
	 *                     | | |  |  |  6 - Azimuth in degrees (0-359)
	 *                     | | |  |  5 - Elevation in degrees (0-90)
	 *                     | | |  4 - First SV PRN Number
	 *                     | | 3 - Total number of SVs in view, and other meanings.
	 *                     | 2 - Message Number
	 *                     1 - Number of messages in this cycle
	 * </pre>
	 * @param sentence the one to parse
	 * @return SVData Map
	 */
	public static Map<Integer, SVData> parseGSV(String sentence) {
		final int NB_MESS = 1;
		final int MESS_NUM = 2;

		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return gsvMap;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		// System.out.println("String [" + s + "]");
		final int DATA_OFFSET = 3; // num of mess, mess num, Total num of SVs.
		final int NB_DATA = 4; // SV num, elev, Z, SNR

		int nbMess = -1;
		int messNum = -1;

		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			nbMess = Integer.parseInt(sa[NB_MESS]);
			messNum = Integer.parseInt(sa[MESS_NUM]);
			int nbSVinView = Integer.parseInt(sa[DATA_OFFSET]);
			if (messNum == 1) { // Reset
				gsvMap = new HashMap<>(nbSVinView);
				gsvData = new ArrayList<>();
			}
			gsvData.add(s);

			for (int indexInSentence = 1; indexInSentence <= 4; indexInSentence++) {
				int rnkInView = ((messNum - 1) * NB_DATA) + (indexInSentence);
				if (rnkInView <= nbSVinView) {
					int svNum = 0;
					int elev = 0;
					int z = 0;
					int snr = 0;
					try {
						svNum = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 1]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						elev = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 2]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						z = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 3]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					try {
						snr = Integer.parseInt(sa[DATA_OFFSET + ((indexInSentence - 1) * NB_DATA) + 4]);
					} catch (Exception pex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							pex.printStackTrace();
						}
					}
					SVData svd = new SVData(svNum, elev, z, snr);
					if (gsvMap != null) {
						gsvMap.put(svNum, svd);
					}
//        			System.out.println("SV #" + rnkInView + ", SV:" + svNum + " H:"+ elev + ", Z:" + z + ", snr:" + snr);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (messNum != -1 && nbMess != -1 && messNum == nbMess) {
			return gsvMap;
		}
		return null;
	}

	public static String GSVtoString(Map<Integer, SVData> hm) {
		String str = "";
		if (hm != null) {
			str += (hm.size() + " Satellites in view:");
			for (Integer sn : hm.keySet()) {
				SVData svd = hm.get(sn);
				str += ("Satellite #" + svd.getSvID() + " Elev:" + svd.getElevation() + ", Z:" + svd.getAzimuth() + ", SNR:" + svd.getSnr() + "db. ");
			}
		}
		return str.trim();
	}

	public static final int GGA_UTC_IDX = 0;
	public static final int GGA_POS_IDX = 1;
	public static final int GGA_NBSAT_IDX = 2;
	public static final int GGA_ALT_IDX = 3;

	// GGA Global Positioning System Fix Data. Time, Position and fix related data for a GPS receiver
	public static List<Object> parseGGA(String sentence) {
		return parseGGA(sentence, true);
	}
	/**
	 * GGA Global Positioning System Fix Data. Time, Position and fix related data for a GPS receiver.<br>
	 * Structure is:
	 * <pre>
	 *  $GPGGA,014457,3739.853,N,12222.821,W,1,03,5.4,1.1,M,-28.2,M,,*7E
	 *  $aaGGA,hhmmss.ss,llll.ll,a,gggg.gg,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh(CR)(LF)
	 *         |         |         |         | |  |   |   | |   | |   |
	 *         |         |         |         | |  |   |   | |   | |   Differential reference station ID
	 *         |         |         |         | |  |   |   | |   | Age of differential GPS data (seconds)
	 *         |         |         |         | |  |   |   | |   Unit of geodial separation, meters
	 *         |         |         |         | |  |   |   | Geodial separation
	 *         |         |         |         | |  |   |   Unit of antenna altitude, meters
	 *         |         |         |         | |  |   Antenna altitude above sea level
	 *         |         |         |         | |  Horizontal dilution of precision
	 *         |         |         |         | number of satellites in use 00-12 (in use, not in view!)
	 *         |         |         |         GPS quality indicator (0:invalid, 1:GPS fix, 2:DGPS fix)
	 *         |         |         Longitude
	 *         |         Latitude
	 *         UTC of position
	 * </pre>
	 * @param sentence the one to parse
	 * @param useSymbol Use degree symbol for GeoPos.toString
	 * @return List of Objects
	 */
	public static List<Object> parseGGA(String sentence, boolean useSymbol) {
		final int KEY_POS = 0;
		final int UTC_POS = 1;
		final int LAT_POS = 2;
		final int LAT_SGN_POS = 3;
		final int LONG_POS = 4;
		final int LONG_SGN_POS = 5;
		final int GPS_Q_POS = 6;
		final int NBSAT_POS = 7;
		final int ANTENNA_ALT = 9;

		ArrayList<Object> al = null;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return al;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = s.substring(0, s.indexOf("*")).split(",");
		double utc = 0L, lat = 0L, lng = 0L;
		int nbsat = 0;
		try {
			utc = parseNMEADouble(sa[UTC_POS]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

		try {
			double l = parseNMEADouble(sa[LAT_POS]);
			int intL = (int) l / 100;
			double m = ((l / 100.0) - intL) * 100.0;
			m *= (100.0 / 60.0);
			lat = intL + (m / 100.0);
			if ("S".equals(sa[LAT_SGN_POS])) {
				lat = -lat;
			}
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		try {
			double g = parseNMEADouble(sa[LONG_POS]);
			int intG = (int) g / 100;
			double m = ((g / 100.0) - intG) * 100.0;
			m *= (100.0 / 60.0);
			lng = intG + (m / 100.0);
			if ("W".equals(sa[LONG_SGN_POS])) {
				lng = -lng;
			}
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		try {
			nbsat = Integer.parseInt(sa[NBSAT_POS]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

//  System.out.println("UTC:" + utc + ", lat:" + lat + ", lng:" + lng + ", nbsat:" + nbsat);
		int h = (int) (utc / 10_000);
		int m = (int) ((utc - (h * 10_000)) / 100);
		float sec = (float) (utc - ((h * 10_000) + (m * 100)));
//  System.out.println(h + ":" + m + ":" + sec);

//  System.out.println(new GeoPos(lat, lng).toString());
//  System.out.println("Done.");

		double alt = 0;
		try {
			alt = parseNMEADouble(sa[ANTENNA_ALT]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}

		al = new ArrayList<Object>(4);
		al.add(new UTC(h, m, sec));
		al.add(new GeoPos(lat, lng, useSymbol));
		al.add(nbsat);
		al.add(alt);

		return al;
	}

	public static class GBS {
		UTCDate utcDate;
		double sigmaErrorLat =0d, sigmaErrorLong = 0d, sigmaErrorAlt = 0d;
		int satId = 0; // TODO Double ?
		double probMissedDetection = 0d, biasInMeters = 0d, stdDevOfBias = 0d;

		private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss 'UTC'");
		static {
			sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}

		@Override
		public String toString() {
			return String.format("HMS: %s, SigErrLat:%.02f, SigErrLng:%.02f, SigErrAlt:%.02f, SatID: %s, PMD: %.03f%%, BIM: %.02fm, SDB: %.03f",
					utcDate.toString(sdf), sigmaErrorLat, sigmaErrorLong, sigmaErrorAlt,
					(satId != 0 ? String.valueOf(satId) : "n/a"),
					probMissedDetection, biasInMeters, stdDevOfBias);
		}
	}
	/**
    GPS Satellite Fault Detection
<pre>
$--GBS,hhmmss.ss,x.x,x.x,x.x,x.x,x.x,x.x,x.x*hh&lt;CR&gt;&lt;LF&gt;
    |         |   |   |   |   |   |   |   |
    |         |   |   |   |   |   |   |   9 - Checksum
    |         |   |   |   |   |   |   8 - Standard deviation of bias estimate
    |         |   |   |   |   |   7 - Estimate of bias in meters on most likely failed satellite
    |         |   |   |   |   6 - Probability of missed detection for most likely failed satellite
    |         |   |   |   5 - ID of most likely failed satellite (1 to 138)
    |         |   |   4 - Expected 1-sigma error in altitude (meters)
    |         |   3 - Expected 1-sigma error in longitude (meters)
    |         2 - Expected 1-sigma error in latitude (meters)
    1 - UTC time of the GGA or GNS fix associated with this sentence. hh is hours, mm is minutes, ss.ss is seconds

Example: $GPGBS,125027,23.43,M,13.91,M,34.01,M*07 -- ??? (from https://gpsd.gitlab.io/gpsd/NMEA.html#_gbs_gps_satellite_fault_detection)
      $GPGBS,163317.00,7.3,5.2,11.7,,,,*74
             |         |   |   |
             |         |   |   4
             |         |   3
             |         2
             1
</pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static GBS parseGBS(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");

		GBS gbs = new GBS();

		if (sa.length > 1 && sa[1].length() > 0) {
			String utc = sa[1];
			int hours = Integer.parseInt(utc.substring(0, 2));
			int mins = Integer.parseInt(utc.substring(2, 4));
			float secs = Float.parseFloat(utc.substring(4));
			UTCDate utcDate = new UTCDate(null, null, null, hours, mins, (int)Math.round(secs), null);
			gbs.utcDate = utcDate;
		}
		if (sa.length > 2 && sa[2].length() > 0) {
			gbs.sigmaErrorLat = Double.parseDouble(sa[2]);
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			gbs.sigmaErrorLong = Double.parseDouble(sa[3]);
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			gbs.sigmaErrorAlt = Double.parseDouble(sa[4]);
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			gbs.satId = Integer.parseInt(sa[5]);
		}
		if (sa.length > 6 && sa[6].length() > 0) {
			gbs.probMissedDetection = Double.parseDouble(sa[6]);
		}
		if (sa.length > 7 && sa[7].length() > 0) {
			gbs.biasInMeters = Double.parseDouble(sa[7]);
		}
		if (sa.length > 8 && sa[8].length() > 0) {
			gbs.stdDevOfBias = Double.parseDouble(sa[8]);
		}
		return gbs;
	}

	/**
	 * GSA GPS DOP and active satellites
	 * <pre>
	 * $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35
	 *        | | |                           |   |   |
	 *        | | |                           |   |   VDOP
	 *        | | |                           |   HDOP
	 *        | | |                           PDOP (dilution of precision). No unit; the smaller the better.
	 *        | | IDs of the SVs used in fix (up to 12)
	 *        | Mode: 1=Fix not available, 2=2D, 3=3D
	 *        Mode: M=Manual, forced to operate in 2D or 3D
	 *              A=Automatic, 3D/2D
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static GSA parseGSA(String sentence) {
		final int MODE_1 = 1;
		final int MODE_2 = 2;
		final int PDOP = 15;
		final int HDOP = 16;
		final int VDOP = 17;
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		GSA gsa = new GSA();
		String[] elements = sentence.substring(0, sentence.indexOf("*")).split(",");
		if (elements.length >= 2) {
			if ("M".equals(elements[MODE_1])) {
				gsa.setMode1(GSA.ModeOne.Manual);
			}
			if ("A".equals(elements[MODE_1])) {
				gsa.setMode1(GSA.ModeOne.Auto);
			}
		}
		if (elements.length >= 3) {
			if ("1".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.NoFix);
			}
			if ("2".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.TwoD);
			}
			if ("3".equals(elements[MODE_2])) {
				gsa.setMode2(GSA.ModeTwo.ThreeD);
			}
		}
		for (int i = 3; i < 15; i++) {
			if (!elements[i].trim().isEmpty()) {
				int sv = Integer.parseInt(elements[i]);
				gsa.getSvArray().add(sv);
			}
		}
		if (elements.length >= 16) {
			gsa.setPDOP(Float.parseFloat(elements[PDOP]));
		}
		if (elements.length >= 17) {
			gsa.setHDOP(Float.parseFloat(elements[HDOP]));
		}
		if (elements.length >= 18) {
			gsa.setVDOP(Float.parseFloat(elements[VDOP]));
		}
		return gsa;
	}

	// VHW Water speed and heading
	public static VHW parseVHW(String sentence) {
		return parseVHW(sentence, 0d);
	}

	/**
	 * VHW Water speed and heading <br>
	 * Structure is
	 * <pre>
	 *         1   2 3   4 5   6 7   8
	 *  $aaVHW,x.x,T,x.x,M,x.x,N,x.x,K*hh(CR)(LF)
	 *         |     |     |     |
	 *         |     |     |     Speed in km/h
	 *         |     |     Speed in knots
	 *         |     Heading in degrees, Magnetic
	 *         Heading in degrees, True
	 * </pre>
	 * @param sentence the one to parse
	 * @param defaultBSP Used if missing
	 * @return The result.
	 */
	public static VHW parseVHW(String sentence, double defaultBSP) {
		final int HDG_IN_DEG_TRUE = 1;
		final int HDG_IN_DEG_MAG = 3;
		final int SPEED_IN_KN = 5;

		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		// We're interested only in Speed in knots.
		double speed = defaultBSP;
		double hdm = -1d; // set to -1.Means not found.
		double hdg = -1d;

		try {
			String[] nmeaElements = sentence.substring(0, sentence.indexOf("*")).split(",");
			try {
				speed = parseNMEADouble(nmeaElements[SPEED_IN_KN]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				hdm = parseNMEADouble(nmeaElements[HDG_IN_DEG_MAG]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				hdg = parseNMEADouble(nmeaElements[HDG_IN_DEG_TRUE]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		return new VHW().bsp(speed).hdm(hdm).hdg(hdg);
	}

	/**
	 * VLW Distance Traveled through Water<br>
	 * Structure is
	 * <pre>
	 * $aaVLW,x.x,N,x.x,N*hh&lt;CR&gt;&lt;LF&gt;
	 *        |   | |   |
	 *        |   | |   Nautical miles
	 *        |   | Distance since reset
	 *        |   Nautical miles
	 *        Total cumulative distance
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static VLW parseVLW(String sentence) {
		final int CUM_DIST = 1;
		final int SINCE_RESET = 3;

		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return (VLW) null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		double cumulative = 0d;
		double sinceReset = 0d;
		try {
			String[] nmeaElements = sentence.substring(0, sentence.indexOf("*")).split(",");
			cumulative = parseNMEADouble(nmeaElements[CUM_DIST]);
			sinceReset = parseNMEADouble(nmeaElements[SINCE_RESET]);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return new VLW().log(cumulative).daily(sinceReset);
	}

	/**
	 * MTW Water Temperature<br>
	 * Structure is
	 * <pre>
	 * $xxMTW,+18.0,C*hh
	 *        |     |
	 *        |     Celsius
	 *        Temparature
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result, in Celsius, as a double
	 */
	public static double parseMTW(String sentence) {
		final int TEMP_CELSIUS = 1;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return 0d;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}

		double temp = 0d;
		try {
			String[] nmeaElements = sentence.substring(0, sentence.indexOf("*")).split(",");
			String _s = nmeaElements[TEMP_CELSIUS];
			if (_s.startsWith("+")) {
				_s = _s.substring(1);
			}
			temp = parseNMEADouble(_s);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0d;
		}
		return temp;
	}

	public static final int TRUE_WIND = 0;
	public static final int APPARENT_WIND = 1;

	/**
	 * MWV Wind Speed and Angle<br>
	 * AWA, AWS (R), possibly TWA, TWS (T)<br>
	 * Structure is<br>
	 * <pre>
	 *  $aaMWV,x.x,a,x.x,a,A*hh
	 *         |   | |   | |
	 *         |   | |   | status : A=data valid
	 *         |   | |   Wind Speed unit (K/M/N)
	 *         |   | Wind Speed
	 *         |   reference R=relative, T=true
	 *         Wind angle 0 to 360 degrees
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static Wind parseMWV(String sentence) {
		int flavor = -1;

		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		// We're interested only in Speed in knots.
		Wind aw = null;
		try {
			if (!s.contains("A*")) { // Data invalid
				return aw;
			} else {
				String speed = "", angle = "";
				if (s.contains("MWV,") && s.contains(",R,")) { // Apparent
					flavor = APPARENT_WIND;
					angle = s.substring(s.indexOf("MWV,") + "MWV,".length(), s.indexOf(",R,"));
				}
				if (s.contains(",R,") && s.contains(",N,")) {
					speed = s.substring(s.indexOf(",R,") + ",R,".length(), s.indexOf(",N,"));
				}
				if (speed.trim().isEmpty() && angle.trim().isEmpty()) {
					if (s.contains("MWV,") && s.contains(",T,")) {
						flavor = TRUE_WIND;
						angle = s.substring(s.indexOf("MWV,") + "MWV,".length(), s.indexOf(",T,"));    // True
					}
					if (s.contains(",T,") && s.contains(",N,")) {
						speed = s.substring(s.indexOf(",T,") + ",T,".length(), s.indexOf(",N,"));
					}
				}
				float awa = 0f;
				double aws = 0d;
				try {
					awa = parseNMEAFloat(angle);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				try {
					aws = parseNMEADouble(speed);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				if (flavor == APPARENT_WIND) {
					aw = new ApparentWind(Math.round(awa), aws);
				} else if (flavor == TRUE_WIND) {
					aw = new TrueWind(Math.round(awa), aws);
				} else {
					System.out.println("UNKNOWN wind type!");
				}
			}
		} catch (Exception e) {
			System.err.println("parseMWV for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return aw;
	}

	/**
	 * MWD Wind Direction &amp; Speed<br>
	 * <pre>
	 * $WIMWD,&lt;1&gt;,&lt;2&gt;,&lt;3&gt;,&lt;4&gt;,&lt;5&gt;,&lt;6&gt;,&lt;7&gt;,&lt;8&gt;*hh
	 *
	 * NMEA 0183 standard Wind Direction and Speed, with respect to north.
	 *
	 * &lt;1&gt; Wind direction, 0.0 to 359.9 degrees True, to the nearest 0.1 degree
	 * &lt;2&gt; T = True
	 * &lt;3&gt; Wind direction, 0.0 to 359.9 degrees Magnetic, to the nearest 0.1 degree
	 * &lt;4&gt; M = Magnetic
	 * &lt;5&gt; Wind speed, knots, to the nearest 0.1 knot.
	 * &lt;6&gt; N = Knots
	 * &lt;7&gt; Wind speed, meters/second, to the nearest 0.1 m/s.
	 * &lt;8&gt; M = Meters/second
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static TrueWind parseMWD(String sentence) {
		TrueWind tw = null;
		if (validCheckSum(sentence)) {
			String[] part = sentence.split(",");
			double dir = 0;
			double speed = 0;
			if ("T".equals(part[2])) {
				dir = Double.parseDouble(part[1]);
			}
			if ("N".equals(part[6])) {
				speed = Double.parseDouble(part[5]);
			}
			tw = new TrueWind((int)Math.round(dir), speed);
		} else {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		return tw;
	}

	/**
	 * VWT True WindSpeed and Angle (obsolete)
	 * <pre>
	 * $--VWT,x.x,a,x.x,N,x.x,M,x.x,K*hh&lt;CR&gt;&lt;LF&gt;
	 *        |     |     |     |
	 *        |     |     |     Wind speed, Km/Hr
	 *        |     |     Wind speed, meters/second
	 *        |     Calculated wind Speed, knots
	 *        Calculated wind angle relative to the vessel, 0 to 180, left/right L/R of vessel heading
     * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static TrueWind parseVWT(String sentence) {
		TrueWind wind = null;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		try {
			// TODO Implement ?
		} catch (Exception e) {
			System.err.println("parseVWT for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return wind;
	}

	/**
	 * VWR Relative Wind Speed and Angle (AWA, AWS)<br>
	 * Structure is<br>
	 * <pre>
	 *  $aaVWR,x.x,a,x.x,N,x.x,M,x.x,K*hh
	 *         |   | |     |     |
	 *         |   | |     |     Wind Speed, in km/h
	 *         |   | |     Wind Speed, in m/s
	 *         |   | Wind Speed, in knots
	 *         |   L=port, R=starboard
	 *         Wind angle 0 to 180 degrees
	 *
	 * Example: VWR,148.,L,02.4,N,01.2,M,04.4,K*XX
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static ApparentWind parseVWR(String sentence) {
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		// We're interested only in Speed in knots.
		ApparentWind aw = null;
		try {
			if (false && !s.contains("K*")) { // Data invalid // Watafok???
				return aw;
			} else {
				String speed = "", angle = "", side = "";
				int firstCommaIndex = s.indexOf(",");
				int secondCommaIndex = s.indexOf(",", firstCommaIndex + 1);
				int thirdCommaIndex = s.indexOf(",", secondCommaIndex + 1);
				int fourthCommaIndex = s.indexOf(",", thirdCommaIndex + 1);
				if (firstCommaIndex > -1 && secondCommaIndex > -1) {
					angle = s.substring(firstCommaIndex + 1, secondCommaIndex);
				}
				while (angle.endsWith(".")) {
					angle = angle.substring(0, angle.length() - 1);
				}
				if (secondCommaIndex > -1 && thirdCommaIndex > -1) {
					side = s.substring(secondCommaIndex + 1, thirdCommaIndex);
				}
				if (thirdCommaIndex > -1 && fourthCommaIndex > -1) {
					speed = s.substring(thirdCommaIndex + 1, fourthCommaIndex);
				}
				double ws = 0d;
				try {
					ws = parseNMEADouble(speed);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				int wa = 0;
				try {
					wa = Integer.parseInt(angle);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				if (side.equals("L")) {
					wa = 360 - wa;
				}
				aw = new ApparentWind(wa, ws);
			}
		} catch (Exception e) {
			System.err.println("parseMWV for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return aw;
	}

	/**
	 * VTG Track made good and Ground speed<br>
	 * Structure is<br>
	 * <pre>
	 * $IIVTG,x.x,T,x.x,M,x.x,N,x.x,K,A*hh
	 *        |   | |  |  |   | |___|
     *        |   | |  |  |   | SOG, km/h
     *        |   | |  |  |___|
     *        |   | |  |  SOG, knots
     *        |   | |__|
     *        |   | COG, mag
     *        |___|
     *        COG, true
     *
     * $IIVTG,17.,T,M,7.9,N,,*36 // B&amp;G does this...
     * $IIVTG,,T,338.,M,N,,*28   // or this...
     * $IIVTG,054.7,T,034.4,M,005.5,N,010.2,K,A*XX
     *        054.7,T      True track made good
     *        034.4,M      Magnetic track made good
     *        005.5,N      Ground speed, knots
	 *        010.2,K      Ground speed, Kilometers per hour
     * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static OverGround parseVTG(String sentence) {
		String s = sentence.trim();
		OverGround og = null;
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		// We're interested only in Speed in knots.
		try {
			if (false && !s.contains("A*")) { // Data invalid, only for NMEA 2.3 and later
				return og;
			} else {
				String speed = "", angle = "";
				String[] sa = s.split(",");

				int tIndex = -1;
				for (int i = 0; i < sa.length; i++) {
					if ("T".equals(sa[i])) {
						tIndex = i;
						break;
					}
				}
				int nIndex = -1;
				for (int i = 0; i < sa.length; i++) {
					if ("N".equals(sa[i])) {
						nIndex = i;
						break;
					}
				}
				angle = sa[tIndex - 1];
				speed = sa[nIndex - 1];
				if (speed.endsWith(".")) {
					speed += "0";
				}
				double sog = parseNMEADouble(speed);
				if (angle.endsWith(".")) {
					angle += "0";
				}
				int cog = (int) Math.round(parseNMEADouble(angle));
				og = new OverGround(sog, cog);
			}
		} catch (Exception e) {
			if ("true".equals(System.getProperty("nmea.parser.verbose", "false"))) {
				System.err.println("parseVTG for " + s + ", " + e.toString());
			}
//    e.printStackTrace();
		}
		return og;
	}

	// GLL Geographical Latitude & Longitude
	public static GLL parseGLL(String senntence) {
		return parseGLL(senntence, true);
	}
	/**
	 * GLL Geographical Latitude &amp; Longitude<br>
	 * Structure is
	 * <pre>
	 *  $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A,D*hh
	 *         |       | |       | |         | |
	 *         |       | |       | |         | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator (not always there)
	 *         |       | |       | |         A:data valid (Active), V: void
	 *         |       | |       | UTC of position
	 *         |       | |       Long sign :E/W
	 *         |       | Longitude
	 *         |       Lat sign :N/S
	 *         Latitude
	 * </pre>
	 * @param sentence the one to parse
	 * @param useSymbol Use degree symbol in GeoPos.toString()
	 * @return The result.
	 */
	public static GLL parseGLL(String sentence, boolean useSymbol) {
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		GeoPos ll = null;
		Date date = null;
		try {
			if (!s.contains("A*")) { // Not Active, Data invalid (void)
				return null;
			} else {
				int i = s.indexOf(",");
				if (i > -1) {
					String lat = "";
					int j = s.indexOf(",", i + 1);
					lat = s.substring(i + 1, j);
					double l = parseNMEADouble(lat);
					int intL = (int) l / 100;
					double m = ((l / 100.0) - intL) * 100.0;
					m *= (100.0 / 60.0);
					l = intL + (m / 100.0);
					String latSgn = s.substring(j + 1, j + 2);
					if (latSgn.equals("S")) {
						l *= -1.0;
					}
					int k = s.indexOf(",", j + 3);
					String lng = s.substring(j + 3, k);
					double g = parseNMEADouble(lng);
					int intG = (int) g / 100;
					m = ((g / 100.0) - intG) * 100.0;
					m *= (100.0 / 60.0);
					g = intG + (m / 100.0);
					String lngSgn = s.substring(k + 1, k + 2);
					if (lngSgn.equals("W")) {
						g *= -1.0;
					}
					ll = new GeoPos(l, g, useSymbol);
					k = s.indexOf(",", k + 2);
					String dateStr = s.substring(k + 1);
					if (dateStr.indexOf(",") > 0) {
						dateStr = dateStr.substring(0, dateStr.indexOf(","));
					}
					double utc = 0D;
					try {
						utc = parseNMEADouble(dateStr);
					} catch (Exception ex) { /*System.out.println("dateStr in StringParsers.parseGLL"); */ }
					int h = (int) (utc / 10_000);
					int mn = (int) ((utc - (10_000 * h)) / 100);
					float sec = (float) (utc % 100f);
					Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//					local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
					local.set(Calendar.YEAR, 1_970);
					local.set(Calendar.MONTH, Calendar.JANUARY);
					local.set(Calendar.DAY_OF_MONTH, 1);
					local.set(Calendar.HOUR_OF_DAY, h);
					local.set(Calendar.MINUTE, mn);
					local.set(Calendar.SECOND, Math.round(sec));
					local.set(Calendar.MILLISECOND, 0);
					try {
						date = local.getTime();
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("parseGLL for [" + s + "] " + e.toString());
		}
		return new GLL().gllPos(ll).gllDate(date);
	}

	/**
	 * HDT Heading - True or Mag<br>
	 * Structure is
	 * <pre>
	 *  $aaHDT,xxx,M*hh(CR)(LF)
	 *         |   |
	 *         |   Magnetic, True
	 *         Heading in degrees
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result, as an int.
	 */
	public static int parseHDT(String sentence) {
		final int KEY_POS = 0;
		final int HDG_POS = 1;
		final int MT_POS = 2;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return -1;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		int hdg = 0;

		String[] elmts = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			if (elmts[KEY_POS].contains("HDT")) {
				if ("T".equals(elmts[MT_POS])) {
					hdg = Math.round(parseNMEAFloat(elmts[HDG_POS]));
				} else {
					throw new RuntimeException("Wrong type [" + elmts[HDG_POS] + "] in parseHDT.");
				}
			} else {
				System.err.println("Wrong chain in parseHDT [" + sentence + "]");
			}
		} catch (Exception e) {
			System.err.println("parseHDT for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return hdg;
	}

	/**
	 * HDM Heading (Mag., True)<br>
	 * Structure is
	 * <pre>
	 *  $aaHDM,xxx,M*hh(CR)(LF)
	 *         |   |
	 *         |   Magnetic, True
	 *         Heading in degrees
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result, as an int.
	 */
	public static int parseHDM(String sentence) {
		final int KEY_POS = 0;
		final int HDG_POS = 1;
		final int MT_POS = 2;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return -1;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		int hdg = 0;

		String[] elmts = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			if (elmts[KEY_POS].contains("HDM")) {
				if ("M".equals(elmts[MT_POS])) {
					hdg = Math.round(parseNMEAFloat(elmts[HDG_POS]));
				} else {
					throw new RuntimeException("Wrong type [" + elmts[HDG_POS] + "] in parseHDM.");
				}
			} else {
				System.err.println("Wrong chain in parseHDM [" + sentence + "]");
			}
		} catch (Exception e) {
			System.err.println("parseHDM for " + s + ", " + e.toString());
//    e.printStackTrace();
		}
		return hdg;
	}

	public static String parseHDMtoString(String s) {
		String ret = "";
		try {
			ret = Integer.toString(parseHDM(s));
		} catch (Exception ignore) {
		}
		return ret;
	}

	/**
	 * HDG - Magnetic heading, deviation, variation<br>
	 * Structure is
	 * <pre>
	 * $xxHDG,x.x,x.x,a,x.x,a*hh&lt;CR&gt;&lt;LF&gt;
	 *        |   |   | |   | |
	 *        |   |   | |   | Checksum
	 *        |   |   | |   Magnetic Variation direction, E = Easterly, W = Westerly
	 *        |   |   | Magnetic Variation degrees
	 *        |   |   Magnetic Deviation direction, E = Easterly, W = Westerly
	 *        |   Magnetic Deviation, degrees
	 *        Magnetic Sensor heading in degrees
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static HDG parseHDG(String sentence) {
		HDG ret = null;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return ret;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		double hdg = 0d;
		double dev = 0d; // -Double.MAX_VALUE;
		double var = 0d; // -Double.MAX_VALUE;
		try {
			String[] nmeaElements = sentence.substring(0, sentence.indexOf("*")).split(",");
			try {
				hdg = parseNMEADouble(nmeaElements[1]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			try {
				dev = parseNMEADouble(nmeaElements[2]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			if (nmeaElements.length > 3 && nmeaElements[3] != null && "W".equals(nmeaElements[3]))
				dev = -dev;
			try {
				var = parseNMEADouble(nmeaElements[4]);
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			if (nmeaElements.length > 5 && nmeaElements[5] != null && "W".equals(nmeaElements[5])) {
				var = -var;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		ret = new HDG().heading(hdg).deviation(dev).variation(var);

		return ret;
	}

	// RMB Recommended Minimum Navigation Information
	public static RMB parseRMB(String sentence) {
		return parseRMB(sentence, true);
	}
	/**
	 * RMB Recommended Minimum Navigation Information<br>
	 * <pre>
	 *        1 2   3 4    5    6       7 8        9 10  11  12  13
	 * $GPRMB,A,x.x,a,c--c,d--d,llll.ll,e,yyyyy.yy,f,g.g,h.h,i.i,j*kk
	 *        | |   | |    |    |       | |        | |   |   |   |
	 *        | |   | |    |    |       | |        | |   |   |   A=Entered or perpendicular passed, V:not there yet
	 *        | |   | |    |    |       | |        | |   |   Destination closing velocity in knots
	 *        | |   | |    |    |       | |        | |   Bearing to destination, degrees, True
	 *        | |   | |    |    |       | |        | Range to destination, nm
	 *        | |   | |    |    |       | |        E or W
	 *        | |   | |    |    |       | Destination Waypoint longitude
	 *        | |   | |    |    |       N or S
	 *        | |   | |    |    Destination Waypoint latitude
	 *        | |   | |    Destination Waypoint ID
	 *        | |   | Origin Waypoint ID
	 *        | |   Direction to steer (L or R) to correct error
	 *        | Crosstrack error in nm
	 *        Data Status (Active or Void)
	 * </pre>
	 * @param sentence the one to parse
	 * @param useSymbol Use degree symbol in GeoPos.toString()
	 * @return The result.
	 */
	public static RMB parseRMB(String sentence, boolean useSymbol) {
		final int RMB_STATUS = 1;
		final int RMB_XTE = 2;
		final int RMB_STEER = 3;
		final int RMB_ORIGIN_WP = 4;
		final int RMB_DEST_WP = 5;
		final int RMB_DEST_WP_LAT = 6;
		final int RMB_DEST_WP_LAT_SIGN = 7;
		final int RMB_DEST_WP_LNG = 8;
		final int RMB_DEST_WP_LNG_SIGN = 9;
		final int RMB_RANGE_TO_DEST = 10;
		final int RMB_BEARING_TO_DEST = 11;
		final int RMB_DEST_CLOSING = 12;
		final int RMB_INFO = 13;

		RMB rmb = null;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		try {
			if (s.contains("RMB,")) {
				rmb = new RMB();
				String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
				if (sa[RMB_STATUS].equals("V")) { // Void
					return null;
				}
				double xte = 0d;
				try {
					xte = parseNMEADouble(sa[RMB_XTE]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setXte(xte);
				rmb.setDts(sa[RMB_STEER]);
				rmb.setOwpid(sa[RMB_ORIGIN_WP]);
				rmb.setDwpid(sa[RMB_DEST_WP]);

				if (sa[RMB_DEST_WP_LAT].length() > 0 && sa[RMB_DEST_WP_LAT_SIGN].length() > 0 && sa[RMB_DEST_WP_LNG].length() > 0 && sa[RMB_DEST_WP_LNG_SIGN].length() > 0) {
					double _lat = 0d;
					try {
						_lat = parseNMEADouble(sa[RMB_DEST_WP_LAT]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					double lat = (int) (_lat / 100d) + ((_lat % 100d) / 60d);
					if ("S".equals(sa[RMB_DEST_WP_LAT_SIGN])) {
						lat = -lat;
					}
					double _lng = 0d;
					try {
						_lng = parseNMEADouble(sa[RMB_DEST_WP_LNG]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					double lng = (int) (_lng / 100d) + ((_lng % 100d) / 60d);
					if ("W".equals(sa[RMB_DEST_WP_LNG_SIGN])) {
						lng = -lng;
					}
					rmb.setDest(new GeoPos(lat, lng, useSymbol));
				} else {
					rmb.setDest(null);
				}
				double rtd = 0d;
				try {
					rtd = parseNMEADouble(sa[RMB_RANGE_TO_DEST]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setRtd(rtd);
				double btd = 0d;
				try {
					btd = parseNMEADouble(sa[RMB_BEARING_TO_DEST]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setBtd(btd);
				double dcv = 0d;
				try {
					dcv = parseNMEADouble(sa[RMB_DEST_CLOSING]);
				} catch (Exception ex) {
					if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
						ex.printStackTrace();
					}
				}
				rmb.setDcv(dcv);
				rmb.setAs(sa[RMB_INFO]);
			}
		} catch (Exception e) {
			System.err.println("parseRMB for " + s + ", " + e.toString());
		}
		return rmb;
	}

	// RMC Recommended minimum specific GPS/Transit data
	public static RMC parseRMC(String sentence) {
		return parseRMC(sentence, true);
	}
	/**
	 * RMC Recommended minimum specific GPS/Transit data<br>
	 * RMC Structure is
	 * <pre>
	 *                                                                    12
	 *         1      2 3        4 5         6 7     8     9      10    11
	 *  $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W,T*6A
	 *         |      | |        | |         | |     |     |      |     | |
	 *         |      | |        | |         | |     |     |      |     | Type: A=autonomous, D=differential, E=Estimated, N=not valid, S=Simulator
	 *         |      | |        | |         | |     |     |      |     Variation sign
	 *         |      | |        | |         | |     |     |      Variation value
	 *         |      | |        | |         | |     |     Date DDMMYY (see rmc.date.offset property)
	 *         |      | |        | |         | |     COG
	 *         |      | |        | |         | SOG
	 *         |      | |        | |         Longitude Sign
	 *         |      | |        | Longitude Value
	 *         |      | |        Latitude Sign
	 *         |      | Latitude value
	 *         |      Active or Void
	 *         UTC
	 * </pre>
	 * @param sentence the one to parse
	 * @param useSymbol Use degree symbol in GeoPos.toString()
	 * @return The result.
	 */
	public static RMC parseRMC(String sentence, boolean useSymbol) {
		final int RMC_UTC = 1;
		final int RMC_ACTIVE_VOID = 2;
		final int RMC_LATITUDE_VALUE = 3;
		final int RMC_LATITUDE_SIGN = 4;
		final int RMC_LONGITUDE_VALUE = 5;
		final int RMC_LONGITUDE_SIGN = 6;
		final int RMC_SOG = 7;
		final int RMC_COG = 8;
		final int RMC_DDMMYY = 9;
		final int RMC_VARIATION_VALUE = 10;
		final int RMC_VARIATION_SIGN = 11;
		final int RMC_TYPE = 12;

		RMC rmc = null;
//		String str = StringUtils.removeNullsFromString(strOne.trim()); // TODO Do it at the consumer level
		if (sentence.length() < 6 || !sentence.contains("*")) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String s = sentence.substring(0, sentence.indexOf("*"));
		try {
			if (s.contains("RMC,")) {
				rmc = new RMC();

				String[] sa = s.split(",");
				rmc = rmc.setValid(sa[RMC_ACTIVE_VOID].equals("A")); // Active. Does not prevent the date and time from being available.
				if (sa[RMC_UTC].length() > 0) { // Time and Date
					double utc = 0D;
					try {
						utc = parseNMEADouble(sa[RMC_UTC]);
					} catch (Exception ex) {
						System.out.println("data[1] in StringParsers.parseRMC");
					}
					int h = (int) (utc / 10_000);
					int m = (int) ((utc - (10_000 * h)) / 100);
					float sec = (float) (utc % 100f);

//        			System.out.println("Data[1]:" + data[1] + ", h:" + h + ", m:" + m + ", s:" + sec);

					Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//					local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
					local.set(Calendar.HOUR_OF_DAY, h);
					local.set(Calendar.MINUTE, m);
					local.set(Calendar.SECOND, (int) Math.round(sec));
					local.set(Calendar.MILLISECOND, 0);
					if (sa[RMC_DDMMYY].length() > 0) {
						int d = 1;
						try {
							d = Integer.parseInt(sa[RMC_DDMMYY].substring(0, 2));
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						int mo = 0;
						try {
							mo = Integer.parseInt(sa[RMC_DDMMYY].substring(2, 4)) - 1;
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						int y = 0;
						try {
							y = Integer.parseInt(sa[RMC_DDMMYY].substring(4));
						} catch (Exception ex) {
							if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
								ex.printStackTrace();
							}
						}
						if (y > 50) {
							y += 1900;
						} else {
							y += 2_000;
						}
						local.set(Calendar.DATE, d);
						local.set(Calendar.MONTH, mo);
						local.set(Calendar.YEAR, y);
						// In case the GPS date is wrong (it happens):
						String gpsOffset = System.getProperty("rmc.date.offset");
						// Offset in DAYS to add to the RMC Date.
						// One of mines has an offset of 7168 (0x1C00) days.
						if (gpsOffset != null) {
							try {
								int offset = Integer.parseInt(gpsOffset);
								if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
									System.out.printf(">>> Adding %d days to %s\n", offset, local.getTime().toString());
								}
								local.add(Calendar.DATE, offset); // Add in Days
								if ("true".equals(System.getProperty("rmc.date.offset.verbose"))) {
									System.out.printf(">>>   that becomes %s\n", local.getTime().toString());
								}
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
							}
						}
						Date rmcDate = local.getTime();
						rmc = rmc.setRmcDate(rmcDate);
					}
					Date rmcTime = local.getTime();
					rmc = rmc.setRmcTime(rmcTime);
					if ("true".equals(System.getProperty("RMC.verbose"))) {
						System.out.printf("RMC: From [%s], GPS date: %s, GPS Time: %s\n", sentence, SDF_UTC.format(rmc.getRmcDate()), SDF_UTC.format(rmcTime));
					}
				}
				if (sa[RMC_LATITUDE_VALUE].length() > 0 && sa[RMC_LONGITUDE_VALUE].length() > 0) {
					String deg = sa[RMC_LATITUDE_VALUE].substring(0, 2);
					String min = sa[RMC_LATITUDE_VALUE].substring(2);
					double l = GeomUtil.sexToDec(deg, min);
					if ("S".equals(sa[RMC_LATITUDE_SIGN])) {
						l = -l;
					}
					deg = sa[RMC_LONGITUDE_VALUE].substring(0, 3);
					min = sa[RMC_LONGITUDE_VALUE].substring(3);
					double g = GeomUtil.sexToDec(deg, min);
					if ("W".equals(sa[RMC_LONGITUDE_SIGN])) {
						g = -g;
					}
					rmc = rmc.setGp(new GeoPos(l, g, useSymbol));
				}
				if (sa[RMC_SOG].length() > 0) {
					double speed = 0;
					try {
						speed = parseNMEADouble(sa[RMC_SOG]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					rmc.setSog(speed);
				}
				if (sa[RMC_COG].length() > 0) {
					double cog = 0;
					try {
						cog = parseNMEADouble(sa[RMC_COG]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					rmc.setCog(cog);
				}
				if (sa[RMC_VARIATION_VALUE].length() > 0 && sa[RMC_VARIATION_SIGN].length() > 0) {
					double d = -Double.MAX_VALUE;
					try {
						d = parseNMEADouble(sa[RMC_VARIATION_VALUE]);
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					if ("W".equals(sa[RMC_VARIATION_SIGN]))
						d = -d;
					rmc = rmc.setDeclination(d);
				}
				if (sa.length > 12) { // Can be missing
					switch (sa[RMC_TYPE]) {
						case "A":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.AUTONOMOUS);
							break;
						case "D":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.DIFFERENTIAL);
							break;
						case "E":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.ESTIMATED);
							break;
						case "N":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.NOT_VALID);
							break;
						case "S":
							rmc = rmc.setRmcType(RMC.RMC_TYPE.SIMULATOR);
							break;
						default:
							rmc = rmc.setRmcType(null);
							break;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("In parseRMC for " + sentence.trim() + ", " + e.toString());
			e.printStackTrace();
		}
		return rmc;
	}

	public static String parseRMCtoString(String sentence) {
		String ret = "";
		try {
			ret = parseRMC(sentence).toString();
		} catch (Exception ignore) {
		}
		return ret;
	}

	public static String getLatFromRMC(String sentence) {
		String result = "";
		try {
			RMC rmc = parseRMC(sentence);
			result = Double.toString(rmc.getGp().lat);
		} catch (Exception ex) {
			result = "n/a";
		}
		return result;
	}

	public static String getLongFromRMC(String sentence) {
		String result = "";
		try {
			RMC rmc = parseRMC(sentence);
			result = Double.toString(rmc.getGp().lng);
		} catch (Exception ex) {
			result = "n/a";
		}
		return result;
	}

	public static String getCOGFromRMC(String sentence) {
		String result = "";
		try {
			RMC rmc = parseRMC(sentence);
			result = Double.toString(rmc.getCog());
		} catch (Exception ex) {
			result = "n/a";
		}
		return result;
	}

	public static String getSOGFromRMC(String sentence) {
		String result = "";
		try {
			RMC rmc = parseRMC(sentence);
			result = Double.toString(rmc.getSog());
		} catch (Exception ex) {
			result = "n/a";
		}
		return result;
	}

	public final static int MESS_NUM = 0;
	public final static int NB_MESS = 1;

	/**
	 * For GSV, returns the message number, and the total number of messages to expect.
	 *
	 * @param gsvString the string to parse
	 * @return teh expected int array
	 */
	public static int[] getMessNum(String gsvString) {
		int mn = -1;
		int nbm = -1;
		if (validCheckSum(gsvString)) {
			String[] elmt = gsvString.trim().split(",");
			try {
				nbm = Integer.parseInt(elmt[1]);
				mn = Integer.parseInt(elmt[2]);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new int[]{mn, nbm};
	}

	/**
	 * ZDA Time &amp; Date - UTC, day, month, year and local time zone<br>
	 * Structure is
	 * <pre>
	 * $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
	 *        1         2  3  4    5  6
	 * $GPZDA,201530.00,04,07,2002,00,00*60
	 *        |         |  |  |    |  |
	 *        |         |  |  |    |  local zone minutes 0..59
	 *        |         |  |  |    local zone hours -13..13
	 *        |         |  |  year
	 *        |         |  month
	 *        |         day
	 *        HrMinSec(UTC)
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static UTCDate parseZDA(String sentence) {
		final int ZDA_UTC = 1;
		final int ZDA_DAY = 2;
		final int ZDA_MONTH = 3;
		final int ZDA_YEAR = 4;
		final int ZDA_LOCAL_ZONE_HOURS = 5;
		final int ZDA_LOCAL_ZONE_MINUTES = 6;

		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");

		Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//		local.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		local.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sa[ZDA_UTC].substring(0, 2)));
		local.set(Calendar.MINUTE, Integer.parseInt(sa[ZDA_UTC].substring(2, 4)));
		local.set(Calendar.SECOND, (int) Math.round(Float.parseFloat(sa[ZDA_UTC].substring(4))));
		local.set(Calendar.MILLISECOND, 0); // TODO Something nicer
		int d = 1;
		try {
			d = Integer.parseInt(sa[ZDA_DAY]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		int mo = 0;
		try {
			mo = Integer.parseInt(sa[ZDA_MONTH]) - 1;
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		int y = 0;
		try {
			y = Integer.parseInt(sa[ZDA_YEAR]);
		} catch (Exception ex) {
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				ex.printStackTrace();
			}
		}
		local.set(Calendar.DATE, d);
		local.set(Calendar.MONTH, mo);
		local.set(Calendar.YEAR, y);

		Date utc = local.getTime();
		return new UTCDate(utc);
	}

	public static final short DEPTH_IN_FEET = 0;
	public static final short DEPTH_IN_METERS = 1;
	public static final short DEPTH_IN_FATHOMS = 2;

	public static String parseDBTinMetersToString(String sentence) {
		String s = sentence.trim();
		String sr = "";
		try {
			float f = parseDBT(s, DEPTH_IN_METERS);
			sr = Float.toString(f);
		} catch (Exception ex) {
			sr = "n/a";
		}
		return sr;
	}

	private final static double METERS_TO_FEET = 3.28083;

	// DBT Depth of Water
	public static float parseDPT(String sentence) {
		return parseDPT(sentence, DEPTH_IN_METERS);
	}
	/**
	 * Water Depth<br>
	 * Structure is
	 * <pre>
	 *  $xxDPT,XX.XX,XX.XX,XX.XX*hh&lt;0D&gt;&lt;OA&gt;
	 *         |     |     |
	 *         |     |     Max depth in meters
	 *         |     offset
	 *         Depth in meters
	 * </pre>
	 * @param sentence the one to parse
	 * @param unit DEPTH_IN_FEET, DEPTH_IN_METERS, DEPTH_IN_FATHOMS
	 * @return The result, as a float.
	 */
	public static float parseDPT(String sentence, short unit) {
		final int IN_METERS = 1;
		final int OFFSET = 2;
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return -1F;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		float feet = 0.0F;
		float meters = 0.0F;
		float fathoms = 0.0F;
		String[] array = sentence.substring(0, sentence.indexOf("*")).split(",");
		try {
			meters = parseNMEAFloat(array[IN_METERS]);
			try {
				String strOffset = array[OFFSET].trim();
				if (strOffset.startsWith("+")) {
					strOffset = strOffset.substring(1);
				}
				float offset = parseNMEAFloat(strOffset);
				meters += offset;
			} catch (Exception ex) {
				if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
					ex.printStackTrace();
				}
			}
			feet = meters * (float) METERS_TO_FEET;
			fathoms = feet / 6F;
		} catch (Exception e) {
			System.err.println("parseDPT For " + s + ", " + e.toString());
			//  e.printStackTrace();
		}

		if (unit == DEPTH_IN_FEET) {
			return feet;
		} else if (unit == DEPTH_IN_METERS) {
			return meters;
		} else if (unit == DEPTH_IN_FATHOMS) {
			return fathoms;
		} else {
			return meters;
		}
	}

	private static float parseDepth(String sentence, short unit, String sentenceID) {
		String s = sentence.trim();
		if (s.length() < MIN_NMEA_LENGTH) {
			return -1F;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		/* Structure is
		 *  $aaDBx,011.0,f,03.3,M,01.8,F*18(CR)(LF)
		 *         |     | |    | |    |
		 *         |     | |    | |    F for fathoms
		 *         |     | |    | Depth in fathoms
		 *         |     | |    M for meters
		 *         |     | Depth in meters
		 *         |     f for feet
		 *         Depth in feet
		 */
		float feet = 0.0F;
		float meters = 0.0F;
		float fathoms = 0.0F;
		String str = "";
		String first = "", last = "";
		try {
			first = String.format("%s,", sentenceID); // Can also be "DBT,", or "DBS,"
			last = ",f,";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			feet = parseNMEAFloat(str);
			first = ",f,";
			last = ",M,";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			meters = parseNMEAFloat(str);
			first = ",M,";
			last = ",F";
			if (s.contains(first) && s.contains(last)) {
				if (s.indexOf(first) < s.indexOf(last)) {
					str = s.substring(s.indexOf(first) + first.length(), s.indexOf(last));
				}
			}
			fathoms = parseNMEAFloat(str);
		} catch (Exception e) {
			System.err.println("parseDepth For " + s + ", " + e.toString());
			// e.printStackTrace();
		}

		if (unit == DEPTH_IN_FEET) {
			return feet;
		} else if (unit == DEPTH_IN_METERS) {
			return meters;
		} else if (unit == DEPTH_IN_FATHOMS) {
			return fathoms;
		} else {
			return feet;
		}
	}
	// Depth Below Transducer
	public static float parseDBT(String sentence) {
		return parseDBT(sentence, DEPTH_IN_METERS);
	}
	// Depth Below Transducer
	public static float parseDBT(String sentence, short unit) {
		return parseDepth(sentence, unit, "DBT");
	}

	/**
	 * Depth Below Surface (Obsolete)<br>
	 * Structure is
	 * <pre>
	 *  $aaDBS,011.0,f,03.3,M,01.8,F*18(CR)(LF)
	 *         |     | |    | |    |
	 *         |     | |    | |    F for fathoms
	 *         |     | |    | Depth in fathoms
	 *         |     | |    M for meters
	 *         |     | Depth in meters
	 *         |     f for feet
	 *         Depth in feet
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result, as a float.
	 */
	public static float parseDBS(String sentence) {
		return parseDBS(sentence, DEPTH_IN_METERS);
	}
	// Depth Below Surface
	public static float parseDBS(String sentence, short unit) {
		return parseDepth(sentence, unit, "DBS");
	}
	/**
	 * WiP<br>
	 * Structure:
	 * <pre>
	 *
	 *        1  2  3  4
	 * $GPTXT,01,01,02,ROM CORE 1.00 (59842) Jun 27 2012 17:43:52*59
	 *        |  |  |  |                                          |
	 *        |  |  |  |                                          Checksum
	 *        |  |  |  Content
	 *        |  |  ?
	 *        |  ?
	 *        ?
	 *
	 * Examples:
	 * $AITXT,01,01,91,FREQ,2087,2088*57
	 * $GPTXT,01,01,02,u-blox ag - www.u-blox.com*50
	 * $GPTXT,01,01,02,HW  UBX-G70xx   00070000 FF7FFFFFo*69
	 * $GPTXT,01,01,02,PROTVER 14.00*1E
	 * $GPTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*20
	 * $GPTXT,01,01,02,ANTSTATUS=OK*3B
	 * $GPTXT,01,01,02,LLC FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFD*2C
	 * </pre>
	 * Pending questions: what are 01,01,02 ?
	 *
	 * @param sentence the one to parse
	 * @return The result, as a String.
	 */
	public static String parseTXT(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String s = sentence.trim();
		if (s.length() < 6 || !s.contains("*")) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			return null;
		}
		String[] elmts = sentence.substring(0, sentence.indexOf("*")).split(",");

		return elmts.length > 4 ? elmts[4] : null;
	}

	public final static class SSD {
		String callSign = "", name = "", talker = "";
		int fromBow = 0, fromStern = 0, fromPortBeam = 0, fromStarboardBeam = 0;
		int DTEFlag = 0;

		@Override
		public String toString() {
			return String.format("CallSign:%s, Name:%s, FromBow:%d, FromStern:%d, FromPort:%d, FromStarboard:%d, DTEFlag:%d, Talker:%s", callSign, name, fromBow, fromStern, fromPortBeam, fromStarboardBeam, DTEFlag, talker);
		}
	}
	/**
	 * Ship Static Data. Up to 8 fields.
	 * <pre>
	 $--SSD,c--c,c--c,xxx,xxx,xx,xx,c,aa*hh&lt;CR&gt;&lt;LF&gt;
	        |    |    |   |   |  |  | |
	        |    |    |   |   |  |  | Source Identifier 5
	        |    |    |   |   |  |  DTE indicator flag 4
	        |    |    |   |   |  Pos. ref. point distance, "D," from starboard beam 3, 0 to 63 Meters
	        |    |    |   |   Pos. ref. point distance, "C," from port beam 3, 0 to 63 Meters
	        |    |    |   Pos. ref. point distance, "B," from stern 3, 0 to 511 Meters
	        |    |    Pos. ref. point distance, "A," from bow 3, 0 to 511 meters
	        |    Ship's Name 2, 1 to 20 characters
	        Ship's Call Sign 1, 1 to 7 characters

	 Example: $AISSD,PD2366@,MERRIMAC@@@@@@@@@@@@,017,000,03,02,1,AI*29
	 </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static SSD parseSSD(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		String callSign = "", name = "", talker = "";
		int fromBow = 0, fromStern = 0, fromPortBeam = 0, fromStarboardBeam = 0;
		int DTEFlag = 0;
		if (sa.length > 1 && sa[1].length() > 0) {
			callSign = sa[1].replaceAll("@", " ").trim();
		}
		if (sa.length > 2 && sa[2].length() > 0) {
			name = sa[2].replaceAll("@", " ").trim();
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			fromBow = Integer.parseInt(sa[3]);
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			fromStern = Integer.parseInt(sa[4]);
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			fromPortBeam = Integer.parseInt(sa[5]);
		}
		if (sa.length > 6 && sa[6].length() > 0) {
			fromStarboardBeam = Integer.parseInt(sa[6]);
		}
		if (sa.length > 7 && sa[7].length() > 0) {
			DTEFlag = Integer.parseInt(sa[7]);
		}
		if (sa.length > 8 && sa[8].length() > 0) {
			talker = sa[8];
		}
		SSD ssd = new SSD();
		ssd.callSign = callSign;
		ssd.name = name;
		ssd.fromBow = fromBow;
		ssd.fromStern = fromStern;
		ssd.fromPortBeam = fromPortBeam;
		ssd.fromStarboardBeam = fromStarboardBeam;
		ssd.DTEFlag = DTEFlag;
		ssd.talker = talker;
		return ssd;
	}

	public final static class VSD {
		int category = 0;
		double draft = 0d;
		int personsOnBoard = 0;
		String destination = "";
		UTCDate estUTCArrival;
		int navStatus = 0;
		int regAppFlag = 0;

		private final static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss 'UTC'");
		static {
			sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}

		@Override
		public String toString() {
			return String.format("Cat: %d, Draft: %.02fm, Pass:%d, Going to: %s, est-UTC-Arr:%s, Status:%d, Flag:%d",
					category, draft, personsOnBoard,
					(destination.isEmpty() ? "n/a" : destination),
					(estUTCArrival != null ? estUTCArrival.toString(sdf) : "n/a"),
					navStatus, regAppFlag);
		}
	}
	/**
	 *
	 * Voyage Static Data<br>
	 * <pre>
	 $--VSD,x.x,x.x,x.x,c--c,hhmmss.ss,xx,xx,x.x,x.x*hh&lt;CR&gt;&lt;LF&gt;
	        |   |   |   |    |         |  |  |   |
	        |   |   |   |    |         |  |  |   Regional application flags 8, 0 to 15
	        |   |   |   |    |         |  |  Navigational status 7, 0 to 15
	        |   |   |   |    |         |  Estimated month of arrival at destination 6, 00 to 12 (UTC)
	        |   |   |   |    |         Estimated day of arrival at destination 6, 00 to 31 (UTC)
	        |   |   |   |    Estimated UTC of arrival at destination 5
	        |   |   |   Destination 4, 1-20 characters
	        |   |   Persons on-board 3, 0 to 8191
	        |   Maximum present static draught 2, 0 to 25.5 Meters
	        Type of ship and cargo category 1, 0 to 255

	 Example: $AIVSD,036,00.0,0000,@@@@@@@@@@@@@@@@@@@@,000000,00,00,00,00*4E
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
	 */
	public static VSD parseVSD(String sentence) {

		if (sentence.length() < MIN_NMEA_LENGTH || !sentence.contains("*")) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}

		if (sentence.contains("VSD,")) {

			String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
			VSD vsd = new VSD();

			if (sa.length > 1 && sa[1].length() > 0) {
				vsd.category = Integer.parseInt(sa[1]);
			}
			if (sa.length > 2 && sa[2].length() > 0) {
				vsd.draft = Double.parseDouble(sa[2]);
			}
			if (sa.length > 3 && sa[3].length() > 0) {
				vsd.personsOnBoard = Integer.parseInt(sa[3]);
			}
			if (sa.length > 4 && sa[4].length() > 0) {
				vsd.destination = sa[4].replaceAll("@", " ").trim();
			}
			// UTC Arrival
			if (sa.length > 7 && sa[5].length() > 0 && sa[6].length() > 0 && sa[7].length() > 0) {
				if (Integer.parseInt(sa[5]) == 0 && "00".equals(sa[6]) && "00".equals(sa[7])) {
					vsd.estUTCArrival = null;
				} else {
					int utcArrivalHours = Integer.parseInt(sa[5].substring(0, 2));
					int utcArrivalMins = Integer.parseInt(sa[5].substring(2, 4));
					float utcArrivalSecs = Float.parseFloat(sa[5].substring(4));
					int estDayArrival = Integer.parseInt(sa[6]);
					int estMonthArrival = Integer.parseInt(sa[7]);
					int d = 1;
					try {
						d = estDayArrival;
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					int mo = 1;
					try {
						mo = estMonthArrival - 1;
					} catch (Exception ex) {
						if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
							ex.printStackTrace();
						}
					}
					UTCDate estUTCDate = new UTCDate(null, mo, d, utcArrivalHours, utcArrivalMins, (int)Math.round(utcArrivalSecs), null);
					vsd.estUTCArrival = estUTCDate;

//					Calendar local = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // new GregorianCalendar();
//					local.set(Calendar.HOUR_OF_DAY, utcArrivalHours);
//					local.set(Calendar.MINUTE, utcArrivalMins);
//					local.set(Calendar.SECOND, (int) Math.round(utcArrivalSecs));
//					local.set(Calendar.MILLISECOND, 0); // TODO Something nicer?
//					local.set(Calendar.DATE, d);
//					local.set(Calendar.MONTH, mo);
//
//					Date utc = local.getTime();
//					vsd.estUTCArrival = new UTCDate(utc);
				}
			}

			if (sa.length > 8 && sa[8].length() > 0) {
				vsd.navStatus = Integer.parseInt(sa[8]);
			}
			if (sa.length > 9 && sa[9].length() > 0) {
				vsd.regAppFlag = Integer.parseInt(sa[9]);
			}
			return vsd;
		} else {
			return null;
		}
	}

	public final static class XTE {
		String statusOne = "", statusTwo = "";
		double xteMag = 0d;
		String dirToSteer = "", unit = "", mode = "";

		public String getStatusOne() {
			return statusOne;
		}

		public String getStatusTwo() {
			return statusTwo;
		}

		public double getXteMag() {
			return xteMag;
		}

		public String getDirToSteer() {
			return dirToSteer;
		}

		public String getUnit() {
			return unit;
		}

		public String getMode() {
			return mode;
		}

		private static String decodeStatusOne(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Data Valid";
					break;
				case "V":
					meaning = "Loran-C Blink or SNR warning";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private static String decodeStatusTwo(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Data Valid";
					break;
				case "V":
					meaning = "Loran-C Cycle Lock warning flag";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private static String decodeMode(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Autonomous mode";
					break;
				case "D":
					meaning = "Differential mode";
					break;
				case "E":
					meaning = "Estimated (dead reckoning) mode";
					break;
				case "M":
					meaning = "Manual input mode";
					break;
				case "S":
					meaning = "Simulator mode";
					break;
				case "N":
					meaning = "Data not valid";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		@Override
		public String toString() {
			return String.format("Status-1: %s, Status-2:%s, XTE Mag: %.02f, Steer:%s, unit:%s, mode: %s",
								 decodeStatusOne(statusOne), decodeStatusTwo(statusTwo),
								 xteMag, dirToSteer, unit,
								 decodeMode(mode));
		}
	}
	/**
    Cross Track Error
	<pre>
    $--XTE,A,A,x.x,a,N,a*hh&lt;CR&gt;&lt;LF&gt;
           | | |   | | |
           | | |   | | Mode Indicator
           | | |   | | - A = Autonomous mode
           | | |   | | - D = Differential mode
           | | |   | | - E = Estimated (dead reckoning) mode
           | | |   | | - M = Manual input mode
           | | |   | | - S = Simulator mode
           | | |   | Units, nautical miles
           | | |   Direction to steer, L/R
           | | Magnitude of Cross-Track-Error
           | Status
           | - A = Data valid
           | - V = Loran-C Cycle Lock warning flag
           Status
             - A = Data valid
             - V = Loran-C Blink or SNR warning
             - V = general warning flag for other navigation systems when a reliable fix is not available

         Example: $GPXTE,,,,,N,N*5E, $GPXTE,V,V,,,N,S*43
     </pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static XTE parseXTE(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		String statusOne = "", statusTwo = "";
		double xteMag = 0d;
		String dirToSteer = "", unit = "", mode = "";
		if (sa.length > 1 && sa[1].length() > 0) {
			statusOne = sa[1];
		}
		if (sa.length > 2 && sa[2].length() > 0) {
			statusTwo = sa[2];
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			xteMag = Double.parseDouble(sa[3]);
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			dirToSteer = sa[4];
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			unit = sa[5];
		}
		if (sa.length > 6 && sa[6].length() > 0) {
			mode = sa[6];
		}
		XTE xte = new XTE();
		xte.statusOne = statusOne;
		xte.statusTwo = statusTwo;
		xte.xteMag = xteMag;
		xte.dirToSteer = dirToSteer;
		xte.unit = unit;
		xte.mode = mode;

		return xte;
	}

	public static class AAM {
		String statusOne = "", statusTwo = "";
		double arrivalRadius = 0d;
		String unit = "";
		String waypointId = "";

		private String decodeStatusOne(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "arrival circle entered";
					break;
				case "V":
					meaning = "arrival circle not entered";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private String decodeStatusTwo(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "perpendicular passed at waypoint";
					break;
				case "V":
					meaning = "perpendicular not passed";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		@Override
		public String toString() {
			return String.format("Status-1:%s, Status-2:%s, Radius:%.02f, Unit:%s, WP:%s",
					decodeStatusOne(statusOne), decodeStatusTwo(statusTwo),
					arrivalRadius, unit, (waypointId.trim().length() > 0 ? waypointId.trim() : "n/a"));
		}
	}
	/**
     * Waypoint Arrival Alarm
     * <pre>
    $--AAM,A,A,x.x,N,c--c*hh&lt;CR&gt;&lt;LF&gt;
           | | |   | |
           | | |   | Waypoint ID
           | | |   Units of radius, nautical miles
           | | Arrival circle radius
           | Status: A = perpendicular passed at waypoint
           |         V = perpendicular not passed
           Status: A = arrival circle entered
                   V = arrival circle not entered

Example: $GPAAM,V,V,0.05,N,*23
	 * </pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static AAM parseAAM(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		AAM aam = new AAM();
		if (sa.length > 1 && sa[1].length() > 0) {
			aam.statusOne = sa[1];
		}
		if (sa.length > 2 && sa[2].length() > 0) {
			aam.statusTwo = sa[2];
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			aam.arrivalRadius = Double.parseDouble(sa[3]);
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			aam.unit = sa[4];
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			aam.waypointId = sa[5];
		}
		return aam;
	}

	public final static class BOD {
		double trueBearing = 0d, magBearing = 0d;
		String fromWP = "", toWP = "";

		@Override
		public String toString() {
			return String.format("TrueBearing: %.02f, MagBearing: %.02f, From: %s, to: %s",
					trueBearing, magBearing, fromWP, toWP);
		}
	}
	/**
    Bearing - Origin to Destination
	<pre>
    $--BOD,x.x,T,x.x,M,c--c,c--c*hh&lt;CR&gt;&lt;LF&gt;
           |     |     |    |
           |     |     |    Origin waypoint ID (6)
           |     |     Destination waypoint ID (5)
           |     Bearing, degrees Magnetic (3)
           Bearing, degrees True (1)

      Example: $GPBOD,213.9,T,213.2,M,,*4C
     * </pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static BOD parseBOD(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
		BOD bod = new BOD();
		if (sa.length > 1 && sa[1].length() > 0) {
			bod.trueBearing = Double.parseDouble(sa[1]);
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			bod.magBearing = Double.parseDouble(sa[3]);
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			bod.toWP = sa[5];
		}
		if (sa.length > 6 && sa[6].length() > 0) {
			bod.fromWP = sa[6];
		}
		return bod;
	}

	public static class BWx {
		UTCDate utcDate;
		GeoPos wpPosition;
		double trueBearing = 0d, magBearing = 0d, distance = 0d;
		String wpId = "", mode = "";

		public GeoPos getWpPosition() {
			return wpPosition;
		}

		public void setWpPosition(GeoPos wpPosition) {
			this.wpPosition = wpPosition;
		}

		private static String decodeMode(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Autonomous mode";
					break;
				case "D":
					meaning = "Differential mode";
					break;
				case "E":
					meaning = "Estimated (dead reckoning) mode";
					break;
				case "M":
					meaning = "Manual input mode";
					break;
				case "S":
					meaning = "Simulator mode";
					break;
				case "N":
					meaning = "Data not valid";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}

		private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss 'UTC'");
		static {
			sdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}

		@Override
		public String toString() {
			return String.format("UTC Obs:%s, from WP:%s, Bearing: True: %.01f\272, Mag: %.01f\272, Distance: %.02f nm, WP: %s, Mode: %s",
					utcDate.toString(sdf),
					wpPosition != null ? wpPosition.toString() : " -'",
					trueBearing, magBearing, distance,
					(wpId.isEmpty() ? "n/a" : wpId), decodeMode(mode));
		}
	}
	/**
    BWC - Bearing &amp; Distance to Waypoint<br>
    BWR - Bearing &amp; Distance to Waypoint - Rhumb Line
    <pre>
$--BWC,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x.x,T,x.x,M,x.x,N,c--c,a*hh&lt;CR&gt;&lt;LF&gt;
$--BWR,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x.x,T,x.x,M,x.x,N,c--c,a*hh&lt;CR&gt;&lt;LF&gt;
    |         |       | |        | |   | |   | |   | |    |
    |         |       | |        | |   . |   . |   . |    Mode Indicator: (13)
    |         |       | |        | |   . |   . |   . |    - A = Autonomous mode
    |         |       | |        | |   . |   . |   . |    - D = Differential mode
    |         |       | |        | |     |     |     |    - E = Estimated (dead reckoning) mode
    |         |       | |        | |     |     |     |    - M = Manual input mode
    |         |       | |        | |     |     |     |    - S = Simulator mode
    |         |       | |        | |     |     |     |    - N = Data not valid
    |         |       | |        | |     |     |     Waypoint ID (12)
    |         |       | |        | |     |     Distance, nautical miles (10)
    |         |       | |        | |     Bearing, degrees Magnetic (8)
    |         |       | |        | Bearing, degrees True (6)
    |         |       | |        E/W (5)
    |         |       | Waypoint longitude (4)
    |         |       N/S (3)
    |         Waypoint latitude (2)
    UTC of observation (1)

    Examples: $GPBWC,195938,5307.2833,N,00521.7536,E,213.9,T,213.2,M,4.25,N,,A*53
              $GPBWR,195938,5307.2833,N,00521.7536,E,213.9,T,213.2,M,4.25,N,,A*42
    </pre>
	  * @param sentence the one to parse
	  * @return The result.
      */
	public static BWx parseBWx(String sentence) {
		if (sentence.length() < 6 || !sentence.contains("*")) {
			return null;
		}
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}

		if (sentence.contains("BWC,") || sentence.contains("BWR,")) {
			String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");
			BWx bwx = new BWx();
			if (sa.length > 1 && sa[1].length() > 0) {
				int hours = Integer.parseInt(sa[1].substring(0, 2));
				int minutes = Integer.parseInt(sa[1].substring(2, 4));
				float seconds = Float.parseFloat(sa[1].substring(4));
				bwx.utcDate = new UTCDate(null, null, null, hours, minutes, (int)Math.round(seconds), null);
			}
			if (sa.length > 2 && sa[2].length() > 0 && sa.length > 3 && sa[3].length() > 0 &&
					sa.length > 4 && sa[4].length() > 0 && sa.length > 5 && sa[5].length() > 0) {
				String deg = sa[2].substring(0, 2);
				String min = sa[2].substring(2);
				double lat = GeomUtil.sexToDec(deg, min);
				if ("S".equals(sa[3])) {
					lat = -lat;
				}
				deg = sa[4].substring(0, 3);
				min = sa[4].substring(3);
				double lng = GeomUtil.sexToDec(deg, min);
				if ("W".equals(sa[5])) {
					lng = -lng;
				}
				bwx.setWpPosition(new GeoPos(lat, lng));
			}
			if (sa.length > 6 && sa[6].length() > 0) {
				bwx.trueBearing = Double.parseDouble(sa[6]);
			}
			if (sa.length > 8 && sa[8].length() > 0) {
				bwx.magBearing = Double.parseDouble(sa[8]);
			}
			if (sa.length > 10 && sa[10].length() > 0) {
				bwx.distance = Double.parseDouble(sa[10]);
			}
			if (sa.length > 12 && sa[12].length() > 0) {
				bwx.wpId = sa[12];
			}
			if (sa.length > 13 && sa[13].length() > 0) {
				bwx.mode = sa[13];
			}
			return bwx;
		} else {
			return null;
		}
	}

	public final static class APB {
		String statusOne = "", statusTwo = "";
		double xteMag = 0d;
		String steer = "", xteUnit = "";
		String arrivalStatus = "", startStatus = "";
		double bearingOriginToDestination = 0d;
		String bodTM = "";
		double bearingPresentToDestination = 0d;
		String bpdTM = "", destWP = "";
		double hdgToSteerToDestination = 0d;
		String htsTM = "";
		String mode = "";

		private static String decodeStatusOne(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Data valid";
					break;
				case "V":
					meaning = "Loran-C Blink or SNR warning";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private static String decodeStatusTwo(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Data Valid or not used";
					break;
				case "V":
					meaning = "Loran-C Cycle Lock warning flag";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private static String decodeMode(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Autonomous mode";
					break;
				case "D":
					meaning = "Differential mode";
					break;
				case "E":
					meaning = "Estimated (dead reckoning) mode";
					break;
				case "M":
					meaning = "Manual input mode";
					break;
				case "S":
					meaning = "Simulator mode";
					break;
				case "N":
					meaning = "Data not valid";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private String decodeArrivalStatus(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "arrival circle entered";
					break;
				case "V":
					meaning = "arrival circle not entered";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}
		private String decodeStartStatus(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "perpendicular passed at waypoint";
					break;
				case "V":
					meaning = "perpendicular not passed";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}

		@Override
		public String toString() {
			return String.format("Status-1:%s, Status-2:%s, XTE: %.02f %s, Steer:%s, WP: %s, Start Status: %s, Arrival Status: %s, Origin-to-Dest: %.01f %s, Present-to-Dest: %.01f %s, Steer-t-Dest: %.01f %s, mode: %s",
					decodeStatusOne(statusOne), decodeStatusTwo(statusTwo),
					xteMag, xteUnit, steer,
					(destWP.trim().length() > 0 ? destWP.trim() : "n/a"),
					decodeStartStatus(startStatus), decodeArrivalStatus(arrivalStatus),
					bearingOriginToDestination, bodTM,
					bearingPresentToDestination, bpdTM,
					hdgToSteerToDestination, htsTM,
					decodeMode(mode));
		}
	}
	/**
    Heading/Track Controller (Autopilot) Sentence "B"
	<pre>
     $--APB,A,A,x.x,a,N,A,A,x.x,a,c--c,x.x,a,x.x,a,a*hh&lt;CR&gt;&lt;LF&gt;
            | | |   | | | | |   | |    |   | |   | |
            | | |   | | | | |   | |    |   | |   | Mode indicator (15)
            | | |   | | | | |   | |    |   | |   |  - A = Autonomous mode
            | | |   | | | | |   | |    |   | |   |  - D = Differential mode
            | | |   | | | | |   | |    |   | |   |  - E = Estimated (dead reckoning) mode
            | | |   | | | | |   | |    |   | |   |  - M = Manual input mode
            | | |   | | | | |   | |    |   | |   |  - S = Simulator mode
            | | |   | | | | |   | |    |   | |   |  - N = Data not valid
            | | |   | | | | |   | |    |   | |   True or Mag (14
            | | |   | | | | |   | |    |   | Heading-to-steer to destination waypoint (13)
            | | |   | | | | |   | |    |   True or Mag (12)
            | | |   | | | | |   | |    Bearing, Present position to destination (11)
            | | |   | | | | |   | Destination waypoint ID (10)
            | | |   | | | | |   True or Mag (9)
            | | |   | | | | Bearing origin to destination (8)
            | | |   | | | Status: A = perpendicular passed at waypoint (7)
            | | |   | | Status: A = arrival circle entered (6)
            | | |   | XTE units, nautical miles (5)
            | | |   Direction to steer, L/R (4)
            | | Magnitude of XTE (cross-track-error) (3)
            | Status (2)
            | - A = Data valid or not used,
            | - V = Loran-C Cycle Lock warning flag
            Status (1)
              - A = Data valid
              - V = Loran-C Blink or SNR warning
              - V = General warning flag for other navigation systems when a reliable fix is not available

    Example: $GPAPB,A,A,0.001,L,N,V,V,213.9,T,,213.9,T,213.9,T,A*77
     * </pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static APB parseAPB(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");

		APB apb = new APB();
		if (sa.length > 1 && sa[1].length() > 0) {
			apb.statusOne = sa[1];
		}
		if (sa.length > 2 && sa[2].length() > 0) {
			apb.statusTwo = sa[2];
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			apb.xteMag = Double.parseDouble(sa[3]);
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			apb.steer = sa[4];
		}
		if (sa.length > 5 && sa[5].length() > 0) {
			apb.xteUnit = sa[5];
		}
		if (sa.length > 6 && sa[6].length() > 0) {
			apb.arrivalStatus = sa[6];
		}
		if (sa.length > 7 && sa[7].length() > 0) {
			apb.startStatus = sa[7];
		}
		if (sa.length > 8 && sa[8].length() > 0) {
			apb.bearingOriginToDestination = Double.parseDouble(sa[8]);
		}
		if (sa.length > 9 && sa[9].length() > 0) {
			apb.bodTM = sa[9];
		}
		if (sa.length > 10 && sa[10].length() > 0) {
			apb.destWP = sa[10];
		}
		if (sa.length > 11 && sa[11].length() > 0) {
			apb.bearingPresentToDestination = Double.parseDouble(sa[11]);
		}
		if (sa.length > 12 && sa[12].length() > 0) {
			apb.bpdTM = sa[12];
		}
		if (sa.length > 13 && sa[13].length() > 0) {
			apb.hdgToSteerToDestination = Double.parseDouble(sa[13]);
		}
		if (sa.length > 14 && sa[14].length() > 0) {
			apb.htsTM = sa[14];
		}
		if (sa.length > 15 && sa[15].length() > 0) {
			apb.mode = sa[15];
		}
		return apb;
	}

	public static class VPW {
		// A VMG
		double speedInKnots = 0d;
		double speedInMS = 0d;

		@Override
		public String toString() {
			return String.format("VMG (VPW): %skt %s, %sm/s %s",
					(speedInKnots != 0 ? String.format("%.02f", speedInKnots) : "n/a "),
					(speedInKnots < 0 ? "downwind" : (speedInKnots != 0 ? "upwind" : "")),
					(speedInMS != 0 ? String.format("%.02f", speedInMS) : "n/a "),
					(speedInMS < 0 ? "downwind" : (speedInMS != 0 ? "upwind" : "")));
		}
	}
	/**
VPW - Speed - Measured Parallel to Wind<br>
The component of the vessel's velocity vector parallel to the direction of the true wind direction.<br>
Sometimes called "speed made good to windward" or "velocity made good to windward".
<pre>
$--VPW,x.x,N,x.x,M*hh&lt;CR&gt;&lt;LF&gt;
   |   | |   |
   |   | |   m/s
   |   | Speed, "-" = downwind
   |   Knots
   Speed, "-" = downwind
</pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static VPW parseVPW(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");

		VPW vpw = new VPW();
		if (sa.length > 1 && sa[1].length() > 0) {
			vpw.speedInKnots = Double.parseDouble(sa[1]);
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			vpw.speedInMS = Double.parseDouble(sa[3]);
		}
		return vpw;
	}

	public static class WCV {
		double velocity = 0;
		String wayPointID = "";
		String mode = "";

		private static String decodeMode(String value) {
			String meaning;
			switch (value) {
				case "A":
					meaning = "Autonomous mode";
					break;
				case "D":
					meaning = "Differential mode";
					break;
				case "E":
					meaning = "Estimated (dead reckoning) mode";
					break;
				case "M":
					meaning = "Manual input mode";
					break;
				case "S":
					meaning = "Simulator mode";
					break;
				case "N":
					meaning = "Data not valid";
					break;
				default:
					meaning = String.format("Unknown[%s]", value);
					break;
			}
			return meaning;
		}

		@Override
		public String toString() {
			return String.format("%.02f kn to %s. Mode: %s",
					velocity,
					(wayPointID.trim().length() > 0 ? wayPointID.trim() : "n/a"),
					decodeMode(mode));
		}

	}
	/**
    WCV - Waypoint Closure Velocity<br>
    The component of the velocity vector in the direction of the waypoint, from present position.<br>
    Sometimes called "speed made good" or "velocity made good".
    <pre>
    $--WCV,x.x,N,c--c,a*hh&lt;CR&gt;&lt;LF&gt;
           |   | |    |
           |   | |    Mode Indicator
           |   | Waypoint identifier
           |   knots
           Velocity component
    Notes:
    Mode Indicator:
        A = Autonomous mode
        D = Differential mode
        E = Estimated (dead reckoning) mode
        M = Manual input mode
        S = Simulator mode
        N = Data not valid

        The positioning system Mode Indicator field shall not be a null field.
     </pre>
	 * @param sentence the one to parse
	 * @return The result.
     */
	public static WCV parseWCV(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid checksum for [%s]", sentence));
		}
		String[] sa = sentence.substring(0, sentence.indexOf("*")).split(",");

		WCV wcv = new WCV();
		if (sa.length > 1 && sa[1].length() > 0) {
			wcv.velocity = Double.parseDouble(sa[1]);
		}
		if (sa.length > 3 && sa[3].length() > 0) {
			wcv.wayPointID = sa[3];
		}
		if (sa.length > 4 && sa[4].length() > 0) {
			wcv.mode = sa[4];
		}
		return wcv;

	}

	/*
	 * UTILITIES
	 * End of String Parsers
	 * Parsing and various Utilities start here
	 */

	public static boolean validCheckSum(String sentence) {
		return validCheckSum(sentence, false);
	}

	public static boolean validCheckSum(String data, boolean verb) {
		String sentence = data.trim();
		boolean b = false;
		try {
			int starIndex = sentence.indexOf("*");
			if (starIndex < 0) {
				return false;
			}
			String csKey = sentence.substring(starIndex + 1);
			int csk = Integer.parseInt(csKey, 16);
			// System.out.println("Checksum  : 0x" + csKey + " (" + csk + ")");
			String str2validate = sentence.substring(1, sentence.indexOf("*"));
			// System.out.println("To validate:[" + str2validate + "]");
			// char[] ca = str2validate.toCharArray();
			// int calcCheckSum = ca[0];
			// for (int i=1; i<ca.length; i++)
			//   calcCheckSum = calcCheckSum ^ ca[i]; // XOR

			int calcCheckSum = calculateCheckSum(str2validate);
			b = (calcCheckSum == csk);
			// System.out.println("Calculated: 0x" + lpad(Integer.toString(calcCheckSum, 16).toUpperCase(), 2, "0"));
		} catch (Exception ex) {
			if (verb) {
				System.err.println("Oops:" + ex.getMessage());
			}
		}
		return b;
	}

	/**
	 * Calculate the checksum. An XOR of all the characters, between the '$' and the '*' (excluded).<br>
	 * In <code>"$GPZDA,201530.00,04,07,2002,00,00*60"</code>, the checksum is calculated with <code>"GPZDA,201530.00,04,07,2002,00,00"</code>
	 *
	 * @param str The (full) NMEA string, from '$' to '*XX'
	 * @return The checksum, as an int.
	 */
	public static int calculateCheckSum(String str) {
		if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
			System.out.printf("Calculating checksum for %s\n", str);
		}
		int cs = 0;
		char[] ca = str.toCharArray();
		cs = ca[0];
		for (int i = 1; i < ca.length; i++) {
			cs = cs ^ ca[i]; // XOR
			if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
				System.out.printf("Checksum is now 0x%02X \n", cs);
			}
		}
		if ("true".equals(System.getProperty("nmea.parser.verbose"))) {
			System.out.printf("Final Checksum %02X \n", cs);
		}
		return cs;
	}

	/**
	 * @param sentence a VALID NMEA Sentence
	 * @return the device ID
	 */
	public static String getDeviceID(String sentence) {
		String id = "";
		if (sentence == null || sentence.length() < 7) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence", sentence));
		}
		id = sentence.substring(1, 3);
		return id;
	}

	/**
	 * @param sentence a VALID NMEA Sentence
	 * @return the device (aka talker) ID
	 */
	public static String getTalkerID(String sentence) {
		return getDeviceID(sentence);
	}
	/**
	 * @param sentence a VALID NMEA Sentence
	 * @return the sentence ID
	 */
	public static String getSentenceID(String sentence) {
		String id = "";
		if (sentence == null || sentence.length() < 7) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence [%s]", sentence));
		}
		id = sentence.substring(3, 6);
		return id;
	}

	/**
	 * Enforce the parsing using the Locale.ENGLISH
	 *
	 * @param str the string to parse
	 * @return the double value
	 * @throws Exception, in case it fails
	 */
	private static double parseNMEADouble(String str) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = nf.parse(str);
		double d = number.doubleValue();
//  System.out.println("Number is " + Double.toString(d));
		return d;
	}

	private static float parseNMEAFloat(String str) throws Exception {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		Number number = nf.parse(str);
		float f = number.floatValue();
		//  System.out.println("Number is " + Double.toString(d));
		return f;
	}

	/**
	 * Parses strings like "2006-05-05T17:35:48.000" + "Z" or UTC Offset like "-10:00"
	 * <pre>
	 * 01234567890123456789012
	 * 1         2         3
	 * </pre>
	 * @param duration Duration Stgring
	 * @return a UTC date
	 */
	public static long durationToDate(String duration) {
		return durationToDate(duration, null);
	}

	/**
	 * <pre>
	 * Sample: "2006-05-05T17:35:48.000Z"
	 *          |    |  |  |  |  |  |
	 *          |    |  |  |  |  |  20
	 *          |    |  |  |  |  17
	 *          |    |  |  |  14
	 *          |    |  |  11
	 *          |    |  8
	 *          |    5
	 *          0
	 * </pre>
	 * @param duration Duration string
	 * @param tz Time Zone
	 * @return an epoch (in milliseconds)
	 * @throws RuntimeException if something's invalid.
	 */
	public static long durationToDate(String duration, String tz)
					throws RuntimeException {
		// A RegEx
		final String regex = // "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(.)$";
				             "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?(.*)$"; // ms, TZ are optional.
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(duration);
		if (!matcher.find()) {
			// TODO Oops, raise ?
			System.out.printf("Oops, no duration match for [%s] %s.\n", duration, tz);
		}

		String yyyy = duration.substring(0, 4);
		String mm = duration.substring(5, 7);
		String dd = duration.substring(8, 10);
		String hh = duration.substring(11, 13);
		String mi = duration.substring(14, 16);
		String ss = duration.substring(17, 19);
		String ms = "0";
		try {
			ms = duration.substring(20, 23);
		} catch (IndexOutOfBoundsException iobe) {
			// Absorb;
		}

		float utcOffset = 0F;

		String trailer = duration.substring(19);
		if (trailer.contains("+") || trailer.contains("-")) {
            // System.out.println(trailer);
			if (trailer.contains("+")) {
				trailer = trailer.substring(trailer.indexOf("+") + 1);
			}
			if (trailer.contains("-")) {
				trailer = trailer.substring(trailer.indexOf("-"));
			}
			if (trailer.contains(":")) {
				String hours = trailer.substring(0, trailer.indexOf(":"));
				String mins = trailer.substring(trailer.indexOf(":") + 1);
				utcOffset = (float) Integer.parseInt(hours) + (float) (Integer.parseInt(mins) / 60f);
			} else {
				utcOffset = Float.parseFloat(trailer);
			}
		}
        // System.out.println("UTC Offset:" + utcOffset);

		Calendar calendar = Calendar.getInstance();
		if (utcOffset == 0f && tz != null) {
			calendar.setTimeZone(TimeZone.getTimeZone(tz));
		} else {
			calendar.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}
		int milliSec = 0;
		try {
			milliSec = Integer.parseInt(ms); // OK for 000,would fail if like '10:', where it is the TZ Offset.
		} catch (NumberFormatException nfe) {
			// Absorb
			milliSec = 0;
		}
		try {
			calendar.set(Integer.parseInt(yyyy),
							Integer.parseInt(mm) - 1,
							Integer.parseInt(dd),
							Integer.parseInt(hh),
							Integer.parseInt(mi),
							Integer.parseInt(ss));
			calendar.set(Calendar.MILLISECOND, milliSec);
			if (false) {
				Date date = calendar.getTime();
				System.out.printf(">> Date: %s\n", SDF_UTC.format(date));
			}
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("durationToDate, for [" + duration + "] : " + nfe.getMessage());
		}
		return calendar.getTimeInMillis() - (long) (utcOffset * (3_600_000));
	}

	public static String durationToExcel(String duration)
					throws RuntimeException {
		String yyyy = duration.substring(0, 4);
		String mm = duration.substring(5, 7);
		String dd = duration.substring(8, 10);
		String hh = duration.substring(11, 13);
		String mi = duration.substring(14, 16);
		String ss = duration.substring(17, 19);
		String result = "";
		try {
			result = yyyy + "/" + mm + "/" + dd + " " + hh + ":" + mi + ":" + ss;
		} catch (Exception ex) {
			throw new RuntimeException("durationToDate, for [" + duration + "] : " + ex.getMessage());
		}
		return result;
	}


	/*
	   AUTO-PARSE
	   Used by autoParse
	   AutoParse Utilities and tools
	 */
	public enum Dispatcher {

		RMC("RMC", "Recommended Minimum Navigation Information, C", StringParsers::parseRMC, RMC.class),
		GLL("GLL", "Geographical Lat & Long", StringParsers::parseGLL, GLL.class),
		DBT("DBT", "Depth Below Transducer", StringParsers::parseDBT, Float.class),
		DBS("DBS", "Depth Below Surface", StringParsers::parseDBS, Float.class),
		DPT("DPT", "Depth of Water", StringParsers::parseDPT, Object.class),
		GGA("GGA", "Global Positioning System Fix Data", StringParsers::parseGGA, List.class),
		GSA("GSA", "GPS DOP and active satellites", StringParsers::parseGSA, GSA.class),
		GBS("GBS", "GPS Satellite Fault Detection", StringParsers::parseGBS, StringParsers.GBS.class),
		GSV("GSV", "Satellites in view", StringParsers::parseGSV, Map.class),
		HDG("HDG", "Heading - Deviation & Variation", StringParsers::parseHDG, HDG.class),
		HDM("HDM", "Heading - Magnetic", StringParsers::parseHDM, Integer.class),
		HDT("HDT", "Heading - True", StringParsers::parseHDT, Integer.class),
		MDA("MDA", "Meteo Composite", StringParsers::parseMDA, MDA.class),
		MMB("MMB", "Atm Pressure", StringParsers::parseMMB, Double.class),
		MTA("MTA", "Air Temperature, Celsius", StringParsers::parseMTA, Double.class),
		MTW("MTW", "Mean Temperature of Water", StringParsers::parseMTW, Double.class),
		MWD("MWD", "Wind Direction & Speed", StringParsers::parseMWD, TrueWind.class),
		MWV("MWV", "Wind Speed and Angle", StringParsers::parseMWV, ApparentWind.class), // Careful, actually returns Wind.class
		RMB("RMB", "Recommended Minimum Navigation Information, B", StringParsers::parseRMB, RMB.class),
		TXT("TXT", "Text Transmission", StringParsers::parseTXT, String.class),
		SSD("SSD", "Ship Static Data", StringParsers::parseSSD, StringParsers.SSD.class),
		VSD("VSD", "Voyage Static Data", StringParsers::parseVSD, StringParsers.VSD.class),
		AAM("AAM", "Waypoint Arrival Alarm", StringParsers::parseAAM, StringParsers.AAM.class),
		BOD("BOD", "Bearing - Origin to Destination", StringParsers::parseBOD, StringParsers.BOD.class),
		BWC("BWC", "Bearing & Distance to Waypoint", StringParsers::parseBWx, StringParsers.BWx.class),
		BWR("BWR", "Bearing & Distance to Waypoint - Rhumb Line", StringParsers::parseBWx, StringParsers.BWx.class),
		APB("APB", "Heading/Track Controller (Autopilot) Sentence \"B\"", StringParsers::parseAPB, StringParsers.APB.class),
		WCV("WCV", "Waypoint Closure Velocity", StringParsers::parseWCV, StringParsers.WCV.class),
		VPW("VPW", "Speed - Measured Parallel to Wind, aka VMG", StringParsers::parseVPW, StringParsers.VPW.class),
		VDR("VDR", "Set and Drift", StringParsers::parseVDR, Current.class),
		VHW("VHW", "Water speed and heading", StringParsers::parseVHW, VHW.class),
		VLW("VLW", "Distance Traveled through Water", StringParsers::parseVLW, VLW.class),
		VTG("VTG", "Track made good and Ground speed", StringParsers::parseVTG, OverGround.class),
		VWR("VWR", "Relative Wind Speed and Angle", StringParsers::parseVWR, ApparentWind.class),
		VWT("VWT", "Wind Data", StringParsers::parseVWT, TrueWind.class),                              // Obsolete
		XDR("XDR", "Transducer Measurement", StringParsers::parseXDR, List.class),
		XTE("XTE", "Cross Track Error", StringParsers::parseXTE, StringParsers.XTE.class),
		ZDA("ZDA", "Time & Date - UTC, day, month, year and local time zone", StringParsers::parseZDA, UTCDate.class);

		private final String key;
		private final String description;
		private final Function<String, Object> parser;
		private final Class returnedType;

		Dispatcher(String key, String description, Function<String, Object> parser, Class returned) {
			this.key = key;
			this.description = description;
			this.parser = parser;
			returnedType = returned;
		}

		public String key() {
			return this.key;
		}
		public String description() {
			return this.description;
		}
		public Function<String, Object> parser() {
			return this.parser;
		}
		public Class returnedType() { return this.returnedType; }
	}

	public static String getSentenceDescription(String id) {
		return Arrays.asList(StringParsers.Dispatcher.values()).stream()
				.filter(disp -> findDispatcherByKey(id) != null)
				.map(disp -> findDispatcherByKey(id).description())
				.findFirst()
				.orElse(null);
	}

	public static class ParsedData {
		private String deviceID;
		private String sentenceId;
		private String fullSentence;
		private Object parsedData;

		public ParsedData deviceID(String deviceID) {
			this.deviceID = deviceID;
			return this;
		}
		public ParsedData sentenceId(String sentenceId) {
			this.sentenceId = sentenceId;
			return this;
		}
		public ParsedData fullSentence(String fullSentence) {
			this.fullSentence = fullSentence;
			return this;
		}
		public ParsedData parsedData(Object parsedData) {
			this.parsedData = parsedData;
			return this;
		}
		public String getDeviceId() {
			return this.deviceID;
		}
		public String getSentenceId() {
			return this.sentenceId;
		}
		public String getFullSentence() {
			return this.fullSentence;
		}
		public Object getParsedData() {
			return this.parsedData;
		}
	}

	public static Dispatcher findDispatcherByKey(String key) {
		Optional<Dispatcher> first = Arrays.stream(Dispatcher.values())
				.filter(disp -> key.equals(disp.key()))
				.findFirst();
		return first.orElse(null);
	}
	/**
	 * Lists available parsers, key and description.
	 */
	public static void listDispatcher() {
		listDispatchers(System.out);
	}
	public static void listDispatchers(PrintStream out) {
		Arrays.stream(Dispatcher.values())
				.forEach(dispatcher -> out.printf("%s: %s\n", dispatcher.key(), dispatcher.description()));
	}

	public static ParsedData autoParse(String sentence) {
		if (!validCheckSum(sentence)) {
			throw new RuntimeException(String.format("Invalid NMEA Sentence CheckSum [%s]", sentence));
		}
		ParsedData parsedData = new ParsedData().fullSentence(sentence);
		String key = getSentenceID(sentence);
		parsedData.sentenceId(key).deviceID(getDeviceID(sentence));
		for (Dispatcher dispatcher : Dispatcher.values()) {
			if (key.equals(dispatcher.key)) {
				Object parsed = dispatcher.parser().apply(sentence);
				parsedData.parsedData(parsed);
				break;
			}
		}
		return parsedData;
	}
}

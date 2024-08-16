package tideengine.publisher;

import calc.GeoPoint;
import calc.GeomUtil;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
// import java.util.TimeZone;

public class TidePublisher {

	// Script names in a System variable. they must be in the xsl folder.
	public final static String AGENDA_TABLE = System.getProperty("publishagenda.script", "publishagenda.sh");
	public final static String TIDE_TABLE = System.getProperty("publishtide.script", "publishtide.sh");
	public final static String MOON_CALENDAR = System.getProperty("publishlunarcalendar.script", "publishlunarcalendar.sh");
	public final static String SCRIPT_PATH = System.getProperty("script.path", "./xsl");
	public final static String PDF_PATH = System.getProperty("pdf.path", "./web");

	public static String publish(
			TideStation ts,
			String timeZoneId,
			int sm,
			int sy,
			int nb,
			int q,
			String utu,
			TideUtilities.SpecialPrm sPrm,
			String scriptToRun) throws Exception {
		return publish(ts, timeZoneId, sm, sy, nb, q, utu, sPrm, scriptToRun, null);
	}

	/**
	 * @param ts         TideStation
	 * @param timeZoneId TimeZone ID to use
	 * @param sm         Start Month
	 * @param sy         Start Year
	 * @param nb         Number of q (see below)
	 * @param q          quantity. Calendar.MONTH or Calendar.YEAR
	 * @param utu        Unit to use
	 * @param sPrm       Special parameters
	 * . . .
	 */
	public static String publish(
			TideStation ts,
			String timeZoneId,
			int sm,
			int sy,
			int nb,
			int q,
			String utu,
			TideUtilities.SpecialPrm sPrm,
			String scriptToRun,
			String finalFileName)
			throws Exception {

		final TideUtilities.SpecialPrm specialBGPrm = sPrm;

		System.out.println("Starting month:" + sm + ", year:" + sy);
		System.out.println("For " + nb + " " + (q == Calendar.MONTH ? "month(s)" : "year(s)"));

		GregorianCalendar start = new GregorianCalendar(sy, sm, 1);
		GregorianCalendar end = (GregorianCalendar) start.clone();
		end.add(q, nb);
		boolean loop = true;
		PrintStream out = System.out;
		String radical = "";
		String prefix = (scriptToRun == null ? TIDE_TABLE : scriptToRun);
		try {
			File tempFile = File.createTempFile(prefix + ".data.", ".xml");
			out = new PrintStream(new FileOutputStream(tempFile));
			radical = tempFile.getAbsolutePath();
			radical = radical.substring(0, radical.lastIndexOf(".xml"));
		    System.out.println("Writing data in " + tempFile.getAbsolutePath());
		} catch (Exception ex) {
			System.err.println("Error creating temp file");
			ex.printStackTrace();
			throw ex;
		}

		try {
			out.println("<tide station='" + URLDecoder.decode(ts.getFullName(), StandardCharsets.UTF_8.toString()).replace("'", "&apos;") +
					"' station-time-zone='" + ts.getTimeZone() +
					"' print-time-zone='" + timeZoneId +
					"' station-lat='" + GeomUtil.decToSex(ts.getLatitude(), GeomUtil.SWING, GeomUtil.NS, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") +
					"' station-lng='" + GeomUtil.decToSex(ts.getLongitude(), GeomUtil.SWING, GeomUtil.EW, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") + "'>");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		while (loop) {
			if (start.equals(end)) {
				loop = false;
			} else {
				out.println("  <period month='" + (start.get(Calendar.MONTH) + 1) + "' year='" + start.get(Calendar.YEAR) + "'>");
				try {
					if ("true".equals(System.getProperty("tide.calc.verbose"))) {
						System.out.println("Calculating tide for " + start.getTime().toString());
					}
					TideForOneMonth.tideForOneMonth(out,
							timeZoneId,
							start.get(Calendar.YEAR),
							start.get(Calendar.MONTH) + 1, // Base: 1
							ts.getFullName(),
							(utu == null ? ts.getUnit() : utu),
							BackEndTideComputer.buildSiteConstSpeed(),
							TideForOneMonth.XML_FLAVOR,
							specialBGPrm);
					start.add(Calendar.MONTH, 1);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				out.println("  </period>");
			}
		}
		out.println("</tide>");
		out.close();
		if ("true".equals(System.getProperty("tide.calc.verbose"))) {
			System.out.println("Generation completed.");
		}
		// Ready for transformation
		try {
			// This part customizable, see script.path VM variable nd Co
			String cmd = // "." + File.separator + "xsl" + File.separator + (scriptToRun == null ? TIDE_TABLE : scriptToRun) + " " + radical;
			SCRIPT_PATH + File.separator + (scriptToRun == null ? TIDE_TABLE : scriptToRun) + " " + radical;
			System.out.println("Executing System Command:" + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			int exitStatus = p.waitFor();
			System.out.println("Script completed, status " + exitStatus);
			System.out.printf("See %s.pdf\n", radical);
			String movingTo = PDF_PATH + (finalFileName != null ? String.format("%s%s.pdf", File.separator, finalFileName) : "");
			System.out.printf("-- Now moving %s to %s\n", radical, movingTo);
			cmd = String.format("mv %s.pdf %s", radical, movingTo);
			p = Runtime.getRuntime().exec(cmd);
			exitStatus = p.waitFor();
			System.out.printf("Command [%s] completed, status %s\n", cmd, exitStatus);

			return "." + radical.substring(radical.lastIndexOf(File.separator)) + ".pdf";
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static String publish(String stationName, int startMonth, int startYear, int nb, int quantity)
			throws Exception {
		return publish(stationName, startMonth, startYear, nb, quantity, null);
	}

	public static String publish(String stationName, int startMonth, int startYear, int nb, int quantity, String script)
			throws Exception {
		return publish(stationName, startMonth, startYear, nb, quantity, script, null);
	}

	/**
	 *
	 * @param stationName Station name
	 * @param startMonth between Calendar.JANUARY and Calendar.DECEMBER
	 * @param startYear like 2024
	 * @param nb Number of quantity (see below)
	 * @param quantity like Calendar.YEAR, Calendar.MONTH, etc
	 * @param script Name of the script to execute to publish. Can come from a System Variable (See TIDE_TABLE & Co). Warning: See the script.path System variable !!
	 * @param finalFileName Will be used if not null. Also see the pdf.path System Variable.
	 * @return The name of the result-file of the fop publication.
	 * @throws Exception
	 */
	public static String publish(String stationName, int startMonth, int startYear, int nb, int quantity, String script, String finalFileName)
			throws Exception {
		TideStation ts;
		try {
			Optional<TideStation> optTs = BackEndTideComputer.getStationData()
					.stream()
					.filter(station -> station.getFullName().equals(stationName))
					.findFirst();
			if (!optTs.isPresent()) {
				throw new Exception(String.format("Station [%s] not found.", stationName));
			} else {
				ts = optTs.get();
				return publish(ts, ts.getTimeZone(), startMonth, startYear, nb, quantity, null, null, script, finalFileName);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}


	// A publish from pos. Returns SunRise, SunSet, SunTransit
	public static String publishFromPos(
			GeoPoint position,
			String stationName,
			String timeZoneId,
			int sm,
			int sy,
			int nb,
			int q,
			String scriptToRun)
			throws Exception {

		System.out.println("Starting month:" + sm + ", year:" + sy);
		System.out.println("For " + nb + " " + (q == Calendar.MONTH ? "month(s)" : "year(s)"));

		GregorianCalendar start = new GregorianCalendar(sy, sm, 1);
		GregorianCalendar end = (GregorianCalendar) start.clone();
		end.add(q, nb);
		boolean loop = true;
		PrintStream out = System.out;
		String radical = "";
		String prefix = (scriptToRun == null ? TIDE_TABLE : scriptToRun);
		try {
			File tempFile = File.createTempFile(prefix + ".data.", ".xml");
			out = new PrintStream(new FileOutputStream(tempFile));
			radical = tempFile.getAbsolutePath();
			radical = radical.substring(0, radical.lastIndexOf(".xml"));
			System.out.println("Writing data in " + tempFile.getAbsolutePath());
		} catch (Exception ex) {
			System.err.println("Error creating temp file");
			ex.printStackTrace();
			throw ex;
		}

		try {
			// With those attributes, we re-use the same XSL Stylesheet as for the tide.
			System.out.println("PublishFromPos, position:" + position);

			out.println("<position print-time-zone='" + timeZoneId +
					"' station='" + (stationName != null ? stationName : "User-Defined") +
					"' station-lat='" + GeomUtil.decToSex(position.getL(), GeomUtil.SWING, GeomUtil.NS, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") +
					"' station-lng='" + GeomUtil.decToSex(position.getG(), GeomUtil.SWING, GeomUtil.EW, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") + "'>");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		while (loop) {
			if (start.equals(end)) {
				loop = false;
			} else {
				out.println("  <period month='" + (start.get(Calendar.MONTH) + 1) + "' year='" + start.get(Calendar.YEAR) + "'>");
				try {
					System.out.println("Calculating sun data for " + start.getTime().toString());
					TideForOneMonth.sunForOneMonth(out,
							timeZoneId,
							start.get(Calendar.YEAR),
							start.get(Calendar.MONTH) + 1, // Base: 1
							position,
							TideForOneMonth.XML_FLAVOR);
					start.add(Calendar.MONTH, 1);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				out.println("  </period>");
			}
		}
		out.println("</position>");
		out.close();
		System.out.println("Generation completed.");
		// Ready for transformation
		try {
			String cmd = // "." + File.separator + "xsl" + File.separator + (scriptToRun == null ? TIDE_TABLE : scriptToRun) + " " + radical;
					     SCRIPT_PATH + File.separator + (scriptToRun == null ? TIDE_TABLE : scriptToRun) + " " + radical;
			System.out.println("Command:" + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			int exitStatus = p.waitFor();
			System.out.println("Script completed, status " + exitStatus);
			System.out.printf("File %s.pdf generated\n", radical);
			System.out.printf("Now moving %s to %s\n", radical, PDF_PATH);
			cmd = String.format("mv %s.pdf %s", radical, PDF_PATH); // Tricky... Works only on some shells.
			p = Runtime.getRuntime().exec(cmd);
			exitStatus = p.waitFor();
			System.out.printf("Command [%s] completed, status %s\n", cmd, exitStatus);

			return "." + radical.substring(radical.lastIndexOf(File.separator)) + ".pdf";
		} catch (Exception ex) {
			throw ex;
		}
	}

	private static String STATION_PREFIX = "--station-name:";
	private static String YEAR_PREFIX = "--tide-year:";


	private static void recurseTreeNode(TreeMap<String, TideUtilities.StationTreeNode> subTree, List<TideUtilities.StationTreeNode> stationList) {
		subTree.keySet().forEach(name -> {
			final TideUtilities.StationTreeNode stationTreeNode = subTree.get(name);
			if (stationTreeNode.getFullStationName() != null) {
				// System.out.printf("Adding %s\n", stationTreeNode.getFullStationName());
				stationList.add(stationTreeNode);
			} else {
				recurseTreeNode(stationTreeNode.getSubTree(), stationList);
			}
		});
	}

	public static List<TideStation> getStationList(double nLat, double sLat, double wLng, double eLng) {
		return getStationList(nLat, sLat, wLng, eLng, null);
	}
	/**
	 * Select tide stations based on their position
	 * @param nLat
	 * @param sLat
	 * @param wLng
	 * @param eLng
	 * @param firstFilter one of "Africa", "America", "Antarctica", "Asia", "Atlantic", "Australia", "Etc", "Europe", Indian", "Pacific".
	 * @return
	 * @throws Exception
	 */
	public static List<TideStation> getStationList(double nLat, double sLat, double wLng, double eLng, String firstFilter) {
		BackEndTideComputer backEndTideComputer = new BackEndTideComputer();
		try {
			backEndTideComputer.connect();
			backEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));
			final Map<String, TideUtilities.StationTreeNode> stationTreeNodeMap = backEndTideComputer.buildStationTree();

			final List<TideUtilities.StationTreeNode> stationList = new ArrayList<>();

			Set<String> keys;
			if (firstFilter == null) {
				keys = stationTreeNodeMap.keySet();
			} else {
				keys = new HashSet<>();
				keys.add(firstFilter);
			}
			keys.forEach(nodeName -> {
				final TideUtilities.StationTreeNode stationTreeNode = stationTreeNodeMap.get(nodeName);
				final TreeMap<String, TideUtilities.StationTreeNode> subTree = stationTreeNode.getSubTree();
				recurseTreeNode(subTree, stationList);
			});

			// System.out.printf("Full list has %d entries\n", stationList.size());

			final List<TideStation> selectedList = new ArrayList<>();
			stationList.forEach(namedStation -> {
				String stationName = namedStation.getFullStationName();
				// System.out.println(stationName);
				TideStation ts = null;
				try {
					Optional<TideStation> optTs = BackEndTideComputer.getStationData()
							.stream()
							.filter(station -> station.getFullName().equals(stationName))
							.findFirst();
					if (!optTs.isPresent()) {
						throw new Exception(String.format("Station [%s] not found.", stationName));
					} else {
						ts = optTs.get();
						if (ts.isTideStation()) {
							if (ts.getLatitude() >= sLat && ts.getLatitude() <= nLat && ts.getLongitude() <= eLng && ts.getLongitude() >= wLng) {
								selectedList.add(ts);
							}
						}
					}
				} catch (Exception ex1) {
					throw new RuntimeException(ex1);
				}
			});
			// System.out.printf("Selected list has %d entries\n", selectedList.size());
			// Sort on latitude, increasing
			Collections.sort(selectedList, Comparator.comparingDouble(ts -> ts.getLatitude()));
			Collections.reverse(selectedList); // N to S

			return selectedList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	/**
	 * For tests
	 *  requires ./xsl/publishtide.sh to be available !!
	 *
	 * @param args --station-name:XXXX --tide-year:2023.
	 *        Like --station-name:"Brest, France" --tide-year:2024
	 */
	public static void main__(String... args) {

		BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

		// String station = "Ocean Beach, California";
		String station = "Brest, France";
		int year = 2023;

		for (String arg : args) {
			System.out.printf("Processing arg [%s]\n", arg);
			if (arg.startsWith(STATION_PREFIX)) {
				station = arg.substring(STATION_PREFIX.length());
				if ((station.startsWith("'") && station.endsWith("'")) || station.startsWith("\"") && station.endsWith("\"")) { // Trim quotes
					station = station.substring(1, station.length() - 1);
				}
				station = station.replace("+", " ");  // Ugly escape trick...
			}
			if (arg.startsWith(YEAR_PREFIX)) {
				year = Integer.parseInt(arg.substring(YEAR_PREFIX.length()));
			}
		}

		System.out.printf("Station [%s], for year %d\n", station, year);

		try {
			backEndTideComputer.connect();
			backEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));

			String f = publish(
					URLEncoder.encode(station, StandardCharsets.UTF_8.toString()).replace("+", "%20"),
					Calendar.JANUARY,
					year,
					1,
					Calendar.YEAR,
					TIDE_TABLE);  // Change at will AGENDA_TABLE, MOON_CALENDAR

			System.out.printf("%s generated, in %s\n", f, System.getProperty("user.dir"));
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {
		try {
//			List<TideStation> selectedList = getStationList(50.0, 42.5, -10.0, 5.0, "Pacific");
			List<TideStation> selectedList = getStationList(90.0, -90, -180.0, 1805.0, "Pacific");
			if (selectedList != null) {
				selectedList.stream().forEach(station -> {
					try {
						String decodedStationName = URLDecoder.decode(station.getFullName(), StandardCharsets.ISO_8859_1.toString());
						System.out.printf("%s, %s\n", decodedStationName, GeomUtil.decToSex(station.getLatitude(), GeomUtil.SWING, GeomUtil.NS, GeomUtil.TRAILING_SIGN));
//						main__(new String[]{
//								String.format("--station-name:\"%s\"", URLDecoder.decode(station.getFullName(), StandardCharsets.ISO_8859_1.toString())),
//								"--tide-year:2024"
//						});
						// System.out.println("More!");
					} catch (Exception ex2) {
						ex2.printStackTrace();
					}
				});
			}
			System.out.println("Et hop!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

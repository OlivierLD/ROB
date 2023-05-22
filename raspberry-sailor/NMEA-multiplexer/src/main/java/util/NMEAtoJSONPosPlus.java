package util;

import calc.GreatCircle;
import calc.GreatCirclePoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import context.ApplicationContext;
import context.NMEADataCache;
import gribprocessing.utils.PolarHelper;
import nmea.computers.current.LongTimeCurrentCalculator;
import nmea.parser.*;
import nmea.utils.NMEAUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * JSON positions generator, from NMEA log (possibly from a zip), for a possible replay in LeafLet
 * Optional SOG and COG (and others if needed, see below...)
 *
 * LeafLet supports this format, like
 * let latlngs = [
 *         [45.51, -122.68],
 *         [37.77, -122.43],
 *         [34.04, -118.2]
 *     ];
 *
 *     Adds other data:
 *     - Heading (true, magnetic, compass) with D and d.
 *     - Computes Current (buffered)
 *     - Apparent and True Wind (computed with GPS data)
 *     - Water temperature ?
 *     - Performances ? (requires polars)
 *
 * Requires sentences like:
 * - RMC, Recommended Minimum Navigation Information, C
 * - MTW, Mean Temperature of Water
 * - VHW, Water speed and heading
 * - HDG, HDM, HDT, Headings... (heading redundant with the above)
 * - VWR, Relative Wind Speed and Angle (prefer to MWV)
 * - MWV, Wind Speed and Angle
 *
 * See CLI prms:
 * --file-name: Mandatory. File name, on the file system, or in the archive
 * --archive-name: Optional. Archive file name
 * --default-decl: Optional, default 0. Used if decl is not in RMC
 * --awa-offset: Optional, default 0.
 * --hdg-offset: Optional, default 0.
 * --bsp-coeff: Optional, default 1.
 * --aws-coeff: Optional, default 1.
 * --max-leeway: Optional, default 10 degrees
 * --dev-curve: Deviation curve
 * --polar-file: Polar file
 * --current-buffer-length: Buffer Length (in ms) for current computation
 * Example:
 *   --file-name:2010-11-08.Nuku-Hiva-Tuamotu.nmea
 *   --archive-name:/Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/sample-data/logged.data.archive.zip
 *   --dev-curve:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv
 *   --polar-file:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff
 *   --current-buffer-length:600000
 */
public class NMEAtoJSONPosPlus {

	private static boolean verbose = "true".equals(System.getProperty("verbose"));
	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	static {
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	private static class ObjectToLog extends GeoPos {
		private double sog;
		private double cog;
		private String rmcDate;
		private double hdt; // True Heading
		private double hdc; // Compass Heading
		private double cmg; // CMG, calculated with leeway
		private double leeway;
		private double decl; // Mag Declination
		private double dev; // Mag deviation
		private double bsp; // Boat Speed
		private double mwt; // Water Temperature in degrees Celsius
		private double awa; // Apparent Wind Angle, in degrees
		private double aws; // Apparent Wind Speed in knots
		private double twa; // True Wind Angle in degrees
		private double tws; // True Wind Speed in knots
		private double twd; // True Wind Direction
		private double csp; // Current Speed (in knots)
		private double cdr; // Current direction
		private double perf; // Performance (from polars)

		public ObjectToLog(double l, double g) {
			super(l, g);
		}
		public double getSog() {
			return sog;
		}

		public void setSog(double sog) {
			this.sog = sog;
		}

		public double getCog() {
			return cog;
		}

		public void setCog(double cog) {
			this.cog = cog;
		}

		public String getRmcDate() {
			return rmcDate;
		}

		public void setRmcDate(String rmcDate) {
			this.rmcDate = rmcDate;
		}

		public double getHdt() {
			return hdt;
		}

		public void setHdt(double hdt) {
			this.hdt = hdt;
		}

		public double getHdc() {
			return hdc;
		}

		public void setHdc(double hdc) {
			this.hdc = hdc;
		}

		public double getDecl() {
			return decl;
		}

		public void setDecl(double decl) {
			this.decl = decl;
		}

		public double getDev() {
			return dev;
		}

		public void setDev(double dev) {
			this.dev = dev;
		}

		public double getBsp() {
			return bsp;
		}

		public void setBsp(double bsp) {
			this.bsp = bsp;
		}

		public double getMwt() {
			return mwt;
		}

		public void setMwt(double mwt) {
			this.mwt = mwt;
		}

		public double getAwa() {
			return awa;
		}

		public void setAwa(double awa) {
			this.awa = awa;
		}

		public double getAws() {
			return aws;
		}

		public void setAws(double aws) {
			this.aws = aws;
		}

		public double getTwa() {
			return twa;
		}

		public void setTwa(double twa) {
			this.twa = twa;
		}

		public double getTws() {
			return tws;
		}

		public void setTws(double tws) {
			this.tws = tws;
		}

		public double getTwd() {
			return twd;
		}

		public void setTwd(double twd) {
			this.twd = twd;
		}

		public double getCsp() {
			return csp;
		}

		public void setCsp(double csp) {
			this.csp = csp;
		}

		public double getCdr() {
			return cdr;
		}

		public void setCdr(double cdr) {
			this.cdr = cdr;
		}

		public double getPerf() {
			return perf;
		}

		public void setPerf(double perf) {
			this.perf = perf;
		}

		public double getCmg() {
			return cmg;
		}

		public void setCmg(double cmg) {
			this.cmg = cmg;
		}

		public double getLeeway() {
			return leeway;
		}

		public void setLeeway(double leeway) {
			this.leeway = leeway;
		}
	}

	private static class TimeCurrent {
		private final long time;
		private final double speed;
		private final double dir;

		public TimeCurrent(long time, double speed, double dir) {
			this.time = time;
			this.speed = speed;
			this.dir = dir;
		}

		public long getTime() {
			return time;
		}

		public double getSpeed() {
			return speed;
		}

		public double getDir() {
			return dir;
		}
	}

	private static class CurrentComputer {

		private long bufferLength = 600_000L;  // 10 minutes in milliseconds
		private final static long ONE_HOUR_MS = 3_600_000L;
		// Time, Position, CMG, BSP.
		private List<TimeCurrent> timeCurrent = new ArrayList<>();
		private List<UTCHolder> timeBuffer = new ArrayList<>();
		private List<GeoPos> positionBuffer = new ArrayList<>();
		private List<Double> cmgBuffer = new ArrayList<>();
		// private List<Double> hdgBuffer = new ArrayList<>();
		private List<Double> bspBuffer = new ArrayList<>();

		private GreatCirclePoint[] groundData = null;
		private GreatCirclePoint[] drData = null;

		public CurrentComputer() {
		}
		public CurrentComputer(long bufferLength) {
			this.bufferLength = bufferLength;
		}

		protected void resetDataBuffers() {
			timeBuffer = new ArrayList<>();
			positionBuffer = new ArrayList<>();
			cmgBuffer = new ArrayList<>();
			// hdgBuffer = new ArrayList<>();
			bspBuffer = new ArrayList<>();
			timeCurrent = new ArrayList<>();
		}

		protected TimeCurrent pushAndCompute(UTCHolder utcDate, GeoPos position, Double cmg, Double bsp /*, Double hdg*/) {
			TimeCurrent result = null;
			if (timeBuffer != null &&
					utcDate != null &&
					!utcDate.isNull() &&
					utcDate.getValue() != null &&
					(timeBuffer.size() == 0 ||
							(timeBuffer.size() > 0 &&
									timeBuffer.get(timeBuffer.size() - 1).getValue() != null &&
									(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime())))) {
				if (utcDate != null && cmg != null && position != null && bsp != null /* && hdg != null */) {
					if (timeBuffer.size() > 0) {
						UTCHolder oldest = timeBuffer.get(0);
						boolean keepGoing = true; // To resize the buffers

						while (keepGoing && oldest.getValue().getTime() < (utcDate.getValue().getTime() - bufferLength)) {
							timeBuffer.remove(0);
							positionBuffer.remove(0);
							cmgBuffer.remove(0);
							bspBuffer.remove(0);
							// hdgBuffer.remove(0);

							if (timeBuffer.size() > 0) {
								oldest = timeBuffer.get(0);
							} else {
								keepGoing = false;
							}
						}
					}

					timeBuffer.add(utcDate);
					positionBuffer.add(position);
					// System.out.println("Adding position:" + position.toString());
					cmgBuffer.add(cmg);
					bspBuffer.add(bsp);
					// hdgBuffer.add(hdg);
					groundData = new GreatCirclePoint[positionBuffer.size()];
					int index = 0;
					for (GeoPos gp : positionBuffer) {
						groundData[index++] = new GreatCirclePoint(gp.lat, gp.lng); // GPS
					}
					// index = 0;
					drData = new GreatCirclePoint[positionBuffer.size()]; // Positions from DoW and CMG (aka on water)
					GeoPos drPos = positionBuffer.get(0);
					int size = positionBuffer.size();
					// DoW: Distance on Water, CMG: Course Made Good
					// From point to point (with EACH DoW and CMG) calculate the point we should have reached at the end of the buffer.
					// The difference is the vector of the current.
					for (int i = 0; i < size; i++) {
						if (i > 0) {
							long timeInterval = timeBuffer.get(i).getValue().getTime() - timeBuffer.get(i - 1).getValue().getTime();
							double bSpeed = bspBuffer.get(i);
							// System.out.println("-- TimeInterval:" + timeInterval + ", bsp:" + bSpeed);
							if (bSpeed > 0) { // Then calculate estimated pos, with DoW and CMG
								double dist = bSpeed * ((double) timeInterval / (double) ONE_HOUR_MS); // in minutes (miles)
								double rv = cmgBuffer.get(i - 1); // rv: Route Vraie (aka Surface): French for CMG.
								// System.out.println("** In " + timeInterval + " ms, at " + bSpeed + " kts, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + "... ");
								if (dist > 0) {
									GreatCirclePoint pt = MercatorUtil.deadReckoning(drPos.lat, drPos.lng, dist, rv);
									// System.out.println("In " + timeInterval + " ms, from " + drPos.toString() + " dist:" + dist + ", hdg:" + hdg + ", ends up " + pt.toString());
									drPos = new GeoPos(pt.getL(), pt.getG()); // We should be here if no current
								}
							}
						} // else we set the starting point
						drData[i] = new GreatCirclePoint(drPos.lat, drPos.lng);
					}
					// final DR (on water...)
					GreatCirclePoint geoFrom = new GreatCirclePoint(
							Math.toRadians(drData[drData.length - 1].getL()),
							Math.toRadians(drData[drData.length - 1].getG()));
					// final GPS (on the ground)
					GreatCirclePoint geoTo = new GreatCirclePoint(
							Math.toRadians(groundData[groundData.length - 1].getL()),
							Math.toRadians(groundData[groundData.length - 1].getG()));

					// Between the 2 above: the current
					double dist = GreatCircle.calculateRhumbLineDistance(geoFrom, geoTo); // Dist between DR & GPS
					double currentDir = Math.toDegrees(GreatCircle.calculateRhumbLineRoute(geoFrom, geoTo));
					double hourRatio = (double) (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() - timeBuffer.get(0).getValue().getTime()) / (double) ONE_HOUR_MS;
					double currentSpeed = dist / hourRatio;
					timeCurrent.add(new TimeCurrent(
							timeBuffer.get(timeBuffer.size() - 1).getValue().getTime(),
							currentSpeed,
							currentDir));
					// Trim current buffer to the time-length.
					// Remove point older than the buffer length (which is a time interval, not a cardinality)
					long oldest = timeCurrent.get(0).getTime();
					boolean keepGoing = true;
					while (keepGoing && oldest < (timeCurrent.get(timeCurrent.size() - 1).getTime() - bufferLength)) {
						timeCurrent.remove(0);
						if (timeBuffer.size() > 0) {
							oldest = timeCurrent.get(0).getTime();
						} else {
							keepGoing = false;
						}
					}
					if (verbose) {
						System.out.println("Inserting Current: on:" + NumberFormat.getInstance().format(bufferLength) + " ms, " + currentSpeed + " kts, dir:" + currentDir);
					}
					long time;
					final UTCTime utcTime = utcDate.getUtcTime();
					if (utcTime != null) {
						time = utcTime.getDate().getTime();
					} else {
						time = utcDate.getUtcDate().getEpoch();
					}
					result = new TimeCurrent(time, currentSpeed, currentDir);
					if (verbose) {
						System.out.println("Calculated Current Map:" + map.size() + " entry(ies)");
					}
				}
			} else if (verbose) {
				//  if (!utcDate.isNull() && (timeBuffer.size() == 0 || (timeBuffer.size() > 0 && (timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()))))
				System.out.println("utcDate is " + (utcDate == null || utcDate.isNull() ? "" : "not ") + "null");
				System.out.println("timeBuffer.size() = " + timeBuffer.size());
				System.out.println("utcDate        :" + (utcDate.isNull() ? "" : new Date(utcDate.getValue().getTime()).toString()));
				System.out.println("last timeBuffer:" + (timeBuffer.size() > 0 ? new Date(timeBuffer.get(timeBuffer.size() - 1).getValue().getTime()).toString() : "none"));
				try {
					if (timeBuffer.size() > 0) {
						System.out.println("-> " + ((timeBuffer.get(timeBuffer.size() - 1).getValue().getTime() < utcDate.getValue().getTime()) ? "true" : "false"));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return result;
		}
	}
	private final static ObjectMapper mapper = new ObjectMapper();
	private final static Map<String, Integer> map = new HashMap<>();

	private static double awsCoeff = 1.0;
	private static double bspCoeff = 1.0;
	private static double awaOffset = 0.0;
	private static double hdgOffset = 0.0;
	private static double maxLeeway = 10.0;

	private static CurrentComputer currentComputer = null;
	private static void transform(String fileInName,
								  String archiveName,
								  double defaultDeclinationValue,
								  String deviationCurveFile,
								  String polarFileName,
	                              String fileOutName) throws Exception {

		List<Object> jsonArray = new ArrayList<>();

		List<double[]> deviationCurve = null;
		if (deviationCurveFile != null) {
			deviationCurve = NMEAUtils.loadDeviationCurve(deviationCurveFile);
		}
		PolarHelper polarHelper = null;
		if (polarFileName != null) {
			polarHelper = new PolarHelper(polarFileName);
		}

		InputStream fis;
		if (archiveName != null) {
			String pathInArchive = fileInName; // Required
			if (verbose) {
				System.out.printf("Will look into %s to analyze %s\n", archiveName, pathInArchive);
			}
			ZipFile zipFile = new ZipFile(archiveName);
			ZipEntry zipEntry = zipFile.getEntry(pathInArchive);
			if (zipEntry == null) { // Path not found in the zip, take first entry.
				zipEntry = zipFile.entries().nextElement();
			}
			fis = zipFile.getInputStream(zipEntry);
		} else {
			if (verbose) {
				System.out.printf("Will analyze %s\n", fileInName);
			}
			fis = new FileInputStream(fileInName);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
		// BufferedReader br = new BufferedReader(new FileReader(fileInName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutName));

		String line = "";

		// Read the data
		double decl = defaultDeclinationValue; // Defaulted, in case not found in RMC
		Double heading = null; // Compass Heading
		Double mtw = null;
		Double bsp = null;
		Double awa = null;
		Double aws = null;
		Double hdt = null; // True Heading

		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.startsWith("$") && line.length() > 6) {
					String prefix = line.substring(3, 6);
					Integer nb = map.get(prefix);
					map.put(prefix, (nb == null) ? (1) : (nb + 1));
					// Specific - To be extended at will...
					if ("RMC".equals(prefix)) {
						if (StringParsers.validCheckSum(line)) {
							RMC rmc = StringParsers.parseRMC(line);
							if (rmc != null && rmc.getRmcTime() != null && rmc.isValid()) {
								// Add everything here !
								ObjectToLog otl = new ObjectToLog(rmc.getGp().lat, rmc.getGp().lng);
								otl.setRmcDate(SDF_UTC.format(rmc.getRmcTime())); // getRmcDate can be null...
								otl.setCog(rmc.getCog());
								otl.setSog(rmc.getSog());
								if (bsp != null) {
									otl.setBsp(bsp);
								}
								if (mtw != null) {
									otl.setMwt(mtw);
								}
								// More, computed data
								if (rmc.getDeclination() != -Double.MAX_VALUE) {
									decl = rmc.getDeclination();
								}
								// Compute True Heading
								if (heading != null) {
									double dev = 0d;
									if (deviationCurve != null) {
										dev = NMEAUtils.getDeviation(heading, deviationCurve); // From the curve
									}
									// double hdm = heading + dev; // Magnetic
									double w = decl + dev;
									hdt = heading + w;   // true
									otl.setHdc(heading);
									otl.setHdt(hdt);
									otl.setDecl(decl);
									otl.setDev(dev);
								}
								// More Computations !
								// Compute TRUE wind
								if (aws != null) {
									// System.out.println("Using the GOOD method");
									double[] tw = NMEAUtils.calculateTWwithGPS(aws,
																			   awsCoeff,
																			   awa,
																			   awaOffset,
																			   heading,
																			   hdgOffset,
																			   rmc.getSog(),
																			   rmc.getCog());
									double twa = tw[0];
									double tws = tw[1];
									double twd = tw[2];
									otl.setAwa(awa + awaOffset);
									otl.setAws(aws * awsCoeff);
									otl.setTwa(twa);
									otl.setTws(tws);
									otl.setTwd(twd);
									// Compute CMG
									double leeway = NMEAUtils.getLeeway(awa, maxLeeway);
									double cmg = hdt + leeway;
									otl.setLeeway(leeway);
									otl.setCmg(cmg);

									// Compute performance
									if (bsp != null && polarHelper != null) {
										double polarSpeed = polarHelper.getSpeed(tws, Math.abs(twa), 1.0);
										double perf = bsp / polarSpeed;
										otl.setPerf(perf);
									}

								}
								// Compute Current
								if (currentComputer != null) {
									final TimeCurrent timeCurrent = currentComputer.pushAndCompute(new UTCHolder(new UTCDate(rmc.getRmcTime())), rmc.getGp(), otl.getCmg(), bsp); //, hdt);
									if (timeCurrent != null && !Double.isNaN(timeCurrent.getDir()) && !Double.isNaN(timeCurrent.getSpeed())) {
										otl.setCsp(timeCurrent.getSpeed());
										otl.setCdr(timeCurrent.getDir());
									}
								}
								// Finally
								jsonArray.add(otl);
							}
						}
					} else if ("HDG".equals(prefix)) {  // Compass Heading
						if (StringParsers.validCheckSum(line)) {
							final HDG hdg = StringParsers.parseHDG(line);
							heading = hdg.getHeading() + hdgOffset;
						}
					} else if ("MTW".equals(prefix)) {  // Water Temperature
						if (StringParsers.validCheckSum(line)) {
							mtw = StringParsers.parseMTW(line);
						}
					} else if ("VHW".equals(prefix)) {  // Water Speed and Heading
						if (StringParsers.validCheckSum(line)) {
							final VHW vhw = StringParsers.parseVHW(line);
							heading = vhw.getHdg() + hdgOffset; // Compass Heading
							bsp = vhw.getBsp() * bspCoeff;
						}
					} else if ("VWR".equals(prefix)) {  // Relative Wind Speed and Angle
						if (StringParsers.validCheckSum(line)) {
							final ApparentWind apparentWind = StringParsers.parseVWR(line);
							awa = (double)apparentWind.getAngle();
							aws = apparentWind.getSpeed();
						}
					}
				}
			}
		}
		br.close();
		if ("true".equals(System.getProperty("verbose"))) {
			System.out.println(mapper.writeValueAsString(jsonArray));
		}
		System.out.printf("Generated %s JSON elements\n", NumberFormat.getInstance().format(jsonArray.size()));
		// TODO Analyze ?

		boolean minified = "true".equals(System.getProperty("minified", "true"));
		// Cleanup: Remove latInDegMinDec nd lngInDegMinDec, to make the json smaller.
		final JsonNode jsonNode = mapper.valueToTree(jsonArray);
		for (JsonNode node : jsonNode) {
			((ObjectNode)node).remove("latInDegMinDec");
			((ObjectNode)node).remove("lngInDegMinDec");
		}
		if (minified) {
			bw.write(mapper.writeValueAsString(jsonNode));
		} else {
			bw.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
		}
		bw.close();
	}

	private final static String FILE_NAME_PREFIX = "--file-name:";
	private final static String ARCHIVE_NAME_PREFIX = "--archive-name:";
	private final static String DEFAULT_DECLINATION_PREFIX = "--default-decl:";
	private final static String AWA_OFFSET_PREFIX = "--awa-offset:";
	private final static String HDG_OFFSET_PREFIX = "--hdg-offset:";
	private final static String AWS_COEFF_PREFIX = "--aws-coeff:";
	private final static String BSP_COEFF_PREFIX = "--bsp-coeff:";
	private final static String MAX_LEEWAY_PREFIX = "--max-leeway:";
	private final static String DEV_CURVE_PREFIX = "--dev-curve:";
	private final static String POLAR_FILE_PREFIX = "--polar-file:";
	private final static String CC_BUFFER_LENGTH_PREFIX = "--current-buffer-length:";

	public static void main(String... args) {

		String fileName = null;
		String archiveName = null;
		double defaultDeclinationValue = 0d;
		String deviationCurveFile = null;
		String polarFileName = null;
		Long currentComputerBufferLength = null;

		if (args.length != 1) {
			for (String arg : args) {
				if (arg.startsWith(FILE_NAME_PREFIX)) {
					fileName = arg.substring(FILE_NAME_PREFIX.length());
				} else if (arg.startsWith(ARCHIVE_NAME_PREFIX)) {
					archiveName = arg.substring(ARCHIVE_NAME_PREFIX.length());
				} else if (arg.startsWith(DEFAULT_DECLINATION_PREFIX)) {
					defaultDeclinationValue = Double.parseDouble(arg.substring(DEFAULT_DECLINATION_PREFIX.length()));
				} else if (arg.startsWith(DEV_CURVE_PREFIX)) {
					deviationCurveFile = arg.substring(DEV_CURVE_PREFIX.length());
				} else if (arg.startsWith(POLAR_FILE_PREFIX)) {
					polarFileName = arg.substring(POLAR_FILE_PREFIX.length());
				} else if (arg.startsWith(AWA_OFFSET_PREFIX)) {
					awaOffset = Double.parseDouble(arg.substring(AWA_OFFSET_PREFIX.length()));
				} else if (arg.startsWith(HDG_OFFSET_PREFIX)) {
					hdgOffset = Double.parseDouble(arg.substring(HDG_OFFSET_PREFIX.length()));
				} else if (arg.startsWith(AWS_COEFF_PREFIX)) {
					awsCoeff = Double.parseDouble(arg.substring(AWS_COEFF_PREFIX.length()));
				} else if (arg.startsWith(BSP_COEFF_PREFIX)) {
					bspCoeff = Double.parseDouble(arg.substring(BSP_COEFF_PREFIX.length()));
				} else if (arg.startsWith(MAX_LEEWAY_PREFIX)) {
					maxLeeway = Double.parseDouble(arg.substring(MAX_LEEWAY_PREFIX.length()));
				} else if (arg.startsWith(CC_BUFFER_LENGTH_PREFIX)) {
					currentComputerBufferLength = Long.parseLong(arg.substring(CC_BUFFER_LENGTH_PREFIX.length()));
				}
			}
		}
		if (fileName == null) {
			throw new IllegalArgumentException("Please provide at least the name of the file to process.");
		}

		if (currentComputerBufferLength != null) {
			currentComputer = new CurrentComputer(currentComputerBufferLength);
		}

		try {
			String inputFileName = fileName;
			String outputFileName = inputFileName + ".json";
			NMEAtoJSONPosPlus.transform(inputFileName,
										archiveName,
										defaultDeclinationValue,
										deviationCurveFile,
										polarFileName,
										outputFileName);
			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// Summary
		map.keySet().forEach(key -> System.out.printf("%s: %d records\n", key, map.get(key)));
	}
}

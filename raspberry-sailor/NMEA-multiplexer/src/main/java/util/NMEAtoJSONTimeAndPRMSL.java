package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import nmea.parser.RMC;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An example of a custom log-file analyzer.
 * Spits out RMC Time and PRMSL (from XDR) in an array of json objects
 */
public class NMEAtoJSONTimeAndPRMSL {
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static {
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	private final static ObjectMapper mapper = new ObjectMapper();

	private final static Map<String, Integer> map = new HashMap<>();

	enum DataType {
		RAW_DATA,
		FOR_DISPLAY_GRAPH_WEB_COMPONENT
	}
	private static void transform(String fileInName,
								  String fileOutName) throws Exception {
		transform(fileInName, fileOutName, DataType.RAW_DATA);
	}

	private static void transform(String fileInName,
	                              String fileOutName,
								  DataType graphType) throws Exception {

		List<Object> jsonArray = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(fileInName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutName));

		String line = "";
		double prmsl = 0d;

		final long FIFTEEN_MINUTES = (15 * 60 * 1_000); // in ms
		long previousDateTime = 0L;
		List<String> xList = null; // Dates, Duration fmt.
		List<Double> yList = null; // Pressure values. hPa.

		if (graphType.equals(DataType.FOR_DISPLAY_GRAPH_WEB_COMPONENT)) {
			xList = new ArrayList<>();
			yList = new ArrayList<>();
		}

		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.startsWith("$") && line.length() > 6) {
					String prefix = line.substring(3, 6);
					Integer nb = map.get(prefix);
					map.put(prefix, (nb == null) ? (1) : (nb + 1));
					// Specific
					if ("RMC".equals(prefix)) {
						if (StringParsers.validCheckSum(line)) {
							RMC rmc = StringParsers.parseRMC(line);
							if (rmc != null && rmc.getRmcTime() != null && rmc.isValid()) {
								Map<String, Object> mapToLog = new HashMap<>();
								if (graphType.equals(DataType.RAW_DATA)) {
									mapToLog.put("raw-rmc-time", rmc.getRmcTime().getTime());
									mapToLog.put("rmc-time", DURATION_FMT.format(rmc.getRmcTime()));
									mapToLog.put("prmsl", prmsl);
									jsonArray.add(mapToLog);
								} else if (graphType.equals(DataType.FOR_DISPLAY_GRAPH_WEB_COMPONENT)) {
									// One log every 15 minutes
									if (rmc.getRmcTime().getTime() - previousDateTime > FIFTEEN_MINUTES) {
										// System.out.printf("Logging pressure at %s\n", DURATION_FMT.format(rmc.getRmcTime()));
										xList.add(DURATION_FMT.format(rmc.getRmcTime()));
										yList.add(prmsl / 100L);
										previousDateTime = rmc.getRmcTime().getTime();
									}
								}
							}
						}
					} else if ("XDR".equals(prefix)) {
						if (StringParsers.validCheckSum(line)) {
							final List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(line);
							final Optional<StringGenerator.XDRElement> firstPrmsl = xdrElements.stream()
									.filter(el -> el.getTypeNunit().equals(StringGenerator.XDRTypes.PRESSURE_P))
									.findFirst();
							if (firstPrmsl.isPresent()) {
								prmsl = firstPrmsl.get().getValue();
							}
						}
					}
				}
			}
		}
		br.close();

		Object jsonData = jsonArray;

		if (graphType.equals(DataType.FOR_DISPLAY_GRAPH_WEB_COMPONENT)) {
			// Optional: Shrink data ?
			if (false) {
				int MAX_CARD = 1_000;
				xList.subList(0, xList.size() - MAX_CARD).clear();
				yList.subList(0, yList.size() - MAX_CARD).clear();
			}
			// jsonData = Map.of("x", xList, "y", yList);  // Java 9+
			Map<String, Object> xmMap = new HashMap<>();
			xmMap.put("x", xList);
			xmMap.put("y", yList);
			jsonData = xmMap;
		}
		if ("true".equals(System.getProperty("verbose"))) {
			System.out.println(mapper.writeValueAsString(jsonArray));
		}
		boolean minified = "true".equals(System.getProperty("minified", "true"));
		if (minified) {
			bw.write(mapper.writeValueAsString(jsonData));
		} else {
			bw.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData));
		}
		bw.close();
		if (graphType.equals(DataType.RAW_DATA)) {
			System.out.printf("Written %s element(s).\n", NumberFormat.getInstance().format(jsonArray.size()));
		} else {
			System.out.printf("Written %s element(s).\n", NumberFormat.getInstance().format(xList.size()));
		}
	}

	public static void main(String... args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Please provide the name of the file to process as first and only parameter");
		}

		try {
			String inputFileName = args[0];
			String outputFileName = inputFileName + ".json";
			DataType typeOption = DataType.FOR_DISPLAY_GRAPH_WEB_COMPONENT;
			NMEAtoJSONTimeAndPRMSL.transform(inputFileName, outputFileName, typeOption);
			System.out.printf("\nGenerated file %s is ready (option %s).\n", outputFileName, typeOption);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		map.keySet().forEach(key -> System.out.printf("%s: %s records\n", key, NumberFormat.getInstance().format(map.get(key))));
	}
}

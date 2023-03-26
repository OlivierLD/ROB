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

	private static void transform(String fileInName,
	                              String fileOutName) throws Exception {

		List<Object> jsonArray = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(fileInName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutName));

		String line = "";
		double prmsl = 0d;

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
								mapToLog.put("raw-rmc-time", rmc.getRmcTime().getTime());
								mapToLog.put("rmc-time", DURATION_FMT.format(rmc.getRmcTime()));
								mapToLog.put("prmsl", prmsl);
								jsonArray.add(mapToLog);
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
		if ("true".equals(System.getProperty("verbose"))) {
			System.out.println(mapper.writeValueAsString(jsonArray));
		}
		boolean minified = "true".equals(System.getProperty("minified", "true"));
		if (minified) {
			bw.write(mapper.writeValueAsString(jsonArray));
		} else {
			bw.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonArray));
		}

		bw.close();
		System.out.printf("Written %s element(s).\n", NumberFormat.getInstance().format(jsonArray.size()));
	}

	public static void main(String... args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first and only parameter");
		}

		try {
			String inputFileName = args[0];
			String outputFileName = inputFileName + ".json";
			NMEAtoJSONTimeAndPRMSL.transform(inputFileName, outputFileName);
			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		map.keySet().forEach(key -> System.out.printf("%s: %s records\n", key, NumberFormat.getInstance().format(map.get(key))));
	}
}

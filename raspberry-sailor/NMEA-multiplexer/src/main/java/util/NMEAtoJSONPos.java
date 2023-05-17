package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JSON positions generator, from NMEA log.
 * Optional SOG and COG (and others if needed...)
 * LeafLet supports this format, like
 * let latlngs = [
 *         [45.51, -122.68],
 *         [37.77, -122.43],
 *         [34.04, -118.2]
 *     ];
 */
public class NMEAtoJSONPos {

	private final static SimpleDateFormat SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
	static {
		SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	}
	private static class ObjectToLog extends GeoPos {
		private double sog;
		private double cog;
		private String rmcDate;

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
	}
	private final static ObjectMapper mapper = new ObjectMapper();
	private final static Map<String, Integer> map = new HashMap<>();

	private static void transform(String fileInName,
	                              String fileOutName) throws Exception {

		List<Object> jsonArray = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(fileInName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutName));

		boolean withOG = "true".equals(System.getProperty("with.og", "false"));

		String line = "";

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
								if (withOG) {
									ObjectToLog otl = new ObjectToLog(rmc.getGp().lat, rmc.getGp().lng);
									otl.setRmcDate(SDF_UTC.format(rmc.getRmcDate()));
									otl.setCog(rmc.getCog());
									otl.setSog(rmc.getSog());
									jsonArray.add(otl);
								} else {
									jsonArray.add(rmc.getGp());
								}
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
	}

	public static void main(String... args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first parameter");
		}

		try {
			String inputFileName = args[0];
			String outputFileName = inputFileName + ".json";
			NMEAtoJSONPos.transform(inputFileName, outputFileName);
			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		map.keySet().forEach(key -> System.out.printf("%s: %d records\n", key, map.get(key)));
	}
}

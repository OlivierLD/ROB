package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JSON positions generator, from NMEA log.
 * Optional SOG and COG (and others if needed...). See system variable "with.og"
 * LeafLet supports this format, like
 * let latlngs = [
 *         [45.51, -122.68],
 *         [37.77, -122.43],
 *         [34.04, -118.2]
 *     ];
 *  ----------------
 *  Do see the System Variable "sorted.data" below
 *  System variable "verbose"
 *  System variable "minified"
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
		BufferedWriter bw = null;
		if (fileOutName != null) {
			bw = new BufferedWriter(new FileWriter(fileOutName));
		}

		// With Over Ground data
		boolean withOG = "true".equals(System.getProperty("with.og", "false"));

		// 1 - Build a list of RMC
		List<RMC> rmcList = new ArrayList<>();
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
								rmcList.add(rmc);
							}
						}
					}
				}
			}
		}
		br.close();
		// 2 - sort by date ?
		if ("true".equals(System.getProperty("sorted.data", "true"))) {
			Collections.sort(rmcList, (rmc1, rmc2) -> (int) (rmc1.getRmcDate().getTime() - rmc2.getRmcDate().getTime()));
		}
		// 3 - JSON from sorted list
		rmcList.forEach(rmc -> {
			if (withOG) {
				ObjectToLog otl = new ObjectToLog(rmc.getGp().lat, rmc.getGp().lng);
				otl.setRmcDate(SDF_UTC.format(rmc.getRmcDate()));
				otl.setCog(rmc.getCog());
				otl.setSog(rmc.getSog());
				jsonArray.add(otl);
			} else {
				jsonArray.add(rmc.getGp());
			}
		});

		// offset, limit. "limit" is the final length of the buffer
		int offset = Integer.parseInt(System.getProperty("offset", "0"));
		int limit = Integer.parseInt(System.getProperty("limit", "-1"));
		if (offset > 0) {
			System.out.printf("Managing offset %d\n", offset);
			for (int i=0; i<offset; i++) {
				jsonArray.remove(0);
				if (jsonArray.size() == 0) {
					System.out.println("offset: No record left in data array !!");
					break;
				}
			}
		}
		if (limit != -1) {
			System.out.printf("Managing limit %d\n", limit);
			while (jsonArray.size() > limit) {
				jsonArray.remove(jsonArray.size() - 1);
			}
		}

		if ("true".equals(System.getProperty("verbose"))) {
			System.out.println(mapper.writeValueAsString(jsonArray));
		}
		boolean minified = "true".equals(System.getProperty("minified", "true"));
		// Cleanup: Remove latInDegMinDec nd lngInDegMinDec, to make the json smaller.
		final JsonNode jsonNode = mapper.valueToTree(jsonArray);
		for (JsonNode node : jsonNode) {
			((ObjectNode)node).remove("latInDegMinDec");
			((ObjectNode)node).remove("lngInDegMinDec");
		}

		if (minified) {
			if (bw != null) {
				bw.write(mapper.writeValueAsString(jsonNode));
			} else {
				System.out.println(mapper.writeValueAsString(jsonNode));
			}
		} else {
			if (bw != null) {
				bw.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
			} else {
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
			}
		}
		if (bw != null) {
			bw.close();
		}
	}

	public static void main(String... args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first parameter");
		}

		String inputFileName = args[0];
		String outputFileName = inputFileName + ".json";

		String userOutput = System.getProperty("output-file");
		if (userOutput != null) {
			outputFileName = userOutput;
		}

		try {
			NMEAtoJSONPos.transform(inputFileName, outputFileName);
			if (outputFileName != null) {
				System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (outputFileName != null) {
			map.keySet().forEach(key -> System.out.printf("%s: %d records\n", key, map.get(key)));
		}
	}
}

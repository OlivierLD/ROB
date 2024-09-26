package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import context.NMEADataCache;
import nmea.parser.Border;
import nmea.parser.Marker;
import nmea.utils.NMEAUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turn Markers and Borders yaml definition into JSON,
 * for validation in a browser with the ChartlessMap WebComponent.
 *
 * See chartless.markers.viewer.html
 */
public class MarkersToJSON {
	private final static ObjectMapper mapper = new ObjectMapper();

	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Please provide the name of the yaml file to analyze as first parameter");
		}

		try {
			String inputFileName = args[0]; // The name of the yaml file

			final List<Marker> markers = NMEAUtils.loadMarkers(inputFileName);
			final List<Border> borders = NMEAUtils.loadBorders(inputFileName);

			String outputFileName = inputFileName + ".json";

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));

			Map<String, Object> cache = new HashMap<>();

			cache.put(NMEADataCache.MARKERS_DATA, markers);
			cache.put(NMEADataCache.BORDERS_DATA, borders);

			bw.write(mapper.writeValueAsString(cache));

			bw.close();

			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

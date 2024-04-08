package util;

import nmea.parser.Border;
import nmea.parser.Marker;
import nmea.utils.NMEAUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Turn Markers and Borders yaml definition into GPX,
 * for validation in OpenCPN...
 * Several hard-coded options for the markers in OpenCPN.
 *
 * This will generate a gpx file that can be later imported into OpenCPN.
 */
public class MarkersToGPX {
	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Please provide the name of the yaml file to analyze as first parameter");
		}

		try {
			String inputFileName = args[0]; // The name of the yaml file

			final List<Marker> markers = NMEAUtils.loadMarkers(inputFileName);
			final List<Border> borders = NMEAUtils.loadBorders(inputFileName);

			String outputFileName = inputFileName + ".gpx";

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
			// GPX start
			bw.write("<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
					"<gpx version=\"1.1\" " +
					"     creator=\"OpenCPN\" " +
					"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
					"     xmlns=\"http://www.topografix.com/GPX/1/1\" " +
					"     xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" " +
					"     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd " +
					"                          http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd\" " +
					"     xmlns:opencpn=\"http://www.opencpn.org\">\n");

			// Markers
			markers.forEach(marker -> {
				try {
					bw.write(String.format("<wpt lat=\"%f\" lon=\"%f\">\n" +
							// "    <time>2022-07-13T08:55:16Z</time>\n" +
							"    <name>%s</name>\n" +
							"    <desc>%s</desc>\n" +
							"    <sym>1st-Diamond</sym>\n" +
							"    <type>WPT</type>\n" +
							"    <extensions>\n" +
							"      <opencpn:guid>06434faf-5a49-4511-875c-17f339ea5602</opencpn:guid>\n" +
							"      <opencpn:viz_name>1</opencpn:viz_name>\n" +
							"      <opencpn:arrival_radius>0.050</opencpn:arrival_radius>\n" +
							"      <opencpn:waypoint_range_rings visible=\"false\" number=\"0\" step=\"1\" units=\"0\" colour=\"#FF0000\" />\n" +
							"      <opencpn:scale_min_max UseScale=\"false\" ScaleMin=\"2147483646\" ScaleMax=\"0\" />\n" +
							"    </extensions>\n" +
							"  </wpt>\n",
							marker.getLatitude(),
							marker.getLongitude(),
							marker.getLabel(),
							marker.getType()));
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			});
			// Borders
			borders.forEach(border -> {
				try {
					bw.write(String.format("<trk>\n" +
							"    <name>%s</name>\n" +
							"    <desc>%s</desc>\n" +
							"    <extensions>\n" +
							"      <opencpn:guid>21180000-44ac-4218-a090-ed331f980000</opencpn:guid>\n" +
							"      <opencpn:viz>1</opencpn:viz>\n" +
							"      <opencpn:start> - </opencpn:start>\n" +
							"      <opencpn:end> - </opencpn:end>\n" +
							"      <gpxx:TrackExtension>\n" +
							"        <gpxx:DisplayColor>Black</gpxx:DisplayColor>\n" +
							"      </gpxx:TrackExtension>\n" +
							"    </extensions>",
							border.getBorderName(),
							border.getBorderName()));
					// Points
					bw.write("<trkseg>\n");
					border.getMarkerList().forEach(marker -> {
						try {
							bw.write(String.format("<trkpt lat=\"%f\" lon=\"%f\">\n" +
									"        <time></time>\n" +
									"      </trkpt>\n",
									marker.getLatitude(),
									marker.getLongitude()));
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					});
					// Open ? If closed, marker list has closed the loop.
					bw.write("</trkseg>\n");

					bw.write("</trk>\n");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			});

			bw.write("</gpx>\n"); // The end !
			bw.close();

			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

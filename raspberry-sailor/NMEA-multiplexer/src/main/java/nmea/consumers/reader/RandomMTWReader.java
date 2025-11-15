package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringParsers;
import utils.StringUtils;

import java.util.List;

/**
 * Generates random temperature sentence.
 * For debugging.
 */
public class RandomMTWReader extends NMEAReader {

	// Initial values. Will be "randomly" modified below.
	double randomWTValue = 10d; // Water Temp, in Celsius
	double randomATValue = 20d; // Air Temp, in Celsius
	double randomPRMSLValue = 1013.25d; // That one in millibars. Will be used as bars.
	double randomSalinityValue = 23.45d; // in ‰ (parts per thousand), \u2030, &permil;


	public RandomMTWReader(List<NMEAListener> al) {
		this(null, al);
	}
	public RandomMTWReader(String threadName, List<NMEAListener> al) {
		super(threadName, al);
	}

	@Override
	public void startReader() {
		super.enableReading();
		while (this.canRead()) {
			// Read data every 1 second
			try {
				// valueDiff in [-0.1..0.1]
				double valueDiff = (Math.random() - 0.5) * 0.2;
				randomWTValue += valueDiff;
				valueDiff = (Math.random() - 0.5) * 0.2;
				randomATValue += valueDiff;
				valueDiff = (Math.random() - 0.5) * 0.2;
				randomPRMSLValue += valueDiff;
				valueDiff = (Math.random() - 0.5) * 0.2;
				randomSalinityValue += valueDiff;

				// Generate NMEA String (MTW)
				String customString = generateSentence("AE", "MTW", String.format("%.01f,C", randomWTValue)) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));

				// More strings ? Like XDR, MMB, MTA, MDA...
				String tsgSensorName = "TSG"; // ThermoSalinoGraphe
				String ctdSensorName = "CTD"; // Conductivity Temperature Depth
				double salinity = randomSalinityValue;
				String stringContent = "C," + String.format("%.01f", randomWTValue) +
							",C," + ctdSensorName +
							",L," + String.format("%.02f", salinity) + ",S," + tsgSensorName;
				customString = generateSentence("AE", "XDR", stringContent) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));

				customString = generateSentence("AE", "MTA", String.format("%.01f,C", randomATValue)) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));

				double pressure = randomPRMSLValue / 1_000d;
				stringContent =
						String.format("%.04f", pressure / 33.8639) + ",I," +  // 1-Pressure in inches
						String.format("%.04f", pressure) + ",B," +            // 3-Pressure in Bars
						String.format("%.01f", randomATValue) + ",C," +       // 5-Air Temp in Celsius
						",,,,,,,,,,,,,,,";                                    // The rest is empty for now
				customString = generateSentence("AE", "MDA", stringContent) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));

				stringContent =	String.format("%.04f", pressure / 33.8639) + ",I," + String.format("%.04f", pressure) + ",B";
				customString = generateSentence("AE", "MMB", stringContent) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1_000L); // TODO Make this a parameter?
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	// Custom methods
	public static String generateSentence(String devicePrefix, String id, String value) {
		String custom = devicePrefix + id + "," + value;
		// Checksum
		int cs = StringParsers.calculateCheckSum(custom);
		custom += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
		return "$" + custom;
	}

	@Override
	public void closeReader() throws Exception {
	}
}
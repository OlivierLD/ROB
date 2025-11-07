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

	double randomValue = 10d;

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

				double valueDiff = (Math.random() - 0.5) * 0.2; // [-0.1..0.1]
				randomValue += valueDiff;

				// Generate NMEA String // TODO Fix this
				String customString = generateSentence("AE", "MTW", String.format("%.01f,C", randomValue)) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1_000L); // TODO Make this a parameter
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
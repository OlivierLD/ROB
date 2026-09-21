package nmea.consumers.reader;

import nmea.api.*;
import nmea.parser.StringParsers;

import java.util.List;
import utils.StringUtils;

/**
 * Generates random crap.
 * For debugging.
 */
public class RandomReader extends NMEAReader {

	public RandomReader(NMEAClient nmeaClient, List<NMEAListener> al) {
		this(nmeaClient, null, al);
	}
	public RandomReader(NMEAClient nmeaClient, String threadName, List<NMEAListener> al) {
		super(nmeaClient, threadName, al);
	}

	@Override
	public void startReader() {
		super.enableReading();
		while (this.canRead()) {
			// Read data every 1 second
			try {
				if (this.getNMEAClient().isActive()) {
					// Generate NMEA String
					String customString = generateSentence("AA", "RND", Double.toString(Math.random())) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
					fireDataRead(new NMEAEvent(this, customString));
				} else {
					// Honk
				}
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
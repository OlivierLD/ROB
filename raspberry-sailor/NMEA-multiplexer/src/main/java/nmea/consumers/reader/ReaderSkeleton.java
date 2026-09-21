package nmea.consumers.reader;

import nmea.api.*;
import nmea.parser.StringParsers;

import java.util.List;
import utils.StringUtils;

/**
 * Reader Skeleton
 */
public class ReaderSkeleton extends NMEAReader {

	public ReaderSkeleton(NMEAClient nmeaClient, List<NMEAListener> al) {
		this(nmeaClient, null, al);
	}
	public ReaderSkeleton(NMEAClient nmeaClient, String threadName, List<NMEAListener> al) {
		super(nmeaClient, threadName, al);
	}

	@Override
	public void startReader() {
		super.enableReading();
		while (this.canRead()) {
			// Read/Generate data every 1 second
			try {
				// Generate NMEA String
				String customString = generateSentence("SK", "XXX", Double.toString(Math.random())) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
				fireDataRead(new NMEAEvent(this, customString));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1_000L);
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
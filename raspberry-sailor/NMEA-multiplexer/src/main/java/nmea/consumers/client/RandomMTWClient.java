package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.RandomMTWReader;
import nmea.consumers.reader.RandomReader;

/**
 * Generates random  MTW sentence, in a valid NMEA Sentence.
 */
public class RandomMTWClient extends NMEAClient {
	public RandomMTWClient() {
		this(null, null, null);
	}

	public RandomMTWClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public RandomMTWClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public RandomMTWClient(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("rnd.mtw.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from MTW-RND:" + e.getContent());
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
		}
	}

	private static RandomMTWClient nmeaClient = null;

	public static class RandomMTWBean implements ClientBean {
		private String cls;
		private final String type = "rnd-mtw";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose = false;

		public RandomMTWBean() { // for Jackson
		}

		public RandomMTWBean(RandomMTWClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
		}

		public String getCls() {
			return cls;
		}

		public boolean isVerbose() {
			return verbose;
		}


		@Override
		public String getType() {
			return this.type;
		}

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
	}

	@Override
	public Object getBean() {
		return new RandomMTWBean(this);
	}

	// For tests. TODO Externalize in the test section.
	public static void main(String... args) {
		System.out.println("RandomMTWClient invoked with " + args.length + " Parameter(s).");

		System.setProperty("rnd.mtw.data.verbose", "true");

		for (String s : args) {
			System.out.println("RandomMTWClient prm:" + s);
		}
		nmeaClient = new RandomMTWClient();

		Runtime.getRuntime().addShutdownHook(new Thread("RandomClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new RandomMTWReader("RndMTWReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
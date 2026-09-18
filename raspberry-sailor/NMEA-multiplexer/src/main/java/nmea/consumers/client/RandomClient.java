package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.RandomReader;

/**
 * Generates random numbers, in a valid NMEA Sentence.
 */
public class RandomClient extends NMEAClient {
	public RandomClient() {
		this(null, null, null, "");
	}

	public RandomClient(Multiplexer mux) {
		this(null, null, mux, "");
	}

	public RandomClient(String[] s, String[] sa) {
		this(s, sa, null, "");
	}

	public RandomClient(String[] s, String[] sa, Multiplexer mux, String desc) {
		super(s, sa, mux, desc);
		this.verbose = "true".equals(System.getProperty("rnd.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from RND:" + e.getContent());
		}
		if (multiplexer != null) {
			if (this.isActive()) {
				multiplexer.onData(e.getContent()); // TODO Manage filters !!
			}
		}
	}

	private static RandomClient nmeaClient = null;

	public static class RandomBean implements ClientBean {
		private String cls;
		private final String type = "rnd";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose = false;
		private boolean active = true;
		private String description = "";

		public RandomBean() { // for Jackson
		}

		public RandomBean(RandomClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			active = instance.isActive();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			description = instance.getDescription();
		}

		public String getCls() {
			return cls;
		}

//		public boolean isVerbose() {
//			return verbose;
//		}

		@Override
		public String getType() {
			return this.type;
		}

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}
		@Override
		public boolean isActive() {
			return this.active;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };

		@Override
		public String getDescription() {
			return description;
		}
	}

	@Override
	public Object getBean() {
		return new RandomBean(this);
	}

	// For tests. TODO Externalize in the test section.
	public static void main(String... args) {
		System.out.println("RandomClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("RandomClient prm:" + s);
		}
		nmeaClient = new RandomClient();

		Runtime.getRuntime().addShutdownHook(new Thread("RandomClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new RandomReader("RndReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
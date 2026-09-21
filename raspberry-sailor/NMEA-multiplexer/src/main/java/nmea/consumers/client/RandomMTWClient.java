package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.RandomMTWReader;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;

/**
 * Generates random MTW sentence, in a valid NMEA Sentence.
 * MTW: Water Temperature
 */
public class RandomMTWClient extends NMEAClient {
	public RandomMTWClient() {
		this(null, null, null, "");
	}

	public RandomMTWClient(Multiplexer mux) {
		this(null, null, mux, "");
	}

	public RandomMTWClient(String[] s, String[] sa) {
		this(s, sa, null, "");
	}

	public RandomMTWClient(String[] s, String[] sa, Multiplexer mux, String desc) {
		super(s, sa, mux, desc);
		this.verbose = "true".equals(System.getProperty("rnd.mtw.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received (generated) from MTW-RND:" + e.getContent());
		}
		if (multiplexer != null) {
			if (this.isActive()) {
				if (verbose) {
					System.out.printf("***\tRandomMTWClient.dataDetectedEvent: [%s]\n", e.getContent());
				}
				// Filters work on that one, as we have here a valid NMEA string...
				boolean ok = MuxNMEAUtils.goesThruFilters(e.getContent(),
						this.getSentenceFilters() == null ? null : Arrays.asList(this.getSentenceFilters()),
						this.getDeviceFilters() == null ? null : Arrays.asList(this.getDeviceFilters()),
						verbose);
				if (ok) {
					if (verbose) {
						System.out.printf("***\tInvoking multiplexer.onData for [%s]\n", e.getContent());
					}
					multiplexer.onData(e.getContent());
				} else {
					if (verbose) {
						System.out.printf("**\t[%s] does not go thru filters\n", e.getContent());
					}
				}
			} else {
				if (verbose) {
					System.out.println("Client INACTIVE, data not sent.");
				}
			}
		}
	}

	private static RandomMTWClient nmeaClient = null;

	public static class RandomMTWBean implements ClientBean {
		private String cls;
		private final String type = "rnd-mtw";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose = false;
		private boolean active = false;
		private String description; // = "";

		public RandomMTWBean() { // for Jackson
		}

		public RandomMTWBean(RandomMTWClient instance) {
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
		public boolean isActive() {
			return this.active;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
		@Override
		public String getDescription() { return this.description; }
	}

	@Override
	public ClientBean getBean() {
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
		nmeaClient.setReader(new RandomMTWReader(nmeaClient, "RndMTWReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
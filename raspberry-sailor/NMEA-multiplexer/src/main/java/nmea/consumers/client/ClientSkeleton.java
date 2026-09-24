package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.ReaderSkeleton;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;

/**
 * Skeleton for your own dynamically loaded Consumer (aka Channel)
 */
public class ClientSkeleton extends NMEAClient {
	public ClientSkeleton() {
		this(null, null, null, false, true, "");
	}

	public ClientSkeleton(Multiplexer mux) {
		this(null, null, mux, false, true, "");
	}

	public ClientSkeleton(String[] s, String[] sa) {
		this(s, sa, null, false, true, "");
	}

	public ClientSkeleton(String[] s, String[] sa, Multiplexer mux, boolean verbose, boolean active, String description) {
		super(s, sa, mux, description);
		this.setVerbose(verbose); // this.verbose = "true".equals(System.getProperty("skeleton.verbose", "false"));
		this.setActive(active);
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from Skeleton:" + e.getContent());
		}
		if (multiplexer != null) {
			if (this.isActive()) {
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
						System.out.printf("**\t[%s] does NOT go thru filters (%s, %s)\n", e.getContent(),
								this.getSentenceFilters() == null ? null : Arrays.asList(this.getSentenceFilters()),
								this.getDeviceFilters() == null ? null : Arrays.asList(this.getDeviceFilters()));
					}
				}
				// multiplexer.onData(e.getContent()); // Manage filters !!
			}
		}
	}

	private static ClientSkeleton nmeaClient = null;

	public static class SkeletonBean implements ClientBean {
		private String cls;
		private final String type = "skeleton";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;
		private boolean active;
		private String description = "";

		public SkeletonBean() {}

		public SkeletonBean(ClientSkeleton instance) {
			cls = instance.getClass().getName();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			verbose = instance.isVerbose();
			active = instance.isActive();
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
		public String getDescription() {
			return description;
		}
	}

	@Override
	public ClientBean getBean() {
		return new SkeletonBean(this);
	}

	public static void main(String... args) {
		System.out.println("ClientSkeleton invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("ClientSkeleton prm:" + s);
		}
		nmeaClient = new ClientSkeleton();

		Runtime.getRuntime().addShutdownHook(new Thread("ClientSkeleton shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new ReaderSkeleton(nmeaClient, "ReaderSkeleton", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAReader;
import nmea.consumers.reader.ZDAReader;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;

/**
 * Generates ZDA numbers, in a valid NMEA Sentence.
 */
public class ZDAClient extends NMEAClient {
	public ZDAClient() {
		this(null, null, null, true, false, "");
	}

	public ZDAClient(Multiplexer mux) {
		this(null, null, mux, true, false, "");
	}

	public ZDAClient(String[] s, String[] sa) {
		this(s, sa, null, true, false, "");
	}

	public ZDAClient(String[] s, String[] sa, Multiplexer mux, boolean active, boolean verbose, String desc) {
		super(s, sa, mux, desc);
		// this.verbose = "true".equals(System.getProperty("zda.data.verbose", "false"));
		this.setActive(active);
		this.setVerbose(verbose);
	}

	public String getSpecificDevicePrefix() {
		String dp = "";
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof ZDAReader) {
			dp = ((ZDAReader)reader).getDevicePrefix();
		}
		return dp;
	}

	public void setSpecificDevicePrefix(String dp) {
		NMEAReader reader = this.getReader();
		if (reader != null && reader instanceof ZDAReader) {
			((ZDAReader)reader).setDevicePrefix(dp);
		}
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Generated from ZDA:" + e.getContent());
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
			}
		}
	}

	private static ZDAClient nmeaClient = null;

	public static class ZDABean implements ClientBean {
		private String cls;
		private final String type = "zda";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private String devicePrefix;
		private boolean verbose;
		private boolean active;
		private String description = "";

		public String getCls() {
			return cls;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public ZDABean() {}
		public ZDABean(ZDAClient instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			active = instance.isActive();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			devicePrefix = instance.getSpecificDevicePrefix();
			description = instance.getDescription();
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

		public String getDevicePrefix() { return this.devicePrefix; }
	}

	@Override
	public ClientBean getBean() {
		return new ZDABean(this);
	}

	public static void main(String... args) {
		System.out.println("ZDAClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("ZDAClient prm:" + s);
		}
		nmeaClient = new ZDAClient();

		Runtime.getRuntime().addShutdownHook(new Thread("ZDAClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
		nmeaClient.setReader(new ZDAReader(nmeaClient,"ZDAReader", nmeaClient.getListeners()));
		nmeaClient.startWorking();
	}
}
package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.UDPReader;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;
import java.util.Properties;

/**
 * WiP... See if OpenCPN is happy
 * Receives data through UDP channel.
 */
public class UDPServer extends NMEAClient {

	private final static String DEFAULT_HOST = "127.0.0.1"; // "230.0.0.1"
	private String hostName = DEFAULT_HOST;

	private int port;

	public UDPServer() {
		this(null, null, null, "localhost", 8001, true, false, "");
	}

	public UDPServer(Multiplexer mux) {
		this(null, null, mux, "localhost", 8001, true, false, "");
	}

	public UDPServer(String[] s, String[] sa) {
		this(s, sa, null, "localhost", 8001, true, false, "");
	}

	public UDPServer(String[] s, String[] sa, Multiplexer mux, String hostname, int port, boolean active, boolean verbose, String desc) {
		super(s, sa, mux, desc);
		this.setHostName(hostname);
		this.setPort(port);
		// this.verbose = "true".equals(System.getProperty("udp.data.verbose", "false"));
		this.setActive(active);
		this.setVerbose(verbose);
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from UDP :" + e.getContent());
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

	private static UDPServer nmeaClient = null;

	public static class UDPBean implements ClientBean {
		private String cls;
		private final String type = "udp";
		private int port;
		private String hostname;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;
		private boolean active;
		private String description = "";

		public String getCls() {
			return cls;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public UDPBean() {}
		public UDPBean(UDPClient instance) {
			cls = instance.getClass().getName();
			port = ((UDPReader) instance.getReader()).getPort();
			hostname = ((UDPReader) instance.getReader()).getHostname();
			verbose = instance.isVerbose();
			active = instance.isActive();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			description = instance.getDescription();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public int getPort() {
			return port;
		}

		public String getHostname() {
			return this.hostname;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
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
		return new UDPBean();
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * For standalone tests
	 * @param args Unused
	 */
	public static void main(String... args) {
		System.out.println("CustomUDPServer invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("CustomUDPServer prm:" + s);
		}
		String serverName = "localhost"; // "192.168.42.2";
		int serverPort = 8_002; // 7_001;

		System.setProperty("nmea.parser.verbose", "true");
		System.setProperty("udp.data.verbose", "true");

		nmeaClient = new UDPServer();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down nicely.");
			nmeaClient.stopDataRead();
		}, "CustomUDPServer shutdown hook"));
		nmeaClient.initClient();
		nmeaClient.setReader(new UDPReader(nmeaClient, "UDPReader", nmeaClient.getListeners(), serverName, serverPort));
		nmeaClient.startWorking();
	}
}
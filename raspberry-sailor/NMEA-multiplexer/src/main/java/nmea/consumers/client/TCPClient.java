package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.TCPReader;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;

/**
 * Read NMEA Data from a TCP server
 */
public class TCPClient extends NMEAClient {
	private String hostName;
	private int port;
	private String initialRequest;
	private boolean keepTrying;

	public TCPClient() {
		this(null, null, null, "localhost", 7001, null, false, "");
	}

	public TCPClient(Multiplexer mux) {
		this(null, null, mux, "localhost", 7001, null, false, "");
	}
	public TCPClient(String[] s, String[] sa) {
		this(s, sa, null, "localhost", 7001, null, false, "");
	}

	// TODO Active, verbose, filters...
	public TCPClient(String[] s, String[] sa, Multiplexer mux, String hostName, int tcpPort, String initialRequest, boolean keepTrying, String desc) {
		super(s, sa, mux, desc);

		System.out.printf("new TCPClient on [%s:%d]\n", hostName, tcpPort);

		this.verbose = "true".equals(System.getProperty("tcp.data.verbose", "false"));
		this.hostName = hostName;
		this.port = tcpPort;
		this.keepTrying = keepTrying;
		this.initialRequest = initialRequest;
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from TCP :" + e.getContent());
		}
		if (multiplexer != null) {
			if (this.isActive()) {
				boolean ok = MuxNMEAUtils.goesThruFilters(e.getContent(),
						this.getSentenceFilters() == null ? null : Arrays.asList(this.getSentenceFilters()),
						this.getDeviceFilters() == null ? null : Arrays.asList(this.getDeviceFilters()),
						verbose);
				if (ok) {
					if (true || verbose) {
						System.out.printf("***\tInvoking multiplexer.onData for [%s]\n", e.getContent());
					}
					multiplexer.onData(e.getContent());
				} else {
					if (true || verbose) {
						System.out.printf("**\t[%s] does NOT go thru filters (%s, %s)\n", e.getContent(),
								this.getSentenceFilters() == null ? null : Arrays.asList(this.getSentenceFilters()),
								this.getDeviceFilters() == null ? null : Arrays.asList(this.getDeviceFilters()));
					}
				}
				// multiplexer.onData(e.getContent()); // TODO Manage filters ??!!
			}
		}
	}

	private static TCPClient nmeaClient = null;

	public static class TCPBean implements ClientBean {
		private String cls;
		private final String type = "tcp";
		private int port;
		private String hostname;
		private String initialRequest;
		private boolean keepTrying;
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

		public TCPBean() {}
		public TCPBean(TCPClient instance) {
			cls = instance.getClass().getName();
			port = ((TCPReader) instance.getReader()).getPort();
			hostname = ((TCPReader) instance.getReader()).getHostname();
			initialRequest = ((TCPReader) instance.getReader()).getInitialRequest();
			keepTrying = ((TCPReader) instance.getReader()).isKeepTrying();
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


		public String getInitialRequest() {
			return initialRequest;
		}

		public boolean isKeepTrying() {
			return keepTrying;
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
		return new TCPBean(this);
	}

	/**
	 * For standalone tests
	 * @param args Unused
	 */
	public static void main(String... args) {
		System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("CustomTCPClient prm:" + s);
		}
//		String serverName = "sinagot.net"; // "192.168.42.2";
//		int serverPort = 2_947; // 7_001;

		String serverName = "localhost"; // "192.168.42.2";
		int serverPort = 7002; // 7_001;

		System.setProperty("nmea.parser.verbose", "true");

		nmeaClient = new TCPClient();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down nicely.");
			nmeaClient.stopDataRead();
		}, "TCPClient shutdown hook"));
		nmeaClient.initClient();
		nmeaClient.setReader(new TCPReader(nmeaClient, "TCPReader", nmeaClient.getListeners(), serverName, serverPort));
		nmeaClient.startWorking();
	}
}
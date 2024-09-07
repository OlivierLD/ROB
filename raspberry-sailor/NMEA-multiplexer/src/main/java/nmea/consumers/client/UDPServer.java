package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.UDPReader;
import java.util.Properties;

/**
 * WiP... See if OpenCPN is happy
 */
public class UDPServer extends NMEAClient {

	private final static String DEFAULT_HOST = "127.0.0.1"; // "230.0.0.1"
	private String hostName = DEFAULT_HOST;

	public UDPServer() {
		this(null, null, null);
	}

	public UDPServer(Multiplexer mux) {
		this(null, null, mux);
	}

	public UDPServer(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public UDPServer(String[] s, String[] sa, Multiplexer mux) {
		super(s, sa, mux);
		this.verbose = "true".equals(System.getProperty("udp.data.verbose", "false"));
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from UDP :" + e.getContent());
		}
		if (multiplexer != null) {
			multiplexer.onData(e.getContent());
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
			deviceFilters = instance.getDevicePrefix();
			sentenceFilters = instance.getSentenceArray();
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
		String serverName = "localhost"; // "sinagot.net"; // "192.168.42.2";
		int serverPort = 8_002; // 7_001;

		System.setProperty("nmea.parser.verbose", "true");

		nmeaClient = new UDPServer();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down nicely.");
			nmeaClient.stopDataRead();
		}, "CustomUDPServer shutdown hook"));
		nmeaClient.initClient();
		nmeaClient.setReader(new UDPReader("UDPReader", nmeaClient.getListeners(), serverName, serverPort));
		nmeaClient.startWorking();
	}
}


package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
// import nmea.consumers.reader.TCPReader;
import nmea.consumers.reader.UDPReader;

/**
 * Read NMEA Data from a UDP server
 */
public class UDPClient extends NMEAClient {
	public UDPClient() {
		this(null, null, null);
	}

	public UDPClient(Multiplexer mux) {
		this(null, null, mux);
	}

	public UDPClient(String[] s, String[] sa) {
		this(s, sa, null);
	}

	public UDPClient(String[] s, String[] sa, Multiplexer mux) {
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

	private static UDPClient udpClient = null;

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
		return new UDPBean(this);
	}

	public static void main(String... args) {
		final int UDP_PORT = 8_002;

		System.out.println("CustomUDPClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("CustomUDPClient prm:" + s);
		}

		final String SERVER_NAME = "localhost"; // "230.0.0.1";

		udpClient = new UDPClient();

		Runtime.getRuntime().addShutdownHook(new Thread("UDPClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				udpClient.stopDataRead();
			}
		});
		udpClient.initClient();
		udpClient.setReader(new UDPReader("UDPReader", udpClient.getListeners(), SERVER_NAME, UDP_PORT));
		udpClient.startWorking();
	}
}

package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.SerialReader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Read NMEA Data from a Serial port
 */
public class SerialClient extends NMEAClient {
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private String clientName; // TODO Put this in the supertype?

	public SerialClient() {
		this(null, null, null, false, true, "");
	}

	public SerialClient(Multiplexer mux) {
		this(null, null, mux, false, true,"");
	}

	public SerialClient(String s[], String[] sa) {
		this(s, sa, null, false, true,"");
	}

	public SerialClient(String[] devices, String[] sentences, Multiplexer mux, boolean verbose, boolean active, String description) {
		super(devices, sentences, mux, description);
		this.setVerbose(verbose); // "true".equals(System.getProperty("serial.data.verbose", "false")));
		this.setActive(active);
		this.clientName = String.valueOf(System.currentTimeMillis()) ; // ((SerialReader) this.getReader()).getPort();
		// this.clientName = String.format("Serial-%d", System.currentTimeMillis()); // TODO Why not?
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			if (this.getReader() != null) {
				this.clientName = ((SerialReader) this.getReader()).getPort();
			}
			System.out.printf("[%s] Received from Serial (%s:%d): %s\n",
					DURATION_FMT.format(new Date()),
					this.clientName,
					((SerialReader) this.getReader()).getBr(),
					e.getContent());
		}
		if (multiplexer != null) { // Only if active !
			if (this.isActive()) {
				multiplexer.onData(e.getContent()); // TODO Manage filters !! See DataFileClient and Writer.
			}
		}
	}

	private static SerialClient nmeaClient = null;

	public static class SerialBean implements ClientBean {
		private String cls;
		private final String type = "serial";
		private String port;
		private int br;
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;
		private boolean active;
		private String description;

		public String getCls() {
			return cls;
		}

		@Override
		public boolean getVerbose() {
			return verbose;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		public SerialBean() {}
		public SerialBean(SerialClient instance) {
			cls = instance.getClass().getName();
			port = ((SerialReader) instance.getReader()).getPort();
			br = ((SerialReader) instance.getReader()).getBr();
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

		public String getPort() {
			return port;
		}

		public int getBr() {
			return br;
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
		return new SerialBean(this);
	}

	/*
	 * For tests.
	 */
	public static void main(String... args) {
		System.out.println("SerialClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("SerialClient prm:" + s);
		}
//  String commPort = "/dev/ttyUSB0"; // "COM1";
		String commPort = "/dev/tty.usbserial"; // Mac
		if (args.length > 0) {
			commPort = args[0];
		}

		nmeaClient = new SerialClient();

		Runtime.getRuntime().addShutdownHook(new Thread("SerialClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new SerialReader(nmeaClient,"SerialReader", nmeaClient.getListeners(), commPort, 4_800));
		nmeaClient.startWorking();
	}
}
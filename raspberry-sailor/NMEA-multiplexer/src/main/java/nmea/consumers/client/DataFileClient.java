package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.DataFileReader;

/**
 * Read a file containing logged data for replay
 */
public class DataFileClient extends NMEAClient {
	private boolean loop = true;
	private boolean zip = false;
	private String pathInArchive = "";

	public DataFileClient() {
		this(null, null, null, false, true, "");
	}

	public DataFileClient(Multiplexer mux) {
		this(null, null, mux, false, true, "");
	}

	public DataFileClient(String[] s, String[] sa) {
		this(s, sa, null, false, true, "");
	}

	public DataFileClient(String[] s, String[] sa, Multiplexer mux, boolean verbose, boolean active, String desc) {
		super(s, sa, mux, desc);
		this.setVerbose(verbose); // verbose = "true".equals(System.getProperty("file.data.verbose", "false"));
		this.setActive(active);
	}

	public boolean isLoop() {
		return this.loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	public boolean isZip() {
		return zip;
	}
	public void setZip(boolean zip) {
		this.zip = zip;
	}
	public String getPathInArchive() {
		return pathInArchive;
	}
	public void setPathInArchive(String pathInArchive) {
		this.pathInArchive = pathInArchive;
	}

	@Override
	public void setVerbose(boolean b ) {
		this.verbose = b;
		if (this.getReader() != null) {
			this.getReader().setVerbose(b);
		}
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println(">> DataFileClient >> Received from File:" + e.getContent());
		}
		if (multiplexer != null) { // Only if active !
			if (this.isActive()) {
				String fullSentence = e.getContent(); // This is NOT an NMEA Sentence... it's stream containing NMEA Data...
				if (verbose) {
					System.out.printf("==>\tDataFileClient.dataDetectedEvent, data is [%s]\n", fullSentence);
				}
				multiplexer.onData(fullSentence);
			}
		}
	}

	private static DataFileClient nmeaClient = null;

	public static class DataFileBean implements ClientBean {
		private String cls;
		private String file;
		private long pause;
		private final String type = "file";
		private String[] deviceFilters;
		private String[] sentenceFilters;
		private boolean verbose;
		private boolean active;
		private boolean loop;
		private boolean zip;
		private String pathInArchive;
		private String description = "";

		public DataFileBean() {}

		public DataFileBean(DataFileClient instance) {
			cls = instance.getClass().getName();
			file = ((DataFileReader) instance.getReader()).getFileName();
			pause = ((DataFileReader) instance.getReader()).getBetweenRecord();
			verbose = instance.isVerbose();
			active = instance.isActive();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			loop = instance.isLoop();
			zip = instance.isZip();
			pathInArchive = instance.getPathInArchive();
			description = instance.getDescription();
		}

		@Override
		public String getType() {
			return this.type;
		}

		public String getFile() {
			return file;
		}

		public String getCls() {
			return cls;
		}

//		public boolean isVerbose() {
//			return verbose;
//		}

		public boolean isLoop() {
			return loop;
		}

		public boolean isZip() {
			return zip;
		}

		public long getPause() {
			return pause;
		}
		public boolean getLoop() { return loop; }
		public boolean getZip() { return zip; }
		public String getPathInArchive() {
			return pathInArchive;
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
		return new DataFileBean(this);
	}

	/**
	 * For tests (TODO isolate in tests?)
	 * @param args
	 */
	public static void main(String... args) {
		System.out.println("DataFileClient invoked with " + args.length + " Parameter(s).");
		for (String s : args) {
			System.out.println("DataFileClient prm:" + s);
		}

		System.setProperty("file.data.verbose", "true");

		String dataFile = // "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
						  "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea.zip";
		boolean zip = true;
		String pathInArchive = "./2010-11-08.Nuku-Hiva-Tuamotu.nmea";
		if (args.length > 0) {
			dataFile = args[0];
		}

		nmeaClient = new DataFileClient(null,
				new String[] { "RMC", "GLL" },
				null,
				"true".equals(System.getProperty("file.data.verbose", "false")),
				true, "" +
				"DataFileClient for Tests");
		// nmeaClient.setVerbose("true".equals(System.getProperty("file.data.verbose", "false")));

		Runtime.getRuntime().addShutdownHook(new Thread("DataFileClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});

		nmeaClient.initClient();
//		nmeaClient.setReader(new DataFileReader("DataFileReader", nmeaClient.getListeners(), dataFile, 10L)); // 10 overrides the default (500)
		nmeaClient.setReader(
				new DataFileReader("DataFileReader",
					nmeaClient.getListeners(),
					dataFile,
					10L, // 10 overrides the default (500)
					zip,
					pathInArchive));
		nmeaClient.getReader().setVerbose("true".equals(System.getProperty("file.data.verbose", "false")));
		((DataFileReader)nmeaClient.getReader()).setLoop(false);
		((DataFileReader)nmeaClient.getReader()).setDeviceFilters(nmeaClient.getDeviceFilters());
		((DataFileReader)nmeaClient.getReader()).setSentenceFilters(nmeaClient.getSentenceFilters());
		nmeaClient.startWorking();
	}
}
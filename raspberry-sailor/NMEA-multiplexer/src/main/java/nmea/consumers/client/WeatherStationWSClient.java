package nmea.consumers.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.consumers.reader.WeatherStationWSReader;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;
import java.util.Properties;

/**
 * Read WeatherStation Data from its WebSocket server, and turns them into NMEA data.
 */
public class WeatherStationWSClient extends NMEAClient {
	public WeatherStationWSClient() {
		this(null, null, null, true, false, "");
	}

	public WeatherStationWSClient(Multiplexer mux) {
		this(null, null, mux, true, false, "");
	}

	public WeatherStationWSClient(String[] s, String[] sa) {
		this(s, sa, null, true, false, "");
	}

	public WeatherStationWSClient(String[] s, String[] sa, Multiplexer mux, boolean active, boolean verbose, String desc) {
		super(s, sa, mux, desc);
		this.verbose = "true".equals(System.getProperty("weather.station.data.verbose", "false"));
		this.setActive(active);
		this.setVerbose(verbose);
	}

	@Override
	public void dataDetectedEvent(NMEAEvent e) {
		if (verbose) {
			System.out.println("Received from WebSocket :" + e.getContent());
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
					multiplexer.onData(e.getContent());  // TODO See where AIS strings are going (to be forwarded)
				} else {
					if (true || verbose) {
						System.out.printf("**\t[%s] does NOT go thru filters (%s, %s)\n", e.getContent(),
								this.getSentenceFilters() == null ? null : Arrays.asList(this.getSentenceFilters()),
								this.getDeviceFilters() == null ? null : Arrays.asList(this.getDeviceFilters()));
					}
				}
			}
		}
	}

	@Override
	public void setProperties(Properties props) {
		super.setProperties(props);
	}

	private static WeatherStationWSClient nmeaClient = null;

	public static class WeatherStationBean implements ClientBean {
		private String cls;
		private final String type = "weather.station";
		private String wsUri;
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

		public WeatherStationBean() {}
		public WeatherStationBean(WeatherStationWSClient instance) {
			cls = instance.getClass().getName();
			wsUri = ((WeatherStationWSReader) instance.getReader()).getWsUri();
			verbose = instance.isVerbose();
			active = instance.isActive();
			deviceFilters = instance.getDeviceFilters();
			sentenceFilters = instance.getSentenceFilters();
			description = instance.getDescription();
		}

		@Override
		public String getType() { return this.type; }

		@Override
		public boolean getVerbose() {
			return this.verbose;
		}
		@Override
		public boolean isActive() {
			return this.active;
		}

		public String getWsUri() {
			return wsUri;
		}

		@Override
		public String[] getDeviceFilters() { return this.deviceFilters; };

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String[] getSentenceFilters() { return this.sentenceFilters; };
	}

	@Override
	public ClientBean getBean() {
		return new WeatherStationBean(this);
	}

	public static void main(String... args) {
		String serverUri = "ws://localhost:9876/";

		nmeaClient = new WeatherStationWSClient();

		Runtime.getRuntime().addShutdownHook(new Thread("WebSocketClient shutdown hook") {
			public void run() {
				System.out.println("Shutting down nicely.");
				nmeaClient.stopDataRead();
			}
		});
		nmeaClient.initClient();
		nmeaClient.setReader(new WeatherStationWSReader(nmeaClient, "WeatherStationWSReader", nmeaClient.getListeners(), serverUri));
		nmeaClient.startWorking();
	}
}
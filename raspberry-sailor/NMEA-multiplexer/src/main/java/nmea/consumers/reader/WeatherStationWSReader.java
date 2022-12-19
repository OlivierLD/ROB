package nmea.consumers.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import nmea.parser.StringGenerator;
//import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * WebSocket reader
 */
public class WeatherStationWSReader extends NMEAReader {
	private final static ObjectMapper mapper = new ObjectMapper();
	private WebSocketClient wsClient = null;
	private final WeatherStationWSReader instance = this;
	private final String wsUri;
	private boolean verbose = false;

	private final static String DEVICE_PREFIX = "WS"; // Weather Station

	public WeatherStationWSReader(List<NMEAListener> al) {
		this(al, (Properties)null);
	}
	public WeatherStationWSReader(List<NMEAListener> al, Properties props) {
		this(al, props.getProperty("ws.uri"));
		verbose = "true".equals(props.getProperty("ws.verbose"));
//		if (verbose) {
//			WebSocketImpl.DEBUG = true; // Previous version
//		}
	}
	public WeatherStationWSReader(List<NMEAListener> al, String wsUri) {
		this(null, al, wsUri);
	}
	public WeatherStationWSReader(String threadName, List<NMEAListener> al, String wsUri) {
		super(threadName, al);
		this.wsUri = wsUri;
		try {
			this.wsClient = this.createWebSocketClient();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private WebSocketClient createWebSocketClient() throws URISyntaxException {
		return new WebSocketClient(new URI(wsUri)) {

			@Override
			public void onOpen(ServerHandshake serverHandshake) {
				System.out.println("WS On Open");
			}

			@Override
			public void onMessage(String mess) {
//        System.out.println("WS On Message");
				// Transform into NMEA
					/*
					   Received message is like
					   {
							"dir": 350.0,
							"avgdir": 345.67,
							"volts": 3.4567,
							"speed": 12.345,
							"gust": 13.456,
							"rain": 0.1,
							"press": 101300.00,
							"temp": 18.34,
							"hum": 58.5,
							"cputemp": 34.56
						}
					 */

				if (verbose) {
					System.out.printf(">> From WeatherStation: %s\n", mess);
				}

				try {
					Map<String, Object> map = mapper.readValue(mess, Map.class);

					double hum = (double)map.get("hum");
					double volts = (double)map.get("volts");
					double dir = (double)map.get("dir");
					double avgdir = (double)map.get("avgdir");
					double speed = (double)map.get("speed");
					double gust = (double)map.get("gust");
					double rain = (double)map.get("rain");
					double press = (double)map.get("press");
					double temp = (double)map.get("temp");

					int deviceIdx = 0; // Instead of "BME280" or so...
					String nmeaXDR = StringGenerator.generateXDR(DEVICE_PREFIX,
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY,
									hum,
									String.valueOf(deviceIdx++)), // %, Humidity
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
									temp,
									String.valueOf(deviceIdx++)), // Celcius, Temperature
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE,
									temp,
									String.valueOf(deviceIdx++)), // mm/h, Rain
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.GENERIC,
									rain,
									String.valueOf(deviceIdx++))); // Pascal, pressure
					nmeaXDR += NMEAParser.NMEA_SENTENCE_SEPARATOR;

					if (verbose) {
						System.out.printf(">>> Generated [%s]\n", nmeaXDR.trim());
					}

					fireDataRead(new NMEAEvent(this, nmeaXDR));

					String nmeaMDA = StringGenerator.generateMDA(DEVICE_PREFIX,
							press / 100,
							temp,
							-Double.MAX_VALUE,  // Water Temp
							hum,
							-Double.MAX_VALUE,  // Abs hum
							-Double.MAX_VALUE,  // dew point
							avgdir,  // TWD
							-Double.MAX_VALUE,  // TWD (mag)
							speed); // TWS
					nmeaMDA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

					if (verbose) {
						System.out.printf(">>> Generated [%s]\n", nmeaMDA.trim());
					}

					instance.fireDataRead(new NMEAEvent(this, nmeaMDA));

					String nmeaMTA = StringGenerator.generateMTA(DEVICE_PREFIX, temp);
					nmeaMTA += NMEAParser.NMEA_SENTENCE_SEPARATOR;

					if (verbose) {
						System.out.printf(">>> Generated [%s]\n", nmeaMTA.trim());
					}

					instance.fireDataRead(new NMEAEvent(this, nmeaMTA));

					String nmeaMMB = StringGenerator.generateMMB(DEVICE_PREFIX, press / 100);
					nmeaMMB += NMEAParser.NMEA_SENTENCE_SEPARATOR;

					if (verbose) {
						System.out.printf(">>> Generated [%s]\n", nmeaMMB.trim());
					}

					instance.fireDataRead(new NMEAEvent(this, nmeaMMB));
				} catch (JsonProcessingException jpe) {
					throw new RuntimeException(jpe);
				}
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				System.out.printf("WS On Close - Code: %d, Reason: %s, remote: %s\n", code, reason, String.valueOf(remote));
				if (code == CloseFrame.ABNORMAL_CLOSE || code == CloseFrame.NEVER_CONNECTED) {
					long delay = 5_000L;
					System.out.printf("Abnormal close, retrying in %d milliseconds.\n", delay);
					Thread connector = new Thread(() -> {
						try {
							Thread.sleep(delay);
							instance.wsClient = instance.createWebSocketClient();
							instance.wsClient.connect();
							System.out.println(">> Reconnected <<");
						} catch (URISyntaxException | InterruptedException e) {
							e.printStackTrace();
						}
					});
					connector.start();
				}
			}

			@Override
			public void onError(Exception exception) {
				System.err.println("==== WS On Error ====");
				exception.printStackTrace();
				System.err.println("=====================");
			}
		};
	}

	@Override
	public void startReader() {
		super.enableReading();
		this.wsClient.connect();
	}

	public String getWsUri() {
		return this.wsUri;
	}

	@Override
	public void closeReader() throws Exception {
		if (this.wsClient != null) {
			this.wsClient.close();
		}
	}
}

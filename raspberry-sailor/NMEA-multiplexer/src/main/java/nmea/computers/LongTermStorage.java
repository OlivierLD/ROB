package nmea.computers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.parser.UTCDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Used to store data like PRMSL, temperature, etc, over time.
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '."storage-data"'
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class LongTermStorage extends Computer {

	private final ObjectMapper jacksonMapper = new ObjectMapper();
	// Format used for the key
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static {
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	// Properties
	private long pingInterval = 900; // 3_600;  // in seconds. 900 : 15 minutes
	private long maxLength = 672; // Max length of the buffer. 900s = 15 minutes, 672 = 4 * 24 * 7: one week, with one point every 15 minutes.

	// TODO Use jq ? Like | jq '."Barometric Pressure".value' implementation 'net.thisptr:jackson-jq:1.0.0-preview.20220705'... Not sure.
	// See in nmea.consumers.reader.RESTReader.java, look for JsonQuery
	private String[] dataPathInCache = { "Barometric Pressure", "value" };  // CSV in the yaml...
	private String storagePathInCache = "BarographData"; // AKA List/Buffer name

	private Map<String, Object> objectMap = new TreeMap<>();
	private final AtomicReference<NMEADataCache> cacheReference = new AtomicReference<>();

	private Thread dataCollector = new Thread(() -> {
		while (true) { // Loop until dead
			// NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
			cacheReference.set(ApplicationContext.getInstance().getDataCache());
			try {
				final String jsonString;
				NMEADataCache cache;
				synchronized (cacheReference) {
					cache = cacheReference.get();
					jsonString = jacksonMapper.writeValueAsString(cache);
				}
				final JsonNode jsonNode = jacksonMapper.readTree(jsonString);

				Object finalData = null;
				JsonNode previousObject = jsonNode;
				for (String path : this.dataPathInCache) {
					finalData = previousObject.get(path);
					if (finalData != null) {
						previousObject = (JsonNode)finalData;
					} else {
						break;
					}
				}
				if (finalData != null) {
					Date measureDate = null;
					// Get GPS Date from cache, from the system if not found in cache
					UTCDate utcDate = (UTCDate) cache.get(NMEADataCache.GPS_DATE_TIME, true);
					if (utcDate != null) {
						measureDate = utcDate.getDate();
					} else {
						measureDate = new Date(System.currentTimeMillis());
					}
					// Fill the map
					objectMap.put(DURATION_FMT.format(measureDate), finalData);
					if (this.verbose) {
						System.out.printf(">> Long Storage Map %s is now %d elements big\n", this.storagePathInCache, objectMap.size());
					}
					// Cut the Map if too long (from the head)
					while (objectMap.keySet().size() > this.maxLength) {
						String first = (String)objectMap.keySet().toArray()[0];
						if (this.verbose) {
							System.out.printf("Dropping map element with key %s\n", first);
						}
						objectMap.remove(first);
					}
					// Push map to cache
					cache.put(storagePathInCache, objectMap);
				}
			} catch (JsonProcessingException jpe) {
				jpe.printStackTrace();
			}
			try {
				Thread.sleep(this.pingInterval * 1_000);
			} catch (InterruptedException ie) {
				// Oops
				ie.printStackTrace();
			}
		}
	}, "dataCollector");

	public LongTermStorage(Multiplexer mux, Long pingInterval, Long maxLength, String[] dataPath, String objectName) {
		super(mux);
		if (pingInterval != null) {
			this.pingInterval = pingInterval;
		}
		if (maxLength != null) {
			this.maxLength = maxLength;
		}
		this.dataPathInCache = dataPath;
		this.storagePathInCache = objectName;
		dataCollector.start();
	}

	/**
	 * Wait for position data (not cached), get the perimeters/borders from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void write(byte[] mess) {
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing Border data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;

		this.verbose = "true".equals(props.getProperty("verbose"));

		if (this.verbose) {
			System.out.println(String.format("LongTerm Computer %s:\n\tVerbose: %s\n",
					this.getClass().getName(),
					this.verbose));
		}
	}

	public static class LongTermComputerBean {
		private String cls;
		private final String type = "longterm-data-computer";
		private boolean verbose;

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public LongTermComputerBean() {}  // This is for Jackson
		public LongTermComputerBean(LongTermStorage instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new LongTermComputerBean(this);
	}
}

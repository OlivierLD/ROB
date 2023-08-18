package nmea.computers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.parser.*;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Used to store data like PRMSL over time.
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '."borders-data"'
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class LongTermStorage extends Computer {

	private final ObjectMapper jacksonMapper = new ObjectMapper();
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	static {
		DURATION_FMT.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	// Properties
	private long pingInterval = 3_600;  // in seconds
	private long maxLength = 618; // Nb members. Length of the buffer
	private String[] dataPathInCache = { "Barometric Pressure", "value" }; // TODO Use jq ?
	private String storagePathInCache = "BarographData";

	private Map<String, Object> objectMap = new TreeMap<>();

	private Thread dataCollector = new Thread(() -> {
		while (true) { // Loop until dead
			NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
			try {
				final String jsonString = jacksonMapper.writeValueAsString(cache);
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
						System.out.printf(">> Long Storage Map is now %d elements big\n", objectMap.size());
					}
					// Cut the Map is too long
					while (objectMap.keySet().size() > this.maxLength) {
						Long first = (Long)objectMap.keySet().toArray()[0];
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

		this.pingInterval = Long.parseLong(props.getProperty("ping.interval", String.valueOf(this.pingInterval)));
		this.verbose = "true".equals(props.getProperty("verbose"));

		if (this.verbose) {
			System.out.println(String.format("LongTerm Computer %s:\n\tVerbose: %s\n\tPing Interval: %d",
					this.getClass().getName(),
					this.verbose,
					this.pingInterval));
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

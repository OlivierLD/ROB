package nmea.mux.context;

//import http.HTTPServer;

import nmea.parser.Marker;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a singleton for the whole mux application
 */
public class Context {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	static {
		LOGGER.setLevel(Level.INFO);
	}

	private long startTime = 0L;
	private long managedBytes = 0L;

	private long nbMessReceived = 0L;

	private String lastDataSentence = "";
	private long lastSentenceTimestamp = 0L;

	private static Context instance;

	/**
	 * Used for the global context.
	 * Displayed in the Admin pages.
	 */
	public static class TopContext {
		private String name;
		private List<String> description;
		private Boolean withHTTPServer = false;
		private int httpPort = -1;
		private Boolean initCache = false;
		private double defaultDeclination = 0d;
		private String deviationFileName;
		private double maxLeeway = 0d;
		private int damping = 0;
		private String markers;
		private List<String[]> markerList;

		private List<Marker> waypointList;
		private String currentWaypointName;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getDescription() {
			return description;
		}

		public void setDescription(List<String> description) {
			this.description = description;
		}

		public Boolean getWithHTTPServer() {
			return withHTTPServer;
		}

		public void setWithHTTPServer(Boolean withHTTPServer) {
			this.withHTTPServer = withHTTPServer;
		}

		public int getHttpPort() {
			return httpPort;
		}

		public void setHttpPort(int httpPort) {
			this.httpPort = httpPort;
		}

		public Boolean getInitCache() {
			return initCache;
		}

		public void setInitCache(Boolean initCache) {
			this.initCache = initCache;
		}

		public double getDefaultDeclination() {
			return defaultDeclination;
		}

		public void setDefaultDeclination(double defaultDeclination) {
			this.defaultDeclination = defaultDeclination;
		}

		public String getDeviationFileName() {
			return deviationFileName;
		}

		public void setDeviationFileName(String deviationFileName) {
			this.deviationFileName = deviationFileName;
		}

		public double getMaxLeeway() {
			return maxLeeway;
		}

		public void setMaxLeeway(double maxLeeway) {
			this.maxLeeway = maxLeeway;
		}

		public int getDamping() {
			return damping;
		}

		public void setDamping(int damping) {
			this.damping = damping;
		}

		public String getMarkers() {
			return markers;
		}

		public void setMarkers(String markers) {
			this.markers = markers;
		}

		public List<String[]> getMarkerList() {
			return markerList;
		}

		public void setMarkerList(List<String[]> markerList) {
			this.markerList = markerList;
		}

		public List<Marker> getWaypointList() {
			return waypointList;
		}

		public void setWaypointList(List<Marker> waypointList) {
			this.waypointList = waypointList;
		}

		public String getCurrentWaypointName() {
			return currentWaypointName;
		}

		public void setCurrentWaypointName(String currentWaypointName) {
			this.currentWaypointName = currentWaypointName;
		}

		@Override
		public String toString() {
			return(String.format("Context.toString:%s", this.getName()));
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	}

	private TopContext mainContext;

	public synchronized static Context getInstance() {
		if (instance == null) {
			instance = new Context();
		}
		return instance;
	}

	public TopContext getMainContext() {
		return mainContext;
	}

	public void setMainContext(TopContext mainContext) {
		this.mainContext = mainContext;
	}

	/**
	 * Those topic listeners can be used like regular event listeners.
	 * They've be designed to be used in conjunction with the POST /events/{topic} service, though.
	 * <br/>
	 * See {@_link nmea.mux.RESTImplementation#broadcastOnTopic(HTTPServer.Request)} for more details about that.
	 */
	private List<TopicListener> topicListeners = new ArrayList<>();
	public void addTopicListener(TopicListener topicListener) {
		synchronized (this.topicListeners) {
			this.topicListeners.add(topicListener);
		}
	}
	public void removeTopicListener(TopicListener topicListener) {
		if (this.topicListeners.contains(topicListener)) {
			synchronized (this.topicListeners) {
				this.topicListeners.remove(topicListener);
			}
		}
	}

	/**
	 *
	 * @param topic A RegEx to match the topic of the payload. The payload will be sent only to those who subscribed to a topic matching the regex.
	 * @param payload Usually a Map&lt;String, Object&gt;, representing the payload json object. Can be null.
	 */
	public void broadcastOnTopic(String topic, Object payload) {
		Pattern pattern = Pattern.compile(topic);
		this.topicListeners.stream()
				.filter(tl -> {
					Matcher matcher = pattern.matcher(tl.getSubscribedTopic());
//		    	System.out.println(
//		    			String.format("[%s] %s [%s]",
//						    tl.getSubscribedTopic(),
//						    (matcher.matches() ? "matches" : "does not match"),
//						    topic));
					return matcher.matches();
				})
				.forEach(tl -> {
					tl.topicBroadcast(topic, payload);
				});
	}

	public Logger getLogger() {
		return this.LOGGER;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getManagedBytes() {
		return managedBytes;
	}

	public void addManagedBytes(long managedBytes) {
		this.managedBytes += managedBytes;
	}

	public void setLastDataSentence(String sentence) {
		try {
			this.nbMessReceived++;
		} catch (Exception ex) { // Overflow? TODO trap it for real
			ex.printStackTrace();
		}
		this.lastDataSentence = sentence;
		this.lastSentenceTimestamp = System.currentTimeMillis();
	}
	public StringAndTimeStamp getLastDataSentence() {
		return new StringAndTimeStamp(this.lastDataSentence, this.lastSentenceTimestamp);
	}
	public long getNbMessReceived() {
		return this.nbMessReceived;
	}

	public static class StringAndTimeStamp {
		String str;
		long timestamp;
		public StringAndTimeStamp(String str, long ts) {
			this.str = str;
			this.timestamp = ts;
		}
		public String getString() { return this.str; }
		public long getTimestamp() { return this.timestamp; }
	}

	public static abstract class TopicListener implements EventListener {
		private String subscribedTopic;
		public TopicListener(String topic) {
			this.subscribedTopic = topic;
		}
		public String getSubscribedTopic() {
			return this.subscribedTopic;
		}

		public abstract void topicBroadcast(String topic, Object payload);
	}
}

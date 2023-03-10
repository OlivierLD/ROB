package nmea.consumers.reader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import http.HTTPServer;
import http.client.HTTPClient;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;
import utils.TimeUtil;

import java.io.IOException;
import java.io.StringReader;
import java.net.BindException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Assumes JSON Output from the REST service invoked with GET
 *
 * For JQ prm: jq syntax doc: https://lzone.de/cheat-sheet/jq, jackson-jq repo: https://github.com/eiiches/jackson-jq
 *
 * REST reader. WiP
 * - verbs ?
 */
public class RESTReader extends NMEAReader {
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static int DEFAULT_HTTP_PORT = 80;
	private final static String DEFAULT_PROTOCOL = "http";
	private final static String DEFAULT_QUERY_PATH = "/";
	private final static String DEFAULT_QUERY_STRING = "";
	private final static long DEFAULT_BETWEEN_LOOPS = 1_000L;

	private int httpPort = DEFAULT_HTTP_PORT;
	private String hostName = DEFAULT_HOST_NAME;
	private String protocol = DEFAULT_PROTOCOL;
	private String queryPath = DEFAULT_QUERY_PATH;
	private String queryString = DEFAULT_QUERY_STRING;
	private String jqsString = null; // jq syntax doc: https://lzone.de/cheat-sheet/jq, jackson-jq repo: https://github.com/eiiches/jackson-jq
	private long betweenLoops = DEFAULT_BETWEEN_LOOPS;

	private final ObjectMapper mapper = new ObjectMapper();
	private final Scope ROOT_SCOPE = Scope.newEmptyScope(); // jq scope

	public RESTReader(List<NMEAListener> al) {
		this(null, al, DEFAULT_PROTOCOL, DEFAULT_HOST_NAME, DEFAULT_HTTP_PORT, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING, null, null);
	}

	public RESTReader(List<NMEAListener> al, int http) {
		this(null, al, DEFAULT_PROTOCOL, DEFAULT_HOST_NAME, http, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING, null, null);
	}

	public RESTReader(List<NMEAListener> al, String host, int http) {
		this(null, al, DEFAULT_PROTOCOL, host, http, DEFAULT_QUERY_PATH, DEFAULT_QUERY_STRING, null, null);
	}
	public RESTReader(String threadName, List<NMEAListener> al, String protocol, String host, int http, String path, String qs, String jqs, Long betweenLoops) {
		super(threadName != null ? threadName : "rest-thread", al);
		if (verbose) {
			System.out.println(this.getClass().getName() + ": There are " + al.size() + " listener(s)");
		}
		this.protocol = protocol;
		this.hostName = host;
		this.httpPort = http;
		this.queryPath = path;
		this.queryString = qs;
		this.jqsString = jqs;
		if (betweenLoops != null) {
			this.betweenLoops = betweenLoops;
		}
	}

	public String getProtocol() {
		return this.protocol;
	}
	public String getHostname() {
		return this.hostName;
	}
	public int getPort() {
		return this.httpPort;
	}
	public String getQueryPath() {
		return this.queryPath;
	}
	public String getQueryString() {
		return this.queryString;
	}
	public String getJQString() {
		return this.jqsString;
	}
	public long getBetweenLoops() { return this.betweenLoops; }

	public Consumer<HTTPServer.Response> responseProcessor = this::manageRESTResponse;

	/**
	 * Processes the REST response. Designed in case it needs to be overridden.
	 * Used as responseProcessor (see above)
	 * Does a super.fireDataRead(new NMEAEvent(this, nmeaContent));
	 * (TODO Illustrate extension)
	 *
	 * IMPORTANT: It's the server's responsibility to provide well formed NMEA Sentences.
	 * If the payload is a JSON Map, every member of the map will be considered as 'sendable'.
	 * Otherwise, the full payload will be sent.
	 * This means that each member VALUE needs to be a valid NMEA string. See detectSentence, in NMEAParser.
	 *
	 * @param response The response to process
	 * @throws RuntimeException Oops.
	 */
	@SuppressWarnings("unchecked")
	private void manageRESTResponse(HTTPServer.Response response) {
		try {
			String payload = new String(response.getPayload());
			// See jackson-jq
			if (response.getHeaders() != null) {
				String contentType = response.getHeaders().get("Content-Type"); // TODO Upper/lower case
				AtomicReference<String> objPayload = new AtomicReference<>(payload);
				if ("application/json".equals(contentType)) {
					String jqString = getJQString();
					if (jqString != null && jqString.trim().length() > 0) {  // Compatible Java 8
						try {
							JsonQuery jq = JsonQuery.compile(jqString /*".NMEA_AS_IS.RMC" */, Versions.JQ_1_6);
							JsonNode jsonNode = mapper.readTree(new StringReader(payload));
							jq.apply(ROOT_SCOPE, jsonNode, (out) -> {
								if (out.isTextual() /*&& command.hasOption(OPT_RAW.getOpt()) */) {
									objPayload.set(out.asText());
								} else {
									try {
										objPayload.set(mapper.writeValueAsString(out));
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
							});
						} catch (JsonQueryException jqe) {
							// Query cannot be parsed.
							jqe.printStackTrace();
						}
					}
					payload = objPayload.get(); // mapper.writeValueAsString(objPayload.get());
				}
			}
			if (verbose) {
				System.out.printf(">> REST Reader: %s\n", payload);
			}
			final List<String> dataToFire = new ArrayList<>();
			// Multi-node result here? Re-parse.
			try {
				Object finalObject = mapper.readValue(payload, Object.class);
				if (finalObject instanceof Map) {
					((Map<String, Object>) finalObject).forEach((k, v) -> dataToFire.add(v.toString())); // Prepare all the elements of the map
				} else {
					dataToFire.add(payload);
				}
			} catch (JsonParseException jpe) {
				// Not an Object, consider it a String.
				dataToFire.add(payload);
			}

			// Loop on all data to fire.
			dataToFire.forEach(data -> {
				if (data != null && !data.endsWith(NMEAParser.NMEA_SENTENCE_SEPARATOR)) {
					data += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				}
				NMEAEvent n = new NMEAEvent(this, data);
				if (verbose) {
					System.out.printf("\tRESTReader firing super.fireDataRead with %s\n", data);
				}
				super.fireDataRead(n);
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void startReader() {
		super.enableReading();

		String restURL = String.format("%s://%s:%d%s%s",
				this.protocol,
				this.hostName,
				this.httpPort,
				this.queryPath,
				this.queryString != null ? this.queryString : "" );
		try {
			while (this.canRead()) {
				try {
					// Hard-coded verb and protocol for now.
					HTTPServer.Request request = new HTTPServer.Request("GET", restURL, "HTTP/1.1");
					Map<String, String> reqHeaders = new HashMap<>();
					request.setHeaders(reqHeaders);
					final HTTPServer.Response response = HTTPClient.doRequest(request);

					responseProcessor.accept(response);

				} catch (BindException be) {
					System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + httpPort);
					be.printStackTrace();
					manageError(be);
				} catch (final SocketException se) {
					if (se.getMessage().contains("Connection refused")) {
						System.out.println("Refused (1)");
//						se.printStackTrace();
					} else if (se.getMessage().contains("Connection reset")) {
						System.out.println("Reset (2)");
					} else {
						if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (1)");
							}
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (2)");
							}
						} else if (/*se instanceof SocketException &&*/ se.getMessage().startsWith("Network is unreachable (connect ")) {
							if ("true".equals(System.getProperty("verbose.data.verbose"))) {
								System.out.println("Will try again (3)");
							}
						} else if (se instanceof ConnectException) { // Et hop!
							System.err.println("REST :" + se.getMessage());
						} else {
							System.err.println("REST Server:" + se.getMessage());
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// Wait like 1 sec.
				TimeUtil.delay(this.betweenLoops);
			}
			System.out.println("Stop Reading REST server.");
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
	}

	@Override
	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			this.goRead = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for REST */ }

}

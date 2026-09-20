package nmea.forwarders;

import http.client.HTTPClient;
import nmea.ais.AISParser;
import nmea.api.BeanInterface;

import java.util.*;
import java.util.stream.Collectors;

// See the test, in the test folder.
public class RESTPublisher implements Forwarder {
	private Properties props = null;

	private boolean verbose;
	private boolean active = true;
	private List<String> sentenceFilters = null; // Sentence filters
	private List<String> deviceFilters = null; // Device filters
	private String description = "No description";
	private int httpPort = 80;                  // Default
	private String serverName = "localhost";    // Default
	private String restResource = null;         // Required. No default.
	private String protocol = "http";           // default
	private String verb = "POST";               // default
	private Map<String, String> headers = null; // Optional

	private HTTPClient restClient = null;

	private AISParser aisParser;

	public RESTPublisher() {
	}
	public RESTPublisher(String verb,
						 String serverName,
						 int port,
						 String resource,
						 String protocol,
						 Map<String, String> headers,
						 boolean verbose,
						 boolean active,
						 String deviceFilters,
						 String sentenceFilters,
						 String description) {
		System.out.printf("- Instantiating %s\n", this.getClass().getName());

		if (sentenceFilters != null) {
			if (sentenceFilters.trim().length() > 0) {
				this.sentenceFilters = Arrays.asList(sentenceFilters.trim().split(","))
						.stream()
						.map(String::trim)
						.collect(Collectors.toList());

			}
		}
		if (deviceFilters != null) {
			if (deviceFilters.trim().length() > 0) {
				this.deviceFilters = Arrays.asList(deviceFilters.trim().split(","))
						.stream()
						.map(String::trim)
						.collect(Collectors.toList());

			}
		}

		this.verb = verb;
		this.serverName = serverName;
		this.httpPort = port;
		this.restResource = resource;
		this.description = description;
		this.verbose = verbose;
		this.active = active;
		if (protocol != null) {
			this.protocol = protocol;
		}
		if (headers != null) {
			this.headers = headers;
		}
	}

	public String getProtocol() {
		return protocol;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public String getServerName() {
		return serverName;
	}

	public String getRestResource() {
		return restResource;
	}

	public String getVerb() {
		return verb;
	}

	public List<String> getSentenceFilters() {
		return sentenceFilters;
	}

	public void setSentenceFilters(List<String> sentenceFilters) {
		this.sentenceFilters = sentenceFilters;
	}

	public List<String> getDeviceFilters() {
		return deviceFilters;
	}

	public void setDeviceFilters(List<String> deviceFilters) {
		this.deviceFilters = deviceFilters;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setRestResource(String restResource) {
		this.restResource = restResource;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}
	@Override
	public void setActive(boolean status) {

		System.out.printf("-- Forwarder DataFileWriter, setActive method: %B\n", status);
		this.active = status;
	}

	@Override
	public void setVerbose(boolean status) {
		this.verbose = status;
	}
	@Override
	public boolean isVerbose() {
		return this.verbose;
	}
	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * Note: This can be sub-classed to filter the sentences,
	 *       and send a formatted message to a Screen Server (for example)...
	 *       See https://github.com/OlivierLD/raspberry-coffee/tree/master/http-client-samples
	 * @param message The NMEA sentence (or whatever you want to send)
	 */
	@Override
	public void write(byte[] message) {
		if (!this.isActive()) {
			if ("true".equals(System.getProperty("mux.infra.verbose", "false"))) {
				System.out.println("DataFileWriter write: INACTIVE forwarder, skipping write."); // TODO Use LOG ?
			}
			return;
		}

		if (restClient == null) {
			restClient = new HTTPClient();
		}
		try {
			switch(this.verb) {
				case "POST":
					String postRequest = String.format("%s://%s:%d%s", protocol, serverName, httpPort, restResource);
					String strContent = new String(message).trim();
//					System.out.println("Verbose: [" + this.props.getProperty("verbose") + "]");
					if (this.verbose) {
						System.out.printf("%s\n%s\n", postRequest, strContent);
						if ("true".equals(System.getProperty("parse.ais"))) {
							if (strContent.startsWith(AISParser.AIS_PREFIX)) {
								if (aisParser == null) {
									aisParser = new AISParser();
								}
								try {
									System.out.println(aisParser.parseAIS(strContent));
								} catch (AISParser.AISException t) {
									System.err.println(t.toString());
								}
							}
						}
					}
					HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(postRequest, headers, strContent);
					if (this.verbose) {
						System.out.printf("POST %s with %s: Response code %d, message: %s\n",
								postRequest,
								strContent,
								httpResponse.getCode(),
								httpResponse.getPayload());
					}
					// TODO return the response message/status ?
					break;
				case "PUT":
					String putRequest = String.format("%s://%s:%d%s", protocol, serverName, httpPort, restResource);
					String putStrContent = new String(message).trim();
//					System.out.println("Verbose: [" + this.props.getProperty("verbose") + "]");
					if (this.verbose) {
						System.out.printf("%s\n%s\n", putRequest, putStrContent);
					}
					HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, putStrContent);
					if (this.verbose) {
						System.out.printf("PUT %s with %s: Response code %d, message: %s\n",
								putRequest,
								putStrContent,
								putResponse.getCode(),
								putResponse.getPayload());
					}
					// TODO return the response message/status ?
					break;
				default:
					break;
			}
		} catch (Exception ex) {
			if (this.verbose) {
				System.err.println(">> Error!");
				ex.printStackTrace();
			}
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
	}

	public static class RESTBean implements BeanInterface {
		private String cls;
		private String protocol;
		private int port;
		private String serverName;
		private String verb;
		private String resource;
		private final String type = "rest";
		private boolean verbose;
		private boolean active;
		private List<String> filters;
		private List<String> deviceFilters;
		private Map<String, String> headers;
		private String description;

		public String getProtocol() {
			return protocol;
		}
		public String getVerb() {
			return verb;
		}
		public String getServerName() {
			return serverName;
		}
		public int getPort() {
			return port;
		}
		public String getResource() {
			return resource;
		}
		@Override
		public String getCls() {
			return cls;
		}
		@Override
		public String getType() {
			return type;
		}
		@Override
		public boolean isActive() {
			return active;
		}
		@Override
		public boolean isVerbose() {
			return verbose;
		}
		@Override
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}
		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public List<String> getFilters() {
			return filters;
		}
		public List<String> getDeviceFilters() { return deviceFilters; }

		public RESTBean() {}   // This is for Jackson

		public RESTBean(RESTPublisher instance) {
			cls = instance.getClass().getName();
			protocol = instance.protocol;
			port = instance.httpPort;
			serverName = instance.serverName;
			verb = instance.verb;
			resource = instance.restResource;
			active = instance.active;
			verbose = instance.verbose;
			description = instance.getDescription();
			filters = instance.sentenceFilters;
			deviceFilters = instance.deviceFilters;
			headers = instance.headers;
		}
	}

	@Override
	public BeanInterface getBean() {
		return new RESTBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
		if (this.props != null) {
			// server name, port, rest resource, verb, headers
			this.serverName = props.getProperty("server.name", this.serverName);
			this.httpPort = Integer.parseInt(props.getProperty("server.port", String.valueOf(this.httpPort)));
			this.restResource = props.getProperty("rest.resource");
			this.verb = props.getProperty("rest.verb", this.verb);
			String propProtocol = props.getProperty("rest.protocol");
			if (propProtocol != null) {
				this.protocol = propProtocol; // http or https
			}
			String headers = props.getProperty("http.headers");
			if (headers != null) {
				String[] headerArray = headers.split(",");
				for (String h : headerArray) {
					String[] nv = h.split(":");
					if (nv.length != 2) {
						// Oops! TODO Honk!
						System.err.println("WTFrench!");
					} else {
						if (this.headers == null) {
							this.headers = new HashMap<>();
						}
						this.headers.put(nv[0], nv[1]);
					}
				}
			}
		}
	}
}
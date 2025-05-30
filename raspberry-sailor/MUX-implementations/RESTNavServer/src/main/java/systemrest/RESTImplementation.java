package systemrest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.HttpHeaders;
import http.RESTProcessorUtil;
import http.client.HTTPClient;
import navrest.CompositeCrawler;
import navrest.NavServerContext;
import utils.StringUtils;
import utils.SystemUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * Those operation mostly retrieve the state of the SunFlower class, and device.
 * <br>
 * The SystemRequestManager will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private final static ObjectMapper mapper = new ObjectMapper();

	private SystemRequestManager systemRequestManager;

	private final static String SYSTEM_PREFIX = "/system";

	private final static boolean VERBOSE = "true".equals(System.getProperty("rest.verbose"));
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final static SimpleDateFormat SYSDATE_FMT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");


	public RESTImplementation(SystemRequestManager restRequestManager) {

		this.systemRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * Frame path parameters with curly braces.
	 * <p>
	 * See {@link #processRequest(Request)}
	 * See {@link HTTPServer}
	 */
	private final List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					"/oplist", // Yes, no prefix here.
					this::getOperationList,
					"List of all available operations, on all request managers."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/generic-get",
					this::genericGet,
					"GET on a specific resource, from the server (no CORS). Provide the URL in the headers (get-url), and expected Content-Type."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/networks",
					this::getNetworks,
					"Get the list of the networks the server is on."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/ip-address",
					this::getIpAddress,
					"Get IP Address (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/cpu-load",
					this::getCPULoad,
					"Get CPU Load (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/cpu-temperature",
					this::getCPUTemperature,
					"Get CPU Temperature (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/disk-usage",
					this::getDiskUsage,
					"Get Disk Usage (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/memory-usage",
					this::getMemoryUsage,
					"Get Memory Usage (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/system-data",
					this::getSystemData,
					"Get all system data (Linux only)."),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/addresses", // Optional QS Prm: v4Only=true|[false], iface=wlan0
					this::getIps,                       // Returns couples like ("iface", "address")
					"Get the list of IP addresses of the server, with the interface names. QS prms: v4Only [false]|true, iface=XXX (optional)"),
			new Operation(
					"GET",
					SYSTEM_PREFIX + "/system-date",
					this::getSystemDate,
					"Get the System Date, in JSON format."),
			new Operation(
					"POST",
					SYSTEM_PREFIX + "/system-date",
					this::setSystemDate,
					"Set the System Date. VERY unusual REST resource..."),
			new Operation(
					"POST",
					SYSTEM_PREFIX + "/start-mux",
					this::emptyOperation,
					"Starts the Multiplexer"),
			new Operation(
					"POST",
					SYSTEM_PREFIX + "/stop-mux",
					this::emptyOperation,
					"Stops the Multiplexer"),
			new Operation(
					"POST",
					SYSTEM_PREFIX + "/stop-all",
					this::emptyOperation,
					"System Shutdown")

	);

	protected List<Operation> getOperations() {
		return this.operations;
	}

	/**
	 * This is the method to invoke to have a REST request processed as defined above.
	 *
	 * @param request as it comes from the client
	 * @return the actual result.
	 */
	public Response processRequest(Request request) throws UnsupportedOperationException {
		Optional<Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		List<Operation> opList = this.systemRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		if (VERBOSE) {
			this.systemRequestManager.getLogger().log(Level.INFO, String.format("getOperationList required in %s => %d operation(s)", this.getClass().getName(), opList.size()));
		}
		String content;
		try {
			content = mapper.writeValueAsString(opList); // new Gson().toJson(opList);
		} catch (JsonProcessingException jpe) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("COMP-0001-1")
							.errorMessage(jpe.toString())
							.errorStack(HTTPServer.dumpException(jpe)));
			return response;
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
		response.setPayload(content.getBytes());
		return response;
	}

	private Response genericGet(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String getUrl = request.getHeaders().get("get-url");
			String getResponse = HTTPClient.doGet(getUrl, null); // TODO Headers!
//			String content = new Gson().toJson(networkName);
			RESTProcessorUtil.generateResponseHeaders(response, getResponse.length());
			response.setPayload(getResponse.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-1000")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getNetworks(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			List<String> networkName = SystemUtils.getNetworkName();
			String content;
			try {
				content = mapper.writeValueAsString(networkName);
			} catch (JsonProcessingException jpe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SERVER-0001-3")
								.errorMessage(jpe.toString())
								.errorStack(HTTPServer.dumpException(jpe)));
				return response;
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getIps(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		boolean v4Only = qs != null && "true".equals(qs.get("v4Only"));
		String iFace = qs != null ? qs.get("iface") : null;
		try {
			List<String[]> ipAddresses = SystemUtils.getIPAddresses(iFace, v4Only);
			String content;
			try {
				content = mapper.writeValueAsString(ipAddresses);
			} catch (JsonProcessingException jpe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SERVER-0002-1")
								.errorMessage(jpe.toString())
								.errorStack(HTTPServer.dumpException(jpe)));
				return response;
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0002")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getIpAddress(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String ipAddress = SystemUtils.getIPAddress();
			String content = ipAddress; // new Gson().toJson(ipAddress);

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", ipAddress, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0003")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getCPUTemperature(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String cpuTemperature = SystemUtils.getCPUTemperature();
			String content = cpuTemperature; // new Gson().toJson(cpuTemperature);

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", cpuTemperature, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0004")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getDiskUsage(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String diskUsage = SystemUtils.getDiskUsage();
			String content = diskUsage; // new Gson().toJson(diskUsage);

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", diskUsage, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0005")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getMemoryUsage(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String memoryUsage = SystemUtils.getMemoryUsage();
			String content = memoryUsage; // new Gson().toJson(memoryUsage);

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", memoryUsage, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0006")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getCPULoad(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String cpuLoad = SystemUtils.getCPULoad();
			String content = cpuLoad; // new Gson().toJson(cpuLoad);

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", cpuLoad, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0007")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	// Must be a Java Bean (with getters and setters) for the toJson to work.
	private static class SystemData {
		String ipAddress;
		String cpuTemperature;
		String cpuLoad;
		String memoryUsage;
		String diskUsage;

		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		public void setCpuTemperature(String cpuTemperature) {
			this.cpuTemperature = cpuTemperature;
		}

		public void setCpuLoad(String cpuLoad) {
			this.cpuLoad = cpuLoad;
		}

		public void setMemoryUsage(String memoryUsage) {
			this.memoryUsage = memoryUsage;
		}

		public void setDiskUsage(String diskUsage) {
			this.diskUsage = diskUsage;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getCpuTemperature() {
			return cpuTemperature;
		}

		public String getCpuLoad() {
			return cpuLoad;
		}

		public String getMemoryUsage() {
			return memoryUsage;
		}

		public String getDiskUsage() {
			return diskUsage;
		}
	}

	private Response getSystemData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String ipAddress = SystemUtils.getIPAddress();
			String cpuLoad = SystemUtils.getCPULoad();
			String cpuTemperature = SystemUtils.getCPUTemperature();
			String memoryUsage = SystemUtils.getMemoryUsage();
			String diskUsage = SystemUtils.getDiskUsage();
			SystemData systemData = new SystemData();
			systemData.ipAddress = ipAddress;
			systemData.cpuTemperature = cpuTemperature;
			systemData.cpuLoad = cpuLoad;
			systemData.memoryUsage = memoryUsage;
			systemData.diskUsage = diskUsage;

			String content;
			try {
				content = mapper.writeValueAsString(systemData);
			} catch (JsonProcessingException jpe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SERVER-0008-1")
								.errorMessage(jpe.toString())
								.errorStack(HTTPServer.dumpException(jpe)));
				return response;
			}

			if (VERBOSE) {
				this.systemRequestManager.getLogger().log(Level.INFO, String.format("%s => %s", systemData, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0008")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private HTTPServer.Response getSystemDate(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		String content = "";
		try {
			String formattedSysDate = SYSDATE_FMT.format(new Date());
			/*
			 *      30 MAY 2023 07:56:31
			 *      |  |   |    |  |  |
			 *      |  |   |    |  |  18
			 *      |  |   |    |  15
			 *      |  |   |    12
			 *      |  |   7
			 *      |  3
			 *      0
			 */
			Map<String, Object> dateHolder = new HashMap<>();
			dateHolder.put("day", Integer.parseInt(formattedSysDate.substring(0, 2)));
			dateHolder.put("month", formattedSysDate.substring(3, 6).toUpperCase());
			dateHolder.put("year", Integer.parseInt(formattedSysDate.substring(7, 11)));
			dateHolder.put("hours", Integer.parseInt(formattedSysDate.substring(12, 14)));
			dateHolder.put("mins", Integer.parseInt(formattedSysDate.substring(15, 17)));
			dateHolder.put("secs", Integer.parseInt(formattedSysDate.substring(18)));
			content = mapper.writeValueAsString(dateHolder);
			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.APPLICATION_JSON, content.getBytes().length);
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("Get System Date")
							.errorMessage(ex.toString()));
			return response;
		}
		return response;
	}

	/*
	 * On Linux/bash, no password for sudo.
	 * curl -X POST http://192.168.50.10:9999/mux/system-date -d '28 MAY 2023 12:19:00'.
	 */
	private HTTPServer.Response setSystemDate(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);

		String payload = new String(request.getContent());
		if (!"null".equals(payload) && payload != null && payload.trim().length() != 0) {
			try {
				String newDate = payload; // Like "19 APR 2012 11:14:00"
				// Trim quotes
				if (newDate.startsWith("\"") || newDate.endsWith("\"")) {
					newDate = StringUtils.trimDoubleQuotes(newDate);
				}
				String command = String.format("sudo date -s '%s'", newDate);
				System.out.printf("Executing command [%s]\n", command);

				Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command }); // Note the '/bin/bash -c' !!
				int exitCode = process.waitFor();
				System.out.printf("Exit code: %d\n", exitCode);
				List<String> returned = new ArrayList<>();
				BufferedReader in = null;
				if (exitCode == 0) {
					in = new BufferedReader(new InputStreamReader(process.getInputStream()));
				} else {
					in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				}
				while (true) {
					String line = in.readLine();
					System.out.println(line);
					if (line == null) {
						break;
					} else {
						returned.add(line);
					}
				}
				if (in != null) {
					in.close();
				}
				String responsePayload = returned.stream().collect(Collectors.joining("\n"));
				if (exitCode == 0) {
					RESTProcessorUtil.generateResponseHeaders(response, responsePayload.length());
					response.setPayload(responsePayload.getBytes());
				} else {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("Set System Date")
									.errorMessage(responsePayload));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("Set System Date")
								.errorMessage(ex.toString()));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("Set System Date")
							.errorMessage("Request payload not found. Need one like '19 APR 2012 11:14:00'."));
			return response;
		}

		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request Request
	 * @return Response
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}

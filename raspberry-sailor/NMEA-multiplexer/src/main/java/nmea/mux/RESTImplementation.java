package nmea.mux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import gnu.io.CommPortIdentifier;
import http.HTTPServer;
import http.HttpHeaders;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAReader;
import nmea.computers.Computer;
import nmea.computers.ExtraDataComputer;
import nmea.consumers.client.*;
import nmea.consumers.reader.*;
import nmea.forwarders.*;
import nmea.forwarders.rmi.RMIServer;
import nmea.mux.context.Context;
import nmea.mux.context.Context.StringAndTimeStamp;
import nmea.parser.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class defines the REST operations supported by the HTTP Server, including Admin operations
 * like Consumers, Forwarders, and Computers creation, deletion, etc.
 * <p>
 * This list of operations is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * The Multiplexer will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 */
public class RESTImplementation {

	private final static ObjectMapper mapper = new ObjectMapper();

	private final List<NMEAClient> nmeaDataClients;
	private final List<Forwarder> nmeaDataForwarders;
	private final List<Computer> nmeaDataComputers;
	private final Multiplexer mux;

	private final static String REST_PREFIX = "/mux";
	private final static SimpleDateFormat DURATION_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public RESTImplementation(List<NMEAClient> nmeaDataClients,
	                          List<Forwarder> nmeaDataForwarders,
	                          List<Computer> nmeaDataComputers,
	                          Multiplexer mux) {
		this.nmeaDataClients = nmeaDataClients;
		this.nmeaDataForwarders = nmeaDataForwarders;
		this.nmeaDataComputers = nmeaDataComputers;
		this.mux = mux;

		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	private static boolean restVerbose() {
		return "true".equals(System.getProperty("rest.verbose", "false"));
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * Frame path parameters with curly braces.
	 * <p>
	 * See {@link #processRequest(HTTPServer.Request)}
	 * See {@link HTTPServer}
	 */
	private final List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					REST_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations, on NMEA request manager."),
			new Operation(
					"POST",
					REST_PREFIX + "/terminate",
					this::stopAll,
					"Hard stop, shutdown. VERY unusual REST resource..."),
			new Operation(
					"GET",
					REST_PREFIX + "/serial-ports",
					this::getSerialPorts,
					"Get the list of the available serial ports."),
			new Operation(
					"GET",
					REST_PREFIX + "/channels",
					this::getChannels,
					"Get the list of the input channels"),
			new Operation(
					"GET",
					REST_PREFIX + "/forwarders",
					this::getForwarders,
					"Get the list of the output channels"),
			new Operation(
					"GET",
					REST_PREFIX + "/computers",
					this::getComputers,
					"Get the list of the computers"),
			new Operation(
					"GET",
					REST_PREFIX + "/mux-config",
					this::getMuxConfig,
					"Get the full mux config, channels, forwarders, and computers"),
			new Operation(
					"DELETE",
					REST_PREFIX + "/forwarders/{id}",
					this::deleteForwarder,
					"Delete an output channel"),
			new Operation(
					"DELETE",
					REST_PREFIX + "/channels/{id}",
					this::deleteChannel,
					"Delete an input channel"),
			new Operation(
					"DELETE",
					REST_PREFIX + "/computers/{id}",
					this::deleteComputer,
					"Delete a computer"),
			new Operation(
					"POST",
					REST_PREFIX + "/forwarders",
					this::postForwarder,
					"Creates an output channel"),
			new Operation(
					"POST",
					REST_PREFIX + "/channels",
					this::postChannel,
					"Creates an input channel (aka consumer)"),
			new Operation(
					"POST",
					REST_PREFIX + "/computers",
					this::postComputer,
					"Creates computer"),
			new Operation(
					"PUT",
					REST_PREFIX + "/channels/{id}",
					this::putChannel,
					"Update channel"),
			new Operation(
					"PUT",
					REST_PREFIX + "/forwarders/{id}",
					this::putForwarder,
					"Update forwarder"),
			new Operation(
					"PUT",
					REST_PREFIX + "/computers/{id}",
					this::putComputer,
					"Update computer"),
			new Operation(
					"PUT",
					REST_PREFIX + "/mux-verbose/{state}",
					this::putMuxVerbose,
					"Update Multiplexer verbose"),
			new Operation(
					"PUT",
					REST_PREFIX + "/mux-process/{state}",
					this::putMuxProcess,
					"Update Multiplexer processing status. Aka enable/disable logging."),
			new Operation(
					"GET",
					REST_PREFIX + "/mux-process",
					this::getMuxProcess,
					"Get the mux process status (on/off)"),
			new Operation(
					"GET",
					REST_PREFIX + "/cache",
					this::getCache,
					"Get ALL the data in the cache. QS prm: option=tiny|txt"),
			new Operation(
					"DELETE",
					REST_PREFIX + "/cache",
					this::resetCache,
					"Reset the cache"),
			new Operation(
					"GET",
					REST_PREFIX + "/dev-curve",
					this::getDevCurve,
					"Get the deviation curve as a json object"),
			new Operation(
					"GET",
					REST_PREFIX + "/position",
					this::getPosition,
					"Get position from the cache"),
			new Operation(
					"POST",
					REST_PREFIX + "/position",
					this::setPosition,
					"Set position in the cache"),
			new Operation(
					"GET",
					REST_PREFIX + "/distance",
					this::getDistance,
					"Get distance traveled since last reset"),
			new Operation(
					"GET",
					REST_PREFIX + "/delta-alt",
					this::getDeltaAlt,
					"Get delta altitude since last reset"),
			new Operation(
					"GET",
					REST_PREFIX + "/nmea-volume",
					this::getNMEAVolumeStatus,
					"Get the time elapsed and the NMEA volume managed so far"),
			new Operation(
					"GET",
					REST_PREFIX + "/sog-cog",
					this::getSCOG,
					"Get Position, Speed and Course Over Ground (from GPS)"),
			new Operation(
					"POST",
					REST_PREFIX + "/sog-cog",
					this::setSCOG,
					"Set Speed and Course Over Ground"),
			new Operation(
					"GET",
					REST_PREFIX + "/run-data",
					this::getRunData,
					"Get Speed and Course Over Ground, distance, and delta-altitude, in one shot."),
			new Operation(
					"GET",
					REST_PREFIX + "/log-files",
					this::getLogFiles,
					"Download the log files list"),
			new Operation(
					"GET",
					REST_PREFIX + "/system-time",
					this::getSystemTime,
					"Get the system time as a long. Optional QS prm 'fmt': date | duration"),
			new Operation(
					"GET",
					REST_PREFIX + "/log-files/{log-file}",
					this::getLogFile,
					"Download the log file"),
			new Operation(
					"DELETE",
					REST_PREFIX + "/log-files/{log-file}",
					this::deleteLogFile,
					"Delete a given log file"),
			new Operation(
					"POST",
					REST_PREFIX + "/events/{topic}",
					this::broadcastOnTopic,
					"Broadcast event (payload in the body) on specific topic. The {topic} can be a regex."),
			new Operation(
					"GET",
					"/custom-protocol/{content}", // ?uri={content}
					this::customProtocolManager,
					"Manage custom protocol"),
			new Operation( // Example: PUT /mux/utc
					"PUT",
					REST_PREFIX + "/utc",
					this::setCurrentTime,
					"Set 'current' UTC Date."),
			new Operation(
					"GET",
					REST_PREFIX + "/last-sentence",
					this::getLastNMEASentence,
					"Get the last available inbound sentence"),
			new Operation(
					"POST",                               // Feed the cache from REST
					REST_PREFIX + "/nmea-sentence",
					this::feedNMEASentence,
					"Push NMEA or AIS Sentence to cache, after parsing it. NMEA Sentence as text/plain in the body."));

	protected List<Operation> getOperations() {
		if (restVerbose()) {
			System.out.printf("%s => %d operations\n", this.getClass().getName(), this.operations.size());
		}
		return this.operations;
	}

	/**
	 * This is the method to invoke to have a REST request processed as defined above.
	 *
	 * @param request as it comes from the client
	 * @return the actual result.
	 */
	public HTTPServer.Response processRequest(HTTPServer.Request request)
			throws UnsupportedOperationException {
		if (restVerbose()) {
			System.out.println(">> " + request.getResource());
		}
		Optional<Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the path parameters later on.
			HTTPServer.Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private HTTPServer.Response getSerialPorts(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		try {
			List<String> portList = getSerialPortList();
			Object[] portArray = portList.toArray(new Object[0]);
			try {
				String content = mapper.writeValueAsString(portArray); // new Gson().toJson(portArray).toString();
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (JsonProcessingException jpe) {
				jpe.printStackTrace();
			}
		} catch (Error error) {
			response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload()
					.errorCode("MUX-0001")
					.errorMessage(error.toString()));
		}
		return response;
	}

	private HTTPServer.Response getChannels(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		List<Object> channelList = getInputChannelList();
		Object[] channelArray = channelList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[channelList.size()]);
		try {
			String content = mapper.writeValueAsString(channelArray); // new Gson().toJson(channelArray);
			if (restVerbose()) {
				System.out.printf("-- Channels --\n%s\n--------------------\n", content);
				System.out.printf("\tlength: %d\n", content.length());
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response = HTTPServer.buildErrorResponse(response, Response.BAD_REQUEST, new HTTPServer.ErrorPayload()
					.errorCode("MUX-0001")
					.errorMessage(jpe.toString()));
		}
		return response;
	}

	private HTTPServer.Response getForwarders(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		List<Object> forwarderList = getForwarderList();
		Object[] forwarderArray = forwarderList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[forwarderList.size()]);

		String content; // = new Gson().toJson(forwarderArray);
		try {
			content = mapper.writeValueAsString(forwarderArray);
			if (restVerbose()) {
				System.out.printf("-- Forwarders --\n%s\n--------------------\n", content);
				System.out.printf("\tlength: %d\n", content.length());
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
		} catch (JsonProcessingException jpe) {
			content = jpe.getMessage();
			jpe.printStackTrace();
		}
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getComputers(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		List<Object> computerList = getComputerList();
		Object[] computerArray = computerList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[computerList.size()]);

//		String content = new Gson().toJson(computerArray);
		String content;
		try {
			content = mapper.writeValueAsString(computerArray);
			if (restVerbose()) {
				System.out.printf("-- Computers --\n%s\n--------------------\n", content);
				System.out.printf("\tlength: %d\n", content.length());
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
		} catch (JsonProcessingException jpe) {
			content = jpe.getMessage();
			jpe.printStackTrace();
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response getMuxConfig(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		List<Object> channelList = getInputChannelList();
		List<Object> forwarderList = getForwarderList();
		List<Object> computerList = getComputerList();

		Object[] channelArray = channelList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[channelList.size()]);

		Object[] forwarderArray = forwarderList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[forwarderList.size()]);

		Object[] computerArray = computerList.stream()
				.collect(Collectors.toList())
				.toArray(new Object[computerList.size()]);

		Map<String, Object[]> map = new HashMap<>();
		map.put("channels", channelArray);
		map.put("forwarders", forwarderArray);
		map.put("computers", computerArray);

//		String content = new Gson().toJson(map);
		String content;
		try {
			content = mapper.writeValueAsString(map);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
		} catch (JsonProcessingException jpe) {
			content = jpe.getMessage();
			jpe.printStackTrace();
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private HTTPServer.Response deleteForwarder(HTTPServer.Request request) {
		Optional<Forwarder> opFwd;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.ACCEPTED);
//	List<String> prmValues = RESTProcessorUtil.getPathPrmValues(request.getRequestPattern(), request.getPath());
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "console":
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof ConsoleWriter)
							.findFirst();
					response = removeForwarderIfPresent(request, opFwd);
					break;
				case "serial":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							SerialWriter.SerialBean serialBean = mapper.readValue(stringReader, SerialWriter.SerialBean.class); // gson.fromJson(stringReader, SerialWriter.SerialBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof SerialWriter &&
											((SerialWriter) fwd).getPort().equals(serialBean.getPort()))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "file":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							DataFileWriter.DataFileBean dataFileBean = mapper.readValue(stringReader, DataFileWriter.DataFileBean.class); // gson.fromJson(stringReader, DataFileWriter.DataFileBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof DataFileWriter &&
											((DataFileWriter) fwd).getLog().equals(dataFileBean.getLog()))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "tcp":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							TCPServer.TCPBean tcpBean = mapper.readValue(stringReader, TCPServer.TCPBean.class); // gson.fromJson(stringReader, TCPServer.TCPBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof TCPServer &&
											((TCPServer) fwd).getTcpPort() == tcpBean.getPort())
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "rest":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							RESTPublisher.RESTBean restBean = mapper.readValue(stringReader, RESTPublisher.RESTBean.class); // gson.fromJson(stringReader, RESTPublisher.RESTBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof RESTPublisher &&
											((RESTPublisher) fwd).getVerb().equals(restBean.getVerb()) &&
											((RESTPublisher) fwd).getServerName().equals(restBean.getServerName()) &&
											((RESTPublisher) fwd).getHttpPort() == restBean.getPort() &&
											((RESTPublisher) fwd).getRestResource().equals(restBean.getResource()))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "gpsd":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							GPSdServer.GPSdBean gpsdBean = mapper.readValue(stringReader, GPSdServer.GPSdBean.class); // gson.fromJson(stringReader, GPSdServer.GPSdBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof GPSdServer &&
											((GPSdServer) fwd).getTcpPort() == gpsdBean.getPort())
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "rmi":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							RMIServer.RMIBean rmiBean = mapper.readValue(stringReader, RMIServer.RMIBean.class); // gson.fromJson(stringReader, RMIServer.RMIBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof RMIServer &&
											((RMIServer) fwd).getRegistryPort() == rmiBean.getPort())
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "ws":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							WebSocketWriter.WSBean wsBean = mapper.readValue(stringReader, WebSocketWriter.WSBean.class); // gson.fromJson(stringReader, WebSocketWriter.WSBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof WebSocketWriter &&
											((WebSocketWriter) fwd).getWsUri().equals(wsBean.getWsUri()))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "wsp":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							WebSocketProcessor.WSBean wsBean = mapper.readValue(stringReader, WebSocketProcessor.WSBean.class); // gson.fromJson(stringReader, WebSocketProcessor.WSBean.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd instanceof WebSocketProcessor &&
											((WebSocketProcessor) fwd).getWsUri().equals(wsBean.getWsUri()))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "udp":
					response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
					break;
				default:
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							@SuppressWarnings("unchecked")
							Map<String, String> custom = (Map<String, String>) mapper.readValue(stringReader, Object.class); // new Gson().fromJson(stringReader, Object.class);
							opFwd = nmeaDataForwarders.stream()
									.filter(fwd -> fwd.getClass().getName().equals(custom.get("cls")))
									.findFirst();
							response = removeForwarderIfPresent(request, opFwd);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
					}
					break;
			}
		} else {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter");
		}
		return response;
	}

	private HTTPServer.Response deleteChannel(HTTPServer.Request request) {
		Optional<NMEAClient> opClient;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NO_CONTENT);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "file":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							DataFileClient.DataFileBean dataFileBean = mapper.readValue(stringReader, DataFileClient.DataFileBean.class); // gson.fromJson(stringReader, DataFileClient.DataFileBean.class);
							opClient = nmeaDataClients.stream()
									.filter(channel -> channel instanceof DataFileClient &&
											((DataFileClient.DataFileBean) channel.getBean()).getFile().equals(dataFileBean.getFile()))
									.findFirst();
							response = removeChannelIfPresent(request, opClient);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "serial":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							SerialClient.SerialBean serialBean = mapper.readValue(stringReader, SerialClient.SerialBean.class); // gson.fromJson(stringReader, SerialClient.SerialBean.class);
							opClient = nmeaDataClients.stream()
									.filter(channel -> channel instanceof SerialClient &&
											((SerialClient.SerialBean) channel.getBean()).getPort().equals(serialBean.getPort())) // No need for BaudRate
									.findFirst();
							response = removeChannelIfPresent(request, opClient);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "tcp":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							TCPClient.TCPBean tcpBean = mapper.readValue(stringReader, TCPClient.TCPBean.class); // gson.fromJson(stringReader, TCPClient.TCPBean.class);
							opClient = nmeaDataClients.stream()
									.filter(channel -> channel instanceof TCPClient &&
											((TCPClient.TCPBean) channel.getBean()).getPort() == tcpBean.getPort())
									.findFirst();
							response = removeChannelIfPresent(request, opClient);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
				case "ws":
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							WebSocketClient.WSBean wsBean = mapper.readValue(stringReader, WebSocketClient.WSBean.class); // gson.fromJson(stringReader, WebSocketClient.WSBean.class);
							opClient = nmeaDataClients.stream()
									.filter(channel -> channel instanceof WebSocketClient &&
											((WebSocketClient.WSBean) channel.getBean()).getWsUri().equals(wsBean.getWsUri()))
									.findFirst();
							response = removeChannelIfPresent(request, opClient);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
					}
					break;
//				case "bmp180":  // TODO Remove that one and the following ones, based on sensors. Replace with TCP ?
//					opClient = nmeaDataClients.stream()
//							.filter(channel -> channel instanceof BMP180Client)
//							.findFirst();
//					response = removeChannelIfPresent(request, opClient);
//					break;
//				case "bme280":
//					opClient = nmeaDataClients.stream()
//							.filter(channel -> channel instanceof BME280Client)
//							.findFirst();
//					response = removeChannelIfPresent(request, opClient);
//					break;
//				case "lsm303":
//					opClient = nmeaDataClients.stream()
//							.filter(channel -> channel instanceof LSM303Client)
//							.findFirst();
//					response = removeChannelIfPresent(request, opClient);
//					break;
//				case "htu21df":
//					opClient = nmeaDataClients.stream()
//							.filter(channel -> channel instanceof HTU21DFClient)
//							.findFirst();
//					response = removeChannelIfPresent(request, opClient);
//					break;
				case "rnd":
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof RandomClient)
							.findFirst();
					response = removeChannelIfPresent(request, opClient);
					break;
				case "zda":
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof ZDAClient)
							.findFirst();
					response = removeChannelIfPresent(request, opClient);
					break;
				default:
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							@SuppressWarnings("unchecked")
							Map<String, String> custom = (Map<String, String>)mapper.readValue(stringReader, Object.class); // (Map<String, String>) new Gson().fromJson(stringReader, Object.class);
							opClient = nmeaDataClients.stream()
									.filter(channel -> channel.getClass().getName().equals(custom.get("cls")))
									.findFirst();
							response = removeChannelIfPresent(request, opClient);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
					}
					break;
			}
		} else {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter");
		}
		return response;
	}

	private HTTPServer.Response deleteComputer(HTTPServer.Request request) {
		Optional<Computer> opComputer;
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NO_CONTENT);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() == 1) {
			String id = prmValues.get(0);
			switch (id) {
				case "tw-current":
					if (request.getContent() != null) {  // Really? Need that?
						opComputer = nmeaDataComputers.stream()
								.filter(cptr -> cptr instanceof ExtraDataComputer)
								.findFirst();
						response = removeComputerIfPresent(request, opComputer);
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "'tw-current' was not found");
					}
					break;
				default:
					if (request.getContent() != null) {
						StringReader stringReader = new StringReader(new String(request.getContent()));
						try {
							@SuppressWarnings("unchecked")
							Map<String, String> custom = (Map<String, String>)mapper.readValue(stringReader, Object.class); // (Map<String, String>) new Gson().fromJson(stringReader, Object.class);
							opComputer = nmeaDataComputers.stream()
									.filter(cptr -> cptr.getClass().getName().equals(custom.get("cls")))
									.findFirst();
							response = removeComputerIfPresent(request, opComputer);
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
						}
					} else {
						response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
					}
					break;
			}
		} else {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter");
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response postForwarder(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<Forwarder> opFwd;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		switch (type) {
			case "console":
				// Check existence
				opFwd = nmeaDataForwarders.stream()
						.filter(fwd -> fwd instanceof ConsoleWriter)
						.findFirst();
				if (!opFwd.isPresent()) {
					try {
						Forwarder consoleForwarder = new ConsoleWriter();
						nmeaDataForwarders.add(consoleForwarder);
						response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
						String content = mapper.writeValueAsString(consoleForwarder.getBean()); // new Gson().toJson(consoleForwarder.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					} catch (Exception ex) {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
						ex.printStackTrace();
					}
				} else {
					// Already there
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, "'console' already exists");
				}
				break;
			case "serial":
				try {
					SerialWriter.SerialBean serialJson = mapper.readValue(new String(request.getContent()), SerialWriter.SerialBean.class); // new Gson().fromJson(new String(request.getContent()), SerialWriter.SerialBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof SerialWriter &&
									((SerialWriter) fwd).getPort().equals(serialJson.getPort()))
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder serialForwarder = new SerialWriter(serialJson.getPort(), serialJson.getBR());
							nmeaDataForwarders.add(serialForwarder);
							String content = mapper.writeValueAsString(serialForwarder.getBean()); // new Gson().toJson(serialForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'serial' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "tcp":
				try {
					TCPServer.TCPBean tcpJson = mapper.readValue(new String(request.getContent()), TCPServer.TCPBean.class); // new Gson().fromJson(new String(request.getContent()), TCPServer.TCPBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof TCPServer &&
									((TCPServer) fwd).getTcpPort() == tcpJson.getPort())
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder tcpForwarder = new TCPServer(tcpJson.getPort());
							nmeaDataForwarders.add(tcpForwarder);
							String content = mapper.writeValueAsString(tcpForwarder.getBean()); // new Gson().toJson(tcpForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'tcp' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "rest":
				try {
					RESTPublisher.RESTBean restJson = mapper.readValue(new String(request.getContent()), RESTPublisher.RESTBean.class); // new Gson().fromJson(new String(request.getContent()), RESTPublisher.RESTBean.class);
					// Check if not there yet.
					// Check if already exists.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof RESTPublisher &&
									((RESTPublisher) fwd).getVerb().equals(restJson.getVerb()) &&
									((RESTPublisher) fwd).getServerName().equals(restJson.getServerName()) &&
									((RESTPublisher) fwd).getHttpPort() == restJson.getPort() &&
									((RESTPublisher) fwd).getRestResource().equals(restJson.getResource()))
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder restForwarder = new RESTPublisher(restJson.getVerb(), restJson.getServerName(), restJson.getPort(), restJson.getResource());
							nmeaDataForwarders.add(restForwarder);
							String content = mapper.writeValueAsString(restForwarder.getBean()); // new Gson().toJson(restForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'rest' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "gpsd":
				try {
					GPSdServer.GPSdBean gpsdJson = mapper.readValue(new String(request.getContent()), GPSdServer.GPSdBean.class); // new Gson().fromJson(new String(request.getContent()), GPSdServer.GPSdBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof GPSdServer &&
									((GPSdServer) fwd).getTcpPort() == gpsdJson.getPort())
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder gpsdForwarder = new GPSdServer(gpsdJson.getPort());
							nmeaDataForwarders.add(gpsdForwarder);
							String content = mapper.writeValueAsString(gpsdForwarder.getBean()); // new Gson().toJson(gpsdForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'gpsd' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "rmi":
				try {
					RMIServer.RMIBean rmiJson = mapper.readValue(new String(request.getContent()), RMIServer.RMIBean.class); // new Gson().fromJson(new String(request.getContent()), RMIServer.RMIBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof RMIServer &&
									((RMIServer) fwd).getRegistryPort() == rmiJson.getPort())
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder rmiForwarder = new RMIServer(rmiJson.getPort(), rmiJson.getBindingName());
							nmeaDataForwarders.add(rmiForwarder);
							String content = mapper.writeValueAsString(rmiForwarder.getBean()); // new Gson().toJson(rmiForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'rmi' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "file":
				try {
					DataFileWriter.DataFileBean fileJson = mapper.readValue(new String(request.getContent()), DataFileWriter.DataFileBean.class); //  new Gson().fromJson(new String(request.getContent()), DataFileWriter.DataFileBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()   // TODO Something more accurate...
							.filter(fwd -> fwd instanceof DataFileWriter &&
									((DataFileWriter) fwd).getLog().equals(fileJson.getLog()))
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder fileForwarder = null;
							if (fileJson.isTimeBased()) {
								fileForwarder = new DataFileWriter(
										fileJson.getLog(),
										fileJson.append(),
										fileJson.isTimeBased(),
										fileJson.getRadix(),
										fileJson.getDir(),
										fileJson.getSplit(),
										fileJson.isFlush());
							} else {
								fileForwarder = new DataFileWriter(fileJson.getLog(), fileJson.append(), fileJson.isFlush());
							}
							nmeaDataForwarders.add(fileForwarder);
							String content = mapper.writeValueAsString(fileForwarder.getBean()); // new Gson().toJson(fileForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'file' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "ws":
				try {
					WebSocketWriter.WSBean wsJson = mapper.readValue(new String(request.getContent()), WebSocketWriter.WSBean.class); // new Gson().fromJson(new String(request.getContent()), WebSocketWriter.WSBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof WebSocketWriter &&
									((WebSocketWriter) fwd).getWsUri().equals(wsJson.getWsUri()))
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder wsForwarder = new WebSocketWriter(wsJson.getWsUri());
							nmeaDataForwarders.add(wsForwarder);
							String content = mapper.writeValueAsString(wsForwarder.getBean()); // new Gson().toJson(wsForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'ws' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "wsp":
				try {
					WebSocketProcessor.WSBean wspJson = mapper.readValue(new String(request.getContent()), WebSocketProcessor.WSBean.class); // new Gson().fromJson(new String(request.getContent()), WebSocketProcessor.WSBean.class);
					// Check if not there yet.
					opFwd = nmeaDataForwarders.stream()
							.filter(fwd -> fwd instanceof WebSocketProcessor &&
									Objects.equals(((WebSocketProcessor) fwd).getWsUri(), wspJson.getWsUri())) // null-safe
							.findFirst();
					if (!opFwd.isPresent()) {
						try {
							Forwarder wspForwarder = new WebSocketProcessor(wspJson.getWsUri());
							nmeaDataForwarders.add(wspForwarder);
							String content = mapper.writeValueAsString(wspForwarder.getBean()); // new Gson().toJson(wspForwarder.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'wsp' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "custom":
				// String payload = new String(request.getContent());
				try {
					Object custom = mapper.readValue(new String(request.getContent()), Object.class); // new Gson().fromJson(payload, Object.class);
					if (custom instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, String> map = (Map<String, String>) custom;
						String forwarderClass = map.get("forwarderClass").trim();
						String propFile = map.get("propFile");
						// Make sure client and reader are not null
						if (forwarderClass == null || forwarderClass.length() == 0) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "Require at least class name.");
							return response;
						}
						// Check Existence
						opFwd = nmeaDataForwarders.stream()
								.filter(fwd -> fwd.getClass().getName().equals(forwarderClass))
								.findFirst();
						if (!opFwd.isPresent()) {
							try {
								// Create dynamic forwarder
								Object dynamic = Class.forName(forwarderClass).getDeclaredConstructor().newInstance();
								if (dynamic instanceof Forwarder) {
									Forwarder forwarder = (Forwarder) dynamic;

									if (propFile != null && !propFile.trim().isEmpty()) {
										try {
											Properties properties = new Properties();
											properties.load(new FileReader(propFile));
											forwarder.setProperties(properties);
										} catch (Exception ex) {
											// Send message
											response.setStatus(HTTPServer.Response.BAD_REQUEST);
											RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
											ex.printStackTrace();
										}
									}
									nmeaDataForwarders.add(forwarder);
									String content = mapper.writeValueAsString(forwarder.getBean()); // new Gson().toJson(forwarder.getBean());
									RESTProcessorUtil.generateResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} else {
									// Wrong class
									response.setStatus(HTTPServer.Response.BAD_REQUEST);
									RESTProcessorUtil.addErrorMessageToResponse(response, String.format("Expected a Forwarder, found a [%s] instead.", dynamic.getClass().getName()));
								}
							} catch (Exception ex) {
								response.setStatus(HTTPServer.Response.BAD_REQUEST);
								RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
								ex.printStackTrace();
							}
						} else {
							// Already there
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "this 'custom' channel already exists");
						}
					} else {
						// Unknown object, not a Map...
						// Duh ?
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			default:
				response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				RESTProcessorUtil.addErrorMessageToResponse(response, "'" + type + "' not implemented");
				break;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response postChannel(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<NMEAClient> opClient;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
			} catch (JsonProcessingException jpe) {
				throw new RuntimeException(jpe);
			}
		}
		switch (type) {
			case "tcp":
				try {
					TCPClient.TCPBean tcpJson = mapper.readValue(new String(request.getContent()), TCPClient.TCPBean.class); // new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof TCPClient &&
									((TCPClient.TCPBean) channel.getBean()).getPort() == tcpJson.getPort() &&
									((TCPClient.TCPBean) channel.getBean()).getHostname().equals(tcpJson.getHostname()))
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient tcpClient = new TCPClient(tcpJson.getDeviceFilters(), tcpJson.getSentenceFilters(), this.mux);
							tcpClient.initClient();
							tcpClient.setReader(new TCPReader("MUX-TCPReader",
													tcpClient.getListeners(),
													tcpJson.getHostname(),
													tcpJson.getPort(),
													tcpJson.getInitialRequest().trim().length() > 0 ? tcpJson.getInitialRequest() : null,
													tcpJson.isKeepTrying()));
							nmeaDataClients.add(tcpClient);
							tcpClient.startWorking();
							String content = mapper.writeValueAsString(tcpClient.getBean()); //  new Gson().toJson(tcpClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'tcp' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "serial":
				try {
					SerialClient.SerialBean serialJson = mapper.readValue(new String(request.getContent()), SerialClient.SerialBean.class); // new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof SerialClient &&
									((SerialClient.SerialBean) channel.getBean()).getPort().equals(serialJson.getPort()))
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient serialClient = new SerialClient(serialJson.getDeviceFilters(), serialJson.getSentenceFilters(), this.mux);
							serialClient.initClient();
							// TODO Reset Interval ?
							serialClient.setReader(new SerialReader("MUX-SerialReader", serialClient.getListeners(), serialJson.getPort(), serialJson.getBr()));
							nmeaDataClients.add(serialClient);
							serialClient.startWorking();
							String content = mapper.writeValueAsString(serialClient.getBean()); //  new Gson().toJson(serialClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'serial' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "ws":
				try {
					WebSocketClient.WSBean wsJson = mapper.readValue(new String(request.getContent()), WebSocketClient.WSBean.class); // new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
					// Check if not there yet.
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof WebSocketClient &&
									((WebSocketClient.WSBean) channel.getBean()).getWsUri().equals(wsJson.getWsUri()))
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient wsClient = new WebSocketClient(wsJson.getDeviceFilters(), wsJson.getSentenceFilters(), this.mux);
							wsClient.initClient();
							wsClient.setReader(new WebSocketReader("MUX-WSReader", wsClient.getListeners(), wsJson.getWsUri()));
							nmeaDataClients.add(wsClient);
							wsClient.startWorking();
							String content = mapper.writeValueAsString(wsClient.getBean()); // new Gson().toJson(wsClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'ws' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "file":
				try {
					DataFileClient.DataFileBean fileJson = mapper.readValue(new String(request.getContent()), DataFileClient.DataFileBean.class); // new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
					// Check if not there yet.
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof DataFileClient &&
									((DataFileClient.DataFileBean) channel.getBean()).getFile().equals(fileJson.getFile()))
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient fileClient = new DataFileClient(fileJson.getDeviceFilters(), fileJson.getSentenceFilters(), this.mux);
							fileClient.initClient();
							fileClient.setReader(new DataFileReader("MUX-FileReader", fileClient.getListeners(), fileJson.getFile(), fileJson.getPause()));
							nmeaDataClients.add(fileClient);
							fileClient.startWorking();
							String content = mapper.writeValueAsString(fileClient.getBean()); // new Gson().toJson(fileClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'file' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
//			case "bmp180":
//				BMP180Client.BMP180Bean bmp180Json = new Gson().fromJson(new String(request.getContent()), BMP180Client.BMP180Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof BMP180Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					try {
//						NMEAClient bmp180Client = new BMP180Client(bmp180Json.getDeviceFilters(), bmp180Json.getSentenceFilters(), this.mux);
//						bmp180Client.initClient();
//						bmp180Client.setReader(new BMP180Reader("MUX-BMP180Reader", bmp180Client.getListeners()));
//						// To do BEFORE startWorking and AFTER setReader
//						if (bmp180Json.getDevicePrefix() != null) {
//							if (bmp180Json.getDevicePrefix().trim().length() != 2) {
//								throw new RuntimeException(String.format("Device prefix length must be exactly 2. [%s] is not valid", bmp180Json.getDevicePrefix().trim()));
//							} else {
//								((BMP180Client) bmp180Client).setSpecificDevicePrefix(bmp180Json.getDevicePrefix().trim());
//							}
//						}
//						nmeaDataClients.add(bmp180Client);
//						bmp180Client.startWorking();
//						String content = new Gson().toJson(bmp180Client.getBean());
//						RESTProcessorUtil.generateResponseHeaders(response, content.length());
//						response.setPayload(content.getBytes());
//					} catch (Exception ex) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
//						ex.printStackTrace();
//					} catch (Error error) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, "Maybe you are not on a Raspberry Pi...");
//						error.printStackTrace();
//					}
//				} else {
//					// Already there
//					response.setStatus(HTTPServer.Response.BAD_REQUEST);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'bmp180' already exists");
//				}
//				break;
//			case "lsm303":
//				LSM303Client.LSM303Bean lsm303Json = new Gson().fromJson(new String(request.getContent()), LSM303Client.LSM303Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof LSM303Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					try {
//						NMEAClient lsm303Client = new LSM303Client(lsm303Json.getDeviceFilters(), lsm303Json.getSentenceFilters(), this.mux);
//						if (lsm303Json.getHeadingOffset() != 0) {
//							((LSM303Client) lsm303Client).setHeadingOffset(lsm303Json.getHeadingOffset());
//						}
//						if (lsm303Json.getReadFrequency() != null) {
//							((LSM303Client) lsm303Client).setReadFrequency(lsm303Json.getReadFrequency());
//						}
//						lsm303Client.initClient();
//						lsm303Client.setReader(new LSM303Reader("MUX-LSM303Reader", lsm303Client.getListeners()));
//						// To do BEFORE startWorking and AFTER setReader
//						if (lsm303Json.getDevicePrefix() != null) {
//							if (lsm303Json.getDevicePrefix().trim().length() != 2) {
//								throw new RuntimeException(String.format("Device prefix length must be exactly 2. [%s] is not valid", lsm303Json.getDevicePrefix().trim()));
//							} else {
//								((LSM303Client) lsm303Client).setSpecificDevicePrefix(lsm303Json.getDevicePrefix().trim());
//							}
//						}
//						nmeaDataClients.add(lsm303Client);
//						lsm303Client.startWorking();
//						String content = new Gson().toJson(lsm303Client.getBean());
//						RESTProcessorUtil.generateResponseHeaders(response, content.length());
//						response.setPayload(content.getBytes());
//					} catch (Exception ex) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
//						ex.printStackTrace();
//					} catch (Error error) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, "Maybe you are not on a Raspberry Pi...");
//						error.printStackTrace();
//					}
//				} else {
//					// Already there
//					response.setStatus(HTTPServer.Response.BAD_REQUEST);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'lsm303' already exists");
//				}
//				break;
			case "zda":
				try {
					ZDAClient.ZDABean zdaJson = mapper.readValue(new String(request.getContent()), ZDAClient.ZDABean.class); // new Gson().fromJson(new String(request.getContent()), ZDAClient.ZDABean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof ZDAClient)
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient zdaClient = new ZDAClient(zdaJson.getDeviceFilters(), zdaJson.getSentenceFilters(), this.mux);
							zdaClient.initClient();
							zdaClient.setReader(new ZDAReader("MUX-ZDAReader", zdaClient.getListeners()));
							// To do BEFORE startWorking and AFTER setReader
							if (zdaJson.getDevicePrefix() != null) {
								if (zdaJson.getDevicePrefix().trim().length() != 2) {
									throw new RuntimeException(String.format("Device prefix length must be exactly 2. [%s] is not valid", zdaJson.getDevicePrefix().trim()));
								} else {
									((ZDAClient) zdaClient).setSpecificDevicePrefix(zdaJson.getDevicePrefix().trim());
								}
							}
							nmeaDataClients.add(zdaClient);
							zdaClient.startWorking();
							String content = mapper.writeValueAsString(zdaClient.getBean()); //  new Gson().toJson(zdaClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						} catch (Error error) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "Maybe you are not on a Raspberry Pi...");
							error.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'zda' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
//			case "bme280":
//				BME280Client.BME280Bean bme280Json = new Gson().fromJson(new String(request.getContent()), BME280Client.BME280Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof BME280Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					try {
//						NMEAClient bme280Client = new BME280Client(bme280Json.getDeviceFilters(), bme280Json.getSentenceFilters(), this.mux);
//						bme280Client.initClient();
//						bme280Client.setReader(new BME280Reader("MUX-BME280Reader", bme280Client.getListeners()));
//						// To do BEFORE startWorking and AFTER setReader
//						if (bme280Json.getDevicePrefix() != null) {
//							if (bme280Json.getDevicePrefix().trim().length() != 2) {
//								throw new RuntimeException(String.format("Device prefix length must be exactly 2. [%s] is not valid", bme280Json.getDevicePrefix().trim()));
//							} else {
//								((BME280Client) bme280Client).setSpecificDevicePrefix(bme280Json.getDevicePrefix().trim());
//							}
//						}
//						nmeaDataClients.add(bme280Client);
//						bme280Client.startWorking();
//						String content = new Gson().toJson(bme280Client.getBean());
//						RESTProcessorUtil.generateResponseHeaders(response, content.length());
//						response.setPayload(content.getBytes());
//					} catch (Exception ex) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
//						ex.printStackTrace();
//					} catch (Error error) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, "Maybe you are not on a Raspberry Pi...");
//						error.printStackTrace();
//					}
//				} else {
//					// Already there
//					response.setStatus(HTTPServer.Response.BAD_REQUEST);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'bme280' already exists");
//				}
//				break;
//			case "htu21df":
//				HTU21DFClient.HTU21DFBean htu21dfJson = new Gson().fromJson(new String(request.getContent()), HTU21DFClient.HTU21DFBean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof HTU21DFClient)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					try {
//						NMEAClient htu21dfClient = new HTU21DFClient(htu21dfJson.getDeviceFilters(), htu21dfJson.getSentenceFilters(), this.mux);
//						htu21dfClient.initClient();
//						htu21dfClient.setReader(new HTU21DFReader("MUX-HTU21DFReader", htu21dfClient.getListeners()));
//						// To do BEFORE startWorking and AFTER setReader
//						if (htu21dfJson.getDevicePrefix() != null) {
//							if (htu21dfJson.getDevicePrefix().trim().length() != 2) {
//								throw new RuntimeException(String.format("Device prefix length must be exactly 2. [%s] is not valid", htu21dfJson.getDevicePrefix().trim()));
//							} else {
//								((HTU21DFClient) htu21dfClient).setSpecificDevicePrefix(htu21dfJson.getDevicePrefix().trim());
//							}
//						}
//						nmeaDataClients.add(htu21dfClient);
//						htu21dfClient.startWorking();
//						String content = new Gson().toJson(htu21dfClient.getBean());
//						RESTProcessorUtil.generateResponseHeaders(response, content.length());
//						response.setPayload(content.getBytes());
//					} catch (Exception ex) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
//						ex.printStackTrace();
//					} catch (Error error) {
//						response.setStatus(HTTPServer.Response.BAD_REQUEST);
//						RESTProcessorUtil.addErrorMessageToResponse(response, "Maybe you are not on a Raspberry Pi...");
//						error.printStackTrace();
//					}
//				} else {
//					// Already there
//					response.setStatus(HTTPServer.Response.BAD_REQUEST);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'htu21df' already exists");
//				}
//				break;
			case "rnd":
				try {
					RandomClient.RandomBean rndJson = mapper.readValue(new String(request.getContent()), RandomClient.RandomBean.class); // new Gson().fromJson(new String(request.getContent()), RandomClient.RandomBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof RandomClient)
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient rndClient = new RandomClient(rndJson.getDeviceFilters(), rndJson.getSentenceFilters(), this.mux);
							rndClient.initClient();
							rndClient.setReader(new RandomReader("MUX-RndReader", rndClient.getListeners()));
							nmeaDataClients.add(rndClient);
							rndClient.startWorking();
							String content = mapper.writeValueAsString(rndClient.getBean()); // new Gson().toJson(rndClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'rnd' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
					ex.printStackTrace();
				}
				break;
			case "rest":
				try {
					RESTClient.RESTBean restJson = mapper.readValue(new String(request.getContent()), RESTClient.RESTBean.class); // new Gson().fromJson(new String(request.getContent()), RandomClient.RandomBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof RESTClient &&
									((RESTClient.RESTBean) channel.getBean()).getHostname().equals(restJson.getHostname())  &&
									((RESTClient.RESTBean) channel.getBean()).getPort() == restJson.getPort()  &&
									((RESTClient.RESTBean) channel.getBean()).getQueryPath().equals(restJson.getQueryPath()) /* && TODO other conditions ? */)
							.findFirst();
					if (!opClient.isPresent()) {
						try {
							NMEAClient restClient = new RESTClient(restJson.getDeviceFilters(), restJson.getSentenceFilters(), this.mux);
							restClient.initClient();
							restClient.setReader(new RESTReader("MUX-RESTReader",
									restClient.getListeners(),
									restJson.getProtocol(),
									restJson.getHostname(),
									restJson.getPort(),
									restJson.getQueryPath(),
									restJson.getQueryString(),
									restJson.getJsonQueryString(),
									restJson.getFrequency()));
							nmeaDataClients.add(restClient);
							restClient.startWorking();
							String content = mapper.writeValueAsString(restClient.getBean()); // new Gson().toJson(rndClient.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'REST' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
					ex.printStackTrace();
				}
				break;
			case "custom":
//				String payload = new String(request.getContent());
				try {
					Object custom = mapper.readValue(new String(request.getContent()), Object.class); // new Gson().fromJson(payload, Object.class);
					if (custom instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) custom;
						String clientClass = ((String) map.get("clientClass")).trim();
						String readerClass = ((String) map.get("readerClass")).trim();
						String propFile = (String) map.get("propFile");
						List<String> deviceFilters = (List<String>) map.get("deviceFilters");
						List<String> sentenceFilters = (List<String>) map.get("sentenceFilters");
						// Make sure client and reader are not null
						if (clientClass == null || clientClass.length() == 0 || readerClass == null || readerClass.length() == 0) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "Require at least both class and reader name.");
							return response;
						}
						// Check Existence
						opClient = nmeaDataClients.stream()
								.filter(channel -> channel.getClass().getName().equals(clientClass))
								.findFirst();
						if (!opClient.isPresent()) {
							try {
								// Create
								String[] devFilters = null;
								String[] senFilters = null;
								if (deviceFilters != null && deviceFilters.size() > 0 && deviceFilters.get(0).length() > 0) {
									List<String> devList = deviceFilters.stream().map(String::trim).collect(Collectors.toList());
									devFilters = new String[devList.size()];
									devFilters = devList.toArray(devFilters);
								}
								if (sentenceFilters != null && sentenceFilters.size() > 0 && sentenceFilters.get(0).length() > 0) {
									List<String> senList = sentenceFilters.stream().map(String::trim).collect(Collectors.toList());
									senFilters = new String[senList.size()];
									senFilters = senList.toArray(senFilters);
								}
								Object dynamic = Class.forName(clientClass)
										.getDeclaredConstructor(String[].class, String[].class, Multiplexer.class)
										.newInstance(devFilters, senFilters, this);
								if (dynamic instanceof NMEAClient) {
									NMEAClient nmeaClient = (NMEAClient) dynamic;

									if (propFile != null && !propFile.trim().isEmpty()) {
										try {
											Properties properties = new Properties();
											properties.load(new FileReader(propFile));
											nmeaClient.setProperties(properties);
										} catch (Exception ex) {
											// Send message
											response.setStatus(HTTPServer.Response.BAD_REQUEST);
											RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
											ex.printStackTrace();
										}
									}
									nmeaClient.initClient();
									NMEAReader reader = null;
									try {
										// Cannot invoke declared constructor with a generic type... :(
										reader = (NMEAReader) Class.forName(readerClass).getDeclaredConstructor(String.class, List.class).newInstance("MUX-" + readerClass, nmeaClient.getListeners());
									} catch (Exception ex) {
										response.setStatus(HTTPServer.Response.BAD_REQUEST);
										RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
										ex.printStackTrace();
									}
									if (reader != null) {
										nmeaClient.setReader(reader);
									}
									nmeaDataClients.add(nmeaClient);
									nmeaClient.startWorking();
									String content = mapper.writeValueAsString(nmeaClient.getBean()); // new Gson().toJson(nmeaClient.getBean());
									RESTProcessorUtil.generateResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} else {
									// Wrong class
									response.setStatus(HTTPServer.Response.BAD_REQUEST);
									RESTProcessorUtil.addErrorMessageToResponse(response, String.format("Expected an NMEAClient, found a [%s] instead.", dynamic.getClass().getName()));
								}
							} catch (Exception ex) {
								response.setStatus(HTTPServer.Response.BAD_REQUEST);
								RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
								ex.printStackTrace();
							}
						} else {
							// Already there
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "this 'custom' channel already exists");
						}
					} else {
						// Unknown object, not a Map...
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			default:
				response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				break;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response postComputer(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<Computer> opComputer;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		switch (type) {
			case "tw-current":
				try {
					ExtraDataComputer.ComputerBean twJson = mapper.readValue(new String(request.getContent()), ExtraDataComputer.ComputerBean.class); // new Gson().fromJson(new String(request.getContent()), ExtraDataComputer.ComputerBean.class);
					// Check if not there yet.
					opComputer = nmeaDataComputers.stream()
							.filter(channel -> channel instanceof ExtraDataComputer)
							.findFirst();
					if (!opComputer.isPresent()) {
						try {
							String[] timeBuffers = twJson.getTimeBufferLength().split(",");
							List<Long> timeBufferLengths = Arrays.asList(timeBuffers).stream().map(tbl -> Long.parseLong(tbl.trim())).collect(Collectors.toList());
							// Check duplicates
							for (int i = 0; i < timeBufferLengths.size() - 1; i++) {
								for (int j = i + 1; j < timeBufferLengths.size(); j++) {
									if (timeBufferLengths.get(i).equals(timeBufferLengths.get(j))) {
										throw new RuntimeException(String.format("Duplicates in time buffer lengths: %d ms.", timeBufferLengths.get(i)));
									}
								}
							}
							Computer twCurrentComputer = new ExtraDataComputer(this.mux, twJson.getPrefix(), timeBufferLengths.toArray(new Long[timeBufferLengths.size()]));
							nmeaDataComputers.add(twCurrentComputer);
							String content = mapper.writeValueAsString(twCurrentComputer.getBean()); //  new Gson().toJson(twCurrentComputer.getBean());
							RESTProcessorUtil.generateResponseHeaders(response, content.length());
							response.setPayload(content.getBytes());
						} catch (Exception ex) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
							ex.printStackTrace();
						}
					} else {
						// Already there
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'computer' already exists");
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "custom":
				// String payload = new String(request.getContent());
				try {
					Object custom = mapper.readValue(new String(request.getContent()), Object.class); // new Gson().fromJson(payload, Object.class);
					if (custom instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, String> map = (Map<String, String>) custom;
						String computerClass = map.get("computerClass").trim();
						String propFile = map.get("propFile");
						// Make sure client and reader are not null
						if (computerClass == null || computerClass.length() == 0) {
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "Require at least class name.");
							return response;
						}
						// Check Existence
						opComputer = nmeaDataComputers.stream()
								.filter(fwd -> fwd.getClass().getName().equals(computerClass))
								.findFirst();
						if (!opComputer.isPresent()) {
							try {
								// Create
								Object dynamic = Class.forName(computerClass).getDeclaredConstructor(Multiplexer.class).newInstance(this);
								if (dynamic instanceof Computer) {
									Computer computer = (Computer) dynamic;

									if (propFile != null && !propFile.trim().isEmpty()) {
										try {
											Properties properties = new Properties();
											properties.load(new FileReader(propFile));
											computer.setProperties(properties);
										} catch (Exception ex) {
											// Send message
											response.setStatus(HTTPServer.Response.BAD_REQUEST);
											RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
											ex.printStackTrace();
										}
									}
									nmeaDataComputers.add(computer);
									String content = mapper.writeValueAsString(computer.getBean()); //  new Gson().toJson(computer.getBean());
									RESTProcessorUtil.generateResponseHeaders(response, content.length());
									response.setPayload(content.getBytes());
								} else {
									// Wrong class
									response.setStatus(HTTPServer.Response.BAD_REQUEST);
									RESTProcessorUtil.addErrorMessageToResponse(response, String.format("Expected a Computer, found a [%s] instead.", dynamic.getClass().getName()));
								}
							} catch (Exception ex) {
								response.setStatus(HTTPServer.Response.BAD_REQUEST);
								RESTProcessorUtil.addErrorMessageToResponse(response, ex.toString());
								ex.printStackTrace();
							}
						} else {
							// Already there
							response.setStatus(HTTPServer.Response.BAD_REQUEST);
							RESTProcessorUtil.addErrorMessageToResponse(response, "this 'custom' channel already exists");
						}
					} else {
						// Unknown object, not a Map...
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			default:
				response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				break;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response putChannel(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<NMEAClient> opClient;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
				List<String> prmValues = request.getPathParameters();
				if (prmValues.size() == 1) {
					String id = prmValues.get(0);
					if (!type.equals(id)) {
						response.setStatus(HTTPServer.Response.BAD_REQUEST);
						RESTProcessorUtil.addErrorMessageToResponse(response, String.format("path and payload do not match. path:[%s], payload:[%s]", id, type));
						return response;
					}
				} else {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, "required path parameter was not found");
					return response;
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		switch (type) {
			case "serial":
				try {
					SerialClient.SerialBean serialJson = mapper.readValue(new String(request.getContent()), SerialClient.SerialBean.class); // new Gson().fromJson(new String(request.getContent()), SerialClient.SerialBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof SerialClient &&
									((SerialClient.SerialBean) channel.getBean()).getPort().equals(serialJson.getPort()))
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'serial' was not found");
					} else { // Then update
						SerialClient serialClient = (SerialClient) opClient.get();
						serialClient.setVerbose(serialJson.getVerbose());
						String content = mapper.writeValueAsString(serialClient.getBean()); // new Gson().toJson(serialClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "file":
				try {
					DataFileClient.DataFileBean fileJson = mapper.readValue(new String(request.getContent()), DataFileClient.DataFileBean.class); // new Gson().fromJson(new String(request.getContent()), DataFileClient.DataFileBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof DataFileClient &&
									((DataFileClient.DataFileBean) channel.getBean()).getFile().equals(fileJson.getFile()))
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'file' was not found");
					} else { // Then update
						DataFileClient dataFileClient = (DataFileClient) opClient.get();
						dataFileClient.setVerbose(fileJson.getVerbose());
						dataFileClient.setLoop(fileJson.getLoop());
						String content = mapper.writeValueAsString(dataFileClient.getBean()); // new Gson().toJson(dataFileClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "tcp":
				try {
					TCPClient.TCPBean tcpJson = mapper.readValue(new String(request.getContent()), TCPClient.TCPBean.class); // new Gson().fromJson(new String(request.getContent()), TCPClient.TCPBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof TCPClient &&
									((TCPClient.TCPBean) channel.getBean()).getHostname().equals(tcpJson.getHostname()) &&
									((TCPClient.TCPBean) channel.getBean()).getPort() == tcpJson.getPort())
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'tcp' was not found");
					} else { // Then update
						TCPClient tcpClient = (TCPClient) opClient.get();
						tcpClient.setVerbose(tcpJson.getVerbose());
						String content = mapper.writeValueAsString(tcpClient.getBean()); // new Gson().toJson(tcpClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			case "ws":
				try {
					WebSocketClient.WSBean wsJson = mapper.readValue(new String(request.getContent()), WebSocketClient.WSBean.class); // new Gson().fromJson(new String(request.getContent()), WebSocketClient.WSBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof WebSocketClient &&
									((WebSocketClient.WSBean) channel.getBean()).getWsUri().equals(wsJson.getWsUri()))
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'ws' was not found");
					} else { // Then update
						WebSocketClient webSocketClient = (WebSocketClient) opClient.get();
						webSocketClient.setVerbose(wsJson.getVerbose());
						String content = mapper.writeValueAsString(webSocketClient.getBean()); // new Gson().toJson(webSocketClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
//			case "bmp180":
//				BMP180Client.BMP180Bean bmp180Json = new Gson().fromJson(new String(request.getContent()), BMP180Client.BMP180Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof BMP180Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					response.setStatus(HTTPServer.Response.NOT_FOUND);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'bmp180' was not found");
//				} else { // Then update
//					BMP180Client bmp180Client = (BMP180Client) opClient.get();
//					bmp180Client.setVerbose(bmp180Json.getVerbose());
//					String content = new Gson().toJson(bmp180Client.getBean());
//					RESTProcessorUtil.generateResponseHeaders(response, content.length());
//					response.setPayload(content.getBytes());
//				}
//				break;
//			case "bme280":
//				BME280Client.BME280Bean bme280Json = new Gson().fromJson(new String(request.getContent()), BME280Client.BME280Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof BME280Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					response.setStatus(HTTPServer.Response.NOT_FOUND);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'bme280' was not found");
//				} else { // Then update
//					BME280Client bme280Client = (BME280Client) opClient.get();
//					bme280Client.setVerbose(bme280Json.getVerbose());
//					String content = new Gson().toJson(bme280Client.getBean());
//					RESTProcessorUtil.generateResponseHeaders(response, content.length());
//					response.setPayload(content.getBytes());
//				}
//				break;
//			case "lsm303":
//				LSM303Client.LSM303Bean lsm303Json = new Gson().fromJson(new String(request.getContent()), LSM303Client.LSM303Bean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof LSM303Client)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					response.setStatus(HTTPServer.Response.NOT_FOUND);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'lsm303' was not found");
//				} else { // Then update
//					LSM303Client lsm303Client = (LSM303Client) opClient.get();
//					lsm303Client.setVerbose(lsm303Json.getVerbose());
//					String content = new Gson().toJson(lsm303Client.getBean());
//					RESTProcessorUtil.generateResponseHeaders(response, content.length());
//					response.setPayload(content.getBytes());
//				}
//				break;
			case "zda":
				try {
					ZDAClient.ZDABean zdaJson = mapper.readValue(new String(request.getContent()), ZDAClient.ZDABean.class); // new Gson().fromJson(new String(request.getContent()), ZDAClient.ZDABean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof ZDAClient)
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'da' was not found");
					} else { // Then update
						ZDAClient zdaClient = (ZDAClient) opClient.get();
						zdaClient.setVerbose(zdaJson.getVerbose());
						String content = mapper.writeValueAsString(zdaClient.getBean()); // new Gson().toJson(zdaClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
//			case "htu21df":
//				HTU21DFClient.HTU21DFBean htu21dfJson = new Gson().fromJson(new String(request.getContent()), HTU21DFClient.HTU21DFBean.class);
//				opClient = nmeaDataClients.stream()
//						.filter(channel -> channel instanceof HTU21DFClient)
//						.findFirst();
//				if (!opClient.isPresent()) {
//					response.setStatus(HTTPServer.Response.NOT_FOUND);
//					RESTProcessorUtil.addErrorMessageToResponse(response, "this 'htu21df' was not found");
//				} else { // Then update
//					HTU21DFClient htu21DFClient = (HTU21DFClient) opClient.get();
//					htu21DFClient.setVerbose(htu21dfJson.getVerbose());
//					String content = new Gson().toJson(htu21DFClient.getBean());
//					RESTProcessorUtil.generateResponseHeaders(response, content.length());
//					response.setPayload(content.getBytes());
//				}
//				break;
			case "rnd":
				try {
					RandomClient.RandomBean rndJson = mapper.readValue(new String(request.getContent()), RandomClient.RandomBean.class); // new Gson().fromJson(new String(request.getContent()), RandomClient.RandomBean.class);
					opClient = nmeaDataClients.stream()
							.filter(channel -> channel instanceof RandomClient)
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "this 'rnd' was not found");
					} else { // Then update
						RandomClient randomClient = (RandomClient) opClient.get();
						randomClient.setVerbose(rndJson.getVerbose());
						String content = mapper.writeValueAsString(randomClient.getBean()); // new Gson().toJson(randomClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			default:
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> custom = (Map<String, Object>)mapper.readValue(new String(request.getContent()), Object.class); //  new Gson().fromJson(new String(request.getContent()), Object.class);
					opClient = nmeaDataClients.stream()
							.filter(cptr -> cptr.getClass().getName().equals(custom.get("cls")))
							.findFirst();
					if (!opClient.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "'custom' not found");
					} else { // Then update
						NMEAClient nmeaClient = opClient.get();
						boolean verbose = ((Boolean) custom.get("verbose")).booleanValue();
						nmeaClient.setVerbose(verbose);
						String content = mapper.writeValueAsString(nmeaClient.getBean()); // new Gson().toJson(nmeaClient.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
//			response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				break;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response putForwarder(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<NMEAClient> opClient;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			List<String> prmValues = request.getPathParameters();
			if (prmValues.size() == 1) {
				String id = prmValues.get(0);
				if (!type.equals(id)) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, String.format("path and payload do not match. path:[%s], payload:[%s]", id, type));
					return response;
				}
			} else {
				response.setStatus(HTTPServer.Response.BAD_REQUEST);
				RESTProcessorUtil.addErrorMessageToResponse(response, "required path parameter was not found");
				return response;
			}
		}
		switch (type) {
			default:
				response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				break;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response putComputer(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		Optional<Computer> opComputer;
		String type = "";
		if (request.getContent() == null || request.getContent().length == 0) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing payload");
			return response;
		} else {
			try {
				Object bean = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
				if (bean instanceof Map) {
					type = ((Map<String, String>) bean).get("type");
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			List<String> prmValues = request.getPathParameters();
			if (prmValues.size() == 1) {
				String id = prmValues.get(0);
				if (!type.equals(id)) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, String.format("path and payload do not match. path:[%s], payload:[%s]", id, type));
					return response;
				}
			} else {
				response.setStatus(HTTPServer.Response.BAD_REQUEST);
				RESTProcessorUtil.addErrorMessageToResponse(response, "required path parameter was not found");
				return response;
			}
		}
		switch (type) { // TODO More cases?, for TW only, etc...
			case "tw-current":
				try {
					ExtraDataComputer.ComputerBean twJson = mapper.readValue(new String(request.getContent()), ExtraDataComputer.ComputerBean.class); // new Gson().fromJson(new String(request.getContent()), ExtraDataComputer.ComputerBean.class);
					opComputer = nmeaDataComputers.stream()
							.filter(cptr -> cptr instanceof ExtraDataComputer)
							.findFirst();
					if (!opComputer.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "'tw-current' not found");
					} else { // Then update
						ExtraDataComputer computer = (ExtraDataComputer) opComputer.get();
						computer.setVerbose(twJson.isVerbose());
						computer.setPrefix(twJson.getPrefix());
						String content = mapper.writeValueAsString(computer.getBean()); // new Gson().toJson(computer.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
				break;
			default:
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> custom = (Map<String, Object>)mapper.readValue(new String(request.getContent()), Object.class); //  new Gson().fromJson(new String(request.getContent()), Object.class);
					opComputer = nmeaDataComputers.stream()
							.filter(cptr -> cptr.getClass().getName().equals(custom.get("cls")))
							.findFirst();
					if (!opComputer.isPresent()) {
						response.setStatus(HTTPServer.Response.NOT_FOUND);
						RESTProcessorUtil.addErrorMessageToResponse(response, "'custom' not found");
					} else { // Then update
						Computer computer = opComputer.get();
						boolean verbose = ((Boolean) custom.get("verbose")).booleanValue();
						computer.setVerbose(verbose);
						String content = mapper.writeValueAsString(computer.getBean()); // new Gson().toJson(computer.getBean());
						RESTProcessorUtil.generateResponseHeaders(response, content.length());
						response.setPayload(content.getBytes());
					}
				} catch (Exception ex) {
					response.setStatus(HTTPServer.Response.BAD_REQUEST);
					RESTProcessorUtil.addErrorMessageToResponse(response, ex.getMessage());
				}
//			response.setStatus(HTTPServer.Response.NOT_IMPLEMENTED);
				break;
		}
		return response;
	}

	private HTTPServer.Response putMuxVerbose(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter");
			return response;
		}
		boolean newValue = "on".equals(prmValues.get(0));
		this.mux.setVerbose(newValue);
		try {
			// JsonElement jsonElement = new Gson().toJsonTree(newValue);
			String content = mapper.writeValueAsString(newValue); // jsonElement.toString();
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response putMuxProcess(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter");
			return response;
		}
		boolean newValue = "on".equals(prmValues.get(0));
		this.mux.setEnableProcess(newValue);
		try {
//			JsonElement jsonElement = new Gson().toJsonTree(newValue);
			String content = mapper.writeValueAsString(newValue); // jsonElement.toString();
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response getLogFiles(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

//	String findCommand = String.format("find %s -name '*.nmea'", System.getProperty("user.dir", "."));
		// find . -name '*.nmea' -print0 | xargs -0 ls -lisah | awk '{ print $7, $8, $9, $10, $11 }'
		String findCommand = "find . -name '*.nmea' | xargs wc -l";
		try {
			Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", findCommand});
			int exitStatus = process.waitFor();

			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
			List<String> list = new ArrayList<>();
			String line;
			while ((line = stdout.readLine()) != null) {
				list.add(line.trim());
			}
			stdout.close();
			System.out.printf("Find script completed, status %d, found %d files\n", exitStatus, list.size());

			String content = mapper.writeValueAsString(list); // new Gson().toJson(list);
			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MUX-2003")
							.errorMessage(ex.toString()));
			return response;
		}
		return response;
	}

	private HTTPServer.Response getSystemTime(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Map<String, String> qsPrms = request.getQueryStringParameters();
		String fmt = null;
		if (qsPrms != null && qsPrms.get("fmt") != null) {
			fmt = qsPrms.get("fmt");
		}
		String content = "";
		try {
			if (fmt == null) {
				long systemTime = System.currentTimeMillis();
				content = String.valueOf(systemTime);
			} else if (fmt.equals("date")) {
				content = new Date().toString();
			} else if (fmt.equals("duration")) {
				content = DURATION_FMT.format(new Date());
			}
			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MUX-3003")
							.errorMessage(ex.toString()));
			return response;
		}
		return response;
	}

	/**
	 * @param request file name is the first (only) request prm. MUST be URLEncoded, specially if it contains slashes ('/' => %2F)
	 * @return Aha!
	 */
	private HTTPServer.Response getLogFile(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter {log-file-name}");
			return response;
		}
		String logFileName = prmValues.get(0); // Slashes are escaped, as %2F
		try {
			logFileName = URLDecoder.decode(logFileName, StandardCharsets.UTF_8.toString());
		} catch (/*UnsupportedEncoding*/ Exception uee) {
			uee.printStackTrace();
		}
		File file = new File(logFileName);
		if (!file.exists()) {
			response.setStatus(HTTPServer.Response.NOT_FOUND);
			RESTProcessorUtil.addErrorMessageToResponse(response, String.format("File %s was not found.", logFileName));
			return response;
		}
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			boolean go = true;
			while (go) {
				line = br.readLine();
				if (line == null) {
					go = false;
				} else {
					sb.append(line + "\n");
				}
			}
			br.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		String content = sb.toString();
		// Force application/octet-stream to download file. text/plain from HTML page displays the content
		RESTProcessorUtil.generateResponseHeaders(response, "application/octet-stream", content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/**
	 * @param request file name is the first (only) request prm. MUST be URLEncoded, specially if it contains slashes ('/' => %2F)
	 * @return Duh.
	 */
	private HTTPServer.Response deleteLogFile(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.ACCEPTED);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter {log-file-name}");
			return response;
		}
		String logFileName = prmValues.get(0); // Slashes are escaped, as %2F
		try {
			logFileName = URLDecoder.decode(logFileName, StandardCharsets.UTF_8.toString());
		} catch (/*UnsupportedEncoding*/ Exception uee) {
			uee.printStackTrace();
		}
		File file = new File(logFileName);
		if (!file.exists()) {
			response.setStatus(HTTPServer.Response.NOT_FOUND);
			RESTProcessorUtil.addErrorMessageToResponse(response, String.format("File %s was not found.", logFileName));
			return response;
		} else {
			try {
				file.delete();
			} catch (Exception ex) {
				ex.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MUX-2004")
								.errorMessage(ex.toString()));
				return response;
			}
		}
		return response;
	}

	private HTTPServer.Response customProtocolManager(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		List<String> prmValues = request.getPathParameters();
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter {content}");
			return response;
		}
		String protocolContent = prmValues.get(0);

//	  String protocolContent = "";
//		Map<String, String> queryStringParameters = request.getQueryStringParameters();
//		if (queryStringParameters.get("uri") == null) {
//			response.setStatus(HTTPServer.Response.BAD_REQUEST);
//			RESTProcessorUtil.addErrorMessageToResponse(response, "missing query string parameter [uri]");
//			return response;
//		} else {
//			protocolContent = queryStringParameters.get("uri");
//		}
		System.out.println("Managing " + protocolContent);
		Map<String, Object> responsePayload = new HashMap<>(1);
		try {
			responsePayload.put("payload", URLDecoder.decode(protocolContent, "utf-8")); // StandardCharsets.UTF_8 in Java 11 ...
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		try {
			String content = mapper.writeValueAsString(responsePayload); // new Gson().toJson(responsePayload);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	/**
	 * This one is a very unusual REST resource; it kills its own server.
	 * And it is a recursive one, it itself invokes another REST resource.
	 * Hence the thread, see the code.
	 */
	private HTTPServer.Response stopAll(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		boolean ok = true;
		// Needs to be in its own thread, as it will send a GET /exit request, it is a recursive call.
		Thread stopThread = new Thread(mux::stopAll);
		stopThread.start();
		try {
//			JsonElement jsonElement = new Gson().toJsonTree(ok);
			String content = mapper.writeValueAsString(ok); // jsonElement.toString();
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return response;
	}

	private HTTPServer.Response getMuxProcess(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		boolean status = this.mux.getEnableProcess();
		Map<String, Object> map = new HashMap<>(2);
		map.put("started", ApplicationContext.getInstance().getDataCache().getStartTime());
		map.put("processing", status);

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getNMEAVolumeStatus", ex);
//		}
//		String content = jsonElement != null ? jsonElement.toString() : "";
		String content;
		try {
			content = mapper.writeValueAsString(map);
		} catch (JsonProcessingException jpe) {
			content = jpe.getMessage();
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	private transient static final List<String> REMOVE_WHEN_TINY = Arrays.asList(new String[] {
			NMEADataCache.LAST_NMEA_SENTENCE,
			NMEADataCache.GPS_TIME,
//			NMEADataCache.GPS_SOLAR_TIME,
			NMEADataCache.DECLINATION,
			NMEADataCache.LOG,
			NMEADataCache.DAILY_LOG,
			NMEADataCache.WATER_TEMP,
			// NMEADataCache.AIR_TEMP,
			// NMEADataCache.BARO_PRESS,
			// NMEADataCache.RELATIVE_HUMIDITY,
			NMEADataCache.AWA,
			NMEADataCache.AWS,
			NMEADataCache.HDG_COMPASS,
			NMEADataCache.HDG_MAG,
			NMEADataCache.HDG_TRUE,
			NMEADataCache.DEVIATION,
			NMEADataCache.VARIATION,
			NMEADataCache.TWA,
			NMEADataCache.TWS,
			NMEADataCache.TWD,
			NMEADataCache.CSP,
			NMEADataCache.CDR,
			NMEADataCache.XTE,
			NMEADataCache.FROM_WP,
			NMEADataCache.TO_WP,
			NMEADataCache.WP_POS,
			NMEADataCache.DBT,
			NMEADataCache.D2WP,
			NMEADataCache.B2WP,
			NMEADataCache.S2WP,
			NMEADataCache.S2STEER,
			NMEADataCache.LEEWAY,
			NMEADataCache.CMG,
			NMEADataCache.SAT_IN_VIEW,

			NMEADataCache.BATTERY,
			NMEADataCache.CALCULATED_CURRENT,
			NMEADataCache.VDR_CURRENT,

			NMEADataCache.BSP_FACTOR,
			NMEADataCache.AWS_FACTOR,
			NMEADataCache.AWA_OFFSET,
			NMEADataCache.HDG_OFFSET,
			NMEADataCache.MAX_LEEWAY,

			NMEADataCache.DEVIATION_FILE,
			NMEADataCache.DEVIATION_DATA,
			NMEADataCache.DEFAULT_DECLINATION,
			NMEADataCache.DAMPING,

			NMEADataCache.VMG_ON_WIND,
			NMEADataCache.VMG_ON_WP,

			NMEADataCache.ALTITUDE,
			NMEADataCache.SMALL_DISTANCE,
			NMEADataCache.DELTA_ALTITUDE,

			NMEADataCache.PRATE,
			NMEADataCache.DEW_POINT_TEMP,

			NMEADataCache.NMEA_AS_IS,

			NMEADataCache.AIS
	});

	@SuppressWarnings("unchecked")
	private HTTPServer.Response getCache(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		// Tiny object option
		boolean tiny = false;
		boolean txt = false;
		Map<String, String> qsPrms = request.getQueryStringParameters();
		if (qsPrms != null && qsPrms.get("option") != null) {
			tiny = qsPrms.get("option").equals("tiny");
			txt = qsPrms.get("option").equals("txt");
		}
		// The point of truth.
		// For appropriate JSON(/Jackson) rendering, make sure every sub-component is a JavaBean (or at least has getters).
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();

//		JsonElement jsonElement = null;
//		try {
//			// Calculate VMG(s)
//			if (cache != null) {
//				synchronized (cache) {
//				// 	NMEAUtils.calculateVMGs(cache);
//					// TODO See how to use Jackson here.
//					Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create(); // To avoid NaN/Double issues
//					final JsonElement _jsonElement = gson.toJsonTree(cache); // I know, ah shit!
//					//	String str = new Gson().toJson(cache);
//					((JsonObject) _jsonElement).remove(NMEADataCache.DEVIATION_DATA); // Usually useless for the client, drop it.
//					if (tiny || txt) {
//						REMOVE_WHEN_TINY.stream()
//								.forEach(((JsonObject) _jsonElement)::remove);
//					}
//					jsonElement = _jsonElement; // Same as above
//				}
//			}
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, String.format("With cache: %s", (cache == null ? "null" : cache.toString())));
//			// Also try Gson gson = new GsonBuilder().setPrettyPrinting().create();
////			Context.getInstance().getLogger().log(Level.INFO, "With cache (JSON):", (cache == null ? "null" : new Gson().toJson(cache)));
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getCache", ex);
//		}

		if (cache != null && (tiny || txt)) {
			REMOVE_WHEN_TINY.stream()
					.forEach(cache::remove);
		}

		String content = "";
		String specialContentType = null;
		if (txt) { // Transformation into text
			specialContentType = HttpHeaders.TEXT_PLAIN;
			double bsp = 0;
			try {
//				bsp = ((JsonObject) jsonElement).getAsJsonObject(NMEADataCache.BSP).get("speed").getAsDouble();
				bsp = ((Speed)cache.get(NMEADataCache.BSP)).getValue();
			} catch (Exception absorb) {
			}
			double latitude = 0, longitude = 0;
			try {
				latitude = ((GeoPos)cache.get(NMEADataCache.POSITION)).lat;
				longitude = ((GeoPos)cache.get(NMEADataCache.POSITION)).lng;
			} catch (Exception absorb) {
			}
			double sog = 0;
			int cog = 0;
			try {
				sog = ((Speed)cache.get(NMEADataCache.SOG)).getValue();				// Angle360
				cog = (int)Math.round(((Angle360)cache.get(NMEADataCache.COG)).getValue());
			} catch (Exception absorb) {
			}
			String date = "";
			int year = 0, month = 0, day = 0, hours = 0, mins = 0, secs = 0;
			try {
				// Use Calendar (JDK 11)...
				Calendar cal = new GregorianCalendar();
				cal.setTime(((UTCDate)cache.get(NMEADataCache.GPS_SOLAR_TIME)).getValue());
				date = (cache.get(NMEADataCache.GPS_DATE_TIME)).toString();
//				year = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getYear();
				year = cal.get(Calendar.YEAR);
//				month = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getMonth();
				month = cal.get(Calendar.MONTH);
//				day = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getDate(); // .getDay();
				day = cal.get(Calendar.DAY_OF_MONTH);
//				hours = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getHours();
				hours = cal.get(Calendar.HOUR_OF_DAY);
//				mins = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getMinutes();
				mins = cal.get(Calendar.MINUTE);
//				secs = ((UTCDate)cache.get(NMEADataCache.GPS_DATE_TIME)).getValue().getSeconds();
				secs = cal.get(Calendar.SECOND);
			} catch (Exception absorb) {
			}

			int solHours = 0, solMins = 0, solSecs = 0;
			/*
			    "Solar Time": {
					"date": "Apr 21, 2019, 12:51:43 AM",
					"fmtDate": {
						"epoch": 1555833103170,
						"year": 2019,
						"month": 4,
						"day": 21,
						"hour": 7,
						"min": 51,
						"sec": 43
					}
				}, ...
			 */
			try {
				// Use Calendar (JDK 11)
				Calendar cal = new GregorianCalendar();
				cal.setTime(((SolarDate)cache.get(NMEADataCache.GPS_SOLAR_TIME)).getValue());
//				solHours = ((SolarDate)cache.get(NMEADataCache.GPS_SOLAR_TIME)).getValue().getHours();
				solHours = cal.get(Calendar.HOUR_OF_DAY);
//				solMins = ((SolarDate)cache.get(NMEADataCache.GPS_SOLAR_TIME)).getValue().getMinutes();
				solMins = cal.get(Calendar.MINUTE);
//				solSecs = ((SolarDate)cache.get(NMEADataCache.GPS_SOLAR_TIME)).getValue().getSeconds();
				solSecs = cal.get(Calendar.SECOND);
			} catch (Exception absorb) {
			}

			boolean rmcStatus = false;
			try {
				rmcStatus = (boolean)cache.get(NMEADataCache.RMC_STATUS);
			} catch (Exception absorb) {
			}

			// Hum, Press, AirTemp
			double hum = 0, press = 0, airTemp = 0;
			try {
				hum = (double)cache.get(NMEADataCache.RELATIVE_HUMIDITY);
			} catch (Exception absorb) {
			}
			try {
				press = (double)cache.get(NMEADataCache.BARO_PRESS);
			} catch (Exception absorb) {
			}
			try {
				airTemp = (double)cache.get(NMEADataCache.AIR_TEMP);
			} catch (Exception absorb) {
			}

			content = String.format(
					"BSP=%.2f\nLAT=%f\nLNG=%f\nSOG=%.2f\nCOG=%d\nDATE=%s\nYEAR=%d\nMONTH=%d\nDAY=%d\nHOUR=%d\nMIN=%d\nSEC=%d\nS_HOUR=%d\nS_MIN=%d\nS_SEC=%d\nRMC_OK=%s\nBARO=%.2f\nTEMP=%.2f\nHUM=%.2f",
					bsp, latitude, longitude, sog, cog, date, year, month, day, hours, mins, secs, solHours, solMins, solSecs, (rmcStatus ? "OK" : "KO"), press, airTemp, hum);
		} else {
			// specialContentType = HttpHeaders.TEXT_PLAIN_ISO_8859;
			try {
//				final byte[] ba = mapper.writeValueAsBytes(cache);
//				content = new String(ba, "UTF-8");
				content = mapper.writeValueAsString(cache); // jsonElement != null ? jsonElement.toString() : "";
				if (false && content.contains("°")) {
					content = content.replace('°', '*'); // ' '); TODO There must be a better way...
				}
				if (restVerbose()) {
					System.out.printf("-- Requested Cache --\n%s\n--------------------\n", content);
					System.out.printf("\tlength: %d\n", content.length());
				}
			} catch (JsonProcessingException jpe) {
				try {
					// content = jpe.getMessage(); // TODO A more structured error message?... Error, text, return.
					content = mapper.writeValueAsString(jpe);
				} catch (JsonProcessingException jpe2) {
					content = jpe.getMessage();
					jpe2.printStackTrace();
				}
//			} catch (UnsupportedEncodingException uee) {
//				content = uee.getMessage();
			}
		}
		RESTProcessorUtil.generateResponseHeaders(response, specialContentType, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	@SuppressWarnings("unchecked")
	private HTTPServer.Response getDevCurve(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		List<double[]> deviationCurve = (List<double[]>)cache.get(NMEADataCache.DEVIATION_DATA);

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(deviationCurve);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> Get Dev Curve", ex);
//		}
		try {
			String content = deviationCurve != null ? mapper.writeValueAsString(deviationCurve) : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response getPosition(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		GeoPos position = ((GeoPos)ApplicationContext.getInstance().getDataCache().get(NMEADataCache.POSITION)).updateGridSquare();

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(position);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> Get Position", ex);
//		}

		try {
			String content = position != null ? mapper.writeValueAsString(position) : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response setPosition(HTTPServer.Request request) {

		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
			StringReader stringReader = new StringReader(payload);
			try {
				GeoPos position = mapper.readValue(stringReader, GeoPos.class); // gson.fromJson(stringReader, GeoPos.class);
				ApplicationContext.getInstance().getDataCache().put(NMEADataCache.POSITION, position);
			} catch (Exception ex) {
				ex.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MUX-1003")
								.errorMessage(ex.toString()));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MUX-1002")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
	}

	private HTTPServer.Response getSCOG(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Speed sog = (Speed) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.SOG);
		Angle360 cog = (Angle360) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.COG);
		GeoPos position = (GeoPos) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.POSITION);

		Map<String, Object> map = new HashMap<>(2);

		Map<String, Object> sogMap = new HashMap<>(2);
		Map<String, Object> cogMap = new HashMap<>(2);

		sogMap.put("sog", (sog != null) ? sog.getValue() : null);
		sogMap.put("unit", "kt");

		cogMap.put("cog", (cog != null) ? cog.getValue() : null);
		cogMap.put("unit", "deg");

		map.put("sog", sogMap);
		map.put("cog", cogMap);

		map.put("pos", position);

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getSCOG", ex);
//		}
		try {
			String content = map.size() > 0 ? mapper.writeValueAsString(map) : "";
			if (false && content.contains("°")) {
				content = content.replace('°', '*'); // ' '); TODO There must be a better way...
			}
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	/**
	 * Expects (in payload) a map like
	 * <pre>
	 * {
	 *     "sog": {
	 *         "unit": "kt",
	 *         "sog": 12.34
	 *     },
	 *     "cog": {
	 *         "unit": "deg",
	 *         "cog": 123
	 *     }
	 * }
	 * </pre>
	 * @param request the REST request
	 * @return payload like above
	 */
	private HTTPServer.Response setSCOG(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
//			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			try {
				Map data = mapper.readValue(stringReader, Map.class); // gson.fromJson(stringReader, Map.class);
				Double cog = (Double)((Map)data.get("cog")).get("cog");
				Double sog = (Double)((Map)data.get("sog")).get("sog");
//				System.out.printf(">> Setting COG: %f, SOG: %f\n", cog, sog);
				ApplicationContext.getInstance().getDataCache().put(NMEADataCache.COG, new Angle360(cog));
				ApplicationContext.getInstance().getDataCache().put(NMEADataCache.SOG, new Speed(sog));
			} catch (Exception ex) {
				ex.printStackTrace();
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MUX-1013")
								.errorMessage(ex.toString()));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MUX-1012")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
	}

	private HTTPServer.Response getRunData(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		Speed sog = (Speed) cache.get(NMEADataCache.SOG);
		Angle360 cog = (Angle360) cache.get(NMEADataCache.COG);

		Map<String, Object> map = new HashMap<>(5);

		Map<String, Object> sogMap = new HashMap<>(2);
		Map<String, Object> cogMap = new HashMap<>(2);

		sogMap.put("sog", (sog != null) ? sog.getValue() : null);
		sogMap.put("unit", "kt");

		cogMap.put("cog", (cog != null) ? cog.getValue() : null);
		cogMap.put("unit", "deg");

		Double dist = (Double) cache.get(NMEADataCache.SMALL_DISTANCE);

		Map<String, Object> distMap = new HashMap<>(2);
		distMap.put("distance", dist);
		distMap.put("unit", "nm");

		Double delta = (Double) cache.get(NMEADataCache.DELTA_ALTITUDE);
		Double altitude = (Double) cache.get(NMEADataCache.ALTITUDE);
		Map<String, Object> altMap = new HashMap<>(2);
		altMap.put("delta-altitude", delta);
		altMap.put("altitude", altitude);
		altMap.put("unit", "m");

		map.put("started", cache.getStartTime());
		map.put("sog", sogMap);
		map.put("cog", cogMap);
		map.put("dist", distMap);
		map.put("alt", altMap);

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getRunData", ex);
//		}
		try {
			String content = map.size() > 0 ? mapper.writeValueAsString(map) : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response getDistance(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Double dist = (Double) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.SMALL_DISTANCE);

		Map<String, Object> map = new HashMap<>(2);
		map.put("distance", dist);
		map.put("unit", "nm");

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getDistance", ex);
//		}
		try {
			String content = mapper.writeValueAsString(map); // jsonElement != null ? jsonElement.toString() : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response getDeltaAlt(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Double delta = (Double) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.DELTA_ALTITUDE);

		Map<String, Object> map = new HashMap<>(2);
		map.put("delta-altitude", delta);
		map.put("unit", "m");

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getDeltaAlt", ex);
//		}
		try {
			String content = mapper.writeValueAsString(map); // jsonElement != null ? jsonElement.toString() : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response resetCache(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.ACCEPTED);

		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		cache.reset();
		// Also reset computers
		nmeaDataComputers.stream()
				.filter(channel -> channel instanceof ExtraDataComputer)
				.forEach(extraDataComputer -> ((ExtraDataComputer) extraDataComputer).resetCurrentComputers());
		RESTProcessorUtil.generateResponseHeaders(response, 0);

		return response;
	}

	private HTTPServer.Response getNMEAVolumeStatus(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Map<String, Long> map = new HashMap<>(2);
		map.put("started", ApplicationContext.getInstance().getDataCache().getStartTime());
		map.put("nmea-bytes", Context.getInstance().getManagedBytes());

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getNMEAVolumeStatus", ex);
//		}
		try {
			String content = mapper.writeValueAsString(map); // jsonElement != null ? jsonElement.toString() : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	private HTTPServer.Response getLastNMEASentence(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		Map<String, Object> map = new HashMap<>(2);
		StringAndTimeStamp lastData = Context.getInstance().getLastDataSentence();
		map.put("timestamp", lastData.getTimestamp());
		map.put("last-data", lastData.getString());

//		JsonElement jsonElement = null;
//		try {
//			jsonElement = new Gson().toJsonTree(map);
//		} catch (Exception ex) {
//			Context.getInstance().getLogger().log(Level.INFO, "Managed >>> getLastNMEASentence", ex);
//		}
		try {
			String content = mapper.writeValueAsString(map); // jsonElement != null ? jsonElement.toString() : "";
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	/**
	 * "Implicit" REST Channel (Consumer)
	 *
	 * @param request Request
	 * @return Bam!
	 */
	private HTTPServer.Response feedNMEASentence(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent()); // NMEA Sentence. Assume type is text/plain
			if (!"null".equals(payload)) {
				final StringBuilder sb = new StringBuilder();
				request.getHeaders().forEach((name, value) -> {
					if (name.equalsIgnoreCase("Content-Type")) {
						sb.append(value);
					}
				});
				if (sb.length() > 0) {
					String contentType = sb.toString().trim();
					if (!contentType.equals("text/plain")) {
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("MUX-1005")
										.errorMessage(String.format("Unexpected Content-Type [%s]. Should be text/plain", contentType)));
						return response;
					}
				}
				try {
					// Verbose
					if ("true".equals(System.getProperty("rest.feeder.verbose"))) {
						System.out.printf("REST Feed: %s\n", payload);
					}
					// Parse NMEA/AIS Data. See System variable put.ais.in.cache
					// Push UTC Date in the cache
					NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
					if (cache == null) {
						// Error!
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("MUX-0005")
										.errorMessage("Cache not initialized!"));
					} else {
						if (this.mux != null) {
							if ("true".equals(System.getProperty("rest.feeder.verbose"))) {
								System.out.println("REST Feeder: There IS a mux, using regular onData method.");
							}
							synchronized (this.mux) {
								this.mux.onData(payload.trim());
							}
						} else {
							// Push here, auto-parse
							if ("true".equals(System.getProperty("rest.feeder.verbose"))) {
								System.out.println("REST Feeder: There is NO mux, just feeding the cache");
							}
							cache.parseAndFeed(payload.trim());
						}
					}
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("MUX-1000")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MUX-1001")
								.errorMessage("Required body payload not found."));
			}
		}
		return response;
	}

	private Response setCurrentTime(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);

		EpochHolder epoch = null;
		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				StringReader stringReader = new StringReader(payload);
				try {
					epoch = mapper.readValue(stringReader, EpochHolder.class); // gson.fromJson(stringReader, EpochHolder.class); // Expect an epoch
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("MUX-0004")
									.errorMessage(ex.toString()));
					return response;
				}
			}
		}
		// Push UTC Date in the cache
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		if (cache == null) {
			// Error!
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MUX-0005")
							.errorMessage("Cache not initialized!"));
		} else {
//			System.out.printf("Setting GPS Time in setCurrentTime: %s%n", epoch.toString());
			cache.put(NMEADataCache.GPS_DATE_TIME, new UTCDate(new Date(epoch.epoch)));
		}
		return response;
	}

	/**
	 * Dynamically composed, based on the <code>operations</code> List.
	 *
	 * @param request REST request
	 * @return list of operations
	 */
	private HTTPServer.Response getOperationList(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
		Operation[] channelArray = operations.stream()
				.collect(Collectors.toList())
				.toArray(new Operation[operations.size()]);
		try {
			String content = mapper.writeValueAsString(channelArray); // new Gson().toJson(channelArray);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (JsonProcessingException jpe) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, jpe.getMessage());
		}
		return response;
	}

	/**
	 * Use this as a temporary placeholder when creating a new operation.
	 *
	 * @param request
	 * @return
	 */
	private HTTPServer.Response emptyOperation(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		return response;
	}

	/**
	 * Used to broadcast an event a component would be listening to.
	 * <p>
	 * This service can be used to broadcast any payload, on any topic.
	 * Whatever component that has subscribed to the topic will receive the event.
	 * </p>
	 * <p>
	 * See {@link Context#addTopicListener(Context.TopicListener)}
	 * </p>
	 *
	 * @param request the REST/HTTP request.
	 * @return the REST/HTTP Response.
	 */
	private HTTPServer.Response broadcastOnTopic(HTTPServer.Request request) {
		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.CREATED);
		List<String> prmValues = request.getPathParameters(); // Path parameters, in the request's url
		if (prmValues.size() != 1) {
			response.setStatus(HTTPServer.Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "missing path parameter {topic}");
			return response;
		}
		String topic = prmValues.get(0);
		if (topic != null) {
			// Get the payload, the one to broadcast
			Object payload = null;
			try {
				payload = mapper.readValue(new String(request.getContent()), Object.class); // new GsonBuilder().create().fromJson(new String(request.getContent()), Object.class);
			} catch (Exception ex) {
				// No payload
			}
			// Broadcast, the actual job
			Context.getInstance().broadcastOnTopic(topic, payload);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), Response.BAD_REQUEST);
			RESTProcessorUtil.addErrorMessageToResponse(response, "Topic cannot be null");
		}
		return response;
	}

	private HTTPServer.Response removeForwarderIfPresent(HTTPServer.Request request, Optional<Forwarder> opFwd) {
		HTTPServer.Response response;
		if (opFwd.isPresent()) {
			Forwarder forwarder = opFwd.get();
			forwarder.close();
			nmeaDataForwarders.remove(forwarder);
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NO_CONTENT);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_FOUND);
			RESTProcessorUtil.addErrorMessageToResponse(response, "forwarder not found");
		}
		return response;
	}

	private HTTPServer.Response removeChannelIfPresent(HTTPServer.Request request, Optional<NMEAClient> nmeaClient) {
		HTTPServer.Response response;
		if (nmeaClient.isPresent()) {
			NMEAClient client = nmeaClient.get();
			client.stopDataRead();
			nmeaDataClients.remove(client);
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NO_CONTENT);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_FOUND);
			RESTProcessorUtil.addErrorMessageToResponse(response, "channel not found");
		}
		return response;
	}

	private HTTPServer.Response removeComputerIfPresent(HTTPServer.Request request, Optional<Computer> nmeaComputer) {
		HTTPServer.Response response;
		if (nmeaComputer.isPresent()) {
			Computer computer = nmeaComputer.get();
			computer.close();
			nmeaDataComputers.remove(computer);
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NO_CONTENT);
		} else {
			response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_FOUND);
			RESTProcessorUtil.addErrorMessageToResponse(response, "computer not found");
		}
		return response;
	}

	private static List<String> getSerialPortList() {
		List<String> portList = new ArrayList<>();
		// Opening Serial port
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			portList.add(cpi.getName());
		}
		return portList;
	}

	private List<Object> getInputChannelList() {
		return nmeaDataClients.stream().map(NMEAClient::getBean).collect(Collectors.toList());
	}

	private List<Object> getForwarderList() {
		return nmeaDataForwarders.stream().map(Forwarder::getBean).collect(Collectors.toList());
	}

	private List<Object> getComputerList() {
		return nmeaDataComputers.stream().map(Computer::getBean).collect(Collectors.toList());
	}

	// TODO Make it an AtomicReference ?
	public static class EpochHolder {
		long epoch;

		public EpochHolder epoch(long epoch) {
			this.epoch = epoch;
			return this;
		}
	}
}

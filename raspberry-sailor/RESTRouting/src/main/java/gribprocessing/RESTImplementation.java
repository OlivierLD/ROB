package gribprocessing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gribprocessing.utils.BlindRouting;
import gribprocessing.utils.GRIBUtils;
import gribprocessing.utils.RoutingUtil;
import http.HTTPServer;
import http.HttpHeaders;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import jgrib.GribFile;
import poc.GRIBDump;
import utils.DumpUtil;
import utils.StringUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * The SunFlower will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private final static ObjectMapper mapper = new ObjectMapper();
	private final static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));
	private final static String GRIB_PREFIX = "/grib";

	private final GRIBRequestManager gribRequestManager;

	public RESTImplementation(GRIBRequestManager restRequestManager) {

		this.gribRequestManager = restRequestManager;
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
					GRIB_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations on the GRIB service."),
			new Operation(
					"POST",
					GRIB_PREFIX + "/get-data",
					this::requestGRIBData,
					"Request a GRIB download from the web, and return its json representation."),
			new Operation(
					"POST",
					GRIB_PREFIX + "/process-grib-file",
					this::requestGRIBUpload,
					"GRIB file-input from HTML form, and return its json representation."),
			new Operation(
					"GET",
					GRIB_PREFIX + "/routing-request",
					this::getRoutingRequest,
					"For development. 100% useless otherwise."),
			new Operation(
					"POST",
					GRIB_PREFIX + "/routing",
					this::requestRouting,
					"Request the best route, and return its json (or other) representations.")
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

		List<Operation> opList = this.getOperations();
		String content;
		try {
			content = mapper.writeValueAsString(opList);
		} catch (JsonProcessingException jpe) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0001")
							.errorMessage(jpe.toString())
							.errorStack(HTTPServer.dumpException(jpe)));
			return response;
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * The payload is a list of requests, like this
	 *
	 * { "request": "GFS:65N,45S,130E,110W|2,2|0,6..24|PRMSL,WIND,HGT500,TEMP,WAVES,RAIN" }
	 *
	 * @param request the request
	 * @return
	 */
	private Response requestGRIBData(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("Tx Request: %s", payload));
				}
				StringReader stringReader = new StringReader(payload);
				try {
					GRIBRequest gribRequest = mapper.readValue(stringReader, GRIBRequest.class);
					URL gribURL;
					GRIBDump dump = new GRIBDump();
					if (gribRequest.request.startsWith("file:")) { // Reusing grib file
						if (verbose) {
							System.out.println(String.format("Reusing %s", gribRequest.request));
						}
						gribURL = new URI(gribRequest.request).toURL();
					} else {
						try {
							String dir = gribRequest.directory;
							if (dir == null) {
								dir = ".";
							}
							File location = new File(dir);
							if (!location.exists()) {
								boolean ok = location.mkdirs();
								System.out.printf("Created directory(ies) %s: %s\n", dir, (ok ? "OK" : "failed"));
							} else {
								System.out.printf("Directory(ies) %s already created.\n", dir);
							}
							String gribFileName = "grib.grb";
							System.out.printf(" >> Will pull new GRIB %s into %s\n", gribFileName, dir);
							String generatedGRIBRequest = GRIBUtils.generateGRIBRequest(gribRequest.request);
							System.out.printf("Generated GRIB Request: %s", generatedGRIBRequest);
							GRIBUtils.getGRIB(generatedGRIBRequest, dir, gribFileName, verbose);
							gribURL = new File(dir, gribFileName).toURI().toURL();
						} catch (Exception ex) {
							ex.printStackTrace();
							response = HTTPServer.buildErrorResponse(response,
									Response.BAD_REQUEST,
									new HTTPServer.ErrorPayload()
											.errorCode("GRIB-0004")
											.errorMessage(ex.toString()));
							return response;
						}
					}

					if (verbose) {
						System.out.println(String.format("GRIB Data %s, opening stream.", gribURL.toString()));
					}
					GribFile gf = new GribFile(gribURL.openStream());
					List<GRIBDump.DatedGRIB> expandedGBRIB = dump.getExpandedGRIB(gf);
					String content;
					try {
						content = mapper.writeValueAsString(expandedGBRIB);
//						System.out.println("--- Expanded GRIB ---");
//						System.out.println(content);
//						System.out.println("---------------------");
					} catch (JsonProcessingException jpe) {
						System.err.println("--- requestGRIBData ---");
						jpe.printStackTrace();
						System.err.println("-----------------------");
						response = HTTPServer.buildErrorResponse(response,
								Response.BAD_REQUEST,
								new HTTPServer.ErrorPayload()
										.errorCode("GRIB-0003-1")
										.errorMessage(jpe.toString())
										.errorStack(HTTPServer.dumpException(jpe)));
						return response;
					}
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("GRIB-0003")
									.errorMessage(ex1.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("GRIB-0002")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0002")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
	}

	private final String HEADER_SEPARATOR = "\r\n";
	private final String END_OF_HEADERS = HEADER_SEPARATOR + HEADER_SEPARATOR;


	private static int findByteArrayIndex(byte[] in, byte[] toFind) {
		int idx = -1;
		for (int i=0; i<in.length - toFind.length; i++) {
			boolean match = true;
			for (int j=0; j<toFind.length; j++) {
				if (in[i + j] != toFind[j]) {
					match = false;
					break;
				}
			}
			if (match) {
				idx = i;
				break;
			}
		}
		return idx;
	}
	/**
	 * WiP... Still not good for binaries.
	 * @param request
	 * @return
	 */
	private Response requestGRIBUpload(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		final Map<String, String> headers = request.getHeaders();
		headers.keySet().forEach(k -> System.out.printf("%s: %s\n", k, headers.get(k)));

		String contentType = headers
				.keySet()
				.stream()
				.filter(k -> k.toUpperCase().equalsIgnoreCase("CONTENT-TYPE"))
				.map(k -> headers.get(k).trim())
				.findFirst().orElse(null);

		System.out.printf("Content-Type: [%s]\n", contentType);
		String boundary = "";
		if (contentType.toUpperCase().startsWith("MULTIPART/FORM-DATA")) {
			if (contentType.toUpperCase().contains("BOUNDARY=")) {
				boundary = contentType.substring(contentType.toUpperCase().indexOf("BOUNDARY=") + "BOUNDARY=".length());
			}
		}

		if (request.getContent() != null && request.getContent().length > 0) {
			final byte[] requestContent = request.getContent();
			System.out.printf("Original Request Content length: %d\n", requestContent.length);
			byte[] gribContent = null;
			String payload = "";
			try {
				payload = new String(requestContent, StandardCharsets.UTF_8.toString()); // , StandardCharsets.UTF_16);
			} catch (UnsupportedEncodingException uee) {
				throw new RuntimeException(uee);
			}
            /*
             * Payload is like :
             *
------WebKitFormBoundaryvJgOKXKBW43PaOKs
Content-Disposition: form-data; name="file"; filename="_cache_weather-cache_EastAtlantic.wind.7days.grb"
Content-Type: application/octet-stream

GRIB �  `��!i 

. . .
� 7777
------WebKitFormBoundaryvJgOKXKBW43PaOKs--
             */
			System.out.printf("--- Request Content ---\n%s\n---------\n", payload);

			if (false) {
				// Dump the beginning of the requestContent
				try {
					byte[] head = new byte[1_024];
					for (int i = 0; i < 1_024; i++) { // TODO use System.arraycopy
						head[i] = requestContent[i];
					}
					String[] sa = DumpUtil.dualDump(head);
					if (sa != null) {
						System.out.println(">>> Request Head (1024):");
						for (String s : sa) {
							System.out.println("\t\t" + s);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			String key = boundary;
			if (key.trim().length() == 0) {
				key = payload.substring(0, payload.indexOf(HEADER_SEPARATOR));
			}
			System.out.println("Key: " + key);
			// Find the offset in the byte array
			int headersEnd = payload.indexOf(END_OF_HEADERS) + END_OF_HEADERS.length();

			int contentEnd = payload.lastIndexOf(HEADER_SEPARATOR + "--" + key + "--");
//			int contentEnd = findByteArrayIndex(requestContent, (HEADER_SEPARATOR + "--" + key + "--").getBytes());

			System.out.printf("HeadersEnd:%d, ContentEnd:%d, length:%d\n", headersEnd, contentEnd, (contentEnd - headersEnd));

			// Different versions, for tests
			if (true) {
				try {
					gribContent = payload.substring(headersEnd, contentEnd).getBytes(StandardCharsets.UTF_8.toString());
				} catch (UnsupportedEncodingException uee) {
					throw new RuntimeException(uee);
				}
			}
			if (false) {
//			gribContent = payload.substring(headersEnd, contentEnd).getBytes(); // StandardCharsets.UTF_16);
				int baLength = (contentEnd - headersEnd);
				gribContent = new byte[baLength];
//			ByteBuffer bb = ByteBuffer.wrap(requestContent); // , headersEnd, (contentEnd - headersEnd));
//			bb.get(gribContent, headersEnd, baLength);

				for (int i = 0; i < baLength; i++) {
					// gribContent[i] = requestContent[i + headersEnd];
					byte from_requestContent = requestContent[headersEnd + i];
					byte b_orig = (byte) payload.charAt(i + headersEnd); // -17 returns 0xFD, 0b1111 1101, should be 0b1000 0010
					byte b = b_orig;
//				gribContent[i] = b;

//				byte b = (byte) (0xFF & requestContent[i + headersEnd]);
					if (true && (b > 0x80 || b < 0x00)) {
//					System.out.printf("b: %d 0b%s, 0x%s\n", b, StringUtils.lpad(Integer.toString((int) (b & 0xFF), 2), 8, "0"), StringUtils.lpad(Integer.toString((int) b & 0xFF, 16), 2, "0"));
						b &= 0x7F;
						// (byte)((~(requestContent[i + headersEnd] & 0x7F)) | 0x80);
//					System.out.printf("b: %d 0b%s, 0x%s\n", b, StringUtils.lpad(Integer.toString((int) (b & 0xFF), 2), 8, "0"), StringUtils.lpad(Integer.toString((int) b & 0xFF, 16), 2, "0"));
						b = (byte) (~b);  // 2's complement
//					System.out.printf("b: %d 0b%s, 0x%s\n", b, StringUtils.lpad(Integer.toString((int) (b & 0xFF), 2), 8, "0"), StringUtils.lpad(Integer.toString((int) b & 0xFF, 16), 2, "0"));
						b = (byte) (b | 0x80); // Might be useless
//					System.out.printf("b: %d 0b%s, 0x%s\n", b, StringUtils.lpad(Integer.toString((int) (b & 0xFF), 2), 8, "0"), StringUtils.lpad(Integer.toString((int) b & 0xFF, 16), 2, "0"));
//					System.out.println("-");
					}
					if (i <= 0xF) {
						System.out.printf("b: (0x%s) 0x%s -> 0x%s\n",
								StringUtils.lpad(Integer.toHexString(from_requestContent & 0xFF).toUpperCase(), 2, "0"),
								StringUtils.lpad(Integer.toHexString(b_orig & 0xFF).toUpperCase(), 2, "0"),
								StringUtils.lpad(Integer.toHexString(b & 0xFF).toUpperCase(), 2, "0"));
					}
					gribContent[i] = b; // (byte)((~(requestContent[i + headersEnd] & 0x7F)) | 0x80);
				}
				System.out.printf("Offset diff %d - %d = %d, content length: %d\n", contentEnd, headersEnd, (contentEnd - headersEnd), gribContent.length);
			}
			if (false) {
				int baLength = (contentEnd - headersEnd);
				gribContent = new byte[baLength];
				System.arraycopy(requestContent, headersEnd, gribContent, 0, baLength);
//				for (int i=0; i<baLength; i++) {
//					gribContent[i] = requestContent[headersEnd + i];
//				}
			}

			if (true ) { // some tests...
				final int CHUNK_SIZE = 64;
				// The head
				byte[] head = new byte[CHUNK_SIZE];
				System.arraycopy(gribContent, 0, head, 0, CHUNK_SIZE);
				String[] dd = DumpUtil.dualDump(head);
				System.out.println("Head");
				for (String l : dd) {
					System.out.println(l);
				}
				// The tail
				byte[] tail = new byte[CHUNK_SIZE];
				System.arraycopy(gribContent, gribContent.length - CHUNK_SIZE, tail, 0, CHUNK_SIZE);
				dd = DumpUtil.dualDump(tail);
				System.out.println("Tail");
				for (String l : dd) {
					System.out.println(l);
				}
			}

			// For tests
			if (true) {
				try {
					File outputFile = new File("outputFile.bin");
					try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
						outputStream.write(gribContent);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try {
				GribFile gf = new GribFile(new ByteArrayInputStream(gribContent)); // <- Read the byte stream !
				// new BufferedReader(new InputStreamReader(new ByteArrayInputStream(gribContent)));
				GRIBDump dump = new GRIBDump();
				List<GRIBDump.DatedGRIB> expandedGBRIB = dump.getExpandedGRIB(gf);
				String content;
				try {
					content = mapper.writeValueAsString(expandedGBRIB);
					System.out.println("--- Expanded GRIB ---");
					System.out.println(content);
					System.out.println("---------------------");
				} catch (JsonProcessingException jpe) {
					System.err.println("--- requestGRIBData ---");
					jpe.printStackTrace();
					System.err.println("-----------------------");
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("GRIB-0100")
									.errorMessage(jpe.toString())
									.errorStack(HTTPServer.dumpException(jpe)));
					return response;
				}
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("GRIB-0102")
								.errorMessage(ex.toString())
								.errorStack(HTTPServer.dumpException(ex)));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0101")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
	}

	private static class RoutingRequest {
		double fromL;
		double fromG;
		double toL;
		double toG;
		String startTime;
		String gribName;
		String polarFile;
		String outputType;
		double timeInterval;
		int routingForkWidth;
		int routingStep;
		int limitTWS;
		int limitTWA;
		double speedCoeff;
		double proximity;
		boolean avoidLand = false;
		boolean verbose = false;

		public RoutingRequest fromL(double fromL) {
			this.fromL = fromL;
			return this;
		}
		public RoutingRequest fromG(double fromG) {
			this.fromG = fromG;
			return this;
		}
		public RoutingRequest toL(double toL) {
			this.toL = toL;
			return this;
		}
		public RoutingRequest toG(double toG) {
			this.toG = toG;
			return this;
		}
		public RoutingRequest startTime(String startTime) {
			this.startTime = startTime;
			return this;
		}
		public RoutingRequest gribName(String gribName) {
			this.gribName = gribName;
			return this;
		}
		public RoutingRequest polarFile(String polarFile) {
			this.polarFile = polarFile;
			return this;
		}
		public RoutingRequest outputType(String outputType) {
			this.outputType = outputType;
			return this;
		}
		public RoutingRequest timeInterval(double timeInterval) {
			this.timeInterval = timeInterval;
			return this;
		}
		public RoutingRequest routingForkWidth(int routingForkWidth) {
			this.routingForkWidth = routingForkWidth;
			return this;
		}
		public RoutingRequest routingStep(int routingStep) {
			this.routingStep = routingStep;
			return this;
		}
		public RoutingRequest limitTWS(int limitTWS) {
			this.limitTWS = limitTWS;
			return this;
		}
		public RoutingRequest limitTWA(int limitTWA) {
			this.limitTWA = limitTWA;
			return this;
		}
		public RoutingRequest speedCoeff(double speedCoeff) {
			this.speedCoeff = speedCoeff;
			return this;
		}
		public RoutingRequest proximity(double proximity) {
			this.proximity = proximity;
			return this;
		}
		public RoutingRequest avoidLand(boolean avoidLand) {
			this.avoidLand = avoidLand;
			return this;
		}
		public RoutingRequest verbose(boolean verbose) {
			this.verbose = verbose;
			return this;
		}

		public double getFromL() {
			return fromL;
		}

		public double getFromG() {
			return fromG;
		}

		public double getToL() {
			return toL;
		}

		public double getToG() {
			return toG;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getGribName() {
			return gribName;
		}

		public String getPolarFile() {
			return polarFile;
		}

		public String getOutputType() {
			return outputType;
		}

		public double getTimeInterval() {
			return timeInterval;
		}

		public int getRoutingForkWidth() {
			return routingForkWidth;
		}

		public int getRoutingStep() {
			return routingStep;
		}

		public int getLimitTWS() {
			return limitTWS;
		}

		public int getLimitTWA() {
			return limitTWA;
		}

		public double getSpeedCoeff() {
			return speedCoeff;
		}

		public double getProximity() {
			return proximity;
		}

		public boolean isAvoidLand() {
			return avoidLand;
		}

		public boolean isVerbose() {
			return verbose;
		}
	}

	/**
	 * For dev.
	 * @param request
	 * @return
	 */
	private Response getRoutingRequest(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		RoutingRequest rr = new RoutingRequest()
				.fromL(37.122)
				.fromG(-122.5)
				.toL(-9.75)
				.toG(-139.10)
				.startTime("2017-10-16T07:00:00")
				.gribName("./GRIB_2017_10_16_07_31_47_PDT.grb")
				.polarFile("./samples/CheoyLee42.polar-coeff")
				.outputType("JSON")
				.speedCoeff(0.75)
				.proximity(25.0)
				.timeInterval(24)
				.routingForkWidth(140)
				.routingStep(10)
				.limitTWS(-1)
				.limitTWA(-1)
				.verbose(false);

		String content;
		try {
			content = mapper.writeValueAsString(rr);
		} catch (JsonProcessingException jpe) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0000-1")
							.errorMessage(jpe.toString())
							.errorStack(HTTPServer.dumpException(jpe)));
			return response;
		}
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 *
	 * @param request payload like
	 *  {
	 *     "fromL": 37.122,
	 *     "fromG": -122.5,
	 *     "toL": -9.75,
	 *     "toG": -139.1,
	 *     "startTime": "2017-10-16T07:00:00",
	 *     "gribName": "./GRIB_2017_10_16_07_31_47_PDT.grb",
	 *     "polarFile": "./samples/CheoyLee42.polar-coeff",
	 *     "outputType": "JSON",
	 *     "timeInterval": 24,
	 *     "routingForkWidth": 140,
	 *     "routingStep": 10,
	 *     "limitTWS": -1,
	 *     "limitTWA": -1,
	 *     "speedCoeff": 0.75,
	 *     "proximity": 25,
	 *     "avoidLand": false,
	 *     "verbose": false
	 *  }
	 * @return
	 */
	private Response requestRouting(Request request) {
		if (verbose) {
			System.out.println("Starting routing computation.");
		}

		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("Tx Request: %s", payload));
				}
				StringReader stringReader = new StringReader(payload);
				try {
					RoutingRequest routingRequest = mapper.readValue(stringReader, RoutingRequest.class);
					long before = System.currentTimeMillis();
					RoutingUtil.RoutingResult routing = new BlindRouting().calculate(
							routingRequest.fromL,
							routingRequest.fromG,
							routingRequest.toL,
							routingRequest.toG,
							routingRequest.startTime,
							routingRequest.gribName,
							routingRequest.polarFile,
							routingRequest.outputType, // "JSON",
							routingRequest.timeInterval,
							routingRequest.routingForkWidth,
							routingRequest.routingStep,
							routingRequest.limitTWS,
							routingRequest.limitTWA,
							routingRequest.speedCoeff,
							routingRequest.proximity,
							routingRequest.avoidLand,
							routingRequest.verbose
					);
					long after = System.currentTimeMillis();
					if (verbose) {
						System.out.printf("--- BlindRouting().calculate completed in %d ms ---\n", (after - before));
					}
					if (false) {  // Dev Test. TODO An option in the request ?
						System.out.println("--- Spitting out fullrouting.json ---");
						// Warning: this is a BIIIIIIG file... More than 100Gb for Atlantic crossing
						if (true) {
							routing.getIsochronals().forEach(iso -> {
								try {
									String oneIsochronal = mapper.writeValueAsString(iso);
									System.out.printf("One Isochronal (%d points): %s bytes.\n",
											iso.size(),
											NumberFormat.getNumberInstance().format(oneIsochronal.length()));
								} catch (JsonProcessingException jpe) {
									jpe.printStackTrace();
								}
							});

							String theFullStuff = mapper.writeValueAsString(routing); // That part seems to be quite memory demanding... TODO Tweak the RoutingPoint object.
							System.out.printf("Full routing is %d bytes big.\n", theFullStuff.length());
							//BufferedWriter br = new BufferedWriter(new FileWriter("fullrouting.json"));
							//br.write(theFullStuff);
							//br.close();
						}
						if (false) {
							Thread spitter = new Thread(() -> {
								System.out.println("Thread spitter started");
								try {
									BufferedWriter br = new BufferedWriter(new FileWriter("fullrouting.json"));
									mapper.writeValue(br, routing);
									br.close();
								} catch (Throwable t) {
									t.printStackTrace();
								}
								System.out.println("Thread spitter completed.");
							}, "spitter");
							spitter.start();
						}
						System.out.println("--- Done spitting out fullrouting.json ---");
					}
					if (verbose) {
						System.out.println("--- Preparing response. ---");
					}
					// String content = routing.getBestRoute(); //  new Gson().toJson(routing); - The full object may be too big !!
					String content = mapper.writeValueAsString(routing);
					String contentType = HttpHeaders.APPLICATION_JSON;
					switch (routingRequest.outputType) {
						case "TXT":
							contentType = HttpHeaders.TEXT_PLAIN;
							break;
						case "CSV":
							contentType = "text/csv";
							break;
						case "KML":
							contentType = "application/vnd.google-earth.kml+xml";
							break;
						case "GPX":
							contentType = "application/gpx+xml";
							break;
						case "JSON":
						default:
							break;
					}
					if (verbose) {
						System.out.println("Routing completed.");
					}
//					System.out.println(String.format("Content-type: %s", contentType));
//					System.out.println(String.format("Content:\n%s", content));
					RESTProcessorUtil.generateResponseHeaders(response, contentType, content.length());
					response.setPayload(content.getBytes());
				} catch (Throwable ex1) {  // To include the OutOfMemoryError
					if (true || verbose) {
						System.err.println("--- Exception in Routing Service ---");
						ex1.printStackTrace();
						System.err.println("------------------------------------");
					}
					String errMess = ex1.toString();
					if (true || ex1 instanceof RuntimeException) {
						errMess = Arrays.stream(ex1.getStackTrace())
								.filter(el -> !el.equals(ex1.getStackTrace()[0])) // Except first one
								.map(StackTraceElement::toString)
								.collect(Collectors.joining(" / "));
					}
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("GRIB-0103")
									.errorStack(Arrays.asList(ex1.getStackTrace())
											.stream()
											.map(StackTraceElement::toString)
											.collect(Collectors.toList()))
									.errorMessage(errMess));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("GRIB-0102")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("GRIB-0101")
							.errorMessage("Request payload not found"));
			return response;
		}
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request Request
	 * @return dummy stuff.
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}

	public static class GRIBRequest {
		String request;
		String directory;

		public String getRequest() {
			return request;
		}

		public String getDirectory() {
			return directory;
		}
	}
}

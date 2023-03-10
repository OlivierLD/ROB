package gribprocessing;

import http.HTTPServer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A standalone main GRIBServer, with ONLY the operations implemented in RESTImplementation
 * Start this server, and do a 'curl -X GET http://localhost:9999/grib/oplist
 */
public class GRIBServer {

	private final HTTPServer httpServer;
	private int httpPort = 9999;

	public GRIBServer() {

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		System.out.printf(">>> Running on port %d%n", httpPort);
		this.httpServer = startHttpServer(httpPort, new GRIBRequestManager(this));
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public HTTPServer startHttpServer(int port, GRIBRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
//		newHttpServer.stopRunning();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}

	public static void main(String... args) {
		new GRIBServer();
	}

}

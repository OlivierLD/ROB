package systemrest;

import http.HTTPServer;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entry point. Very Simple.
 * This intends to interact with the system!!!!
 *
 * Non standard, by far.
 * Can be touchy, if not dangerous!
 *
 * Starts and stop the NavServer
 * Change the system date
 * Shutdown the system
 * etc...
 */
public class SystemServer {

	private HTTPServer httpServer = null;
	private int httpPort = 1234;

	public SystemServer() {

		String port = System.getProperty("http.port", "1234");
		httpPort = Integer.parseInt(port);

		boolean verbose = "true".equals(System.getProperty("rest.verbose"));

		System.out.printf("(%s) running on port %d\n", this.getClass().getName(), httpPort);
		this.httpServer = startHttpServer(httpPort, new SystemRequestManager(this));
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public HTTPServer startHttpServer(int port, SystemRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
			System.out.printf("\t>> %s (%s) - Starting HTTP server\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
			System.out.printf("Try curl -X GET http://localhost:%d/oplist \n", port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}

	public static void main(String... args) {
		new SystemServer();
	}
}

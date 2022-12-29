package tiderest;

import astrorest.AstroRequestManager;
import http.HTTPServer;
import tideengine.BackEndTideComputer;

import java.util.List;
import java.util.stream.Collectors;

public class TideServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	private static BackEndTideComputer backEndTideComputer;

	public TideServer() {

		backEndTideComputer = new BackEndTideComputer();

		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}

		if (false) { // Done in TideRequestManager
			try {
				backEndTideComputer.connect();
				backEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println(String.format("Running on port %d", httpPort));
		// TideRequestManager will do a BackEndTideComputer.connect();
		this.httpServer = startHttpServer(httpPort, new TideRequestManager(this));
		// Add astronomical features...
		this.httpServer.addRequestManager(new AstroRequestManager());
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public static void main(String... args) {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("TideServer coming down");
			try {
				if (backEndTideComputer != null && backEndTideComputer.getDataComputer() != null) {
					backEndTideComputer.disconnect();
				} else {
					System.err.println("\tNo DataComputer to disconnect from...");
				}
			} catch (Exception ex) {
				System.err.println("When disconnecting:");
				ex.printStackTrace();
			}
		}));

		new TideServer();
	}

	public HTTPServer startHttpServer(int port, TideRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}
}

package mpsrest;

import http.HTTPServer;

public class MPSServer {

	//private boolean httpVerbose = "true".equals(System.getProperty("http.verbose", "false"));
	private HTTPServer httpServer = null;
	private int httpPort = 9999;
	private MPSRequestManager requestManager;

	public MPSServer() {
		String port = System.getProperty("http.port");
		if (port != null) {
			try {
				httpPort = Integer.parseInt(port);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		System.out.printf("Running on port %d\n", httpPort);
		requestManager = new MPSRequestManager();
		this.httpServer = startHttpServer(httpPort);
	}

	public MPSServer(int port) {
		httpPort = port;
		requestManager = new MPSRequestManager();
		this.httpServer = startHttpServer(httpPort);
	}

	public static void main(String... args) {
		new MPSServer();
	}


	public HTTPServer startHttpServer(int port) {
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
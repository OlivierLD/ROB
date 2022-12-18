package utils.proxyguisample;

import http.HTTPServer;
import http.HttpHeaders;
import http.client.HTTPClient;
// import utils.TimeUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Minimal Swing GUI for a proxy
 */
public class ProxyGUI extends JFrame {

	final private TrafficRawPanel requestPanel;
	final private TrafficRawPanel responsePanel;

	private static int port = 9999; // Default
	private static HTTPServer httpServer;

	public static void main(String... args) throws Exception {
		port = Integer.parseInt(System.getProperty("http.port", String.valueOf(port)));

		Properties props = new Properties();
		props.setProperty("autobind", "true"); // AutoBind test

		System.out.println("For the test, autobind is set to true.");

		httpServer = new HTTPServer(port, props);
		port = httpServer.getPort(); // may have been updated by the autobind

		ProxyGUI proxyGui = new ProxyGUI();
		httpServer.setProxyFunction(proxyGui::proxyImpl);

		httpServer.setPortOpenCallback(proxyGui::portUpdater);

		httpServer.startServer();
		System.out.println("Proxy Started");

	}

	public ProxyGUI(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600, 500);
		this.setTitle(String.format("HTTP Proxy, port %d", httpServer.getPort()));
		this.setLayout(new BorderLayout());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		this.setVisible(true);

		JPanel container = new JPanel();
		requestPanel = new TrafficRawPanel("Request");
		responsePanel = new TrafficRawPanel("Response");

		container.setLayout(new GridLayout(1,2));
		container.add(requestPanel);
		container.add(responsePanel);

		this.add(container);
		this.pack();
	}

	private HTTPServer.Response proxyImpl(HTTPServer.Request request) { // assume request is not null

		// Dump the request in its frame
		// headers
		if (request.getHeaders() != null) {
			Map<String, String> requestHeaders = request.getHeaders();
			requestHeaders.keySet().forEach(k -> {
//			System.out.println(String.format("%s: %s", k, requestHeaders.get(k)));
				requestPanel.addData(String.format("%s: %s", k, requestHeaders.get(k)));
			});
		}

		String requestStr = String.format("%s %s %s", request.getVerb(), request.getPath(true), request.getProtocol());
		requestPanel.addData("------ R e q u e s t --------");
		requestPanel.addData(requestStr);
		requestPanel.addData("-----------------------------");

		// content?
		if (request.getContent() != null && request.getContent().length > 0) {
			if (request.getHeaders() != null && request.getHeaders().get(HttpHeaders.CONTENT_TYPE) != null && HTTPServer.isText(request.getHeaders().get(HttpHeaders.CONTENT_TYPE))) {
				String requestPayload = new String(request.getContent());
				requestPanel.addData(String.format("%s", requestPayload));
			}
		}
		// An HTTPClient makes the received request, and returns the response
		HTTPServer.Response response = null;
		try {
			response = HTTPClient.doRequest(request);
		} catch (Exception ex) {
//		throw new RuntimeException(ex);
			System.err.println(String.format(">>> For request %s %s", request.getVerb(), request.getPath(true)));
			ex.printStackTrace();
		}
//	String rCode = String.format("Response code: %d", response.getStatus());
//	responsePanel.addData(rCode);
		if (response != null && response.getHeaders() != null) {
			Map<String, String> respHeaders = response.getHeaders();
			respHeaders.keySet().forEach(k -> responsePanel.addData(String.format("%s: %s", k, respHeaders.get(k))));
		}
		if (response != null && response.getPayload() != null) {
			if (response.getHeaders() != null && response.getHeaders().get(HttpHeaders.CONTENT_TYPE) != null && HTTPServer.isText(response.getHeaders().get(HttpHeaders.CONTENT_TYPE))) {
				String responsePayload = new String(response.getPayload());
				responsePanel.addData(String.format("%s", responsePayload));
			}
		}
		responsePanel.addData("");
		return response;
	}

	private void portUpdater(Integer newPort) {
		System.out.println("...... New port is now " + newPort);
		this.setTitle(String.format("HTTP Proxy, port %d", httpServer.getPort()));
	}
}

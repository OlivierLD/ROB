package tcp.clients;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple interactive TCP client.
 * Assumes that the server is sending lines (ending with a LF)
 *
 * CLI args: --host:<IP or name> --port:5555
 *
 * See the "/flip", "/exit" user input...
 */
public class SimpleTCPClient {

	private final static boolean parseReturnedJSON = "true".equals(System.getProperty("parse.json.response"));
	private static boolean spitOutDummyReader = "true".equals(System.getProperty("display.server.feed"));

	private final static ObjectMapper mapper = new ObjectMapper(); // Jackson

	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void startConnection(String ip, int port) throws Exception {
		try {
			clientSocket = new Socket(ip, port);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (ConnectException ce) {
			throw new RuntimeException(String.format("ConnectException for %s, port %d", ip, port));
		}
	}

	public String sendMessage(String msg) throws Exception {
		System.out.printf("Client sending message: %s\n", msg);
		out.println(msg);
		/*String resp =*/ return readMessage();
		// return resp;
	}

	public String readMessage() throws Exception {
		synchronized (in) {
			return in.readLine();
		}
	}

	public void stopConnection() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
	}

	private final static String PORT_PREFIX = "--port:";
	private final static String HOST_PREFIX = "--host:";

	private static void displayHelp() {
		System.out.println("Commands entered at the prompt:");
		System.out.println("\t/help : You just did it.");
		System.out.println("\t/exit or /quit to stop the client");
		System.out.println("\t/flip to see/hide the server's output (related to the -Ddisplay.server.feed)");
		System.out.println("\t... Any other non-empty command will be sent to the server.");
	}

	public static void main(String... args) {

		final AtomicInteger port = new AtomicInteger(5_555);
		final AtomicReference<String> host = new AtomicReference<>("127.0.0.1");

		// Parse CLI parameters
		Arrays.asList(args).forEach(arg -> {
			if (arg.startsWith(PORT_PREFIX)) {
				try {
					port.set(Integer.parseInt(arg.substring(PORT_PREFIX.length())));
					System.out.printf("(%s) Port now set to %d\n", SimpleTCPClient.class.getName(), port.get());
				} catch (NumberFormatException nfe) {
					System.out.printf("Invalid port in [%s], keeping default %d\n", arg, port.get());
					System.out.printf("(%s) Host is now %s\n", SimpleTCPClient.class.getName(), host);
				}
			} else if (arg.startsWith(HOST_PREFIX)) {
				host.set(arg.substring(HOST_PREFIX.length()));
			}
		});

		AtomicBoolean keepDummyReading = new AtomicBoolean(true);
		AtomicBoolean keepDummyAlive = new AtomicBoolean(true);

		SimpleTCPClient client = new SimpleTCPClient();
		try {
			client.startConnection(host.get(), port.get());
			System.out.printf("(%s) At the prompt, enter '/help' for help,\n'/exit' to stop,\n'/flip' to see/hide server's output.\nAny non-empty string (sent to server) otherwise.\n", SimpleTCPClient.class.getName());

			Thread mainThread = Thread.currentThread();

			// Reader thread...
			Thread dummyReader = new Thread(() -> {
				while (keepDummyAlive.get()) {
					if (keepDummyReading.get()) {
						try {
							String serverMessage = client.readMessage();
							if (spitOutDummyReader) {
								System.out.printf("\t\tFrom dummy thread: [%s]\n", serverMessage);
							}
							if (serverMessage == null) { // Assume server is dead. Does IOException happen?
								keepDummyAlive.set(false);
							}
						} catch (IOException ex) {
							if (!ex.getMessage().startsWith("Stream closed")) {
								ex.printStackTrace();
							}
							keepDummyAlive.set(false);
							break;
						} catch (Exception ex2) {
							ex2.printStackTrace();
							keepDummyAlive.set(false);
							break;
						}
					} else {
						try {
							synchronized (mainThread) {
								mainThread.notify();
							}
							Thread.sleep(10L); // 500L); // This is gonfled. TODO A better way...
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("Done with dummy reader thread.");
			}, "DummyReader");
			dummyReader.start();

			boolean keepWorking = true;
			while (keepWorking) {
				String request = System.console().readLine("User Request > ");
				if (request.trim().length() > 0) {
					if ("/exit".equals(request.trim()) || "/quit".equals(request.trim())) {
						keepWorking = false;
						keepDummyAlive.set(false);
						dummyReader.interrupt();
					} else if ("/help".equals(request.trim())) {
						displayHelp();
					} else if ("/flip".equals(request.trim())) {
						spitOutDummyReader = !spitOutDummyReader;
						System.out.printf("Setting server output to %b\n", spitOutDummyReader);
					} else {
						keepDummyReading.set(false);
						try {
							synchronized (mainThread) {
								mainThread.wait();
								System.out.println("\tClient main thread was released.");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						String response = client.sendMessage(request);
						keepDummyReading.set(true);
                        System.out.printf(">> Server responded %s\n", response);
						// Parse as JSON ?
						if (parseReturnedJSON) {
							try {
								final Map<String, Object> sensorData = mapper.readValue(response, Map.class);
								sensorData.forEach((k, v) -> System.out.printf("- Key: %s, Value (%s): %s\n", k, v.getClass().getName(), v));
							} catch (Throwable boom) {
								System.out.println("-- Not a JSON string/Map... --");
								boom.printStackTrace();
								System.out.println("------------------------------");
							}
						} else {
							System.out.println("Leaving data as-is.");
						}
					}
				} else {
					System.out.println("... Enter something!");
				}
			}
			System.out.printf("(%s) Client exiting\n", SimpleTCPClient.class.getName());
			client.stopConnection();
		} catch (Exception ex) {
			if (ex instanceof SocketException) {
				if (ex.getMessage().contains("Connection reset")) {
					System.out.println("Server connection was reset.");
				} else {
					ex.printStackTrace();
				}
			} else {
				// Ooch!
				ex.printStackTrace();
			}
		}
	}
}

package utils.samples.tcp.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Accepts multiple clients.
 * Sends back to the client what the client told the server.
 */
public class TCPMultiServer {
	private ServerSocket serverSocket;
	private int nbConnectedClients = 0;
	private List<EchoClientHandler> clientList = new ArrayList<>();
	/**
	 * Start the server
	 * @param port TCP Port
	 * @throws Exception
	 */
	public void start(int port) throws Exception {
		System.out.printf("(%s) - Starting server on port %d\n", this.getClass().getName(), port);
		serverSocket = new ServerSocket(port);
		while (true) {
			EchoClientHandler newClient = new EchoClientHandler(this, serverSocket.accept());
			newClient.start();
			clientList.add(newClient);
		}
	}

	/**
	 * Stop the server
	 * @throws Exception
	 */
	public void stop() throws Exception {
		System.out.printf("(%s) Stopping TCP Server\n", this.getClass().getName());
		try {
			System.out.printf("(%s) %d client%s %s created during this session.\n",
					this.getClass().getName(),
					clientList.size(),
					clientList.size() > 1 ? "s" : "",
					clientList.size() > 1 ? "were" : "was");

			if (!serverSocket.isClosed()) {
				serverSocket.close();
			} else {
				System.out.printf("(%s) Server Socket already closed\n", this.getClass().getName());
			}
		} catch (Exception ex) {
			System.err.println("----- In STOP -----");
			ex.printStackTrace();
			System.err.println("-------------------");
			throw ex;
		}
	}

	/**
	 * Listener to ONE TCP client
	 * Stops when a "." is received.
	 * Features are implemented in the "run" method, in its while loop.
	 */
	private static class EchoClientHandler extends Thread {
		private final TCPMultiServer server;
		private final Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		public EchoClientHandler(TCPMultiServer server, Socket socket) {
			this.server = server;
			this.clientSocket = socket;
			this.server.nbConnectedClients++;
			System.out.printf("(%s) New client just joined (%s), now %d client%s connected\n",
					this.getClass().getName(),
					socket.toString(),
					this.server.nbConnectedClients,
					this.server.nbConnectedClients > 1 ? "s" : "");
		}

		public void run() {
			try {
				out = new PrintWriter(this.clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

				String inputLine;
				// Loops until a "." is received from the client.
				while ((inputLine = in.readLine()) != null) {
					System.out.printf("(%s) Read >> %s\n", this.getClass().getName(), inputLine);
					if (".".equals(inputLine)) {
						out.println("bye");
						break;
					} else {
						// The actual server's job.
						out.println(new StringBuilder(inputLine).reverse().toString()); // Return what's received, backwards.
					}
				}

				in.close();
				out.close();
				clientSocket.close();
				this.server.nbConnectedClients--;

				System.out.printf("(%s) - End of Client Thread (%s), %d client%s left.\n",
						this.getClass().getName(),
						this.clientSocket.toString(),
						this.server.nbConnectedClients,
						this.server.nbConnectedClients > 1 ? "s" : "");

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private final static String PORT_PREFIX = "--port:";

	public static void main(String... args) throws Exception {
		final AtomicInteger port = new AtomicInteger(5_555);
		// Parse CLI prms
		Arrays.asList(args).forEach(arg -> {
			if (arg.startsWith(PORT_PREFIX)) {
				try {
					port.set(Integer.parseInt(arg.substring(PORT_PREFIX.length())));
				} catch (NumberFormatException nfe) {
					System.out.printf("Invalid port in [%s], keeping default %d\n", arg, port.get());
				}
			}
		});
		TCPMultiServer server = new TCPMultiServer();

		// Manage Ctrl-C
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.printf("\n(%s) Stopping TCP server on user's request\n", server.getClass().getName());
			try {
				server.clientList.forEach(clientThread -> {
					try {
						clientThread.join();
					} catch (InterruptedException e) {
						System.err.println("In joining...");
						e.printStackTrace();
					}
				});
				server.stop();
			} catch (Exception e) {
				System.err.println("----- In Ctrl-C Int -----");
				e.printStackTrace();
				System.err.println("-------------------------");
			}
		}, "Ctrl-C"));

		server.start(port.get());
	}
}

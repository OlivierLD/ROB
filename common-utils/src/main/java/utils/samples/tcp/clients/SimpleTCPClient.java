package utils.samples.tcp.clients;

import utils.StaticUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple interactive TCP client
 */
public class SimpleTCPClient {
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void startConnection(String ip, int port) throws Exception {
		clientSocket = new Socket(ip, port);
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public String sendMessage(String msg) throws Exception {
		return sendMessage(msg, 1);
	}

	public String sendMessage(String msg, int nbRead) throws Exception {
		out.println(msg);
		String resp = "";
		for (int i=0; i<nbRead; i++) {
			String one = in.readLine();
			resp += (one + "\n");
		}
		return resp;
	}

	public void stopConnection() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
	}

	private final static String PORT_PREFIX = "--port:";
	private final static String HOST_PREFIX = "--host:";

	public static void main(String... args) {

		final AtomicInteger port = new AtomicInteger(5_555);
		final AtomicReference<String> host = new AtomicReference<>("127.0.0.1");

		// Parse CLI prms
		Arrays.asList(args).forEach(arg -> {
			if (arg.startsWith(PORT_PREFIX)) {
				try {
					port.set(Integer.parseInt(arg.substring(PORT_PREFIX.length())));
					System.out.printf("(%s) Port now set to %d\n", SimpleTCPClient.class.getName(), port.get());
				} catch (NumberFormatException nfe) {
					System.out.printf("Invalid port in [%s], keeping default %d\n", arg, port.get());
					System.out.printf("(%s) Host is now %s\n", SimpleTCPClient.class.getName(), host.get());
				}
			} else if (arg.startsWith(HOST_PREFIX)) {
				host.set(arg.substring(HOST_PREFIX.length()));
			}
		});

		SimpleTCPClient client = new SimpleTCPClient();
		try {
			client.startConnection(host.get(), port.get());
			System.out.printf("(%s) Enter '.' at the prompt to stop\n", SimpleTCPClient.class.getName());

			boolean keepWorking = true;
			while (keepWorking) {
				String request = StaticUtil.userInput("Request > ");
				if (".".equals(request)) {
					keepWorking = false;
				} else {
					// Response works if server talks when told to.
					// Continuous feed would not like this.
					String response = client.sendMessage(request, 1);
					System.out.printf("Server responded %s\n", response.trim());
				}
			}
			System.out.printf("(%s) Client exiting\n", SimpleTCPClient.class.getName());
			client.stopConnection();
		} catch (Exception ex) {
			// Ooch!
			System.err.printf("On machine %s, port %d\n", host.get(), port.get());
			ex.printStackTrace();
		}
	}
}

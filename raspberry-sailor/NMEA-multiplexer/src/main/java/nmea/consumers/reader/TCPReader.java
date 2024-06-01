package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * TCP reader
 */
public class TCPReader extends NMEAReader {
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static int DEFAULT_TCP_PORT = 80;
	private int tcpPort = DEFAULT_TCP_PORT;
	private String hostName = DEFAULT_HOST_NAME;
	private String initialRequest = null;  // Like for GPSd...
	private boolean keepTrying = false;

	public TCPReader(List<NMEAListener> al) {
		this(null, al, DEFAULT_HOST_NAME, DEFAULT_TCP_PORT, false);
	}

	public TCPReader(List<NMEAListener> al, int tcp) {
		this(null, al, DEFAULT_HOST_NAME, tcp, false);
	}

	public TCPReader(List<NMEAListener> al, String host, int tcp) {
		this(null, al, host, tcp, false);
	}

	public TCPReader(String threadName, List<NMEAListener> al, String host, int tcp) {
		this(threadName, al, host, tcp, null, false);
	}

	public TCPReader(String threadName, List<NMEAListener> al, String host, int tcp, boolean keepTrying) {
		this(threadName, al, host, tcp, null, keepTrying);
	}

	public TCPReader(String threadName, List<NMEAListener> al, String host, int tcp, String initialRequest, boolean keepTrying) {
		super(threadName != null ? threadName : "tcp-thread", al);
		this.hostName = host;
		this.tcpPort = tcp;
		this.initialRequest = initialRequest;
		this.keepTrying = keepTrying;
	}

	private Socket skt = null;

	public int getPort() {
		return this.tcpPort;
	}

	public String getHostname() {
		return this.hostName;
	}

	public String getInitialRequest() {
		return initialRequest;
	}

	public boolean isKeepTrying() {
		return keepTrying;
	}

	@Override
	public void startReader() {
		super.enableReading();
		try {
			InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
			skt = new Socket(address, tcpPort);

			if (this.initialRequest != null) {
				// Like "?WATCH={\"enable\":true,\"json\":false,\"nmea\":true,\"raw\":0,\"scaled\":false,\"timing\":false,\"split24\":false,\"pps\":false}"
				OutputStream os = skt.getOutputStream();
				PrintWriter out = new PrintWriter(os, true);
				out.println(this.initialRequest);
			}

			InputStream theInput = skt.getInputStream();
			byte[] buffer = new byte[4_096];
			String s;
			int nbReadTest = 0;
			while (this.canRead()) {
				int bytesRead = theInput.read(buffer);
				if (bytesRead == -1) {
					System.out.println("Nothing to read...");
					if (nbReadTest++ > 10) {
						break;
					}
				} else {
					int nn = bytesRead;
					for (int i = 0; i < Math.min(buffer.length, bytesRead); i++) {
						if (buffer[i] != 0) {
							continue;
						}
						nn = i;
						break;
					}
					byte[] toPrint = new byte[nn];
					System.arraycopy(buffer, 0, toPrint, 0, nn);
					s = new String(toPrint) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
	//      System.out.println("TCP:" + s);
					NMEAEvent n = new NMEAEvent(this, s);
					super.fireDataRead(n);
				}
			}
			System.out.println("Stop Reading TCP port.");
			theInput.close();
		} catch (BindException be) {
			System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpPort);
			be.printStackTrace();
			manageError(be);
		} catch (final SocketException se) {
//			if ("true".equals(System.getProperty("tcp.data.verbose"))) {
//		    se.printStackTrace();
//			}
			if (se.getMessage().indexOf("Connection refused") > -1) {
				System.out.println("Refused (1)");
			} else if (se.getMessage().indexOf("Connection reset") > -1) {
				System.out.println("Reset (2)");
			} else {
				boolean tryAgain = false;
				if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (1)");
					}
					tryAgain = true;
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (2)");
					}
				} else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (3)");
					}
					tryAgain = true;
				} else if (se instanceof ConnectException) { // Et hop!
					tryAgain = false;
					System.err.println("TCP :" + se.getMessage());
				} else {
					tryAgain = false;
					System.err.println("TCP Socket:" + se.getMessage());
				}
			}
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
		if (this.keepTrying && this.goRead) { // Reconnect
			System.out.println("--------------------------------");
			System.out.println("-- Re-connecting the TCP feed --");
			System.out.println("--------------------------------");
			startReader();
		} else {
			System.out.println("-- End of TCP reader.");
		}
	}

	@Override
	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			if (skt != null) {
				this.goRead = false;
				skt.close();
				skt = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		t.printStackTrace();
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for TCP */ }

}

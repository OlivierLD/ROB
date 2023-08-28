package nmea.forwarders;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Properties;

public class UDPClient implements Forwarder {
	private int udpPort = 8001;
	private InetAddress address = null;
	private Properties props = null;

	private final static String DEFAULT_HOST = "127.0.0.1"; // "230.0.0.1"
	private String hostName = DEFAULT_HOST;

	private DatagramSocket socket;

	public UDPClient(int port) throws Exception {
		this(port, DEFAULT_HOST);
	}

	public UDPClient(int port, String host) throws Exception {
		this.hostName = host;
		this.udpPort = port;
		try {
			this.address = InetAddress.getByName(this.hostName); // For Broadcasting, multicast address.
			this.socket = new DatagramSocket();
		} catch (Exception ex) {
			throw ex;
			// ex.printStackTrace();
		}
	}

	@Override
	public void write(byte[] message) {
		try {
			// Initialize a datagram
			DatagramPacket packet = new DatagramPacket(message, message.length, address, udpPort);
			this.socket.send(packet);

			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.println("UDP Message sent");
			}
		} catch (Exception ex) {
			if ("No such device".equals(ex.getMessage())) {
				System.out.println("No such device [" + address + "] (from " + this.getClass().getName() + ")");
			} else {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		if (this.socket != null) {
			try {
				if (this.address.isMulticastAddress()) {
					((MulticastSocket) this.socket).leaveGroup(this.address);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			this.socket.close();
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.println("DatagramSocket Closed");
			}
		}
	}

	public static class UDPBean {
		private String cls;
		private int port;
		private final String type = "udp";

		public UDPBean() {}  // This is for Jackson
		public UDPBean(UDPClient instance) {
			cls = instance.getClass().getName();
			port = instance.udpPort;
		}

		public int getPort() {
			return port;
		}

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}
	}

	@Override
	public Object getBean() {
		return new UDPBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}


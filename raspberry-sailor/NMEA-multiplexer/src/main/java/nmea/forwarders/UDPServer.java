package nmea.forwarders;

import nmea.api.BeanInterface;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Properties;

/**
 * WiP... Not implemented
 */
public class UDPServer implements Forwarder {
	private int udpPort = 8_001;
	private InetAddress address = null;
	private Properties props = null;
	private boolean active = true;
	private boolean verbose;
	private String description = "No desc.";

	private final static String DEFAULT_HOST = "127.0.0.1"; // "230.0.0.1"
	private String hostName = DEFAULT_HOST;

	public UDPServer(int port) throws Exception {
		this(port, DEFAULT_HOST);
	}

	public UDPServer(int port, String host) throws Exception {
		this.hostName = host;
		this.udpPort = port;
		try {
			this.address = InetAddress.getByName(this.hostName); // For Broadcasting, multicast address.
		} catch (Exception ex) {
			throw ex;
			// ex.printStackTrace();
		}
	}

	@Override
	public boolean isActive() {
		// System.out.printf("--> TCPServer, getting active : %B\n", this.active);
		return this.active;
	}
	@Override
	public void setActive(boolean status) {
		// System.out.printf("--> TCPServer, setting active : %B\n", status);
		this.active = status;
	}
	@Override
	public boolean isVerbose() {
		return verbose;
	}
	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	@Override
	public String getDescription() {
		return description;
	}
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void write(byte[] message) {
		if (!this.isActive()) {
			// TODO Honk
			return;
		}
		try {
			// Create datagram socket
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.printf("Creating Datagram Socket (%s, %d)\n", address, udpPort);
			}
			DatagramSocket dsocket = null;
			if (address.isMulticastAddress()) {
				dsocket = new MulticastSocket(udpPort);
				((MulticastSocket) dsocket).joinGroup(address);
			} else {
				dsocket = new DatagramSocket(udpPort, address);
			}
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.println("DatagramSocket created");
			}

			// Initialize a datagram
			DatagramPacket packet = new DatagramPacket(message, message.length, address, udpPort);
			dsocket.send(packet);
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.println("UDP Message sent");
			}
			if (address.isMulticastAddress()) {
				((MulticastSocket) dsocket).leaveGroup(address);
			}
			dsocket.close();
			if (this.props != null && "true".equals(this.props.getProperty("verbose"))) {
				System.out.println("DatagramSocket Closed");
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
	}

	public static class UDPBean implements BeanInterface {
		private String cls;
		private boolean active = true;
		private boolean verbose;
		private String description;
		private int port;
		private final String type = "udp";

		public UDPBean() {}  // This is for Jackson
		public UDPBean(UDPServer instance) {
			cls = instance.getClass().getName();
			port = instance.udpPort;
			verbose = instance.isVerbose();
			active = instance.isActive();
			description = instance.getDescription();
		}

		public int getPort() {
			return port;
		}

		@Override
		public String getCls() {
			return cls;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public boolean isVerbose() {
			return verbose;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}

	@Override
	public BeanInterface getBean() {
		return new UDPBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
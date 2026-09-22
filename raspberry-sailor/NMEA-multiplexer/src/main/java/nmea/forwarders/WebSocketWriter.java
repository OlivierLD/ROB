package nmea.forwarders;

import nmea.api.BeanInterface;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;
import java.util.Properties;

public class WebSocketWriter implements Forwarder {
	private WebSocketClient wsClient = null;
	private boolean isConnected = false;
	private final String wsUri;
	private Properties props = null;
	private boolean verbose;
	private boolean active = true;

	// TODO Manage Filters
	private List<String> sentenceFilters = null; // Sentence filters
	private List<String> deviceFilters = null; // Device filters
	private String description = "--";


	/**
	 * @param serverURL like ws://hostname:port/
	 * @throws Exception when it fails
	 */
	public WebSocketWriter(String serverURL) throws Exception {
		this.wsUri = serverURL;
		try {
			wsClient = new WebSocketClient(new URI(serverURL)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					System.out.println("WS On Open");
					isConnected = true;
				}

				@Override
				public void onMessage(String string) {
//                System.out.println("WS On Message");
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					System.out.println("WS On Close");
					isConnected = false;
				}

				@Override
				public void onError(Exception exception) {
					System.out.println("WS On Error");
					exception.printStackTrace();
				}
			};
			wsClient.connect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getWsUri() {
		return this.wsUri;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}
	@Override
	public void setActive(boolean status) {
		System.out.printf("-- Forwarder WebSocketWriter, setActive method: %B\n", status);
		this.active = status;
	}
	@Override
	public void setVerbose(boolean status) {
		this.verbose = status;
	}
	@Override
	public boolean isVerbose() {
		return this.verbose;
	}
	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	@Override
	public String getDescription() {
		return this.description;
	}
	@Override
	public void write(byte[] message) {
		if (!this.isActive()) {
			// TODO Honk
			return;
		}
		try {
			String mess = new String(message);
			if (!mess.isEmpty() && isConnected) {
				this.wsClient.send(mess);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.wsClient.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class WSBean implements BeanInterface {
		private String cls;
		private String wsUri;
		private final String type = "ws";
		private boolean active = true;
		private boolean verbose;
		private String description;

		public WSBean() {}  // This is for Jackson
		public WSBean(WebSocketWriter instance) {
			cls = instance.getClass().getName();
			wsUri = instance.wsUri;
			active = instance.isActive();
			verbose = instance.isVerbose();
			description = instance.getDescription();
		}

		@Override
		public String getCls() {
			return cls;
		}

		@Override
		public String getType() {
			return type;
		}

		public String getWsUri() {
			return wsUri;
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
		return new WSBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
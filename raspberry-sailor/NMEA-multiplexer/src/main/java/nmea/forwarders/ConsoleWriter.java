package nmea.forwarders;

import nmea.api.BeanInterface;

import java.util.Properties;

public class ConsoleWriter implements Forwarder {
	private Properties props = null;

	public ConsoleWriter() throws Exception {
	}

	@Override
	public void write(byte[] message) {
		if (this.isActive()) {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				System.out.println(mess.trim()); // That is what this Forwarder does.
			}
		} else {
			// TODO Honk ?
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to the console. (" + this.getClass().getName() + ")");
	}

	private static class ConsoleBean implements BeanInterface {
		private String cls;
		private final String type = "console";
		private boolean active;
		private boolean verbose;
		private String description;

		public ConsoleBean() {}  // This is for Jackson
		public ConsoleBean(ConsoleWriter instance) {
			cls = instance.getClass().getName();
			verbose = instance.isVerbose();
			active = instance.isActive();
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
		return new ConsoleBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
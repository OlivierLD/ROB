package nmea.forwarders;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.BeanInterface;
import nmea.forwarders.displays.CharacterModeConsole;

import java.util.Properties;

/**
 * This is a <i>Custom</i> forwarder.
 *
 * Requires:
 * <table border='1'>
 *   <caption>Description</caption>
 *   <tr>
 *     <td>Forwarder Class</td>
 *     <td>nmea.forwarders.CharacterConsoleWriter</td>
 *   </tr>
 *   <tr>
 *     <td>Properties file</td>
 *     <td>char.console.properties</td>
 *   </tr>
 * </table>
 *
 * The console actually starts when its properties are set.
 * See the {@link #setProperties(Properties)} method.
 */
public class CharacterConsoleWriter implements Forwarder {

	private Properties consoleProps;
	private boolean keepWorking = true;
	private CharacterModeConsole cmConsole;

	public CharacterConsoleWriter() throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}
		cmConsole = new CharacterModeConsole();
	}

	@Override
	public void write(byte[] message) {
		// Do nothing here. The cache is read by a Thread.
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to the character console. (" + this.getClass().getName() + ")");
		try {
			// Stop Cache thread
			keepWorking = false;
			try { Thread.sleep(2_000L); } catch (Exception ex) {}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		if (cmConsole != null) {
			cmConsole.quit();
		}
	}

	private static class ConsoleBean implements BeanInterface {
		private String cls;
		private boolean active;
		private boolean verbose;
		private String description;
		private final String type = "char-console";

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

		public ConsoleBean() {
		}
		public ConsoleBean(CharacterConsoleWriter instance) {
			cls = instance.getClass().getName();
			active = instance.isActive();
			verbose = instance.isVerbose();
			description = instance.getDescription();
		}
	}

	@Override
	public BeanInterface getBean() {
		return new ConsoleBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		props.stringPropertyNames().stream()
						.forEach(propName -> System.out.println(String.format("%s = %s", propName, props.get(propName))));
		if (props != null) {
			this.consoleProps = props;
			this.cmConsole.initializeConsole(props);

			Thread cacheThread = new Thread("CharacterModeConsole CacheThread") {
				public void run() {
					while (keepWorking) { // Ping the cache every second.
						NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
						cmConsole.displayData(cache, consoleProps);
						try { Thread.sleep(1_000L); } catch (Exception ex) {}
					}
				}
			};
			cacheThread.start();
		}
	}
}
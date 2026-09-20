package nmea.forwarders;

import java.util.Properties;

import nmea.api.BeanInterface;
import nmea.parser.StringParsers;
import util.TextToSpeech;

/**
 * This is a {@link Forwarder}, speaking the air temperature
 * when it changes of more than one degree Celsius.
 * <br>
 * It can be loaded dynamically. As such, it can be set only from the properties file
 * used at startup. It - for now - cannot be managed from the Web UI.
 * The REST api is not aware of it.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.SpeakingThermometer
 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 */
public class SpeakingThermometer implements Forwarder {
	private double previousTemp = -Double.MAX_VALUE;

	/*
	 * @throws Exception
	 */
	public SpeakingThermometer() throws Exception {
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
//		String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			if ("MTA".equals(sentenceId)) {
				double airTemp = StringParsers.parseMTA(str);
				if (Math.abs(airTemp - previousTemp) >= 1.0) {
					previousTemp = airTemp;
					TextToSpeech.speak(String.format("Temperature is now %.1f degrees Celsius.", airTemp));
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing (speaking) to " + this.getClass().getName());
	}

	public static class SpeakerBean implements BeanInterface {
		private String cls;
		private String type = "thermo-speaker";
		private boolean active;
		private boolean verbose;
		private String description;

		public SpeakerBean(SpeakingThermometer instance) {
			cls = instance.getClass().getName();
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
		return new SpeakerBean(this);
	}

	@Override
	public void setProperties(Properties props) {
	}
}
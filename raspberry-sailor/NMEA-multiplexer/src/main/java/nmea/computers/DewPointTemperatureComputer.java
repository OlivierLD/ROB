package nmea.computers;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.api.Multiplexer;
import nmea.api.NMEAParser;
import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import nmea.parser.Temperature;
import utils.StringUtils;
import utils.WeatherUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Computer for Dew Point Temperature
 */
public class DewPointTemperatureComputer extends Computer {

	private final static String DEFAULT_PREFIX = "OS";  // Oliv Soft
	// MTA: Air Temp
	// Rel Hum in XDR, MDA
	private final List<String> requiredStrings = Arrays.asList(new String[]{"MTA", "XDR", "MDA"});

	private String generatedStringsPrefix = DEFAULT_PREFIX;

	public DewPointTemperatureComputer(Multiplexer mux) {
	  this(mux, DEFAULT_PREFIX);
	}

	public void setPrefix(String prefix) {
		this.generatedStringsPrefix = prefix;
	}

	public DewPointTemperatureComputer(Multiplexer mux, String prefix) {
		super(mux);
		if (prefix == null || prefix.length() != 2) {
			throw new RuntimeException("Prefix must exist, and be EXACTLY 2 character long.");
		}
		this.generatedStringsPrefix = prefix;
	}

	/**
	 * Receives the data, and potentially produces new ones.
	 * Check if Air Temperature and Relative Humidity are available, and produces a DewPointTemperature if yes.
	 *
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);
		if (StringParsers.validCheckSum(sentence)) {
			String sentenceID = StringParsers.getSentenceID(sentence);
			if (!generatedStringsPrefix.equals(StringParsers.getDeviceID(sentence)) &&  // IMPORTANT: To prevent re-computing of computed data.
						requiredStrings.contains(sentenceID)) {
				NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
				switch (sentenceID) {
					case "MTA":
						final double mta = StringParsers.parseMTA(sentence);
						cache.put(NMEADataCache.AIR_TEMP, new Temperature(mta));
						break;
					case "MDA":
						final StringParsers.MDA mda = StringParsers.parseMDA(sentence);
						if (mda.airT != null) {
							cache.put(NMEADataCache.AIR_TEMP, new Temperature(mda.airT));
						}
						if (mda.relHum != null) {
							cache.put(NMEADataCache.RELATIVE_HUMIDITY, mda.relHum);
						}
						break;
					case "XDR":
						final List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(sentence);
						xdrElements.forEach(xdr -> {
							if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.HUMIDITY)) { // Relative Humidity
								cache.put(NMEADataCache.RELATIVE_HUMIDITY, xdr.getValue());
							}
							if (xdr.getTypeNunit().equals(StringGenerator.XDRTypes.TEMPERATURE)) { // Air Temp. Assume it's the only one (no DEWP)
								cache.put(NMEADataCache.AIR_TEMP, new Temperature(xdr.getValue()));
							}
						});
						break;
				}

				Double airTemp = null, relHum = null;
				if (cache != null) {
					synchronized (cache) {
						Temperature temp = ((Temperature)cache.get(NMEADataCache.AIR_TEMP));
						if (temp != null) {
							airTemp = temp.getTemperature();
						}
						relHum = (Double)cache.get(NMEADataCache.RELATIVE_HUMIDITY);
					}
				}
				if (airTemp != null && relHum != null) {
					double dewPointTemperature = WeatherUtil.dewPointTemperature(relHum, airTemp);

					String dptString = StringGenerator.generateXDR(generatedStringsPrefix,
							new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE, dewPointTemperature, StringGenerator.XDR_DEW_POINT));
					if (this.verbose) {
						System.out.printf("Producing Dew Point Temperature: %s\n", dptString);
					}
					this.produce(dptString);
				} else if (this.verbose) {
					System.out.println("Not enough data to compute Dew Point Temperature");
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing Dew Point Temperature, " + this.getClass().getName());
	}

	public static class DewPointComputerBean {
		private String cls;
		private final String type = "dew-point-computer";
		private boolean verbose;
		private boolean active;

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public boolean isActive() {
			return active;
		}

		public DewPointComputerBean() {}  // This is for Jackson
		public DewPointComputerBean(DewPointTemperatureComputer instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
			this.active = instance.isActive();
		}
	}

	@Override
	public Object getBean() {
		return new DewPointComputerBean(this);
	}
}
package nmea.computers;

import calc.GeomUtil;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.ais.AISParser;
import nmea.api.Multiplexer;
import nmea.parser.Border;
import nmea.parser.GeoPos;
import nmea.parser.StringParsers;
import util.TextToSpeech;
import utils.TimeUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Border Manager. WiP.
 * Uses current position and borders definition <u>possible collision threats</u>.
 * <br/>
 * Does NOT put anything in the cache.
 * <br/>
 * To be used as a custom computer:
 * <pre>
 * computers:
 *   - class: nmea.computers.BorderManager
 *     properties: mux-configs/border.mgr.properties
 * </pre>
 * Properties file like:
 * <pre>
 * # Properties of the BorderManager Computer
 * minimum.distance=1
 * heading.fork.width=10
 *
 * # For test (big distance, big fork)
 * #minimum.distance=50
 * #heading.fork.width=90
 *
 * # border.threat.callback=nmea.computers.SpeakingCallback
 * border.threat.callback=default
 * </pre>
 *
 * To try:
 * curl -X GET http://localhost:1234/mux/cache | jq '."borders-data"'
 * (Cheat Sheet at https://lzone.de/cheat-sheet/jq)
 */
public class BorderManager extends Computer {

	private final static double DEFAULT_MINIMUM_DISTANCE = 20D;
	private double minimumDistance = DEFAULT_MINIMUM_DISTANCE;
	public BorderManager(Multiplexer mux) {
		super(mux);
	}

	public Consumer<String> defaultCallback = s -> {
		System.out.println("---- B O R D E R   T H R E A T ---");
		System.out.println(s);
		System.out.println("----------------------------------");
	};
	private Consumer<String> collisionCallback = null;

	/**
	 * Wait for position data (not cached), get the perimeters/borders from the cache,
	 * and computes threat.
	 *
	 * @param mess Received message
	 */
	@Override
	public void write(byte[] mess) {
		String sentence = new String(mess);

		if (StringParsers.validCheckSum(sentence)) {

			// On position data only ? GLL, RMC ?
			if ("RMC".equals(sentence.substring(3, 6)) || "GLL".equals(sentence.substring(3, 6))) {

				NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
				GeoPos position = (GeoPos) cache.get(NMEADataCache.POSITION);
				if (position != null) {

					List<Border> borders = (List<Border>) cache.get(NMEADataCache.BORDERS_DATA);
					// TODO Compute threat here
					// See https://www.alloprof.qc.ca/fr/eleves/bv/mathematiques/la-distance-d-un-point-a-une-droite-dans-un-plan-m1315
					final String collected = borders.stream().map(border -> "[" + border.getBorderName() + "]").collect(Collectors.joining(" "));
					System.out.println("Borders: " + collected);

					boolean threatDetected = false; // Computation result.

					if (threatDetected) {
						String warningText = "Collision message here";
						System.out.println(warningText);
						// TODO Honk! Define a callback Consumer<String> (see 'speak' below), or just a signal (sent to a buzzer, a light, whatever).
						if (collisionCallback != null) {
							// A test
							String messageToSpeak = "Honk honk!!"; // With more data
							collisionCallback.accept(messageToSpeak);
							// TextToSpeech.speak(messageToSpeak);
						}
					}
				}
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop Computing AIS data, " + this.getClass().getName());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setProperties(Properties props) {
		this.props = props;
		this.minimumDistance = Double.parseDouble(props.getProperty("minimum.distance", String.valueOf(DEFAULT_MINIMUM_DISTANCE)));
		this.verbose = "true".equals(props.getProperty("verbose"));
		String callback = props.getProperty("border.threat.callback");
		if (callback != null) {
			if (callback.equals("default")) {
				this.collisionCallback = defaultCallback;
			} else {
				try {
					Class<?> aConsumer = Class.forName(callback);
					this.collisionCallback = (Consumer<String>) aConsumer.getDeclaredConstructor().newInstance();
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				} catch (IllegalAccessException iae) {
					iae.printStackTrace();
				} catch (InstantiationException ie) {
					ie.printStackTrace();
				} catch (NoSuchMethodException nsme) {
					nsme.printStackTrace();
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				}
			}
		}
		if (this.verbose) {
			System.out.println(String.format("Border Computer %s:\n\tVerbose: %s\n\tMinimum Distance: %.02f",
					this.getClass().getName(),
					this.verbose,
					this.minimumDistance));
		}
	}

	public static class BorderComputerBean {
		private String cls;
		private final String type = "border-computer";
		private boolean verbose;

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public boolean isVerbose() {
			return verbose;
		}

		public BorderComputerBean() {}  // This is for Jackson
		public BorderComputerBean(BorderManager instance) {
			this.cls = instance.getClass().getName();
			this.verbose = instance.isVerbose();
		}
	}

	@Override
	public Object getBean() {
		return new BorderComputerBean(this);
	}
}

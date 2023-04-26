package nmea.parser;

/**
 * @deprecated Use {@link java.util.concurrent.atomic.AtomicReference<Double>}
 */
public interface NMEADoubleValueHolder {
	public void setDoubleValue(double d);

	public double getDoubleValue();
}

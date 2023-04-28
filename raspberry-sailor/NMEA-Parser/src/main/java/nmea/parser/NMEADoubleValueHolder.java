package nmea.parser;

/**
 * @deprecated Use {@link java.util.concurrent.atomic.AtomicReference} on a &lt;Double&gt;
 */
public interface NMEADoubleValueHolder {
	public void setDoubleValue(double d);

	public double getDoubleValue();
}

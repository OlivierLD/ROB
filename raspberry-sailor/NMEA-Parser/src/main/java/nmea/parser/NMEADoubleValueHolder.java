package nmea.parser;

/**
 * Maybe redundant with {@link java.util.concurrent.atomic.AtomicReference} on a &lt;Double&gt;
 */
public interface NMEADoubleValueHolder {
	void setDoubleValue(double d);

	double getDoubleValue();
}

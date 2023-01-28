package nmea.forwarders;

import java.util.Properties;

public interface Forwarder {
	// Receives data. Triggered everytime data is read - by a Consumer (Channel).
	void write(byte[] mess);
	// Called after the setProperties, in case some re-driving the Forwarder's initialization.
	default void init() {}
	void close();
	void setProperties(Properties props);

	Object getBean();
}

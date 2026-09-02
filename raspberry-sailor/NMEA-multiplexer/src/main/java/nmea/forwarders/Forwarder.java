package nmea.forwarders;

import java.util.Properties;

public interface Forwarder {
	// Receives data. Triggered everytime data is read - by a Consumer (Channel).
	void write(byte[] mess);
	// Called after the setProperties, in case some re-driving the Forwarder's initialization.
	default void init() {}
	void close();
	void setProperties(Properties props);

	/**
	 * Paused or not
	 *
	 * @return true if active, false otherwise
	 */
	default boolean isActive() {
		return true;
	}

	/**
	 * Set the active flag to a forwarder.
	 * @param status true for active, false for inactive
	 */
	default void setActive(boolean status) {
	}

	Object getBean();
}
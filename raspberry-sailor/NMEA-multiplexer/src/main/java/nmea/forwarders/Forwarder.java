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

	default void setVerbose(boolean status) {
	}
	default boolean isVerbose() {
		return false;
	}

	default String getDescription() {
		return "No desc.";
	}
	default void setDescription(String desc) {
	}
//	String getDescription();
//	void setDescription(String desc);
//
	/**
	 * Used by the RESTImplementation
	 * @return the bean corresponding to the object (consumer, forwarder or computer)
	 */
	Object getBean();
}
package nmea.consumers.client;

/**
 * Used by the RESTImplementation
 * The bean corresponding to the object (consumer here))
 */
public interface ClientBean {
	String getType();
	boolean getVerbose();
	default boolean isActive() {
		return true;
	}
	String[] getDeviceFilters();
	String[] getSentenceFilters();
	String getDescription();

}
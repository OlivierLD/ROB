package nmea.consumers.client;

/**
 * Used by the RESTImplementation
 * On the bean's corresponding object (consumer here)
 */
public interface ClientBean {
	String getType();
	boolean getVerbose();
	boolean isActive();
	String[] getDeviceFilters();
	String[] getSentenceFilters();
	String getDescription();

}
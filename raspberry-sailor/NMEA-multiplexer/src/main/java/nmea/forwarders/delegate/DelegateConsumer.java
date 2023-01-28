package nmea.forwarders.delegate;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public interface DelegateConsumer {
    default void setProperties(Properties props) {}
    Consumer<List<String>> getConsumer();
}

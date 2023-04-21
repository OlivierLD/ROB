package nmea.consumers.client;

import java.util.Properties;

public class Janitor {
    public void executeOnClose(Properties props) {
        System.out.println("-------- Default Janitor --------");
        System.out.printf("Shutting down %s, props are:\n", this.getClass().getName());
        props.forEach((n, v) -> System.out.printf("%s:%s\n", n, v));
        System.out.println("---------------------------------");
    }
}

package nmea.publisher;

import nmea.forwarders.RESTPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Run it like in
 * ../gradlew test --tests "nmea.publisher.RESTPublisherTest.testRESTEInk"
 */
public class RESTPublisherTest {
    // Java 11
//    private final static List<String> DATA_TO_SEND = List.of(
//            "Ping", "Pong", "Paf",
//            "Bing", "Boom", "Bang"
//    );
    // Java 11
//    private final static List<String> DATA_TO_SEND_TO_SSD = List.of(
//            "Ping|Pong|Paf",
//            "Bing|Boom|Bang"
//    );
    // Java 8
    private final static List<String> DATA_TO_SEND ;
    static {
        DATA_TO_SEND = Arrays.asList("Ping", "Pong", "Paf","Bing", "Boom", "Bang");
    }
    // Java 8
    private final static List<String> DATA_TO_SEND_TO_SSD;
    static {
        DATA_TO_SEND_TO_SSD = Arrays.asList("Ping|Pong|Paf", "Bing|Boom|Bang");
    }

    // TODO Before/After to start/stop the REST Server

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testRESTEInk() {
        String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
        try {
            RESTPublisher restPublisher = new RESTPublisher();

            Properties props = new Properties();
            props.put("server.name", "localhost"); // That one must be up and running for this main to work.
            props.put("server.port", "8080");
//			props.put("rest.resource", "/rest/endpoint?qs=prm");
            props.put("rest.resource", "/eink2_13/display");
            props.put("rest.verb", "POST");
            props.put("http.headers", "Content-Type:plain/text");
            restPublisher.setProperties(props);

            for (int i = 0; i < 10; i++) {
                System.out.println(DATA_TO_SEND.get(i % DATA_TO_SEND.size()));
                try {
                    wpl = DATA_TO_SEND.get(i % DATA_TO_SEND.size()); // Comment that one if needed.
                    restPublisher.write(wpl.getBytes());
                } catch (Exception ex) {
                    System.err.println(ex.getLocalizedMessage());
                    fail(ex.toString());
                    break;
                }
                try {
                    Thread.sleep(1_000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Oops");
        }
        assertTrue("Argh!", true);
    }

    @Test
    public void testREST_SSD1306() {
        try {
            RESTPublisher restPublisher = new RESTPublisher();

            Properties props = new Properties();
            props.put("server.name", "192.168.1.101"); // That one must be up and running for this main to work.
            props.put("server.port", "8080");
            props.put("rest.protocol", "http");
            props.put("rest.resource", "/ssd1306/nmea-data");
            props.put("rest.verb", "PUT");
            props.put("http.headers", "Content-Type:plain/text");
            restPublisher.setProperties(props);

            for (int i = 0; i < 10; i++) {
                System.out.println(DATA_TO_SEND_TO_SSD.get(i % DATA_TO_SEND_TO_SSD.size()));
                try {
                    String wpl = DATA_TO_SEND_TO_SSD.get(i % DATA_TO_SEND_TO_SSD.size());
                    restPublisher.write(wpl.getBytes());
                } catch (Exception ex) {
                    System.err.println(ex.getLocalizedMessage());
                    fail(ex.toString());
                    break;
                }
                try {
                    Thread.sleep(1_000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Oops");
        }
        assertTrue("Argh!", true);
    }
}

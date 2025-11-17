package nmea.consumers.reader;

import nmea.consumers.client.RESTClient;
import org.junit.Test;
import utils.TimeUtil;

/**
 * Run it like in
 * ../gradlew test --tests "nmea.consumers.reader.RESTReaderTest.RESTReader"
 */
public class RESTReaderTest {

    @Test
    public void RESTReader() {

        String serverNameOrIP = "localhost"; // ""192.168.1.102";

        RESTClient nmeaClient = new RESTClient();

        Runtime.getRuntime().addShutdownHook(new Thread("RESTClient shutdown hook") {
            public void run() {
                System.out.println("Shutting down nicely.");
                nmeaClient.stopDataRead();
            }
        });
        nmeaClient.initClient();
//        nmeaClient.setReader(new RESTReader("RESTReader", nmeaClient.getListeners(),
//                "http",     // protocol
//                serverNameOrIP,     // machine name
//                8_080,              // port
//                "/eink2_13/oplist", // path
//                "",                 // query string
//                null,               // jqs
//                null,               // nmea-processor
//                null));             // between loops
        nmeaClient.setReader(new RESTReader("RESTReader", nmeaClient.getListeners(),
                "http",     // protocol
                "192.168.1.41",     // machine name
                9999,              // port
                "/sense-hat/all-env-sensors/", // path
                "",                 // query string
                ".pressure",        // jqs
                "nmea.parser.StringGenerator.generateMMB(String:\"SH\", double:value)", // nmea-processor
                null));             // between loops

        nmeaClient.startWorking();

        TimeUtil.delay(10_000L);
        nmeaClient.stopDataRead();
    }

}
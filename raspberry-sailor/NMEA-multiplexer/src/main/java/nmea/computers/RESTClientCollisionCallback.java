package nmea.computers;

import http.client.HTTPClient;
import nmea.ais.AISParser;
import util.TextToSpeech;

import java.io.FileInputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * An example.
 * On collision threat, make a REST call, that will trigger the action.
 * All its parameters are defined in the associated properties file.
 *
 * Call a service like 'curl -X POST http://192.168.1.41:9999/sense-hat/honk'
 */
public class RESTClientCollisionCallback implements Consumer<String> {

    private boolean active = true;
    private String verb = "GET";
    private String machine = "localhost";
    private int port = 80;
    private String path = "/this/that";
    private String data = null; // Unused. See accept method.

    private Map<String, String> headers = null; // Optional. From properties ?

    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    public RESTClientCollisionCallback() {
        System.out.println(">> RESTClientCollisionCallback - creating new Instance!!");
        headers = new HashMap<>();
        headers.put("Content-Type", "plain/text");
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    @Override
    public void accept(String s) {
        if (VERBOSE) {
            System.out.printf("Accept >> [%s] in %s\n", s, this.getClass().getName());
        }
        String command = String.format("http://%s:%d%s", machine, port, path);
        System.out.printf("Command: curl +X %s %s\n", verb, command);

        if (this.active) {
            try {
                switch (this.verb) {
                    case "POST":
                        String postRequest = command;
                        String strContent = new String(s).trim();
                        if (VERBOSE) {
                            System.out.printf("%s\n%s\n", postRequest, strContent);
                        }
                        HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(postRequest, headers, strContent);
                        if (true || VERBOSE) {
                            System.out.printf("POST %s with %s: Response code %d, message: %s\n",
                                    postRequest,
                                    strContent,
                                    httpResponse.getCode(),
                                    httpResponse.getPayload());
                        }
                        // TODO return the response message/status ?
                        break;
                    case "PUT":
                        String putRequest = command;
                        String putStrContent = new String(s).trim();
//					System.out.println("Verbose: [" + this.props.getProperty("verbose") + "]");
                        if (VERBOSE) {
                            System.out.printf("%s\n%s\n", putRequest, putStrContent);
                        }
                        HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, putStrContent);
                        if (VERBOSE) {
                            System.out.printf("PUT %s with %s: Response code %d, message: %s\n",
                                    putRequest,
                                    putStrContent,
                                    putResponse.getCode(),
                                    putResponse.getPayload());
                        }
                        // TODO return the response message/status ?
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                if (VERBOSE) {
                    System.err.println(">> Error!");
                    ex.printStackTrace();
                }
                throw new RuntimeException(ex);
            }
        }
    }

    public void setProperties(String propFileName) {
        System.out.printf("Loading properties file %s\n", propFileName);
        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(propFileName);
            props.load(fis);

            if (props.getProperty("rest-verb") != null) {
                verb = props.getProperty("rest-verb");
            }
            if (props.getProperty("rest-machine") != null) {
                machine = props.getProperty("rest-machine");
            }
            if (props.getProperty("rest-port") != null) {
                port = Integer.parseInt(props.getProperty("rest-port"));
            }
            if (props.getProperty("rest-path") != null) {
                path = props.getProperty("rest-path");
            }
            if (props.getProperty("rest-data") != null) {
                data = props.getProperty("rest-data");
            }
        } catch (Exception e) {
            System.out.printf("%s file problem...\n", propFileName);
            throw new RuntimeException(String.format("File not found: %s", propFileName));
        }
    }
}
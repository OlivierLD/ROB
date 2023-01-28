package nmea.forwarders.delegate;

import http.client.HTTPClient;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SSD1306RESTConsumer implements DelegateConsumer {

    private String protocol;   // http or https
    private int port;
    private String serverName; // Name or IP address
    private String verb;       // GET, POST, PUT, etc
    private String resource;   // Path and Query String (if needed)
    private boolean verbose = false;

    private Map<String, String> headers = Map.of("Content-Type", "text/plain");  // TODO Get from props ?

    private final Consumer<List<String>> consumer = (dataList) -> {
        // Default...
//        dataList.forEach(line -> System.out.println(">>> " + line));
//        System.out.println("---------------");
        // Specific
        String putRequest = String.format("%s://%s:%d%s", this.protocol, this.serverName, this.port, this.resource);
        String putStrContent = dataList.stream().map(String::trim).collect(Collectors.joining("|"));
        if (this.verbose) {
            System.out.printf("%s\n%s\n", putRequest, putStrContent);
        }
        try {
            HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, putStrContent);
            if (this.verbose) {
                System.out.printf("PUT %s with %s: Response code %d, message: %s\n",
                        putRequest,
                        putStrContent,
                        putResponse.getCode(), // Return the response message/status ?
                        putResponse.getPayload());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    @Override
    public void setProperties(Properties props) {
        this.protocol = props.getProperty("ssd1306.dg.protocol", "http");
        if (!this.protocol.equals("http") && !this.protocol.equals("https")) {
            throw new RuntimeException(String.format("Protocol [%s] not supported, only http and https."));
        }
        this.serverName = props.getProperty("ssd1306.dg.server-name", "localhost");
        String portStr = props.getProperty("ssd1306.dg.port", "80");
        try {
            this.port = Integer.parseInt(portStr);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
        this.verb = props.getProperty("ssd1306.dg.verb", "PUT");
        if (!this.verb.equals("PUT")) {
            throw new RuntimeException(String.format("Verb %s not supported, only PUT for now.", this.verb));
        }
        this.resource = props.getProperty("ssd1306.dg.resource", "/");
        this.verbose = "true".equals(props.getProperty("ssd1306.dg.verbose", "false"));

        if (this.verbose) {
            System.out.println("-- Read properties for SSD1306RESTConsumer --");
            props.forEach((k, v) -> System.out.printf("%s: %s\n", k, v));
            System.out.println("---------------------------------------------");
        }
    }

    @Override
    public Consumer<List<String>> getConsumer() {
        return consumer;
    }
}

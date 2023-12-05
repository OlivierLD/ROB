package nmea.computer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import http.HTTPServer;
import http.client.HTTPClient;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import nmea.mux.GenericNMEAMultiplexer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LongTermStorageTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private final static Scope ROOT_SCOPE = Scope.newEmptyScope(); // jq scope
    private final static boolean verbose = false;
    private static Object pingTheCache(String protocol,
                                       String hostName,
                                       int httpPort,
                                       String queryPath,
                                       String queryString,
                                       String jqString) {
        Object returned = null;

        String restURL = String.format("%s://%s:%d%s%s",
                protocol,
                hostName,
                httpPort,
                queryPath,
                queryString != null ? queryString : "" );
        System.out.printf("Ping the cache: [%s]\n", restURL);
        try {
            // 1 - Make request
            HTTPServer.Request request = new HTTPServer.Request("GET", restURL, "HTTP/1.1");
            Map<String, String> reqHeaders = new HashMap<>();
            request.setHeaders(reqHeaders);
            final HTTPServer.Response response = HTTPClient.doRequest(request);
            // 2 - Manage response
            try {
                String payload = new String(response.getPayload());
                // See jackson-jq
                if (response.getHeaders() != null) {
                    String contentType = response.getHeaders().get("Content-Type"); // TODO Upper/lower case
                    AtomicReference<String> objPayload = new AtomicReference<>(payload);
                    if ("application/json".equals(contentType)) {
                        if (jqString != null && jqString.trim().length() > 0) {  // Compatible Java 8
                            try {
                                JsonQuery jq = JsonQuery.compile(jqString /*".NMEA_AS_IS.RMC" */, Versions.JQ_1_6);
                                JsonNode jsonNode = mapper.readTree(new StringReader(payload));
                                jq.apply(ROOT_SCOPE, jsonNode, (out) -> {
                                    if (out.isTextual() /*&& command.hasOption(OPT_RAW.getOpt()) */) {
                                        objPayload.set(out.asText());
                                    } else {
                                        try {
                                            objPayload.set(mapper.writeValueAsString(out));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                            } catch (JsonQueryException jqe) {
                                // Query cannot be parsed.
                                jqe.printStackTrace();
                            }
                        }
                        payload = objPayload.get(); // mapper.writeValueAsString(objPayload.get());
                    }
                }
                if (verbose) {
                    System.out.printf(">> REST Reader: %s\n", payload);
                }
                final List<String> dataToFire = new ArrayList<>();
                // Multi-node result here? Re-parse.
                try {
                    Object finalObject = mapper.readValue(payload, Object.class);
                    returned = finalObject;
                } catch (JsonParseException jpe) {
                    // Not an Object, consider it a String.
                    returned = payload;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returned;
    }

    /*
    Equivalent to
    COMMAND="${SUDO}java ${JAVA_OPTIONS} ${LOGGING_FLAG} ${JFR_FLAGS} ${REMOTE_DEBUG_FLAGS} -cp ${CP} nmea.mux.GenericNMEAMultiplexer ${CLI_PRMS}"
    In JAVA_OPTIONS: -Ddefault.mux.latitude and -Ddefault.mux.longitude
     */
    public static void main(String[] args) {
        try {
            final URL resource = LongTermStorageTest.class.getResource("ForLTSTest.yaml");
            if (resource != null) {
                System.out.printf("Resource URL: %s\n", resource.toString());
                final String path = resource.toURI().getPath();
                System.out.printf("Resource path: %s\n", path);
                System.setProperty("mux.properties", path);
                System.setProperty("default.mux.latitude", Double.toString(47.677667));
                System.setProperty("default.mux.longitude", Double.toString(-3.135667));
                Thread muxThread = new Thread(() -> {
                    GenericNMEAMultiplexer.main();
                });
                try {
                    muxThread.start();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
                // Now, make sure that the LongStorageMap never grows over 10.
                try {
                    try {
                        Thread.sleep(3_000L); // Wait for the server to be up
                    } catch (Exception ex3) {
                        ex3.printStackTrace();
                    }
                    // Request
                    for (int i=0; i<20; i++) {
                        try {
                            Object data = pingTheCache("http", "localhost", 9999, "/mux/cache", null, ".\"ZDA-store\"");
//                            System.out.println("Data:" + data);
                            if (data instanceof Map) {
                                if (((Map)data).size() > 10) {
                                    System.err.printf("Map size over 10 (%d) !!\n", ((Map)data).size());
                                } else {
                                    System.out.println("Map size OK");
                                }
                            } else {
                                System.err.println("Not a Map, argh !");
                            }
                        } catch (Exception oops) {
                            oops.printStackTrace();
                        }
                        try {
                            Thread.sleep(1_000L); // Wait between loops
                        } catch (Exception ex3) {
                            ex3.printStackTrace();
                        }
                    }
//                    startReadingMux();

                    System.out.println("\tNow killing the MUX.");
                    muxThread.interrupt();
//                    muxThread.stop(); // Bam ! Deprecated, but will do the job here.
                } catch (Throwable ex2) {
                    ex2.printStackTrace();
                }
                System.out.println("\tDone.");
                try {
                    Thread.sleep(3_000L);
                } catch (Exception ex3) {
                    ex3.printStackTrace();
                }
                System.exit(0); // Will kill the threads...
            } else {
                System.out.println("Resource not found, aborting");
            }
        } catch (Throwable boom) {
            boom.printStackTrace();
        }

    }
}

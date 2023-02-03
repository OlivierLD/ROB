package nmea.forwarders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import http.client.HTTPClient;
import java.util.Map;
import java.util.Properties;

/**
 * Forward the full NMEA Cache to a REST resource, in JSON.
 * This requires a knowledge of its structure...
 * REST verb can be PUT or POST.
 */
public class NMEACachePublisher implements Forwarder {

    private final static ObjectMapper mapper = new ObjectMapper();

    private boolean keepWorking = true;

    private static NMEACachePublisher instance = null;

    protected boolean verbose = false;
    protected long betweenPublish = 1_000L;
    protected String protocol = "http";
    protected String verb = "PUT";
    protected String machineName = "localhost";
    protected int port = 8888;
    protected String resource = "/";
    protected String queryString = null;

    public static NMEACachePublisher getInstance() {
        return instance;
    }

    public NMEACachePublisher(Long betweenPublish,
                              String verb,
                              String protocol,
                              String machineName,
                              Integer port,
                              String resource,
                              String qs) throws Exception {

        instance = this;

        if (betweenPublish != null) {
            this.betweenPublish = betweenPublish * 1_000L;
        }
        if (verb != null) {
            this.verb = verb;
        }
        if (protocol != null) {
            this.protocol = protocol;
        }
        if (machineName != null) {
            this.machineName = machineName;
        }
        if (port != null) {
            this.port = port;
        }
        if (resource != null) {
            this.resource = resource;
        }
        if (qs != null) {
            this.queryString = qs;
        }

        int nbTry = 0;
        boolean ok = false;
        while (!ok) {
            // Make sure the cache has been initialized.
            if (ApplicationContext.getInstance().getDataCache() == null) {
                if (nbTry < 10) {
                    try {
                        Thread.sleep(1_000L);
                    } catch (Exception ex) {
                    }
                    nbTry++;
                } else {
                    throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
                }
            } else {
                ok = true;
            }
        }
    }

    protected void initCacheThread() {
        // This is the loop providing the cache data
        Thread cacheThread = new Thread("CachePublisherThread") {
            public void run() {
                while (keepWorking) {
                    NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
                    try {
                        final String jsonCache = mapper.writeValueAsString(cache);
                        try {
                            Map<String, String> headers = Map.of("Content-Type", "application/json");
                            switch (instance.verb) {
                                case "POST":
                                    String postRequest = String.format("%s://%s:%d%s%s",
                                            instance.protocol,
                                            instance.machineName,
                                            instance.port,
                                            instance.resource,
                                            instance.queryString == null ? "" : instance.queryString);
                                    String strContent = jsonCache;
                                    if (instance.verbose) {
                                        System.out.printf("%s\n%s\n", postRequest, strContent);
                                    }
                                    try {
                                        HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(postRequest, headers, strContent);
                                        if (instance.verbose) {
                                            System.out.printf("POST %s with %s: Response code %d, message: %s\n",
                                                    postRequest,
                                                    strContent,
                                                    httpResponse.getCode(),
                                                    httpResponse.getPayload());
                                        }
                                    } catch (Throwable restFailure) {
                                        System.err.printf(">> POST Error in NMEACachePublisher: %s\n", restFailure.getMessage());
                                        if (instance.verbose) {
                                            restFailure.printStackTrace();
                                        }
                                    }
                                    break;
                                case "PUT":
                                    String putRequest = String.format("%s://%s:%d%s%s",
                                            instance.protocol,
                                            instance.machineName,
                                            instance.port,
                                            instance.resource,
                                            instance.queryString == null ? "" : instance.queryString);
                                    String putStrContent = jsonCache;
                                    if (instance.verbose) {
                                        System.out.printf("%s\n%s\n", putRequest, putStrContent);
                                    }
                                    try {
                                        HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, putStrContent);
                                        if (instance.verbose) {
                                            System.out.printf("PUT %s with %s: Response code %d, message: %s\n",
                                                    putRequest,
                                                    putStrContent,
                                                    putResponse.getCode(),
                                                    putResponse.getPayload());
                                        }
                                    } catch (Throwable restFailure) {
                                        System.err.printf(">> PUT Error in NMEACachePublisher: %s\n", restFailure.getMessage());
                                        if (instance.verbose) {
                                            restFailure.printStackTrace();
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception ex) {
                            if (instance.verbose) {
                                System.err.println(">> Error!");
                                ex.printStackTrace();
                            }
                            throw new RuntimeException(ex);
                        }
                    } catch (JsonProcessingException jpe) {
                        jpe.printStackTrace();
                    }
                    try {
                        Thread.sleep(1_000L);
                    } catch (Exception ex) {
                    }
                }
                System.out.println("Cache thread completed.");
            }
        };
        cacheThread.start();
    }

    @Override
    public void init() {
        initCacheThread();
    }

    @Override
    public void write(byte[] message) {
        // Nothing is done here.
        // It is replaced by the Thread in the constructor, in init -> initCacheThread
    }

    @Override
    public void close() {
        System.out.println("- Stop writing to " + this.getClass().getName());
        try {
            // Stop Cache thread
            keepWorking = false;
            try {
                Thread.sleep(2_000L);
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class NMEACacheBean {
        private final String cls; // Class
        private final String type = "nmea-cache-publisher";
        protected long betweenLoops;
        protected String protocol;
        protected String verb;
        protected String machineName;
        protected int port;
        protected String resource;
        protected String queryString;

        public NMEACacheBean(NMEACachePublisher instance,
                             long betweenLoops,  // TODO Check factor 1000
                             String protocol,
                             String verb,
                             String machineName,
                             int port,
                             String resource,
                             String qs) {
            this.cls = instance.getClass().getName();
            this.betweenLoops = betweenLoops;
            this.protocol = protocol;
            this.verb = verb;
            this.machineName = machineName;
            this.port = port;
            this.resource = resource;
            this.queryString = qs;
        }

        public String getCls() {
            return cls;
        }

        public String getType() {
            return type;
        }
    }

    @Override
    public Object getBean() {
        return new NMEACacheBean(this, this.betweenPublish, this.protocol, this.verb, this.machineName, this.port, this.resource, this.queryString);
    }

    @Override
    public void setProperties(Properties props) {
		
		String betweenLoops = props.getProperty("between.loops", "1");
        try {
            betweenPublish = Long.parseLong(betweenLoops) * 1_000L;
        } catch (NumberFormatException nfe) {
            System.err.println("Using default value for between.loops time");
        }
        verbose = "true".equals(props.getProperty("verbose", "false"));
        protocol = props.getProperty("rest.protocol", "http");  // Make sure it is http or https
        verb = props.getProperty("rest.verb", "PUT");
        machineName = props.getProperty("rest.machine-name", "http");
        port = Integer.parseInt(props.getProperty("rest.port", "8080")); // TODO trap exception
        resource = props.getProperty("rest.resource", "/");
        queryString = props.getProperty("rest.query.string");
    }
}

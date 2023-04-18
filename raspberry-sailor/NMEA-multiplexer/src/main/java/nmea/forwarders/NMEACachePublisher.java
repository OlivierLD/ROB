package nmea.forwarders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import http.client.HTTPClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Forward the full NMEA Cache to a REST resource, in JSON.
 * This requires a knowledge of its structure...
 * REST verb can be PUT or POST. Other verbs would not really make sense here.
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

    protected String onCloseResource = null;
    protected String onCloseVerb = null;

    public static NMEACachePublisher getInstance() {
        return instance;
    }

    public NMEACachePublisher(Long betweenPublish,
                              String verb,
                              String protocol,
                              String machineName,
                              Integer port,
                              String resource,
                              String qs,
                              boolean verbose) throws Exception {
        this(betweenPublish, verb, protocol, machineName, port, resource, qs, verbose, null, null);
    }

    public NMEACachePublisher(Long betweenPublish,
                              String verb,
                              String protocol,
                              String machineName,
                              Integer port,
                              String resource,
                              String qs,
                              boolean verbose,
                              String doOnClose,
                              String onCloseVerb) throws Exception {

        instance = this;

        this.verbose = verbose;

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
        if (doOnClose != null) {
            this.onCloseResource = doOnClose;
        }
        if (onCloseVerb != null) {
            this.onCloseVerb = onCloseVerb;
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
                            // Java 11
//                            Map<String, String> headers = Map.of("Content-Type", "application/json");
                            // Java 8
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");

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
                                        System.err.printf(">> POST (%s) Error in NMEACachePublisher: %s\n",
                                                postRequest,
                                                restFailure.getMessage());
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
                                        System.err.printf(">> PUT (%s) Error in NMEACachePublisher: %s\n",
                                                putRequest,
                                                restFailure.getMessage());
                                        if (instance.verbose) {
                                            restFailure.printStackTrace();
                                        }
                                    }
                                    break;
                                default: // TODO Honk ?
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

        if (this.verbose) {
            System.out.printf(">> On close, resource: %s, verb: %s\n", this.onCloseResource, this.onCloseVerb);
        }
        // Add an 'onclose' action...., like to reset an oled screen?
        if (this.onCloseResource != null && this.onCloseVerb != null) {
            try {
                // Java 11
                // Map<String, String> headers = Map.of("Content-Type", "application/json");
                // Java 8
                Map<String, String> headers = new HashMap<>();
                // headers.put("Content-Type", "application/json");

                switch (this.onCloseVerb) {
                    case "POST":
                        String postRequest = String.format("%s://%s:%d%s",
                                this.protocol,
                                this.machineName,
                                this.port,
                                this.onCloseResource);
                        if (this.verbose) {
                            System.out.println("+======================================");
                            System.out.printf("| onClose doing a POST %s\n", postRequest);
                            System.out.println("+======================================");
                        }
                        try {
                            HTTPClient.HTTPResponse httpResponse = HTTPClient.doPost(postRequest, headers, null);
                            if (this.verbose) {
                                System.out.printf("POST %s with %s: Response code %d, message: %s\n",
                                        postRequest,
                                        null,
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
                        String putRequest = String.format("%s://%s:%d%s",
                                this.protocol,
                                this.machineName,
                                this.port,
                                this.onCloseResource);
                        if (instance.verbose) {
                            System.out.printf("onClose doing a PUT %s\n", putRequest);
                        }
                        try {
                            HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, null);
                            if (this.verbose) {
                                System.out.printf("PUT %s with %s: Response code %d, message: %s\n",
                                        putRequest,
                                        null,
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

        }

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

    /**
     * Unused getters are for Jackson.
     */
    public static class NMEACacheBean {
        private String cls; // Class
        private String type = "nmea-cache-publisher";
        protected long betweenLoops;
        protected String protocol;
        protected String verb;
        protected String machineName;
        protected int port;
        protected String resource;
        protected String queryString;
        protected String doOnClose;
        protected String onCloseVerb;

        public NMEACacheBean() {}   // This is for Jackson
        public NMEACacheBean(NMEACachePublisher instance,
                             long betweenLoops,  // TODO Check factor 1000 ?
                             String protocol,
                             String verb,
                             String machineName,
                             int port,
                             String resource,
                             String qs,
                             String doOnClose,
                             String onCloseVerb) {
            this.cls = instance.getClass().getName();
            this.betweenLoops = betweenLoops;
            this.protocol = protocol;
            this.verb = verb;
            this.machineName = machineName;
            this.port = port;
            this.resource = resource;
            this.queryString = qs;
            this.doOnClose = doOnClose;
            this.onCloseVerb = onCloseVerb;
        }

        public String getCls() {
            return cls;
        }

        public String getType() {
            return type;
        }

        public long getBetweenLoops() {
            return betweenLoops;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getVerb() {
            return verb;
        }

        public String getMachineName() {
            return machineName;
        }

        public int getPort() {
            return port;
        }

        public String getResource() {
            return resource;
        }

        public String getQueryString() {
            return queryString;
        }

        public String getDoOnClose() {
            return doOnClose;
        }

        public String getOnCloseVerb() {
            return onCloseVerb;
        }
    }

    @Override
    public Object getBean() {
        return new NMEACacheBean(this, this.betweenPublish, this.protocol, this.verb, this.machineName, this.port, this.resource, this.queryString, this.onCloseResource, this.onCloseVerb);
    }

    @Override
    public void setProperties(Properties props) {
		
		String betweenLoops = props.getProperty("between.loops", "1");
        try {
            this.betweenPublish = Long.parseLong(betweenLoops) * 1_000L;
        } catch (NumberFormatException nfe) {
            System.err.println("Using default value for between.loops time");
        }
        this.verbose = "true".equals(props.getProperty("verbose", "false"));
        this.protocol = props.getProperty("rest.protocol", "http");  // Make sure it is http or https
        this.verb = props.getProperty("rest.verb", "PUT");
        this.machineName = props.getProperty("rest.machine-name", "http");
        this.port = Integer.parseInt(props.getProperty("rest.port", "8080")); // TODO trap exception
        this.resource = props.getProperty("rest.resource", "/");
        this.queryString = props.getProperty("rest.query.string");

        this.onCloseResource = props.getProperty("rest.onclose.resource");;
        this.onCloseVerb = props.getProperty("rest.onclose.verb");;
    }
}

package nmea.forwarders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import context.NMEADataCache;
import http.client.HTTPClient;
import nmea.api.BeanInterface;
import utils.DumpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Forward the full (or shrinked) NMEA Cache to a REST resource, in JSON.
 * This requires a knowledge of its structure...
 * This can be seen as an NMEA bus, forwarding the content to a server (a display or so) that
 * knows what to do with it (like what to display from the cache).
 *
 * REST verb can be PUT or POST. Other verbs would not really make sense here.
 */
public class NMEACachePublisher implements Forwarder {

    private final static ObjectMapper mapper = new ObjectMapper();

    private boolean keepWorking = true;

    private NMEACachePublisher instance = null;

    private boolean verbose = false;
    private long betweenPublish = 1; // In seconds
    private String protocol = "http";
    private String verb = "PUT";
    private String machineName = "localhost";
    private int port = 8888;
    private String resource = "/";
    private String queryString = null;
    private String option = "full"; // TODO Manage that one: 'full', 'small', 'tiny', 'minimal'

    private boolean active = true;
    private String description = "No description";
    private String idx;
    private String onCloseResource = null;
    private String onCloseVerb = null;

    private Thread cacheThread;

    public NMEACachePublisher getInstance() {
        return this.instance;
    }

    public NMEACachePublisher(String idx,
                              Long betweenPublish,
                              String verb,
                              String protocol,
                              String machineName,
                              Integer port,
                              String resource,
                              String qs,
                              boolean verbose,
                              boolean active,
                              String desc) throws Exception {
        this(idx,
             betweenPublish,
             verb,
             protocol,
             machineName,
             port,
             resource,
             qs,
             verbose,
             active,
             null,
             null,
             desc);
    }

    public NMEACachePublisher(String idx,
                              Long betweenPublish,
                              String verb,
                              String protocol,
                              String machineName,
                              Integer port,
                              String resource,
                              String qs,
                              boolean verbose,
                              boolean active,
                              String doOnClose,
                              String onCloseVerb,
                              String desc) throws Exception {

        if (false) {
            System.out.println("Before constructor:");
            System.out.printf("Idx: %s, Resource: %s\n", this.idx, this.resource);
        }

        this.instance = this;
        this.idx = idx;

        this.verbose = verbose;
        this.active = active;
        this.description = desc;

        if (betweenPublish != null) {
            this.betweenPublish = betweenPublish;
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
        if (false) {
            System.out.println("After constructor:");
            System.out.printf("Idx: %s, Resource: %s\n", this.idx, this.resource);
        }

    }

    private void initCacheThread(String idx) {
        // This is the loop providing the cache data
        if (instance.verbose) {
            System.out.printf("-- NMEACacheForwarder, initCacheThread, %s\n",
                    String.format("%s: %s://%s:%d%s%s",
                            idx,
                            this.protocol,
                            this.machineName,
                            this.port,
                            this.resource,
                            this.queryString == null ? "" : this.queryString));
        }
        Thread restThread = new Thread("CachePublisherThread-" + idx) {
            public void run() {
                while (instance.keepWorking) {

                    String restRequest = String.format("%s://%s:%d%s%s",
                            instance.protocol,
                            instance.machineName,
                            instance.port,
                            instance.resource,
                            instance.queryString == null ? "" : instance.queryString);

                    if (instance.verbose) {
                        System.out.printf("\tIn thread %s, --> In the loop, TOP: curl -X %s %s\n",
                                this.getName(),
                                instance.verb,
                                restRequest);
                    }

                    if (instance.isActive()) {
                        NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
                        try {
                            // Options to minimize the cache (like 'full', 'small', 'tiny', 'minimal') ?
                            if (!option.equals("full")) {
                                if (instance.verbose) {
                                    System.out.printf("--> CachePublisher, shrinking the cache (option %s)\n", option);
                                }
                                // To remove: markers-file-name, Bearing to WP, borders-data, routes-data,
                                //            next-waypoint, Deviation data
                                cache.remove("markers-file-name");
                                cache.remove("Bearing to WP");
                                cache.remove("borders-data");
                                cache.remove("routes-data");
                                cache.remove("next-waypoint");
                                cache.remove("Deviation data");
                            }
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
                                            System.out.printf("\tPUT case: curl -X PUT %s\n%s\n", putRequest, putStrContent);
                                        }
                                        try {
                                            HTTPClient.HTTPResponse putResponse = HTTPClient.doPut(putRequest, headers, putStrContent);
                                            if (instance.verbose) {
                                                System.out.printf("\tPUT %s with %s: Response code %d, message: %s\n",
                                                        putRequest,
                                                        putStrContent,
                                                        putResponse.getCode(),
                                                        putResponse.getPayload());
                                            }
                                        } catch (Throwable restFailure) {
                                            System.err.printf("\t>> PUT (%s) Error in NMEACachePublisher: %s\n",
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
                    } else {
                        // Inactive instance
                        if (instance.verbose) {
                            System.out.printf("\tInactive Forwarder: %s \n", restRequest);
                        }
                    }
                    try {
                        if (instance.verbose) {
                            System.out.printf("Sleeping for %d s\n", betweenPublish);
                        }
                        Thread.sleep(betweenPublish * 1_000L);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                System.out.println("Cache thread completed.");
            }
        };
        this.cacheThread = restThread;
        this.cacheThread.start();
    }

    @Override
    public void init() {
        initCacheThread(this.idx);
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean status) {

        System.out.printf("-- Forwarder NMEACachePublisher, setActive method: from %B to %B\n", this.active, status);

        this.active = status;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public void setOption(String opt) {
        this.option = opt;
    }

    public Thread getCacheThread() {
        return this.cacheThread;
    }

    @Override
    public void write(byte[] message) {
        // Nothing is done here.
        // It is replaced by the Thread in the constructor, in init -> initCacheThread
        if (false && this.verbose) {
            System.out.printf("write was invoked on NMEACachePublisher (with payload [%s]\n", new String(message).trim());
        }
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
            System.out.printf("Killing thread %s\n", this.cacheThread.getName());
            this.keepWorking = false;
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
    public static class NMEACacheBean implements BeanInterface {
        private String cls; // Class
        private String type = "nmea-cache-publisher";
        private long betweenLoops;
        private String protocol;
        private String verb;
        private String machineName;
        private int port;
        private String resource;
        private String queryString;
        private String doOnClose;
        private String onCloseVerb;
        private boolean active;
        private boolean verbose;
        private String description;

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
                             String onCloseVerb,
                             boolean active,
                             String desc) {
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
            this.active = active;
            this.description = desc;
        }

        @Override
        public String getCls() {
            return cls;
        }

        @Override
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
        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public boolean isVerbose() {
            return verbose;
        }

        @Override
        public String getDescription() {
            return description;
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

        public String getCURLString() {
            return String.format("curl -X %s %s://%s:%d%s [data]", this.verb, this.protocol, this.machineName, this.port, this.resource);
        }
    }

    @Override
    public BeanInterface getBean() {
        return new NMEACacheBean(this,
                this.betweenPublish,
                this.protocol,
                this.verb,
                this.machineName,
                this.port,
                this.resource,
                this.queryString,
                this.onCloseResource,
                this.onCloseVerb,
                this.active,
                this.description);
    }

    @Override
    public void setProperties(Properties props) {

        if (true) {
            System.out.println("--> NMEACachePublisher.setProperties invoked from:");
            final List<String> strings = DumpUtil.whoCalledMe();
            strings.stream().forEach(System.out::println);
            System.out.println("--------------------------------------------------");
        }
        // TODO Implement...
    }
}
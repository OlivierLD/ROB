package nmea.mux;

import context.ApplicationContext;
import http.HTTPServer;
import http.RESTRequestManager;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAParser;
import nmea.computers.Computer;
import nmea.forwarders.Forwarder;
import nmea.mux.context.Context;
import org.yaml.snakeyaml.Yaml;
import utils.DumpUtil;
import utils.StaticUtil;

import java.io.*;
import java.net.ConnectException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * <b>NMEA Multiplexer.</b><br>
 * The main. See main method javadoc for more.
 * <br/>
 * Also contains the definition of the REST operations for admin purpose.<br>
 * See {@link RESTRequestManager} and {@link HTTPServer}.<br>
 * Also see below the definition of <code>List&lt;Operation&gt; operations</code>.
 */
public class GenericNMEAMultiplexer implements RESTRequestManager, Multiplexer {
    private HTTPServer adminServer; // = null;
    protected Properties muxProperties;

    private Context.TopContext topContext;
    private final List<NMEAClient> nmeaDataClients = new ArrayList<>(); // Consumers, aka Channels
    private final List<Forwarder> nmeaDataForwarders = new ArrayList<>();
    private final List<Computer> nmeaDataComputers = new ArrayList<>();

    private final RESTImplementation restImplementation;

    public Context.TopContext getTopContext() {
        return this.topContext;
    }

    /**
     * Implements the management of the REST requests (see {@link RESTImplementation})
     * Dedicated Admin Server.
     * This method is called by the HTTPServer through the current RESTRequestManager
     *
     * @param request the parsed request.
     * @return the response, along with its HTTP status code.
     */
    @Override
    public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
//	HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.NOT_IMPLEMENTED);
        HTTPServer.Response response = restImplementation.processRequest(request); // All the skill is here.
        if (verbose) {
            this.getLogger().log(Level.INFO, "======================================");
            this.getLogger().log(Level.INFO, "Request :\n" + request.toString());
            this.getLogger().log(Level.INFO, "Response :\n" + response.toString());
            this.getLogger().log(Level.INFO, "======================================");
        }
        return response;
    }

    @Override
    public List<HTTPServer.Operation> getRESTOperationList() {
        return restImplementation.getOperations();
    }

    @Override
    public synchronized void onData(String mess) {
        // To measure the flow (in bytes per time)
        Context.getInstance().addManagedBytes(mess.length());

        // Last sentence (inbound)
        Context.getInstance().setLastDataSentence(mess); // That one also increments the nb of messages processed.

        if (verbose) {
            System.out.println("==== From MUX: " + mess);
            DumpUtil.displayDualDump(mess);
            System.out.println("==== End Mux =============");
        }
        // Cache, if initialized
        if (ApplicationContext.getInstance().getDataCache() != null) {
            ApplicationContext.getInstance().getDataCache().parseAndFeed(mess);
        }

        if (this.process) {
            // Computers. Must go first, as a computer may re-feed the present onData method.
            synchronized (nmeaDataComputers) {
                nmeaDataComputers
                        .forEach(computer -> computer.write(mess.getBytes()));
            }

            // Forwarders
            synchronized (nmeaDataForwarders) {
                nmeaDataForwarders
                        .forEach(fwd -> {
                            try {
                                fwd.write((mess.trim() + NMEAParser.STANDARD_NMEA_EOS).getBytes());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    @Override
    public void setVerbose(boolean b) {
        verbose = b;
    }

    @Override
    public void setEnableProcess(boolean b) {
        this.process = b;
    }

    @Override
    public boolean getEnableProcess() {
        return this.process;
    }

    @Override
    public void stopAll() {
        // Send Ctrl+C
        softStop = true;
        terminateMux();
//	System.exit(0);
//	try {  Thread.sleep(2_500L); } catch (InterruptedException ie) {}
        System.out.println("Soft Exit");
        Runtime.getRuntime().exit(0); // Ctrl-C for the HTTP Server
    }

    private static boolean verbose = "true".equals(System.getProperty("mux.data.verbose"));
    private final static boolean infraVerbose = "true".equals(System.getProperty("mux.infra.verbose", "true")); // Defaulted to true (for now)
    private boolean process = true; // onData, forward to computers and forwarders

    private boolean softStop = false;

    public void terminateMux() {
        System.out.println("Shutting down multiplexer nicely.");
        if (adminServer != null && softStop) {
            // Delay for the REST response
            //	System.out.println("Waiting a bit (for REST terminate request to complete)...");
            try {
                Thread.sleep(1_000L);
            } catch (InterruptedException ie) {
                // Absorb
            }
//		System.out.println("Done waiting");
        }
        nmeaDataClients.forEach(NMEAClient::stopDataRead);
        nmeaDataForwarders.forEach(Forwarder::close);
        nmeaDataComputers.forEach(Computer::close);

        if (adminServer != null) {
            synchronized (adminServer) {
                // System.out.println("Mux Stopping Admin server");
                this.getLogger().log(Level.INFO, "Mux Stopping Admin server");
                try {
                    adminServer.stopRunning();
                } catch (Exception ex) {
                    if (ex instanceof RuntimeException) {
                        if (ex.getCause() instanceof ConnectException) {
                            System.err.println(">> ConnectException when shutting down the adminServer. Not un-expected, already dead.");
                        } else {
                            ex.printStackTrace();
                        }
                    } else {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Constructor.
     *
     * @param muxProps Initial config. See {@link #main(String...)} method.
     */

    public GenericNMEAMultiplexer(Properties muxProps) { // TODO A Constructor with yaml?

        /* From definition into context: name, description, context
         * -----------------------------
         * name: "Log GPS and AIS Data."
         * description:
         *   - Reads GPS and AIS from serial ports
         *   - Log data in 'logged' folder
         *   - Collision detection
         * context:
         *   with.http.server: true
         *   http.port: 9999
         *   init.cache: true
         *   default.declination: -1
         *   deviation.file.name: "dp_2011_04_15.csv"
         *   max.leeway: 10.0
         *   damping: 30
         *   markers: markers.yaml
         *   markers.list:
         *     - markers: markers.04.yaml
         *     - markers: markers.05.yaml
         *     - markers: markers.houat.hoedic.belle-ile.yaml
         *     - markers: markers.couregant.la.plate.yaml
         */
        final Context context = Context.getInstance();
        Context.TopContext instanceContext = new Context.TopContext();
        // name
        instanceContext.setName(muxProps.getProperty("name"));
        // description
        int _descIdx = 1;
        List<String> desc = null;
        while (muxProps.getProperty(String.format("description.%02d", _descIdx)) != null) {
            String value = muxProps.getProperty(String.format("description.%02d", _descIdx));
            _descIdx++;
            if (desc == null) {
                desc = new ArrayList<>();
            }
            desc.add(value);
        }
        if (desc != null) {
            instanceContext.setDescription(desc);
        }
        // with.http.server
        if (muxProps.getProperty("with.http.server") != null) {
            instanceContext.setWithHTTPServer("true".equals(muxProps.getProperty("with.http.server")));
        }
        // http.port
        if (muxProps.getProperty("http.port") != null) {
            instanceContext.setHttpPort(Integer.parseInt(muxProps.getProperty("http.port")));
        }
        // init.cache
        if (muxProps.getProperty("init.cache") != null) {
            instanceContext.setInitCache("true".equals(muxProps.getProperty("init.cache")));
        }
        // default.declination
        if (muxProps.getProperty("default.declination") != null) {
            instanceContext.setDefaultDeclination(Double.parseDouble(muxProps.getProperty("default.declination")));
        }
        // deviation.file.name
        instanceContext.setDeviationFileName(muxProps.getProperty("deviation.file.name"));
        // max.leeway
        if (muxProps.getProperty("max.leeway") != null) {
            instanceContext.setMaxLeeway(Double.parseDouble(muxProps.getProperty("max.leeway")));
        }
        // damping
        if (muxProps.getProperty("damping") != null) {
            instanceContext.setDamping(Integer.parseInt(muxProps.getProperty("damping")));
        }
        // markers and markers.list
        instanceContext.setMarkers(muxProps.getProperty("markers"));
        _descIdx = 1;
        List<String[]> markers = null;
        while (muxProps.getProperty(String.format("markers.list.%02d", _descIdx)) != null) {
            String value = muxProps.getProperty(String.format("markers.list.%02d", _descIdx));
            _descIdx++;
            if (markers == null) {
                markers = new ArrayList<>();
            }
            markers.add(new String[] { value, "--" });
        }
        if (markers != null) {
            instanceContext.setMarkerList(markers);
        }
        // And finally
        context.setMainContext(instanceContext);
        this.topContext = instanceContext;

        // Display logging config
        LogManager logManager = LogManager.getLogManager();
        System.out.printf("Log available in %s, level %s\nLog file pattern %s\n",
                this.getLogger().getName(),
                this.getLogger().getLevel().getName(),
                logManager.getProperty("java.util.logging.FileHandler.pattern"));
        // ----------------------

        this.muxProperties = muxProps;
        Context.getInstance().setStartTime(System.currentTimeMillis());

        if (infraVerbose) {
            System.out.printf("\t>> %s (%s) - Constructor %s, Initializing RESTImplementation...\n",
                    NumberFormat.getInstance().format(System.currentTimeMillis()),
                    this.getClass().getName(),
                    this.getClass().getName());
        }
        // Read initial config from the properties file. See the main method.
//		verbose = "true".equals(System.getProperty("mux.data.verbose", "false")); // Initial verbose.
        restImplementation = new RESTImplementation(nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this);
        MuxInitializer.setup(muxProps, nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, this, verbose);

        if (infraVerbose) {
            System.out.printf("\t>> %s (%s) - RESTImplementation initialized.\n",
                    NumberFormat.getInstance().format(System.currentTimeMillis()),
                    this.getClass().getName());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!softStop) {
                terminateMux();
            }
        }, "Multiplexer shutdown hook"));

        nmeaDataClients.forEach(client -> {
            if (infraVerbose) {
                System.out.printf("\t>> %s (%s) - NMEADataClient: Starting %s...\n",
                        NumberFormat.getInstance().format(System.currentTimeMillis()),
                        client.getClass().getName(),
                        client.getClass().getName());
            }
            try {
                client.startWorking();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        if (infraVerbose) {
            System.out.printf("\t>> %s (%s) constructor completed.\n",
                    NumberFormat.getInstance().format(System.currentTimeMillis()),
                    this.getClass().getName());
        }
    }

    //	@Override
    public Properties getMuxProperties() {
        return this.muxProperties;
    }

    public void startAdminServer(int port) {
        try {
            this.adminServer = new HTTPServer(port, this);
            this.adminServer.startServer();
            if (infraVerbose) {
                System.out.printf("\t>> %s (%s) - Starting Admin server on port %d\n",
                        NumberFormat.getInstance().format(System.currentTimeMillis()),
                        this.getClass().getName(),
                        port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getDefinitions() {
        Properties properties = null;
        String propertiesFile = System.getProperty("mux.properties", "nmea.mux.properties");
        if (propertiesFile.endsWith(".yaml") || propertiesFile.endsWith(".yml")) { // Yaml to props
            Yaml yaml = new Yaml();
            try {
                InputStream inputStream = new FileInputStream(propertiesFile);
                Map<String, Object> map = yaml.load(inputStream);
                properties = MuxInitializer.yamlToProperties(map);
            } catch (IOException ioe) {
                throw new RuntimeException(String.format("File [%s] not found in %s", propertiesFile, System.getProperty("user.dir")));
            }
        } else if (propertiesFile.endsWith(".properties")) {
            Properties definitions = new Properties();
            File propFile = new File(propertiesFile);
            if (!propFile.exists()) {
                throw new RuntimeException(String.format("File [%s] not found in %s, see property 'mux.properties'", propertiesFile, System.getProperty("user.dir")));
            } else {
                try {
                    definitions.load(new FileReader(propFile));
                    properties = definitions;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return properties;
    }

    /**
     * WiP.
     * Triggered from the main, if a CLI parameter "--interactive-config" is present.
     *
     * TODO Update that one !!! REST & Cie...
     *
     * @return the generated Properties Object used at runtime.
     */
    private static Properties interactiveConfig() {
        Properties props;

        System.out.println("--- W A R N I N G ---");
        System.out.println(" This is a development feature... \nIt might not be (actually it is NOT) 100% in sync with the real soft.");
        System.out.println(" Look into the code to finish the job!");
        System.out.println(" You are going to be prompted to enter whatever would be read from the config files (yaml or properties).");
        System.out.println(" This being said, let's proceed. (Ctrl-C will get you off the hook)\n");
        System.out.println("-- Enter Mux config interactively --");
        Yaml yaml = new Yaml();
        Map<String, Object> topMap = new HashMap<>();

        String input = StaticUtil.userInput("> Enter a name for this MUX > ");
        topMap.put("name", input);

        Map<String, Object> contextMap = new HashMap<>();
        input = StaticUtil.userInput("> With HTTP server y|n ? > ");
        boolean withHttpServer = input.equalsIgnoreCase("Y");
        contextMap.put("with.http.server", withHttpServer);
        if (withHttpServer) {
            // http port
            input = StaticUtil.userInput("> HTTP port ? > ");
            int port = Integer.parseInt(input);
            contextMap.put("http.port", port);
            // init.cache
            input = StaticUtil.userInput("> Init cache y|n ? > ");
            boolean initCache = input.equalsIgnoreCase("Y");
            contextMap.put("init.cache", initCache);
        }
        topMap.put("context", contextMap);

        // Channels (List)
        List<Map<String, Object>> channels = new ArrayList<>();

        input = StaticUtil.userInput("Replay (hard-coded) log file y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {

            final String zipFile = "./sample-data/logged.data.zip";
            final String pathInZip = "2010-11-08.Nuku-Hiva-Tuamotu.nmea";

            System.out.printf("\tWill (try to) replay the data logged in %s, %s\n", zipFile, pathInZip);

            Map<String, Object> oneChannel = new HashMap<>();

            oneChannel.put("type", "file");
            oneChannel.put("filename", zipFile);
            oneChannel.put("path.in.zip", pathInZip);
            oneChannel.put("zip", true);
            oneChannel.put("verbose", false);

            channels.add(oneChannel);
        }
        input = StaticUtil.userInput("Read Serial port y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {

            final String serialPort = "/dev/ttyS80";
            final int baudRate = 4_800;

            System.out.printf("\tWill read serial port %s:%d\n", serialPort, baudRate);

            Map<String, Object> oneChannel = new HashMap<>();

            oneChannel.put("type", "serial");
            oneChannel.put("port", serialPort);
            oneChannel.put("baudrate", baudRate);
            oneChannel.put("verbose", false);

            channels.add(oneChannel);
        }

        topMap.put("channels", channels);

        // Forwarder (List)
        List<Map<String, Object>> forwarders = new ArrayList<>();
        Map<String, Object> oneForwarder;

        input = StaticUtil.userInput("Forwarder. Console y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "console");
            oneForwarder.put("verbose", false);
            forwarders.add(oneForwarder);
        }

        input = StaticUtil.userInput("Forwarder. REST y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "rest");
            oneForwarder.put("server.name", "192.168.42.6");
            oneForwarder.put("server.port", 9999);
            oneForwarder.put("rest.resource", "/mux/nmea-sentence");
            oneForwarder.put("rest.verb", "POST");
            oneForwarder.put("http.headers", "Content-Type:text/plain");
            oneForwarder.put("verbose", true);
            forwarders.add(oneForwarder);
        }

        input = StaticUtil.userInput("Forwarder. TCP (no AIS) y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "tcp");
            oneForwarder.put("port", 7002);
            oneForwarder.put("properties", "no.ais.properties");
            forwarders.add(oneForwarder);
        }

        input = StaticUtil.userInput("Forwarder. TCP (AIS only) y|n ? > ");
        if ("Y".equalsIgnoreCase(input)) {
            oneForwarder = new HashMap<>();
            oneForwarder.put("type", "tcp");
            oneForwarder.put("subclass", "nmea.forwarders.AISTCPServer");
            oneForwarder.put("port", 7003);
            oneForwarder.put("verbose", false);
            forwarders.add(oneForwarder);
        }

        topMap.put("forwarders", forwarders);

        // Computers (List)

        // Others (dev curve, and so)

        String output = yaml.dumpAsMap(topMap);
        // FYI...
        System.out.println("-- Running with config ---");
        System.out.println(output);
        System.out.println("--------------------------");
        /* String dummy = */ StaticUtil.userInput("Enter [Return] to move on > ");

        props = MuxInitializer.yamlToProperties(topMap);
        return props;
    }

    /**
     *
     * @param definitions
     * definition like (in yaml here):
     *
     * name: "Log GPS and AIS Data."    => 'name'
     * description:                     => 'description.01..xx'
     *   - Reads GPS and AIS from serial ports
     *   - Log data in 'logged' folder
     *   - Collision detection
     * context:
     *   with.http.server: true         => 'with.http.server'
     *   http.port: 9999                => 'http.port'
     *   init.cache: true               => 'init.cache'
     *   default.declination: -1        => 'default.declination'
     *   deviation.file.name: "dp_2011_04_15.csv"   => 'deviation.file.name'
    *    max.leeway: 10.0                           => 'max.leeway'
    *    damping: 30                                => 'damping'
     *   markers: markers.yaml                      => 'markers'
     *   markers.list:                              => 'markers.list.01..xx'
     *     - markers: markers.04.yaml
     *     - markers: markers.05.yaml
     *     - markers: markers.houat.hoedic.belle-ile.yaml
     *     - markers: markers.couregant.la.plate.yaml
     *   next-waypoint: BUGALET
     */
    public static void initDefinitions(Properties definitions) {
        if (definitions.get("name") != null) {
            System.out.printf("Definition Name: %s\n", definitions.get("name"));
        }
        int descIdx = 1;
        while (true) {
            String propName = String.format("description.%02d", descIdx);
            if (definitions.get(propName) != null) {
                if (descIdx == 1) {
                    System.out.println("-- Description --");
                }
                System.out.println(definitions.get(propName));
                descIdx++;
            } else {
                break;
            }
        }
        if (descIdx > 1) {
            System.out.println("-----------------");
        }

        if (infraVerbose) {
            System.out.println("-- MUX Definitions: --");
            definitions.list(System.out);
            System.out.println("----------------------");
        }

        boolean startProcessingOnStart = "true".equals(System.getProperty("process.on.start", "true"));
        if (infraVerbose) {
            System.out.printf("PROCESS ON START: %b\n", startProcessingOnStart);
        }
        GenericNMEAMultiplexer mux = new GenericNMEAMultiplexer(definitions);
        mux.setEnableProcess(startProcessingOnStart);
//        if (true) {
//            System.out.printf("TopContext is %s\n", instanceContext == null ? "Null !" : String.format("not null: %s", instanceContext.toString()));
//        }
//        mux.topContext = instanceContext;

        // with.http.server=yes
        // http.port=9999
        String withHttpServer = definitions.getProperty("with.http.server", "no");
        if ("yes".equals(withHttpServer) || "true".equals(withHttpServer)) {
            int httpPort = Integer.parseInt(definitions.getProperty("http.port", "9999"));
            if (infraVerbose) {
                System.out.printf("(%s) Starting Admin server on port %d\n",
                        GenericNMEAMultiplexer.class.getName(),
                        httpPort);
            }
            mux.startAdminServer(httpPort);
        } else {
            if (infraVerbose) {
                System.out.printf("\t>> (%s) NO Admin server started\n",
                        GenericNMEAMultiplexer.class.getName());
            }
        }
    }

    /**
     * Start the Multiplexer from here.
     * Supported System variables (VM options):
     * - process.on.start (default true)
     * - mux.data.verbose (default false)
     * - mux.infra.verbose (default false)
     * - mux.properties (default 'nmea.mux.properties')
     * - yaml.tx.verbose (default no)
     *
     * @param args CLI prms. see "--interactive-config".
     */
    public static void main(String... args) {
        AtomicBoolean interactiveConfig = new AtomicBoolean(false);
        Arrays.asList(args).forEach(prm -> {
            if ("--interactive-config".equals(prm)) {
                interactiveConfig.set(true);
            }
        });

        Properties definitions = interactiveConfig.get() ? GenericNMEAMultiplexer.interactiveConfig() :  GenericNMEAMultiplexer.getDefinitions();
        System.out.println("TOP Level, definition:\n>>------------\n" + definitions + "\n------------<<\n");

        initDefinitions(definitions);
    }
}

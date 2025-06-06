package nmea.mux;

import context.ApplicationContext;
import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAReader;
import nmea.computers.Computer;
import nmea.computers.DewPointTemperatureComputer;
import nmea.computers.ExtraDataComputer;
import nmea.computers.LongTermStorage;
import nmea.consumers.client.*;
import nmea.consumers.client.UDPServer;
import nmea.consumers.reader.*;
import nmea.forwarders.*;
import nmea.forwarders.UDPClient;
import nmea.forwarders.rmi.RMIServer;
import nmea.parser.StringParsers;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Initialize the configuration of the Multiplexer, at startup,
 * with the properties read from the file system, through one
 * package-private method named <code>{@link #setup}</code>.
 * <br/>
 * Initializes:
 * <ul>
 *   <li>NMEA Channels</li>
 *   <li>NMEA Forwarders</li>
 *   <li>NMEA Computers</li>
 * </ul>
 * All those objects can be also managed later on, through the REST Admin Interface
 * <br/>
 * (see {@link RESTImplementation}).
 */
public class MuxInitializer {

    private final static NumberFormat MUX_IDX_FMT = new DecimalFormat("00");

    private static void spitOutSentenceFilters(String sentenceFilters) {
        if (!sentenceFilters.trim().isEmpty()) {
            Arrays.asList(sentenceFilters.trim().split(","))
                    .stream()
                    .map(String::trim)
                    .forEach(filter -> {
                        String key = filter;
                        boolean exclude = filter.startsWith("~");
                        if (exclude) {
                            key = filter.substring(1);
                        }
                        final StringParsers.Dispatcher dispatcherByKey = StringParsers.findDispatcherByKey(key);
                        String description = "";
                        if (dispatcherByKey != null) {
                            description = dispatcherByKey.description();
                        }
                        System.out.printf("%s sentence [%s]%s\n",
                                exclude ? "Excluding" : "Including",
                                key,
                                description.trim().length() > 0 ? String.format(" (%s)", description) : "");
                    });
        }
    }

    public static void setup(Properties muxProps,
                             List<NMEAClient> nmeaDataClients,
                             List<Forwarder> nmeaDataForwarders,
                             List<Computer> nmeaDataComputers,
                             Multiplexer mux) {
        setup(muxProps, nmeaDataClients, nmeaDataForwarders, nmeaDataComputers, mux, false);
    }

    /**
     * This is the method to call to initialize the {@link Multiplexer}.
     * The 3 <code>List</code>s must have been created in it, as they will be populated here.
     *
     * @param muxProps           The properties to get the data from. See <a href="../../../../README.md">here</a> for more details.
     * @param nmeaDataClients    List of the input channels
     * @param nmeaDataForwarders List of the output channels
     * @param nmeaDataComputers  List of the data computers
     * @param mux                the Multiplexer instance to initialize
     * @param verbose            Speak up!
     */
    public static void setup(Properties muxProps,
                             List<NMEAClient> nmeaDataClients,
                             List<Forwarder> nmeaDataForwarders,
                             List<Computer> nmeaDataComputers,
                             Multiplexer mux,
                             boolean verbose) {
        int muxIdx = 1;
        boolean thereIsMore = true;
        // 1 - Input channels
        while (thereIsMore) {
            String classProp = String.format("mux.%s.class", MUX_IDX_FMT.format(muxIdx));
            String clss = muxProps.getProperty(classProp);
            if (clss != null) { // Dynamic loading
                if (verbose) {
                    System.out.printf("\t>> %s - Dynamic loading for input channel %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                }
                try {
                    // Devices and Sentences filters.
                    String deviceFilters = "";
                    String sentenceFilters = "";
                    deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    if (verbose) {
                        spitOutSentenceFilters(sentenceFilters);
                    }
                    Object dynamic = Class.forName(clss)
                            .getDeclaredConstructor(String[].class, String[].class, Multiplexer.class)
                            .newInstance(
                                    !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                    !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                    mux);
                    if (dynamic instanceof NMEAClient) {
                        NMEAClient nmeaClient = (NMEAClient) dynamic;

                        String verboseProp = String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx));
                        nmeaClient.setVerbose("true".equals(muxProps.getProperty(verboseProp)));

                        String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                        String propFileName = muxProps.getProperty(propProp);
                        Properties readerProperties = null;
                        if (propFileName != null) {
                            try {
                                readerProperties = new Properties();
                                readerProperties.load(new FileReader(propFileName));
                                nmeaClient.setProperties(readerProperties);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        nmeaClient.initClient();
                        NMEAReader reader = null;
                        try {
                            String readerProp = String.format("mux.%s.reader", MUX_IDX_FMT.format(muxIdx));
                            String readerClass = muxProps.getProperty(readerProp);
                            if (readerClass != null) {
                                // Cannot invoke declared constructor with a generic type... :(
                                if (readerProperties == null) {
                                    reader = (NMEAReader) Class.forName(readerClass).getDeclaredConstructor(String.class, List.class).newInstance(readerProp, nmeaClient.getListeners());
                                } else {
                                    reader = (NMEAReader) Class.forName(readerClass).getDeclaredConstructor(String.class, List.class, Properties.class).newInstance(readerProp, nmeaClient.getListeners(), readerProperties);
                                }
                            } else {
                                // A dynamic Consumer may require a Reader.
                                System.out.println("No 'reader' in the properties.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (reader != null) {
                            nmeaClient.setReader(reader);
                        }
                        nmeaDataClients.add(nmeaClient);
                    } else {
                        throw new RuntimeException(String.format("Expected an NMEAClient, found a [%s]", dynamic.getClass().getName()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                String typeProp = String.format("mux.%s.type", MUX_IDX_FMT.format(muxIdx));
                String type = muxProps.getProperty(typeProp);
                if (type == null) {
                    thereIsMore = false;
                } else {
                    if (verbose) {
                        System.out.printf("\t>> %s - Loading channel %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                    }
                    // String deviceFilters = "";
                    // String sentenceFilters = "";

                    // Make this generic, not specific like below.
                    String deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    String sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                    if (verbose) {
                        spitOutSentenceFilters(sentenceFilters);
                    }

                    switch (type) {
                        case "serial": // Consumer
                            try {
                                String serialPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
                                String br = muxProps.getProperty(String.format("mux.%s.baudrate", MUX_IDX_FMT.format(muxIdx)));
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String resetIntervalStr = muxProps.getProperty(String.format("mux.%s.reset.interval", MUX_IDX_FMT.format(muxIdx)));
                                Long resetInterval = null;
                                if (resetIntervalStr != null) {
                                    try {
                                        resetInterval = Long.parseLong(resetIntervalStr);
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
                                    }
                                }
                                SerialClient serialClient = new SerialClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        serialClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                serialClient.initClient();
                                serialClient.setReader(new SerialReader("MUX-SerialReader", serialClient.getListeners(), serialPort, Integer.parseInt(br), resetInterval));
                                serialClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(serialClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "rest": // Consumer
                            try {
//                                System.out.printf("REST Consumer required here (#%d), come back soon! (See above)\n", muxIdx);
                                String machineName = muxProps.getProperty(String.format("mux.%s.machine-name", MUX_IDX_FMT.format(muxIdx)));
                                String protocol = muxProps.getProperty(String.format("mux.%s.protocol", MUX_IDX_FMT.format(muxIdx)));
                                String httPort = muxProps.getProperty(String.format("mux.%s.http-port", MUX_IDX_FMT.format(muxIdx)));
                                String queryPath = muxProps.getProperty(String.format("mux.%s.query-path", MUX_IDX_FMT.format(muxIdx)));
                                String queryString = muxProps.getProperty(String.format("mux.%s.query-string", MUX_IDX_FMT.format(muxIdx)));
                                String jqString = muxProps.getProperty(String.format("mux.%s.jqs", MUX_IDX_FMT.format(muxIdx)));
                                Long betweenLoops = null;
                                String strBetweenLoops = muxProps.getProperty(String.format("mux.%s.between-loops", MUX_IDX_FMT.format(muxIdx)));
                                if (strBetweenLoops != null) {
                                    try {
                                        betweenLoops = Long.parseLong(strBetweenLoops);
                                    } catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
                                    }
                                }
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                RESTClient restClient = new RESTClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        restClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                restClient.initClient();
                                restClient.setReader(new RESTReader("MUX-RESTReader",
                                                        restClient.getListeners(),
                                                        protocol,
                                                        machineName,
                                                        Integer.parseInt(httPort),
                                                        queryPath,
                                                        queryString,
                                                        jqString,
                                                        betweenLoops));
                                restClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(restClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "tcp": // Consumer
                            try {
                                String tcpPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
                                String tcpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                String initialRequest = muxProps.getProperty(String.format("mux.%s.initial.request", MUX_IDX_FMT.format(muxIdx)), "");
                                boolean keepTrying = "true".equals(muxProps.getProperty(String.format("mux.%s.keep.trying", MUX_IDX_FMT.format(muxIdx)), "false"));
                                TCPClient tcpClient = new TCPClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        tcpClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                tcpClient.initClient();
                                if (initialRequest.trim().isEmpty()) {
                                    tcpClient.setReader(new TCPReader("MUX-TCPReader", tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort), keepTrying));
                                } else {
                                    tcpClient.setReader(new TCPReader("MUX-TCPReader", tcpClient.getListeners(), tcpServer, Integer.parseInt(tcpPort), initialRequest, keepTrying));
                                }
                                tcpClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(tcpClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "file": // Consumer
//                            System.out.println("MUX Props:\n" + muxProps);
                            try {
                                String filename = muxProps.getProperty(String.format("mux.%s.filename", MUX_IDX_FMT.format(muxIdx)));
                                long betweenRec = 500;
                                boolean zip = false;
                                String pathInArchive = null;
                                try {
                                    betweenRec = Long.parseLong(muxProps.getProperty(String.format("mux.%s.between-records", MUX_IDX_FMT.format(muxIdx)), "500"));
                                } catch (NumberFormatException nfe) {
                                    betweenRec = 500; // Default value
                                }
                                try {
                                    zip = "true".equals(muxProps.getProperty(String.format("mux.%s.zip", MUX_IDX_FMT.format(muxIdx)), "false"));
                                } catch (NumberFormatException nfe) {
                                    zip = false; // Default value
                                }
                                try {
                                    pathInArchive = muxProps.getProperty(String.format("mux.%s.path.in.zip", MUX_IDX_FMT.format(muxIdx)));
                                } catch (NumberFormatException nfe) {
                                    pathInArchive = null; // Default value
                                }
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                System.out.printf("From props: %s=%s%n",
//                                        String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx)),
//                                        muxProps.getProperty(String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx))));
                                boolean loop = "true".equals(muxProps.getProperty(String.format("mux.%s.loop", MUX_IDX_FMT.format(muxIdx)), "true").trim());
                                DataFileClient fileClient = new DataFileClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        fileClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                fileClient.initClient();
								fileClient.setLoop(loop);
                                fileClient.setReader(new DataFileReader("MUX-FileReader", fileClient.getListeners(), filename, betweenRec, loop, zip, pathInArchive));
                                fileClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                fileClient.setZip(zip);
                                fileClient.setPathInArchive(pathInArchive);
                                nmeaDataClients.add(fileClient);
//                                System.out.printf(">>> Loop: %b", fileClient.isLoop()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "ws": // Consumer
                            try {
                                String wsUri = muxProps.getProperty(String.format("mux.%s.wsuri", MUX_IDX_FMT.format(muxIdx)));
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                WebSocketClient wsClient = new WebSocketClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        wsClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                wsClient.initClient();
                                wsClient.setReader(new WebSocketReader("MUX-WSReader", wsClient.getListeners(), wsUri));
                                wsClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(wsClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "rnd":  // Consumer. Random generator, for debugging
                            try {
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                RandomClient rndClient = new RandomClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        rndClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                rndClient.initClient();
                                rndClient.setReader(new RandomReader("MUX-RndReader", rndClient.getListeners()));
                                rndClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(rndClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "zda": // Consumer. ZDA generator
                            try {
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                ZDAClient zdaClient = new ZDAClient(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        zdaClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                zdaClient.initClient();
                                zdaClient.setReader(new ZDAReader("MUX-ZDAReader", zdaClient.getListeners()));
                                zdaClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(zdaClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error err) {
                                err.printStackTrace();
                            }
                            break;
                        case "udp":    // User Defined Protocol
                            try {
                                String udpPort = muxProps.getProperty(String.format("mux.%s.port", MUX_IDX_FMT.format(muxIdx)));
                                String udpServer = muxProps.getProperty(String.format("mux.%s.server", MUX_IDX_FMT.format(muxIdx)));
                                String udpServerTimeout = muxProps.getProperty(String.format("mux.%s.timeout", MUX_IDX_FMT.format(muxIdx)));
//                                deviceFilters = muxProps.getProperty(String.format("mux.%s.device.filters", MUX_IDX_FMT.format(muxIdx)), "");
//                                sentenceFilters = muxProps.getProperty(String.format("mux.%s.sentence.filters", MUX_IDX_FMT.format(muxIdx)), "");
                                // String initialRequest = muxProps.getProperty(String.format("mux.%s.initial.request", MUX_IDX_FMT.format(muxIdx)), "");
                                // boolean keepTrying = "true".equals(muxProps.getProperty(String.format("mux.%s.keep.trying", MUX_IDX_FMT.format(muxIdx)), "false"));
                                nmea.consumers.client.UDPServer udpClient = new nmea.consumers.client.UDPServer(
                                        !deviceFilters.trim().isEmpty() ? deviceFilters.split(",") : null,
                                        !sentenceFilters.trim().isEmpty() ? sentenceFilters.split(",") : null,
                                        mux);
                                String propProp = String.format("mux.%s.properties", MUX_IDX_FMT.format(muxIdx));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        Properties readerProperties = new Properties();
                                        readerProperties.load(new FileReader(propFileName));
                                        udpClient.setProperties(readerProperties);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                udpClient.initClient();
                                final UDPReader udpReader = new UDPReader("MUX-UDPReader", udpClient.getListeners(), udpServer, Integer.parseInt(udpPort));
                                if (udpServerTimeout != null) {
                                    udpReader.setTimeout(Long.parseLong(udpServerTimeout));
                                }
                                udpClient.setReader(udpReader);
                                udpClient.setVerbose("true".equals(muxProps.getProperty(String.format("mux.%s.verbose", MUX_IDX_FMT.format(muxIdx)), "false")));
                                nmeaDataClients.add(udpClient);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "batt":   // Consumer. Battery Voltage, use XDR
                        default:
                            throw new RuntimeException(String.format("mux type [%s] not supported yet.", type));
                    }
                }
            }
            muxIdx++;
        }
        if (verbose) {
            System.out.printf("\t>> %s - Done with input channels\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
        }

        // Data Cache
        if ("true".equals(muxProps.getProperty("init.cache", "false"))) {
            try {
                if (verbose) {
                    System.out.printf("\t>> %s - Initializing Cache\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
                }
                String deviationFile = muxProps.getProperty("deviation.file.name", "zero-deviation.csv");
                double maxLeeway = Double.parseDouble(muxProps.getProperty("max.leeway", "0"));
                double bspFactor = Double.parseDouble(muxProps.getProperty("bsp.factor", "1"));
                double awsFactor = Double.parseDouble(muxProps.getProperty("aws.factor", "1"));
                double awaOffset = Double.parseDouble(muxProps.getProperty("awa.offset", "0"));
                double hdgOffset = Double.parseDouble(muxProps.getProperty("hdg.offset", "0"));
                double defaultDeclination = Double.parseDouble(muxProps.getProperty("default.declination", "0"));
                int damping = Integer.parseInt(muxProps.getProperty("damping", "1"));
                String markerFile = muxProps.getProperty("markers"); // default null
                List<String[]> markerList = new ArrayList<>();
                if (markerFile != null) {
                    markerList.add(new String[] { markerFile, "no-description" });
                }
                int listIdx = 1;
                while (true) {
                    String makerFileFromList = muxProps.getProperty(String.format("markers.list.%02d", listIdx));
                    if (makerFileFromList != null) {
                        markerList.add(new String[] { makerFileFromList, "no-description-yet" });
                        listIdx++;
                    } else {
                        break;
                    }
                }
                if (false) {
                    String markerFileList = muxProps.getProperty("markers.list"); // default null, or [{markers=mux-configs/bretagne.bumper.markers.yaml}, {markers=mux-configs/san.juan.markers.yaml}]
                    if (markerFileList != null) {
                        markerFileList = markerFileList.trim();
                        String[] fileList = markerFileList.substring(1, markerFileList.length() - 1).split(","); // Trim [ and ], and split
                        Arrays.stream(fileList).forEach(f -> {
                            String fName = f.trim();
                            String newFile = fName.substring(1, fName.length() - 1).substring("markers=".length());
                            markerList.add(new String[] { newFile, "dummy" });
                            System.out.println(newFile);
                        });
                    }
                }
                String nextWayPoint = muxProps.getProperty("next-waypoint");
                ApplicationContext.getInstance().initCache(deviationFile, maxLeeway, bspFactor, awsFactor, awaOffset, hdgOffset, defaultDeclination, damping, markerList, nextWayPoint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        thereIsMore = true;
        int fwdIdx = 1;
        // 2 - Output channels, aka forwarders
        while (thereIsMore) {
            String classProp = String.format("forward.%s.class", MUX_IDX_FMT.format(fwdIdx));
            String clss = muxProps.getProperty(classProp);
            if (clss != null) { // Dynamic loading
                if (verbose) {
                    System.out.printf("\t>> %s - Dynamic loading for output %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                }
                try {
                    Object dynamic = Class.forName(clss).getDeclaredConstructor().newInstance();
                    if (dynamic instanceof Forwarder) {
                        Forwarder forwarder = (Forwarder) dynamic;
                        String propProp = String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx));
                        String propFileName = muxProps.getProperty(propProp);
                        if (propFileName != null) {
                            try {
                                Properties properties = new Properties();
                                properties.load(new FileReader(propFileName));
                                forwarder.setProperties(properties);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        forwarder.init();
                        nmeaDataForwarders.add(forwarder);
                    } else {
                        throw new RuntimeException(String.format("Expected a Forwarder, found a [%s]", dynamic.getClass().getName()));
                    }
                } catch (Exception ioe) {
                    // Some I2C device was not found?
                    System.err.println("---------------------------");
                    ioe.printStackTrace();
                    System.err.println("---------------------------");
                } catch (Throwable ex) {
                    System.err.println("===========================");
                    System.err.println("Classpath: " + System.getProperty("java.class.path"));
                    ex.printStackTrace();
                    System.err.println("===========================");
                }
            } else {
                String typeProp = String.format("forward.%s.type", MUX_IDX_FMT.format(fwdIdx));
                String type = muxProps.getProperty(typeProp);
                if (type == null) {
                    thereIsMore = false;
                } else {
                    if (verbose) {
                        System.out.printf("\t>> %s - Loading for output channel %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                    }
                    switch (type) {
                        case "serial": // Forwarder
                            String serialPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            int baudrate = Integer.parseInt(muxProps.getProperty(String.format("forward.%s.baudrate", MUX_IDX_FMT.format(fwdIdx))));
                            String propFileSerial = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String serialSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String serialVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder serialForwarder;
                                if (serialSubClass == null) {
                                    serialForwarder = new SerialWriter(serialPort, baudrate);
                                } else {
                                    serialForwarder = (SerialWriter) Class.forName(serialSubClass.trim()).getConstructor(String.class, Integer.class).newInstance(serialPort, baudrate);
                                }
                                if (propFileSerial != null || serialVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (propFileSerial != null) {
                                        forwarderProps.load(new FileReader(propFileSerial));
                                    }
                                    if (serialVerbose != null) {
                                        forwarderProps.setProperty("verbose", serialVerbose.trim());
                                    }
                                    serialForwarder.setProperties(forwarderProps);
                                }
                                serialForwarder.init();
                                nmeaDataForwarders.add(serialForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "tcp": // Forwarder
                            String tcpPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String tcpPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String tcpSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String tcpVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder tcpForwarder;
                                if (tcpSubClass == null) {
                                    tcpForwarder = new TCPServer(Integer.parseInt(tcpPort));
                                } else {
                                    tcpForwarder = (TCPServer) Class.forName(tcpSubClass.trim()).getConstructor(Integer.class).newInstance(Integer.parseInt(tcpPort));
                                }
                                if (tcpPropFile != null || tcpVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (tcpPropFile != null) {
                                        forwarderProps.load(new FileReader(tcpPropFile));
                                    }
                                    if (tcpVerbose != null) {
                                        forwarderProps.setProperty("verbose", tcpVerbose.trim());
                                    }
                                    tcpForwarder.setProperties(forwarderProps);
                                }
                                tcpForwarder.init();
                                nmeaDataForwarders.add(tcpForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "udp": // Forwarder
                            String udpPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String udpPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String udpSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String udpVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder udpForwarder;
                                if (udpSubClass == null) {
                                    // udpForwarder = new UDPServer(Integer.parseInt(udpPort));
                                    udpForwarder = new UDPClient(Integer.parseInt(udpPort));  // This is the way OpenCPN likes it.
                                } else {
                                    udpForwarder = (nmea.forwarders.UDPServer) Class.forName(udpSubClass.trim()).getConstructor(Integer.class).newInstance(Integer.parseInt(udpPort));
                                }
                                Properties forwarderProps = new Properties();
                                if (udpPropFile != null) {
                                    forwarderProps.load(new FileReader(udpPropFile));
                                }
                                if (udpVerbose != null) {
                                    forwarderProps.setProperty("verbose", udpVerbose);
                                }
                                if (!forwarderProps.isEmpty()) {
                                    udpForwarder.setProperties(forwarderProps);
                                }
                                udpForwarder.init();
                                nmeaDataForwarders.add(udpForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "rest": // Forwarder
                            String restPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String restSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String verboseStr = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));

                            List<String> properties = Arrays.asList(
                                    "server.name", "server.port", "rest.resource", "rest.verb", "http.headers", "rest.protocol"
                            );
                            final int idx = fwdIdx;
                            Properties configProps = new Properties();
                            if (verboseStr != null) {
                                System.out.printf("Setting verbose to %s (%s)\n", verboseStr, verboseStr.trim());
                                configProps.put("verbose", verboseStr.trim());
                            }
                            properties.forEach(prop -> {
                                String propVal = muxProps.getProperty(String.format("forward.%s.%s", MUX_IDX_FMT.format(idx), prop));
                                if (propVal != null) {
                                    configProps.put(prop, propVal);
                                }
                            });
//							props.put("server.name", "192.168.42.6");
//							props.put("server.port", "8080");
//							props.put("rest.resource", "/rest/endpoint?qs=prm");
//							props.put("rest.verb", "POST");
//							props.put("rest.protocol", "http[s]");
//							props.put("http.headers", "Content-Type:plain/text");
                            try {
                                Forwarder restForwarder;
                                if (restSubClass == null) {
                                    restForwarder = new RESTPublisher();
                                } else {
                                    restForwarder = (RESTPublisher) Class.forName(restSubClass.trim()).getConstructor().newInstance();
                                }
                                if (restPropFile != null) {
                                    Properties forwarderProps = new Properties();
                                    forwarderProps.load(new FileReader(restPropFile));
                                    restForwarder.setProperties(forwarderProps);
                                }
                                if ("true".equals(System.getProperty("mux.props.verbose"))) {
                                    System.out.printf("Props for forwarder %s\n", restForwarder.getClass().getName());
                                    configProps.forEach((name, value) -> System.out.printf("%s : %s\n", name, value));
                                }
                                restForwarder.setProperties(configProps);
                                restForwarder.init();
                                nmeaDataForwarders.add(restForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "gpsd": // Forwarder
                            String gpsdPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String gpsdPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String gpsdSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String gpsdVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder gpsdForwarder;
                                if (gpsdSubClass == null) {
                                    gpsdForwarder = new GPSdServer(Integer.parseInt(gpsdPort));
                                } else {
                                    gpsdForwarder = (GPSdServer) Class.forName(gpsdSubClass.trim()).getConstructor(Integer.class).newInstance(Integer.parseInt(gpsdPort));
                                }
                                if (gpsdPropFile != null || gpsdVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (gpsdPropFile != null) {
                                        forwarderProps.load(new FileReader(gpsdPropFile));
                                    }
                                    if (gpsdVerbose != null) {
                                        forwarderProps.setProperty("verbose", gpsdVerbose.trim());
                                    }
                                    gpsdForwarder.setProperties(forwarderProps);
                                }
                                gpsdForwarder.init();
                                nmeaDataForwarders.add(gpsdForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "file": // Forwarder
                            String fName = muxProps.getProperty(String.format("forward.%s.filename", MUX_IDX_FMT.format(fwdIdx)), "data.nmea");
                            boolean append = "true".equals(muxProps.getProperty(String.format("forward.%s.append", MUX_IDX_FMT.format(fwdIdx)), "false"));
                            boolean timeBased = "true".equals(muxProps.getProperty(String.format("forward.%s.timebase.filename", MUX_IDX_FMT.format(fwdIdx)), "false"));
                            String propFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String fSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String radix = muxProps.getProperty(String.format("forward.%s.filename.suffix", MUX_IDX_FMT.format(fwdIdx)));
                            String logDir = muxProps.getProperty(String.format("forward.%s.log.dir", MUX_IDX_FMT.format(fwdIdx)));
                            String split = muxProps.getProperty(String.format("forward.%s.split", MUX_IDX_FMT.format(fwdIdx)));
                            String flush = muxProps.getProperty(String.format("forward.%s.flush", MUX_IDX_FMT.format(fwdIdx)));
                            String fileVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            String sentenceFilters = muxProps.getProperty(String.format("forward.%s.sentence.filters", MUX_IDX_FMT.format(fwdIdx)), null); // TODO Make it for other forwarders too ?
                            if (verbose && sentenceFilters != null) {
                                spitOutSentenceFilters(sentenceFilters);
                            }
                            try {
                                Forwarder fileForwarder;
                                if (fSubClass == null) {
                                    fileForwarder = new DataFileWriter(fName, append, timeBased, radix, logDir, split, "true".equals(flush), sentenceFilters);
                                } else {
                                    try {
                                        fileForwarder = (DataFileWriter) Class.forName(fSubClass.trim())
                                                .getConstructor(String.class, Boolean.class, Boolean.class, String.class, String.class, String.class, Boolean.class, String.class)
                                                .newInstance(fName, append, timeBased, radix, logDir, split, "true".equals(flush), sentenceFilters);
                                    } catch (NoSuchMethodException nsme) {
                                        fileForwarder = (DataFileWriter) Class.forName(fSubClass.trim()) // Fallback on previous constructor
                                                .getConstructor(String.class, Boolean.class)
                                                .newInstance(fName, append);
                                    }
                                }
                                if (propFile != null || fileVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (propFile != null) {
                                        forwarderProps.load(new FileReader(propFile));
                                    }
                                    if (fileVerbose != null) {
                                        forwarderProps.setProperty("verbose", fileVerbose.trim());
                                    }
                                    fileForwarder.setProperties(forwarderProps);
                                }
                                fileForwarder.init();
                                nmeaDataForwarders.add(fileForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "ws": // Forwarder
                            String wsUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
                            String wsPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String wsSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String wsVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder wsForwarder;
                                if (wsSubClass == null) {
                                    wsForwarder = new WebSocketWriter(wsUri);
                                } else {
                                    wsForwarder = (WebSocketWriter) Class.forName(wsSubClass.trim()).getConstructor(String.class).newInstance(wsUri);
                                }
                                if (wsPropFile != null || wsVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (wsPropFile != null) {
                                        forwarderProps.load(new FileReader(wsPropFile));
                                    }
                                    if (wsVerbose != null) {
                                        forwarderProps.setProperty("verbose", wsVerbose.trim());
                                    }
                                    wsForwarder.setProperties(forwarderProps);
                                }
                                wsForwarder.init();
                                nmeaDataForwarders.add(wsForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "wsp": // Forwarder
                            String wspUri = muxProps.getProperty(String.format("forward.%s.wsuri", MUX_IDX_FMT.format(fwdIdx)));
                            String wspPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String wspSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            String wspVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder wspForwarder;
                                if (wspSubClass == null) {
                                    wspForwarder = new WebSocketProcessor(wspUri);
                                } else {
                                    wspForwarder = (WebSocketProcessor) Class.forName(wspSubClass.trim()).getConstructor(String.class).newInstance(wspUri);
                                }
                                if (wspPropFile != null || wspVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (wspPropFile != null) {
                                        forwarderProps.load(new FileReader(wspPropFile));
                                    }
                                    if (wspVerbose != null) {
                                        forwarderProps.setProperty("verbose", wspVerbose.trim());
                                    }
                                    wspForwarder.setProperties(forwarderProps);
                                }
                                wspForwarder.init();
                                nmeaDataForwarders.add(wspForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "console": // Forwarder
                            try {
                                String consolePropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                                String consoleSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                                String consoleVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                                Forwarder consoleForwarder = new ConsoleWriter();
                                if (consoleSubClass == null) {
                                    consoleForwarder = new ConsoleWriter();
                                } else {
                                    consoleForwarder = (ConsoleWriter) Class.forName(consoleSubClass.trim()).getConstructor().newInstance();
                                }
                                if (consolePropFile != null || consoleVerbose  != null) {
                                    Properties forwarderProps = new Properties();
                                    if (consolePropFile != null) {
                                        forwarderProps.load(new FileReader(consolePropFile));
                                    }
                                    if (consoleVerbose != null) {
                                        forwarderProps.setProperty("verbose", consoleVerbose.trim());
                                    }
                                    consoleForwarder.setProperties(forwarderProps);
                                }
                                consoleForwarder.init();
                                nmeaDataForwarders.add(consoleForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "rmi": // Forwarder
                            String rmiPort = muxProps.getProperty(String.format("forward.%s.port", MUX_IDX_FMT.format(fwdIdx)));
                            String rmiName = muxProps.getProperty(String.format("forward.%s.name", MUX_IDX_FMT.format(fwdIdx)));
                            String rmiPropFile = muxProps.getProperty(String.format("forward.%s.properties", MUX_IDX_FMT.format(fwdIdx)));
                            String subClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx))); // TODO Manage that one...
                            String rmiVerbose = muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)));
                            try {
                                Forwarder rmiServerForwarder;
                                if (rmiName != null && !rmiName.trim().isEmpty()) {
                                    rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort), rmiName);
                                } else {
                                    rmiServerForwarder = new RMIServer(Integer.parseInt(rmiPort));
                                }
                                if (rmiPropFile != null || rmiVerbose != null) {
                                    Properties forwarderProps = new Properties();
                                    if (rmiPropFile != null) {
                                        forwarderProps.load(new FileReader(rmiPropFile));
                                    }
                                    if (rmiVerbose != null) {
                                        forwarderProps.setProperty("verbose", rmiVerbose.trim());
                                    }
                                    rmiServerForwarder.setProperties(forwarderProps);
                                }
                                rmiServerForwarder.init();
                                nmeaDataForwarders.add(rmiServerForwarder);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case "nmea-cache-publisher": // Forwarder
                            String cacheSubClass = muxProps.getProperty(String.format("forward.%s.subclass", MUX_IDX_FMT.format(fwdIdx)));
                            Integer restPort = null;
                            Long betweenLoops = null;
                            boolean fwdVerbose = "true".equals(muxProps.getProperty(String.format("forward.%s.verbose", MUX_IDX_FMT.format(fwdIdx)), "false"));
                            String strPort = muxProps.getProperty(String.format("forward.%s.rest.port", MUX_IDX_FMT.format(fwdIdx)));
                            String strBetweenLoops = muxProps.getProperty(String.format("forward.%s.between-loops", MUX_IDX_FMT.format(fwdIdx)));
                            String protocol = muxProps.getProperty(String.format("forward.%s.rest.protocol", MUX_IDX_FMT.format(fwdIdx)));
                            String machine = muxProps.getProperty(String.format("forward.%s.rest.machine-name", MUX_IDX_FMT.format(fwdIdx)));
                            String resource = muxProps.getProperty(String.format("forward.%s.rest.resource", MUX_IDX_FMT.format(fwdIdx)));
                            String verb = muxProps.getProperty(String.format("forward.%s.rest.verb", MUX_IDX_FMT.format(fwdIdx)));
                            String qs = muxProps.getProperty(String.format("forward.%s.rest.query.string", MUX_IDX_FMT.format(fwdIdx)));
                            String closeResource = muxProps.getProperty(String.format("forward.%s.rest.onclose.resource", MUX_IDX_FMT.format(fwdIdx)));
                            String closeVerb = muxProps.getProperty(String.format("forward.%s.rest.onclose.verb", MUX_IDX_FMT.format(fwdIdx)));
                            // TODO, properties ?
                            try {
                                if (strPort != null) {
                                    restPort = Integer.parseInt(strPort);
                                }
                                if (strBetweenLoops != null) {
                                    betweenLoops = Long.parseLong(strBetweenLoops);
                                }
                                // Validate values (protocol, verb, ...)
                                if (protocol != null && !protocol.equals("http") && !protocol.equals("https")) {
                                    protocol = null;
                                    System.err.printf("Protocol [%s] not supported. Only http and https can be used. Keeping default.\n", protocol);
                                }
                                if (verb != null && !verb.equals("PUT") && !verb.equals("POST")) {
                                    verb = null;
                                    System.err.printf("Verb [%s] not supported. Only PUT and POST can be used. Keeping default.\n", verb);
                                }
                                Forwarder cachePublisher = null;
                                if (cacheSubClass == null) {
                                    if (closeResource != null) {
                                        cachePublisher = new NMEACachePublisher(betweenLoops, verb, protocol, machine, restPort, resource, qs, fwdVerbose, closeResource, closeVerb);
                                    } else {
                                        cachePublisher = new NMEACachePublisher(betweenLoops, verb, protocol, machine, restPort, resource, qs, fwdVerbose);
                                    }
                                } else {
                                    // TODO Manage this subclass case
                                    System.err.println("Subclass case not managed yet...");
                                }

                                cachePublisher.init();
                                nmeaDataForwarders.add(cachePublisher);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;
                        default:
                            throw new RuntimeException(String.format("forward type [%s] not supported yet.", type));
                    }
                }
            }
            fwdIdx++;
        }
        if (verbose) {
            System.out.printf("\t>> %s - Done with forwarders\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
        }
        // Init cache (for Computers et al).
        if ("true".equals(muxProps.getProperty("init.cache", "false"))) {
            try {
                // If there is a cache, then let's see what computers to start.
                thereIsMore = true;
                int cptrIdx = 1;
                // 3 - Computers
                while (thereIsMore) {
                    String classProp = String.format("computer.%s.class", MUX_IDX_FMT.format(cptrIdx));
                    String clss = muxProps.getProperty(classProp);
                    if (clss != null) { // Dynamic loading
                        if (verbose) {
                            System.out.printf("\t>> %s - Dynamic loading for computer %s\n", NumberFormat.getInstance().format(System.currentTimeMillis()), classProp);
                        }
                        try {
                            Object dynamic = Class.forName(clss).getDeclaredConstructor(Multiplexer.class).newInstance(mux);
                            if (dynamic instanceof Computer) {
                                Computer computer = (Computer) dynamic;
                                String propProp = String.format("computer.%s.properties", MUX_IDX_FMT.format(cptrIdx));
                                Properties properties = new Properties();
                                properties.put("verbose", String.valueOf("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx))))));
                                String propFileName = muxProps.getProperty(propProp);
                                if (propFileName != null) {
                                    try {
                                        properties.load(new FileReader(propFileName));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                computer.setProperties(properties);
                                nmeaDataComputers.add(computer);
                            } else {
                                throw new RuntimeException(String.format("Expected a Computer, found a [%s]", dynamic.getClass().getName()));
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        String typeProp = String.format("computer.%s.type", MUX_IDX_FMT.format(cptrIdx));
                        String type = muxProps.getProperty(typeProp);
                        if (type == null) {
                            thereIsMore = false;
                        } else {
                            if (verbose) {
                                System.out.printf("\t>> %s - Loading computer %s (%s)\n", NumberFormat.getInstance().format(System.currentTimeMillis()), typeProp, type);
                            }
                            switch (type) {
                                case "tw-current": // True Wind and Current computer. True Wind is calculated with GPS COG & SOG), as it should. Also involves the LongTimeCurrentCalculator.
                                    String prefix = muxProps.getProperty(String.format("computer.%s.prefix", MUX_IDX_FMT.format(cptrIdx)), "OS");
                                    String[] timeBuffers = muxProps.getProperty(String.format("computer.%s.time.buffer.length", MUX_IDX_FMT.format(cptrIdx)), "600000").split(",");
                                    List<Long> timeBufferLengths = Arrays.stream(timeBuffers).map(tbl -> Long.parseLong(tbl.trim())).collect(Collectors.toList());
                                    // Check duplicates
                                    for (int i = 0; i < timeBufferLengths.size() - 1; i++) {
                                        for (int j = i + 1; j < timeBufferLengths.size(); j++) {
                                            if (timeBufferLengths.get(i).equals(timeBufferLengths.get(j))) {
                                                throw new RuntimeException(String.format("Duplicates in time buffer lengths: %d ms.", timeBufferLengths.get(i)));
                                            }
                                        }
                                    }
                                    try {
                                        Computer twCurrentComputer = new ExtraDataComputer(mux, prefix, timeBufferLengths.toArray(new Long[timeBufferLengths.size()]));
                                        twCurrentComputer.setVerbose("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx)))));
                                        nmeaDataComputers.add(twCurrentComputer);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                case "dew-point-computer": // Computer
                                    String dpPrefix = muxProps.getProperty(String.format("computer.%s.prefix", MUX_IDX_FMT.format(cptrIdx)), "OS");
                                    try {
                                        Computer dewPointComputer = new DewPointTemperatureComputer(mux, dpPrefix);
                                        dewPointComputer.setVerbose("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx)))));
                                        nmeaDataComputers.add(dewPointComputer);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                case "long-term-storage":
                                    // properties: "ping-interval", "max-length", "data-path", "object-name"
                                    Long pingInterval = null;
                                    Long maxLength = null;
                                    String[] dataPath = null;
                                    String objectName = null;
                                    try {
                                        String propValue = muxProps.getProperty(String.format("computer.%s.ping-interval", MUX_IDX_FMT.format(cptrIdx)));
                                        if (propValue != null) {
                                            pingInterval = Long.parseLong(propValue);
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("ping-interval property:");
                                        ex.printStackTrace();
                                    }
                                    try {
                                        String propValue = muxProps.getProperty(String.format("computer.%s.max-length", MUX_IDX_FMT.format(cptrIdx)));
                                        if (propValue != null) {
                                            maxLength = Long.parseLong(propValue);
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("max-length property:");
                                        ex.printStackTrace();
                                    }
                                    try {
                                        String propValue = muxProps.getProperty(String.format("computer.%s.data-path", MUX_IDX_FMT.format(cptrIdx)));
                                        if (propValue != null) {
                                            dataPath = propValue.split(",");
                                            // Check that one below
//                                            dataPath = (String[])Arrays.asList(dataPath).stream()
//                                                                       .map(String::trim)
//                                                                       // .map(Object::toString)
//                                                                       .toArray();
                                            for (int i=0; i<dataPath.length; i++) {
                                                dataPath[i] = dataPath[i].trim();
                                            }
                                            if (verbose) {
                                                System.out.println("-- Data Path: --");
                                                Arrays.asList(dataPath).stream()
                                                        .forEach(el -> System.out.printf("[%s]\n", el));
                                                System.out.println("----------------");
                                            }
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("data-path property:");
                                        ex.printStackTrace();
                                    }
                                    try {
                                        String propValue = muxProps.getProperty(String.format("computer.%s.object-name", MUX_IDX_FMT.format(cptrIdx)));
                                        if (propValue != null) {
                                            objectName = propValue;
                                        }
                                    } catch (Exception ex) {
                                        System.err.println("object-name property:");
                                        ex.printStackTrace();
                                    }
                                    try {
                                        Computer longTermStorage = new LongTermStorage(mux, pingInterval, maxLength, dataPath, objectName);
                                        longTermStorage.setVerbose("true".equals(muxProps.getProperty(String.format("computer.%s.verbose", MUX_IDX_FMT.format(cptrIdx)))));
                                        nmeaDataComputers.add(longTermStorage);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    break;
                                default:
                                    System.err.printf("Computer type [%s] not supported.\n", type);
                                    break;
                            }
                        }
                    }
                    cptrIdx++;
                }
                if (verbose) {
                    System.out.printf("\t>> %s - Done with computers\n", NumberFormat.getInstance().format(System.currentTimeMillis()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Properties yamlToProperties(Map<String, Object> yamlMap) {
        Properties properties = new Properties();

        yamlMap.keySet().forEach(k -> {
//		System.out.printf("%s -> %s", k, yamlMap.get(k).getClass().getName()));
            switch (k) {
                case "name":
                    // System.out.printf("Definition Name: %s\n", yamlMap.get(k));
                    properties.setProperty(k, (String)yamlMap.get(k));
                    break;
                case "description":
                    // System.out.println("-- Description --");
                    // ((List<String>)yamlMap.get(k)).forEach(System.out::println);
                    AtomicInteger nbDesc = new AtomicInteger(0);
                    ((List<String>)yamlMap.get(k)).forEach(line -> {
                        int nb = nbDesc.incrementAndGet();
                        String propName = String.format("description.%02d", nb);
                        properties.setProperty(propName, line);
                    });
                    // System.out.println("-----------------");
                    break;
                case "context":
                    Map<String, Object> context = (Map<String, Object>) yamlMap.get(k);
                    context.keySet().forEach(ck -> {
                        Object thatOne = context.get(ck);
                        if ("markers.list".equals(ck)) {
                            List<?> thatList = (List<?>)thatOne;
                            AtomicInteger idx = new AtomicInteger(0);
                            thatList.forEach(item -> {
                                idx.incrementAndGet();
                                properties.setProperty(String.format("%s.%02d", ck, idx.get()), ((Map<String, String>)item).get("markers"));
                            });
                        } else {
                            properties.setProperty(ck, thatOne.toString());
                        }
                    });
                    System.out.println(context);
                    break;
                case "channels":
                    List<Map<String, Object>> channels = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbChannels = new AtomicInteger(0);
                    channels.forEach(channel -> {
                        int nb = nbChannels.incrementAndGet();
                        channel.keySet().forEach(channelKey -> {
                            String propName = String.format("mux.%02d.%s", nb, channelKey);
                            properties.setProperty(propName, channel.get(channelKey) == null ? null : channel.get(channelKey).toString());
                            if ("yes".equals(System.getProperty("yaml.tx.verbose", "no"))) {
                                System.out.printf("Setting [%s] to [%s]\n", propName, channel.get(channelKey).toString());
                            }
                        });
                    });
                    break;
                case "forwarders":
                    List<Map<String, Object>> forwarders = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbForwarders = new AtomicInteger(0);
                    forwarders.forEach(channel -> {
                        int nb = nbForwarders.incrementAndGet();
                        channel.keySet().forEach(forwardKey -> {
                            String propName = String.format("forward.%02d.%s", nb, forwardKey);
                            properties.setProperty(propName, channel.get(forwardKey).toString());
                        });
                    });
                    break;
                case "computers":
                    List<Map<String, Object>> computers = (List<Map<String, Object>>) yamlMap.get(k);
                    AtomicInteger nbComputers = new AtomicInteger(0);
                    computers.forEach(channel -> {
                        int nb = nbComputers.incrementAndGet();
                        channel.keySet().forEach(computeKey -> {
                            String propName = String.format("computer.%02d.%s", nb, computeKey);
                            properties.setProperty(propName, channel.get(computeKey).toString());
                        });
                    });
                    break;
                default:
                    break;
            }
        });

        // For tests
        if ("yes".equals(System.getProperty("yaml.tx.verbose", "no"))) {
            try {
                properties.store(new FileOutputStream("multiplexer.properties"), "Generated from yaml");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return properties;
    }
}

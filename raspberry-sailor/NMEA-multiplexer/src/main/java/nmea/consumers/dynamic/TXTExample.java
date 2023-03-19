package nmea.consumers.dynamic;

import nmea.api.*;
import nmea.parser.StringParsers;
import utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This is an example showing how to implement a custom/dynamic Consumer (aka Channel)
 * Use it from a yaml like
 *
 * name: "Dynamic Consumer"
 * . . .
 * channels:
 *   - class: nmea.consumers.dynamic.TXTExample
 *     # reader: nmea.consumers.dynamic.TXTExample.TXTReader
 *     properties: blah.properties
 *     verbose: false
 *     device.filters: . . .
 *     sentence.filters: . . .
 * forwarders:
 *   . . .
 */
public class TXTExample extends NMEAClient {

    private static final SimpleDateFormat SDF_DATETIME = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS 'UTC'");
    static {
        SDF_DATETIME.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
    }

    private static TXTExample nmeaClient = null;

    public TXTExample() {
        this(null, null, null);
    }

    public TXTExample(Multiplexer mux) {
        this(null, null, mux);
    }

    public TXTExample(String[] s, String[] sa) {
        this(s, sa, null);
    }

    public TXTExample(String[] s, String[] sa, Multiplexer mux) {
        super(s, sa, mux);
        this.nmeaClient = this;
        // Here is a way to set the reader without the 'reader' property.
        this.setReader(new TXTExample.TXTReader("TXTProducer", this.getListeners()));
    }

    // Default values. Can be overridden by properties.
    private String talkerId = "XX";
    private String txtPrompt = "Hi there - time is";
    private long betweenLoops = 1_000L;

    @Override
    public void initClient() {
        super.initClient();

        if (this.props != null) {
            System.out.println("Found user-provided properties.");
            this.talkerId = this.props.getProperty("text-talker-id", this.talkerId);
            this.txtPrompt = this.props.getProperty("text-prefix", this.txtPrompt);
            ((TXTReader)this.getReader()).setDevicePrefix(this.talkerId);
            final String blProp = this.props.getProperty("between-loops", String.valueOf(this.betweenLoops));
            try {
                betweenLoops = Long.parseLong(blProp);
            } catch (NumberFormatException nfe) {
                System.err.printf("Unparsable %d\n", blProp);
                nfe.printStackTrace();
            }
        }
    }

    @Override
    public void dataDetectedEvent(NMEAEvent e) {
        if (verbose) {
            System.out.println("Generated from TXT Producer:" + e.getContent());
        }
        if (multiplexer != null) {
            multiplexer.onData(e.getContent());
        }
    }

    @Override
    public Object getBean() {
        return null;
    }

    /**
     * All the skill of the Client is in this method.
     *
     * @param prefix Talker ID. Sentence ID is hard-coded 'TXT'
     * @param epoch Current time.
     * @return A valid NMEA Sentence
     */
    String produceTextSentence(String prefix, long epoch) {
        String txt = prefix + "TXT,";
        Date utc = new Date(epoch);
        String strUTC = SDF_DATETIME.format(utc);
        txt += String.format("%s %s", this.txtPrompt, strUTC);
        // Checksum
        int cs = StringParsers.calculateCheckSum(txt);
        txt += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
        return "$" + txt;
    }

    public static class TXTReader extends NMEAReader {

        private static final String DEFAULT_DEVICE_PREFIX = "XX";
        private String devicePrefix = DEFAULT_DEVICE_PREFIX;

        public TXTReader(String threadName, List<NMEAListener> al) {
            super(threadName, al);
        }
        public TXTReader(List<NMEAListener> al) {
            super(al);
        }

        public String getDevicePrefix() {
            return this.devicePrefix;
        }

        public void setDevicePrefix(String devicePrefix) {
            this.devicePrefix = devicePrefix;
        }

        @Override
        public void startReader() {
            super.enableReading();
            while (this.canRead()) {
                // Read data every 1 second
                try {
                    if (nmeaClient != null) {
                        // Generate NMEA String
                        String txtString = nmeaClient.produceTextSentence(this.devicePrefix, System.currentTimeMillis());
                        txtString += NMEAParser.NMEA_SENTENCE_SEPARATOR;
                        fireDataRead(new NMEAEvent(this, txtString));
                        if (false) {
                            System.out.printf("Produced [%s]\n", txtString.trim());
                        }
                    } else {
                        System.out.println("NMEA Client is null...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(nmeaClient.betweenLoops);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        @Override
        public void closeReader() throws Exception {
        }
    }

    /**
     * For tests
     * @param args unused.
     */
    public static void main(String... args) {
        System.out.println("TXT producer invoked with " + args.length + " Parameter(s).");
        for (String s : args) {
            System.out.println("TXTClient prm:" + s);
        }
        nmeaClient = new TXTExample();

        Runtime.getRuntime().addShutdownHook(new Thread("TXTClient shutdown hook") {
            public void run() {
                System.out.println("Shutting down nicely.");
                nmeaClient.stopDataRead();
            }
        });

        nmeaClient.initClient();
        nmeaClient.setReader(new TXTExample.TXTReader("TXTProducer", nmeaClient.getListeners()));
        nmeaClient.startWorking();
    }
}

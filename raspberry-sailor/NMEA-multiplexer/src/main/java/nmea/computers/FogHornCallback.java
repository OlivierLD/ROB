package nmea.computers;

import nmea.utils.SoundUtil;
import util.TextToSpeech;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This is just an example for AISManager (or others) collision callback.
 * See in nmea.computers.AISManager and ais.mgr.properties
 *
 * This one is just honking
 */
public class FogHornCallback implements Consumer<String> {
    private String soundFileName = "EN";
    private boolean foghornVerbose = false;
    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    private final static long POLLING_INTERVAL = 10; // in seconds. this can be a parameter (in the setProperties method)
    private long pollingInterval = POLLING_INTERVAL;
    private Set<String> threatList = new HashSet<>();
    public FogHornCallback() {
        System.out.println(">> BufferedCollisionCallback - creating new Instance!!");
        // Start a thread, polling the threats buffer
        Thread pollingThread = new Thread(() -> {
            while (true) {
                synchronized (threatList) {
                    int length = threatList.size();
                    if (length > 0) {

                        try {
                            URL soundURL = new File(soundFileName).toURI().toURL();
                            if (foghornVerbose) {
                                System.out.printf("FogHornCallback playing sound at [%s]\n", soundURL.toString());
                            }
                            SoundUtil.playSound(soundURL);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (VERBOSE || foghornVerbose) {
                            System.out.printf(">> Found %d threat(s) :\n", length);
                            threatList.stream().forEach(el -> {
                                System.out.printf("Threat with: %s\n", el);
                            });
                        }
                        threatList.clear();
                    } else {
                        if (VERBOSE) {
                            System.out.printf(">> Found NO %s threat.\n", soundFileName);
                        }
                    }
                }
                try {
                    Thread.sleep(pollingInterval * 1_000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        pollingThread.start();
    }
    @Override
    public void accept(String s) {
        if (VERBOSE) {
            System.out.printf("Accept >> Adding %s threat [%s] to %s\n", soundFileName, s, this.getClass().getName());
        }
        synchronized (threatList) {
            threatList.add(s);
        }
    }

    public void setProperties(String propFileName) {
        System.out.printf("Loading properties file %s\n", propFileName);
        Properties props = new Properties();
        try {
            FileInputStream fis = new FileInputStream(propFileName);
            props.load(fis);

            if (props.getProperty("sound.file") != null) {
                soundFileName = props.getProperty("sound.file");
            }

            if (props.getProperty("foghorn.verbose") != null) {
                foghornVerbose = "true".equals(props.getProperty("foghorn.verbose"));
            }

            if (props.getProperty("polling.interval") != null) {
                pollingInterval = Long.parseLong(props.getProperty("polling.interval"));
            }

        } catch (Exception e) {
            System.out.printf("%s file problem...\n", propFileName);
            throw new RuntimeException(String.format("File not found: %s", propFileName));
        }
    }
}
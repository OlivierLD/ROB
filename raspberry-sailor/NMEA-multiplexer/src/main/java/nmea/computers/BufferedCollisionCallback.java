package nmea.computers;

import util.TextToSpeech;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Just an example for AISManager (or others like BorderManager) collision callback.
 * A Singleton
 */
public class BufferedCollisionCallback implements Consumer<String> {

    private String collisionVocabulary = "collision";

    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    private final static long POLLING_INTERVAL = 10; // in seconds. this can be a parameter (in the setProperties method)
    private long pollingInterval = POLLING_INTERVAL;
    private Set<String> threatList = new HashSet<>();
    private static BufferedCollisionCallback instance = null;
    private BufferedCollisionCallback() {
        System.out.println(">> BufferedCollisionCallback - creating new Instance!!");
        // Start a thread, polling the threats buffer
        Thread pollingThread = new Thread(() -> {
            while (true) {
                synchronized (threatList) {
                    int length = threatList.size();
                    if (length > 0) {
                        String message = String.format("Warning ! %d %s threat%s !", length, collisionVocabulary, length > 1 ? "s" : "");
                        TextToSpeech.speak(message);
                        if (VERBOSE) {
                            System.out.printf(">> Found %d threat(s) :\n", length);
                            threatList.stream().forEach(el -> {
                                System.out.printf("Threat with: %s\n", el);
                            });
                        }
                        threatList.clear();
                    } else {
                        System.out.printf(">> Found NO %s threat.\n", collisionVocabulary);
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
    public static synchronized BufferedCollisionCallback getInstance() {
        if (instance == null) {
            instance = new BufferedCollisionCallback();
        }
        return instance;
    }
    @Override
    public void accept(String s) {
        System.out.printf("Accept >> Adding %s threat [%s] to %s\n", collisionVocabulary, s, this.getClass().getName());
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

            if (props.getProperty("collision.name") != null) {
                collisionVocabulary = props.getProperty("collision.name");
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

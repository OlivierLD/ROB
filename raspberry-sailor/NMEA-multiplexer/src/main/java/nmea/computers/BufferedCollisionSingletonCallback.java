package nmea.computers;

import util.TextToSpeech;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Just an example for AISManager (or others like BorderManager) collision callback.
 * >> A Singleton
 */
public class BufferedCollisionSingletonCallback implements Consumer<String> {
    private boolean active = true;
    private String collisionLanguage = "EN";
    private String collisionVocabulary = "collision";

    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    private final static long POLLING_INTERVAL = 10; // in seconds. this can be a parameter (in the setProperties method)
    private long pollingInterval = POLLING_INTERVAL;
    private Set<String> threatList = new HashSet<>();
    private static BufferedCollisionSingletonCallback instance = null;
    private BufferedCollisionSingletonCallback() {
        System.out.println(">> BufferedCollisionCallback - creating new Instance!!");
        // Start a thread, polling the threats buffer
        Thread pollingThread = new Thread(() -> {
            while (true) {
                synchronized (threatList) {
                    if (this.active) {
                        int length = threatList.size();
                        if (length > 0) {
                            String message = String.format("Warning ! %d %s threat%s !", length, collisionVocabulary, length > 1 ? "s" : "");
                            if ("FR".equals(collisionLanguage)) {
                                message = String.format("Attention ! %d danger%s de %s !", length, length > 1 ? "s" : "", collisionVocabulary);
                            }
                            TextToSpeech.speak(message, collisionLanguage);
                            if (VERBOSE) {
                                System.out.printf(">> Found %d threat(s) :\n", length);
                                threatList.stream().forEach(el -> {
                                    System.out.printf("Threat with: %s\n", el);
                                });
                            }
                            threatList.clear();
                        } else {
                            if (VERBOSE) {
                                System.out.printf(">> Found NO %s threat.\n", collisionVocabulary);
                            }
                        }
                    } else {
                        if (VERBOSE) {
                            System.out.printf("=> INFO: %s is not active.\n", this.getClass().getName());
                        }
                    }                }
                try {
                    Thread.sleep(pollingInterval * 1_000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        pollingThread.start();
    }

    /**
     * Assuming that the constructor is private.
     * THIS method is the way to access an existing instance, or create a new one.
     *
     * @return the instance, unique in the JVM.
     */
    public static synchronized BufferedCollisionSingletonCallback getInstance() {
        if (instance == null) {
            instance = new BufferedCollisionSingletonCallback();
        } else {
            System.out.printf("Reusing instance of %s\n", BufferedCollisionSingletonCallback.instance.getClass().getName());
        }
        return instance;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    @Override
    public void accept(String s) {
        if (VERBOSE) {
            System.out.printf("Accept >> Adding %s threat [%s] to %s\n", collisionVocabulary, s, this.getClass().getName());
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

            if (props.getProperty("collision.lang") != null) {
                collisionLanguage = props.getProperty("collision.lang");
            }

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
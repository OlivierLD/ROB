package nmea.computers;

import util.TextToSpeech;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Just an example for AISManager (or others like BorderManager) collision callback.
 * A Singleton
 */
public class BufferedCollisionCallback implements Consumer<String> {

    private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));
    private final static long POLLING_INTERVAL = 10; // in seconds
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
                        String message = String.format("Warning ! %d collision threat%s !", length, length > 1 ? "s" : "");
                        TextToSpeech.speak(message);
                        if (VERBOSE) {
                            System.out.printf(">> Found %d threat(s) :\n", length);
                            threatList.stream().forEach(el -> {
                                System.out.printf("Threat with: %s\n", el);
                            });
                        }
                        threatList.clear();
                    } else {
                        System.out.println("Found NO threat.");
                    }
                }
                try {
                    Thread.sleep(POLLING_INTERVAL * 1_000);
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
        // System.out.printf("Talk to me ! [%s]\n", s);
        synchronized (threatList) {
            threatList.add(s);
        }
    }
}

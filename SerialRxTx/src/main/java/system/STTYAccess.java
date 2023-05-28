package system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class STTYAccess {

    /*
     * After doing a
     *   stty -F|-f /dev/tty.usbserial-14210 raw 38400 cs8 clocal
     */
    public static void main(String[] args) {
        String file = System.getProperty("port-name", "/dev/tty.usbserial-14210");
        BufferedReader br = null;
        AtomicBoolean keepGoing = new AtomicBoolean(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            keepGoing.set(false);
        }));

        System.out.println("Start reading...");
        try {
            br = new BufferedReader(new FileReader(file));
            String line = "";
            while (keepGoing.get() && line != null) {
                line = br.readLine();
                if (line != null) {
                    System.out.printf("Read: [%s]\n", line);
                } else {
                    System.out.println("Done reading.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (br != null) {
            try {
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

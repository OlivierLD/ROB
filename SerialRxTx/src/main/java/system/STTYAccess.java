package system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class STTYAccess {

    /*
     * After doing a
     *   stty -f /dev/tty.usbserial-14210 raw 38400 cs8 clocal
     *   stty -F /dev/ttyUSB0 raw 38400 cs8 clocal
     *
     * This could be a workaround to a bug seen in LibRxTx with BR 38400, on Raspberry P.
     */
    public static void main(String... args) {
        String file = System.getProperty("port-name", "/dev/tty.usbserial-14210");
        File f = new File(file);
        if (!f.exists()) {
            System.out.printf("File %s was not found.\n", file);
            System.out.println("Exiting.");
            System.exit(1);
        }
        BufferedReader br = null;
        AtomicBoolean keepGoing = new AtomicBoolean(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            keepGoing.set(false);
            System.out.println("Stop requested.");
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

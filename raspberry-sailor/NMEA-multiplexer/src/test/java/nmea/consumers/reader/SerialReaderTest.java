package nmea.consumers.reader;

import nmea.api.NMEAListener;

import java.util.ArrayList;
import java.util.List;

public class SerialReaderTest {
    public static void main(String... args) {
        List<NMEAListener> al = new ArrayList<>();
        // Test the retry. Client is null, careful with that.
        SerialReader serialReader = new SerialReader(null,"SerialReader", al, "/dev/ttyUSB0", 4_800);
        serialReader.setVerbose(true);
        serialReader.startReader();
    }
}
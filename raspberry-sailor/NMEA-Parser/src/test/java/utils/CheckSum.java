package utils;

import nmea.parser.StringParsers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CheckSum {

    @Test
    public void testCheckSum() {
//        System.setProperty("nmea.parser.verbose", "true");
        String nmeaString = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
        String stringToTest = nmeaString.substring(1, nmeaString.indexOf('*'));
        int cs = StringParsers.calculateCheckSum(stringToTest);
        System.out.printf("CS is 0x%02X\n", cs);
        assertEquals(String.format("Unexpected CheckSum, expected 0x%02X (%d), calculated 0x%02X (%d)", 106, 106, cs, cs), 106, cs);
    }

    @Test
    public void testCheckSum02() {
//        System.setProperty("nmea.parser.verbose", "true");
        String nmeaString = "$AIVSD,036,00.0,0000,@@@@@@@@@@@@@@@@@@@@,123456,06,03,00,00*4C"; // 4E";
        String stringToTest = nmeaString.substring(1, nmeaString.indexOf('*'));
        int cs = StringParsers.calculateCheckSum(stringToTest);
        System.out.printf("CS is 0x%02X\n", cs);
        assertEquals(String.format("Unexpected CheckSum, expected 0x%02X (%d), calculated 0x%02X (%d)", 76, 76, cs, cs), 76, cs);
    }
}

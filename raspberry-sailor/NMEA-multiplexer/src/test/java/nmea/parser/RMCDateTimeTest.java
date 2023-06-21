package nmea.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RMCDateTimeTest {

    static ObjectMapper mapper = new ObjectMapper();
    public static void seeForYourself() {
        final String nmeaSentence = "$GPRMC,075647.00,A,4740.66499,N,00308.13282,W,0.201,,210623,,,A*60";

        RMC rmc = null;
        try {
            rmc = StringParsers.parseRMC(nmeaSentence, true);
        } catch (Exception ex) { // Some NULL happen to sneak in some strings... TBD.
            System.err.println("Managed >>");
            ex.printStackTrace();
        }
        if (rmc != null) {
            if (rmc.isValid()) {
                if (rmc.getRmcDate() != null) {
                    UTCDate utcDate = new UTCDate(rmc.getRmcDate());
                    try {
                        final String dateStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(utcDate);
                        System.out.println("UTCDate:\n" + dateStr);
                    } catch (JsonProcessingException jpe) {
                        jpe.printStackTrace();
                    }
                }
                if (rmc.getRmcTime() != null) {
                    UTCTime utcTime = new UTCTime(rmc.getRmcTime());
                    try {
                        final String timeStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(utcTime);
                        System.out.println("UTCTime:\n" + timeStr);
                    } catch (JsonProcessingException jpe) {
                        jpe.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String... args) {
        seeForYourself();
    }
}

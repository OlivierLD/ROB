package nmea.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

public class GGADateTimeTest {

    static ObjectMapper mapper = new ObjectMapper();
    public static void seeForYourself() {
        final String nmeaSentence = "$GPGGA,014457,3739.853,N,12222.821,W,1,03,5.4,1.1,M,-28.2,M,,*7E";
         // "$GPGGA,124456.00,4740.66379,N,00308.13164,W,1,08,1.26,33.9,M,49.4,M,,*73";

        List<Object> gga = null;
        try {
            gga = StringParsers.parseGGA(nmeaSentence, true);
        } catch (Exception ex) { // Some NULL happen to sneak in some strings... TBD.
            System.err.println("Managed >>");
            ex.printStackTrace();
        }
        if (gga != null) {
            GeoPos ggaPos = (GeoPos) gga.get(StringParsers.GGA_POS_IDX);
            if (ggaPos != null) {
                // Duh
            }
            UTC ggaDate = (UTC) gga.get(StringParsers.GGA_UTC_IDX);
            if (ggaDate != null) {
                Date date = ggaDate.getDate();
                UTCDate utcDate =  new UTCDate(date.getYear() + 1900, date.getMonth(), date.getDate(), ggaDate.getH(), ggaDate.getM(), (int)ggaDate.getS(), (int)(1000 * (ggaDate.getS() - (int)ggaDate.getS())));
                try {
                    final String dateStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(utcDate);
                    System.out.println("UTCDate from GGA:\n" + dateStr);
                } catch (JsonProcessingException jpe) {
                    jpe.printStackTrace();
                }
            }
        }
    }

    public static void main(String... args) {
        seeForYourself();
    }
}

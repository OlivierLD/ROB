package generator;

import nmea.parser.StringGenerator;
import java.util.Date;
import java.util.TimeZone;

public class GenerateNMEA {

    public static void main(String[] args) {

        Date now = new Date();
        TimeZone.setDefault(TimeZone.getTimeZone("etc/UTC"));
        System.out.printf("UTC Date: %s\n", now);

        double lat=37.750;
        double lng=-122.300;

        TimeZone.setDefault(TimeZone.getTimeZone("etc/UTC"));
        String generatedRMC = StringGenerator.generateRMC("PC",
                new Date(), lat, lng, 4.5, 315, 0);

        System.out.printf("Generated NMEA: [%s]\n", generatedRMC );
        System.out.printf("Go ahead, do a\ncurl -ivX POST http://localhost:9999/mux/nmea-sentence -H \"Content-Type: text/plain\" -d '%s'\n",
                generatedRMC);

        System.out.println("Bye!");
    }

}
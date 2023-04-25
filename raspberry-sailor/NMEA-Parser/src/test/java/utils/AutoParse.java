package utils;

import nmea.parser.StringParsers;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AutoParse {

    // TODO More Strings...
    private final static String[] NMEA_DATA_SAMPLE = {
            "$GPRMC,170000.00,A,3744.79693,N,12223.30420,W,0.052,,200621,,,D*62",
            "$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D",
            "$GPRMC,170001.00,A,3744.79690,N,12223.30424,W,0.183,,200621,,,D*69",
            "$IIRMB,A,0.00,L,,HAKAAU  ,,,,,026.60,173,,V,A*67",
            "$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D",
            "$GPRMC,170002.00,A,3744.79681,N,12223.30435,W,0.228,,200621,,,D*68",
            "$IIGLL,0906.458,S,14012.521,W,220716,A,A*59",
            "$GPRMC,170003.00,A,3744.79677,N,12223.30440,W,0.035,,200621,,,D*6C",
            "$BMXDR,H,48.1,P,0,C,23.8,C,1,P,101775,P,2*6B",
            "$23DBS,01.9,f,0.58,M,00.3,F*21",
            "$GPGBS,163317.00,7.3,5.2,11.7,,,,*74",
            "$AISSD,PD2366@,MERRIMAC@@@@@@@@@@@@,017,000,03,02,1,AI*29",
            "$AIVSD,036,00.0,0000,@@@@@@@@@@@@@@@@@@@@,000000,00,00,00,00*4E",
            "$AIVSD,036,00.0,0000,@@@@@@@@@@@@@@@@@@@@,123456,06,03,00,00*4C",
            "$GPXTE,,,,,N,N*5E",
            "$GPXTE,V,V,,,N,S*43",
            "$GPAAM,V,V,0.05,N,*23",
            "$GPBOD,213.9,T,213.2,M,,*4C",
            "$GPBWC,195938,5307.2833,N,00521.7536,E,213.9,T,213.2,M,4.25,N,,A*53",
            "$GPBWR,195938,5307.2833,N,00521.7536,E,213.9,T,213.2,M,4.25,N,,A*42",
            "$GPAPB,A,A,0.001,L,N,V,V,213.9,T,,213.9,T,213.9,T,A*77",
            "$GPWCV,,N,,D*5F",
            "$IIVPW,5.30,N,,*07"
    };

    @Test
    public void autoParser() {
        Arrays.stream(NMEA_DATA_SAMPLE)
                .forEach(nmea -> {
                    try {
                        System.out.printf("Parsing [%s]\n", nmea);
                        StringParsers.ParsedData obj = StringParsers.autoParse(nmea);
                        if (obj != null) {
                            System.out.printf(">> Parsed >> %s\n", obj.getParsedData().toString());
                        } else {
                            System.out.println(">> null");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fail(String.format("Exception for %s: %s", nmea, ex));
                    }
                });
        assertTrue("Argh!", true); // Arf !
    }
}

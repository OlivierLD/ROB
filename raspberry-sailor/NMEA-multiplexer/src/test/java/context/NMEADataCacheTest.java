package context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NMEADataCacheTest {

    // private final String RMC_STRING = "$IIRMC,224044,A,0909.226,S,14015.162,W,06.7,222,211110,10,E,A*05";
    private final static String RMC_STRING = "$GPRMC,093054.00,A,4740.66504,N,00308.12883,W,0.959,150.09,280223,,,A*7B";

    // For tests
    public static void main(String... args) {
        System.setProperty("nmea.cache.verbose", "true");
        System.setProperty("put.ais.in.cache", "false");
        NMEADataCache cache = new NMEADataCache();
        try {
            cache.parseAndFeed(RMC_STRING);
            // Here, look at the cache
            System.out.println("Cache was fed.");
            // To JSON, with Jackson
            ObjectMapper mapper = new ObjectMapper();
//            mapper.readerFor(NMEADataCache.class);
            String json = mapper.writeValueAsString(cache);
            System.out.println("Look at the JSON.");
            final Object gpsDateTime = cache.get(NMEADataCache.GPS_DATE_TIME);
            String jsonGPSDateTime = mapper.writeValueAsString(gpsDateTime);
            System.out.println("Look at the UTCDate");
            // Tree, test
            final JsonNode jsonNode = mapper.valueToTree(cache);
            System.out.println("Look at the node");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("Done.");
    }
}

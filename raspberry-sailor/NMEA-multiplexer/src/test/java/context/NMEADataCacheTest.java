package context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NMEADataCacheTest {
    // For tests
    public static void main(String... args) {
        System.setProperty("nmea.cache.verbose", "true");
        System.setProperty("put.ais.in.cache", "false");
        NMEADataCache cache = new NMEADataCache();
        try {
            cache.parseAndFeed("$IIRMC,224044,A,0909.226,S,14015.162,W,06.7,222,211110,10,E,A*05");
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

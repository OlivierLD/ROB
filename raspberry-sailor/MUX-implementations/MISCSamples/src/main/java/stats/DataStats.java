package stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An example of stats based on the json file used by LeafLet
 * Beyond jq...
 * cat ./data/tbi.max-leeway-10.json  | jq '.[194]'
 *
 * JSON data generated with
 * ./log.to.leaflet.sh --file-name:2010-07-10.tacking.back.in.nmea \
 *                     --archive-name:sample-data/logged.data.zip \
 *                     --polar-file:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff \
 *                     --dev-curve:${HOME}/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv \
 *                     --max-leeway:10 \
 *                     --current-buffer-length:600000 \
 *                     --output-file-name:tbi.max-leeway-10.json \
 *                     --calc-tw-with-gps:true \
 *                     --aws-coef:1.0 \
 *                     --awa-offset:0
 */
public class DataStats {

    final static String DATA_FILE = "./data/tbi.max-leeway-10.json";
    final static int rangeBottom = 0;
    final static int rangeTop = // 1500; // Almost all
                                714; // Without motoring
    public static void main(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        final List<Map<String, Object>> objectList = mapper.readValue(new File(DATA_FILE), new TypeReference<>() {});
        System.out.printf("Read %d objects\n", objectList.size());
        List<Double> positiveAWA = new ArrayList<>();
        List<Double> negativeAWA = new ArrayList<>();

        List<Double> positiveTWA = new ArrayList<>();
        List<Double> negativeTWA = new ArrayList<>();

        objectList.stream().skip(rangeBottom).limit(rangeTop).forEach(obj -> {
            // System.out.printf("[%s] : %s\n", obj.getClass().getName(), obj);
            double awa = (double)obj.get("awa");
            if (awa > 0) {
                positiveAWA.add(awa);
            } else {
                negativeAWA.add(awa);
            }
            double twa = (double)obj.get("twa");
            if (twa > 0) {
                positiveTWA.add(twa);
            } else {
                negativeTWA.add(twa);
            }
        });
        System.out.printf("%d positive AWA, %d negative AWA\n", positiveAWA.size(), negativeAWA.size());
        final double averagePositiveAWA = positiveAWA.stream().mapToDouble(d -> d).average().getAsDouble();
        final double averageNegativeAWA = negativeAWA.stream().mapToDouble(d -> d).average().getAsDouble();
        System.out.printf("AWA: AVG Positive: %f, AVG Negative: %f\n", averagePositiveAWA, averageNegativeAWA);

        // Indexes of min and max
        final double maxAWA = positiveAWA.stream().mapToDouble(d -> d).max().getAsDouble();
        final double minAWA = negativeAWA.stream().mapToDouble(d -> d).min().getAsDouble();

        int maxAWAindex = objectList.indexOf(objectList.stream().filter(obj -> maxAWA == (double) obj.get("awa")).findFirst().get());
        int minAWAindex = objectList.indexOf(objectList.stream().filter(obj -> minAWA == (double) obj.get("awa")).findFirst().get());

        System.out.printf(">> Max AWA (%.02f) at index %d, Min AWA (%.02f) at index %d\n", maxAWA, maxAWAindex, minAWA, minAWAindex);

        System.out.printf("%d positive AWA, %d negative AWA\n", positiveTWA.size(), negativeTWA.size());
        final double averagePositiveTWA = positiveTWA.stream().mapToDouble(d -> d).average().getAsDouble();
        final double averageNegativeTWA = negativeTWA.stream().mapToDouble(d -> d).average().getAsDouble();
        System.out.printf("TWA: AVG Positive: %f, AVG Negative: %f\n", averagePositiveTWA, averageNegativeTWA);

        // Average TWD
        final double averageCos = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> Math.toRadians((double) obj.get("twd"))).mapToDouble(Math::cos).average().getAsDouble();
        final double averageSin = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> Math.toRadians((double) obj.get("twd"))).mapToDouble(Math::sin).average().getAsDouble();
        double avgTWD = Math.toDegrees(Math.atan2(averageSin, averageCos));
        if (avgTWD < 0) {
            avgTWD += 360;
        }
        System.out.printf("AVG TWD: %.02f\u00b0\n", avgTWD);

        System.out.println("----------");
        // Average speeds
        final double averageTws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("tws")).filter(d -> d != 0).mapToDouble(d -> d).average().getAsDouble();
        final double minTws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("tws")).filter(d -> d != 0).mapToDouble(d -> d).min().getAsDouble();
        final double maxTws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("tws")).filter(d -> d != 0).mapToDouble(d -> d).max().getAsDouble();

        final double averageAws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("aws")).mapToDouble(d -> d).average().getAsDouble();
        final double minAws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("aws")).filter(d -> d != 0).mapToDouble(d -> d).min().getAsDouble();
        final double maxAws = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("aws")).filter(d -> d != 0).mapToDouble(d -> d).max().getAsDouble();

        final double averageBsp = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("bsp")).mapToDouble(d -> d).average().getAsDouble();
        final double minBsp = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("bsp")).filter(d -> d != 0).mapToDouble(d -> d).min().getAsDouble();
        final double maxBsp = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("bsp")).filter(d -> d != 0).mapToDouble(d -> d).max().getAsDouble();

        final double averageSog = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("sog")).mapToDouble(d -> d).average().getAsDouble();
        final double minSog = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("sog")).filter(d -> d != 0).mapToDouble(d -> d).min().getAsDouble();
        final double maxSog = objectList.stream().skip(rangeBottom).limit(rangeTop).map(obj -> (double) obj.get("sog")).filter(d -> d != 0).mapToDouble(d -> d).max().getAsDouble();

        System.out.printf("Average TWS: %.02f kn (in [%.02f, %.02f])\n", averageTws, minTws, maxTws);
        System.out.printf("Average AWS: %.02f kn (in [%.02f, %.02f])\n", averageAws, minAws, maxAws);
        System.out.printf("Average BSP: %.02f kn (in [%.02f, %.02f])\n", averageBsp, minBsp, maxBsp);
        System.out.printf("Average SOG: %.02f kn (in [%.02f, %.02f])\n", averageSog, minSog, maxSog);
    }
}

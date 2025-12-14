package algebra.conic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/*
  See https://fr.wikipedia.org/wiki/Conique
 */
public class TestOne {

    private final static ObjectMapper mapper = new ObjectMapper(); // Jackson
    private static String jsonPointsCloud = "cloud.json";

    private static class Point {
        double x;
        double y;

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
    public static void main(String[] args) {

        try {
            InputStream inputStream = new FileInputStream(jsonPointsCloud);

            // The two options below work fine.
            List<Point> pointList =
                mapper.readValue(inputStream, new TypeReference<List<Point>>(){});
                // mapper.readerForListOf(Point.class).readValue(inputStream);

            System.out.printf("Data loaded. Read %d points\n", pointList.size());

            double minX = pointList.stream().mapToDouble(p -> p.x).min().getAsDouble();
            double maxX = pointList.stream().mapToDouble(p -> p.x).max().getAsDouble();
            double minY = pointList.stream().mapToDouble(p -> p.y).min().getAsDouble();
            double maxY = pointList.stream().mapToDouble(p -> p.y).max().getAsDouble();

            System.out.printf("MinX: %f, MaxX: %f, MinY: %f, MaxY: %f\n", minX, maxX, minY, maxY);

        } catch (IOException fnfe) {
            System.err.println("Oops: ");
            fnfe.printStackTrace();
        }
    }

}
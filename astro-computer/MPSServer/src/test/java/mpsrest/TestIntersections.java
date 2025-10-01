package mpsrest;

import calc.GeoPoint;
import mps.MPSToolBox;

import java.util.ArrayList;
import java.util.List;

public class TestIntersections {

    private static void testOne() {
        System.out.println("--- Test One ---");
        try {
            List<MPSToolBox.ConesIntersection> conesIntersections = new ArrayList<>();
            MPSToolBox.ConesIntersection one = new MPSToolBox.ConesIntersection("Saturn", "Jupiter",
                    new GeoPoint(47.677643, -3.135670), new GeoPoint(47.677643, -3.135670),
                    new GeoPoint(-10.904689, 13.240187), new GeoPoint(-10.904689, 13.240187));
            MPSToolBox.ConesIntersection two = new MPSToolBox.ConesIntersection("Saturn", "Rigel",
                    new GeoPoint(47.677643, -3.135670), new GeoPoint(47.677643, -3.135670),
                    new GeoPoint(-63.208440, -12.106294), new GeoPoint(-63.208440, -12.106294));
            conesIntersections.add(one);
            conesIntersections.add(two);

            final GeoPoint geoPoint = MPSToolBox.processIntersectionsList(conesIntersections, false);
            System.out.printf("testOne: Intersection final result: %s\n", geoPoint);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void testTwo() {
        System.out.println("--- Test Two ---");
        try {
            List<MPSToolBox.ConesIntersection> conesIntersections = new ArrayList<>();
            MPSToolBox.ConesIntersection one = new MPSToolBox.ConesIntersection("Jupiter", "Saturn",
                    new GeoPoint(-10.904689, 13.240187), new GeoPoint(-10.904689, 13.240187),
                    new GeoPoint(47.677643, -3.135670), new GeoPoint(47.677643, -3.135670));
            MPSToolBox.ConesIntersection two = new MPSToolBox.ConesIntersection("Saturn", "Rigel",
                    new GeoPoint(47.677643, -3.135670), new GeoPoint(47.677643, -3.135670),
                    new GeoPoint(-63.208440, -12.106294), new GeoPoint(-63.208440, -12.106294));
            conesIntersections.add(one);
            conesIntersections.add(two);

            final GeoPoint geoPoint = MPSToolBox.processIntersectionsList(conesIntersections, false);
            System.out.printf("testTwo: Intersection final result: %s\n", geoPoint);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void testThree() {
// curl -X POST http://localhost:9999/mps/process-intersections -d '[{"bodyOneName":"Saturn","bod{"latitude":7.677668,"longitude":-3.135668},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-10.904689,"longitude":13.240187},"coneTwoIntersectionTwo":{"latitude":-10.904689,"longitude":13.240187}},{"bodyOneName":"Saturn","bodyTwoName":"Rigel","coneOneIntersectionOne":{"latitude":7.677668,"longitude":-3.135668},"coneOneIntersectionTwo":{"latitude":47.677643,"longitude":-3.13567},"coneTwoIntersectionOne":{"latitude":-63.20844,"longitude":-12.106294},"coneTwoIntersectionTwo":{"latitude":-63.20844,"longitude":-12.106294}}]'
/*
        [
  {
    "bodyOneName": "Saturn",
    "bodyTwoName": "Jupiter",
    "coneOneIntersectionOne": {
      "latitude": 7.677668,
      "longitude": -3.135668
    },
    "coneOneIntersectionTwo": {
      "latitude": 47.677643,
      "longitude": -3.13567
    },
    "coneTwoIntersectionOne": {
      "latitude": -10.904689,
      "longitude": 13.240187
    },
    "coneTwoIntersectionTwo": {
      "latitude": -10.904689,
      "longitude": 13.240187
    }
  },
  {
    "bodyOneName": "Saturn",
    "bodyTwoName": "Rigel",
    "coneOneIntersectionOne": {
      "latitude": 7.677668,
      "longitude": -3.135668
    },
    "coneOneIntersectionTwo": {
      "latitude": 47.677643,
      "longitude": -3.13567
    },
    "coneTwoIntersectionOne": {
      "latitude": -63.20844,
      "longitude": -12.106294
    },
    "coneTwoIntersectionTwo": {
      "latitude": -63.20844,
      "longitude": -12.106294
    }
  }
]
*/
        System.out.println("--- Test Three (Should not work fine... Absurd data) ---");
        try {
            List<MPSToolBox.ConesIntersection> conesIntersections = new ArrayList<>();
            MPSToolBox.ConesIntersection one = new MPSToolBox.ConesIntersection("Jupiter", "Saturn",
                    new GeoPoint(7.677668, -3.135668), new GeoPoint(47.677643, -3.13567),
                    new GeoPoint(10.904689, 13.240187), new GeoPoint(10.904689, 13.240187));
            MPSToolBox.ConesIntersection two = new MPSToolBox.ConesIntersection("Saturn", "Rigel",
                    new GeoPoint(7.677668, -3.135668), new GeoPoint(47.677643, -3.13567),
                    new GeoPoint(-63.20844, -12.106294), new GeoPoint(-63.20844, -12.106294));
            conesIntersections.add(one);
            conesIntersections.add(two);

            final GeoPoint geoPoint = MPSToolBox.processIntersectionsList(conesIntersections, false);
            System.out.printf("testThree: Intersection final result: %s\n", geoPoint);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) {

        testOne();
        testTwo();
        testThree();

    }
}
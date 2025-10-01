package mpsrest;

import calc.GeoPoint;
import mps.MPSToolBox;

import java.util.ArrayList;
import java.util.List;

public class TestIntersections {

    private static void testOne() {
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

            final GeoPoint geoPoint = MPSToolBox.processIntersectionsList(conesIntersections, true);
            System.out.printf("testOne: Intersection final result: %s\n", geoPoint);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    private static void testTwo() {
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

            final GeoPoint geoPoint = MPSToolBox.processIntersectionsList(conesIntersections, true);
            System.out.printf("testTwo: Intersection final result: %s\n", geoPoint);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    public static void main(String... args) {

        testOne();
        testTwo();

    }
}
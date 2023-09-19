package geopos;

import calc.GeomUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CodePlusTest {

    @Test
    public void testOne() {
        final String googleCodePlus = GeomUtil.googleCodePlus(47.677667, -3.135667);
        System.out.println(googleCodePlus);
        assertEquals(googleCodePlus, "8CVRMVH7+3P");
    }
}

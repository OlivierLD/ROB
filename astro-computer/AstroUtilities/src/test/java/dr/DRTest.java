package dr;

import calc.CelestialDeadReckoning;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DRTest {
    @Test
    public void deadReckoningTest() {
        double ahg = 0d;
        double d = 0d;
        double l = 47.677667;
        double g = -3.135667;
        CelestialDeadReckoning dr = new CelestialDeadReckoning(ahg, d, l, g).calculate();
        double he = 42.244264171723536;
        double z = 175.7624980808786;
        assertTrue("Unexpected He", dr.getHe() == he);
        assertTrue("Unexpected Z", dr.getZ() == z);
        System.out.println("Done");
    }
}
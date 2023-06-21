package suntest;

import implementation.almanac.AlmanacComputerImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNBDays {

    @Test
    public void testNBDays() {
        int nbDays = AlmanacComputerImpl.getNbDays(2020, 2); // Leap year
        assertEquals("2020 is a leap year", 29, nbDays);
        nbDays =  AlmanacComputerImpl.getNbDays(2000, 2); // Leap year
        assertEquals("2000 is INDEED a leap year", 29, nbDays);
        nbDays =  AlmanacComputerImpl.getNbDays(2100, 2); // NOT Leap year
        assertEquals("2100 is NOT a leap year", 28, nbDays);
        nbDays =  AlmanacComputerImpl.getNbDays(2022, 1);
        assertEquals("JANUARY always has 31 days", 31, nbDays);
    }
}

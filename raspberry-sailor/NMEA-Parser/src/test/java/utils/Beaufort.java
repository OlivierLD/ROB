package utils;

import nmea.utils.WindUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Beaufort {

    @Test
    public void beaufortTest() {
        int beaufort = WindUtils.getBeaufort(23.45);
        assertEquals(String.format("Expected 6, got %d", beaufort), 6, beaufort);

        beaufort = WindUtils.getBeaufort(70d);
        assertTrue(String.format("Expected 12+, got %d", beaufort), beaufort >= 12);

        beaufort = WindUtils.getBeaufort(20d);
        assertEquals(String.format("Expected 5, got %d", beaufort), 5, beaufort);
    }

}

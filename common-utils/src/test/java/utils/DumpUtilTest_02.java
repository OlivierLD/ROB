package utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class DumpUtilTest_02 {
    public static void main(String... args) {

        String fileToDump = "/Users/olivierlediouris/repos/ROB/raspberry-sailor/RESTRouting/samples/_cache_weather-cache_EastAtlantic.wind.7days.grb";

        try (FileInputStream fis = new FileInputStream(fileToDump)) {
            int chunkLen = 64;
            byte[] chunk = new byte[chunkLen];
            int offset = 0;
            boolean keepReading = true;
            while (keepReading) {
                int read = fis.read(chunk);
                if (read > 0) {

                    String[] dd = DumpUtil.dualDump(chunk);
                    System.out.printf("Offset: %d\n", offset);
                    for (String l : dd) {
                        System.out.println(l);
                    }
                    offset += chunkLen;
                    // reset chunk
                    Arrays.fill(chunk, (byte)0);

                } else {
                    keepReading = false;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

package memory;

import java.text.NumberFormat;

public class GCTest {

    private final String BOLD_RED = "\033[0;31;1m";            // Red and Bold
    private final String GREEN = "\033[92m";                   // Green
    private final String RED = "\033[91m";                     // Red
    private final String BOLD_GREEN_BLINK = "\033[0;32;1;5m";  // Green, bold, blink.
    private final String BOLD_RED_BLINK = "\033[0;31;1;5m";    // Red, bold, blink.
    private final String NC = "\033[0m";                       // Back to No Color

    public void runGC() {
        Runtime runtime = Runtime.getRuntime();
        long memoryMax = runtime.maxMemory();
        System.out.printf("The maximum memory: %s bytes (%s Mb, %s Gb)\n",
                NumberFormat.getInstance().format(memoryMax),
                NumberFormat.getInstance().format(memoryMax / (1024 * 1024)),
                NumberFormat.getInstance().format(memoryMax / (1024 * 1024 * 1024)));
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;
        System.out.printf("The memory used by program (in percent): %s %.02f %% %s\n", RED, memoryUsedPercent, NC);
        if (memoryUsedPercent > 90.0){
            System.gc();
        }
        String testString = "this a test";

        System.out.printf("%s %s %s\n", RED, testString, NC);
        System.out.printf("%s %s %s\n", GREEN, testString, NC);
        System.out.printf("%s %s %s\n", BOLD_RED, testString, NC);
        System.out.printf("%s %s %s\n", BOLD_GREEN_BLINK, testString, NC);
        System.out.printf("%s %s %s\n", BOLD_RED_BLINK, testString, NC);
    }
    public static void main(String args[]) {
        GCTest test = new GCTest();
        test.runGC();
    }
}
package memory;

import java.text.NumberFormat;

public class GCTest {
    public void runGC() {
        Runtime runtime = Runtime.getRuntime();
        long memoryMax = runtime.maxMemory();
        System.out.printf("The maximum memory: %s bytes (%s Mb, %s Gb)\n",
                NumberFormat.getInstance().format(memoryMax),
                NumberFormat.getInstance().format(memoryMax / (1024 * 1024)),
                NumberFormat.getInstance().format(memoryMax / (1024 * 1024 * 1024)));
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        double memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;
        System.out.printf("The memory used by program (in percent): %f %%\n", memoryUsedPercent);
        if (memoryUsedPercent > 90.0){
            System.gc();
        }
    }
    public static void main(String args[]) {
        GCTest test = new GCTest();
        test.runGC();
    }
}
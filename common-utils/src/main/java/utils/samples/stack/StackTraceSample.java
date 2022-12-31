package utils.samples.stack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class StackTraceSample {

    private static void tryToOpen(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String... args) {
        try {
            tryToOpen("non-existent.stuff");
        } catch (Exception ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                System.out.println("OK, that was expected.");
            } else {
                final StackTraceElement[] stackTrace = ex.getStackTrace();
                for (StackTraceElement ste : stackTrace) {
                    System.out.printf(">> Stack -> %s\n", ste);
                }
            }
            ex.printStackTrace();
        }
    }
}

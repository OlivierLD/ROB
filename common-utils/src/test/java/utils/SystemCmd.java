package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
 * Outside the IDE, use:
 * $ javac --source-path src/test/java -d build/classes src/test/java/utils/SystemCmd.java
 * $ java -cp build/classes utils.SystemCmd
 */
public class SystemCmd {
    public static void main(String... args) {
        System.out.printf("Running from %s\n", System.getProperty("user.dir"));
        try {
//            final String[] command = new String[] { "ls", "-l", "README.*" };  // Fails
//            final String[] command = new String[] { "/bin/bash", "-c", "ls", "-l", "README.*" };  // Works!!
            // --------------------------------
//            final String[] command = new String[] { "/bin/bash", "-c", "ls -l README.*" };  // Works!!
            // --------------------------------
//            final String[] command = new String[] { "ls", "-l", "README.md" }; // Good
//            final String[] command = new String[] { "time", "ls", "-l", "README.md" }; // Good... ish.
//            final String[] command = new String[] { "time ls -l README.md" };    // Fails
//            final String[] command = new String[] { "time", "ls -l README.md" }; // Fails, but nicer than above
//            final String[] command = new String[] { "date" }; // Good
            final String[] command = new String[] { "/bin/bash", "-c", "date -s '29 MAY 2023 14:34:00'" };
//            final String[] command = new String[] { "/bin/bash", "-c", "time", "date" }; // Good... ish
//            final String[] command = new String[] { "/bin/bash", "-c", "time date" }; // Good... ish

            Process p = Runtime.getRuntime().exec(command);  // That one may raise uncaught IOException...
            List<String> output = new ArrayList<>();
            int exitCode = p.waitFor();
            BufferedReader reader = null;
            if (exitCode == 0) {
                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            }
            String line = "";
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    output.add(line);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (exitCode == 0) {
                System.out.println("-- Success! --");
                output.forEach(System.out::println);
                System.out.println("--------------");
            } else {
                System.out.printf("-- Error Code:%d --\n", exitCode);
                output.forEach(System.out::println);
                System.out.println("------------------");
            }
            System.out.println("Cheers!");
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }
}

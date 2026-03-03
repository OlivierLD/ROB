package utils;

public class DumpUtilTest {
    public static void main(String... args) {
        String forTests = "$GPGSA,A,3,07,17,30,11,28,13,01,19,,,,,2.3,1.4,1.9*3D";
        String[] dd = DumpUtil.dualDump(forTests);
        for (String l : dd) {
            System.out.println(l);
        }
        System.out.println("--- W H O   C A L L E D   M E ---");
        DumpUtil.whoCalledMe().stream().forEach(System.out::println);

        fromMethod();
    }

    private static void fromMethod() {
        System.out.printf("From another method than the main...\n");
        System.out.println("--- W H O   C A L L E D   M E ---");
        DumpUtil.whoCalledMe().stream().forEach(System.out::println);

        System.out.println("--- W H O   C A L L E D   M E (method) ---");
        DumpUtil.whoCalledMe().stream().forEach(line -> System.out.println(DumpUtil.extractMethodName(line)));

        System.out.println("--- TOP METHOD ---");
        System.out.printf("Top Method: %s\n", DumpUtil.topMethodInTheStack());

    }
}
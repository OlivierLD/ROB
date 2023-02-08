package grid;

import utils.DumpUtil;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GridFileDump {

    private static void binaryStuff() {
        byte one = (byte)0xEF;
        byte oneToBe = (byte)0xFF;

        byte two = (byte)0xBF;
        byte twoToBe = (byte)0x80;

        // To try, x & 0x7F, x | 0x10, x | 0x80, x = ~x (2's complement)

        System.out.printf("One: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(one & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(one & 0xFF).toUpperCase(), 2, "0"));
        System.out.printf("One2Be: \t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneToBe & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneToBe & 0xFF).toUpperCase(), 2, "0"));

        System.out.printf("Two: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(two & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(two & 0xFF).toUpperCase(), 2, "0"));
        System.out.printf("Two2Be: \t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(twoToBe & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(twoToBe & 0xFF).toUpperCase(), 2, "0"));

        byte oneBis = (byte)(one & 0x7F);
        System.out.printf("One & 0x7F: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)(one | 0x80);
        System.out.printf("One | 0x80: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)(one | 0x10);
        System.out.printf("One | 0x10: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)((one & 0x7F) | 0x80);
        System.out.printf("(One & 0x7F) | 0x80: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)(~one);
        System.out.printf("~One: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)(~(one & 0x7F));
        System.out.printf("~(One & 0x7F): \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));

        oneBis = (byte)(~((one | 0x80) & 0x7F));
        System.out.printf("~((One | 0x80) & 0x7F): \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(oneBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(oneBis & 0xFF).toUpperCase(), 2, "0"));


        byte twoBis = (byte)(two | 0x10);
        System.out.printf("Two | 0x10: \t\t0b%s 0x%s\n",
                StringUtils.lpad(Integer.toBinaryString(twoBis & 0xFF), 8, "0"),
                StringUtils.lpad(Integer.toHexString(twoBis & 0xFF).toUpperCase(), 2, "0"));
    }

    public static void main(String... args) {
        String filePath = "GRIB_2009_02_25_Sample.grb";
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));

            // file to byte[], File -> Path
//        File file = new File(filePath);
//        byte[] bytes = Files.readAllBytes(file.toPath());

            System.out.printf("Read %d byte(s)\n", bytes.length);

            final int CHUNK_SIZE = 512;
            byte[] chunk = new byte[CHUNK_SIZE];
            System.arraycopy(bytes, 0, chunk, 0, CHUNK_SIZE);
            String[] dd = DumpUtil.dualDump(chunk);
            System.out.println("Head");
            for (String l : dd) {
                System.out.println(l);
            }

            System.arraycopy(bytes, bytes.length - CHUNK_SIZE, chunk, 0, CHUNK_SIZE);
            dd = DumpUtil.dualDump(chunk);
            System.out.println("Tail");
            for (String l : dd) {
                System.out.println(l);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        binaryStuff();
    }
}

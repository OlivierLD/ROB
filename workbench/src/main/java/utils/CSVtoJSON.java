package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.jdeprscan.CSV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CSVtoJSON {

    final static ObjectMapper mapper = new ObjectMapper();
    public final static class Payload {
        String firstName;
        String lastName;
        String email;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
    private final static String CSV_FILE = "../temp/passe-coque.adherents.csv";
    public static void main(String... args) {
        System.out.printf("Running from %s\n", System.getProperty("user.dir"));
        File csv = new File(CSV_FILE);
        if (csv.exists()) {
            System.out.println("All good, moving on.");
        } else {
            System.out.printf("File %s not found\n", CSV_FILE);
            System.exit(1);
        }
        try {
            List<Payload> jsonList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(csv));
            String line = "";
            while (line != null) {
                line = br.readLine();
                if (line != null) {
                    String[] members = line.split("\t");
                    if (members.length >= 5) {
                        System.out.printf("Read: %s, %s, %s\n", members[0], members[1], members[5]);
                        Payload payload = new Payload();
                        payload.firstName = members[1];
                        payload.lastName = members[0];
                        payload.email = members[5];
                        jsonList.add(payload);
                    }
                }
            }
            br.close();

            FileWriter jsonOutput = new FileWriter("members.json");
            mapper.writeValue(jsonOutput, jsonList);
            jsonOutput.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

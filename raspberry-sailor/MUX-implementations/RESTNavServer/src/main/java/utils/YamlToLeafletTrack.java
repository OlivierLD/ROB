package utils;

import org.yaml.snakeyaml.Yaml;
import util.MarkersToJSON;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class YamlToLeafletTrack {

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("Please provide yaml file name as CLI parameter.");
            // Like /Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/web/muxbuilder/norderney.la.trinite.yaml
            System.exit(1);
        }
        String yamlFileName = args[0];
        try {
            String content = MarkersToJSON.convertToJSON(yamlFileName, true);
            // System.out.println(content);
            String outputFile = yamlFileName.replace(".yaml", ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            bw.write(content);
            bw.flush();
            bw.close();
            System.out.printf("JSON content is in %s\n", outputFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
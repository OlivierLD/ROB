package utils;

import nmea.ais.AISParser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParseAIS {

    private final static String[] AIS_SENTENCES = new String [] {
            "!AIVDM,2,2,0,,000000003,2,2*78",
            "!AIVDM,2,1,0,,53aFgph000010KWWOV1`4p@EQ18E>3F22222220Q9h8252w100000000000000,0,2*48",
            "!AIVDM,1,1,2,,15Q:RP002g0FNinN`lK9fWk<0000,0,0*18",
            "!AIVDM,1,1,3,,13aL?>HP00PHj5:NKAH00?w>2000,0,0*7B",
            "!AIVDM,1,1,4,,13bHGL?P0KPHNMpNUAwRigw@2000,0,0*57"
    };

    private AISParser aisParser;

    @Before
    public void before() {
        System.setProperty("ais.verbose", "true");
        System.setProperty("ais.decode.verbose", "true");
        aisParser = new AISParser();
    }

    @Test
    public void aisParserTestOne() {
        String aisSentenceOne = AIS_SENTENCES[2];
        try {
            final AISParser.AISRecord aisRecord = aisParser.parseAIS(aisSentenceOne);
            assertNotNull("AISRecord is null", aisRecord);
            System.out.println("-- Decoded AIS Data: --\n" + aisRecord.toString(true));
            System.out.println("-----------------------");
        } catch (AISParser.AISException ae) {
            ae.printStackTrace();
            fail(String.format("Failed parsing [%s]: %s", aisSentenceOne, ae));
        }
    }
}

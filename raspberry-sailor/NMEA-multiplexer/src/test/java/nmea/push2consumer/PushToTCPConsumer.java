package nmea.push2consumer;

import nmea.forwarders.TCPServer;
import nmea.parser.StringGenerator;
import nmea.utils.MuxNMEAUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Requires a MUX with a TCP consumer on port 7002.
 */
public class PushToTCPConsumer {

    static String[] nmeaData = new String [] {
            "$GPRMC,112847.00,V,,,,,,,,,,N*74",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51",
            "$CCMWD,000.0,T,000.0,M,0.0,N,0.0,M*44",
            "$GPVTG,,,,,,,,,N*30",
            "$GPGLL,,,,,112847.00,V,N*43",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51",
            "$CCMWD,000.0,T,000.0,M,0.0,N,0.0,M*44",
            "$GPRMC,112848.00,V,,,,,,,,,,N*7B",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51",
            "$CCMWD,000.0,T,000.0,M,0.0,N,0.0,M*44",
            "$GPVTG,,,,,,,,,N*30",
            "$GPGLL,,,,,112848.00,V,N*4C",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51",
            "$CCMWD,000.0,T,000.0,M,0.0,N,0.0,M*44",
            "$GPRMC,112849.00,V,,,,,,,,,,N*7A",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51",
            "$CCMWD,000.0,T,000.0,M,0.0,N,0.0,M*44",
            "$GPVTG,,,,,,,,,N*30",
            "$PYMTA,28.3,C*05",
            "$PYMMB,30.1084,I,1.0195,B*72",
            "$PYXDR,H,100.0,P,0,C,28.3,C,1,C,28.3,C,DEWP,P,101947,P,3,P,1.0195,B,4*7A",
            "$GPGLL,,,,,112849.00,V,N*4D",
            "$CCMWV,000.0,T,000.0,N,A*3B",
            "$CCVWT,0.0,L,0.0,N,0.0,M,0.0,K*51"
    };

    public static void main(String[] args) {

        Date now = new Date();
        TimeZone.setDefault(TimeZone.getTimeZone("etc/UTC"));
        System.out.printf("UTC Date: %s\n", now);

        int tcpPort = 7002;

        if (true) {
            try {
                TCPServer tcpw = new TCPServer(tcpPort);

                // Will send the sentences
                Arrays.asList(nmeaData).forEach(sentence -> {
                    System.out.printf("Sending [%s]...\n", sentence);
                    try {
                        tcpw.write(sentence.getBytes());
                    } catch (Exception ex) {
                        System.err.println(ex.getLocalizedMessage());
                    }
                    try {
                        Thread.sleep(250L);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                System.out.println("------------------");

                String generatedRMC = StringGenerator.generateRMC("PC",
                        new Date(), 47d, -3d, 4.5, 45, 0);
                System.out.printf("Sending generated [%s]...\n", generatedRMC);
                try {
                    tcpw.write(generatedRMC.getBytes());
                } catch (Exception ex) {
                    System.err.println(ex.getLocalizedMessage());
                }

                try {
                    tcpw.close();
                    System.out.println("TCPServer closed.");
                    try {
                        tcpw.write("Just in Case".getBytes());
                    } catch (Exception ex) {
                        System.err.println(ex.getLocalizedMessage());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("---------------------------------");

            // Wait for the previous to be closed
            try {
                Thread.sleep(5_000L);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        TimeZone.setDefault(TimeZone.getTimeZone("etc/UTC"));
        String generatedRMC = StringGenerator.generateRMC("AA",
                new Date(), 47d, -3d, 4.5, 135, 0);
        String [] dataArray = new String[] { generatedRMC };


        try {
            MuxNMEAUtils.pushToTCPConsumer(tcpPort, dataArray, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Bye!");
    }
}
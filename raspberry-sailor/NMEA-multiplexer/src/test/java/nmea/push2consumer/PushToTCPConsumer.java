package nmea.push2consumer;

import nmea.forwarders.TCPServer;

import java.util.Arrays;

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
        // String gpsd = "{\"class\":\"TVP\",\"tag\":\"MID2\",\"time\":\"2010-04-30T11:48:20.10Z\",\"ept\":0.005,\"lat\":46.498204497,\"lon\":7.568061439,\"alt\":1327.689,\"epx\":15.319,\"epy\":17.054,\"epv\":124.484,\"track\":10.3797,\"speed\":0.091,\"climb\":-0.085,\"eps\",34.11,\"mode\":3}";
        // String gpsd = "?WATCH={...};";
        String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
        try {
            TCPServer tcpw = new TCPServer(7002);

            // Will send the sentences
            Arrays.asList(nmeaData).forEach(sentence -> {
                System.out.printf("Sending [%s]...\n", sentence);
                try {
                    tcpw.write(sentence.getBytes());
                } catch (Exception ex) {
                    System.err.println(ex.getLocalizedMessage());
                }
                try {
                    Thread.sleep(1_000L);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            try {
                tcpw.close();
                System.out.println("Server closed.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
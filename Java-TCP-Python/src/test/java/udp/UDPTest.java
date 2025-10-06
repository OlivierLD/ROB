package udp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import udp.client.EchoClient;
import udp.server.SimpleUDPServer;

import java.net.SocketException;

import static org.junit.Assert.*;

public class UDPTest {
    EchoClient client;

    @Before
    public void setup() {
        System.setProperty("udp.verbose", "true");
        new SimpleUDPServer().start();
        client = new EchoClient();
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() {
        try {
            String payload = "hello server";
            System.out.printf("1 - Sending [%s]\n", payload);
            String echo = client.sendEcho(payload);
            System.out.printf(">> Server replied: [%s]\n", echo);
            assertEquals(payload, echo);
            //
            payload = "server is working";
            System.out.printf("2 - Sending [%s]\n", payload);
            echo = client.sendEcho(payload);
            System.out.printf(">> Server replied: [%s]\n", echo);
            assertTrue(echo.equals(payload));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            System.out.println("Closing client...");
            Thread byeThread = new Thread(() -> {
                try {
                    client.sendEcho("end"); // Tell the server to shut down (and up).
                } catch (SocketException sex) {
                    // Socket is closed ?
                    if ("Socket is closed".equals(sex.getMessage().trim())) {
                       // That's OK.
                    } else {
                        sex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, "bye");
            byeThread.start();
            System.out.println("END request sent");
            client.close();
            System.out.println("tearDown completed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * No need to use that main here.
     *
     * @param args
     */
    public static void main(String... args) {
        udp.test.UDPTest test = new udp.test.UDPTest();
        test.setup();
        test.whenCanSendAndReceivePacket_thenCorrect();
        test.tearDown();
        System.out.println("Bye!");
    }
}
package udp.test;

import udp.client.EchoClient;
import udp.server.SimpleUDPServer;

/**
 * This is NOT a UnitTest.
 * In case you missed it.
 */
public class UDPTest {
    EchoClient client;

    // @Before
    public void setup(){
        new SimpleUDPServer().start();
        client = new EchoClient();
    }

    // @Test
    public void whenCanSendAndReceivePacket_thenCorrect() {
        try {
            String echo = client.sendEcho("hello server");
            // assertEquals("hello server", echo);
            System.out.printf("Sending [%s]\n", echo);
            echo = client.sendEcho("server is working");
            // assertFalse(echo.equals("hello server"));
            System.out.printf("Server replied: [%s]\n", echo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // @After
    public void tearDown() {
        try {
            client.sendEcho("end");
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) {
        System.out.println("Get ready...");
        UDPTest test = new UDPTest();
        test.setup();
        test.whenCanSendAndReceivePacket_thenCorrect();
        test.tearDown();
        System.out.println("Bye!");
    }
}
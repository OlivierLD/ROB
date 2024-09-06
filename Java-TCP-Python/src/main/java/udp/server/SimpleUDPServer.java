package udp.server;
/*
 * From https://www.baeldung.com/udp-in-java
 * Also see https://www.codejava.net/java-se/networking/java-udp-client-server-program-example
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleUDPServer extends Thread {

    private final static int PORT = 8_002; // 4_445;
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    private final static boolean VERBOSE = /* true; */ "true".equals(System.getProperty("udp.verbose"));
    // Default true
    private final static boolean SEND_BACK = /* false; */ !("false".equals(System.getProperty("udp.send.back")));

    public SimpleUDPServer() {
        try {
            socket = new DatagramSocket(PORT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                for (int i=0; i<buf.length; i++) {
                    buf[i] = (byte)0;
                }
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                if (VERBOSE) {
                    System.out.println("Receiving...");
                }
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (VERBOSE) {
                    System.out.println("Received !");
                    System.out.printf("[%s]\n", received);
                }
                // TODO Warning: packet might not have been reset...
                if (received.equals("end") || received.startsWith("end")) {
                    System.out.println("(Server exiting.)");
                    running = false;
                    continue; // Skip out of the while loop, to avoid (anyway) the socket.send below.
                }
                if (SEND_BACK) {
                    socket.send(packet); // Send it back
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        socket.close();
        System.out.println("Server's out.");
    }

    /*
        This is just for tests.
        Use it with a UDP forwarder from OpenCPN... Sett VERBOSE to true.
        Make sure the port is right.
     */
    public static void main(String[] args) {
        new SimpleUDPServer().start();
    }
}

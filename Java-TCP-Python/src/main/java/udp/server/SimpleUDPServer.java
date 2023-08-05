package udp.server;
/*
 * From https://www.baeldung.com/udp-in-java
 * Also see https://www.codejava.net/java-se/networking/java-udp-client-server-program-example
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleUDPServer extends Thread {

    private final static int PORT = 4_445;
    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

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
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());

                // TODO Warning: packet was not reset...
                if (received.equals("end") || received.startsWith("end")) {
                    System.out.println("(Server exiting.)");
                    running = false;
                    continue; // Skip out of the while loop, to avoid the socket.send below.
                }
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        socket.close();
        System.out.println("Server's out.");
    }
}

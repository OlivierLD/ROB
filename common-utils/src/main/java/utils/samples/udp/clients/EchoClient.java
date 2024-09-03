package utils.samples.udp.clients;

import utils.StaticUtil;

import java.io.IOException;
import java.net.*;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public EchoClient() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }
    }

    public String sendEcho(String msg) {
        buf = msg.getBytes();
        System.out.printf("Client sending %d bytes\n", buf.length);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4_445);
        try {
            socket.send(packet);  // Outbound
            packet = new DatagramPacket(buf, buf.length);
            if (! "end".equals(msg)) {
                socket.receive(packet); // Inbound
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (! "end".equals(msg)) {
            String received = new String(packet.getData(), 0, packet.getLength());
            return received;
        } else {
            System.out.println("No wait.");
            return null;  // Do not wait for a reply...
        }
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
        EchoClient client  = new EchoClient();
        System.out.println("Send '.' to the server to stop it.");
        try {
            boolean keepWorking = true;
            while (keepWorking) {
                String request = StaticUtil.userInput("Request > ");
                if (".".equals(request)) {
                    keepWorking = false;
                    client.sendEcho("end");
                } else {
                    // Response works if server talks when told to.
                    // Continuous feed would not like this.
                    String response = client.sendEcho(request);
                    System.out.printf("Server responded %s\n", response.trim());
                }
            }
            client.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Bye-bye!");
    }
}
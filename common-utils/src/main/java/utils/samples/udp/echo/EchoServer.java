package utils.samples.udp.echo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class EchoServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public EchoServer() {
        try {
            socket = new DatagramSocket(4_445);
        } catch (SocketException se) {
            se.printStackTrace();
        }
    }

    public void run() {
        running = true;
        System.out.println("Server is up.");

        while (running) {
            for (int i=0; i<buf.length; i++) {
                buf[i] = (byte)0;
            }
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            byte[] ba = packet.getData();
            // Find first null character
            int len = 0;
            for (int i=0; i<packet.getLength(); i++) {
                if (ba[i] == 0) {
                    len = i;
                    break;
                } else {
                    len = i;
                }
            }
            String received = new String(packet.getData(), 0, len); // packet.getLength());

            System.out.printf("UDP Server received [%s], len:%d\n", received, packet.getLength());

            if (received.equals("end")) {
                System.out.println("Stop required by client.");
                running = false;
                continue;
            }
            try {
                socket.send(packet); // Send back the package received from the client
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        socket.close();
    }

    public static void main(String[] args) {
        // Ctrl-C to stop, or client sends "end"
        new EchoServer().start();
    }

}
package nmea.push2consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PushToTCP7002 {

    private static String server = "127.0.0.1"; // ""localhost"; // Could be an IP address
    private static int port = 7002;

    static class TCPTalker {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public void startConnection(String ip, int port) throws IOException {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public String sendMessage(String msg) throws IOException {
            out.println(msg);
            String resp = in.readLine();
            return resp;
        }

        public void stopConnection() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }
    }
    public static void main(String... args) {
        TCPTalker client = new TCPTalker();

        boolean OK = false;
        int nbTry = 0;
        while (!OK && nbTry < 10) {
            try {
                nbTry++;
                client.startConnection(server, port);
                OK = true;
            } catch (Exception ex) {
                System.err.printf("SocketThread port %d: %s\n", port, ex.getLocalizedMessage());
            }
        }

        if (OK) {
            try {
                String response = client.sendMessage("$GPWPL,3739.856,N,12222.812,W,OPMRNA*59");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                client.stopConnection();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Nope...");
        }
    }
}
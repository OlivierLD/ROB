package samples.janitors;

import nmea.consumers.client.Janitor;
import util.SimpleTCPClient;

import java.util.Properties;

public class TCPJanitorSample extends Janitor {

    @Override
    public void executeOnClose(Properties props) {
        // super.executeOnClose(props);
        System.out.println("---- Custom Janitor ----");
        // Expects properties 'tcp.server', 'tcp.port', 'tcp.command'
        String server = (String)props.get("tcp.server");
        String portStr = (String)props.get("tcp.port");
        String cmd = (String)props.get("tcp.command");

        System.out.printf("Sending TCP Request %s on %s:%s\n", cmd, server, portStr);

        try {
            SimpleTCPClient tcpClient = new SimpleTCPClient();
            tcpClient.startConnection(server, Integer.parseInt(portStr));
            tcpClient.sendMessage(cmd);
            tcpClient.stopConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("-------- Done -----------");
    }
}

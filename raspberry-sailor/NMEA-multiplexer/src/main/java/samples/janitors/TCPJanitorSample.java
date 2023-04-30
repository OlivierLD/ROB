package samples.janitors;

import nmea.consumers.client.Janitor;
import util.SimpleTCPClient;

import java.net.SocketException;
import java.util.Properties;

/**
 * This is a Janitor example.
 * Look into manual.md to know more about Janitor.
 * There is in this repo a RaspberryPythonServers/python.tcp.server.properties mentioning it, itself mentioned in
 * RaspberryPythonServers/nmea.mux.log.tcp-sensor.rest-actuator.yaml.
 * This one is sending a TCP command to some TCP server, to shut it down.
 *
 * The class below expects properties tcp.server, tcp.port, and tcp.command to be set.
 */
public class TCPJanitorSample extends Janitor {

    @Override
    public void executeOnClose(Properties props) {
        // super.executeOnClose(props);
        System.out.println("---- Custom Janitor ----");
        // Expects properties 'tcp.server', 'tcp.port', 'tcp.command'.
        String server = props.getProperty("tcp.server", "localhost");
        String portStr = props.getProperty("tcp.port", "7001");
        String cmd = props.getProperty("tcp.command", "EXIT"); // A bit gonfled...

        Thread janitorThread = new Thread(() -> {
            System.out.printf("Sending TCP Request %s on %s:%s\n", cmd, server, portStr);
            try {
                SimpleTCPClient tcpClient = new SimpleTCPClient();
                tcpClient.startConnection(server, Integer.parseInt(portStr));
                try {
                    String response = tcpClient.sendMessage(cmd);
                    System.out.printf("Command [%s] returned [%s]\n", cmd, response);
                    tcpClient.stopConnection();
                } catch (SocketException se) { // Could be dead already...
                    System.err.printf("Managed SocketException %s\n", se.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println("-------- Done -----------");
        }, "Janitor");
        janitorThread.start(); /// Et hop !
    }
}

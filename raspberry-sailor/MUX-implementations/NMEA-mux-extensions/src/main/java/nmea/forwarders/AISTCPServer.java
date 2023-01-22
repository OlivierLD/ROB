package nmea.forwarders;

import nmea.ais.AISParser;

/**
 * Example: How to override a Forwarder
 *
 * This one does re-broadcasts the AIS Sentences over TCP
 */
public class AISTCPServer extends TCPServer {

	public AISTCPServer(Integer port) throws Exception { // Required for dynamic load. 'int' won't work.
		super(port);
	}
	public AISTCPServer(int port) throws Exception {
		super(port);
	}

	@Override
	public void write(byte[] message) {
		String mess = new String(message).trim(); // trim removes \r\n
		if (mess.startsWith(AISParser.AIS_PREFIX)) {
			super.write(message);
		}
	}

}

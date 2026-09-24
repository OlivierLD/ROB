package nmea.api;

// import java.util.Properties;

import http.HTTPServer;
// import nmea.mux.GenericNMEAMultiplexer;

import java.util.List;

public interface Multiplexer {
	void onData(String mess);
	void setVerbose(boolean b);
	void setEnableProcess(boolean b);
	boolean getEnableProcess();
	void stopAll();
//	Properties getMuxProperties();
    List<HTTPServer.Operation> getRESTOperationList();
}
package nmea.computers;

import nmea.api.Multiplexer;
import nmea.forwarders.Forwarder;

import java.util.Properties;

public abstract class Computer implements Forwarder {

	private Multiplexer multiplexer;

	protected boolean verbose = false;
	protected boolean active = true;
	protected Properties props = null;
	protected String description;

	public Computer(Multiplexer mux){
		this.multiplexer = mux;
	}

	/**
	 * Broadcasts data to all forwarders and computers.
	 * @param mess
	 */
	protected synchronized void produce(String mess) {
		this.multiplexer.onData(mess);
	}

	@Override
	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	public void setProperties(Properties props) {
		this.props = props;
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	@Override
	public String getDescription() {
		return description;
	}
}
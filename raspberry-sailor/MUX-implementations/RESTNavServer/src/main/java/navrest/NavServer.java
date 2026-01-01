package navrest;

import astrorest.AstroRequestManager;
import gribprocessing.GRIBRequestManager;
import http.HTTPServer;
import imageprocessing.ImgRequestManager;
import nmea.api.Multiplexer;
import nmea.mux.GenericNMEAMultiplexer;
import tiderest.TideRequestManager;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Entry point. Very Simple.
 * The complexity is somewhere else.
 *
 * Gathers other REST Services, all in one place.
 * See RESTTideEngine, RESTNauticalAlmanac, etc.
 * See the addRequestManager invocations below.
 */
public class NavServer {

	private HTTPServer httpServer = null;
	private int httpPort = 9999;

	private final Multiplexer multiplexer;

	public NavServer() {

		boolean infraVerbose = "true".equals(System.getProperty("mux.infra.verbose", "true"));

		final Properties muxDefinitions = GenericNMEAMultiplexer.getDefinitions();

		String withHttpServer = muxDefinitions.getProperty("with.http.server", "no");
		if (withHttpServer.equals("no") || withHttpServer.equals("false")) {
			System.out.printf("(%s) Warning: WILL instantiate an HTTP Server (enforced)\n", this.getClass().getName());
			muxDefinitions.setProperty("with.http.server", "true");
		}
		String initCache = muxDefinitions.getProperty("init.cache", "no");
		if (initCache.equals("no") || initCache.equals("false")) {
			System.out.printf("(%s) Warning: WILL initialize the cache (enforced)\n", this.getClass().getName());
			muxDefinitions.setProperty("init.cache", "true");
		}

		String port = muxDefinitions.getProperty("http.port");
		if (port != null) {
			httpPort = Integer.parseInt(port);
		} else {
			port = System.getProperty("http.port");
			if (port != null) {
				try {
					httpPort = Integer.parseInt(port);
					System.out.printf("(%s) Will use HTTP Port %d (from -Dhttp.port)\n", this.getClass().getName(), httpPort);
				} catch (NumberFormatException nfe) {
					System.err.println(nfe.toString());
				}
			} else {
				System.out.printf("(%s) HTTP Port defaulted to %d\n", this.getClass().getName(), httpPort);
			}
		}

		System.out.printf("(%s) running on port %d\n", this.getClass().getName(), httpPort);
		this.httpServer = startHttpServer(httpPort, new NavRequestManager(this));

		// Add astronomical features...
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - adding AstroRequestManager\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
		this.httpServer.addRequestManager(new AstroRequestManager());
		// Add tide features...
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - adding TideRequestManager\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
		this.httpServer.addRequestManager(new TideRequestManager());
		// Add Nav features: Dead Reckoning, logging, re-broadcasting, from the NMEA Multiplexer
		Properties definitions = muxDefinitions; // GenericNMEAMultiplexer.getDefinitions();
		multiplexer = new GenericNMEAMultiplexer(definitions);
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - adding GenericNMEAMultiplexer\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
		this.httpServer.addRequestManager((GenericNMEAMultiplexer)multiplexer); // refers to nmea.mux.properties, unless -Dmux.properties is set
		// Add image processing service...
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - adding ImgRequestManager\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
		this.httpServer.addRequestManager(new ImgRequestManager());
		// Add GRIB features
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - adding GRIBRequestManager\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
		this.httpServer.addRequestManager(new GRIBRequestManager());
		if (infraVerbose) {
			System.out.printf("\t>> %s (%s) - End of NavServer constructor\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		}
	}

	public Multiplexer getMultiplexer() {
		return this.multiplexer;
	}

	protected List<HTTPServer.Operation> getAllOperationList() {
		return this.httpServer.getRequestManagers()
				.stream()
				.flatMap(requestManager -> requestManager.getRESTOperationList().stream())
				.collect(Collectors.toList());
	}

	public HTTPServer startHttpServer(int port, NavRequestManager requestManager) {
		HTTPServer newHttpServer = null;
		try {
			newHttpServer = new HTTPServer(port, requestManager);
			newHttpServer.startServer();
			System.out.printf("\t>> %s (%s) - Starting HTTP server\n",
					NumberFormat.getInstance().format(System.currentTimeMillis()),
					this.getClass().getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newHttpServer;
	}

	private static class MemoryGauge extends Thread {
		private final long pollingInterval;
		public MemoryGauge(long pollingInterval) {
			super();
			this.pollingInterval = pollingInterval;
		}
		@Override
		public void run() {
			boolean keepLooping = true;
			while (keepLooping) {
				try {
					Runtime runtime = Runtime.getRuntime();
					long memoryMax = runtime.maxMemory();
					System.out.printf("At %s:\n", new Date());
					System.out.printf("- Max Memory: %s bytes (%s Mb, %s Gb)\n",
							NumberFormat.getInstance().format(memoryMax),
							NumberFormat.getInstance().format(memoryMax / (1024L * 1024L)),
							NumberFormat.getInstance().format(memoryMax / (1024L * 1024L * 1024L)));
					long memoryUsed = runtime.totalMemory() - runtime.freeMemory(); // used = total - free.
					double memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;
					System.out.printf("- Used by program: %s bytes (%s Mb, %s Gb), %.02f %%\n",
							NumberFormat.getInstance().format(memoryUsed),
							NumberFormat.getInstance().format(memoryUsed / (1024L * 1024L)),
							NumberFormat.getInstance().format(memoryUsed / (1024L * 1024L * 1024L)),
							memoryUsedPercent);
					if (memoryUsedPercent > 50) { // Arbitrary 50%...
						System.out.println("===============================");
						System.out.println("-- Trying garbage collector...");
						System.gc();
						memoryUsed = runtime.totalMemory() - runtime.freeMemory(); // used = total - free.
						memoryUsedPercent = (memoryUsed * 100.0) / memoryMax;
						System.out.printf("-- After GC, used by program: %s bytes (%s Mb, %s Gb), %.02f %%\n",
								NumberFormat.getInstance().format(memoryUsed),
								NumberFormat.getInstance().format(memoryUsed / (1024L * 1024L)),
								NumberFormat.getInstance().format(memoryUsed / (1024L * 1024L * 1024L)),
								memoryUsedPercent);
						System.out.println("===============================");
					}
					try {
						Thread.sleep(pollingInterval);
					} catch (InterruptedException ie) {
						keepLooping = false;
						ie.printStackTrace();
					}
				} catch (Error error) {
					error.printStackTrace();
				}
			}
		}
	}

	public static void main(String... args) {
		new NavServer();
		// Display memory usage after startup,
		if (true) {
			MemoryGauge memoryGauge = new MemoryGauge(2 + 60 * 1_000); // 2 minutes
			memoryGauge.start();
		}
	}
}
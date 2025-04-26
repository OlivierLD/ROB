package context;

import nmea.parser.*;
import nmea.utils.MuxNMEAUtils;
import nmea.utils.NMEAUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationContext {

	private static ApplicationContext instance = null;
	NMEADataCache dataCache = null;

	private ApplicationContext() {
	}

	public NMEADataCache getDataCache() {
		if (this.dataCache != null) {
			GeoPos position = (GeoPos) this.dataCache.get(NMEADataCache.POSITION);
			if (position != null) {
				if (position.gridSquare == null || position.gridSquare.isEmpty()) {
					this.dataCache.put(NMEADataCache.POSITION, position.updateGridSquare());
				}
			}
		}
		return this.dataCache;
	}

	public static synchronized ApplicationContext getInstance() {
		if (instance == null) {
			instance = new ApplicationContext();
		}
		return instance;
	}

	public void initCache(String deviationFileName, // Default "zero-deviation.csv"
						  double maxLeeway,         // Default 0
						  double bspFactor,         // Default 1
						  double awsFactor,         // Default 1
						  double awaOffset,         // Default 0
						  double hdgOffset,         // Default 0
						  double defaultDeclination,// Default 0
						  int damping) {            // Default 1
		initCache(deviationFileName, maxLeeway, bspFactor, awsFactor, awaOffset, hdgOffset, defaultDeclination, damping, null);
	}

	public void initCache(String deviationFileName, // Default "zero-deviation.csv"
	                      double maxLeeway,         // Default 0
	                      double bspFactor,         // Default 1
	                      double awsFactor,         // Default 1
	                      double awaOffset,         // Default 0
	                      double hdgOffset,         // Default 0
	                      double defaultDeclination,// Default 0
	                      int damping,              // Default 1
						  List<String[]> markerFiles) {   // Default null TODO: routes ?

		dataCache = new NMEADataCache();

		if (markerFiles != null && markerFiles.size() > 0) {
			// Marker file list, marker list, border list
			List<String[]> markerList = new ArrayList<>();
			List<Marker> allMarkers = new ArrayList<>();
			List<Border> allBorders = new ArrayList<>();
			List<Route> allRoutes = new ArrayList<>();
			Yaml yaml = new Yaml(); // Yaml parser
			markerFiles.forEach(markers -> {
				if (!markers[0].trim().endsWith(".yaml") && !markers[0].trim().endsWith(".yml")) {
					System.err.printf("Markers and Borders file must be a yaml file, not %s\n", markers[0]);
					System.err.println("Moving on anyway, skipping this marker file.");
				} else {
					try {
						String description = "no description";
						InputStream inputStream = new FileInputStream(markers[0]);
						Map<String, Object> map = yaml.load(inputStream);
						if (map.get("markers") != null || map.get("borders") != null) { // Just to make sure...
							if (map.get("description") != null) {
								description = (String) map.get("description");
							}
						}
						markerList.add(new String[] { markers[0], description });
						allMarkers.addAll(NMEAUtils.loadMarkers(markers[0]));
						allBorders.addAll(NMEAUtils.loadBorders(markers[0]));
						Route route = NMEAUtils.loadRoute(markers[0], allMarkers);
						if (route.getWaypoints() != null && route.getWaypoints().size() > 0) {
							allRoutes.add(route);
						}
					} catch (Exception ex) { // File Not found ?
						System.err.printf("Building markers (%s)... \n%s\n", markers[0], ex);
						ex.printStackTrace();
					}
				}
			});
			dataCache.put(NMEADataCache.MARKERS_FILE, markerList);
			dataCache.put(NMEADataCache.MARKERS_DATA, allMarkers);
			dataCache.put(NMEADataCache.BORDERS_DATA, allBorders);
			dataCache.put(NMEADataCache.ROUTES_DATA, allRoutes);
		}

		try {
			List<double[]> deviationCurve = NMEAUtils.loadDeviationCurve(deviationFileName);
			dataCache.put(NMEADataCache.DEVIATION_FILE, deviationFileName);
			dataCache.put(NMEADataCache.DEVIATION_DATA, deviationCurve);
		} catch (Exception ex) {
			System.err.println("No deviation curve, sorry.");
			ex.printStackTrace();
			System.err.println("... moving on anyway.");
		}
		dataCache.put(NMEADataCache.MAX_LEEWAY, maxLeeway);

		dataCache.put(NMEADataCache.BSP_FACTOR, bspFactor);
		dataCache.put(NMEADataCache.AWS_FACTOR, awsFactor);
		dataCache.put(NMEADataCache.AWA_OFFSET, awaOffset);
		dataCache.put(NMEADataCache.HDG_OFFSET, hdgOffset);

		dataCache.put(NMEADataCache.DEFAULT_DECLINATION, new Angle180EW(defaultDeclination));
		dataCache.put(NMEADataCache.DAMPING, damping);
	}
}

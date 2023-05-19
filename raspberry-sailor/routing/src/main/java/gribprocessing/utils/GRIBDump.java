package gribprocessing.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jgrib.GribFile;
import jgrib.GribRecord;
import jgrib.GribRecordBDS;
import jgrib.GribRecordGDS;
import jgrib.GribRecordPDS;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;
import gribprocessing.data.GribDate;
import gribprocessing.data.GribType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class GRIBDump {

	private final static boolean verbose = "true".equals(System.getProperty("grib.verbose", "false"));

	private Map<GribDate, Map<GribType, Float[][]>> gribDataMap = null;
	private List<String> feedback = null;

	public GRIBDump() {
		super();
	}

	public final Map<GribDate, Map<GribType, Float[][]>> dump(GribFile gribFile) {
		feedback = new ArrayList<>(1);
		try {
//    GribPDSParamTable.turnOffJGRIBLogging();
			gribDataMap = new HashMap<>();

			for (int i = 0; i < gribFile.getLightRecords().length; i++) {
				try {
					GribRecord gr = new GribRecord(gribFile.getLightRecords()[i]);
					GribRecordPDS grpds = gr.getPDS(); // Headers and Data
					GribRecordGDS grgds = gr.getGDS(); // Boundaries and Steps
					GribRecordBDS grbds = gr.getBDS(); // TASK get min and max from this one.

					Date date = grpds.getGMTForecastTime().getTime();

					int width = grgds.getGridNX();
					int height = grgds.getGridNY();
					double stepX = grgds.getGridDX();
					double stepY = grgds.getGridDY();
					double top = Math.max(grgds.getGridLat1(), grgds.getGridLat2());
					double bottom = Math.min(grgds.getGridLat1(), grgds.getGridLat2());
					double left = Math.min(grgds.getGridLon1(), grgds.getGridLon2());
					double right = Math.max(grgds.getGridLon1(), grgds.getGridLon2());

					String type = grpds.getType();
					String description = grpds.getDescription();
					String unit = grpds.getUnit();

					if (right - left > 180) { // then swap. like left=-110, right=130
						double tmp = right;
						right = left;
						left = tmp;
					}

					GribDate gDate = new GribDate(date, height, width, stepX, stepY, top, bottom, left, right);

					Float[][] data = new Float[height][width];
					float val = 0F;
					for (int col = 0; col < width; col++) {
						for (int row = 0; row < height; row++) {
							try {
								val = gr.getValue(col, row);
								if (val > grbds.getMaxValue() || val < grbds.getMinValue()) {
									if (verbose) {
										System.out.println("type:" + type + " val:" + val + " is out of [" + grbds.getMinValue() + ", " + grbds.getMaxValue() + "]");
									}
									val = grbds.getMinValue(); // TODO Make sure that's right...
								}
								data[row][col] = val;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}

					Map<GribType, Float[][]> subMap = gribDataMap.get(gDate);
					// Ugly trick. With a GribDate extends Date, it works OK.
					// But otherwise, the gribDataMap.get(gDate) does not return the expected map.
					// The key and the gDate are NOT 'equals'
					// >> Fixed by implementing a hashCode() and a equals() methods in GribDate...
					if (false && subMap == null) {
						final Object[] keys = gribDataMap.keySet().toArray();
						for (Object key : keys) {
							GribDate keyDate = (GribDate) key;
							if (keyDate.getDate().equals(gDate.getDate())) {
								subMap = gribDataMap.get(keyDate);
								break;
							}
						}
					}
//					gribDataMap.keySet().forEach(keyDate -> {
//						if (keyDate.getDate().equals(gDate.getDate())) {
//							System.out.printf(">> Found existing map for %s\n", keyDate.getFormattedUTCDate());
//						}
//					});
					if (subMap == null) {
						subMap = new HashMap<>();
					}
					subMap.put(new GribType(type, description, unit, grbds.getMinValue(), grbds.getMaxValue()), data);
					gribDataMap.put(gDate, subMap);
					if (verbose) {
						System.out.printf("Adding data for type %s to date %s\n", type, gDate);
					}
				} catch (NoValidGribException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NotSupportedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			feedback.add(ex.toString());
		}
		return gribDataMap;
	}

	public void dumpFeedback() {
		if (feedback != null) {
			feedback.forEach(System.out::println);
		}
	}

	/**
	 * For Gson to work properly... TODO Check if needed with Jackson
	 *
	 * @param data a Map
	 * @return a list
	 */
	public static List<DatedGRIB> expandGrib(Map<GribDate, Map<GribType, Float[][]>> data) {
		List<DatedGRIB> grib = new ArrayList<>();
		// Sort by date
		SortedSet<GribDate> ss = new TreeSet<>(data.keySet());
		for (GribDate gribDate : ss) {
			Map<GribType, Float[][]> typeMaps = data.get(gribDate);
			DatedGRIB datedGRIB = new DatedGRIB();
			datedGRIB.gribDate = gribDate;
			datedGRIB.typedData = new ArrayList<>();

			for (GribType type : typeMaps.keySet()) {
				GRIBTypedData typedData = new GRIBTypedData();
				typedData.gribType = type;
				typedData.data = typeMaps.get(type);
				datedGRIB.getTypedData().add(typedData);
			}
			grib.add(datedGRIB);
		}
		return grib;
	}

	public static class GRIBTypedData {
		GribType gribType;
		Float[][] data;

		public GribType getGribType() {
			return gribType;
		}

		public Float[][] getData() {
			return data;
		}
	}

	public static class DatedGRIB {
		GribDate gribDate;
		List<GRIBTypedData> typedData;

		public GribDate getGribDate() {
			return gribDate;
		}

		public List<GRIBTypedData> getTypedData() {
			return typedData;
		}
	}

	public List<DatedGRIB> getExpandedGRIB(GribFile gf) {
		return expandGrib(dump(gf));
	}

	// For standalone tests
	public static void main(String... args) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		GRIBDump gribDump = new GRIBDump();
//	"GRIB_2017_10_16_07_31_47_PDT.grb", "GRIB_2009_02_25_Sample.grb";
		String gribFileName = // "GRIB_2009_02_25_Sample.grb";
		                      "/Users/olivierlediouris/repos/ROB/raspberry-sailor/RESTRouting/GRIB_2009_02_25_Sample.grb";
//		String gribFileName = "grib.grb";
		URL gribURL = new File(gribFileName).toURI().toURL();
		GribFile gf = new GribFile(gribURL.openStream());
		final List<DatedGRIB> expandedGRIB = gribDump.getExpandedGRIB(gf);

		System.out.printf("expandedGRIB has %d entries\n", expandedGRIB.size());

		if (expandedGRIB.size() > 0) {
			final GribDate firstGribDate = expandedGRIB.get(0).getGribDate();
			String firstJsonDate = mapper.writeValueAsString(firstGribDate);
			System.out.println(firstJsonDate);
		}

		String dump = mapper.writeValueAsString(expandedGRIB);
		System.out.println(dump);

		if (verbose) {
			System.out.println("Done:");
			gribDump.dumpFeedback();
		}
	}
}

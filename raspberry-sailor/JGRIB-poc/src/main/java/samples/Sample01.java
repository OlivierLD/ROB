package samples;

import com.fasterxml.jackson.databind.ObjectMapper;
import jgrib.GribFile;
import jgrib.NoValidGribException;
import jgrib.NotSupportedException;

import java.io.IOException;
import java.text.NumberFormat;

public class Sample01 {

	private final static ObjectMapper mapper = new ObjectMapper();

	public static void main(String... args) {
		String fName = "samples/GRIB_2017_10_16_07_31_47_PDT.grb";
		// String fName = "grib.grb";
		try {
			long before = System.currentTimeMillis();
			GribFile gribFile = new GribFile(fName);
			long after = System.currentTimeMillis();
			long duration1 = (after - before);
			before = System.currentTimeMillis();
			String content = mapper.writeValueAsString(gribFile);
			after = System.currentTimeMillis();
			long duration2 = (after - before);
			System.out.println(content);

			System.out.println(String.format("Parsed in %s ms, into JSON in %s ms",
					NumberFormat.getInstance().format(duration1),
					NumberFormat.getInstance().format(duration2)));

			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		} catch (NoValidGribException e) {
			e.printStackTrace();
		}
	}
}

package nmea.forwarders;

import nmea.parser.StringParsers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * for forwarder.XX.type=file
 */
public class DataFileWriter implements Forwarder {
	private BufferedWriter dataFile;
	private String log;
	private final boolean append;
	private final boolean timeBased;
	private final String radix;
	private String dir;
	private String split;
	private final boolean flush;
	private long timeSplitThreshold = 0L;

	private final static long MIN_MS  = 60 * 1_000;
	private final static long HOUR_MS = 60 * MIN_MS;
	private final static long DAY_MS  = 24 * HOUR_MS;
	private final static long WEEK_MS =  7 * DAY_MS;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // Use UTC time!!
	static {
		SDF.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	private enum Split {
		min, hour, day, week, month, year
	}

	private List<String> filters = null;

	public DataFileWriter(String fName) throws Exception {
		this(fName, false);
	}
	public DataFileWriter(String fName, boolean append) throws Exception {
		this(fName, append, false, null, null, null, false, null);
	}
	public DataFileWriter(String fName, boolean append, boolean flush) throws Exception {
		this(fName, append, false, null, null, null, flush, null);
	}

	public DataFileWriter(String fName, boolean append, boolean timeBased, String radix, String dir, String split, boolean flush) throws Exception {
		this(fName, append, timeBased, radix, dir, split, flush, null);
	}
	public DataFileWriter(String fName, boolean append, boolean timeBased, String radix, String dir, String split, boolean flush, String sentenceFilters) throws Exception {
		System.out.printf("- Start writing to %s, %s \n", this.getClass().getName(), fName);

		if (sentenceFilters != null) {
			if (sentenceFilters.trim().length() > 0) {
				filters = Arrays.asList(sentenceFilters.trim().split(","))
						.stream()
						.map(String::trim)
						.collect(Collectors.toList());

			}
		}
		this.log = fName;
		this.append = append;
		this.timeBased = timeBased;
		this.radix = radix;
		this.dir = dir;
		this.flush = flush;
		if (this.timeBased) { // Then add a subdirectory, based on the time the logging was started. Each new log series is in its own folder.
			String subDirName = SDF.format(new Date());
			this.dir += (File.separator + subDirName);
		}
		if (split != null) {
			Optional<Split> foundSplit = Arrays.stream(Split.values()).filter(val -> val.toString().equals(split)).findFirst();
			if (foundSplit.isPresent()) {
				this.split = foundSplit.get().toString();
			} else {
				throw new RuntimeException(String.format("Invalid Split value [%s]", split));
			}
		}
		if (this.timeBased) {
			this.log = generateFileName();
			if (this.split != null) {
				this.timeSplitThreshold = nextSplit();
			}
		}
		try {
			this.dataFile = new BufferedWriter(new FileWriter(this.log, this.append));
		} catch (Exception ex) {
			System.err.printf("When creating [%s]\n", this.log);
			throw ex;
		}
	}

	boolean VERBOSE = false;

	@Override
	public void write(byte[] message) {
		try {
			String mess = new String(message).trim(); // trim removes \r\n
			boolean ok = true;
			if (mess.startsWith("$") && mess.length() > 6 && filters != null) {
				ok = false;
				String key = mess.substring(3, 6);
				for (String filter : filters) {
					if (!filter.startsWith("~")) { // include
						if (filter.equals(key)) {
							ok = true;
							if (VERBOSE) {
								System.out.printf("DataFileWriter >> Including [%s] (%s), %s\n", key, StringParsers.findDispatcherByKey(key).description(), mess);
							}
						}
					} else {  // exclude
						if (filter.substring(1).equals(key)) { // Don't !
							ok = false;
							if (VERBOSE) {
								System.out.printf("DataFileWriter >> Excluding [%s] (%s), %s\n", key, StringParsers.findDispatcherByKey(key).description(), mess);
							}
							break;
						} else {
							ok = true;
						}
					}
				}
			}
			if (!mess.isEmpty() && ok) {
				this.dataFile.write(mess + '\n');
				if (this.flush) {
					this.dataFile.flush();
				}
				if (this.timeBased) {
					long now = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC")).getTimeInMillis();
					if (this.split != null && now > this.timeSplitThreshold) {
						this.dataFile.close();
						this.log = generateFileName();
						try {
							this.dataFile = new BufferedWriter(new FileWriter(this.log, this.append));
						} catch (Exception ex) {
							System.err.printf("When creating [%s]\n", this.log);
							throw ex;
						}
						this.timeSplitThreshold = nextSplit();
//					} else {
//						System.out.println(String.format("Keep going %d < %d, %s < %s",
//								now,
//								this.timeSplitThreshold,
//								SDF.format(new Date(now)),
//								SDF.format(new Date(this.timeSplitThreshold))));
					}
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.dataFile.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getLog() {
		return this.log;
	}

	private String generateFileName() {
		if (this.dir != null) {
			File logDir = new File(this.dir);
			if (!logDir.exists()) {
				logDir.mkdirs();
			}
		}
		String newFileName = (this.dir == null ? "" : this.dir + File.separator) + SDF.format(new Date()) + "_UTC" + (this.radix == null ? "" : this.radix) + ".nmea";
		return newFileName;
	}

	private long nextSplit() {
		if (this.split == null) {
			return 0L;
		}
		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
//	System.out.println(">> NextSplit, now " + SDF.format(new Date(now.getTimeInMillis())));
		Calendar today00 = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0);
		today00.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
		long timeSplitThreshold = today00.getTimeInMillis();
		switch (this.split) {
			case "min":
				Calendar todayThisMinute = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
				todayThisMinute.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
				todayThisMinute.add(Calendar.MINUTE, 1);
				timeSplitThreshold = todayThisMinute.getTimeInMillis();
				break;
			case "hour":
				Calendar todayThisHour = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY), 0);
				todayThisHour.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
				todayThisHour.add(Calendar.HOUR, 1);
				timeSplitThreshold = todayThisHour.getTimeInMillis();
				break;
			case "day":
				timeSplitThreshold += DAY_MS;
				break;
			case "week":
				timeSplitThreshold += WEEK_MS;
				break;
			case "month":
				Calendar today1st = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1);
				today1st.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
				today1st.add(Calendar.MONTH, 1);
				timeSplitThreshold = today1st.getTimeInMillis();
				break;
			case "year":
				Calendar todayJan1st = new GregorianCalendar(now.get(Calendar.YEAR), Calendar.JANUARY, 1);
				todayJan1st.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
				todayJan1st.add(Calendar.YEAR, 1);
				timeSplitThreshold = todayJan1st.getTimeInMillis();
				break;
			default:
				break;
		}
//		{
//			Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
//			cal.setTimeInMillis(timeSplitThreshold);
//			Date d = new Date(cal.getTimeInMillis());
//			System.out.println(">> NextSplit, now + " + this.split + " >> " + SDF.format(d));
//		}
		return timeSplitThreshold;
	}

	public static class DataFileBean {
		private String cls;
		private String log;
		private boolean append;
		private boolean timeBased;
		private String radix;
		private String dir;
		private String split;
		private boolean flush;
		private List<String> filters;


		private final String type = "file";

		public DataFileBean() {}   // This is for Jackson
		public DataFileBean(DataFileWriter instance) {
			cls = instance.getClass().getName();
			log = instance.log;
			append = instance.append;
			timeBased = instance.timeBased;
			radix = instance.radix;
			dir = instance.dir;
			split = instance.split;
			flush = instance.flush;
			filters = instance.filters;
		}

		public String getCls() {
			return cls;
		}

		public boolean isAppend() {
			return append;
		}

		public String getType() {
			return type;
		}

		public String getLog() {
			return log;
		}

		public boolean append() { return append; } // Any useful ?

		public boolean isTimeBased() {
			return timeBased;
		}

		public String getRadix() {
			return radix;
		}

		public String getDir() {
			return dir;
		}

		public String getSplit() {
			return split;
		}

		public boolean isFlush() {
			return flush;
		}

		public List<String> getFilters() {
			return filters;
		}
	}

	@Override
	public Object getBean() {
		return new DataFileBean(this);
	}

	@Override
	public void setProperties(Properties props) {
	}
}

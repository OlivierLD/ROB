package nmea.forwarders;

import nmea.parser.StringParsers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * <b>For dynamic loading</b> (non-standard, as an example)<br/>
 * --------------------------<br/>
 * <ul>
 * <li>Requires a file like <code>sqlite.properties</code> to provide the broker url.</li>
 * <li>Requires a table <code>NMEA_DATA</code> created like this:<br/>
 * <pre>
 * CREATE TABLE NMEA_DATA(
 * 		id INTEGER PRIMARY KEY AUTOINCREMENT,
 * 		sentence_id VARCHAR2(3),
 * 		data VARCHAR2,
 * 		date DATETIME
 * );
 * </pre>
 * </li>
 * <li>See the script <code>sql/nmea.sql</code></li>
 * </ul>
 *
 * Requires the following dep in gradle:<br/>
 * <code>implementation 'org.xerial:sqlite-jdbc:3.34.0'</code>
 * <br/>
 * SQLite doc <a href="https://sqlite.org/lang_select.html">here</a>.
 * <br/>
 * --------------------------<br/>
 */
public class SQLitePublisher implements Forwarder {
	private Connection dbConnection = null;
	private String dbURL;

	private Properties props;

	public SQLitePublisher() {
		super();
	}

	/*
	 * dbURL like jdbc:sqlite:/path/to/db.db
	 */
	private void initConnection() throws Exception {
		if (this.props == null) {
			throw new RuntimeException("Need props!");
		}
		String dbUrl = this.props.getProperty("db.url");
		if (dbUrl == null) {
			throw new RuntimeException("No db.url found in the props...");
		}
		this.dbURL = dbUrl;

		try {

			this.dbConnection = DriverManager.getConnection(dbURL);
			if ("true".equals(props.getProperty("verbose"))) {
				DatabaseMetaData dm = dbConnection.getMetaData();
				System.out.println("Driver name: " + dm.getDriverName());
				System.out.println("Driver version: " + dm.getDriverVersion());
				System.out.println("Product name: " + dm.getDatabaseProductName());
				System.out.println("Product version: " + dm.getDatabaseProductVersion());
			}

		} catch (Exception e) {
			System.err.println(("Argh!"));
			throw e;
		}
	}

	public String getDbURL() {
		return this.dbURL;
	}

	@Override
	public void write(byte[] message) {

		if (this.dbConnection == null) {
			try {
				initConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			String mess = new String(message);
			if (!mess.isEmpty()) {
				String sentenceId = StringParsers.getSentenceID(mess);
				String SQLStatement = String.format(
						"INSERT INTO NMEA_DATA (sentence_id, data, date) VALUES (\"%s\", \"%s\", datetime(\"now\"))",
						sentenceId, mess);
				// TODO More verbose?
				Statement statement = this.dbConnection.createStatement();
				statement.executeUpdate(SQLStatement);
				statement.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			this.dbConnection.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class SQLiteBean {
		private String cls;
		private String dbURL;
		private String type = "sqlite";

		public SQLiteBean() {}   // This is for Jackson
		public SQLiteBean(SQLitePublisher instance) {
			cls = instance.getClass().getName();
			dbURL = instance.dbURL;
		}

		public String getCls() {
			return cls;
		}

		public String getType() {
			return type;
		}

		public String getDbURL() {
			return dbURL;
		}
	}

	@Override
	public Object getBean() {
		return new SQLiteBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}

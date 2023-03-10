--
-- Create the db for the SQLite Publisher
-- Use:
-- $ # From NMEA-multiplexer, to re-init the table:
-- $ sqlite3 nmea.db < sql/nmea.sql
--
DROP TABLE IF EXISTS NMEA_DATA;
CREATE TABLE NMEA_DATA(id INTEGER PRIMARY KEY AUTOINCREMENT, sentence_id VARCHAR2(3), data VARCHAR2, date DATETIME);

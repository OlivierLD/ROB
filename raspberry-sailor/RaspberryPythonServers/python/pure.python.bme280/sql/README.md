# Log the data in a SQLite Database

## SQLite on the Raspberry Pi
Details, ideas, examples [here](../../../../NMEA-multiplexer/sql/SQLITE.md).

---

Execute a script from the command line:
```
$ sqlite3 oliv.db < oliv.sql
```
Execute a script from the sqlite3:
```
$ sqlite3 oliv.db
sqlite> .read oliv.sql
```
--- 

First, to create the database and its required tables:
```
$ sqlite3 weather.db < create.db.sql
$ 
```

## From Python
- See <https://docs.python.org/3/library/sqlite3.html>.


#
# More here: https://docs.python.org/3/library/sqlite3.html
#
import sqlite3

con: sqlite3.Connection = sqlite3.connect("weather.db")
print(f"Connection is a {type(con)}")
cur: sqlite3.Cursor = con.cursor()
print(f"Cursor is a {type(cur)}")

prmsl: float = 1013.15;
sql_stmt: str = f'insert into WEATHER_DATA (type, data_date, value) VALUES ("PRMSL", datetime("now"), {prmsl});'

try:
    cur.execute(sql_stmt)
except sqlite3.OperationalError as DBException:
    print(f">> Oops: {DBException}")
except Exception as exception:
    print(f"Exception {type(exception)} : {exception}")

con.commit()

con.close()

print("We're off!")
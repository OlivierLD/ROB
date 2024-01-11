#
# More here: https://docs.python.org/3/library/sqlite3.html
#
import sqlite3

con = sqlite3.connect("weather.db")
cur = con.cursor()

prmsl: float = 1013.15;
sql_stmt: str = f'insert into WEATHER_DATA (type, data_date, value) VALUES ("PRMSL", datetime("now"), {prmsl});'

try:
    cur.execute(sql_stmt)
except Exception as exception:
    print(exception)

con.commit()

con.close()

print("We're off!")
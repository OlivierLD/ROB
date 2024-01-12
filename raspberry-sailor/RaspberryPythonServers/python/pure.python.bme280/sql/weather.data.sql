--
-- See logged data
--
.mode columns
.headers on
SELECT "Weather Data:";
SELECT COUNT(DISTINCT DATA_DATE) AS "Nb Dates" FROM WEATHER_DATA;
--
SELECT * FROM WEATHER_DATA ORDER BY DATA_DATE, TYPE;
--
.width 30 20 16
SELECT B.DESCRIPTION AS "Data",
       A.DATA_DATE AS "Date",
       printf("%.2f", A.VALUE) || ' ' || B.UNIT AS "Value"
FROM WEATHER_DATA A,
     DATA_TYPES B
WHERE A.TYPE = B.NAME
ORDER BY A.DATA_DATE, A.TYPE;

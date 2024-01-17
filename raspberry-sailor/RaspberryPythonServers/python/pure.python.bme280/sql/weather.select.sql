--
-- See logged data
-- Date functions: https://www.sqlite.org/lang_datefunc.html
--
.mode columns
.headers on
SELECT COUNT(DISTINCT DATA_DATE) AS "Nb Measures" FROM WEATHER_DATA;
--
.width 30 40 16
SELECT A.DATA_DATE || ' UTC' AS "Date",
       A.TYPE || ' - ' || B.DESCRIPTION AS "Data",
       printf("%.2f", A.VALUE) || ' ' || B.UNIT AS "Value"
FROM WEATHER_DATA A,
     DATA_TYPES B
WHERE A.TYPE = B.NAME
ORDER BY A.DATA_DATE, A.TYPE;

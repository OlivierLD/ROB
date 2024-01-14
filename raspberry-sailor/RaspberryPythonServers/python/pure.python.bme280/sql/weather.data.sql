--
-- See logged data
-- Date functions: https://www.sqlite.org/lang_datefunc.html
-- Degree sign: chr(186)
--
.mode columns
.headers on
SELECT "Weather Data:";
SELECT 'On ' || COUNT(DISTINCT(date(DATA_DATE))) || ' day(s)' AS "Fork in Days" FROM WEATHER_DATA;
SELECT COUNT(DISTINCT DATA_DATE) AS "Nb Dates" FROM WEATHER_DATA;
--
SELECT * FROM WEATHER_DATA ORDER BY DATA_DATE, TYPE;
--
.width 30 40 16
SELECT A.DATA_DATE || ' UTC' AS "Date",
       A.TYPE || ' - ' || B.DESCRIPTION AS "Data",
       printf("%.2f", A.VALUE) || ' ' || B.UNIT AS "Value"
FROM WEATHER_DATA A,
     DATA_TYPES B
WHERE A.TYPE = B.NAME
ORDER BY A.DATA_DATE, A.TYPE;
--
-- DECODE sample: use CASE
-- Decode on column exact value
--
SELECT TYPE,
       CASE TYPE
           WHEN 'RH' THEN 'REL Hum'
           WHEN 'AT' THEN 'Air Temp'
           ELSE TYPE
       END DataType
FROM
    WEATHER_DATA
ORDER BY
    DATA_DATE,
    TYPE;
--
SELECT printf("%.2f", VALUE) || char(186) || 'C' FROM WEATHER_DATA WHERE TYPE = 'AT';
--
-- Decode on boolean expression (no VALUE after CASE)
--
SELECT
    CASE
        WHEN VALUE > 21.0 THEN 'Fait chaud'
        WHEN VALUE < 20.0 THEN 'Ca caille'
        ELSE printf("%.2f", VALUE) || char(186) || 'C'
    END AirTemp
FROM WEATHER_DATA
WHERE TYPE = 'AT'
ORDER BY DATA_DATE;

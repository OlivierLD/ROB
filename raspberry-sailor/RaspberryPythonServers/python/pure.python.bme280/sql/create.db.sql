--
-- https://sqlite.org/docs.html
--
-- Create data-type table
CREATE TABLE DATA_TYPES (
    name varchar2 PRIMARY KEY,
    unit VARCHAR2 not null,
    description VARCHAR2
);
-- Create data table
CREATE TABLE WEATHER_DATA (
    type varchar2,
    data_date datetime not null,
    value numeric not null,
    constraint pk_data primary key (type, data_date),
    constraint data_fk_type foreign key (type) references data_types(name) on delete cascade
);

-- Init types
BEGIN;
INSERT INTO DATA_TYPES (name, unit, description) VALUES ("PRMSL", "hPa",     "Pressure at Mean Sea Level");
INSERT INTO DATA_TYPES (name, unit, description) VALUES ("AT",    "Celsius", "Air Temperature");
INSERT INTO DATA_TYPES (name, unit, description) VALUES ("RH",    "%",       "Relative Humidity");
INSERT INTO DATA_TYPES (name, unit, description) VALUES ("DEW-P", "Celsius", "Dew Point");
INSERT INTO DATA_TYPES (name, unit, description) VALUES ("AH",    "g/m3",    "Absolute Humidity");
COMMIT;

-- Example...
BEGIN;
INSERT INTO WEATHER_DATA (type, data_date, value) VALUES ("PRMSL", datetime("now"), 1013.15);
.mode columns
.headers on
SELECT "Data inserted, as an example:";
SELECT * FROM WEATHER_DATA;
-- COMMIT;
ROLLBACK;


-- for sqlite3
-- use:
-- sqlite3 sql/tides.db < sql/create_tables.sql
-- or
-- sqlite3 db/tides.db
-- sqlite> .read sql/create_tables.sql
--------------------------------------------
--
-- drop tables first
--
drop table equilibriums;
drop table nodefactors;
drop table speedconstituents;
drop table stationdata;
drop table stations;
drop table coeffdefs;
--
select 'All tables dropped';

--
-- create tables
--
create table coeffdefs (
  rank integer not null primary key,
  name varchar(64) not null,
  constraint uk_name_coeffedefs unique (name)
);
--
create table speedconstituents (
  coeffname varchar(64) primary key,
  coeffvalue numeric not null,
  constraint speed_fk_coeff foreign key (coeffname) references coeffdefs(name) on delete cascade
);
--
create table equilibriums (
  coeffname varchar(64) not null,
  year integer not null,
  value numeric not null,
  constraint pk_equilibriums primary key (coeffname, year),
  constraint equilibriums_fk_speedconstituents foreign key (coeffname) references speedconstituents(coeffname) on delete cascade
);
--
create table nodefactors (
  coeffname varchar(64) not null,
  year integer not null,
  value numeric not null,
  constraint pk_nodefactors primary key (coeffname, year),
  constraint nodefactors_fk_speedconstituents foreign key (coeffname) references speedconstituents(coeffname) on delete cascade
);
--
create table stations (
  name varchar(128) primary key,
  latitude numeric not null,
  longitude numeric not null,
  tzOffset varchar(64) not null,
  tzName varchar(64) not null,
  baseheightvalue numeric not null,
  baseheightunit varchar(16) not null
);
--
create table stationdata (
  stationname varchar(128),
  coeffname varchar(64),
  amplitude numeric not null,
  epoch numeric not null,
  constraint pk_stationdata primary key (stationname, coeffname),
  constraint stationdata_fk_coeff foreign key (coeffname) references coeffdefs(name) on delete cascade,
  constraint stationdata_fk_station foreign key (stationname) references stations(name) on delete cascade
);
--
select 'All tide tables created';
--
-- Done
--
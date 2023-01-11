#!/bin/bash
#
# Reads NMEA log file, and produces a JSON with twa, tws, bsp.
#
echo -e "Usage is:"
echo -e "${0} --from:data.nmea --to:output.json"
echo -e " or"
echo -e "${0} --from-zip:archive.zip --path-in-zip:path/to/data.nmea --to:output.json"
echo -e "-------------------"
#
# To list a zip content:
# $ unzip -vl file.zip
#
# in sample-data/logged.data.zip:
# - 2010-07-10.tacking.back.in.nmea
# - 2010-11-08.Nuku-Hiva-Tuamotu.nmea
# - 2010-11.03.Taiohae.nmea
# - 2011-01-29.strait.to.tongareva.for.DR.small.nmea
# - 2011-01-29.strait.to.tongareva.for.DR.txt.nmea
# - 2012-06-10.china.camp-oyster.point.nmea
# - 2012-08-12.OysterPoint.ChinaCamp.valid.nmea
# - 2014-08-30.headless.labor.day.week.end.sail.only.nmea
# - 2014-09-01.headless.labor.day.week.end.anchored.nmea
# - 2014-09-01.headless.labor.day.week.end.nmea
# - 2017.06.10.nmea
# - 2017.06.17.nmea
# and more...
#
# By default, no 0-BSP data is recorded.
SKIP_NO_BSP=
# SKIP_NO_BSP="-Dskip.bsp.00=false"  # Keep record with BSP = 0
#
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
java -cp ${CP} ${SKIP_NO_BSP} util.LogToPolarPoints $*

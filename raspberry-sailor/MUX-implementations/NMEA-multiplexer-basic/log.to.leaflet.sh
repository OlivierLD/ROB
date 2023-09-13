#!/usr/bin/env bash
#
# Suitable for LeafLet. Spits out positions in JSON format, and much more.
# Can be used to find boat's parameters, like max-leeway.
#
if [[ $# == 0 ]]; then
  echo -e "Several CLI prms are available:"
  echo -e "--file-name:2010-11-08.Nuku-Hiva-Tuamotu.nmea"
  echo -e "--archive-name:sample-data/logged.data.archive.zip"
  echo -e "--dev-curve:dp_2011_04_15.csv"
  echo -e "--polar-file:CheoyLee42.polar-coeff"
  echo -e "--max-leeway:10"
  echo -e "--current-buffer-length:600000"
  echo -e "--output-file-name:/path/to/output.json"
  echo -e "example: ${0} --file-name:sample-data/2010-11.03.Taiohae.nmea"
  echo -e "-> We need at least the --file-name:xxxx "
  echo -e "-> If data are only logged from a GPS, consider using log.to.json.sh"
  exit 1
fi
#
# --file-name:2010-11-08.Nuku-Hiva-Tuamotu.nmea --archive-name:/Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/sample-data/logged.data.archive.zip --dev-curve:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv --polar-file:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff --current-buffer-length:600000
# --file-name:2010-07-10.tacking.back.in.nmea --archive-name:/Users/olivierlediouris/repos/ROB/raspberry-sailor/NMEA-multiplexer/sample-data/logged.data.zip --dev-curve:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/dp_2011_04_15.csv --polar-file:/Users/olivierlediouris/repos/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers/sample.data/polars/CheoyLee42.polar-coeff --current-buffer-length:600000
#
CP=$(dirname $0)/build/libs/*.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dminified=false"
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Doffset=484"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dlimit=179"
#
# java ${JAVA_OPTIONS} -cp ${CP} util.NMEAtoJSONPosPlus $*
COMMAND="java ${JAVA_OPTIONS} -cp ${CP} util.NMEAtoJSONPosPlus $*"
echo -e "Running ${COMMAND}"
${COMMAND}
#

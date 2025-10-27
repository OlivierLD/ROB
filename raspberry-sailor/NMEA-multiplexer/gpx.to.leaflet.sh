#!/usr/bin/env bash
#
# Suitable for LeafLet. Spits out positions in JSON format, and much more.
# Can be used to find boat's parameters, like max-leeway.
# Requires GPS and boat data (BSP, current, Wind, etc). Otherwise, use log.to.json.sh.
#
if [[ $# == 0 ]]; then
  echo -e "Several CLI prms are available:"
  echo -e "--file-name:2010-11-08.Nuku-Hiva-Tuamotu.nmea"
  echo -e "--output-file-name:/path/to/output.json"
  echo -e "example: ${0} --file-name:sample-data/2010-11.03.Taiohae.gpx"
  echo -e "-> We need at least the --file-name:xxxx "
  exit 1
fi
#
CP=$(dirname $0)/build/libs/NMEA-multiplexer-1.0-all.jar
# CP=$(dirname $0)/build/libs/*.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dminified=false"
java ${JAVA_OPTIONS} -cp ${CP} util.GPXtoJSONPosPlus $*
#
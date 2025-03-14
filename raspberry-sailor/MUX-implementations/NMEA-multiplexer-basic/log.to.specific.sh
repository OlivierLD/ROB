#!/usr/bin/env bash
#
# Specific transformer, an example.
# Spits out rmctime and prmsl in JSON format.
#
if [[ $# != 1 ]]; then
  echo -e "Usage is ${0} [log.file.name]"
  echo -e "example: ${0} sample-data/2010-11.03.Taiohae.nmea"
  exit 1
fi
#
CP=$(dirname $0)/build/libs/*.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=true"
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dminified=false"
java ${JAVA_OPTIONS} -cp ${CP} util.NMEAtoJSONTimeAndPRMSL "$1"
#

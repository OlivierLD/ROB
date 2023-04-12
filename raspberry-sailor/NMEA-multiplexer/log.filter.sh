#!/usr/bin/env bash
#
# Filter the log, on talker or sentence IDs.
#
if [[ "$1" == "" ]]; then
  echo -e "Usage is:"
  echo -e "$0 --input-data-file:original.nmea --output-data-file:filtered.nmea --include-talkers:GP --exclude-sentences:TXT,VTG"
  echo -e "$0 --help"
  exit 0
fi
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# sudo java ${JAVA_OPTIONS} $LOGGING_FLAG $JFR_FLAGS $REMOTE_DEBUG_FLAGS -cp ${CP} nmea.mux.GenericNMEAMultiplexer
java ${JAVA_OPTIONS} -cp ${CP} util.LogFilter $*
#

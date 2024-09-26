#!/usr/bin/env bash
#
# Markers to JSON, for validation with Chartless Map
# See chartless.markers.viewer.html
#
CP=$(dirname $0)/build/libs/*.jar
JAVA_OPTIONS=
#
# JAVA_OPTIONS="${JAVA_OPTIONS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
# use sudo on Raspberry Pi
# JAVA_OPTIONS="${JAVA_OPTIONS} -Drmc.date.offset=7168"
java ${JAVA_OPTIONS} -cp ${CP} util.MarkersToJSON $1
#

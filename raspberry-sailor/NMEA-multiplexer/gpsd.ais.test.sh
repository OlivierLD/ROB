#!/usr/bin/env bash
#
# Requires a
# ../gradlew clean build shadowJar
CP=./build/libs/NMEA-multiplexer-1.0-all.jar
CP="${CP}:build/classes/java/test/"
JAVA_OPTS=
# JAVA_OPTS="${JAVA_OPTS} -Dverbose=true"
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80"
java -cp ${CP} ${JAVA_OPTS} tcptests.GPSDClient

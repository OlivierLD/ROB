#!/bin/bash
CP=./build/libs/Java-TCP-Python-1.0-all.jar
#
JVM_OPTIONS=
JVM_OPTIONS="${JVM_OPTIOND} -Dparse.json.response=false"
PRMS=$*
# PRMS="--port:8888 --host:192.168.1.106"
java -cp ${CP} ${JVM_OPTIOND} tcp.clients.SimpleTCPClient ${PRMS}

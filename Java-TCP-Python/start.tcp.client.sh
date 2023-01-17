#!/bin/bash
CP=./build/libs/Java-TCP-Python-1.0-all.jar
#
JVM_OPTIONS=
JVM_OPTIONS="${JVM_OPTIONS} -Dparse.json.response=true"
# JVM_OPTIONS="${JVM_OPTIONS} -Ddisplay.server.feed=true"
PRMS=$*
# PRMS="--port:8888 --host:192.168.1.106"
#
echo -e "Enter '/flip' at the prompt, to turn the server data display on or off."
#
java -cp ${CP} ${JVM_OPTIONS} tcp.clients.SimpleTCPClient ${PRMS}

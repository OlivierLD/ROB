#!/bin/bash
# MPS server
#
echo -e "----------------------------"
echo -e "Usage is ${0}  "
echo -e "     --http-port:8888"
echo -e "----------------------------"
#
echo -e "⚓ Starting the MPS Server 🌴"
echo -e "----------------------------------------"
# shellcheck disable=SC2145
echo -e "Args are $@"
echo -e "----------------------------------------"
#
function openBrowser() {
  if [[ $(uname -s) == *Linux* ]]; then
    sensible-browser "$1"
  else
    open "$1"  # Darwin
  fi
}
#
HTTP_PORT=9999
#
for ARG in "$@"; do
  echo -e "Managing prm ${ARG}"
  if [[ "${ARG}" == "--http-port:"* ]]; then
    HTTP_PORT=${ARG#*:}
  fi
done
#
if [[ "${INFRA_VERBOSE}" == "" ]]; then
  INFRA_VERBOSE=true
  echo -e "Setting INFRA_VERBOSE to ${INFRA_VERBOSE}"
fi
HTTP_VERBOSE=false
#
# CP=../build/libs/MPSServer-1.0-all.jar
CP=$(find ./build -name '*-all.jar')
#
OS=$(uname -a | awk '{ print $1 }')
if [[ "${OS}" == "Darwin" ]]; then
  CP=${CP}:./libs/RXTXcomm.jar # for Mac
elif [[ "${OS}" == "Linux" ]]; then
  CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi. Should already be in the fat-jar.
fi
#
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Djava.util.logging.config.file=logging.properties"
#
if [[ "$OS" == "Darwin" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions" # for Mac
fi
if [[ "$OS" == "Linux" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni" # for Raspberry Pi
fi
# For the value of Delta T, see:
# - http://maia.usno.navy.mil/ser7/deltat.data
# - http://maia.usno.navy.mil/
# Delta T predictions: http://maia.usno.navy.mil/ser7/deltat.preds
# JAVA_OPTS="${JAVA_OPTS} -DdeltaT=68.9677" # 01-Jan-2018
if [[ "${DELTA_T}" != "" ]]; then
  echo -e "Using DeltaT: [${DELTA_T}]"
  JAVA_OPTS="${JAVA_OPTS} -DdeltaT=${DELTA_T}"
else
  echo -e "Using default DeltaT"
  JAVA_OPTS="${JAVA_OPTS} -DdeltaT=AUTO" # can also use -DdeltaT=68.9677, -DdeltaT=AUTO:2025-10, if needed
fi
# JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=$HTTP_VERBOSE"
#
#JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
#JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose.dump=true"
#JAVA_OPTS="${JAVA_OPTS} -Dhttp.client.verbose=true"
#
JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=${ASTRO_VERBOSE}"
JAVA_OPTS="${JAVA_OPTS} -Drest.verbose=${REST_VERBOSE}"
# Hard-coded ones:
JAVA_OPTS="${JAVA_OPTS} -Drest.mps.verbose=false"
# JAVA_OPTS="${JAVA_OPTS} -Dnmea.utils.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Drest.feeder.verbose=true"
#
# For remote debugging:
## JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
# JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"  # new
# For remote JVM Monitoring
# JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$(hostname)"
#
echo -e "Using properties:${JAVA_OPTS}"
#
SUDO=
# DARWIN=`uname -a | grep Darwin`
DARWIN=$(uname -a | grep Darwin)  # TODO Redundant ?
#
if [[ "${DARWIN}" != "" ]]; then
  echo Running on Mac
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions" # for Mac
else
  echo Assuming Linux/Raspberry Pi
  JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni" # RPi
  # No sudo require if running as root, in Docker for example.
  if [[ "$(whoami)" != "root" ]]; then
    SUDO="sudo "
  fi
fi
#
if [[ "${HTTP_PORT}" != "" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${HTTP_PORT}"   # Use only if not in config file yet.
fi
#
COMMAND="${SUDO}java -cp ${CP} ${JAVA_OPTS} mpsrest.MPSServer"
if [[ "${CMD_VERBOSE}" == "Y" || 1 -eq 1 ]]; then    # Always true...
  echo -e "Running ${COMMAND}"
fi
#
${COMMAND} &
#
echo -e ">>> Waiting for the server to start..."
sleep 5  # Wait (5s) for the server to be operational
echo -en " ==> Open index.html in browser (y|n) ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  # openBrowser ${URL_01} &
  openBrowser "http://localhost:${HTTP_PORT}/web/index.html" &
fi
#
echo -e "Use killMPSServer.sh to turn it down."
echo -e "Bye now ✋"
#
#!/bin/bash
# Test the publisher
CP=./build/libs/RESTTideEngine-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=false"
# JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dwith.tide.coeffs=true"
# STATION="Ocean Beach, California"
# STATION="Port-Tudy,+France"
STATION="Brest,+France"
COMMAND="java -cp ${CP} ${JAVA_OPTS} tideengine.publisher.TidePublisher --station-name:\"${STATION}\" --tide-year:2024"
echo -e "Executing [${COMMAND}]"
${COMMAND}


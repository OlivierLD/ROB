#!/bin/bash
cd $(dirname $0)
# Test the publisher
CP=../../build/libs/RESTNavServer-1.0-all.jar
JAVA_OPTS=
JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=false"
# JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=true"
JAVA_OPTS="${JAVA_OPTS} -Dscript.path=."
JAVA_OPTS="${JAVA_OPTS} -Dpdf.path=."
#
# Do escape the station name !
#
# STATION="Ocean Beach, California"
STATION="Port-Tudy,+France"
#
echo -e "---------------------------------------------------------------------------------"
echo -e "This shows a way to publish tide tables \"manually\"."
echo -e "This can also be done - easily - from some web pages hosted by the RESTNavServer."
echo -e "Do look into it, you might like it."
echo -e "+-- S O M E   S T A T I O N S --+"
echo -e "|  1 - Port-Tudy, France        |"
echo -e "|  2 - Port-Navalo, France      |"
echo -e "|  3 - Lorient, France          |"
echo -e "|  4 - Ocean Beach, California  |"
echo -e "|  5 - Concarneau, France       |"
echo -e "|  6 - Cap d'Antifer, France    |"
echo -e "|  7 - Deauville, France        |"
echo -e "|  8 - Dunkerque, France        |"
echo -e "|  9 - New York, New York       |"
echo -e "| 10 - Sydney, Australia        |"
echo -e "| 11 - Matavai, Tahiti          |"
echo -e "+-------------------------------+"
echo -en "You choose > "
read RESP
case "${RESP}" in
    "1")
      STATION="Port-Tudy,+France"
      ;;
    "2")
      STATION="Port-Navalo,+France"
      ;;
    "3")
      STATION="Lorient,+France"
      ;;
    "4")
      STATION="Ocean+Beach,+California"
      ;;
    "5")
      STATION="Concarneau,+France"
      ;;
    "6")
      STATION="Cap+d'Antifer,+France"
      ;;
    "7")
      STATION="Deauville,+France"
      ;;
    "8")
      STATION="Dunkerque,+France"
      ;;
    "9")
      STATION="New+York,+New+York"
      ;;
    "10")
      STATION="Sydney,+Australia"
      ;;
    "11")
      STATION="Matavai,+Tahiti"
      ;;
    *)
      echo -e "Unknown option ${RESP}, exiting."
      exit 1
      ;;
esac
export def_year=$(date +%Y)
YEAR=${def_year}
echo -e "Default year is ${YEAR}"
echo -en "Your year > "
read RESP
if [[ "${RESP}" != "" ]]; then
  YEAR=${RESP}
fi
#
COMMAND="java -cp ${CP} ${JAVA_OPTS} tideengine.publisher.TidePublisher --station-name:\"${STATION}\" --tide-year:${YEAR}"
echo -e "Executing [${COMMAND}]"
${COMMAND}


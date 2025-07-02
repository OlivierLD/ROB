#!/bin/bash
#
# Merge all the *.nmea file of a given directory (and under) into a single one.
#
echo -e "Usage is:"
echo -e "${0} nmea-path"
echo -e "It will analyze all the *.nmea in {nmea-path}"
if [[ $# != 1 ]]; then
  echo -e "Wrong number of parameters: $#, expecting 1."
  exit 1
fi
LOG_PATH=$1
#
find ${LOG_PATH} -name '*.nmea' > nmea.file.list
#
CP=$(dirname $0)/build/libs/*.jar
JAVA_OPTIONS=
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspeed.unit=KN"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dverbose=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dsummary=true"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -Dgencsv=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -DswingGUI=false"
#
JAVA_OPTIONS="${JAVA_OPTIONS} -ea"  # Enable Assertions
#
for nmeadata in `sort nmea.file.list`; do
  # echo -e "Analyzing ${nmeadata}"
  java ${JAVA_OPTIONS} -cp ${CP} util.LogAnalyzer ${nmeadata}
  echo -e "----------------------------------------------------------------------"
done
#
echo -e "Done"
rm nmea.file.list 2> /dev/null

#!/bin/bash
#
# Cones Resolution
# Generate the input file from user's input, and pass it to PlayGround09
# A data file can be provided from the CLI. Like inputSample*.txt.
#
export CP=./build/libs/MPS-1.0-all.jar
export OPTIONS="-Dverbose=false"
#
# User input
#
PRM_FILE_NAME=
ASK_USER=true
if [[ "${1}" != "" ]]; then
  # File provided on CLI
  PRM_FILE_NAME=${1}
  ASK_USER=false
else
  PRM_FILE_NAME=cones.input.txt
fi
#
if [[ "${ASK_USER}" == "true" ]]; then
  rm cones.input.txt 2> /dev/null
  echo -e "You will be prompted to enter the user's position.\n"
  echo -en "Latitude (format like N 47º40.66'): "
  read LATITUDE
  echo -en "Longitude (format like W 3º08.14'): "
  read LONGITUDE
  echo "Position Latitude=${LATITUDE} Longitude=${LONGITUDE}" >> cones.input.txt
  #
  KEEP_LOOPING=true
  echo -e "Now, you will be prompted to enter body names, dates, and observed altitudes.\n"
  while [[ "${KEEP_LOOPING}" == "true" ]]; do
    echo -en "Body name: "
    read BODY_NAME
    echo -en "Date (duration format): "
    read THE_DATE
    #
    echo "Body=${BODY_NAME};Date=${THE_DATE}" >> cones.input.txt
    #
    echo -en "More [n]|y ? > "
    read REPLY
    if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      echo -e "Ok, another one."
    else
      echo -e "Now proceeding."
      KEEP_LOOPING=false
    fi
  done
else
  echo -e "Using data from ${PRM_FILE_NAME} (if found)"
fi
#
# Resolution
#
java -classpath ${CP} ${OPTIONS} mps.pg.PlayGround10 ${PRM_FILE_NAME}
#
echo -e "Done"
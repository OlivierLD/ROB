#!/bin/bash
#
# Cones Resolution
# Generate the input file from user's input, and pass it to PlayGround09
#
export CP=./build/libs/MPS-1.0-all.jar
export OPTIONS="-Dverbose=false"
#
# User input
#
echo -e "You will be prompted to enter body names, dates, and observed altitudes.\n"
rm cones.input.txt 2> /dev/null
KEEP_LOOPING=true
while [[ "${KEEP_LOOPING}" == "true" ]]; do
  echo -en "Body name: "
  read BODY_NAME
  echo -en "Date (duration format): "
  read THE_DATE
  echo -en "Observed Altitude: "
  read OBS_ALT
  #
  echo "Body=${BODY_NAME};Date=${THE_DATE};ObsAlt=${OBS_ALT}" >> cones.input.txt

  echo -en "More [n]|y ? > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Ok, another one."
  else
    echo -e "Now proceeding."
    KEEP_LOOPING=false
  fi
done
#
# Resolution
#
java -classpath ${CP} ${OPTIONS} mps.pg.PlayGround09 cones.input.txt
#
echo -e "Done"
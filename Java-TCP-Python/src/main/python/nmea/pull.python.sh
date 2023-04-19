#!/bin/bash
#
FILES_TO_PULL_LOCATION=../../../../../../raspberry-sailor/NMEA-Parser/src/main/python/NMEAParserROB/*.py
echo -e "Will pull resources from the following location:"
ls -lisa ${FILES_TO_PULL_LOCATION}
echo -e "This would overwrite existing data."
echo -en "OK to proceed y|[n] ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, proceeding."
  cp ${FILES_TO_PULL_LOCATION} .
else
  echo -e "Cancelling."
fi
echo -e "Done"
#

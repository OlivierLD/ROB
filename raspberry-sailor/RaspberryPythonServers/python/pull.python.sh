#!/bin/bash
#
PYTHON_ROOT=../../NMEA-Parser/src/main/python/NMEAParserROB
#
echo -e "Will pull resources from the following location:"
ls -lisa ${PYTHON_ROOT}
echo -e "This would overwrite existing data."
echo -en "OK to proceed y|[n] ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, proceeding."
  cp ${PYTHON_ROOT}/NMEABuilder.py .
  cp ${PYTHON_ROOT}/NMEAParser.py .
  cp ${PYTHON_ROOT}/checksum.py .
  cp ${PYTHON_ROOT}/utils.py .
  cp ${PYTHON_ROOT}/prefixes.py .
else
  echo -e "Cancelling."
fi
echo -e "Done"
#

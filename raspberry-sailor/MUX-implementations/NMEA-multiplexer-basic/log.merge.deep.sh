#!/bin/bash
#
# Merge all the *.nmea file of a given directory (and under) into a single one.
#
echo -e "Usage is:"
echo -e "${0} nmea-path final-file-name"
echo -e "It will merge all the *.nmea in {nmea-path} into {final-file-name}"
if [[ $# != 2 ]]; then
  echo -e "Wrong number of parameters: $#, expecting 2."
  exit 1
fi
LOG_PATH=$1
MERGED_FILE_NAME=$2
#
echo -e "Merging nmea data into ${MERGED_FILE_NAME}"
#
rm ${MERGED_FILE_NAME} 2> /dev/null
#
find ${LOG_PATH} -name '*.nmea' > nmea.file.list
# find ${LOG_PATH} -name '*.nmea' -exec echo -e "Adding {}"; cat {} >> ${MERGED_FILE_NAME} \;
# find ${LOG_PATH} -name '*.nmea' -exec cat {} \; # >> ${MERGED_FILE_NAME}
for nmeadata in `cat nmea.file.list`; do
  echo -e "Adding ${nmeadata}"
  cat "${nmeadata}" >> ${MERGED_FILE_NAME}
done
#
echo -e "Done"
echo -e "Now you might want to run a ./log.shrinker.sh ${MERGED_FILE_NAME}"

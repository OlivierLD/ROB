#!/bin/bash
#
# Visualize all data in a given directory without having to merge them.
# Merge all the *.nmea file of a given directory (and under) into a single temp one.
#
echo -e "Usage is:"
echo -e "${0} nmea-path"
echo -e "It will merge all the *.nmea in {nmea-path} and analyze the merged result"
if [[ $# != 1 ]]; then
  echo -e "Wrong number of parameters: $#, expecting 1."
  exit 1
fi
#
TMPFILE=$(mktemp /tmp/merged.nmea)
#
LOG_PATH=$1
MERGED_FILE_NAME=${TMPFILE}
#
echo -e "Merging nmea data into ${MERGED_FILE_NAME}"
#
rm ${MERGED_FILE_NAME} 2> /dev/null
#
find ${LOG_PATH} -name '*.nmea' > nmea.file.list
# find ${LOG_PATH} -name '*.nmea' -exec echo -e "Adding {}"; cat {} >> ${MERGED_FILE_NAME} \;
# find ${LOG_PATH} -name '*.nmea' -exec cat {} \; # >> ${MERGED_FILE_NAME}
for nmeadata in `cat nmea.file.list`; do
  echo -e "Adding ${nmeadata} ($(wc -l ${nmeadata}  | awk '{ print $1}') lines)"
  cat "${nmeadata}" >> ${MERGED_FILE_NAME}
done
#
echo -e "Done with merge"
$(dirname $0)/log.analyzer.sh ${TMPFILE}
rm ${TMPFILE}                     # Delete the file
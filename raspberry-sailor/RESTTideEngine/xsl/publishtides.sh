#!/bin/bash
echo ----------------------------
echo Tide publisher, from positions
echo ----------------------------
#
export SCRIPT_DIR=`dirname ${0}`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export HOME=..
#
export CP=${CP}:../build/libs/RESTTideEngine-1.0-all.jar
#
N_LAT=50.0
S_LAT=42.5
W_LNG=-10.0
E_LNG=5.0
echo -e "Selecting tide stations with latitudes in [${S_LAT}, ${N_LAT}], and longitudes in [${W_LNG}, ${E_LNG}]."
CMD="java -classpath ${CP} tiderest.utils.StationSelector --select --n-lat:${N_LAT} --s-lat:${S_LAT} --w-lng:${W_LNG} --e-lng:${E_LNG}"
$CMD > station.list.txt
#
DATA_DIR=pdf-folder
if [[ -d ${DATA_DIR} ]]; then
  rm -rf ${DATA_DIR}
fi
mkdir ${DATA_DIR}
#
while read -r station; do
  echo -e "Processing data for [${station}]"
  # Now, publish it !
  # ./publishtide.sh ${RADICAL}
  CMD="java -classpath ${CP} -Dscript.path=. -Dpdf.path=${DATA_DIR} tiderest.utils.StationSelector --publish --tide-year:2024 --station-name:${station}"
  echo -e "Executing ${CMD} ..."
  ${CMD}
done < station.list.txt
#
echo -e "See your data in the ${DATA_DIR} folder."

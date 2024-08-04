#!/bin/bash
#
# Stolen from gradlew
die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}
#
echo ---------------------------------------
echo Tide publisher, from filtered positions
echo ---------------------------------------
#
# shellcheck disable=SC2006
export SCRIPT_DIR=$(dirname "${0}")
echo -e "moving to folder ${SCRIPT_DIR}"
cd "${SCRIPT_DIR}" || die "No folder ${SCRIPT_DIR}"
#
export HOME=..
#
export CP=${CP}:../build/libs/RESTTideEngine-1.0-all.jar
#
# France Atlantic Facade # TODO Make those script prms
# N_LAT=50.0
# S_LAT=42.5
N_LAT=48.0
S_LAT=46.0
W_LNG=-10.0
E_LNG=5.0
echo -e "Selecting tide stations with latitudes in [${S_LAT}, ${N_LAT}], and longitudes in [${W_LNG}, ${E_LNG}]."
CMD="java -classpath ${CP} tiderest.utils.StationSelector --select --n-lat:${N_LAT} --s-lat:${S_LAT} --w-lng:${W_LNG} --e-lng:${E_LNG}"
$CMD > station.list.txt
#
DATA_DIR=pdf-folder
echo -e "Will generate tide almanacs for $(echo $(wc -l < station.list.txt)) station(s), in $(pwd)/${DATA_DIR}."
echo -en "Hit [Return] when ready... "
read REPLY
#
if [[ -d ${DATA_DIR} ]]; then
  rm -rf ${DATA_DIR}
fi
mkdir ${DATA_DIR}
#
DATA_YEAR=2024
while read -r station; do
  # echo -e "Processing data for [${station}], in ${DATA_YEAR}"
  # Now, publish it !
  SYSTEM_VARS="-Dscript.path=. -Dpdf.path=${DATA_DIR} -Dwith.tide.coeffs=true -Dtide.calc.verbose=false"
  CMD="java -classpath ${CP} ${SYSTEM_VARS} tiderest.utils.StationSelector --publish --tide-year:${DATA_YEAR} --station-name:${station}"
  # echo -e "Executing ${CMD} ..."
  ${CMD}
done < station.list.txt
#
echo -e "See your data in the ${DATA_DIR} folder."

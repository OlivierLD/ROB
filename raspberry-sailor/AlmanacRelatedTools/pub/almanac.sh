#!/bin/bash
echo ----------------------------
echo Nautical Almanac or Lunar Distnaces Calculation
echo and pdf generation
echo ----------------------------
#
export def_month=$(date +%m)
export def_day=$(date +%d)
export def_year=$(date +%Y)
#
export SCRIPT_DIR=$(dirname ${0})
echo -e "Moving to ${SCRIPT_DIR}"
pushd ${SCRIPT_DIR}
echo -e "Working from $(pwd -P)"
#
export FOP_HOME=../../MUX-implementations/RESTNavServer/launchers
#
export CP=${CP}:../build/libs/AlmanacRelatedTools-1.0-all.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-collation.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${FOP_HOME}/libs/fnd2.zip
export CP=${CP}:${FOP_HOME}/libs/xdo-0301.jar
#
# Reset
year=
month=
day=
genData=
fromY=
fromM=
fromD=
toY=
toM=
toD=
type=
# Prompt
export DeltaT=AUTO
#set /p DeltaT=Delta T [65.984] ^>
#if [%DeltaT%] == [] set DeltaT=65.984
export progOption="-type continuous"
echo -en "[C]ontinuous or [F]rom-To ? > "
read type
if [[ "${type}" == "F" ]] || [[ "${type}" == "f" ]]; then
  export progOption="-type from-to"
  echo -en "From Year  > "
  read fromY
  echo -en "From Month > "
  read fromM
  echo -en "From Day   > "
  read fromD
  echo -en "To Year    > "
  read toY
  echo -en "To Month   > "
  read toM
  echo -en "To Day     > "
  read toD
  progOption="${progOption} -from-year ${fromY} -from-month ${fromM} -from-day ${fromD} -to-year ${toY} -to-month ${toM} -to-day ${toD}"
else
  echo -en "Year         > "
  read year
  progOption="${progOption} -year ${year}"
  echo -en "Month [1-12] > "
  read month
  progOption="${progOption} -month ${month}"
  echo -en "Day [1-31]   > "
  read day
  progOption="${progOption} -day ${day}"
fi
#
#:resume
# Confirm
echo Generating data, with: ${progOption}, deltaT: ${DeltaT}
export proceed=
echo -en "Proceed with data generation? [y]|n > "
read proceed
if [[ "${proceed}" == "N" ]]; then
  exit 0
fi
echo Generating Data...
java -classpath ${CP} -DdeltaT="${DeltaT}" implementation.almanac.AlmanacComputerImpl ${progOption} -out ./data.xml
# processPDF
#set publishData=
#set /p publishData=Publish Data [y]^|n ?   ^>
#if [%publishData%] == [n] goto end
echo -e "Processing PDF file"
SS=
echo -en "Almanac [1] (default), Lunar Distances [2] > "
read SS
# XSL_STYLESHEET=./lunar2fop.xsl
XSL_STYLESHEET=./data2fop_2pages.xsl
if [[ "${SS%}" == "1" ]]; then
  XSL_STYLESHEET=./data2fop_2pages.xsl
elif [[ "${SS%}" == "2" ]]; then
  XSL_STYLESHEET=./lunar2fop.xsl
fi
PRM_OPTION=
echo -en "English [1], French [2] > "
read LANG
if [[ "${LANG}" == "2" ]]; then
  echo Will speak French
  PRM_OPTION="-docconf ./lang_fr.cfg"
  cp literals_fr.xsl literals.xsl
else
  echo Will speak English
  cp literals_en.xsl literals.xsl
  PRM_OPTION="-docconf ./lang_en.cfg"
fi
echo Now Publishing
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ./data.xml -xsl ${XSL_STYLESHEET} -pdf data.pdf
echo -e "Done calculating, checkout data.pdf!"

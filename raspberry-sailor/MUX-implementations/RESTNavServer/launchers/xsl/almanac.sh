#!/bin/bash
echo ----------------------------
echo Nautical Almanac or Lunar Distances Calculation
echo and pdf generation.
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
echo -e ">> System Date: year ${def_year}, month ${def_month}, day ${def_day}"
#
export FOP_HOME=..
#
export CP=${CP}:../../build/libs/RESTNavServer-1.0-all.jar
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
PROCESS_DATA=y
if [[ -f data.xml ]]; then
  echo -e "data.xml already exist:"
  ls -lisah data.xml
  echo -en "(re-)Generate data.xml ? [y]|n > "
  read resp
  if [[ "${resp}" == "N" ]] || [[ "${resp}" == "n" ]]; then
    PROCESS_DATA=n
  fi
fi
if [[ "${PROCESS_DATA}" == "y" ]]; then
  export DeltaT=AUTO
  #set /p DeltaT=Delta T [65.984] ^>
  #if [%DeltaT%] == [] set DeltaT=65.984
  export progOption="-type continuous"
  echo -e "-------------------------------------------------------------"
  echo -e "Continuous: Day by day for one year, one month, or one day."
  echo -e "From-to: Day by day for a given day to another given day."
  echo -e "-------------------------------------------------------------"
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
    if [[ "${month}" != "" ]]; then
      progOption="${progOption} -month ${month}"
      echo -en "Day [1-31]   > "
      read day
      if [[ "${day}" != "" ]]; then
        progOption="${progOption} -day ${day}"
      fi
    fi
  fi
  #
  #:resume
  # Confirm
  echo -e "Generating data, with: [${progOption}], deltaT: ${DeltaT}"
  export proceed=
  echo -en "Proceed with data generation? [y]|n > "
  read proceed
  if [[ "${proceed}" == "N" ]] || [[ "${proceed}" == "n" ]]; then
    echo -e "Bye."
    exit 0
  fi
  echo Generating Data...
  java -classpath ${CP} -DdeltaT="${DeltaT}" implementation.almanac.AlmanacComputerImpl ${progOption} -out ./data.xml
fi
# processPDF
#set publishData=
#set /p publishData=Publish Data [y]^|n ?   ^>
#if [%publishData%] == [n] goto end
echo -e "Now Processing PDF file"
SS=
echo -en "Almanac [1] (default, Sun, Planets, Stars), Lunar Distances [2] > "
read SS
# XSL_STYLESHEET=./lunar2fop.xsl
XSL_STYLESHEET=./data2fop_2pages.xsl  # Default
if [[ "${SS%}" == "1" ]]; then
  XSL_STYLESHEET=./data2fop_2pages.xsl
elif [[ "${SS%}" == "2" ]]; then
  XSL_STYLESHEET=./lunar2fop.xsl
fi
PRM_OPTION=
echo -en "English [1], French [2] > "
read LANG  # Default english
if [[ "${LANG}" == "2" ]]; then
  echo -e "Will speak French"
  cp literals_fr.xsl literals.xsl
  PRM_OPTION="-docconf ./lang_fr.cfg"
  if [[ "${SS%}" != "2" ]]; then
    echo -en "With Stars ? [y]|n > "
    read RESP
    if [[ "${RESP}" == "n" ]] || [[ "${RESP}" == "N" ]]; then
      PRM_OPTION="-docconf ./lang_fr_ns.cfg"
    fi
  fi
else
  echo -e "Will speak English"
  cp literals_en.xsl literals.xsl
  PRM_OPTION="-docconf ./lang_en.cfg"
  if [[ "${SS%}" != "2" ]]; then
    echo -en "With Stars ? [y]|n > "
    read RESP
    if [[ "${RESP}" == "n" ]] || [[ "${RESP}" == "N" ]]; then
      PRM_OPTION="-docconf ./lang_en_ns.cfg"
    fi
  fi
fi
# Page Format
echo -en "Format: US Letter [1], A4 [2] > "
read FORMAT  # Default Letter
if [[ "${FORMAT}" == "2" ]]; then
  echo -e "A4 selected"
  cp page_portrait_A4.xsl page_portrait.xsl
else
  echo -e "US Letter selected"
  cp page_portrait_Letter.xsl page_portrait.xsl
fi
#
echo -e "Now Publishing (bumping Memory to 2048Mb)"
java -Xms256m -Xmx2048m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ./data.xml -xsl ${XSL_STYLESHEET} -pdf data.pdf
echo -e "Done calculating, checkout data.pdf ! (in $(pwd))"
popd

#!/bin/bash
#
# This is a bonus.
# It is actually the Tide Almanac, without the tide data.
#
echo ----------------------------
echo Agenda publisher
echo ----------------------------
#
export SCRIPT_DIR=`dirname ${0}`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export RADICAL=$1
export LANG=$2
export FORMAT=$3
echo Transforming ${RADICAL}.xml into ${RADICAL}.pdf
#
export HOME=..
#
export CP=${CP}:../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
export XSL_STYLESHEET=./agenda2fop.xsl
# Page Format?
if [[ "${FORMAT}" == "" ]]; then
  # Manage that, interactively, or not...
  if [[ -t 1 ]]; then  # Run from a terminal
    echo -e "Running from a Terminal"
    echo -en "Final document format: US Letter [1], A4 [2] > "
    read FORMAT  # Default Letter
    if [[ "${FORMAT}" == "" ]]; then
      FORMAT=1
    fi
  else     # Not from a terminal
    echo -e "Not-a-terminal, defaulting to US"
    FORMAT=1
  fi
fi
if [[ "${FORMAT}" == "2" || "${FORMAT}" == "A4" ]]; then
  echo -e "A4 selected"
  cp page_A4.xsl page.xsl
else
  echo -e "US Letter selected"
  cp page_USLetter.xsl page.xsl
fi
#
if [[ "${LANG}" == "FR" ]]; then # Language Option, mentioned in main stylesheet
  echo -e "We'll speak French."
  cp literals_fr.xsl literals.xsl
else
  echo -e "We'll speak English."
  cp literals_en.xsl literals.xsl
fi
#
echo Publishing
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -xml ${RADICAL}.xml -xsl ${XSL_STYLESHEET} -pdf ${RADICAL}.pdf
echo Done transforming, document ${RADICAL}.pdf is ready.
# open ${RADICAL}.pdf
# exit
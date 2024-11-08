#!/bin/bash
echo ----------------------------
echo Moon Calendar publisher
echo To run on a one-year tide xml file
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
export XSL_STYLESHEET=./lunarcal2fop.xsl
# Page Format?
if [[ "${FORMAT}" == "" ]]; then
  echo -en "Final document format: US Letter [1], A4 [2] > "
  read FORMAT  # Default Letter
fi
if [[ "${FORMAT}" == "2" || "$FORMAT}" == "A4" ]]; then
  echo -e "A4 selected"
  cp page_A4.xsl page.xsl
else
  echo -e "US Letter selected"
  cp page_USLetter.xsl page.xsl
fi
#
# TODO Manage $LANG
#
echo Publishing
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -xml ${RADICAL}.xml -xsl ${XSL_STYLESHEET} -pdf ${RADICAL}.pdf
echo Done transforming, document is ready.
echo See ${RADICAL}.pdf
# exit

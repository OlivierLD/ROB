#!/bin/bash
echo ----------------------------
echo Perpetual Nautical Almanac Publication
echo ----------------------------
#
export SCRIPT_DIR=`dirname ${0}`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
export HOME=..
#
export CP=${CP}:../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${HOME}/libs/orai18n-collation.jar
export CP=${CP}:${HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${HOME}/libs/fnd2.zip
export CP=${CP}:${HOME}/libs/xdo-0301.jar
#
FORMAT=$3
echo Processing PDF file
echo Publishing perpetual almanac
cp literals_en.xsl literals.xsl
# Page Format?
if [[ "${FORMAT}" == "" ]]; then
  echo -en "Final document format: US Letter [1], A4 [2] > "
  read FORMAT  # Default Letter
  if [[ "${FORMAT}" == "2" ]]; then
    echo -e "A4 selected"
    cp page_A4.xsl page.xsl
  else
    echo -e "US Letter selected"
    cp page_USLetter.xsl page.xsl
  fi
else
  if [[ "${FORMAT}" == "A4" ]]; then
    echo -e "A4 selected"
    cp page_A4.xsl page.xsl
  else
    echo -e "US Letter selected"
    cp page_USLetter.xsl page.xsl
  fi
fi
XSL_STYLESHEET=./perpetual.xsl
PRM_OPTION="-docconf ./scalable.cfg"
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml $1 -xsl $XSL_STYLESHEET -pdf $2
echo Done calculating, $2 is ready.
#

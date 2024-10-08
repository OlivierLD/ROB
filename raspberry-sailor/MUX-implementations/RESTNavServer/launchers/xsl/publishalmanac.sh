#!/bin/bash
echo ----------------------------
echo Almanac publisher
echo Usage is: ${0} lang withStars xmlData pdf
echo lang: EN\|FR
echo withStars: true\|false
echo xmlData: computed XML data file
echo pdf: name of the final document
echo example: ${0} EN true ../../data.2017.xml almanac.2017.pdf A4
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
XSL_STYLESHEET=./data2fop_2pages.xsl
LANG=$1
WITH_STARS=$2
FORMAT=$5
PRM_OPTION=
if [[ $LANG == "FR" ]]; then
  echo On parle francais
  PRM_OPTION="-docconf ./lang_fr.cfg"
  if [[ $WITH_STARS = "false" ]]; then
    PRM_OPTION="-docconf ./lang_fr_ns.cfg"
  fi
  cp literals_fr.xsl literals.xsl
else
  echo Will speak English
  cp literals_en.xsl literals.xsl
  PRM_OPTION="-docconf ./lang_en.cfg"
  if [[ $WITH_STARS = "false" ]]; then
    PRM_OPTION="-docconf ./lang_en_ns.cfg"
  fi
fi
# Page Format?
if [[ $FORMAT == "" ]]; then
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
echo Publishing, be patient.
#
# Note on -Xmx:
# A 1 year almanac with stars is about 11Mb big, and 1024 seems to be a bit too tight.
#
COMMAND="java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml $3 -xsl $XSL_STYLESHEET -pdf $4"
echo Running from $PWD: ${COMMAND}
${COMMAND}
echo Done transforming, document $4 is ready.
#

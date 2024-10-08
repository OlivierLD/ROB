#!/bin/bash
echo ----------------------------
echo Lunar Distances publisher
echo Usage is: ${0} lang xmlData pdf
echo lang: EN\|FR
echo xmlData: computed XML data file
echo pdf: name of the final document
echo example: ${0} EN ../../data.2017.xml lunar.2017.pdf A4
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
XSL_STYLESHEET=./lunar2fop.xsl
LANG=$1
FORMAT=$4
if [[ $LANG = "FR" ]]; then
  echo On parle francais
  PRM_OPTION="-docconf ./lang_fr.cfg"
  cp literals_fr.xsl literals.xsl
else
  echo Will speak English
  PRM_OPTION="-docconf ./lang_en.cfg"
  cp literals_en.xsl literals.xsl
fi
if [[ "${FORMAT}" == "" ]]; then
  # Page Format?
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
java -Xms256m -Xmx1536m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml $2 -xsl $XSL_STYLESHEET -pdf $3
echo Done calculating, $3 is ready.
#

#!/bin/bash
echo ----------------------------
echo Tide publisher
echo ----------------------------
#
export SCRIPT_DIR=`dirname ${0}`
echo moving to ${SCRIPT_DIR}
cd ${SCRIPT_DIR}
#
LANG=$2
PRM_OPTION=
#
if [[ $LANG == "FR" ]]; then
  echo On parle francais
  PRM_OPTION="-docconf ./tide_lang_fr.cfg"
  # echo avec les parametres :
  # cat ./tide_lang_fr.cfg
  # cp literals_fr.xsl literals.xsl
else
  echo Will speak English
  PRM_OPTION="-docconf ./tide_lang_en.cfg"
  # echo with prms:
  # cat ./tide_lang_en.cfg
  # cp literals_en.xsl literals.xsl
fi
echo Publishing, please be patient.
#
export RADICAL=$1
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
export XSL_STYLESHEET=./tide2fop.xsl
echo Publishing
COMMAND="java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ${RADICAL}.xml -xsl ${XSL_STYLESHEET} -pdf ${RADICAL}.pdf"
echo -e "Executing ${COMMAND}"
${COMMAND}
echo Done transforming, document is ready.
# open ${RADICAL}.pdf
# exit

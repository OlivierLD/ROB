#!/bin/bash
echo ----------------------------
echo Perpetual Nautical Almanac Calculation
echo and pdf generation.
echo ----------------------------
#
export SCRIPT_DIR=$(dirname ${0})
echo -e "Moving to ${SCRIPT_DIR}"
pushd ${SCRIPT_DIR}
echo -e "Working from $(pwd -P)"
#
export FOP_HOME=../..
#
export CP=${CP}:../../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-collation.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${FOP_HOME}/libs/fnd2.zip
export CP=${CP}:${FOP_HOME}/libs/xdo-0301.jar
#
echo -en "From year > "
read from
echo -en "To year   > "
read to
#
echo Generating Data...
java -classpath ${CP} implementation.perpetualalmanac.Publisher ${from} ${to} ./data.xml
# :processPDF
echo Processing PDF file
PRM_OPTION=
echo -en "English [1], French [2] > "
read LANG
if [[ "${LANG}" == "2" ]]; then
  echo -e "Will speak French"
  cp literals_fr.xsl literals.xsl
else
  echo -e "Will speak English"
  cp literals_en.xsl literals.xsl
fi
XSL_STYLESHEET=./perpetual.xsl
PRM_OPTION="-docconf ./scalable.cfg"
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ./data.xml -xsl ${XSL_STYLESHEET} -pdf perpetual.pdf
echo -e "Done calculating and publishing, checkout perpetual.pdf, in $(pwd)."

# open perpetual.pdf
#
popd

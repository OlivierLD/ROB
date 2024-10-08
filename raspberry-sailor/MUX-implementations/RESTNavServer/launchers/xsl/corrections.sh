#!/bin/bash
echo ----------------------------
echo Correction Tables Calculation
echo and pdf generation
echo ----------------------------
#
export SCRIPT_DIR=$(dirname ${0})
echo -e "Moving to ${SCRIPT_DIR}"
pushd ${SCRIPT_DIR}
echo -e "Now working from $(pwd -P)"
#
export FOP_HOME=..
#
export CP=${CP}:../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-collation.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${FOP_HOME}/libs/fnd2.zip
export CP=${CP}:${FOP_HOME}/libs/xdo-0301.jar
#
# The raw data
java -classpath ${CP} tables.CorrectionTables
# The pdf transformation
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
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -xml ./corrections.xml -xsl corr-to-fo.xsl -pdf corrections.pdf
echo -e "Done calculating and publishing, checkout corrections.pdf, in $(pwd)."
# call corrections.pdf
popd

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
export FOP_HOME=../..
#
export CP=${CP}:../../../build/libs/RESTNavServer-1.0-all.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-collation.jar
export CP=${CP}:${FOP_HOME}/libs/orai18n-mapping.jar
export CP=${CP}:${FOP_HOME}/libs/fnd2.zip
export CP=${CP}:${FOP_HOME}/libs/xdo-0301.jar
#
java -classpath ${CP} tables.CorrectionTables
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor -xml ./corrections.xml -xsl corr-to-fo.xsl -pdf corrections.pdf
echo Done calculating, checkout corrections.pdf.
# call corrections.pdf
popd

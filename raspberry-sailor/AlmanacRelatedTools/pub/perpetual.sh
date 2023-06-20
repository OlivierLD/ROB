#!/bin/bash
echo ----------------------------
echo Perpetual Nautical Almanac Calculation
echo and pdf generation
echo ----------------------------
#
export SCRIPT_DIR=$(dirname ${0})
echo -e "Moving to ${SCRIPT_DIR}"
pushd ${SCRIPT_DIR}
echo -e "Working from $(pwd -P)"
#
export FOP_HOME=../../MUX-implementations/RESTNavServer/launchers
#
export CP=${CP}:../build/libs/AlmanacRelatedTools-1.0-all.jar
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
XSL_STYLESHEET=./perpetual.xsl
PRM_OPTION="-docconf ./scalable.cfg"
java -Xms256m -Xmx1024m -classpath ${CP} oracle.apps.xdo.template.FOProcessor ${PRM_OPTION} -xml ./data.xml -xsl ${XSL_STYLESHEET} -pdf perpetual.pdf
echo Done calculating, see perpetual.pdf !
# open perpetual.pdf
#

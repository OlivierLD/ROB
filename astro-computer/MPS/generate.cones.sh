#!/bin/bash
#
# Cones Generation (in json)
#
export CP=./build/libs/MPS-1.0-all.jar
export OPTIONS="-Dverbose=false"
#
java -classpath ${CP} ${OPTIONS} mps.samplesandtools.Context01
echo -e "Done"
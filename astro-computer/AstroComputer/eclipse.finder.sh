#!/bin/bash
echo -e "-------------------------------------------------------------------------------"
echo -e "Will use gradle (tests) to find the next solar and lunar eclipses in the next 5 years"
echo -e "Use CLI prm --help, -h, or ?, for help." # Soon...
echo -e "-------------------------------------------------------------------------------"
if [[ -f "./build/libs/AstroComputer-1.0.jar" ]]; then
  echo -e "Using the jar ./build/libs/AstroComputer-1.0.jar..."
  CP=./build/libs/AstroComputer-1.0.jar:../../common-utils/build/libs/common-utils-1.0.jar
  java -cp ${CP} astro.EclipseFinder $*
else
  COMMAND="../../gradlew eclipseFinder -Pargs='$*'"
  echo -e "Using gradle cmd ${COMMAND}"
  ../../gradlew eclipseFinder -Pargs="$*"
fi
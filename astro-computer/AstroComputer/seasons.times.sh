#!/bin/bash
echo -e "-------------------------------------------------------------------------------"
echo -e "Will use gradle (tests) to get to the precise time of the solstices and equinox"
echo -e "Use CLI prm --help, -h, or ?, for help."
echo -e "-------------------------------------------------------------------------------"
if [[ -f "./build/libs/AstroComputer-1.0.jar" ]]; then
  echo -e "Using the jar ./build/libs/AstroComputer-1.0.jar..."
  CP=./build/libs/AstroComputer-1.0.jar:../../common-utils/build/libs/common-utils-1.0.jar
  java -cp ${CP} astro.SpringSummerFallWinter $*
else
  COMMAND="../../gradlew runSeasonsTime -Pargs='$*'"
  echo -e "Using gradle cmd ${COMMAND}"
  ../../gradlew runSeasonsTime -Pargs="$*"
fi
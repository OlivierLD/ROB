#!/bin/bash
#
echo -e "Deploy the Weather Wizard and related resources."
#
# Final folder
#
if [[ ! -d ./ww.prod ]]; then
  mkdir ww.prod
else
  # cleanup
  rm -r ww.prod/*
fi
#
# 1 - Build
#
../../../gradlew shadowJar
cp build/libs/*all.jar ww.prod
pushd ../ww-user-exits/WW-UserExit_CurrentDustlet
../../../../gradlew build
cp build/libs/*1.0.jar ../../WeatherWizard/ww.prod
popd
#
pushd ../ww-user-exits/WW-UserExit_Dustlets
../../../../gradlew build
cp build/libs/*1.0.jar ../../WeatherWizard/ww.prod
popd
#
pushd ../ww-user-exits/WW-UserExits
../../../../gradlew build
cp build/libs/*1.0.jar ../../WeatherWizard/ww.prod
popd
#
pushd ../ww-user-exits/WW-UserExits_II
../../../../gradlew build
cp build/libs/*1.0.jar ../../WeatherWizard/ww.prod
popd
#
# Resources
#
cp run-ww-prod.sh ww.prod/run-ww.sh
cp run-ww-prod.bat ww.prod/run-ww.bat
#
mkdir ww.prod/config
cp -r config/* ww.prod/config
#
mkdir ww.prod/composites
mkdir ww.prod/GRIBFiles
mkdir ww.prod/WeatherFaxes
#
mkdir ww.prod/patterns
cp -r patterns/* ww.prod/patterns
#
mkdir ww.prod/polars
cp -r polars/* ww.prod/polars
#
mkdir ww.prod/sounds
cp -r sounds/* ww.prod/sounds
#
cp *.xml ww.prod
#
# Now zip it up
#
echo -e "Now archiving..."
zip -q -r ww-prod.zip ww.prod
# tar -cvzf ${distdir}.tar.gz ${distdir}
# Drop directory ?
echo -e "Archive ww-prod.zip was generated:"
ls -lisah ww-prod.zip
echo -en "Can we drop the ww-prod directory ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, moving on..."
  rm -rf ww.prod
else
  echo -e "OK. This will be your responsibility to clean it up."
fi
#
echo -e "----------------------------------------------------------------------------------"
echo -e "You can now send the archive ww-prod.zip to the destination machine, and unzip it."
echo -e "You need java to be available on the destination machine, then you're good to go"
echo -e "On Windows, run the script run-ww.bat"
echo -e "On other systems, run the script run-ww.sh"
echo -e "----------------------------------------------------------------------------------"
#
echo -e "Ready! Bye!"
#!/bin/bash
cd $(dirname $0)
CP=./build/libs/WeatherWizard-1.0-all.jar
# For user exits
CP=${CP}:../ww-user-exits/WW-UserExits/build/libs/WW-UserExits-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExits_II/build/libs/WW-UserExits_II-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExit_Dustlets/build/libs/WW-UserExit_Dustlets-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExit_CurrentDustlet/build/libs/WW-UserExit_CurrentDustlet-1.0.jar
#
java -cp ${CP} main.splash.Splasher
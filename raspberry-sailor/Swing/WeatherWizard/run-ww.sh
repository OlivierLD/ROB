#!/bin/bash
cd $(dirname $0)
CP=./build/libs/WeatherWizard-1.0-all.jar
# For user exits
CP=${CP}:../ww-user-exits/WW-UserExits/build/libs/WW-UserExits-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExits_II/build/libs/WW-UserExits_II-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExit_Dustlets/build/libs/WW-UserExit_Dustlets-1.0.jar
CP=${CP}:../ww-user-exits/WW-UserExit_CurrentDustlet/build/libs/WW-UserExit_CurrentDustlet-1.0.jar
# CP=${CP}:../ww-user-exits/WW-UserExits_Processing/build/libs/WW-UserExits_Processing-1.0.jar
#
# For Processing (... WiP)
#
# CP=${CP}:/Applications/Processing.v4.app/Contents/Java/core/library/core.jar
#
# Options:
# headless
# start.loop.at <- For default composite
# display.composite, default true
#
# Default L&F is Metal (unless enforced below)
#
LNF=
#
# LNF="-Dkeep.system.lnf=true"  # Applied if -Dswing.defaultlaf is NOT here.
#
# System Specific
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
# Generic
# LNF="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel"
#
LANG=
# LANG="-Duser.country=FR -Duser.language=fr"
#
java -cp ${CP} ${LNF} ${LANG} ${TIDE_EMAILS} main.splash.Splasher

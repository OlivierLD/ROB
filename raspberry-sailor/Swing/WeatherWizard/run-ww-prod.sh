#!/bin/bash
cd $(dirname $0)
CP=./WeatherWizard-1.0-all.jar
# For user exits
CP=${CP}:./WW-UserExits-1.0.jar
CP=${CP}:./WW-UserExits_II-1.0.jar
CP=${CP}:./WW-UserExit_Dustlets-1.0.jar
CP=${CP}:./WW-UserExit_CurrentDustlet-1.0.jar
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
LNF="-Dkeep.system.lnf=true"  # Applied if -Dswing.defaultlaf is NOT here.
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
OPT=
OPT="${OPT} -Dswing.defaultlaf"
OPT="${OPT} -Dfix-wacky-values=true"  # See the code for details
# For Mac: -Xdock:name="Weather Wizard"
# OPT="${OPT} -Xdock:name=\"Weather Wizard\""
java -cp ${CP} ${LNF} ${LANG} ${OPT} main.splash.Splasher
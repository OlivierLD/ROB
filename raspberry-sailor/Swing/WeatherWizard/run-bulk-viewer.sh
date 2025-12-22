#!/bin/bash
cd $(dirname $0)
CP=./build/libs/WeatherWizard-1.0-all.jar
# For Processing (... WiP)
#
# CP=${CP}:/Applications/Processing.v4.app/Contents/Java/core/library/core.jar
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
java -cp ${CP} ${LNF} ${LANG} main.BulkGribViewer
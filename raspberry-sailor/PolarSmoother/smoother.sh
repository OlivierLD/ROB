#!/usr/bin/env bash
CP=./build/libs/PolarSmoother-1.0-all.jar
#
LNF=
#
# LNF="-Dkeep.system.lnf=true"  # Applied if -Dswing.defaultlaf is NOT here.
#
# System Specific
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
# Generic
LNF="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
# LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel"
#
LANG=
# LANG="-Duser.country=FR -Duser.language=fr"
#
java -cp ${CP} ${LNF} ${LANG} polarmaker.polars.main.PolarSmoother
#
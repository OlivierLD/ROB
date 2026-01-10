@echo off
::
:: For Windows. WiP.
::
@setlocal
set CP=./build/libs/WeatherWizard-1.0-all.jar
::
:: Default L&F is Metal (unless enforced below)
::
set LNF=
::
:: set LNF="-Dkeep.system.lnf=true"
::
:: System Specific
:: set LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
set LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
:: Generic
:: set LNF="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
:: LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel"
::
set LANG=
:: LANG="-Duser.country=FR -Duser.language=fr"
::
java -cp %CP% %LNF% %LANG% main.BulkGribViewer
@endlocal
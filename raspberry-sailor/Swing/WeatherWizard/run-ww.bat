@echo off
::
:: For Windows. WiP.
::
@setlocal
::
set CP=.\build\libs\WeatherWizard-1.0-all.jar
:: For user exits
set CP=%CP%;..\ww-user-exits\WW-UserExits\build\libs\WW-UserExits-1.0.jar
set CP=%CP%;..\ww-user-exits\WW-UserExits_II\build\libs\WW-UserExits_II-1.0.jar
set CP=%CP%;..\ww-user-exits\WW-UserExit_Dustlets\build\libs\WW-UserExit_Dustlets-1.0.jar
set CP=%CP%;..\ww-user-exits\WW-UserExit_CurrentDustlet\build\libs\WW-UserExit_CurrentDustlet-1.0.jar
::
:: Options:
set LNF=
:: Applied if -Dswing.defaultlaf is NOT here.
:: set LNF="-Dkeep.system.lnf=true"
::
:: System Specific
:: set LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
set LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
:: Generic
:: set LNF="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
:: set LNF="-Dswing.defaultlaf=com.sun.java.swing.plaf.motif.MotifLookAndFeel"
::
set LANG=
:: set LANG="-Duser.country=FR -Duser.language=fr"
::
java -cp %CP% %LNF% %LANG% main.splash.Splasher
@endlocal
@echo off
::
:: For Windows. WiP.
::
@setlocal
::
set CP=.\WeatherWizard-1.0-all.jar
:: For user exits
set CP=%CP%;.\WW-UserExits-1.0.jar
set CP=%CP%;.\WW-UserExits_II-1.0.jar
set CP=%CP%;.\WW-UserExit_Dustlets-1.0.jar
set CP=%CP%;.\WW-UserExit_CurrentDustlet-1.0.jar
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
set OPT=
:: set OPT=%OPT% -Dswing.defaultlaf
set OPT=%OPT% -Dfix-wacky-values=true
::
set COMMAND=java -cp %CP% %LNF% %LANG% %OPT% main.splash.Splasher
echo Running %COMMAND%
::
:: java -cp %CP% %LNF% %LANG% %OPT% main.splash.Splasher
%COMMAND%
::
@endlocal
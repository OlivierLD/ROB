@setlocal
@echo off
@echo Usage:
@echo   %0 ^<command ^<prm1 ^<prm2 ^<...^>^>^>
set begintime=%time%
@echo -----------------------------------------
@echo Starting: %begintime% ...
@echo -----------------------------------------
@echo %CD%^> %*
:: Execution to time goes here
%*
::
@echo -----------------------------------------
set finishtime=%time%
call :timediff %begintime% %finishtime%
::@echo.
@echo Done at %finishtime%, execution of [%*] took %timetaken% sec.
@echo -----------------------------------------
goto eos
::
:timediff
:: echo from [%1] to [%2]
set starttime=%1
set endtime=%2
if [%starttime:~1,1%] == [:] set starttime=0%starttime%
if [%endtime:~1,1%] == [:] set endtime=0%endtime%
set startcsec=%starttime:~9,2%
set startsecs=%starttime:~6,2%
set startmins=%starttime:~3,2%
set starthour=%starttime:~0,2%
:: Remove leading 0 (considered as octal numbers)
call :removeLeadingZero %startcsec%
set startcsec=%truncated%
call :removeLeadingZero %startsecs%
set startsecs=%truncated%
call :removeLeadingZero %startmins%
set startmins=%truncated%
call :removeLeadingZero %starthour%
set starthour=%truncated%
::
set /a starttime=(%starthour%*60*60*100)+(%startmins%*60*100)+(%startsecs%*100)+(%startcsec%)
::
set endcsec=%endtime:~9,2%
set endsecs=%endtime:~6,2%
set endmins=%endtime:~3,2%
set endhour=%endtime:~0,2%
:: Remove leading 0 (considered as octal numbers)
call :removeLeadingZero %endcsec%
set endcsec=%truncated%
call :removeLeadingZero %endsecs%
set endsecs=%truncated%
call :removeLeadingZero %endmins%
set endmins=%truncated%
call :removeLeadingZero %endhour%
set endhour=%truncated%
::
if %endhour% LSS %starthour% set /a endhour+=24
set /a endtime=(%endhour%*60*60*100)+(%endmins%*60*100)+(%endsecs%*100)+(%endcsec%)
set /a timetaken= ( %endtime% - %starttime% )
set /a timetakens= %timetaken% / 100
set timetaken=%timetakens%.%timetaken:~-2%
goto eos
::
:removeLeadingZero
set truncated=%1
:top
if not [%truncated:~1%] == [] (
  if [%truncated:~0,1%] == [0] (
    set truncated=%truncated:~1%
    goto top
  )
)
:: if not [%1] == [%truncated%] @echo [%1] becomes [%truncated%]
goto eos
:: End Of Script
:eos
@endlocal
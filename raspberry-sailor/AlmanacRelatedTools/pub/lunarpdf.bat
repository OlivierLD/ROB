@echo off
@setlocal
@echo ----------------------------
@echo Nautical Almanac Calculation
@echo and pdf generation
@echo ----------------------------
rem
rem TODO From / To Date
rem
:: --------------------
:: Isolate current date
:: --------------------
for /f "tokens=2-4 skip=1 delims=(/-)" %%G in ('echo.^|date') do (
	for /f "tokens=2 delims= " %%A in ('date /t') do (
		set v_first=%%G
		set v_second=%%H
		set v_third=%%I
		set v_all=%%A
	)
)
set def_month=%v_all:~0,2%
set def_day=%v_all:~3,2%
set def_year=%v_all:~6,4%
::
set OLIVSOFT_HOME=C:\_myWork\_ForExport\dev-corner\olivsoft
::
set CP=%OLIVSOFT_HOME%\all-libs\nauticalalmanac.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-libs\almanactools.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-libs\geomutil.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xmlparserv2.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-collation.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-mapping.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\fnd2.zip
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xdo-0301.jar
::
:: Reset
set year=
set month=
set day=
set genData=
set fromY=
set fromM=
set fromD=
set toY=
set toM=
set toD=
set type=
:: Prompt
set /p genData=Calculate Data [y]^|n ? ^> 
if [%genData%] == [n] goto processPDF
set /p DeltaT=Delta T [65.984] ^> 
if [%DeltaT%] == [] set DeltaT=65.984
set progOption=-type continuous
set /p type=[C]ontinuous or [F]rom-To ? ^> 
if /i not [%type%] == [F] goto optionOne
set progOption=-type from-to
set /p fromY=From Year  ^> 
set /p fromM=From Month ^> 
set /p fromD=From Day   ^> 
set /p toY=To Year    ^> 
set /p toM=To Month   ^> 
set /p toD=To Day     ^> 
set progOption=%progOption% -from-year %fromY% -from-month %fromM% -from-day %fromD% -to-year %toY% -to-month %toM% -to-day %toD%
goto resume
:optionOne
set /p year=Year [%def_year%]    ^> 
if [%year%] == [] set year=%def_year%
set progOption=%progOption% -year %year%
set /p month=Month [1-12] ^> 
if not [%month%] == [] ( 
  set progOption=%progOption% -month %month%
  set /p day=Day [1-31]   ^> 
)  
if not [%day%] == [] set progOption=%progOption% -day %day%
:resume
:: Confirm
@echo Generating data, with %progOption%, deltaT %DeltaT%
set proceed=
set /p proceed=Proceed? [y]^|n ^> 
if /i [%proceed%] == [N] goto end
@echo Generating Data...
@java -classpath %CP% -DdeltaT=%DeltaT% app.almanac.AlmanacComputer %progOption% -out .\data.xml
:processPDF
set publishData=
set /p publishData=Publish Data [y]^|n ? ^> 
if [%publishData%] == [n] goto end
@echo Processing PDF file
set XSL_STYLESHEET=.\lunar2fop.xsl
set /p LANG=English [1], French [2] ^> 
if [%LANG%] == [2] (
  echo Will speak French
  set PRM_OPTION=-docconf .\lang_fr.cfg 
  copy literals_fr.xsl literals.xsl
) else (
  echo Will speak English
  copy literals_en.xsl literals.xsl
  set PRM_OPTION=-docconf .\lang_en.cfg 
)
@echo Publishing
@java -Xms256m -Xmx1024m -classpath %CP% oracle.apps.xdo.template.FOProcessor %PRM_OPTION% -xml .\data.xml -xsl %XSL_STYLESHEET% -pdf data.pdf
@echo Done calculating, displaying.
call data.pdf
:end
::pause
@endlocal

@echo off
@setlocal
@echo ----------------------------
@echo Perpetual Nautical Almanac Calculation
@echo and pdf generation
@echo ----------------------------
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
set /p genData=Generate Data [y]^|n ? ^> 
if /i [%genData%] == [n] goto processPDF
set /p from=From Year ^> 
set /p to=To Year   ^> 
@echo Generating Data...
@java -classpath %CP% app.perpetualalmanac.Publisher %from% %to% .\data.xml
:processPDF
set publishData=
set /p publishData=Publish Data  [y]^|n ? ^> 
if [%publishData%] == [n] goto end
@echo Processing PDF file
:: TODO Get option(s) here
@echo Publishing
set XSL_STYLESHEET=./perpetual.xsl
set PRM_OPTION=-docconf .\scalable.cfg
@java -Xms256m -Xmx1024m -classpath %CP% oracle.apps.xdo.template.FOProcessor %PRM_OPTION% -xml .\data.xml -xsl %XSL_STYLESHEET% -pdf perpetual.pdf
@echo Done calculating, displaying.
call perpetual.pdf
:end
::pause
@endlocal

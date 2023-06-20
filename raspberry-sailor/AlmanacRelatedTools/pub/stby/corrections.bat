@echo off
@setlocal
@echo ----------------------------
@echo Correction Calculation
@echo and pdf generation
@echo ----------------------------
rem
set OLIVSOFT_HOME=C:\_myWork\_ForExport\dev-corner\olivsoft
::
set CP=%OLIVSOFT_HOME%\AlmanacRelatedTools\classes
set CP=%CP%;%OLIVSOFT_HOME%\all-libs\nauticalalmanac.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-libs\geomutil.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xmlparserv2.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-collation.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\orai18n-mapping.jar
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\fnd2.zip
set CP=%CP%;%OLIVSOFT_HOME%\all-3rd-party\xdo-0301.jar
::
@java -classpath %CP% app.tables.CorrectionTables
@java -Xms256m -Xmx1024m -classpath %CP% oracle.apps.xdo.template.FOProcessor -xml .\corrections.xml -xsl corr-to-fo.xsl -pdf corrections.pdf
@echo Done calculating, displaying.
call corrections.pdf
:end
::pause
@endlocal

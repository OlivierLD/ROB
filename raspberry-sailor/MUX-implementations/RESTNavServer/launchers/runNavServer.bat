@echo off
@setlocal
:: Navigation REST server. WiP !!
::
@REM echo ----------------------------
@REM echo Usage is ${0} [-p|--proxy] [-m:propertiesfile|--mux:propertiesfile] [--no-date] [--sun-flower] --delta-t:[value]
@REM echo      -p or --proxy means with a proxy (proxy definition in the script ${0})
@REM echo      -m or --mux points to the properties file to use for the Multiplexer, default is nmea.mux.properties
@REM echo      -sf or --sun-flower means with Sun Flower option (extra Request Manager)
@REM echo      --http-verbose:true|false
@REM echo      --no-date does not put any GPS date or time (replayed or live) in the cache (allows you to use a ZDA generator)
@REM echo      --no-rmc-time will NOT set rmc time (only date & time). Useful when replaying data
@REM echo ----------------------------
::
echo - Starting the Navigation Rest Server
echo ----------------------------------------
echo Args are %*
echo ----------------------------------------
::
set CP=..\build\libs\RESTNavServer-1.0-all.jar
:: CP=$(find .. -name '*-all.jar')
::
:: OS=$(uname -a | awk '{ print $1 }')
@REM if [[ "${OS}" == "Darwin" ]]; then
@REM   CP=${CP}:./libs/RXTXcomm.jar # for Mac
@REM elif [[ "${OS}" == "Linux" ]]; then
@REM   CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi. Should already be in the fat-jar.
@REM fi
::
@REM Hard=coded for now
set PROP_FILE=mux-configs\nmea.mux.no.gps.yaml
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -Dmux.properties=%PROP_FILE%
@REM JAVA_OPTS="${JAVA_OPTS} -Djava.util.logging.config.file=logging.properties"
@REM JAVA_OPTS="${JAVA_OPTS} -Dwith.tide.coeffs=${WITH_TIDE_COEFFS}"
echo Using default DeltaT
set JAVA_OPTS=%JAVA_OPTS% -DdeltaT=AUTO
@REM #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=$HTTP_VERBOSE"
@REM #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
@REM #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose.dump=true"
@REM JAVA_OPTS="${JAVA_OPTS} -Dhttp.client.verbose=$HTTP_VERBOSE"
@REM #
@REM JAVA_OPTS="${JAVA_OPTS} -Dmux.infra.verbose=${INFRA_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=${TIDE_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dwith.tide.coeffs=${BREST_COEFF}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=${ASTRO_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dimage.verbose=${IMAGE_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dgrib.verbose=${GRIB_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dais.cache.verbose=${AIS_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Dais.verbose=${AIS_VERBOSE}"
@REM JAVA_OPTS="${JAVA_OPTS} -Drest.verbose=${REST_VERBOSE}"
@REM # Hard-coded ones:
@REM # JAVA_OPTS="${JAVA_OPTS} -Drest.nav.verbose=true"
@REM # JAVA_OPTS="${JAVA_OPTS} -Dnmea.utils.verbose=true"
@REM JAVA_OPTS="${JAVA_OPTS} -Drest.feeder.verbose=true"
@REM #
@REM if [[ "${USE_PROXY}" == "true" ]]; then
@REM   echo "Using proxy (hard-coded)"
@REM   JAVA_OPTS="${JAVA_OPTS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
@REM fi
echo --- Warning: Bumping Max Memory to 1Gb
set JAVA_OPTS=%JAVA_OPTS% -Xms64M -Xmx1G
::
@REM # For remote debugging:
@REM ## JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
@REM # JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"  # new
@REM # For remote JVM Monitoring
@REM # JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$(hostname)"
@REM #
echo -----------------------------
echo Using properties:%JAVA_OPTS%
echo -----------------------------
::
set SUDO=
@REM # DARWIN=`uname -a | grep Darwin`
@REM DARWIN=$(uname -a | grep Darwin)
@REM #
@REM if [[ "${DARWIN}" != "" ]]; then
@REM   echo Running on Mac
@REM   JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions" # for Mac
@REM else
@REM   echo Assuming Linux/Raspberry Pi
@REM   JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni" # RPi
@REM   # No sudo require if running as root, in Docker for example.
@REM   if [[ "$(whoami)" != "root" ]]; then
@REM     SUDO="sudo "
@REM   fi
@REM fi
::
@REM if [[ "${HTTP_PORT}" != "" ]]; then
@REM   JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${HTTP_PORT}"   # Use only if not in config file yet.
@REM fi
::
set COMMAND=java -cp %CP% %JAVA_OPTS% navrest.NavServer
@REM if [[ "${CMD_VERBOSE}" == "Y" || 1 -eq 1 ]]; then    # Always true...
echo Running %COMMAND%
@REM fi
::
%COMMAND%
::
echo Bye now!
::
@endlocal

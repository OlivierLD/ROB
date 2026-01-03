@echo off
@setlocal
:: Navigation REST server. WiP !!
::
@rem echo ----------------------------
@rem echo Usage is ${0} [-p|--proxy] [-m:propertiesfile|--mux:propertiesfile] [--no-date] [--sun-flower] --delta-t:[value]
@rem echo      -p or --proxy means with a proxy (proxy definition in the script ${0})
@rem echo      -m or --mux points to the properties file to use for the Multiplexer, default is nmea.mux.properties
@rem echo      -sf or --sun-flower means with Sun Flower option (extra Request Manager)
@rem echo      --http-verbose:true|false
@rem echo      --no-date does not put any GPS date or time (replayed or live) in the cache (allows you to use a ZDA generator)
@rem echo      --no-rmc-time will NOT set rmc time (only date & time). Useful when replaying data
@rem echo ----------------------------
::
echo - Starting the Navigation Rest Server
echo ----------------------------------------
echo Script args are %*
echo ----------------------------------------
:loopTop
if (%1) == () goto loopEnd
echo Managing param %1
shift
goto loopTop
:loopEnd
::
set CP=..\build\libs\RESTNavServer-1.0-all.jar
:: CP=$(find .. -name '*-all.jar')
::
:: OS=$(uname -a | awk '{ print $1 }')
@rem if [[ "${OS}" == "Darwin" ]]; then
@rem   CP=${CP}:./libs/RXTXcomm.jar # for Mac
@rem elif [[ "${OS}" == "Linux" ]]; then
@rem   CP=${CP}:/usr/share/java/RXTXcomm.jar # For Raspberry Pi. Should already be in the fat-jar.
@rem fi
::
@rem Hard=coded for now
set PROP_FILE=mux-configs\nmea.mux.no.gps.yaml
set JAVA_OPTS=
set JAVA_OPTS=%JAVA_OPTS% -Dmux.properties=%PROP_FILE%
@rem JAVA_OPTS="${JAVA_OPTS} -Djava.util.logging.config.file=logging.properties"
@rem JAVA_OPTS="${JAVA_OPTS} -Dwith.tide.coeffs=${WITH_TIDE_COEFFS}"
echo Using default DeltaT
set JAVA_OPTS=%JAVA_OPTS% -DdeltaT=AUTO
@rem #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=$HTTP_VERBOSE"
@rem #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose=true"
@rem #JAVA_OPTS="${JAVA_OPTS} -Dhttp.verbose.dump=true"
@rem JAVA_OPTS="${JAVA_OPTS} -Dhttp.client.verbose=$HTTP_VERBOSE"
@rem #
@rem JAVA_OPTS="${JAVA_OPTS} -Dmux.infra.verbose=${INFRA_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dtide.verbose=${TIDE_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dwith.tide.coeffs=${BREST_COEFF}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dastro.verbose=${ASTRO_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dimage.verbose=${IMAGE_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dgrib.verbose=${GRIB_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dais.cache.verbose=${AIS_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Dais.verbose=${AIS_VERBOSE}"
@rem JAVA_OPTS="${JAVA_OPTS} -Drest.verbose=${REST_VERBOSE}"
@rem # Hard-coded ones:
@rem # JAVA_OPTS="${JAVA_OPTS} -Drest.nav.verbose=true"
@rem # JAVA_OPTS="${JAVA_OPTS} -Dnmea.utils.verbose=true"
@rem JAVA_OPTS="${JAVA_OPTS} -Drest.feeder.verbose=true"
@rem #
@rem if [[ "${USE_PROXY}" == "true" ]]; then
@rem   echo "Using proxy (hard-coded)"
@rem   JAVA_OPTS="${JAVA_OPTS} -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80"
@rem fi
echo --- Warning: Bumping Max Memory to 1Gb
set JAVA_OPTS=%JAVA_OPTS% -Xms64M -Xmx1G
::
@rem # For remote debugging:
@rem ## JAVA_OPTS="${JAVA_OPTS} -client -agentlib:jdwp=transport=dt_socket,server=y,address=4000"
@rem # JAVA_OPTS="${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"  # new
@rem # For remote JVM Monitoring
@rem # JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=$(hostname)"
@rem #
echo -----------------------------
echo Using properties:%JAVA_OPTS%
echo -----------------------------
::
set SUDO=
@rem # DARWIN=`uname -a | grep Darwin`
@rem DARWIN=$(uname -a | grep Darwin)
@rem #
@rem if [[ "${DARWIN}" != "" ]]; then
@rem   echo Running on Mac
@rem   JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/Library/Java/Extensions" # for Mac
@rem else
@rem   echo Assuming Linux/Raspberry Pi
@rem   JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/lib/jni" # RPi
@rem   # No sudo require if running as root, in Docker for example.
@rem   if [[ "$(whoami)" != "root" ]]; then
@rem     SUDO="sudo "
@rem   fi
@rem fi
::
@rem if [[ "${HTTP_PORT}" != "" ]]; then
@rem   JAVA_OPTS="${JAVA_OPTS} -Dhttp.port=${HTTP_PORT}"   # Use only if not in config file yet.
@rem fi
::
set COMMAND=java -cp %CP% %JAVA_OPTS% navrest.NavServer
@rem if [[ "${CMD_VERBOSE}" == "Y" || 1 -eq 1 ]]; then    # Always true...
echo Running %COMMAND%
@rem fi
::
%COMMAND%
::
:endScript
echo Bye now!
::
@endlocal
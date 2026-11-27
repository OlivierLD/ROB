#!/usr/bin/env bash
#
# WIP. Runs on bash.
# Warning: If possible, run the process on the target machine.
#          That will avoid unwanted version mismatch (java class version...)
#
RED='\033[0;31;1m'                 # Red and Bold
GREEN="\033[1;32m"                 # Green and bold
YELLOW="\033[1;33m"                # Yellow and bold
BLUE="\033[1;34m"                  # Blue and bold
CYAN="\033[1;36m"                  # Cyan and bold
BOLD_GREEN_BLINK='\033[0;32;1;5m'  # Green, bold, blink.
NC='\033[0m'                       # Back to No Color
#
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "|                          P A C K A G E   f o r   D I S T R I B U T I O N                           |"
echo -e "| CLI parameters:                                                                                    |"
echo -e "|    ${YELLOW}--help${NC}  : Guess what!                                                                           |"
echo -e "|    ${YELLOW}--basic${NC} : Default, just Nav Server                                                              |"
echo -e "| or ${YELLOW}--big${NC}   : NavServer and all REST Services, including tide, astro, almanac publication, etc.     |"
echo -e "| ${YELLOW}--go${NC}       : NOT to be prompted step by step. This goes faster.                                    |"
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "| This is an EXAMPLE showing how to generate a 'production' version, without having the full github  |"
echo -e "| repo on the destination machine. We will deploy only what is needed to run the NMEA Multiplexer,   |"
echo -e "| possibly with several configurations - and its web clients.                                        |"
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "| Now you may start a fresh build...                                                                 |"
echo -e "| Make sure the java version is compatible with your target. Current version:                        |"
echo -e "+----------------------------------------------------------------------------------------------------+"
java -version > jvers.txt 2>&1
while read line; do
  echo -e "| ${line}"
done < jvers.txt
rm jvers.txt
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "| Make sure the current Java version is compatible with the target one!!                             |"
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e ""
#
OPTION=BASIC        # Default value
GO_FOR_IT=false
#
if [[ $# -gt 0 ]]; then
  for ARG in "$@"; do
    echo -e "Managing prm ${ARG}"
#    if [[ "${ARG}" == "--tiny:"* ]]; then
#      TINY=${ARG#*:}
#    fi
    # echo -e "CLI prm: $1"
    if [[ "${ARG}" == "--basic" ]]; then
      OPTION=BASIC
    elif [ "${ARG}" == "--big" ]; then
      OPTION=BIG
    elif [ "${ARG}" == "--go" ]; then
      GO_FOR_IT=true
      echo -e "We'll go for it!!"
    else
      if [ "${ARG}" != "--help" ] && [ "${ARG}" != "-h" ] && [ "${ARG}" != "?" ]; then
        echo -e "Unmanaged CLI parameter ${1}"
      fi
      echo -e "Managed parameters are:"
      echo -e "$0 --basic (default, just Nav Server)"
      echo -e "$0 --big   (all REST Services, including tide, astro, almanac publication, etc)"
      echo -e "$0 --go    (not prompted step by step)"
      echo -e "$0 --help  (guess what)"
      exit 1
    fi
  done
fi
#
if [[ "${GO_FOR_IT}" == "false" ]]; then
  echo -e ">> You'll be prompted step by step."
fi
#
echo -e "We will use ${OPTION} option."
echo -e "------------------------------------"
#
# 1 - Build
#
# Set the proxy if needed, for maven access during the build.
PROXY_SETTINGS=
# PROXY_SETTINGS="-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80"
#
REBUILD_REQUEST=Y
#
SMALL_DIR=$(realpath ../NMEA-multiplexer-basic)
BIG_DIR=$(realpath ../RESTNavServer)
#
if [[ "${OPTION}" == "BASIC" ]]; then
  JAR_NAME=${SMALL_DIR}/build/libs/NMEA-multiplexer-basic-1.0-all.jar
elif [[ "${OPTION}" == "BIG" ]]; then
  JAR_NAME=${BIG_DIR}/build/libs/RESTNavServer-1.0-all.jar
fi
#
if [[ -f ${JAR_NAME} ]]; then
  echo -e "There is an existing jar-file:"
  ls -lisah ${JAR_NAME}
  echo -e "With the following MANIFEST:"
  ./type.manifest.sh ${JAR_NAME}
  # echo -e "----------------------------"
  echo -en "Do we re-build the Java part y|[n] ? > "
  read REPLY
  if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Ok, moving on."
    REBUILD_REQUEST=N
  fi
  echo -e "----------------------------"
fi
#
if [[ "${REBUILD_REQUEST}" == "Y" ]]; then
  echo -e "Rebuilding from source (No Scala, no Kotlin)..."
  if [[ "${OPTION}" == "BASIC" ]]; then
    pushd ${SMALL_DIR}
      ../../../gradlew clean shadowJar -x :astro-computer:AstroComputer:compileScala -x :astro-computer:AstroComputer:compileKotlin ${PROXY_SETTINGS}
    popd
  else
    pushd ${BIG_DIR}
      ../../../gradlew clean shadowJar -x :astro-computer:AstroComputer:compileScala -x :astro-computer:AstroComputer:compileKotlin ${PROXY_SETTINGS}
    popd
  fi
fi
#
# 2 - Create new dir
#
echo -e "You will need to provide a name for your archive, that will also be the name of the folder where the runtime components will live."
echo -e "A name like 'nmea-dist' would do the job..."
echo -en "Which (non existent) folder should we create the distribution in ? > "
# Directory name, that will become the archive name.
read distdir
if [[ -d "${distdir}" ]]; then
	echo -e "Folder ${distdir} exists. Please drop it or choose another name."
	echo -e "Exiting."
	exit 1
fi
echo -e "-> Creating folder ${distdir}"
mkdir ${distdir}
mkdir ${distdir}/build
mkdir ${distdir}/build/libs
mkdir ${distdir}/polars
mkdir ${distdir}/scripts
mkdir ${distdir}/resources
#
# 3 - Copying required resources
#
echo -e "Copying resources..."
if [[ "${OPTION}" == "BASIC" ]]; then
  echo -e "Using jar-file:"
  ls -lisa ${SMALL_DIR}/build/libs/NMEA-multiplexer-basic-1.0-all.jar
  cp ${SMALL_DIR}/build/libs/NMEA-multiplexer-basic-1.0-all.jar ${distdir}/build/libs/mux-all.jar
else
  echo -e "Using jar-file:"
  ls -lisa ${BIG_DIR}/build/libs/RESTNavServer-1.0-all.jar
  cp ${BIG_DIR}/build/libs/RESTNavServer-1.0-all.jar ${distdir}/build/libs/mux-all.jar
fi
# Log folder
# mkdir ${distdir}/logged
# Web resources, zipped. To see the content: unzip -vl web.zip
# cp -R web ${distdir}

# Go to the right web folder. The extra stuff for tides, astro, fromSpace, etc...
TO_PROD_DIR=$PWD
echo -e "Working from ${TO_PROD_DIR}"
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
mkdir ${distdir}/web
echo -e "${distdir}/web folder created"
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
if [[ "${OPTION}" == "BASIC" ]]; then
  pushd ${SMALL_DIR}
    pushd web
      cp -rf * ${TO_PROD_DIR}/${distdir}/web
      echo -e "Star Wars index... to be fixed"
      cp index.star.wars.html index.html  # Default index becomes star wars. Could depend on the option (big or not)
    popd
  popd
else
  pushd ${BIG_DIR}/launchers
    pushd web
      echo -e "Copying web content from ${PWD}"
      if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
      cp -rf * ${TO_PROD_DIR}/${distdir}/web
      cp ${SMALL_DIR}/web/index.star.wars.html ${TO_PROD_DIR}/${distdir}/web/index.html
    popd
  popd
fi
#
# Remove the unwanted resources from the web folder...
echo -e "From ${PWD} : removing unwanted stuff... rm -r ${distdir}/web/20* etc."
rm -r ${distdir}/web/20* 2> /dev/null
rm -r ${distdir}/web/scratch 2> /dev/null
rm -r ${distdir}/web/catalog 2> /dev/null
rm -r ${distdir}/web/samples 2> /dev/null
rm -r ${distdir}/web/json 2> /dev/null
rm -r ${distdir}/web/misc.stuff 2> /dev/null
rm -r ${distdir}/web/weather.wizard 2> /dev/null
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
cd ${TO_PROD_DIR}/${distdir}/web
echo -e "Now in ${PWD}..."
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
find . -name '.DS_Store' -exec rm {} \;
#
if [[ "${OPTION}" == "BASIC" ]]; then
  echo -e "Zipping from $(pwd) ..."
  zip -r ${TO_PROD_DIR}/${distdir}/web.zip *
  cd ..
  echo -e "web.zip created, removing the web folder."
  if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
  rm -r ${TO_PROD_DIR}/${distdir}/web
  echo -e "web folder removed."
else
  cd ..
  echo -e "NO web.zip created."
fi
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
cd ${TO_PROD_DIR}
echo -e "Copying extra stuff..., from ${PWD}"
cp -r ${SMALL_DIR}/scripts/* ${TO_PROD_DIR}/${distdir}/scripts
cp -r ${SMALL_DIR}/resources/* ${TO_PROD_DIR}/${distdir}/resources
# Properties files
cp ${SMALL_DIR}/*.properties ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/*.yaml ${TO_PROD_DIR}/${distdir}
if [[ "${OPTION}" == "BIG" ]]; then
  cp -R ${BIG_DIR}/launchers/mux-configs ${TO_PROD_DIR}/${distdir}
fi
# If needed, more resources would go here (like dev curves, etc)
cp ${SMALL_DIR}/mux.sh ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/runNavServer.sh ${TO_PROD_DIR}/${distdir}
# cp mk.link.sh ${distdir}
cp ${SMALL_DIR}/kill.all.sample.sh ${TO_PROD_DIR}/${distdir}/kill.all.sh
cp ${SMALL_DIR}/show.processes.sh ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/start.all.sh ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/log.*.sh ${TO_PROD_DIR}/${distdir}
cp -r ${SMALL_DIR}/polars/* ${TO_PROD_DIR}/${distdir}/polars
cp ${SMALL_DIR}/zero*.csv ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/dp_2011*.csv ${TO_PROD_DIR}/${distdir}
cp ../../NMEA-multiplexer/find.port.sh ${TO_PROD_DIR}/${distdir}
# cp tomux.sh ${distdir}
# cp killmux.sh ${distdir}
if [[ "${OPTION}" == "BIG" ]]; then
  mkdir ${TO_PROD_DIR}/${distdir}/pub
  mkdir ${TO_PROD_DIR}/${distdir}/libs
  mkdir ${TO_PROD_DIR}/${distdir}/xsl
  #
  cp -R ${BIG_DIR}/launchers/pub/* ${TO_PROD_DIR}/${distdir}/pub
  cp -R ${BIG_DIR}/launchers/libs/* ${TO_PROD_DIR}/${distdir}/libs
  cp -R ${BIG_DIR}/launchers/xsl/* ${TO_PROD_DIR}/${distdir}/xsl
fi
#
# The Python part...
PACKAGE_PYTHON=Y
echo -en "Do we package the Python part ? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, skipping python."
  PACKAGE_PYTHON=N
fi
if [[ "${PACKAGE_PYTHON}" == "Y" ]]; then
  # Packaging Python scripts
  PYTHON_SRC_DIR=RaspberryPythonServers
  # TODO Make sure the Python resources were copied from NMEA-Parser
  mkdir ${TO_PROD_DIR}/${distdir}/python
  echo -e "Copying Python stuff from ../../${PYTHON_SRC_DIR}/python into ${TO_PROD_DIR}/${distdir}/python"
  cp -R ${TO_PROD_DIR}/../../${PYTHON_SRC_DIR}/python/ ${TO_PROD_DIR}/${distdir}/python
  rm -rf ${TO_PROD_DIR}/${distdir}/python/__pycache__
  echo -e "Done with Python"
  echo -e "Do look at the script named install.all.sh for the dependencies..."
fi
#
# 4 - Archiving
#
cd ${TO_PROD_DIR}
find . -name '.DS_Store' -exec rm {} \;
echo -e "Now archiving the whole stuff... from ${PWD}"
if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
# zip -q -r ${distdir}.zip ${distdir}
tar -cvzf ${distdir}.tar.gz ${distdir}
# Drop directory ?
echo -e "Archive ${distdir}.tar.gz was generated:"
ls -lisah ${distdir}.tar.gz
echo -en "Do you want to run a first basic test ? y|[n] > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Server are going to start. Use Ctrl+C to stop them..."
  # echo -en "Hit return..."; read DUMMY
  #
  pushd ${distdir}
    if [[ "${OPTION}" == "BASIC" ]]; then
      echo -e "Once the server is started, from a browser reach"
      echo -e "    http://localhost:9999/zip/index.html ..."
      echo -en "Hit return..."; read DUMMY
      #
      ./mux.sh nmea.mux.basics.yaml
    else
      echo -e "Once the server is started, from a browser reach"
      echo -e "    http://localhost:9999/web/index.html"
      echo -e "    http://localhost:9999/web/index.prod.html ..."
      echo -e " or curl -X GET http://localhost:9999/oplist ..."
      echo -en "Hit return..."; read DUMMY
      #
      ./runNavServer.sh --mux:nmea.mux.basics.yaml
    fi
  popd
  echo -e "Server test completed."
  echo -en "Hit return..."; read DUMMY
fi
#
echo -en "Can we drop the ${distdir} directory ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, moving on, dropping ${distdir}."
  rm -rf ${distdir}
else
  echo -e "OK. This will be your responsibility to clean it up."
  echo -e "To test the distrib, from ${distdir}, you can run:"
  echo -e "./mux.sh nmea.mux.basics.yaml (standard option) "
  echo -e " or "
  echo -e "./runNavServer.sh --mux:nmea.mux.basics.yaml (--big option) "
  echo -e "- Use Ctrl+C to stop it."
  echo -e "    ./mux.sh nmea.mux.basics.yaml (standard option) - Ctrl+C to stop it"
  echo -e "       or "
  echo -e "    ./runNavServer.sh --mux:nmea.mux.basics.yaml (--big option) - Ctrl+C to stop it"
  echo -e "    Then, for a first basic test, from a browser (on the machine you generated the distrib, or the target one), reach"
  echo -e "    http://localhost:9999/web/index.html, or http://localhost:9999/zip/index.html ..."
  echo -e "    Also http://localhost:9999/web/index.star.wars.html, or http://localhost:9999/zip/index.html ..."
  echo -e "         http://localhost:9999/web/index.prod.html, curl -X GET http://localhost:9999/oplist ..."
  #
  if [[ "${GO_FOR_IT}" == "false" ]]; then echo -en "Hit return..."; read DUMMY; fi
fi
#
# 5 - Ready!
#
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e " >> ${BOLD_GREEN_BLINK}Archive $PWD/${distdir}.tar.gz ready for deployment.${NC}"
echo -e " >> size $(ls -lh ${PWD}/${distdir}.tar.gz | awk '{ print $5 }')"
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e "| Send it to another machine (with scp for example), and un-archive it.                            |"
echo -e "| Use 'tar -xzvf ${distdir}.tar.gz' to un-archive.                                                  "
echo -e "| External dependencies like librxtx-java may be needed if you intend to use a serial port,        |"
echo -e "| in which case you may need to run a 'sudo apt-get install librxtx-java' .                        |"
if [[ "${PACKAGE_PYTHON}" == "Y" ]]; then
  echo -e "| > For the python scripts, make sure you run (once) the script ./python/scripts/install.all.sh    |"
  echo -e "| > after unzipping the archive on the target machine.                                             |"
  echo -e "| > In case you see an \"Externally Managed\" message,                                               |"
  echo -e "|   see https://www.makeuseof.com/fix-pip-error-externally-managed-environment-linux/ ...          |"
  echo -e "|   cd /usr/lib/python3.11 && sudo rm EXTERNALLY-MANAGED                                           |"
fi
echo -e "| The script to launch will be 'mux.sh'                                                            |"
echo -e "| It is your responsibility to use the right properties file, possibly modified to fit your needs. |"
echo -e "| For the runner/logger, use nmea.mux.gps.tcp.properties                                           |"
echo -e "| Use it - for example - like:                                                                     |"
echo -e "| $ cd ${distdir}                                                                                   "
echo -e "| $ nohup ./mux.sh nmea.mux.gps.tcp.yaml &                                                         |"
echo -e "| The script start.all.sh can also be invoked from an /etc/rc.local, it can be tweak for your      |"
echo -e "|                                                        configuration (extra Python servers, etc) |"
echo -e "|  >> Static web resources can be reached like http://<host>:<port>/zip/index.html                 |"
echo -e "| Examples of /etc/rc.local are provided. See rc.local*.sh, in the 'scripts' directory.            |"
echo -e "| The script 'start.all.sh' is worth a look too.                                                   |"
echo -e "| Once the server is started, try http://<host>:<port>/zip/index.html from a browser...            |"
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e "PS: Current folder is ${PWD} "
echo -e ""
if [[ 1 == 2 ]]; then
  echo -e "For tests, you can run (form the distrib folder, if not dropped):"
  echo -e "    ./mux.sh nmea.mux.basics.yaml (standard option) - Ctrl+C to stop it"
  echo -e "       or "
  echo -e "    ./runNavServer.sh --mux:nmea.mux.basics.yaml (--big option) - Ctrl+C to stop it"
  echo -e "    Then, for a first basic test, from a browser (on the machine you generated the distrib, or the target one), reach"
  echo -e "    http://localhost:9999/web/index.html, or http://localhost:9999/zip/index.html ..."
  echo -e "    Also http://localhost:9999/web/index.star.wars.html, or http://localhost:9999/zip/index.html ..."
  echo -e "         http://localhost:9999/web/index.prod.html, curl -X GET http://localhost:9999/oplist..."
fi
#
# Also see ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/DRY_RUN.md
# Yo!
#
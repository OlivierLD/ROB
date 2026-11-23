#!/usr/bin/env bash
#
# WIP
# Warning: If possible, run the process on the target machine.
#          That will avoid unwanted version mismatch (java class version...)
#
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "|                          P A C K A G E   f o r   D I S T R I B U T I O N                           |"
echo -e "| CLI options:                                                                                       |"
echo -e "|    --help  (guess what)                                                                            |"
echo -e "|    --basic (default, just Nav Server)                                                              |"
echo -e "| or --big   (all REST Services, including tide, astro, etc. But no Almanac publication)             |"
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
if [[ $# -gt 0 ]]; then
  # echo -e "CLI prm: $1"
  if [[ "$1" == "--basic" ]]; then
    OPTION=BASIC
  elif [ "$1" == "--big" ]; then
    OPTION=BIG
  else
    if [ "$1" != "--help" ]; then
      echo -e "Unmanaged CLI parameter ${1}"
    fi
    echo -e "Managed parameters are:"
    echo -e "$0 --basic (default, just Nav Server)"
    echo -e "$0 --big   (all REST Services, including tide, astro, etc. But no Almanac publication)"
    echo -e "$0 --help  (guess what)"
    exit 1
  fi
else
  if [[ 1 == 0 ]]; then
    echo -e "+----------------------------------------------------------------------------------------+"
    echo -e "| CLI options:                                                                           |"
    echo -e "|    --help  (guess what)                                                                |"
    echo -e "|    --basic (default, just Nav Server)                                                  |"
    echo -e "| or --big   (all REST Services, including tide, astro, etc. But no Almanac publication) |"
    echo -e "+----------------------------------------------------------------------------------------+"
    echo -e "We will you ${OPTION}."
    echo -e "------------------------------------"
  fi
fi
#
echo -e "We will use ${OPTION}."
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
SMALL_DIR=../NMEA-multiplexer-basic
BIG_DIR=../RESTNavServer
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
#echo -en "1 - Hit return..."
#read AKEU
mkdir ${distdir}/web
echo -en "2 - Hit return..."
read AKEU
if [[ "${OPTION}" == "BASIC" ]]; then
  pushd ${SMALL_DIR}
    pushd web
      cp -rf * ${TO_PROD_DIR}/${distdir}/web
    popd

#    zip -r ${TO_PROD_DIR}/${distdir}/web.zip *
#    cp index.star.wars.html index.html  # Default index becomes star wars. Could depend on the option (big or not)
#    zip ${TO_PROD_DIR}/${distdir}/web.zip index.html
#    rm index.html
  popd
else
  pushd ${BIG_DIR}/launchers
    pushd web
      cp -rf * ${TO_PROD_DIR}/${distdir}/web
    popd
#    zip -r ${TO_PROD_DIR}/${distdir}/web.zip *
#    #
#    echo -e "Star Wars index... to be fixed"
#    # cp index.star.wars.html index.html  # Default index becomes star wars. Could depend on the option (big or not)
#    zip ${TO_PROD_DIR}/${distdir}/web.zip index.html
#    rm index.html
  popd
fi
#
# Remove the unwanted resources...
echo -e "From ${PWD} : rm -r ${distdir}/web/20*"
rm -r ${distdir}/web/20*
echo -en "3 - Hit return..."
read AKEU
cd ${TO_PROD_DIR}/web
zip -r ${TO_PROD_DIR}/${distdir}/web.zip *
cd ..
echo -en "4 - Hit return..."
read AKEU
rm -r ${TO_PROD_DIR}/${distdir}/web
echo -en "5 - Hit return..."
read AKEU
cd ${TO_PROD_DIR}
#
echo -e "Copying extra stuff..., from ${PWD}"
cp -r ${SMALL_DIR}/scripts/* ${TO_PROD_DIR}/${distdir}/scripts
cp -r ${SMALL_DIR}/resources/* ${TO_PROD_DIR}/${distdir}/resources
# Properties files
cp ${SMALL_DIR}/*.properties ${TO_PROD_DIR}/${distdir}
cp ${SMALL_DIR}/*.yaml ${TO_PROD_DIR}/${distdir}
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
echo -e "Now archiving... from $(pwd)"
# zip -q -r ${distdir}.zip ${distdir}
tar -cvzf ${distdir}.tar.gz ${distdir}
# Drop directory ?
echo -e "Archive ${distdir}.tar.gz was generated:"
ls -lisah ${distdir}.tar.gz
echo -en "Can we drop the ${distdir} directory ? > "
read REPLY
if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo -e "Ok, moving on, dropping ${distdir}."
  rm -rf ${distdir}
else
  echo -e "OK. This will be your responsibility to clean it up."
  echo -e "To test the distrib, from ${distdir}, you can run:"
  echo -e "./mux.sh nmea.mux.basics.yaml "
  echo -e "- Use Ctrl+C to stop it."
fi
#
# 5 - Ready!
#
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e " >> Archive $PWD/${distdir}.tar.gz ready for deployment."
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
echo -e "|  >> Static web resources can be reached like http://<host>:<port>/zip/index.html                 |"
echo -e "| Examples of /etc/rc.local are provided. See rc.local*.sh, in the 'scripts' directory.            |"
echo -e "| The script 'start.all.sh' is worth a look too.                                                   |"
echo -e "| Once the server is started, try http://<host>:<port>/zip/index.html from a browser...            |"
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e "PS: Current folder is ${PWD} "
echo -e ""
echo -e "For tests, you can run (form the distrib folder, if not dropped):"
echo -e "    ./mux.sh nmea.mux.basics.yaml   (Ctrl+C to stop it)"
echo -e "    Then, for a first basic test, from a browser (on the machine you generated the distrib, or the target one), reach"
echo -e "    http://localhost:9999/web/index.html, or http://localhost:9999/zip/index.html ..."
echo -e "    Also http://localhost:9999/web/index.star.wars.html, or http://localhost:9999/zip/index.html ..."
#
# Also see ROB/raspberry-sailor/MUX-implementations/NMEA-multiplexer-basic/DRY_RUN.md
# Yo!
#
#!/usr/bin/env bash
#
# WIP
# Warning: Run the process on the target machine. That will avoid unwanted version mismatch (java class version...)
#
echo -e "+----------------------------------------------------------------------------------------------------+"
echo -e "|                          P A C K A G E   f o r   D I S T R I B U T I O N                           |"
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
# 1 - Build
#
# Set the proxy if needed, for maven access during the build.
PROXY_SETTINGS=
# PROXY_SETTINGS="-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80"
#
REBUILD_REQUEST=Y
if [[ -f ./build/libs/NMEA-multiplexer-basic-1.0-all.jar ]]; then
  echo -e "There is an existing jar-file:"
  ls -lisah ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
  echo -e "With the following MANIFEST:"
  ./type.manifest.sh ./build/libs/NMEA-multiplexer-basic-1.0-all.jar
  echo -e "----------------------------"
  echo -en "Do we re-build the Java part ? > "
  read REPLY
  if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Ok, moving on."
    REBUILD_REQUEST=N
  fi
fi
#
if [[ "${REBUILD_REQUEST}" == "Y" ]]; then
  echo -e "Rebuilding from source (No Scala)..."
  ../../../gradlew clean shadowJar -x :astro-computer:AstroComputer:compileScala ${PROXY_SETTINGS}
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
echo -e "Creating folder ${distdir}"
mkdir ${distdir}
mkdir ${distdir}/build
mkdir ${distdir}/build/libs
#
# 3 - Copying required resources
#
echo -e "Copying resources"
cp ./build/libs/NMEA-multiplexer-basic-1.0-all.jar ${distdir}/build/libs
# Log folder
# mkdir ${distdir}/logged
# Web resources, zipped. To see the content: unzip -vl web.zip
# cp -R web ${distdir}
cd web
zip -r ../${distdir}/web.zip *
cd ..
#
# Properties files
cp *.properties ${distdir}
cp *.yaml ${distdir}
# If needed, more resources would go here (like dev curves, etc)
cp mux.sh ${distdir}
cp kill.all.sample.sh ${distdir}
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
  mkdir ${distdir}/python
  echo -e "Copying Python stuff from ${PYTHON_SRC_DIR}/python into ${distdir}/python"
  cp -R ../../${PYTHON_SRC_DIR}/python/ ${distdir}/python
  rm -rf ${distdir}/python/__pycache__
  echo -e "Done with Python"
fi
#
# 4 - Archiving
#
# zip -q -r ${distdir}.zip ${distdir}
tar -cvzf ${distdir}.tar.gz ${distdir}
# Drop directory
rm -rf ${distdir}
#
# 5 - Ready!
#
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e " >> Archive $PWD/${distdir}.tar.gz ready for deployment."
echo -e "+--------------------------------------------------------------------------------------------------+"
echo -e "| Send it to another machine, and un-archive it.                                                   |"
echo -e "| Use 'tar -xzvf ${distdir}.tar.gz' to un-archive.                                                   |"
echo -e "| External dependencies like librxtx-java may be needed if you intend to use a serial port,        |"
echo -e "| in which case you may need to run a 'sudo apt-get install librxtx-java' .                        |"
if [[ "${PACKAGE_PYTHON}" == "Y" ]]; then
  echo -e "| > For the python scripts, make sure you run (once) the script ./python/scripts/install.all.sh    |"
  echo -e "| > after unzipping the archive on the target machine.                                             |"
fi
echo -e "| The script to launch will be 'mux.sh'                                                            |"
echo -e "| It is your responsibility to use the right properties file, possibly modified to fit your needs. |"
echo -e "| For the runner/logger, use nmea.mux.gps.tcp.properties                                           |"
echo -e "| Use it - for example - like:                                                                     |"
echo -e "| $ cd ${distdir}                                                                                   "
echo -e "| $ nohup ./mux.sh nmea.mux.gps.tcp.yaml &                                                         |"
echo -e "|  >> Static web resources can be reached like http://<host>:<port>/zip/index.html                 |"
echo -e "+--------------------------------------------------------------------------------------------------+"

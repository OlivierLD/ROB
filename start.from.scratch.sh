#!/bin/bash
#
# This is an example, a work in progress,
# use it at your own risks !
# Written for Raspberry Pi, Debian, Ubuntu.
#
# Can be used like:
# wget https://github.com/OlivierLD/ROB/raw/master/start.from.scratch.sh
# chmod +x ./start.from.scratch.sh
# ./start.from.scratch.sh
#
die ( ) {
    echo "--------------------"
    echo "$*"
    echo "--------------------"
    exit 1
}
# 1. See if JDK is available
JDK_TO_BE_INSTALLED=true
JAVA_TO_FIND=javac
if [[ "$(which ${JAVA_TO_FIND})" != "" ]]; then
  # Java(c) is here, version ?
  # JAVA_VERSION=$(javac -version 2>&1 | awk -F '"' '/version/ {print $2}')
  JAVA_VERSION=$(${JAVA_TO_FIND} -version | awk '{ print $2 }')
  MAJOR_VERSION=$(echo ${JAVA_VERSION} | awk -F'.' '{print $1}')
  if [[ ${MAJOR_VERSION} -ge 11 ]]; then
    echo -e "Major JDK version is ${MAJOR_VERSION}, good."
    JDK_TO_BE_INSTALLED=false
  fi
else
  echo -e "No JDK found."
fi
if [[ "${JDK_TO_BE_INSTALLED}" == "true" ]]; then
  # Install JDK 11
  echo -e "JDK is required"
  echo -en "Do we install it now? > "
  read REPLY
  if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      echo -e "Ok, moving on."
      sudo apt-get update
      sudo apt-get install openjdk-11-jdk
  fi
fi
#
# Git ?
#
GIT_TO_BE_INSTALLED=true
if [[ "$(which git)" != "" ]]; then
  echo -e "Git was found. OK."
  GIT_TO_BE_INSTALLED=false
else
  echo -e "No Git found."
fi
if [[ "${GIT_TO_BE_INSTALLED}" == "true" ]]; then
  # Install GIT
  echo -e "GIT is required"
  echo -en "Do we install it now? > "
  read REPLY
  if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      echo -e "Ok, moving on."
      sudo apt-get install git-all
  fi
fi
#
# librxtx-java
#
sudo apt-get install librxtx-java
#
# Clone the repo
#
mkdir repos
pushd repos
git clone https://github.com/OlivierLD/ROB.git
#
cd ROB
# Build the module RESTNavServer
cd raspberry-sailor/MUX-implementations/RESTNavServer
../../../gradlew shadowJar -x :astro-computer:AstroComputer:compileScala || die "The build went wrong, stopping there."
# Start the demo menu
cd launchers
./demoLauncher.sh
popd
#
# Done with this script
#

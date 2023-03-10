#!/bin/bash
#
# This is an example, a work in progress,
# use it at your own risks !
# Written for bash, on Raspberry Pi, Debian, Ubuntu.
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
UNAME=$(uname)
#
if [[ "${UNAME}" != "Linux" ]]; then
  echo -e "Warning! If some soft is to be installed, this will work for Linux,"
  echo -e "and you're running on ${UNAME}."
  echo -en "Hit [return] to move on, or [Ctrl + C] to cancel."
  read REPLY
fi
# 1. See if JDK is available
JDK_TO_BE_INSTALLED=true
JAVA_TO_FIND=javac
if [[ "$(which ${JAVA_TO_FIND})" != "" ]]; then
  # Java(c) is here, version ?
  # JAVA_VERSION=$(javac -version 2>&1 | awk -F '"' '/version/ {print $2}')
  JAVA_VERSION=$(${JAVA_TO_FIND} -version | awk '{ print $2 }')
  MAJOR_VERSION=$(echo ${JAVA_VERSION} | awk -F'.' '{print $1}')
  if [[ ${MAJOR_VERSION} -ge 8 ]]; then
    echo -e "Major JDK version is ${MAJOR_VERSION}, good."
    echo -en "Does it match the JDK installed on your target machine (like a Raspberry Pi) ? > "
    read REPLY
    if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      JDK_TO_BE_INSTALLED=false
    else
      JDK_TO_BE_INSTALLED=true
    fi
  fi
else
  echo -e "No JDK found."
fi
if [[ "${JDK_TO_BE_INSTALLED}" == "true" ]]; then
  # Install JDK 11 or 8
  echo -e "JDK is required"
  echo -en "Install it now? > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      echo -e "Ok, we need to install a JDK."
      echo -e "What JDK do we install here ? Version 8 or 11 ?"
      KEEP_ASKING=true
      JDK_VERSION=
      while [[ "${KEEP_ASKING}" == "true" ]]; do
        echo -en "Say 8, 11, or 'Q' to quit > "
        read REPLY
        echo -e "... You said ${REPLY}."
        case "${REPLY}" in
          "8")
            JDK_VERSION=8
            KEEP_ASKING=false
            ;;
          "11")
            JDK_VERSION=11
            KEEP_ASKING=false
            ;;
          "q" | "Q")
            echo -e "Stopping here."
            exit 0
            ;;
          *)
            echo -e "Unsupported user input. Read again."
            ;;
        esac
      done
      sudo apt-get update
      if [[ "${JDK_VERSION}" == "11" ]]; then
        sudo apt-get install openjdk-11-jdk
      fi
      if [[ "${JDK_VERSION}" == "8" ]]; then
        sudo apt-get install openjdk-8-jdk
      fi
  else
      echo -e "Skipping JDK installation."
  fi
fi
#
echo -en "Ready to move on ? > "
read REPLY
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
  echo -en "Install it now? > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
      echo -e "Ok, installing Git."
      sudo apt-get install git-all
  else
      echo -e "Skipping git installation."
  fi
fi
#
# librxtx-java, on Linux.
#
sudo apt-get install librxtx-java
#
# Clone the repo
#
mkdir repos
pushd repos

# echo warning about the size !
echo -e "The GIT repo is about to be cloned in ${PWD}."
echo -en "This will be roughly 1.4 Gb (Feb-2023), do we proceed ? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
    echo -e "Ok, stopping here."
    exit 0
fi
#
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


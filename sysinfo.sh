#!/bin/bash
#
# Move this to your $HOME directory, and modify the FOLDER_NAME variable below accordingly, if needed
# Compare to rpi.status.sh ;)
#
echo -e "All Raspberry Pi Models' description available at https://www.raspberrypi.org/products/"
echo -e ""
#
TINY=false
for ARG in "$@"; do
  echo -e "Managing prm ${ARG}"
  if [[ "${ARG}" == "--tiny:"* ]]; then
    TINY=${ARG#*:}
  fi
done
# echo -e "Tiny: ${TINY} ...!"
#
FOLDER_NAME=~/repos/ROB/common-utils
ARCHIVE_NAME=${FOLDER_NAME}/build/libs/common-utils-1.0-all.jar
if [[ ! -f ${ARCHIVE_NAME} ]]; then
  ARCHIVE_NAME=${FOLDER_NAME}/build/libs/common-utils-1.0.jar
fi
if [[ -f ${ARCHIVE_NAME} ]]; then
  echo -e "(Using jar ${ARCHIVE_NAME})"
  if [[ "${TINY}" == "true" ]]; then
    java -cp ${ARCHIVE_NAME} utils.SystemUtils --minimal --no-free-mem --tiny
  else
    java -cp ${ARCHIVE_NAME} utils.SystemUtils --minimal --no-free-mem # --tiny
  fi
else
  pushd ${FOLDER_NAME}
    # Will run a build before a run
    if [[ "${TINY}" == "true" ]]; then
      ../gradlew run -P--minimal -P--no-free-mem -P--tiny
    else
      ../gradlew run -P--minimal -P--no-free-mem # --tiny
    fi
  popd
fi
#
echo -e "-------------------------"
echo -e "uname -m: $(uname -m)"
echo -e "-------------------------"
lscpu
echo -e "-------------------------"
echo -e "Architecture: $(getconf LONG_BIT) bits"
echo -e "-------------------------"
file /lib/systemd/systemd
echo -e "-------------------------"
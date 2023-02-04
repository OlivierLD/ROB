#!/bin/bash
#
# A playground
#
echo -e "Script is in $(dirname $0)"

# Stolen from gradlew
die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

FIND_THIS=java
# FIND_THIS=tagada
which ${FIND_THIS} || die "Ah ben merdalor! No ${FIND_THIS}!"

MACHINE_NAME=
if MACHINE_NAME=$(hostname -I); then
    echo -e "It worked: ${MACHINE_NAME}"
else
    MACHINE_NAME=$(hostname)
    echo -e "Plan B: ${MACHINE_NAME}"
fi
echo -e "Machine name or IP: [${MACHINE_NAME}]"
MACHINE_NAME="$(echo ${MACHINE_NAME})"
echo -e "Trimmed machine name or IP: [${MACHINE_NAME}]"

# A test: move to the place where the script runs from
echo -e "Starting from $PWD"
pushd $(dirname $0)
  echo -e "Now is $PWD"
  ls -lisa
popd
echo -e "Back in $PWD"

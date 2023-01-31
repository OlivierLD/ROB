#!/bin/bash
echo -e "Script is in $(dirname $0)"

MACHINE_NAME=
if MACHINE_NAME=$(hostname -I) ; then
    echo -e "It worked: ${MACHINE_NAME}"
else
    MACHINE_NAME=$(hostname)
    echo -e "Plan B: ${MACHINE_NAME}"
fi

# A test: move to the place where the script runs from
echo -e "Starting from $PWD"
pushd $(dirname $0)
  echo -e "Now is $PWD"
  ls -lisa
popd

#!/bin/bash
echo -e "Script is in $(dirname $0)"

MACHINE_NAME=
if MACHINE_NAME=$(hostname -I) ; then
    echo -e "It worked: ${MACHINE_NAME}"
else
    MACHINE_NAME=$(hostname)
    echo -e "Plan B: ${MACHINE_NAME}"
fi


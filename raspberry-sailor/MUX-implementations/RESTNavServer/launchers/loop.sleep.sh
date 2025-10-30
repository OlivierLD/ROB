#!/bin/bash
#
# An example...
#
stty -F /dev/ttyACM0 raw 4800 cs8 clocal

trap cleanup SIGINT

cleanup() {
    KEEP_LOOPING=false
    if [[ "${pid}" != "" ]]; then
        echo "Killing process #${FILE_NO}, ${pid}..."
        kill -15 ${pid}
    fi
}

export KEEP_LOOPING=true
export FILE_NO=1
while [[ "${KEEP_LOOPING}" == "true" ]]; do
    echo "Processing iteration #${FILE_NO}"
    NB=1
    ./serial.log.sh > LOG_${FILE_NO}.log 2>&1 &
    pid=$!
    sleep 30
    echo "Killing process #${FILE_NO}, ${pid}..."
    kill -15 ${pid}
    #
    FILE_NO=$(expr ${FILE_NO} + 1)
done
# Tcho !
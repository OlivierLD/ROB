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
        kill -9 ${pid}
    fi
}

export KEEP_LOOPING=true
export FILE_NO=1
while [[ "${KEEP_LOOPING}" == "true" ]]; do
    echo "Processing iteration #${FILE_NO}"
    # cat /dev/ttyACM0 | grep -e "RMC" > LOG_${FILE_NO}.log 2>&1 &
    ./serial.dump.sh > LOG_${FILE_NO}.log 2>&1 &
    pid=$!
    echo "Process #${pid} on its way..."
    sleep 30
    echo "Killing process #${FILE_NO}, ${pid}..."
    kill -9 ${pid}
    #
    # See log file...
    echo "LOG_${FILE_NO}.log contains $(cat LOG_${FILE_NO}.log | wc -l) lines..."
    #
    FILE_NO=$(expr ${FILE_NO} + 1)
done
# Tcho !
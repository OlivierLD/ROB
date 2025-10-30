#!/bin/bash
#
# An example...
#
export KEEP_LOOPING=true
export FILE_NO=1
while [[ "${KEEP_LOOPING}" == "true" ]]; do
    echo "Processing iteration #${FILE_NO}"

    # cat /dev/ttyACM0 >> LOG_${FILE_NO}.log &
    cat logging.properties >> LOG_${FILE_NO}.log &

    pid=$!
    sleep 5
    echo "Killing process #${FILE_NO}, ${pid}..."
    kill -15 ${pid}

    FILE_NO=$((FILE_NO + 1))
done

# Tcho !
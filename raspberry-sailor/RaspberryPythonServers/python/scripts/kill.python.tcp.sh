#!/bin/bash
ps -ef | grep python | grep TCP_ | awk '{ print $2 }' > server.id.txt
#
for pid in `cat server.id.txt`; do
  echo Killing process ${pid}
  sudo kill -15 ${pid}
done
rm server.id.txt

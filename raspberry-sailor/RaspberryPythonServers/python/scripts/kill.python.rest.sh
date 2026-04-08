#!/bin/bash
ps -ef | grep python | grep REST | awk '{ print $2 }' > server.id.txt
#
for pid in `cat server.id.txt`; do
  echo Killing process ${pid}
  # sudo kill -15 ${pid}
  sudo kill -9 ${pid}
  # sudo kill -2 ${pid}
  # sudo kill -SIGINT ${pid}
done
rm server.id.txt

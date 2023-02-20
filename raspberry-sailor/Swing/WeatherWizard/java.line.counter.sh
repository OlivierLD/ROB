#!/bin/bash
# Line counter...
#
cd $(dirname $0)
echo -e "Counting, please wait..."
JAVA_LINES=$((find . -name '*.java' -print0 | xargs -0 cat) |  wc -l)
JAVA_FILES=$(find . -name '*.java' |  wc -l)
#
echo -e "------------------------"
echo -e "${JAVA_FILES} java files, $(printf "%'.0f" ${JAVA_LINES}) lines of code"
echo -e "------------------------"


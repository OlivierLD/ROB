#!/bin/bash
# Line counter...
#
cd $(dirname $0)
echo -e "Counting, please wait..."
JAVA_LINES=$((find . -name '*.java' -print0 | xargs -0 cat) |  wc -l)
SCALA_LINES=$((find . -name '*.scala' -print0 | xargs -0 cat) |  wc -l)
PYTHON_LINES=$((find . -name '*.py' -print0 | xargs -0 cat) |  wc -l)
ES6_LINES=$((find . -name '*.js' -print0 | xargs -0 cat) |  wc -l)
MD_LINES=$((find . -name '*.md' -print0 | xargs -0 cat) |  wc -l)
CSS_LINES=$((find . -name '*.css' -print0 | xargs -0 cat) |  wc -l)
HTML_LINES=$((find . -name '*.html' -print0 | xargs -0 cat) |  wc -l)
SH_LINES=$((find . -name '*.sh' -print0 | xargs -0 cat) |  wc -l)
JSON_LINES=$((find . -name '*.json' -print0 | xargs -0 cat) |  wc -l)
IPYNB_LINES=$((find . -name '*.ipynb' -print0 | xargs -0 cat) |  wc -l)
SQL_LINES=$((find . -name '*.sql' -print0 | xargs -0 cat) |  wc -l)
XML_LINES=$((find . -name '*.xml' -print0 | xargs -0 cat) |  wc -l)
#
# This formatting might not work on Mac...
#
echo -e "$(printf "%'.0f" ${JAVA_LINES}) lines of Java code"
echo -e "$(printf "%'.0f" ${SCALA_LINES}) lines of Scala code"
echo -e "$(printf "%'.0f" ${PYTHON_LINES}) lines of Python code"
echo -e "$(printf "%'.0f" ${ES6_LINES}) lines of JavaScript code"
echo -e "$(printf "%'.0f" ${JSON_LINES}) lines of JSON"
echo -e "$(printf "%'.0f" ${HTML_LINES}) lines of HTML code"
echo -e "$(printf "%'.0f" ${CSS_LINES}) lines of CSS code"
echo -e "$(printf "%'.0f" ${MD_LINES}) lines of MD doc"
echo -e "$(printf "%'.0f" ${SH_LINES}) lines of script"
echo -e "$(printf "%'.0f" ${IPYNB_LINES}) lines of notebooks"
echo -e "$(printf "%'.0f" ${SQL_LINES}) lines of SQL code"
echo -e "$(printf "%'.0f" ${XML_LINES}) lines of XML"
echo -e " "
echo -e "...and counting!"
echo -e "------------------------"
TOTAL=$(expr ${JAVA_LINES} + ${SCALA_LINES} + ${PYTHON_LINES} + ${ES6_LINES} + ${JSON_LINES} + ${HTML_LINES} + ${CSS_LINES} + ${MD_LINES} + ${IPYNB_LINES} + ${SH_LINES} + ${SQL_LINES} + ${XML_LINES})
# printf "%'.3f\n" ${TOTAL}
echo -e "This is a total of $(printf "%'.0f" ${TOTAL}) lines..."
echo -e "------------------------"


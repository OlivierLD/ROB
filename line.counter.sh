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

echo -e "${JAVA_LINES} lines of Java code"
echo -e "${SCALA_LINES} lines of Scala code"
echo -e "${PYTHON_LINES} lines of Python code"
echo -e "${ES6_LINES} lines of JavaScript code"
echo -e "${JSON_LINES} lines of JSON"
echo -e "${HTML_LINES} lines of HTML code"
echo -e "${CSS_LINES} lines of CSS code"
echo -e "${MD_LINES} lines of MD doc"
echo -e "${SH_LINES} lines of script"
echo -e "${IPYNB_LINES} lines of notebooks"
echo -e "${SQL_LINES} lines of SQL code"
echo -e " "
echo -e "...and counting!"
echo -e "------------------------"
echo -e "This is a total of $(expr ${JAVA_LINES} + ${SCALA_LINES} + ${PYTHON_LINES} + ${ES6_LINES} + ${JSON_LINES} + ${HTML_LINES} + ${CSS_LINES} + ${MD_LINES} + ${IPYNB_LINES} + ${SH_LINES} + ${SQL_LINES}) lines..."
echo -e "------------------------"


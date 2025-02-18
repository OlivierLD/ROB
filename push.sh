#!/bin/bash
#
# Use this script to push the generated artifacts to the local maven repo.
# Run it from the "repository" branch of the repo.
#
CURRENT_BRANCH=$(git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/ \1/')
CURRENT_BRANCH=$(echo ${CURRENT_BRANCH})   # Trim
if [[ "${CURRENT_BRANCH}" != "repository" ]]; then
  echo -e "---------------------------------------------------------------------"
  echo -e "The current branch is [${CURRENT_BRANCH}], it should be [repository]."
  echo -e "Exiting"
  echo -e "---------------------------------------------------------------------"
  exit 1
fi
#
GROUP=raspberry.on.board
ARTIFACT=http-tiny-server
VERSION=1.0
#
echo -en "Enter the GROUP (default is ${GROUP}) > "
read a
if [[ "${a}" != "" ]]; then
  GROUP=${a}
fi
echo -e "Group is ${GROUP}"
#
REPO_ROOT=${HOME}/.m2/repository/$(echo ${GROUP} | tr "." "/")
#
echo -e "Available artifacts:"
ls -lish ${REPO_ROOT} | awk '{ print $11 " (size:" $7 ", created " $8 " " $9 " " $10 ")" }' > artifacts.txt
NL=0
while read -r line; do
  if [[ ${NL} -gt 0 ]]; then
    echo -e "${NL} - ${line}"
  fi
  NL=$(expr ${NL} + 1)
done < artifacts.txt
#
echo -en "Enter the ARTIFACT line number (default is ${ARTIFACT}) > "
read LINE_NO
if [[ "${LINE_NO}" != "" ]]; then
  ARTIFACT=
  NL=0
  while read -r line; do
    if [[ "${NL}" == "${LINE_NO}" ]]; then
      ARTIFACT=$(echo ${line} | awk '{ print $1 }')
      echo -e "Selected artifact ${ARTIFACT}"
      break
    fi
    NL=$(expr ${NL} + 1)
  done < artifacts.txt
fi
rm artifacts.txt 2>/dev/null
#
if [[ "${ARTIFACT}" == "" ]]; then
  echo -e "Try again, with a valid line number..."
  echo "Canceled."
  exit 0
else
  echo -e "Artifact will be ${ARTIFACT}, line ${LINE_NO}"
fi
#
echo -e "Available versions:"
for VERS in $(find ${REPO_ROOT}/${ARTIFACT}/* -type d -maxdepth 1 -exec basename {} \;); do
  echo -e "- ${VERS}"
done
echo -en "Enter the VERSION (default is ${VERSION}) > "
read a
if [[ "${a}" != "" ]]; then
  VERSION=${a}
fi
#
JAR_FILE_NAME=${REPO_ROOT}/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar
DATE_MODIFIED=$(ls -lisah ${JAR_FILE_NAME} | awk '{ print $8 " " $9 " " $10 }')
echo -e "${ARTIFACT} version ${VERSION}, was modified ${DATE_MODIFIED}"
echo -en "Do we proceed? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo "Canceled."
  exit 0
fi

COMMAND="mvn install:install-file \
-DgroupId=${GROUP} \
-DartifactId=${ARTIFACT} \
-Dversion=${VERSION} \
-Dfile=${REPO_ROOT}/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar \
-Dpackaging=jar \
-DgeneratePom=true \
-DlocalRepositoryPath=. \
-DcreateChecksum=true"
#
echo -e "Command is:"
echo -e "${COMMAND}"
echo -en "Still OK? Do we proceed? > "
read REPLY
if [[ ! ${REPLY} =~ ^(yes|y|Y)$ ]]; then
  echo "Canceled."
  exit 0
else
  ${COMMAND}
  echo -e "Done"
  git status
  echo -e "+---------------------------------------------------------------+"
  echo -e "| Do not forget to commit and push the changes in this branch!! |"
  echo -e "| Do a 'git commit -am \"...\"',                                  |"
  echo -e "| and a 'git push origin repository'                            |"
  echo -e "+---------------------------------------------------------------+"
  # git status
fi
#

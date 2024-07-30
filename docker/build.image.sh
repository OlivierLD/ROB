#!/bin/bash
#
# Interactive script
# to create a Docker image
# NavServer demo
#
DOCKER_FILE=navserver.Dockerfile
IMAGE_NAME=mux-sample-vnc
# RUN_CMD="docker run -d --name mux-desktop ${IMAGE_NAME}:latest"
RUN_CMD="docker run -it --name mux-desktop --rm -p 5901:5901 -p 9999:9999 -e USER=root [--user oliv] ${IMAGE_NAME}:latest /bin/bash"
# RUN_CMD="docker run -d ${IMAGE_NAME}:latest"
#
MESSAGE="---------------------------------------------------\n"
MESSAGE="${MESSAGE}Log in using: ${RUN_CMD}\n"
MESSAGE="${MESSAGE}    or using: docker exec -it mux-desktop /bin/bash\n"
MESSAGE="${MESSAGE}- you can now run ./demoLauncher.sh to start the server you want.\n"
MESSAGE="${MESSAGE}- then run 'vncserver :1 -geometry 1280x800 (or 1440x900, 1680x1050, etc) -depth 24'\n"
MESSAGE="${MESSAGE}- then use a vncviewer on localhost:1, password is 'mate'\n"
# MESSAGE="${MESSAGE}- then 'node server.js' or 'npm start', and reach http://localhost:9999/web/index.html ...\n"
MESSAGE="${MESSAGE} \n"
MESSAGE="${MESSAGE}- Or docker run --detach --name mux-sample --rm -p 5901:5901 -p 9999:9999 ${IMAGE_NAME}:latest \n"
MESSAGE="${MESSAGE}- and reach http://localhost:9999/web/index.html ...\n"
MESSAGE="${MESSAGE}---------------------------------------------------\n"
#
if [[ "${DOCKER_FILE}" != "" ]]; then
  #
  # Proxies, if needed
  # export HTTP_PROXY=http://www-proxy.us.oracle.com:80
  # export HTTPS_PROXY=http://www-proxy.us.oracle.com:80
  #
  EXTRA=""
  if [[ "${EXTRA_PRM}" != "" ]]; then
    EXTRA="with ${EXTRA_PRM}"
  fi
  echo -e "---------------------------------------------------"
  echo -e "Generating ${IMAGE_NAME} from ${DOCKER_FILE} ${EXTRA}"
  echo -e "---------------------------------------------------"
  # Possibly use --quiet, --no-cache
  NO_CACHE=
  echo -en "Use the docker cache [y]|n ? > "
  read REPLY
  if [[ ${REPLY} =~ ^(no|n|N)$ ]]; then
    NO_CACHE="--no-cache"
  fi
  docker build ${NO_CACHE} -f ${DOCKER_FILE} -t ${IMAGE_NAME} ${EXTRA_PRM} .
  #
  # Now run
  echo -e "To create a container, run ${RUN_CMD} ..."
  echo -en "Do you want to run it y|n ? > "
  read REPLY
  if [[ ${REPLY} =~ ^(yes|y|Y)$ ]]; then
#    CONTAINER_ID=`$RUN_CMD`
    CONTAINER_ID=$(${RUN_CMD})
    echo -e "Running container ID ${CONTAINER_ID}"
    echo -e "To log in to the running container: docker run -it --rm -e USER=root mux-sample-vnc:latest /bin/bash "
  fi
fi
printf "%b" "${MESSAGE}"
# Prompt for export
if [[ "${DOCKER_FILE}" != "" ]] && [[ "${CONTAINER_ID}" != "" ]]; then
  echo -en "== Do you want to export this container ${CONTAINER_ID} ? [n]|y > "
  read choice
  # choice=N
  if [[ "${choice}" == "Y" ]]  || [[ "${choice}" == "y" ]]; then
    echo -e "\nLast generated one is ${IMAGE_NAME}:latest, its ID is ${CONTAINER_ID}"
    echo -en "== Please enter the name of the tar file to generate (like export.tar) > "
    read fName
    echo -en "Will export container ${CONTAINER_ID} into ${fName} - Is that correct ? [n]|y > "
    read choice
    if [[ "${choice}" == "Y" ]]  || [[ "${choice}" == "y" ]]; then
      docker export --output ${fName} ${CONTAINER_ID}
    fi
  fi
  echo -e "\nYou can export a running container any time by running 'docker export --output export.tar [Container ID]'"
  echo -e "Docker commands are documented at https://docs.docker.com/engine/reference/commandline/docker/"
fi

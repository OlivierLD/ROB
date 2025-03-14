ARG http_proxy=""
ARG https_proxy=""
ARG no_proxy=""
#
#ARG http_proxy="http://www-proxy.us.oracle.com:80"
#ARG https_proxy="http://www-proxy.us.oracle.com:80"
#ARG no_proxy=""
#
# The :buster is optional. But bullseye may have some security issues (when downloading the image)
# FROM debian:buster
FROM debian:latest
#
# To run on a laptop - not necessarily on an RPi (hence the default-jdk below)
# Demos the NavServer (Tide, Almanac, Weather faxes, etc)
# Clones the repo and recompiles everything.
# proxy settings are passed as ARGs
#
LABEL maintainer="Olivier Le Diouris <olivier@lediouris.net>"
#
# Uncomment if running behind a firewall (also set the proxies at the Docker level to the values below)
ENV http_proxy ${http_proxy}
ENV https_proxy ${https_proxy}
# ENV ftp_proxy $http_proxy
ENV no_proxy ${no_proxy}
#

RUN echo "alias ll='ls -lisa'" >> $HOME/.bashrc
RUN echo "alias lll='ls -lisah'" >> $HOME/.bashrc

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  DEBIAN_FRONTEND=noninteractive && \
  apt-get install --fix-missing -y curl wget git build-essential default-jdk sysvbanner mate-desktop-environment-core tightvncserver vim net-tools && \
  rm -rf /var/lib/apt/lists/*

# RUN apt-get install net-tools -y

RUN mkdir ~/.vnc

RUN echo "mate" | vncpasswd -f >> ~/.vnc/passwd
RUN chmod 600 ~/.vnc/passwd

# RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
RUN apt-get install -y nodejs

RUN apt-get install -y chromium
RUN apt-get install -y firefox-esr
RUN apt-get install -y inkscape

RUN apt-get install -y libgtk2.0-dev

EXPOSE 5901

RUN useradd -d /home/oliv -s /bin/bash -g root -G sudo -p oliv oliv
# Command above seems NOT to encrypt the password... Run 'passwd oliv' (as root) to do it.
# passwd oliv

RUN echo "banner Nav Server" >> $HOME/.bashrc
RUN echo "git --version" >> $HOME/.bashrc
RUN echo "echo -n 'node:' && node -v" >> $HOME/.bashrc
RUN echo "echo -n 'npm:' && npm -v" >> $HOME/.bashrc
RUN echo "java -version" >> $HOME/.bashrc
#
RUN echo "echo -e \"Architecture: $(getconf LONG_BIT) bits\"" >> $HOME/.bashrc
RUN echo "lsb_release -a" >> $HOME/.bashrc
RUN echo "echo -e \"Type 'lscpu' for more\"" >> $HOME/.bashrc
RUN echo "echo -e \"To start VNCServer: type 'vncserver :1 -geometry 1680x1050 -depth 24' \"" >> $HOME/.bashrc
#
RUN mkdir /workdir
WORKDIR /workdir
RUN git clone https://github.com/OlivierLD/ROB.git
WORKDIR /workdir/ROB
# RUN git submodule update --init
RUN ./gradlew tasks
# RUN ./gradlew tasks -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
WORKDIR /workdir/ROB/raspberry-sailor/MUX-implementations/RESTNavServer
RUN ../../../gradlew shadowJar
#
# Build the Swing stuff, usable from the Mate Desktop
WORKDIR /workdir/ROB/raspberry-sailor/Swing/WeatherWizard
RUN ../../../gradlew shadowJar
# Will be ready to use with ./run-ww.sh
# RUN ../gradlew shadowJar -Dhttp.proxyHost=www-proxy.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80
WORKDIR /workdir/ROB/raspberry-sailor/MUX-implementations/RESTNavServer/launchers

#ENV http_proxy ""
#ENV https_proxy ""
#ENV no_proxy ""

EXPOSE 9999
# This is delivering the functionality. Change it at will.
# CMD ["./demoLauncher.sh", "--option:1", "--nohup:N"]

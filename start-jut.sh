#!/bin/bash

error="$(tput setaf 1)$(tput bold)"
warn="$(tput setaf 3)$(tput bold)"
ok="$(tput setaf 2)$(tput bold)"

norm="$(tput setaf 0)$(tput sgr0)"

# shellcheck disable=SC2164
JENKINS_HOME=$(cd ./docker/volumes/jenkins-home; pwd)
export JENKINS_HOME

export PLUGINS_DIR=$JENKINS_HOME/plugins
if [ ! -d $PLUGINS_DIR ]; then
    echo "${error}No Jenkins plugins found in ${PLUGINS_DIR}${norm}"
    exit 1;
fi

echo "${ok}Running Jenkins with plugins in ${PLUGINS_DIR}"

# shellcheck disable=SC2164
JENKINS_WAR="$(cd ./acceptance-test-harness/; pwd)/jenkins.war"
export JENKINS_WAR
if [ ! -f $JENKINS_WAR ]; then
    echo "${warn}Jenkins war does not exist at ${JENKINS_WAR}"
    echo "${norm}Downloading latest LTS..."
    curl -L "http://mirrors.jenkins.io/war-stable/latest/jenkins.war" > $JENKINS_WAR
fi

echo "${ok}Using Jenkins under test ${JENKINS_WAR}${norm}"

export WORKSPACE=/tmp

cd acceptance-test-harness || exit 1;

./jut-server.sh -n 1

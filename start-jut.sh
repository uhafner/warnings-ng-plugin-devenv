#!/bin/bash

export JENKINS_HOME=$(cd ./jenkins-home; pwd)
export JENKINS_WAR="$(cd ./jenkins-war/; pwd)/jenkins.war"

export WORKSPACE=/tmp
export PLUGINS_DIR=$JENKINS_HOME/plugins

echo JENKINS_HOME=$JENKINS_HOME
echo JENKINS_WAR=$JENKINS_WAR
echo PLUGINS_DIR=$PLUGINS_DIR

cd acceptance-test-harness
./jut-server.sh -n 1

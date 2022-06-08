#!/bin/bash

set -e

JENKINS_HOME=docker/volumes/jenkins-home

echo "Removing plugins in $JENKINS_HOME"

rm -rfv $JENKINS_HOME/plugins/analysis-model-api*
rm -rfv $JENKINS_HOME/plugins/code-coverage-api*
rm -rfv $JENKINS_HOME/plugins/forensics-api*
rm -rfv $JENKINS_HOME/plugins/git-forensics*
rm -rfv $JENKINS_HOME/plugins/warnings-ng*

cp -fv analysis-model-api-plugin/target/*hpi $JENKINS_HOME/plugins/analysis-model-api.jpi
cp -fv code-coverage-api-plugin/plugin/target/*hpi $JENKINS_HOME/plugins/code-coverage-api.jpi
cp -fv forensics-api-plugin/target/*hpi $JENKINS_HOME/plugins/forensics-api.jpi
cp -fv git-forensics-plugin/plugin/target/*hpi $JENKINS_HOME/plugins/git-forensics.jpi
cp -fv warnings-ng-plugin/plugin/target/*hpi $JENKINS_HOME/plugins/warnings-ng.jpi

CURRENT_UID="$(id -u):$(id -g)"
export CURRENT_UID
IS_RUNNING=$(docker-compose ps -q jenkins-controller)
if [[ "$IS_RUNNING" != "" ]]; then
    docker-compose restart
    echo "Restarting Jenkins (docker compose with user ID ${CURRENT_UID}) ..."
fi

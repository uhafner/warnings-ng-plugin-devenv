#!/bin/bash

set -e

JENKINS_HOME=docker/volumes/jenkins-home

echo "Removing plugins in $JENKINS_HOME"

rm -rfv $JENKINS_HOME/plugins/analysis-model-api*
rm -rfv $JENKINS_HOME/plugins/coverage-*
rm -rfv $JENKINS_HOME/plugins/warnings-ng*

cp -fv analysis-model-api-plugin/target/*hpi $JENKINS_HOME/plugins/analysis-model-api.jpi
cp -fv coverage-plugin/plugin/target/*hpi $JENKINS_HOME/plugins/coverage.jpi
cp -fv warnings-ng-plugin/plugin/target/*hpi $JENKINS_HOME/plugins/warnings-ng.jpi

CURRENT_UID="$(id -u):$(id -g)"
export CURRENT_UID
IS_RUNNING=$(docker compose ps -q jenkins)
if [[ "$IS_RUNNING" != "" ]]; then
    docker compose restart
    echo "Restarting Jenkins (docker compose with user ID ${CURRENT_UID}) ..."
fi

#!/bin/bash

JENKINS_HOME=docker/volumes/jenkins-home

mvn clean install -Pskip || { echo "Build failed"; exit 1; }

echo "Removing plugins in $JENKINS_HOME"

rm -rfv $JENKINS_HOME/plugins/{plugin-util,bootstrap4,data-tables,echarts,font-awesome,jquery3,popper,analysis-model,forensics}-api*
rm -rfv $JENKINS_HOME/plugins/{warnings-ng,git-forensics}*

cp -fv plugin-util-api-plugin/target/*hpi $JENKINS_HOME/plugins/plugin-util-api.jpi
cp -fv bootstrap4-api-plugin/target/*hpi $JENKINS_HOME/plugins/bootstrap4-api.jpi
cp -fv data-tables-api-plugin/target/*hpi $JENKINS_HOME/plugins/data-tables-api.jpi
cp -fv echarts-api-plugin/target/*hpi $JENKINS_HOME/plugins/echarts-api.jpi
cp -fv font-awesome-api-plugin/target/*hpi $JENKINS_HOME/plugins/font-awesome-api.jpi
cp -fv jquery3-api-plugin/target/*hpi $JENKINS_HOME/plugins/jquery3-api.jpi
cp -fv popper-api-plugin/target/*hpi $JENKINS_HOME/plugins/popper-api.jpi
cp -fv analysis-model-api-plugin/target/*hpi $JENKINS_HOME/plugins/analysis-model-api.jpi
cp -fv forensics-api-plugin/target/*hpi $JENKINS_HOME/plugins/forensics-api.jpi
cp -fv warnings-ng-plugin/target/*hpi $JENKINS_HOME/plugins/warnings-ng.jpi
cp -fv git-forensics-plugin/target/*hpi $JENKINS_HOME/plugins/git-forensics.jpi

CURRENT_UID="$(id -u):$(id -g)"
export CURRENT_UID
IS_RUNNING=$(docker-compose ps -q jenkins-master)
if [[ "$IS_RUNNING" != "" ]]; then
    docker-compose restart
    echo "Restarting Jenkins (docker compose with user ID ${CURRENT_UID}) ..."
fi

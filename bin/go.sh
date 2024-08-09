#!/bin/bash

JENKINS_HOME=docker/volumes/jenkins-home

mvn clean install -Pskip || { echo "Build failed"; exit 1; }

exec ./deploy.sh

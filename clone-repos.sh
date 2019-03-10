#!/bin/bash

echo Clone of Jenkins plugins using SSH and your GitHub key

git clone git@github.com:jenkinsci/analysis-model.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/analysis-model-api-plugin.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/warnings-ng-plugin.git || { echo "Clone failed"; exit 1; }

git clone git@github.com:jenkinsci/acceptance-test-harness.git || { echo "Clone failed"; exit 1; }

echo Done cloning. Note that you need to change the remotes for each
echo of the repositories you want to contribute to.

read -n 1 -s -r -p "Press any key to continue and compile the projects"

mvn -V -U -e install -DskipTests

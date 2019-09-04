#!/bin/bash

mark="$(tput setaf 2)$(tput bold)"
bold="$(tput bold)"
norm="$(tput setaf 0)$(tput sgr0)"

echo ${mark}${bold}Clone of Jenkins plugins using SSH and your GitHub key
read -n 1 -s -r -p "${norm}Press any key to continue..."
echo

git clone git@github.com:jenkinsci/analysis-model.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/analysis-model-api-plugin.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/forensics-api-plugin.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/git-forensics-plugin.git || { echo "Clone failed"; exit 1; }
git clone git@github.com:jenkinsci/warnings-ng-plugin.git || { echo "Clone failed"; exit 1; }

git clone git@github.com:jenkinsci/acceptance-test-harness.git || { echo "Clone failed"; exit 1; }

echo ${mark}${bold}Done cloning. Note that you need to change the remotes for each
echo of the repositories you want to contribute to.

read -n 1 -s -r -p "${norm}Press any key to compile the projects..."
echo

mvn -V -U -e install -DskipTests

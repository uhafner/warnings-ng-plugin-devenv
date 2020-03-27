#!/bin/bash

protocol="$1"
message="$2"

if [ -z "$1" ] || [ -z "$2" ]; then
        echo "Usage: _clone.sh protocol"
        exit 0
fi

protocol="${1}"

mark="$(tput setaf 2)$(tput bold)"
bold="$(tput bold)"
norm="$(tput setaf 0)$(tput sgr0)"

echo "${mark}${bold}Clone of Jenkins plugins using ${message}"
read -n 1 -s -r -p "${norm}Press any key to continue..."
echo

git clone ${protocol}uhafner/codingstyle.git || { echo "Clone failed"; exit 1; }

git clone ${protocol}jenkinsci/analysis-model.git || { echo "Clone failed"; exit 1; }
git clone ${protocol}jenkinsci/analysis-model-api-plugin.git || { echo "Clone failed"; exit 1; }
git clone ${protocol}jenkinsci/autograding-plugin.git || { echo "Clone failed"; exit 1; }
git clone ${protocol}jenkinsci/forensics-api-plugin.git || { echo "Clone failed"; exit 1; }
git clone ${protocol}jenkinsci/git-forensics-plugin.git || { echo "Clone failed"; exit 1; }
git clone ${protocol}jenkinsci/warnings-ng-plugin.git || { echo "Clone failed"; exit 1; }

echo ${mark}${bold}Done cloning. Note that you need to change the remotes for each
echo of the repositories you want to contribute to.

read -n 1 -s -r -p "${norm}Press any key to compile the projects..."
echo

mvn -V -U -e install -DskipTests

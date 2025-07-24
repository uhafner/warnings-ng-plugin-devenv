#!/usr/bin/env bash

mark="$(tput setaf 2)$(tput bold)"
warn="$(tput setaf 3)$(tput bold)"
err="$(tput setaf 1)$(tput bold)"
bold="$(tput bold)"
norm="$(tput setaf 0)$(tput sgr0)"

echo "${mark}${bold}Fork and clone of Jenkins plugins using Github CLI${norm}"
read -n 1 -s -r -p "Press any key to continue..."
echo

if ! gh auth status &>/dev/null; then
  echo "${err}${bold}!${norm} GitHub CLI not authenticated. Run: gh auth login"
  exit 1
fi


me=$(gh api user -q ".login")

clone() {
  owner="${1}"
  repo="${2}"

  if [ -d "$repo" ]; then
    echo "${warn}${bold}!${norm} Directory ${bold}${repo}${norm} already exists. Skipping cloning this repo."
  else
    echo "Cloning ${bold}${repo}${norm}"
    gh repo clone "${owner}/${repo}" || { echo "Clone failed"; exit 1; }
  fi
}

fork() {
  original="${1}"
  repo="${2}"
  if [ -d "$repo" ]; then
    echo "${warn}${bold}!${norm} Directory ${bold}${repo}${norm} already exists. Skipping forking and cloning this repo."
  else
    echo "Forking and cloning ${bold}${repo}${norm}"
    gh repo fork "${original}/${repo}" --clone --remote || { echo "Fork failed"; exit 1; }
  fi
}

clone "uhafner" "codingstyle" || { exit 1; }
clone "jenkinsci" "acceptance-test-harness" || { exit 1; }
fork "jenkinsci" "analysis-model" || { exit 1; }
fork "jenkinsci" "analysis-model-api-plugin" || { exit 1; }
fork "jenkinsci" "coverage-model" || { exit 1; }
fork "jenkinsci" "coverage-plugin" || { exit 1; }
fork "jenkinsci" "warnings-ng-plugin" || { exit 1; }

echo "${mark}${bold}Done cloning.${norm}"
echo "${bold}Note that you need to change the remotes for each of the repositories you want to contribute to.${norm}"

read -n 1 -s -r -p "Press any key to compile the projects..."
echo

mvn -V -U -e verify -Pskip

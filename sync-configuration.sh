#!/bin/bash

#find . -name ci.yml -exec cp -vR etc/github/workflows/ci.yml {} \;

#find . -name assertj-templates -exec cp -v etc/assertj-templates/* {} \;
#find . -name .github -exec cp -vR etc/github/* {} \;
#find . -name .github -exec cp -vR etc/github/labels.yml {} \;
#find . -name .github -exec cp -vR etc/github/labels.yml {} \;
#find . -name .github -exec cp -vR etc/github/release-drafter.yml {} \;
find . -name .github -exec cp -vR etc/github/dependabot.yml {} \;
#find . -name assign-pr.yml -exec cp -vR etc/github/workflows/assign-pr.yml {} \;

#!/bin/bash

#find . -name workflows -exec cp -vR etc/github/workflows/* {} \;

#find .  -maxdepth 4 -name autograding.yml -exec rm -i {} \;
#find . -name .github -exec cp -vR etc/github/check-md-links.json {} \;
#find . -name ci.yml -exec cp -vR etc/github/workflows/ci.yml {} \;

#find . -name assertj-templates -exec cp -v etc/assertj-templates/* {} \;
#find . -name .github -exec cp -vR etc/github/labels.yml {} \;
#find . -name workflows -exec cp -vR etc/github/workflows/enforce-labels.yml {} \;
find . -name workflows -exec cp -vR etc/github/workflows/codeql.yml {} \;
#find . -name .github -exec cp -vR etc/github/release-drafter.yml {} \;
#find . -name .github -exec cp -vR etc/github/dependabot.yml {} \;
#find . -name assign-pr.yml -exec cp -vR etc/github/workflows/assign-pr.yml {} \;

#find . -maxdepth 4 -path '*plugin*autograding.yml' -exec rm -f {} \;

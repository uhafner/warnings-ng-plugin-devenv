#!/bin/bash

# find . -name assertj-templates -exec cp -v etc/assertj-templates/* {} \;
#find . -name .github -exec cp -vR etc/github/* {} \;
#find . -name .github -exec cp -vR etc/github/labels.yml {} \;
find . -name .github -exec cp -vR etc/github/dependabot.yml {} \;

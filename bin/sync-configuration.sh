#!/bin/bash

#find . -name workflows -exec cp -vR etc/github/workflows/* {} \;

#find .  -maxdepth 4 -name autograding.yml -exec rm -i {} \;
#find . -name .github -exec cp -vR etc/github/check-md-links.json {} \;

#find . -name assertj-templates -exec cp -v etc/assertj-templates/* {} \;
#find . -name .github -exec cp -vR etc/github/labels.yml {} \;
#find . -name workflows -exec cp -vR etc/github/workflows/enforce-labels.yml {} \;
#find . -name workflows -exec cp -vR etc/github/workflows/codeql.yml {} \;
#find . -name .github -exec cp -vR etc/github/release-drafter.yml {} \;
#find . -name .github -exec cp -vR etc/github/dependabot.yml {} \;
#find . -name assign-pr.yml -exec cp -vR etc/github/workflows/assign-pr.yml {} \;
find . -name auto_assign.yml -exec cp -vR etc/github/auto_assign.yml {} \;
#find . -name check-md-links.yml -exec cp -vR etc/github/workflows/check-md-links.yml {} \;
#find . -name linkspector.yml -exec cp -vR etc/github/linkspector.yml {} \;
#find . -name enforce-labels.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/enforce-labels.yml {} \;
#find . -name run-release-drafter.yml -exec cp -vR etc/github/workflows/run-release-drafter.yml {} \;
#find . -name sync-labels.yml -exec cp -vR etc/github/workflows/sync-labels.yml {} \;

#find . -maxdepth 4 -path '*plugin*autograding.yml' -exec rm -f {} \;

#find . -name ci.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/ci.yml {} \;
#find . -name codeql.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/codeql.yml {} \;
#find . -name coverage.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/coverage.yml {} \;
#find . -name quality-monitor-build.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/quality-monitor-build.yml {} \;
#find . -name update-badges.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/update-badges.yml {} \;
# find . -name quality-monitor-pit.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/quality-monitor-pit.yml {} \;
# find . -name quality-monitor.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/quality-monitor.yml {} \;

#find . -name dependabot.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/dependabot.yml {} \;
#find . -name jenkins-security-scan.yml -prune -not -regex '.*target.*' -exec cp -vR etc/github/workflows/jenkins-security-scan.yml {} \;

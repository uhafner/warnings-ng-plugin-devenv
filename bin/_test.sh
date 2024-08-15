#!/bin/bash

GECKO_DRIVER_PATH=/opt/bin/geckodriver
CHROME_DRIVER_PATH=/opt/bin/chromedriver

BROWSER=$1 # first commandline parameter
RETRY=2 # number of retries if a test fails due to timeout errors, etc.
TEST_CASE=$2 # second commandline parameter (leave empty for warnings plugin smoke tests)
ELASTIC=2 # increase if your machine is slow

error="$(tput setaf 1)$(tput bold)"
warn="$(tput setaf 3)$(tput bold)"
ok="$(tput setaf 2)$(tput bold)"

bold="$(tput bold)"
norm="$(tput setaf 0)$(tput sgr0)"

if [ ! -x ${GECKO_DRIVER_PATH} ]; then
    GECKO_DRIVER_PATH=$(which geckodriver)
fi

if [ ! -x ${GECKO_DRIVER_PATH} ]; then
    echo "${error}Did not find Selenium driver for Firefox ${GECKO_DRIVER_PATH}${norm}"
    exit 1;
fi

if [ ! -x ${CHROME_DRIVER_PATH} ]; then
    CHROME_DRIVER_PATH=$(which chromedriver)
fi

if [ ! -x ${CHROME_DRIVER_PATH} ]; then
    echo "${error}Did not find Selenium driver for Chrome ${CHROME_DRIVER_PATH}${norm}"
    exit 1;
fi

if [ -z "$TEST_CASE" ];
then
    TEST_CASE=SmokeTests
fi

mvnOptions="-V test -Dtest=${TEST_CASE} -Dquite -Dsurefire.rerunFailingTestsCount=${RETRY} -Dbrowser=${BROWSER} -DElasticTime.factor=${ELASTIC} -Dwebdriver.gecko.driver=${GECKO_DRIVER_PATH} -Dwebdriver.chrome.driver=${CHROME_DRIVER_PATH} -P-no-ui-tests-on-mac -P-ui-tests-locally"

echo Running: env LC_NUMERIC="en_US.UTF-8" mvn ${mvnOptions}

cd warnings-ng-plugin/ui-tests
env LC_NUMERIC="en_US.UTF-8" mvn ${mvnOptions}

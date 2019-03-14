# Warnings Next Generation Plugin - Development Environment

In order to reduce the initial ramp-up time for new contributors of the Warnings Next Generation Plugin I prepared a 
docker based development environment that is described in this document. It consists of the following parts:

- Scripts to checkout all modules of the warnings plugin from GitHub. 
Depending on the part where you want to contribute you normally work with just one of these modules. 
However, it simplifies the development if all modules are already part of the workspace. Then you
can switch at any time to one of the other modules.
- Docker based Jenkins master and agent that has all required plugins installed to see the Warnings plugin in action. 
This Jenkins instance is already configured properly to build Java modules on an Linux based agent. It also has
some jobs defined, that build some of the modules of the Warnings plugin. These jobs record issues of several static
analysis tools so you immediately get an impression of the functionality of the Warnings plugin.
- IntelliJ project that references the modules of the Warnings plugin. 
This project contains presets of my [coding style](https://github.com/uhafner/codingstyle) and some other helpful 
configurations. Note that IntelliJ (or global IntelliJ preferences) are not part of this project.
- [Acceptance test harness](https://github.com/jenkinsci/acceptance-test-harness) project and some scripts to start
the UI tests of the Warnings plugin. These tests will run by using a pooled Jenkins controller with the preconfigured
set of Jenkins plugins in the docker container. 

## Supported operating systems

The development environment has been tested on macOS Mojave and Ubuntu Linux 18.04 (in a virtual machine running on
macOS). It still needs to be verified, which of these steps work on Windows. Pull requests are always welcome.   

## TLDR

### Installation Requirements

#### Main Development

The following tools are required (latest version):
- Docker
- Docker Compose
- IntelliJ Ultimate
- Maven
- JDK 8

#### UI Testing

Additionally, the following tools (latest version) are required:

- firefox
- gecko-driver
- chrome
- chrome-driver

### Installation Steps

1. Clone the warnings plugin modules using the script `clone-repos.sh`.
2. Import the project into Intellij
    1. Start IntelliJ
    2. Select Open...
    3. Select the folder `warnings-ng-plugin-devenv`
    4. When IntelliJ asks : *Maven projects need to be imported* select *Enable Auto-Import*
3. Start Jenkins with `docker-compose up`
4. Login to Jenkins at: http://localhost:8081/ (admin:admin)
5. Start the provided Jenkins jobs that show the analysis results for the modules analysis-model and warnings-ng. 

## Cloning the modules

In order to clone the modules of the Warnings plugin a simple shell script is provided. The script checks out the 
following modules using the git SSH protocol. This requires that you have registered your public key in GitHub.
- [analysis-model](https://github.com/jenkinsci/analysis-model): A library to read static analysis reports into a 
Java object model. This module is not depending on Jenkins.
- [analysis-model-api-plugin](https://github.com/jenkinsci/analysis-model-api-plugin): A simple wrapper for the 
analysis model library. It provides the analysis-model classes as a Jenkins plugin. This overhead is required
to simplify upgrades of the analysis-model module in Jenkins.
- [warnings-ng-plugin](https://github.com/jenkinsci/warnings-ng-plugin): The actual plugin that contains all steps
and UI classes. 
- [acceptance-test-harness](https://github.com/jenkinsci/acceptance-test-harness) : Jenkins acceptance test harness. 
Contains tests for all Jenkins plugins including the Warnings plugin.

## Modifying and debugging code with IntelliJ

IntelliJ (Ultimate) is the main supported development environment for the Warnings plugin. A predefined project is stored 
in the folder `.idea` that references all modules of the Warnings plugin. This project contains presets of my 
[coding style](https://github.com/uhafner/codingstyle) and some other helpful configurations. 

It should be possible to use other IDEs (Eclipse, Netbeans) as well. The analysis-model library has configuration files 
(coding style, analysis configuration) for Eclipse. However, these files are not yet available for the other modules. 

### Running unit and integration tests

In order to run the unit and integrations tests of the modules analysis-model and warnings-ng use the provided 
Run configurations `All in analysis-model`  and `All in warnings-ng`. These configurations are already configured
to record the branch coverage of the corresponding module packages (`Run with Coverage` menu).   

### Debugging the Jenkins instance

The Jenkins master is always started in 'Debug' mode, i.e. it is listening to remote debug requests. 
In order to debug your changes you simply need to attach a remote debugger at `localhost:8000` (mapped to the same
port in the docker container). Use the provided `Jenkins (Remote Debugger)` Debug configuration to connect 
a debugger in IntelliJ.

TODO: Debugging in agent

### Running UI tests

UI tests can be started using the corresponding launchers `UI Tests (Firefox)` or `UI Tests (Chrome)`. 
Note that both launchers require an installation of the corresponding Selenium drivers. If these drivers are not
installed in `/usr/local/bin` on your local machine then you need to adapt the launcher configurations to match
your setup.

All UI tests require to run within a given subject under test (i.e, Jenkins under test, JUT), see section 
[Acceptance Test Harness](#acceptance-test-harness) for more details.

## Starting the Jenkins instance

In order to see changes in the Warnings plugin modules it is required to deploy the plugins to a Jenkins instance that
contain some jobs that use the plugins. If you have no such instance on your machine already configured, start the 
provided Jenkins master in this project (you need to install [docker](https://www.docker.com) and 
[docker-compose](https://docs.docker.com/compose/overview/)). Open a terminal and run `docker-compose up` 
in the top level folder. This command creates the 
docker containers of all application services. This will require some time when called the first time since the docker
images will be composed. After the images have been created the following containers will be started:
- jenkins-master: [Official Jenkins LTS docker image](https://github.com/jenkinsci/docker) (Alpine Linux). The master is
preconfigured using [JCasC](https://github.com/jenkinsci/configuration-as-code-plugin) to build Java applications 
on an agent. The master is not allowed to run jobs.
- java-agent: A minimal Java agent based on the 
[official OpenJDK8 docker image](https://hub.docker.com/_/openjdk?tab=description) (Alpine Linux). Master and slave
are connected using SSH.
- nginx-proxy: A reverse proxy.  

### Volumes

The home directory of the Jenkins master (JENKINS_HOME) is mounted as a 
[docker volume](https://docs.docker.com/storage/volumes/). I.e., it is visible on the host as a normal directory at
`./docker/volumes/jenkins-home`. It will survive sessions and can be changed directly on the host, see 
[official documentation](https://github.com/jenkinsci/docker/blob/master/README.md) for details. 

The agent data directory (Jenkins workspace, tools downloads, shared maven repository etc.) is also mounted 
as a docker volume under `./docker/volumes/jenkins-home`. 

#### macOS notes

Note that volumes under macOS are quite slow. On my MacBook running the provided Jenkins job of the analysis-model in the
docker container is slower than running the same Jenkins job in a docker container that is running in a linux virtual machine
on the same MacBook (sounds kind of absurd).

## Deploying changed plugins to the Jenkins instance 

Once your local development changes are done (i.e., the unit tests are all green) you should test your changes in the
Jenkins instance. This also helps to prepare an integration test or UI test for your change. There are three possible 
kind of changes:

### Changing analysis model without adding new API methods
 
If you have only changes in the analysis-model module (and you added no new API methods) then you need to rebuild 
the maven module `analysis-model.jar` and afterwards rebuild the associated Jenkins wrapper plugin 
analysis-model-api-plugin. This plugin then needs to be deployed into the Jenkins instance. For this process the 
script 

### Changing the warnings plugin 

If you have only changes in the warnings-ng plugin then you need to rebuild the Jenkins plugin `warnings-ng.jpi` 
and deploy it into the Jenkins instance. This task is provided by one of the following shell scripts:
- `./clean.sh`: Builds the plugin using `mvn clean install` and deploys it on success into the Jenkins instance.
- `./go.sh`: Builds the plugin using `mvn clean install -DskipITs` (skips the integration tests) 
and deploys it on success into the Jenkins instace.
- `./skip.sh`: Builds the plugin using `mvn clean install -DskipTests` (skips all tests and static analysis) 
and deploys it on success into the Jenkins instance.

## Acceptance Test Harness

UI tests can be started using an IntelliJ launcher configuration or using a command line script. As already mentioned,
all UI tests require to run within a given subject under test. In our case we use the latest available Jenkins LTS
version and the predefined set of plugins from our docker image.      

### Pooling of Jenkins Under Test (JUT)

UI tests execute really slowly. This is mostly because each test wants to launch its own clean Jenkins, and we end up 
mostly just waiting for Jenkins under test (JUT) to come up. This delay is also quite annoying when you are developing a 
new test. Often you have to run the test under development multiple times before you get your test right. And every 
time you run a test, you end up waiting for JUT to come up.

To help cope with this situation, this project comes with a separate entry point that runs a JUT server. 
The JUT server will maintain a fixed number of Jenkins instances booted. There's a corresponding PooledJenkinsController 
implementation you'd use when you run a test, which asks the JUT server to hand off a fresh JUT.  

Start the pooled Jenkins controller by calling the script `start-jut.sh`. This script creates a pool of Jenkins 
instances that is handed out on request. Since each test requires several seconds it is sufficient to set the pool
size to 1. I.e., as soon as a Jenkins instance has been handed out, the next one is prepared.

For more details please refer to the corresponding 
[documentation of the ATH](https://github.com/jenkinsci/acceptance-test-harness/blob/master/docs/PRELAUNCH.md).

### Running UI tests in IntelliJ

UI tests can be started using the corresponding launchers `UI Tests (Firefox)` or `UI Tests (Chrome)`. 
Note that both launchers require an installation of the corresponding Selenium drivers. If these drivers are not
installed in `/usr/local/bin` on your local machine then you need to adapt the launcher configurations to match
your setup.

### Running UI tests from the console

You can also start the UI tests using the provided shell scrips `testFirefox.sh` or `testChrome.sh`. Note that
you might need to adapt these scripts as well (see previous section).





# Development Environment for Jenkins Plugins 

This docker-based development environment is for new contributors of the Jenkins Warnings and Coverage Plugins to reduce the initial ramp-up time. It consists of the following parts:

- Scripts to check out all modules of these Jenkins plugins from GitHub. Depending on the part where you want to contribute to, you usually work with just one of these modules. However, it simplifies the development if all modules are already part of the workspace. Then you can switch at any time to one of the other modules.
- Docker-based Jenkins controller that has all required plugins installed to see these plugins in action. This Jenkins controller is already configured properly using [JCasC](https://github.com/jenkinsci/configuration-as-code-plugin) to build Java modules on a Linux based agent (also provided as docker container). It already contains some jobs that use all of these plugins (tests, coverage, static analysis).
- IntelliJ project that configures these plugins as Maven modules. This project contains presets of my [coding style](https://github.com/uhafner/codingstyle) and some other helpful configurations. Especially, it has runners configured to debug Jenkins plugins either on the controller or agent.

I presented this development environment in a [recorded Jenkins Online Meetup](https://youtu.be/u3eCEw6l8t0) in January 2022.

## Supported operating systems

The development environment has been tested on macOS, Ubuntu Linux (in a virtual machine running on macOS), and Windows. Pull requests are always welcome.

## TLDR

### Installation Requirements

#### Main Development

Latest version of the following tools:
- Docker (for Windows and macOS: install [Docker Desktop](https://www.docker.com/products/docker-desktop))
- IntelliJ Ultimate (or Community)
- Maven
- JDK 21
- Git
- Github CLI (optional)

#### UI Testing

Additionally, the latest versions of the following tools are required:

- firefox and gecko-driver
- chrome and chrome-driver

### Installation Steps

If errors occur, note the troubleshooting hints [below](#installation---troubleshooting). For Windows users: Use the Git Bash to execute the Shell scripts.

1. Clone and build the plugin modules using one of the scripts:
   1. `./bin/clone-repos-https.sh` to clone over http.
   2. `./bin/clone-repos.sh` if you already have set up an SSH key in GitHub.
   3. `./bin/fork-repos.sh` to fork and clone the repos using Github CLI.

   You must wait until the build succeeds before opening IntelliJ, otherwise IntelliJ will not find all generated classes. First time Maven users need to wait a couple of minutes until all dependencies have been downloaded from Maven central. 
2. Import the project into Intellij:
    1. Start IntelliJ
    2. Select Open...
    3. Select the folder `warnings-ng-plugin-devenv`
    4. Optional (only when IntelliJ asks): *Maven projects need to be imported* select *Enable Auto-Import*. 
    5. Optional (only when IntelliJ asks): *Trust And Open Maven project* select *Trust Project* (see [IntelliJ Online Help](https://www.jetbrains.com/help/idea/project-security.html))
3. Run the Test Launchers in IntelliJ for the analysis-model and coverage-model projects.
4. For Windows users: start Docker Desktop if not already done
5. Start Jenkins with `./bin/jenkins.sh`. This command builds the Jenkins Docker image, downloads all registered plugins and initializes the Jenkins workspace with some jobs. This requires some minutes as well (see Step 9).
6. Login to Jenkins at: http://localhost:8080/
7. Use the following credentials: 
    - User: admin
    - Password: admin
8. Start the provided Jenkins jobs that show the plugins in action:
    - **freestyle-analysis-model**: A Jenkins FreeStyle job that builds the analysis-model library. This job groups all warnings in a single Jenkins report and additionally shows the code coverage results of JaCoCo.
    - **pipeline-codingstyle**: A scripted Jenkins Pipeline that builds the codingstyle library. This job shows individual results for the different warnings parsers. Additionally, this job shows details of the JaCoCo code coverage and PIT mutation coverage.
    - **history-coverage-model**: A scripted Jenkins Pipeline that builds all existing releases of the coverage model project. The builds will take a while but show how the trend charts are working and how the delta calculations are done. The job produces results for all static analysis tools, the JaCoCo code coverage, and the PIT mutation coverage.
9. Deploy the current HEAD of the plugins to the Jenkins instance using the Launchers in IntelliJ.

### Installation - Troubleshooting

#### Step 1 - Installation failed
If all downloads have succeeded, but the installation failed due to errors, fix them 
and execute `mvn -V -U -e install –DskipTests` to retry only the installation.

#### Step 2 - "Command line is too long."
If the error "Command line is too long." occurs, execute following steps:
1. Click *Edit Configuration* and select the failed run configuration 
   (or click on the Test Launcher's name within the error message)
2. Click *Build and run* > *Modify options* > *Shorten command line*
3. Within the appeared field *Shorten command line*, select `@argfile (Java9+)`
4. Click *Apply* and *OK* and execute the Test Launcher again
5. (Possibly, IntelliJ has to be restarted if no tests has been found)

#### Step 3 - Jenkins test timeout
If tests fail due to a Jenkins test timeout, execute following steps:
1. Click *Edit Configuration* and select the failed run configuration
2. Add to *VM options*: `-Djenkins.test.timeout=1000`. This increases the timeout limit to 1000 seconds.

## Cloning the modules

You can use a simple shell script (`./bin/clone-repos.sh`) to clone and build the modules in a single step. The script checks out the following modules using the git SSH protocol. This requires that you have registered your public key in GitHub. If you have no keys in GitHub you can alternatively use the script `./bin/clone-repos-https.sh` that uses the HTTPS protocol.
- [codingstyle](https://github.com/uhafner/codingstyle): My codingstyle with preconfigured rules for CheckStyle, PMD, and SpotBugs. These configurations can be linked in IntelliJ to ensure that the code is compliant with the rules.
- [analysis-model](https://github.com/jenkinsci/analysis-model): A library to read static analysis reports into a Java object model. This module is not depending on Jenkins.
- [analysis-model-api-plugin](https://github.com/jenkinsci/analysis-model-api-plugin): A simple wrapper for the 
analysis model library. It provides the analysis-model classes as a Jenkins plugin. This overhead is required to simplify upgrades of the analysis-model module in Jenkins.
- [warnings-ng-plugin](https://github.com/jenkinsci/warnings-ng-plugin): A plugin to read static analysis reports and show the corresponding results in Jenkins.
- [coverage-model](https://github.com/jenkinsci/coverage-model): A library to read coverage reports into a Java object model. This module is not depending on Jenkins.
- [coverage-plugin](https://github.com/jenkinsci/coverage-plugin): A plugin to read coverage reports and show the corresponding results in Jenkins.

## Forking some modules

When you are planning to provide a pull request for one of the plugins you need to create a fork of the repository and make all changes in this fork. I created [a GitHub collaboration documentation](https://github.com/uhafner/codingstyle/blob/main/doc/Working-with-Github.md) in my coding style project.

## Modifying and debugging code with IntelliJ

IntelliJ (Ultimate) is the main supported development environment for the Warnings plugin. A predefined project is stored 
in the folder `.idea` that references all modules of the Warnings plugin. This project contains presets of my 
[coding style](https://github.com/uhafner/codingstyle) and some other helpful configurations. 

It should be possible to use other IDEs (Eclipse, Netbeans, Visual Studio Code) as well. 

### Running unit and integration tests

Use the provided IntelliJ Run Configurations `All in [module-name]` to run the unit and integrations tests of the corresponding module. These configurations are already configured to record the branch coverage of the corresponding module packages (use the `Run with Coverage` action).   

### Debugging 

Before you can debug your changes, you first need to find out where your code is running: on the controller or on the agent? If you are unsure, then run both remote debuggers, set some breakpoints and wait for the corresponding debugger to stop.

#### Debugging the Jenkins controller

The docker compose configuration starts the Jenkins controller automatically in 'Debug' mode, i.e., it is listening to remote debug requests. If your code runs in the controller, then you need to attach a remote debugger at `localhost:8000` (mapped to the same port in the docker container). Use the provided `Jenkins Controller (Remote Debugger)` Debug configuration to connect a debugger in IntelliJ.

#### Debugging the Jenkins agent

The docker compose configuration also starts the Jenkins agent automatically in 'Debug' mode, i.e., it is listening to remote debug requests. Attach a remote debugger at `localhost:8001` (mapped to the same port in the docker container) to debug code that is running on the agent. Use the provided `Jenkins Agent (Remote Debugger)` Debug configuration to connect a debugger in IntelliJ.

### Running UI tests

UI tests can be started using the corresponding launchers `UI Tests [module] (Firefox)` or `UI Tests [module] (Chrome)`. Note that both launchers require an installation of the corresponding Selenium drivers. If these drivers are not installed in `/opt/bin` on your local machine then you need to adapt the launcher configurations to match your setup.

All UI tests require running within a given subject under test (i.e, Jenkins under test, JUT), see [Acceptance Test Harness](https://github.com/jenkinsci/acceptance-test-harness) project for more details.

## Starting Jenkins 

This development environment contains a customized Jenkins installation where you can deploy
your modified plugins to, so you can see your changes directly in some preconfigured jobs that use these plugins.

Start the provided Jenkins controller in this project (you need to install [docker](https://www.docker.com) and 
[docker-compose](https://docs.docker.com/compose/overview/)). Open a terminal and run `./jenkins.sh` 
in the top level folder. This command is a wrapper to `docker-compose up`: it uses the right user and group settings
so that the permissions of the docker volume for the Jenkins home folder are correctly set.
This command creates a docker container for the Jenkins controller and one for the Java agent.
This will require some time when called the first time since the docker
images will be composed. After the images have been created the following two containers will be started:
- jenkins-controller: [Official Jenkins LTS docker image](https://github.com/jenkinsci/docker) (Alpine Linux). The controller is
preconfigured using [JCasC](https://github.com/jenkinsci/configuration-as-code-plugin) to build Java applications 
on an agent. The controller is not allowed to run jobs.
- java-agent: A minimal Java agent based on the 
[official OpenJDK8 docker image](https://hub.docker.com/_/openjdk?tab=description) (Alpine Linux). 
Controller and agent are connected using SSH.

You can then open Jenkins at the URL http://localhost:8080/. Use the following credentials to log in as administrator: 
- User: admin
- Password: admin

### Volume for JENKINS_HOME

The home directory of the Jenkins controller (JENKINS_HOME) is mounted as a 
[docker volume](https://docs.docker.com/storage/volumes/). I.e., it is visible on the host as a normal directory at
`./docker/volumes/jenkins-controller`. It will survive sessions and can be changed directly on the host, see 
[official documentation](https://github.com/jenkinsci/docker/blob/master/README.md) for details. 
This helps to inspect the files that have been created by the Jenkins controller.

Due to a performance problem in Jenkins' Job DSL plugin, setting up the new Jenkins instance is very slow.
Therefore, it makes sense to remove the job's configuration part of your `jenkins.yaml` file after the Jobs have been
created. You can overwrite the content of the file `./docker/volumes/jenkins-home/jenkins.yaml` in your newly created
Jenkins instance with the content in `jenkins-no-jobs.yaml`.

Volumes under macOS are quite slow. On my MacBook running the provided Jenkins job of the `analysis-model` in the
docker container is slower than running the same Jenkins job in a docker container that is running in a linux virtual machine
on the same MacBook (sounds kind of absurd :astonished:).

## Deploying changed plugins to Jenkins 

Once you finished your local development changes (i.e., the unit tests are all green) you should test your changes in 
Jenkins. This also helps to prepare an integration test or UI test for your change.

### Changing analysis model without adding new API methods
 
If you have only changes in the `analysis-model` module (and you added no new API methods) then you need to rebuild and install the maven module `analysis-model.jar` and afterwards rebuild the associated Jenkins wrapper plugin `analysis-model-api-plugin`. This plugin then needs to be deployed to Jenkins.

This process is simplified by running the script `./bin/go.sh` in the `analysis-model` module, it will install the module `analysis-model.jar` in your local maven repository. Then this script will build the actual plugin and deploy it to Jenkins. 

### Changing the warnings plugin 

If you have only changes in the warnings-ng plugin then you need to rebuild the Jenkins plugin `warnings-ng.jpi` 
and deploy it to Jenkins. You can use one of the following shell scripts for this task:
- `./bin/clean.sh`: Builds the plugin using `mvn clean install` and deploys it on success into the Jenkins instance.
- `./bin/go.sh`: Builds the plugin using `mvn clean install -DskipITs` (skips the integration tests) 
and deploys it on success into the Jenkins instace.
- `./bin/skip.sh`: Builds the plugin using `mvn clean install -DskipTests` (skips all tests and static analysis) 
and deploys it on success into the Jenkins instance.

### Changing analysis model by adding new API methods

TODO

### Changing forensics-api-plugin and git-forensics-plugin

If you have changes in one of the Foresics Plugins (API or Git implementation) then you need to 
rebuild these Jenkins plugins and deploy them into the Jenkins instance. 

To simplify this process run the script `./go.sh` in the corresponding plugin folder, it will build the
plugin and deploy it on success to Jenkins.

### Changing a module with breaking API changes

Before making breaking changes please get in touch with me. Typically, it is possible to make changes
backward compatible.

### IntelliJ Launchers to deploy the plugins 

The build scripts from the last section can also be started using one of the IntelliJ launchers 
`Build and Deploy [module-name]`.
These launchers build the corresponding plugin and deploy it into Jenkins. 

## Acceptance Test Harness

UI tests can be started using an IntelliJ launcher configuration or using a command line script. As already mentioned,
all UI tests require to run within a given subject under test. In our case we use the latest available Jenkins LTS
version and the predefined set of plugins from our docker image.      

### Running UI tests in IntelliJ

UI tests can be started using the corresponding launchers `UI Tests Warnings (Firefox)` or `UI Warnings Tests (Chrome)`. 
Note that both launchers require an installation of the corresponding Selenium drivers. If these drivers are not
installed in `/opt/bin` on your local machine then you need to adapt the launcher configurations to match
your setup.

### Running UI tests from the console

You can also start the UI tests using the provided shell scrips `testFirefox.sh` or `testChrome.sh`. Note that
you might need to adapt these scripts as well (see previous section).





node {
        def url = "https://github.com/jenkinsci/forensics-api-plugin.git"
        def tags = "git ls-remote -t --sort v:refname --refs $url | cut -f1 | tail -n +34"
        env.COUNT = sh(script: tags + " | wc -l", returnStdout: true).trim()
        if (env.COUNT.toInteger() > env.BUILD_NUMBER.toInteger()) {

            stage('Checkout') {
                echo "Build: ${BUILD_NUMBER}"
                env.HASH = sh(script: tags + " | head -n ${BUILD_NUMBER} | tail -1", returnStdout: true).trim()
                echo "Hash: ${HASH}"
                checkout([$class           : 'GitSCM',
                          branches         : [[name: "${HASH}"]],
                          userRemoteConfigs: [[url: "$url"]]])
            }

            stage ('Git mining') {
                    discoverGitReferenceBuild()
                    mineRepository()
                    gitDiffStat()
            }

            stage ('Build, Test, and Static Analysis') {
                withMaven(maven: 'mvn-default', mavenLocalRepo: '/var/data/m2repository', mavenOpts: '-Xmx768m -Xms512m') {
                    sh "mvn -V -U --fail-never -e clean verify -Denforcer.skip -Dmaven.test.failure.ignore"
                }

                recordIssues tools: [java(), javaDoc()], aggregatingResults: 'true', id: 'java', name: 'Java'
                recordIssues tool: errorProne()

                junit(allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml')

                publishCoverage(adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_ALL_BUILD'))
                recordIssues tools: [checkStyle(pattern: 'target/checkstyle-result.xml'),
                    spotBugs(pattern: 'target/spotbugsXml.xml'),
                    pmdParser(pattern: 'target/pmd.xml'),
                    cpd(pattern: 'target/cpd.xml'),
                    revApi(pattern: 'target/revapi-result.json'),
                    taskScanner(highTags:'FIXME', normalTags:'TODO', includePattern: '**/*.java', excludePattern: 'target/**/*')],
                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]]
            }

            build(job: env.JOB_NAME, wait: false)

        }
        else {
            echo "Stopping after ${COUNT} tags"
        }
}

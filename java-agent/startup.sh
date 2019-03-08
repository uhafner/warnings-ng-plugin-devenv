#!/usr/bin/sh

bash ./wait-for-it.sh ${JENKINS_MASTER_SERVER}:${JENKINS_MASTER_PORT} -t 300
bash ./wait-for-it.sh ${JENKINS_MASTER_SERVER}:${JENKINS_MASTER_JNLP_PORT} -t 300

java -jar slave.jar -jnlpUrl http://${JENKINS_MASTER_SERVER}:${JENKINS_MASTER_PORT}/computer/${SLAVE_NAME}/slave-agent.jnlp -secret ${JENKINS_TOKEN} -workDir /var/data

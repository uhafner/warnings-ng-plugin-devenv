FROM openjdk:8-jdk-alpine

RUN apk update \
    && apk add --virtual build-dependencies \
        bash \
        git

RUN mkdir /var/data
VOLUME /var/data

COPY slave.jar .
COPY wait-for-it.sh .
COPY startup.sh .
RUN chmod u+x startup.sh wait-for-it.sh

ENV JENKINS_MASTER_SERVER=localhost
ENV JENKINS_MASTER_PORT=8081
ENV JENKINS_MASTER_JNLP_PORT=50000
ENV SLAVE_NAME=java-agent
ENV JENKINS_TOKEN=6419cf9ecd7e991705ac12788f8c3e0fe1328d49b34cfee8cadd80a9d905fb1a

CMD ["sh", "./startup.sh"]

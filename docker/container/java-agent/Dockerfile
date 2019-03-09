FROM openjdk:8-jdk-alpine

RUN apk update \
    && apk add bash git openssh \
    && mkdir /root/.ssh \
    && chmod 0700 /root/.ssh \
    && ssh-keygen -A \
    && sed -i s/^#PasswordAuthentication\ yes/PasswordAuthentication\ no/ /etc/ssh/sshd_config \
    && echo "RSAAuthentication yes" > /etc/ssh/sshd_config \
    && echo "UsePAM yes" > /etc/ssh/sshd_config \
    && echo "PubkeyAuthentication yes" > /etc/ssh/sshd_config

RUN echo "root:Docker!" | chpasswd

RUN mkdir /var/data
VOLUME /var/data

COPY unsafe.pub /root/.ssh/authorized_keys
COPY docker-entrypoint.sh /
RUN chmod u+x docker-entrypoint.sh

RUN git config --global user.name "Jenkins Java Agent"
RUN git config --global user.email "java.agent@jenkins.master"

EXPOSE 22

ENTRYPOINT ["/docker-entrypoint.sh"]

# -D in CMD below prevents sshd from becoming a daemon. -e is to log everything to stderr.
CMD ["/usr/sbin/sshd", "-D", "-e"]

FROM artifactory.service.anz:8081/baseimages/redhat-openjdk-18/openjdk18-openshift:latest
 
USER root
 
ENV SERVER_NAME=rtl-txns-service-v1Server

COPY jvm.options /opt/$SERVER_NAME/conf/
COPY *.jar /opt/$SERVER_NAME/app/


RUN useradd -c 'application user' -m -u 1000 -g root -d /home/pspadmin -s /bin/bash pspadmin && \
mkdir -p /opt/$SERVER_NAME/logs && \
chown -R pspadmin:root /opt && \
chmod g+w /etc/passwd && \
chmod 777 /home/pspadmin && \
mkdir -p /home/pspuser && \
chmod 775 /home/pspuser
 
USER pspadmin
WORKDIR /opt/$SERVER_NAME/logs
RUN chmod -R 775 /opt

EXPOSE 8080

CMD java $(eval "echo \"$(cat /opt/$SERVER_NAME/conf/jvm.options)\"") -jar /opt/$SERVER_NAME/app/*.jar --spring.config.location=file:/opt/$SERVER_NAME/conf/

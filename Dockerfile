FROM openjdk:9-jdk-slim

ENV ENV docker

RUN mkdir -p /opt/ddfb

COPY build/libs/ddfb.jar /opt/ddfb/ddfb.jar

WORKDIR /opt/ddfb
ENTRYPOINT ["java", "-jar", "-Xmx256m", "ddfb.jar"]

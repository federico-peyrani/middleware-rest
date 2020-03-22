FROM openjdk:8u212-jdk

# MAVEN
RUN apt-get update \
     && apt-get install -y maven \
     && apt-get clean \
     && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Copy the POM-file first, for separate dependency resolving and downloading
# COPY pom.xml /usr/src/app
# RUN cd /usr/src/app \
#      && mvn dependency:resolve
# RUN cd /usr/src/app \
#      && mvn verify

COPY . /usr/src/app
RUN cd /usr/src/app \
     && mvn clean package

ENV PORT 8080
EXPOSE $PORT

COPY entrypoint.sh /usr/local/bin
RUN chmod 777 /usr/local/bin/entrypoint.sh
ENTRYPOINT ["entrypoint.sh"]

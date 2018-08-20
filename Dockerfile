FROM maven:3.5.4-jdk-8 AS build

# Add pom.xml and install dependencies
COPY pom.xml /build/pom.xml
WORKDIR /build
RUN mvn clean

# Add source code and (by default) build the jar
COPY src /build/src
ARG BUILD_COMMAND="mvn package"
RUN ${BUILD_COMMAND}

# This base image is only appropriate for running Tomcat services. Instead, for
# now, use the base openjdk image.
FROM openjdk:8-jre-slim

ENV HEALTHY_RESPONSE_CONTAINS='{"status":"UP"}'
COPY --from=build /build/target/nldi-crawler-*.jar app.jar

CMD java -jar app.jar ${CRAWLER_SOURCE_ID}

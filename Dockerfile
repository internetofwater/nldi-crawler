FROM maven:3.5.3-jdk-8 AS build

# Add pom.xml and install dependencies
COPY pom.xml /build/pom.xml
WORKDIR /build
RUN mvn clean

# Add source code and (by default) build the jar
COPY src /build/src
ARG BUILD_COMMAND="mvn verify"
RUN ${BUILD_COMMAND}


FROM openjdk:8-jre-slim

RUN useradd -ms /bin/bash spring

COPY --from=build /build/target/nldi-crawler-*.jar app.jar

USER spring
CMD java -jar app.jar ${CRAWLER_SOURCE_ID}

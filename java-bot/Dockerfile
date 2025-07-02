FROM eclipse-temurin:21-jdk-alpine

ARG VERSION
LABEL version="$VERSION"

VOLUME /tmp

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

USER appuser

ENV JAVA_OPTS=""

ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar

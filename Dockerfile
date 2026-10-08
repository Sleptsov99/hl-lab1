FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY checkstyle.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify -DskipTests=false

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget \
    && addgroup -S spamer && adduser -S spamer -G spamer
USER spamer
COPY --from=build /app/target/spamer-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

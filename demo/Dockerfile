FROM eclipse-temurin:25-jdk AS build
ARG DEVONN_REGISTRY_USERNAME
ARG DEVONN_REGISTRY_PASSWORD
WORKDIR /workspace

COPY mvnw mvnw
COPY .mvn .mvn
RUN mkdir -p /root/.m2
COPY docker/maven-settings.xml /root/.m2/settings.xml
COPY pom.xml pom.xml
COPY src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]

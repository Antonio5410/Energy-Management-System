# ---- Stage 1: Build the JAR ----
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build
WORKDIR /workspace
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -q -f auth-service/pom.xml -DskipTests dependency:go-offline
COPY auth-service/src auth-service/src
RUN mvn -q -f auth-service/pom.xml -DskipTests package

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
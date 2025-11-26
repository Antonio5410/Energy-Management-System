# ---- Stage 1: Build the JAR ----
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build
WORKDIR /workspace
COPY demo/pom.xml demo/pom.xml
RUN mvn -q -f demo/pom.xml -DskipTests dependency:go-offline
COPY demo/src demo/src
RUN mvn -q -f demo/pom.xml -DskipTests package

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY --from=build /workspace/demo/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
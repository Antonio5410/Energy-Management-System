# Stage 1: build cu Maven
FROM maven:3.9.9-eclipse-temurin-23-alpine AS build

WORKDIR /workspace

# Copiem pom-ul și tragem dependențele
COPY demo/pom.xml demo/pom.xml
RUN mvn -q -f demo/pom.xml -DskipTests dependency:go-offline

# Copiem sursele și build-uim jar-ul
COPY demo/src demo/src
RUN mvn -q -f demo/pom.xml -DskipTests package

# Stage 2: imagine mică de runtime
FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

# Copiem jar-ul rezultat din build
COPY --from=build /workspace/demo/target/*.jar app.jar

# Variabile de mediu pentru config
ENV PORT=8080
ENV DB_IP=energy-monitoring-db
ENV DB_PORT=5432
ENV DB_DBNAME=energy-monitoring-db
ENV DB_USER=postgres
ENV DB_PASSWORD=root

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

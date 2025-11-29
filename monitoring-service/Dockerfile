FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY demo/target/demo-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
ENV DB_IP=energy-monitoring-db
ENV DB_PORT=5432
ENV DB_DBNAME=energy-monitoring-db
ENV DB_USER=postgres
ENV DB_PASSWORD=root

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

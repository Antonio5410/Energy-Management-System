Energy Management System – README

1. Overview

    The Energy Management System is a distributed application composed of multiple microservices that manage users, devices and energy consumption data. The system communicates through REST APIs and RabbitMQ message brokers. Each microservice is deployed in Docker and routed through Traefik as a reverse proxy.

    The project contains the following major components:
        1. User Service – handles CRUD operations on users and publishes synchronization events.
        2. Device Service – manages devices, including their maximum hourly consumption, and processes synchronization messages from User Service.
        3. Monitoring Service – consumes measurement data from smart meter simulators, aggregates it into hourly consumption totals, and stores the results.
        4. Device Data Simulator – standalone application that generates synthetic smart meter readings and sends them through RabbitMQ.
        5. RabbitMQ Brokers – one for synchronization (user/device events) and one for measurement data.
        6. Traefik Reverse Proxy – exposes the services and handles routing inside Docker.

    The system follows the architecture and requirements from Assignment 1 and Assignment 2 of the Distributed Systems laboratory.



2. Architecture Description

    2.1 Microservices
        User Service
            Exposes REST endpoints for creating, retrieving, updating and deleting users.
            Publishes a message to the Synchronization Queue whenever a new user is created.
            Stores user information in its own PostgreSQL database.

        Device Service
            Exposes REST endpoints for device management.
            Stores devices together with their maximum hourly consumption.
            Consumes user synchronization events and inserts user IDs in its local database.
            Publishes device synchronization events when a new device is created.
            Stores device data in a separate PostgreSQL database.

        Monitoring Service
            Subscribes to the Measurement Queue and processes incoming device readings.
            Aggregates 10-minute measurement values into hourly totals.
            Stores hourly consumption in a dedicated PostgreSQL database.
            Consumes device synchronization events to maintain an updated list of known device IDs.
            Exposes endpoints for retrieving historical consumption for chart visualization.

    2.2 Communication Flow
        There are two RabbitMQ-based communication paths:
            1. Synchronization Flow
                User Service → publishes user.created
                Device Service → consumes user.created
                Device Service → publishes device.created
                User Service and Monitoring Service → consume device.created

            2. Measurement Flow
                Device Data Simulator → publishes measurement messages (every 10 minutes)
                Monitoring Service → consumes messages and computes hourly totals

    2.3 Deployment Model
        All components are deployed using Docker Compose.
        Traefik acts as reverse proxy and exposes the services under different routes.

        Each service runs in its own container and connects to its own PostgreSQL instance.
        RabbitMQ runs in a dedicated container and exposes management UI if needed.



3. How to Build and Run the Project

    3.1 Prerequisites
        Docker and Docker Compose
        Java 17+
        Maven
        Node.js (if building frontend components)
        RabbitMQ (handled automatically through Docker)

    3.2 Running the entire system
        From the project root, run:
            docker compose up --build
        This command will:
            build the microservices,
            start PostgreSQL databases,
            start RabbitMQ,
            start Traefik,
            expose the services through configured HTTP routes.

    3.3 Running a single microservice locally
        Inside a microservice folder:
            mvn clean install
            mvn spring-boot:run
        Make sure to stop any conflicting Docker instance of the same service to avoid port collisions.



4. Device Data Simulator

    The simulator is a standalone Java application that:
        Generates a random baseline load for each device.
        Produces one measurement every 10 minutes.
        Sends messages in JSON format to the Measurement Queue.

    Each message contains:
        timestamp
        device ID
        measurement value
    The device ID is configurable in the simulator configuration file.



5. Databases

    Each microservice uses its own PostgreSQL database:
        people-db for User Service
        device-db for Device Service
        monitoring-db for Monitoring Service

    The schema is automatically created on first run when spring.jpa.hibernate.ddl-auto is set to update or create.



6. REST Endpoints Overview

    User Service
        POST /people – create user
        GET /people – list users
        GET /people/{id} – get user details

    Device Service
        POST /devices – create device
        GET /devices – list devices
        GET /devices/{id} – get device details

    Monitoring Service
        GET /monitoring/consumption?deviceId=&date= – returns hourly totals for the selected date



7. Synchronization Events

    User and device synchronization ensures consistency between microservices.

    Example user event:
        {
        "event": "user.created",
        "id": "UUID",
        "name": "...",
        "age": ...
        }

    Example device event:
        {
        "event": "device.created",
        "deviceId": 7,
        "maxHourlyConsumption": 2.5
        }

    Each microservice updates its internal tables accordingly.



8. Measurement Processing Logic

    Monitoring Service receives measurement messages in the form:
        {
        "timestamp": "...",
        "deviceId": ...,
        "measurement": ...
        }

    Processing steps:
        1. Parse timestamp and determine hour of day.
        2. Search for an existing hourly record for that device.
        3. If found, increment the total.
        4. Otherwise, create a new hourly record.
        5. Save the updated total to the database.

    Hourly consumption data is later used for rendering daily charts.



9. Traefik Configuration

    Traefik routes external requests to each microservice using label-based routing.
    Each service defines:
        router rule
        service name
        internal port
        optional middleware (path prefix stripping)

    Example route rule:
        traefik.http.routers.device.rule=PathPrefix(/devices)



10. Limitations and Notes

    The project does not implement alerting when hourly consumption exceeds the device’s maximum allowed hourly limit.
    No authentication or authorization mechanism is enabled.
    Device simulator must be started manually; it is not containerized unless added explicitly.
    In case of schema changes, database migration tools (Flyway/Liquibase) are recommended but not included.


11. Future Extensions

    Possible improvements include:
        Real-time WebSocket notifications for consumption threshold violations.
        JWT authentication and role-based access control.
        Unified logging with ELK or Loki.
        Horizontal scaling with multiple replicas.
        Property-based simulator configuration for multiple concurrent devices.
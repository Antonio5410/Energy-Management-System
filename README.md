Energy Management System – README

1. Overview

    The Energy Management System is a distributed application composed of multiple microservices that manage users, devices, energy consumption data, and real-time user support. The system communicates through REST APIs, RabbitMQ message brokers, and WebSocket connections. Each microservice is deployed in Docker containers and routed through Traefik as a reverse proxy.
   
    The project follows the architecture and requirements defined in Assignment 1, Assignment 2, and is extended in Assignment 3 with real-time communication and AI-assisted support.

    The system contains the following major components:
   
        1. User Service – handles CRUD operations on users and publishes synchronization events.
        2. Device Service – manages devices, including their maximum hourly consumption, and processes synchronization messages.
        3. Monitoring Service – processes energy measurements, aggregates hourly consumption, and detects abnormal situations.
        4. Realtime Support Service – provides real-time chat, notifications, rule-based support, and AI-assisted suggestions.
        5. Device Data Simulator – standalone application that generates synthetic smart meter readings.
        6. RabbitMQ Brokers – used for synchronization events, measurement data, and alert notifications.
        7. Traefik Reverse Proxy – exposes the services and handles routing inside Docker.

3. Architecture Description

    2.1 Microservices
   
        User Service
            Exposes REST endpoints for creating, retrieving, updating and deleting users.
            Publishes a message to the synchronization queue whenever a new user is created.
            Stores user information in its own PostgreSQL database.
   
        Device Service
            Exposes REST endpoints for device management.
            Stores devices together with their maximum allowed hourly consumption.
            Consumes user synchronization events and inserts user IDs in its local database.
            Publishes device synchronization events when a new device is created.
            Stores device data in a separate PostgreSQL database.
   
        Monitoring Service
            Subscribes to the measurement queue and processes incoming device readings.
            Aggregates 10-minute measurement values into hourly consumption totals.
            Stores hourly consumption in a dedicated PostgreSQL database.
            Consumes device synchronization events to maintain an updated list of devices.
            Detects overconsumption situations based on device thresholds.
            Publishes overconsumption alerts to a dedicated RabbitMQ queue.
   
        Realtime Support Service
            Provides real-time communication between administrators and clients.
            Uses WebSocket (STOMP) for bidirectional communication.
            Consumes overconsumption alert events from RabbitMQ.
            Sends real-time notifications to connected users.
            Implements rule-based support suggestions using predefined rules.
            Integrates an external AI service for intelligent assistance.

    2.2 Communication Flow
   
        The system uses multiple communication paths:
        1. Synchronization Flow
            User Service publishes user.created events.
            Device Service consumes user.created events.
            Device Service publishes device.created events.
            Monitoring Service consumes device.created events.
        2. Measurement Flow
            Device Data Simulator publishes measurement messages every 10 minutes.
            Monitoring Service consumes messages and computes hourly totals.
        3. Alert and Realtime Flow (Assignment 3)
            Monitoring Service publishes overconsumption alerts.
            Realtime Support Service consumes alert events.
            Realtime Support Service forwards notifications to clients via WebSocket.

    2.3 Deployment Model
   
        All components are deployed using Docker Compose.
        Traefik acts as a reverse proxy and routes HTTP and WebSocket traffic.
        Each microservice runs in its own container.
        Each service connects to its own PostgreSQL database.
        RabbitMQ runs in a dedicated container and exposes a management interface.

5. How to Build and Run the Project

    3.1 Prerequisites
   
        Docker and Docker Compose
        Java 17 or higher
        Maven
        Node.js (for frontend components if applicable)

    3.2 Running the entire system
   
        From the project root directory, run:
            docker compose up --build
        This command builds and starts all microservices, databases, RabbitMQ, Traefik, and the frontend.

    3.3 Running a single microservice locally
   
        Inside the microservice directory:
            mvn clean install
            mvn spring-boot:run
        The corresponding Docker container must be stopped to avoid port conflicts.

7. Device Data Simulator

    The simulator is a standalone Java application that:
   
        Generates a baseline load for each device.
        Produces one measurement every 10 minutes.
        Sends JSON messages to the measurement queue.

    Each message contains:
   
        timestamp
        device ID
        measurement value

    The device ID and generation parameters are configurable.

9. Databases

    Each microservice uses its own PostgreSQL database:
   
        people-db for User Service
        device-db for Device Service
        monitoring-db for Monitoring Service

    Database schemas are generated automatically at startup when Hibernate DDL auto-update is enabled.

11. REST Endpoints Overview
   
    User Service
    
        POST /people – create user
        GET /people – list users
        GET /people/{id} – get user details

    Device Service
    
        POST /devices – create device
        GET /devices – list devices
        GET /devices/{id} – get device details

    Monitoring Service
    
        GET /monitoring/consumption?deviceId=&date= – returns hourly consumption totals

13. Realtime Support and WebSocket Communication

    The Realtime Support Service exposes WebSocket endpoints using the STOMP protocol.
    Clients establish a persistent WebSocket connection to receive:
    
        chat messages between administrator and client
        system notifications
        overconsumption alerts

    Messages are routed using topic-based destinations, allowing user-specific notifications.

15. Rule-Based and AI-Assisted Support

    The Realtime Support Service includes a rule-based support system implemented using predefined rules.
    These rules generate immediate suggestions without relying on external services.

    In addition, the system integrates an external AI service using the OpenAI API.
    AI requests are performed via HTTPS and isolated in a dedicated service layer.
    The OpenAI API key is provided through environment variables.
    If the AI service is unavailable, the system continues to function using rule-based logic.

16. RabbitMQ Queues

    The following queues are used:
    
        synchronization queue for user and device events
        measurement queue for device readings
        overconsumption.alerts queue for alert notifications

    RabbitMQ ensures asynchronous communication and loose coupling between microservices.

18. Traefik Configuration

    Traefik routes external requests to microservices using label-based configuration.
    Each service defines routing rules, internal ports, and optional middleware.
    WebSocket connections are routed through Traefik using HTTP upgrade.

19. Limitations and Notes

    Authentication and authorization are not enforced.
    The device simulator is not containerized by default.
    Database migration tools are not included.
    Alert thresholds are configured per device but can be extended.

20. Future Extensions

    Possible improvements include:
    
        authentication with JWT and role-based access control
        persistent chat history storage
        advanced AI prompt customization
        horizontal scaling of realtime services
        centralized logging and monitoring

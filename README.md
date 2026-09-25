# LogisticsConnect

LogisticsConnect is a Java-based distributed logistics system that demonstrates how multiple services can communicate using **REST APIs**, **JSON**, and **JMS messaging**.

The system represents a logistics environment where packages move through different hubs and their status can be tracked as they progress through the system.

---

## Architecture

```text
                    hubs-global.csv
                          |
                          v
                +--------------------+
                | Ingestion Service  |
                |      :7050         |
                +---------+----------+
                          |
                         REST
                          |
                          v
                +--------------------+
                |    Hub Service     |
                |      :7051         |
                +---------+----------+
                          |
                         REST
                          |
                          v
                +--------------------+
                |  Transit Service  |
                |      :7053         |
                +--------------------+
                          ^
                          |
                    JMS message
                          |
             +------------+-------------+
             | package-status-topic     |
             |       ActiveMQ           |
             +------------+-------------+
                          ^
                          |
                    JMS publish
                          |
                +---------+----------+
                | Delay Stage Service|
                |      :7052         |
                +--------------------+

                         |
                         | same topic
                         v
                +--------------------+
                |     AlertBot       |
                |      :7054         |
                +--------------------+
```


### Services

| Service             | Port | Responsibility                                                                    |
| ------------------- | ---: | --------------------------------------------------------------------------------- |
| Ingestion Service   | 7050 | Reads package/hub information from the CSV file and provides it to other services |
| Hub Service         | 7051 | Provides hub information through a REST API                                       |
| Delay Stage Service | 7052 | Processes package status and detects delays                                       |
| Transit Service     | 7053 | Handles package transit information                                               |
| AlertBot Service    | 7054 | Receives package updates and provides alerts                                      |

---

## Technologies Used

* Java
* Maven
* REST APIs
* JSON
* JMS
* Apache ActiveMQ
* Docker
* Docker Compose
* JUnit

---

# Stage 1 - Data Ingestion

The first stage of LogisticsConnect is responsible for loading logistics information from the provided CSV file.

The ingestion service reads the hub/package information and makes it available through a REST endpoint.

### Ingestion Service

The service runs on:

```text
http://localhost:7050
```

The purpose of this service is to separate the initial data ingestion from the rest of the application.

Instead of having every service read the CSV file directly, the ingestion service provides the data to other services.

---

# Stage 2 - REST Communication

The second stage introduces communication between the different services using REST APIs.

The services communicate using JSON data.

The main communication flow is:

```text
Ingestion Service
       |
       v
Hub Service
       |
       v
Delay Stage Service
       |
       v
Transit Service
```

REST is useful when one service needs to directly request information from another service.

For example:

```text
Delay Stage Service
        |
        | HTTP Request
        v
Transit Service
```

The response is returned as JSON.

### Why REST?

REST is simple to understand and works well when a service needs an immediate response from another service.

---

# Stage 3 - Message Queue

The third stage introduces asynchronous communication using JMS.

Instead of the `delay-stage-service` directly calling the `transit-service`, the service publishes a message to a JMS topic.

The topic used by the project is:

```text
package-status-topic
```

The communication becomes:

```text
Delay Stage Service
        |
        | JMS Message
        v
package-status-topic
        |
        v
Transit Service
```

This means that the delay stage does not need to wait for the transit service to process the message.

---

## ActiveMQ

LogisticsConnect uses **Apache ActiveMQ** as the message broker.

The broker is started using Docker Compose.

The default broker port is:

```text
61616
```

The ActiveMQ web console is available on:

```text
http://localhost:8161
```

The application uses the following topic:

```text
package-status-topic
```

---

# Running the Project

## Requirements

Before running the project, make sure you have:

* Java installed
* Maven installed
* Docker installed
* Docker Compose available

---

## 1. Start ActiveMQ

From the project directory, run:

```bash
docker compose up -d
```

This starts the ActiveMQ message broker.

You can check that the container is running with:

```bash
docker ps
```

---

## 2. Build the Project

Run:

```bash
mvn clean install
```

This compiles the services and runs the tests.

---

## 3. Start the Services

Start each service using Maven or IntelliJ.

### Ingestion Service

```text
Port: 7050
```

### Hub Service

```text
Port: 7051
```

### Delay Stage Service

```text
Port: 7052
```

### Transit Service

```text
Port: 7053
```

### AlertBot Service

```text
Port: 7054
```

---

# REST and JSON

The project uses REST endpoints for communication between services.

JSON is used as the data format because it is lightweight and easy for different services to exchange.

A typical JSON message can look like:

```json
{
  "packageId": "PKG001",
  "status": "IN_TRANSIT",
  "hub": "JHB01"
}
```

The receiving service can read the JSON and use the information to perform its task.

---

# Messaging Flow

The asynchronous messaging flow uses the JMS topic:

```text
package-status-topic
```

The `delay-stage-service` publishes package status messages.

The `transit-service` subscribes to the topic and receives those messages.

This creates a looser connection between the services.

```text
                    +----------------------+
                    | Delay Stage Service  |
                    +----------+-----------+
                               |
                               | JMS
                               v
                    +----------------------+
                    | package-status-topic |
                    +----------+-----------+
                               |
                               | JMS
                               v
                    +----------------------+
                    |   Transit Service    |
                    +----------------------+
```

---

# Why Use REST and Messaging?

REST and messaging are used for different communication requirements.

### REST

REST is useful when:

* A service needs information immediately.
* One service directly requests data from another service.
* The communication is request/response based.

### Messaging

Messaging is useful when:

* A service can process information asynchronously.
* The sender should not have to wait for the receiver.
* Services should be more loosely coupled.
* Events need to be published for other services to consume.

LogisticsConnect uses both approaches to demonstrate the difference between synchronous and asynchronous communication.

---

# Project Structure

The project is organised into separate services:

```text
LogisticsConnect/
│
├── ingestion-service/
│
├── hub-service/
│
├── delay-stage-service/
│
├── transit-service/
│
├── alertbot-service/
│
├── docs/
│   └── architecture.png
│
├── docker-compose.yml
│
└── README.md
```

Each service has its own responsibility instead of putting the entire application into one large program.

---

# Design Decisions

## Microservices

The project is divided into multiple services so that each service has a specific responsibility.

This makes it easier to understand and maintain the application.

## REST

REST is used where one service needs to directly communicate with another service and receive a response.

## JMS

JMS is used for asynchronous communication between services.

The `package-status-topic` allows package status information to be published and consumed without requiring a direct REST call.

## JSON

JSON provides a simple format for exchanging information between services.

## ActiveMQ

ActiveMQ acts as the message broker between the services that use JMS.

## Docker

Docker is used to run the ActiveMQ broker without requiring it to be installed directly on the host machine.

---

# Testing

The project uses tests to check that the services and their communication work as expected.

Tests can be run using:

```bash
mvn test
```

The tests should be run after making changes to ensure that existing functionality has not been broken.

---

# Learning Resources

The project is based around concepts including:

* RESTful APIs
* JSON serialisation
* JMS
* Message queues and topics
* Microservices
* Event-driven architecture
* Integration patterns

Useful resources:

* [microservices.io](https://microservices.io/)
* [Martin Fowler - Integration Patterns](https://martinfowler.com/)

---

# Project Demonstration

The project can be demonstrated by showing:

1. The individual services.
2. The REST communication between services.
3. JSON being exchanged between services.
4. ActiveMQ running through Docker.
5. The `package-status-topic`.
6. A message being published by the delay stage service.
7. The transit service receiving the message.
8. The overall architecture shown in the architecture diagram.

---

# Author

**Phuluso Matumba**

GitHub:

https://github.com/Phuluso14

# CDI Eureka Service

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

## Introduction

The CDI Eureka Service simplifies managing and monitoring microservice ecosystems using Netflix Eureka by providing a unified API to query and group registered services and their instances. This aids DevOps and developers in gaining visibility across multiple Eureka servers, enabling smarter infrastructure and service management.


## 🎯 Project Purpose

In large-scale microservice architectures, manually tracking hundreds of services and instances distributed across different Eureka Discovery Servers becomes complex and time-consuming. This project provides through **a single API endpoint**:

- **Server-based groupings** (which services are running on which servers?)
- **Service-based groupings** (on which servers is a service running?)
- **Centralized monitoring and visibility**

## 🏗️ Architectural Design

### Design Patterns Used

- **Strategy Pattern**: Different grouping strategies (`ServerGroupingStrategy`, `ServiceGroupingStrategy`)
- **Factory Pattern**: Strategy selection (`GroupingStrategyFactory`)
- **Builder Pattern**: Response object construction
- **Chain of Responsibility**: Exception handling layers
- **Template Method**: Base classes for common operation flows

### High-Level Architecture
```
+-----------------+    +------------------+    +-----------------+
|   REST Client   | -> |  CDI Eureka      | -> |  Eureka Server  |
|                 |    |  Service         |    |                 |
+-----------------+    +------------------+    +-----------------+
                             |
                             v
                      +----------------+
                      |  Grouped       |
                      |  Response      |
                      +----------------+
```

### Layered Architecture

```
┌─────────────────────────────────────┐
│           Controller Layer          │ ← REST endpoints
├─────────────────────────────────────┤
│            Service Layer            │ ← Business logic
├─────────────────────────────────────┤
│           Strategy Layer            │ ← Grouping algorithms
├─────────────────────────────────────┤
│            Client Layer             │ ← Eureka communication
├─────────────────────────────────────┤
│             Data Layer              │ ← Response mapping
└─────────────────────────────────────┘
```


## 🚀 Key Features

- **Flexible Grouping**: Server-based or service-based data organization
- **Robust Error Handling**: Comprehensive exception management with custom error codes
- **Transaction Tracking**: Unique transaction ID for each request
- **Performance Monitoring**: Built-in response time tracking
- **Validation**: Request parameter validation with detailed error messages
- **Logging**: Structured logging with transaction correlation
- **Fault Tolerance**: Timeout and connection error handling

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle
- **Language**: Java 17+
- **Web**: Spring WebMVC
- **Validation**: Jakarta Validation API
- **JSON Processing**: Jackson
- **HTTP Client**: RestTemplate
- **Logging**: SLF4J + Logback

## 📋 API Specification

### Endpoint

```http
GET /cdi-eureka-service/v1/eureka/apps
```

### Request Body

```json
{
    "eurekaServerURL": "http://localhost:8761/eureka",
    "groupBy": "services"
}
```

**Parameters:**
- `eurekaServerURL` (required): Valid Eureka server URL
    - Must start with `http://` or `https://`
    - Must contain `/eureka` path segment
    - Example: `http://localhost:8761/eureka`

- `groupBy` (required): Grouping strategy
    - `servers`: Group by server hostnames, list services per server
    - `services`: Group by service names, list servers per service


#### Success Response
```json
{
  "returnCode": "SUCCESS",
  "message": "Request completed successfully.",
  "httpStatusCode": 200,
  "transactionID": "87f5c9e4-62c4-4172-b185-bdfbd945f4f3",
  "elapsedTime": 20.0,
  "services": [
    {
      "service": {
        "serviceName": "EUREKA-SERVER",
        "servers": [
          {
            "server": {
              "hostname": "server-2",
              "instanceDetail": {
                "ipAddr": "172.21.0.3",
                "port": 8761,
                "securePort": 443,
                "url": "http://server-2:8761/actuator/health",
                "homePageUrl": "http://server-2:8761/",
                "statusPageUrl": "http://server-2:8761/actuator/info",
                "status": "UP",
                "lastUpdatedTimestamp": 1749468244945,
                "lastDirtyTimestamp": 1749468214929,
                "isCoordinatingDiscoveryServer": true,
                "metadataMap": {
                  "version": null,
                  "region": null,
                  "zone": null,
                  "instanceType": null,
                  "buildNumber": null
                },
                "leaseInfo": {
                  "renewalIntervalInSecs": 30,
                  "durationInSecs": 90,
                  "registrationTimestamp": 1749468244945,
                  "lastRenewalTimestamp": 1749471305394,
                  "evictionTimestamp": 0,
                  "serviceUpTimestamp": 1749468215284
                }
              }
            }
          }
        ]
      }
    }
  ]
}

```

#### Error Response
```json
{
  "returnCode": "INVALID_REQUEST",
  "message": "eurekaServerURL: Eureka server URL must start with http:// or https://, contain a valid host, and end with /eureka; ",
  "httpStatusCode": 400,
  "transactionID": "f6b3f959-92e4-4520-9fbf-8a661a3de5aa",
  "elapsedTime": 0.0
}
```

## 🔄 Grouping Strategies

### 1. Server-based Grouping (`groupBy: "servers"`)

Groups services by their host servers. Useful for:
- **Infrastructure monitoring**
- **Server capacity planning**
- **Service distribution analysis**

**Response Structure:**
```json
{
    "servers": [
        {
            "server": {
                "hostName": "app-server-01",
                "services": [
                    {"service": {"serviceName": "user-service"}},
                    {"service": {"serviceName": "order-service"}}
                ]
            }
        }
    ]
}
```
### 2. Service-based Grouping (`groupBy: "services"`)

Groups servers by service types. Useful for:
- **Service scalability analysis**
- **Load balancing decisions**
- **Service deployment tracking**

**Response Structure:**
```json
{
    "services": [
        {
            "service": {
                "serviceName": "user-service",
                "servers": [
                    {"server": {"hostname": "app-server-01"}},
                    {"server": {"hostname": "app-server-02"}}
                ]
            }
        }
    ]
}
```
## 🎯 Error Codes

| Code                  | HTTP Status | Description                           |
|-----------------------|-------------|---------------------------------------|
| `SUCCESS`             | 200         | Request completed successfully        |
| `INVALID_REQUEST`     | 400         | Malformed request or validation error |
| `INVALID_TOKEN`       | 400         | Invalid JWT token                     |
| `AUTH_REQUIRED`       | 401         | Authentication required               |
| `ACCESS_DENIED`       | 403         | Insufficient permissions              |
| `SERVICE_DOWN`        | 502         | Target service unavailable            |
| `TIMEOUT`             | 504         | Request timeout                       |
| `INVALID_HOST`        | 400         | Host resolution failed                |
| `SERVICE_UNAVAILABLE` | 503         | Service temporarily unavailable       |
| `UNKNOWN`             | 500         | Unexpected internal error             |

## 🧪 Testing

### Test Environment Setup

The project includes two different Docker Compose configuration that simulates multiple Eureka servers with different applications:

```yaml
version: '3.8'
services:
  eureka1:
    build:
      context: ./eureka-server
    container_name: eureka1
    ports:
      - "8761:8761"
    environment:
      - PORT=8761
      - HOSTNAME=server-1
      - EUREKA_URLS=http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
    networks:
      - eureka-shared-network
      - eureka-network-1

  service-a-1:
    build:
      context: ./example-service-a
    environment:
      - HOSTNAME=server-1
      - EUREKA_URLS=http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
    depends_on:
      - eureka1
    networks:
      - eureka-network-1

  service-b-1:
    build:
      context: ./example-service-b
    environment:
      - HOSTNAME=server-1
      - EUREKA_URLS=http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
    depends_on:
      - eureka1
    networks:
      - eureka-network-1

networks:
  eureka-network-1:
    driver: bridge
  eureka-shared-network:
    external: true
```
```yaml
version: '3.8'
services:
  eureka2:
    build:
      context: ./eureka-server
    container_name: eureka2
    ports:
      - "8762:8761"
    environment:
      - PORT=8761
      - HOSTNAME=server-2
      - EUREKA_URLS=http://eureka1:8761/eureka/,http://eureka2:8761/eureka/
    networks:
      - eureka-shared-network
      - eureka-network-2

  service-a-2:
    build:
      context: ./example-service-a
    environment:
      - HOSTNAME=server-2
      - EUREKA_URLS=http://eureka2:8761/eureka/,http://eureka1:8761/eureka/
    depends_on:
      - eureka2
    networks:
      - eureka-network-2

  service-b-2:
    build:
      context: ./example-service-b
    environment:
      - HOSTNAME=server-2
      - EUREKA_URLS=http://eureka2:8761/eureka/,http://eureka1:8761/eureka/
    depends_on:
      - eureka2
    networks:
      - eureka-network-2


networks:
  eureka-network-2:
    driver: bridge
  eureka-shared-network:
    external: true

```

### Running Tests
1. **Start Test Environment**
   ```bash
   docker compose -f docker-compose.server1.yml -p server1 up -d 
   docker compose -f docker-compose.server2.yml -p server2 up -d 
   ```
2. **Wait for Services to Register**
   ```bash
   # Wait 30-60 seconds for services to register with Eureka
   ```
3. **Test Server Grouping**
   ```bash
   curl -X GET http://localhost:8080/cdi-eureka-service/v1/eureka/apps \
     -H "Content-Type: application/json" \
     -d '{
       "eurekaServerURL": "http://localhost:8761/eureka",
       "groupBy": "servers"
     }'
   ```

4. **Test Service Grouping**
   ```bash
   curl -X GET http://localhost:8080/cdi-eureka-service/v1/eureka/apps \
     -H "Content-Type: application/json" \
     -d '{
       "eurekaServerURL": "http://localhost:8761/eureka",
       "groupBy": "services"
     }'
   ```
5. **Test Error Scenarios**
```bash
   # Invalid URL format
   curl -X GET http://localhost:8080/cdi-eureka-service/v1/eureka/apps \
     -H "Content-Type: application/json" \
     -d '{
       "eurekaServerURL": "invalid-url",
       "groupBy": "servers"
     }'

   # Invalid groupBy parameter
   curl -X GET http://localhost:8080/cdi-eureka-service/v1/eureka/apps \
     -H "Content-Type: application/json" \
     -d '{
       "eurekaServerURL": "http://localhost:8761/eureka",
       "groupBy": "invalid"
     }'
   ```
### Expected Test Results
- **Server Grouping**: Returns services grouped by their host names
- **Service Grouping**: Returns servers grouped by service types
- **Error Handling**: Returns appropriate error codes with detailed messages
- **Performance**: Response times should be under 1 second for typical loads

## 🔧 Configuration

### Application Properties

```yaml
spring:
  application:
    name: cdi-eureka-service
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://${eureka.instance.hostname}:8761/eureka/,http://${eureka.instance.hostname}:8761/eureka/

server:
  port: 8080
```

## 📊 Logging

The service implements structured logging with transaction tracking:

### Log Format
```
2025-06-09T16:21:11.472+04:00  INFO 33164 --- [cdi-eureka-service] [nio-8080-exec-8] c.d.dto.response.EurekaQueryResponse     : [TxID: aa309a44-5b38-4137-adae-b4a3f6ba0635] Starting Eureka query - GroupBy: servers, URL: http://localhost:8762/eureka
```

### Log Levels
- **TRACE**: Detailed execution flow
- **DEBUG**: Development and troubleshooting information
- **INFO**: General application flow
- **WARN**: Recoverable errors and important notices
- **ERROR**: Serious errors requiring attention

### Transaction Tracking
Each request receives a unique transaction ID that appears in all related log entries, making it easy to trace request execution across components.


### Code Quality Standards
- Java naming conventions (PascalCase for classes, camelCase for variables)
- 4-space indentation
- Defensive programming practices
- SOLID principles adherence

## 🧪 Testing

> **Note**: Comprehensive unit and integration tests are planned for the next development phase.

### Planned Test Coverage
- **Integration Tests**: End-to-end API testing
- **Performance Tests**: Load testing with multiple Eureka instances

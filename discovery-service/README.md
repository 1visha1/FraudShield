# Discovery Service

> **Eureka Service Registry** - enables service-to-service discovery without hardcoded IP addresses or ports.

---

## Overview

The **Discovery Service** runs Netflix Eureka Server and acts as the central service registry for all microservices in the SecurePay360 platform. All services register themselves on startup and query Eureka to find each other by logical service name.

---

## Responsibilities

- Run the Eureka Server
- Accept service registrations from all microservices
- Maintain health heartbeats and deregister unresponsive instances
- Enable the API Gateway to resolve `lb://SERVICE-NAME` to actual service instances

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Registry | Spring Cloud Netflix Eureka Server |

---

## Port

`8761` (publicly accessible for service registration and UI)

---

## Eureka Dashboard

Access the Eureka UI to see all registered services:

```
http://localhost:8761
```

The dashboard shows:
- All registered service instances
- Instance health status
- Service metadata (host, port, IP)
- Last heartbeat received

---

## Configuration

```yaml
server:
  port: 8761

spring:
  application:
    name: discovery-service

eureka:
  client:
    register-with-eureka: false   # Does not register itself
    fetch-registry: false          # Does not fetch others' registrations
  server:
    enable-self-preservation: false  # Disabled for development
```

---

## Service Registration

All other microservices register using:

```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true
```

---

## Startup Order

The Discovery Service **must start first** before any other service:

```
PostgreSQL + Redis + RabbitMQ (parallel)
          |
Discovery Service :8761   <-- must be healthy first
          |
Config Server :8888
          |
All other services
```

---

## Local Development

```bash
cd discovery-service
mvn spring-boot:run
```

Verify: Open `http://localhost:8761` - you should see the Eureka dashboard with no instances registered yet.

Health check: `GET http://localhost:8761/actuator/health`

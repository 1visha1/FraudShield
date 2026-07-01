# Config Server

> **Centralized Spring Cloud Config Server** - provides externalized configuration for all microservices from a single source.

---

## Overview

The **Config Server** serves configuration properties to all microservices at startup. It uses the **native** profile to serve configuration files from the local `config-repo/` directory. This eliminates the need for each service to manage its own complete configuration, enabling centralized management of shared settings.

---

## Responsibilities

- Serve configuration to all microservices via HTTP
- Provide a shared `application.yml` that applies to all services
- Allow service-specific overrides via `{service-name}.yml` files
- Register with Eureka so services can discover it dynamically

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Config | Spring Cloud Config Server |
| Discovery | Spring Cloud Netflix Eureka Client |

---

## Port

`8888`

---

## Configuration Source

Uses native profile - reads from local filesystem:

```
config-repo/
└── application.yml    # Shared properties for ALL services
```

Service-specific config files can be added as `{service-name}.yml` in `config-repo/`.

---

## Configuration

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

---

## How Services Connect

Each microservice bootstraps with:

```yaml
spring:
  config:
    import: optional:configserver:http://config-server:8888
```

---

## Startup Order

Config Server starts after Discovery Service:

```
Discovery Service :8761
         |
Config Server :8888
         |
All business services
```

---

## Local Development

```bash
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
```

Verify config is served:
```bash
curl http://localhost:8888/application/default
```

Health check: `GET http://localhost:8888/actuator/health`

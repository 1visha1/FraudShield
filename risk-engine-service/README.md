# Risk Engine Service

> **Calculates a preliminary risk score** for every transaction based on amount thresholds and device risk signals.

---

## Overview

The **Risk Engine Service** is the first async stage in the fraud detection pipeline. It consumes `transaction.created` events from RabbitMQ, retrieves the device risk score from Redis, computes a risk score and risk level, persists the assessment, and publishes a `risk.assessed` event to trigger the next stage.

---

## Responsibilities

- Consume `transaction.created` events from RabbitMQ
- Read device risk score from Redis cache (set by Device Auth Service)
- Calculate a risk score based on transaction amount thresholds plus device risk
- Classify the risk level as LOW, MEDIUM, HIGH, or CRITICAL
- Persist `RiskAssessment` to PostgreSQL
- Publish `risk.assessed` event to RabbitMQ

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Messaging | Spring AMQP / RabbitMQ |
| Cache | Spring Data Redis (read only) |
| Database | Spring Data JPA + PostgreSQL |
| Service Discovery | Spring Cloud Eureka Client |
| Utilities | Lombok |

---

## Port

`8083` (internal only)

---

## Folder Structure

```
risk-engine-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/securepay/risk/
    ├── RiskEngineServiceApplication.java
    ├── config/
    │   └── RabbitMQConfig.java       # Queues and exchange declarations
    ├── consumer/
    │   └── TransactionCreatedConsumer.java  # Listens: transaction.created
    ├── service/
    │   └── RiskAssessmentService.java       # Scoring logic
    ├── publisher/
    │   └── RiskAssessedPublisher.java
    ├── entity/
    │   └── RiskAssessment.java
    ├── repository/
    │   └── RiskAssessmentRepository.java
    └── event/
        ├── TransactionCreatedEvent.java
        └── RiskAssessedEvent.java
```

---

## RabbitMQ

### Consumed Events

| Queue | Routing Key | Description |
|---|---|---|
| `risk.transaction.created.q` | `transaction.created` | New transaction to assess |
| `risk.transaction.created.retry.q` | N/A | Retry queue TTL 5000ms |
| `risk.transaction.created.dlq` | N/A | Dead-letter queue for failures |

### Published Events

| Routing Key | Payload |
|---|---|
| `risk.assessed` | `{transactionId, customerId, amount, riskScore, riskLevel}` |

---

## Database

### Table: `risk_assessments`

| Column | Type | Description |
|---|---|---|
| `id` | UUID PK | Auto-generated UUID |
| `transaction_id` | UUID | Related transaction |
| `customer_id` | UUID | Customer identifier |
| `risk_score` | INT | Calculated risk score 0-100 |
| `risk_level` | VARCHAR | LOW, MEDIUM, HIGH, or CRITICAL |
| `assessed_at` | TIMESTAMP | When assessment was performed |

---

## Redis

| Key Pattern | Type | Operation | Description |
|---|---|---|---|
| `device:{customerId}` | String Integer | READ only | Device risk score set by Device Auth Service TTL 24h |

---

## Risk Scoring Logic

The risk score is calculated as follows:

| Condition | Points Added |
|---|---|
| Base score | 0 |
| amount greater than 50000 | +40 |
| amount greater than 100000 | +20 additional |
| Device risk score from Redis | +N from cache |
| Maximum possible score | 100 |

**Risk Level Classification:**

| Score Range | Risk Level |
|---|---|
| 0 to 29 | LOW |
| 30 to 69 | MEDIUM |
| 70 to 89 | HIGH |
| 90 to 100 | CRITICAL |

---

## Internal Request Flow

```
RabbitMQ delivers transaction.created to risk.transaction.created.q
          |
  TransactionCreatedConsumer.consume(event)
          |
  RiskAssessmentService.assess(event)
          |
  1. Redis GET device:{customerId} -> deviceRiskScore
          |
  2. Calculate: riskScore = amountScore + deviceRiskScore
          |
  3. Classify: riskLevel based on score range
          |
  4. INSERT INTO risk_assessments
          |
  5. PUBLISH risk.assessed -> securepay.exchange
```

---

## Configuration

```yaml
server:
  port: 8083

spring:
  application:
    name: risk-engine-service
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/securepay}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

---

## Resilience: DLQ Pattern

The `risk.transaction.created.q` queue is configured with:

```
Primary Queue (risk.transaction.created.q)
  -> on NACK -> Retry Queue (risk.transaction.created.retry.q) TTL 5000ms
  -> TTL expires -> back to Primary Queue
  -> after max retries -> DLQ (risk.transaction.created.dlq)
```

---

## Local Development

```bash
docker-compose up -d postgres redis rabbitmq
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd risk-engine-service && mvn spring-boot:run
```

Health check: `GET http://localhost:8083/actuator/health`

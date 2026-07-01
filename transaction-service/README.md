# Transaction Service

> **Manages the end-to-end transaction lifecycle** - from creation through fraud detection and final settlement.

---

## Overview

The **Transaction Service** is the primary entry point for all payment transactions. It accepts REST API requests from the API Gateway, persists transaction records, triggers the fraud detection pipeline via RabbitMQ, and finalizes transaction status based on downstream decisions.

---

## Responsibilities

- Accept authenticated transaction creation requests
- Call Device Auth Service synchronously to verify the device fingerprint
- Persist transactions with `PENDING` status to PostgreSQL
- Publish `transaction.created` event to RabbitMQ to trigger the fraud detection pipeline
- Consume `auth.challenge.completed` events to finalize transactions as `APPROVED`
- Consume `transaction.blocked` events to finalize transactions as `BLOCK`
- Publish `transaction.approved` event after successful approval

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| REST API | Spring Web MVC |
| Messaging | Spring AMQP / RabbitMQ |
| Database | Spring Data JPA + PostgreSQL |
| Service Discovery | Spring Cloud Eureka Client |
| Security | Spring Security OAuth2 Resource Server |
| Utilities | Lombok, MapStruct |

---

## Port

`8081` (internal only, accessed via API Gateway on port 8080)

---

## Folder Structure

```
transaction-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/securepay/transaction/
    ├── TransactionServiceApplication.java
    ├── config/
    │   └── RabbitMQConfig.java       # Queue and exchange declarations
    ├── controller/
    │   └── TransactionController.java # POST /api/v1/transactions
    ├── service/
    │   └── TransactionService.java    # Core business logic
    ├── consumer/
    │   ├── AuthChallengeCompletedConsumer.java  # Listens: auth.challenge.completed
    │   └── TransactionBlockedConsumer.java      # Listens: transaction.blocked
    ├── publisher/
    │   └── TransactionEventPublisher.java
    ├── entity/
    │   └── Transaction.java           # JPA entity - transactions table
    ├── repository/
    │   └── TransactionRepository.java
    ├── dto/
    │   ├── CreateTransactionRequest.java
    │   └── TransactionResponse.java
    ├── event/
    │   ├── TransactionCreatedEvent.java
    │   └── AuthChallengeCompletedEvent.java
    ├── mapper/
    │   └── TransactionMapper.java
    └── exception/
        └── GlobalExceptionHandler.java
```

---

## REST API

### Create Transaction

```http
POST /api/v1/transactions
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "customerId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "amount": 25000.00
}
```

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "amount": 25000.00,
  "status": "PENDING",
  "createdAt": "2026-06-30T14:00:00"
}
```

The response returns immediately with `PENDING` status. Fraud detection is asynchronous.

---

## RabbitMQ

### Published Events

| Routing Key | Trigger | Payload |
|---|---|---|
| `transaction.created` | After transaction saved | `{transactionId, customerId, amount, deviceInfo}` |
| `transaction.approved` | After receiving auth.challenge.completed | `{transactionId, customerId}` |

### Consumed Events

| Queue | Routing Key | Action |
|---|---|---|
| `transaction.challenge.completed.q` | `auth.challenge.completed` | UPDATE status=APPROVED |
| `transaction.blocked.q` | `transaction.blocked` | UPDATE status=BLOCK |

---

## Database

### Table: `transactions`

| Column | Type | Description |
|---|---|---|
| `id` | UUID PK | Auto-generated UUID |
| `customer_id` | UUID | Customer identifier |
| `amount` | DECIMAL | Transaction amount |
| `status` | VARCHAR | PENDING, APPROVED, or BLOCK |
| `created_at` | TIMESTAMP | Record creation time |

---

## Internal Request Flow

```
POST /api/v1/transactions
          |
  TransactionController.create()
          |
  TransactionService.createTransaction()
          |
  1. REST call -> Device Auth Service /api/v1/devices/verify
          |
  2. INSERT INTO transactions (status=PENDING)
          |
  3. PUBLISH transaction.created -> securepay.exchange
          |
  Return TransactionResponse {id, status:PENDING}
```

After creation, the service listens for finalization events:

```
auth.challenge.completed consumed
          |
  TransactionService.handleAuthCompleted()
          |
  UPDATE transactions SET status=APPROVED
          |
  PUBLISH transaction.approved
```

---

## Configuration

```yaml
server:
  port: 8081

spring:
  application:
    name: transaction-service
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/securepay}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

eureka:
  client:
    serviceUrl:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

---

## Error Handling

| HTTP Status | Scenario |
|---|---|
| 400 Bad Request | Invalid request body or missing required fields |
| 401 Unauthorized | Missing or invalid JWT |
| 503 Service Unavailable | Device Auth Service unreachable |
| 500 Internal Server Error | Database error or unexpected exception |

---

## Local Development

```bash
# Start infrastructure
docker-compose up -d postgres rabbitmq redis

# Start dependencies
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run

# Run transaction service
cd transaction-service
mvn spring-boot:run
```

Health check: `GET http://localhost:8081/actuator/health`

# Fraud Engine Service

> **Calculates the final fraud score and makes the authorization decision** - APPROVE, STEP_UP_AUTH, HIGH_RISK, or BLOCK.

---

## Overview

The **Fraud Engine Service** is the third and final async scoring stage. It consumes both `risk.assessed` and `rule.evaluated` events, stores partial data in a Redis-backed context cache, and when both scores are available for a transaction, computes a final `fraudScore` and makes a decision. The decision is published as a `fraud.detected` event to trigger Auth Orchestration.

---

## Responsibilities

- Consume `risk.assessed` events and cache the `riskScore` in Redis
- Consume `rule.evaluated` events, cache the `ruleScore`, and trigger the fraud decision
- Combine `riskScore + ruleScore` to calculate `fraudScore`
- Make a decision based on score thresholds
- Persist the fraud decision to PostgreSQL
- Publish `fraud.detected` event with the decision

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Messaging | Spring AMQP / RabbitMQ |
| Cache | Spring Data Redis (FraudContextCache + velocity tracking) |
| Database | Spring Data JPA + PostgreSQL |
| Service Discovery | Spring Cloud Eureka Client |
| Utilities | Lombok |

---

## Port

`8085` (internal only)

---

## Folder Structure

```
fraud-engine-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/securepay/fraud/
    ├── FraudEngineServiceApplication.java
    ├── config/
    │   └── RabbitMQConfig.java       # Queues and exchange declarations
    ├── consumer/
    │   ├── RiskConsumer.java          # Listens: risk.assessed
    │   └── RuleConsumer.java          # Listens: rule.evaluated
    ├── service/
    │   └── FraudProcessor.java        # Core scoring and decision logic
    ├── cache/
    │   └── FraudContextCache.java     # Redis-backed partial result store
    ├── entity/
    │   └── FraudDecision.java
    ├── repository/
    │   └── FraudDecisionRepository.java
    └── event/
        ├── RiskAssessedEvent.java
        ├── RuleEvaluatedEvent.java
        └── FraudDetectedEvent.java
```

---

## RabbitMQ

### Consumed Events

| Queue | Routing Key | Consumer | Action |
|---|---|---|---|
| `fraud.risk.assessed.q` | `risk.assessed` | RiskConsumer | Cache riskScore, call FraudProcessor |
| `fraud.rule.evaluated.q` | `rule.evaluated` | RuleConsumer | Cache ruleScore, call FraudProcessor |
| `fraud.rule.evaluated.retry.q` | N/A | N/A | Retry queue TTL 5000ms |
| `fraud.rule.evaluated.dlq` | N/A | N/A | Dead-letter queue |

### Published Events

| Routing Key | Payload |
|---|---|
| `fraud.detected` | `{transactionId, customerId, fraudScore, decision}` |

---

## Database

### Table: `fraud_decisions`

| Column | Type | Description |
|---|---|---|
| `id` | UUID PK | Auto-generated UUID |
| `transaction_id` | UUID | Related transaction |
| `customer_id` | UUID | Customer identifier |
| `fraud_score` | INT | Final combined fraud score |
| `decision` | VARCHAR | APPROVE, STEP_UP_AUTH, HIGH_RISK, or BLOCK |
| `created_at` | TIMESTAMP | Decision timestamp |

---

## Redis

| Key Pattern | Type | TTL | Purpose |
|---|---|---|---|
| `fraud:context:{transactionId}` | Hash | 10 minutes | Stores partial scores awaiting both risk and rule results |
| `velocity:user:{customerId}` | Counter INCR | 1 hour | Transaction velocity tracking |

### FraudContextCache Design

The Fraud Engine receives two separate events per transaction (risk.assessed and rule.evaluated). The FraudContextCache stores partial results until both are received:

```
RiskConsumer receives risk.assessed:
  cache.saveRisk(txnId, customerId, riskScore)
  processor.process(txnId)  // checks if ruleScore also present

RuleConsumer receives rule.evaluated:
  cache.saveRule(txnId, customerId, ruleScore, triggeredRules)
  processor.process(txnId)  // checks if riskScore also present

FraudProcessor.process():
  context = cache.get(txnId)
  if context has both riskScore and ruleScore:
    fraudScore = context.riskScore + context.ruleScore
    decision = decideBasedOnScore(fraudScore)
    save to DB, publish fraud.detected
    cache.evict(txnId)
```

---

## Fraud Decision Thresholds

| fraudScore | Decision | Description |
|---|---|---|
| 0 to 30 | `APPROVE` | Low risk - auto approve |
| 31 to 70 | `STEP_UP_AUTH` | Medium risk - SMS OTP required |
| 71 to 100 | `HIGH_RISK` | High risk - SMS and Email OTP required |
| Greater than 100 | `BLOCK` | Critical risk - block immediately |

---

## Internal Request Flow

```
risk.assessed AND rule.evaluated both consumed for same transactionId
          |
FraudProcessor.process(transactionId)
          |
1. FraudContextCache.get(transactionId)
   -> {riskScore: 40, ruleScore: 35}
          |
2. fraudScore = 40 + 35 = 75
          |
3. decision = HIGH_RISK (71-100 range)
          |
4. INSERT INTO fraud_decisions {transactionId, fraudScore=75, decision=HIGH_RISK}
          |
5. PUBLISH fraud.detected -> securepay.exchange
   payload: {transactionId, customerId, fraudScore=75, decision=HIGH_RISK}
          |
6. cache.evict(transactionId)
```

---

## Configuration

```yaml
server:
  port: 8085

spring:
  application:
    name: fraud-engine-service
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/securepay}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

---

## Error Handling

| Scenario | Behavior |
|---|---|
| Only one of the two events received | Context cache holds partial state TTL 10m |
| Redis unavailable | NACK, message retried |
| Database write failure | NACK, message retried via DLQ pattern |

---

## Local Development

```bash
docker-compose up -d postgres redis rabbitmq
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd fraud-engine-service && mvn spring-boot:run
```

Health check: `GET http://localhost:8085/actuator/health`

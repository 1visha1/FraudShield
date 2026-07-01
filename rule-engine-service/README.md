# Rule Engine Service

> **Evaluates dynamic fraud rules using MVEL expressions** against transaction data to produce a rule-based risk score.

---

## Overview

The **Rule Engine Service** is the second async stage in the fraud detection pipeline. It consumes `risk.assessed` events, loads all enabled fraud rules from PostgreSQL, evaluates each rule's MVEL expression against the transaction context (amount, riskScore), aggregates a `ruleScore`, and publishes a `rule.evaluated` event.

Rules are managed dynamically via a CRUD REST API - no code deployment needed to add or modify fraud rules.

---

## Responsibilities

- Consume `risk.assessed` events from RabbitMQ
- Load all enabled fraud rules from PostgreSQL
- Evaluate each rule's MVEL expression against `{amount, riskScore}` context
- Aggregate a combined `ruleScore` from all matching rules
- Publish `rule.evaluated` event to RabbitMQ
- Provide a REST API for CRUD management of fraud rules

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Messaging | Spring AMQP / RabbitMQ |
| Database | Spring Data JPA + PostgreSQL |
| Rule Engine | MVEL2 2.x - dynamic expression evaluation |
| Service Discovery | Spring Cloud Eureka Client |
| Utilities | Lombok |

---

## Port

`8084` (internal only for messaging; REST API accessible via API Gateway)

---

## Folder Structure

```
rule-engine-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/securepay/risk/
    ├── RuleEngineServiceApplication.java
    ├── config/
    │   └── RabbitMQConfig.java       # Queues and exchange declarations
    ├── controller/
    │   └── RuleController.java       # CRUD /api/v1/rules
    ├── service/
    │   ├── RuleEvaluationService.java # MVEL evaluation logic
    │   └── RuleManagementService.java # CRUD operations
    ├── consumer/
    │   └── RiskAssessedConsumer.java  # Listens: risk.assessed
    ├── entity/
    │   └── FraudRule.java             # JPA entity
    ├── repository/
    │   └── FraudRuleRepository.java
    ├── dto/
    │   ├── CreateRuleRequest.java
    │   └── UpdateRuleRequest.java
    └── event/
        ├── RiskAssessedEvent.java
        └── RuleEvaluatedEvent.java
```

---

## REST API

### Create Rule

```http
POST /api/v1/rules
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "ruleName": "High Amount Rule",
  "ruleVersion": 1,
  "ruleExpression": "amount > 50000",
  "riskScore": 40,
  "enabled": true
}
```

### Get All Rules

```http
GET /api/v1/rules
Authorization: Bearer {JWT}
```

### Update Rule

```http
PUT /api/v1/rules/{id}
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "ruleName": "High Amount Rule v2",
  "ruleExpression": "amount > 100000 && riskScore > 50",
  "riskScore": 60,
  "enabled": true
}
```

### Enable or Disable Rule

```http
PATCH /api/v1/rules/{id}/enable
PATCH /api/v1/rules/{id}/disable
Authorization: Bearer {JWT}
```

### Delete Rule

```http
DELETE /api/v1/rules/{id}
Authorization: Bearer {JWT}
```

---

## MVEL Expression Context

When evaluating rules, the following variables are available in the MVEL expression context:

| Variable | Type | Source |
|---|---|---|
| `amount` | Double | Transaction amount from event |
| `riskScore` | Integer | Risk score from Risk Engine event |

### Example MVEL Expressions

```java
// Simple amount threshold
"amount > 50000"

// Combined amount and risk
"amount > 100000 && riskScore > 50"

// Multiple conditions
"amount > 25000 || riskScore > 70"
```

---

## RabbitMQ

### Consumed Events

| Queue | Routing Key | Description |
|---|---|---|
| `rule.risk.assessed.q` | `risk.assessed` | Risk assessment result to evaluate rules against |
| `rule.risk.assessed.retry.q` | N/A | Retry queue TTL 5000ms |
| `rule.risk.assessed.dlq` | N/A | Dead-letter queue |

### Published Events

| Routing Key | Payload |
|---|---|
| `rule.evaluated` | `{transactionId, customerId, amount, riskScore, ruleScore}` |

---

## Database

### Table: `fraud_rules` / `rules`

| Column | Type | Description |
|---|---|---|
| `rule_id` | UUID PK | Auto-generated UUID |
| `rule_name` | VARCHAR | Human-readable rule name |
| `rule_version` | INT | Version number for tracking |
| `rule_expression` | TEXT | MVEL expression string |
| `risk_score` | INT | Score to add if rule matches |
| `enabled` | BOOLEAN | Whether rule is active |
| `created_at` | TIMESTAMP | Creation timestamp |

---

## Rule Evaluation Logic

```
RiskAssessedEvent received
        |
RuleEvaluationService.evaluate(event)
        |
1. SELECT * FROM fraud_rules WHERE enabled = true
        |
2. For each rule:
   Map context = {amount: event.amount, riskScore: event.riskScore}
   Boolean matched = (Boolean) MVEL.eval(rule.expression, context)
   if matched: ruleScore += rule.riskScore
        |
3. PUBLISH rule.evaluated {transactionId, ruleScore, ...}
```

---

## Configuration

```yaml
server:
  port: 8084

spring:
  application:
    name: rule-engine-service
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/securepay}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

---

## Error Handling

| Scenario | Behavior |
|---|---|
| MVEL expression syntax error | Rule skipped, exception logged |
| Database unavailable | NACK sent, message retried via retry queue |
| No enabled rules found | ruleScore = 0, event published normally |

---

## Local Development

```bash
docker-compose up -d postgres rabbitmq
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd rule-engine-service && mvn spring-boot:run
```

Health check: `GET http://localhost:8084/actuator/health`

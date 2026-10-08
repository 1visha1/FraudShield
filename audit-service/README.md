# Audit Service

> **Provides a centralized, immutable audit trail** for every business event across the entire platform, stored in Elasticsearch for search and analytics.

---

## Overview

The **Audit Service** is a cross-cutting concern that captures every event published to the RabbitMQ exchange using a wildcard binding (`#`). It uses a two-phase approach: events are first written to a **Redis Stream** for buffering, then consumed from the stream and indexed to **Elasticsearch** for long-term storage and querying via Kibana.

---

## Responsibilities

- Consume ALL events from RabbitMQ via wildcard binding
- Write each event to a Redis Stream (`audit-stream`) for durable buffering
- Run a `StreamMessageListenerContainer` consumer group to read from the stream
- Index each event as an `AuditEvent` document in Elasticsearch
- Provide a queryable audit trail for compliance and operational analysis

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| Messaging | Spring AMQP / RabbitMQ |
| Buffer | Spring Data Redis - Redis Streams |
| Storage | Spring Data Elasticsearch |
| Service Discovery | Spring Cloud Eureka Client |
| Utilities | Lombok |

---

## Port

`8088` (internal only)

---

## Folder Structure

```
audit-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/fraudshield/audit/
    ├── AuditServiceApplication.java
    ├── config/
    │   ├── RabbitMQConfig.java         # Wildcard audit.q binding
    │   ├── RedisConfig.java            # Stream setup + consumer group
    │   └── ElasticsearchConfig.java    # ES client configuration
    ├── service/
    │   ├── AuditService.java            # RabbitMQ consumer -> writes to Redis Stream
    │   └── RedisStreamConsumer.java     # Reads stream -> indexes to Elasticsearch
    ├── entity/
    │   └── AuditEvent.java              # Elasticsearch document model
    └── repository/
        └── AuditEventRepository.java    # Spring Data ES repository
```

---

## RabbitMQ

### Consumed Events

| Queue | Routing Key | Description |
|---|---|---|
| `audit.q` | `#` (wildcard) | ALL events from fraudshield.exchange |

The `#` binding captures every single message published to `fraudshield.exchange`, regardless of routing key.

### No Published Events

The Audit Service is a pure consumer - it does not publish any events.

---

## Redis Stream Architecture

```
RabbitMQ audit.q (all events)
         |
AuditService.auditEvent(message)
         |
Redis XADD audit-stream {eventType, payload, timestamp}
         |
StreamMessageListenerContainer polls every 1 second
Consumer Group: audit-group
Consumer: audit-consumer
         |
RedisStreamConsumer.onMessage(message)
         |
AuditEventRepository.save(auditEvent)
         |
Elasticsearch Index: audit-events
```

### Redis Stream Configuration

```
Stream Name: audit-stream
Consumer Group: audit-group
Consumer Name: audit-consumer
Poll Timeout: 1 second
Read Offset: lastConsumed (XREADGROUP)
```

---

## Elasticsearch

### Index: `audit-events`

| Field | Type | Description |
|---|---|---|
| `id` | String | Auto-generated document ID |
| `event_type` | Keyword | Routing key e.g. transaction.created |
| `service_name` | Keyword | Source service name |
| `transaction_id` | Keyword | Related transaction UUID |
| `payload` | Text | Raw event JSON payload |
| `timestamp` | Date | Event timestamp |

### Querying Audit Events

```bash
# All audit events
curl http://localhost:9200/audit-events/_search?pretty

# Events for a specific transaction
curl -X GET "http://localhost:9200/audit-events/_search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match": {
        "transactionId": "550e8400-e29b-41d4-a716-446655440000"
      }
    }
  }'

# Events by type
curl "http://localhost:9200/audit-events/_search?q=eventType:fraud.detected&pretty"
```

---

## Configuration

```yaml
server:
  port: 8088

spring:
  application:
    name: audit-service
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  elasticsearch:
    host: ${ELASTICSEARCH_HOST:localhost}
    port: ${ELASTICSEARCH_PORT:9200}

eureka:
  client:
    serviceUrl:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}
```

---

## Why Two-Phase Storage (Redis Stream -> Elasticsearch)

| Reason | Explanation |
|---|---|
| **Decoupling** | Writing to Redis Stream is fast < 1ms while Elasticsearch indexing is slower ~10ms |
| **Backpressure** | Redis Stream buffers events if Elasticsearch is temporarily unavailable |
| **Durability** | Messages are acknowledged from Redis Stream only after successful ES indexing |
| **Reliability** | If the Audit Service crashes mid-stream, Redis Stream consumer offset ensures no events are lost |

---

## Kibana Dashboards (Conceptual)

| Dashboard | Metrics |
|---|---|
| **Transaction Overview** | Total transactions, status distribution, amounts |
| **Fraud Analysis** | Fraud score distribution, decision breakdown, top triggered rules |
| **Authentication** | OTP success/failure rates, step-up auth frequency |
| **System Health** | Event throughput per service, DLQ message counts |

---

## Error Handling

| Scenario | Behavior |
|---|---|
| Elasticsearch unavailable | Events buffer in Redis Stream until ES is restored |
| Redis Stream full | RabbitMQ messages wait in audit.q |
| Consumer crashes | Redis Stream consumer offset preserves position - no events lost |

---

## Local Development

```bash
docker-compose up -d redis rabbitmq elasticsearch kibana
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd audit-service && mvn spring-boot:run
```

To verify audit events are being captured:
```bash
# Check Redis Stream length
redis-cli XLEN audit-stream

# Check Elasticsearch index
curl http://localhost:9200/audit-events/_count

# View in Kibana
# Go to http://localhost:5601 -> Discover -> audit-events index
```

Health check: `GET http://localhost:8088/actuator/health`

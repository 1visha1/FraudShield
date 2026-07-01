# SecurePay360 Architecture
                                      ┌──────────────────────┐
                                      │      Client App      │
                                      │  Web / Mobile / API  │
                                      └──────────┬───────────┘
                                                 │
                                                 ▼
                                      ┌──────────────────────┐
                                      │     API Gateway      │
                                      │  Spring Cloud Gateway│
                                      └───────┬───────┬──────┘
                                              │       │
                 ┌────────────────────────────┘       └────────────────────────────┐
                 │                                                         │
                 ▼                                                         ▼
      ┌─────────────────────┐                               ┌────────────────────────┐
      │ Authorization Layer │                               │ Infrastructure Layer   │
      ├─────────────────────┤                               ├────────────────────────┤
      │ Authorization Server│                               │ Eureka Discovery       │
      │ OAuth2 + JWT        │                               │ Config Server          │
      └──────────┬──────────┘                               └────────────────────────┘
                 │
                 ▼
          PostgreSQL (Users)

────────────────────────────────────────────────────────────────────────────────────────────

                     BUSINESS SERVICES

                 │
                 ▼
      ┌──────────────────────────┐
      │ Device Authentication    │
      │ Fingerprint Validation   │
      └──────────┬───────────────┘
                 │
                 ▼
      ┌──────────────────────────┐
      │ Transaction Service      │
      │ Create Transaction       │
      └──────────┬───────────────┘
                 │
                 ▼
         RabbitMQ Event Bus
                 │
      ┌──────────┼──────────────┬──────────────┐
      ▼          ▼              ▼              ▼
┌─────────┐ ┌─────────┐ ┌────────────┐ ┌───────────┐
│ Risk    │ │ Rule    │ │ Fraud      │ │ Audit     │
│ Engine  │ │ Engine  │ │ Engine     │ │ Service   │
└────┬────┘ └────┬────┘ └──────┬─────┘ └─────┬─────┘
│           │             │             │
└───────────┴─────────────┘             │
│                         │
▼                         ▼
┌──────────────────────────┐     Elasticsearch
│ Authentication           │           │
│ Orchestrator             │           ▼
└──────────┬───────────────┘       Kibana
│
▼
┌──────────────────────────┐
│ Notification Service     │
│ SMS / Email OTP          │
└──────────┬───────────────┘
│
▼
┌──────────────────────────┐
│ OTP Verification         │
└──────────┬───────────────┘
│
▼
Transaction Approved / Blocked

────────────────────────────────────────────────────────────────────────────────────────────

Shared Infrastructure

• PostgreSQL
• Redis
• RabbitMQ
• Elasticsearch
• Prometheus
• Grafana
• Zipkin


────────────────────────────────────────────────────────────────────────────────────────────
────────────────────────────────────────────────────────────────────────────────────────────
**PlantUML-style text sequence diagram**

```text
+--------+     +-------------+     +----------------+     +----------------+     +------------+
| Client |     | API Gateway |     | Auth Server    |     | Device Service |     | PostgreSQL |
+--------+     +-------------+     +----------------+     +----------------+     +------------+
     |                 |                    |                      |                     |
1. Login Request       |                    |                      |                     |
---------------------->|                    |                      |                     |
     |                 | Authenticate User  |                      |                     |
     |                 |------------------->|                      |                     |
     |                 |                    | Validate User        |                     |
     |                 |                    |--------------------->| (DB Lookup)         |
     |                 |                    |<---------------------|                     |
     |                 |<-------------------| JWT Token            |                     |
<----------------------|                    |                      |                     |
2. Login Successful    |                    |                      |                     |
     |                 |                    |                      |                     |
3. Verify Device       |                    |                      |                     |
---------------------->|                    |                      |                     |
     |                 |------------------------------->| Check Device              |
     |                 |                                |-------------------------->|
     |                 |                                |<--------------------------|
     |                 |<-------------------------------| Device Verified           |
<----------------------|                                |                          |
```

---

### Transaction & Fraud Processing

```text
+--------+    +-------------+    +------------------+    +-----------+    +-------------+
| Client |    | API Gateway |    | Transaction Svc  |    | RabbitMQ  |    | Risk Engine |
+--------+    +-------------+    +------------------+    +-----------+    +-------------+
     |                 |                    |                   |                 |
1. Create Transaction  |                    |                   |                 |
---------------------->|                    |                   |                 |
     |                 |------------------->|                   |                 |
     |                 |                    | Save Transaction  |                 |
     |                 |                    |-------------------------------> DB
     |                 |                    | Publish Event     |                 |
     |                 |                    |------------------>|                 |
     |                 |                    |                   | Deliver Event   |
     |                 |                    |                   |---------------->|
     |                 |                    |                   |                 |
```

---

### Fraud Decision Flow

```text
+-------------+    +-------------+    +--------------------+    +--------------------+
| Rule Engine |    | Fraud Engine|    | Auth Orchestrator  |    | Notification Svc   |
+-------------+    +-------------+    +--------------------+    +--------------------+
      |                   |                     |                        |
1. Rule Evaluated         |                     |                        |
------------------------->|                     |                        |
      |                   | Fraud Score        |                        |
      |                   |------------------->|                        |
      |                   |                    | Decision               |
      |                   |                    |                        |
      |                   |                    |---- APPROVE ---------->|
      |                   |                    |                        |
      |                   |                    |---- OTP Required ----->|
      |                   |                    |                        |
      |                   |                    |<----- OTP Verified ----|
      |                   |                    |                        |
      |                   |                    |---- BLOCK ------------>|
```

---

### Complete Business Flow

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Authorization Server
   │
   ▼
JWT Generated
   │
   ▼
Device Authentication
   │
   ▼
Transaction Service
   │
   ▼
RabbitMQ
   │
   ├────────────► Risk Engine
   │                  │
   │                  ▼
   │            Rule Engine
   │                  │
   │                  ▼
   │            Fraud Engine
   │                  │
   │                  ▼
   │        Authentication Orchestrator
   │          ┌───────────┴───────────┐
   │          │                       │
   │      APPROVE                OTP Required
   │                                  │
   │                                  ▼
   │                        Notification Service
   │                                  │
   │                           OTP Verification
   │                                  │
   └──────────────────────────────────┘
                 │
                 ▼
       Transaction Approved / Blocked
                 │
                 ▼
           Audit Service
```
## 📚 Documentation

### Core Infrastructure

| Component | Documentation |
|-----------|---------------|
| API Gateway | [README](./api-gateway/README.md) |
| Authorization Server | [README](./authorization-server/README.md) |
| Discovery Service | [README](./discovery-service/README.md) |
| Config Server | [README](./config-server/README.md) |

### Business Services

| Service | Documentation |
|---------|---------------|
| Auth Orchestrator Service | [README](./auth-orchestrator-service/README.md) |
| Device Authentication Service | [README](./device-auth-service/README.md) |
| Risk Engine Service | [README](./risk-engine-service/README.md) |
| Rule Engine Service | [README](./rule-engine-service/README.md) |
| Fraud Engine Service | [README](./fraud-engine-service/README.md) |
| Notification Service | [README](./notification-service/README.md) |
| Audit Service | [README](./audit-service/README.md) |

### Repository Documentation

- [Project Context](./PROJECT_CONTEXT.md)
- [Project Status Report](./PROJECT_STATUS_REPORT.md)
- [Code of Conduct](./CODE_OF_CONDUCT.md)
- [Security Policy](./SECURITY.md)

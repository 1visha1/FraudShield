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


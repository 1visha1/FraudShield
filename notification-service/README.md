# Notification Service

> **Generates and delivers OTPs for multi-factor authentication challenges** - bridging the Auth Orchestrator and the customer.

---

## Overview

The **Notification Service** handles the delivery of one-time passwords (OTPs) during step-up authentication flows. It consumes `auth.challenge.created` events, generates a 6-digit OTP, stores it in Redis with a 5-minute TTL, and simulates delivery via SMS or Email (logged to console). It also exposes a REST endpoint for OTP verification.

---

## Responsibilities

- Consume `auth.challenge.created` events from RabbitMQ
- Generate a secure 6-digit OTP
- Store OTP in Redis with a 5-minute TTL
- Simulate SMS/Email delivery (log to console - production would use Twilio or AWS SNS)
- Publish `otp.generated` event to RabbitMQ
- Expose `POST /api/v1/notifications/otp/verify` for OTP verification
- Publish `auth.challenge.completed.notification` after verification (success or failure)

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3 |
| REST API | Spring Web MVC |
| Messaging | Spring AMQP / RabbitMQ |
| Cache | Spring Data Redis (OTP storage) |
| Service Discovery | Spring Cloud Eureka Client |
| Utilities | Lombok |

---

## Port

`8087` (accessible via API Gateway for OTP verification)

---

## Folder Structure

```
notification-service/
├── Dockerfile
├── pom.xml
└── src/main/java/com/securepay/notification/
    ├── NotificationServiceApplication.java
    ├── config/
    │   └── RabbitMQConfig.java         # Queue declarations with DLQ pattern
    ├── controller/
    │   └── OtpController.java          # POST /api/v1/notifications/otp/verify
    ├── service/
    │   ├── OtpCacheService.java         # Redis get/set/delete for OTPs
    │   ├── OtpGenerator.java            # 6-digit OTP generation
    │   └── NotificationService.java     # SMS and Email simulation
    ├── consumer/
    │   └── AuthChallengeConsumer.java   # Listens: auth.challenge.created
    ├── event/
    │   ├── AuthChallengeCreatedEvent.java
    │   ├── AuthChallengeCompletedEvent.java
    │   └── OtpVerifiedEvent.java
    └── dto/
        ├── OtpVerificationRequest.java
        └── OtpVerificationResponse.java
```

---

## REST API

### Verify OTP

```http
POST /api/v1/notifications/otp/verify
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "otp": "482931"
}
```

**Response 200 OK - Valid OTP:**
```json
{
  "success": true,
  "message": "OTP verified successfully.",
  "data": {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "OTP_VERIFIED"
  }
}
```

**Response 410 Gone - Expired OTP:**
```json
{
  "success": false,
  "message": "OTP has expired.",
  "data": {
    "transactionId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "OTP_EXPIRED"
  }
}
```

**Response 400 Bad Request - Invalid OTP:**
```json
{
  "success": false,
  "message": "Invalid OTP.",
  "data": {
    "status": "OTP_INVALID"
  }
}
```

---

## RabbitMQ

### Consumed Events

| Queue | Routing Key | Description |
|---|---|---|
| `notification.auth.challenge.q` | `auth.challenge.created` | Trigger OTP generation and delivery |
| `notification.auth.challenge.retry.q` | N/A | Retry queue TTL 5000ms |
| `notification.auth.challenge.dlq` | N/A | Dead-letter queue |

### Published Events

| Routing Key | Trigger | Payload |
|---|---|---|
| `otp.generated` | After OTP stored in Redis | `{transactionId, customerId, authType}` |
| `auth.challenge.completed.notification` | After OTP verify REST call | `{transactionId, status: COMPLETED or FAILED}` |

---

## Redis

| Key Pattern | Type | TTL | Purpose |
|---|---|---|---|
| `otp:{transactionId}` | String | 5 minutes | Active OTP value for verification |

OTP is deleted from Redis immediately upon successful verification or after TTL expiry.

---

## OTP Generation and Delivery Flow

```
auth.challenge.created event consumed
          |
AuthChallengeConsumer.consume(event)
          |
1. otp = OtpGenerator.generate() -> random 6 digits e.g. "482931"
          |
2. Redis SET otp:{transactionId} = "482931" TTL 5 minutes
          |
3. LOG: "[SIMULATED SMS] OTP=482931 for transaction=txnId customer=customerId"
   (Production: call Twilio or AWS SNS here)
          |
4. PUBLISH otp.generated -> securepay.exchange
```

---

## OTP Verification Flow

```
POST /api/v1/notifications/otp/verify {transactionId, otp}
          |
OtpController.verify(request)
          |
1. stored = OtpCacheService.get(transactionId)  -> Redis GET otp:txnId
          |
if stored == null:
  PUBLISH auth.challenge.completed.notification {status=FAILED}
  return 410 GONE {status: OTP_EXPIRED}

if !stored.equals(otp):
  PUBLISH auth.challenge.completed.notification {status=FAILED}
  return 400 BAD_REQUEST {status: OTP_INVALID}

if stored.equals(otp):
  OtpCacheService.delete(transactionId)  -> Redis DEL otp:txnId
  PUBLISH auth.challenge.completed.notification {status=COMPLETED}
  return 200 OK {status: OTP_VERIFIED}
```

---

## Configuration

```yaml
server:
  port: 8087

spring:
  application:
    name: notification-service
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/securepay}
```

---

## Mock vs Production Delivery

| Mode | Implementation |
|---|---|
| Current (Mock) | OTP printed to application logs |
| Production SMS | Replace with Twilio SMS API or AWS SNS |
| Production Email | Replace with AWS SES or SendGrid |

The `NotificationService` is designed with an interface/strategy pattern to easily swap the delivery implementation without changing business logic.

---

## Error Handling

| HTTP Status | Scenario |
|---|---|
| 200 OK | OTP valid and verified |
| 400 Bad Request | OTP submitted but incorrect |
| 410 Gone | OTP expired (not in Redis) |
| 500 Internal Server Error | Redis connection failure |

---

## Local Development

```bash
docker-compose up -d redis rabbitmq
cd discovery-service && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

To test OTP flow:
1. Create a transaction via `POST /api/v1/transactions`
2. Check application logs for simulated OTP: `[SIMULATED SMS] OTP=xxxxxx`
3. Call `POST /api/v1/notifications/otp/verify` with the OTP

Health check: `GET http://localhost:8087/actuator/health`

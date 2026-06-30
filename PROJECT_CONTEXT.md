# SecurePay 360 - Project Context

## 1. Current Architecture

The project follows a microservices architecture with the following services:

- `api-gateway`
- `authorization-server`
- `transaction-service`
- `device-auth-service`
- `discovery-server`

The services are orchestrated using Docker Compose and communicate via REST APIs and RabbitMQ messaging.

## 2. Implemented Services

- **`api-gateway`:** Provides routing and basic JWT authentication.
- **`authorization-server`:** Issues JWTs via OAuth2, but uses an insecure in-memory client repository.
- **`transaction-service`:** A stub implementation that creates a "PENDING" transaction and publishes an event.
- **`device-auth-service`:** A stub implementation that performs a simplistic device check.
- **`discovery-server`:** A functional Eureka server for service discovery.

## 3. Pending Services

- `risk-engine-service`
- `rule-engine-service`
- `fraud-engine-service`
- `auth-orchestrator-service`
- `notification-service`
- `audit-service`
- `config-server`

## 4. Feature Completion Matrix

| Feature | Status | Completion |
|---|---|---|
| Device Verification | Missing | 15% |
| JWT Authentication | Partial | 40% |
| OAuth2 Authorization | Partial | 30% |
| Transaction Processing | Missing | 20% |
| RabbitMQ Event Processing | Partial | 30% |
| Retry/DLQ | Missing | 0% |

## 5. Risks

- **Security:** The `authorization-server`'s in-memory client repository is a critical vulnerability.
- **Resilience:** The lack of retry and dead-letter queues in RabbitMQ configurations makes the system fragile.
- **Incomplete Features:** The core business logic is largely unimplemented.

## 6. Technical Debt

- Widespread use of stub implementations.
- Lack of tests.
- Missing documentation.

## 7. Next Milestones

- **Sprint 1:**
    - Secure the `authorization-server`.
    - Implement RabbitMQ resiliency patterns.
    - Begin implementation of the `risk-engine-service`.
- **Sprint 2:**
    - Complete the `transaction-service` and `device-auth-service`.
    - Add comprehensive testing.
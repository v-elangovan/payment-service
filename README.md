# Payment Service

A Spring Boot REST API that simulates payment processing using a mock payment gateway. The project demonstrates a layered architecture, REST communication, asynchronous callback simulation, validation, exception handling, logging, and unit testing.

---

# Technology Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- Lombok
- JUnit 5
- Mockito
- SLF4J Logging
- RestTemplate

---

# Project Structure

```
src/main/java
│
├── configuration
├── controller
├── dto
├── entity
├── exception
├── repository
├── service
│     └── impl
└── PaymentServiceApplication
```

---

# Features

- Create Payment API
- Get Payment by ID
- Get All Payments
- Get Payments by Status
- Mock Payment Gateway API
- Callback API
- Duplicate Callback Handling
- Global Exception Handling
- Bean Validation
- Logging using SLF4J
- JPA Auditing using @PrePersist and @PreUpdate
- H2 In-Memory Database
- Unit Testing using JUnit and Mockito

---

# Payment Flow

```
Client
   │
   ▼
POST /payments
(Thread-1)
PaymentController
      │
      ▼
PaymentService
      │
      ▼
GatewayClient
      │
      ▼
RestTemplate
      │
      ▼
POST /mock-gateway/pay

──────────────────────────────────────────

(Thread-2)

MockGatewayController
      │
      ▼
MockGatewayService
      │
      ├── Validate Currency
      ├── Check Amount
      ├── SUCCESS / FAILED
      │
      └── Start Background Thread
              │
              ▼
        POST /callbacks/payment

──────────────────────────────────────────

(Thread-3)

CallbackController
      │
      ▼
PaymentService.processCallback()
      │
      ▼
Update Payment Status

──────────────────────────────────────────

Thread-1 receives gateway response
      │
      ▼
Return PaymentResponse
```

---

# Business Rules

## Currency

Only **INR** payments are supported.

Any other currency will fail.

---

## Amount

| Amount | Status |
|---------|----------|
| <= 5000 | SUCCESS |
| > 5000 | FAILED |

---

## Callback

The callback updates payment status asynchronously.

Duplicate callbacks are ignored once payment status is no longer **PENDING**.

---

# Setup


## Build

```bash
mvn clean install
```

---

## Run

```bash
mvn spring-boot:run
```

or

Run

```
PaymentServiceApplication.java
```

from your IDE.

---

# H2 Database

Open

```
http://localhost:8080/h2-console
```

Configuration

```
Driver Class :
org.h2.Driver

JDBC URL :
jdbc:h2:mem:paymentdb

Username :
elangovan

Password :
elangovan
```

---

# API Endpoints

## 1. Create Payment

POST

```
/payments
```

Request

```json
{
  "amount": 1000,
  "currency": "INR"
}
```

Response

```json
{
  "paymentId": "6b95d7...",
  "status": "PENDING"
}
```

---

## 2. Get Payment

GET

```
/payments/{paymentId}
```

Example

```
GET /payments/6b95d7...
```

Response

```json
{
  "paymentId":"6b95d7...",
  "amount":1000,
  "currency":"INR",
  "status":"SUCCESS",
  "transactionId":"TXN123",
  "failureReason":null
}
```

---

## 3. Get All Payments

GET

```
/payments
```

Response

```json
[
   {
      "paymentId":"...",
      "amount":1000,
      "currency":"INR",
      "status":"SUCCESS"
   },
   {
      "paymentId":"...",
      "amount":7000,
      "currency":"INR",
      "status":"FAILED"
   }
]
```

---

## 4. Get Payments By Status

GET

```
/payments/status/{status}
```

Example

```
GET /payments/status/SUCCESS
```

Response

```json
[
   {
      "paymentId":"...",
      "amount":1000,
      "currency":"INR",
      "status":"SUCCESS"
   }
]
```

---

## 5. Mock Gateway

POST

```
/mock-gateway/pay
```

Request

```json
{
    "paymentId":"PAY123",
    "amount":1000,
    "currency":"INR"
}
```

This API simulates an external payment gateway.

---

## 6. Callback API

POST

```
/callbacks/payment
```

Request

```json
{
   "paymentId":"PAY123",
   "status":"SUCCESS",
   "transactionId":"TXN123",
   "failureReason":null
}
```

Normally called internally by the Mock Gateway.

---

# Validation

Payment Request

- Amount is required
- Amount must be greater than zero
- Currency is required
- Only INR is supported

Callback Request

- Payment Id is required
- Status is required

---

# Exception Handling

Global exception handling is implemented using **@RestControllerAdvice**.

Handled Exceptions

- PaymentNotFoundException
- Validation Errors
- Gateway Communication Errors
- Internal Server Errors

Example

```json
{
    "timestamp":"2026-07-30T17:10:45",
    "status":404,
    "error":"Not Found",
    "message":"Payment not found with id : PAY123",
    "path":"/payments/PAY123"
}
```

---

# Logging

Application logs include

- Payment creation
- Gateway request
- Gateway response
- Callback processing
- Validation failures
- Exceptions
- Payment status updates

---

# Testing

Unit tests are implemented using

- JUnit 5
- Mockito

Covered Components

- PaymentServiceImpl
- GatewayClientImpl
- MockGatewayServiceImpl


Run tests

```bash
mvn test
```

---

# Assumptions

- Authentication is not implemented.
- Mock Gateway simulates an external payment provider.
- Callback is asynchronous using a background thread.
- Only INR currency is supported.
- Duplicate callbacks are ignored.
- H2 in-memory database is sufficient for this assessment.

---

# Production Improvements

If this service were deployed to production, the following improvements would be recommended:

- Spring WebClient instead of RestTemplate
- Async messaging using Kafka/RabbitMQ
- PostgreSQL or MySQL
- JWT Authentication
- HTTPS
- Callback Signature Verification
- Retry Mechanism
- Resilience4j Circuit Breaker
- Distributed Tracing
- Monitoring using Prometheus and Grafana
- API Rate Limiting
- Idempotency Keys
- Docker and Kubernetes deployment
- OpenAPI / Swagger documentation

---

# Questions for Production

1. Which payment gateway will be integrated?
2. What callback retry policy is expected?
3. Should payments support multiple currencies?
4. How should duplicate payment requests be handled?
5. Is callback signature verification required?
6. What are the transaction throughput requirements?
7. What audit requirements exist?
8. What SLA is expected for payment processing?

---

# AI Tool Usage

AI Tool Used

- ChatGPT

Where AI helped

- Project structure
- Layered architecture
- Spring Boot best practices
- REST API design
- Logging recommendations
- Unit testing guidance

Manual work performed

- Integrated generated code into the project.
- Fixed enum mappings and DTO types.
- Added duplicate callback handling.
- Switched amount fields from `Double` to `BigDecimal`.
- Refined gateway failure handling.
- Implemented asynchronous callback simulation.
- Added payment listing and search-by-status APIs.

---

# Author

**M V Elangovan**

Java Developer Assessment

Matrix Business Services India Pvt. Ltd.
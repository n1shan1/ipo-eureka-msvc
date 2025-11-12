# IPO Microservices - API Routes Documentation

## Overview

All API requests should go through the **API Gateway** at `http://localhost:8080` (when using Docker) or the gateway port in your deployment.

## Service Architecture

```
Client → API Gateway (8080) → Service Registry (8761) → Microservices
                                                       ├─ Application Service (8081)
                                                       ├─ Payment Service (8082)
                                                       ├─ Allotment Service (8087)
                                                       └─ Notification Service (8084)
```

---

## API Routes

### 1. Application Service Routes

#### Submit IPO Application
**Endpoint:** `POST /api/v1/ipo/{ipoId}/apply`  
**Gateway URL:** `http://localhost:8080/api/v1/ipo/{ipoId}/apply`  
**Service URL:** `http://localhost:8081/api/v1/ipo/{ipoId}/apply`

**Description:** Submit a new IPO application for an investor.

**Headers:**
- `Content-Type: application/json`
- `Idempotency-Key: <unique-key>` (required for preventing duplicate submissions)

**Path Parameters:**
- `ipoId` - The IPO identifier (e.g., "testipo")

**Request Body:**
```json
{
  "investorId": "user1",
  "lots": 5,
  "userUpiId": "user@upi"
}
```

**Response:**
- `202 Accepted` - Application submitted successfully
```json
{
  "applicationId": "uuid-string",
  "ipoId": "testipo",
  "investorId": "user1",
  "lots": 5,
  "status": "PENDING",
  "idempotencyKey": "test123"
}
```

- `200 OK` - Request was already processed (idempotency)
- `409 Conflict` - Duplicate application for the same investor and IPO

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: test123' \
  -d '{
    "investorId": "user1",
    "lots": 5,
    "userUpiId": "user@upi"
  }'
```

---

### 2. Payment Service Routes

#### Payment Webhook (Bank Callback)
**Endpoint:** `POST /webhook`  
**Gateway URL:** `http://localhost:8080/webhook`  
**Service URL:** `http://localhost:8082/webhook`

**Description:** Webhook endpoint for bank to send payment mandate approval/rejection.

**Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "mandateId": "mandate-uuid",
  "status": "APPROVED"
}
```

**Status Values:**
- `APPROVED` - Payment mandate approved by bank
- `FAILED` - Payment mandate rejected by bank

**Response:**
- `200 OK` - Webhook processed successfully
```json
"Webhook processed"
```

- `400 Bad Request` - Mandate not found

**Example:**
```bash
# Approve payment
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{
    "mandateId": "mandate-uuid-here",
    "status": "APPROVED"
  }'

# Reject payment
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{
    "mandateId": "mandate-uuid-here",
    "status": "FAILED"
  }'
```

**Note:** In a real scenario, this endpoint would be called by the bank's payment gateway, not directly by clients.

---

### 3. Allotment Service Routes

#### Trigger Allotment Process
**Endpoint:** `POST /api/v1/allotment/trigger`  
**Gateway URL:** `http://localhost:8080/api/v1/allotment/trigger`  
**Service URL:** `http://localhost:8087/api/v1/allotment/trigger`

**Description:** Manually trigger the IPO share allotment lottery process.

**Headers:**
- None required

**Request Body:**
- None

**Response:**
- `200 OK` - Allotment process triggered successfully
```json
"Allotment process triggered successfully"
```

- `500 Internal Server Error` - Error during allotment process
```json
"Error: <error-message>"
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/allotment/trigger
```

---

#### Manual Allotment (Admin)
**Endpoint:** `POST /allotment/allot`  
**Gateway URL:** `http://localhost:8080/allotment/allot?ipoId={ipoId}&totalShares={totalShares}`  
**Service URL:** `http://localhost:8087/allotment/allot?ipoId={ipoId}&totalShares={totalShares}`

**Description:** Perform manual allotment for a specific IPO with a specified number of shares.

**Headers:**
- None required

**Query Parameters:**
- `ipoId` - The IPO identifier (required)
- `totalShares` - Total number of shares available for allotment (required)

**Request Body:**
- None

**Response:**
- `200 OK` - Allotment completed
```json
"Allotment completed for IPO: testipo"
```

**Example:**
```bash
curl -X POST "http://localhost:8080/allotment/allot?ipoId=testipo&totalShares=1000"
```

---

### 4. Notification Service

**No REST API** - This service is event-driven and listens to message queues/topics to send notifications. It does not expose any HTTP endpoints.

**Events Handled:**
- Application created
- Payment mandate approved
- Payment mandate failed
- Shares allotted
- Allotment failed

---

## Complete Application Flow

### Step 1: Submit IPO Application
```bash
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: unique-key-123' \
  -d '{
    "investorId": "investor001",
    "lots": 3,
    "userUpiId": "investor001@okaxis"
  }'
```

**Response:**
```json
{
  "applicationId": "app-uuid-123",
  "ipoId": "testipo",
  "investorId": "investor001",
  "lots": 3,
  "status": "PENDING"
}
```

**What Happens:**
1. Application service saves the application
2. Sends event to payment service via ActiveMQ
3. Payment service creates a mandate and sends UPI request

---

### Step 2: Bank Approves Payment (Webhook)
```bash
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{
    "mandateId": "mandate-uuid-from-db",
    "status": "APPROVED"
  }'
```

**Response:**
```json
"Webhook processed"
```

**What Happens:**
1. Payment service updates mandate status to APPROVED
2. Publishes "mandate.approved" event to topic
3. Application service listens and updates application status to APPROVED
4. Notification service sends confirmation to investor

---

### Step 3: Run Lottery/Allotment
**Option A: Automatic (Scheduled)**
- Allotment service runs automatically based on scheduled tasks

**Option B: Manual Trigger**
```bash
curl -X POST http://localhost:8080/api/v1/allotment/trigger
```

**Response:**
```json
"Allotment process triggered successfully"
```

**What Happens:**
1. Allotment service fetches all APPROVED applications
2. Runs lottery algorithm to distribute shares
3. Creates allotment records
4. Publishes allotment events
5. Notification service sends allotment results to investors

---

## Gateway Routes Configuration

The API Gateway uses Spring Cloud Gateway with Eureka service discovery. All routes use load balancing (`lb://service-name`).

### Configured Routes:

| Route ID | Path Pattern | Target Service | Description |
|----------|-------------|----------------|-------------|
| `app_service_route` | `/api/v1/ipo/**` | `ipo-application-service` | IPO application endpoints |
| `payment_service_route` | `/api/v1/payments/**` | `ipo-payment-service` | Payment management (future) |
| `payment_webhook_route` | `/webhook/**` | `ipo-payment-service` | Bank webhook callbacks |
| `allotment_trigger_route` | `/api/v1/allotment/**` | `ipo-allotment-service` | Allotment management |
| `lottery_route` | `/allotment/**` | `ipo-allotment-service` | Manual lottery trigger |

---

## Service Discovery

All services register with **Eureka Service Registry** at `http://localhost:8761`.

**View Registered Services:**
```
http://localhost:8761
```

---

## Health Check Endpoints

Each Spring Boot service exposes actuator health endpoints:

- **Service Registry:** `http://localhost:8761/actuator/health`
- **API Gateway:** `http://localhost:8080/actuator/health`
- **Application Service:** `http://localhost:8081/actuator/health`
- **Payment Service:** `http://localhost:8082/actuator/health`
- **Allotment Service:** `http://localhost:8087/actuator/health`
- **Notification Service:** `http://localhost:8084/actuator/health`

**Example:**
```bash
curl http://localhost:8080/actuator/health
```

---

## Error Responses

All services follow standard HTTP status codes:

- `200 OK` - Request successful
- `201 Created` - Resource created
- `202 Accepted` - Request accepted for processing
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Conflict (e.g., duplicate application)
- `500 Internal Server Error` - Server error

**Error Response Format:**
```json
{
  "timestamp": "2025-11-12T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/ipo/testipo/apply"
}
```

---

## Testing with Postman

Import the provided `IPO_Microservices_API.postman_collection.json` into Postman for pre-configured API requests.

The collection includes:
1. Submit IPO Application
2. Approve Payment Webhook
3. Reject Payment Webhook
4. Trigger Allotment
5. Manual Allotment

---

## Infrastructure Endpoints

### PostgreSQL Database
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `ipo_db`
- **Username:** `postgres`
- **Password:** `password`

**Connect:**
```bash
psql -h localhost -p 5432 -U postgres -d ipo_db
```

### ActiveMQ Web Console
- **URL:** `http://localhost:8161`
- **Username:** `admin`
- **Password:** `admin`

**Queues:**
- `app.created.queue` - Application created events
- `mandate.created.queue` - Payment mandate created events

**Topics:**
- `mandate.approved.topic` - Payment approved events
- `mandate.failed.topic` - Payment failed events
- `allotment.completed.topic` - Allotment completed events

---

## Notes

1. **Idempotency:** Always use a unique `Idempotency-Key` header when submitting applications to prevent duplicate submissions.

2. **Service Discovery:** All requests through the gateway are automatically load-balanced across multiple instances of the same service (if scaled).

3. **Asynchronous Processing:** Most operations are asynchronous. Check the service logs or database for processing status.

4. **Event-Driven:** Services communicate via ActiveMQ for loose coupling and better scalability.

5. **Direct Service Access:** While services can be accessed directly via their ports, it's recommended to use the API Gateway for better load balancing, routing, and future features like authentication.

---

## Quick Reference

### Most Common Operations

**1. Submit Application:**
```bash
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: key-$(date +%s)' \
  -d '{"investorId":"user1","lots":5,"userUpiId":"user@upi"}'
```

**2. Approve Payment:**
```bash
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{"mandateId":"MANDATE_ID","status":"APPROVED"}'
```

**3. Trigger Allotment:**
```bash
curl -X POST http://localhost:8080/api/v1/allotment/trigger
```

---

## Support

For issues or questions about the API routes:
1. Check service logs: `docker compose logs -f <service-name>`
2. Verify service registration at Eureka dashboard: `http://localhost:8761`
3. Check ActiveMQ console for message flow: `http://localhost:8161`

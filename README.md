# IPO Microservices System

This is a simple example of a microservices architecture for an IPO (Initial Public Offering) application system. It's designed to be easy to understand for beginners learning about microservices.

## What are Microservices?

Microservices are small, independent services that work together to form a complete application. Each service has its own:

- **Database** (or part of a shared database)
- **Business logic**
- **API endpoints**
- **Responsibilities**

## Architecture Overview

Our IPO system has 4 microservices:

### 1. Application Service (Port 8081)
- **Purpose**: Handles IPO applications from investors
- **Technology**: Spring Boot, REST API, PostgreSQL
- **Key Features**:
  - Accept application submissions
  - Prevent duplicates with idempotency
  - Send events when applications are created

### 2. Payment Service (Port 8082)
- **Purpose**: Manages payment mandates
- **Technology**: Spring Boot, REST API, PostgreSQL, ActiveMQ
- **Key Features**:
  - Listen for new applications
  - Simulate bank API calls
  - Handle payment webhooks

### 3. Allotment Service (Port 8083)
- **Purpose**: Runs the lottery to allocate shares
- **Technology**: Spring Boot, Scheduling, PostgreSQL, ActiveMQ
- **Key Features**:
  - Scheduled daily lottery
  - Fair random selection algorithm
  - Call other services for data

### 4. Notification Service (Port 8084)
- **Purpose**: Sends notifications to users
- **Technology**: Spring Boot, ActiveMQ
- **Key Features**:
  - Listen for allotment results
  - Send mock emails to winners/losers

## Communication Between Services

Services communicate in two ways:

### 1. Synchronous (REST APIs)
- Direct HTTP calls between services
- Used when immediate response is needed
- Example: Allotment service calls Application service to get approved applications

### 2. Asynchronous (Events with ActiveMQ)
- Services send messages without waiting for response
- Used for decoupling and reliability
- Example: Application service sends "application created" event

## Event Types

- **Queues** (Point-to-Point): One service sends, one service receives
  - `app.created.queue`: Application → Payment
- **Topics** (Publish-Subscribe): One service sends, multiple services can receive
  - `mandate.approved.topic`: Payment → Application + Notification
  - `allotment.done.topic`: Allotment → Notification

## How to Run

### Quick Start (Recommended)
```bash
# Clean build and start everything
./run-services.sh

# Stop everything
./stop-services.sh
```

### Manual Setup
1. **Start Infrastructure**:
   ```bash
   docker-compose up -d
   ```

2. **Start Services** (in separate terminals):
   ```bash
   cd ipo-application-service && mvn spring-boot:run
   cd ipo-payment-service && mvn spring-boot:run
   cd ipo-allotment-service && mvn spring-boot:run
   cd ipo-notification-service && mvn spring-boot:run
   ```

3. **Test the System**:
   ```bash
   # Submit an application
   curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
     -H "Content-Type: application/json" \
     -H "Idempotency-Key: key123" \
     -d '{"investorId":"user1","lots":10,"userUpiId":"upi@user"}'

   # Approve the payment (simulate bank webhook)
   curl -X POST http://localhost:8082/api/v1/payments/webhook \
     -H "Content-Type: application/json" \
     -d '{"applicationId":"returned-app-id","status":"APPROVED","bankReferenceId":"bank123"}'
   ```

### API Testing with Postman
- Import `IPO_Microservices_API.postman_collection.json` into Postman
- Use the "Complete Flow Test" folder for end-to-end testing
- See `TESTING_README.md` for detailed testing instructions

## Key Concepts Demonstrated

- **Service Independence**: Each service can be developed, deployed, and scaled separately
- **Event-Driven Architecture**: Loose coupling through message passing
- **Database per Service**: Each service manages its own data
- **API Gateway Pattern**: REST endpoints for external communication
- **Saga Pattern**: Business transactions across multiple services
- **Circuit Breaker**: Fault tolerance (not implemented, but could be added)

## Learning Points

1. **Why Microservices?**
   - Easier to maintain and scale individual components
   - Technology diversity (different services can use different languages/frameworks)
   - Fault isolation (one service failing doesn't break others)

2. **Challenges of Microservices**
   - Distributed system complexity
   - Service discovery and communication
   - Data consistency across services
   - Testing multiple services together

3. **When to Use Microservices**
   - Large, complex applications
   - Teams that can work independently
   - Need for frequent deployments
   - Different scaling requirements

## Next Steps

- Add service discovery (Eureka)
- Implement API gateway (Spring Cloud Gateway)
- Add distributed tracing (Zipkin)
- Implement circuit breakers (Resilience4j)
- Add authentication/authorization
- Containerize services (Docker)
- Add monitoring (Prometheus/Grafana)
# IPO Microservices System - Implementation Documentation

## Overview

This document provides comprehensive documentation for the IPO (Initial Public Offering) Management System, a distributed microservices architecture built with Spring Boot, Apache Kafka, and PostgreSQL. The system handles the complete IPO application lifecycle from submission to allotment and notification.

## Architecture Overview

### System Components

The system consists of four independent microservices:

1. **IPO Application Service** (Port 8081) - Handles IPO application submissions
2. **IPO Payment Service** (Port 8082) - Manages payment mandates and webhooks
3. **IPO Allotment Service** (Port 8083) - Runs lottery-based allotment processing
4. **IPO Notification Service** (Port 8084) - Sends notifications based on allotment results

### Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Message Broker**: Apache Kafka 7.4.0 with Zookeeper
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **ORM**: Hibernate/JPA

### Communication Pattern

The system uses **event-driven architecture** with Apache Kafka for inter-service communication:

- **Synchronous**: REST APIs for external client interactions
- **Asynchronous**: Kafka events for internal service communication

## Infrastructure Setup

### Docker Compose Configuration

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ipo_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1

volumes:
  postgres_data:
```

### Database Schema

The system uses a shared PostgreSQL database with separate tables for each service:

#### IPO Application Service
```sql
CREATE TABLE ipo_applications (
    id BIGSERIAL PRIMARY KEY,
    application_id VARCHAR(255) UNIQUE NOT NULL,
    ipo_id VARCHAR(255) NOT NULL,
    investor_id VARCHAR(255) NOT NULL,
    lots INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    UNIQUE(ipo_id, investor_id)
);
```

#### Payment Service
```sql
CREATE TABLE mandates (
    id BIGSERIAL PRIMARY KEY,
    application_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    bank_reference_id VARCHAR(255)
);
```

#### Allotment Service
```sql
CREATE TABLE allotments (
    id BIGSERIAL PRIMARY KEY,
    ipo_id VARCHAR(255) NOT NULL,
    winner_application_ids TEXT[], -- PostgreSQL array
    non_winner_application_ids TEXT[] -- PostgreSQL array
);
```

## Service Implementation Details

### 1. IPO Application Service

**Purpose**: Handles IPO application submissions with idempotency and duplicate prevention.

#### Key Components

**Entity: IPOApplication.java**
```java
@Entity
@Table(name = "ipo_applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ipo_id", "investor_id"}))
public class IPOApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", unique = true)
    private String applicationId;

    @Column(name = "ipo_id")
    private String ipoId;

    @Column(name = "investor_id")
    private String investorId;

    private int lots;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    // Getters and setters...
}
```

**Controller: ApplicationController.java**
```java
@RestController
@RequestMapping("/api/v1/ipo")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private KafkaTemplate<String, ApplicationCreatedEvent> kafkaTemplate;

    @PostMapping("/{ipoId}/apply")
    public ResponseEntity<IPOApplication> applyForIPO(
            @PathVariable String ipoId,
            @RequestBody ApplicationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        // Idempotency check
        Optional<IPOApplication> existing = applicationRepository
            .findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        // Duplicate application check
        Optional<IPOApplication> duplicate = applicationRepository
            .findByIpoIdAndInvestorId(ipoId, request.getInvestorId());
        if (duplicate.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Create and save application
        IPOApplication application = new IPOApplication();
        application.setApplicationId(UUID.randomUUID().toString());
        application.setIpoId(ipoId);
        application.setInvestorId(request.getInvestorId());
        application.setLots(request.getLots());
        application.setStatus(IPOApplication.Status.PENDING);
        application.setIdempotencyKey(idempotencyKey);

        IPOApplication saved = applicationRepository.save(application);

        // Publish event
        ApplicationCreatedEvent event = new ApplicationCreatedEvent();
        event.setApplicationId(saved.getApplicationId());
        event.setInvestorId(saved.getInvestorId());
        event.setUserUpiId(request.getUserUpiId());
        event.setAmount(calculateAmount(saved.getLots()));

        kafkaTemplate.send("application.created", event);

        return ResponseEntity.accepted().body(saved);
    }

    private double calculateAmount(int lots) {
        return lots * 100.0; // Mock calculation
    }
}
```

**Event Listener: PaymentStatusListener.java**
```java
@Component
public class PaymentStatusListener {

    @Autowired
    private ApplicationRepository applicationRepository;

    @KafkaListener(topics = "mandate.approved", groupId = "application-service")
    public void handleMandateApproved(MandateApprovedEvent event) {
        Optional<IPOApplication> applicationOpt = applicationRepository
            .findByApplicationId(event.getApplicationId());
        if (applicationOpt.isPresent()) {
            IPOApplication application = applicationOpt.get();
            application.setStatus(IPOApplication.Status.APPROVED);
            applicationRepository.save(application);
        }
    }
}
```

#### Repository Interface
```java
@Repository
public interface ApplicationRepository extends JpaRepository<IPOApplication, Long> {
    Optional<IPOApplication> findByApplicationId(String applicationId);
    Optional<IPOApplication> findByIdempotencyKey(String idempotencyKey);
    Optional<IPOApplication> findByIpoIdAndInvestorId(String ipoId, String investorId);
}
```

### 2. IPO Payment Service

**Purpose**: Manages payment mandates and processes bank integrations.

#### Key Components

**Entity: Mandate.java**
```java
@Entity
@Table(name = "mandates")
public class Mandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private String applicationId;

    private double amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "bank_reference_id")
    private String bankReferenceId;

    public enum Status {
        PENDING, APPROVED, FAILED
    }

    // Getters and setters...
}
```

**Event Listener: ApplicationCreatedListener.java**
```java
@Component
public class ApplicationCreatedListener {

    @Autowired
    private MandateRepository mandateRepository;

    @KafkaListener(topics = "application.created", groupId = "payment-service")
    public void handleApplicationCreated(ApplicationCreatedEvent event) {
        System.out.println("Mock: Calling bank API to create mandate for application " +
            event.getApplicationId() + " for amount " + event.getAmount() + ".");

        Mandate mandate = new Mandate();
        mandate.setApplicationId(event.getApplicationId());
        mandate.setAmount(event.getAmount());
        mandate.setStatus(Mandate.Status.PENDING);
        mandateRepository.save(mandate);
    }
}
```

**Webhook Controller: PaymentWebhookController.java**
```java
@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    @Autowired
    private MandateRepository mandateRepository;

    @Autowired
    private KafkaTemplate<String, MandateApprovedEvent> kafkaTemplate;

    @PostMapping("/payment/status")
    public ResponseEntity<Void> handlePaymentStatus(@RequestBody PaymentStatusWebhook webhook) {
        Optional<Mandate> mandateOpt = mandateRepository
            .findByApplicationId(webhook.getApplicationId());

        if (mandateOpt.isPresent()) {
            Mandate mandate = mandateOpt.get();
            mandate.setStatus(webhook.isSuccess() ?
                Mandate.Status.APPROVED : Mandate.Status.FAILED);
            mandate.setBankReferenceId(webhook.getBankReferenceId());
            mandateRepository.save(mandate);

            if (webhook.isSuccess()) {
                MandateApprovedEvent event = new MandateApprovedEvent();
                event.setApplicationId(mandate.getApplicationId());
                kafkaTemplate.send("mandate.approved", event);
            }
        }

        return ResponseEntity.ok().build();
    }
}
```

### 3. IPO Allotment Service

**Purpose**: Runs lottery-based allotment processing for oversubscribed IPOs.

#### Key Components

**Entity: Allotment.java**
```java
@Entity
@Table(name = "allotments")
public class Allotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipoId;

    @ElementCollection
    private Set<String> winnerApplicationIds;

    @ElementCollection
    private Set<String> nonWinnerApplicationIds;

    // Getters and setters...
}
```

**Lottery Engine: LotteryEngine.java**
```java
@Service
public class LotteryEngine {

    public Set<String> runLottery(List<String> allApprovedIds,
                                  int totalLotsAvailable, String seed) {
        if (allApprovedIds.size() <= totalLotsAvailable) {
            return new HashSet<>(allApprovedIds);
        }

        List<ApplicantScore> scores = new ArrayList<>();
        for (String appId : allApprovedIds) {
            String hash = sha256(seed + ":" + appId);
            scores.add(new ApplicantScore(appId, hash));
        }

        scores.sort(Comparator.comparing(ApplicantScore::getHash));

        Set<String> winners = new HashSet<>();
        for (int i = 0; i < totalLotsAvailable && i < scores.size(); i++) {
            winners.add(scores.get(i).getAppId());
        }

        return winners;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ApplicantScore {
        private String appId;
        private String hash;

        public ApplicantScore(String appId, String hash) {
            this.appId = appId;
            this.hash = hash;
        }

        public String getAppId() { return appId; }
        public String getHash() { return hash; }
    }
}
```

**Scheduler: LotteryScheduler.java**
```java
@Component
public class LotteryScheduler {

    @Autowired
    private LotteryEngine lotteryEngine;

    @Autowired
    private AllotmentRepository allotmentRepository;

    @Autowired
    private KafkaTemplate<String, AllotmentDoneEvent> kafkaTemplate;

    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void runAllotmentProcess() {
        System.out.println("Starting allotment process...");

        String ipoId = "some-ipo-id";
        List<String> approvedApplicationIds = getApprovedApplications(ipoId);
        Set<String> winners = lotteryEngine.runLottery(approvedApplicationIds, 100, "seed");
        Set<String> nonWinners = new HashSet<>(approvedApplicationIds);
        nonWinners.removeAll(winners);

        // Save to database
        Allotment allotment = new Allotment();
        allotment.setIpoId(ipoId);
        allotment.setWinnerApplicationIds(winners);
        allotment.setNonWinnerApplicationIds(nonWinners);
        allotmentRepository.save(allotment);

        // Publish event
        AllotmentDoneEvent event = new AllotmentDoneEvent();
        event.setIpoId(ipoId);
        event.setWinnerApplicationIds(winners);
        event.setNonWinnerApplicationIds(nonWinners);
        kafkaTemplate.send("allotment.done", event);
    }

    private List<String> getApprovedApplications(String ipoId) {
        // Mock: In real implementation, call application service
        return List.of("app1", "app2", "app3");
    }
}
```

### 4. IPO Notification Service

**Purpose**: Sends notifications to users based on allotment results.

#### Key Components

**Event Listener: NotificationListener.java**
```java
@Component
public class NotificationListener {

    @KafkaListener(topics = "allotment.done", groupId = "notification-service")
    public void handleAllotmentDone(AllotmentDoneEvent event) {
        event.getWinnerApplicationIds()
            .forEach(appId -> System.out.println(
                "Mock: Sending 'CONGRATS' email to user " + appId + "."));

        event.getNonWinnerApplicationIds()
            .forEach(appId -> System.out.println(
                "Mock: Sending 'SORRY' email to user " + appId + "."));
    }
}
```

## Event Flow and Data Transfer Objects

### Event Classes

**ApplicationCreatedEvent.java**
```java
public class ApplicationCreatedEvent {
    private String applicationId;
    private String investorId;
    private String userUpiId;
    private double amount;
    // Getters and setters...
}
```

**MandateApprovedEvent.java**
```java
public class MandateApprovedEvent {
    private String applicationId;
    // Getters and setters...
}
```

**AllotmentDoneEvent.java**
```java
public class AllotmentDoneEvent {
    private String ipoId;
    private Set<String> winnerApplicationIds;
    private Set<String> nonWinnerApplicationIds;
    // Getters and setters...
}
```

### Request/Response DTOs

**ApplicationRequest.java**
```java
public class ApplicationRequest {
    private String investorId;
    private int lots;
    private String userUpiId;
    // Getters and setters...
}
```

**PaymentStatusWebhook.java**
```java
public class PaymentStatusWebhook {
    private String applicationId;
    private boolean success;
    private String bankReferenceId;
    // Getters and setters...
}
```

## Configuration

### Application Configuration (application.yml)

Each service has similar configuration with service-specific ports:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ipo_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update
      dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: {service-name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.ipo.app

server:
  port: {service-port}
```

### Maven Dependencies (pom.xml)

All services share similar dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## API Endpoints

### IPO Application Service (Port 8081)

**POST /api/v1/ipo/{ipoId}/apply**
- **Headers**: `Idempotency-Key` (required)
- **Body**:
```json
{
  "investorId": "string",
  "lots": 1,
  "userUpiId": "string"
}
```
- **Response**: IPOApplication object

### IPO Payment Service (Port 8082)

**POST /api/webhooks/payment/status**
- **Body**:
```json
{
  "applicationId": "string",
  "success": true,
  "bankReferenceId": "string"
}
```

## Business Logic Implementation

### Idempotency Handling

The application service implements idempotency using unique keys:

```java
@RequestHeader("Idempotency-Key") String idempotencyKey
Optional<IPOApplication> existing = applicationRepository
    .findByIdempotencyKey(idempotencyKey);
if (existing.isPresent()) {
    return ResponseEntity.ok(existing.get());
}
```

### Duplicate Prevention

Prevents multiple applications for the same IPO by the same investor:

```java
Optional<IPOApplication> duplicate = applicationRepository
    .findByIpoIdAndInvestorId(ipoId, request.getInvestorId());
if (duplicate.isPresent()) {
    return ResponseEntity.badRequest().build();
}
```

### Lottery Algorithm

Uses SHA-256 hashing for deterministic, fair lottery:

```java
private String sha256(String input) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
    // Convert to hex string...
}
```

### Event-Driven Communication

Services communicate asynchronously through Kafka topics:

1. `application.created` - Triggers mandate creation
2. `mandate.approved` - Updates application status
3. `allotment.done` - Triggers notifications

## Deployment and Running

### Prerequisites

- Java 17
- Maven 3.6+
- Docker & Docker Compose

### Startup Sequence

1. **Start Infrastructure**:
```bash
docker-compose up -d
```

2. **Start Services** (in any order):
```bash
# Terminal 1
cd ipo-application-service && mvn spring-boot:run

# Terminal 2
cd ipo-payment-service && mvn spring-boot:run

# Terminal 3
cd ipo-allotment-service && mvn spring-boot:run

# Terminal 4
cd ipo-notification-service && mvn spring-boot:run
```

### Health Checks

- **PostgreSQL**: `docker exec ipo-misvc-postgres-1 pg_isready -U postgres`
- **Kafka**: `docker exec ipo-misvc-kafka-1 kafka-topics --list --bootstrap-server localhost:9092`
- **Services**: Check application logs for successful startup messages

## Testing the System

### Complete IPO Flow Test

1. **Submit Application**:
```bash
curl -X POST http://localhost:8081/api/v1/ipo/IPO001/apply \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-key-123" \
  -d '{
    "investorId": "INV001",
    "lots": 10,
    "userUpiId": "user@upi"
  }'
```

2. **Simulate Payment Success**:
```bash
curl -X POST http://localhost:8082/api/webhooks/payment/status \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": "generated-app-id",
    "success": true,
    "bankReferenceId": "BANK_REF_123"
  }'
```

3. **Trigger Allotment** (or wait for scheduled run):
```bash
curl -X POST http://localhost:8083/api/allotment/trigger
```

4. **Check Notifications**: Monitor console logs for email notifications.

## Monitoring and Observability

### Logging

- All services use Spring Boot's default logging (Logback)
- Database queries are logged (`spring.jpa.show-sql: true`)
- Kafka events are logged in listeners

### Health Endpoints

Spring Boot Actuator provides health check endpoints:

- `http://localhost:{port}/actuator/health`
- `http://localhost:{port}/actuator/info`

### Metrics

- Database connection pools
- Kafka producer/consumer metrics
- HTTP request metrics

## Security Considerations

### Current Implementation (Development)

- No authentication/authorization
- Plain HTTP endpoints
- No input validation beyond basic constraints
- No rate limiting

### Production Requirements

- JWT/OAuth2 authentication
- HTTPS/TLS encryption
- Input validation and sanitization
- Rate limiting and circuit breakers
- API gateway for cross-cutting concerns

## Scalability and Performance

### Database Optimization

- Connection pooling (HikariCP default)
- Proper indexing on frequently queried columns
- Read replicas for reporting queries

### Kafka Optimization

- Partitioning for high-throughput topics
- Consumer group scaling
- Message compression
- Dead letter queues for failed messages

### Service Scaling

- Horizontal scaling with Kubernetes
- Load balancing
- Circuit breakers for inter-service calls
- Database connection limits

## Future Enhancements

### Service Mesh

- Istio or Linkerd for service discovery
- Distributed tracing (Jaeger)
- Traffic management and policies

### API Gateway

- Spring Cloud Gateway
- Rate limiting
- Request routing and filtering

### Configuration Management

- Spring Cloud Config
- Externalized configuration
- Environment-specific properties

### Monitoring Stack

- Prometheus for metrics collection
- Grafana for dashboards
- ELK stack for log aggregation
- Alert Manager for notifications

### Database Improvements

- Database sharding for multi-tenant support
- Read/write splitting
- Connection pooling optimization

This implementation provides a solid foundation for an IPO management system with proper separation of concerns, event-driven architecture, and scalability considerations.</content>
<parameter name="filePath">/Users/nishantdev/Developer/ipo-misvc/IMPLEMENTATION_DOCUMENTATION.md

# IPO Microservices - Testing Guide

This guide explains how to test the IPO microservices system using the provided scripts and Postman collection.

## 🚀 Quick Start

### 1. Run the Complete System
```bash
./run-services.sh
```
This script will:
- Clean all build artifacts
- Start PostgreSQL and ActiveMQ
- Build all 4 microservices
- Start all services in the correct order
- Provide testing instructions

### 2. Stop the System
```bash
./stop-services.sh
```
Stops all services and infrastructure cleanly.

## 📋 API Testing with Postman

### Import the Collection
1. Open Postman
2. Click "Import" → "File"
3. Select `IPO_Microservices_API.postman_collection.json`
4. The collection includes all available endpoints

### Available Endpoints

#### Application Service (Port 8081)
- `GET /actuator/health` - Health check
- `POST /{ipoId}/apply` - Submit IPO application
- `GET /{ipoId}/applications` - Get approved applications

#### Payment Service (Port 8082)
- `POST /webhook` - Bank payment webhook

## 🧪 Complete Flow Test

Use the "Complete Flow Test" folder in Postman:

### Step 1: Submit Application
```bash
curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: test-flow-001" \
  -d '{
    "investorId": "testuser001",
    "lots": 3,
    "userUpiId": "testuser001@upi"
  }'
```
**Expected:** Returns application details with unique `applicationId`

### Step 2: Approve Payment
```bash
curl -X POST http://localhost:8082/api/v1/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": "PASTE_APP_ID_HERE",
    "status": "APPROVED",
    "bankReferenceId": "BANK_TEST_001"
  }'
```
**Expected:** 200 OK response

### Step 3: Verify Approval
```bash
curl http://localhost:8081/api/v1/ipo/testipo/applications
```
**Expected:** Array containing the approved application ID

## 🔄 Event Flow Demonstration

The system demonstrates event-driven architecture:

1. **Application Submission** → Sends `ApplicationCreatedEvent` to Payment Service
2. **Payment Approval** → Sends `MandateApprovedEvent` to Application + Notification Services
3. **Daily Lottery** → Allotment Service calls Application Service API, then sends results to Notification Service

## 📊 Service Architecture

```
┌─────────────────┐    REST     ┌─────────────────┐
│ Application     │◄──────────►│   Payment       │
│ Service (8081)  │             │   Service (8082)│
└─────────────────┘             └─────────────────┘
         ▲                              ▲
         │                              │
         │ JMS Topics                   │
         │                              │
┌─────────────────┐             ┌─────────────────┐
│ Allotment       │             │ Notification    │
│ Service (8083)  │             │ Service (8084)  │
└─────────────────┘             └─────────────────┘
```

## 🐛 Troubleshooting

### Services Won't Start
- Check if ports 8081-8084 are available
- Ensure Docker is running for infrastructure
- Check service logs: `*.log` files

### API Returns 404
- Verify service is running: `curl http://localhost:PORT/actuator/health`
- Check port numbers match the configuration

### Database Connection Issues
- Ensure PostgreSQL container is running: `docker-compose ps`
- Check connection string in `application.yml`

### Message Queue Issues
- Ensure ActiveMQ container is running
- Check JMS configuration in `JmsConfig.java`

## 📝 Manual Testing Examples

### Test Idempotency
```bash
# First request
curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
  -H "Idempotency-Key: test-123" \
  -H "Content-Type: application/json" \
  -d '{"investorId":"user1","lots":5,"userUpiId":"user@upi"}'

# Second request with same key (should return same result)
curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
  -H "Idempotency-Key: test-123" \
  -H "Content-Type: application/json" \
  -d '{"investorId":"user1","lots":5,"userUpiId":"user@upi"}'
```

### Test Duplicate Prevention
```bash
# First application
curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
  -H "Idempotency-Key: unique1" \
  -H "Content-Type: application/json" \
  -d '{"investorId":"user1","lots":5,"userUpiId":"user@upi"}'

# Duplicate application (same user + IPO) - should fail
curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \
  -H "Idempotency-Key: unique2" \
  -H "Content-Type: application/json" \
  -d '{"investorId":"user1","lots":3,"userUpiId":"user@upi"}'
```

## 🎯 Learning Points

This system demonstrates:
- **Microservices Architecture** - Independent, deployable services
- **Event-Driven Design** - Asynchronous communication with JMS
- **REST APIs** - Synchronous service communication
- **Database per Service** - Each service manages its own data
- **Idempotency** - Safe retry of operations
- **External Integrations** - Webhook pattern for bank APIs
- **Scheduled Tasks** - Background processing with Spring Scheduler
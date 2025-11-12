# IPO Microservices - Quick Reference Card

## 🚀 Quick Start

```bash
# Start all services
./docker-run.sh

# Stop all services
./docker-stop.sh

# Check status
docker compose ps
```

## 🔗 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Single entry point for all requests |
| **Service Registry** | http://localhost:8761 | Eureka dashboard - service discovery |
| **ActiveMQ Console** | http://localhost:8161 | Message broker (admin/admin) |
| **Application Service** | http://localhost:8081 | Direct access (bypasses gateway) |
| **Payment Service** | http://localhost:8082 | Direct access (bypasses gateway) |
| **Allotment Service** | http://localhost:8083 | Direct access (bypasses gateway) |
| **Notification Service** | http://localhost:8084 | Direct access (bypasses gateway) |
| **PostgreSQL** | localhost:5432 | Database (postgres/postgres) |

## 📡 API Endpoints (via Gateway)

### Submit IPO Application
```bash
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: unique-key-123' \
  -d '{
    "investorId": "investor001",
    "lots": 5,
    "userUpiId": "investor@okaxis"
  }'
```

**Response:** `202 Accepted` with application details

### Approve Payment
```bash
# First, get mandate ID from database:
docker compose exec postgres psql -U postgres -d ipo_db -t -c "SELECT id FROM mandates ORDER BY id DESC LIMIT 1;"

# Then approve:
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{
    "mandateId": "PASTE_MANDATE_ID_HERE",
    "status": "APPROVED"
  }'
```

**Response:** `Webhook processed`

### Trigger Allotment
```bash
curl -X POST http://localhost:8080/api/v1/allotment/trigger
```

**Response:** `Allotment process triggered successfully`

⚠️ **Note:** May take 30-60 seconds if services just started

## 🗃️ Database Queries

### Connect to Database
```bash
docker compose exec postgres psql -U postgres -d ipo_db
```

### View Applications
```sql
SELECT application_id, investor_id, status FROM ipo_applications;
```

### View Mandates
```sql
SELECT id, application_id, status FROM mandates;
```

### View Allotments
```sql
SELECT * FROM allotments;
```

### List All Tables
```sql
\dt
```

### Quick Check (One-liner)
```bash
# Applications
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT application_id, investor_id, status FROM ipo_applications;"

# Mandates
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT id, status FROM mandates;"

# Allotments
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT * FROM allotments;"
```

## 📋 Common Tasks

### View Service Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f ipo-application-service
docker compose logs -f ipo-payment-service
docker compose logs -f ipo-allotment-service
docker compose logs -f ipo-notification-service

# Last 20 lines
docker compose logs --tail=20 ipo-application-service
```

### Restart Services
```bash
# All services
docker compose restart

# Specific service
docker compose restart ipo-application-service
```

### Rebuild After Code Changes
```bash
# Build JARs
mvn clean install -DskipTests

# Rebuild Docker images
docker compose build

# Start services
./docker-run.sh
```

### Clean Everything
```bash
# Stop and remove containers, networks, volumes
docker compose down -v

# Remove images
docker compose down --rmi all -v

# Start fresh
./docker-run.sh
```

## 🔍 Troubleshooting

### Check if Services are Registered with Eureka
```bash
# Open browser
open http://localhost:8761

# Or check with curl
curl -s http://localhost:8761/eureka/apps | grep "<app>"
```

### Check Service Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Application Service
curl http://localhost:8081/actuator/health

# Payment Service
curl http://localhost:8082/actuator/health
```

### Check ActiveMQ Queues
```bash
# Open browser
open http://localhost:8161

# Login: admin/admin
# Navigate to: Queues → Check message counts
```

### Connection Refused Error
**Problem:** `Connection refused` when calling allotment trigger

**Solution:**
1. Wait 30-60 seconds after startup
2. Check Eureka: http://localhost:8761
3. Ensure all services show "UP" status
4. Check service logs for errors

### Mandate Not Found
**Problem:** Webhook returns "Mandate not found"

**Solution:**
1. Verify mandate exists in database
2. Check mandate ID is correct UUID format
3. Wait 5 seconds after application submission

## 🎯 Complete Flow Test (3 Steps)

### 1. Submit Application
```bash
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: test-'$(date +%s) \
  -d '{
    "investorId": "user'$(date +%s)'",
    "lots": 5,
    "userUpiId": "user@okaxis"
  }'
```

**Wait 5 seconds** for payment service to create mandate.

### 2. Get Mandate ID & Approve
```bash
# Get mandate ID
MANDATE_ID=$(docker compose exec postgres psql -U postgres -d ipo_db -t -c "SELECT id FROM mandates ORDER BY id DESC LIMIT 1;" | tr -d ' ')

# Approve payment
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d "{
    \"mandateId\": \"$MANDATE_ID\",
    \"status\": \"APPROVED\"
  }"
```

**Wait 5 seconds** for application status to update.

### 3. Trigger Allotment
```bash
curl -X POST http://localhost:8080/api/v1/allotment/trigger
```

**Wait 10-60 seconds** for allotment process to complete.

### 4. Check Results
```bash
# View allotments
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT * FROM allotments;"

# View notifications in logs
docker compose logs ipo-notification-service | tail -20
```

## 📦 Postman Collection

### Import Collection
1. Open Postman
2. Click **Import**
3. Select: `IPO_Microservices_API.postman_collection.json`

### Set Environment Variables
In Postman environment, set:
- `baseUrl`: http://localhost:8080
- `ipoId`: testipo
- `mandateId`: (get from database after application submission)

### Run Complete Flow
Navigate to: **Complete Flow Test** folder
1. Run "Step 1 - Submit Application"
2. Get mandate ID from database
3. Set `mandateId` in environment
4. Run "Step 2 - Approve Payment"
5. Wait 30 seconds
6. Run "Step 3 - Trigger Allotment"

## 🎓 Architecture Notes

### Event Flow
```
Application Submission
  ↓ (REST API)
Application Service
  ↓ (JMS Event: application.created)
Payment Service → Creates Mandate
  ↓ (Webhook from Bank)
Payment Service → Approves Mandate
  ↓ (JMS Event: mandate.approved)
Application Service → Updates Status to APPROVED
  ↓ (Manual Trigger)
Allotment Service → Fetches Approved Applications
  ↓ (Lottery Algorithm)
Allotment Service → Creates Allotments
  ↓ (JMS Events: allotment.success/failure)
Notification Service → Sends Notifications
```

### Services
- **Service Registry**: Eureka for service discovery
- **API Gateway**: Routes requests to services
- **Application Service**: Manages IPO applications
- **Payment Service**: Handles payment mandates & webhooks
- **Allotment Service**: Runs lottery & allocates shares
- **Notification Service**: Sends notifications to investors

### Infrastructure
- **PostgreSQL**: Single database, tables per service
- **ActiveMQ Artemis**: Message broker for events
- **Docker Compose**: Container orchestration

## 📚 Documentation

- **TESTING_GUIDE.md** - Complete testing instructions
- **API_ROUTES.md** - Detailed API documentation
- **DOCKER_README.md** - Docker setup and management
- **README.md** - Project overview

## 🆘 Need Help?

1. Check logs: `docker compose logs -f`
2. Verify services: http://localhost:8761
3. Check database: `docker compose exec postgres psql -U postgres -d ipo_db`
4. Review documentation in repository
5. Ensure all containers are healthy: `docker compose ps`

---

**Pro Tip:** Keep Eureka dashboard (http://localhost:8761) open in a browser tab to monitor service health in real-time!

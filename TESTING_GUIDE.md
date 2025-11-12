# IPO Microservices - Testing Guide

## 🎯 Overview

This guide explains how to test the IPO Microservices application using the provided Postman collection.

## 📋 Prerequisites

### 1. Start the Application
```bash
# Start all services with Docker
./docker-run.sh

# Wait for all services to be healthy (30-60 seconds)
docker compose ps
```

### 2. Verify Services are Running
Check that all services are registered with Eureka:
- Open browser: http://localhost:8761
- Verify all 5 services show "UP" status:
  - API-GATEWAY
  - IPO-APPLICATION-SERVICE
  - IPO-PAYMENT-SERVICE
  - IPO-ALLOTMENT-SERVICE
  - IPO-NOTIFICATION-SERVICE

⚠️ **Important:** Services may take 30-60 seconds to fully register with Eureka after startup.

## 📦 Import Postman Collection

1. Open Postman
2. Click **Import**
3. Select `IPO_Microservices_API.postman_collection.json`
4. Collection will be imported with all endpoints organized in folders

## 🔧 Environment Setup

### Option 1: Use Collection Variables (Recommended)
The collection includes default variables:
- `baseUrl`: http://localhost:8080 (API Gateway)
- `ipoId`: testipo (default IPO for testing)
- `applicationId`: Auto-populated
- `mandateId`: Must be set manually

### Option 2: Create Postman Environment
1. Create new environment: **IPO Microservices - Local**
2. Add variables:
   - `baseUrl`: http://localhost:8080
   - `ipoId`: testipo
   - `applicationId`: (leave empty)
   - `mandateId`: (leave empty)

## 🧪 Testing Scenarios

### Scenario 1: Quick Test - Individual Endpoints

#### 1.1 Test IPO Application Submission
```
Folder: IPO Application Service
Request: Submit IPO Application
```

**What it does:**
- Creates a new IPO application
- Sends event to payment service
- Returns application details

**Expected Response:**
```json
{
  "applicationId": "uuid-here",
  "ipoId": "testipo",
  "investorId": "investor-xxx",
  "lots": 5,
  "status": "PENDING"
}
```

**Auto-populated:**
- `applicationId` is saved to environment variables

#### 1.2 Test Idempotency
```
Folder: IPO Application Service
Request: Submit IPO Application (Idempotency Test)
```

**What it does:**
- Uses fixed Idempotency-Key
- Returns existing application if key was used before

**First run:** Returns 202 with new application  
**Subsequent runs:** Returns 200 with existing application

#### 1.3 Test Payment Webhook
```
Folder: Payment Service
Request: Approve Payment (Webhook)
```

**⚠️ Before running:**
1. Wait 5 seconds after submitting application
2. Get mandate ID from database:
   ```bash
   docker compose exec postgres psql -U postgres -d ipo_db -t -c "SELECT id FROM mandates ORDER BY id DESC LIMIT 1;"
   ```
3. Copy the UUID and set `mandateId` in Postman environment

**What it does:**
- Approves payment mandate
- Publishes event to update application status

**Expected Response:**
```
Webhook processed
```

### Scenario 2: Complete End-to-End Flow

Use the **Complete Flow Test** folder with 3 sequential requests.

#### Step 1: Submit Application
```
Request: Step 1 - Submit Application
```

**Instructions:**
1. Click **Send**
2. Check Console tab for instructions
3. Copy the `applicationId` (auto-saved to environment)
4. **Wait 5-10 seconds** for payment service to process

#### Step 2: Get Mandate ID

Run this command in terminal:
```bash
docker compose exec postgres psql -U postgres -d ipo_db -t -c "SELECT id FROM mandates ORDER BY id DESC LIMIT 1;"
```

Copy the UUID and set it in Postman:
- Click on environment dropdown
- Edit environment
- Set `mandateId` to the UUID value

#### Step 3: Approve Payment
```
Request: Step 2 - Approve Payment
```

**Instructions:**
1. Ensure `mandateId` is set
2. Click **Send**
3. Check Console for success message
4. **Wait 5-10 seconds** for application status to update

#### Step 4: Trigger Allotment
```
Request: Step 3 - Trigger Allotment
```

**⚠️ Important:**
- This may take 30-60 seconds on first run
- Check Eureka dashboard first: http://localhost:8761
- Ensure all services show "UP" status

**Instructions:**
1. Verify all services are registered with Eureka
2. Click **Send**
3. Wait for response (may take time)
4. Check Console for results

**Verify Results:**
```bash
# Check allotments table
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT * FROM allotments;"

# Check notification service logs
docker compose logs ipo-notification-service | tail -30
```

## 🐛 Troubleshooting

### Issue: "Connection refused" error

**Symptoms:**
```
Error: I/O error on GET request for "http://localhost:8081/...": Connection refused
```

**Solution:**
1. Check if all services are registered with Eureka:
   ```
   http://localhost:8761
   ```
2. Wait 30-60 seconds for service discovery to complete
3. Check service logs:
   ```bash
   docker compose logs ipo-allotment-service
   docker compose logs ipo-application-service
   ```
4. Restart services if needed:
   ```bash
   ./docker-stop.sh
   ./docker-run.sh
   ```

### Issue: "mandateId not found"

**Symptoms:**
```json
{
  "error": "Mandate not found"
}
```

**Solution:**
1. Check mandates table:
   ```bash
   docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT id, status FROM mandates;"
   ```
2. Use the correct mandate UUID
3. Ensure the mandate exists (application was submitted)

### Issue: Services not registered with Eureka

**Symptoms:**
- Eureka dashboard shows no instances
- Services not appearing in registry

**Solution:**
1. Wait 30-60 seconds after startup
2. Check service logs for Eureka registration:
   ```bash
   docker compose logs service-registry
   docker compose logs api-gateway
   ```
3. Verify network connectivity:
   ```bash
   docker compose ps
   docker network inspect ipo-misvc_ipo-network
   ```
4. Restart services if needed

### Issue: Database connection errors

**Symptoms:**
```
Connection to localhost:5432 refused
```

**Solution:**
1. Check if PostgreSQL is running:
   ```bash
   docker compose ps postgres
   ```
2. Check if database is healthy:
   ```bash
   docker compose exec postgres pg_isready
   ```
3. Restart database:
   ```bash
   docker compose restart postgres
   ```

## 📊 Database Verification

### Check Applications
```bash
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT application_id, investor_id, status FROM ipo_applications;"
```

### Check Mandates
```bash
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT id, application_id, status FROM mandates;"
```

### Check Allotments
```bash
docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT * FROM allotments;"
```

### Check All Tables
```bash
docker compose exec postgres psql -U postgres -d ipo_db -c "\dt"
```

## 🔍 Monitoring & Logs

### View Service Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f ipo-application-service
docker compose logs -f ipo-payment-service
docker compose logs -f ipo-allotment-service
docker compose logs -f ipo-notification-service

# Last 50 lines
docker compose logs --tail=50 ipo-application-service
```

### Check ActiveMQ
- URL: http://localhost:8161
- Username: `admin`
- Password: `admin`

**What to check:**
- Queue depths
- Message throughput
- Consumer connections

### Check Eureka Dashboard
- URL: http://localhost:8761

**What to check:**
- All 5 services registered
- Status: UP
- Last heartbeat timestamp

## 📝 Test Data Examples

### Sample Application Request
```json
{
  "investorId": "test-investor-001",
  "lots": 5,
  "userUpiId": "investor@okaxis"
}
```

### Sample Webhook Request (Approve)
```json
{
  "mandateId": "146cd796-5e41-453a-8973-303c40475cbe",
  "status": "APPROVED"
}
```

### Sample Webhook Request (Reject)
```json
{
  "mandateId": "146cd796-5e41-453a-8973-303c40475cbe",
  "status": "FAILED"
}
```

## 🎯 Success Criteria

### Application Submission
- ✅ Returns 202 status
- ✅ Response includes `applicationId`
- ✅ Application appears in database with PENDING status
- ✅ Mandate created in database with PENDING status

### Payment Approval
- ✅ Returns 200 status
- ✅ Response: "Webhook processed"
- ✅ Mandate status updated to APPROVED
- ✅ Application status updated to APPROVED (after event processing)

### Allotment
- ✅ Returns 200 status
- ✅ Allotment records created in database
- ✅ Winners and non-winners identified
- ✅ Notification service logs show notifications sent

## 🚀 Performance Tips

### Reduce Response Times
1. Keep services running (don't restart frequently)
2. Allow 30-60 seconds for Eureka registration after restart
3. Use connection pooling (already configured)

### Parallel Testing
You can run multiple application submissions in parallel:
- Each needs unique Idempotency-Key
- Use `{{$randomUUID}}` in Postman for unique keys

### Load Testing
For load testing with multiple applications:
1. Use Postman Collection Runner
2. Set iterations (e.g., 10)
3. Add delays between requests (e.g., 100ms)
4. Monitor ActiveMQ queue depths

## 📚 Additional Resources

- **API Documentation**: See `API_ROUTES.md` for detailed endpoint descriptions
- **Docker Guide**: See `DOCKER_README.md` for container management
- **Architecture**: Event-driven microservices with Spring Boot
- **Service Discovery**: Eureka for dynamic service registration
- **Messaging**: ActiveMQ Artemis for event-driven communication

## 🆘 Support

If you encounter issues not covered in this guide:

1. Check service logs for detailed error messages
2. Verify all prerequisites are met
3. Ensure all Docker containers are healthy
4. Check Eureka dashboard for service registration
5. Review database tables for data integrity

## 🎓 Learning Points

This application demonstrates:
- ✅ **Microservices Architecture**: Independent, scalable services
- ✅ **Event-Driven Design**: Asynchronous communication with ActiveMQ
- ✅ **Service Discovery**: Dynamic service registration with Eureka
- ✅ **API Gateway Pattern**: Single entry point for all requests
- ✅ **Idempotency**: Prevent duplicate operations
- ✅ **Webhook Integration**: External service callbacks
- ✅ **Database per Service**: Each service owns its data
- ✅ **Health Checks**: Service availability monitoring
- ✅ **Containerization**: Docker for consistent environments

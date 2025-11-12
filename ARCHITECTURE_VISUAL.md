# IPO Microservices - Visual Architecture & Testing Flow

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Client (Browser/Postman)                         │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ HTTP Requests
                                ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                               │
│  Routes:                                                                 │
│  • /api/v1/ipo/** → Application Service                                │
│  • /webhook → Payment Service                                           │
│  • /api/v1/allotment/** → Allotment Service                            │
│  • /allotment/** → Allotment Service                                    │
└───┬──────────────────┬──────────────────┬──────────────────────────────┘
    │                  │                  │
    │ Service          │ Service          │ Service
    │ Discovery        │ Discovery        │ Discovery
    ↓                  ↓                  ↓
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  Application    │ │    Payment      │ │   Allotment     │
│   Service       │ │    Service      │ │    Service      │
│  (Port 8081)    │ │  (Port 8082)    │ │  (Port 8083)    │
└────┬────────────┘ └────┬────────────┘ └────┬────────────┘
     │                   │                   │
     │   Events          │   Events          │   Events
     ├───────────────────┼───────────────────┤
     │                   │                   │
     ↓                   ↓                   ↓
┌─────────────────────────────────────────────────────────────┐
│          ActiveMQ Artemis Message Broker                     │
│                   (Port 61616)                               │
│  Events:                                                     │
│  • application.created → Payment Service                     │
│  • mandate.approved → Application Service                    │
│  • allotment.success → Notification Service                  │
│  • allotment.failure → Notification Service                  │
└──────────────────────────┬──────────────────────────────────┘
                           │ Events
                           ↓
                 ┌─────────────────────┐
                 │   Notification      │
                 │     Service         │
                 │   (Port 8084)       │
                 └─────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│               Service Registry - Eureka                      │
│                   (Port 8761)                                │
│  Registered Services:                                        │
│  • API-GATEWAY                                              │
│  • IPO-APPLICATION-SERVICE                                  │
│  • IPO-PAYMENT-SERVICE                                      │
│  • IPO-ALLOTMENT-SERVICE                                    │
│  • IPO-NOTIFICATION-SERVICE                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│            PostgreSQL Database (Port 5432)                   │
│  Database: ipo_db                                           │
│  Tables:                                                     │
│  • ipo_applications (by Application Service)                │
│  • mandates (by Payment Service)                            │
│  • allotments (by Allotment Service)                        │
│  • eligible_applicants (by Allotment Service)               │
│  • allotment_winner_application_ids (by Allotment Service)  │
│  • allotment_non_winner_application_ids (by Allotment)      │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    STEP 1: SUBMIT APPLICATION                            │
└─────────────────────────────────────────────────────────────────────────┘

    Postman/Client
         │
         │ POST /api/v1/ipo/testipo/apply
         │ + Idempotency-Key
         │ + Application data
         ↓
    API Gateway (8080)
         │
         │ Route to Application Service
         ↓
    Application Service (8081)
         │
         ├─→ Check Idempotency Key
         │   (if duplicate, return existing)
         │
         ├─→ Save to Database
         │   Status: PENDING
         │   Returns: applicationId
         │
         └─→ Publish Event
             "application.created"
                 │
                 ↓
            ActiveMQ
                 │
                 ↓
         Payment Service (8082)
                 │
                 └─→ Create Mandate
                     Status: PENDING
                     (async process)

┌─────────────────────────────────────────────────────────────────────────┐
│              STEP 2: APPROVE PAYMENT (BANK WEBHOOK)                      │
└─────────────────────────────────────────────────────────────────────────┘

    Bank System (simulated by Postman)
         │
         │ POST /webhook
         │ + mandateId
         │ + status: APPROVED
         ↓
    API Gateway (8080)
         │
         │ Route to Payment Service
         ↓
    Payment Service (8082)
         │
         ├─→ Update Mandate
         │   Status: APPROVED
         │   Returns: "Webhook processed"
         │
         └─→ Publish Event
             "mandate.approved"
                 │
                 ↓
            ActiveMQ
                 │
                 ↓
         Application Service (8081)
                 │
                 └─→ Update Application
                     Status: APPROVED
                     (ready for allotment)

┌─────────────────────────────────────────────────────────────────────────┐
│                  STEP 3: TRIGGER ALLOTMENT                               │
└─────────────────────────────────────────────────────────────────────────┘

    Postman/Client
         │
         │ POST /api/v1/allotment/trigger
         ↓
    API Gateway (8080)
         │
         │ Route to Allotment Service
         ↓
    Allotment Service (8083)
         │
         ├─→ Call Application Service
         │   GET /api/v1/ipo/testipo/applications
         │   (via Eureka service discovery)
         │
         ├─→ Fetch Approved Applications
         │   
         ├─→ Run Lottery Algorithm
         │   - Select winners
         │   - Calculate share allocation
         │
         ├─→ Save Allotments to Database
         │   
         ├─→ Publish Events
         │   "allotment.success" for winners
         │   "allotment.failure" for non-winners
         │        │
         │        ↓
         │   ActiveMQ
         │        │
         │        ↓
         │   Notification Service (8084)
         │        │
         │        └─→ Send Notifications
         │            (logged to console)
         │
         └─→ Returns: "Allotment triggered successfully"
```

## 🧪 Postman Collection Structure

```
IPO Microservices API Collection
│
├── 📁 IPO Application Service
│   ├── 📄 Submit IPO Application
│   │   • Auto-generates random investorId
│   │   • Auto-generates unique Idempotency-Key
│   │   • Saves applicationId to environment
│   │   • Test assertions for 200/202 status
│   │
│   └── 📄 Submit IPO Application (Idempotency Test)
│       • Uses fixed Idempotency-Key
│       • Tests duplicate prevention
│       • First run: 202, subsequent: 200
│
├── 📁 Payment Service
│   ├── 📄 Approve Payment (Webhook)
│   │   • Pre-request: Check mandateId is set
│   │   • Uses mandateId from environment
│   │   • Status: APPROVED
│   │   • Test assertions for 200 status
│   │
│   └── 📄 Reject Payment (Webhook)
│       • Status: FAILED
│       • Tests payment rejection flow
│
├── 📁 Allotment Service
│   ├── 📄 Trigger Allotment Process
│   │   • Fetches approved applications
│   │   • Runs lottery
│   │   • Creates allotments
│   │   • Note: May take 30-60s first time
│   │
│   └── 📄 Manual Allotment with Parameters
│       • Query params: ipoId, totalShares
│       • Allows custom allotment configuration
│
├── 📁 Complete Flow Test ⭐
│   ├── 📄 Step 1 - Submit Application
│   │   • Unique timestamp-based Idempotency-Key
│   │   • Random investorId
│   │   • Console logging with instructions
│   │   • Auto-saves applicationId
│   │
│   ├── 📄 Step 2 - Approve Payment
│   │   • Pre-request: Validate mandateId
│   │   • Console instructions for database query
│   │   • Uses mandateId from environment
│   │   • Console logging for next steps
│   │
│   └── 📄 Step 3 - Trigger Allotment
│       • Pre-request: Eureka check reminder
│       • Detailed success/error logging
│       • Database query instructions
│       • Log viewing instructions
│
└── 📁 Infrastructure & Monitoring
    ├── 📄 Eureka Service Registry
    │   • URL: http://localhost:8761
    │   • View all registered services
    │
    ├── 📄 ActiveMQ Artemis Console
    │   • URL: http://localhost:8161
    │   • Login: admin/admin
    │   • Monitor queues and messages
    │
    └── 📄 API Gateway Health
        • Actuator health check
        • Verify gateway is running
```

## 🔐 Environment Variables Flow

```
Collection Variables (Default):
├── baseUrl: http://localhost:8080
├── ipoId: testipo
├── applicationId: (empty initially)
└── mandateId: (empty initially)

Step 1: Submit Application
    ↓
    Response contains applicationId
    ↓
    Auto-saved to environment
    ↓
    applicationId: "eecf1f5c-fb19-41a0-b46d-4a2d4e21132b"

Step 1.5: Query Database (manual)
    ↓
    docker compose exec postgres psql ...
    ↓
    Get mandate UUID
    ↓
    Manually set in Postman environment
    ↓
    mandateId: "146cd796-5e41-453a-8973-303c40475cbe"

Step 2: Approve Payment
    ↓
    Uses {{mandateId}} from environment
    ↓
    Webhook processes successfully

Step 3: Trigger Allotment
    ↓
    Uses {{ipoId}} from environment
    ↓
    Allotment completed
```

## 📊 Data Flow Through System

```
                Application Data
                       │
    ┌──────────────────┼──────────────────┐
    │                  │                  │
    ↓                  ↓                  ↓
PostgreSQL        ActiveMQ          Eureka
ipo_db            Events            Registry
    │                  │                  │
    │                  │                  │
┌───┴───┐         ┌────┴────┐       ┌────┴────┐
│Tables │         │ Queues  │       │Services │
├───────┤         ├─────────┤       ├─────────┤
│ ipo_  │         │ app.    │       │ API-    │
│ appli-│         │ created │       │ GATEWAY │
│ cation│         │         │       │         │
├───────┤         ├─────────┤       ├─────────┤
│mandate│         │ mandate │       │ IPO-APP │
│       │         │ .approve│       │ SERVICE │
├───────┤         ├─────────┤       ├─────────┤
│allot- │         │ allot.  │       │ IPO-PAY │
│ment   │         │ success │       │ SERVICE │
└───────┘         └─────────┘       └─────────┘
```

## 🎯 Success Indicators

```
✅ Application Submission
   ├─ HTTP 202 Accepted
   ├─ Response includes applicationId
   ├─ Database: ipo_applications has new row
   └─ Database: mandates has new row (after 5s)

✅ Payment Approval
   ├─ HTTP 200 OK
   ├─ Response: "Webhook processed"
   ├─ Database: mandate.status = APPROVED
   └─ Database: application.status = APPROVED (after 5s)

✅ Allotment Trigger
   ├─ HTTP 200 OK (after 30-60s first time)
   ├─ Response: "triggered successfully"
   ├─ Database: allotments table has new rows
   ├─ Database: eligible_applicants populated
   ├─ Logs: Notification service shows messages sent
   └─ ActiveMQ: allotment.* events processed
```

## 🐛 Troubleshooting Decision Tree

```
API Request Failed?
    │
    ├─→ Connection Refused?
    │   │
    │   ├─→ Check: docker compose ps
    │   │   All containers UP?
    │   │   │
    │   │   ├─→ Yes → Check Eureka (http://localhost:8761)
    │   │   │          All services registered?
    │   │   │          │
    │   │   │          ├─→ Yes → Wait 30-60 seconds
    │   │   │          │         Try again
    │   │   │          │
    │   │   │          └─→ No → Wait, check logs
    │   │   │                   docker compose logs -f
    │   │   │
    │   │   └─→ No → Start services
    │   │             ./docker-run.sh
    │   │
    │   └─→ Wrong port? Check port mapping
    │                   in docker-compose.yml
    │
    ├─→ Mandate Not Found?
    │   │
    │   ├─→ Check: mandateId set in environment?
    │   │   │
    │   │   ├─→ Yes → Verify in database
    │   │   │         docker compose exec postgres psql ...
    │   │   │
    │   │   └─→ No → Get from database and set
    │   │
    │   └─→ Mandate exists but error?
    │       Check mandate.status in database
    │
    └─→ Timeout?
        │
        └─→ Increase request timeout
            Services may need time to process
            (especially allotment: 30-60s)
```

## 📁 Documentation Files Map

```
ipo-misvc/
│
├── 📄 IPO_Microservices_API.postman_collection.json
│   └─→ Import this in Postman
│       13 requests, 5 folders, complete test scripts
│
├── 📘 TESTING_GUIDE.md
│   └─→ Complete testing instructions
│       • Prerequisites
│       • Environment setup
│       • Testing scenarios
│       • Troubleshooting
│       • Database verification
│       • Success criteria
│
├── 📗 QUICK_REFERENCE.md
│   └─→ Quick copy-paste commands
│       • Service URLs
│       • curl commands
│       • Database queries
│       • Common tasks
│       • Troubleshooting
│
├── 📙 TESTING_SUMMARY.md
│   └─→ What was created and tested
│       • Deliverables
│       • Test results
│       • Known issues
│       • Next steps
│
├── 📕 ARCHITECTURE_VISUAL.md (this file)
│   └─→ Visual diagrams and flows
│       • System architecture
│       • Data flow
│       • Testing flow
│       • Decision trees
│
├── 📄 API_ROUTES.md
│   └─→ Detailed API documentation
│
├── 📄 DOCKER_README.md
│   └─→ Docker setup and management
│
└── 📄 README.md
    └─→ Project overview
```

## 🚀 Quick Command Reference

### Start Everything
```bash
./docker-run.sh
# Wait 60 seconds
open http://localhost:8761  # Check Eureka
```

### Complete Test Flow
```bash
# 1. Submit application
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: test-'$(date +%s) \
  -d '{"investorId":"user001","lots":5,"userUpiId":"user@okaxis"}'

# 2. Get mandate ID (wait 5s first)
MANDATE_ID=$(docker compose exec postgres psql -U postgres -d ipo_db -t -c \
  "SELECT id FROM mandates ORDER BY id DESC LIMIT 1;" | tr -d ' ')

# 3. Approve payment
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d "{\"mandateId\":\"$MANDATE_ID\",\"status\":\"APPROVED\"}"

# 4. Trigger allotment (wait 10s first)
curl -X POST http://localhost:8080/api/v1/allotment/trigger

# 5. Check results
docker compose exec postgres psql -U postgres -d ipo_db -c \
  "SELECT * FROM allotments;"
```

### Monitor Services
```bash
# Logs
docker compose logs -f

# Eureka
open http://localhost:8761

# ActiveMQ
open http://localhost:8161
# Login: admin/admin

# Database
docker compose exec postgres psql -U postgres -d ipo_db
```

---

**Use this guide alongside the Postman collection for complete testing!** 🎯

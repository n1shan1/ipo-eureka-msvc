# IPO Microservices - Docker Setup

This guide explains how to run the IPO Microservices application using Docker Compose.

## Prerequisites

- Docker Desktop (version 20.10 or higher)
- Docker Compose (included with Docker Desktop)
- At least 4GB of available RAM
- At least 10GB of free disk space

## Quick Start

### 1. Start All Services

```bash
chmod +x docker-run.sh
./docker-run.sh
```

This script will:
- Build all Docker images
- Start PostgreSQL, ActiveMQ, and all microservices
- Wait for services to become healthy
- Display service URLs and testing instructions

### 2. Stop All Services

```bash
chmod +x docker-stop.sh
./docker-stop.sh
```

Or simply:
```bash
docker compose down
```

### 3. Stop and Remove Data

To stop services and remove all data (including database):
```bash
docker compose down -v
```

## Architecture

The application consists of the following services:

### Infrastructure Services
- **PostgreSQL** (port 5432): Database for storing IPO applications, payments, and allotments
- **ActiveMQ Artemis** (ports 61616, 8161): Message broker for asynchronous communication

### Application Services
- **Service Registry** (port 8761): Eureka service discovery
- **API Gateway** (port 8080): Entry point for all API requests
- **Application Service** (port 8081): Handles IPO application submissions
- **Payment Service** (port 8082): Manages payment processing
- **Allotment Service** (port 8087): Runs lottery for share allotment
- **Notification Service** (port 8084): Sends notifications to investors

## Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | Main entry point |
| Service Registry | http://localhost:8761 | Eureka dashboard |
| Application Service | http://localhost:8081 | Direct access (use gateway instead) |
| Payment Service | http://localhost:8082 | Direct access (use gateway instead) |
| ActiveMQ Console | http://localhost:8161 | Message broker UI (admin/admin) |

## Testing the Application

### Using the API Gateway (Recommended)

All API requests should go through the API Gateway at `http://localhost:8080`.

#### 1. Submit an IPO Application

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

Response will include the application ID.

#### 2. Approve Payment

Replace `APP_ID` with the actual application ID from step 1:

```bash
curl -X POST http://localhost:8080/api/v1/payments/webhook \
  -H 'Content-Type: application/json' \
  -d '{
    "applicationId": "APP_ID",
    "status": "APPROVED",
    "bankReferenceId": "BANK123"
  }'
```

#### 3. Trigger Lottery (Manual)

```bash
curl -X POST http://localhost:8080/run-lottery/testipo
```

### Using Postman

Import the `IPO_Microservices_API.postman_collection.json` file into Postman and use the "Complete Flow Test" folder.

## Docker Commands

### View Running Containers

```bash
docker compose ps
```

### View Logs

View logs for all services:
```bash
docker compose logs -f
```

View logs for a specific service:
```bash
docker compose logs -f ipo-application-service
docker compose logs -f ipo-payment-service
docker compose logs -f postgres
```

### Restart a Service

```bash
docker compose restart ipo-application-service
```

### Rebuild a Service

```bash
docker compose up -d --build ipo-application-service
```

### Access Container Shell

```bash
docker compose exec ipo-application-service sh
docker compose exec postgres psql -U postgres -d ipo_db
```

### Check Service Health

```bash
docker compose ps --format json | grep Health
```

## Troubleshooting

### Services Not Starting

1. Check if Docker is running:
   ```bash
   docker info
   ```

2. Check available resources:
   ```bash
   docker system df
   ```

3. View service logs:
   ```bash
   docker compose logs
   ```

### Database Connection Issues

1. Check if PostgreSQL is healthy:
   ```bash
   docker compose ps postgres
   ```

2. Test database connection:
   ```bash
   docker compose exec postgres psql -U postgres -d ipo_db -c "SELECT 1;"
   ```

### ActiveMQ Connection Issues

1. Check if ActiveMQ is healthy:
   ```bash
   docker compose ps activemq
   ```

2. Access ActiveMQ web console:
   - URL: http://localhost:8161
   - Username: admin
   - Password: admin

### Eureka Registration Issues

1. Check Service Registry:
   - URL: http://localhost:8761
   - All services should be registered within 30-60 seconds

2. Check service logs for connection errors:
   ```bash
   docker compose logs ipo-application-service | grep -i eureka
   ```

### Port Conflicts

If you see "port already in use" errors:

1. Check what's using the port:
   ```bash
   lsof -i :8080  # or whichever port is conflicted
   ```

2. Stop the conflicting process or change the port in `docker-compose.yml`

### Build Failures

1. Clean Docker build cache:
   ```bash
   docker builder prune -a
   ```

2. Rebuild without cache:
   ```bash
   docker compose build --no-cache
   ```

### Out of Disk Space

1. Remove unused images and containers:
   ```bash
   docker system prune -a
   ```

2. Remove unused volumes:
   ```bash
   docker volume prune
   ```

## Development Workflow

### Making Code Changes

1. Stop the services:
   ```bash
   docker compose down
   ```

2. Make your code changes

3. Rebuild and restart:
   ```bash
   docker compose up --build -d
   ```

### Viewing Database Data

```bash
docker compose exec postgres psql -U postgres -d ipo_db

# Inside psql:
\dt                          # List all tables
SELECT * FROM applications;  # Query applications
SELECT * FROM payments;      # Query payments
SELECT * FROM allotments;    # Query allotments
\q                          # Exit
```

### Monitoring

1. **Container Stats**:
   ```bash
   docker stats
   ```

2. **Service Health**:
   ```bash
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   ```

3. **ActiveMQ Queue Status**:
   - Web Console: http://localhost:8161

## Performance Tuning

### Increase Container Resources

Edit `docker-compose.yml` and add resource limits:

```yaml
services:
  ipo-application-service:
    # ... other config ...
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          memory: 512M
```

### Java Heap Settings

Add JVM options in Dockerfile:

```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

## Production Considerations

Before deploying to production:

1. **Change Default Passwords**: Update database and ActiveMQ passwords
2. **Enable SSL/TLS**: Configure HTTPS for API Gateway
3. **Add Authentication**: Implement OAuth2/JWT for API security
4. **Set Up Monitoring**: Use Prometheus + Grafana for metrics
5. **Configure Logging**: Use ELK stack or similar for centralized logging
6. **Use External Database**: Don't run PostgreSQL in Docker for production
7. **Use External Message Broker**: Use managed ActiveMQ or AWS MQ
8. **Set Resource Limits**: Define CPU and memory limits for all containers
9. **Enable Health Checks**: Configure proper health check endpoints
10. **Set Up Backup**: Implement database backup strategy

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [ActiveMQ Artemis Docker Hub](https://hub.docker.com/r/apache/activemq-artemis)

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review service logs: `docker compose logs -f`
3. Check the main README.md for application-specific documentation

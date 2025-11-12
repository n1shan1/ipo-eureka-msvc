# 🪟 Windows Setup Guide for IPO Microservices

## ✅ Problem Solved: JAR File Not Found

The Dockerfiles have been updated to use **multi-stage builds**. This means:
- ✅ **No need to build JARs locally** - Docker builds them automatically
- ✅ **Works on any machine** - Windows, Mac, Linux
- ✅ **Fresh build every time** - No stale JAR issues
- ✅ **No Maven installation required** on your machine

## 🚀 Quick Start on Windows

### Prerequisites
1. **Docker Desktop for Windows** - [Download here](https://www.docker.com/products/docker-desktop/)
2. **Git** (optional) - To clone the repository

### Option 1: Using PowerShell (Recommended)

```powershell
# Navigate to project directory
cd path\to\ipo-misvc

# Start all services (this will build JARs inside Docker)
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

### Option 2: Using Command Prompt (CMD)

```cmd
cd path\to\ipo-misvc
docker-compose up --build
```

### Option 3: Using Git Bash

```bash
cd /path/to/ipo-misvc
docker-compose up --build
```

## ⏱️ First Build Takes Longer

**Expected build time:**
- First build: ~10-15 minutes (downloads Maven, dependencies, builds all services)
- Subsequent builds: ~2-3 minutes (uses Docker cache)

**What Docker is doing:**
1. Downloading base Java 17 JDK image
2. Installing Maven inside containers
3. Downloading all Maven dependencies
4. Compiling Java source code
5. Packaging JAR files
6. Creating runtime containers with Java 17 JRE

## 📋 Step-by-Step Instructions

### Step 1: Open Terminal

**PowerShell (Recommended):**
- Press `Win + X`
- Select "Windows PowerShell" or "Terminal"

**Command Prompt:**
- Press `Win + R`
- Type `cmd` and press Enter

### Step 2: Navigate to Project

```powershell
cd C:\Users\YourName\Documents\ipo-misvc
# Adjust path to where you cloned/downloaded the project
```

### Step 3: Start Services

```powershell
# Build and start all services
docker-compose up --build
```

**What you'll see:**
```
[+] Building 245.3s (85/85) FINISHED
 => [service-registry builder 1/7] FROM docker.io/library/eclipse-temurin:17-jdk
 => [service-registry builder 2/7] WORKDIR /app
 => [service-registry builder 3/7] COPY pom.xml ../pom.xml
 => [service-registry builder 4/7] COPY common-dto ../common-dto
 => [service-registry builder 5/7] COPY pom.xml .
 => [service-registry builder 6/7] COPY src ./src
 => [service-registry builder 7/7] RUN mvn clean package -DskipTests...
```

### Step 4: Wait for Services to Start

Services are ready when you see:
```
ipo-service-registry    | Started ServiceRegistryApplication in 12.345 seconds
ipo-api-gateway         | Started ApiGatewayApplication in 8.123 seconds
ipo-application-service | Started Application in 10.456 seconds
ipo-payment-service     | Started Application in 9.789 seconds
ipo-allotment-service   | Started Application in 11.234 seconds
ipo-notification-service| Started Application in 8.567 seconds
```

### Step 5: Verify Services

Open browser and check:
- **Eureka Dashboard:** http://localhost:8761
- **API Gateway Health:** http://localhost:8080/actuator/health

## 🛑 Stopping Services

### Option 1: Graceful Stop (Recommended)

```powershell
# In the same terminal where docker-compose is running
Press Ctrl + C

# Then clean up
docker-compose down
```

### Option 2: Force Stop

```powershell
# In a new terminal
docker-compose down
```

### Option 3: Stop and Remove Everything

```powershell
# Stop containers, remove volumes, and remove images
docker-compose down -v --rmi all
```

## 🐛 Troubleshooting on Windows

### Issue 1: "Docker daemon is not running"

**Solution:**
1. Open Docker Desktop
2. Wait for it to fully start (Docker icon in system tray should be stable)
3. Try again

### Issue 2: "Port is already in use"

**Error:**
```
Error: bind: address already in use
```

**Solution:**
```powershell
# Find what's using the port (e.g., 8080)
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID_NUMBER> /F
```

### Issue 3: "Cannot connect to Docker daemon"

**Solution:**
1. Restart Docker Desktop
2. In Docker Desktop settings:
   - Go to "General"
   - Ensure "Expose daemon on tcp://localhost:2375" is checked
3. Restart your terminal

### Issue 4: Build fails with "No space left on device"

**Solution:**
```powershell
# Clean up Docker
docker system prune -a --volumes

# This will remove:
# - All stopped containers
# - All networks not used by at least one container
# - All volumes not used by at least one container
# - All images without at least one container
# - All build cache
```

### Issue 5: "Line endings" or "exec format error"

**Solution:**
This happens when shell scripts have Windows line endings (CRLF).

**Fix using Git Bash:**
```bash
dos2unix docker-run.sh
dos2unix docker-stop.sh
```

**Or reconfigure Git:**
```powershell
git config --global core.autocrlf false
git rm -r --cached .
git reset --hard
```

### Issue 6: PowerShell execution policy error

**Error:**
```
Cannot be loaded because running scripts is disabled on this system
```

**Solution:**
```powershell
# Run PowerShell as Administrator, then:
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 📊 Monitoring on Windows

### View All Containers

```powershell
docker ps
```

### View Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f ipo-application-service

# Last 50 lines
docker-compose logs --tail=50 ipo-application-service
```

### Check Resource Usage

```powershell
# Docker stats
docker stats

# Specific container
docker stats ipo-application-service
```

### Access Database (PostgreSQL)

```powershell
# Connect to PostgreSQL
docker exec -it ipo-postgres psql -U postgres -d ipo_db

# Run query
docker exec -it ipo-postgres psql -U postgres -d ipo_db -c "SELECT * FROM ipo_applications;"
```

## 🔧 Windows-Specific Tips

### 1. Use PowerShell, Not CMD
PowerShell has better support for Docker commands and colored output.

### 2. Allocate More Resources to Docker

1. Open Docker Desktop
2. Go to Settings → Resources
3. Increase:
   - **CPUs:** At least 4
   - **Memory:** At least 4GB (8GB recommended)
   - **Disk:** At least 20GB
4. Click "Apply & Restart"

### 3. Enable WSL 2 (Recommended)

Docker Desktop works better with WSL 2:

1. Open PowerShell as Administrator
2. Run:
```powershell
wsl --install
wsl --set-default-version 2
```
3. Restart your computer
4. In Docker Desktop settings:
   - Go to "General"
   - Enable "Use the WSL 2 based engine"

### 4. Disable Antivirus Scanning for Docker

Some antivirus software slows down Docker builds:

1. Add Docker Desktop folders to exclusion list:
   - `C:\Program Files\Docker`
   - `C:\ProgramData\Docker`
   - `%USERPROFILE%\.docker`

### 5. Use Docker Desktop GUI

Docker Desktop provides a GUI for:
- Viewing running containers
- Checking logs
- Stopping/starting services
- Cleaning up resources

## 📝 Testing on Windows

### Using PowerShell

```powershell
# Submit IPO application
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/ipo/testipo/apply" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"; "Idempotency-Key"="test-123"} `
  -Body '{"investorId":"user1","lots":5,"userUpiId":"user@okaxis"}'
```

### Using curl (if installed)

```powershell
curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: test-123" `
  -d '{\"investorId\":\"user1\",\"lots\":5,\"userUpiId\":\"user@okaxis\"}'
```

### Using Postman (Recommended)

1. Import `IPO_Microservices_API.postman_collection.json`
2. Run requests from the collection
3. See `TESTING_GUIDE.md` for details

## 🎯 Build Time Optimization

### First Build (Slow)
```powershell
# ~10-15 minutes
docker-compose up --build
```

### Rebuild After Code Changes
```powershell
# ~2-3 minutes (uses cache)
docker-compose build
docker-compose up
```

### Rebuild Single Service
```powershell
# Only rebuild one service
docker-compose build ipo-application-service
docker-compose up -d ipo-application-service
```

### Force Clean Build
```powershell
# No cache, fresh build
docker-compose build --no-cache
docker-compose up
```

## 🌐 Accessing Services on Windows

All services are accessible on `localhost`:

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Service Registry | http://localhost:8761 |
| ActiveMQ Console | http://localhost:8161 (admin/admin) |
| PostgreSQL | localhost:5432 (postgres/password) |

## 💡 Pro Tips for Windows Users

### 1. Create Shortcuts

Create `start.bat`:
```batch
@echo off
cd /d "%~dp0"
docker-compose up --build
pause
```

Create `stop.bat`:
```batch
@echo off
cd /d "%~dp0"
docker-compose down
pause
```

### 2. Use Windows Terminal

Windows Terminal provides better experience:
- Multiple tabs
- Split panes
- Better colors
- GPU acceleration

Install from: https://aka.ms/terminal

### 3. Schedule Regular Cleanup

Create `cleanup.bat`:
```batch
@echo off
echo Cleaning up Docker...
docker system prune -f
docker volume prune -f
echo Done!
pause
```

Run monthly to free up disk space.

## 📚 Additional Resources

- **Docker Desktop Docs:** https://docs.docker.com/desktop/windows/
- **WSL 2 Setup:** https://docs.microsoft.com/en-us/windows/wsl/install
- **Docker Compose Docs:** https://docs.docker.com/compose/
- **Project Documentation:** See `DOCUMENTATION_INDEX.md`

## ✅ Success Checklist

- [ ] Docker Desktop installed and running
- [ ] WSL 2 enabled (recommended)
- [ ] At least 4GB RAM allocated to Docker
- [ ] Project directory accessible
- [ ] PowerShell or Terminal open
- [ ] Run `docker-compose up --build`
- [ ] Wait 10-15 minutes for first build
- [ ] Check http://localhost:8761 for Eureka
- [ ] Import Postman collection
- [ ] Run Complete Flow Test

## 🆘 Still Having Issues?

1. Check Docker Desktop logs:
   - Click Docker icon in system tray
   - Select "Troubleshoot"
   - View logs

2. Restart Docker Desktop completely

3. Restart Windows (fresh start helps!)

4. Check Windows Firewall isn't blocking Docker

5. Ensure Hyper-V or WSL 2 is enabled in Windows Features

---

**You're all set!** The multi-stage Docker builds will handle everything automatically. No need to install Java or Maven on your Windows machine! 🎉

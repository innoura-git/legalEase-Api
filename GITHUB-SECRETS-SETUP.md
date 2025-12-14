# GitHub Secrets Setup Guide - LegalEase API

## Required Secrets

Navigate to: **Repository → Settings → Secrets and variables → Actions → New repository secret**

### Docker Registry Secrets

| Secret Name | Value |
|-------------|-------|
| `DOCKER_REGISTRY` | `docker.io` |
| `DOCKER_USERNAME` | `innoura` |
| `DOCKER_PASSWORD` | Your Docker PAT |
| `DOCKER_IMAGE_NAME` | `innoura/legalease-api` |

### VM Deployment Secrets

| Secret Name | Value |
|-------------|-------|
| `VM_HOST` | Your VM IP address |
| `VM_USERNAME` | SSH username |
| `VM_PASSWORD` | SSH password |
| `VM_PORT` | `22` (optional) |

---

## How to Run the Workflow

1. Go to **Actions** tab
2. Select **Build and Deploy Docker Container**
3. Click **Run workflow**
4. Enter:
   - **Branch**: `main`, `develop`, etc.
   - **Container name**: `legalease-api` (default)
5. Click **Run workflow**

---

## Application Requirements

### For Health Check to Work

Add Spring Boot Actuator to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

And enable health endpoint in `application.properties`:

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
```

### If NOT using Actuator

Update the Dockerfile health check to use your own endpoint:

```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1
```

---

## Ports

- **API runs on**: `8080`
- Ensure VM firewall allows port `8080`


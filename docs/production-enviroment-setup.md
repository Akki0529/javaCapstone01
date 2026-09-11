# AWS Deployment Guide: RDS & Elastic Beanstalk

## Overview

This guide provides instructions for deploying your Spring Boot application to AWS Elastic Beanstalk with an RDS
PostgreSQL database.

---

## Part 1: RDS PostgreSQL Database Setup

### Create Database Instance

**AWS Console → RDS → Create database**

**Required Configuration:**

| Setting                | Value                     | Notes                               |
|------------------------|---------------------------|-------------------------------------|
| Deployment             | Single-AZ DB instance     | Cost-effective option               |
| DB instance identifier | `java-capstone`           | Unique name for your instance       |
| Master username        | `postgres`                | Database admin user                 |
| Master password        | Create secure password    | Save this - required for connection |
| Credentials management | Self managed              | Manual password control             |
| Instance class         | `db.t4g.micro`            | Burstable classes section           |
| Storage type           | General Purpose SSD (gp2) | Default option                      |
| Allocated storage      | 20 GiB                    | As specified                        |
| Compute resource       | Don't connect to EC2      | Manual configuration                |
| Network type           | IPv4                      | Standard                            |
| VPC                    | Default VPC               | Must match Elastic Beanstalk        |
| DB subnet group        | default                   | Use existing                        |
| Public access          | No                        | Security best practice              |
| VPC security group     | default                   | Will configure later                |
| Initial database name  | `librarydb`               | Creates the actual database         |

**After Creation:**

- Wait for status to show "Available" (5-10 minutes)
- Navigate to database in RDS console
- Copy the **Endpoint** from Connectivity & security tab
- Format: `java-capstone.xxxxx.us-east-1.rds.amazonaws.com`
- Save this endpoint for application configuration

---

## Part 2: Application Preparation

### Build Application

Build your Spring Boot application JAR file:

```bash
./mvnw clean package -DskipTests
```

Verify the JAR file exists:

```bash
ls target/*.jar
```

Expected output: `library-management-api-0.0.1-SNAPSHOT.jar` (or your project name)

### Configure Production Settings

Ensure your `application.properties` or `application-prod.properties` uses environment variables for database
connection:

```properties
spring.datasource.url=jdbc:postgresql://${RDS_HOSTNAME}:${RDS_PORT}/${RDS_DB_NAME}
spring.datasource.username=${RDS_USERNAME}
spring.datasource.password=${RDS_PASSWORD}
server.port=${SERVER_PORT:5000}
```

---

## Part 3: Elastic Beanstalk Deployment

### Create Application

**AWS Console → Elastic Beanstalk → Create application**

**Configuration:**

| Setting              | Value                                              |
|----------------------|----------------------------------------------------|
| Environment tier     | Web server environment                             |
| Application name     | `library-management-api`                           |
| Environment name     | `library-api-env`                                  |
| Domain               | Leave blank (auto-generated)                       |
| Platform             | Java                                               |
| Platform branch      | Corretto 21 running on 64bit Amazon Linux 2023     |
| Platform version     | 4.6.5 (Recommended) or latest                      |
| Application code     | Upload your code                                   |
| Version label        | `v1.0.0` (increment for each deployment)           |
| Source               | Local file → Select your JAR file                  |
| VPC                  | Default VPC (same as RDS)                          |
| Public IP address    | Leave unchecked                                    |
| Instance subnets     | Select at least one (e.g., us-east-1a, us-east-1b) |
| Database             | Leave unchecked (using existing RDS)               |
| Service role         | `aws-elasticbeanstalk-service-role`                |
| EC2 instance profile | `aws-elasticbeanstalk-ec2-role`                    |
| EC2 key pair         | Leave as default (optional)                        |

### Configure Environment Variables

**Required environment variables:**

| Name                     | Value                      | Description                              |
|--------------------------|----------------------------|------------------------------------------|
| `SERVER_PORT`            | `5000`                     | Elastic Beanstalk port requirement       |
| `SPRING_PROFILES_ACTIVE` | `prod`                     | Activates production configuration       |
| `RDS_HOSTNAME`           | `[your-rds-endpoint]`      | From RDS console                         |
| `RDS_PORT`               | `5432`                     | PostgreSQL default port                  |
| `RDS_DB_NAME`            | `librarydb`                | Database name from RDS setup             |
| `RDS_USERNAME`           | `postgres`                 | Master username                          |
| `RDS_PASSWORD`           | `[your-password]`          | Master password from RDS setup           |
| `JWT_SECRET`             | `[generate-secure-secret]` | Generate with: `openssl rand -base64 32` |

**To get your RDS endpoint:**

1. Go to RDS console
2. Click on your database `java-capstone`
3. Copy the endpoint from Connectivity & security section
4. Use the full endpoint URL as `RDS_HOSTNAME` value

**To generate JWT secret:**

```bash
openssl rand -base64 32
```

Copy the output and use it as `JWT_SECRET` value. Store it securely.

### Deploy

1. Review all settings
2. Click "Next" to continue
3. Click "Submit" to create environment
4. Wait for environment creation (5-10 minutes)
5. Check environment health status

---

## Part 4: Security Configuration

### Update RDS Security Group

After both RDS and Elastic Beanstalk are running:

1. **AWS Console → EC2 → Security Groups**
2. Find the RDS security group (check RDS instance details for security group ID)
3. Click **Edit inbound rules**
4. Add new rule:
    - **Type:** PostgreSQL
    - **Port:** 5432
    - **Source:** Select Elastic Beanstalk security group
    - **Description:** "Allow EB to connect to RDS"
5. Click **Save rules**

This allows your application to connect to the database.

---

## Verification

### Test Deployment

**Check Health Endpoint:**

```bash
curl http://library-api-env.us-east-1.elasticbeanstalk.com/actuator/health
```

Expected response: `{"status":"UP"}`

**Access Swagger UI:**
Open in browser:

```
http://library-api-env.us-east-1.elasticbeanstalk.com/swagger-ui/index.html
```

**Test API Endpoints:**

```bash
# Register a user
curl -X POST http://library-api-env.us-east-1.elasticbeanstalk.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1-555-0123"
  }'

# Login
curl -X POST http://library-api-env.us-east-1.elasticbeanstalk.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'

# Browse catalog (no auth required)
curl http://library-api-env.us-east-1.elasticbeanstalk.com/api/catalog/books
```

### Check Application Logs

**Elastic Beanstalk Console → Logs → Request Logs → Last 100 Lines**

Look for:

- Successful database connection: "HikariPool started successfully"
- Hibernate table creation logs
- Application startup confirmation

---

## Common Issues & Solutions

### Issue: Environment Health Degraded/Severe

**Check:**

- Application logs in Elastic Beanstalk console
- Verify `SERVER_PORT=5000` in environment variables
- Confirm all environment variables are set correctly
- Look for startup errors in logs

### Issue: Database Connection Failed

**Check:**

- `RDS_HOSTNAME` matches your RDS endpoint exactly (format: `java-capstone.xxxxx.us-east-1.rds.amazonaws.com`)
- RDS security group allows inbound traffic from EB security group on port 5432
- Both RDS and EB are in the same VPC
- Database credentials (`RDS_USERNAME`, `RDS_PASSWORD`) are correct
- RDS instance status is "Available"
- Database name is `librarydb` (matches RDS configuration)

### Issue: Application Won't Start

**Check:**

- All 8 environment variables are configured
- JAR file uploaded correctly
- Java version compatibility (Corretto 21)
- Review full stack trace in EB logs

### Issue: 404 on All Endpoints

**Check:**

- Application started successfully (check logs)
- `server.port=5000` configuration is active
- Controller mappings loaded (check logs for "Mapped" statements)
- Verify correct JAR file was uploaded

### Issue: JWT Authentication Not Working

**Check:**

- `JWT_SECRET` environment variable is set
- JWT secret is at least 256 bits (32 characters in base64)
- Token is being sent in Authorization header with "Bearer " prefix

---

## Deployment Checklist

- [ ] RDS PostgreSQL database created (`java-capstone`)
- [ ] Database status is "Available"
- [ ] RDS endpoint documented and saved
- [ ] Application JAR file built successfully
- [ ] Elastic Beanstalk environment created
- [ ] All 8 environment variables configured
- [ ] Security group allows EB → RDS communication on port 5432
- [ ] Environment health shows "Ok" (green)
- [ ] Health endpoint returns 200 OK
- [ ] Swagger UI accessible
- [ ] User registration works
- [ ] User login returns JWT token
- [ ] Catalog browsing works (no auth)
- [ ] Authenticated endpoints require token
- [ ] LIBRARIAN operations restricted correctly
- [ ] Database tables created automatically

---

## Additional Resources

- Spring Boot Configuration: https://docs.spring.io/spring-boot/reference/features/external-config.html
- AWS Elastic Beanstalk Java: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-se-platform.html
- AWS RDS PostgreSQL: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html
- Spring Security JWT: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
# Development Environment Setup

## Overview

This guide walks you through setting up your local development environment for the Library Management System API. You'll
learn how to configure your Spring Boot application for both local development (using H2 in-memory database) and
production deployment on AWS (using PostgreSQL RDS).

The provided starter project includes pre-configured files for database connectivity and deployment, so you can focus on
building the core functionality of your library system.

---

## Project Structure

```
api/
├── .idea/                              # IntelliJ IDEA configuration
├── .mvn/                               # Maven wrapper files
├── docs/                               # Project documentation
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.library/
│   │   │       ├── controllers/        # REST controllers (you will create)
│   │   │       ├── ApiApplication.java # Spring Boot main class
│   │   │       ├── config/            # Configuration classes (you will create)
│   │   │       ├── entity/            # JPA entities (you will create)
│   │   │       ├── repository/        # Spring Data repositories (you will create)
│   │   │       ├── service/           # Business logic (you will create)
│   │   │       ├── dto/               # Data transfer objects (you will create)
│   │   │       └── security/          # Security configuration (you will create)
│   │   └── resources/
│   │       ├── static/                # Static files
│   │       ├── templates/             # Templates (if needed)
│   │       ├── application.properties      # Base config (PROVIDED)
│   │       ├── application-dev.properties  # Local config (PROVIDED)
│   │       └── application-prod.properties # AWS config (PROVIDED)
│   └── test/
│       └── java/                      # Test classes (you will create)
├── target/                            # Compiled files (generated)
├── .gitattributes                     # Git attributes
├── .gitignore                         # Git ignore rules
├── mvnw                              # Maven wrapper (Unix)
├── mvnw.cmd                          # Maven wrapper (Windows)
├── pom.xml                           # Maven dependencies
└── README.md                         # This file
```

---

## Provided Configuration Files

You've been given configuration files that handle database connectivity and application settings for different
environments. Understanding these files will help you work effectively in both local development and production
environments.

### 1. application.properties (Base Configuration)

This is the main configuration file that sets common application properties and determines which profile to use.

**Key Settings:**

```properties
spring.application.name=library-management-system
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

The `spring.profiles.active` property determines which environment-specific configuration file Spring Boot will load. By
default, it uses the `dev` profile (local development). When deployed to AWS, this will automatically switch to
the `prod` profile.

### 2. application-dev.properties (Local Development)

This file contains all configuration settings for running the application on your local machine using an H2 in-memory
database. H2 is a lightweight, embedded database that's perfect for development—no installation or setup required!

**Database Configuration:**

```properties
spring.datasource.url=jdbc:h2:mem:librarydb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
```

H2 runs entirely in memory, meaning the database is created when your application starts and destroyed when it stops.
This gives you a clean slate every time you restart during development.

**H2 Console (Database Viewer):**

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

The H2 Console is a web-based interface that lets you view and query your database while the application is running.
Access it at `http://localhost:8080/h2-console`.

**JPA/Hibernate Settings:**

```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.properties.hibernate.format_sql=true
```

The `ddl-auto=create-drop` setting is perfect for development. Hibernate will:

1. Create all database tables when the application starts
2. Drop all tables when the application stops
3. Give you a fresh database on every restart

The `show-sql=true` setting displays all generated SQL queries in your console, helping you understand what's happening
behind the scenes.

**Server and Logging:**

```properties
server.port=8080
logging.level.com.library=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

The application runs on port 8080, and debug-level logging is enabled for detailed information during development.

### 3. application-prod.properties (AWS Production)

This file is specifically designed for deploying your application to AWS Elastic Beanstalk with an RDS PostgreSQL
database. Unlike the local configuration, it uses environment variables instead of hardcoded values for security.

**Environment Variables for Database:**

```properties
spring.datasource.url=jdbc:postgresql://${RDS_HOSTNAME:localhost}:${RDS_PORT:5432}/${RDS_DB_NAME:librarydb}
spring.datasource.username=${RDS_USERNAME:postgres}
spring.datasource.password=${RDS_PASSWORD:password}
spring.datasource.driver-class-name=org.postgresql.Driver
```

AWS Elastic Beanstalk injects these environment variables at runtime. You'll configure them through the AWS Console when
deploying (Milestone 6). This approach keeps sensitive credentials out of your codebase.

**Connection Pool Configuration:**

```properties
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

HikariCP is a high-performance JDBC connection pool that manages database connections efficiently in production. These
settings are optimized for AWS RDS.

**Production-Optimized JPA Settings:**

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Unlike development, production uses `ddl-auto=update` to preserve data between deployments, and SQL logging is disabled
for performance.

**Server Configuration:**

```properties
server.port=${SERVER_PORT:5000}
```

The server runs on port 5000 by default (Elastic Beanstalk requirement), but can be overridden via environment variable.

**Health Check Endpoints:**

```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
management.health.db.enabled=true
```

These endpoints allow AWS to monitor your application's health and database connectivity.

**Production Logging:**

```properties
logging.level.com.library=INFO
logging.level.org.hibernate.SQL=WARN
logging.level.root=WARN
```

Logging is minimized in production to reduce noise and improve performance.

**Required AWS Environment Variables:**

- `RDS_HOSTNAME` - Your RDS database endpoint (e.g., `library-db.xxxxx.us-east-1.rds.amazonaws.com`)
- `RDS_PORT` - Database port (default: `5432`)
- `RDS_DB_NAME` - Database name (e.g., `librarydb`)
- `RDS_USERNAME` - Database username
- `RDS_PASSWORD` - Database password
- `JWT_SECRET` - Production JWT secret (generate with `openssl rand -base64 32`)
- `SERVER_PORT` - Application port (default: `5000`)

---

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed on your machine:

- **Java 17 or 21**: The JDK (Java Development Kit) is required to compile and run Spring Boot applications. Check your
  version with `java -version`.
- **Maven**: Build tool for managing dependencies and building the project. The Maven wrapper (`mvnw`) is included in
  the project, so a separate Maven installation is optional.
- **IDE**: IntelliJ IDEA (recommended), Eclipse, or Visual Studio Code with Java extensions.

**Note**: Unlike many Spring Boot projects, you do NOT need Docker or PostgreSQL installed locally. The H2 database runs
entirely within your application!

### Step 1: Build the Project

Maven compiles your Java code, downloads dependencies, and packages everything into an executable JAR file. The first
build may take a few minutes as Maven downloads all required libraries.

```bash
# Navigate to your project directory
cd api

# Clean any previous builds and compile
./mvnw clean install

# On Windows, use:
mvnw.cmd clean install
```

The `clean` command removes old compiled files, and `install` compiles your code, runs tests, and installs the artifact
in your local Maven repository.

### Step 2: Run the Application

Spring Boot's Maven plugin makes it easy to run your application directly from source code without manually creating JAR
files.

```bash
# Run the application (uses dev profile by default)
./mvnw spring-boot:run

# On Windows:
mvnw.cmd spring-boot:run
```

Watch the console output as Spring Boot starts. You'll see:

1. Spring Boot banner
2. H2 database initialization
3. Hibernate generating DDL statements (creating tables)
4. Application startup completion with timing information

**The application will be accessible at:** `http://localhost:8080`

### Step 3: Access the H2 Console

One of the great features of H2 is its built-in web console that lets you view and query your database:

1. Open your browser and navigate to: `http://localhost:8080/h2-console`
2. Use these connection settings:
    - **JDBC URL**: `jdbc:h2:mem:librarydb`
    - **User Name**: `sa`
    - **Password**: (leave empty)
3. Click "Connect"

You'll see a database browser where you can:

- View all tables created by Hibernate
- Run SQL queries
- Inspect table structures and data
- Test your entity relationships

### Step 4: Verify Everything Works

Once the application starts, verify it's functioning correctly:

```bash
# Check the health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}
```

The health endpoint confirms the application is running and the database is accessible.

**What to Look For in Logs:**

```
✓ H2 console available at '/h2-console'
✓ Hibernate: drop table if exists users cascade
✓ Hibernate: create table users (...)
✓ Hibernate: create table books (...)
✓ Hibernate: create table reservations (...)
✓ Started ApiApplication in 3.456 seconds
```

These log messages confirm successful database initialization and table creation. If you don't see table creation logs,
check that your entities have proper `@Entity` annotations.

---

## Development Workflow

### Daily Development Process

Here's the typical workflow when working on this project:

**Morning Startup:**

```bash
# Navigate to project directory
cd api

# Run the application
./mvnw spring-boot:run

# Application is ready at http://localhost:8080
```

**During Development:**

- Make code changes in your IDE
- Save files (Spring Boot DevTools can auto-reload, if configured)
- Restart the application to see changes: Stop with `Ctrl+C`, run `./mvnw spring-boot:run` again
- Test endpoints using Postman or curl
- Check logs for errors or SQL queries
- Use H2 Console at `http://localhost:8080/h2-console` to inspect database

**End of Day:**

```bash
# Stop the application: Ctrl+C
# H2 data is automatically cleared (fresh start next time)
```

### Local Development Details

**Environment:**

- **Profile**: `dev` (default from `spring.profiles.active`)
- **Database**: H2 in-memory (no persistence)
- **Application Port**: `8080`
- **Schema Management**: Hibernate DDL create-drop (fresh database each run)
- **Logging**: Verbose (DEBUG level for your code)

**What Happens When You Run Locally:**

1. Spring Boot reads `application.properties` and loads `application-dev.properties`
2. H2 in-memory database is created
3. Hibernate scans entity classes
4. Creates all tables from scratch
5. Application starts and listens on port 8080
6. Health endpoint becomes available
7. H2 Console is accessible for database inspection
8. You can now test your endpoints

### AWS Production Deployment (Milestone 6)

When you're ready to deploy to AWS, you'll need to build a production JAR file and upload it to Elastic Beanstalk.

**Step 1: Build Production JAR**

```bash
# Build your application (skip tests for faster builds)
./mvnw clean package -DskipTests

# On Windows:
mvnw.cmd clean package -DskipTests
```

This creates a JAR file at `target/library-management-system-0.0.1-SNAPSHOT.jar` (or similar name based on
your `pom.xml`).

**Step 2: Upload to AWS Elastic Beanstalk**

1. Log into AWS Console and navigate to Elastic Beanstalk
2. Create or select your application environment
3. Choose "Upload and Deploy"
4. Configure the deployment:
    - **Version label**: `v1` (or any descriptive label)
    - **Source code origin**: Select "Local file"
    - **Choose file**: Select your JAR file from `target/` directory (
      e.g., `library-management-system-0.0.1-SNAPSHOT.jar`)
5. Click "Deploy"

**Step 3: Configure Environment Variables**

In the AWS Elastic Beanstalk Console:

1. Go to Configuration → Software
2. Add the following environment variables:
    - `SPRING_PROFILES_ACTIVE=prod`
    - `RDS_HOSTNAME=your-rds-endpoint.rds.amazonaws.com`
    - `RDS_PORT=5432`
    - `RDS_DB_NAME=librarydb`
    - `RDS_USERNAME=your-username`
    - `RDS_PASSWORD=your-password`
    - `JWT_SECRET=your-generated-secret`
    - `SERVER_PORT=5000`
3. Click "Apply"

**Production Environment:**

- **Profile**: `prod` (set by `SPRING_PROFILES_ACTIVE` environment variable)
- **Database**: AWS RDS PostgreSQL
- **Application Port**: `5000` (Elastic Beanstalk requirement)
- **Schema Management**: Hibernate DDL update (preserves data, creates tables on first deploy)
- **Logging**: Minimal (WARN level)

The key difference is that production reads `application-prod.properties` and expects all database credentials and JWT
secrets through environment variables rather than hardcoded values.

---

## Understanding Configuration

### Why Three Configuration Files?

Separating base, development, and production configurations follows the "Twelve-Factor App" methodology, which
recommends storing configuration in environment variables. This separation provides several benefits:

**Base Configuration (`application.properties`):**

- Sets application name
- Defines default profile
- Common settings across all environments

**Development (`application-dev.properties`):**

- **Convenience**: Uses H2 in-memory database with no setup
- **Debugging**: Verbose logging and SQL output help identify issues
- **Fresh Start**: Database resets on each run for consistent testing
- **Security**: Less critical since it's not exposed to the internet

**Production (`application-prod.properties`):**

- **Security**: Sensitive values come from environment variables, never committed to Git
- **Performance**: Reduced logging improves response times
- **Scalability**: AWS RDS handles database connection pooling and backups
- **Data Persistence**: Uses `update` mode to preserve data between deployments
- **Different Port**: Port 5000 aligns with Elastic Beanstalk expectations

### Profile Selection Mechanism

Spring Boot's profile system allows you to activate different configurations:

```bash
# Default: uses dev profile (application-dev.properties)
./mvnw spring-boot:run

# Explicitly set production profile (for testing prod config locally)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Or via environment variable
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

AWS Elastic Beanstalk automatically sets `SPRING_PROFILES_ACTIVE=prod` through its environment configuration, so your
application knows to use production settings when deployed.

### Configuration Comparison Table

| Setting                   | Local Development     | AWS Production         |
|---------------------------|-----------------------|------------------------|
| **Database**              | H2 in-memory          | AWS RDS PostgreSQL     |
| **Host**                  | in-memory             | RDS endpoint           |
| **Schema Mode**           | create-drop (fresh)   | update (persistent)    |
| **SQL Logging**           | Enabled (see queries) | Disabled (performance) |
| **Application Log Level** | DEBUG (verbose)       | WARN (errors only)     |
| **Server Port**           | 8080                  | 5000                   |
| **JWT Secret**            | Hardcoded string      | Environment variable   |
| **Credentials**           | Hardcoded             | Environment variables  |
| **Data Persistence**      | None (resets)         | Full persistence       |

This table highlights how the same application behaves differently based on which profile is active, optimizing for
either development speed or production efficiency.

---

## Common Issues and Solutions

### Issue: Port 8080 Already in Use

**Symptoms**: Application fails to start with an error like "Port 8080 is already in use"

**Cause**: Another application is using port 8080

**Solutions**:

```bash
# Option 1: Find and stop the process using port 8080
# On macOS/Linux:
lsof -i :8080
kill -9 <PID>

# On Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Option 2: Change the port in application-dev.properties
server.port=8081
```

### Issue: H2 Console Won't Connect

**Symptoms**: Can access H2 Console but connection fails

**Common Mistakes**:

- Wrong JDBC URL (must be exactly `jdbc:h2:mem:librarydb`)
- Trying to connect after application stopped (database destroyed)
- Wrong username (must be `sa`)

**Solution**:

1. Ensure application is running
2. Use exact connection settings:
    - JDBC URL: `jdbc:h2:mem:librarydb`
    - Username: `sa`
    - Password: (empty)
3. Click "Connect"

### Issue: Tables Aren't Created

**Symptoms**: Application starts but H2 Console shows no tables

**Cause**: Hibernate didn't generate DDL statements

**Diagnosis**:

```bash
# Check logs for Hibernate DDL statements
# Should see: "Hibernate: create table users (...)"
```

**Solutions**:

1. Verify entities have `@Entity` annotation
2. Check entities are in the correct package (Spring Boot scans from main class package downward)
3. Ensure `application-dev.properties` has `spring.jpa.hibernate.ddl-auto=create-drop`
4. Look for startup errors in logs indicating mapping problems
5. Restart application (H2 creates tables on each startup)

### Issue: Application Won't Start

**Symptoms**: Exception during startup, application exits

**Common Causes and Solutions**:

1. **Wrong Java version**:
   ```bash
   java -version  # Must be 17 or 21
   ```

2. **Missing dependencies**:
   ```bash
   ./mvnw clean install -U  # -U forces dependency update
   ```

3. **H2 dependency missing**: Check `pom.xml` includes:
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

Always read the stack trace carefully—Spring Boot error messages usually pinpoint the exact problem.

### Issue: Need to Reset Database

**Symptoms**: Database has incorrect data or you want a fresh start

**Solution**:

```bash
# Simply restart the application
# Stop: Ctrl+C
# Start: ./mvnw spring-boot:run

# H2 automatically creates a fresh database on each startup!
```

This is one of the major advantages of using H2 in development—you always get a clean database.

---

## Important Reminders

- **Never commit sensitive data** to version control (`.gitignore` is configured to exclude sensitive files)
- **H2 data is temporary**—all data is lost when you stop the application (perfect for development)
- **Hibernate creates tables automatically**—you don't write SQL DDL scripts
- **Use `application-dev.properties` for local work**—don't modify `application-prod.properties` until AWS deployment
- **Generate a secure JWT secret for production**: `openssl rand -base64 32` (never use the dev secret in production)
- **Read error messages carefully**—Spring Boot provides detailed stack traces that usually reveal the exact problem
- **Use H2 Console** at `http://localhost:8080/h2-console` to inspect your database during development

---

## Getting Help

If you encounter issues not covered in this guide:

1. **Check application logs**: Spring Boot prints detailed startup information
2. **Verify Java version**: `java -version` (must be 17 or 21)
3. **Use H2 Console**: Access `http://localhost:8080/h2-console` to inspect database
4. **Review Hibernate logs**: Look for "create table" statements in console output
5. **Consult milestone documentation**: Each milestone has specific troubleshooting guidance
6. **Check Spring Boot documentation**: [docs.spring.io](https://docs.spring.io/spring-boot/docs/current/reference/)

Good luck building your Library Management System!
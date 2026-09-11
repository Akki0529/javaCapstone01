# Milestone 6: Deployment & Production Readiness

**Goal:** Deploy application to cloud infrastructure with production database

**Related User Stories:** All (US-001 through US-011) - Production deployment

---

### ⏱️ Don't Leave Deployment to the Last Minute

Deployment is the biggest last-minute point-sink here. Deploy the walking skeleton early, or at least leave real time before the deadline: just don't let it become a final-hours scramble.

**Grade reality:** a deployed app at 70% beats a flawless localhost app at 100%. Last cohort, strong projects scored zero on deployment for leaving it to the final hours.

---

## Business Requirements

### Deployment Objectives
- Application must be publicly accessible via internet
- System must use production-grade database (not in-memory)
- All API endpoints must function in production environment
- Application must be secure and properly configured
- Database credentials and secrets must be protected

### Production Environment Requirements
- Publicly accessible API endpoint
- Persistent data storage
- Environment-specific configuration
- Secure credential management
- Health monitoring capability

---

## General Technical Requirements

**Deployment Platform:**
- AWS Elastic Beanstalk (or equivalent cloud platform)
- Java runtime environment (Java 17 or 21)
- Single instance deployment (free tier eligible)

**Database:**
- PostgreSQL 15.x on AWS RDS (or equivalent)
- Persistent storage
- Secure network configuration
- Automated backups

**Configuration:**
- Environment-based configuration management
- Secure storage of sensitive data (passwords, secrets, API keys)
- Port configuration for cloud platform
- Database connection parameters

**Security:**
- Restricted database access (not publicly accessible)
- Network security groups configured correctly
- Secure JWT secret generation and storage
- HTTPS support (recommended)

---

## Deliverables

### 1. Application Build
Prepare application for deployment:
- Build production-ready JAR file
- Verify build includes all dependencies
- Ensure configuration supports environment variables

### 2. Database Setup
Create production database:
- PostgreSQL database instance
- Initial database creation
- Secure credential generation
- Network configuration for application access

### 3. Application Deployment
Deploy application to cloud platform:
- Create application environment
- Upload application artifact
- Configure runtime environment
- Set up necessary IAM roles and permissions

### 4. Environment Configuration
Configure application environment:
- Set server port for platform requirements
- Configure database connection parameters
- Set JWT secret for token generation
- Enable production profile

### 5. Network Security
Configure secure network access:
- Set up security groups
- Allow application to connect to database
- Restrict database to private network
- Configure application accessibility

### 6. Verification
Verify deployment success:
- Confirm application health
- Test all API endpoints
- Verify database connectivity
- Check API documentation accessibility

### 7. README & Deployment Proof
Your README is graded. Beyond setup/run/test, include:
- **Design decisions** and any deliberate deviations from the spec.
- **What you found and fixed in the starter code**: undocumented means we can't tell you caught it.
- **Above and Beyond work attempted**, and why you chose it.
- **Proof it runs in prod**: live URL + screenshots (Swagger, a reserve → checkout → return, a green health check).

---

## Required Environment Configuration

Your application must be configured with:

**Application Settings:**
- Server port (cloud platform specific)
- Active profile (production)

**Database Connection:**
- Database hostname/endpoint
- Database port
- Database name
- Database username
- Database password

**Security:**
- JWT secret key (minimum 256 bits)

### ⚠️ Credential Safety: Non-Negotiable

Never commit secrets: **AWS keys, e.g.

Leaked AWS keys are scraped within minutes and billed for crypto-mining. Last cohort, submissions lost their *entire* deployment score after committing AWS creds to a public repo. Leak one? **Rotate immediately**: deleting the commit won't help; git history keeps it.


---

## Acceptance Criteria

- [ ] Application builds successfully as deployable artifact
- [ ] Production PostgreSQL database created and accessible
- [ ] Application deployed to cloud platform
- [ ] Environment health shows healthy/running status
- [ ] All environment variables configured correctly
- [ ] Network security allows application-to-database communication
- [ ] Network security restricts public database access
- [ ] Health check endpoint responds successfully
- [ ] API documentation (Swagger) accessible
- [ ] User registration works in production
- [ ] User login returns JWT token
- [ ] Catalog browsing works without authentication
- [ ] Authenticated endpoints require valid token
- [ ] Role-based authorization enforced (LIBRARIAN operations)
- [ ] Database schema created automatically
- [ ] All 11 API endpoints functional in production

---

## Deployment Verification Checklist

After deployment, verify:

### Basic Connectivity
- Application URL is accessible
- Health endpoint returns success
- API documentation loads

### Authentication Flow
- User can register
- User can login
- JWT token is returned
- Token works for authenticated endpoints

### Public Endpoints
- Catalog browsing works
- Book search and filtering work
- Book details retrieval works

### Protected Endpoints
- Profile endpoint requires authentication
- Reservation creation requires authentication
- Active reservations require authentication

### Authorization
- PATRON cannot access checkout endpoint (403)
- PATRON cannot access return endpoint (403)
- LIBRARIAN can access checkout endpoint
- LIBRARIAN can access return endpoint

### Data Persistence
- Created users persist after application restart
- Books remain in catalog
- Reservations persist correctly

---

## Troubleshooting Guidelines

If deployment fails or application doesn't work:

**Check Application Health:**
- Review application logs
- Verify all environment variables are set
- Confirm application started successfully

**Database Connection Issues:**
- Verify database endpoint is correct
- Check database credentials
- Confirm security groups allow connection
- Ensure database is running and accessible

**Application Errors:**
- Review startup logs for errors
- Verify Java version compatibility
- Check all required dependencies included
- Confirm port configuration matches platform requirements

**API Not Working:**
- Verify application started successfully
- Check endpoint mappings in logs
- Test with simple curl commands
- Verify authentication works

---

## Suggested Approach

1. Build and verify application artifact locally
2. Set up cloud database instance
3. Configure database security and credentials
4. Create cloud application environment
5. Upload application artifact
6. Configure all environment variables
7. Configure network security (security groups)
8. Deploy and monitor application startup
9. Verify health endpoint
10. Test all API functionality
11. Document deployment (URLs, credentials, configuration)

**Note:** You have flexibility in choosing cloud platform services and configuration approaches. Focus on achieving a working, secure, production deployment that meets all acceptance criteria.

---

## Resources

- Refer to `user-stories.md` for all functionality to verify in production
- Refer to `api-contracts.md` for endpoint testing
- Refer to `production-enviroment-setup.md` for production setup

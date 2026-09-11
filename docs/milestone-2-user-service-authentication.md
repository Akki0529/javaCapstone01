# Milestone 2: User Service & Authentication

**Goal:** Implement complete user authentication and authorization system

**Related User Stories:** US-001 (User Registration), US-002 (User Authentication), US-003 (View Profile)

---

## Business Requirements

### User Registration (US-001)
- Library patrons must be able to create accounts to access library services
- New users are assigned PATRON role by default
- New users have ACTIVE membership status by default
- Email addresses must be unique across all users
- Password must meet security requirements:
   - Minimum 8 characters
   - Must contain uppercase letter
   - Must contain lowercase letter
   - Must contain number
   - Must contain special character
- Phone number must be in valid format

### User Authentication (US-002)
- Users must be able to securely log in with email and password
- System issues JWT tokens upon successful authentication
- Tokens are valid for 24 hours (86400 seconds)
- Tokens use Bearer authentication scheme
- Failed login attempts should not reveal whether email exists

### User Profile (US-003)
- Authenticated users can view their complete profile information
- Profile includes statistics:
   - Active reservations count (status = RESERVED or CHECKED_OUT)
   - Borrowing history count (total number of reservations)

### Authorization
- Two role types: PATRON and LIBRARIAN
- Public endpoints (no authentication required):
   - POST /api/auth/register
   - POST /api/auth/login
   - GET /api/catalog/books
   - GET /api/catalog/books/{bookId}
- All other endpoints require authentication
- Some endpoints require LIBRARIAN role (checkout and return operations)

---

## General Technical Requirements

**Technology Stack:**
- Spring Security 6.x
- JWT for stateless authentication
- BCrypt for password hashing

**Security Requirements:**
- Passwords must be hashed (never stored in plain text)
- JWT tokens must include: userId, email, role in claims
- Token expiration: 86400 seconds (24 hours)
- Stateless session management (no server-side session storage)
- Bearer token format: `Authorization: Bearer {token}`

**API Documentation:**
- Interactive API documentation must be available
- Must support Bearer token authentication testing
- All endpoints documented with request/response examples

---

## Deliverables

### 1. User Registration & Profile Management
Implement registration endpoint that:
- Accepts user details (email, password, firstName, lastName, phoneNumber)
- Validates email uniqueness
- Enforces password security requirements
- Hashes passwords before storage
- Sets default role (PATRON) and status (ACTIVE)
- Returns appropriate success/error responses

Implement profile endpoint that:
- Returns authenticated user's profile
- Calculates and includes active reservations count
- Calculates and includes borrowing history count

### 2. JWT Authentication System
Implement authentication that:
- Validates user credentials
- Generates JWT tokens with required claims
- Sets appropriate token expiration
- Returns token with user profile information

### 3. Role-Based Access Control
Implement authorization that:
- Protects endpoints based on authentication requirements
- Enforces role-based access (PATRON vs LIBRARIAN)
- Returns appropriate HTTP status codes (401 for authentication, 403 for authorization)
- Allows public access to specified endpoints

### 4. API Documentation
Set up interactive API documentation that:
- Documents all authentication and user endpoints
- Provides request/response examples
- Supports testing with Bearer tokens
- Documents all error responses

---

## API Endpoints to Implement

Based on `api-contracts.md`, implement these endpoints:

### POST /api/auth/register
- **Access:** Public
- **Request:** email, password, firstName, lastName, phoneNumber
- **Success (201):** userId, email, firstName, lastName, role, membershipStatus, createdAt, message
- **Error (400):** Validation errors including duplicate email

### POST /api/auth/login
- **Access:** Public
- **Request:** email, password
- **Success (200):** accessToken, tokenType, expiresIn, user object
- **Error (401):** Invalid credentials

### GET /api/users/profile
- **Access:** Requires authentication
- **Success (200):** Complete user profile with activeReservations and borrowingHistory counts
- **Error (401):** Missing or invalid token

---

## Acceptance Criteria

- [ ] User can register with all required fields
- [ ] Registration validates email uniqueness (400 if duplicate)
- [ ] Registration validates password requirements (400 if invalid)
- [ ] Registration assigns PATRON role and ACTIVE status by default
- [ ] Registration returns 201 with user details and success message
- [ ] User can login with valid credentials
- [ ] Login returns 200 with JWT token (Bearer type, 86400 expiration)
- [ ] Login returns user profile information
- [ ] Invalid credentials return 401 error
- [ ] Profile endpoint requires valid JWT token
- [ ] Profile endpoint calculates activeReservations count correctly
- [ ] Profile endpoint calculates borrowingHistory count correctly
- [ ] Missing/invalid token returns 401 error
- [ ] Public endpoints (register, login, catalog) accessible without authentication
- [ ] Protected endpoints return 401 without authentication
- [ ] API documentation is accessible and functional
- [ ] Bearer token authentication works in API documentation

---

## Suggested Approach

1. Set up Spring Security configuration
2. Implement password hashing mechanism
3. Create registration endpoint with validation
4. Implement JWT token generation and validation
5. Create login endpoint
6. Implement authentication filter for protected endpoints
7. Create profile endpoint with statistics calculation
8. Configure role-based access control
9. Set up API documentation
10. Test all authentication flows

**Note:** You have flexibility in how you structure your security configuration, implement JWT handling, and organize your authentication logic. Focus on meeting the business requirements and API contract specifications.

---

## Resources

- Refer to `user-stories.md` for US-001, US-002, US-003 details
- Refer to `api-contracts.md` for exact request/response formats
- Spring Security documentation for implementation guidance
# Milestone 5: Testing & Quality Assurance

**Goal:** Achieve comprehensive test coverage for all 11 API endpoints

**Related User Stories:** All (US-001 through US-011)

---

## Business Requirements

### Testing Objectives
- Verify all API endpoints function according to specifications
- Ensure business rules are correctly implemented
- Validate security and authorization requirements
- Confirm error handling and edge cases
- Demonstrate system reliability and quality

### Quality Standards
- Minimum 80% code coverage overall
- All 11 endpoints must have automated tests
- All business rules validated through tests
- Security requirements verified
- Error responses tested and consistent

---

## General Technical Requirements

**Test Types Required:**
- Unit tests for isolated component testing
- Integration tests for end-to-end workflows
- API contract validation tests

**Test Coverage Areas:**
1. **Authentication & Authorization**
    - User registration and login flows
    - JWT token generation and validation
    - Role-based access control (PATRON vs LIBRARIAN)
    - Public vs protected endpoint access

2. **Catalog Operations**
    - Pagination and sorting
    - Search and filtering
    - Book details retrieval
    - Public access validation

3. **Reservation Lifecycle**
    - Reservation creation with business rule validation
    - Active reservations view
    - Checkout process (LIBRARIAN only)
    - Return process with late fee calculation
    - Borrowing history

4. **Business Rule Validation**
    - 5 reservation limit enforcement
    - 7-day reservation expiry
    - 14-day checkout period
    - $1/day late fee calculation
    - Available copies synchronization

5. **Error Handling**
    - Validation errors (400)
    - Authentication failures (401)
    - Authorization failures (403)
    - Resource not found (404)
    - Server errors (500)

**Performance Considerations:**
- Tests should run efficiently
- Test data should be isolated
- Tests should be repeatable and deterministic

---

## Deliverables

### 1. Unit Tests
Implement tests that verify individual components in isolation:
- Controller layer tests
- Service layer business logic tests
- Input validation tests
- Security configuration tests
- Error handling tests

### 2. Integration Tests
Implement tests that verify complete workflows:
- Authentication flow (register → login → access protected endpoint)
- Reservation lifecycle (reserve → checkout → return)
- Multi-step user workflows
- Authorization enforcement across endpoints
- Public endpoint accessibility

### 3. Test Spring Data Repositories using @DataJpaTest
`@DataJpaTest` loads only the JPA-related slice of the Spring context (repositories, entities, and the
`EntityManager`) rather than the full application - this makes repository tests fast and focused, and by default it
auto-configures an embedded database for the test, which lines up with this project already using H2 for local
development (see [Data Modeling guide](milestone-1-data-modeling-guide.md)). Use it to test each repository's
custom query methods directly against real JPA/Hibernate behavior, separate from any service-layer logic:

- **UserRepository**
    - Find a user by email (used during login and duplicate-registration checks)
    - Confirm the unique constraint on email is enforced at the database level
- **BookRepository**
    - Full-text style search across title and author for the `query` parameter
    - Filter by genre
    - Filter by exact ISBN match
    - Filter to only books where `availableCopies > 0` (the `availableOnly` parameter)
    - Combined filtering (query + genre + availableOnly together, matching the catalog endpoint's supported
      combinations)
    - Sorting by title, author, and publicationYear in both directions
    - Pagination metadata (page, size, totalElements, totalPages) returned correctly for a known dataset
    - Confirm the unique constraint on ISBN is enforced at the database level
- **ReservationRepository**
    - Find all active reservations (RESERVED or CHECKED_OUT) for a given user, and confirm the count used to
      enforce the 5-reservation limit is accurate
    - Find a user's complete borrowing history, paginated, including RETURNED and CANCELLED reservations
    - Find reservations by status (e.g., all RESERVED reservations past their `expiresAt`, if you implement
      expiry handling)
    - Confirm relationships load correctly - a `Reservation` should resolve its associated `Book` and `User`
      as expected (watch for N+1 query patterns here if you use `@ManyToOne` associations)
- **General repository test practices**
    - Use `TestEntityManager` (auto-configured alongside `@DataJpaTest`) to set up test data directly, rather than
      going through the repository under test, so you're not testing the repository using itself
    - Each test should start from a clean, known state - `@DataJpaTest` wraps each test in a transaction that
      rolls back automatically, so tests stay isolated and repeatable without manual cleanup
    - Test both the "happy path" (data exists, query returns expected results) and the empty-result case (no
      matches, returns an empty page/list rather than null or an error)

### 4. API Contract Tests
Implement tests that verify external API interface:
- Request/response structure validation
- HTTP status codes
- Response format consistency
- Error response structure
- Pagination structure

### 5. Business Rule Tests
Implement tests that verify business logic:
- Reservation limit (maximum 5 active)
- Available copies management
- Date calculations (expiry, due date)
- Late fee calculations
- Status transitions (RESERVED → CHECKED_OUT → RETURNED)

### 6. Edge Case Tests
Implement tests for boundary conditions:
- Empty results
- Exactly at limit (5 reservations)
- Zero available copies
- Invalid identifiers
- Missing required fields
- Expired tokens

---

## Test Scenarios to Cover

### Authentication Endpoints
- User registration with valid data
- Registration with duplicate email (400)
- Registration with invalid password (400)
- Login with valid credentials
- Login with invalid credentials (401)
- Access protected endpoint with valid token
- Access protected endpoint without token (401)
- Token expiration after 24 hours

### User Profile Endpoint
- Retrieve profile with valid authentication
- Profile includes correct activeReservations count
- Profile includes correct borrowingHistory count
- Access without authentication (401)

### Catalog Endpoints
- Browse books with default pagination
- Browse books with custom pagination and sorting
- Search books by title/author
- Filter by genre
- Filter by ISBN
- Filter by availability only
- Combined filters (query + genre + availableOnly)
- Retrieve specific book by ID
- Retrieve non-existent book (404)
- Public access (no authentication required)

### Reservation Endpoints
- Create reservation with available book
- Create reservation when at limit of 5 (400)
- Create reservation for unavailable book (400)
- View active reservations
- Checkout as LIBRARIAN
- Checkout as PATRON (403)
- Checkout non-RESERVED reservation (400)
- Return as LIBRARIAN (on time)
- Return as LIBRARIAN (late with fees)
- Return as PATRON (403)
- Return non-CHECKED_OUT reservation (400)
- View paginated borrowing history
- History includes wasLate flag

### Data Integrity Tests
- Available copies decrements on reservation
- Available copies increments on return
- Late days calculated correctly
- Late fee calculated at $1/day
- Expiry date set to 7 days from reservation
- Due date set to 14 days from checkout

---

## Acceptance Criteria

- [ ] Overall test coverage exceeds 80%
- [ ] All 11 API endpoints have automated tests
- [ ] All business rules validated (5 reservation limit, date calculations, late fees)
- [ ] Authentication flows tested (register, login, token validation)
- [ ] Authorization rules tested (PATRON vs LIBRARIAN access)
- [ ] All public endpoints accessible without authentication
- [ ] All protected endpoints require authentication
- [ ] Pagination tested on catalog and history endpoints
- [ ] Complete reservation lifecycle tested (create → checkout → return)
- [ ] Late fee calculation verified with multiple scenarios
- [ ] Error responses have consistent format
- [ ] All edge cases and boundary conditions tested
- [ ] Tests run successfully and repeatably
- [ ] No failing tests

---

## Suggested Approach

1. Start with unit tests for core business logic
2. Add integration tests for complete workflows
3. Implement authentication and authorization tests
4. Test all CRUD operations
5. Verify business rules (limits, dates, fees)
6. Test error handling and edge cases
7. Validate API contracts
8. Measure and verify code coverage
9. Ensure tests are isolated and repeatable

**Note:** You have flexibility in choosing testing frameworks, organizing test structure, and implementing test utilities. Focus on achieving comprehensive coverage of business requirements and API contracts.

---

## Resources

- Refer to `user-stories.md` for all business requirements to test
- Refer to `api-contracts.md` for API contract specifications
- Spring Boot Testing documentation for framework guidance
- JUnit and testing framework documentation

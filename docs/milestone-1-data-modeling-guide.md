# Milestone 1: Data Modeling

**Goal:** Create entity classes and establish database schema for the Library Management System

**Related User Stories:** Foundation for US-001, US-002, US-003, US-004, US-005, US-006, US-007, US-008, US-009, US-010, US-011

---

## Business Requirements

The Library Management System requires three core domain entities:

1. **User** - Represents library patrons and librarians with different access levels
2. **Book** - Represents items in the library catalog with inventory tracking
3. **Reservation** - Represents the borrowing lifecycle from reservation through return

### Key Business Rules:
- Users have roles (PATRON or LIBRARIAN) that determine system access
- Users can have membership statuses (ACTIVE or SUSPENDED)
- Users need to track when they became a member (memberSince)
- Books track total copies and available copies for inventory management
- Book availability status is derived from available copies (not stored separately)
- Reservations track the complete lifecycle: reserved → checked out → returned
- Reservations must capture dates/times for:
   - When the book was reserved (reservedAt)
   - When the reservation expires if not picked up (expiresAt - 7 days from reservation)
   - When the book was checked out (checkedOutAt)
   - When the book is due back (dueDate - 14 days from checkout)
   - When the book was actually returned (returnedAt)
- Late fees are calculated and stored when books are returned overdue
- Book condition must be recorded upon return
- All entities need audit timestamps (createdAt, updatedAt) for tracking

---

## General Technical Requirements

**Technology Stack:**
- Spring Boot 3.2+
- Java 17 or 21
- H2 for local development
- Spring Data JPA with Hibernate
- Maven build tool

**Database Schema:**
- Use UUID for all primary keys
- Implement proper foreign key relationships between entities
- Enable JPA auditing for automatic timestamp management (createdAt, updatedAt)
- Use Hibernate DDL auto-update for schema management in development
- Use appropriate date/time types for temporal fields

**Data Integrity:**
- Email addresses must be unique across users
- ISBN must be unique across books
- Passwords must be stored securely (hashed)
- All enum values should be stored as strings in the database
- All timestamps should be in ISO 8601 format (UTC)

---

## Deliverables

### 1. Entity Classes
Create JPA entity classes for User, Book, and Reservation with:
- Appropriate fields to support all user stories and API contracts
- Proper date/time fields for tracking reservation lifecycle
- Proper relationships between entities
- JPA annotations for database mapping
- Audit fields (createdAt, updatedAt)

### 2. Enum Types
Define enums to represent:
- User roles (PATRON, LIBRARIAN)
- Membership statuses (ACTIVE, SUSPENDED)
- Reservation statuses (RESERVED, CHECKED_OUT, RETURNED, CANCELLED)
- Book condition ratings (GOOD, FAIR, POOR, DAMAGED)

### 3. Repository Interfaces
Create Spring Data JPA repository interfaces for data access

---

## Required Data Fields

Based on the API contracts, your entities must support the following data:

### User Entity Fields:
- Unique identifier
- Email (unique)
- Password (hashed)
- First name and last name
- Phone number
- Role (PATRON or LIBRARIAN)
- Membership status (ACTIVE or SUSPENDED)
- Member since date
- Audit timestamps (created, updated)

### Book Entity Fields:
- Unique identifier
- ISBN (unique)
- Title
- Author
- Genre
- Publication year
- Description
- Publisher
- Page count
- Language
- Total copies
- Available copies
- Audit timestamps (created, updated)

### Reservation Entity Fields:
- Unique identifier
- Reference to book
- Reference to user
- Status (RESERVED, CHECKED_OUT, RETURNED, CANCELLED)
- Reserved at timestamp
- Expires at timestamp (for pickup deadline)
- Checked out at timestamp
- Due date timestamp
- Returned at timestamp
- Renewal count
- Late days (calculated)
- Late fee amount
- Book condition at return
- Notes field
- Audit timestamps (created, updated)

---

## Acceptance Criteria

- [ ] Spring Boot application starts successfully on port 8080
- [ ] Database schema is created automatically by Hibernate
- [ ] All three entities can be persisted to PostgreSQL
- [ ] Entity relationships are properly configured
- [ ] All required enum types are defined
- [ ] Repository interfaces are created
- [ ] JPA auditing automatically populates createdAt and updatedAt fields
- [ ] All date/time fields are properly typed and support ISO 8601 format
- [ ] Can connect to database and view created tables
- [ ] Application runs with Docker Compose PostgreSQL on port 5432

---

## Suggested Approach

1. Set up PostgreSQL using Docker Compose
2. Review the API contracts to understand the data structure requirements
3. Design your entity classes based on the user stories, business requirements, and API contracts
4. Define the necessary enums
5. Create repository interfaces
6. Verify schema creation by starting the application
7. Test basic CRUD operations through repositories

**Note:** You have flexibility in how you structure your entities, choose field names (as long as they align with API contracts), and implement relationships. Focus on supporting the business requirements outlined in the user stories and the external API interface defined in the API contracts.

---

## Resources

- Refer to `user-stories.md` for detailed functional requirements
- Refer to `api-contracts.md` for the external API structure and required data fields
- Refer to `dev-environment-setup.md` for local development setup

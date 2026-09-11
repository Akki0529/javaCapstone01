# Digital Library Management System API

## Business Context

### Overview

The Digital Library Management System is a modern backend API solution designed to digitize and streamline library operations for public and institutional libraries transitioning from manual record-keeping to digital platforms.

### Business Problem

Traditional libraries face several operational challenges:

- Manual tracking of book availability and reservations leads to errors and inefficiency
- Limited visibility into borrowing patterns and inventory usage
- Poor user experience with no self-service capabilities for browsing or reserving books
- Difficulty managing overdue books and calculating late fees
- Time-consuming checkout and return processes at the library desk

### Solution

Our Digital Library Management System provides:

- **Self-service portal** for users to browse, search, and reserve books online
- **Automated reservation management** with 7-day pickup windows
- **Real-time availability tracking** to reduce operational overhead
- **Librarian tools** for efficient checkout and return processing
- **Borrowing history** for patrons to track their reading activity
- **Scalable architecture** ready for cloud deployment

### Target Users

1. **Library Patrons**: Browse catalog, reserve books, view borrowing history
2. **Librarians**: Process checkouts and returns, manage reservations

---

## A Note on the Starter Code

This scaffold was inherited, not written for you, like most code you'll take over on a real engagement. It is **not production-ready as handed over.** It contains at least one security/configuration choice that a careful engineer would catch and fix before building on top of it.

Trust the voice in the back of your head that says *"this is a weird / unsafe / bad choice."* That instinct is one of the most valuable things you're developing here: inherited code should be read with a critical eye, not trusted by default.

**But** get the baseline requirements working first, *then* harden and optimise. Don't rabbit-hole on a clever fix before the core reserve → checkout → return flow runs end-to-end (as seen via the Swagger API). As Donald Knuth put it, *"premature optimization is the root of all evil."* Finishing the contract beats polishing a corner of it, and that's a grading reality too: a complete **Baseline Build** counts for more than a half-built **Above and Beyond** (see [Grading Tiers](#grading-tiers)).

---

## Getting Started

### Core Requirements Documents

**Review these foundational documents before implementation:**

1. **[User Stories](docs/user-stories.md)** - **START HERE**
   - 11 user stories defining all system functionality
   - Business requirements and acceptance criteria
   - Your primary requirements document

2. **[API Contracts](docs/api-contracts.md)** - **CRITICAL**
   - Complete external API interface specification
   - All 10 endpoint definitions with request/response formats
   - Defines the contract you must fulfill

3. **[Development Environment Setup](docs/dev-enviroment-setup.md)**
   - Initial project setup and local development configuration

### Implementation Approach

**Prioritize understanding requirements over implementation details:**

- User Stories define business requirements and desired outcomes
- API Contracts define the exact external interface
- Milestone documents provide technical guidance and acceptance criteria

You have flexibility in **HOW** you implement the solution, but must meet the requirements defined in User Stories and API Contracts.

---

## Project Structure

### Requirements Documentation

- **[User Stories](docs/user-stories.md)** - Business requirements
- **[API Contracts](docs/api-contracts.md)** - External API interface

### Implementation Guides (Milestones)

1. [Milestone 1: Data Modeling](docs/milestone-1-data-modeling-guide.md)
2. [Milestone 2: User Service & Authentication](docs/milestone-2-user-service-authentication.md)
3. [Milestone 3: Catalog Service](docs/milestone-3-catalog-service.md)
4. [Milestone 4: Reservation Service](docs/milestone-4-reservation-service-core-functionality.md)
5. [Milestone 5: Testing & Quality Assurance](docs/milestone-5-testing-quality-assurance.md)
6. [Milestone 6: Deployment & Production Readiness](docs/milestone-6-deployment-production-readiness.md)

### Environment Setup

- [Development Environment Setup](docs/dev-enviroment-setup.md)
- [Production Environment Setup](docs/production-enviroment-setup.md)

---

## API Endpoints (10 Total)

### Authentication & User Management (3)

- `POST /api/auth/register` - Create new user account
- `POST /api/auth/login` - Authenticate and receive JWT token
- `GET /api/users/profile` - View user profile with statistics

### Catalog Management (2)

- `GET /api/catalog/books` - Browse and search books with pagination
- `GET /api/catalog/books/{bookId}` - View detailed book information

### Reservation Management (5)

- `POST /api/reservations` - Reserve an available book
- `GET /api/reservations` - View active reservations
- `POST /api/reservations/{reservationId}/checkout` - Checkout book (Librarian only)
- `POST /api/reservations/{reservationId}/return` - Return book with late fee calculation (Librarian only)
- `GET /api/reservations/history` - View complete borrowing history

**See [API Contracts](docs/api-contracts.md) for complete specifications.**

---

## Technical Stack

### Required Technologies

- **Java**: 17 or 21 (LTS)
- **Spring Boot**: 3.2+
- **Spring Security**: 6.x with JWT authentication
- **Spring Data JPA**: Database access
- **PostgreSQL**: 15+ (via Docker locally, RDS in production)
- **Maven**: Build tool

### Additional Libraries

Choose appropriate libraries for:

- JWT token handling
- API documentation (e.g., SpringDoc OpenAPI)
- Testing frameworks
- Validation

### Deployment

- **AWS Elastic Beanstalk**: Application hosting
- **AWS RDS**: PostgreSQL database

---

## Grading Tiers

Your submission is assessed against three cumulative tiers. Reach for the next only once the previous one is solid.

| Tier | The question it answers | What it looks like |
| --- | --- | --- |
| **Baseline Build** | *Does it work?* | All 10 endpoints implemented to the contract; the app runs end-to-end (reserve → checkout → return); Swagger UI is live; JWT auth and role checks are in place; tests are present. This is the core bar. |
| **Production-Ready** | *Would this survive contact with a real client?* | Clean, considered, trustworthy work: the kind you'd be comfortable handing to a client, not just something that passes on your machine. |
| **Above and Beyond** | *Did you handle what the spec didn't spell out?* | Optional, harder work that goes past the brief. Deliberately challenging, entirely optional: attempt it only once Baseline Build is complete. |

<details>
<summary><strong>How the tiers work</strong></summary>

- **Baseline Build** is the floor: a complete, working build to the contract. Finish this first; a complete Baseline Build always counts for more than a half-finished attempt at the tier above it.
- **Production-Ready** is the standard of craft we're really looking for: clean, considered, trustworthy work.
- **Above and Beyond** is genuinely optional. You are **not** expected to attempt all of it in the time given: pick what interests you and finish it cleanly. Choosing *what* to attempt, and knowing what to leave alone, is itself part of what we're looking at.

Security runs through all three tiers. Revisit the **RESTful APIs & Security** lesson: the attack vectors it covers are exactly the kinds of failure modes we'll be probing for.
</details>

### Above and Beyond (optional)

Baseline Build comes first, always. What follows are only *ideas*: "follow your curiosity" is an instruction from Deloitte, not a checklist. **Whatever you attempt, write it up in your README.**

<details>
<summary><strong>A menu of ideas</strong></summary>

- **Testing**: one could perhaps explore behaviour-driven testing (e.g. Cucumber), among other approaches.
- **Security**: revisit the RESTful APIs & Security lesson, and go as deep as you dare.
- **Robustness**: under load, at the edges, when things don't go to plan.
- **Performance**: where will this strain as the catalogue grows?
- **Code craft**: clean abstractions, modern Java, static analysis.
- **Architecture**: split into services (Feign, gateway, Eureka). Biggest effort, do it last: it breaks the single-instance deployment, so keep it on a separate, undeployed branch and attempt it only after a successful normal deploy. Doable on full AWS, likely not in our sandbox, so you'd be on your own.
- **Your own instinct and curiosity**: spot something worth improving, justify it, build it, and document it in your README. Noticing what's worth doing is the real skill.

</details>

---

## Success Criteria

> Capstones are graded as **Does Not Meet**, **Partially Meets**, **Meets**, or **Exceeds Expectations**, with instructor feedback.

### User Story Compliance

- All 11 user stories fully implemented
- All acceptance criteria met
- All business rules enforced (5 reservation limit, 7-day expiry, 14-day checkout, $1/day late fees)

### API Contract Compliance

- All 10 endpoints implemented as specified
- Request/response formats match exactly
- HTTP status codes correct
- Error response format consistent
- Authentication and authorization working properly

### Technical Quality

- Minimum 80% test coverage
- All endpoints tested (unit and integration)
- Proper error handling (400, 401, 403, 404, 500)
- Security properly implemented (JWT, role-based access)
- Successfully deployed to cloud environment

### Functional Verification

- Complete reservation lifecycle works (reserve → checkout → return)
- Role-based access control enforced (PATRON vs LIBRARIAN)
- Real-time availability tracking works correctly
- Late fee calculation accurate
- Pagination and search functional

---

## Development Philosophy

### Requirements-Driven Development

1. Understand the requirements (User Stories and API Contracts)
2. Plan your implementation (data model, architecture)
3. Build to meet the contract
4. Verify completeness (test against acceptance criteria)

### Implementation Flexibility

You decide:

- Internal code organization and architecture
- Service layer design patterns
- Repository implementation approaches
- Validation strategies
- Testing frameworks
- Error handling mechanisms

### Non-Negotiable Constraints

You must adhere to:

- User Story requirements and acceptance criteria
- API Contract specifications
- Business rules (reservation limits, dates, fees)
- Technology stack (Spring Boot, PostgreSQL, JWT)
- Security requirements (authentication, authorization)

---

## Quick Start Guide

1. Read [User Stories](docs/user-stories.md) to understand what you're building
2. Study [API Contracts](docs/api-contracts.md) to understand the exact API interface
3. Set up your environment using [Development Environment Setup](docs/dev-enviroment-setup.md)
4. Follow the milestones for structured implementation guidance
5. Test against requirements to verify acceptance criteria
6. Deploy to production following Milestone 6 guidance

---

## Support & Resources

- **User Stories**: Business requirements and functionality definitions
- **API Contracts**: External API interface specifications
- **Milestone Guides**: Implementation guidance and acceptance criteria
- **Spring Boot Documentation**: Framework reference
- **PostgreSQL Documentation**: Database reference

---

**Remember**: User Stories and API Contracts define **WHAT** you must build. Milestone documents suggest **HOW** you might approach it, but you have flexibility in implementation as long as you meet the requirements.
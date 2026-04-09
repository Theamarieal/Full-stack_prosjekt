# Chekd — Digital Internal Control System

> **Kjekt. Enkelt. Ordentlig.**

A full-stack web application for digital internal control in restaurants and food-serving establishments. The system digitalises IK-Mat (food safety) and IK-Alkohol (alcohol compliance) by replacing manual paper-based routines with structured checklists, temperature logging, deviation management, and alcohol documentation.

This project was developed as a semester assignment for IDATT2105 Full-stack Application Development at NTNU, spring 2026. The solution is sponsored by the upcoming restaurant Everest Sushi & Fusion AS.

---

## Table of contents

- [Prerequisites](#prerequisites)
- [How to run the project](#how-to-run-the-project)
- [Test credentials](#test-credentials)
- [Test data](#test-data)
- [How to run the tests](#how-to-run-the-tests)
- [Useful URLs](#useful-urls)
- [Project structure](#project-structure)
- [Architecture overview](#architecture-overview)
- [Security](#security)
- [Prioritization](#prioritization)

---

## Prerequisites

| Tool | Required version |
|------|-----------------|
| Java | 17 or higher |
| Maven | 3.9 or higher (or use the included `./mvnw`) |
| Node.js | 20.19.0 or higher |
| npm | Included with Node.js |

No external database installation is required. The backend uses an **H2 in-memory database** that is created automatically on startup.

---

## How to run the project

### 1. Start the backend

From the **root** of the project:

```bash
./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**.

On first startup, `DataInitializer` automatically populates the database with an organization, three test users, four pieces of equipment, and a sample checklist. No manual database setup is needed.

### 2. Start the frontend

Open a **separate terminal**, navigate to the `frontend/` folder, and run:

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:5173**.

Open your browser and go to **http://localhost:5173** to use the application.

---

## Test credentials

All test users belong to the organization **Testrestaurant AS** and share the same password.

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@test.no` | `password123` |
| Manager | `manager@test.no` | `password123` |
| Employee | `employee@test.no` | `password123` |

**What each role can do:**

- **Employee** — fill in checklists, log temperatures, report deviations, log alcohol routines
- **Manager** — everything an employee can do, plus create and delete checklists, update deviation status, and view all data for the organization
- **Admin** — full access (same as manager in the current implementation)

---

## Test data

The following test data is created automatically by `DataInitializer` when the backend starts for the first time:

**Organization:** Testrestaurant AS

**Equipment (with temperature limits):**
- Kitchen Fridge — 0°C to 4°C
- Cold Storage Fridge — 0°C to 4°C
- Freezer Room — −25°C to −18°C
- Hot Holding Unit — 60°C to 90°C

**Checklist:** "Morningroutine Kitchen" (daily, IK-Mat module)
- Check temperature in fridge
- Turn on coffeemachine

To trigger a temperature deviation in the UI, log a temperature outside the equipment's limits — for example, 10°C for "Kitchen Fridge" (limit is 0–4°C).

---

## How to run the tests

From the project root:

```bash
./mvnw test
```

The test suite uses a separate H2 in-memory database configured in `src/test/resources/application.properties` and does not require any external setup.

**Tests included:**
- `JwtServiceTest` — unit tests for JWT token generation and validation
- `JwtAuthenticationFilterTest` — filter unit tests
- `SecurityIntegrationTest` — Spring Security integration tests
- `UserDetailsServiceTest` — user loading unit tests
- `AuthenticationControllerIT` — integration tests for login and registration
- `ChecklistControllerIT` — integration tests for checklist endpoints and role-based access
- `DeviationControllerIT` — integration tests for deviation reporting and status updates
- `TemperatureControllerIT` — integration tests for temperature logging and deviation detection

---

## Useful URLs

With the backend running on port 8080:

| Resource | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Swagger UI (API docs) | http://localhost:8080/swagger-ui/index.html |
| H2 Database console | http://localhost:8080/h2-console |

**H2 console login:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: *(leave blank)*

---

## Project structure

```
Full-stack_prosjekt/
├── src/                                   # Spring Boot backend
│   ├── main/java/ntnu/no/fs_v26/
│   │   ├── auth/                          # Login, registration, JWT response
│   │   ├── config/                        # DataInitializer, OpenAPI config
│   │   ├── controller/                    # REST controllers and request DTOs
│   │   ├── dto/                           # Response DTOs
│   │   ├── exception/                     # GlobalExceptionHandler (400 responses)
│   │   ├── model/                         # JPA entities
│   │   ├── repository/                    # Spring Data JPA repositories
│   │   ├── security/                      # JWT filter, SecurityConfig, RateLimitingService
│   │   └── service/                       # Business logic
│   ├── main/resources/
│   │   ├── application.properties         # App config (H2, JWT)
│   │   └── schema.sql                     # Database schema
│   └── test/                              # Integration and unit tests
├── frontend/                              # Vue 3 frontend
│   └── src/
│       ├── api/                           # Axios modules per feature
│       ├── components/                    # Reusable components (LoadingSpinner)
│       ├── router/                        # Vue Router with navigation guards
│       ├── stores/                        # Pinia auth store
│       └── views/                         # Vue pages
└── README.md
```

---

## Architecture overview

The application follows a standard three-tier architecture:

```
Vue 3 (port 5173)
      │
      │  HTTP requests with JWT in Authorization header
      │
      ▼
Spring Boot REST API (port 8080)
      │
      │  JPA / Spring Data
      │
      ▼
H2 in-memory database
```

**Backend:** Spring Boot 3 with Spring Security (stateless JWT sessions), Spring Data JPA, H2, and Springdoc OpenAPI for documentation.

**Frontend:** Vue 3 with Vite, Pinia for state management, Vue Router with authentication guards, and Axios with a request interceptor that attaches the JWT token automatically.

---

## Security

The following security measures have been implemented:

- **JWT authentication** — stateless tokens stored in `sessionStorage` on the frontend
- **Role-based access control** — `@PreAuthorize` on all sensitive endpoints; unauthenticated requests return 401, unauthorized requests return 403
- **Organization-based data isolation** — users can only access data belonging to their own organization; enforced in the service layer, not just the controller
- **CORS** — restricted to `http://localhost:5173` only
- **Rate limiting** — login endpoint limited to 10 attempts per minute per IP using Bucket4j
- **Input validation** — `@Valid`, `@NotBlank`, `@Email`, `@Size` on all request DTOs; a global `@RestControllerAdvice` returns structured 400 responses on validation failures
- **DTO pattern** — dedicated request DTOs prevent mass-assignment vulnerabilities; callers cannot set `organization`, `status`, or `reportedBy` through the request body

---

## Prioritization

Given the three-week timeframe and a team of two, we prioritized completing core functionality end-to-end over implementing every optional feature.

**Must-have features completed:**
- Full authentication flow (login, logout, role-based access)
- Digital checklists with completion tracking
- Temperature logging with automatic deviation detection
- Deviation reporting and status tracking (OPEN → IN_PROGRESS → RESOLVED)
- IK-Alkohol documentation (age control and serving hours)

**Should-have features completed:**
- Temperature history with filtering
- Dashboard with compliance status and colour indicators for both modules
- Checklist management for managers (create, delete)
- Alcohol routine overview with warning on missing registration

**Deliberately not implemented:**
Add later, maybe we have some time left over for implementation!!!


We chose to prioritize OWASP security measures, input validation, test coverage, and a working end-to-end experience over adding more features that would not be fully implemented.

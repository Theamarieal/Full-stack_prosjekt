# Checkd — Digital Internal Control System

> **Kjekt. Enkelt. Ordentlig.**

A full-stack web application for digital internal control in restaurants and food-serving establishments. The system digitalises IK-Mat (food safety) and IK-Alkohol (alcohol compliance) by replacing manual paper-based routines with structured checklists, temperature logging, deviation management, and alcohol documentation.

This project was developed as a semester assignment for IDATT2105 Full-stack Application Development at NTNU, spring 2026. The solution is sponsored by the upcoming restaurant Everest Sushi & Fusion AS.

---

## Group members

| Name | Student ID |
|------|------------|
| Ida Aspenes-Isaksen | 137433 |
| Regine Hjelmtveit | 560578 |
| Thea Marie Alver Lønvik | 542330 |


## Table of contents

- [Prerequisites](#prerequisites)
- [How to run the project](#how-to-run-the-project)
- [Test credentials](#test-credentials)
- [Test data](#test-data)
- [How to run the tests](#how-to-run-the-tests)
- [Useful URLs](#useful-urls)
- [Documentation](#documentation)
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

On first startup, `DataInitializer` automatically populates the database with two organizations, six test users, equipment, and sample checklists. No manual database setup is needed.

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


### Organization 1 — Testrestaurant AS

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@test.no` | `password123` |
| Manager | `manager@test.no` | `password123` |
| Employee | `employee@test.no` | `password123` |

### Organization 2 — Testrestaurant 2 AS

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@test2.no` | `password123` |
| Manager | `manager@test2.no` | `password123` |
| Employee | `employee@test2.no` | `password123` |

**What each role can do:**

- **Employee** — fill in checklists, log temperatures, report deviations, log alcohol routines, complete training materials
- **Manager** — everything an employee can do, plus create and delete checklists, update deviation status, create training content, and view all compliance data for the organization
- **Admin** — full access, including user management (create users, change roles, deactivate/delete accounts) and organization management

---

## Test data

The following test data is created automatically by `DataInitializer` when the backend starts for the first time:

**Organization 1 — Testrestaurant AS**

Equipment: Kitchen Fridge (0–4°C), Cold Storage Fridge (0–4°C), Freezer Room (−25–−18°C), Hot Holding Unit (60–90°C)

Checklist: "Morningroutine Kitchen" — daily, IK-Mat module
- Check temperature in fridge
- Turn on coffeemachine

**Organization 2 — Testrestaurant 2 AS**

Equipment: Kitchen Fridge (0–4°C), Freezer Room (−25–−18°C)

Checklist: "Evening Routine Bar" — daily

**Triggering deviations in the UI:**
- Temperature: log 10°C for "Kitchen Fridge" (limit is 0–4°C) — a deviation is automatically created
- Alcohol: register an age check with a guest under 18 and serviceDenied = false — a deviation is automatically created

**Note on data persistence:** The application uses an H2 in-memory database. All data except the seed data above is lost when the backend is restarted. In a production setup, this would be replaced with a persistent database (e.g. MySQL) by changing `application.properties` the code requires no changes.

**Alcohol**
Age verification for alcohol serving is automatically enforced in accordance with Norwegian alcohol regulations. Attempts to serve alcohol to underage individuals (e.g., under 18) are automatically registered as deviations. Additionally, alcohol-related actions can only be recorded within registered serving hours. The system restricts serving sessions to one start and one end per day to ensure compliance with legal requirements.

---

## How to run the tests

From the project root:

```bash
./mvnw verify
```

Using `verify` (not `test`) ensures that the JaCoCo coverage check also runs. The build will fail if instruction coverage falls below 50%.

The test suite uses a separate H2 in-memory database configured in `src/test/resources/application.properties`.
**Tests included:**
- `JwtServiceTest` — unit tests for JWT token generation and validation
- `JwtAuthenticationFilterTest` — unit tests for JWT request filtering
- `SecurityIntegrationTest` — integration tests for Spring Security configuration and protected endpoints
- `UserDetailsServiceTest` — unit tests for loading users from the database
- `AuthenticationControllerIT` — integration tests for registration and login endpoints
- `AuthenticationServiceTest` — unit tests for authentication logic
- `ChecklistControllerIT` — integration tests for checklist endpoints and role-based access control
- `ChecklistServiceTest` — unit tests for checklist business logic
- `DeviationControllerIT` — integration tests for deviation creation and status management
- `DeviationServiceTest` — unit tests for deviation service logic
- `TemperatureControllerIT` — integration tests for temperature logging and automatic deviation detection
- `TemperatureServiceTest` — unit tests for temperature service logic
- `MultiTenancyIT` — integration tests for organization-based data isolation across all controllers

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

## Documentation

All project documentation is located in the `/docs` folder:

| File | Description |
|------|-------------|
| `system-documentation.pdf` | System documentation — architecture, class diagrams, and setup instructions for further development |
| `JavaDoc.overview.pdf` | JavaDoc overview of all packages |
| `JavaDoc.All.classes.and.interfaces.pdf` | Full JavaDoc for all classes and interfaces |
| `JSDoc.pdf` | JSDoc documentation for the Vue 3 frontend |

Swagger UI (interactive API documentation) is available at `http://localhost:8080/swagger-ui/index.html` when the backend is running. All endpoints include descriptions, parameter explanations, and example responses.

---

## Project structure

---

## Project structure

```
Full-stack_prosjekt/
├── docs/                                  # System documentation (PDF)
├── src/                                   # Spring Boot backend
│   ├── main/java/ntnu/no/fs_v26/
│   │   ├── auth/                          # Login, registration, JWT response
│   │   ├── config/                        # DataInitializer, OpenAPI config
│   │   ├── controller/                    # REST controllers and request DTOs
│   │   ├── dto/                           # Response DTOs
│   │   ├── exception/                     # GlobalExceptionHandler (400/404/409)
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
│       ├── components/                    # Reusable components
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

## Security

The following security measures have been implemented:

- **JWT authentication** — stateless tokens stored in `sessionStorage` on the frontend
- **Role-based access control** — `@PreAuthorize` on all sensitive endpoints; unauthenticated requests return 403, unauthorized requests return 403
- **Organization-based data isolation** — users can only access data belonging to their own organization; enforced in the service layer, not just the controller
- **CORS** — restricted to `http://localhost:5173` only
- **Rate limiting** — login endpoint limited to 10 attempts per minute per IP using Bucket4j
- **Input validation** — `@Valid`, `@NotBlank`, `@Email`, `@Size` on all request DTOs; a global `@RestControllerAdvice` returns structured 400 responses with field-level error messages
- **DTO pattern** — dedicated request DTOs prevent mass-assignment vulnerabilities; callers cannot set `organization`, `status`, or `reportedBy` through the request body

---

## Prioritization 

Given the three-week timeframe and a team of three, we prioritized completing core functionality end-to-end over implementing every optional feature. <br><br>

### Must-have features completed

- Full authentication flow (login, logout, role-based access)
- Digital checklists with completion tracking
- Temperature logging (IK-Mat) with automatic deviation detection
- Deviation reporting and status tracking (OPEN → IN_PROGRESS → RESOLVED)
- IK-Alkohol documentation (age control and serving hours) with automatic deviation detection
- Training module with policy acknowledgement and interactive training materials (quizzes and practical training sign-off)
  - Managers can create training content and upload PDF documents
  - Employees can complete training and receive certifications upon passing quizzes
  - Managers have full overview of employee training progress


### Should-have features completed

- Temperature history with filtering
- Dashboard with compliance status and colour indicators for both modules
- Checklist management for managers (create, delete, paginated)
- Alcohol routine overview with warnings on missing registration
- Report generation with JSON export for managers
- Pagination on Deviations, Checklists, and Manage pages

### Technical decisions and trade-offs

Styling is scoped per component rather than using separate global stylesheets. This kept development fast but made the CSS less structured than ideal. We prioritized working functionality over perfect code organization in the presentation layer.

### Deliberately limited features

**Single organization on signup** — new users registering via the sign-up page are placed in the default organization (Testrestaurant AS). Additional organizations and their users are managed through the Admin panel. This is a deliberate simplification appropriate for a single-tenant demo context.

**In-memory database** — H2 is used for simplicity and zero-setup. Switching to MySQL or PostgreSQL requires only changing `application.properties`; no code changes are needed.


### **Future improvements**
- Persistent database for production use
- Extend pagination to additional views such as Temperature History and Alcohol Log
- Expand file upload support beyond PDF to include additional formats (e.g., images, videos)
- Enhance reporting with more export formats (e.g., CSV, PDF)
- Improve validation and user feedback across the application
- Enhanced analytics and statistics dashboard

- The system is designed to be easily extensible with additional functionality
  - Checklists can be further developed to automatically align with regulatory hygiene requirements (IK-Mat)
  - Additional analytics and statistics can be introduced to provide deeper insights into organizational performance
<br>

We chose to prioritize OWASP security measures, input validation, test coverage, and a working end-to-end experience over adding more features that would not be fully implemented.

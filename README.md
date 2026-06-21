# vibe-store

Backend for a multi-store retail management system. Handles employee lifecycle,
payroll with bonus calculations, sales tracking, and has a built-in AI assistant
powered by Spring AI + Gemini.

## What it does

The system is designed for companies that run multiple retail stores under
a warehouse structure. Main things it covers:

- Hiring employees, assigning them to stores/positions, tracking transfers
- Recording sales per employee and calculating monthly payroll
- A flexible bonus engine with fixed, percentage, and threshold-based strategies
- JWT-based auth with refresh token rotation and Google OAuth support
- An AI assistant that can answer questions from uploaded docs (RAG) or
  pull live data from the database using function calling

## Tech stack

- Java 21, Spring Boot 4
- Spring Security + JWT (jjwt) + Redis for token blacklisting
- Spring Data JPA / Hibernate, MySQL
- Spring AI with Google Gemini + local ONNX embeddings
- Bucket4j (rate limiting), Resilience4j (circuit breaker, retry)
- MapStruct, Lombok, Jakarta Validation
- Docker & Docker Compose
- Swagger / OpenAPI (springdoc)

## Auth & security

Authentication is stateless JWT. On login you get an access token (15min)
and a refresh token (7 days). Refresh tokens are stored in DB as SHA-256
hashes with device info and IP — supports rotation and reuse detection.
If a used refresh token is submitted again, all sessions for that user
get revoked.

Google OAuth is also supported — send the Google ID token to `/api/auth/google`
and the backend verifies it, creates/finds the user, and returns JWT tokens.

Role-based access: `ADMIN`, `MANAGER`, `EMPLOYEE`. Controllers are protected
with `@PreAuthorize` — admins can do everything, managers can see their own
store's data, employees can view/update their own profile.

Token blacklisting on logout via Redis with TTL matching the token's
remaining lifetime.

## Rate limiting

Three tiers, all in-memory with Bucket4j:

| Tier | Endpoints | Limit |
|------|-----------|-------|
| Auth | `/auth/login`, `/auth/google`, `/auth/refresh` | 5 req/min per IP |
| AI | `/api/ai/**` | 10 req/min per user |
| General | everything else | 60 req/min per IP |

## Fault tolerance

AI endpoints are wrapped with Resilience4j:

- **Circuit breaker** — opens after 50% failure rate over 10 calls, waits 10s
- **Retry** — up to 3 attempts with 1s delay
- **Rate limiter** — 20 calls/min server-side

All three have fallback methods that return a friendly error instead of 500.

## API overview

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/login` | public |
| POST | `/api/auth/google` | public |
| POST | `/api/auth/refresh` | public |
| POST | `/api/auth/logout` | authenticated |
| POST | `/api/auth/admin` | ADMIN |
| PUT | `/api/auth/role` | ADMIN |
| PUT | `/api/auth/password` | authenticated |
| GET | `/api/auth/sessions` | authenticated |
| DELETE | `/api/auth/sessions/{id}` | authenticated |
| DELETE | `/api/auth/sessions` | authenticated |

### Stores
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/stores` | ADMIN |
| GET | `/api/stores` | ADMIN |
| GET | `/api/stores/{id}` | ADMIN / store manager |
| DELETE | `/api/stores/{id}` | ADMIN |

### Employees
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/employees` | ADMIN |
| GET | `/api/employees` | ADMIN, MANAGER |
| GET | `/api/employees/{id}` | ADMIN / owner |
| PATCH | `/api/employees/{id}/profile` | ADMIN / owner |
| POST | `/api/employees/changeJobDetails` | ADMIN |
| POST | `/api/employees/positions` | ADMIN |
| GET | `/api/employees/positions` | ADMIN, MANAGER |
| GET | `/api/employees/positions/{id}` | ADMIN, MANAGER |

### Grades & payroll
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/grades` | ADMIN |
| GET | `/api/grades` | ADMIN, MANAGER |
| GET | `/api/grades/{id}` | ADMIN, MANAGER |
| POST | `/api/grades/{id}/rules` | ADMIN |
| POST | `/api/grades/assign` | ADMIN |
| POST | `/api/sales` | authenticated |
| POST | `/api/payroll/store/{storeId}/calculate` | ADMIN / store manager |
| POST | `/api/payroll/employee/{id}/calculate` | ADMIN |

### AI
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/ai/documents` | ADMIN |
| POST | `/api/ai/ask` | authenticated |
| GET | `/api/ai/ask/simple` | authenticated |
| POST | `/api/ai/ask/with-tools` | authenticated |

All list endpoints support pagination via `?page=0&size=20&sort=id,desc`.

## Running locally

You need Docker for MySQL and Redis:

```bash
docker compose up -d
```

Then create a `.env` file in the project root:

```
DB_URL=jdbc:mysql://localhost:3307/mydatabase
DB_USERNAME=root
DB_PASSWORD=yourpassword
GEMINI_API_KEY=your-gemini-key
GOOGLE_CLIENT_ID=your-google-client-id
JWT_SECRET=some-long-random-string-at-least-256-bits
```

Run the app:

```bash
./mvnw spring-boot:run
```

Swagger UI will be at `http://localhost:8080/swagger-ui.html`.

## Project structure

```
src/main/java/com/example/vibe_store/
├── config/          # AppConfig, AiConfig, ToolsConfig
├── controller/      # REST controllers + auth/
├── dto/             # Request/response records (auth, employee, grade, payroll, sale, store)
├── entity/          # JPA entities + employee/, grade/
├── enums/           # Role enum
├── exception/       # GlobalExceptionHandler, custom exceptions
├── filter/          # MdcFilter (trace ID)
├── mapper/          # MapStruct interfaces
├── repository/      # Spring Data JPA repos
├── security/        # JWT, filters, OAuth, RBAC
└── service/         # Business logic + impl/
```

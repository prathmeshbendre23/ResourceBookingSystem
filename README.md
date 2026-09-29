# Resource Booking System - Production Backend Assignment

A complete, production-grade RESTful Resource Booking System backend built with **Java 17+**, **Spring Boot 3.3.4**, **Spring Security**, **JWT (JSON Web Token)**, **Spring Data JPA**, **Hibernate**, **MySQL**, **Bean Validation**, and **OpenAPI/Swagger**.

---

## 1. Project Overview

The Resource Booking System manages organizational resources (such as meeting rooms, vehicles, and specialized equipment) and allows authenticated users to make reservations while strictly preventing scheduling overlaps and unauthorized access. The backend incorporates production-grade patterns: layered architecture, stateless JWT authentication, role-based access control (RBAC), fine-grained IDOR protection, JPA specifications for dynamic filtering, standardized error handling, and automated test coverage.

---

## 2. Features

- **Stateless JWT Authentication**: Secure login endpoint emitting signed JWTs containing user identity and role.
- **Strict Role-Based Access Control (RBAC)**:
  - `ADMIN`: Full CRUD on resources and reservations, can view all bookings, modify reservation statuses.
  - `USER`: Read resources, create reservations for themselves, view/update/cancel only their own reservations.
- **IDOR Protection & Server-Side Identity**:
  - User identity is **never** trusted from the request body. It is strictly extracted server-side from the authenticated JWT token.
  - Users cannot view, update, or delete reservations owned by other users.
- **Reservation Conflict & Overlap Detection**:
  - Real-time overlap detection preventing double bookings on the same resource during identical or intersecting time windows.
  - Cancelled bookings do not block new reservations.
  - Proper exclusion logic during reservation reschedule/update.
- **Monetary Precision**: All monetary attributes (`price` on resources and reservations) use `BigDecimal` with rounding and scale.
- **Dynamic Filtering, Pagination & Sorting**:
  - Filter reservations by `status`, `minPrice`, `maxPrice`.
  - Spring Data pagination with page index, page size, total elements, total pages, and last flag metadata.
  - Sorting on allowed fields (`id`, `startTime`, `endTime`, `price`, `status`, `createdAt`) with ascending/descending order.
- **Centralized Exception Handling**: Standardized RESTful JSON error representations for 400, 401, 403, 404, 409, and 500 status codes.
- **Automatic Data Seeding**: Pre-loaded default admin, user, and multiple sample resources upon startup.
- **Interactive Swagger / OpenAPI 3**: Integrated Swagger UI with BearerAuth JWT token header testing.
- **Automated Test Suite**: 35 unit and integration tests covering security, controllers, services, repositories, validations, and business logic.

---

## 3. Technology Stack

- **Language**: Java 17+ (tested with JDK 23)
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6.3, BCryptPasswordEncoder
- **Authentication**: JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Persistence**: Spring Data JPA, Hibernate 6.5
- **Database**: MySQL 8.x (runtime), H2 (in-memory test profile)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **API Documentation**: `springdoc-openapi-starter-webmvc-ui` 2.6.0
- **Build Tool**: Apache Maven 3.9.9 / Maven Wrapper (`mvnw`)
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Security Test, MockMvc

---

## 4. Project Structure

```
ResourceBookingSystem/
├── .mvn/wrapper/                      # Maven wrapper configuration
├── postman/
│   └── Resource_Booking_System.postman_collection.json # Postman collection
├── src/
│   ├── main/
│   │   ├── java/com/example/resourcebooking/
│   │   │   ├── ResourceBookingApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java        # DB seed runner
│   │   │   │   └── OpenApiConfig.java          # Swagger configuration
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java         # /auth/login
│   │   │   │   ├── ReservationController.java  # /api/reservations/**
│   │   │   │   └── ResourceController.java     # /api/resources/**
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── ReservationRequest.java
│   │   │   │   │   ├── ReservationUpdateRequest.java
│   │   │   │   │   └── ResourceRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ErrorResponse.java
│   │   │   │       ├── LoginResponse.java
│   │   │   │       ├── PagedResponse.java
│   │   │   │       ├── ReservationResponse.java
│   │   │   │       ├── ResourceResponse.java
│   │   │   │       └── UserSummaryResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Reservation.java
│   │   │   │   ├── ReservationStatus.java
│   │   │   │   ├── Resource.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidReservationException.java
│   │   │   │   ├── ReservationConflictException.java
│   │   │   │   ├── ReservationNotFoundException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UserNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── ReservationRepository.java
│   │   │   │   ├── ResourceRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomAccessDeniedHandler.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── ReservationService.java
│   │   │       ├── ResourceService.java
│   │   │       └── impl/
│   │   │           ├── AuthServiceImpl.java
│   │   │           ├── ReservationServiceImpl.java
│   │   │           └── ResourceServiceImpl.java
│   │   └── resources/
│   │       └── application.properties          # Production properties (MySQL)
│   └── test/
│       ├── java/com/example/resourcebooking/
│       │   ├── controller/
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── ReservationControllerTest.java
│       │   │   └── ResourceControllerTest.java
│       │   ├── repository/
│       │   │   └── UserRepositoryTest.java
│       │   ├── security/
│       │   │   └── JwtServiceTest.java
│       │   └── service/
│       │       ├── ReservationServiceTest.java
│       │       └── ResourceServiceTest.java
│       └── resources/
│           └── application-test.properties     # H2 test profile
├── ASSIGNMENT_CHECKLIST.md                     # Verification requirements checklist
├── mvnw                                       # Unix Maven wrapper
├── mvnw.cmd                                   # Windows Maven wrapper
├── pom.xml                                    # Maven POM configuration
└── README.md                                  # Documentation
```

---

## 5. Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher (Java 21/23 supported)
- **MySQL Database Server**: MySQL 8.0+
- **Maven**: Maven 3.9+ (or use the included `./mvnw` / `mvnw.cmd` wrapper)

---

## 6. MySQL Database Creation

Connect to your MySQL server (via CLI or MySQL Workbench) and execute:

```sql
CREATE DATABASE IF NOT EXISTS resource_booking_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Hibernate will automatically create and update the schema tables (`users`, `resources`, `reservations`) upon application startup via `spring.jpa.hibernate.ddl-auto=update`.

---

## 7. Environment Variables

The application is configured using 12-factor application principles with standard fallbacks:

| Variable Name | Description | Default Fallback |
|---|---|---|
| `DB_URL` | JDBC URL for MySQL | `jdbc:mysql://localhost:3306/resource_booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL Database Username | `root` |
| `DB_PASSWORD` | MySQL Database Password | `root` |
| `JWT_SECRET` | 256-bit Secret Key for signing JWTs | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION_MS`| JWT validity duration in milliseconds | `86400000` (24 hours) |

---

## 8. Configuration

### `application.properties` (Main Application)
```properties
spring.application.name=resource-booking-system
server.port=8080

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/resource_booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

jwt.secret=${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 9. How to Run

### Run All Unit and Integration Tests
```powershell
# Using the Maven wrapper on Windows:
.\mvnw.cmd test

# Or on Linux / macOS:
./mvnw test
```

### Build Executable JAR
```powershell
.\mvnw.cmd clean package
```

### Launch the Application
```powershell
# Directly with Maven:
.\mvnw.cmd spring-boot:run

# Or run the packaged JAR with custom environment variables:
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
java -jar target\resource-booking-system-1.0.0.jar
```

The application runs on `http://localhost:8080`.

---

## 10. Seed Users

The application automatically seeds default accounts and sample resources via `DataInitializer`:

| Role | Username | Password | Email | Notes |
|---|---|---|---|---|
| **ADMIN** | `admin` | `Admin@123` | `admin@example.com` | BCrypt encrypted. Full administrative permissions. |
| **USER** | `user` | `User@123` | `user@example.com` | BCrypt encrypted. Standard booking permissions. |

Sample resources created:
1. `Conference Room Alpha` (Room, $75.00/hr)
2. `Executive Shuttle Van` (Vehicle, $120.00/hr)
3. `4K Cinema Projector` (Equipment, $35.00/hr)
4. `Sound Recording Studio` (Room, $95.00/hr)
5. `Industrial 3D Printer` (Equipment, $45.00/hr)

---

## 11. API Endpoints

| HTTP Method | Endpoint | Required Role | Description |
|---|---|---|---|
| **POST** | `/auth/login` | *Public* | Authenticate user & receive JWT token |
| **GET** | `/api/resources` | `USER`, `ADMIN` | Retrieve all bookable resources |
| **GET** | `/api/resources/{id}` | `USER`, `ADMIN` | Retrieve specific resource by ID |
| **POST** | `/api/resources` | `ADMIN` | Create new resource |
| **PUT** | `/api/resources/{id}` | `ADMIN` | Update existing resource |
| **DELETE**| `/api/resources/{id}` | `ADMIN` | Delete resource by ID |
| **POST** | `/api/reservations` | `USER`, `ADMIN` | Create reservation (user identity from JWT) |
| **GET** | `/api/reservations` | `USER`, `ADMIN` | List reservations (role-scoped, filters, paging, sort) |
| **GET** | `/api/reservations/{id}` | `USER`, `ADMIN` | View reservation (USER scoped to own, ADMIN views all) |
| **PUT** | `/api/reservations/{id}` | `USER`, `ADMIN` | Update/cancel reservation (ownership enforced) |
| **DELETE**| `/api/reservations/{id}` | `USER`, `ADMIN` | Delete reservation (ownership enforced) |
| **GET** | `/swagger-ui.html` | *Public* | Swagger UI interactive documentation |
| **GET** | `/v3/api-docs` | *Public* | OpenAPI 3 JSON specification |

---

## 12. Authentication Instructions

1. Send a POST request to `/auth/login` with your credentials:
   ```json
   {
       "username": "admin",
       "password": "Admin@123"
   }
   ```
2. The server responds with an HTTP 200 containing your JWT:
   ```json
   {
       "token": "eyJhbGciOiJIUzI1NiJ9...",
       "username": "admin",
       "role": "ADMIN"
   }
   ```
3. Include the token in the `Authorization` header for all protected endpoints:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```

---

## 13. USER Role Permissions

- **Resources**:
  - `GET /api/resources`: Allowed (browse available resources).
  - `GET /api/resources/{id}`: Allowed.
  - `POST /api/resources`: **Forbidden (403)**.
  - `PUT /api/resources/{id}`: **Forbidden (403)**.
  - `DELETE /api/resources/{id}`: **Forbidden (403)**.
- **Reservations**:
  - `POST /api/reservations`: Allowed (creates reservation tied to their authenticated identity).
  - `GET /api/reservations`: Returns **only** the authenticated user's reservations.
  - `GET /api/reservations/{id}`: Returns 200 OK only if the reservation belongs to this user. If another user's ID is passed, returns **403 Forbidden** (IDOR prevention).
  - `PUT /api/reservations/{id}`: Allowed only on their own reservation (can change status to `CANCELLED` or reschedule). Attempting to edit someone else's reservation returns **403 Forbidden**.
  - `DELETE /api/reservations/{id}`: Can delete only their own reservation. Attempting to delete another user's reservation returns **403 Forbidden**.

---

## 14. ADMIN Role Permissions

- **Resources**: Full CRUD access.
- **Reservations**:
  - `GET /api/reservations`: Can view **all** reservations across all users.
  - `GET /api/reservations/{id}`: Can view any reservation.
  - `PUT /api/reservations/{id}`: Can update any reservation, including administrative status changes (`CONFIRMED`, `PENDING`, `CANCELLED`).
  - `DELETE /api/reservations/{id}`: Can delete any reservation.

---

## 15. Reservation Filtering

The `GET /api/reservations` endpoint supports the following optional query parameters:

- `status`: `PENDING`, `CONFIRMED`, or `CANCELLED`.
- `minPrice`: Minimum total reservation price (`BigDecimal`).
- `maxPrice`: Maximum total reservation price (`BigDecimal`).

### Example Filter Requests:
```http
GET /api/reservations?status=CONFIRMED
GET /api/reservations?minPrice=50&maxPrice=250
GET /api/reservations?status=CONFIRMED&minPrice=100&maxPrice=500
```
> For `USER`, filters apply strictly across their own reservations. For `ADMIN`, filters apply across all reservations in the system.

---

## 16. Pagination

Pagination is supported via Spring Data Pageable:

- `page`: 0-indexed page number (default: `0`). Must be $\ge 0$.
- `size`: Number of records per page (default: `10`). Must be $> 0$ and $\le 100$.

### Example Pagination Request:
```http
GET /api/reservations?page=0&size=5
```

### Paginated Response Structure:
```json
{
    "content": [ ... ],
    "page": 0,
    "size": 5,
    "totalElements": 18,
    "totalPages": 4,
    "last": false
}
```

---

## 17. Sorting

Sorting is supported through the `sort` query parameter:

- Format: `property(,asc|desc)`
- Default: `createdAt,desc`
- Allowed properties: `id`, `startTime`, `endTime`, `price`, `status`, `createdAt`.
- Passing an unallowed property returns an HTTP 400 with a clear error message.

### Example Sorting Requests:
```http
GET /api/reservations?sort=price,desc
GET /api/reservations?page=0&size=10&sort=startTime,asc
GET /api/reservations?status=CONFIRMED&sort=createdAt,desc
```

---

## 18. Swagger / OpenAPI URL

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html` (or `http://localhost:8080/swagger-ui.html`)
- **OpenAPI 3 JSON Spec**: `http://localhost:8080/v3/api-docs`

To authorize in Swagger UI:
1. Click the **Authorize** button (padlock icon) at the top right.
2. Enter the token obtained from `/auth/login` (without the `Bearer ` prefix).
3. Click **Authorize**. All protected endpoints will now send the Bearer header.

---

## 19. Sample Login Requests

### Login as Admin:
```http
POST /auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "Admin@123"
}
```

### Login as Regular User:
```http
POST /auth/login
Content-Type: application/json

{
    "username": "user",
    "password": "User@123"
}
```

---

## 20. Sample API Requests

### 1. Create Resource (ADMIN only)
```http
POST /api/resources
Authorization: Bearer <ADMIN_JWT>
Content-Type: application/json

{
    "name": "Design Sprint Studio",
    "description": "Collaborative workshop space with digital whiteboards",
    "type": "Room",
    "price": 80.00,
    "available": true
}
```

### 2. Create Reservation (User or Admin)
> Note: Notice that `userId` is **not** accepted in the request body. User identity is derived strictly from the JWT.
```http
POST /api/reservations
Authorization: Bearer <USER_JWT>
Content-Type: application/json

{
    "resourceId": 1,
    "startTime": "2026-09-10T10:00:00",
    "endTime": "2026-09-10T12:00:00"
}
```

### 3. Update Reservation Status (Admin or User Cancelling Own)
```http
PUT /api/reservations/1
Authorization: Bearer <USER_JWT>
Content-Type: application/json

{
    "status": "CANCELLED"
}
```

---

## 21. Sample Responses

### 1. Successful Login Response (200 OK)
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInN1YiI6InVzZXIiLCJpYXQiOjE3NTcyMDM4NDYsImV4cCI6MTc1NzI5MDI0Nn0...",
    "username": "user",
    "role": "USER"
}
```

### 2. Create Reservation Response (201 CREATED)
```json
{
    "id": 1,
    "resource": {
        "id": 1,
        "name": "Conference Room Alpha",
        "description": "Executive boardroom with 4K interactive display and video conferencing system",
        "type": "Room",
        "price": 75.00,
        "available": true,
        "createdAt": "2026-09-06T20:30:00"
    },
    "user": {
        "id": 2,
        "username": "user",
        "email": "user@example.com",
        "role": "USER"
    },
    "startTime": "2026-09-10T10:00:00",
    "endTime": "2026-09-10T12:00:00",
    "price": 150.00,
    "status": "CONFIRMED",
    "createdAt": "2026-09-06T22:00:00"
}
```

### 3. Overlap / Conflict Response (409 CONFLICT)
```json
{
    "timestamp": "2026-09-06T22:05:12.124567",
    "status": 409,
    "error": "Conflict",
    "message": "Resource 'Conference Room Alpha' is already reserved for the requested time interval",
    "path": "/api/reservations"
}
```

### 4. IDOR / Access Denied Response (403 FORBIDDEN)
```json
{
    "timestamp": "2026-09-06T22:06:45.981234",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied: Access denied: You are not authorized to view this reservation",
    "path": "/api/reservations/2"
}
```

---

## 22. Postman Testing Instructions

1. Open Postman.
2. Click **Import** and select `postman/Resource_Booking_System.postman_collection.json`.
3. The collection is pre-configured with collection variables:
   - `baseUrl`: `http://localhost:8080`
   - `admin_token`: Automatically populated upon calling `Login Admin`.
   - `user_token`: Automatically populated upon calling `Login User`.
4. Run `AUTH > Login Admin`:
   - Inspect the test script which automatically saves the token to `admin_token`.
5. Run `AUTH > Login User`:
   - Inspect the test script which automatically saves the token to `user_token`.
6. Run resource and reservation requests; Bearer tokens are automatically attached.

---

## 23. Error Handling

The system uses `@RestControllerAdvice` in `GlobalExceptionHandler` to produce uniform RESTful error responses:

```json
{
    "timestamp": "2026-09-06T22:00:00.123",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/resources",
    "validationErrors": {
        "name": "Resource name is required",
        "price": "Price must be positive"
    }
}
```

### Mapped HTTP Status Codes:
- `200 OK`: Successful read/update.
- `201 CREATED`: Successful resource or reservation creation.
- `204 NO CONTENT`: Successful deletion.
- `400 BAD REQUEST`: Bean validation failure, illegal argument, malformed JSON, start time after end time.
- `401 UNAUTHORIZED`: Invalid credentials, missing or expired JWT.
- `403 FORBIDDEN`: Insufficient role (e.g. USER creating resource) or IDOR violation (accessing another user's reservation).
- `404 NOT FOUND`: Target user, resource, or reservation does not exist.
- `409 CONFLICT`: Overlapping reservation on the same resource.
- `500 INTERNAL SERVER ERROR`: Unhandled server exceptions.

---

## 24. Security Considerations

1. **Passwords**: Stored exclusively as one-way cryptographic hashes using BCrypt (`BCryptPasswordEncoder`). Raw passwords never touch database rows or response DTOs.
2. **Identity from JWT (No IDOR on Creation)**: Reservation creation intentionally ignores any `userId` in JSON payloads. The system extracts identity directly from `SecurityContextHolder.getContext().getAuthentication()`.
3. **Data Isolation (IDOR on Read/Update/Delete)**: Users can only retrieve, view, cancel, or delete their own reservations. The service validates `reservation.getUser().getId().equals(currentUser.getId())` for all non-admin requests.
4. **Secret Management**: JWT secrets and database credentials support externalization via environment variables (`JWT_SECRET`, `DB_PASSWORD`, etc.) to prevent secret leakage into source control.
5. **Session Management**: Configured as `SessionCreationPolicy.STATELESS` with CSRF disabled for RESTful API security.

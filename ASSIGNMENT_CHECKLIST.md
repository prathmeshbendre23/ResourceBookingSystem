# ASSIGNMENT CHECKLIST - Resource Booking System

All assignment requirements have been verified against the codebase and test suites.

| Requirement ID | Requirement Category & Description | Status | Verification & Evidence |
|---|---|---|---|
| **1.1** | Java 17+ Baseline | **[PASS]** | Configured for Java 17 compatibility in `pom.xml`, built & tested with JDK 23. |
| **1.2** | Spring Boot 3.x | **[PASS]** | Spring Boot `3.3.4` parent configured. |
| **1.3** | Spring Web & REST | **[PASS]** | REST controllers implemented with `@RestController` and standard HTTP semantics. |
| **1.4** | Spring Data JPA & Hibernate | **[PASS]** | JPA Repositories and Hibernate 6.5 entity mappings with relationships. |
| **1.5** | Spring Security | **[PASS]** | Stateless security filter chain, method security enabled, RBAC rules. |
| **1.6** | JWT Authentication | **[PASS]** | JJWT 0.12.6 with HS256 HMAC signing, claims validation, and expiration. |
| **1.7** | MySQL Database Integration | **[PASS]** | MySQL driver configured with environment variable support & H2 test profile. |
| **1.8** | Jakarta Bean Validation | **[PASS]** | `@NotBlank`, `@NotNull`, `@Positive`, `@DecimalMin`, etc. on all DTOs. |
| **1.9** | Lombok Integration | **[PASS]** | `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor` utilized cleanly. |
| **1.10**| Swagger / OpenAPI Documentation | **[PASS]** | `springdoc-openapi-starter-webmvc-ui` 2.6.0 configured with BearerAuth JWT. |
| **1.11**| Maven Build System | **[PASS]** | Clean POM, Maven Wrapper (`mvnw`, `mvnw.cmd`) included; `mvn test` & `mvn package` pass. |
| **1.12**| JUnit 5 + Mockito Testing | **[PASS]** | 35 automated unit/integration tests with 0 failures, 0 errors, 0 skipped. |
| **2.1** | Layered Project Structure | **[PASS]** | Standard package structure: `controller`, `service`, `service/impl`, `repository`, `entity`, `dto`, `security`, `exception`, `config`. |
| **3.1** | Domain Model (Resources & Bookings) | **[PASS]** | Bookable items (Rooms, Vehicles, Equipment) and user reservations. |
| **3.2** | Role Definitions | **[PASS]** | `ADMIN` and `USER` roles defined in `Role` enum. |
| **4.1** | User Entity | **[PASS]** | `id`, `username` (unique), `email` (unique), `password`, `role`, `createdAt`. |
| **4.2** | Resource Entity | **[PASS]** | `id`, `name`, `description`, `type`, `price` (BigDecimal), `available`, `createdAt`. |
| **4.3** | Reservation Entity | **[PASS]** | `id`, `resource` (ManyToOne), `user` (ManyToOne), `startTime`, `endTime`, `price` (BigDecimal), `status`, `createdAt`. |
| **4.4** | Reservation Status Enum | **[PASS]** | `PENDING`, `CONFIRMED`, `CANCELLED` enum values. |
| **4.5** | Password Encryption (BCrypt) | **[PASS]** | `BCryptPasswordEncoder` used for hashing; verified in `UserRepositoryTest`. |
| **4.6** | BigDecimal Monetary Values | **[PASS]** | Resource and Reservation prices strictly use `BigDecimal` with 2 decimal places. |
| **5.1** | POST /auth/login Endpoint | **[PASS]** | Public endpoint returns `{ token, username, role }`. |
| **5.2** | JwtService & Claims Extraction | **[PASS]** | Generates tokens with subject and role claims; extracts and validates expiration. |
| **5.3** | JwtAuthenticationFilter | **[PASS]** | OncePerRequestFilter validates Bearer token and populates `SecurityContext`. |
| **5.4** | CustomUserDetailsService | **[PASS]** | Loads user by username and assigns `ROLE_ADMIN` / `ROLE_USER` authorities. |
| **6.1** | ADMIN Role Permissions | **[PASS]** | Full CRUD on resources, full CRUD on reservations, access to all bookings. |
| **6.2** | USER Role Permissions | **[PASS]** | Read-only resources, create reservations, read/cancel own reservations only. |
| **6.3** | Server-Side RBAC Enforcement | **[PASS]** | Enforced via `SecurityFilterChain` rules and `@PreAuthorize` annotations. |
| **7.1** | Resource CRUD Endpoints | **[PASS]** | `POST`, `GET`, `GET /{id}`, `PUT`, `DELETE /api/resources` implemented and tested. |
| **8.1** | Server-Side Identity from JWT | **[PASS]** | Identity extracted strictly from `Authentication.getName()`. No `userId` accepted from body. |
| **8.2** | Reservation Creation Validation | **[PASS]** | Validates availability, start < end time, and time not in past. |
| **8.3** | Price Calculation Logic | **[PASS]** | Hourly rate * duration with `BigDecimal` rounding. |
| **9.1** | Reservation Retrieval Scoping | **[PASS]** | `USER` sees only own reservations; `ADMIN` sees all reservations. |
| **9.2** | IDOR Protection on GET /{id} | **[PASS]** | Non-owners receive 403 Forbidden when accessing another user's reservation ID. |
| **10.1**| Reservation Status Management | **[PASS]** | `ADMIN` can update status to any state; `USER` can only cancel own booking. |
| **11.1**| Reservation Filtering | **[PASS]** | Optional filters: `status`, `minPrice`, `maxPrice` via JPA Specification. |
| **12.1**| Pagination Support | **[PASS]** | `page` and `size` parameters with metadata (`content`, `totalPages`, etc.). |
| **13.1**| Sorting Support | **[PASS]** | Validated sort parameter (e.g. `sort=price,desc`, `sort=createdAt,desc`). |
| **14.1**| Reservation Conflict / Overlap Detection | **[PASS]** | Overlap check: `existing.startTime < requested.endTime && existing.endTime > requested.startTime`. |
| **14.2**| Overlap Returns HTTP 409 CONFLICT | **[PASS]** | Throws `ReservationConflictException` mapped to 409 Conflict. |
| **14.3**| Cancelled Bookings Do Not Block | **[PASS]** | Overlap query explicitly filters only `PENDING` and `CONFIRMED` reservations. |
| **15.1**| Jakarta Bean Validation | **[PASS]** | DTOs annotated with validation constraints and verified via tests. |
| **16.1**| Centralized Exception Handling | **[PASS]** | `@RestControllerAdvice` in `GlobalExceptionHandler` with uniform JSON structure. |
| **17.1**| Database Configuration & Env Vars | **[PASS]** | Configured in `application.properties` with fallback support for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`. |
| **18.1**| Automated Seed Data | **[PASS]** | Default `admin` (`Admin@123`) and `user` (`User@123`) seeded automatically alongside 5 sample resources. |
| **19.1**| Swagger / OpenAPI Documentation | **[PASS]** | Configured at `/swagger-ui.html` and `/v3/api-docs` with JWT Bearer auth. |
| **20.1**| Request & Response DTOs | **[PASS]** | Clean separation of models; passwords never exposed in responses. |
| **21.1**| Security & Anti-IDOR Protections | **[PASS]** | Verified in automated tests (`ReservationControllerTest`, `ReservationServiceTest`). |
| **22.1**| HTTP Status Codes | **[PASS]** | Proper use of 200, 201, 204, 400, 401, 403, 404, 409, and 500. |
| **23.1**| Automated Test Suite | **[PASS]** | 35 passing tests in `AuthControllerTest`, `ResourceControllerTest`, `ReservationControllerTest`, `ReservationServiceTest`, `ResourceServiceTest`, `JwtServiceTest`, `UserRepositoryTest`. |
| **24.1**| Comprehensive README.md | **[PASS]** | Complete 24-section documentation created. |
| **25.1**| Postman Collection | **[PASS]** | Postman v2.1 collection exported with automated token capture tests. |
| **26.1**| Code Quality & Clean Architecture | **[PASS]** | Constructor injection, SLF4J logging, `@Transactional` boundaries, no raw print statements. |
| **27.1**| Final Verification & Compilation | **[PASS]** | Full Maven build (`mvn clean package`) produces runnable JAR with 0 errors. |

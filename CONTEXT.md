# CONTEXT: ClinicalRecordService

## 1. Project Overview

* **Domain:** Medical records and prescriptions management (Clinical History).
* **Role:** A core microservice within a larger healthcare system. It handles the complete lifecycle of a patient's clinical history.
* **Current State:** Green-field development (Empty repository).

## 2. Tech Stack & Infrastructure

* **Framework:** Spring Boot 3.x (Java 17 or 21).
* **Database:** MongoDB (NoSQL). Chosen for its flexibility handling diverse medical documents (exams, notes, prescriptions).
* **Testing:** JUnit 5, Mockito, and Testcontainers (for real DB integration testing).
* **Libraries:** Lombok (to reduce boilerplate), MapStruct (for DTO to Domain/Entity mapping).
* **Build Tool:** Maven or Gradle.

## 3. Architectural Pattern

* **Hexagonal Architecture (Ports & Adapters) / Clean Architecture:**
  * `Domain`: Core business models and rules. Absolutely NO framework dependencies.
  * `Application`: Use cases / Services implementing domain logic.
  * `Infrastructure`: Outbound adapters (MongoDB repositories, external API clients, Message Broker publishers).
  * `Presentation`: Inbound adapters (REST Controllers, Event Listeners).

## 4. Development Rules & Guidelines

* **Language:** All code, variables, classes, comments, and commit messages MUST be strictly in **English**.
* **TDD (Test-Driven Development):** Strict adherence. Write tests first (Red), implement code to pass (Green), then optimize (Refactor).
* **SOLID Principles:** High cohesion and low coupling. Apply the Single Responsibility Principle rigorously to classes and methods.
* **Naming Conventions:**
  * Use standard RESTful conventions for APIs (e.g., `GET /api/v1/clinical-records/{patientId}`).
  * Interfaces should be named by their capability (e.g., `ClinicalRecordRepository`). Avoid the `I` prefix (no `IClinicalRecordRepository`).
  * Impls should end with `Impl` or the technology used (e.g., `MongoClinicalRecordRepository`).
* **Immutability:** Favor `final` variables, immutable domain records/classes, and pure functions where possible.
* **Error Handling:** Use a global `@ControllerAdvice` to handle exceptions and return standardized RFC 7807 Problem Detail responses. Never expose stack traces to the client.

## 5. Immediate Next Steps (For the AI Agent)

1. Initialize the Hexagonal Architecture folder structure.
2. Define the core Domain model (`ClinicalRecord`, `Prescription`).
3. Begin the TDD cycle for the "Create Clinical Record" use case.

## 6. Observability & Health Monitoring

* **Spring Boot Actuator** is enabled and exposes the following endpoints:
  * `GET /actuator/health` — liveness/readiness probe for container orchestrators (Kubernetes, Docker Compose).
  * `GET /actuator/prometheus` — Prometheus-compatible metrics scrape endpoint.
* **Micrometer** (`micrometer-registry-prometheus`) is the metrics facade. All JVM, HTTP, and custom business metrics are exported in the Prometheus exposition format.
* Health check detail level is set to `always` to provide component-level status (MongoDB connectivity, disk space, etc.).
* Future integrations: alerting rules in Prometheus and dashboards in Grafana should be based on metrics exposed by this endpoint.

## 7. API Documentation

* **SpringDoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui` v2.x) generates interactive API documentation automatically from `@RestController` annotations and Bean Validation constraints.
* Swagger UI is available at `GET /swagger-ui.html` and the raw OpenAPI 3.0 spec at `GET /v3/api-docs` when the application is running.
* All REST controllers **must** be annotated with `@Tag`, and all endpoints with `@Operation` and `@ApiResponse` to produce meaningful documentation.
* The generated spec serves as the contract for consumers and for automated contract testing in CI/CD.

## 8. Code Coverage (JaCoCo)

* The **JaCoCo Maven Plugin** (`jacoco-maven-plugin`) is configured to run automatically as part of `mvn test` or `mvn verify`.
* The `prepare-agent` goal instruments bytecode before tests; the `report` goal runs in the `test` phase and generates HTML and XML reports under `target/site/jacoco/`.
* **Minimum coverage thresholds** (to be enforced via a future `check` goal): 80% line coverage and 70% branch coverage across all application layers.
* The XML report (`jacoco.xml`) is consumed by SonarQube / CI quality gates to prevent merging code that degrades coverage.
* Annotation-only classes (`@Entity`, `@Document`, Lombok-generated code) should be excluded from coverage rules once thresholds are enforced.

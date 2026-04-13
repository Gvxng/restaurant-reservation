# Migration Change Log

Course: 420-N45-LA Web Services and Distributed Computing  
Milestone: Project Milestone 1  
Project: Restaurant Reservation Microservices  
Date: April 12, 2026

## Selected Process Option

This project follows **Option 2** from the milestone instructions:

> Integrate the classes/interfaces from the Lab webservice into the microservices project, making needed changes as you go.

The final codebase was built by:

- bootstrapping Spring Boot service shells
- creating a Gradle multi-project setup
- moving and adapting Lab 1 domain code into the new services
- making manual refactoring changes so the monolith structure would work as independent microservices

This project was **not** implemented as a completely fresh greenfield system. It was migrated from the Lab-style layered application into a microservices landscape.

## Original Monolith to Microservices Mapping

The Lab 1 restaurant reservation domain was decomposed into the following bounded contexts and services:

- `reservation-service`
  Reason: owns restaurant reservation workflows and table/floor management that are tightly related in Milestone 1.
- `menu-service`
  Reason: owns menu and menu-item management independently.
- `loyalty-service`
  Reason: owns loyalty accounts and loyalty business rules independently.
- `api-gateway-service`
  Reason: exposes a single entry point and adds HATEOAS without putting HATEOAS in the low-level services.

### Service Split Decisions

- `booking` and `floor` were grouped inside `reservation-service`
  Reason: the milestone requires **three** low-level microservices, not four. Floor management was treated as part of the reservation bounded context rather than a separate microservice.
- `menu` remained its own service
  Reason: it has its own entities, repository layer, controller layer, and persistence concerns.
- `loyalty` remained its own service
  Reason: it has its own business rules, exceptions, and persistence concerns.

## Main Migration Changes

### 1. Multi-project Gradle structure

Created a root Gradle multi-project setup so all services can be built from one command.

Files involved:

- `settings.gradle`
- `build.gradle`
- root `gradlew`, `gradlew.bat`, `gradle/`

Why this change was made:

- the milestone requires a multi-project Gradle build
- the peer grading requires `./gradlew clean build` from the root

### 2. Spring Boot microservice shells

Created individual Spring Boot service projects for:

- `reservation-service`
- `menu-service`
- `loyalty-service`
- `api-gateway-service`

Files involved:

- `create-projets.bash`
- per-service `build.gradle`
- per-service `settings.gradle`
- per-service `gradlew`, `gradlew.bat`, `gradle/`, `.gitattributes`, `HELP.md`

Why this change was made:

- each microservice needs an independent Spring Boot application
- each service must still be buildable on its own
- the structure now resembles the course microservices landscape example

### 3. Moving layered code from the monolith into services

The Lab-style layered structure was preserved inside each low-level service:

- `presentationlayer`
- `businesslogiclayer`
- `dataaccesslayer`
- `datamappinglayer`
- `domain`

How the code was moved:

- reservation-related classes were moved into `reservation-service`
- floor/table classes were moved into `reservation-service`
- menu classes were moved into `menu-service`
- loyalty classes were moved into `loyalty-service`

Why this change was made:

- it reuses the Lab project structure instead of rewriting everything from scratch
- it minimizes package-level logic changes during migration
- it preserves the layered architecture expected in the course

### 4. Database separation by service

Each low-level microservice now owns its own database:

- `reservation-service` -> MySQL
- `menu-service` -> Postgres
- `loyalty-service` -> MySQL

Files involved:

- `docker-compose.yml`
- each service `src/main/resources/application.properties`

Why this change was made:

- the milestone requires one database per low-level microservice
- the milestone requires one MySQL service, one Postgres service, and one service using either MySQL or Postgres
- independent databases enforce microservice boundaries

### 5. Docker and Docker Compose landscape

Created a Docker Compose setup for the landscape, including:

- API gateway container
- 3 low-level microservice containers
- 3 database containers
- phpMyAdmin
- Adminer

Why this change was made:

- the milestone requires Docker containerization
- the milestone requires Docker Compose deployment
- the peer demo requires showing the landscape running

### 6. API Gateway with HATEOAS

Created `api-gateway-service` as the public entry point.

Gateway responsibilities:

- exposes public REST endpoints
- calls low-level microservices using `RestTemplate`
- adds HATEOAS links in gateway response DTOs

Why this change was made:

- low-level microservices must not implement HATEOAS in Milestone 1
- the instructions explicitly place HATEOAS in the API gateway
- the gateway provides a single public facade over the internal services

### 7. DTO flattening and gateway package cleanup

The gateway DTOs were flattened under:

- `com.example.restaurantreservation.apigateway.presentation.dto`

This replaced copied low-level package roots inside the gateway.

Why this change was made:

- to keep the gateway separate from the internal domain packages of other services
- to avoid turning the gateway into a distributed monolith
- to make the gateway package structure cleaner and easier to present

### 8. Sub-domain specific exceptions

Each low-level service includes at least one domain-specific exception:

- `reservation-service`
  - `TableAlreadyBookedException`
  - `DuplicateTableNumberException`
- `menu-service`
  - `InvalidMenuItemPriceException`
- `loyalty-service`
  - `DuplicateLoyaltyAccountException`
  - `NegativePointsBalanceException`

Why this change was made:

- the milestone requires at least one sub-domain specific exception per low-level microservice
- these exceptions are used directly in service logic and tested at controller level

### 9. Repository and controller integration tests

Added low-level microservice tests for:

- repository integration
- controller integration using `WebTestClient`
- positive paths
- negative paths
- exception-specific negative scenarios

Why this change was made:

- the milestone requires both repository-based and controller-based integration testing
- the milestone requires tests for the custom sub-domain exceptions
- the milestone requires high Jacoco coverage

### 10. Root monolith cleanup

After the migration, the old monolith root `src` folder was removed.

Also cleaned each microservice so it only contains packages that belong to that service.

Examples:

- `reservation-service` keeps `booking` and `floor`
- `menu-service` keeps `menu`
- `loyalty-service` keeps `loyalty`
- `api-gateway-service` keeps only gateway code and gateway DTOs

Why this change was made:

- to avoid confusion between the old monolith and the new microservices landscape
- to make the project structure easier to present during peer grading
- to make each service visually consistent with its bounded context

## Important Manual Refactoring Decisions

- Merged `floor` into `reservation-service`
  Reason: keeps the low-level service count at three while preserving the floor/table domain.
- Kept low-level services independent
  Reason: Milestone 1 says not to implement aggregates that require another microservice.
- Preserved layered package naming from the Lab project
  Reason: reduces migration risk and aligns with the course architecture style.
- Added the API gateway as a facade service
  Reason: centralizes HATEOAS and external access without coupling clients directly to low-level services.

## Files and Areas Changed During Migration

Key files and folders created or updated during the migration include:

- `settings.gradle`
- `build.gradle`
- `docker-compose.yml`
- `create-projets.bash`
- `diagrams/`
- `reservation-service/`
- `menu-service/`
- `loyalty-service/`
- `api-gateway-service/`

Common types of code changes:

- package renaming and import fixes
- controller extraction into service-specific applications
- repository isolation by service
- DTO updates
- exception handling updates
- application property changes for per-service ports and databases
- Dockerfile creation and Docker Compose wiring
- integration test migration and expansion

## Final Result

The Lab 1 monolith was migrated into a microservices architecture with:

- 3 independent low-level microservices
- 1 API gateway
- separate databases per low-level service
- root multi-project Gradle build
- Docker Compose landscape
- repository and controller integration tests
- HATEOAS implemented only in the gateway


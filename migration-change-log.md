# Migration Change Log

Course: 420-N45-LA Web Services and Distributed Computing  
Project: Restaurant Reservation Microservices  
Milestones: Project Milestone 1 and Project Milestone 2

## Migration Strategy

The project started from the restaurant reservation lab application and was migrated into a Spring Boot microservices landscape. The migration followed the course approach of keeping the existing domain concepts, moving them into bounded service folders, and then adding gateway, Docker, testing, and aggregator behavior required by the milestones.

The project is not a fresh greenfield rewrite. It is a structured migration from a Lab-style layered application into independently buildable microservices.

## Milestone 1 Migration

### Original Monolith To Low-level Services

The Lab 1 restaurant reservation domain was decomposed into these low-level services:

| Service | Ownership |
|---|---|
| `reservation-service` | Bookings, pre-orders, dining tables, floor/table availability rules |
| `menu-service` | Menu items, menu prices, availability, dietary/category data |
| `loyalty-service` | Loyalty accounts, tiers, points balances |
| `api-gateway-service` | Public REST facade and HATEOAS links |

`booking` and `floor` were grouped inside `reservation-service` because the milestone requires three low-level microservices. Floor management is treated as part of the reservation bounded context.

### Multi-project Gradle Setup

Created a root multi-project Gradle setup so the complete system can be built from the root:

```text
settings.gradle
build.gradle
gradlew
gradlew.bat
gradle/
```

Included projects:

```text
reservation-service
menu-service
loyalty-service
api-gateway-service
```

### Layered Package Migration

The Lab-style package structure was preserved inside each low-level service:

```text
presentationlayer
businesslogiclayer
dataaccesslayer
datamappinglayer
domain
exception
```

This kept the migrated code close to the course examples while still separating each bounded context into its own Spring Boot application.

### Database Separation

Each low-level microservice was given its own database:

| Service | Database |
|---|---|
| `reservation-service` | MySQL |
| `menu-service` | Postgres |
| `loyalty-service` | MySQL |

This enforces service ownership of data and avoids sharing tables across services.

### API Gateway

The `api-gateway-service` was introduced as the single public facade. In Milestone 1 it forwarded requests to the low-level services and added HATEOAS links in gateway responses.

Gateway DTOs were flattened under:

```text
com.example.restaurantreservation.apigateway.presentation.dto
```

This avoided copying complete low-level service packages into the gateway.

### Low-level Exceptions

Each low-level service includes subdomain-specific exceptions:

| Service | Custom Exceptions |
|---|---|
| `reservation-service` | `TableAlreadyBookedException`, `DuplicateTableNumberException` |
| `menu-service` | `InvalidMenuItemPriceException` |
| `loyalty-service` | `DuplicateLoyaltyAccountException`, `NegativePointsBalanceException` |

Global exception handlers were added to return appropriate HTTP status codes.

### Milestone 1 Test Migration

Low-level service tests were added for repository and controller integration paths. They include positive and negative cases, including custom exception scenarios.

## Milestone 2 Migration

### Added Reservation Aggregator Service

Added a new service:

```text
reservation-aggregator-service
```

It was added to the root Gradle build:

```text
include 'reservation-aggregator-service'
```

The service follows the required course package layout:

```text
presentationlayer
businesslogiclayer
domainclientlayer
dataaccesslayer
datamappinglayer
domain
exception
```

It also includes its own wrapper/support files so it can be recognized as an independently scaffolded Spring Boot project:

```text
.gradle/
gradle/
.gitattributes
.gitignore
gradlew
gradlew.bat
HELP.md
settings.gradle
```

The helper script for recreating the service shell is:

```text
create-reservation-aggregator-microservice.bash
```

### Aggregator Persistence

The aggregator persists reservation aggregate documents in MongoDB:

| Service | Database |
|---|---|
| `reservation-aggregator-service` | MongoDB |

Docker service:

```text
aggregator-db
```

Mongo database:

```text
reservation_aggregates
```

### Aggregator Endpoints

The aggregator exposes the required five aggregate endpoints:

| Method | Endpoint |
|---|---|
| GET | `/api/v1/reservations` |
| GET | `/api/v1/reservations/{aggregateId}` |
| POST | `/api/v1/reservations` |
| PUT | `/api/v1/reservations/{aggregateId}` |
| DELETE | `/api/v1/reservations/{aggregateId}` |

These endpoints are also exposed through the API Gateway at the same paths.

### Aggregator Orchestration

The aggregator coordinates all three low-level services:

| Low-level Service | Aggregator Use |
|---|---|
| `reservation-service` | Creates/updates/deletes bookings and pre-orders |
| `menu-service` | Reads menu item availability and price |
| `loyalty-service` | Reads loyalty account snapshots and awards points when appropriate |

The aggregate response combines:

- data from the request model
- booking/pre-order data from `reservation-service`
- menu item snapshots from `menu-service`
- loyalty snapshots from `loyalty-service`
- aggregate metadata persisted in MongoDB

### Aggregate Invariant

Milestone 2 aggregate invariant:

```text
A reservation pre-order can only be created or updated with available menu items,
and the pre-order total must be computed from menu-service prices instead of
being trusted from client input.
```

Implementation location:

```text
reservation-aggregator-service/src/main/java/com/example/restaurantreservation/aggregator/businesslogiclayer/ReservationAggregatorServiceImpl.java
```

The aggregator reads every requested menu item from `menu-service`, rejects unavailable menu items, and computes line totals using menu-service prices. The client request does not provide or control `totalAmount`.

### Aggregator DTOs

Request DTO:

```text
CreateReservationRequestDTO
```

Response DTO:

```text
ReservationAggregateResponseDTO
```

Supporting request DTO:

```text
PreOrderItemRequestDTO
```

### Aggregator Exceptions

The aggregator implements global exception handling and these exception types:

| Exception | HTTP Status |
|---|---|
| `NotFoundException` | 404 |
| `InvalidInputException` | 400 |
| `MenuItemUnavailableException` | 409 |
| `DownstreamServiceException` | Downstream status |

`MenuItemUnavailableException` is the aggregator-specific custom domain exception.

### Aggregator Domain Client Layer

The aggregator has a domain client layer using WebClient:

```text
reservation-aggregator-service/src/main/java/com/example/restaurantreservation/aggregator/domainclientlayer
```

Clients:

- `ReservationDomainClient`
- `MenuDomainClient`
- `LoyaltyDomainClient`

These clients handle downstream HTTP errors and service-unavailable cases.

### Low-level Loyalty Endpoints Added For Orchestration

Added endpoints needed by the aggregator:

| Method | Endpoint |
|---|---|
| GET | `/api/v1/loyalty-accounts/customer/{customerId}` |
| POST | `/api/v1/loyalty-accounts/customer/{customerId}/points` |

These allow the aggregator to read a loyalty account by customer ID and award points.

### API Gateway Domain Client Layer

The API Gateway was refactored to include an explicit domain client layer, as required by GI2:

```text
api-gateway-service/src/main/java/com/example/restaurantreservation/apigateway/domainclientlayer
```

Concrete gateway HTTP clients:

- `AggregatorGatewayServiceImpl`
- `ReservationGatewayServiceImpl`
- `MenuGatewayServiceImpl`
- `LoyaltyGatewayServiceImpl`

The gateway controllers still depend on interfaces in `businesslogic`, while the concrete HTTP forwarding implementations live in `domainclientlayer`.

### API Gateway Global Exception Handling

The gateway implements global exception handling in:

```text
api-gateway-service/src/main/java/com/example/restaurantreservation/apigateway/presentation/GatewayExceptionHandler.java
```

It handles:

- `HttpStatusCodeException` by forwarding the downstream status and body
- `ResourceAccessException` by returning HTTP `503 Service Unavailable`

### API Gateway HATEOAS

The API Gateway adds `_links` to gateway responses, including reservation aggregate responses. For `/api/v1/reservations`, links include:

- `self`
- `all-reservations`
- `booking`
- `table`
- `pre-order` when a pre-order exists

### Docker Changes

Default `docker-compose.yml` now contains the application landscape:

- `api-gateway-service`
- `reservation-aggregator-service`
- `reservation-service`
- `menu-service`
- `loyalty-service`
- `reservation-db`
- `menu-db`
- `loyalty-db`
- `aggregator-db`

Only the API Gateway publishes a host port in the default compose file:

```text
8080:8080
```

This satisfies the Milestone 2 requirement that only the API Gateway is externally accessible.

### Admin Docker Tools In The Main Compose File

Admin/browser tools were added to the main compose file so the rubric requirement of 13 containers with `docker-compose up` is satisfied:

```text
docker-compose.yml
```

The GUI/documentation tools are:

| Service | Host Port |
|---|---|
| Swagger UI | `8081` |
| phpMyAdmin | `8091` |
| pgAdmin | `8082` |
| Mongo Express | `8083` |

Run the full landscape with:

```bash
docker compose up --build
```

If using the older Compose CLI, run:

```bash
docker-compose up --build
```

This starts all 13 containers from one compose file. The low-level microservice API ports are not published; all API requests still go through the API Gateway on port `8080`. The other published ports are only for GUI evidence tools.

### Postman Collection

Added:

```text
postman/Restaurant-Reservation-Milestone2.postman_collection.json
```

The collection uses:

```text
baseUrl = http://localhost:8080
```

It includes low-level service endpoints, aggregator endpoints, and negative paths for exception handling/invariant demonstration.

### Bash System Integration Script

Added:

```text
testing_scripts/milestone2_system_tests.bash
```

The script sends all requests through:

```text
http://localhost:8080
```

It verifies:

- GET and POST for menu items
- GET by ID for the created menu item
- GET and POST for loyalty accounts
- GET by ID for the created loyalty account
- GET and POST for dining tables
- GET by ID for the created dining table
- GET ALL, GET by ID, POST, PUT, and DELETE for reservation aggregates
- a negative aggregate invariant path using an unavailable menu item

### Testing And Coverage

Milestone 2 testing now includes:

| Service | Test Types |
|---|---|
| `reservation-aggregator-service` | repository integration, controller integration, controller unit, service unit, domain client unit, coverage support |
| `api-gateway-service` | controller integration, controller unit, service/domain-client unit |
| `reservation-service` | repository integration, controller integration, service integration, coverage support |
| `menu-service` | repository integration, controller integration, service integration, coverage support |
| `loyalty-service` | repository integration, controller integration, service integration, coverage support |

Each microservice has JaCoCo verification configured for `90%` line coverage.

Recent local coverage results:

| Service | JaCoCo Line Coverage |
|---|---:|
| `api-gateway-service` | 96% |
| `reservation-aggregator-service` | 91% |
| `reservation-service` | 92% |
| `menu-service` | 95% |
| `loyalty-service` | 99% |

### Diagrams Updated

Updated diagrams are stored in:

```text
diagrams/
```

Files:

- `Restaurant_ddd.PUML`
- `Restaurant_ddd.png`
- `C4_Level1_context.PUML`
- `C4_Level1_context.png`
- `C4_Level2_container.PUML`
- `C4_Level2_container.png`

The diagrams now show:

- API Gateway as the only public application entry point
- three low-level microservices
- the reservation aggregator microservice
- MySQL, Postgres, and MongoDB persistence
- admin GUI tools for presentation evidence
- the aggregate invariant on the DDD model
- API Gateway calling all downstream services
- aggregator internally orchestrating reservation, menu, and loyalty services

## Final Current State

The current project contains:

- 3 low-level microservices
- 1 reservation aggregator microservice
- 1 API Gateway microservice
- 4 databases
- 4 admin/browser tools in the main Docker Compose file
- root multi-project Gradle build
- service-level JaCoCo coverage verification
- global exception handling in services, aggregator, and gateway
- API Gateway HATEOAS
- Postman collection for presentation
- bash system integration script for Milestone 2

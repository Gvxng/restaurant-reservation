# Restaurant Reservation Microservices

**Course:** 420-N45-LA Web Services and Distributed Computing, Winter 2026  
**Project:** Milestone 2 - Microservices-based Architecture  
**Team:** Yanis Achamou and Nathan Toko

This project implements a restaurant reservation system using Spring Boot microservices, an API Gateway, and an aggregator microservice. All application requests for the Milestone 2 demo must go through the API Gateway at `http://localhost:8080`.

## Current Architecture

| Service | Role | Database | Internal Responsibility |
|---|---|---|---|
| `api-gateway-service` | Public entry point | None | Exposes REST endpoints, adds HATEOAS links, and forwards requests through a domain client layer |
| `reservation-aggregator-service` | Core orchestrator | MongoDB | Creates and stores reservation aggregates by coordinating reservation, menu, and loyalty services |
| `reservation-service` | Low-level microservice | MySQL | Owns bookings, pre-orders, dining tables, and table availability rules |
| `menu-service` | Low-level microservice | Postgres | Owns menu items, menu prices, and menu availability |
| `loyalty-service` | Low-level microservice | MySQL | Owns customer loyalty accounts and loyalty points |

The root Gradle build includes all five services:

```text
reservation-service
menu-service
loyalty-service
reservation-aggregator-service
api-gateway-service
```

## Milestone 2 Aggregate

The new aggregate is exposed through the API Gateway at:

```text
/api/v1/reservations
```

The aggregator stores the aggregate document in MongoDB and coordinates all three low-level services:

- `reservation-service` for bookings, dining tables, and pre-orders
- `menu-service` for menu item price and availability
- `loyalty-service` for customer loyalty snapshots and points

Aggregate invariant:

```text
A reservation pre-order can only be created or updated with available menu items,
and the pre-order total must be computed from menu-service prices instead of
being trusted from client input.
```

The invariant is enforced in `reservation-aggregator-service` before creating or updating the downstream pre-order. If a requested menu item is unavailable, the aggregator throws `MenuItemUnavailableException` and returns HTTP `409 Conflict`.

## Docker

### Default Milestone Run

Use this command for the strict Milestone 2 setup:

```bash
docker compose up --build
```

If your machine uses the older Compose command, use `docker-compose up --build` instead.

Default exposed port:

| URL | Purpose |
|---|---|
| `http://localhost:8080` | API Gateway |

In the default `docker-compose.yml`, only the API Gateway publishes a host port. The low-level services, aggregator, and databases are reachable only inside the Docker network.

### Presentation/Admin Run

Use this command when you need the browser GUI tools for presentation evidence:

```bash
docker compose -f docker-compose.yml -f docker-compose.admin.yml up --build
```

Older Compose command:

```bash
docker-compose -f docker-compose.yml -f docker-compose.admin.yml up --build
```

Additional admin URLs:

| URL | Tool | Purpose |
|---|---|---|
| `http://localhost:8081` | Swagger UI | Browse API Gateway OpenAPI docs |
| `http://localhost:8091` | phpMyAdmin | Inspect MySQL databases |
| `http://localhost:8082` | pgAdmin | Inspect the Postgres menu database |
| `http://localhost:8083` | Mongo Express | Inspect the MongoDB aggregate database |

The admin overlay adds GUI containers only. Application HTTP requests should still be demonstrated through `http://localhost:8080`.

## Build And Test

Run the complete Gradle build from the project root:

```powershell
$env:GRADLE_OPTS='-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT'
.\gradlew.bat clean build --console=plain --no-daemon
```

The service `build.gradle` files are configured to display passed, skipped, and failed test events. JaCoCo verification is configured at `90%` line coverage for each microservice.

Recent local verification:

| Service | JaCoCo Line Coverage |
|---|---:|
| `api-gateway-service` | 96% |
| `reservation-aggregator-service` | 91% |
| `reservation-service` | 92% |
| `menu-service` | 95% |
| `loyalty-service` | 99% |

## System Integration Script

The Milestone 2 bash script is:

```bash
testing_scripts/milestone2_system_tests.bash
```

Syntax check:

```bash
bash -n testing_scripts/milestone2_system_tests.bash
```

Run it after Docker is up:

```bash
bash testing_scripts/milestone2_system_tests.bash
```

The script uses only `BASE_URL`, which defaults to:

```text
http://localhost:8080
```

It covers:

- GET and POST for `menu-service` through the API Gateway
- GET by ID for the created menu item through the API Gateway
- GET and POST for `loyalty-service` through the API Gateway
- GET by ID for the created loyalty account through the API Gateway
- GET and POST for `reservation-service` dining tables through the API Gateway
- GET by ID for the created dining table through the API Gateway
- all five aggregator endpoints: GET ALL, GET by ID, POST, PUT, DELETE
- a negative aggregate invariant path using an unavailable menu item

## Postman

The Postman collection is stored at:

```text
postman/Restaurant-Reservation-Milestone2.postman_collection.json
```

It uses this base variable:

```text
baseUrl = http://localhost:8080
```

Import the collection in Postman and run the requests through the API Gateway only.

## API Gateway Endpoints

### Reservation Aggregates

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/reservations` | Get all reservation aggregates |
| GET | `/api/v1/reservations/{aggregateId}` | Get one reservation aggregate |
| POST | `/api/v1/reservations` | Create a reservation aggregate |
| PUT | `/api/v1/reservations/{aggregateId}` | Update a reservation aggregate |
| DELETE | `/api/v1/reservations/{aggregateId}` | Delete a reservation aggregate |

### Reservation/Floor Low-level Service

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/bookings` | Get all bookings |
| GET | `/api/v1/bookings/{id}` | Get booking by ID |
| POST | `/api/v1/bookings` | Create booking |
| PUT | `/api/v1/bookings/{id}` | Update booking |
| DELETE | `/api/v1/bookings/{id}` | Delete booking |
| GET | `/api/v1/dining-tables` | Get all dining tables |
| GET | `/api/v1/dining-tables/{id}` | Get dining table by ID |
| POST | `/api/v1/dining-tables` | Create dining table |
| PUT | `/api/v1/dining-tables/{id}` | Update dining table |
| DELETE | `/api/v1/dining-tables/{id}` | Delete dining table |
| GET | `/api/v1/pre-orders/{id}` | Get pre-order by ID |
| POST | `/api/v1/pre-orders` | Create pre-order |
| PUT | `/api/v1/pre-orders/{id}` | Update pre-order |
| DELETE | `/api/v1/pre-orders/{id}` | Delete pre-order |

### Menu Low-level Service

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/menu-items` | Get all menu items |
| GET | `/api/v1/menu-items/{id}` | Get menu item by ID |
| POST | `/api/v1/menu-items` | Create menu item |
| PUT | `/api/v1/menu-items/{id}` | Update menu item |
| DELETE | `/api/v1/menu-items/{id}` | Delete menu item |

### Loyalty Low-level Service

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/loyalty-accounts` | Get all loyalty accounts |
| GET | `/api/v1/loyalty-accounts/{id}` | Get loyalty account by ID |
| GET | `/api/v1/loyalty-accounts/customer/{customerId}` | Get loyalty account by customer ID |
| POST | `/api/v1/loyalty-accounts` | Create loyalty account |
| POST | `/api/v1/loyalty-accounts/customer/{customerId}/points` | Add loyalty points for a customer |
| PUT | `/api/v1/loyalty-accounts/{id}` | Update loyalty account |
| DELETE | `/api/v1/loyalty-accounts/{id}` | Delete loyalty account |

## API Gateway Layers

The API Gateway has a domain client layer under:

```text
api-gateway-service/src/main/java/com/example/restaurantreservation/apigateway/domainclientlayer
```

That layer contains the concrete HTTP clients that call downstream services:

- `AggregatorGatewayServiceImpl`
- `ReservationGatewayServiceImpl`
- `MenuGatewayServiceImpl`
- `LoyaltyGatewayServiceImpl`

The gateway also has global exception handling in `GatewayExceptionHandler`. It forwards downstream HTTP errors with the original status code and maps unavailable downstream services to HTTP `503 Service Unavailable`.

## Aggregator DTOs

Request DTO:

```text
CreateReservationRequestDTO
```

Fields:

- `customerId`
- `tableId`
- `reservationDate`
- `timeSlotStart`
- `timeSlotEnd`
- `partySize`
- `status`
- `preOrderItems`

Response DTO:

```text
ReservationAggregateResponseDTO
```

Fields include:

- `aggregateId`
- `bookingId`
- `preOrderId`
- `customerId`
- `tableId`
- reservation time details
- `status`
- `totalAmount`
- `currency`
- `loyaltyPointsEarned`
- dining table snapshot
- loyalty account snapshot
- pre-order item snapshots
- timestamps

## Diagrams

Updated diagram sources and PNG exports are in:

```text
diagrams/
```

Included files:

- `Restaurant_ddd.PUML`
- `Restaurant_ddd.png`
- `C4_Level1_context.PUML`
- `C4_Level1_context.png`
- `C4_Level2_container.PUML`
- `C4_Level2_container.png`

These diagrams show the API Gateway, the three low-level services, the reservation aggregator, each database, and the aggregate invariant.

## Helper Scripts

The Spring Initializr script for recreating the aggregator service shell is:

```bash
create-reservation-aggregator-microservice.bash
```

It follows the same course style as the other service creation scripts and uses Spring Boot, Gradle, Java 17, Web, WebFlux, MongoDB, Validation, Actuator, and Lombok.

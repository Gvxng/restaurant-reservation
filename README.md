# Restaurant Table Reservation System
### DDD Full Implementation — Lab Part 3
**Team:** Yanis Achamou & Nathan Toko  
**Course:** 420-N45-LA Web Services and Distributed Computing — Winter 2026

---

## Architecture Overview

This project implements a Domain-Driven Design (DDD) RESTful web service for a Restaurant Table Reservation System, structured around **four bounded contexts**:

| Subdomain | Type | Aggregate Root(s) | Base Path |
|---|---|---|---|
| Table Reservation Management | **Core (Orchestrator)** | `TableBooking`, `PreOrder` | `/api/v1/bookings`, `/api/v1/pre-orders` |
| Menu Management | Supporting | `MenuItem` | `/api/v1/menu-items` |
| Floor Layout | Supporting | `DiningTable` | `/api/v1/dining-tables` |
| Customer Loyalty | Supporting | `LoyaltyAccount` | `/api/v1/loyalty-accounts` |

---

## Running with Docker

```bash
# Build and start all containers (MySQL + phpMyAdmin + App)
docker-compose up --build

# App:        http://localhost:8080
# phpMyAdmin: http://localhost:8081  (root / root)
```

All three containers must be healthy before the app starts. The `init.sql` script automatically creates the schema and seeds 10+ rows per table.

---

## API Endpoints

### Menu Management — `/api/v1/menu-items`
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/menu-items` | Get all menu items | 200 |
| GET | `/api/v1/menu-items/{id}` | Get menu item by ID | 200 / 404 |
| POST | `/api/v1/menu-items` | Create menu item | 201 / 400 / 409 |
| PUT | `/api/v1/menu-items/{id}` | Update menu item | 200 / 404 |
| DELETE | `/api/v1/menu-items/{id}` | Delete menu item | 204 / 404 |

### Floor Layout — `/api/v1/dining-tables`
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/dining-tables` | Get all tables | 200 |
| GET | `/api/v1/dining-tables/{id}` | Get table by ID | 200 / 404 |
| POST | `/api/v1/dining-tables` | Create table | 201 / 400 / 409 |
| PUT | `/api/v1/dining-tables/{id}` | Update table | 200 / 404 |
| DELETE | `/api/v1/dining-tables/{id}` | Delete table | 204 / 404 |

### Customer Loyalty — `/api/v1/loyalty-accounts`
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/loyalty-accounts` | Get all accounts | 200 |
| GET | `/api/v1/loyalty-accounts/{id}` | Get account by ID | 200 / 404 |
| POST | `/api/v1/loyalty-accounts` | Create account | 201 / 400 / 409 |
| PUT | `/api/v1/loyalty-accounts/{id}` | Update account | 200 / 404 |
| DELETE | `/api/v1/loyalty-accounts/{id}` | Delete account | 204 / 404 |

### Table Reservation (Core) — `/api/v1/bookings`
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/bookings` | Get all bookings (aggregated) | 200 |
| GET | `/api/v1/bookings/{id}` | Get booking by ID (aggregated) | 200 / 404 |
| POST | `/api/v1/bookings` | Create booking (orchestration) | 201 / 400 / 409 |
| PUT | `/api/v1/bookings/{id}` | Update booking | 200 / 404 |
| DELETE | `/api/v1/bookings/{id}` | Cancel booking | 204 / 404 |

### Pre-Orders — `/api/v1/pre-orders`
| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/api/v1/pre-orders/{id}` | Get pre-order by ID | 200 / 404 |
| POST | `/api/v1/pre-orders` | Create pre-order | 201 / 400 / 409 |
| PUT | `/api/v1/pre-orders/{id}` | Update pre-order | 200 / 404 |
| DELETE | `/api/v1/pre-orders/{id}` | Delete pre-order | 204 / 404 |

---

## Orchestration (R10)

`GET /api/v1/bookings/{id}` returns a **fully aggregated response** that pulls data from all three supporting subdomains:

```json
{
  "bookingId": 1,
  "customerId": 1,
  "tableId": 2,
  "reservationDate": "2026-03-10",
  "timeSlotStart": "18:00:00",
  "timeSlotEnd": "20:00:00",
  "partySize": 3,
  "status": "CONFIRMED",
  "table": {
    "tableId": 2,
    "tableNumber": "T02",
    "seatingCapacity": 4,
    "tableType": "INDOOR",
    "status": "RESERVED",
    "sectionName": "Main Dining Room"
  },
  "loyaltyAccount": {
    "accountId": 1,
    "customerId": 1,
    "pointsBalance": 1500,
    "tier": "SILVER"
  },
  "preOrder": {
    "preOrderId": 1,
    "totalAmount": 86.00,
    "status": "CONFIRMED",
    "items": ["..."]
  },
  "_links": {
    "self":         { "href": "/api/v1/bookings/1" },
    "all-bookings": { "href": "/api/v1/bookings" },
    "table":        { "href": "/api/v1/dining-tables/2" },
    "loyalty":      { "href": "/api/v1/loyalty-accounts/customer/1" },
    "pre-order":    { "href": "/api/v1/pre-orders/1" },
    "cancel":       { "href": "/api/v1/bookings/1", "method": "DELETE" }
  }
}
```

---

## Aggregate Invariants (R5)

| # | Invariant | Where Enforced |
|---|-----------|----------------|
| INV-1 | A table cannot be double-booked for the same date and overlapping time slot | `TableBookingRepository.existsOverlappingBooking()` |
| INV-2 | Party size must not exceed the table's seating capacity | `DiningTableService.assertReservable()` |
| INV-3 | A booking cannot be confirmed without a valid, available table | `DiningTableService.assertReservable()` |
| INV-4 | A PreOrder cannot be submitted for a CANCELLED or COMPLETED booking | `TableBookingService.createPreOrder()` |
| INV-5 | PreOrder total must equal sum of all line item amounts | `TableBookingService` total calculation |
| INV-6 | MenuItem price must be > 0 | `MenuItemService.create()` + DB CHECK constraint |
| INV-7 | Table seating capacity must be >= 1 | `DiningTableService` + DB CHECK constraint |
| INV-8 | Loyalty points balance cannot go below zero | `LoyaltyAccountService` + DB CHECK constraint |
| INV-9 | One loyalty account per customer | `LoyaltyAccountService` + DB UNIQUE constraint |
| INV-10 | Tables in MAINTENANCE cannot be reserved | `DiningTableService.assertReservable()` |

---

## Exception Handling (R11)

| Exception | HTTP Status |
|-----------|-------------|
| `ResourceNotFoundException` | 404 Not Found |
| `BusinessRuleViolationException` | 409 Conflict |
| `MethodArgumentNotValidException` | 400 Bad Request |
| `IllegalArgumentException` | 400 Bad Request |
| `Exception` (catch-all) | 500 Internal Server Error |

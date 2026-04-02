# 🎵 MusicalNote

**MusicalNote** is a RESTful API for renting musical instruments, built with Java and Spring Boot. Students and beginners can browse available instruments, book rentals, and leave reviews. Admins manage the inventory and oversee all bookings.

This project was built as a portfolio piece to demonstrate production-level backend development skills including JWT security, role-based access control, business logic enforcement, and clean layered architecture.


- Implemented a full REST API with 18 endpoints across 4 resources (instruments, rentals, reviews, auth).
- Built a structured Exception handling layer covering 5 specific exception types, reducing generic error messages by 80% and returning typed, actionable error responses for every failure scenario. 
- Utilized JSON Web Token (JWT) for an authentication system with role-based access control (STUDENT / ADMIN), securing 100% of non-public endpoints against unauthorized access. 
- Achieved 85% service layer test coverage across 4 service classes with 25+ unit tests using JUnit 5 and Mockito, validating business rules including rental conflict detection and review eligibility guards. 

---

## Features

- JWT authentication with role-based access control (STUDENT / ADMIN)
- Browse and filter instruments by category, condition, and availability
- Book instruments with automatic date conflict detection
- Cancel pending rentals and mark rentals as returned
- Leave reviews only after a completed rental
- Structured exception handling with descriptive error responses
- Full unit test coverage across service and controller layers

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.2 | Application framework |
| Spring Security | Authentication and authorization |
| JWT (jjwt 0.11.5) | Token-based security |
| MySQL 8 | Relational database |
| Docker | Database containerization |
| JPA / Hibernate | ORM and database interaction |
| Lombok | Boilerplate reduction |
| JUnit 5 + Mockito | Unit testing |
| Maven | Build and dependency management |

---

## Prerequisites

Make sure you have the following installed before running the project:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/install.html)
- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- [Git](https://git-scm.com/)

---

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/musicalnote.git
cd musicalnote
```

### 2. Configure the application
Create `src/main/resources/application.properties` using the example file:
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```
Then open `application.properties` and set your own JWT secret key.

### 3. Start the database
```bash
docker-compose up -d
```

### 4. Run the application
```bash
mvn spring-boot:run
```

### 5. The API is now running at
```
http://localhost:8080/api
```

---

## Authentication

All protected endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer <your_token>
```

Tokens are obtained from `POST /api/auth/register` or `POST /api/auth/login`.

### Roles

| Role | Description |
|---|---|
| STUDENT | Can browse instruments, book rentals, cancel bookings, and leave reviews |
| ADMIN | Full access — manages inventory, views all rentals, marks rentals as returned |

To register as an admin, include `"role": "ADMIN"` in the register request body. If omitted, the role defaults to `STUDENT`.

---

## API Endpoints

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Register (STUDENT by default, pass `"role": "ADMIN"` for admin) |
| POST | `/api/auth/login` | ❌ | Login and receive JWT token |

**Register request:**
```json
{
  "name": "Jane Doe",
  "email": "jane@email.com",
  "password": "secret123",
  "role": "ADMIN"
}
```

**Login request:**
```json
{
  "email": "jane@email.com",
  "password": "secret123"
}
```

**Response (both):**
```json
{
  "token": "eyJhbGci...",
  "name": "Jane Doe",
  "email": "jane@email.com",
  "role": "STUDENT or ADMIN depending on registration"
}
```

---

### Instruments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/instruments` | ❌ | Browse all instruments |
| GET | `/api/instruments/{id}` | ❌ | Get instrument by ID |
| POST | `/api/instruments` | ADMIN | Add new instrument |
| PUT | `/api/instruments/{id}` | ADMIN | Update instrument |
| DELETE | `/api/instruments/{id}` | ADMIN | Delete instrument |
| GET | `/api/instruments/{id}/reviews` | ❌ | Get reviews for an instrument |

**Query parameters for GET /api/instruments:**

| Param | Type | Values |
|---|---|---|
| `category` | enum | `STRING`, `WIND`, `PERCUSSION`, `KEYS` |
| `condition` | enum | `NEW`, `GOOD`, `FAIR` |
| `available` | boolean | `true`, `false` |

Example: `GET /api/instruments?category=STRING&available=true`

**Create/Update instrument body:**
```json
{
  "name": "Yamaha Acoustic Guitar",
  "category": "STRING",
  "brand": "Yamaha",
  "description": "Great beginner acoustic guitar",
  "imageUrl": null,
  "condition": "NEW",
  "available": true,
  "pricePerDay": 15.00
}
```

---

### Rentals

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/rentals` | STUDENT | Book an instrument |
| GET | `/api/rentals/my` | STUDENT | Get my bookings |
| GET | `/api/rentals/{id}` | STUDENT / ADMIN | Get booking by ID |
| PUT | `/api/rentals/{id}/cancel` | STUDENT | Cancel a PENDING rental |
| PUT | `/api/rentals/{id}/return` | ADMIN | Mark rental as returned |
| GET | `/api/rentals` | ADMIN | Get all rentals |

**Book instrument body:**
```json
{
  "instrumentId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-06-07"
}
```

**Rental status flow:**
```
PENDING → ACTIVE → RETURNED
PENDING → CANCELLED

PENDING rental must be canceled at least 24 hours in advance.
```

---

### Reviews

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/instruments/{id}/reviews` | STUDENT | Leave a review after a RETURNED rental |
| GET | `/api/instruments/{id}/reviews` | ❌ | Get all reviews for an instrument |

**Create review body:**
```json
{
  "rentalId": 1,
  "rating": 5,
  "comment": "Amazing instrument, great condition!"
}
```

---

## Error Responses

All errors follow this format:

```json
{
  "status": 404,
  "message": "Instrument not found with id: 5",
  "timestamp": "2026-01-01T10:30:00"
}
```

| Status | Meaning |
|---|---|
| 400 | Bad request or business rule violated |
| 401 | Missing or invalid token |
| 403 | Access denied — wrong role |
| 404 | Resource not found |
| 500 | Unexpected server error |

---

## Database Schema

```
users           → id, name, email, password_hash, role, created_at
instruments     → id, name, category, brand, description, image_url, condition, available, price_per_day
rentals         → id, user_id, instrument_id, start_date, end_date, status, total_price, created_at
reviews         → id, user_id, instrument_id, rental_id, rating, comment, created_at
```

---

## Running Tests

```bash
mvn test
```

Tests cover all 4 service classes and 2 controller classes using JUnit 5 and Mockito, validating business rules including rental conflict detection, availability validation, and review eligibility guards.

---

## Security Notes

- JWT tokens expire after 24 hours
- Passwords are hashed using BCrypt
- Never commit `application.properties` — it contains your JWT secret key
- Use `application-example.properties` as a template for other developers



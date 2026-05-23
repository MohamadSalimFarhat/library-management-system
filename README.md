# Library Management System

A RESTful API built with **Spring Boot 3** for managing library books, patrons, and borrowing records. Includes JWT authentication, AOP logging, Spring Cache, and full transaction management.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** + **JWT (JJWT 0.12)**
- **Spring Data JPA** + **H2 (in-memory)**
- **Spring AOP** (logging aspect)
- **Spring Cache** (simple in-memory)
- **Lombok**
- **JUnit 5 + Mockito** (testing)

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run the application

```bash
git clone https://github.com/MohamadSalimFarhat/library-management-system.git
cd library-management-system
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

Two default users are created on startup:

| Username    | Password   | Role      |
|-------------|------------|-----------|
| `admin`     | `admin123` | ADMIN     |
| `librarian` | `lib123`   | LIBRARIAN |

---

## Authentication

All `/api/**` endpoints (except `/api/auth/**`) require a valid **JWT Bearer token**.

### 1. Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "librarian",
  "password": "lib123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "librarian",
  "role": "LIBRARIAN"
}
```

### 2. Register a new user

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "role": "LIBRARIAN"
}
```

### 3. Use the token

Add the header to all subsequent requests:

```
Authorization: Bearer <your-token>
```

---

## API Endpoints

### Books

| Method | Endpoint            | Description              |
|--------|---------------------|--------------------------|
| GET    | `/api/books`        | Get all books            |
| GET    | `/api/books/{id}`   | Get book by ID           |
| POST   | `/api/books`        | Add a new book           |
| PUT    | `/api/books/{id}`   | Update a book            |
| DELETE | `/api/books/{id}`   | Delete a book            |

**Book JSON:**
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "publicationYear": 2008,
  "isbn": "9780132350884"
}
```

---

### Patrons

| Method | Endpoint              | Description              |
|--------|-----------------------|--------------------------|
| GET    | `/api/patrons`        | Get all patrons          |
| GET    | `/api/patrons/{id}`   | Get patron by ID         |
| POST   | `/api/patrons`        | Add a new patron         |
| PUT    | `/api/patrons/{id}`   | Update a patron          |
| DELETE | `/api/patrons/{id}`   | Delete a patron          |

**Patron JSON:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "+9611234567"
}
```

---

### Borrowing

| Method | Endpoint                                   | Description              |
|--------|--------------------------------------------|--------------------------|
| POST   | `/api/borrow/{bookId}/patron/{patronId}`   | Borrow a book            |
| PUT    | `/api/return/{bookId}/patron/{patronId}`   | Return a book            |

---

## Example Usage (curl)

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"librarian","password":"lib123"}' | jq -r '.token')

# 2. Add a book
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","publicationYear":2008,"isbn":"9780132350884"}'

# 3. Add a patron
curl -X POST http://localhost:8080/api/patrons \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Smith","email":"jane@example.com","phoneNumber":"+9611234567"}'

# 4. Borrow book (bookId=1, patronId=1)
curl -X POST http://localhost:8080/api/borrow/1/patron/1 \
  -H "Authorization: Bearer $TOKEN"

# 5. Return book
curl -X PUT http://localhost:8080/api/return/1/patron/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## H2 Database Console

While running, access the H2 web console at:

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:librarydb
Username: sa
Password: (leave empty)
```

---

## Running Tests

```bash
mvn test
```

Tests include:

- `BookServiceTest` — unit tests for all CRUD operations
- `PatronServiceTest` — unit tests for patron management
- `BorrowingServiceTest` — unit tests for borrow/return logic
- `BookControllerTest` — integration tests for HTTP layer with Spring Security

---

## Features Implemented

| Feature                  | Status |
|--------------------------|--------|
| Book CRUD endpoints      | ✅      |
| Patron CRUD endpoints    | ✅      |
| Borrow / Return endpoints | ✅     |
| Input validation         | ✅      |
| Error handling (global)  | ✅      |
| JWT Authentication       | ✅      |
| AOP Logging Aspect       | ✅      |
| Spring Caching           | ✅      |
| Transaction Management   | ✅      |
| Unit + Integration Tests | ✅      |

---

## Project Structure

```
src/
├── main/java/com/maids/lms/
│   ├── aspect/          # LoggingAspect (AOP)
│   ├── config/          # SecurityConfig, DataInitializer
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JwtService, JwtAuthenticationFilter
│   └── service/         # Business logic
└── test/java/com/maids/lms/
    ├── controller/      # Controller integration tests
    └── service/         # Service unit tests
```

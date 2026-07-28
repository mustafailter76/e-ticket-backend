# E-TICKET – Event Ticketing Backend API

RESTful backend application for browsing events, purchasing tickets, managing user accounts, and monitoring event sales through an admin dashboard.

---

## 🚀 Project Overview

**E-TICKET** is an event ticketing backend built with Java and Spring Boot.

The application provides role-based flows:

- **Users can:**
  - register and log in
  - browse and filter events
  - purchase between 1 and 3 tickets
  - view their ticket purchases
  - cancel tickets before the event date
  - view their profile
  - change their password
  - delete their account
- **Administrators can:**
  - create new events
  - delete events without existing purchases
  - view ticket sales and revenue statistics

---

## 🧱 Tech Stack

### Backend

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Maven
- Layered architecture (Controller / Service / Repository / DTO)

### Security

- Spring Security
- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Stateless session management

### Database

- PostgreSQL 16
- Relational data model
- Foreign-key constraints
- Database indexes
- SQL schema and sample data initialization

### Documentation & Testing

- Springdoc OpenAPI
- Swagger UI
- JUnit 5
- Mockito
- AssertJ

### Deployment

- Docker
- Docker Compose
- Multi-stage Docker build

---

## ✨ Core Features

- **Authentication & Authorization**
  - user registration and login
  - JWT access tokens
  - `USER` and `ADMIN` roles
  - protected API endpoints

- **Event Management**
  - create and delete events
  - list events by date
  - search by event name
  - filter by category
  - track total and available ticket stock

- **Ticket Management**
  - purchase 1 to 3 tickets at a time
  - automatic total-price calculation
  - stock validation before purchase
  - stock reduction after purchase
  - ticket cancellation before the event
  - automatic stock restoration after cancellation

- **User Management**
  - current-user profile
  - password update
  - account deletion
  - future-event stock restoration when an account is deleted

- **Admin Dashboard**
  - total event count
  - total tickets sold
  - total revenue
  - event-based sales details

- **Error Handling**
  - centralized exception handling
  - request validation
  - consistent success and error responses

---

## 🔐 Roles & Permissions

### Public

- Register
- Login
- Swagger UI
- OpenAPI documentation

### User

- Browse and filter events
- Purchase tickets
- View purchased tickets
- Cancel ticket purchases
- Manage profile and account

### Admin

- Browse and filter events
- Create events
- Delete events
- View the sales dashboard
- Manage profile and password

---

## ⚙️ Installation

### Docker Setup

The easiest way to run the project is with Docker Compose:

```bash
docker compose up --build
```

This starts:

- PostgreSQL on `localhost:5433`
- Backend API on `localhost:8080`

Stop the containers:

```bash
docker compose down
```

Remove the containers and database volume:

```bash
docker compose down -v
```

### Local Setup

Requirements:

- Java 17
- PostgreSQL

Create a PostgreSQL database named:

```text
e-ticket_db
```

Create the local configuration file:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Update the database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/e-ticket_db
spring.datasource.username=your_local_username
spring.datasource.password=your_local_password
```

Run the application on macOS or Linux:

```bash
./mvnw spring-boot:run
```

Run the application on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 👤 Demo Accounts

The following accounts are created automatically when the application starts:

### Admin

```text
Email:    admin@eticket.com
Password: admin123
```

### User

```text
Email:    deneme@eticket.com
Password: deneme123
```

These accounts are intended for local development and demonstration.

---

## 🌐 API Endpoints

### Authentication

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login
```

### Events

```text
GET    /api/v1/events
POST   /api/v1/events/create
DELETE /api/v1/events/delete/{eventId}
GET    /api/v1/events/sales-dashboard
```

Optional event filters:

```text
GET /api/v1/events?name={name}
GET /api/v1/events?category={category}
GET /api/v1/events?name={name}&category={category}
```

### Ticket Purchases

```text
POST   /api/v1/ticket-purchase/purchase
GET    /api/v1/ticket-purchase/all
DELETE /api/v1/ticket-purchase/cancel/{id}
```

### Users

```text
GET    /api/v1/users/me
PATCH  /api/v1/users/change-password
DELETE /api/v1/users/delete
```

Protected requests require a JWT:

```http
Authorization: Bearer <access-token>
```

---

## 📁 Project Structure

```text
src/main/java/com/mustafa_mert/backend
├── auth
│   ├── controller
│   ├── dto
│   └── service
├── common
│   ├── exception
│   └── response
├── config
├── event
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── security
├── ticket_purchase
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
└── user
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service
```

---

## 🧪 Testing

Run the test suite:

```bash
./mvnw test
```

The project contains tests for:

- authentication services
- event services
- ticket purchase services
- user services
- Spring application context

---

## 📖 API Documentation

After starting the application:

```text
Swagger UI:  http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

Swagger UI can be used to inspect request and response models and test the endpoints.

---

## 📌 Notes

- Build outputs are excluded from the repository.
- Local database credentials are stored in a Git-ignored configuration file.
- Sample events are inserted automatically during database initialization.
- Demo credentials and development secrets must be replaced before production use.
- CORS is configured for local frontend applications on ports `5173` and `3000`.
- This project demonstrates REST API design, layered architecture, authentication, authorization, validation, and transactional business logic.

---

## 📄 License

This repository was developed for educational and portfolio purposes.

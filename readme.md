# 🏗️ Architecture Decisions & Module Progress

## ✅ Module 1 — User Authentication & Authorization

### Objective

Build a secure, stateless authentication system that can be reused by every microservice without creating a central authentication bottleneck.

---

## Features Implemented

* User Registration
* Seller Registration
* Login API
* JWT Authentication
* BCrypt Password Hashing
* Spring Security
* JWT Authentication Filter
* Request Validation
* Global Exception Handling
* Role-Based Authorization (`USER`, `SELLER`)

---

## Database Design

### Users

```text
id
name
email
password
role
```

### Addresses

```text
id
house_no
street
city
state
country
pincode
```

### User Addresses

```text
user_id
address_id
```

---

# Authentication Flow

```text
Client
    │
    ▼
POST /auth/login
    │
    ▼
Verify Email
    │
    ▼
Verify Password (BCrypt)
    │
    ▼
Generate JWT
    │
    ▼
Return Access Token
```

For every protected request:

```text
Client
    │
Authorization: Bearer <JWT>
    │
    ▼
JwtAuthenticationFilter
    │
    ▼
Extract JWT
    │
    ▼
Validate Signature
    │
    ▼
Extract Claims
    │
    ▼
Load UserDetails
    │
    ▼
Create Authentication Object
    │
    ▼
Store in SecurityContextHolder
    │
    ▼
Protected Controller
```

---

# Key Design Decisions

## 1. JWT instead of HTTP Sessions

### Options Considered

**Option 1**

* HTTP Session
* Server stores session information.

**Option 2 (Chosen)**

* JWT (Stateless Authentication)

### Why JWT?

* No server-side session storage.
* Every request carries its own authentication.
* Better suited for distributed microservices.
* Horizontal scaling becomes much easier.

---

## 2. BCrypt instead of Plain Text Passwords

Passwords are never stored directly in the database.

Registration

```text
Password
    │
BCrypt Hash
    │
Database
```

Login

```text
User Password
    │
BCrypt.matches()
    │
Stored Hash
```

This prevents password exposure even if the database is compromised.

---

## 3. Enum for Roles

Chosen:

```java
Role.USER
Role.SELLER
```

instead of

```java
String role;
```

Reasons:

* Compile-time safety
* Prevents invalid role values
* Easier to maintain
* Stored as readable strings using `EnumType.STRING`

---

## 4. Separate Registration Endpoints

Implemented

```text
POST /auth/register
POST /auth/register/seller
```

instead of

```json
{
  "role": "SELLER"
}
```

Reason:

The server—not the client—should decide user privileges. This prevents clients from assigning themselves unauthorized roles.

---

## 5. Stateless Authentication

Spring Security is configured with

```text
SessionCreationPolicy.STATELESS
```

Every request must include a valid JWT.

No HTTP session is maintained.

---

## 6. Why CustomUserDetailsService?

Spring Security works with the `UserDetails` interface rather than custom entity classes.

`CustomUserDetailsService` converts our `User` entity into a `UserDetails` object understood by Spring Security.

---

## 7. Why JwtAuthenticationFilter?

Every incoming request passes through the filter before reaching the controller.

Responsibilities:

* Read Authorization header
* Extract JWT
* Validate signature
* Verify expiration
* Load user
* Create Authentication object
* Store authentication inside `SecurityContextHolder`

This allows Spring Security to recognize the current authenticated user for the remainder of the request.

---

## 8. Why SecurityContextHolder?

Once authentication is stored inside `SecurityContextHolder`, every layer of the application can access the authenticated user without parsing the JWT again.

---

## 9. Microservice Authentication Strategy

### Option 1 (Rejected)

```text
Product Service
      │
      ▼
User Service
      │
Validate JWT
```

Problem:

Every request requires a network call to the User Service, making it a bottleneck and a single point of failure.

---

### Option 2 (Chosen)

```text
Client
    │
JWT
    │
    ▼
Product Service
Order Service
Cart Service
Inventory Service
```

Each service validates the JWT locally using the shared signing key.

Advantages:

* No network calls for authentication
* No authentication bottleneck
* Better scalability
* Stateless architecture

The User Service is responsible only for issuing JWTs during login.

---

## APIs Implemented

### Public

```http
POST /auth/register
POST /auth/register/seller
POST /auth/login
```

### Protected

All remaining endpoints require a valid JWT.

---

### Env formate
```
jwt.secret
jwt.expiration
DB_PASSWORD
DB_USERNAME
DB_URL
```
## Module Outcome

After completing this module:

* Secure user registration
* Seller registration
* Stateless JWT authentication
* Role-based authorization foundation
* Production-style Spring Security configuration
* Authentication ready to be reused across all upcoming microservices

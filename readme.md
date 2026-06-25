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


---


## ✅ Module 2 — Product Service

### Objective

Build a dedicated Product Service responsible for managing product information while keeping inventory management isolated in a separate microservice. The service should remain lightweight, stateless, and independently scalable.

---

## Features Implemented

* Create Product API
* Get Product by ID API
* JWT Authentication
* Stateless Authentication
* Role-Based Authorization (`SELLER`)
* Request Validation
* Spring Security
* Global Exception Handling
* MySQL Integration
* Environment Variable Configuration

---

## Database Design

### Products

```text
id
seller_id
name
description
brand
category
gender
actual_price
offer_price
is_active
created_at
updated_at
```

---

# Product Creation Flow

```text
Seller
    │
Authorization: Bearer <JWT>
    │
    ▼
POST /products
    │
    ▼
JwtAuthenticationFilter
    │
    ▼
Validate JWT
    │
    ▼
Extract Seller Information
    │
    ▼
Product Controller
    │
    ▼
Product Service
    │
    ▼
Validate Business Rules
    │
    ▼
Save Product
    │
    ▼
Return Product ID
```

---

# Product Retrieval Flow

```text
Client
    │
Authorization: Bearer <JWT>
    │
    ▼
GET /products/{productId}
    │
    ▼
JwtAuthenticationFilter
    │
    ▼
Validate JWT
    │
    ▼
Product Controller
    │
    ▼
Product Service
    │
    ▼
Fetch Product
    │
    ▼
Return Product Details
```

---

# Key Design Decisions

## 1. Separate Product and Inventory Services

### Option 1 (Rejected)

Store product information and stock inside the same service.

```text
Product
id
name
price
stock
```

### Option 2 (Chosen)

Separate responsibilities into two independent services.

```text
Product Service
----------------
Product Information

Inventory Service
-----------------
Stock Information
```

### Why?

Product information changes infrequently, whereas inventory changes continuously due to:

* Orders
* Returns
* Restocking

Keeping them separate allows each service to evolve and scale independently.

---

## 2. Seller Ownership from JWT

### Option 1 (Rejected)

Allow the client to send the seller ID.

```json
{
  "sellerId": 5,
  "name": "T-Shirt"
}
```

Problem:

A malicious client could create products under another seller's account.

---

### Option 2 (Chosen)

Extract the seller ID directly from the validated JWT.

```text
JWT
    │
    ▼
Extract userId
    │
    ▼
Store as seller_id
```

Advantages:

* Prevents privilege escalation.
* Seller identity cannot be manipulated.
* Server remains the source of truth.

---

## 3. Local JWT Validation

Every protected request validates the JWT locally.

```text
Client
    │
JWT
    │
    ▼
Product Service
```

instead of

```text
Product Service
      │
      ▼
User Service
      │
Validate JWT
```

Reasons:

* Eliminates unnecessary network calls.
* Lower request latency.
* Better scalability.
* No authentication bottleneck.

---

## 4. Separate Product and Inventory Databases

Chosen Architecture

```text
Product Database
----------------
Product Information

Inventory Database
------------------
Stock
Sizes
Availability
```

Reasons:

* Independent ownership.
* Independent scaling.
* Better separation of business domains.
* Easier maintenance.

---

## 5. Size-wise Inventory

Instead of

```text
Stock = 50
```

Inventory will maintain

```text
S  → 10
M  → 20
L  → 15
XL → 5
```

Reason:

Since this project focuses on clothing, inventory must be managed size-wise.

---

## 6. Why Product Service Does Not Store Stock

Product Service owns only product metadata.

Responsibilities:

* Name
* Description
* Brand
* Category
* Gender
* Price

Inventory Service owns:

* Available Sizes
* Stock Quantity
* Future Stock Updates
* Stock Reservation

This follows the **Single Responsibility Principle (SRP)**.

---

## 7. Synchronous Service Communication

Current Architecture

```text
Product Service
      │
HTTP (RestTemplate)
      │
      ▼
Inventory Service
```

### Why?

Product creation is a low-frequency operation.

Advantages:

* Immediate consistency.
* Simpler implementation.
* Easier debugging.
* Suitable for the current project scope.

Future versions may migrate to RabbitMQ or Kafka for asynchronous communication.

---

## 8. Stateless Authentication

Spring Security is configured with

```text
SessionCreationPolicy.STATELESS
```

Every request must include a valid JWT.

No HTTP session is maintained.

---

## APIs Implemented

### Protected APIs

```http
POST /products

GET /products/{productId}
```

---

## Environment Variables

```text
JWT_SECRET
PRODUCT_DB_URL
PRODUCT_DB_USERNAME
PRODUCT_DB_PASSWORD
```

---

## Module Outcome

After completing this module:

* Product information is managed independently.
* Products are persisted using MySQL.
* Seller ownership is enforced through JWT.
* Stateless authentication is reused from the User Service.
* Product metadata is completely separated from inventory.
* Foundation is ready for Inventory Service integration.


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



---

---

# ✅ Module 3 — Inventory Service

### Objective

Build a dedicated Inventory Service responsible for managing product stock independently from product metadata while supporting size-wise inventory and secure service-to-service communication.

---

## Features Implemented

* Create Inventory API
* Get Inventory API
* Size-wise Stock Management
* JWT Authentication
* Stateless Authentication
* Service-to-Service Authentication
* Spring Security
* Request Validation
* Global Exception Handling
* MySQL Integration
* Environment Variable Configuration

---

## Database Design

### Inventory

```text
id
product_id
size
stock
```

---

# Inventory Creation Flow

```text
Seller
    │
POST /products
    │
    ▼
Product Service
    │
Save Product
    │
Forward JWT
    │
    ▼
POST /internal/inventory
    │
    ▼
JwtAuthenticationFilter
    │
Validate JWT
    │
Extract Claims
    │
Create Authentication
    │
    ▼
Inventory Controller
    │
    ▼
Inventory Service
    │
    ▼
Save Inventory
```

---

# Inventory Retrieval Flow

```text
Client
    │
GET /inventory/{productId}
    │
    ▼
Inventory Service
    │
Fetch Stock
    │
Return Available Sizes and Stock
```

---

# Key Design Decisions

## 1. Inventory as a Separate Microservice

### Option 1 (Rejected)

Store stock inside Product Service.

```text
Product
-------
id
name
price
stock
```

### Option 2 (Chosen)

Dedicated Inventory Service.

```text
Product Service
----------------
Product Metadata

Inventory Service
-----------------
Stock
Availability
```

### Why?

Inventory changes much more frequently than product information.

Keeping inventory separate allows:

* Independent scaling
* Better separation of concerns
* Easier future extensions (reservations, warehouse management, etc.)

---

## 2. Product Service Stores Available Sizes

Product Service stores only the available size variants.

```text
Product Sizes

product_id
size
```

Inventory Service stores only stock quantities.

```text
Inventory

product_id
size
stock
```

### Why?

The Product Service should always be able to return all available variants, even when stock becomes zero.

---

## 3. Service-to-Service JWT Authentication

Instead of exposing internal APIs publicly,

```text
Product Service
        │
JWT
        │
        ▼
Inventory Service
```

The original client JWT is forwarded to Inventory Service.

Advantages:

* No public access to internal APIs
* Reuses existing authentication
* No additional authentication server
* Stateless communication

---

## 4. Local JWT Validation

Inventory Service validates JWT locally using the shared signing key.

Advantages:

* No network calls to User Service
* Low latency
* Better scalability

---

## 5. Stateless Authentication

Spring Security is configured with

```text
SessionCreationPolicy.STATELESS
```

Every request requires a valid JWT.

---

## APIs Implemented

### Internal APIs

```http
POST /internal/inventory
```

### Public APIs

```http
GET /inventory/{productId}
```

---

## Environment Variables

```text
JWT_SECRET
INVENTORY_DB_URL
INVENTORY_DB_USERNAME
INVENTORY_DB_PASSWORD
REDIS_HOST
REDIS_PORT

```
```
Cache write through pattren- when product is added we first add to the mysql then redis stock.




when order service asks inventory service for the stock,it reads from the cache and do dec operation, then update the mysql db 

what if we use read through- if we do that cache miss happens and all rqst goes to the db all read the same stock ans some inconsistency happens
```

```
what if db saved butredis not ?

Now we have:

MySQL
-------
Product 101
Stock 50

Redis
------
No Key

Now the Order Service places an order.

Inventory Service tries:

GET inventory:101:M

Redis returns:

Key not found

Even though MySQL has stock.

So are we inconsistent?

Yes.

This is called data inconsistency between the cache and the database.

How do real companies solve it?

There are several approaches.

Option 1 (What we'll do now)

Fail the entire request.

Save MySQL ✅

↓

Save Redis ❌

↓

Throw Exception

The request fails.

But...

If MySQL has already committed, we still have inconsistent data.

So this alone isn't enough.

Option 2 (Transaction + Compensation)
Save MySQL

↓

Redis Failed

↓

Delete MySQL Record

Roll back the database change.

The problem?

Redis isn't part of the database transaction.

Spring cannot roll back Redis automatically.

You'd have to write compensation logic yourself.

Option 3 (Retry)
MySQL Saved

↓

Redis Failed

↓

Retry 3 Times

Often Redis failures are temporary.

This works well for transient network issues.

Option 4 (Kafka / Outbox) ⭐

This is what large systems commonly do.

Save MySQL

↓

Save Outbox Event

↓

Commit Transaction

↓

Kafka

↓

Update Redis

Eventually Redis becomes consistent.

This is the most robust solution, but it introduces Kafka and the Outbox Patte
```

---

## Module Outcome

After completing this module:

* Inventory is managed independently from product metadata.
* Size-wise stock management is implemented.
* Product and Inventory services communicate securely.
* Stateless JWT authentication is reused across services.
* Foundation is ready for Order Service and stock deduction.


---

# ✅ Module 3 — OrderService Service

-> here order service gets the price from the product service ,if order happend update the redis and db synchrnously ,no improve in latency ,throughput ,
->I chose this design intentionally because I wanted to learn Redis and atomic operations without introducing additional distributed system complexity.
If I wanted to optimize throughput and reduce latency further, I would introduce a message broker like Kafka. The request would return immediately after the Redis deduction, and MySQL would be updated asynchronously by a Kafka consumer. That removes MySQL from the critical request path and significantly improves throughput."


## Inventory Flow with Redis Cache


The Inventory Service is responsible for managing product stock. It owns the inventory database and ensures that stock updates are performed safely and efficiently.

When a seller creates a new product, the Product Service forwards the inventory information to the Inventory Service. The Inventory Service stores the inventory in both MySQL and Redis using the **Write-Through Cache** pattern.

---

# Product Creation Flow

```text
Seller
   │
   ▼
POST /products
   │
   ▼
Product Service
   │
   ├── Save Product
   ├── Save Product Sizes
   └── Call Inventory Service
            │
            ▼
      Create Inventory
            │
     ┌──────┴──────┐
     ▼             ▼
 MySQL         Redis Cache
```

---

# Inventory Creation

For every product size, Inventory Service creates a separate inventory record.

Example:

| Product ID | Size | Stock |
|------------|------|------:|
| 15 | S | 20 |
| 15 | M | 15 |
| 15 | L | 10 |

The same information is immediately written to Redis.

---

# Redis Cache

Redis follows the **Write-Through Cache** strategy.

Whenever inventory is created, the data is written to:

1. MySQL (Persistent Storage)
2. Redis (Fast Cache)

Both remain synchronized during inventory creation.

---

# Redis Key Format

Each product-size combination is stored as an individual Redis key.

```
inventory:{productId}:{size}
```

Example:

```
inventory:15:S
inventory:15:M
inventory:15:L
```

Values stored:

```
inventory:15:S -> 20
inventory:15:M -> 15
inventory:15:L -> 10
```

The value represents the **currently available stock** for that specific size.

---

# Why Separate Keys?

Instead of storing all sizes inside a single object, each size has its own Redis key.

Example:

```
inventory:15:S
inventory:15:M
inventory:15:L
```

Advantages:

- Constant time (O(1)) lookup
- Independent stock updates
- Simple key structure
- Easy debugging
- Supports atomic stock deduction

---

# Stock Deduction Flow

When a customer places an order:

```text
Customer
   │
   ▼
Order Service
   │
   ▼
Inventory Service
   │
   ▼
Redis (Lua Script)
   │
   ▼
MySQL
   │
   ▼
Order Created
```

---

# Why Redis First?

Inventory Service first updates Redis using a Lua script.

The Lua script performs the following operations atomically:

1. Read current stock.
2. Verify sufficient stock is available.
3. Deduct requested quantity.
4. Return remaining stock.

Since the entire script executes as a single atomic operation, concurrent requests cannot modify the same stock simultaneously.

This prevents race conditions and overselling.

---

# Synchronizing MySQL

After Redis successfully deducts the stock, the Inventory Service updates MySQL with the latest stock value.

Current flow:

```text
Redis
   │
   ▼
MySQL
   │
   ▼
Response
```

This guarantees that Redis and MySQL remain consistent after each successful inventory update.

---

# Current Limitation

The current implementation updates MySQL synchronously after Redis.

While Redis provides atomic stock deduction, every request still waits for MySQL to complete before returning a response.

As a result:

- Request latency still depends on MySQL.
- MySQL remains the write bottleneck under heavy load.

---

# Future Improvement

To improve scalability, MySQL updates can be made asynchronous using Kafka.

Future architecture:

```text
Customer
   │
   ▼
Order Service
   │
   ▼
Inventory Service
   │
   ▼
Redis (Atomic DECR)
   │
   ▼
Return Success
   │
   ▼
Kafka
   │
   ▼
Inventory Consumer
   │
   ▼
MySQL
```

Benefits:

- Lower response latency
- Higher throughput
- MySQL removed from the critical request path
- Better scalability during high traffic
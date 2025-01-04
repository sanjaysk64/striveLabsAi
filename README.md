# Microservices Architecture Setup

This document provides instructions for setting up and running the various services in the microservices architecture.

## Services Overview

- **Eureka Server**: Service registry for microservices.
- **Security Service**: Provides authentication and authorization at the microservice level.
- **API Gateway**: Routes incoming requests to the appropriate microservices.
- **StriLabs Service**: A sample service that performs business logic.

### Service Startup Order

1. **Eureka Server**  
   The Eureka server must be started first as it acts as the service registry for all other services to discover each other.

2. **Security Service**  
   The Security service should be started next, as it provides authentication and authorization at the microservice level.

3. **API Gateway**  
   Start the API Gateway after the Security service. The API Gateway will route requests to the appropriate StriLabs service and also manage security headers, such as authentication tokens.

4. **Strive Labs Service**  
   Finally, start the StriLabs service that handles the business logic. It relies on the security and API gateway layers for communication and access control.

## Security Implementation in Microservices

### 1. **Security Service**  
The Security service is responsible for authenticating and authorizing requests. It ensures that only authorized users can access the StriLabs services. This service integrates with JWT (JSON Web Tokens) for stateless authentication. 

### 2. **API Gateway**  
The API Gateway handles routing of the incoming requests and also forwards the JWT token to downstream services, ensuring that the requests are secure. 

- The API Gateway verifies the token before forwarding requests to StriLabs services.
- It forwards only valid requests, and unauthorized requests are blocked.

### 3. **StriLabs Service**  
Each microservice, including StriLabs, implements security checks at the service level:
- **Token Validation**: Each service checks for a valid token before proceeding with processing any request.

## How to Start the Services

Follow the steps below to start the services in the correct order:

### Step 1: Start Eureka Server
- Navigate to the `eureka-server` directory and run the server using your IDE 

2. **Start Security Service**  
   Security Service must start after Eureka Server. This service will handle the authentication and authorization logic.
   - Navigate to the Security Service directory.
   - Run the Security Service

3. **Start API Gateway**  
   API Gateway must start after the Security Service to manage routing, load balancing, and to enforce security policies.
   - Navigate to the API Gateway directory.
   - Run the API Gateway 

4. **Start Strive Labs Services**  
   Finally, start the Strive Labs services. These services will be registered with Eureka and routed through the API Gateway.
   - Navigate to the Strive Labs Services directory.
   - Run the Strive Labs service


**Before accessing Strive Labs services, you must first register a user to receive an authentication token.**

- **URL**: `http://localhost:8080/auth/register`
- **Method**: POST
- **Request Payload**:
  ```json
  {
    "name": "ANY",
    "email": "ANY",
    "password": "ANY"
  }

  ## Step 2: Obtain Authentication Token

After registering, use the registered username and password to obtain a token that will be required for accessing protected endpoints.

- **URL**: `http://localhost:8080/auth/token`
- **Method**: POST
- **Request Payload**:

  ```json
  {
    "username": "sanjayla",
    "password": "aceace@50"
  }

  ## Step 3: Use the Token for Accessing Strive Labs Services

Once you have the JWT token, you need to include it in the `Authorization` header as a Bearer token for accessing Strive Labs services through the API Gateway.

- **Authorization Header**:

  ```bash
  Authorization: Bearer your-jwt-token-here


## Security Implementation in Microservices

Security is implemented at the microservice level to ensure that only authorized requests are allowed to access the services. The security logic is as follows:

- **Authentication**: All incoming requests are authenticated by the Security Service using tokens (JWT).
- **Authorization**: After authentication, the Security Service verifies the permissions for each request.
- **Secure Endpoints**: Each microservice is secured using Spring Security.


# Key-Value Data Store Service

## Overview

This project implements a high-performance, scalable key-value data store service with advanced features. The service supports Create, Read, and Delete (CRD) operations and includes functionality for time-to-live (TTL), batch operations, multi-tenancy, and tenant-specific storage limits.

### Features

- **Create API**: Add a key-value pair with optional TTL. Keys are unique per tenant.
- **Read API**: Retrieve values by key, ensuring TTL expiration is respected.
- **Delete API**: Remove key-value pairs by key.
- **Batch API**: Create multiple key-value pairs in a single request with validation.
- **Time-to-Live (TTL)**: Optional expiration time for each key.
- **Multi-Tenancy**: Isolated data storage and management per tenant.
- **Storage Limits**: Enforce tenant-specific storage size limits.
- **Concurrency**: Supports concurrent operations with optimistic locking.

## Tech Stack

- **Language**: Java
- **Framework**: Spring Boot
- **Database**: PostgreSQL (or any RDBMS)
- **Build Tool**: Maven

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 12+
- Git

### Steps

1. **Clone the repository**:
    ```bash
    git clone <repository_url>
    cd key-value-data-store
    ```

2. **Configure the database**:
    Update `application.properties` or `application.yml` with your database credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/key_value_db
    spring.datasource.username=<your_db_username>
    spring.datasource.password=<your_db_password>
    ```

3. **Create the `tenants` table**:
    ```sql
    CREATE TABLE IF NOT EXISTS tenants (
        tenant_id VARCHAR(255) PRIMARY KEY,
        storage_limit BIGINT NOT NULL
    );

    INSERT INTO tenants (tenant_id, storage_limit) 
    VALUES ('TenantA', 1000000), ('TenantB', 2000000), ('TenantC', 1500000);
    ```

## API Endpoints
Note: Pass Tenant id in request header X-Tenant-ID  and its value
### Create Key-Value Pair

**POST** `/api/object`

- **Request Body**:
    ```json
    {
    "key": "key1",
    "data": "{\"name\":\"John Doe\"}",
    "ttl": 3600
    }

    ```
- **Response**:
    - `201 Created` on success
    - `400 Bad Request` if key already exists or validation fails

### Get Key-Value Pair

**GET** `/api/object/{key}`

- **Response**:
    ```json
    {
        "key": "key1",
        "data": { "name": "John Doe" }
    }
    ```
    - `404 Not Found` if key does not exist or is expired

### Delete Key-Value Pair

**DELETE** `/api/object/{key}`

- **Response**:
    - `204 No Content` on success
### Batch Create Key-Value Pairs

**POST** `/api/batch/object`

- **Request Body**:
    ```json
    [
        { "key": "key1", "data": "{\"name\":\"Johnson\",\"age\":30}", "ttl": 60 },
        { "key": "key2", "data": "{\"name\":\"Jane Doe\",\"age\":25}", "ttl": 1800 },
        { "key": "key3", "data": "{\"name\":\"Alice\",\"age\":28}", "ttl": 7200 }
    ]
    ```
- **Response**:
    - `201 Created` on success
    - `400 Bad Request` if validation fails

## Design Decisions

- **Optimistic Locking**: Ensures data integrity during concurrent updates.
- **Batch Limit**: A batch size limit of 100 is enforced to maintain performance and prevent overloading the database.

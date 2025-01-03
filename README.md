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
    - `404 Not Found` if key does not exist

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

## Limitations
- No built-in authentication/authorization; tenants are identified by `tenantId` in requests.

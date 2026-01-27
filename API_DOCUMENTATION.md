# Receipt Management System - API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Authentication Endpoints](#authentication-endpoints)
5. [Receipt Endpoints](#receipt-endpoints)
6. [Property Endpoints](#property-endpoints)
7. [Report Endpoints](#report-endpoints)
8. [Error Handling](#error-handling)
9. [Response Codes](#response-codes)

---

## Overview

The Receipt Management System API provides a RESTful interface for managing receipts, properties, and generating reports. The API is built with Spring Boot and requires JWT authentication for most endpoints.

**API Version:** 1.0  
**Last Updated:** January 27, 2026

---

## Authentication

### JWT Token Authentication

Most endpoints require JWT (JSON Web Token) authentication. Tokens are obtained by logging in with valid credentials.

**How to use JWT:**
1. Call the `/api/auth/login` endpoint with your credentials
2. Extract the `token` from the response
3. Include the token in the `Authorization` header of subsequent requests:
   ```
   Authorization: Bearer <your_jwt_token>
   ```

**Token Expiration:** Tokens expire after 24 hours. Login again to get a new token.

---

## Base URL

```
http://localhost:8080
```

For production, replace with your actual server URL.

---

## Authentication Endpoints

### 1. Login

Authenticate a user and receive a JWT token.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "roles": ["USER"]
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Invalid username or password"
}
```

**Headers:**
- `Content-Type: application/json`

**Required Fields:**
- `username` (string): Username
- `password` (string): Password

---

### 2. Signup

Create a new user account.

**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Response (201 Created):**
```json
{
  "message": "User registered successfully"
}
```

**Response (400 Bad Request):**
```json
{
  "message": "Signup failed: Email already in use"
}
```

**Headers:**
- `Content-Type: application/json`

**Required Fields:**
- `username` (string): Unique username (3-50 characters)
- `email` (string): Valid email address
- `password` (string): Password (min 8 characters)
- `confirmPassword` (string): Must match password

---

## Receipt Endpoints

All receipt endpoints require authentication with `Authorization: Bearer <token>` header.

### 1. Upsert Receipt

Create a new receipt or update an existing one.

**Endpoint:** `POST /api/receipts/upsert`

**Request Body:**
```json
{
  "id": null,
  "storeName": "WALMART",
  "receiptDate": "2026-01-27T10:30:00Z",
  "amount": 150.50,
  "description": "Walmart - Grocery items",
  "receiptDescription": "Weekly groceries",
  "properties": [
    {
      "name": "Main Property",
      "percentage": 60
    },
    {
      "name": "Secondary Property",
      "percentage": 40
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "description": "Walmart - Grocery items",
  "amount": 150.50,
  "receiptDate": "2026-01-27T10:30:00Z",
  "year": 2026,
  "receiptSourceId": 1,
  "propertyAllocations": [
    {
      "name": "Main Property",
      "percentage": 60
    },
    {
      "name": "Secondary Property",
      "percentage": 40
    }
  ]
}
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "title": "Invalid receipt data",
  "message": "Property percentages must sum to 100, but got 90"
}
```

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer <token>`

**Required Fields:**
- `storeName` (string): Name of the store
- `receiptDate` (string): ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ)
- `amount` (number): Receipt amount (positive number)
- `description` (string): Receipt description
- `properties` (array): Property allocations

**Validation Rules:**
- Property percentages must sum to exactly 100
- Amount must be greater than 0
- Receipt date must be in valid ISO 8601 format
- Store name is required and cannot be empty

---

### 2. Get All Receipts

Retrieve all receipts with pagination support.

**Endpoint:** `GET /api/receipts`

**Query Parameters:**
- `page` (integer, optional): Page number (0-based, default: 0)
- `size` (integer, optional): Number of receipts per page (default: 100, max: 500)

**Example:**
```
GET /api/receipts?page=0&size=50
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "description": "Walmart - Grocery items",
      "amount": 150.50,
      "receiptDate": "2026-01-27T10:30:00Z",
      "year": 2026,
      "receiptSourceId": 1,
      "propertyAllocations": []
    },
    {
      "id": 2,
      "description": "Target - Household items",
      "amount": 89.99,
      "receiptDate": "2026-01-26T14:15:00Z",
      "year": 2026,
      "receiptSourceId": 2,
      "propertyAllocations": []
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 150,
  "totalPages": 3,
  "numberOfElements": 50,
  "first": true,
  "last": false
}
```

**Headers:**
- `Authorization: Bearer <token>`

---

### 3. Get Receipt by ID

Retrieve a specific receipt by its ID.

**Endpoint:** `GET /api/receipts/{id}`

**Path Parameters:**
- `id` (integer, required): Receipt ID

**Example:**
```
GET /api/receipts/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "description": "Walmart - Grocery items",
  "amount": 150.50,
  "receiptDate": "2026-01-27T10:30:00Z",
  "year": 2026,
  "receiptSourceId": 1,
  "propertyAllocations": [
    {
      "name": "Main Property",
      "percentage": 60
    },
    {
      "name": "Secondary Property",
      "percentage": 40
    }
  ]
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Receipt with ID 999 not found"
}
```

**Headers:**
- `Authorization: Bearer <token>`

---

### 4. Search Receipts by Year

Retrieve all receipts for a specific year.

**Endpoint:** `GET /api/receipts/search/year/{year}`

**Path Parameters:**
- `year` (integer, required): Year (e.g., 2024, 2025, 2026)

**Example:**
```
GET /api/receipts/search/year/2024
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "description": "Walmart - Grocery items",
    "amount": 150.50,
    "receiptDate": "2024-06-15T10:30:00Z",
    "year": 2024,
    "receiptSourceId": 1,
    "propertyAllocations": []
  },
  {
    "id": 2,
    "description": "Target - Household items",
    "amount": 89.99,
    "receiptDate": "2024-07-20T14:15:00Z",
    "year": 2024,
    "receiptSourceId": 2,
    "propertyAllocations": []
  }
]
```

**Response (200 OK - Empty):**
```json
[]
```

**Headers:**
- `Authorization: Bearer <token>`

---

### 5. Search Receipts by Source

Retrieve all receipts from a specific receipt source (store).

**Endpoint:** `GET /api/receipts/search/source/{sourceId}`

**Path Parameters:**
- `sourceId` (integer, required): Receipt source ID

**Example:**
```
GET /api/receipts/search/source/1
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "description": "Walmart - Grocery items",
    "amount": 150.50,
    "receiptDate": "2026-01-27T10:30:00Z",
    "year": 2026,
    "receiptSourceId": 1,
    "propertyAllocations": []
  },
  {
    "id": 3,
    "description": "Walmart - Electronics",
    "amount": 299.99,
    "receiptDate": "2026-01-25T09:00:00Z",
    "year": 2026,
    "receiptSourceId": 1,
    "propertyAllocations": []
  }
]
```

**Headers:**
- `Authorization: Bearer <token>`

---

### 6. Search Receipts by Source and Year

Retrieve receipts from a specific source for a specific year.

**Endpoint:** `GET /api/receipts/search/source/{sourceId}/year/{year}`

**Path Parameters:**
- `sourceId` (integer, required): Receipt source ID
- `year` (integer, required): Year

**Example:**
```
GET /api/receipts/search/source/1/year/2024
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "description": "Walmart - Grocery items",
    "amount": 150.50,
    "receiptDate": "2024-06-15T10:30:00Z",
    "year": 2024,
    "receiptSourceId": 1,
    "propertyAllocations": []
  }
]
```

**Headers:**
- `Authorization: Bearer <token>`

---

### 7. Delete Receipt

Delete a receipt by its ID.

**Endpoint:** `DELETE /api/receipts/{id}`

**Path Parameters:**
- `id` (integer, required): Receipt ID

**Example:**
```
DELETE /api/receipts/1
```

**Response (204 No Content):**
```
(Empty response body)
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Receipt with ID 999 not found"
}
```

**Headers:**
- `Authorization: Bearer <token>`

---

## Property Endpoints

All property endpoints require authentication with `Authorization: Bearer <token>` header.

### Get Property with Receipts

Retrieve a property along with its associated receipts.

**Endpoint:** `GET /api/properties/{propertyId}/receipts`

**Path Parameters:**
- `propertyId` (integer, required): Property ID

**Query Parameters:**
- `year` (integer, optional): Filter receipts by year

**Example:**
```
GET /api/properties/1/receipts?year=2024
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Main Property",
  "description": "Primary residential property",
  "receipts": [
    {
      "id": 1,
      "description": "Walmart - Grocery items",
      "amount": 150.50,
      "receiptDate": "2024-06-15T10:30:00Z",
      "year": 2024,
      "receiptSourceId": 1,
      "propertyAllocations": []
    },
    {
      "id": 5,
      "description": "Home Depot - Repairs",
      "amount": 450.00,
      "receiptDate": "2024-08-20T11:45:00Z",
      "year": 2024,
      "receiptSourceId": 3,
      "propertyAllocations": []
    }
  ]
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Property with ID 999 not found"
}
```

**Headers:**
- `Authorization: Bearer <token>`

**Validation:**
- Property ID must be a positive number
- If year is provided, it must be 1900 or later

---

## Report Endpoints

### 1. Generate Yearly Report

Generate a yearly report for a property (asynchronous operation).

**Endpoint:** `POST /api/reports/yearly`

**Query Parameters:**
- `propertyName` (string, required): Name of the property
- `year` (integer, required): Year for the report

**Example:**
```
POST /api/reports/yearly?propertyName=Main%20Property&year=2024
```

**Response (202 Accepted):**
```json
{
  "message": "Report generation started. You will receive the PDF via email shortly.",
  "status": "PROCESSING",
  "reportId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Invalid year. Must be between 1900 and 2100."
}
```

**Headers:**
- `Authorization: Bearer <token>`

**Notes:**
- Report generation is asynchronous and may take a few minutes
- The PDF will be sent to the authenticated user's email
- The `reportId` can be used to track the report status (if supported in the future)

---

### 2. Report Service Health

Check if the report service is running.

**Endpoint:** `GET /api/reports/health`

**Response (200 OK):**
```json
{
  "message": "Report service is running"
}
```

**Headers:** None required

---

## Error Handling

### Error Response Format

All errors follow a consistent format:

```json
{
  "status": 400,
  "title": "Invalid receipt data",
  "message": "Property percentages must sum to 100, but got 90"
}
```

### Common Error Scenarios

**1. Missing Authentication Token**
```json
{
  "status": 401,
  "message": "Unauthorized: Missing or invalid JWT token"
}
```

**2. Invalid JSON Body**
```json
{
  "status": 400,
  "message": "Invalid JSON in request body"
}
```

**3. Validation Error**
```json
{
  "status": 400,
  "title": "Validation Error",
  "message": "Amount must be greater than 0"
}
```

**4. Resource Not Found**
```json
{
  "status": 404,
  "message": "Receipt with ID 999 not found"
}
```

**5. Server Error**
```json
{
  "status": 500,
  "message": "Internal server error. Please try again later."
}
```

---

## Response Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created successfully |
| 202 | Accepted | Asynchronous request accepted (e.g., report generation) |
| 204 | No Content | Request succeeded but no content to return (e.g., delete) |
| 400 | Bad Request | Invalid request data or validation error |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Authenticated but not authorized to access resource |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists or conflict in data |
| 500 | Internal Server Error | Server-side error |
| 503 | Service Unavailable | Service temporarily unavailable |

---

## Best Practices

### 1. Authentication
- Always include the JWT token in the `Authorization` header
- Tokens expire after 24 hours; refresh by logging in again
- Never share your authentication token

### 2. Pagination
- Use the `page` and `size` parameters to retrieve large datasets
- Default size is 100; use smaller sizes for better performance
- Maximum page size is 500

### 3. Date Format
- All dates must be in ISO 8601 format: `YYYY-MM-DDTHH:mm:ssZ`
- Example: `2026-01-27T10:30:00Z`

### 4. Request/Response
- Always set `Content-Type: application/json` for POST/PUT requests
- Check the response status code before processing the response body
- Implement exponential backoff for retrying failed requests

### 5. Property Percentages
- Percentages must be numeric values
- Sum of all property percentages must equal exactly 100
- Each property allocation should represent a portion of the receipt amount

### 6. Error Handling
- Always check for error responses before using the data
- Log errors for debugging purposes
- Provide meaningful error messages to end users

---

## Rate Limiting

Currently, there are no enforced rate limits. However, it's recommended to:
- Implement client-side throttling (max 10 requests per second)
- Use pagination to reduce data transfer
- Cache responses when appropriate

---

## Support and Feedback

For issues, questions, or feedback, please contact the development team or submit an issue in the project repository.

---

**Last Updated:** January 27, 2026  
**Version:** 1.0

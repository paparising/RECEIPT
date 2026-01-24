# Spring Security & JWT Token Implementation

## Overview
The Receipt application now includes comprehensive Spring Security with JWT (JSON Web Token) authentication and role-based access control.

## Components Added

### 1. Entities
- **User.java** - User entity with username, password, email, and role associations
- **Role.java** - Role entity with name and description (USER, ADMIN)

### 2. Repositories
- **UserRepository** - Custom queries for user lookup by username and email
- **RoleRepository** - Role lookup by name

### 3. Security Components
- **JwtTokenProvider** - JWT token generation and validation
- **JwtAuthenticationFilter** - Filter to extract and validate JWT tokens from requests
- **CustomUserDetailsService** - Custom implementation to load user details from database
- **JwtAuthenticationEntryPoint** - Handles authentication exceptions with JSON response

### 4. Configuration
- **SecurityConfig** - Spring Security configuration with JWT filter integration
  - Session management set to STATELESS (no sessions needed with JWT)
  - CSRF protection disabled (stateless API doesn't need CSRF protection)
  - JWT filter added to filter chain

### 5. Authentication Service
- **AuthenticationService** - Handles user login and signup
  - Login: Authenticates user and generates JWT token
  - Signup: Creates new user with default USER role

### 6. Controllers
- **AuthController** - REST endpoints for authentication
  - POST /api/auth/login - Authenticate and get JWT token
  - POST /api/auth/signup - Register new user

### 7. DTOs
- **LoginRequest** - Username and password for login
- **SignupRequest** - Username, email, and password for registration
- **JwtResponse** - JWT token response with user info

## API Endpoints

### Authentication Endpoints (Public)
```
POST /api/auth/login
Body: {
  "username": "user",
  "password": "password"
}
Response: {
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "user",
  "email": "user@example.com"
}

POST /api/auth/signup
Body: {
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123"
}
```

### Protected Endpoints (Require JWT Token)
```
GET /api/properties/{propertyId}/receipts?year=2024
Headers: Authorization: Bearer <jwt_token>
```

## Usage Flow

1. **User Registration**
   ```bash
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "username": "john_doe",
       "email": "john@example.com",
       "password": "securePassword123"
     }'
   ```

2. **User Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "john_doe",
       "password": "securePassword123"
     }'
   ```
   
   Response:
   ```json
   {
     "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNkMhNKw7Ow5...",
     "type": "Bearer",
     "id": 1,
     "username": "john_doe",
     "email": "john@example.com"
   }
   ```

3. **Access Protected Resource**
   ```bash
   curl -X GET http://localhost:8080/api/properties/1/receipts \
     -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNkMhNKw7Ow5..."
   ```

## Configuration Properties

Add to `application.properties`:
```properties
# JWT Configuration
app.jwtSecret=mySecretKeyForJWTTokenGenerationAndValidation123456789012345678901234567890
app.jwtExpirationMs=86400000
```

## Security Features

1. **Password Encryption** - Passwords are encrypted using BCryptPasswordEncoder
2. **JWT Token Validation** - Tokens are validated on every request
3. **Role-Based Access Control** - Endpoints can be restricted to specific roles
4. **STATELESS Authentication** - No session data stored on server
5. **Authorization Header** - Tokens sent via standard Authorization header with Bearer scheme

## Database Schema

The application will create the following tables:
- `users` - User accounts
- `roles` - Available roles
- `user_roles` - Join table for user-role relationships

## Default Roles
- `USER` - Standard user with access to property and receipt endpoints
- `ADMIN` - Administrative user with full access

## Exception Handling
- Invalid credentials → HTTP 401 (Unauthorized)
- Missing token → HTTP 401 (Unauthorized)
- Expired token → HTTP 401 (Unauthorized)
- Invalid token format → HTTP 401 (Unauthorized)
- User not found → HTTP 400 (Bad Request) during signup

## Next Steps
- Initialize database with roles (INSERT INTO roles VALUES (1, 'USER', 'Standard User'), (2, 'ADMIN', 'Administrator'))
- Configure environment-specific JWT secret in production
- Set appropriate JWT expiration time (86400000 ms = 24 hours)
- Add role-based endpoint restrictions using @PreAuthorize annotations
- Implement refresh token mechanism for long-lived sessions

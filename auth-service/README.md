# Auth Service

Authentication and authorization service for Cash Manager using Keycloak integration.

## Features

- User registration
- User login with JWT tokens
- Token refresh
- User logout
- Get current user information
- Keycloak Admin API integration

## API Endpoints

### Public Endpoints (No Authentication Required)

#### POST /api/v1/auth/register
Register a new user.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "emailVerified": false
}
```

#### POST /api/v1/auth/login
Authenticate user and get access tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_expires_in": 1800
}
```

#### POST /api/v1/auth/refresh
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_expires_in": 1800
}
```

#### POST /api/v1/auth/logout
Logout user and invalidate refresh token.

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (204 No Content)**

### Protected Endpoints (Authentication Required)

#### GET /api/v1/auth/me
Get current authenticated user information.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "emailVerified": false
}
```

## Keycloak Setup

### 1. Access Keycloak Admin Console
- URL: http://localhost:9090
- Username: admin
- Password: admin

### 2. Create Realm
1. Click "Create Realm" button
2. Enter realm name: `cash-manager`
3. Click "Create"

### 3. Create Client
1. Navigate to Clients → Create Client
2. Client type: OpenID Connect
3. Client ID: `cash-manager-frontend`
4. Click "Next"

**Capability config:**
- Client authentication: OFF (public client)
- Authorization: OFF
- Authentication flow:
  - Standard flow: ON
  - Direct access grants: ON
  - Implicit flow: OFF
  - Service accounts roles: OFF

5. Click "Next"

**Login settings:**
- Root URL: http://localhost:5173
- Home URL: http://localhost:5173
- Valid redirect URIs:
  - http://localhost:5173/*
  - http://localhost:8080/*
- Valid post logout redirect URIs: +
- Web origins:
  - http://localhost:5173
  - http://localhost:8080

6. Click "Save"

### 4. Configure Realm Settings (Optional)

**Tokens:**
- Access Token Lifespan: 5 minutes (default 300s)
- Refresh Token Max Reuse: 0
- SSO Session Idle: 30 minutes
- SSO Session Max: 10 hours

**Login:**
- User registration: ON (if you want self-registration)
- Forgot password: ON (if needed)
- Remember me: ON (if needed)

## Environment Variables

The following environment variables are required:

```bash
KEYCLOAK_SERVER_URL=http://localhost:9090
KEYCLOAK_REALM=cash-manager
KEYCLOAK_ADMIN_CLIENT_ID=admin-cli
KEYCLOAK_ADMIN_CLIENT_SECRET=
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_CLIENT_ID=cash-manager-frontend
KEYCLOAK_CLIENT_SECRET=

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cash_manager
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

## Running the Service

### With Docker Compose
```bash
docker-compose up auth-service
```

The service will be available at http://localhost:8082

### Locally (for development)
```bash
cd auth-service
./gradlew bootRun
```

## Testing

### Build and Test
```bash
./gradlew clean build
./gradlew test
```

### Manual Testing with curl

**Register:**
```bash
curl -X POST http://localhost:8082/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Get Current User:**
```bash
curl -X GET http://localhost:8082/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Error Handling

The service returns standardized error responses:

```json
{
  "timestamp": "2025-11-29T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "validationErrors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

## Security

- Stateless authentication with JWT
- Password validation (minimum 8 characters)
- Email validation
- CSRF disabled (stateless API)
- OAuth2 Resource Server for JWT validation
- All endpoints except auth operations require valid JWT token

## Architecture

- **Controller Layer:** AuthController - REST API endpoints
- **Service Layer:** KeycloakService - Business logic and Keycloak integration
- **Configuration:** KeycloakConfig, SecurityConfig
- **DTOs:** Request/Response objects with validation
- **Exception Handling:** Global exception handler with custom exceptions

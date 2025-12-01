# Cash Manager

Personal finance management platform with microservices architecture, transaction imports, automatic categorization, and analytics.

## Quick Start

### 1. Prerequisites

- Docker & Docker Compose
- Node.js 20+ (for local frontend development)
- Java 21+ (for local backend development)

### 2. Clone and Setup

```bash
git clone <repository-url>
cd cash-manager
```

The `.env` file is already configured with default values. For production, update sensitive values.

### 3. Start All Services

```bash
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Keycloak (port 9090)
- Database migrations
- Auth Service (port 8082)
- Finance Service (port 8092)
- Frontend (port 5173)

### 4. Configure Keycloak

**Important:** You must configure Keycloak before using the application.

Follow the complete guide: [docs/keycloak-setup.md](docs/keycloak-setup.md)

Quick setup:
1. Access Keycloak Admin: http://localhost:9090 (admin/admin)
2. Create realm: `cash-manager`
3. Create client: `cash-manager-frontend`
4. (Optional) Configure GitHub OAuth
5. (Optional) Configure Google OAuth

### 5. Access Application

- **Frontend**: http://localhost:5173
- **Keycloak Admin**: http://localhost:9090
- **Auth Service**: http://localhost:8082
- **Finance Service**: http://localhost:8092

## Services

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| Frontend | 5173 | ✅ Active | React SPA with Tailwind CSS |
| Auth Service | 8082 | ✅ Active | Authentication & user management |
| Finance Service | 8092 | ✅ Active | Transaction CRUD operations |
| Keycloak | 9090 | ✅ Active | OAuth2/OpenID Connect provider |
| PostgreSQL | 5432 | ✅ Active | Primary database |

## Authentication

The application supports multiple authentication methods:

1. **Email/Password** - Standard registration and login
2. **Google OAuth** - Login with Google account
3. **GitHub OAuth** - Login with GitHub account

All authentication is handled by Keycloak. The frontend integrates via `keycloak-js` library, and the backend provides REST API for user management.

### Auth Service Endpoints

- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login with credentials
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout user
- `GET /api/v1/auth/me` - Get current user info

See [auth-service/README.md](auth-service/README.md) for API details.

## Environment Variables

The `.env` file contains all configuration. Key variables:

### Database
```bash
POSTGRES_PORT=5432
POSTGRES_DATABASE=cash_manager
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=postgres
```

### Keycloak
```bash
KEYCLOAK_HTTP_PORT=9090
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_REALM=cash-manager
KEYCLOAK_CLIENT_ID=cash-manager-frontend
```

### OAuth Providers (Optional)
```bash
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
```

**To enable OAuth:** Fill in these values after creating OAuth apps. See [docs/keycloak-setup.md](docs/keycloak-setup.md).

## Development

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

Frontend uses:
- React 19 + TypeScript
- Vite for build
- Tailwind CSS
- Keycloak JS for authentication
- React Query for server state

### Backend Development

**Auth Service:**
```bash
cd auth-service
./gradlew bootRun
```

**Finance Service:**
```bash
cd finance-service
./gradlew bootRun
```

### Database Migrations

Migrations are managed by Liquibase and run automatically on startup.

To run manually:
```bash
docker-compose up migration
```

## Documentation

- [CLAUDE.md](CLAUDE.md) - Project overview and development guidelines
- [docs/keycloak-setup.md](docs/keycloak-setup.md) - Complete Keycloak setup guide
- [auth-service/README.md](auth-service/README.md) - Auth Service API documentation
- [docs/architecture.md](docs/architecture.md) - System architecture (Russian)
- [docs/services.md](docs/services.md) - Service details (Russian)
- [docs/datamodels.md](docs/datamodels.md) - Database schema (Russian)

## Troubleshooting

### Keycloak connection errors

Ensure Keycloak is running and accessible:
```bash
docker-compose logs -f keycloak
```

### Frontend can't connect to backend

Check that all services are running:
```bash
docker-compose ps
```

### Database migration errors

Rebuild and restart:
```bash
docker-compose down
docker-compose up migration
```

### OAuth login not working

1. Verify OAuth app credentials in `.env`
2. Check Keycloak Identity Provider configuration
3. Ensure redirect URLs match exactly
4. See [docs/keycloak-setup.md](docs/keycloak-setup.md) troubleshooting section

## Project Structure

```
cash-manager/
├── auth-service/          # Authentication service (Java/Spring Boot)
├── finance-service/       # Finance/transaction service (Java/Spring Boot)
├── frontend/              # React frontend application
├── infrastructure/        # Database migrations and configs
│   └── database/
│       └── migration/     # Liquibase migrations
├── docs/                  # Documentation
└── docker-compose.yaml    # Service orchestration
```

## Technology Stack

- **Frontend**: React 19, TypeScript, Vite, Tailwind CSS
- **Backend**: Java 21, Spring Boot 4, Spring Security
- **Authentication**: Keycloak (OAuth2/OpenID Connect)
- **Database**: PostgreSQL 15
- **Migrations**: Liquibase
- **Containerization**: Docker & Docker Compose

## License

[Add your license here]
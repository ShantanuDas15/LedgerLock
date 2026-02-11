# LedgerLock - Banking-as-a-Service Simulation

A portfolio project demonstrating institutional-grade financial consistency with ACID-compliant transactions.

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.9+ (or use the included wrapper)

### Local Development

1. **Start the database:**

   ```bash
   docker-compose -f infra/docker-compose.yml up -d
   ```

2. **Run the backend:**

   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Test the API:**

   ```bash
   # Health check
   curl http://localhost:8080/api/v1/auth/health

   # Register a new user
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"SecurePass123!"}'

   # Login
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"SecurePass123!"}'
   ```

### Running Tests

```bash
cd backend
./mvnw test
```

## 📁 Project Structure

```
LedgerLock/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/ledgerlock/
│   │   ├── config/            # Security, CORS, Exception handling
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # Request/Response objects
│   │   ├── exception/         # Custom exceptions
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Data access layer
│   │   ├── security/          # JWT authentication
│   │   └── service/           # Business logic
│   └── src/test/              # Unit & integration tests
├── frontend/                   # Flutter Web (Phase 3)
├── infra/                      # Infrastructure
│   ├── docker-compose.yml     # Local development
│   └── k8s/                   # Kubernetes manifests (portfolio signal)
└── .github/workflows/         # CI/CD pipelines
```

## 🔐 API Endpoints

| Method | Endpoint                  | Description             | Auth |
| ------ | ------------------------- | ----------------------- | ---- |
| POST   | `/api/v1/auth/register`   | Create user & wallet    | ❌   |
| POST   | `/api/v1/auth/login`      | Get JWT token           | ❌   |
| GET    | `/api/v1/wallet/balance`  | Get current balance     | ✅   |
| POST   | `/api/v1/wallet/transfer` | Send money              | ✅   |
| GET    | `/api/v1/transactions`    | Get history (paginated) | ✅   |

## 🏗️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database:** PostgreSQL (Neon serverless in production)
- **Authentication:** JWT (jjwt library)
- **Testing:** JUnit 5, Mockito, MockMvc
- **Migrations:** Flyway
- **Frontend:** Flutter Web (Phase 3)
- **Deployment:** Cloud Run, Firebase Hosting

## ✅ Phase 1 Checklist

- [x] Spring Boot project initialization
- [x] Flyway database migrations
- [x] JPA entities (User, Account, Transaction)
- [x] JWT authentication
- [x] Register/Login endpoints
- [x] Unit tests for services and controllers
- [x] Docker Compose for local development

## 📝 License

MIT License - See LICENSE file for details.

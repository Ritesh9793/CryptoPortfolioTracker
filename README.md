# Project Title - Crypto Tracker and Scam Detection
## Crypto Portfolio Tracker Backend - Ritesh Gupta
### Overview
- This project is developed as part of the **Infosys Springboard Virtual Internship Program**. The objective is to design and implement a backend service capable of managing and tracking cryptocurrency portfolios for users. The system allows users to store, update, and retrieve their crypto asset details in a structured and secure manner.

### Problem Statement
- With the rising popularity of digital assets, users often face challenges in monitoring their crypto investments across multiple platforms. Existing solutions are either complex, paid, or lack customization. This project aims to build a simple, reliable, and scalable backend that helps users maintain their crypto holdings in one place with easy access and efficient data management.

### Project Objective 
- Build a robust **Spring Boot MVC** backend following clean architecture principles.
- Design secure and efficient **REST APIs** for portfolio and asset management.
- Implement CRUD functionality for user portfolios.
- Store and manage data using **MySQL** relational database.
- Test and validate all endpoints using **Postman**.

### Scope of work
- Design and develop REST controllers for portfolio operations.
- Implement service-layer logic for business operations.
- Create database models and define entity relationships.
- Configure MySQL database integration with Spring Boot.
- Build and document API endpoints for user operations.
- Validate API functionality through Postman test cases.

### Tech Stack Used
- **Backend Framework**: Java, Spring Boot MVC
- **Security**: Spring Security, AES, JWT
- **ORM**: Hibernate/JPA
- **Database**: MySQL
- **API Testing**: Postman
- **Build & Dependency Tool**: Maven
- **Version Control**: Git & GitHub

### Expected Deliverables 
- Fully functional Spring Boot application with **CRUD APIs**.
- MySQL database schema and tables.
- Postman collection for API testing.
- Project documentation including architecture and workflow.

### Project Architecture
  Controller → Service → Repository → Database
                  ↓
           Security (JWT + AES)
- **Controller Layer**: Handles HTTP Requests
- **Service Layer**: Business Logic
- **Repository Layer**: Database Interaction
- **Security Layer**: Authentication and Encryption

### Security Features
- JWT authentication (stateless)
- BCrypt password hashing
- AES-256 encryption for API secrets
- Role-based endpoint protection
- Secure HTTP headers
- No session-based authentication

### API Endpoints Overview
#### Authentication APIs

| Method | Endpoint             | Description       |
| ------ | -------------------- | ----------------- |
| POST   | `/api/auth/register` | Register new user |
| POST   | `/api/auth/login`    | Login & get JWT   |

#### API Key Management

| Method | Endpoint        | Description          |
| ------ | --------------- | -------------------- |
| POST   | `/api/api-keys` | Add exchange API key |
| GET    | `/api/api-keys` | Get stored API keys  |

#### Portfolio and Holdings

| Method | Endpoint              | Description   |
| ------ | --------------------- | ------------- |
| GET    | `/api/holdings`       | View holdings |
| GET    | `/api/prices/latest`  | Latest prices |
| GET    | `/api/prices/history` | Price history |

#### PnL Analysis

| Method | Endpoint              | Description     |
| ------ | --------------------- | --------------- |
| GET    | `/api/pnl/realized`   | Realized PnL    |
| GET    | `/api/pnl/unrealized` | Unrealized PnL  |
| GET    | `/api/pnl/summary`    | Overall summary |

#### Risk and Scam Detection

| Method | Endpoint           | Description     |
| ------ | ------------------ | --------------- |
| GET    | `/api/risk`        | Risk analysis   |
| POST   | `/api/risk`        | Run risk checks |
| GET    | `/api/risk-alerts` | View alerts     |

#### Reports

| Method | Endpoint          | Description |
| ------ | ----------------- | ----------- |
| GET    | `/api/report/csv` | Export CSV  |
| GET    | `/api/report/pdf` | Export PDF  |

### API Testing - Postman
1. **Testing Setup**
- Import Postman Collection
- Create environment variable:
        token = <JWT_TOKEN>
- Use header:
        Authorization: Bearer {{token}}
2. **Sample Test Flow**
- Register User
- Login → Get JWT
- Add Exchange API Key
- Fetch Holdings
- View Prices
- Generate Reports
- Trigger Risk Analysis

#### Sample Request - Login
{
  "email": "user@example.com",
  "password": "password123"
}

#### Sample Response
{
  "userId": 3,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}

### Application Configuration
Key configurations in application.properties:
- MySQL datasource
- JWT secret & expiration
- AES encryption key
- External API URLs
- Email SMTP settings

### How To Run The Project
git clone https://github.com/Ritesh9793/CryptoPortfolioTracker
cd CP-Tracker
mvn clean install
mvn spring-boot:run

**Access Api's at : http://localhost:8080**

### Future Enhancements
- Refresh tokens
- Role-based access (ADMIN / USER)
- WebSocket real-time updates
- Advanced scam detection
- Frontend integration (React / Angular)
- Cloud deployment (AWS)

### Author
**Ritesh Gupta**
Infosys Springboard Virtual Internship Program





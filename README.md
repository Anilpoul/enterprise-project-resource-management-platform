# Enterprise Project & Resource Management Platform

A production-grade enterprise microservices platform inspired by Azure DevOps and Jira for managing projects, resources, teams, KPIs, analytics, and enterprise workflows.

---

# 🚀 Project Overview

This platform is designed for organizations to efficiently manage:

- Projects
- Teams
- Sprint Planning
- Task Allocation
- Employee Utilization
- KPI Tracking
- Notifications
- Analytics
- Reporting

The system follows modern enterprise architecture patterns using:

- Microservices Architecture
- Event-Driven Architecture
- API Gateway Pattern
- JWT Authentication & RBAC
- Distributed Caching
- Centralized Logging
- Monitoring & Observability

---

# 🏗️ Architecture

## High-Level Architecture

```text
                        ┌─────────────────────┐
                        │      React UI       │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │     API Gateway     │
                        └──────────┬──────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        ▼                          ▼                          ▼

┌─────────────────┐     ┌─────────────────┐      ┌─────────────────┐
│   Auth Service  │     │ Project Service │      │ Sprint Service  │
└─────────────────┘     └─────────────────┘      └─────────────────┘

        ▼                          ▼                          ▼

┌─────────────────┐     ┌─────────────────┐      ┌─────────────────┐
│ PostgreSQL DB   │     │ PostgreSQL DB   │      │ PostgreSQL DB   │
└─────────────────┘     └─────────────────┘      └─────────────────┘

                ┌─────────────────────────────┐
                │         Kafka Broker        │
                └─────────────────────────────┘

                ┌─────────────────────────────┐
                │            Redis            │
                └─────────────────────────────┘
```

---

# 🧩 Microservices

| Service | Responsibility |
|---|---|
| API Gateway | Routing, authentication filter, centralized entry point |
| Config Server | Centralized configuration management |
| Discovery Server | Service discovery using Eureka |
| Auth Service | JWT authentication, RBAC, refresh tokens |
| Project Service | Project & team management |
| Sprint Service | Sprint planning & tracking |
| Task Service | Task lifecycle management |
| Notification Service | Email & system notifications |
| Analytics Service | KPI & productivity analytics |
| Audit Service | Audit logging & activity tracking |

---

# 🛠️ Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud
- Spring Data JPA
- Spring Validation
- Spring Web
- Spring Kafka
- Spring Cache
- Spring AOP

## Database

- PostgreSQL

## Caching

- Redis

## Messaging

- Apache Kafka

## Infrastructure

- Docker
- Docker Compose

## API Documentation

- Swagger / OpenAPI

## Frontend (Planned)

- React
- Tailwind CSS
- Axios
- React Query

## Monitoring (Planned)

- Prometheus
- Grafana
- ELK Stack

---

# 🔐 Security Features

- JWT Authentication
- Refresh Token Rotation
- Role-Based Access Control (RBAC)
- Permission-Based Authorization
- BCrypt Password Encryption
- Redis Token Blacklisting
- Stateless Authentication
- Secure API Gateway Validation

---

# 👥 User Roles

| Role | Responsibilities |
|---|---|
| Admin | Full platform access |
| Manager | Manage projects, teams, and allocations |
| Team Lead | Sprint and task management |
| Team Member | Task execution and updates |

---

# 📦 Core Features

## Authentication & Authorization

- Login/Register
- JWT Access Token
- Refresh Token
- Logout
- RBAC
- Permission Management

## Project Management

- Create Projects
- Team Assignment
- Resource Allocation

## Sprint Management

- Sprint Creation
- Sprint Planning
- Sprint Tracking

## Task Management

- Task Assignment
- Status Workflow
- Priority Management

## Resource Management

- Utilization Tracking
- Capacity Planning
- Bench Tracking

## Analytics & Reporting

- KPI Dashboards
- Productivity Reports
- Team Performance Metrics

## Notifications

- Email Notifications
- Event-Driven Alerts

## Audit Logs

- User Activity Tracking
- Security Audit Logs

---

# 📁 Monorepo Structure

```text
enterprise-project-resource-management-platform/
│
├── infrastructure/
│   ├── api-gateway/
│   ├── config-server/
│   └── discovery-server/
│
├── services/
│   ├── auth-service/
│   ├── project-service/
│   ├── sprint-service/
│   ├── task-service/
│   ├── notification-service/
│   ├── analytics-service/
│   └── audit-service/
│
├── docker/
│   ├── compose/
│   └── postgres/
│
├── monitoring/
│
├── k8s/
│
├── docs/
│
└── pom.xml
```

---

# ⚙️ Running the Project

## Prerequisites

- Java 21
- Docker Desktop
- Git

---

## Clone Repository

```bash
git clone https://github.com/Anilpoul/enterprise-project-resource-management-platform.git
```

---

## Build Project

```bash
./mvnw clean package -DskipTests
```

---

## Start Infrastructure

```bash
cd docker/compose
docker compose up --build
```

---

# 🌐 Access Services

| Service | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Auth Service Swagger | http://localhost:8081/swagger-ui.html |

---

# 🐳 Docker Infrastructure

The platform runs using Docker Compose with:

- PostgreSQL
- Redis
- Kafka
- Zookeeper
- Config Server
- Discovery Server
- API Gateway
- Auth Service

---

# 📡 Event-Driven Architecture

Kafka is used for:

- Authentication Events
- Notification Events
- Audit Events
- Project Events
- Task Events

---

# 📚 API Documentation

Swagger/OpenAPI is enabled for all services.

Example:

```text
http://localhost:8081/swagger-ui.html
```

---

# 🧪 Testing

Planned:

- Unit Testing
- Integration Testing
- Testcontainers
- API Testing
- Security Testing

---

# 📈 Monitoring & Observability (Upcoming)

- Prometheus
- Grafana
- ELK Stack
- Distributed Tracing
- Correlation IDs
- Micrometer Metrics

---

# 🚀 CI/CD (Upcoming)

Planned:

- GitHub Actions
- Docker Registry
- Kubernetes Deployment
- Helm Charts
- Automated Testing Pipeline

---

# 🎯 Learning Goals

This project is built to demonstrate:

- Enterprise Backend Development
- Microservices Architecture
- Distributed Systems
- Secure Authentication Systems
- Event-Driven Architecture
- DevOps & Containerization
- Production-Grade Coding Standards

---

# 👨‍💻 Author

## Anil Poul

GitHub:  
https://github.com/Anilpoul

---

# ⭐ Future Enhancements

- React Frontend
- Kubernetes Deployment
- AI-based Resource Allocation
- Real-time Notifications
- WebSocket Integration
- Multi-Tenant Architecture
- SSO Integration
- Terraform Infrastructure

---

# 📄 License

This project is for educational and portfolio purposes.

# Enterprise Project & Resource Management Platform

[![CI Pipeline](https://github.com/Anilpoul/enterprise-project-resource-management-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/Anilpoul/enterprise-project-resource-management-platform/actions/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.3-blue.svg)](https://spring.io/projects/spring-cloud)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![Tests](https://img.shields.io/badge/Tests-311%20Passed-success.svg)](file:///d:/enterprise-platform-parent/enterprise-platform-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A production-grade, multi-tenant enterprise microservices platform inspired by Azure DevOps, Jira, and Linear for managing organizations, projects, resources, teams, sprints, tasks, KPIs, notifications, and immutable compliance auditing.


---

## 🚀 Platform Overview

The platform provides complete end-to-end capabilities for modern software organizations:

- **Multi-Tenant Governance**: Organization onboarding, member invitations, and tenant isolation via `TenantContext` (`X-Organization-Id`).
- **User & Department Management**: Employee profiles, designations, and departmental hierarchies.
- **Project Life Cycle Management**: Workspaces, project memberships, and access controls.
- **Task Workflow Engine**: Kanban & Scrum workflows, issue hierarchies, priorities, assignments, and discussions.
- **Sprint & Agile Planning**: Sprint lifecycles, velocity metrics, and interactive Scrum boards.
- **Resource Allocation & Capacity**: Real-time workload allocation, over-allocation alerts (>100%), and bench tracking.
- **Executive Analytics & Reporting**: Organization KPI dashboards, project health indicators, sprint velocity, and employee scorecards.
- **Multi-Channel Notifications**: In-app alert inbox and simulated email dispatcher with user preference controls.
- **Compliance & Immutable Audit Trail**: Append-only activity logging, forensic entity timelines, and aggregate compliance summaries.

---

## 🏗️ Architecture & Technology Stack

The platform is designed around cloud-native, event-driven microservice patterns:

- **Java 21** & **Spring Boot 3.3.5**
- **Spring Cloud 2023.0.3** (Eureka Discovery, Config Server, Spring Cloud Gateway)
- **Spring Data JPA & Hibernate 6** with **Flyway** database migrations
- **PostgreSQL 17** with dedicated databases per service
- **Apache Kafka** for asynchronous, event-driven domain communication
- **Redis** for distributed caching and JWT blacklisting
- **Springdoc OpenAPI / Swagger UI 2.5.0** documentation on every service
- **MapStruct & Lombok** for clean, boilerplate-free data transformation

```text
                        ┌────────────────────────┐
                        │   Web Client / React   │
                        └───────────┬────────────┘
                                    │
                                    ▼
                        ┌────────────────────────┐
                        │   API Gateway (8080)   │
                        └───────────┬────────────┘
                                    │
      ┌────────────────┬────────────┼────────────┬────────────────┐
      ▼                ▼            ▼            ▼                ▼
┌────────────┐  ┌────────────┐┌────────────┐┌────────────┐  ┌────────────┐
│Auth Service│  │Org Service ││Proj Service││Task Service│  │Audit Svc  │
│   (8081)   │  │   (8083)   ││   (8084)   ││   (8085)   │  │   (8090)   │
└─────┬──────┘  └─────┬──────┘└─────┬──────┘└─────┬──────┘  └─────┬──────┘
      │               │             │             │               │
      └───────────────┼─────────────┴─────────────┼───────────────┘
                      ▼                           ▼
          ┌────────────────────────┐  ┌────────────────────────┐
          │  Apache Kafka Broker   │  │   PostgreSQL Engine    │
          │   (Domain Event Bus)   │  │ (Independent Databases)│
          └────────────────────────┘  └────────────────────────┘
```

---

## 🧩 Microservices Directory

| Service | Port | Database | Responsibilities | Swagger UI |
| :--- | :---: | :---: | :--- | :--- |
| **`api-gateway`** | 8080 | - | Centralized routing, authentication filtering, rate limiting | - |
| **`config-server`** | 8888 | - | Spring Cloud centralized configuration repository | - |
| **`discovery-server`**| 8761 | - | Netflix Eureka service discovery & health monitoring | `http://localhost:8761` |
| **`auth-service`** | 8081 | `auth_db` | JWT authentication, refresh token rotation, RBAC | `http://localhost:8081/swagger-ui.html` |
| **`user-service`** | 8082 | `user_db` | User profiles, departments, employee designations | `http://localhost:8082/swagger-ui.html` |
| **`organization-service`** | 8083 | `organization_db` | Multi-tenancy, org lifecycle, membership roles | `http://localhost:8083/swagger-ui.html` |
| **`project-service`** | 8084 | `project_db` | Project workspaces, member access controls | `http://localhost:8084/swagger-ui.html` |
| **`task-service`** | 8085 | `task_db` | Task lifecycle, priority, comments, workflows | `http://localhost:8085/swagger-ui.html` |
| **`sprint-service`** | 8086 | `sprint_db` | Agile sprints, Kanban/Scrum boards, columns | `http://localhost:8086/swagger-ui.html` |
| **`resource-service`** | 8087 | `resource_db` | Resource capacity, allocation percentage, bench | `http://localhost:8087/swagger-ui.html` |
| **`analytics-service`**| 8088 | `analytics_db` | KPI dashboards, project health, team scorecards | `http://localhost:8088/swagger-ui.html` |
| **`notification-service`** | 8089 | `notification_db` | In-app alerts, email stubs, user preferences | `http://localhost:8089/swagger-ui.html` |
| **`audit-service`** | 8090 | `audit_db` | Immutable compliance activity logs, timelines | `http://localhost:8090/swagger-ui.html` |
| **`frontend`** | 3000 | - | React 18 SPA (Kanban, Sprints, Dashboards, Personas) | `http://localhost:3000` |

---

## 📡 Event-Driven Architecture (Kafka)

Microservices communicate asynchronously via Apache Kafka topics defined in the `event-contracts` shared library:

- **`auth-events`**: User registration, login, logout, password updates.
- **`org-events`**: Organization creation, updates, member status changes.
- **`project-events`**: Project creation, status transitions, member additions.
- **`task-events`**: Task creation, assignments, status transitions, comment additions.
- **`sprint-events`**: Sprint created, started, completed, velocity updates.
- **`resource-events`**: Work allocations, capacity shifts, over-allocation warnings.
- **`audit-events`**: Manual or system compliance audit records.

---

## 🧪 Automated Testing & Quality Metrics

The entire Maven multi-module reactor compiles cleanly and executes **311 automated tests with 100% pass rate**:

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for enterprise-platform-parent 1.0.0-SNAPSHOT:
[INFO] 
[INFO] enterprise-platform-parent ......................... SUCCESS
[INFO] config-server ...................................... SUCCESS
[INFO] discovery-server ................................... SUCCESS
[INFO] api-gateway ........................................ SUCCESS
[INFO] event-contracts .................................... SUCCESS
[INFO] auth-service ....................................... SUCCESS [14 tests]
[INFO] user-service ....................................... SUCCESS [ 8 tests]
[INFO] organization-service ............................... SUCCESS [38 tests]
[INFO] project-service .................................... SUCCESS [52 tests]
[INFO] task-service ....................................... SUCCESS [37 tests]
[INFO] sprint-service ..................................... SUCCESS [36 tests]
[INFO] resource-service ................................... SUCCESS [42 tests]
[INFO] analytics-service .................................. SUCCESS [29 tests]
[INFO] notification-service ............................... SUCCESS [35 tests]
[INFO] audit-service ...................................... SUCCESS [20 tests]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS (311 Tests Passed, 0 Failures, 0 Skipped)
[INFO] ------------------------------------------------------------------------
```

---

## ⚙️ Getting Started

### Prerequisites

- **Java 21** (JDK 21+)
- **Maven 3.9+**
- **Docker Desktop**
- **PostgreSQL 17** & **Apache Kafka**

### Build the Full Reactor

```bash
mvn clean test
```

### Run Infrastructure with Docker Compose

```bash
cd docker/compose
cp .env.example .env
docker compose up -d
```

### Run React Frontend Application

The platform includes a modern React 18 frontend built with Vite, Vanilla CSS design tokens, dark mode, and an interactive Kanban board:

```bash
cd frontend
npm install
npm run dev
```

- **Frontend URL**: `http://localhost:3000`
- **Proxy Configuration**: Automatically proxies `/api/*` to Spring Cloud API Gateway on `http://localhost:8080`.

### 👥 Pre-Configured Demo Personas

The application includes built-in 1-click persona switching and pre-seeded credentials for instant local evaluation:

| Persona | Name | Role & Permissions | Email | Password |
| :--- | :--- | :--- | :--- | :--- |
| 👑 **Administrator** | Sarah Connor | Platform Administrator (Full Access) | `admin@enterprise.com` | `Admin@123` |
| 📋 **Project Manager** | Alex Chen | Senior Project Manager / Scrum Master | `pm@enterprise.com` | `Pm@123` |
| 💻 **Staff Engineer** | Elena Rostova | Principal Cloud Architect / Tech Lead | `engineer@enterprise.com` | `Dev@123` |

---

## 📂 Repository Structure

```text
enterprise-platform-parent/
├── .github/
│   └── workflows/              # GitHub Actions CI/CD workflows (ci, publish, smoke-test)
├── docker/
│   └── compose/                # Multi-service Docker Compose orchestration (.env.example)
├── frontend/                   # React 18 Single Page Application (Vite, Vanilla CSS)
│   ├── src/
│   │   ├── api/                # Axios gateway client & offline mock fallback
│   │   ├── components/         # Modals, Navbar, Sidebar, NotificationDrawer
│   │   ├── context/            # AuthContext, TenantContext, NotificationContext
│   │   └── pages/              # Dashboard, Projects, Board, Sprints, Resources, Audit...
│   ├── Dockerfile              # Multi-stage production build (Node -> Nginx)
│   └── nginx.conf              # SPA routing & API reverse proxy configuration
├── infrastructure/
│   ├── api-gateway/            # Reactive Spring Cloud Gateway (Port 8080)
│   ├── config-server/          # Spring Cloud Config Server (Port 8888)
│   └── discovery-server/       # Netflix Eureka Service Registry (Port 8761)
├── k8s/                        # Declarative Kubernetes manifests (Kustomize bundle)
│   ├── infrastructure/         # Postgres, Kafka/Zookeeper, Redis
│   ├── services/               # Microservice Deployments & Services (Ports 8081-8090)
│   └── frontend/               # React Frontend Deployment, Service & Ingress
├── scripts/                    # Cross-platform local CI validation (PowerShell & Bash)
├── services/                   # Core Business Domain Microservices
│   ├── analytics-service/      # Executive KPIs & performance scoring (Port 8088)
│   ├── audit-service/          # Immutable compliance activity logging (Port 8090)
│   ├── auth-service/           # JWT auth, refresh tokens, RBAC (Port 8081)
│   ├── notification-service/   # Multi-channel alerts & preferences (Port 8089)
│   ├── organization-service/   # Multi-tenancy & org governance (Port 8083)
│   ├── project-service/        # Workspaces & project membership (Port 8084)
│   ├── resource-service/       # Capacity allocation & over-allocation alerts (Port 8087)
│   ├── sprint-service/         # Sprints & interactive Kanban columns (Port 8086)
│   ├── task-service/           # Task lifecycle, priorities & comments (Port 8085)
│   └── user-service/           # Employee profiles & departments (Port 8082)
└── shared/
    └── event-contracts/        # Canonical Kafka domain event models (Java library)
```


---

## 🎨 Frontend Architecture & Key Views

- **Executive Dashboard**: High-level KPIs, organization capacity metrics, and live audit feed.
- **Project Management**: Multi-status filtering, team rosters, and project creation modal.
- **Interactive Kanban Board**: 4-column drag-and-drop workflow (To Do, In Progress, Review, Done) with story points and instant task creation.
- **Sprint Management**: Velocity burndown, active sprints, and planned backlog iterations.
- **Resource Allocation**: Workload distribution meters with over-allocation warnings (>100% capacity) and bench management.
- **Analytics & Scorecards**: Project health indicators, velocity tracking, and employee performance matrices.
- **Compliance Audit Forensics**: Searchable activity log with structured JSON diff modal inspector.
- **Notifications Hub**: In-app alert drawer and channel preference toggles (Email / In-App).

---

## 🚀 CI/CD Pipelines & Kubernetes Orchestration

The platform features enterprise-grade automation for continuous integration, multi-stage container delivery, and Kubernetes orchestration:

### 1. GitHub Actions Workflows (`.github/workflows/`)

- **Platform CI (`ci.yml`)**: Parallel jobs for Java 21 backend Maven reactor (all 311 automated tests with Surefire report uploads), React frontend production bundle verification, and Docker Compose syntax validation.
- **Container Registry Delivery (`docker-publish.yml`)**: Builds and tags container images for all 14 services and publishes to GitHub Container Registry (`ghcr.io`) using buildx layer caching.
- **Staging Environment Smoke Test (`staging-smoke-test.yml`)**: Automates end-to-end integration probes against Eureka (`:8761`), Spring Cloud API Gateway (`:8080`), and React Frontend (`:3000`).

### 2. Local CI Validation Scripts (`scripts/`)

Engineers can execute the identical CI checks locally prior to committing:

```bash
# Windows PowerShell
.\scripts\ci-local.ps1

# Linux / macOS Bash
./scripts/ci-local.sh
```

### 3. Kubernetes Deployment Suite (`k8s/`)

The platform includes a complete Kustomize bundle for Kubernetes clusters:

```bash
kubectl apply -k k8s/
```

- **Namespace**: `enterprise-platform`
- **Infrastructure**: PostgreSQL, Apache Kafka + Zookeeper, Redis
- **Microservices**: High-availability deployments for all 10 domain services and 3 infrastructure servers
- **Ingress Controller**: NGINX Ingress exposing the React frontend on `/` and Spring Cloud API Gateway on `/api/`

---

## 📄 License

This project is licensed under the Apache 2.0 License.


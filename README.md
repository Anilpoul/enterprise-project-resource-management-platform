# Enterprise Project & Resource Management Platform

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
docker compose up -d
```

---

## 📄 License

This project is licensed under the Apache 2.0 License.

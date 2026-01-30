# Volunteer Management Platform — TQS Project

[![CI](https://github.com/pedrorafaell82/TQS_Recurso/actions/workflows/ci.yml/badge.svg)](https://github.com/pedrorafaell82/TQS_Recurso/actions)

<!-- Optional: SonarCloud badges (replace placeholders if you want to enable them)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT_KEY&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=YOUR_PROJECT_KEY)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT_KEY&metric=coverage)](https://sonarcloud.io/summary/new_code?id=YOUR_PROJECT_KEY)
-->

## Project Overview

This project was developed in the context of the **Teste e Qualidade de Software (TQS)** course.

It implements a web-based **volunteer management platform** that allows:

- Organizations/promoters to create volunteer opportunities
- Volunteers to browse and enroll in opportunities
- Promoters to approve or reject participations
- Automatic assignment of points for completed participations
- Redemption of rewards using accumulated points
- Administrative management of users, rewards, and point rules

The main goal of the project is to apply and demonstrate **software quality assurance and DevOps practices**, including automated testing, static analysis, and continuous integration.

---

## System Architecture

The system follows a client-server architecture:

- **Frontend:** React application served via **Nginx**
- **Backend:** Spring Boot **REST API**
- **Database:** **H2** (in-memory, for development/testing)
- **CI:** GitHub Actions
- **Quality:** SonarCloud + JaCoCo
- **Containers:** Docker and Docker Compose

High-level flow:


## Key Features

- User registration and role management (**VOLUNTEER**, **PROMOTER**, **ADMIN**)
- Volunteer opportunity creation, update, listing, and deactivation
- Participation lifecycle (enroll, approve, reject, cancel)
- Points and rewards system (balance + redemption)
- Admin management of rewards and point rules
- REST API documented with **Swagger/OpenAPI**

---

## Quality Engineering Practices

This project emphasizes software quality engineering:

### Automated Testing

- Unit tests (e.g., `UserServiceTest`, `RewardTest`)
- Integration tests for REST API and business workflows (using Spring Boot + MockMvc)
- High test coverage (~93%) measured with **JaCoCo**

### Static Analysis

**SonarCloud** used for:

- Bugs and vulnerabilities
- Code smells
- Maintainability

Quality Gates enforced on Pull Requests (merge blocked when failing)

### Continuous Integration

GitHub Actions pipeline:

- Runs on Pull Requests and on pushes to `main`
- Executes `mvn verify`
- Generates JaCoCo coverage reports
- Uploads analysis to SonarCloud

---

## How to Run the Project

### Run with Docker

#### Prerequisites

- Docker
- Docker Compose

From the repository root:

```bash
docker compose up --build
```
### Services

- Frontend: http://localhost:5173  
- Backend: http://localhost:8080  

# SSH (Shared Services Hub)

> Connecting youth program alumni to SMEs through paid tasks, verified work, and guaranteed outcomes.

## Table of Contents

- [What is SSH?](#what-is-ssh)
- [The SSH Guarantee](#the-ssh-guarantee)
- [The Problem](#the-problem)
- [The Solution](#the-solution)
- [How It Works](#how-it-works)
- [Revenue Model](#revenue-model)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Running Tests](#running-tests)
- [Core User Flows](#core-user-flows)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Contributing](#contributing)
- [License](#license)

## What is SSH?

SSH is a platform that connects youth program alumni—young people who completed training but couldn't secure permanent
employment—to SMEs for paid tasks. Corporates fund the youth's income directly, SMEs access talent at no cost, and SSH
pays youth directly from corporate-funded wallets, eliminating exploitation and ensuring fair compensation.

Every completed task is verified by the SME and recorded in the Experience Ledger, building a portable, verifiable work
record that proves capability. Youth remain active in the hub until they secure permanent employment, at which point
they become SSH alumni.

## The SSH Guarantee

| Guarantee           | How SSH Delivers It                                                                            |
|---------------------|------------------------------------------------------------------------------------------------|
| Work                | SSH actively matches youth to tasks. If no SME tasks exist, SSH creates internal tasks.        |
| Income              | Corporates fund youth wages. SSH pays directly. SMEs never handle money.                       |
| Verified Experience | The Experience Ledger records every verified task. Youth exit with portable, verifiable proof. |

The promise: "You will not leave this platform without a verified work record and an income. We guarantee it."

## The Problem

South Africa has 4.7 million unemployed youth (45.8% unemployment rate). Youth programs exist, but alumni fall through
the cracks after completion. They have skills but no verified work experience. SMEs need capacity but can't afford
permanent hires. Corporates need B-BBEE compliance proof but lack verifiable data.

Nobody is connecting these groups effectively.

## The Solution

SSH connects youth program alumni to SMEs for paid tasks. Corporates fund the youth's income, SMEs access talent for
free, and SSH pays youth directly—eliminating exploitation and ensuring fair compensation.

Every completed task is verified by the SME and recorded in the Experience Ledger, building a portable, verifiable work
record that proves capability. Youth remain active in the hub until they secure permanent employment, at which point
they become SSH alumni.

## How It Works

| Step | Action                                               |
|------|------------------------------------------------------|
| 1    | Corporate funds a cohort of youth                    |
| 2    | Youth enters the Shared Services Hub                 |
| 3    | SME posts a task (free)                              |
| 4    | Youth applies and completes the task                 |
| 5    | SME verifies completion                              |
| 6    | SSH pays the youth directly (from corporate funding) |
| 7    | Experience Ledger records verified work              |
| 8    | Youth exits when permanently placed                  |

## Revenue Model

| Revenue Stream     | Source                | How It Works                                                                      |
|--------------------|-----------------------|-----------------------------------------------------------------------------------|
| Corporate Sponsors | Corporates            | Fund cohorts of youth. Get B-BBEE recognition and impact reports.                 |
| SME Subscriptions  | SMEs                  | Free tier (basic access) or premium tier (priority matching, compliance reports). |
| Platform Licensing | Government/Corporates | License the tracking platform to other organizations.                             |

Key principle: SMEs pay nothing. Corporates fund the youth. Youth receive direct payment.

## Architecture

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (SvelteKit)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │  Youth   │  │   SME    │  │Corporate │  │  Admin   │         │
│  │Dashboard │  │Dashboard │  │Dashboard │  │Dashboard │         │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘         │
└───────┼──────────────┼──────────────┼──────────────┼─────────────┘
        │              │              │              │
        ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                         │
│                                                                  │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐                 │
│  │   Auth     │  │   Task     │  │  Payment   │                 │
│  │ Controller │  │ Controller │  │ Controller │                 │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘                 │
│        │               │               │                        │
│  ┌─────▼──────┐  ┌─────▼──────┐  ┌─────▼──────┐                 │
│  │   Auth     │  │   Task     │  │  Payment   │                 │
│  │  Service   │  │  Service   │  │  Service   │                 │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘                 │
│        │               │               │                        │
└────────┼───────────────┼───────────────┼────────────────────────┘
         │               │               │
         ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                              │
│                                                                  │
│  ┌─────────────────────┐    ┌─────────────────────┐             │
│  │    PostgreSQL        │    │       Redis         │             │
│  │  (Transactional)     │    │  (Cache/Sessions)   │             │
│  │                      │    │                     │             │
│  │  - users             │    │  - JWT sessions     │             │
│  │  - smes              │    │  - Task cache       │             │
│  │  - youth             │    │  - Match indices    │             │
│  │  - tasks             │    │                     │             │
│  │  - payments          │    │                     │             │
│  │  - experiences       │    │                     │             │
│  │  - audit_logs        │    │                     │             │
│  └─────────────────────┘    └─────────────────────┘             │
└─────────────────────────────────────────────────────────────────┘
```

### Core Data Model

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    users    │     │    smes     │     │    youth    │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id (PK)     │◄────│ user_id (FK)│     │ user_id (FK)│
│ name        │     │ businessName│     │ skills      │
│ email       │     │ industry    │     │ education   │
│ password    │     │ isVerified  │     │ isAlumni    │
│ role        │     └──────┬──────┘     └──────┬──────┘
│ isActive    │            │                    │
└─────────────┘            │                    │
                           │                    │
                    ┌──────▼──────┐      ┌──────▼──────┐
                    │    tasks    │      │ experiences │
                    ├─────────────┤      ├─────────────┤
                    │ id (PK)     │◄─────│ task_id(FK) │
                    │ sme_id (FK) │      │ youth_id(FK)│
                    │ assignedTo  │      │ status      │
                    │ title       │      │ isVerified  │
                    │ status      │      │ rating      │
                    │ budget      │      └─────────────┘
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  payments   │
                    ├─────────────┤
                    │ id (PK)     │
                    │ task_id(FK) │
                    │ youth_id(FK)│
                    │ amount      │
                    │ status      │
                    └─────────────┘
```

## Tech Stack

| Layer              | Technology             | Version |
|--------------------|------------------------|---------|
| Frontend           | SvelteKit + TypeScript | Latest  |
| Backend            | Spring Boot            | 4.1.1   |
| Language           | Java                   | 25      |
| Database           | PostgreSQL             | 16      |
| Cache              | Redis                  | 7       |
| Authentication     | JWT with BCrypt        | 0.12.6  |
| Documentation      | Springdoc OpenAPI      | 3.0.1   |
| Migrations         | Liquibase              | Latest  |
| Testing (Backend)  | JUnit 5 + Mockito      | Latest  |
| Testing (Frontend) | Vitest + Playwright    | Latest  |

## Project Structure

```
ssh-platform/
├── backend/
│   ├── src/main/java/com/ssh/
│   │   ├── SshApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── security/
│   │   ├── exception/
│   │   └── util/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/changelog/
│   └── src/test/java/com/ssh/
├── frontend/
│   ├── src/lib/
│   ├── src/routes/
│   └── package.json
├── docker-compose.yml
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

## Getting Started

### Prerequisites

| Tool           | Version |
|----------------|---------|
| Java           | 25      |
| Node.js        | 18+     |
| Docker         | Latest  |
| Docker Compose | Latest  |
| Git            | Latest  |

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/ssh-platform.git
cd ssh-platform
```

### 2. Start Infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL on port 5432 and Redis on port 6379.

### 3. Run the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend will start on http://localhost:8080.

### 4. Run the Frontend

```bash
cd frontend
pnpm install
pnpm run dev
```

The frontend will start on http://localhost:5173.

### 5. Access the Application

| Service     | URL                                   |
|-------------|---------------------------------------|
| Frontend    | http://localhost:5173                 |
| Backend API | http://localhost:8080                 |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

## API Documentation

Once the backend is running, visit http://localhost:8080/swagger-ui.html.

This provides all available endpoints, request/response schemas, and interactive testing with JWT authentication.

## Running Tests

### Backend

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
pnpm run test
```

## Core User Flows

### SME Flow

Register → Login → Create Task → Review Applicants → Assign Task → Verify Completion

### Youth Flow

Register → Login → Browse Tasks → Apply → Complete Task → Earn Verified Experience

### Corporate Flow

Register → Login → Fund Cohort → View Impact Reports

### Admin Flow

Login → Manage Users → View Audit Logs → Mark Youth as Placed (Alumni Exit)

## Coding Standards

### Backend (Java)

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Write Javadoc for public APIs
- Keep methods small and focused
- Use DTOs for API contracts (never expose entities directly)
- Add @Transactional annotations where appropriate

### Frontend (TypeScript/Svelte)

- Use TypeScript for all new code
- Follow SvelteKit conventions
- Keep components small and reusable
- Use Tailwind CSS for styling
- Use Svelte stores for state management

## Commit Messages

We follow the Conventional Commits specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

| Type     | Description             |
|----------|-------------------------|
| feat     | New feature             |
| fix      | Bug fix                 |
| docs     | Documentation           |
| style    | Code style (formatting) |
| refactor | Code restructuring      |
| test     | Adding tests            |
| chore    | Maintenance tasks       |

### Examples

```
feat(auth): add JWT authentication
fix(task): handle null SME response
docs(readme): update setup guide
test(user): add registration unit tests
chore(deps): update Spring Boot to 4.1.1
```

## Pull Request Process

1. Create a branch from develop
2. Write your code with tests
3. Ensure all tests pass
4. Update documentation if needed
5. Create a PR to develop
6. PR needs at least 1 approval
7. CI/CD must pass

### Branch Naming Convention

| Branch Type   | Format              | Example                   |
|---------------|---------------------|---------------------------|
| Feature       | feature/description | feature/user-registration |
| Bug Fix       | bugfix/description  | bugfix/login-error        |
| Hotfix        | hotfix/issue-number | hotfix/security-patch     |
| Documentation | docs/description    | docs/api-documentation    |

### PR Checklist

- [ ] Code follows the style guide
- [ ] Tests are written and passing
- [ ] Documentation is updated
- [ ] No security vulnerabilities
- [ ] Commit messages follow convention

## Issue Reporting

When reporting an issue, include:

- A clear and descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Screenshots (if applicable)
- Environment details (OS, Java version, etc.)

## Contributing

We welcome contributions from everyone. By participating, you agree to:

- Be respectful and inclusive
- Provide constructive feedback
- Accept responsibility for your actions
- Focus on what is best for the community

See the Pull Request Process section for details on how to submit changes.

## License

This project is licensed under the Apache License, Version 2.0. See LICENSE for details.

## Vision

A South Africa where every young person has access to a credible pathway into economic participation.

## Mission

To connect young people, businesses, institutions, and capital through technology and partnerships that create practical
pathways into employment, enterprise, and income.

**SSH: From Potential to Participation.**

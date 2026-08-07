# AI Resume Analyzer

A production-ready monorepo for an AI-powered Resume Analyzer application built with Spring Boot (Java 21, Maven) and React + Vite.

## Project Structure

```text
AI-Resume-Analyzer/
├── backend/            # Spring Boot backend application (Java 21, Maven)
├── frontend/           # React + Vite frontend application
├── database/           # Database schemas, migration scripts, and seeds
├── docs/               # System architecture and API documentation
├── .gitignore          # Git ignore rules for Monorepo
├── LICENSE             # Project license (MIT)
└── README.md           # Project documentation overview
```

## Directory Details

- **`backend/`**: Contains the Spring Boot 3.x REST API layer configured with Java 21 and Maven build system. Structured following clean architecture pattern (`config`, `controller`, `service`, `repository`, `model`, `dto`, `exception`, `util`).
- **`frontend/`**: Contains the React SPA built with Vite for fast bundling, structured into clear components, pages, custom hooks, contexts, and API services.
- **`database/`**: Contains database migration scripts (e.g., Flyway/Liquibase), raw SQL schemas, and developmental seed data.
- **`docs/`**: Holds architectural diagrams, API spec contracts (OpenAPI/Swagger), and deployment guide assets.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.3.7 REST API for a restaurant review platform (맛집 리뷰). Java 17, Gradle 9.2.1, MySQL, JPA/Hibernate.

## Common Commands

```bash
./gradlew build              # Build
./gradlew build -x test      # Build without tests
./gradlew bootRun            # Run (dev profile, port 8080)
./gradlew test               # Run all tests
./gradlew test jacocoTestReport  # Tests with coverage
./gradlew clean              # Clean
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Architecture

DDD-style layered architecture under `src/main/java/project/food/`:

- **`domain/`** — 6 business domains, each with `controller/`, `dto/`, `entity/`, `repository/`, `service/` layers:
  - `member` — User management with roles
  - `post` — Restaurant review posts (rating, category, location, images)
  - `comment` — Hierarchical comments with soft delete
  - `tag` — Tagging system via PostTag junction
  - `like/postlike` and `like/commentlike` — Like functionality

- **`global/`** — Cross-cutting concerns:
  - `common/BaseTimeEntity` — JPA auditing (createdAt/updatedAt) inherited by all entities
  - `config/` — Swagger, JPA, Web, File upload configs
  - `exception/` — `@RestControllerAdvice` with `ErrorCode` enum and `CustomException`
  - `file/FileService` — Local file storage (`./uploads/post`), max 10 files, 10MB each

## Key Patterns

- Controller → Service → Repository → Entity with separate DTOs for request/response
- Centralized exception handling: add new codes to `ErrorCode` enum, throw `CustomException`
- All entities extend `BaseTimeEntity` for automatic timestamps
- Comments use parent-child self-reference for replies
- Swagger annotations (`@Operation`, `@ApiResponse`) on all controller methods

## Environment Profiles

- `dev` (default): port 8080, `ddl-auto: update`, SQL logging
- `test`: port 8081, `ddl-auto: create-drop`
- `prod`: `ddl-auto: validate`, HikariCP pooling, Caffeine caching
- Database credentials in `application-secret.yml` (gitignored)

## PR/Issue Workflow

Korean-language templates in `.github/`. Branch-based development on `develop` branch.
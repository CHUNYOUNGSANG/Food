# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./gradlew build          # Build project
./gradlew bootRun        # Run application (default: dev profile, port 8080)
./gradlew test           # Run all tests
./gradlew clean build    # Clean and rebuild
```

- Java 17, Spring Boot 3.3.7, Gradle 9.2.1
- MySQL database (credentials in `application-secret.yml`, not committed)
- Profiles: `dev` (default, ddl-auto: update), `prod` (ddl-auto: validate), `test`

## Architecture

Standard layered Spring Boot app under `project.food`:

- **`domain/`** — Feature modules, each with `controller/`, `service/`, `repository/`, `entity/`, `dto/` sub-packages
  - `post` — Posts with image uploads (multipart), view counting, category filtering
  - `comment` — Self-referential replies (parentCommentId), soft delete
  - `member` — User management with Role enum (USER/ADMIN)
  - `like/postlike`, `like/commentlike` — Like system with unique constraints preventing duplicates
  - `tag` — Under development
- **`global/`** — Cross-cutting concerns
  - `config/` — Web (CORS for `/api/**`), Swagger, JPA auditing, file storage config
  - `exception/` — `GlobalExceptionHandler` (@RestControllerAdvice), `ErrorCode` enum with domain-prefixed codes (G/M/P/C/F/L), `CustomException` base class
  - `file/service/FileStorageService` — UUID-based file naming, type/size/count validation, stores to `./uploads/post`
  - `common/BaseTimeEntity` — JPA auditing base with `createdAt`/`updatedAt`

## Key Patterns

- All entities extend `BaseTimeEntity` for automatic timestamps
- Services use `@Transactional` (read-only for queries)
- DTOs separate request/response from entities
- Lombok throughout (`@Getter`, `@Builder`, `@Slf4j`, etc.)
- Error codes follow pattern: domain letter + number (e.g., P001 = post not found, F003 = invalid file type)
- API base path: `/api` (e.g., `/api/posts`, `/api/comments`)
- Swagger UI at `/swagger-ui.html`

## Entity Relationships

Member → (1:N) Post, Comment, PostLike, CommentLike
Post → (1:N) Comment, PostImage (cascade delete), PostLike
Comment → self-referential replies, (1:N) CommentLike
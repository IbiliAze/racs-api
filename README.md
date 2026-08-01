# RACS API

Spring Boot API for the Reader Access Control System. It manages readers,
cards, campaigns, locations, scans, users, roles, permissions, reports, and
reader-to-reader synchronisation.

## Requirements

- Java 25
- Docker with Docker Compose, or a MySQL 8 instance

## Local setup

1. Start the development database:

   ```bash
   docker compose -f deploy/docker/docker-compose.dev.yml up -d
   ```

2. Create `.env` from `.env.example` and replace the placeholder with a strong
   JWT secret:

   ```dotenv
   JWT_SECRET=replace-with-a-long-random-secret
   ```

3. Start the API:

   ```bash
   ./mvnw spring-boot:run
   ```

The development profile exposes HTTPS on port `3443` and the companion HTTP
connector on port `5001`. The OpenAPI UI is available at
`https://localhost:3443/swagger-ui/index.html`.

The default local database is `racs`, with development credentials defined in
`src/main/resources/application-dev.yml`.

## Verification

```bash
./mvnw test
./mvnw -DskipTests package
```

Integration tests require their configured MySQL service. Database changes are
managed by Flyway migrations in `src/main/resources/db/migration`.

## Deployment

Container and reverse-proxy definitions live under `deploy/`. Environment
profiles are configured in `src/main/resources/application-*.yml`; provide
database credentials and the JWT secret through the deployment environment.

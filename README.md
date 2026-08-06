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
   docker compose up -d mysql
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

`docker-compose.yml` builds the API image and runs it alongside MySQL:

```bash
docker compose up -d
```

Provide `JWT_SECRET`, `DB_USERNAME`, and `DB_PASSWORD` via a `.env` file or
your deployment environment. The API listens on port `5000`.

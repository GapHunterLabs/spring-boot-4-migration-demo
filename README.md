# Spring Boot 3.5 → 4.1 migration, step by step

A small orders service (REST API, JPA, Flyway, H2), migrated from Spring Boot 3.5.16 to 4.1.1 with every step kept
in the git history. It shows what the free OpenRewrite recipe does, what it leaves behind, and which changes compile,
start and still break API clients.

**Read [MIGRATION_REPORT.md](MIGRATION_REPORT.md)** for the findings, each with the commit that shows it.

## The service

- `GET /api/orders`: the orders, in JSON with snake_case field names and ASCII-only strings (non-ASCII characters
  escaped), the format its clients parse.
- `GET /api/orders/export`: the same orders as CSV, through a custom `HttpMessageConverter`.
- `GET /api/orders/{id}`, `POST /api/orders` with bean validation.

The service was written for this demonstration. Its 3.5 code uses common patterns that the official Spring Boot 4.0
migration guide lists as changed: a custom `ObjectMapper` bean, a `spring.jackson.generator.*` property, Flyway
without a starter, `@MockBean`, `TestRestTemplate` with `@SpringBootTest`.

## Tests

- `OrderApiContractTest`: compares the raw JSON and CSV bodies byte for byte with `src/test/resources/contract/`,
  which were recorded from the 3.5 baseline. When the body differs, the one actually received is written to
  `build/contract-actual/`.
- `OrderControllerValidationTest`: `@WebMvcTest` with a mocked service.
- `OrderApiHttpTest`: real HTTP with `TestRestTemplate`, on its own in-memory database.

```
./gradlew test
```

Requires Java 21. The Gradle wrapper is included.

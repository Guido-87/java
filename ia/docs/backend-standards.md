# Backend Standards

## Stack and structure

- Use the Java and Spring Boot versions declared by `pom.xml`; do not duplicate version numbers in application code.
- Keep responsibilities in the existing package boundaries: HTTP handling in `controller`, request/response types in `dto`, orchestration in `service`, external API integration in `client`, and cross-cutting errors in `exception`.
- Use constructor injection for Spring-managed dependencies, as in the existing services and controllers.
- Use Jakarta Bean Validation for request constraints and handle API errors consistently through the existing exception advice.
- Keep persistence and cache interactions behind service abstractions such as `RedisService`.

## Configuration and integrations

- Bind environment-dependent values through Spring configuration; do not hard-code credentials, service URLs, or deployment-specific ports.
- Treat Groq and Redis as external services. Handle failures deliberately, avoid leaking upstream response bodies to clients, and keep logs free of secrets and full user prompts.
- Preserve session-based conversation identity and Redis expiration behavior unless the requested feature changes that contract.

## Tests

- Place tests under `src/test/java` using the same package structure as production code.
- Use focused unit tests for service and client behavior, MVC tests for controllers, and integration tests for cross-component behavior, following existing test patterns.
- Run `.\mvnw.cmd test` for the test suite. Use `.\mvnw.cmd verify` when checking the configured JaCoCo coverage gate.

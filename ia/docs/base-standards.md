# Project Standards

## Project context

- This repository is a Java application using Spring Boot 4.1.1 and Maven.
- The Maven build targets Java 25 (`maven.compiler.release`). Use the project's Maven wrapper (`mvnw.cmd`) on Windows.
- The application provides a Spring MVC page rendered with Thymeleaf and a JSON chat endpoint. It calls the Groq API and stores conversation history in Redis.
- The code is organized under `com.spring.ia` in `client`, `controller`, `dto`, `exception`, `service`, and `util` packages.

Treat the checked-in source, `pom.xml`, and `src/main/resources/application.properties` as authoritative when details change. Do not infer architecture or domain models that are not present in the code.

## Working principles

- Make focused changes that solve the requested problem and fit the existing code.
- For behavior changes, add or update appropriate automated tests. Prefer test-first development when practical; do not create tests for documentation-only changes.
- Preserve existing public behavior unless the request explicitly changes it.
- Keep Java names and APIs idiomatic. Keep user-facing messages in the language already used by that part of the application; technical documentation in `docs/` should be written in English.
- Do not add dependencies or introduce architectural patterns without a concrete need.

## Security and configuration

- Keep credentials out of source control. Configure secrets through environment variables such as `GROQ_API_KEY` and `REDIS_URL`.
- Validate untrusted request data at the boundary and avoid logging prompts, credentials, or other sensitive user content.
- Preserve secure session-cookie and forwarded-header settings unless a requested change requires otherwise.

## Verification commands

- Run unit and integration tests with `.\mvnw.cmd test`.
- Run the full verification lifecycle with `.\mvnw.cmd verify`. The Maven build configures JaCoCo to enforce a minimum 80% line coverage ratio.
- Start the application with `.\mvnw.cmd spring-boot:run` when runtime verification is needed. Redis and `GROQ_API_KEY` may be required by the relevant paths.

## OpenSpec

OpenSpec is initialized in `openspec/`. Keep `openspec/config.yaml` consistent with the actual project and consult the applicable OpenSpec skill before running an OpenSpec workflow.

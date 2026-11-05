# Project Standards

## Project context

- This repository is a Java application using Spring Boot 4.1.2 and Maven.
- The Maven build targets Java 25 (`maven.compiler.release`). Use the project's Maven wrapper (`mvnw.cmd`) on Windows.
- The application provides a Spring MVC page rendered with Thymeleaf and a JSON chat endpoint. It calls the Groq API and stores conversation history in Redis.
- The code is organized under `com.spring.ia` in `client`, `controller`, `dto`, `exception`, `service`, and `util` packages.

Treat the checked-in source, `pom.xml`, and `src/main/resources/application.properties` as authoritative when details change. Do not infer architecture or domain models that are not present in the code.

## Working principles

- Make focused changes that solve the requested problem and fit the existing code.
- For behavior changes, use TDD: write a failing automated test first, implement the minimum change to pass it, then refactor. Add or update appropriate tests; do not create tests for documentation-only changes.
- Preserve existing public behavior unless the request explicitly changes it.
- Keep Java names and APIs idiomatic. Keep user-facing messages in the language already used by that part of the application; technical documentation in `docs/` should be written in English.
- Do not add dependencies or introduce architectural patterns without a concrete need.

## Architecture and design principles

- Apply Clean Architecture as a dependency and responsibility guide: keep HTTP concerns in controllers, orchestration in services, external-process or API details in clients, and transport models in DTOs. Keep dependencies pointed toward stable application logic; do not add layers that the use case does not need.
- Use SOLID as practical guidance. Keep each class focused, extend behavior through clear seams, preserve interface contracts, keep interfaces small, and inject dependencies where that improves isolation or testability. Avoid abstractions that only rename a single implementation without a real boundary.
- Follow KISS: choose the simplest design that meets the requirement and remains clear to the next maintainer.
- Follow DRY for repeated knowledge or behavior. Share stable rules in one place, but do not couple code merely because two snippets look similar.
- Prefer readable names, small cohesive methods, explicit error handling at system boundaries, and automated tests for behavior. Avoid premature optimization and speculative features (YAGNI).
- Favor composition over inheritance when it keeps responsibilities easier to change. Keep security, accessibility, and observability in mind as part of the design, not as afterthoughts.

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

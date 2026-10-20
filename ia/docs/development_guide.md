# Development Guide

## Requirements

- Java 25, as declared by `pom.xml`.
- Use the Maven wrapper included in this repository; it is configured for Maven 3.9.12.
- Redis is used for conversation storage. The default URL is `redis://localhost:6379` and can be overridden with `REDIS_URL`.
- Set `GROQ_API_KEY` before using chat requests that call the Groq API. `BASE_URL` and `PORT` can also be overridden through the environment.

## Common commands on Windows

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

The application defaults to port 8080. `verify` includes the configured JaCoCo line coverage check.

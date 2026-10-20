# Web UI Standards

The application serves a server-rendered page with Thymeleaf templates, static CSS, and JavaScript. It does not currently use React or a separate frontend build.

- Keep templates under `src/main/resources/templates` and static assets under `src/main/resources/static`.
- Keep browser code focused on presentation and HTTP interaction; put business rules and validation on the server as well.
- Preserve accessible labels, keyboard behavior, and responsive layout when changing the UI.
- Keep user-facing chat text in Spanish unless the feature explicitly requires another language. Avoid introducing a frontend framework or build tool without a project need.
- Update relevant controller and browser-facing tests when changing page or endpoint behavior.

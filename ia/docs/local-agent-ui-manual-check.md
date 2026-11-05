# Local Agent UI Manual Check

Use this checklist when verifying the first-task startup flow in a browser:

1. Enable the local agent service in application configuration, but leave the Codex App Server process stopped.
2. Reload the page. The status should say that the task is ready to start, and the “Tarea Codex local” action should be available.
3. Open the task dialog, enter a prompt, and submit it. The browser should send the task request even though the status endpoint initially reported `connection: disconnected`.
4. Confirm that the task status replaces the ready-to-start message once the backend returns its task snapshot.
5. Disable the local agent service and reload. The page should report that the service is disabled and prevent opening or submitting a local task.

The project has no frontend test harness; these checks are intentionally manual and are not run as part of this change.

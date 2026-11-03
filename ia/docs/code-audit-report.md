# Code Audit Report — Local Agent Orchestrator

Date: 2026-11-03
Scope: Java/Spring backend, Codex App Server client and task lifecycle, approval policy, chat endpoints, browser UI and error messages, project configuration, and automated tests.

## Summary

The web interface and user-facing errors are in Spanish (`lang="es"`). Chat responses use a lightweight Spanish/English marker detector; it defaults to Spanish when evidence is tied or absent. English technical prompts now keep the specialized Java instructions in English as well.

The full Maven verification completed successfully after the language updates: 80 tests passed, 0 failed, 0 errored, 1 skipped. JaCoCo's configured coverage gate passed. The skipped case is the Windows symlink escape test, because this environment does not allow creating symlinks.

The live application is configured to use the active worktree through `CODEX_AGENT_WORKSPACE_ROOT` (falling back to `user.dir`) and was restarted on `127.0.0.1:8080` with the `local` profile. The page returned HTTP 200 and declared `lang="es"`.

## Findings

### Resolved — Root thread with a null parent ID was mistaken for a subagent

Codex returns `parentThreadId: null` for a root conversation thread. The service used a field-presence check, which treated that explicit null as evidence of a parent and never promoted the task from `QUEUED` to `WORKING`; the startup timer then stopped the valid task. Root detection now treats missing, null, or blank parent IDs as a root thread. A regression test reproducing the null field failed before the fix and passes afterward.

### Resolved — English language detection missed common prompts and mixed technical instructions

The previous detector recognized only a few English words, causing prompts such as “Please explain virtual threads” to default to Spanish. Java/Spring expert instructions also remained Spanish even when the base prompt was English. Detection now scores common English and Spanish markers, and the specialized instructions follow the selected language. Unit tests cover both languages and the default.

### Low — Missing or non-owned task IDs return a generic 500

`AgentTaskController.get` calls `AgentTaskService.get` without mapping the ownership lookup's `IllegalArgumentException` to a not-found response. A missing task or a task belonging to another session therefore falls through to the generic internal-error response. Consider returning 404 (or a consistent non-disclosing 404) and add an MVC test.

## Reviewed and addressed in this work

- Translated interface labels, task statuses, approval details, and visible errors to Spanish, including task and startup timeout messages.
- Kept the local task workspace rooted in the configured worktree and bounded task startup with a timeout so a stalled App Server does not leave the task queued forever.
- Prevented automatic workspace file-change approval from accepting paths that escape through symlinks; the Windows-only symlink test could not run here.
- Added request-size validation to the public chat prompt.
- Corrected manual approval fallback details, retained pending approvals when writing a decision fails, and ignored stale events from replaced App Server processes.
- Sanitized and bounded Codex subprocess diagnostics before logging.
- Checked that browser-rendered untrusted text uses text nodes/text content in the reviewed chat and agent UI paths.

## Verification and limitations

- `mvnw.cmd verify`: 80 passed, 0 failed, 0 errors, 1 skipped; coverage gate passed.
- Served page: HTTP 200, Spanish document language, with translated labels and updated script cache keys.
- Live automatic smoke test completed and returned the project root file list without editing files.
- Live manual smoke test completed and returned `READY` without calling tools or editing files.
- Git metadata in this worktree points to an unavailable parent path, so `git status`/diff could not be used to confirm the final working-tree change list.

## Priority next steps

1. Map missing or non-owned task retrieval to a deliberate 404 response and cover it with a controller test.
2. Exercise manual approval requests that require a command or file change, then verify accept and decline flows in the browser.

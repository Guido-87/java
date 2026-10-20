# Agent instructions

Read and follow [docs/base-standards.md](docs/base-standards.md) for project-wide guidance.

Project-specific backend guidance is in [docs/backend-standards.md](docs/backend-standards.md). Use the relevant skills under `.agents/skills/` when their descriptions match the task. The OpenSpec skills in that directory are retained alongside the LIDR skills.

## Specialized Codex agents

Project-scoped Codex agents are defined in `.codex/agents/`. Delegate focused, independent work to the matching role: `enzo_seguridad` for security review, `nahitan_devops` for runtime and deployment, `jona_dev1` for backend implementation, `jesse_dev2` for the web UI, `rucau_arquitecto` for design analysis, and `juli_qa` for test and acceptance review. These names are role identifiers; keep the main agent responsible for coordinating shared changes.

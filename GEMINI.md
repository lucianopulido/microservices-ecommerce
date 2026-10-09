<!-- gentle-ai:codegraph-guidance -->
## CodeGraph

When answering structural or codebase questions, use CodeGraph before broad filesystem searches. This is a hard ordering rule for repo maps, architecture, call flow, dependencies, symbol references, impact analysis, and "how does X work" questions.

Required order for structural/codebase questions:
1. Resolve the project root with `git rev-parse --show-toplevel || pwd`.
2. Confirm the root is a real project/workspace. Do not ask the user before initializing CodeGraph in a real project.
3. Check for `<project-root>/.codegraph/` before any broad Read/Glob/Grep filesystem exploration.
4. If `.codegraph/` is missing and CodeGraph is enabled/available, immediately run `gentle-ai codegraph init --cwd <project-root>` once.
5. Missing `.codegraph/` is the trigger to initialize, not a reason to skip CodeGraph.
6. Prefer the `codegraph_explore` MCP tool when available.
7. Only fall back to normal filesystem tools after CodeGraph initialization or use fails, and briefly explain the fallback.
<!-- /gentle-ai:codegraph-guidance -->

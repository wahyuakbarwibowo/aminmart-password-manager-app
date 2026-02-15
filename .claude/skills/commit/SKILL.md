---
name: commit
description: Stage and commit all changes with a descriptive message
disable-model-invocation: true
---

Commit all current changes following these steps:

1. Run `git status` to see all changed and untracked files
2. Run `git diff` to understand what changed
3. Run `git log --oneline -5` to see recent commit style
4. Stage all relevant files (do NOT use `git add .`, add files by name)
5. Write a concise commit message that follows conventional commits format (feat/fix/chore/refactor/docs)
6. If `$ARGUMENTS` is provided, use it as the commit message. Otherwise, generate one based on the changes.
7. Always append `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>` to the commit message
8. Run `git status` after commit to verify success

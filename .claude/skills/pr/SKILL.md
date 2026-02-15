---
name: pr
description: Create a pull request to main branch
disable-model-invocation: true
---

Create a pull request from the current branch to `main`. Follow these steps:

1. Run `git branch --show-current` to check current branch
2. If on `main`, STOP and tell the user to create a branch first using `/branch`
3. Run `git status` to check for uncommitted changes — if any, STOP and tell the user to commit first using `/commit`
4. Run `git log main..HEAD --oneline` and `git diff main...HEAD --stat` to understand all changes
5. Push the branch: `git push -u origin <branch-name>`
6. Create PR using `gh pr create` targeting `main` with:
   - A short title (under 70 chars) using conventional commits format
   - Body with `## Summary` (bullet points), `## Test plan` (checklist), and the Claude Code footer
7. Return the PR URL

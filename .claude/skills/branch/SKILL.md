---
name: branch
description: Create a new git branch from main
disable-model-invocation: true
argument-hint: [branch-name]
---

Create a new git branch. Follow these steps:

1. Run `git branch --show-current` to check current branch
2. If NOT on `main`, ask the user if they want to switch from the current branch
3. If `$ARGUMENTS` is provided, use it as the branch name. Otherwise, ask the user for a branch name.
4. Ensure the branch name uses kebab-case (e.g. `feature/add-login`, `fix/crash-on-startup`)
5. Run `git checkout main && git pull origin main` to ensure main is up to date
6. Run `git checkout -b <branch-name>` to create and switch to the new branch
7. Confirm the new branch is active with `git branch --show-current`

---
name: implement-spec-here
description: "Like /implement-spec, but does not create a feature branch. Ticket worktrees branch from and merge back into the currently checked-out branch. Use when several sessions share the repo."
disable-model-invocation: true
---

Read `.agents/skills/implement-spec/SKILL.md`, then follow it exactly, with these overrides:

- **Do not create an integration branch** (step 3). Do not create any feature branch. The **integration branch is the branch currently checked out** when this skill starts. Record its name and starting commit up front.
- Everywhere `implement-spec` says "integration branch", use that current branch.
- **Still create one worktree + branch per ticket** (step 4), based on the current branch tip. Merger subagents merge completed ticket branches into the current branch.
- Do not switch branches in the primary working directory at any point.
- Draft PR: only open one if the user explicitly asks. The current branch must be ahead of its base for that to work.
- Step 7 code review: review the range from the recorded starting commit to the current branch tip, so other sessions' commits are not swept in.
- Step 9: still remove all ticket worktrees and their merged branches.

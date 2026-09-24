---
name: case-viewer-usecases
description: Define and document use cases for the case-viewer solution (Case/Activity domain). Use when asked to define, write, or update use cases, user stories, or requirements for case-viewer, or when scoping new features before implementation.
---

# Case Viewer use-case definition

Use this skill to produce or update use-case documents for `case-viewer` before
implementation work starts. Each use case is one file under `docs/use-cases/`.

## Domain vocabulary (keep consistent across all use cases)

- **Actor**: `User` (authenticated, `role=USER` today — the only role that exists).
  There is no admin/agent role yet; if a use case needs one, call that out as a new
  actor rather than assuming it exists.
- **Case**: the primary entity — `caseNumber`, `title`, `description`, `status`
  (`CaseStatus`), timestamps. Identified externally by `caseNumber` (e.g.
  `CASE-100001`), internally by a UUID `id`.
- **Activity**: a child of `Case` (one-to-many) — `activityType`, `description`,
  `status` (`ActivityStatus`), `performedBy`, `activityAt`.
- Current implemented capabilities: register, login, view a case by number (with its
  activities), create a case. Everything else (search/filter, update, delete,
  activity creation, tenancy) is unimplemented or stubbed — check
  `src/main/java/com/example/case_viewer/controller/` and
  `.claude/skills/case-viewer-review/SKILL.md` before assuming something exists.

## Use case template

Write each use case as `docs/use-cases/UC-<NNN>-<kebab-title>.md`:

```markdown
# UC-<NNN>: <Title>

## Actor
Who initiates this (e.g. "Authenticated user", "Case agent").

## Goal
One sentence: what the actor is trying to accomplish.

## Preconditions
State required before the use case starts (e.g. "actor holds a valid JWT",
"case CASE-XXXX exists").

## Main flow
1. Numbered steps, actor action / system response pairs.
2. ...

## Alternate / exception flows
- **<condition>**: what happens instead (e.g. "case not found -> 404
  CASE_NOT_FOUND").

## Postconditions
Observable state after success (what changed, what the actor sees).

## Related API(s)
Endpoint(s) this maps to today, or "none yet — new endpoint required" if it doesn't
exist.

## Open questions
Anything that needs a product/design decision before this can be built.
```

## Process

1. Check existing use cases in `docs/use-cases/` and the current API surface
   (`controller/`, `CASE_VIEWER_README.md`) so a new use case doesn't duplicate or
   silently contradict one already written or already implemented.
2. Number sequentially (`UC-001`, `UC-002`, ...) — never reuse a number, even for a
   superseded use case (mark it `Status: Superseded by UC-NNN` in that case instead).
3. Keep one use case per file, one primary actor goal per use case. Split anything
   that needs "and" to describe its goal.
4. Flag any use case that implies a new actor, role, or entity (e.g. an admin role,
   a tenant concept, an approval workflow) as an open question rather than quietly
   assuming it into the flow — those are solution-design decisions, not use-case
   details.
5. When a use case is approved and ready to build, it becomes the input to a design/
   implementation plan — this skill only covers defining it, not implementing it.

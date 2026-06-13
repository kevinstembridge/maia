# Many-to-Many Join History Design

## Goal
Support `recordVersionHistory = true` on `manyToManyEntity()` for non-effective-timestamp joins, producing a full history blotter (global page) of add/remove events for the join — consistent with regular entity history.

## Scope / Constraints
- Only **non-effective-timestamp** joins. Effective-timestamp joins never delete rows (date-ranged instead), so the join table itself already serves as history. `manyToManyEntity(recordVersionHistory = true, ...)` on an effective-timestamp join throws `ModelDefinitionException` at spec-build time.
- History blotter is **global** (like other `allowFindAll` blotters), not scoped under a specific entity's detail page — a join has no natural single "owner" for a per-entity history view.
- Change types: reuse existing `ChangeType` enum (`CREATE`, `UPDATE`, `DELETE`). Plain joins only ever produce `CREATE` (pair added) and `DELETE` (pair removed) — no field-level updates in the set-diff path, so `UPDATE` is unused for joins.

## Design

### 1. Spec layer validation
`manyToManyEntity()` (`AbstractSpec.kt:476`) throws `ModelDefinitionException` if `recordVersionHistory && hasEffectiveTimestamps`.

### 2. History entity generation
`EntityDef.historyEntityDef` (`EntityDef.kt:457`) already builds generically from `withVersionHistory` — join entities are root entities with a surrogate `id` PK (post commit 035c54fe), so the existing logic should produce a valid history `EntityDef` without changes. Verify via test that a 2-FK join entity builds cleanly.

### 3. History blotter columns for join-derived history
`EntityHistoryBlotterDef.kt:28-36` currently filters out FK fields, `id`, and `createdTimestampUtc` — correct for regular entities, wrong for joins where the FKs are the meaningful content.

Add a join-history branch:
- **Columns:** left entity (name, via existing PK+Name lookup), right entity (name), `changeType`, `createdTimestampUtc`, `createdBy`.
- **Excluded:** join's own surrogate `id`, `version`.

Requires `EntityDef` to know it is join-derived — add a back-reference (e.g. `manyToManyEntityDef: ManyToManyEntityDef?`, set post-construction) or thread a boolean flag through `EntityDefBuilder`.

URL/routing (`EntityHistoryBlotterDef.kt:109`, currently `/api/{entityKebab}/{entityId}/history/search`) drops `{entityId}` for join history — global path, same pattern as other `allowFindAll` blotters.

### 4. History-writing wrappers in JdbcDaoRenderer
For join entities with `historyEntityDef != null`, generate repo methods alongside existing `insertHistory()`/`bulkInsertHistory()` (`JdbcDaoRenderer.kt:478-583`), reusing the historyDao wiring already triggered by `historyEntityDef != null` (`JdbcDaoRenderer.kt:120-127`):

- `bulkInsertWithHistory(entities)` — `bulkInsert` + write a `CREATE` history row per entity.
- `deleteByPrimaryKeyWithHistory(id)` — fetch row, delete, write a `DELETE` history row from the fetched snapshot.

### 5. CrudServiceRenderer set-diff integration
Non-effective-timestamp join set-diff branch (`CrudServiceRenderer.kt:561-579`): when `historyEntityDef != null`, generate calls to `bulkInsertWithHistory(...)` / `deleteByPrimaryKeyWithHistory(it.id)` instead of `bulkInsert(...)` / `deleteByPrimaryKey(it.id)`. No other structural change to the generated method.

### 6. Showcase fixture + tests
Set `recordVersionHistory = true` on `LeftToRightSimpleJoin` (non-effective-timestamp example) in `MaiaShowcaseSpec.kt`. Generate and verify:
- History table + entity generated correctly.
- Blotter page renders with left/right/changeType/timestamp/createdBy columns.
- Set-diff update produces `CREATE` history rows for newly added pairs and `DELETE` history rows for removed pairs.

## Open Questions
- None — design approved as-is.

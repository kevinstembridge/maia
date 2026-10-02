# Bulk `setFields` generator support

## Problem

`JdbcDaoRenderer`/`EntityRepoRenderer` generate a list-variant of `setFields` for every entity that just loops and calls the single-row `setFields` once per item — one `jdbcOps.update` round trip (plus, for entities with history, one `findByPrimaryKey` + history insert) per row. For large batch jobs this doesn't scale. `bulkInsert` already solves the equivalent problem for inserts via `jdbcOps.batchUpdate` (a true JDBC batch). This spec adds an equivalent `bulkSetFields` for updates.

## Why this is non-trivial

JDBC batching requires one identical SQL statement across all rows in the batch. But each `EntityUpdater` in the input list can carry a different subset of `FieldUpdate`s (different columns being set), so a single list of updaters can't always be batched as one SQL statement.

## Design

### Grouping

Group the input `updaters` by the set of `classFieldName`s present in `updater.fields`:

```kotlin
val groups = updaters.groupBy { it.fields.map { f -> f.classFieldName }.toSet() }
```

All members of a group produce identical generated SQL text (same columns set, same effective-timestamp branch), because grouping key is the field-name set. The SQL template is built once per group from one representative updater; parameters are built per-row.

### Generated `bulkSetFields` (DAO level)

```kotlin
fun bulkSetFields(updaters: List<UsaRegistrationEntityUpdater>) {

    val groups = updaters.groupBy { it.fields.map { f -> f.classFieldName }.toSet() }
    val failedUpdaters = mutableListOf<UsaRegistrationEntityUpdater>()
    val updatedIds = mutableListOf<DomainId>()

    groups.values.forEach { group ->

        val representative = group.first()
        val sql = StringBuilder()
        sql.append("update la.caa_registration set ")
        // same fieldClauses text-building logic as single-row setFields,
        // built from representative.fields (including the effective-timestamp
        // tstzrange(...) special case when effectiveFrom/effectiveTo are present)
        sql.append(/* fieldClauses */)
        sql.append(" where id = :id and version = :version")   // or multi-column for composite PK

        val sqlParamsList = group.map { updater ->
            val sqlParams = SqlParams()
            updater.fields.forEach { field -> addField(field, sqlParams) }   // reused unchanged
            // effectiveFrom/effectiveTo added explicitly here when present, same as single-row setFields
            sqlParams.addValue("id", updater.id)                             // or per-PK-field for composite PK
            sqlParams.addValue("version", updater.version)
            sqlParams.addValue("version_incremented", updater.version + 1)
            sqlParams
        }

        val updateCounts = jdbcOps.batchUpdate(sql.toString(), sqlParamsList)

        group.forEachIndexed { i, updater ->
            if (updateCounts[i] == 0) failedUpdaters.add(updater) else updatedIds.add(updater.id)
        }
    }

    if (entityDef.withVersionHistory) {
        val updatedEntities = findAllByPrimaryKeys(updatedIds)
        bulkInsertHistory(updatedEntities, ChangeType.UPDATE)
    }

    if (failedUpdaters.isNotEmpty()) {
        throw BulkOptimisticLockingException(TABLE_NAME, failedUpdaters.map { it.primaryKeyMap to it.version })
    }
}
```

(`primaryKeyMap`/`primaryKey` here follows the same single-vs-composite-PK naming the existing single-row `setFields` already uses for `OptimisticLockingException` — see `JdbcDaoRenderer.kt:2234-2244`.)

Reused unchanged: `addField(...)` (per-field param population) and `bulkInsertHistory(...)` (already groups by concrete subtype for entity hierarchies).

New pieces: the grouping, a `findAllByPrimaryKeys` DAO helper, and `BulkOptimisticLockingException`.

### Failure handling

Spring's `NamedParameterJdbcOperations.batchUpdate` returns an `IntArray` of per-row affected-row counts rather than throwing on a 0-count row (optimistic-lock version mismatch). `bulkSetFields` collects every failed updater across every group and throws **one aggregate exception** (`BulkOptimisticLockingException`, carrying the full list of failed `(primaryKey, version)` pairs) after the whole batch has run, rather than failing fast on the first mismatch. This gives the caller complete visibility into which rows were stale in one shot, matching the batch-job use case this exists for.

### History

For entities with `withVersionHistory`, instead of today's per-row `findByPrimaryKey` + `insertHistory`, `bulkSetFields` does one bulk re-fetch of all successfully-updated rows (`findAllByPrimaryKeys`, new helper) followed by the existing `bulkInsertHistory` (already used by `bulkInsert`, already hierarchy-aware).

`findAllByPrimaryKeys`:
- Single (non-composite) PK: `where id in (:ids)`, using `SqlParams.addListValue` (already exists, used nowhere else in this renderer yet).
- Composite PK: no native tuple-`IN` via named-parameter collection expansion, so the generated SQL is a dynamic OR-chain built at runtime from the updated-row count: `(pk1 = :pk1_0 and pk2 = :pk2_0) or (pk1 = :pk1_1 and pk2 = :pk2_1) or ...`. Still one round trip.

### Effective-dated entities

`addField` already excludes `effectiveFrom`/`effectiveTo` (they need the `tstzrange(...)` SQL form, not a plain column assignment). Because grouping is by exact field-name set, a group consistently has both/one/neither of these fields, so the per-group SQL-text builder reuses the same branch logic (`both` / `from only` / `to only` / `else`) that single-row `setFields` already has, and per-row param population explicitly sets `effectiveFrom`/`effectiveTo` values (bypassing `addField`), exactly as today.

### Entity hierarchies

No new dispatch logic needed. `bulkInsertHistory` already groups by concrete subtype internally (`entities.groupBy { when (entity) { is X -> "X" ... } }`). The only hierarchy-specific requirement is that `findAllByPrimaryKeys` returns the shared supertype, which `bulkInsertHistory` already knows how to handle.

### Repo-level passthrough

Mirrors `bulkInsert`'s existing passthrough pattern:

```kotlin
fun bulkSetFields(updaters: List<UsaRegistrationEntityUpdater>) {
    logger.debug("bulkSetFields {}", updaters)
    this.dao.bulkSetFields(updaters)
}
```

### Scope

Full parity with what single-row `setFields` already supports: simple entities, composite primary keys, effective-dated entities, and entity hierarchies (with or without version history).

## Testing

Exercised against real generated+compiled code in `maia-showcase` (not just renderer-output snapshots), covering each axis already present in the showcase domain:

- `AlphaWithHistory` / `BravoWithHistory` — simple entity + history: baseline happy path, and mixed-field-set grouping (one batch call with updaters setting different fields → multiple groups/batches)
- `EffectiveTimestamp` — effective-dated branch
- `HistorySuper` / `HistorySubTwo` — hierarchy + history dispatch
- `NonSurrogatePrimaryKey` / `NonSurrogateIdPrimaryKey` — composite PK path
- A mixed-version-conflict case per entity type, to verify `BulkOptimisticLockingException` collects *all* failed rows across groups, not just the first

## Branch

This work lives on `feature/bulk-set-fields-generator` in the `maia` repo and is a spike/investigation — not intended to merge to `main` at this stage.

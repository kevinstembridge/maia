# History FK Version Stamping

## Problem

When entity `E` has `recordVersionHistory = true` and a `foreignKey("x", X)` field where `X`
also has `recordVersionHistory = true`, `E`'s history table only stores the id of the
referenced `X` row (e.g. `alpha_id`). Because `X`'s own history table's primary key is
composite — `(id, version)` — there is currently no way to know, from a given `E` history
row, exactly which `X` history row was in effect at the moment that `E` history row was
written. This makes point-in-time reconstruction across the FK relationship ambiguous.

`maia-gen-spec`'s `HistoryFkValidationTest` already asserts "history entity FKing another
history entity is valid" — this is a legal, exercised scenario, just incompletely
implemented.

### Worked example

`BravoWithHistory.alpha` references `AlphaWithHistory`
(`maia-showcase/spec/.../MaiaShowcaseSpec.kt:1324-1336`). Both entities have
`recordVersionHistory = true`. Today:

- `BravoWithHistoryHistoryEntity` (`maia-showcase/domain/src/generated/.../join/BravoWithHistoryHistoryEntity.kt`)
  has an `alpha: DomainId` field and no way to know which `AlphaWithHistoryHistoryEntity`
  version was current when that Bravo history row was written.
- The generated DDL (`maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql:464-473`)
  shows `bravo_with_history_history.alpha_id` with a single-column
  `REFERENCES maia.alpha_with_history(id)` — pointing at Alpha's *live* table, not a specific
  historical snapshot.

This spec uses `BravoWithHistory` → `AlphaWithHistory` as the running example, and is the
proof-of-concept target for the implementation.

## Scope

- Applies whenever a `recordVersionHistory = true` entity has a plain `foreignKey()` field
  (`ForeignKeyFieldType`) referencing another `recordVersionHistory = true` entity.
- Inherited FK fields (via `superclassEntityDef`) are covered for free — the fix lives in the
  base `EntityDef.historyEntityDef` construction that every subclass's history entity already
  recurses through.
- **Out of scope**: many-to-many join entities' `leftEntity`/`rightEntity` references. These
  use a separate mechanism from `ForeignKeyFieldDef` and are a candidate follow-up once this
  pattern is proven on plain FK fields.

## Design

### 1. New synthetic history field

In `EntityDef.historyEntityDef` (`maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt:469-542`),
when building `historyFieldDefs`, for every FK field on the entity whose
`foreignEntityDef.withVersionHistory.value == true`, synthesize one extra `EntityFieldDef`
alongside the cloned fields (a new companion factory, analogous to the existing
`versionFieldDef()` / `changeTypeFieldDef()` helpers at the bottom of `EntityDef.kt` — there's
nothing to clone from since this field doesn't exist on the live entity):

- Name: `${fkFieldName}Version` (e.g. `alphaVersion`) → column `${fk_column}_version`
  (e.g. `alpha_version`)
- Type `Long`
- `IsPrimaryKey.FALSE` — it's a value column, not part of `E`'s own history PK
- `IsDeltaField.TRUE` — same delta-tracking treatment as other cloned history fields, so the
  History Blotter UI can highlight when the referenced version changed between two history
  rows
- Nullability mirrors the FK field's own nullability

### 2. Populating the value: live lookup at history-write time

The referenced entity's Dao (`AlphaWithHistoryDao`) gains a small lookup method, e.g.
`findVersionByPrimaryKey(id: DomainId): Long` — a single-column, PK-indexed read against the
*live* `alpha_with_history` table. (`recordVersionHistory` implies `versioned`, so the live
table always has a `version` column — confirmed via `AlphaWithHistoryEntity.kt`, which has
`val version: Long`.)

The referencing entity's Dao (`BravoWithHistoryDao`) gets `alphaWithHistoryDao` injected as an
extra constructor dependency — the same mechanism `JdbcDaoRenderer` already uses to inject
`historyDao` (`JdbcDaoRenderer.kt:119-126`).

Inside `insertHistory()` (which already performs I/O), immediately before
`historyDao.insert(...)`, look up:

```kotlin
val alphaVersion = alphaWithHistoryDao.findVersionByPrimaryKey(entity.alpha)
```

and pass it as an explicit override parameter into `history(entity, version, changeType,
alphaVersion)` — the same pattern already used for `version` and `changeType`, which are
passed explicitly rather than read directly off `entity`.

This captures Alpha's version at the *exact instant* Bravo's snapshot is written, even for
Bravo edits that don't touch `alpha` — which is the semantically correct fact to record. It
was chosen over denormalizing an `alphaVersion` column onto Bravo's *live* table because that
alternative would go stale between edits that don't touch `alpha`, unless refreshed on every
write path (not just ones that set the FK) — a more invasive change spread across every
`updateX()` method rather than a single centralized lookup point.

### 3. RowMapper

`BravoWithHistoryHistoryEntityRowMapper` needs one more line —
`rsa.readLong("alpha_version")` — same as the existing `version` read.

### 4. DDL — column + composite FK constraint

`CreateTableSqlRenderer` (`maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt`)
currently renders FK constraints inline, single-column only (`:95-106`). It needs to:

- Add the new column to the history table's `CREATE TABLE` block.
- Replace the *history table's* existing single-column
  `alpha_id ... REFERENCES maia.alpha_with_history(id)` with a table-level composite
  constraint:

  ```sql
  FOREIGN KEY (alpha_id, alpha_version) REFERENCES maia.alpha_with_history_history(id, version)
  ```

  - The **live** `bravo_with_history.alpha_id` column keeps its current single-column
    reference to the live `alpha_with_history(id)` table, unchanged.
  - Pointing the *history* table's constraint at Alpha's *history* table (rather than its live
    table, as today) is more correct, not just more precise: Alpha's history rows are
    permanent, so this constraint doesn't block deleting a live Alpha row just because old
    Bravo history rows mention it. It remains valid forever, referencing a specific immutable
    snapshot.
- This requires the renderer to support multi-column FK constraints, which it currently does
  not.

### 5. Schema-check tooling

`maia-gen-schema-check`'s `ExpectedSchemaExtractor` (see `ExpectedSchemaExtractorTest`) needs
to learn about composite FK constraints too, so it doesn't flag the new constraint as
unexpected drift when compared against the real database schema.

## Error handling / edge cases

- In the worked example the FK is `NOT NULL`, so the lookup always finds a row: the existing
  single-column FK on the *live* table already guarantees the referenced Alpha row exists
  whenever a Bravo write happens (Postgres won't allow deleting a live Alpha row while a live
  Bravo row still references it).
- If the FK field is nullable, skip the lookup and stamp `null` for `alphaVersion` when the FK
  id is null.
- Circular FK graphs (A → B → A) are not handled by this design — this is a pre-existing
  assumption of the FK modeling in general, not something new introduced here.

## Testing

- `maia-gen-spec`: extend `HistoryFkValidationTest`-style coverage to assert the new field
  appears on `historyEntityDef` (name, type, nullability) when the FK target is historized,
  and is absent when it isn't.
- `maia-gen-generator`: a `CreateTableSqlRendererTest` case for the composite FK constraint;
  coverage for the new RowMapper read and the new `history()` constructor parameter.
- Regenerate `maia-showcase` (`BravoWithHistory` / `AlphaWithHistory`) as the live
  proof-of-concept, confirm it compiles, and confirm the migration diff is clean (schema-check
  tooling doesn't flag drift).

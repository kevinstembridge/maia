# DB Schema Check Tool

## Purpose

Compare the actual Postgres schema of a running application database against the schema implied by that application's `ModelDef` (its spec DSL). Catches drift between hand-written Flyway migrations (`src/main/resources/db/migration/*.sql`) and the spec they're supposed to implement — e.g. a spec field added but the migration never written, or a migration typo that doesn't match the spec's declared type/nullability.

## Non-goals

- Not a migration generator or auto-fixer. It only reports drift; a human decides what to do about it.
- Postgres only. No support for other RDBMSes or for Mongo-backed entities.
- Does not compare Elasticsearch/typeahead index mappings, or anything not backed by a Postgres table.

## Architecture

New standalone Gradle module: `maia-gen/maia-gen-schema-check` (Kotlin, JVM 21, same conventions as sibling `maia-gen-*` modules). Depends on `maia-gen-spec`, `maia-gen-generator` (for the extracted expected-schema logic — see below), `maia-jdbc` (for `JdbcCompatibleType`), and the Postgres JDBC driver.

The Gradle task that wraps it lives in the existing `maia-gradle-plugin`, alongside `MaiaGenerationTask` — one Gradle plugin overall, following the existing pattern where a task is a thin worker wrapper around a library entry point on a dedicated classpath.

```
maia-gen-spec (ModelDef, EntityDef, EntityHierarchy, FieldType, JdbcCompatibleType...)
      |
maia-gen-generator (CreateTableSqlRenderer, new: ExpectedSchemaExtractor)
      |
maia-gen-schema-check (new module: ActualSchemaIntrospector, SchemaComparator, reporters, CLI main)
      |
maia-gradle-plugin (new: MaiaSchemaCheckTask, wraps the above via WorkerExecutor)
```

## Expected-schema extraction

`CreateTableSqlRenderer` (`maia-gen/maia-gen-generator/.../renderers/CreateTableSqlRenderer.kt`) already derives everything needed to know what a table *should* look like — it just renders that knowledge straight to SQL text instead of exposing it as data. Extract the derivation into a new public class in `maia-gen-generator`:

```kotlin
class ExpectedSchemaExtractor {
    fun extract(entityHierarchies: List<EntityHierarchy>): List<ExpectedTableDef>
}

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
)
data class ExpectedColumnDef(val name: String, val postgresType: String, val nullable: Boolean)
data class ExpectedForeignKeyDef(val columnName: String, val referencedSchemaAndTable: String, val referencedColumn: String)
data class ExpectedIndexDef(val name: String, val columns: List<String>, val unique: Boolean)
```

This carries over the existing logic as-is: `toSqlFieldDef` (type/nullable/size-constraint resolution per column, deduped by `TableColumnName` across a hierarchy), `shouldBeNullable`, `determineSizeConstraintFor`/`maxLengthOf` (varchar length appended to the type string, e.g. `varchar(100)`), PK column list, `databaseIndexDefs` per entity (deduped by column list), and FK derivation from `ForeignKeyFieldType` fields.

`CreateTableSqlRenderer` is refactored to call `ExpectedSchemaExtractor.extract(...)` and just format the result as `CREATE TABLE` SQL — no behavior change to existing generated output, verified by its existing tests.

Input is `applicationModelDef.rootEntityHierarchies` — one `ExpectedTableDef` per root `EntityHierarchy` (subclasses collapse into the same table with a `type_discriminator` column, matching existing behavior). This list already includes history tables (`entityDef.historyEntityDef`) and many-to-many join tables (`manyToManyEntity(...)`), confirmed in `AbstractSpec.populateEntityHierarchy` — both get added as their own root hierarchies, so no special-casing is needed to pick them up.

## Actual-schema introspection

New class `PostgresSchemaIntrospector` in `maia-gen-schema-check`, using plain JDBC (`java.sql.Connection`, no Spring/JdbcTemplate dependency needed) against a target database:

- **Tables**: `information_schema.tables`
- **Columns**: `information_schema.columns` — `column_name`, `data_type`, `udt_name`, `character_maximum_length`, `is_nullable`
- **Primary keys**: `information_schema.table_constraints` joined to `key_column_usage` (`constraint_type = 'PRIMARY KEY'`)
- **Foreign keys**: `table_constraints` + `key_column_usage` + `constraint_column_usage`
- **Indexes**: `pg_indexes` joined with `pg_index`/`pg_class` for `indisunique` and ordered column lists

Produces the actual-side mirror of the expected model:

```kotlin
data class ActualTableDef(
    val schemaAndTableName: String,
    val columns: List<ActualColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ActualForeignKeyDef>,
    val indexes: List<ActualIndexDef>,
)
data class ActualColumnDef(val name: String, val postgresType: String, val nullable: Boolean)
// ActualForeignKeyDef / ActualIndexDef mirror the Expected shapes
```

### Type normalization

Postgres reports types via `information_schema` in a form that doesn't literally match `JdbcCompatibleType.postgresDataType` strings — e.g. `timestamp without time zone` vs expected `timestamp`, `character varying` vs `varchar`, `ARRAY` with `udt_name = _text` vs expected `text[]`. A `PostgresActualTypeNormalizer` maps the introspected representation into the same `postgresDataType` string space the expected side uses (including appending `(<length>)` for varchar, mirroring `ExpectedColumnDef`'s format), so the diff is a plain string/boolean comparison, not fuzzy matching.

Identifier matching (table, column, index, constraint names) is by exact lowercase string — Postgres folds unquoted identifiers to lowercase, and the generator's names are already lowercase snake_case (`PostgresIdentifiers` already enforces the 63-char limit at generation time, so no truncation-collision handling is needed here).

## Diff engine

New class `SchemaComparator` in `maia-gen-schema-check`:

```kotlin
class SchemaComparator {
    fun compare(expected: List<ExpectedTableDef>, actual: List<ActualTableDef>): SchemaDiffReport
}

data class SchemaDiffReport(val tables: List<TableDiff>) {
    val hasErrors: Boolean get() = tables.any { it.hasErrors }
}

enum class TableStatus { OK, MISSING, EXTRA, MISMATCHED }

data class TableDiff(
    val schemaAndTableName: String,
    val status: TableStatus,
    val missingColumns: List<ExpectedColumnDef> = emptyList(),
    val extraColumns: List<String> = emptyList(),
    val mismatchedColumns: List<ColumnMismatch> = emptyList(),
    val primaryKeyMismatch: PrimaryKeyMismatch? = null,
    val missingForeignKeys: List<ExpectedForeignKeyDef> = emptyList(),
    val extraForeignKeys: List<String> = emptyList(),
    val missingIndexes: List<ExpectedIndexDef> = emptyList(),
    val extraIndexes: List<String> = emptyList(),
) {
    val hasErrors: Boolean get() = status == TableStatus.MISSING
        || missingColumns.isNotEmpty() || mismatchedColumns.isNotEmpty()
        || primaryKeyMismatch != null || missingForeignKeys.isNotEmpty() || missingIndexes.isNotEmpty()
}

data class ColumnMismatch(val name: String, val expectedType: String, val actualType: String, val expectedNullable: Boolean, val actualNullable: Boolean)
data class PrimaryKeyMismatch(val expected: List<String>, val actual: List<String>)
```

Classification:
- **Errors** (drive a non-zero exit code): missing table, missing column, column type/nullable mismatch, PK mismatch, missing FK, missing index.
- **Warnings** (reported, don't affect exit code by default): extra table, extra column, extra FK, extra index — visible so leftover cruft (e.g. a column removed from the spec but never dropped in the DB) is spotted without failing CI outright.

## Reporting

Two renderers consume the same `SchemaDiffReport`, no duplicated diff logic:

- **Console renderer**: grouped by table, ✓/✗ per table, errors and warnings clearly separated, summary line (`N tables checked, M errors, K warnings`).
- **JSON renderer**: the report serialized directly (kotlinx.serialization or Jackson, matching whatever the rest of `maia-gen-generator` already uses) — same shape for tooling/dashboards to consume.

## CLI entry point

`SchemaCheckMain.kt` in `maia-gen-schema-check`, with a `main()` for standalone use and a library function (`runSchemaCheck(...)`) the Gradle task also calls.

Args: `--spec-class=<fqcn>` (passed to the existing `ApplicationModelDefInstantiator.instantiate`), `--jdbc-url=<url>`, `--username=<user>`, `--password=<pass>` (optional; prefer the `MAIA_SCHEMA_CHECK_DB_PASSWORD` env var instead, since a CLI arg can leak into shell history/process listings), `--format=text|json` (default `text`), `--output=<file>` (optional, default stdout).

Exit codes: `0` = no errors (warnings allowed), `1` = errors found in the diff, `2` = tool failure (bad args, can't connect to DB, spec class not found/instantiable).

## Gradle task

`MaiaSchemaCheckTask` in `maia-gradle-plugin`, registered alongside `MaiaGenerationTask`. New properties on the existing `maia` extension:

```kotlin
val schemaCheckJdbcUrl = objects.property(String::class.java)
val schemaCheckUsername = objects.property(String::class.java)
val schemaCheckPassword = objects.property(String::class.java) // convention: providers.environmentVariable(...)
val schemaCheckOutputFormat = objects.property(String::class.java) // "text" | "json", convention "text"
```

New configuration `maiaSchemaCheckImplementation` (classpath = `maia-gen-schema-check` + the app's own spec module classes), mirroring how `maiaGenImplementation` works for `MaiaGenerationTask` today. The task is a `WorkerExecutor`-based wrapper (same `classLoaderIsolation` pattern as `MaiaGenerationWorkAction`) that calls `runSchemaCheck(...)`. The task fails the build (throws) when the report has errors, unless an `ignoreErrors` flag is set on the task — matching Gradle's usual verification-task convention (e.g. `Test.ignoreFailures`).

## Testing

- **Unit tests** for `ExpectedSchemaExtractor` — reuse/extend `CreateTableSqlRenderer`'s existing test fixtures, since the logic just moved location, not behavior.
- **Unit tests** for `PostgresActualTypeNormalizer` — table-driven: Postgres `information_schema` type representation → expected `postgresDataType` string, covering every `JdbcCompatibleType` value.
- **Unit tests** for `SchemaComparator` — synthetic `ExpectedTableDef`/`ActualTableDef` pairs covering each diff category: missing/extra table, missing/extra/mismatched column, nullable mismatch, PK mismatch, missing/extra FK, missing/extra index.
- **Integration test**: against `maia-showcase`'s real spec and its Docker Postgres (`docker compose -f maia-showcase/compose.yaml up -d`, port 5433) with its actual Flyway-migrated schema — run the tool end-to-end and assert on deliberately-seeded drift (e.g. a test-only schema tweak that removes/retypes a column) to get a true regression test, since showcase's migrations are hand-written and could legitimately drift from its spec.
- No UI to test — this is a headless CLI/Gradle task.

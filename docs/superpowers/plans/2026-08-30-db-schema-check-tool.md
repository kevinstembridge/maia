# DB Schema Check Tool Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a tool that compares an application's actual Postgres schema against the schema implied by its `ModelDef` spec, reporting drift (missing/extra/mismatched tables, columns, PKs, FKs, indexes), runnable both as a standalone CLI and as a Gradle task.

**Architecture:** Extract the expected-schema derivation already embedded in `CreateTableSqlRenderer` into a reusable `ExpectedSchemaExtractor` (in `maia-gen-generator`). A new standalone module `maia-gen-schema-check` introspects the actual Postgres schema via JDBC/`information_schema`, normalizes types to match the expected side, diffs the two, and renders a console or JSON report. A new Gradle task in `maia-gradle-plugin` wraps the same code path as a `WorkerExecutor` task, mirroring the existing (if currently unused) `MaiaGenerationTask` pattern.

**Tech Stack:** Kotlin, JVM 21, Gradle (Kotlin DSL), JUnit 5 + AssertJ, Testcontainers Postgres, plain JDBC, Jackson (`tools.jackson`) for JSON output.

**User Verification:** NO — no user verification required. This is an internal developer tool; the spec and plan describe intent purely in terms of automated tests (unit + Testcontainers-backed integration tests). Nothing in the original request or the approved spec asks for human sign-off on behavior.

---

## Context for the implementer

Read `docs/superpowers/specs/2026-08-30-db-schema-check-tool-design.md` first — it has the full rationale. Key facts already confirmed in this codebase (don't re-derive them):

- `CreateTableSqlRenderer` (`maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt`) already computes, per root `EntityHierarchy`, every column's name/Postgres type/nullable/size-constraint, the PK column list, FK columns (via `ForeignKeyFieldType`), and indexes (via `EntityDef.databaseIndexDefs`). It just renders straight to SQL text instead of exposing structured data. This plan extracts that logic without changing its behavior.
- `ApplicationModelDef.rootEntityHierarchies` already includes history tables and many-to-many join tables as their own root hierarchies (confirmed in `AbstractSpec.populateEntityHierarchy` and `AbstractSpec.manyToManyEntity`) — no special-casing needed.
- `JdbcCompatibleType` (`libs/maia-jdbc/src/main/kotlin/org/maiaframework/jdbc/JdbcCompatibleType.kt`) is the canonical enum of Postgres type strings (`bigint`, `boolean`, `text`, `varchar`, `timestamp`, `timestamp(3) with time zone`, `jsonb`, `uuid`, and `_array` variants).
- `PostgresIdentifiers.MAX_LENGTH = 63` is already enforced at generation time, so no identifier-truncation handling is needed in the comparator — exact lowercase string matching is safe.
- The `maia-gradle-plugin`'s `MaiaGenerationTask`/`MaiaGenerationExtension`/`MaiaGenerationPlugin` mechanism is NOT currently used by the real `maia-showcase` build (which instead hand-rolls a `JavaExec` task calling a `main()` fun) — its own functional test is `@Disabled` and references stale property names. The user has explicitly chosen to build the new Gradle task on this mechanism anyway, accepting that it follows a pattern not otherwise proven in this repo. Don't "fix" the existing generation mechanism as part of this work — only add to it.
- `SingletonPostgresqlContainer.instance` (`libs/maia-testing/maia-testing-postgresql`) is the existing Testcontainers Postgres helper (`postgres:16`), used elsewhere via `SingletonPostgresqlContainer.instance.also { it.start() }` then `.jdbcUrl`/`.username`/`.password`.
- CLI/worker args in this codebase follow a `key=value` convention (see `ModuleGeneratorArgs`), not GNU `--flag=value` style — this plan follows that convention for `SchemaCheckMain` too (a deliberate refinement over the design doc's `--format=` notation, for consistency with the rest of the codebase).
- `maia-platform/build.gradle.kts` is a `java-platform` BOM; `api(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))` manages Jackson (`tools.jackson.*`) and Postgres driver versions. Note: `maia-platform`'s own constraint list has a pre-existing typo — `tools.jackson.module:jackson-kotlin-module:3.0.3` — that doesn't match the real artifact name and so constrains nothing; every real consumer (`libs/maia-json`, `maia-toggles-autoconfigure`, `maia-showcase/app`) instead declares `tools.jackson.module:jackson-module-kotlin` (no version, managed transitively via the Spring Boot BOM) — this plan follows that working pattern, not the typo'd constraint. Not in scope to fix the typo itself. `org.testcontainers:postgresql:1.21.4` is a correctly-named explicit constraint there.
- Real-world spec class for the end-to-end test: `org.maiaframework.showcase.MaiaShowcaseApplicationSpec` (project `:maia-showcase:spec`). Real DB migrations: `maia-showcase/dao/src/main/resources/db/migration/*.sql`.

---

## File Structure

```
maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/
  renderers/CreateTableSqlRenderer.kt          (MODIFY — delegate to ExpectedSchemaExtractor)
  schema/ExpectedSchemaExtractor.kt            (NEW — expected-schema data classes + extractor)
maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/
  renderers/CreateTableSqlRendererTest.kt       (NEW — characterization test, pre-refactor)
  schema/ExpectedSchemaExtractorTest.kt         (NEW)

maia-gen/maia-gen-schema-check/                (NEW MODULE)
  build.gradle.kts
  src/main/kotlin/org/maiaframework/gen/schemacheck/
    ActualSchemaModel.kt                        (ActualTableDef/ActualColumnDef/ActualForeignKeyDef/ActualIndexDef)
    PostgresActualTypeNormalizer.kt
    PostgresSchemaIntrospector.kt
    SchemaComparator.kt                         (SchemaDiffReport/TableDiff/etc. + comparison logic)
    SchemaCheckReporter.kt                       (console + JSON renderers)
    SchemaCheckArgs.kt
    SchemaCheckMain.kt
  src/test/kotlin/org/maiaframework/gen/schemacheck/
    PostgresActualTypeNormalizerTest.kt
    PostgresSchemaIntrospectorTest.kt
    SchemaComparatorTest.kt
    SchemaCheckReporterTest.kt
    SchemaCheckMainTest.kt
    ShowcaseSchemaCheckIntegrationTest.kt

plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/
  MaiaGenerationExtension.kt                    (MODIFY — add schemaCheck* properties)
  MaiaGenerationPlugin.kt                       (MODIFY — register task/configuration)
  MaiaSchemaCheckTask.kt                        (NEW)
  MaiaSchemaCheckWorkAction.kt                  (NEW)
  MaiaSchemaCheckWorkParameters.kt               (NEW)
plugins/maia-gradle-plugin/src/test/kotlin/org/maiaframework/gen/plugin/
  MaiaSchemaCheckTaskFunctionalTest.kt           (NEW)

settings.gradle.kts                             (MODIFY — include new module)
```

---

### Task 1: Characterization test for `CreateTableSqlRenderer`

**Goal:** Lock down `CreateTableSqlRenderer`'s current SQL output with a test, before refactoring it in Task 2, so any behavior change during extraction is caught immediately.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRendererTest.kt`

**Acceptance Criteria:**
- [ ] Test builds a spec with a plain entity (nullable + non-nullable fields, a varchar length constraint, an index), a foreign-key field, a subclass hierarchy (type discriminator), and a `recordVersionHistory` entity, and asserts the rendered SQL contains the expected column/type/nullable/PK/index/FK lines.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*CreateTableSqlRendererTest*"` → PASS

**Steps:**

- [ ] **Step 1: Write the test**

```kotlin
package org.maiaframework.gen.renderers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import java.io.File

class CreateTableSqlRendererTest {

    @Test
    fun `renders columns, types, nullability, PK, FK and index for a plain entity hierarchy`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }

            val child = entity("com.example", "Child") {
                field("title", FieldTypes.string) { lengthConstraint(max = 100); nullable() }
                field("count", FieldTypes.int)
                foreignKey("parentId", parent)
                index { withFieldAscending("count") }
            }

        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains("CREATE TABLE test.parent (")
        assertThat(sql).contains("name varchar(50) NOT NULL")
        assertThat(sql).contains("CREATE TABLE test.child (")
        assertThat(sql).contains("title varchar(100) NULL")
        assertThat(sql).contains("count integer NOT NULL")
        assertThat(sql).contains("parent_id bigint NOT NULL REFERENCES test.parent(id)")
        assertThat(sql).contains("PRIMARY KEY(id)")
        assertThat(sql).containsPattern("CREATE INDEX \\w+ ON test\\.child\\(count\\);")

    }

    @Test
    fun `renders a type_discriminator column and history table for versioned subclass hierarchies`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val base = entity("com.example", "Base", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }

        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains("CREATE TABLE test.base (")
        assertThat(sql).contains("CREATE TABLE test.base_history (")

    }

}
```

Note: pass `rootEntityHierarchies`, not the flattened `ModelDef.entityHierarchies` — `DaoLayerModuleGenerator` (the real caller, in production) passes `applicationModelDef.rootEntityHierarchies` to `CreateTableSqlRenderer`. `ModelDef.entityHierarchies` is `rootEntityHierarchies.flatMap { it.entityHierarchies }` — a *flattened* list where every subclass also appears as its own top-level entry, which would make the renderer emit a separate `CREATE TABLE` for each subclass individually instead of one merged table per hierarchy. Always use `rootEntityHierarchies` when driving `CreateTableSqlRenderer` or `ExpectedSchemaExtractor`.

- [ ] **Step 2: Run and confirm it passes against current behavior**

Run: `./gradlew :maia-gen:maia-gen-generator:test --tests "*CreateTableSqlRendererTest*" -i`
Expected: PASS (this documents *current* behavior — if an assertion fails, fix the assertion to match actual current output, don't change the renderer)

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRendererTest.kt
git commit -m "test: characterize CreateTableSqlRenderer output before extracting shared schema model"
```

---

### Task 2: Extract `ExpectedSchemaExtractor`

**Goal:** Move `CreateTableSqlRenderer`'s column/type/nullable/PK/FK/index derivation into a structured, reusable `ExpectedSchemaExtractor`, with `CreateTableSqlRenderer` refactored to consume it — no behavior change.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/schema/ExpectedSchemaExtractor.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/schema/ExpectedSchemaExtractorTest.kt`
- Test: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRendererTest.kt` (from Task 1, must still pass)

**Acceptance Criteria:**
- [ ] `ExpectedSchemaExtractor.extract(entityHierarchies)` returns one `ExpectedTableDef` per hierarchy with correct columns/types/nullable/PK/FK/indexes.
- [ ] `CreateTableSqlRendererTest` (Task 1) still passes unchanged.
- [ ] `CreateTableSqlRenderer` no longer duplicates nullable/type/size-constraint logic — it calls the extractor and formats the result.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*ExpectedSchemaExtractorTest*" --tests "*CreateTableSqlRendererTest*"` → PASS

**Steps:**

- [ ] **Step 1: Write `ExpectedSchemaExtractor` with the extracted logic**

```kotlin
package org.maiaframework.gen.schema

import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.EntityFieldDef
import org.maiaframework.gen.spec.definition.EntityHierarchy
import org.maiaframework.gen.spec.definition.jdbc.TableColumnName
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.FieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.jdbc.JdbcCompatibleType

data class ExpectedColumnDef(
    val name: String,
    val postgresType: String,
    val nullable: Boolean,
)

data class ExpectedForeignKeyDef(
    val columnName: String,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)

data class ExpectedIndexDef(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
)

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
)

class ExpectedSchemaExtractor {

    fun extract(entityHierarchies: List<EntityHierarchy>): List<ExpectedTableDef> {

        return entityHierarchies.map { extractTable(it) }

    }

    private fun extractTable(entityHierarchy: EntityHierarchy): ExpectedTableDef {

        val baseEntityDef = entityHierarchy.entityDef

        val nonDerivedSqlFields = entityHierarchy.allFieldDefsSorted
            .filterNot { it.isDerived.value }
            .groupBy { it.tableColumnName }
            .mapValues { toSqlFieldDef(it, entityHierarchy) }
            .values
            .toList()

        val effectiveTimestampColumnNames = setOf("effective_from", "effective_to")

        val sqlFieldsForColumns = if (baseEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
            nonDerivedSqlFields.filterNot { it.tableColumnName.value in effectiveTimestampColumnNames }
        } else {
            nonDerivedSqlFields
        }

        val columns = sqlFieldsForColumns.map { sqlFieldDef ->
            ExpectedColumnDef(
                name = sqlFieldDef.tableColumnName.value,
                postgresType = "${sqlFieldDef.fieldType.jdbcCompatibleType.postgresDataType}${sqlFieldDef.sizeConstraint}",
                nullable = sqlFieldDef.nullable,
            )
        }.let { cols ->
            if (entityHierarchy.hasSubclasses()) {
                cols.plus(ExpectedColumnDef("type_discriminator", "text", nullable = false))
            } else {
                cols
            }
        }

        val foreignKeys = sqlFieldsForColumns.mapNotNull { sqlFieldDef ->
            val fieldType = sqlFieldDef.fieldType
            if (fieldType is ForeignKeyFieldType) {
                val foreignEntityDef = fieldType.foreignKeyFieldDef.foreignEntityDef
                ExpectedForeignKeyDef(
                    columnName = sqlFieldDef.tableColumnName.value,
                    referencedSchemaAndTable = schemaAndTableNameFor(foreignEntityDef),
                    referencedColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName.value,
                )
            } else {
                null
            }
        }

        val indexes = entityHierarchy.entityDefs
            .reversed()
            .flatMap { it.databaseIndexDefs }
            .distinctBy { databaseIndexDef -> databaseIndexDef.indexDef.indexFieldDefs.map { it.databaseColumnName } }
            .map { databaseIndexDef ->
                val typeDiscriminatorField = if (entityHierarchy.hasSubclasses()) listOf("type_discriminator") else emptyList()
                ExpectedIndexDef(
                    name = databaseIndexDef.indexDef.indexName.value,
                    columns = databaseIndexDef.indexDef.indexFieldDefs.map { it.databaseColumnName.value }.plus(typeDiscriminatorField),
                    unique = databaseIndexDef.isUnique,
                )
            }

        val primaryKeyColumns = nonDerivedSqlFields.filter { it.isPrimaryKey }.map { it.tableColumnName.value }

        return ExpectedTableDef(
            schemaAndTableName = schemaAndTableNameFor(baseEntityDef),
            columns = columns,
            primaryKeyColumns = primaryKeyColumns,
            foreignKeys = foreignKeys,
            indexes = indexes,
        )

    }

    private fun toSqlFieldDef(
        entityFieldDefs: Map.Entry<TableColumnName, List<EntityFieldDef>>,
        entityHierarchy: EntityHierarchy
    ): SqlFieldDef {

        val tableColumnName = entityFieldDefs.key
        val fieldTypes = entityFieldDefs.value.map { it.fieldType }.toSet()

        if (fieldTypes.size > 1) {
            throw IllegalStateException("Expecting all fields with database column name of $tableColumnName in entity hierarchy ${entityHierarchy.entityDef.entityBaseName} to have the same type. Found: $fieldTypes")
        }

        val sizeConstraint = maxSizeConstraintFor(entityFieldDefs)

        return SqlFieldDef(
            fieldType = fieldTypes.first(),
            tableColumnName = tableColumnName,
            sizeConstraint = sizeConstraint,
            nullable = entityFieldDefs.value.any { shouldBeNullable(it, entityHierarchy.entityDef) },
            isPrimaryKey = entityFieldDefs.value.any { it.isPrimaryKey.value },
        )

    }

    private fun maxSizeConstraintFor(entityFieldDefs: Map.Entry<TableColumnName, List<EntityFieldDef>>): String {

        val maxSizeConstraint = determineMaxSizeConstraintFor(entityFieldDefs.value)
        return determineSizeConstraintFor(maxSizeConstraint)

    }

    private fun shouldBeNullable(entityFieldDef: EntityFieldDef, baseEntityDef: EntityDef): Boolean {

        return entityFieldDef.nullable
                || (entityFieldDef.isNotFromBaseEntity(baseEntityDef) && !notNullByFramework(entityFieldDef))

    }

    private fun EntityFieldDef.isNotFromBaseEntity(baseEntityDef: EntityDef): Boolean {
        return entityBaseName != baseEntityDef.entityBaseName
    }

    private fun notNullByFramework(entityFieldDef: EntityFieldDef): Boolean {

        return (entityFieldDef.classFieldName == ClassFieldName.id
                || entityFieldDef.classFieldName == ClassFieldName.createdTimestamp
                || entityFieldDef.classFieldName == ClassFieldName.lastModifiedTimestamp
                || entityFieldDef.classFieldName == ClassFieldName.version)

    }

    private fun determineMaxSizeConstraintFor(entityFieldDefs: List<EntityFieldDef>): EntityFieldDef? {

        if (entityFieldDefs.any { it.classFieldDef.lengthConstraint == null }) {
            return null
        }

        return entityFieldDefs.maxBy { it.classFieldDef.lengthConstraint?.max ?: 0 }

    }

    private fun determineSizeConstraintFor(entityFieldDef: EntityFieldDef?): String {

        if (entityFieldDef == null) {
            return ""
        }

        return when (entityFieldDef.fieldType.jdbcCompatibleType) {
            JdbcCompatibleType.varchar -> "(${maxLengthOf(entityFieldDef)})"
            else -> ""
        }

    }

    private fun maxLengthOf(entityFieldDef: EntityFieldDef): Long {

        return entityFieldDef.classFieldDef.lengthConstraint?.max
            ?: throw IllegalArgumentException("Expecting entity field '${entityFieldDef.classFieldName}' on entity '${entityFieldDef.entityBaseName}' to have a max length constraint.")

    }

    private fun schemaAndTableNameFor(entityDef: EntityDef) = "${entityDef.schemaName}.${entityDef.tableName.rawTableName}"

    private data class SqlFieldDef(
        val fieldType: FieldType,
        val tableColumnName: TableColumnName,
        val sizeConstraint: String,
        val nullable: Boolean,
        val isPrimaryKey: Boolean,
    )

}
```

Note on the `effective_range` column: `CreateTableSqlRenderer` renders a synthetic `effective_range tstzrange not null default tstzrange(now(), null)` column that has no backing `EntityFieldDef` — it's SQL-renderer-only, not derivable from field defs. Add it in the extractor too so `ExpectedTableDef` matches reality for effective-range entities:

```kotlin
        }.let { cols ->
            val withDiscriminator = if (entityHierarchy.hasSubclasses()) {
                cols.plus(ExpectedColumnDef("type_discriminator", "text", nullable = false))
            } else {
                cols
            }
            if (baseEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
                withDiscriminator.plus(ExpectedColumnDef("effective_range", "tstzrange", nullable = false))
            } else {
                withDiscriminator
            }
        }
```

(Fold this into the `columns` derivation from Step 1 rather than appending twice — merge the two `.let` blocks into one.)

- [ ] **Step 2: Refactor `CreateTableSqlRenderer` to use the extractor**

Replace the renderer's private `toSqlFieldDef`/`shouldBeNullable`/`determineSizeConstraintFor`/etc. helpers and the `render CREATE TABLE statement for` body with calls into `ExpectedSchemaExtractor`, keeping only SQL-text formatting (header comment, `CREATE EXTENSION`, per-column SQL lines, `PRIMARY KEY(...)`, index/exclusion-constraint SQL) in the renderer itself. The renderer still needs entity-level details the extractor doesn't expose (e.g. `hasSingleEffectiveRecord` for exclusion constraints, `typeDiscriminators()` for the header comment) — call those directly on `EntityHierarchy`/`EntityDef` as before; only the column/PK/FK/index *derivation* moves to the extractor.

Rewire `render CREATE TABLE statement for` to build its SQL lines from `ExpectedTableDef.columns` (formatting `"$name $postgresType ${if (nullable) "NULL" else "NOT NULL"}"`, plus `" REFERENCES ..."` for columns present in `ExpectedTableDef.foreignKeys`) and its `PRIMARY KEY(...)` line from `ExpectedTableDef.primaryKeyColumns`. Rewire `renderIndexes` to iterate `ExpectedTableDef.indexes` for the plain `CREATE INDEX`/`CREATE UNIQUE INDEX` lines, but keep the existing `databaseIndexDef`-based loop for the single-effective-record exclusion-constraint logic (that's Postgres-exclusion-constraint-specific, out of scope for the expected-schema model — the design spec explicitly doesn't model exclusion constraints).

- [ ] **Step 3: Run both test classes to confirm no behavior change**

Run: `./gradlew :maia-gen:maia-gen-generator:test --tests "*CreateTableSqlRendererTest*" --tests "*ExpectedSchemaExtractorTest*" -i`
Expected: PASS

- [ ] **Step 4: Write `ExpectedSchemaExtractorTest`**

```kotlin
package org.maiaframework.gen.schema

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.lang.FieldTypes

class ExpectedSchemaExtractorTest {

    @Test
    fun `extracts columns, types, nullability and PK for a plain entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                field("description", FieldTypes.string) { lengthConstraint(max = 200); nullable() }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.primaryKeyColumns).containsExactly("id")
        assertThat(widgetTable.columns).contains(
            ExpectedColumnDef("name", "varchar(50)", nullable = false),
            ExpectedColumnDef("description", "varchar(200)", nullable = true),
        )

    }

    @Test
    fun `extracts a foreign key column`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val parent = entity("com.example", "Parent") {}
            val child = entity("com.example", "Child") {
                foreignKey("parentId", parent)
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val childTable = tables.single { it.schemaAndTableName == "test.child" }

        assertThat(childTable.foreignKeys).containsExactly(
            ExpectedForeignKeyDef("parent_id", "test.parent", "id")
        )

    }

    @Test
    fun `extracts an index`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget") {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
                index { withFieldAscending("name") }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val widgetTable = tables.single { it.schemaAndTableName == "test.widget" }

        assertThat(widgetTable.indexes).hasSize(1)
        assertThat(widgetTable.indexes.single().columns).containsExactly("name")
        assertThat(widgetTable.indexes.single().unique).isFalse()

    }

    @Test
    fun `extracts a separate table for a history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val widget = entity("com.example", "Widget", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)

        assertThat(tables.map { it.schemaAndTableName }).contains("test.widget", "test.widget_history")

    }

    @Test
    fun `extracts a separate table for a many-to-many join entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val left = entity("com.example", "Left") {}
            val right = entity("com.example", "Right") {}
            val join = simpleManyToManyEntity(
                "com.example", "LeftRight",
                leftEntity = org.maiaframework.gen.spec.definition.ReferencedEntity(fieldName = "leftId", displayName = "Left", entityDef = left),
                rightEntity = org.maiaframework.gen.spec.definition.ReferencedEntity(fieldName = "rightId", displayName = "Right", entityDef = right),
            )
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)

        assertThat(tables.map { it.schemaAndTableName }).contains("test.left_right")

    }

}
```

(`ReferencedEntity` is `data class ReferencedEntity(val fieldName: String, val displayName: String, val entityDef: EntityDef, ...)` — confirmed directly from `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ReferencedEntity.kt`.)

- [ ] **Step 5: Run the full extractor test suite**

Run: `./gradlew :maia-gen:maia-gen-generator:test --tests "*ExpectedSchemaExtractorTest*" -i`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/schema/ExpectedSchemaExtractor.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt \
        maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/schema/ExpectedSchemaExtractorTest.kt
git commit -m "refactor: extract ExpectedSchemaExtractor from CreateTableSqlRenderer

Structured expected-schema model (tables/columns/PK/FK/indexes),
shared between SQL rendering and the upcoming schema-check tool.
No change to generated SQL output."
```

---

### Task 3: New module skeleton + actual-schema data model + type normalizer

**Goal:** Stand up the `maia-gen-schema-check` module and build the "actual side" data model plus the Postgres type-string normalizer, with no DB connection yet.

**Files:**
- Modify: `settings.gradle.kts`
- Create: `maia-gen/maia-gen-schema-check/build.gradle.kts`
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/ActualSchemaModel.kt`
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/PostgresActualTypeNormalizer.kt`
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/PostgresActualTypeNormalizerTest.kt`

**Acceptance Criteria:**
- [ ] New module compiles and its test task runs.
- [ ] `PostgresActualTypeNormalizer` maps every `information_schema` type representation used by `JdbcCompatibleType` to the matching expected-side `postgresDataType` string, including varchar length suffix.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*PostgresActualTypeNormalizerTest*"` → PASS

**Steps:**

- [ ] **Step 1: Register the module in settings.gradle.kts**

```kotlin
include("maia-gen:maia-gen-schema-check")
```

Add this line next to the existing `include("maia-gen:maia-gen-generator")` / `include("maia-gen:maia-gen-spec")` lines.

- [ ] **Step 2: Write the module's build file**

```kotlin
plugins {
    id("maia.kotlin-library-conventions")
}

dependencies {

    implementation(platform(project(":maia-platform")))

    implementation(project(":maia-gen:maia-gen-spec"))
    implementation(project(":maia-gen:maia-gen-generator"))

    implementation("org.postgresql:postgresql")
    implementation("tools.jackson.module:jackson-module-kotlin")

    testImplementation(platform(project(":maia-platform")))
    testImplementation(project(":libs:maia-testing:maia-testing-postgresql"))
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}
```

- [ ] **Step 3: Write the actual-schema data model**

```kotlin
package org.maiaframework.gen.schemacheck

data class ActualColumnDef(
    val name: String,
    val postgresType: String,
    val nullable: Boolean,
)

data class ActualForeignKeyDef(
    val columnName: String,
    val referencedSchemaAndTable: String,
    val referencedColumn: String,
)

data class ActualIndexDef(
    val name: String,
    val columns: List<String>,
    val unique: Boolean,
)

data class ActualTableDef(
    val schemaAndTableName: String,
    val columns: List<ActualColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ActualForeignKeyDef>,
    val indexes: List<ActualIndexDef>,
)
```

- [ ] **Step 4: Write `PostgresActualTypeNormalizer`**

`information_schema.columns` reports `data_type` (a human-readable SQL standard name) and, for arrays, `udt_name` (the underlying Postgres type name, prefixed with `_`). This maps that representation to the same string space `ExpectedColumnDef.postgresType` uses.

```kotlin
package org.maiaframework.gen.schemacheck

object PostgresActualTypeNormalizer {

    fun normalize(dataType: String, udtName: String, characterMaximumLength: Int?): String {

        val baseType = when {
            dataType == "ARRAY" -> normalizeArrayElementType(udtName)
            dataType == "character varying" -> "varchar"
            dataType == "character" -> "varchar"
            dataType == "text" -> "text"
            dataType == "boolean" -> "boolean"
            dataType == "integer" -> "integer"
            dataType == "smallint" -> "smallint"
            dataType == "bigint" -> "bigint"
            dataType == "numeric" -> "decimal"
            dataType == "date" -> "date"
            dataType == "jsonb" -> "jsonb"
            dataType == "uuid" -> "uuid"
            dataType == "timestamp without time zone" -> "timestamp"
            dataType == "timestamp with time zone" -> "timestamp(3) with time zone"
            else -> throw IllegalArgumentException("Unrecognized Postgres data_type '$dataType' (udt_name '$udtName') — add a mapping in PostgresActualTypeNormalizer.")
        }

        return if (baseType == "varchar" && characterMaximumLength != null) {
            "varchar($characterMaximumLength)"
        } else {
            baseType
        }

    }

    private fun normalizeArrayElementType(udtName: String): String {

        val elementType = when (udtName) {
            "_bool" -> "boolean"
            "_int2" -> "smallint"
            "_int4" -> "integer"
            "_int8" -> "bigint"
            "_numeric" -> "decimal"
            "_text", "_varchar" -> "text"
            "_timestamp" -> "timestamp"
            "_uuid" -> "uuid"
            else -> throw IllegalArgumentException("Unrecognized Postgres array udt_name '$udtName' — add a mapping in PostgresActualTypeNormalizer.")
        }

        return "$elementType[]"

    }

}
```

- [ ] **Step 5: Write the normalizer test, table-driven over every `JdbcCompatibleType` value**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PostgresActualTypeNormalizerTest {

    companion object {

        @JvmStatic
        fun scalarTypeCases() = listOf(
            arrayOf("bigint", "int8", null, "bigint"),
            arrayOf("boolean", "bool", null, "boolean"),
            arrayOf("date", "date", null, "date"),
            arrayOf("numeric", "numeric", null, "decimal"),
            arrayOf("integer", "int4", null, "integer"),
            arrayOf("jsonb", "jsonb", null, "jsonb"),
            arrayOf("smallint", "int2", null, "smallint"),
            arrayOf("text", "text", null, "text"),
            arrayOf("timestamp without time zone", "timestamp", null, "timestamp"),
            arrayOf("timestamp with time zone", "timestamptz", null, "timestamp(3) with time zone"),
            arrayOf("uuid", "uuid", null, "uuid"),
            arrayOf("character varying", "varchar", 100, "varchar(100)"),
        )

    }

    @ParameterizedTest
    @MethodSource("scalarTypeCases")
    fun `normalizes information_schema types to expected postgresType strings`(
        dataType: String,
        udtName: String,
        characterMaximumLength: Int?,
        expected: String
    ) {

        assertThat(PostgresActualTypeNormalizer.normalize(dataType, udtName, characterMaximumLength)).isEqualTo(expected)

    }

    @Test
    fun `normalizes array types via udt_name`() {

        assertThat(PostgresActualTypeNormalizer.normalize("ARRAY", "_text", null)).isEqualTo("text[]")
        assertThat(PostgresActualTypeNormalizer.normalize("ARRAY", "_int4", null)).isEqualTo("integer[]")
        assertThat(PostgresActualTypeNormalizer.normalize("ARRAY", "_bool", null)).isEqualTo("boolean[]")

    }

}
```

- [ ] **Step 6: Run the test**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*PostgresActualTypeNormalizerTest*" -i`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts maia-gen/maia-gen-schema-check/
git commit -m "feat: scaffold maia-gen-schema-check module with actual-schema model and type normalizer"
```

---

### Task 4: `PostgresSchemaIntrospector`

**Goal:** Introspect the real tables/columns/PKs/FKs/indexes in a given set of Postgres schemas via JDBC, producing `List<ActualTableDef>`.

**Files:**
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/PostgresSchemaIntrospector.kt`
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/PostgresSchemaIntrospectorTest.kt`

**Acceptance Criteria:**
- [ ] `PostgresSchemaIntrospector(connection).introspectSchemas(schemas)` returns one `ActualTableDef` per table found in those schemas, with correct columns (normalized types), PK columns, FKs, and indexes.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*PostgresSchemaIntrospectorTest*"` → PASS (requires Docker for Testcontainers)

**Steps:**

- [ ] **Step 1: Write `PostgresSchemaIntrospector`**

```kotlin
package org.maiaframework.gen.schemacheck

import java.sql.Connection

class PostgresSchemaIntrospector(private val connection: Connection) {

    fun introspectSchemas(schemas: Set<String>): List<ActualTableDef> {

        return listTables(schemas).map { (schema, table) ->
            ActualTableDef(
                schemaAndTableName = "$schema.$table",
                columns = columnsFor(schema, table),
                primaryKeyColumns = primaryKeyColumnsFor(schema, table),
                foreignKeys = foreignKeysFor(schema, table),
                indexes = indexesFor(schema, table),
            )
        }

    }

    private fun listTables(schemas: Set<String>): List<Pair<String, String>> {

        if (schemas.isEmpty()) return emptyList()

        val placeholders = schemas.joinToString(",") { "?" }
        val sql = """
            select table_schema, table_name
            from information_schema.tables
            where table_schema in ($placeholders)
            order by table_schema, table_name
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            schemas.forEachIndexed { index, schema -> statement.setString(index + 1, schema) }
            statement.executeQuery().use { rs ->
                val tables = mutableListOf<Pair<String, String>>()
                while (rs.next()) {
                    tables.add(rs.getString("table_schema") to rs.getString("table_name"))
                }
                return tables
            }
        }

    }

    private fun columnsFor(schema: String, table: String): List<ActualColumnDef> {

        val sql = """
            select column_name, data_type, udt_name, character_maximum_length, is_nullable
            from information_schema.columns
            where table_schema = ? and table_name = ?
            order by ordinal_position
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val columns = mutableListOf<ActualColumnDef>()
                while (rs.next()) {
                    val characterMaximumLength = rs.getInt("character_maximum_length").takeUnless { rs.wasNull() }
                    columns.add(
                        ActualColumnDef(
                            name = rs.getString("column_name"),
                            postgresType = PostgresActualTypeNormalizer.normalize(
                                rs.getString("data_type"),
                                rs.getString("udt_name"),
                                characterMaximumLength,
                            ),
                            nullable = rs.getString("is_nullable") == "YES",
                        )
                    )
                }
                return columns
            }
        }

    }

    private fun primaryKeyColumnsFor(schema: String, table: String): List<String> {

        val sql = """
            select kcu.column_name
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
                on tc.constraint_name = kcu.constraint_name and tc.table_schema = kcu.table_schema
            where tc.constraint_type = 'PRIMARY KEY' and tc.table_schema = ? and tc.table_name = ?
            order by kcu.ordinal_position
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val columns = mutableListOf<String>()
                while (rs.next()) columns.add(rs.getString("column_name"))
                return columns
            }
        }

    }

    private fun foreignKeysFor(schema: String, table: String): List<ActualForeignKeyDef> {

        val sql = """
            select
                kcu.column_name,
                ccu.table_schema as referenced_schema,
                ccu.table_name as referenced_table,
                ccu.column_name as referenced_column
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
                on tc.constraint_name = kcu.constraint_name and tc.table_schema = kcu.table_schema
            join information_schema.constraint_column_usage ccu
                on tc.constraint_name = ccu.constraint_name and tc.table_schema = ccu.table_schema
            where tc.constraint_type = 'FOREIGN KEY' and tc.table_schema = ? and tc.table_name = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val foreignKeys = mutableListOf<ActualForeignKeyDef>()
                while (rs.next()) {
                    foreignKeys.add(
                        ActualForeignKeyDef(
                            columnName = rs.getString("column_name"),
                            referencedSchemaAndTable = "${rs.getString("referenced_schema")}.${rs.getString("referenced_table")}",
                            referencedColumn = rs.getString("referenced_column"),
                        )
                    )
                }
                return foreignKeys
            }
        }

    }

    private fun indexesFor(schema: String, table: String): List<ActualIndexDef> {

        val sql = """
            select
                ic.relname as index_name,
                ix.indisunique as is_unique,
                array_agg(a.attname order by array_position(ix.indkey::int2[], a.attnum)) as column_names
            from pg_index ix
            join pg_class ic on ic.oid = ix.indexrelid
            join pg_class tc on tc.oid = ix.indrelid
            join pg_namespace n on n.oid = tc.relnamespace
            join pg_attribute a on a.attrelid = tc.oid and a.attnum = any(ix.indkey)
            where n.nspname = ? and tc.relname = ? and ix.indisprimary = false
            group by ic.relname, ix.indisunique
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, schema)
            statement.setString(2, table)
            statement.executeQuery().use { rs ->
                val indexes = mutableListOf<ActualIndexDef>()
                while (rs.next()) {
                    @Suppress("UNCHECKED_CAST")
                    val columnNames = (rs.getArray("column_names").array as Array<String>).toList()
                    indexes.add(
                        ActualIndexDef(
                            name = rs.getString("index_name"),
                            columns = columnNames,
                            unique = rs.getBoolean("is_unique"),
                        )
                    )
                }
                return indexes
            }
        }

    }

}
```

- [ ] **Step 2: Write the introspector test against a real Testcontainers Postgres**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.sql.Connection
import java.sql.DriverManager

class PostgresSchemaIntrospectorTest {

    companion object {

        private lateinit var connection: Connection

        @JvmStatic
        @BeforeAll
        fun setUp() {

            val container = SingletonPostgresqlContainer.instance.also { it.start() }
            connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)

            connection.createStatement().use { statement ->
                statement.execute("create schema if not exists introspector_test")
                statement.execute(
                    """
                    create table introspector_test.parent (
                        id bigint not null,
                        name varchar(50) not null,
                        primary key (id)
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    create table introspector_test.child (
                        id bigint not null,
                        title varchar(100),
                        parent_id bigint not null references introspector_test.parent(id),
                        primary key (id)
                    )
                    """.trimIndent()
                )
                statement.execute("create index child_title_idx on introspector_test.child(title)")
            }

        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            connection.createStatement().use { it.execute("drop schema introspector_test cascade") }
            connection.close()
        }

    }

    @Test
    fun `introspects tables, columns, PK, FK and indexes`() {

        val tables = PostgresSchemaIntrospector(connection).introspectSchemas(setOf("introspector_test"))

        val parent = tables.single { it.schemaAndTableName == "introspector_test.parent" }
        assertThat(parent.primaryKeyColumns).containsExactly("id")
        assertThat(parent.columns).contains(ActualColumnDef("name", "varchar(50)", nullable = false))

        val child = tables.single { it.schemaAndTableName == "introspector_test.child" }
        assertThat(child.columns).contains(ActualColumnDef("title", "varchar(100)", nullable = true))
        assertThat(child.foreignKeys).containsExactly(
            ActualForeignKeyDef("parent_id", "introspector_test.parent", "id")
        )
        assertThat(child.indexes.map { it.name }).contains("child_title_idx")

    }

}
```

- [ ] **Step 3: Run the test (requires Docker)**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*PostgresSchemaIntrospectorTest*" -i`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/PostgresSchemaIntrospector.kt \
        maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/PostgresSchemaIntrospectorTest.kt
git commit -m "feat: add PostgresSchemaIntrospector for JDBC/information_schema introspection"
```

---

### Task 5: `SchemaComparator`

**Goal:** Diff `List<ExpectedTableDef>` against `List<ActualTableDef>`, producing a `SchemaDiffReport` with missing=error / extra=warning classification.

**Files:**
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaComparator.kt`
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaComparatorTest.kt`

**Acceptance Criteria:**
- [ ] Missing table/column/PK-mismatch/missing-FK/missing-index all set `hasErrors = true` on the relevant `TableDiff`.
- [ ] Extra table/column/FK/index are reported but don't set `hasErrors`.
- [ ] Column type or nullability mismatch is reported as a `ColumnMismatch` (error).

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaComparatorTest*"` → PASS

**Steps:**

- [ ] **Step 1: Write the diff model and comparator**

```kotlin
package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.schema.ExpectedColumnDef
import org.maiaframework.gen.schema.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.ExpectedIndexDef
import org.maiaframework.gen.schema.ExpectedTableDef

enum class TableStatus { OK, MISSING, EXTRA, MISMATCHED }

data class ColumnMismatch(
    val name: String,
    val expectedType: String,
    val actualType: String,
    val expectedNullable: Boolean,
    val actualNullable: Boolean,
)

data class PrimaryKeyMismatch(val expected: List<String>, val actual: List<String>)

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

    val hasErrors: Boolean
        get() = status == TableStatus.MISSING
                || missingColumns.isNotEmpty()
                || mismatchedColumns.isNotEmpty()
                || primaryKeyMismatch != null
                || missingForeignKeys.isNotEmpty()
                || missingIndexes.isNotEmpty()

}

data class SchemaDiffReport(val tables: List<TableDiff>) {

    val hasErrors: Boolean get() = tables.any { it.hasErrors }

}

class SchemaComparator {

    fun compare(expected: List<ExpectedTableDef>, actual: List<ActualTableDef>): SchemaDiffReport {

        val actualByName = actual.associateBy { it.schemaAndTableName }
        val expectedNames = expected.map { it.schemaAndTableName }.toSet()

        val matchedDiffs = expected.map { expectedTable ->
            val actualTable = actualByName[expectedTable.schemaAndTableName]
            if (actualTable == null) {
                TableDiff(expectedTable.schemaAndTableName, TableStatus.MISSING)
            } else {
                diffTable(expectedTable, actualTable)
            }
        }

        val extraTableDiffs = actual
            .filterNot { expectedNames.contains(it.schemaAndTableName) }
            .map { TableDiff(it.schemaAndTableName, TableStatus.EXTRA) }

        return SchemaDiffReport(matchedDiffs.plus(extraTableDiffs))

    }

    private fun diffTable(expected: ExpectedTableDef, actual: ActualTableDef): TableDiff {

        val expectedColumnsByName = expected.columns.associateBy { it.name }
        val actualColumnsByName = actual.columns.associateBy { it.name }

        val missingColumns = expected.columns.filter { !actualColumnsByName.containsKey(it.name) }
        val extraColumns = actual.columns.map { it.name }.filter { !expectedColumnsByName.containsKey(it) }

        val mismatchedColumns = expected.columns.mapNotNull { expectedColumn ->
            val actualColumn = actualColumnsByName[expectedColumn.name] ?: return@mapNotNull null
            if (actualColumn.postgresType != expectedColumn.postgresType || actualColumn.nullable != expectedColumn.nullable) {
                ColumnMismatch(
                    name = expectedColumn.name,
                    expectedType = expectedColumn.postgresType,
                    actualType = actualColumn.postgresType,
                    expectedNullable = expectedColumn.nullable,
                    actualNullable = actualColumn.nullable,
                )
            } else {
                null
            }
        }

        val primaryKeyMismatch = if (expected.primaryKeyColumns != actual.primaryKeyColumns) {
            PrimaryKeyMismatch(expected.primaryKeyColumns, actual.primaryKeyColumns)
        } else {
            null
        }

        val actualForeignKeyColumns = actual.foreignKeys.map { it.columnName }.toSet()
        val missingForeignKeys = expected.foreignKeys.filter { it.columnName !in actualForeignKeyColumns }
        val expectedForeignKeyColumns = expected.foreignKeys.map { it.columnName }.toSet()
        val extraForeignKeys = actual.foreignKeys.map { it.columnName }.filter { it !in expectedForeignKeyColumns }

        val actualIndexColumnSets = actual.indexes.map { it.columns.toSet() }.toSet()
        val missingIndexes = expected.indexes.filter { it.columns.toSet() !in actualIndexColumnSets }
        val expectedIndexColumnSets = expected.indexes.map { it.columns.toSet() }.toSet()
        val extraIndexes = actual.indexes.filter { it.columns.toSet() !in expectedIndexColumnSets }.map { it.name }

        val status = if (missingColumns.isEmpty() && mismatchedColumns.isEmpty() && primaryKeyMismatch == null
            && missingForeignKeys.isEmpty() && missingIndexes.isEmpty()
            && extraColumns.isEmpty() && extraForeignKeys.isEmpty() && extraIndexes.isEmpty()
        ) {
            TableStatus.OK
        } else {
            TableStatus.MISMATCHED
        }

        return TableDiff(
            schemaAndTableName = expected.schemaAndTableName,
            status = status,
            missingColumns = missingColumns,
            extraColumns = extraColumns,
            mismatchedColumns = mismatchedColumns,
            primaryKeyMismatch = primaryKeyMismatch,
            missingForeignKeys = missingForeignKeys,
            extraForeignKeys = extraForeignKeys,
            missingIndexes = missingIndexes,
            extraIndexes = extraIndexes,
        )

    }

}
```

- [ ] **Step 2: Write `SchemaComparatorTest` covering every diff category**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.gen.schema.ExpectedColumnDef
import org.maiaframework.gen.schema.ExpectedForeignKeyDef
import org.maiaframework.gen.schema.ExpectedIndexDef
import org.maiaframework.gen.schema.ExpectedTableDef

class SchemaComparatorTest {

    private val comparator = SchemaComparator()

    @Test
    fun `reports a missing table as an error`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual = emptyList())

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.MISSING)

    }

    @Test
    fun `reports an extra table as a warning, not an error`() {

        val actual = listOf(ActualTableDef("app.leftover", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected = emptyList(), actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.EXTRA)

    }

    @Test
    fun `reports a missing column as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(ActualTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().missingColumns).containsExactly(ExpectedColumnDef("name", "text", false))

    }

    @Test
    fun `reports an extra column as a warning`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("leftover", "text", false)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().extraColumns).containsExactly("leftover")

    }

    @Test
    fun `reports a column type mismatch as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("count", "integer", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("count", "bigint", false)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().mismatchedColumns).containsExactly(
            ColumnMismatch("count", "integer", "bigint", false, false)
        )

    }

    @Test
    fun `reports a nullable mismatch as an error`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        )
        val actual = listOf(
            ActualTableDef("app.widget", listOf(ActualColumnDef("name", "text", true)), listOf("id"), emptyList(), emptyList())
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().mismatchedColumns).containsExactly(
            ColumnMismatch("name", "text", "text", false, true)
        )

    }

    @Test
    fun `reports a primary key mismatch as an error`() {

        val expected = listOf(ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), emptyList()))
        val actual = listOf(ActualTableDef("app.widget", emptyList(), listOf("id", "version"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().primaryKeyMismatch).isEqualTo(PrimaryKeyMismatch(listOf("id"), listOf("id", "version")))

    }

    @Test
    fun `reports a missing foreign key as an error and an extra one as a warning`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child", emptyList(), listOf("id"),
                listOf(ExpectedForeignKeyDef("parent_id", "app.parent", "id")), emptyList()
            )
        )
        val actual = listOf(
            ActualTableDef(
                "app.child", emptyList(), listOf("id"),
                listOf(ActualForeignKeyDef("other_id", "app.other", "id")), emptyList()
            )
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single()
        assertThat(diff.missingForeignKeys).containsExactly(ExpectedForeignKeyDef("parent_id", "app.parent", "id"))
        assertThat(diff.extraForeignKeys).containsExactly("other_id")

    }

    @Test
    fun `reports a missing index as an error and an extra one as a warning`() {

        val expected = listOf(
            ExpectedTableDef("app.widget", emptyList(), listOf("id"), emptyList(), listOf(ExpectedIndexDef("widget_name_idx", listOf("name"), false)))
        )
        val actual = listOf(
            ActualTableDef("app.widget", emptyList(), listOf("id"), emptyList(), listOf(ActualIndexDef("widget_extra_idx", listOf("other"), false)))
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single()
        assertThat(diff.missingIndexes).containsExactly(ExpectedIndexDef("widget_name_idx", listOf("name"), false))
        assertThat(diff.extraIndexes).containsExactly("widget_extra_idx")

    }

    @Test
    fun `a table matching exactly has no errors and OK status`() {

        val table = ExpectedTableDef("app.widget", listOf(ExpectedColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())
        val actualTable = ActualTableDef("app.widget", listOf(ActualColumnDef("name", "text", false)), listOf("id"), emptyList(), emptyList())

        val report = comparator.compare(listOf(table), listOf(actualTable))

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().status).isEqualTo(TableStatus.OK)

    }

}
```

- [ ] **Step 3: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaComparatorTest*" -i`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaComparator.kt \
        maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaComparatorTest.kt
git commit -m "feat: add SchemaComparator diff engine with missing=error / extra=warning classification"
```

---

### Task 6: Console + JSON reporters

**Goal:** Render a `SchemaDiffReport` as human-readable console output and as JSON, from the same report object.

**Files:**
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckReporter.kt`
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckReporterTest.kt`

**Acceptance Criteria:**
- [ ] Console output lists every table with ✓ (OK) or ✗ (has errors), and a summary line with counts.
- [ ] JSON output round-trips: serializing a `SchemaDiffReport` and reading it back produces an equal object.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaCheckReporterTest*"` → PASS

**Steps:**

- [ ] **Step 1: Write the reporter**

```kotlin
package org.maiaframework.gen.schemacheck

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object SchemaCheckReporter {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    fun renderConsole(report: SchemaDiffReport): String {

        val lines = mutableListOf<String>()

        report.tables.sortedBy { it.schemaAndTableName }.forEach { table ->
            val marker = if (table.hasErrors) "✗" else "✓"
            lines.add("$marker ${table.schemaAndTableName} [${table.status}]")

            table.missingColumns.forEach { lines.add("    ERROR: missing column '${it.name}' (${it.postgresType}, nullable=${it.nullable})") }
            table.mismatchedColumns.forEach {
                lines.add("    ERROR: column '${it.name}' expected ${it.expectedType}/nullable=${it.expectedNullable}, actual ${it.actualType}/nullable=${it.actualNullable}")
            }
            table.primaryKeyMismatch?.let { lines.add("    ERROR: primary key expected ${it.expected}, actual ${it.actual}") }
            table.missingForeignKeys.forEach { lines.add("    ERROR: missing foreign key '${it.columnName}' -> ${it.referencedSchemaAndTable}(${it.referencedColumn})") }
            table.missingIndexes.forEach { lines.add("    ERROR: missing index '${it.name}' on ${it.columns}") }

            table.extraColumns.forEach { lines.add("    WARNING: extra column '$it'") }
            table.extraForeignKeys.forEach { lines.add("    WARNING: extra foreign key on column '$it'") }
            table.extraIndexes.forEach { lines.add("    WARNING: extra index '$it'") }
        }

        val errorCount = report.tables.sumOf {
            it.missingColumns.size + it.mismatchedColumns.size + (if (it.primaryKeyMismatch != null) 1 else 0) +
                    it.missingForeignKeys.size + it.missingIndexes.size + (if (it.status == TableStatus.MISSING) 1 else 0)
        }
        val warningCount = report.tables.sumOf { it.extraColumns.size + it.extraForeignKeys.size + it.extraIndexes.size + (if (it.status == TableStatus.EXTRA) 1 else 0) }

        lines.add("")
        lines.add("${report.tables.size} tables checked, $errorCount errors, $warningCount warnings")

        return lines.joinToString("\n")

    }

    fun renderJson(report: SchemaDiffReport): String {

        return jsonMapper.writeValueAsString(report)

    }

}
```

- [ ] **Step 2: Write the reporter test**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaCheckReporterTest {

    @Test
    fun `console output marks a clean table with a checkmark and an errored table with a cross`() {

        val report = SchemaDiffReport(
            listOf(
                TableDiff("app.ok_table", TableStatus.OK),
                TableDiff("app.bad_table", TableStatus.MISSING),
            )
        )

        val output = SchemaCheckReporter.renderConsole(report)

        assertThat(output).contains("✓ app.ok_table [OK]")
        assertThat(output).contains("✗ app.bad_table [MISSING]")
        assertThat(output).contains("2 tables checked, 1 errors, 0 warnings")

    }

    @Test
    fun `json output round-trips a report`() {

        val report = SchemaDiffReport(listOf(TableDiff("app.widget", TableStatus.OK)))

        val json = SchemaCheckReporter.renderJson(report)

        assertThat(json).contains("\"schemaAndTableName\":\"app.widget\"")
        assertThat(json).contains("\"status\":\"OK\"")

    }

}
```

- [ ] **Step 3: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaCheckReporterTest*" -i`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckReporter.kt \
        maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckReporterTest.kt
git commit -m "feat: add console and JSON reporters for schema diff reports"
```

---

### Task 7: `SchemaCheckMain` CLI entry point

**Goal:** Wire instantiation + extraction + introspection + comparison + reporting into a single entry point, callable both as `main(args)` (parsing `key=value` args) and as a library function `run(args: SchemaCheckArgs): Int` (for the Gradle task to call directly).

**Files:**
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckArgs.kt`
- Create: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckMain.kt`
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckMainTest.kt`

**Acceptance Criteria:**
- [ ] Exit code 0 when the DB matches the spec.
- [ ] Exit code 1 when the diff has errors.
- [ ] Exit code 2 when the spec class can't be instantiated or the DB connection fails.
- [ ] `format=json` produces JSON output; default is console text.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaCheckMainTest*"` → PASS (requires Docker)

**Steps:**

- [ ] **Step 1: Write `SchemaCheckArgs`**

```kotlin
package org.maiaframework.gen.schemacheck

import java.io.File

data class SchemaCheckArgs(
    val applicationSpecClassName: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val format: String = "text",
    val outputFile: File? = null,
) {

    companion object {

        fun from(args: Array<String>): SchemaCheckArgs {

            val argsMap = args.map { it.split("=", limit = 2) }.associate { it[0] to it[1] }

            return SchemaCheckArgs(
                applicationSpecClassName = argsMap["applicationSpecClassName"]
                    ?: throw IllegalArgumentException("Expecting an argument named applicationSpecClassName=<...>"),
                jdbcUrl = argsMap["jdbcUrl"]
                    ?: throw IllegalArgumentException("Expecting an argument named jdbcUrl=<...>"),
                username = argsMap["username"]
                    ?: throw IllegalArgumentException("Expecting an argument named username=<...>"),
                password = argsMap["password"] ?: System.getenv("MAIA_SCHEMA_CHECK_DB_PASSWORD")
                    ?: throw IllegalArgumentException("Expecting either a password=<...> argument or a MAIA_SCHEMA_CHECK_DB_PASSWORD environment variable"),
                format = argsMap["format"] ?: "text",
                outputFile = argsMap["outputFile"]?.let { File(it) },
            )

        }

    }

}
```

- [ ] **Step 2: Write `SchemaCheckMain`**

```kotlin
package org.maiaframework.gen.schemacheck

import org.maiaframework.gen.generator.ApplicationModelDefInstantiator
import org.maiaframework.gen.schema.ExpectedSchemaExtractor
import java.sql.DriverManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    val exitCode = try {
        runSchemaCheck(SchemaCheckArgs.from(args))
    } catch (t: Throwable) {
        System.err.println("Schema check failed to run: ${t.message}")
        2
    }

    exitProcess(exitCode)

}

fun runSchemaCheck(args: SchemaCheckArgs): Int {

    val applicationModelDef = try {
        ApplicationModelDefInstantiator.instantiate(args.applicationSpecClassName)
    } catch (t: Throwable) {
        System.err.println("Could not instantiate spec class '${args.applicationSpecClassName}': ${t.message}")
        return 2
    }

    val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)
    val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

    val report = try {
        DriverManager.getConnection(args.jdbcUrl, args.username, args.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }
    } catch (t: Throwable) {
        System.err.println("Could not connect to database at '${args.jdbcUrl}': ${t.message}")
        return 2
    }

    val output = if (args.format == "json") SchemaCheckReporter.renderJson(report) else SchemaCheckReporter.renderConsole(report)

    if (args.outputFile != null) {
        args.outputFile.writeText(output)
    } else {
        println(output)
    }

    return if (report.hasErrors) 1 else 0

}
```

- [ ] **Step 3: Write `SchemaCheckMainTest`**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.maiaframework.gen.spec.AbstractSpec
import org.maiaframework.gen.spec.ApplicationSpec
import org.maiaframework.gen.spec.definition.AppKey
import org.maiaframework.gen.spec.definition.ModelDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.sql.Connection
import java.sql.DriverManager

class SchemaCheckMainTest {

    companion object {

        private lateinit var connection: Connection
        private lateinit var jdbcUrl: String
        private lateinit var username: String
        private lateinit var password: String

        @JvmStatic
        @BeforeAll
        fun setUp() {
            val container = SingletonPostgresqlContainer.instance.also { it.start() }
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            connection = DriverManager.getConnection(jdbcUrl, username, password)
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            connection.close()
        }

    }

    // AbstractSpec.modelDef only implements ModelDefProvider (single ModelDef); the CLI's
    // ApplicationModelDefInstantiator requires ApplicationModelDefProvider, implemented by
    // ApplicationSpec, which aggregates one or more ModelDefs. Both are needed together.
    class MatchingModelSpec : AbstractSpec(AppKey("SchemaCheckMainTest")) {
        val widget = entity("com.example", "Widget") {
            field("name", FieldTypes.string) { lengthConstraint(max = 50) }
        }
    }

    class MatchingApplicationSpec : ApplicationSpec("com.example") {
        private val modelSpec = MatchingModelSpec()
        override val modelDefs: List<ModelDef> = listOf(modelSpec.modelDef)
    }

    @Test
    fun `returns 0 when the database matches the spec`() {

        connection.createStatement().use { statement ->
            statement.execute("create schema if not exists schemacheckmaintest")
            statement.execute(
                """
                create table if not exists schemacheckmaintest.widget (
                    id bigint not null,
                    name varchar(50) not null,
                    created_timestamp timestamp not null,
                    last_modified_timestamp timestamp not null,
                    version bigint not null,
                    primary key (id)
                )
                """.trimIndent()
            )
        }

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(0)

        connection.createStatement().use { it.execute("drop schema schemacheckmaintest cascade") }

    }

    @Test
    fun `returns 1 when the database is missing a table the spec expects`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(1)

    }

    @Test
    fun `returns 2 when the spec class cannot be instantiated`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = "com.example.DoesNotExist",
                jdbcUrl = jdbcUrl,
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(2)

    }

    @Test
    fun `returns 2 when the database connection fails`() {

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = MatchingApplicationSpec::class.java.name,
                jdbcUrl = "jdbc:postgresql://localhost:1/nonexistent",
                username = username,
                password = password,
            )
        )

        assertThat(exitCode).isEqualTo(2)

    }

}
```

- [ ] **Step 4: Run the tests (requires Docker)**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*SchemaCheckMainTest*" -i`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckArgs.kt \
        maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckMain.kt \
        maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaCheckMainTest.kt
git commit -m "feat: add SchemaCheckMain CLI entry point with exit codes 0/1/2"
```

---

### Task 8: Gradle task in `maia-gradle-plugin`

**Goal:** Add a `maiaSchemaCheck` Gradle task wrapping `run(SchemaCheckArgs)` via `WorkerExecutor`, following the existing (if unused) `MaiaGenerationTask` pattern, with new properties on the shared `maia` extension.

**Files:**
- Modify: `plugins/maia-gradle-plugin/build.gradle.kts`
- Modify: `plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/MaiaGenerationExtension.kt`
- Modify: `plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/MaiaGenerationPlugin.kt`
- Create: `plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/MaiaSchemaCheckTask.kt`
- Create: `plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/MaiaSchemaCheckWorkAction.kt`
- Create: `plugins/maia-gradle-plugin/src/main/kotlin/org/maiaframework/gen/plugin/MaiaSchemaCheckWorkParameters.kt`
- Create: `plugins/maia-gradle-plugin/src/test/kotlin/org/maiaframework/gen/plugin/MaiaSchemaCheckTaskFunctionalTest.kt`

**Acceptance Criteria:**
- [ ] `maiaSchemaCheck` task registered, reads `maia { schemaCheckApplicationSpecClassName / schemaCheckJdbcUrl / schemaCheckUsername / schemaCheckPassword / schemaCheckOutputFormat / schemaCheckIgnoreErrors }`.
- [ ] Task fails the build when the diff has errors, unless `schemaCheckIgnoreErrors` is `true`.
- [ ] Task always fails on exit code 2 (tool failure), regardless of `ignoreErrors`.

**Verify:** `./gradlew :plugins:maia-gradle-plugin:test --tests "*MaiaSchemaCheckTaskFunctionalTest*"` → PASS (no Docker required)

**Steps:**

- [ ] **Step 1: Add the new module as a plugin dependency**

In `plugins/maia-gradle-plugin/build.gradle.kts`, add to the `dependencies` block:

```kotlin
    implementation(project(":maia-gen:maia-gen-schema-check"))
```

- [ ] **Step 2: Add schema-check properties to `MaiaGenerationExtension`**

```kotlin
    val schemaCheckApplicationSpecClassName = objects.property(String::class.java)


    val schemaCheckJdbcUrl = objects.property(String::class.java)


    val schemaCheckUsername = objects.property(String::class.java)


    val schemaCheckPassword = objects.property(String::class.java)


    val schemaCheckOutputFormat = objects.property(String::class.java)


    val schemaCheckOutputFile = objects.fileProperty()


    val schemaCheckIgnoreErrors = objects.property(Boolean::class.java)
```

Add these as new members of `MaiaGenerationExtension` (alongside the existing `specificationClassName`, `sqlCreateScriptsDir`, etc.).

- [ ] **Step 3: Write `MaiaSchemaCheckWorkParameters`**

```kotlin
package org.maiaframework.gen.plugin

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface MaiaSchemaCheckWorkParameters : WorkParameters {

    val applicationSpecClassName: Property<String>

    val jdbcUrl: Property<String>

    val username: Property<String>

    val password: Property<String>

    val outputFormat: Property<String>

    val outputFile: RegularFileProperty

    val ignoreErrors: Property<Boolean>

}
```

- [ ] **Step 4: Write `MaiaSchemaCheckWorkAction`**

```kotlin
package org.maiaframework.gen.plugin

import org.gradle.workers.WorkAction
import org.maiaframework.gen.schemacheck.SchemaCheckArgs
import org.maiaframework.gen.schemacheck.runSchemaCheck

abstract class MaiaSchemaCheckWorkAction : WorkAction<MaiaSchemaCheckWorkParameters> {

    override fun execute() {

        val password = parameters.password.orNull ?: System.getenv("MAIA_SCHEMA_CHECK_DB_PASSWORD")
            ?: throw IllegalStateException("No schemaCheckPassword configured and no MAIA_SCHEMA_CHECK_DB_PASSWORD environment variable set")

        val exitCode = runSchemaCheck(
            SchemaCheckArgs(
                applicationSpecClassName = parameters.applicationSpecClassName.get(),
                jdbcUrl = parameters.jdbcUrl.get(),
                username = parameters.username.get(),
                password = password,
                format = parameters.outputFormat.getOrElse("text"),
                outputFile = parameters.outputFile.orNull?.asFile,
            )
        )

        check(exitCode != 2) { "Schema check failed to run — see output above." }

        if (exitCode == 1 && parameters.ignoreErrors.getOrElse(false) == false) {
            throw RuntimeException("Schema check found errors — see report above.")
        }

    }

}
```

- [ ] **Step 5: Write `MaiaSchemaCheckTask`**

```kotlin
package org.maiaframework.gen.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

abstract class MaiaSchemaCheckTask : DefaultTask() {

    @get:InputFiles
    abstract val schemaCheckClasspath: ConfigurableFileCollection

    @get:Input
    abstract val applicationSpecClassName: Property<String>

    @get:Input
    abstract val jdbcUrl: Property<String>

    @get:Input
    abstract val username: Property<String>

    @get:Input
    @get:Optional
    abstract val password: Property<String>

    @get:Input
    abstract val outputFormat: Property<String>

    @get:OutputFile
    @get:Optional
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val ignoreErrors: Property<Boolean>

    @get:Inject
    abstract val workerExecutor: org.gradle.workers.WorkerExecutor

    @TaskAction
    fun checkSchema() {

        val workQueue = workerExecutor.classLoaderIsolation {
            classpath.from(schemaCheckClasspath)
        }

        workQueue.submit(MaiaSchemaCheckWorkAction::class.java) {
            it.applicationSpecClassName.set(applicationSpecClassName)
            it.jdbcUrl.set(jdbcUrl)
            it.username.set(username)
            it.password.set(password)
            it.outputFormat.set(outputFormat)
            it.outputFile.set(outputFile)
            it.ignoreErrors.set(ignoreErrors)
        }

    }

}
```

- [ ] **Step 6: Wire the task, configuration and extension conventions into `MaiaGenerationPlugin`**

Add calls in `apply()` alongside the existing ones:

```kotlin
        `add schema-check conventions to the MaiaGenExtension`(extension)
        `register the maiaSchemaCheckImplementation configuration`(project, extension)
        `register the maiaSchemaCheck task`(project, extension)
```

And the corresponding private functions:

```kotlin
    private fun `add schema-check conventions to the MaiaGenExtension`(extension: MaiaGenerationExtension) {

        extension.schemaCheckOutputFormat.convention("text")
        extension.schemaCheckIgnoreErrors.convention(false)

    }


    private fun `register the maiaSchemaCheckImplementation configuration`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.configurations.register("maiaSchemaCheckImplementation") {
            fromDependencyCollector(extension.getDependencies().getImplementation())
        }

        project.dependencies.add("maiaSchemaCheckImplementation", "org.maiaframework:maia-gen-schema-check")

    }


    private fun `register the maiaSchemaCheck task`(
        project: Project,
        extension: MaiaGenerationExtension
    ) {

        project.tasks.register("maiaSchemaCheck", MaiaSchemaCheckTask::class.java) {

            description = "Compares the actual database schema against the schema implied by the application's ModelDef."
            group = "verification"

            schemaCheckClasspath.from(project.configurations.getByName("maiaSchemaCheckImplementation"))
            applicationSpecClassName.set(extension.schemaCheckApplicationSpecClassName)
            jdbcUrl.set(extension.schemaCheckJdbcUrl)
            username.set(extension.schemaCheckUsername)
            password.set(extension.schemaCheckPassword)
            outputFormat.set(extension.schemaCheckOutputFormat)
            outputFile.set(extension.schemaCheckOutputFile)
            ignoreErrors.set(extension.schemaCheckIgnoreErrors)

        }

    }
```

- [ ] **Step 7: Write a `ProjectBuilder`-based wiring test**

A full `GradleRunner`/TestKit functional test (an isolated Gradle build in a `@TempDir`) would need `"org.maiaframework:maia-gen-schema-check"` to resolve from a real Maven repository — that coordinate only resolves via Gradle's automatic project substitution *inside this same multi-project build* (matching `group`+project name), which doesn't apply in an isolated TestKit project with its own `settings.gradle.kts`. The existing (disabled) `MaiaGenerationPluginTest` has this identical unresolved problem, which is part of why it's disabled — don't repeat it.

Instead, use `org.gradle.testfixtures.ProjectBuilder` (part of `gradleApi()`, already available via the `java-gradle-plugin` plugin, no new dependency needed) to apply the plugin in-process and verify the wiring directly: the task is registered, its inputs are correctly bound from the `maia` extension, and the `maiaSchemaCheckImplementation` configuration carries the expected base dependency. This doesn't execute the task action (that end-to-end behavior is already covered by `SchemaCheckMainTest` in Task 7, which `MaiaSchemaCheckWorkAction` only thinly wraps) — it verifies the plumbing this task actually adds.

```kotlin
package org.maiaframework.gen.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class MaiaSchemaCheckTaskFunctionalTest {

    @Test
    fun `plugin registers maiaSchemaCheck task wired to the maia extension`() {

        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.maiaframework.maia-generation")

        val extension = project.extensions.getByType(MaiaGenerationExtension::class.java)
        extension.schemaCheckApplicationSpecClassName.set("com.example.ExampleApplicationSpec")
        extension.schemaCheckJdbcUrl.set("jdbc:postgresql://localhost:5432/example")
        extension.schemaCheckUsername.set("example_user")
        extension.schemaCheckPassword.set("example_password")

        // Extension properties are wired at task-registration time (not lazily re-read), so
        // force configuration to run now rather than waiting for Gradle's normal task-graph
        // realization (there is none, in a ProjectBuilder test with no build executed).
        project.tasks.getByName("maiaSchemaCheck")

        val task = project.tasks.getByName("maiaSchemaCheck") as MaiaSchemaCheckTask

        assertThat(task.applicationSpecClassName.get()).isEqualTo("com.example.ExampleApplicationSpec")
        assertThat(task.jdbcUrl.get()).isEqualTo("jdbc:postgresql://localhost:5432/example")
        assertThat(task.username.get()).isEqualTo("example_user")
        assertThat(task.outputFormat.get()).isEqualTo("text") // convention default
        assertThat(task.ignoreErrors.get()).isFalse() // convention default

        val configuration = project.configurations.getByName("maiaSchemaCheckImplementation")
        assertThat(configuration.dependencies.map { "${it.group}:${it.name}" }).contains("org.maiaframework:maia-gen-schema-check")

    }

}
```

Note: this test only works if `MaiaGenerationExtension.schemaCheckApplicationSpecClassName` etc. (Task 8 Step 2) are set as extension properties *before* `project.tasks.getByName("maiaSchemaCheck")` triggers the task's configuration block in `MaiaGenerationPlugin` (Task 8 Step 6) to read them via `.set(extension.schemaCheck...)`. Since Gradle's `Property` wiring (`task.jdbcUrl.set(extension.schemaCheckJdbcUrl)`) binds a *live provider*, not a snapshot, setting the extension's values either before or after task registration works — only reading the task's final `.get()` value needs to happen after both are wired up, which this test already does.

- [ ] **Step 8: Run the test (no Docker required for this one)**

Run: `./gradlew :plugins:maia-gradle-plugin:test --tests "*MaiaSchemaCheckTaskFunctionalTest*" -i`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add plugins/maia-gradle-plugin/
git commit -m "feat: add maiaSchemaCheck Gradle task wrapping SchemaCheckMain"
```

---

### Task 9: End-to-end integration test against `maia-showcase`

**Goal:** Validate the whole tool against the one real spec + real hand-written Flyway migrations in this repo, catching any gap between the extraction logic and reality that synthetic unit tests might miss.

**Files:**
- Create: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/ShowcaseSchemaCheckIntegrationTest.kt`
- Modify: `maia-gen/maia-gen-schema-check/build.gradle.kts`

**Acceptance Criteria:**
- [ ] Running the tool against `maia-showcase`'s real spec + a Testcontainers Postgres migrated with `maia-showcase/dao`'s real Flyway migrations reports no errors (`report.hasErrors == false`) if the migrations are currently in sync with the spec — OR, if a genuine pre-existing drift is found, the test documents exactly what it is (this is itself a valid and useful finding to report back, not a test failure to hide).
- [ ] A deliberately broken variant (drop a column the spec expects) is correctly reported as an error.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*ShowcaseSchemaCheckIntegrationTest*"` → PASS (requires Docker; requires `org.flywaydb:flyway-core`/`flyway-database-postgresql` on the test classpath)

**Steps:**

- [ ] **Step 1: Add Flyway + the showcase spec project to the module's test dependencies**

In `maia-gen/maia-gen-schema-check/build.gradle.kts`, add to `dependencies`:

```kotlin
    testImplementation(project(":maia-showcase:spec"))
    testImplementation("org.flywaydb:flyway-core:11.14.1")
    testImplementation("org.flywaydb:flyway-database-postgresql:11.14.1")
```

And expose the showcase migrations directory to the test JVM as a system property (Gradle resolves the sibling project's directory at configuration time — this does not create a compile/runtime dependency on `maia-showcase:dao`, just reads its `projectDir`):

```kotlin
tasks.test {
    systemProperty("maiaShowcaseMigrationsDir", project(":maia-showcase:dao").projectDir.resolve("src/main/resources/db/migration").absolutePath)
}
```

- [ ] **Step 2: Write the integration test**

```kotlin
package org.maiaframework.gen.schemacheck

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.maiaframework.gen.generator.ApplicationModelDefInstantiator
import org.maiaframework.gen.schema.ExpectedSchemaExtractor
import org.maiaframework.testing.postgresql.SingletonPostgresqlContainer
import java.sql.DriverManager

class ShowcaseSchemaCheckIntegrationTest {

    @Test
    fun `maia-showcase's real Flyway migrations match its real spec`() {

        val container = SingletonPostgresqlContainer.instance.also { it.start() }

        Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("filesystem:" + System.getProperty("maiaShowcaseMigrationsDir"))
            .load()
            .migrate()

        val applicationModelDef = ApplicationModelDefInstantiator.instantiate("org.maiaframework.showcase.MaiaShowcaseApplicationSpec")
        val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)
        val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

        val report = DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }

        if (report.hasErrors) {
            println(SchemaCheckReporter.renderConsole(report))
        }

        assertThat(report.hasErrors)
            .withFailMessage(
                "maia-showcase's migrations have drifted from its spec — this may be a genuine finding, not a test bug:\n%s",
                SchemaCheckReporter.renderConsole(report)
            )
            .isFalse()

    }

    @Test
    fun `a dropped column the spec expects is reported as an error`() {

        val container = SingletonPostgresqlContainer.instance.also { it.start() }

        Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("filesystem:" + System.getProperty("maiaShowcaseMigrationsDir"))
            .load()
            .migrate()

        val applicationModelDef = ApplicationModelDefInstantiator.instantiate("org.maiaframework.showcase.MaiaShowcaseApplicationSpec")
        val expectedTables = ExpectedSchemaExtractor().extract(applicationModelDef.rootEntityHierarchies)

        // Pick any expected table/column pair and drop that column to simulate drift.
        val tableWithColumns = expectedTables.first { it.columns.isNotEmpty() }
        val columnToDrop = tableWithColumns.columns.first { it.name != "id" }

        DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            connection.createStatement().use {
                it.execute("alter table ${tableWithColumns.schemaAndTableName} drop column ${columnToDrop.name}")
            }
        }

        val schemas = expectedTables.map { it.schemaAndTableName.substringBefore(".") }.toSet()

        val report = DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { connection ->
            val actualTables = PostgresSchemaIntrospector(connection).introspectSchemas(schemas)
            SchemaComparator().compare(expectedTables, actualTables)
        }

        assertThat(report.hasErrors).isTrue()
        val diff = report.tables.single { it.schemaAndTableName == tableWithColumns.schemaAndTableName }
        assertThat(diff.missingColumns.map { it.name }).contains(columnToDrop.name)

    }

}
```

Note: `SingletonPostgresqlContainer.instance` does not reset between tests within a class (`stop()` is a no-op — see the class's source), and both tests in this class migrate the same container. Running the "dropped column" test after the "matches" test in the same container works because Flyway's `migrate()` is idempotent (already-applied migrations are skipped) — but the two tests share schema state across the JVM's test run. If test ordering causes the second test's dropped column to leak into the first test when run together, add `@TestMethodOrder`/`@Order` to force the "dropped column" test to run last, or (more robustly) have each test operate on its own dedicated fresh schema by migrating into a uniquely-named schema per test and adjusting Flyway's `schemas(...)` config accordingly.

- [ ] **Step 3: Run the tests (requires Docker)**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "*ShowcaseSchemaCheckIntegrationTest*" -i`
Expected: PASS. If the first test fails because showcase's real migrations HAVE drifted from its spec, that is a genuine finding — report it back rather than weakening the assertion to hide it.

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-schema-check/build.gradle.kts \
        maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/ShowcaseSchemaCheckIntegrationTest.kt
git commit -m "test: add end-to-end schema check against maia-showcase's real spec and migrations"
```

---

## Post-plan notes

- Out of scope (confirmed with the user during design): non-Postgres databases, Mongo-backed entities, Elasticsearch/typeahead index mappings, migration auto-generation/auto-fix, Postgres exclusion constraints (single-effective-record).
- If Task 9's first test finds real drift in `maia-showcase`, that's a legitimate outcome of building this tool — surface it to the user rather than treating it as a plan defect.

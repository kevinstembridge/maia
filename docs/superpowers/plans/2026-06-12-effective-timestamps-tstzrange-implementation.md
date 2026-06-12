# Effective timestamps tstzrange Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Switch DDL/DAO generation for entities with `withEffectiveTimestamps()` from two `effective_from`/`effective_to` columns to one `effective_range tstzrange` column, without changing the Kotlin entity API.

**Architecture:** A new `EffectiveTimestampRendererHelper` object provides shared collapse/select-clause logic. `CreateTableSqlRenderer` emits the single range column. `JdbcDaoRenderer` collapses the column pair for INSERT/bulkInsert/setFields and appends `lower()/upper()` projections + `effective_range @> current_timestamp` for SELECTs. `RowMapperRenderer`'s many-to-many join-fetch query is updated similarly. Spec: `docs/superpowers/specs/2026-06-12-effective-timestamps-tstzrange-design.md`.

**Tech Stack:** Kotlin code generator (maia-gen-generator, maia-gen-spec), Gradle, Postgres `tstzrange`.

**User Verification:** NO — pure generator change, user reconciles Flyway migration manually (out of scope).

---

## Task 1: ClassFieldName constants + EffectiveTimestampRendererHelper

**Goal:** Add `effectiveFrom`/`effectiveTo` `ClassFieldName` constants and a shared helper object for collapsing the column pair.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldName.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EffectiveTimestampRendererHelper.kt`

**Acceptance Criteria:**
- [ ] `ClassFieldName.effectiveFrom` and `ClassFieldName.effectiveTo` constants exist
- [ ] `EffectiveTimestampRendererHelper` provides `usesEffectiveRange`, `collapseEffectiveColumns`, `collapseEffectiveValuePlaceholders`, `selectStarClause`, `EFFECTIVE_RANGE_WHERE_CLAUSE`

**Verify:** `./gradlew :maia:maia-gen:maia-gen-spec:compileKotlin :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add ClassFieldName constants**

In `ClassFieldName.kt`, insert after `createdTimestampUtc` (alphabetical order):

```kotlin
        val createdTimestampUtc = ClassFieldName("createdTimestampUtc")

        val effectiveFrom = ClassFieldName("effectiveFrom")

        val effectiveTo = ClassFieldName("effectiveTo")

        val id = ClassFieldName("id")
```

- [ ] **Step 2: Create EffectiveTimestampRendererHelper**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityDef

object EffectiveTimestampRendererHelper {

    private const val EFFECTIVE_FROM_COLUMN = "effective_from"
    private const val EFFECTIVE_TO_COLUMN = "effective_to"
    private const val EFFECTIVE_RANGE_COLUMN = "effective_range"

    const val EFFECTIVE_RANGE_WHERE_CLAUSE = "effective_range @> current_timestamp"


    fun usesEffectiveRange(entityDef: EntityDef): Boolean = entityDef.hasEffectiveTimestamps.value


    /**
     * Replaces "effective_from" with "effective_range" in place and drops "effective_to".
     */
    fun collapseEffectiveColumns(entityDef: EntityDef, columnNames: List<String>): List<String> {

        if (!usesEffectiveRange(entityDef)) {
            return columnNames
        }

        return columnNames
            .map { if (it == EFFECTIVE_FROM_COLUMN) EFFECTIVE_RANGE_COLUMN else it }
            .filterNot { it == EFFECTIVE_TO_COLUMN }

    }


    /**
     * Replaces ":effectiveFrom" with "tstzrange(:effectiveFrom, :effectiveTo)" in place and drops ":effectiveTo".
     */
    fun collapseEffectiveValuePlaceholders(entityDef: EntityDef, placeholders: List<String>): List<String> {

        if (!usesEffectiveRange(entityDef)) {
            return placeholders
        }

        return placeholders
            .map { if (it == ":effectiveFrom") "tstzrange(:effectiveFrom, :effectiveTo)" else it }
            .filterNot { it == ":effectiveTo" }

    }


    /**
     * "select *" plus the effective_range -> effectiveFrom/effectiveTo projection, for entities
     * with hasEffectiveTimestamps. Entities without it (incl. hasEffectiveLocalDates) get plain "select *".
     */
    fun selectStarClause(entityDef: EntityDef): String =
        if (usesEffectiveRange(entityDef)) {
            "select *, lower(effective_range) as effective_from, upper(effective_range) as effective_to"
        } else {
            "select *"
        }


}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-spec:compileKotlin :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldName.kt maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EffectiveTimestampRendererHelper.kt
git commit -m "feat: add EffectiveTimestampRendererHelper and ClassFieldName constants for effective_range migration"
```

---

## Task 2: CreateTableSqlRenderer — emit effective_range column

**Goal:** For entities with `hasEffectiveTimestamps`, emit `effective_range tstzrange not null default tstzrange(now(), null)` instead of the `effective_from`/`effective_to` columns.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt:68-90`

**Acceptance Criteria:**
- [ ] Entities with `hasEffectiveTimestamps = true` get one `effective_range tstzrange not null default tstzrange(now(), null)` line, no `effective_from`/`effective_to` lines
- [ ] Entities without `hasEffectiveTimestamps` (incl. `hasEffectiveLocalDates`) are unaffected

**Verify:** `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full regeneration verified in Task 7)

**Steps:**

- [ ] **Step 1: Edit `render CREATE TABLE statement for`**

Replace (lines 68-90):

```kotlin
        val nonDerivedSqlFields: List<SqlFieldDef> = entityHierarchy.allFieldDefsSorted
            .filterNot { it.isDerived.value }
            .groupBy { it.tableColumnName}
            .mapValues { toSqlFieldDef(it, entityHierarchy) }
            .values
            .toList()

        val lines = nonDerivedSqlFields.map { sqlFieldDef ->

            val fieldType = sqlFieldDef.fieldType

            val nullSuffix = if (sqlFieldDef.nullable) "NULL" else "NOT NULL"
            val postgresDataType = FieldTypeRendererHelper.determineSqlDataType(fieldType)
            val sizeConstraint = sqlFieldDef.sizeConstraint
            val foreignKey = if (fieldType is ForeignKeyFieldType) { " REFERENCES ${schemaAndTableNameFor(fieldType.foreignKeyFieldDef.foreignEntityDef)}(id)" } else ""

            "${sqlFieldDef.tableColumnName} $postgresDataType$sizeConstraint $nullSuffix$foreignKey"

        }

        val allLines = listOfNotNull(typeDiscriminatorLineOrNull).plus(lines)
        allLines.forEach { appendLine("    $it,") }
```

with:

```kotlin
        val nonDerivedSqlFields: List<SqlFieldDef> = entityHierarchy.allFieldDefsSorted
            .filterNot { it.isDerived.value }
            .groupBy { it.tableColumnName}
            .mapValues { toSqlFieldDef(it, entityHierarchy) }
            .values
            .toList()

        val effectiveTimestampColumnNames = setOf("effective_from", "effective_to")

        val sqlFieldsForColumns = if (baseEntityDef.hasEffectiveTimestamps.value) {
            nonDerivedSqlFields.filterNot { it.tableColumnName.value in effectiveTimestampColumnNames }
        } else {
            nonDerivedSqlFields
        }

        val lines = sqlFieldsForColumns.map { sqlFieldDef ->

            val fieldType = sqlFieldDef.fieldType

            val nullSuffix = if (sqlFieldDef.nullable) "NULL" else "NOT NULL"
            val postgresDataType = FieldTypeRendererHelper.determineSqlDataType(fieldType)
            val sizeConstraint = sqlFieldDef.sizeConstraint
            val foreignKey = if (fieldType is ForeignKeyFieldType) { " REFERENCES ${schemaAndTableNameFor(fieldType.foreignKeyFieldDef.foreignEntityDef)}(id)" } else ""

            "${sqlFieldDef.tableColumnName} $postgresDataType$sizeConstraint $nullSuffix$foreignKey"

        }

        val effectiveRangeLineOrNull = if (baseEntityDef.hasEffectiveTimestamps.value) {
            "effective_range tstzrange not null default tstzrange(now(), null)"
        } else {
            null
        }

        val allLines = listOfNotNull(typeDiscriminatorLineOrNull).plus(lines).plus(listOfNotNull(effectiveRangeLineOrNull))
        allLines.forEach { appendLine("    $it,") }
```

Note: `primaryKeyFieldCsv` below (line ~91) still uses `nonDerivedSqlFields` (unchanged) — `effective_from`/`effective_to` are never primary key fields, so this is safe.

- [ ] **Step 2: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt
git commit -m "feat: emit effective_range tstzrange column for entities with effective timestamps"
```

---

## Task 3: JdbcDaoRenderer — SELECT projections and findEffective WHERE clauses

**Goal:** Update all `select * from ...` sites for entities with `hasEffectiveTimestamps` to append `lower(effective_range) as effective_from, upper(effective_range) as effective_to`, and switch `findAllEffective`/`findEffectiveBy*` WHERE clauses to `effective_range @> current_timestamp`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt`

**Acceptance Criteria:**
- [ ] All 10 listed `select * from ...` sites use `EffectiveTimestampRendererHelper.selectStarClause(entityDef)`
- [ ] `findAllEffective` and `findEffectiveBy for fields` use `effective_range @> current_timestamp` for `hasEffectiveTimestamps` entities, unchanged two-condition clause for `hasEffectiveLocalDates`-only entities

**Verify:** `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full regeneration verified in Task 7)

**Steps:**

- [ ] **Step 1: `findByPrimaryKeyOrNull` (~line 730)**

Replace:
```kotlin
        appendLine("            \"select * from ${this.entityDef.schemaAndTableName} where $whereClauseFields\",")
```
with:
```kotlin
        appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${this.entityDef.schemaAndTableName} where $whereClauseFields\",")
```

- [ ] **Step 2: `findAll` (~line 808)**

Replace:
```kotlin
        appendLine("            \"select * from ${entityDef.schemaAndTableName}\",")
```
with:
```kotlin
        appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName}\",")
```

- [ ] **Step 3: `findAllEffective` (~lines 826-839)**

Replace:
```kotlin
        appendLine("        return this.jdbcOps.queryForList(\"\"\"")
        appendLine("            select * from ${entityDef.schemaAndTableName}")
        appendLine("            where effective_from <= current_timestamp")
        appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams(),")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
```
with:
```kotlin
        appendLine("        return this.jdbcOps.queryForList(\"\"\"")

        if (this.entityDef.hasEffectiveTimestamps.value) {
            appendLine("            ${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName}")
            appendLine("            where ${EffectiveTimestampRendererHelper.EFFECTIVE_RANGE_WHERE_CLAUSE}")
        } else {
            appendLine("            select * from ${entityDef.schemaAndTableName}")
            appendLine("            where effective_from <= current_timestamp")
            appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        }

        appendLine("            \"\"\".trimIndent(),")
        appendLine("            SqlParams(),")
        appendLine("            this.entityRowMapper")
        appendLine("        )")
```

(The `else` branch covers `hasEffectiveLocalDates`-only entities, which this function is also reachable for — see the guard at the top of this function. Out of scope for this change, behavior preserved.)

- [ ] **Step 4: `findAllByFilter` (~line 854)**

Replace:
```kotlin
        appendLine("            \"select * from ${entityDef.schemaAndTableName} where \$whereClause\",")
```
with:
```kotlin
        appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName} where \$whereClause\",")
```

- [ ] **Step 5: `findAllByFilterAsSequence` (~line 880)**

Same replacement as Step 4 (identical line text, different function).

- [ ] **Step 6: `findAllByFilterAndPageRequest` (~line 941)**

Replace:
```kotlin
        appendLine("            \"select * from ${entityDef.schemaAndTableName} where \$whereClause \$orderByClause \$limitClause \$offsetClause\",")
```
with:
```kotlin
        appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName} where \$whereClause \$orderByClause \$limitClause \$offsetClause\",")
```

- [ ] **Step 7: `findAllAsSequence` (~line 980)**

Replace:
```kotlin
        appendLine("            \"select * from ${entityDef.schemaAndTableName};\",")
```
with:
```kotlin
        appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName};\",")
```

- [ ] **Step 8: `findOneOrNullByForFields` (~line 1329)**

Replace:
```kotlin
        appendLine("            select * from ${entityDef.schemaAndTableName}")
```
with:
```kotlin
        appendLine("            ${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName}")
```

- [ ] **Step 9: `findBy for fields` (~line 1407)**

Replace:
```kotlin
        appendLine("            select * from ${this.entityDef.schemaAndTableName}")
```
with:
```kotlin
        appendLine("            ${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${this.entityDef.schemaAndTableName}")
```

- [ ] **Step 10: `findEffectiveBy for fields` (~lines 1465-1470)**

Replace:
```kotlin
        appendLine("            select * from ${this.entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)
        appendLine("            and effective_from <= current_timestamp")
        appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        appendLine("            \"\"\".trimIndent(),")
```
with:
```kotlin
        appendLine("            ${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${this.entityDef.schemaAndTableName}")

        appendWhereOrAndClauses(entityFieldDefs)

        if (this.entityDef.hasEffectiveTimestamps.value) {
            appendLine("            and ${EffectiveTimestampRendererHelper.EFFECTIVE_RANGE_WHERE_CLAUSE}")
        } else {
            appendLine("            and effective_from <= current_timestamp")
            appendLine("            and (effective_to > current_timestamp or effective_to is null)")
        }

        appendLine("            \"\"\".trimIndent(),")
```

(This function is only called for entities with `hasEffectiveTimestamps || hasEffectiveLocalDates` — see `render finders for indexes` guard. The `else` branch covers the local-dates case, behavior preserved.)

- [ ] **Step 11: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 12: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt
git commit -m "feat: project effective_range as effective_from/effective_to in DAO SELECT queries"
```

---

## Task 4: JdbcDaoRenderer — INSERT/bulkInsert column collapse

**Goal:** Collapse the `effective_from`/`effective_to` column pair into a single `effective_range` column with value `tstzrange(:effectiveFrom, :effectiveTo)` in `insert`/`insertSubclass`/`bulkInsert`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt`

**Acceptance Criteria:**
- [ ] `renderFunctionInsertFor` and `render function bulkInsert` emit `effective_range` column with `tstzrange(:effectiveFrom, :effectiveTo)` value, for entities with `hasEffectiveTimestamps`
- [ ] `SqlParams` still binds `effectiveFrom`/`effectiveTo` as two separate `Instant?` params (unchanged, via existing `renderSqlParamsForEntity`)

**Verify:** `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full regeneration verified in Task 7)

**Steps:**

- [ ] **Step 1: `databaseColumnNames()` extension (~line 2191-2193)**

Replace:
```kotlin
    private fun EntityDef.databaseColumnNames(): List<String> = allEntityFieldsSorted
        .filterNot { it.isDerived.value }
        .map { it.tableColumnName.value }
```
with:
```kotlin
    private fun EntityDef.databaseColumnNames(): List<String> = EffectiveTimestampRendererHelper.collapseEffectiveColumns(
        this,
        allEntityFieldsSorted
            .filterNot { it.isDerived.value }
            .map { it.tableColumnName.value }
    )
```

- [ ] **Step 2: `renderFunctionInsertFor` value placeholders (~line 335)**

Replace:
```kotlin
        val fieldNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
```
with:
```kotlin
        val fieldNames = EffectiveTimestampRendererHelper.collapseEffectiveValuePlaceholders(
            entityDef,
            entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
        )
```

- [ ] **Step 3: `render function bulkInsert` column names (~line 367)**

Replace:
```kotlin
        val databaseColumnNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { it.tableColumnName.value }
        renderStrings(databaseColumnNames, indent = 16)
```
with:
```kotlin
        renderStrings(entityDef.databaseColumnNames(), indent = 16)
```

- [ ] **Step 4: `render function bulkInsert` value placeholders (~line 382)**

Replace:
```kotlin
        val fieldNames = entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
```
with:
```kotlin
        val fieldNames = EffectiveTimestampRendererHelper.collapseEffectiveValuePlaceholders(
            entityDef,
            entityDef.allEntityFieldsSorted.filterNot { it.isDerived.value }.map { ":${it.classFieldName.value}" }
        )
```

- [ ] **Step 5: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt
git commit -m "feat: collapse effectiveFrom/effectiveTo into effective_range in INSERT/bulkInsert"
```

---

## Task 5: JdbcDaoRenderer — setFields/addField collapse

**Goal:** For entities with `hasEffectiveTimestamps`, generate a `setFields` that collapses any `effectiveFrom`/`effectiveTo` `FieldUpdate`s into a single `effective_range = tstzrange(...)` assignment, handling all three cases (both ends, from only, to only).

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt:1904-2075` (`render function setFields`)

**Acceptance Criteria:**
- [ ] For `hasEffectiveTimestamps` entities, generated `setFields` excludes `effectiveFrom`/`effectiveTo` from the generic per-field `map`, and instead appends one of:
  - `effective_range = tstzrange(:effectiveFrom, :effectiveTo)` when both are set
  - `effective_range = tstzrange(:effectiveFrom, upper(effective_range))` when only `effectiveFrom` is set
  - `effective_range = tstzrange(lower(effective_range), :effectiveTo)` when only `effectiveTo` is set
  - nothing when neither is set
- [ ] Generated `addField`'s `when` block no longer has `"effectiveFrom"`/`"effectiveTo"` cases for `hasEffectiveTimestamps` entities (handled above instead)
- [ ] Entities without `hasEffectiveTimestamps` get unchanged `setFields`/`addField` output

**Verify:** `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full regeneration + generated-code compile verified in Task 7)

**Steps:**

- [ ] **Step 1: Branch the `fieldClauses` generation**

In `render function setFields`, replace this block:

```kotlin
        appendLine("        val fieldClauses = updater.fields")

        if (entityDef.versioned.value) {
            appendLine("            .plus(FieldUpdate(\"version_incremented\", \"version\", updater.version + 1))")
        }

        appendLine("            .map { field ->")
        blankLine()
        appendLine("                addField(field, sqlParams)")
        appendLine($$"                \"${field.dbColumnName} = :${field.classFieldName}\"")
        blankLine()
        appendLine("            }.joinToString(\", \")")
```

with:

```kotlin
        if (entityDef.hasEffectiveTimestamps.value) {

            appendLine("        val effectiveFromUpdate = updater.fields.find { it.classFieldName == \"effectiveFrom\" }")
            appendLine("        val effectiveToUpdate = updater.fields.find { it.classFieldName == \"effectiveTo\" }")
            blankLine()
            appendLine("        val fieldClauses = updater.fields")
            appendLine("            .filterNot { it.classFieldName == \"effectiveFrom\" || it.classFieldName == \"effectiveTo\" }")

            if (entityDef.versioned.value) {
                appendLine("            .plus(FieldUpdate(\"version_incremented\", \"version\", updater.version + 1))")
            }

            appendLine("            .map { field ->")
            blankLine()
            appendLine("                addField(field, sqlParams)")
            appendLine($$"                \"${field.dbColumnName} = :${field.classFieldName}\"")
            blankLine()
            appendLine("            }")
            appendLine("            .plus(")
            appendLine("                when {")
            appendLine("                    effectiveFromUpdate != null && effectiveToUpdate != null -> {")
            appendLine("                        sqlParams.addValue(\"effectiveFrom\", effectiveFromUpdate.value as Instant?)")
            appendLine("                        sqlParams.addValue(\"effectiveTo\", effectiveToUpdate.value as Instant?)")
            appendLine("                        listOf(\"effective_range = tstzrange(:effectiveFrom, :effectiveTo)\")")
            appendLine("                    }")
            appendLine("                    effectiveFromUpdate != null -> {")
            appendLine("                        sqlParams.addValue(\"effectiveFrom\", effectiveFromUpdate.value as Instant?)")
            appendLine("                        listOf(\"effective_range = tstzrange(:effectiveFrom, upper(effective_range))\")")
            appendLine("                    }")
            appendLine("                    effectiveToUpdate != null -> {")
            appendLine("                        sqlParams.addValue(\"effectiveTo\", effectiveToUpdate.value as Instant?)")
            appendLine("                        listOf(\"effective_range = tstzrange(lower(effective_range), :effectiveTo)\")")
            appendLine("                    }")
            appendLine("                    else -> emptyList()")
            appendLine("                }")
            appendLine("            )")
            appendLine("            .joinToString(\", \")")

        } else {

            appendLine("        val fieldClauses = updater.fields")

            if (entityDef.versioned.value) {
                appendLine("            .plus(FieldUpdate(\"version_incremented\", \"version\", updater.version + 1))")
            }

            appendLine("            .map { field ->")
            blankLine()
            appendLine("                addField(field, sqlParams)")
            appendLine($$"                \"${field.dbColumnName} = :${field.classFieldName}\"")
            blankLine()
            appendLine("            }.joinToString(\", \")")

        }
```

- [ ] **Step 2: Exclude effectiveFrom/effectiveTo from `addField`'s `when` block**

Replace:
```kotlin
        this.entityDef.allEntityFieldsSorted
            .filter { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value }
            .forEach { entityFieldDef ->
```
with:
```kotlin
        this.entityDef.allEntityFieldsSorted
            .filter { it.classFieldDef.isModifiableBySystem || it.classFieldDef.isEditableByUser.value }
            .filterNot {
                entityDef.hasEffectiveTimestamps.value
                        && (it.classFieldName == ClassFieldName.effectiveFrom || it.classFieldName == ClassFieldName.effectiveTo)
            }
            .forEach { entityFieldDef ->
```

- [ ] **Step 3: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt
git commit -m "feat: collapse effectiveFrom/effectiveTo FieldUpdates into effective_range in setFields"
```

---

## Task 6: RowMapperRenderer — many-to-many join-fetch query

**Goal:** Update the join-fetch query generated for many-to-many entities with effective timestamps to read `effective_from`/`effective_to` via `lower(mtm.effective_range)`/`upper(mtm.effective_range)`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt:196-220`

**Acceptance Criteria:**
- [ ] Generated SQL selects `lower(mtm.effective_range) as effective_from, upper(mtm.effective_range) as effective_to` instead of `mtm.effective_from, mtm.effective_to`
- [ ] `rsa.readInstantOrNull("effective_from"/"effective_to")` calls unchanged

**Verify:** `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL (full regeneration verified in Task 7)

**Steps:**

- [ ] **Step 1: Edit the join-fetch query template**

In `render functions for manyToManyPkAndNameDtos`, replace:

```kotlin
                    |            select
                    |                other.id,
                    |                other.${joinFetchDtoDef.nameTableColumnName},
                    |                mtm.effective_from,
                    |                mtm.effective_to
                    |            from ${joinFetchDtoDef.otherSideEntitySchemaAndTableName} other
```

with:

```kotlin
                    |            select
                    |                other.id,
                    |                other.${joinFetchDtoDef.nameTableColumnName},
                    |                lower(mtm.effective_range) as effective_from,
                    |                upper(mtm.effective_range) as effective_to
                    |            from ${joinFetchDtoDef.otherSideEntitySchemaAndTableName} other
```

(The `effectiveFrom = rsa.readInstantOrNull("effective_from")` / `effectiveTo = rsa.readInstantOrNull("effective_to")` lines further down are unchanged — they read by result-set alias, which is preserved.)

- [ ] **Step 2: Compile**

Run: `./gradlew :maia:maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt
git commit -m "feat: read effective_from/effective_to from effective_range in many-to-many join-fetch query"
```

---

## Task 7: Regenerate maia-showcase and verify

**Goal:** Regenerate `maia-showcase` generated sources, confirm the new DDL/DAO/row-mapper output is correct for `EffectiveTimestampEntity` (single effective record) and `LeftToRightManyToManyJoin` (multiple effective records), and confirm generated Kotlin compiles.

**Files:**
- Generated (inspect, do not hand-edit):
  - `maia-showcase/dao/src/generated/sql/create_entity_tables_maia.sql`
  - `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/effective_dated/EffectiveTimestampDao.kt`
  - `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/effective_dated/EffectiveTimestampEntityRowMapper.kt`
  - `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftToRightManyToManyJoinDao.kt`
  - `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftToRightManyToManyJoinEntityRowMapper.kt`

**Acceptance Criteria:**
- [ ] `create_entity_tables_maia.sql` has `effective_range tstzrange not null default tstzrange(now(), null)` for `effective_timestamp` and `left_to_right_many_to_many_join` tables, no `effective_from`/`effective_to` columns
- [ ] `EffectiveTimestampDao.kt` and `LeftToRightManyToManyJoinDao.kt`: insert/bulkInsert use `effective_range` column with `tstzrange(:effectiveFrom, :effectiveTo)`; finders use `lower(effective_range) as effective_from, upper(effective_range) as effective_to`; `findAllEffective`/`findEffectiveBy*` use `effective_range @> current_timestamp`; `setFields`/`addField` collapse as designed
- [ ] `LeftToRightManyToManyJoinEntityRowMapper.kt` join-fetch query uses `lower(mtm.effective_range)`/`upper(mtm.effective_range)`
- [ ] `./gradlew :maia-showcase:dao:compileKotlin :maia-showcase:domain:compileKotlin :maia-showcase:repo:compileKotlin :maia-showcase:service:compileKotlin` succeeds

**Verify:** Commands in Steps 1-3 below.

**Steps:**

- [ ] **Step 1: Regenerate**

Run:
```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration :maia-showcase:service:maiaGeneration
```
Expected: BUILD SUCCESSFUL, generated sources rewritten under `src/generated/`.

- [ ] **Step 2: Inspect diffs**

Run: `git diff --stat -- maia-showcase/*/src/generated`

Confirm only `EffectiveTimestamp*` and `*ManyToMany*` generated files (plus `create_entity_tables_maia.sql`) changed. Spot-check against the Acceptance Criteria above. `domain`/`repo`/`service` layers should show no diff (entity/updater/builder classes are unchanged by design).

- [ ] **Step 3: Compile generated code**

Run:
```bash
./gradlew :maia-showcase:dao:compileKotlin :maia-showcase:domain:compileKotlin :maia-showcase:repo:compileKotlin :maia-showcase:service:compileKotlin
```
Expected: BUILD SUCCESSFUL.

Note: integration tests that hit Postgres will fail until the Flyway migration is reconciled with the new DDL — that reconciliation is the user's responsibility (out of scope), so do not run `:maia-showcase:dao:test` here.

- [ ] **Step 4: Commit generated output**

```bash
git add maia-showcase
git commit -m "chore: regenerate maia-showcase for effective_range tstzrange migration"
```

---

## Self-Review Notes

1. **Spec coverage:** DDL (Task 2), select projections + findEffective WHERE (Task 3), insert/bulkInsert (Task 4), setFields (Task 5), many-to-many join-fetch (Task 6), regeneration/verification (Task 7) — all spec sections covered. GiST exclusions and `withEffectiveLocalDates` explicitly out of scope and untouched.
2. **Placeholder scan:** none — all steps have complete code.
3. **Type consistency:** `EffectiveTimestampRendererHelper` method names/signatures used identically across Tasks 2-6 (`usesEffectiveRange`, `collapseEffectiveColumns`, `collapseEffectiveValuePlaceholders`, `selectStarClause`, `EFFECTIVE_RANGE_WHERE_CLAUSE`).
4. **User verification scan:** original request is a pure generator change; user explicitly said they'll reconcile the Flyway migration themselves and the generator just needs to produce correct DDL. No human-in-the-loop requirement → `requiresUserVerification: false` for all tasks.

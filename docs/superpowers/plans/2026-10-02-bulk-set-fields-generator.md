# Bulk setFields Generator Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `bulkSetFields` method to the Maia code generator's `JdbcDaoRenderer`/`EntityRepoRenderer`, mirroring the existing `bulkInsert` JDBC-batch pattern, so bulk field updates no longer require one DB round trip per row.

**Architecture:** Group incoming `EntityUpdater`s by the set of fields they set (same field-set → same generated SQL text → one real JDBC batch via `jdbcOps.batchUpdate`). Reuse the existing `addField` (per-field param binding) and `bulkInsertHistory` (hierarchy-aware history bulk-insert) unchanged. Add one new DAO helper (`findAllByPrimaryKeys`) and one new exception (`BulkOptimisticLockingException`, collecting every failed row rather than failing fast). Full design rationale: `docs/superpowers/specs/2026-10-02-bulk-set-fields-design.md`.

**Tech Stack:** Kotlin code generator (`maia-gen-generator`), Spring `NamedParameterJdbcOperations` batch JDBC, Postgres via Testcontainers (`maia-showcase` end-to-end tests).

**User Verification:** NO — no human sign-off checkpoint required; this is a spike on `feature/bulk-set-fields-generator`, not merged to `main`.

**Repo:** All paths below are relative to `/home/kevin/dev/code/maia`, on branch `feature/bulk-set-fields-generator` (already created and checked out).

**Note on native task tools:** This session's harness has no `TaskCreate`/`TaskList` tools available, so this document's checkboxes are the sole source of truth for progress — there is no `.tasks.json` mirror to keep in sync.

---

## Before you start

There is no existing convention in this codebase for unit-testing a renderer's string output directly (no `JdbcDaoRendererTest`/`EntityRepoRendererTest` exists for comparable functions like `bulkInsert`). Verification instead happens end-to-end: edit the renderer → regenerate `maia-showcase`'s generated code → compile → run a real Testcontainers-backed integration test in `maia-showcase/app/src/test`. Every task below follows that loop. Expect to iterate on string-escaping inside the renderer's `appendLine(...)` calls — hand-writing nested Kotlin string templates is fiddly; treat the code blocks below as a strong starting point, not gospel, and fix based on actual compiler errors from the regenerated file.

Regeneration command (run after every renderer change, before compiling/testing showcase):
```bash
./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration
```

Fast renderer-only compile check (run before regenerating, to catch renderer syntax errors cheaply):
```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```

Test command shape (swap the `--tests` class per task):
```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"
```

---

### Task 1: Add `BulkOptimisticLockingException`

**Goal:** A new exception type that carries every failed-version-check row from a batch, not just the first.

**Files:**
- Create: `libs/maia-jdbc/src/main/kotlin/org/maiaframework/jdbc/BulkOptimisticLockingException.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt:107-108` (insert a new line between the existing `MAIA_JDBC_AND_OR` and `MAIA_JDBC_DB_COLUMN` entries)

**Acceptance Criteria:**
- [ ] `BulkOptimisticLockingException` compiles in `libs/maia-jdbc`
- [ ] `Fqcns.MAIA_JDBC_BULK_OPTIMISTIC_LOCKING_EXCEPTION` resolves to `org.maiaframework.jdbc.BulkOptimisticLockingException`

**Verify:** `./gradlew :libs:maia-jdbc:compileKotlin :maia-gen:maia-gen-spec:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create the exception class**, mirroring the existing `OptimisticLockingException.kt` in the same package exactly (same package = no imports needed for `TableName`/`MaiaDataAccessException`):

```kotlin
package org.maiaframework.jdbc

class BulkOptimisticLockingException(
    tableName: TableName,
    failures: List<Pair<Any, Long>>
) : MaiaDataAccessException(
    "OPTIMISTIC_LOCKING: table=$tableName, failures=$failures"
)
```

- [ ] **Step 2: Add the Fqcns constant.** In `Fqcns.kt`, between line 107 (`MAIA_JDBC_AND_OR`) and line 108 (`MAIA_JDBC_DB_COLUMN`), insert:

```kotlin
    val MAIA_JDBC_BULK_OPTIMISTIC_LOCKING_EXCEPTION = Fqcn.valueOf("org.maiaframework.jdbc.BulkOptimisticLockingException")
```

- [ ] **Step 3: Compile and verify**

Run: `./gradlew :libs:maia-jdbc:compileKotlin :maia-gen:maia-gen-spec:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add libs/maia-jdbc/src/main/kotlin/org/maiaframework/jdbc/BulkOptimisticLockingException.kt maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt
git commit -m "Add BulkOptimisticLockingException for aggregate bulk-update lock failures

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 2: Add `findAllByPrimaryKeys` DAO helper

**Goal:** A generated DAO method to bulk re-fetch rows by primary key after a batch update, needed by `bulkSetFields`'s history step (Task 3+).

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt` — insert a new call `` `render the findAllByPrimaryKeys function`() `` in the `renderFunctions()` dispatch, immediately after the existing call to `` `render the findByPrimaryKeyOrNull function`() `` (around line 276); add the new private function near `` `render the findByPrimaryKeyOrNull function`() `` (around line 883)
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt` — add one test method

**Acceptance Criteria:**
- [ ] Single-PK entities (e.g. `HistorySubOneDao`) get `fun findAllByPrimaryKeys(ids: List<DomainId>): List<X>` doing one `where id in (:ids)` query
- [ ] Composite-PK entities (e.g. `CompositePrimaryKeyDao`) get `fun findAllByPrimaryKeys(primaryKeys: List<XPk>): List<X>` — implemented as `primaryKeys.mapNotNull { findByPrimaryKeyOrNull(it) }` (deliberately N single-row lookups rather than a dynamic OR-chain: composite-key `IN`-expansion isn't natively supported by Spring's named-parameter binding, and building one at the generator level is a nested-string-escaping hazard disproportionate to the win for what is already the less-common case — the actual perf-critical path, the UPDATE batch itself, is still a single JDBC batch for composite-PK entities too; only this secondary re-fetch-for-history step falls back to N queries)
- [ ] `HistorySubOneDao.findAllByPrimaryKeys(listOf(id1, id3))` returns exactly those two entities

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"` → all tests pass (including new one)

**Steps:**

- [ ] **Step 1: Write the failing test.** Add to `HistorySuperDaoTest.kt` (it already has `historySubOneDao` autowired and imports `assertThat`):

```kotlin
    @Test
    fun testFindAllByPrimaryKeys_returnsOnlyRequestedEntities() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()
        val entity3 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)
        this.historySubOneDao.insert(entity3)

        val result = this.historySubOneDao.findAllByPrimaryKeys(listOf(entity1.id, entity3.id))

        assertThat(result.map { it.id }).containsExactlyInAnyOrder(entity1.id, entity3.id)

    }
```

- [ ] **Step 2: Run the test to confirm it fails to compile** (method doesn't exist yet)

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"`
Expected: compile error, `Unresolved reference: findAllByPrimaryKeys`

- [ ] **Step 3: Add the dispatch call.** In `JdbcDaoRenderer.kt`'s `renderFunctions()`, change:

```kotlin
        `render the findByPrimaryKey function`()
        `render the findByPrimaryKeyOrNull function`()
        `render the findVersionByPrimaryKey function`()
```
to:
```kotlin
        `render the findByPrimaryKey function`()
        `render the findByPrimaryKeyOrNull function`()
        `render the findAllByPrimaryKeys function`()
        `render the findVersionByPrimaryKey function`()
```

- [ ] **Step 4: Add the render function.** Add this private function next to `` `render the findByPrimaryKeyOrNull function`() ``:

```kotlin
    private fun `render the findAllByPrimaryKeys function`() {

        blankLine()
        blankLine()

        if (entityDef.hasCompositePrimaryKey) {

            appendLine("    fun findAllByPrimaryKeys(primaryKeys: List<${entityDef.entityPkClassDef.uqcn}>): List<${entityDef.entityUqcn}> {")
            blankLine()
            appendLine("        return primaryKeys.mapNotNull { findByPrimaryKeyOrNull(it) }")
            blankLine()
            appendLine("    }")

        } else {

            addImportFor<DomainId>()

            val pk = entityDef.primaryKeyFields.single()

            appendLine("    fun findAllByPrimaryKeys(ids: List<DomainId>): List<${entityDef.entityUqcn}> {")
            blankLine()
            appendLine("        if (ids.isEmpty()) {")
            appendLine("            return emptyList()")
            appendLine("        }")
            blankLine()
            appendLine("        return jdbcOps.queryForList(")
            appendLine("            \"${EffectiveTimestampRendererHelper.selectStarClause(entityDef)} from ${entityDef.schemaAndTableName} where ${pk.tableColumnName} in (:ids)\",")
            appendLine("            SqlParams().apply {")
            appendLine("                addListValue(\"ids\", ids)")
            appendLine("            },")
            appendLine("            this.entityRowMapper")
            appendLine("        )")
            blankLine()
            appendLine("    }")

        }

    }
```

- [ ] **Step 5: Fast-compile the generator, then regenerate and compile showcase**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration
./gradlew :maia-showcase:dao:compileKotlin
```
Fix any Kotlin syntax errors in the generated `.kt` files by correcting the `appendLine(...)` calls above, then repeat.

- [ ] **Step 6: Run the test again**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"`
Expected: PASS (new test + the pre-existing `testInsertAndSetFields`)

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt maia-showcase/dao/src/generated maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt
git commit -m "Add findAllByPrimaryKeys DAO generator helper

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 3: Add `bulkSetFields` DAO renderer (core logic, all branches)

**Goal:** The main `bulkSetFields` generator function — field-set grouping, JDBC batching, aggregate optimistic-lock failure collection, bulk history re-insert — written once to cover every branch `setFields` already has (versioned/non-versioned, with/without history, effective-timestamp, composite PK). Validated here against the common case: `HistorySubOneDao` (single PK, versioned, with history, no effective timestamps).

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt` — insert dispatch call after `` `render the setFields function`() `` (around line 292); add the new private function near it
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt` — add three test methods

**Acceptance Criteria:**
- [ ] A batch where every updater sets the same field runs as a single JDBC batch and updates all rows
- [ ] A batch where updaters set *different* fields is split into multiple field-set groups, each updated correctly
- [ ] A batch containing some stale-version updaters throws one `BulkOptimisticLockingException` listing every stale row, while still applying the valid updaters in the same batch
- [ ] Every successfully-updated row gets exactly one new history row (via `findAllByPrimaryKeys` + `bulkInsertHistory`)

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"` → all tests pass

**Steps:**

- [ ] **Step 1: Write the three failing tests.** Add to `HistorySuperDaoTest.kt` (add `import java.time.Instant`, `import org.assertj.core.api.Assertions.assertThatThrownBy`, and `import org.maiaframework.jdbc.BulkOptimisticLockingException` at the top):

```kotlin
    @Test
    fun testBulkSetFields_updatesAllRows_whenAllUpdatersSetTheSameField() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)

        val updater1 = HistorySubOneEntityUpdater.forPrimaryKey(entity1.id, entity1.version) {
            someString("updated1")
        }
        val updater2 = HistorySubOneEntityUpdater.forPrimaryKey(entity2.id, entity2.version) {
            someString("updated2")
        }

        this.historySubOneDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.historySubOneDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.historySubOneDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.someString).isEqualTo("updated1")
        assertThat(updatedEntity1.version).isEqualTo(2)
        assertThat(updatedEntity2.someString).isEqualTo("updated2")
        assertThat(updatedEntity2.version).isEqualTo(2)

        val historyEntity1V2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entity1.id, 2))
        assertHistoryEntity(historyEntity1V2, updatedEntity1, ChangeType.UPDATE)

        val historyEntity2V2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(entity2.id, 2))
        assertHistoryEntity(historyEntity2V2, updatedEntity2, ChangeType.UPDATE)

    }


    @Test
    fun testBulkSetFields_groupsByFieldSet_whenUpdatersSetDifferentFields() {

        val entity1 = HistorySubOneEntityTestBuilder().build()
        val entity2 = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(entity1)
        this.historySubOneDao.insert(entity2)

        val newTimestamp = Instant.now().plusSeconds(120)

        val updater1 = HistorySubOneEntityUpdater.forPrimaryKey(entity1.id, entity1.version) {
            someString("updated1")
        }
        val updater2 = HistorySubOneEntityUpdater.forPrimaryKey(entity2.id, entity2.version) {
            lastModifiedTimestamp(newTimestamp)
        }

        this.historySubOneDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.historySubOneDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.historySubOneDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.someString).isEqualTo("updated1")
        assertThat(updatedEntity2.lastModifiedTimestamp).isEqualTo(newTimestamp)
        assertThat(updatedEntity2.someString).isEqualTo(entity2.someString)

    }


    @Test
    fun testBulkSetFields_throwsAggregateException_listingAllStaleRows_butStillUpdatesValidOnes() {

        val staleEntity1 = HistorySubOneEntityTestBuilder().build()
        val staleEntity2 = HistorySubOneEntityTestBuilder().build()
        val validEntity = HistorySubOneEntityTestBuilder().build()

        this.historySubOneDao.insert(staleEntity1)
        this.historySubOneDao.insert(staleEntity2)
        this.historySubOneDao.insert(validEntity)

        val staleUpdater1 = HistorySubOneEntityUpdater.forPrimaryKey(staleEntity1.id, 999L) {
            someString("shouldNotApply1")
        }
        val staleUpdater2 = HistorySubOneEntityUpdater.forPrimaryKey(staleEntity2.id, 999L) {
            someString("shouldNotApply2")
        }
        val validUpdater = HistorySubOneEntityUpdater.forPrimaryKey(validEntity.id, validEntity.version) {
            someString("shouldApply")
        }

        assertThatThrownBy {
            this.historySubOneDao.bulkSetFields(listOf(staleUpdater1, validUpdater, staleUpdater2))
        }.isInstanceOf(BulkOptimisticLockingException::class.java)

        val updatedStale1 = this.historySubOneDao.findByPrimaryKey(staleEntity1.id)
        val updatedStale2 = this.historySubOneDao.findByPrimaryKey(staleEntity2.id)
        val updatedValid = this.historySubOneDao.findByPrimaryKey(validEntity.id)

        assertThat(updatedStale1.someString).isEqualTo(staleEntity1.someString)
        assertThat(updatedStale1.version).isEqualTo(1)
        assertThat(updatedStale2.someString).isEqualTo(staleEntity2.someString)
        assertThat(updatedStale2.version).isEqualTo(1)
        assertThat(updatedValid.someString).isEqualTo("shouldApply")
        assertThat(updatedValid.version).isEqualTo(2)

    }
```

- [ ] **Step 2: Run to confirm compile failure** (`bulkSetFields` doesn't exist yet)

- [ ] **Step 3: Add the dispatch call.** In `renderFunctions()`, change:
```kotlin
        `render the setFields function`()
        `render the closeEffectiveRange function`()
```
to:
```kotlin
        `render the setFields function`()
        `render the bulkSetFields function`()
        `render the closeEffectiveRange function`()
```

- [ ] **Step 4: Add the render function**, next to `` `render the setFields function`() ``:

```kotlin
    private fun `render the bulkSetFields function`() {

        if (entityDef.hasNoModifiableFields()) {
            return
        }

        val needsOutcomeTracking = entityDef.versioned.value || entityDef.withVersionHistory.value
        val pkField = if (entityDef.hasCompositePrimaryKey) null else entityDef.primaryKeyFields.single()

        addImportFor(Fqcns.MAIA_FIELD_UPDATE)

        blankLine()
        blankLine()
        appendLine("    fun bulkSetFields(updaters: List<${entityDef.entityUpdaterClassDef.uqcn}>) {")
        blankLine()
        appendLine("        val groups = updaters.groupBy { updater -> updater.fields.map { it.classFieldName }.toSet() }")

        if (entityDef.versioned.value) {
            addImportFor(Fqcns.MAIA_JDBC_BULK_OPTIMISTIC_LOCKING_EXCEPTION)
            appendLine("        val failedUpdaters = mutableListOf<${entityDef.entityUpdaterClassDef.uqcn}>()")
        }

        if (entityDef.withVersionHistory.value) {
            if (entityDef.hasCompositePrimaryKey) {
                appendLine("        val updatedPrimaryKeys = mutableListOf<${entityDef.entityPkClassDef.uqcn}>()")
            } else {
                addImportFor<DomainId>()
                appendLine("        val updatedIds = mutableListOf<DomainId>()")
            }
        }

        blankLine()
        appendLine("        groups.values.forEach { group ->")
        blankLine()
        appendLine("            val representative = group.first()")
        appendLine("            val sql = StringBuilder()")
        appendLine("            sql.append(\"update ${entityDef.schemaAndTableName} set \")")
        blankLine()

        if (entityDef.hasEffectiveTimestamps) {

            addImportFor<Instant>()
            appendLine("            val effectiveFromUpdate = representative.fields.find { it.classFieldName == \"effectiveFrom\" }")
            appendLine("            val effectiveToUpdate = representative.fields.find { it.classFieldName == \"effectiveTo\" }")
            blankLine()
            appendLine("            val fieldClauses = representative.fields")
            appendLine("                .filterNot { it.classFieldName == \"effectiveFrom\" || it.classFieldName == \"effectiveTo\" }")

            if (entityDef.versioned.value) {
                appendLine("                .plus(FieldUpdate(\"version_incremented\", \"version\", 0))")
            }

            appendLine("                .map { field -> \"\${field.dbColumnName} = :\${field.classFieldName}\" }")
            appendLine("                .plus(")
            appendLine("                    when {")
            appendLine("                        effectiveFromUpdate != null && effectiveToUpdate != null -> listOf(\"effective_range = tstzrange(:effectiveFrom, :effectiveTo)\")")
            appendLine("                        effectiveFromUpdate != null -> listOf(\"effective_range = tstzrange(:effectiveFrom, upper(effective_range))\")")
            appendLine("                        effectiveToUpdate != null -> listOf(\"effective_range = tstzrange(lower(effective_range), :effectiveTo)\")")
            appendLine("                        else -> emptyList()")
            appendLine("                    }")
            appendLine("                )")
            appendLine("                .joinToString(\", \")")

        } else {

            appendLine("            val fieldClauses = representative.fields")

            if (entityDef.versioned.value) {
                appendLine("                .plus(FieldUpdate(\"version_incremented\", \"version\", 0))")
            }

            appendLine("                .joinToString(\", \") { field -> \"\${field.dbColumnName} = :\${field.classFieldName}\" }")

        }

        blankLine()
        appendLine("            sql.append(fieldClauses)")

        if (entityDef.hasCompositePrimaryKey) {
            val whereClause = entityDef.primaryKeyFields.joinToString(" and ") { "${it.tableColumnName} = :${it.classFieldName}" }
            appendLine("            sql.append(\" where $whereClause\")")
        } else {
            appendLine("            sql.append(\" where ${pkField!!.tableColumnName} = :${pkField.classFieldName}\")")
        }

        if (entityDef.versioned.value) {
            appendLine("            sql.append(\" and version = :version\")")
        }

        blankLine()
        appendLine("            val sqlParamsList = group.map { updater ->")
        appendLine("                val sqlParams = SqlParams()")
        appendLine("                updater.fields.forEach { field -> addField(field, sqlParams) }")

        if (entityDef.hasEffectiveTimestamps) {
            appendLine("                val effectiveFrom = updater.fields.find { it.classFieldName == \"effectiveFrom\" }")
            appendLine("                val effectiveTo = updater.fields.find { it.classFieldName == \"effectiveTo\" }")
            appendLine("                effectiveFrom?.let { sqlParams.addValue(\"effectiveFrom\", it.value as Instant?) }")
            appendLine("                effectiveTo?.let { sqlParams.addValue(\"effectiveTo\", it.value as Instant?) }")
        }

        blankLine()

        if (entityDef.hasCompositePrimaryKey) {
            entityDef.primaryKeyFields.forEach {
                appendLine("                sqlParams.addValue(\"${it.classFieldName}\", updater.primaryKey.${it.classFieldName})")
            }
        } else {
            appendLine("                sqlParams.addValue(\"${pkField!!.classFieldName}\", updater.${pkField.classFieldName})")
        }

        if (entityDef.versioned.value) {
            appendLine("                sqlParams.addValue(\"version\", updater.version)")
            appendLine("                sqlParams.addValue(\"version_incremented\", updater.version + 1)")
        }

        appendLine("                sqlParams")
        appendLine("            }")
        blankLine()

        if (needsOutcomeTracking) {

            appendLine("            val updateCounts = jdbcOps.batchUpdate(sql.toString(), sqlParamsList)")
            blankLine()
            appendLine("            group.forEachIndexed { i, updater ->")
            appendLine("                if (updateCounts[i] == 0) {")

            if (entityDef.versioned.value) {
                appendLine("                    failedUpdaters.add(updater)")
            }

            appendLine("                } else {")

            if (entityDef.withVersionHistory.value) {
                if (entityDef.hasCompositePrimaryKey) {
                    appendLine("                    updatedPrimaryKeys.add(updater.primaryKey)")
                } else {
                    appendLine("                    updatedIds.add(updater.${pkField!!.classFieldName})")
                }
            }

            appendLine("                }")
            appendLine("            }")

        } else {
            appendLine("            jdbcOps.batchUpdate(sql.toString(), sqlParamsList)")
        }

        blankLine()
        appendLine("        }")

        if (entityDef.withVersionHistory.value) {

            blankLine()

            if (entityDef.hasCompositePrimaryKey) {
                appendLine("        val updatedEntities = findAllByPrimaryKeys(updatedPrimaryKeys)")
            } else {
                appendLine("        val updatedEntities = findAllByPrimaryKeys(updatedIds)")
            }

            addImportFor<ChangeType>()
            appendLine("        bulkInsertHistory(updatedEntities, ChangeType.UPDATE)")

        }

        if (entityDef.versioned.value) {

            blankLine()
            appendLine("        if (failedUpdaters.isNotEmpty()) {")

            val primaryKeyExpr = if (entityDef.hasCompositePrimaryKey) "it.primaryKey" else "it.${pkField!!.classFieldName}"
            appendLine("            throw BulkOptimisticLockingException(${entityDef.metaClassDef.uqcn}.TABLE_NAME, failedUpdaters.map { $primaryKeyExpr to it.version })")
            appendLine("        }")

        }

        blankLine()
        appendLine("    }")

    }
```

- [ ] **Step 5: Fast-compile, regenerate, compile showcase, fix syntax issues until clean**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration
./gradlew :maia-showcase:dao:compileKotlin
```

- [ ] **Step 6: Run the tests**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"`
Expected: PASS — all 5 tests (2 pre-existing + 1 from Task 2 + 3 new)

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt maia-showcase/dao/src/generated maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt
git commit -m "Add bulkSetFields DAO generator function

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 4: Validate non-versioned/non-history + effective-timestamp branch

**Goal:** Prove the `bulkSetFields` function written in Task 3 handles the opposite end of the spectrum correctly: no version check, no history, and the `tstzrange(...)` effective-timestamp SQL branch. Target: `EffectiveTimestampDao` (not versioned, no history, has effective timestamps). No new renderer code is expected — this task's job is to write the test, find bugs the branches above didn't exercise, and fix them in the Task 3 function if needed.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/effective_dated/EffectiveTimestampDaoTest.kt`
- Modify (if bugs found): `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt`

**Acceptance Criteria:**
- [ ] Bulk-updating `effectiveFrom`+`effectiveTo` together works via the `tstzrange(:effectiveFrom, :effectiveTo)` clause
- [ ] Bulk-updating a plain field (`someString`) alongside effective-timestamp updates in the same call groups correctly (different field-sets → different groups)
- [ ] No `BulkOptimisticLockingException` class or version/history logic appears in the generated `EffectiveTimestampDao.bulkSetFields` (confirms the conditional branches in Task 3 correctly omit them for non-versioned entities)

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.effective_dated.EffectiveTimestampDaoTest"` → PASS

**Steps:**

- [ ] **Step 1: Write the failing test**

```kotlin
package org.maiaframework.showcase.effective_dated

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit


class EffectiveTimestampDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var effectiveTimestampDao: EffectiveTimestampDao


    @Test
    fun testBulkSetFields_updatesEffectiveRangeAndPlainField_inOneCall() {

        val entity1 = EffectiveTimestampEntityTestBuilder().build()
        val entity2 = EffectiveTimestampEntityTestBuilder().build()

        this.effectiveTimestampDao.insert(entity1)
        this.effectiveTimestampDao.insert(entity2)

        val newEffectiveFrom = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)
        val newEffectiveTo = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS)

        val updater1 = EffectiveTimestampEntityUpdater.forPrimaryKey(entity1.id, entity1.version) {
            effectiveFrom(newEffectiveFrom)
            effectiveTo(newEffectiveTo)
        }
        val updater2 = EffectiveTimestampEntityUpdater.forPrimaryKey(entity2.id, entity2.version) {
            someString("updated2")
        }

        this.effectiveTimestampDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.effectiveTimestampDao.findByPrimaryKey(entity1.id)
        val updatedEntity2 = this.effectiveTimestampDao.findByPrimaryKey(entity2.id)

        assertThat(updatedEntity1.effectiveFrom).isEqualTo(newEffectiveFrom)
        assertThat(updatedEntity1.effectiveTo).isEqualTo(newEffectiveTo)
        assertThat(updatedEntity2.someString).isEqualTo("updated2")

    }


}
```

If `EffectiveTimestampEntityUpdater.Builder` doesn't expose `effectiveFrom`/`effectiveTo`/`someString` setters with exactly these names, read the generated `EffectiveTimestampEntityUpdater.kt` first and adjust the test to match its actual builder methods — don't guess.

- [ ] **Step 2: Run to confirm failure** (either compile error if `bulkSetFields` signature issue, or assertion failure if a logic bug) — expected to actually PASS already if Task 3's function is correct, since no new renderer code is anticipated. If it fails, debug using the `systematic-debugging` skill: inspect the generated `EffectiveTimestampDao.kt` output and compare it against the hand-traced single-row `setFields` equivalent to find the divergence.

- [ ] **Step 3: Fix any bugs found in the Task 3 render function**, regenerate, recompile, rerun until green.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/effective_dated/EffectiveTimestampDaoTest.kt maia-showcase/dao/src/generated
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt 2>/dev/null || true
git commit -m "Validate bulkSetFields for non-versioned effective-timestamp entities

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 5: Validate composite primary key branch

**Goal:** Prove `bulkSetFields` and `findAllByPrimaryKeys` work for composite-PK entities. Target: `CompositePrimaryKeyDao` (versioned, with history, two-column PK, exactly one modifiable field).

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePrimaryKeyEntityTestBuilder.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk/CompositePrimaryKeyDaoTest.kt`
- Modify (if bugs found): `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt`

**Acceptance Criteria:**
- [ ] `bulkSetFields` on `CompositePrimaryKeyDao` updates rows identified by `(someString, someInt)` correctly
- [ ] `findAllByPrimaryKeys` (composite branch: `primaryKeys.mapNotNull { findByPrimaryKeyOrNull(it) }`) correctly bulk re-fetches for the history step
- [ ] A stale-version row in the batch is collected into the `BulkOptimisticLockingException`, keyed by `CompositePrimaryKeyEntityPk`, not a bare id

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.composite_pk.CompositePrimaryKeyDaoTest"` → PASS

**Steps:**

- [ ] **Step 1: Create the test builder** (`CompositePrimaryKeyEntity` constructor is `(createdTimestamp: Instant, someInt: Int, someModifiableString: String, someString: String, version: Long)` — verified against the generated entity class):

```kotlin
package org.maiaframework.showcase.composite_pk

import java.time.Instant
import java.util.UUID


data class CompositePrimaryKeyEntityTestBuilder(
    val createdTimestamp: Instant = Instant.now(),
    val someInt: Int = (1..1_000_000).random(),
    val someModifiableString: String = "modifiable-${UUID.randomUUID()}",
    val someString: String = "key-${UUID.randomUUID()}",
    val version: Long = 1L
) {


    fun build(): CompositePrimaryKeyEntity {

        return CompositePrimaryKeyEntity(
            this.createdTimestamp,
            this.someInt,
            this.someModifiableString,
            this.someString,
            this.version
        )

    }


}
```

- [ ] **Step 2: Write the failing test**

```kotlin
package org.maiaframework.showcase.composite_pk

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.maiaframework.jdbc.BulkOptimisticLockingException
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired


class CompositePrimaryKeyDaoTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var compositePrimaryKeyDao: CompositePrimaryKeyDao


    @Test
    fun testBulkSetFields_updatesRowsByCompositeKey_andInsertsHistory() {

        val entity1 = CompositePrimaryKeyEntityTestBuilder().build()
        val entity2 = CompositePrimaryKeyEntityTestBuilder().build()

        this.compositePrimaryKeyDao.insert(entity1)
        this.compositePrimaryKeyDao.insert(entity2)

        val updater1 = CompositePrimaryKeyEntityUpdater.forPrimaryKey(entity1.primaryKey, entity1.version) {
            someModifiableString("updated1")
        }
        val updater2 = CompositePrimaryKeyEntityUpdater.forPrimaryKey(entity2.primaryKey, entity2.version) {
            someModifiableString("updated2")
        }

        this.compositePrimaryKeyDao.bulkSetFields(listOf(updater1, updater2))

        val updatedEntity1 = this.compositePrimaryKeyDao.findByPrimaryKey(entity1.primaryKey)
        val updatedEntity2 = this.compositePrimaryKeyDao.findByPrimaryKey(entity2.primaryKey)

        assertThat(updatedEntity1.someModifiableString).isEqualTo("updated1")
        assertThat(updatedEntity1.version).isEqualTo(2)
        assertThat(updatedEntity2.someModifiableString).isEqualTo("updated2")
        assertThat(updatedEntity2.version).isEqualTo(2)

    }


    @Test
    fun testBulkSetFields_throwsAggregateException_whenCompositeKeyRowIsStale() {

        val staleEntity = CompositePrimaryKeyEntityTestBuilder().build()
        val validEntity = CompositePrimaryKeyEntityTestBuilder().build()

        this.compositePrimaryKeyDao.insert(staleEntity)
        this.compositePrimaryKeyDao.insert(validEntity)

        val staleUpdater = CompositePrimaryKeyEntityUpdater.forPrimaryKey(staleEntity.primaryKey, 999L) {
            someModifiableString("shouldNotApply")
        }
        val validUpdater = CompositePrimaryKeyEntityUpdater.forPrimaryKey(validEntity.primaryKey, validEntity.version) {
            someModifiableString("shouldApply")
        }

        assertThatThrownBy {
            this.compositePrimaryKeyDao.bulkSetFields(listOf(staleUpdater, validUpdater))
        }.isInstanceOf(BulkOptimisticLockingException::class.java)

        val updatedStale = this.compositePrimaryKeyDao.findByPrimaryKey(staleEntity.primaryKey)
        val updatedValid = this.compositePrimaryKeyDao.findByPrimaryKey(validEntity.primaryKey)

        assertThat(updatedStale.someModifiableString).isEqualTo(staleEntity.someModifiableString)
        assertThat(updatedValid.someModifiableString).isEqualTo("shouldApply")

    }


}
```

- [ ] **Step 3: Run, debug, fix any issues in the Task 3 render function or `findAllByPrimaryKeys`'s composite branch, regenerate, recompile, rerun until green.**

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/composite_pk maia-showcase/dao/src/generated
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt 2>/dev/null || true
git commit -m "Validate bulkSetFields and findAllByPrimaryKeys for composite primary keys

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 6: Validate entity-hierarchy branch

**Goal:** Prove that `bulkSetFields` on the abstract supertype's DAO (`HistorySuperDao`), given a mixed batch of updaters whose underlying rows are different concrete subtypes (`HistorySubOne`/`HistorySubTwo`), correctly dispatches history inserts to each subtype's own history DAO via the existing `bulkInsertHistory` hierarchy logic. No new renderer code is expected here — `bulkInsertHistory`'s hierarchy dispatch and `findAllByPrimaryKeys`'s supertype-typed return value were already written in Tasks 2–3; this is validation only.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt` — add one autowired field + one test method

**Acceptance Criteria:**
- [ ] A single `bulkSetFields` call on `HistorySuperDao` with one `HistorySubOne`-backed updater and one `HistorySubTwo`-backed updater updates both rows
- [ ] History rows land in `historySubOneHistoryDao` and `historySubTwoHistoryDao` respectively (not cross-wired)

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistorySuperDaoTest"` → PASS

**Steps:**

- [ ] **Step 1: Add the autowired field.** The existing field `historySuperDao` in this test class is already (confusingly) typed `HistorySuperHistoryDao` — do not touch it. Add a new field with a different name for the actual entity DAO:

```kotlin
    @Autowired
    private lateinit var historySuperEntityDao: HistorySuperDao
```

- [ ] **Step 2: Write the failing test**

```kotlin
    @Test
    fun testBulkSetFields_withMixedHierarchyTypes_dispatchesHistoryToCorrectSubtype() {

        val subOneEntity = HistorySubOneEntityTestBuilder().build()
        val subTwoEntity = HistorySubTwoEntityTestBuilder().build()

        this.historySubOneDao.insert(subOneEntity)
        this.historySubTwoDao.insert(subTwoEntity)

        val newLastModifiedTimestamp = Instant.now().plusSeconds(60)

        val updaterSubOne = HistorySuperEntityUpdater.forPrimaryKey(subOneEntity.id, subOneEntity.version) {
            lastModifiedTimestamp(newLastModifiedTimestamp)
        }
        val updaterSubTwo = HistorySuperEntityUpdater.forPrimaryKey(subTwoEntity.id, subTwoEntity.version) {
            lastModifiedTimestamp(newLastModifiedTimestamp)
        }

        this.historySuperEntityDao.bulkSetFields(listOf(updaterSubOne, updaterSubTwo))

        val updatedSubOne = this.historySubOneDao.findByPrimaryKey(subOneEntity.id)
        val updatedSubTwo = this.historySubTwoDao.findByPrimaryKey(subTwoEntity.id)

        assertThat(updatedSubOne.lastModifiedTimestamp).isEqualTo(newLastModifiedTimestamp)
        assertThat(updatedSubOne.version).isEqualTo(2)
        assertThat(updatedSubTwo.lastModifiedTimestamp).isEqualTo(newLastModifiedTimestamp)
        assertThat(updatedSubTwo.version).isEqualTo(2)

        val historySubOneV2 = this.historySubOneHistoryDao.findByPrimaryKey(HistorySubOneHistoryEntityPk(subOneEntity.id, 2))
        assertThat(historySubOneV2.changeType).isEqualTo(ChangeType.UPDATE)

        val historySubTwoV2 = this.historySubTwoHistoryDao.findByPrimaryKey(HistorySubTwoHistoryEntityPk(subTwoEntity.id, 2))
        assertThat(historySubTwoV2.changeType).isEqualTo(ChangeType.UPDATE)

    }
```

If `HistorySubTwoHistoryEntityPk`'s constructor shape differs from `HistorySubOneHistoryEntityPk(id, version)`, read the generated file first and adjust — don't guess.

- [ ] **Step 3: Run, debug, fix.** If this fails, the most likely cause is `findAllByPrimaryKeys` on the supertype DAO not being generated with the supertype's entity type, or `bulkInsertHistory`'s existing hierarchy `groupBy`/`when` dispatch not lining up with how `bulkSetFields` calls it — inspect the generated `HistorySuperDao.kt` directly to compare against the hand-verified `bulkInsertHistory` shape from the design spec.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt maia-showcase/dao/src/generated
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt 2>/dev/null || true
git commit -m "Validate bulkSetFields for entity hierarchies

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 7: Add `bulkSetFields` Repo passthrough (with cache eviction)

**Goal:** Expose `bulkSetFields` on the generated Repo layer, mirroring `bulkInsert`'s passthrough, including the cache-eviction behavior the single-row `setFields` passthrough already has for `cacheable` entities.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityRepoRenderer.kt` — insert dispatch call after `` `render function setFields`() `` (around line 96); add the new private function near it
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistorySuperDaoTest.kt` — no change needed (Repo layer isn't exercised by existing DAO-level tests); verification here is compile-only

**Acceptance Criteria:**
- [ ] `HistorySuperRepo` (and every other showcase Repo) gets a `bulkSetFields` method compiling cleanly, delegating to `this.dao.bulkSetFields(updaters)`
- [ ] No showcase entity is `cacheable`, so the cache-eviction branch cannot get integration-test coverage here — note this explicitly rather than silently skipping it; it is verified by code review against the existing single-row cacheable `setFields` passthrough (`EntityRepoRenderer.kt:576-605`) only

**Verify:** `./gradlew :maia-showcase:repo:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add the dispatch call.** In `EntityRepoRenderer.kt`'s `renderFunctions()`, change:
```kotlin
        `render function setFields`()
        `render function closeEffectiveRange`()
```
to:
```kotlin
        `render function setFields`()
        `render function bulkSetFields`()
        `render function closeEffectiveRange`()
```

- [ ] **Step 2: Add the render function**, mirroring `` `render function setFields`() `` (`EntityRepoRenderer.kt:556-624`) and `` `render function bulkInsert`() `` (`EntityRepoRenderer.kt:475-485`):

```kotlin
    private fun `render function bulkSetFields`() {

        if (this.entityDef.hasNoModifiableFields()) {
            return
        }

        blankLine()
        blankLine()
        appendLine("    fun bulkSetFields(updaters: List<${entityDef.entityUpdaterClassDef.uqcn}>) {")
        blankLine()
        appendLine("        logger.debug(\"bulkSetFields {}\", updaters)")
        blankLine()

        if (cacheable) {

            val primaryKeyUpdaterFields = if (entityDef.hasCompositePrimaryKey) {
                "updater.primaryKey"
            } else {
                entityDef.primaryKeyClassFields.joinToString(", ") { "updater.${it.classFieldName}" }
            }

            appendLine("        try {")
            appendLine("            this.dao.bulkSetFields(updaters)")
            appendLine("        } finally {")
            appendLine("            updaters.forEach { updater -> this.cache.evict($primaryKeyUpdaterFields) }")
            appendLine("        }")

        } else {

            appendLine("        this.dao.bulkSetFields(updaters)")

        }

        blankLine()
        appendLine("    }")

    }
```

The `try`/`finally` evicts every updater's cache entry even when `BulkOptimisticLockingException` is thrown — correct, since some rows in the batch may have succeeded before the failure surfaced, and an evicted cache entry is always safe (next read just re-fetches), whereas a stale one is not.

- [ ] **Step 3: Fast-compile, regenerate repo module, compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
./gradlew :maia-showcase:repo:maiaGeneration
./gradlew :maia-showcase:repo:compileKotlin
```

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityRepoRenderer.kt maia-showcase/repo/src/generated
git commit -m "Add bulkSetFields Repo passthrough with cache eviction

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

### Task 8: Full regeneration and full test suite run

**Goal:** Confirm the complete feature is consistent across the whole `maia-showcase` module (not just the entities touched by earlier tasks) and nothing else broke.

**Files:** None (verification only — may touch any `src/generated` file across `maia-showcase` if regeneration picks up drift)

**Acceptance Criteria:**
- [ ] Every entity across `maia-showcase` regenerates without error
- [ ] The full `maia-showcase:app` test suite passes, not just the new tests
- [ ] `git status` on `maia-showcase/dao/src/generated` and `maia-showcase/repo/src/generated` shows only `bulkSetFields`/`findAllByPrimaryKeys` additions, no unrelated diffs

**Verify:** `./gradlew :maia-showcase:app:test` → BUILD SUCCESSFUL, all tests pass

**Steps:**

- [ ] **Step 1: Full regeneration across all showcase modules**

```bash
./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration
```

- [ ] **Step 2: Full compile**

```bash
./gradlew :maia-showcase:compileKotlin :maia-showcase:dao:compileKotlin :maia-showcase:repo:compileKotlin :maia-showcase:app:compileTestKotlin
```

- [ ] **Step 3: Full test suite**

Run: `./gradlew :maia-showcase:app:test`
Expected: BUILD SUCCESSFUL

If anything outside the entities touched in Tasks 1–7 fails, investigate with the `systematic-debugging` skill before proceeding — a regression here means some entity shape wasn't accounted for in Task 3's conditional branches.

- [ ] **Step 4: Review the full diff for anything unexpected**

```bash
git status
git diff --stat
```

- [ ] **Step 5: Commit any remaining regenerated files**

```bash
git add -A
git status
git commit -m "Regenerate full maia-showcase after bulkSetFields generator changes

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

(Skip this commit if `git status` shows nothing to commit.)

- [ ] **Step 6: Report back** — summarize what branches of `setFields` parity were proven out (with which showcase entity), any deliberate scope reductions made along the way (e.g. the composite-PK re-fetch simplification from Task 2), and remind that this branch (`feature/bulk-set-fields-generator`) is a spike and should **not** be merged to `main` without the user's explicit go-ahead.

---

## Self-review notes

- **Spec coverage:** every section of `2026-10-02-bulk-set-fields-design.md` has a corresponding task — grouping/core mechanism (Task 3), failure handling (Task 3), history (Tasks 2–3, 6), effective-dated (Task 4), composite PK (Tasks 2, 5) with the OR-chain idea deliberately simplified to N single-row lookups (documented in Task 2, not a silent deviation), entity hierarchies (Task 6), Repo passthrough (Task 7).
- **Known risk:** the hand-written `appendLine(...)` string escaping in Task 3's render function is the single highest-risk piece of this plan — it was composed by reading real generated-code precedent but not compiled. Tasks 3–6 budget explicit "fix and iterate" steps for this reason rather than assuming first-try correctness.
- **Branch discipline:** every task commits independently on `feature/bulk-set-fields-generator`; no task pushes or merges to `main`.

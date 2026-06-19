# System-Managed Effective Timestamps Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the generator produce correct SYSTEM-managed semantics for MtM join entities with `effectiveRangeDef.managedBy == SYSTEM && dateType == TIMESTAMP`: `effectiveFrom = Instant.now()` on insert, soft-delete on remove, effective-only queries for edit and FK guards.

**Architecture:** Two generator renderer files change (`CrudServiceRenderer`, `RowMapperRenderer`). After changing the generators, run `maiaGeneration` on the showcase to regenerate affected service and row-mapper files. Finally add DB assertions to the existing Playwright test to prove the new behavior.

**Tech Stack:** Kotlin, Spring Boot (JDBC), JUnit 5, AssertJ

**User Verification:** NO — no human sign-off required.

---

## File Map

| File | Role |
|---|---|
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt` | Generates service create/update/delete logic — 3 change sites |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt` | Generates fetch-for-edit join query — 1 change site |
| `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt` | Regenerated — do not edit manually |
| `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyFetchForEditDtoRowMapper.kt` | Regenerated — do not edit manually |
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt` | Add two @Autowired fields and two DB assertion blocks |

---

### Task 0: Fix CrudServiceRenderer — SYSTEM-managed service logic

**Goal:** The generator emits `Instant.now()` / soft-delete / `findEffectiveBy` / `findEffectiveBy.isNotEmpty()` whenever the join entity has `managedBy == SYSTEM && dateType == TIMESTAMP`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt`

**Acceptance Criteria:**
- [ ] `EffectiveRangeManagedBy` imported in `CrudServiceRenderer`
- [ ] Create path: `effectiveFrom = Instant.now()` / `effectiveTo = null` for SYSTEM-managed
- [ ] Update path: loads effective-only joins, soft-deletes removed ones, inserts new with `Instant.now()`, skips update-existing loop
- [ ] Delete path: uses `findEffectiveBy...isNotEmpty()` instead of `existsBy...` for SYSTEM-managed FK guard
- [ ] `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add `EffectiveRangeManagedBy` import**

In `CrudServiceRenderer.kt`, after the existing `import org.maiaframework.gen.spec.definition.EffectiveRangeDateType` line, add:

```kotlin
import org.maiaframework.gen.spec.EffectiveRangeManagedBy
```

- [ ] **Step 2: Fix the create path**

Locate the block starting at `if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {` inside `render create by DTO`. Replace the entire `if` block body (lines that set up `joinDtoFieldName` and call `appendLine`/`renderNewInstanceArgsMultiLine`) with:

```kotlin
if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP) {
    val isSystemManaged = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
    val joinDtoFieldName = "${otherSideFieldName}Entities"
    val effectiveFromValue = if (isSystemManaged) "Instant.now()" else "joinDto.effectiveFrom"
    val effectiveToValue = if (isSystemManaged) "null" else "joinDto.effectiveTo"
    appendLine("        createDto.${joinDtoFieldName}.forEach { joinDto ->")
    appendLine("            this.${joinRepoFieldName}.insert(")
    appendLine("                ${joinEntityClass}.newInstance(")
    renderNewInstanceArgsMultiLine(
        indentSize = 20,
        "effectiveFrom" to effectiveFromValue,
        "effectiveTo" to effectiveToValue,
        thisSideEntityIdFieldName to "entity.id",
        otherSideFieldName to "joinDto.${otherSideFieldName}EntityId"
    )
    appendLine("                )")
    appendLine("            )")
    appendLine("        }")
```

- [ ] **Step 3: Fix the update path**

Locate the block starting at `if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP && manyToManyEntityDef.entityDef.isDeletable) {` inside `render the update function`. Replace the entire contents of that `if` block with:

```kotlin
if (manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP && manyToManyEntityDef.entityDef.isDeletable) {

    val isSystemManaged = manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
    val otherSideDtoFieldName = "${otherSideFieldName}Entities"
    val findByMethodName = if (isSystemManaged) "findEffectiveBy${thisSideFieldNameCapitalized}" else "findBy${thisSideFieldNameCapitalized}"
    val effectiveFromValue = if (isSystemManaged) "Instant.now()" else "joinDto.effectiveFrom"
    val effectiveToValue = if (isSystemManaged) "null" else "joinDto.effectiveTo"

    append("""
        |
        |        val existing${joinNamePrefix}JoinsById = this.${joinRepoFieldName}.${findByMethodName}(id).associateBy { it.id }
        |        val submitted${joinNamePrefix}JoinIds = editDto.${otherSideDtoFieldName}.mapNotNull { it.id }.toSet()
        |""".trimMargin())

    if (isSystemManaged) {
        addImportFor<Instant>()
        append("""
            |
            |        existing${joinNamePrefix}JoinsById.keys.filterNot { it in submitted${joinNamePrefix}JoinIds }.forEach {
            |            this.${joinRepoFieldName}.setFields(
            |                ${joinEntityClass}Updater.forPrimaryKey(it) {
            |                    effectiveTo(Instant.now())
            |                }
            |            )
            |        }
            |""".trimMargin())
    } else {
        append("""
            |
            |        existing${joinNamePrefix}JoinsById.keys.filterNot { it in submitted${joinNamePrefix}JoinIds }.forEach {
            |            this.${joinRepoFieldName}.deleteByPrimaryKey(it)
            |        }
            |""".trimMargin())
    }

    append("""
        |
        |        val new${joinNamePrefix}Joins = editDto.${otherSideDtoFieldName}.filter { it.id == null }.map { joinDto ->
        |            ${joinEntityClass}.newInstance(
        |""".trimMargin())

    renderNewInstanceArgsMultiLine(
        indentSize = 16,
        "effectiveFrom" to effectiveFromValue,
        "effectiveTo" to effectiveToValue,
        thisSideFieldName to "id",
        otherSideFieldName to "joinDto.${otherSideFieldName}EntityId"
    )

    append("""
        |            )
        |        }
        |        this.${joinRepoFieldName}.bulkInsert(new${joinNamePrefix}Joins)
        |""".trimMargin())

    if (!isSystemManaged) {
        append("""
            |
            |        editDto.${otherSideDtoFieldName}.filter { it.id != null }.forEach { joinDto ->
            |            val joinId = joinDto.id!!
            |            val existingJoin = existing${joinNamePrefix}JoinsById[joinId]
            |                ?: throw this.maiaProblems.joinRecordNotFound("$joinEntityClass")
            |
            |            if (existingJoin.effectiveFrom != joinDto.effectiveFrom || existingJoin.effectiveTo != joinDto.effectiveTo) {
            |                this.${joinRepoFieldName}.setFields(
            |                    ${joinEntityClass}Updater.forPrimaryKey(joinId) {
            |                        effectiveFrom(joinDto.effectiveFrom)
            |                        effectiveTo(joinDto.effectiveTo)
            |                    }
            |                )
            |            }
            |        }
            |""".trimMargin())
    }

}
```

- [ ] **Step 4: Fix the delete FK guard**

Locate the `referencingEntities.forEach { referencingEntityDef ->` block inside `render the delete function`. Replace the lines that append the `if (this.${daoName}.existsBy...)` check with:

```kotlin
referencingEntities.forEach { referencingEntityDef ->

    val daoName = referencingEntityDef.entityRepoFqcn.uqcn.firstToLower()

    val field = referencingEntityDef.allForeignKeyEntityFieldDefs.find { it.foreignKeyFieldDef?.foreignEntityBaseName == this.entityDef.entityBaseName }

    val fieldName = field!!.classFieldName.firstToUpper()

    val isSystemManagedRef = referencingEntityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
        && referencingEntityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP

    blankLine()
    if (isSystemManagedRef) {
        appendLine("        if (this.${daoName}.findEffectiveBy$fieldName($primaryKeyFieldNamesCsv).isNotEmpty()) {")
    } else {
        appendLine("        if (this.${daoName}.existsBy$fieldName($primaryKeyFieldNamesCsv)) {")
    }
    appendLine("            throw this.maiaProblems.foreignKeyRecordsExist(\"${referencingEntityDef.entityBaseName}\")")
    appendLine("        }")

}
```

- [ ] **Step 5: Compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt
git commit -m "fix: CrudServiceRenderer implements SYSTEM-managed effective-timestamp semantics"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["EffectiveRangeManagedBy imported", "Create path uses Instant.now() for SYSTEM-managed", "Update path soft-deletes and uses findEffectiveBy for SYSTEM-managed", "Delete path uses findEffectiveBy.isNotEmpty() for SYSTEM-managed FK guard", "compileKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

---

### Task 1: Fix RowMapperRenderer — effective range filter in fetch-for-edit

**Goal:** For SYSTEM-managed TIMESTAMP join entities, the fetch-for-edit join query only returns currently-effective records, preventing soft-deleted entries from reappearing in the edit form.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt`

**Acceptance Criteria:**
- [ ] `EffectiveRangeManagedBy` and `EffectiveRangeDateType` imported in `RowMapperRenderer`
- [ ] Join fetch query includes `and mtm.effective_range @> current_timestamp` for SYSTEM-managed joins
- [ ] Non-SYSTEM-managed join queries are unchanged
- [ ] `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add imports**

In `RowMapperRenderer.kt`, after the existing imports block (after `import org.maiaframework.gen.spec.definition.lang.ClassFieldDef`), add:

```kotlin
import org.maiaframework.gen.spec.EffectiveRangeManagedBy
import org.maiaframework.gen.spec.definition.EffectiveRangeDateType
```

- [ ] **Step 2: Add effective range filter to the join fetch query**

Locate the `if (joinFetchDtoDef != null) {` block inside the `manyToManyFieldDefs.forEach` loop (the block that calls `append("""...""".trimMargin())`). Add the `isSystemManagedJoin` and `effectiveRangeClause` local variables immediately after the `if (joinFetchDtoDef != null) {` line, and substitute `effectiveRangeClause` into the WHERE clause:

```kotlin
if (joinFetchDtoDef != null) {

    val isSystemManagedJoin = manyToManyRowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef.entityDef.effectiveRangeDef?.managedBy == EffectiveRangeManagedBy.SYSTEM
        && manyToManyRowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef.entityDef.effectiveRangeDef?.dateType == EffectiveRangeDateType.TIMESTAMP
    val effectiveRangeClause = if (isSystemManagedJoin) "\n            and mtm.effective_range @> current_timestamp" else ""

    addImportFor(Fqcns.MAIA_DOMAIN_ID)
    addImportFor(Fqcns.MAIA_SQL_PARAMS)
    addImportFor(joinFetchDtoDef.fqcn)

    append("""
        |
        |
        |    private fun fetch${classFieldName.firstToUpper()}JoinFetchDtos(entityId: DomainId): List<${joinFetchDtoDef.uqcn}> {
        |
        |        return this.jdbcOps.queryForList(
        |            $tripleQuote
        |            select
        |                mtm.id,
        |                other.id as entity_id,
        |                other.${joinFetchDtoDef.nameTableColumnName},
        |                lower(mtm.effective_range) as effective_from,
        |                upper(mtm.effective_range) as effective_to
        |            from ${joinFetchDtoDef.otherSideEntitySchemaAndTableName} other
        |            join ${joinFetchDtoDef.joinEntitySchemaAndTableName} mtm
        |                on other.id = mtm.${joinFetchDtoDef.otherSideIdTableColumnName}
        |            where mtm.${joinFetchDtoDef.thisSideIdTableColumnName} = :entityId${effectiveRangeClause}
        |            order by other.${joinFetchDtoDef.nameTableColumnName}
        |            $tripleQuote.trimIndent(),
        |            SqlParams().apply {
        |                addValue("entityId", entityId)
        |            },
        |        ) { rsa ->
        |            ${joinFetchDtoDef.uqcn}(
        |                id = rsa.readDomainId("id"),
        |                entityId = rsa.readDomainId("entity_id"),
        |                name = rsa.readString("${joinFetchDtoDef.nameTableColumnName}"),
        |                effectiveFrom = rsa.readInstantOrNull("effective_from"),
        |                effectiveTo = rsa.readInstantOrNull("effective_to"),
        |            )
        |        }
        |
        |    }
        |""".trimMargin())
```

The only change to the `append` block is on the WHERE clause line:
- Old: `|            where mtm.${joinFetchDtoDef.thisSideIdTableColumnName} = :entityId`
- New: `|            where mtm.${joinFetchDtoDef.thisSideIdTableColumnName} = :entityId${effectiveRangeClause}`

- [ ] **Step 3: Compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt
git commit -m "fix: RowMapperRenderer adds effective-range filter for SYSTEM-managed join fetch"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["EffectiveRangeManagedBy and EffectiveRangeDateType imported", "Join fetch query includes effective_range @> current_timestamp for SYSTEM-managed", "Non-SYSTEM-managed queries unchanged", "compileKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

---

### Task 2: Regenerate showcase UI and verify

**Goal:** Run `maiaGeneration` on the showcase to produce updated `LeftManyCrudService.kt` and `LeftManyFetchForEditDtoRowMapper.kt`, then verify the correct behavior is present in the generated output.

**Files:**
- Regenerated: `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt`
- Regenerated: `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyFetchForEditDtoRowMapper.kt`

**Acceptance Criteria:**
- [ ] `LeftManyCrudService.kt` contains `findEffectiveByLeft(id)` in the update path
- [ ] `LeftManyCrudService.kt` contains `effectiveTo(Instant.now())` soft-delete block
- [ ] `LeftManyCrudService.kt` contains `Instant.now()` for `effectiveFrom` in both create and update-add paths
- [ ] `LeftManyCrudService.kt` delete guard uses `findEffectiveByLeft(id).isNotEmpty()`
- [ ] `LeftManyFetchForEditDtoRowMapper.kt` `fetchRightEntitiesJoinFetchDtos` contains `and mtm.effective_range @> current_timestamp`
- [ ] `LeftManyFetchForEditDtoRowMapper.kt` `fetchRightEffectiveEntitiesJoinFetchDtos` does NOT contain the effective_range filter (it's USER-managed, should be unchanged)
- [ ] `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Run generation**

```bash
./gradlew :maia-showcase:maiaGeneration
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Verify LeftManyCrudService**

```bash
grep -n "findEffectiveByLeft\|effectiveTo(Instant\|effectiveFrom = Instant" maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt
```

Expected output (exact line numbers vary):
```
<line>:        val existingLeftToRightManyToManyJoinJoinsById = this.leftToRightManyToManyJoinRepo.findEffectiveByLeft(id).associateBy { it.id }
<line>:                    effectiveTo(Instant.now())
<line>:                effectiveFrom = Instant.now(),
<line>:                effectiveFrom = Instant.now(),
<line>:        if (this.leftToRightManyToManyJoinRepo.findEffectiveByLeft(id).isNotEmpty()) {
```

- [ ] **Step 3: Verify LeftManyFetchForEditDtoRowMapper**

```bash
grep -n "effective_range @> current_timestamp" maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyFetchForEditDtoRowMapper.kt
```

Expected: exactly 1 match (in `fetchRightEntitiesJoinFetchDtos`, not in `fetchRightEffectiveEntitiesJoinFetchDtos`).

- [ ] **Step 4: Compile test**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/service/src/generated/ maia-showcase/dao/src/generated/
git commit -m "regen: showcase after SYSTEM-managed effective-timestamp service fix"
```

```json:metadata
{"files": ["maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt", "maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyFetchForEditDtoRowMapper.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["LeftManyCrudService uses findEffectiveByLeft in update path", "LeftManyCrudService soft-deletes with effectiveTo(Instant.now())", "LeftManyCrudService uses Instant.now() for effectiveFrom on create and add", "LeftManyCrudService delete guard uses findEffectiveByLeft.isNotEmpty()", "LeftManyFetchForEditDtoRowMapper fetchRightEntitiesJoinFetchDtos has effective_range filter", "fetchRightEffectiveEntitiesJoinFetchDtos unchanged (USER-managed)", "compileTestKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

---

### Task 3: Add DB assertions to ManyToManyEffectiveRangeCrudPlaywrightTest

**Goal:** Add two `@Autowired` fields and two DB assertion blocks to the existing Playwright test, proving that `effectiveFrom` is set on join creation and `effectiveTo` is set on join removal.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `leftManyDao: LeftManyDao` and `leftToRightManyToManyJoinDao: LeftToRightManyToManyJoinDao` injected
- [ ] After create: assert `effectiveFrom` is between `beforeCreate` and `Instant.now()`, `effectiveTo` is null
- [ ] After remove + edit submit: assert `effectiveTo` is between `beforeRemove` and `Instant.now()`
- [ ] `leftId` declared once after create and reused in the edit step
- [ ] `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Verify:** `./gradlew :maia-showcase:app:compileTestKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add `@Autowired` fields**

After the existing `@Autowired private lateinit var rightManyTypeaheadEsIndex: RightManyTypeaheadEsIndex` field, add:

```kotlin
@Autowired
private lateinit var leftManyDao: LeftManyDao

@Autowired
private lateinit var leftToRightManyToManyJoinDao: LeftToRightManyToManyJoinDao
```

- [ ] **Step 2: Add `import java.time.Instant`**

Add to the import block (if not already present via a wildcard import):

```kotlin
import java.time.Instant
```

Also add AssertJ import if not already present:
```kotlin
import org.assertj.core.api.Assertions.assertThat
```

- [ ] **Step 3: Restructure the create section and add assertion**

The current create section is:
```kotlin
leftManyCreatePage.apply {
    assertOnPage()
    fillCreateForm()
    clickAddRightJoinEntityButton()
    searchAndSelectRightJoinEntityInMiniForm("right-gamma")
    clickConfirmAddRightJoinInMiniForm()
    clickSubmitButton()
}

leftManyViewPage.apply {
    assertOnPage()
    clickEditButton()
}
```

Replace with:
```kotlin
val beforeCreate = Instant.now()

leftManyCreatePage.apply {
    assertOnPage()
    fillCreateForm()
    clickAddRightJoinEntityButton()
    searchAndSelectRightJoinEntityInMiniForm("right-gamma")
    clickConfirmAddRightJoinInMiniForm()
    clickSubmitButton()
}

leftManyViewPage.assertOnPage()

val leftId = leftManyDao.findAllAsSequence().toList().single().id
val joinAfterCreate = leftToRightManyToManyJoinDao.findByLeft(leftId).single()
assertThat(joinAfterCreate.effectiveFrom).isBetween(beforeCreate, Instant.now())
assertThat(joinAfterCreate.effectiveTo).isNull()

leftManyViewPage.clickEditButton()
```

- [ ] **Step 4: Restructure the edit section and add assertion**

The current edit section is:
```kotlin
leftManyEditPage.apply {
    assertOnPage()
    assertRightJoinEntryVisible("right-gamma")
    removeRightJoinEntry("right-gamma")
    fillEditForm()
    clickSubmitButton()
}

leftManyViewPage.assertOnPage()
```

Replace with:
```kotlin
val beforeRemove = Instant.now()

leftManyEditPage.apply {
    assertOnPage()
    assertRightJoinEntryVisible("right-gamma")
    removeRightJoinEntry("right-gamma")
    fillEditForm()
    clickSubmitButton()
}

leftManyViewPage.assertOnPage()

val joinAfterRemove = leftToRightManyToManyJoinDao.findByLeft(leftId).single()
assertThat(joinAfterRemove.effectiveTo).isBetween(beforeRemove, Instant.now())
```

Note: `findByLeft` (not `findEffectiveByLeft`) is used here to retrieve the soft-deleted record. `leftId` was declared in Step 3 and is in scope.

- [ ] **Step 5: Compile**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt
git commit -m "test: add DB assertions for SYSTEM-managed effective timestamps in MtM Playwright test"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/ManyToManyEffectiveRangeCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:compileTestKotlin", "acceptanceCriteria": ["leftManyDao and leftToRightManyToManyJoinDao injected", "effectiveFrom assertion after create: isBetween(beforeCreate, now)", "effectiveTo assertion after remove: isBetween(beforeRemove, now)", "compileTestKotlin BUILD SUCCESSFUL"], "requiresUserVerification": false}
```

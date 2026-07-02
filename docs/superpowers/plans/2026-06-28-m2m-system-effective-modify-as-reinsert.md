# M2M System-Managed Effective Timestamps: Modify as Close+Reinsert

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a many-to-many join entity with system-managed effective timestamps has extra fields and a submitted record's extra fields differ from the stored record, close the old record and insert a new one instead of silently doing nothing.

**Architecture:** All changes are in the generator (`CrudServiceRenderer.kt`). The system-managed reconcile branch gains a third phase: a `close effectiveRange on modified` helper that detects changed extra fields, closes old records, and returns them; the insert helper is widened to also insert those replacements. The new phases are only generated when extra fields exist. Verification is via existing black-box integration tests plus two new ones.

**Tech Stack:** Kotlin, Spring Boot, JUnit 5 / `AbstractBlackBoxTest`, `MockMvcTester`, Testcontainers PostgreSQL

**User Verification:** NO

---

### Task 1: Write failing integration tests

**Goal:** Add two tests to `LeftCrudRightEntitiesTest` that document the expected close+reinsert behaviour; both fail before the generator change is made.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightEntitiesTest.kt`

**Acceptance Criteria:**
- [ ] Test `update with changed someInt closes old record and creates new record` exists and fails with current generated code
- [ ] Test `update with unchanged someInt preserves join record` exists and passes with current generated code (change-detection no-op baseline)

**Verify:** `./gradlew :maia-showcase:app:test --tests "*.LeftCrudRightEntitiesTest.update with changed someInt*" 2>&1 | tail -20` → FAILED

**Steps:**

- [ ] **Step 1: Add the two tests to `LeftCrudRightEntitiesTest`**

Insert the following two methods before the `private fun post(...)` method at the bottom of the class (after the existing `update with mixed submission...` test, around line 228):

```kotlin
    @Test
    fun `update with changed someInt closes old record and creates new record`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = manyToManyJoinDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 20}]}"""
        ).hasStatus(HttpStatus.OK)

        val allJoins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(allJoins).hasSize(2)

        val closedJoin = allJoins.single { it.id == joinBefore.id }
        assertThat(closedJoin.effectiveTo).isNotNull()

        val newJoin = allJoins.single { it.id != joinBefore.id }
        assertThat(newJoin.effectiveTo).isNull()
        assertThat(newJoin.someInt).isEqualTo(20)
        assertThat(newJoin.right).isEqualTo(rightEntity1.id)

    }


    @Test
    fun `update with unchanged someInt preserves join record`() {

        post(
            "/api/left-many/create",
            """{"someInt": 1, "someString": "test", "rightEntities": [{"rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.CREATED)

        val leftId = leftDao.findAllAsSequence().first().id
        val joinBefore = manyToManyJoinDao.findByLeft(leftId).single()

        put(
            "/api/left-many/update",
            """{"id": "$leftId", "someInt": 1, "someString": "test", "version": 1, "rightEntities": [{"id": "${joinBefore.id}", "rightEntityId": "${rightEntity1.id}", "someInt": 10}]}"""
        ).hasStatus(HttpStatus.OK)

        val allJoins = manyToManyJoinDao.findByLeft(leftId)
        assertThat(allJoins).hasSize(1)
        assertThat(allJoins.single().id).isEqualTo(joinBefore.id)
        assertThat(allJoins.single().effectiveTo).isNull()

    }
```

- [ ] **Step 2: Run the new failing test to confirm it fails for the right reason**

```
./gradlew :maia-showcase:app:test --tests "*.LeftCrudRightEntitiesTest.update with changed someInt*" 2>&1 | tail -30
```

Expected: FAILED — assertion error on `allJoins` size (expected 2, got 1), because the current generator does not close+reinsert on changed extra fields.

- [ ] **Step 3: Run the no-op test to confirm it already passes**

```
./gradlew :maia-showcase:app:test --tests "*.LeftCrudRightEntitiesTest.update with unchanged someInt*" 2>&1 | tail -20
```

Expected: PASSED

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightEntitiesTest.kt
git commit -m "test: add failing tests for M2M system-effective modify as close+reinsert"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightEntitiesTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests '*.LeftCrudRightEntitiesTest.update with changed someInt*' 2>&1 | tail -30", "acceptanceCriteria": ["new failing test exists and fails for the right reason", "no-op test passes"], "requiresUserVerification": false}
```

---

### Task 2: Modify CrudServiceRenderer to generate three-phase reconcile

**Goal:** Replace the two-helper system-managed reconcile block in `CrudServiceRenderer` with a three-helper block that detects changed extra fields and closes+reinserts the affected records.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt:631-691`

**Acceptance Criteria:**
- [ ] When `extraReconcileArgs` is non-empty, the reconcile function calls three helpers: close-removed, close-modified (returns list), insert-added-and-replaced
- [ ] When `extraReconcileArgs` is empty, the reconcile function still calls two helpers with unchanged signatures (preserves `LeftToRightSystemEffective` behaviour)
- [ ] `close effectiveRange on modified` fetches `findEffectiveBy${thisSideFieldNameCapitalized}`, compares extra fields, closes changed records, returns their DTOs
- [ ] `insert added and replaced` inserts both `id == null` records and `modifiedDtos` with `effectiveFrom = Instant.now()`
- [ ] Invalid submitted id throws `maiaProblems.joinRecordNotFound`

**Verify:** After running maiaGeneration (Task 3), the generated `LeftManyCrudService.kt` contains `close effectiveRange on modified leftToRightManyToManyJoin entities` and `insert added and replaced leftToRightManyToManyJoin entities`.

**Steps:**

- [ ] **Step 1: Replace the `isSystemManaged` block (lines 631–691)**

In `CrudServiceRenderer.kt`, find the `if (isSystemManaged) {` block starting at line 631 and replace it entirely with:

```kotlin
                if (isSystemManaged) {

                    val joinNamePrefixLower = joinNamePrefix.replaceFirstChar { it.lowercaseChar() }
                    val closeRemovedMethodName = "close effectiveRange on removed $joinNamePrefixLower entities"
                    val hasExtraFields = extraReconcileArgs.isNotEmpty()
                    val closeModifiedMethodName = "close effectiveRange on modified $joinNamePrefixLower entities"
                    val insertMethodName = if (hasExtraFields) "insert added and replaced $joinNamePrefixLower entities" else "insert added $joinNamePrefixLower entities"

                    if (hasExtraFields) {
                        append("""
                            |
                            |
                            |    private fun `reconcile $joinNamePrefix joins`(
                            |        id: DomainId,
                            |        submitted: List<${joinDtoDef.uqcn}>
                            |    ) {
                            |
                            |        `$closeRemovedMethodName`(id, submitted)
                            |
                            |        val modifiedDtos = `$closeModifiedMethodName`(id, submitted)
                            |
                            |        `$insertMethodName`(id, submitted, modifiedDtos)
                            |
                            |    }
                            |""".trimMargin())
                    } else {
                        append("""
                            |
                            |
                            |    private fun `reconcile $joinNamePrefix joins`(
                            |        id: DomainId,
                            |        submitted: List<${joinDtoDef.uqcn}>
                            |    ) {
                            |
                            |        `$closeRemovedMethodName`(id, submitted)
                            |
                            |        `$insertMethodName`(id, submitted)
                            |
                            |    }
                            |""".trimMargin())
                    }

                    append("""
                        |
                        |
                        |    private fun `$closeRemovedMethodName`(
                        |        id: DomainId,
                        |        submitted: List<${joinDtoDef.uqcn}>
                        |    ) {
                        |
                        |        val existingById = this.${joinRepoFieldName}.${findByMethodName}(id).associateBy { it.id }
                        |        val submittedIds = submitted.mapNotNull { it.id }.toSet()
                        |
                        |        existingById.keys.filterNot { it in submittedIds }.forEach {
                        |            this.${joinRepoFieldName}.closeEffectiveRange(it)
                        |        }
                        |
                        |    }
                        |""".trimMargin())

                    if (hasExtraFields) {
                        val changeDetection = extraReconcileArgs.joinToString(" ||\n            ") { (fieldName, _) -> "existing.$fieldName != joinDto.$fieldName" }
                        append("""
                            |
                            |
                            |    private fun `$closeModifiedMethodName`(
                            |        id: DomainId,
                            |        submitted: List<${joinDtoDef.uqcn}>
                            |    ): List<${joinDtoDef.uqcn}> {
                            |
                            |        val existingById = this.${joinRepoFieldName}.${findByMethodName}(id).associateBy { it.id }
                            |
                            |        return submitted.filter { it.id != null }.filter { joinDto ->
                            |            val existing = existingById[joinDto.id!!]
                            |                ?: throw this.maiaProblems.joinRecordNotFound("$joinEntityClass")
                            |            $changeDetection
                            |        }.onEach { this.${joinRepoFieldName}.closeEffectiveRange(it.id!!) }
                            |
                            |    }
                            |""".trimMargin())
                    }

                    if (hasExtraFields) {
                        append("""
                            |
                            |
                            |    private fun `$insertMethodName`(
                            |        id: DomainId,
                            |        submitted: List<${joinDtoDef.uqcn}>,
                            |        modifiedDtos: List<${joinDtoDef.uqcn}>
                            |    ) {
                            |
                            |        val newJoins = (submitted.filter { it.id == null } + modifiedDtos).map { joinDto ->
                            |            ${joinEntityClass}.newInstance(
                            |""".trimMargin())
                    } else {
                        append("""
                            |
                            |
                            |    private fun `$insertMethodName`(
                            |        id: DomainId,
                            |        submitted: List<${joinDtoDef.uqcn}>
                            |    ) {
                            |
                            |        val newJoins = submitted.filter { it.id == null }.map { joinDto ->
                            |            ${joinEntityClass}.newInstance(
                            |""".trimMargin())
                    }

                    renderNewInstanceArgsMultiLine(indentSize = 16, newInstanceArgs)

                    append("""
                        |            )
                        |        }
                        |
                        |        this.${joinRepoFieldName}.bulkInsert(newJoins)
                        |
                        |    }
                        |""".trimMargin())

                }
```

The block to replace starts at the line containing `if (isSystemManaged) {` (line 631) and ends at the closing `}` at line 691 (just before the `} else {` that opens the user-managed branch at line 693).

- [ ] **Step 2: Compile the generator module**

```
./gradlew :maia-gen:maia-gen-generator:compileKotlin 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt
git commit -m "feat: generate three-phase reconcile for system-managed M2M joins with extra fields"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin 2>&1 | tail -20", "acceptanceCriteria": ["generator compiles", "isSystemManaged block produces three helpers when extraReconcileArgs non-empty", "isSystemManaged block produces two helpers when extraReconcileArgs empty"], "requiresUserVerification": false}
```

---

### Task 3: Regenerate showcase and verify all tests pass

**Goal:** Regenerate `LeftManyCrudService.kt` from the updated generator and confirm all existing and new tests pass.

**Files:**
- Modify (generated): `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt`

**Acceptance Criteria:**
- [ ] Generated `LeftManyCrudService.kt` contains `close effectiveRange on modified leftToRightManyToManyJoin entities`
- [ ] Generated `LeftManyCrudService.kt` contains `insert added and replaced leftToRightManyToManyJoin entities`
- [ ] Generated `LeftManyCrudService.kt` does NOT contain `close effectiveRange on modified leftToRightSystemEffective entities` (no extra fields on that join)
- [ ] All tests in `LeftCrudRightEntitiesTest` pass

**Verify:** `./gradlew :maia-showcase:app:test --tests "*.LeftCrudRightEntitiesTest" 2>&1 | tail -30` → BUILD SUCCESSFUL, all tests PASSED

**Steps:**

- [ ] **Step 1: Regenerate the showcase service module**

```
./gradlew :maia-showcase:service:maiaGeneration 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify the generated service contains the new helpers**

```
grep -n "close effectiveRange on modified\|insert added and replaced\|close effectiveRange on removed" \
  maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt
```

Expected output (line numbers will vary):
```
NNN:    private fun `close effectiveRange on removed leftToRightManyToManyJoin entities`(
NNN:    private fun `close effectiveRange on modified leftToRightManyToManyJoin entities`(
NNN:    private fun `insert added and replaced leftToRightManyToManyJoin entities`(
NNN:    private fun `close effectiveRange on removed leftToRightSystemEffective entities`(
NNN:    private fun `insert added leftToRightSystemEffective entities`(
```

(`leftToRightSystemEffective` must NOT have a `close effectiveRange on modified` line — it has no extra fields.)

- [ ] **Step 3: Run the full LeftCrudRightEntitiesTest suite**

```
./gradlew :maia-showcase:app:test --tests "*.LeftCrudRightEntitiesTest" 2>&1 | tail -40
```

Expected: BUILD SUCCESSFUL — all tests pass, including the two new ones.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt
git commit -m "regen: regenerate LeftManyCrudService with three-phase M2M reconcile"
```

```json:metadata
{"files": ["maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftManyCrudService.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests '*.LeftCrudRightEntitiesTest' 2>&1 | tail -40", "acceptanceCriteria": ["generated service contains close effectiveRange on modified helper", "generated service contains insert added and replaced helper", "leftToRightSystemEffective join does not get the new helpers", "all LeftCrudRightEntitiesTest tests pass"], "requiresUserVerification": false}
```

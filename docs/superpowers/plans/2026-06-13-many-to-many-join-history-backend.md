# Many-to-Many Join History (Backend) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Support `recordVersionHistory = true` on `manyToManyEntity()` for non-effective-timestamp joins — generates a history table + DAO + global (non-entity-scoped) REST search/count endpoints recording CREATE/DELETE rows when join pairs are added/removed.

**Architecture:** Reuses the existing generic `historyEntityDef`/`bulkInsertHistory`/`insertHistory` machinery in `JdbcDaoRenderer` (already triggered by `withVersionHistory.value`, already called from `bulkInsert`/`deleteByPrimaryKey`). New work: (1) a `isManyToManyJoinEntity` flag on `EntityDef` so downstream renderers know this history is for a join, not a single entity; (2) spec-time validation rejecting `recordVersionHistory` on effective-timestamp joins; (3) `EntityHistoryBlotterDef` column/path adjustments for join-derived history (show FK columns, drop the `{entityId}` scoping); (4) matching signature changes in the 4 history-blotter REST chain renderers (Dao/Repo/SearchService/Endpoint) to drop the `entityId` filter for join history.

**Tech Stack:** Kotlin code generator (maia-gen-spec, maia-gen-generator), regenerated Spring/JDBC backend code in maia-showcase.

**User Verification:** NO — no user verification required (Angular UI is a separate follow-up plan).

---

### Task 1: Add `isManyToManyJoinEntity` flag + spec-time validation

**Goal:** `EntityDef` knows whether it was built via `manyToManyEntity()`, and `manyToManyEntity(recordVersionHistory = true, ...)` rejects effective-timestamp joins.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt:44-84` (constructor), `:496-527` (historyEntityDef construction)
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityDefBuilder.kt:49-64` (constructor), `:136-168` (`build()`)
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt:476-556` (`manyToManyEntity`)

**Acceptance Criteria:**
- [ ] `EntityDef` has `val isManyToManyJoinEntity: Boolean = false`
- [ ] `manyToManyEntity()` builds entities with `isManyToManyJoinEntity = true`
- [ ] `manyToManyEntity(recordVersionHistory = true, ...)` on a join with `withEffectiveTimestamps()` throws `ModelDefinitionException`
- [ ] `:maia-gen:maia-gen-spec:compileKotlin` and `:maia-gen:maia-gen-generator:compileKotlin` succeed

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add the flag to `EntityDef`'s constructor**

In `EntityDef.kt:44-84`, add a new trailing constructor param (after `angularFormSystem`):

```kotlin
    val cacheableDef: CacheableDef?,
    val angularFormSystem: AngularFormSystem,
    val isManyToManyJoinEntity: Boolean = false
) {
```

- [ ] **Step 2: Propagate the flag onto `historyEntityDef`**

In `EntityDef.kt`, the `historyEntityDef` computed property (around line 496) builds a nested `EntityDef(...)`. Add `isManyToManyJoinEntity = this.isManyToManyJoinEntity` as a new named arg at the end of that constructor call (after `withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,`):

```kotlin
            withHandCodedDao = WithHandCodedDao.FALSE,
            withHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
            isManyToManyJoinEntity = this.isManyToManyJoinEntity,
        )
```

- [ ] **Step 3: Thread the flag through `EntityDefBuilder`**

In `EntityDefBuilder.kt:49-64`, add a new trailing constructor param with default `false`:

```kotlin
class EntityDefBuilder(
    private val packageName: PackageName,
    val entityBaseName: EntityBaseName,
    private val isDeltaEntity: IsDeltaEntity,
    private val defaultFieldTypeFieldReaderProvider: (FieldType) -> ParameterizedType?,
    private val defaultFieldTypeFieldWriterProvider: (FieldType) -> ParameterizedType?,
    private val withVersionHistory: WithVersionHistory,
    private val versioned: Versioned,
    private val deletable: Deletable = Deletable.FALSE,
    private val allowDeleteAll: AllowDeleteAll,
    private val allowFindAll: AllowFindAll,
    private val nameFieldForPkAndNameDto: String?,
    private val withHandcodedDao: WithHandCodedDao = WithHandCodedDao.FALSE,
    private val withHandcodedEntityDao: WithHandCodedEntityDao = WithHandCodedEntityDao.FALSE,
    defaultSchemaName: SchemaName,
    private val isManyToManyJoinEntity: Boolean = false
) {
```

In `build()` (around line 136-168), add the new named arg to the `EntityDef(...)` call, after `this.angularFormSystem`:

```kotlin
            this.cacheableDef,
            this.angularFormSystem,
            isManyToManyJoinEntity = this.isManyToManyJoinEntity
        )
```

- [ ] **Step 4: Pass `isManyToManyJoinEntity = true` from `manyToManyEntity()`**

In `AbstractSpec.kt`, the `EntityDefBuilder(...)` call inside `manyToManyEntity()` (around line 492-507) is fully positional with 14 args ending in `defaultSchemaName`. Add a new named arg after it:

```kotlin
        val builder = EntityDefBuilder(
            PackageName(packageName),
            EntityBaseName(entityBaseName),
            IsDeltaEntity(value = false),
            lookupFieldReaderByFieldType,
            lookupFieldWriterByFieldType,
            WithVersionHistory(recordVersionHistory),
            Versioned(versioned || recordVersionHistory),
            deletable,
            allowDeleteAll,
            allowFindAll,
            pkAndNameFieldName,
            withHandcodedDao,
            withHandCodedEntityDao,
            defaultSchemaName,
            isManyToManyJoinEntity = true
        )
```

- [ ] **Step 5: Add validation after `build()`**

In `AbstractSpec.kt`, right after `val entityDef = builder.build()` (around line 538), before `entityDefs.add(entityDef)`:

```kotlin
        val entityDef = builder.build()

        if (entityDef.withVersionHistory.value && entityDef.hasEffectiveTimestamps.value) {
            throw ModelDefinitionException(
                "manyToManyEntity '$entityBaseName': recordVersionHistory is not supported for joins with effective timestamps"
            )
        }

        entityDefs.add(entityDef)
```

`ModelDefinitionException` is in the same package (`org.maiaframework.gen.spec.definition`) as `AbstractSpec`'s other definition imports — no new import needed (check existing imports in `AbstractSpec.kt`; if `ModelDefinitionException` isn't imported, add `import org.maiaframework.gen.spec.definition.ModelDefinitionException`).

- [ ] **Step 6: Compile and commit**

Run: `./gradlew :maia-gen:maia-gen-spec:compileKotlin :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt \
        maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityDefBuilder.kt \
        maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/AbstractSpec.kt
git commit -m "feat: add isManyToManyJoinEntity flag and recordVersionHistory validation for joins"
```

---

### Task 2: `EntityHistoryBlotterDef` column + path rules for join history

**Goal:** When the source entity is a many-to-many join, the history blotter shows the FK columns (left/right entity ids) plus `createdTimestampUtc`/`changeType`/`createdBy`, and exposes global (non-`{entityId}`-scoped) endpoint/route paths.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt`

**Acceptance Criteria:**
- [ ] `blotterColumns` for a join-entity history includes the FK fields and `createdTimestampUtc`, excludes `id` and `version`
- [ ] `blotterColumns` for a regular entity history is unchanged (still excludes FK/id/createdTimestampUtc)
- [ ] `searchEndpointPath`/`countEndpointPath`/`routePath` for join-entity history have no `{entityId}`/`:id` segment
- [ ] `:maia-gen:maia-gen-generator:compileKotlin` succeeds

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add a join-history flag and branch `blotterColumns`**

In `EntityHistoryBlotterDef.kt`, add a property and rewrite `blotterColumns` (currently lines 28-36):

```kotlin
    val isJoinEntityHistory: Boolean = entityDef.isManyToManyJoinEntity


    val blotterColumns: List<EntityFieldDef> = historyEntityDef.allEntityFieldsSorted.filter { fieldDef ->
        val name = fieldDef.classFieldDef.classFieldName.value
        val isFK = fieldDef.classFieldDef.fieldType is ForeignKeyFieldType
        val isEntityId = name == "id"
        val isVersion = name == "version"
        val isCreatedTimestamp = name == "createdTimestampUtc"

        if (isJoinEntityHistory) {
            !isEntityId && !isVersion
        } else {
            !isFK && !isEntityId && !isCreatedTimestamp
        }

    }
```

(`changeType` and `createdBy`/`createdById` are not filtered out by either branch, so they remain in `blotterColumns` for join history — same as they already do for regular entity history.)

- [ ] **Step 2: Global endpoint/route paths for join history**

Replace lines 109-118:

```kotlin
    val searchEndpointPath = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/search"
    } else {
        "/api/${entityKebab}/{entityId}/history/search"
    }


    val countEndpointPath = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/count"
    } else {
        "/api/${entityKebab}/{entityId}/history/count"
    }


    val pageTitle = "${entityDef.entityBaseName.value} History"


    val routePath = if (isJoinEntityHistory) {
        "${entityKebab}-history"
    } else {
        "${entityKebab}/history"
    }
```

- [ ] **Step 3: Update `searchEndpointUrlForTypescript` for join history**

Line 136 currently hardcodes `${this.entityId}` into the URL. Replace with:

```kotlin
    val searchEndpointUrlForTypescript = if (isJoinEntityHistory) {
        "/api/${entityKebab}/history/search"
    } else {
        $$"/api/$${entityKebab}/${this.entityId}/history/search"
    }
```

- [ ] **Step 4: Compile and commit**

Run: `./gradlew :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt
git commit -m "feat: join-aware blotter columns and global routes for join entity history"
```

---

### Task 3: Drop `entityId` from the history search/count REST chain for join history

**Goal:** For join-entity history, `search`/`count` work globally — no `entityId: DomainId` parameter, no `id = :entityId` filter — across Dao → Repo → SearchService → Endpoint.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoDaoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRepoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchServiceRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchEndpointRenderer.kt`

**Acceptance Criteria:**
- [ ] For `def.isJoinEntityHistory == true`, generated `search`/`count` functions in all 4 layers take no `entityId` parameter and apply no `id = :entityId` filter
- [ ] For `def.isJoinEntityHistory == false`, generated code is unchanged (entityId param + filter retained)
- [ ] `:maia-gen:maia-gen-generator:compileKotlin` succeeds

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: `EntityHistoryBlotterRowDtoDaoRenderer.kt` — search/count**

Replace `render function search` (lines 52-108) and `render function count` (lines 111-140) bodies. For `search`:

```kotlin
    private fun `render function search`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val table = def.historyTableSchemaAndTable
        val selectColumns = def.blotterColumns
            .sortedBy { it.classFieldDef.classFieldName.value }
            .joinToString(",\n                ") { col ->
                "$table.${col.tableColumnName.value} as ${col.classFieldDef.classFieldName.value}"
            }

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"

        append("""
            |
            |
            |    fun search($params): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        val sqlParams = SqlParams()
            |""".trimMargin())

        if (!def.isJoinEntityHistory) {
            append("""
                |        sqlParams.addValue("entityId", entityId)
                |        val entityIdFilter = "$table.id = :entityId"
                |""".trimMargin())
        }

        val whereClauseArgs = if (def.isJoinEntityHistory) "searchModel.filterModel, sqlParams" else "searchModel.filterModel, sqlParams, entityIdFilter"

        append("""
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor($whereClauseArgs)
            |        val offsetAndLimitClause = this.searchModelConverter.buildOffsetAndLimitFor(searchModel)
            |        val orderByClause = this.searchModelConverter.buildOrderByClause(searchModel)
            |
            |        val sqlForTotalCount = ${"\"\"\""}
            |            select count(*)
            |            from $table
            |            ${"$"}whereClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        val sqlForPage = ${"\"\"\""}
            |            select
            |                $selectColumns
            |            from $table
            |            ${"$"}whereClause
            |            ${"$"}orderByClause
            |            ${"$"}offsetAndLimitClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        val totalResultCount = this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |        val results = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)
            |        val endRow = searchModel.endRow
            |        val limit = if (endRow == null) null else (endRow - searchModel.startRow)
            |
            |        return SearchResultPage(
            |            results,
            |            totalResultCount,
            |            searchModel.startRow,
            |            limit
            |        )
            |
            |    }
            |""".trimMargin())

    }
```

For `count`, the same pattern (drop `entityId` param + filter when `def.isJoinEntityHistory`):

```kotlin
    private fun `render function count`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SQL_PARAMS)

        val table = def.historyTableSchemaAndTable
        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"

        append("""
            |
            |
            |    fun count($params): Long {
            |
            |        val sqlParams = SqlParams()
            |""".trimMargin())

        if (!def.isJoinEntityHistory) {
            append("""
                |        sqlParams.addValue("entityId", entityId)
                |        val entityIdFilter = "$table.id = :entityId"
                |""".trimMargin())
        }

        val whereClauseArgs = if (def.isJoinEntityHistory) "searchModel.filterModel, sqlParams" else "searchModel.filterModel, sqlParams, entityIdFilter"

        append("""
            |        val whereClause = this.searchModelConverter.buildWhereClauseFor($whereClauseArgs)
            |
            |        val sqlForTotalCount = ${"\"\"\""}
            |            select count(*)
            |            from $table
            |            ${"$"}whereClause
            |            ${"\"\"\""}.trimIndent()
            |
            |        return this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |
            |    }
            |""".trimMargin())

    }
```

- [ ] **Step 2: `EntityHistoryBlotterRowDtoRepoRenderer.kt` — getRows/countRows**

Replace both functions:

```kotlin
    private fun `render function getRows`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION)

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun getRows($params): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        try {
            |            return this.dao.search($args)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function countRows`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCHABLE_EXCEPTION)

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun countRows($params): Long {
            |
            |        try {
            |            return this.dao.count($args)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |""".trimMargin())

    }
```

- [ ] **Step 3: `EntityHistoryBlotterSearchServiceRenderer.kt` — search/count**

```kotlin
    private fun `render function search`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun search($params): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.repo.getRows($args)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        val params = if (def.isJoinEntityHistory) "searchModel: AgGridSearchModel" else "entityId: DomainId, searchModel: AgGridSearchModel"
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    fun count($params): Long {
            |
            |        return this.repo.countRows($args)
            |
            |    }
            |""".trimMargin())

    }
```

- [ ] **Step 4: `EntityHistoryBlotterSearchEndpointRenderer.kt` — search/count**

```kotlin
    private fun `render function search`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_MEDIA_TYPE)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)
        addImportFor(Fqcns.MAIA_SEARCH_RESULT_PAGE)

        val params = if (def.isJoinEntityHistory) {
            "@RequestBody searchModel: AgGridSearchModel"
        } else {
            "@PathVariable entityId: DomainId,\n        @RequestBody searchModel: AgGridSearchModel"
        }
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    @PostMapping("${def.searchEndpointPath}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun search(
            |        $params
            |    ): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.searchService.search($args)
            |
            |    }
            |""".trimMargin())

    }


    private fun `render function count`() {

        addImportFor(Fqcns.SPRING_POST_MAPPING)
        addImportFor(Fqcns.SPRING_PATH_VARIABLE)
        addImportFor(Fqcns.SPRING_REQUEST_BODY)
        addImportFor(Fqcns.MAIA_DOMAIN_ID)
        addImportFor(Fqcns.MAIA_AG_GRID_SEARCH_MODEL)

        val params = if (def.isJoinEntityHistory) {
            "@RequestBody searchModel: AgGridSearchModel"
        } else {
            "@PathVariable entityId: DomainId,\n        @RequestBody searchModel: AgGridSearchModel"
        }
        val args = if (def.isJoinEntityHistory) "searchModel" else "entityId, searchModel"

        append("""
            |
            |
            |    @PostMapping("${def.countEndpointPath}")
            |    fun count(
            |        $params
            |    ): Long {
            |
            |        return this.searchService.count($args)
            |
            |    }
            |""".trimMargin())

    }
```

- [ ] **Step 5: Compile and commit**

Run: `./gradlew :maia-gen:maia-gen-generator:compileKotlin`
Expected: BUILD SUCCESSFUL

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoDaoRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRepoRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchServiceRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchEndpointRenderer.kt
git commit -m "feat: global search/count REST chain for join entity history"
```

---

### Task 4: Enable `recordVersionHistory` on `LeftToRightSimpleJoin` and regenerate

**Goal:** Showcase fixture exercises the new feature end to end (history table, DAO, blotter REST endpoint generated for `LeftToRightSimpleJoinEntity`).

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt:1372-1389` (`leftToRightSimpleJoinEntityDef`)
- Regenerate: `maia-showcase/domain`, `maia-showcase/dao`, `maia-showcase/repo`, `maia-showcase/service`, `maia-showcase/web` (`src/generated/`)

**Acceptance Criteria:**
- [ ] `leftToRightSimpleJoinEntityDef` is built with `recordVersionHistory = true`
- [ ] Regeneration produces `LeftToRightSimpleJoinHistoryEntity`, `LeftToRightSimpleJoinHistoryDao`, and history-blotter Dao/Repo/SearchService/Endpoint classes with no `entityId` param (per Task 3)
- [ ] `./gradlew :maia-showcase:web:build` succeeds (generated code compiles)

**Verify:** `./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration :maia-showcase:service:maiaGeneration :maia-showcase:web:maiaGeneration :maia-showcase:web:build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Flip the flag in the spec**

In `MaiaShowcaseSpec.kt`, change the `leftToRightSimpleJoinEntityDef` definition (around line 1372-1389):

```kotlin
    val leftToRightSimpleJoinEntityDef = manyToManyEntity(
        "org.maiaframework.showcase.many_to_many",
        "LeftToRightSimpleJoin",
        recordVersionHistory = true,
        deletable = Deletable.TRUE,
        allowDeleteAll = AllowDeleteAll.TRUE,
        leftEntity = ReferencedEntity(
            fieldName = "left",
            displayName = "Left",
            leftManyEntityDef,
            IsEditableByUser.TRUE
        ),
        rightEntity = ReferencedEntity(
            fieldName = "right",
            displayName = "Right",
            rightManyEntityDef,
            IsEditableByUser.TRUE
        )
    )
```

- [ ] **Step 2: Regenerate and build**

Run: `./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:repo:maiaGeneration :maia-showcase:service:maiaGeneration :maia-showcase:web:maiaGeneration`
Expected: BUILD SUCCESSFUL, and `maia-showcase/dao/src/generated/.../many_to_many/LeftToRightSimpleJoinHistoryDao.kt` exists.

Run: `./gradlew :maia-showcase:web:build`
Expected: BUILD SUCCESSFUL (generated code compiles, including the join-history blotter endpoint).

- [ ] **Step 3: Commit spec change + generated code**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt \
        maia-showcase/domain/src/generated maia-showcase/dao/src/generated \
        maia-showcase/repo/src/generated maia-showcase/service/src/generated maia-showcase/web/src/generated
git commit -m "feat: enable recordVersionHistory on LeftToRightSimpleJoin fixture"
```

---

### Task 5: Integration test — history rows written on add/remove, queryable via REST

**Goal:** Verify that adding/removing `LeftToRightSimpleJoin` pairs writes `CREATE`/`DELETE` history rows, and the global history search endpoint returns them.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightSimpleJoinTest.kt`

**Acceptance Criteria:**
- [ ] Test adds a new join pair via the edit-form update path, then asserts `LeftToRightSimpleJoinHistoryDao` has a `CREATE` row for it
- [ ] Test removes an existing join pair, then asserts `LeftToRightSimpleJoinHistoryDao` has a `DELETE` row for it
- [ ] Test calls the global history search endpoint (`POST /api/left-to-right-simple-join/history/search`) and asserts both rows are returned

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftCrudRightSimpleJoinTest"` → BUILD SUCCESSFUL, all tests pass

**Steps:**

- [ ] **Step 1: Read the existing test**

Open `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightSimpleJoinTest.kt` to see the existing `@BeforeEach` setup (left/right entity fixtures) and the existing set-diff update test, so the new test reuses the same fixtures (`leftEntity`, `rightEntity1/2/3`).

- [ ] **Step 2: Add `LeftToRightSimpleJoinHistoryDao` autowire**

```kotlin
    @Autowired
    private lateinit var joinHistoryDao: LeftToRightSimpleJoinHistoryDao
```

- [ ] **Step 3: Write the history test**

```kotlin
    @Test
    @WithMockUser(authorities = ["WRITE"])
    fun `adding and removing a join pair writes CREATE and DELETE history rows`() {

        // start with rightEntity1 joined to leftEntity (set up via existing edit-form update,
        // following the same pattern as the existing set-diff test in this file)
        crudService.update(leftEntity.id, editDtoWithRightEntities(listOf(rightEntity1.id)))

        val joinAfterCreate = joinDao.findByLeft(leftEntity.id).single { it.right == rightEntity1.id }

        val createHistory = joinHistoryDao.findAll().filter { it.id == joinAfterCreate.id && it.changeType == ChangeType.CREATE }
        assertThat(createHistory).hasSize(1)
        assertThat(createHistory.single().right).isEqualTo(rightEntity1.id)

        // now remove rightEntity1, add rightEntity2
        crudService.update(leftEntity.id, editDtoWithRightEntities(listOf(rightEntity2.id)))

        val deleteHistory = joinHistoryDao.findAll().filter { it.id == joinAfterCreate.id && it.changeType == ChangeType.DELETE }
        assertThat(deleteHistory).hasSize(1)
        assertThat(deleteHistory.single().right).isEqualTo(rightEntity1.id)

    }
```

`editDtoWithRightEntities(...)` should match whatever helper/DTO-construction the existing set-diff test in this file already uses to call `crudService.update` with a list of right-entity ids — reuse that helper rather than duplicating DTO construction.

- [ ] **Step 4: Write the REST search test**

```kotlin
    @Test
    @WithMockUser(authorities = ["READ"])
    fun `global history search endpoint returns join history rows`() {

        crudService.update(leftEntity.id, editDtoWithRightEntities(listOf(rightEntity1.id)))
        crudService.update(leftEntity.id, editDtoWithRightEntities(emptyList()))

        val searchModel = AgGridSearchModel(startRow = 0, endRow = 100, filterModel = jsonMapper.createObjectNode(), sortModel = emptyList())

        val result = mockMvc.post("/api/left-to-right-simple-join/history/search") {
            contentType = MediaType.APPLICATION_JSON
            content = jsonMapper.writeValueAsString(searchModel)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val page = jsonMapper.readValue(result.response.contentAsString, SearchResultPage::class.java)
        assertThat(page.totalResultCount).isGreaterThanOrEqualTo(2)

    }
```

Adapt `mockMvc`/`jsonMapper` field names and `AgGridSearchModel` constructor args to match whatever existing showcase tests use for posting to other search endpoints — grep for an existing `*BlotterSearchEndpoint` test (e.g. for `LeftToRightSimpleJoin`'s own blotter, or any `allowFindAll` entity) and mirror its request-building style exactly.

- [ ] **Step 5: Run and commit**

Run: `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftCrudRightSimpleJoinTest"`
Expected: BUILD SUCCESSFUL

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftCrudRightSimpleJoinTest.kt
git commit -m "test: verify join history rows written on add/remove and queryable via REST"
```

---

## Unresolved Questions

- Step 3/4 of Task 5 reference helper names (`editDtoWithRightEntities`, `mockMvc`, `jsonMapper`) that must be confirmed against the actual existing test file content when the task is executed — the existing `LeftCrudRightSimpleJoinTest` and a sibling blotter-search test should be read first to get exact names/signatures.
- Angular UI (global history blotter page, routing, nav) is deliberately out of scope here — follow-up plan once this backend plan is verified.

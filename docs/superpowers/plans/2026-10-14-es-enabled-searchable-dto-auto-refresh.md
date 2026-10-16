# ES-Enabled SearchableDto + Automatic EsDoc Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A `searchableDto` can be marked elasticsearch-enabled; the generator then emits an EsDoc, index service and blotter wiring for it, and keeps the index fresh whenever any contributing entity field changes.

**Architecture:** `SearchableDtoDefBuilder.elasticsearchEnabled()` derives one `EsDocDef` per version from the DTO's `SimpleSearchableDtoFieldDef`s (root entity, FK `FieldPath`s, m2m joins). A generated index service rebuilds docs by id via the DTO's own DAO query. `CrudListener.onXUpdated` becomes `(before, after)`; a generated per-(EsDoc, contributing entity) listener diffs contributing fields, runs a generated reverse-join query to find root ids, and enlists them in a transaction-scoped `EsRefreshCollector` that dedupes and bulk-upserts in chunks after commit. Existing `refreshIndex` job stays as the safety net.

**Tech Stack:** Kotlin 21, Spring Boot 4 (`TransactionSynchronization`), Maia generator (renderers in `maia-gen-generator`), Elasticsearch Java client, JUnit 5, Testcontainers (ES).

**User Verification:** NO — no human sign-off required; verification is automated tests.

**Decisions this plan implements:** field-level granularity; reuse CrudNotifier/CrudListener; update event = before+after entities (always loaded); EsDoc derived from searchableDto, FK paths any depth + m2m; after-commit same thread, errors logged, `refreshIndex` is safety net; dedupe + chunked `bulkUpsert`; contributing-entity delete = capture root ids before delete, re-evaluate after commit (missing from query → delete doc); auto-refresh on by default with opt-out; all live EsDoc versions refreshed; `esDoc{}` DSL EsDocs must declare `autoRefresh` explicitly; showcase integration test against real ES.

**Run generator tests:** `./gradlew :maia-gen:maia-gen-spec:test :maia-gen:maia-gen-generator:test`
**Regenerate a module:** `./gradlew :<module>:maiaGeneration` (never hand-edit `src/generated/`).

---

## File Structure

| File | Responsibility |
|---|---|
| `maia-gen/maia-gen-spec/.../builders/SearchableDtoDefBuilder.kt` | `elasticsearchEnabled(version, autoRefresh)` DSL |
| `maia-gen/maia-gen-spec/.../SearchableDtoDef.kt` | holds derived `esDocDefs`, `esRefreshDependencies` |
| `maia-gen/maia-gen-spec/.../SearchableDtoEsDocFactory.kt` (new) | builds `EsDocDef` from a searchable DTO; default mapping types |
| `maia-gen/maia-gen-spec/.../EsRefreshDependencyDef.kt` (new) | contributing-entity → reverse path + watched fields |
| `maia-gen/maia-gen-spec/.../AbstractSpec.kt` | `blotter(searchableDtoDef)` picks ES source; registers derived EsDocs |
| `libs/maia-elasticsearch-parent/maia-elasticsearch/.../refresh/EsRefreshTarget.kt`, `EsRefreshCollector.kt` (new) | tx-scoped dedupe + after-commit chunked flush |
| `maia-gen/maia-gen-generator/.../renderers/SearchableDtoJdbcDaoRenderer.kt` | `findByIds`, `findAllAsSequence`, reverse-join `findRootIdsBy*` |
| `.../renderers/SearchableDtoRepoRenderer.kt` | passthroughs |
| `.../renderers/SearchableDtoEsIndexServiceRenderer.kt` (new) | generated `<Dto>V<n>IndexService` |
| `.../renderers/SearchableDtoEsDocCrudListenerRenderer.kt` (new) | generated listeners |
| `.../renderers/CrudListenerRenderer.kt`, `CrudNotifierRenderer.kt`, `CrudServiceRenderer.kt`, `TypeaheadCrudListenerRenderer.kt` | `(before, after)` + `BeforeDelete` |
| `.../generator/ElasticServiceModuleGenerator.kt`, `DomainModuleGenerator.kt`, `RepoLayerModuleGenerator.kt` | wire renderers |
| `maia-showcase/spec/.../MaiaShowcaseSpec.kt` | enable ES on a DTO with an FK path |
| `maia-showcase/app/src/test/.../es/EsDocAutoRefreshIntegrationTest.kt` (new) | end-to-end scenarios |

---

### Task 1: Transaction-scoped `EsRefreshCollector`

**Goal:** Library class that dedupes ids per target during a transaction and flushes in chunks after commit.

**Files:**
- Create: `libs/maia-elasticsearch-parent/maia-elasticsearch/src/main/kotlin/org/maiaframework/elasticsearch/refresh/EsRefreshTarget.kt`
- Create: `.../refresh/EsRefreshCollector.kt`
- Test: `libs/maia-elasticsearch-parent/maia-elasticsearch/src/test/kotlin/org/maiaframework/elasticsearch/refresh/EsRefreshCollectorTest.kt`

**Acceptance Criteria:**
- [ ] Ids enlisted in a tx are not flushed until commit; none flushed on rollback
- [ ] Duplicate ids to the same target collapse to one
- [ ] Flush calls `refreshByIds` in chunks of `chunkSize` (default 1000, prop `maia.es.refresh.chunkSize`)
- [ ] No active tx → flush immediately
- [ ] Exception from one target is logged at error and does not stop other targets

**Verify:** `./gradlew :libs:maia-elasticsearch-parent:maia-elasticsearch:test --tests "*EsRefreshCollectorTest"` → PASS

**Steps:**

- [ ] **Step 1: Write failing test**

```kotlin
class EsRefreshCollectorTest {

    private val calls = mutableListOf<Pair<String, List<DomainId>>>()
    private val target = object : EsRefreshTarget {
        override fun refreshByIds(ids: Collection<DomainId>) { calls += "refresh" to ids.toList() }
        override fun deleteById(id: DomainId) { calls += "delete" to listOf(id) }
    }
    private val collector = EsRefreshCollector(chunkSize = 2)

    @AfterEach fun cleanup() { if (TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization() }

    @Test fun `dedupes and chunks after commit`() {
        TransactionSynchronizationManager.initSynchronization()
        val a = DomainId.newId(); val b = DomainId.newId(); val c = DomainId.newId()
        collector.enlistRefresh(target, listOf(a, b, a, c))
        assertThat(calls).isEmpty()
        TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCommit() }
        assertThat(calls.map { it.second.size }).containsExactly(2, 1)
        assertThat(calls.flatMap { it.second }).containsExactlyInAnyOrder(a, b, c)
    }

    @Test fun `rollback flushes nothing`() {
        TransactionSynchronizationManager.initSynchronization()
        collector.enlistRefresh(target, listOf(DomainId.newId()))
        TransactionSynchronizationManager.getSynchronizations().forEach { it.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK) }
        assertThat(calls).isEmpty()
    }

    @Test fun `no transaction flushes immediately`() {
        collector.enlistRefresh(target, listOf(DomainId.newId()))
        assertThat(calls).hasSize(1)
    }
}
```

- [ ] **Step 2: Run → FAIL (classes missing)**

- [ ] **Step 3: Implement**

```kotlin
interface EsRefreshTarget {
    fun refreshByIds(ids: Collection<DomainId>)
    fun deleteById(id: DomainId)
}

@Component
class EsRefreshCollector(@Value("\${maia.es.refresh.chunkSize:1000}") private val chunkSize: Int) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val pending = ThreadLocal<MutableMap<EsRefreshTarget, MutableSet<DomainId>>>()

    fun enlistRefresh(target: EsRefreshTarget, ids: Collection<DomainId>) {
        if (ids.isEmpty()) return
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { flush(mapOf(target to ids.toSet())); return }
        val map = pending.get() ?: newPending()
        map.getOrPut(target) { linkedSetOf() }.addAll(ids)
    }

    private fun newPending(): MutableMap<EsRefreshTarget, MutableSet<DomainId>> {
        val map = linkedMapOf<EsRefreshTarget, MutableSet<DomainId>>()
        pending.set(map)
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() { flush(map) }
            override fun afterCompletion(status: Int) { pending.remove() }
        })
        return map
    }

    private fun flush(work: Map<EsRefreshTarget, Set<DomainId>>) {
        work.forEach { (target, ids) ->
            try { ids.chunked(chunkSize).forEach { target.refreshByIds(it) } }
            catch (e: Exception) { logger.error("ES refresh failed for ${target.javaClass.simpleName}; refreshIndex job will reconcile", e) }
        }
    }
}
```

Note: deletes of root docs are expressed as `refreshByIds` of ids that no longer exist (index service deletes missing ids — Task 5), so the collector needs no separate delete queue; `deleteById` stays on the interface for the typeahead-style direct call.

- [ ] **Step 4: Run → PASS**
- [ ] **Step 5: Commit** `git add libs/maia-elasticsearch-parent && git commit -m "feat(elasticsearch): add tx-scoped EsRefreshCollector"`

---

### Task 2: `elasticsearchEnabled()` DSL + derived `EsDocDef`

**Goal:** Searchable DTOs can declare ES versions and expose derived `EsDocDef`s.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/SearchableDtoDefBuilder.kt`
- Modify: `.../definition/SearchableDtoDef.kt` (constructor gets `esVersions: List<EsVersionSpec>`; add `val esDocDefs`)
- Create: `.../definition/SearchableDtoEsDocFactory.kt`
- Test: `maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/definition/SearchableDtoEsDocTest.kt` (create the test dir/JUnit wiring if absent — mirror `maia-gen-generator` test setup)

**Acceptance Criteria:**
- [ ] `elasticsearchEnabled()` ⇒ `esDocDefs.size == 1`, version 1, same package/base name as DTO, `generateRefreshIndexJob = true`, `entityDef = dtoRootEntityDef`
- [ ] EsDoc fields mirror `SimpleSearchableDtoFieldDef`s (names/types), id field = DTO id field
- [ ] m2m fields included as `object` mapping
- [ ] Setting `elasticsearchEnabled` forces `generateFindById = TRUE`
- [ ] Composite-PK root entity → `IllegalArgumentException` ("elasticsearchEnabled requires a single-column id")
- [ ] Unknown field type with no explicit mapping → exception naming the field; `esMapping(...)` on `field{}` overrides
- [ ] Calling twice with different versions ⇒ two EsDocDefs; duplicate version rejected

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test --tests "*SearchableDtoEsDocTest"` → PASS

**Steps:**

- [ ] **Step 1: Failing test** — build a minimal `EntityDef` via the existing test builders (look at how `AbstractSpec`-based specs build entities; if none exist in `maia-gen-spec` tests, build via a tiny `object : AbstractSpec(AppKey("t"))` and expose defs). Assert the acceptance criteria above.
- [ ] **Step 2: Run → FAIL**
- [ ] **Step 3: Implement**

```kotlin
// SearchableDtoDefBuilder
data class EsVersionSpec(val version: Int, val autoRefresh: Boolean)
private val esVersions = mutableListOf<EsVersionSpec>()

fun elasticsearchEnabled(version: Int = 1, autoRefresh: Boolean = true) {
    require(esVersions.none { it.version == version }) { "ES version $version already declared on $dtoBaseName" }
    require(rootEntityDef.primaryKeyFields.size == 1) { "elasticsearchEnabled requires a single-column id on ${rootEntityDef.entityBaseName}" }
    esVersions += EsVersionSpec(version, autoRefresh)
    generateFindById = GenerateFindById.TRUE
}
```

```kotlin
// SearchableDtoEsDocFactory
object SearchableDtoEsDocFactory {
    fun build(dto: SearchableDtoDef, spec: EsVersionSpec): EsDocDef {
        val fields = dto.allFieldsSorted.map { f ->
            val mapping = when (f) {
                is SimpleSearchableDtoFieldDef -> f.esMappingOverride ?: defaultMapping(f.classFieldDef)
                is ManyToManySearchableDtoFieldDef -> EsDocMappingTypes.`object`
                is CompositeIdFieldDef -> throw IllegalArgumentException("composite id unsupported")
            }
            EsDocFieldDef(f.classFieldDef, mapping, (f as? SimpleSearchableDtoFieldDef)?.entityFieldDef, isIdField = f.classFieldName == dto.idField.classFieldName)
        }
        return EsDocDef(dto.packageName, dto.dtoBaseName, ElasticIndexBaseName.of(dto.dtoBaseName.toSnakeCase()), spec.version,
            Description("Index for ${dto.dtoBaseName}"), fields, renderFieldEnum = false,
            generateRefreshIndexJob = true, disableRendering = false, entityDef = dto.dtoRootEntityDef)
    }

    private fun defaultMapping(f: ClassFieldDef): EsDocMappingType = when (f.fieldType) {
        is BooleanFieldType, is BooleanTypeFieldType, is BooleanValueClassFieldType -> EsDocMappingTypes.boolean
        is DoubleFieldType -> EsDocMappingTypes.double
        is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType, is LongFieldType, is LongTypeFieldType -> EsDocMappingTypes.long
        is InstantFieldType, is LocalDateFieldType -> EsDocMappingTypes.date
        is StringFieldType, is StringTypeFieldType, is StringValueClassFieldType -> EsDocMappingTypes.textAndKeyword
        is EnumFieldType, is DomainIdFieldType, is ForeignKeyFieldType -> EsDocMappingTypes.keyword
        else -> throw IllegalArgumentException("No default ES mapping for field ${f.classFieldName} (${f.fieldType}); declare esMapping(...)")
    }
}
```

Add `esMappingOverride: EsDocMappingType?` to `SimpleSearchableDtoFieldDef` and `fun esMapping(t: EsDocMappingType)` to `SearchableDtoFieldDefBuilder`. In `SearchableDtoDef`: `val esDocDefs = esVersions.map { SearchableDtoEsDocFactory.build(this, it) }`; `val esDocDef get() = esDocDefs.maxByOrNull { it.esDocVersion }`.
- [ ] **Step 4: Run → PASS**
- [ ] **Step 5: Commit** `feat(gen-spec): elasticsearchEnabled on searchableDto derives EsDocDef`

---

### Task 3: Register derived EsDocs + ES-sourced blotter

**Goal:** Derived EsDocs flow through existing ES generators; blotters on an ES-enabled DTO use the ES data source.

**Files:**
- Modify: `maia-gen/maia-gen-spec/.../AbstractSpec.kt` (`searchableDto()` ~L1011: `esDocDefs.addAll(def.esDocDefs)`; `blotter(searchableDtoDef…)` ~L1072)
- Test: `maia-gen/maia-gen-spec/src/test/.../ElasticsearchEnabledBlotterTest.kt`

**Acceptance Criteria:**
- [ ] `ModelDef.esDocsDefs` contains derived EsDocs
- [ ] `blotter(esEnabledDto)` ⇒ `blotterSourceDef is BlotterEsDocSourceDef` over the **latest** version, `dataSourceType == ELASTIC_SEARCH`
- [ ] Non-enabled DTOs unchanged (still `BlotterSearchableDtoSourceDef`)

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test --tests "*ElasticsearchEnabledBlotterTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing test** for the three criteria.
- [ ] **Step 2: Implement**

```kotlin
// in searchableDto(): after this.searchableDtoDefs.add(def)
this.esDocDefs.addAll(def.esDocDefs)

// in blotter(searchableDtoDef,...): replace blotterSourceDef = BlotterSearchableDtoSourceDef(searchableDtoDef) with
blotterSourceDef = searchableDtoDef.esDocDef?.let { BlotterEsDocSourceDef(it) } ?: BlotterSearchableDtoSourceDef(searchableDtoDef),
```
- [ ] **Step 2b (from Task 2 review):** when registering derived EsDocs in `searchableDto()`, fail fast if two registered `EsDocDef`s share the same `elasticIndexBaseName` + `esDocVersion` (e.g. an ES-enabled DTO `Alpha` vs a manual `esDoc("x", "Alpha")`, or the same DTO base name in two packages). Add a test. Do NOT propagate `esVersions` to the derived `*BlotterRow` DTOs in `BlotterDef.kt:197` (they intentionally get no EsDocs). `autoRefresh` lives only on `SearchableDtoDef.esVersions` (`EsDocDef` does not carry it).
- [ ] **Step 3: Audit** — `grep -rn "BlotterSearchableDtoSourceDef\|is BlotterEsDocSourceDef\|DataSourceType.DATABASE" maia-gen --include=*.kt` and confirm every generator branch that previously keyed off the searchable-DTO source still behaves for ES (e.g. `AngularUiModuleGenerator`, `ElasticSearchModuleGenerator.renderEsDocFieldNameMappers`). Fix any branch that assumes `searchableDtoDef` is non-null for blotters.
- [ ] **Step 4: Run → PASS; Commit** `feat(gen-spec): ES-enabled searchableDto feeds ES blotters`

---

### Task 4: DAO/Repo support — `findByIds`, `findAllAsSequence`

**Goal:** Index service can batch-load DTO rows via the DTO's own join query.

**Files:**
- Modify: `maia-gen/maia-gen-generator/.../renderers/SearchableDtoJdbcDaoRenderer.kt` (next to `findById`, ~L192)
- Modify: `.../renderers/SearchableDtoRepoRenderer.kt`
- Test: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/renderers/SearchableDtoJdbcDaoRendererTest.kt`

**Acceptance Criteria:**
- [ ] Only rendered when `esDocDefs.isNotEmpty()`
- [ ] `findByIds(ids: Collection<DomainId>): List<Dto>` uses `where <rootTable>.id in (:ids)`; empty input returns `emptyList()` without querying
- [ ] `findAllAsSequence(): Sequence<Dto>` streams the same select without `where`

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*SearchableDtoJdbcDaoRendererTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing renderer test** asserting rendered text contains `fun findByIds(ids: Collection<DomainId>): List<`, `in (:ids)`, `fun findAllAsSequence()`. (Mirror `CreateTableSqlRendererTest` style.)
- [ ] **Step 2: Implement** — copy the select/join assembly from `render function findById` into a private `selectAndJoins()` helper, then:

```kotlin
private fun `render function findByIds`() {
    if (searchableDtoDef.esDocDefs.isEmpty()) return
    blankLine(); blankLine()
    appendLine("    fun findByIds(ids: Collection<DomainId>): List<$dtoUqcn> {")
    blankLine()
    appendLine("        if (ids.isEmpty()) return emptyList()")
    appendLine("        val sqlParams = SqlParams().addValue(\"ids\", ids.map { it.value })")
    appendLine("        val sql = \"\"\"")
    appendLine("            select "); renderStrings(selectColumns, indent = 16); newLine()
    appendLine("            from $rootEntitySchemaAndTable")
    joinClauses.forEach { appendLine("            $it") }
    appendLine("            where $rootEntityTable.id in (:ids)")
    appendLine("            \"\"\".trimIndent()")
    appendLine("        return this.jdbcOps.query(sql, sqlParams, this.dtoRowMapper)")
    appendLine("    }")
}
```
`findAllAsSequence` same without `where`, using the existing sequence/stream query helper (grep `queryForSequence\|asSequence` in `libs/maia-jdbc*` for the exact JdbcOps method; use it). Qualify `id` with the root table alias in **both** `findById` and `findByIds` if joins make `id` ambiguous. Repo renderer: passthroughs `findByIds`, `findAllAsSequence`.
- [ ] **Step 3: Run → PASS; Commit** `feat(gen): searchable dto dao findByIds/findAllAsSequence`

---

### Task 5: Generated EsDoc repo + index service

**Goal:** Generate `<Dto>V<n>IndexService` (implements `EsRefreshTarget`) and `<Dto>V<n>EsDocRepo` for ES-enabled DTOs.

**Files:**
- Create: `.../renderers/SearchableDtoEsDocRepoRenderer.kt`
- Create: `.../renderers/SearchableDtoEsIndexServiceRenderer.kt`
- Modify: `.../generator/ElasticServiceModuleGenerator.kt` (`onGenerateSource`)
- Test: `.../renderers/SearchableDtoEsIndexServiceRendererTest.kt`

**Acceptance Criteria:**
- [ ] EsDoc repo: `findByIds(ids)`, `findAllAsSequence()` mapping DTO→EsDoc field-for-field (m2m lists mapped as-is)
- [ ] Index service has `refreshByIds` (upsert found docs via `esIndexOps.bulkUpsert`, `deleteById` for requested ids not returned), `deleteById`, `refreshIndex(jm)` compatible with `RefreshEsIndexJobRenderer`'s call (read it first and match signature), implements `EsRefreshTarget`
- [ ] `refreshIndex` upserts all in chunks and calls `removeDeletedRecordsFromIndex` (same as typeahead renderer)
- [ ] (from Task 4 review) `esDocRepo.findAllAsSequence()` holds a live JDBC connection/ResultSet until the sequence is fully consumed or its stream closed. `refreshIndex` must: iterate it inside a read-only transaction (Postgres only streams via cursor when autocommit is off, otherwise the driver loads everything into memory), consume it fully (or close it) via `.chunked(chunkSize)` + `bulkUpsert`, never collect it to a list, never hold it across threads, and keep slow ES calls per chunk. DTOs with many-to-many fields run a nested query per row on the same thread (second connection while the first is held) — note this limitation in the generated KDoc or fall back to `findByIds` over a pre-fetched id list for such DTOs (decide while implementing; add a test or a documented limitation).

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*SearchableDtoEsIndexServiceRendererTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing test** asserting rendered output contains the three functions and `EsRefreshTarget`.
- [ ] **Step 2: Implement** — base on `TypeaheadIndexServiceRenderer` (same ctor args: `elasticClient`, `esIndexOps`, `props`, `esDocRepo`, `indexNameFactory`, `jsonMapper`). Target generated shape:

```kotlin
override fun refreshByIds(ids: Collection<DomainId>) {
    val docs = this.esDocRepo.findByIds(ids)
    val found = docs.map { it.id }.toSet()
    if (docs.isNotEmpty()) this.esIndexOps.bulkUpsert(docs.map { buildEsDocHolder(it) })
    ids.filterNot { it in found }.forEach { this.esIndexOps.deleteById(it.value, this.indexResolvedName) }
}
```
- [ ] **Step 3:** In `ElasticServiceModuleGenerator`: `applicationModelDef.searchableDtoDefs.flatMap { dto -> dto.esDocDefs.map { dto to it } }.forEach { render both }`. Confirm `RefreshEsIndexJobRenderer` + `ElasticSearchModuleGenerator` already emit index/meta/control classes for `esDocsDefs` (they do for `EsIndexRenderer`/`EsIndexMetaClassRenderer`; **add `EsIndexControlRenderer` for derived EsDocs** as is done for typeaheads at `renderTypeaheadEsIndexes`).
- [ ] **Step 4: Run → PASS; Commit** `feat(gen): generate EsDoc repo and index service for ES-enabled searchableDto`

---

### Task 6: Listener API → `(before, after)` + `BeforeDelete`

**Goal:** Change generated CrudListener/Notifier/CrudService contract.

**Files:**
- Modify: `renderers/CrudListenerRenderer.kt:31-43`, `renderers/CrudNotifierRenderer.kt` (`render function onEntityUpdated` ~L400, `renderNotificationOfUpdateListeners` ~L194, delete ~L216), `renderers/CrudServiceRenderer.kt` (`setFields` ~L950, delete ~L970-1016), `renderers/TypeaheadCrudListenerRenderer.kt:76-90`
- Regenerate: every module with generated CrudListeners (`libs/maia-toggles-parent/*`, `maia-showcase/*`, any other with `grep -rl CrudListener --include=*.kt libs maia-showcase | grep generated`)
- Test: `maia-gen/maia-gen-generator/src/test/.../CrudListenerRendererTest.kt`; existing showcase tests as regression

**Acceptance Criteria:**
- [ ] Listener: `fun onXUpdated(before: XEntity, after: XEntity)`, `fun onXBeforeDelete(entity: XEntity) {}` (default no-op), delete/create unchanged
- [ ] Notifier: `onEntityUpdated(before, after)`, `onEntityBeforeDelete(entity)` fan out to listeners (incl. superclass listeners as today)
- [ ] CrudService `setFields`: loads `before = entityRepo.findByPrimaryKey(pk)` first, after update `after = entityRepo.findByPrimaryKey(pk)`; notifies only if `count > 0`
- [ ] Delete: `onEntityBeforeDelete(entityToDelete)` invoked after the existing guards and **before** `deleteByPrimaryKey`
- [ ] Typeahead listener impl compiles: `onXUpdated(before, after) = indexService.refreshById(after.id)`; **verify the AlphaWithHistory typeahead listener implements the AlphaWithHistory listener** (generated file currently shows `AlphaCrudListener`/`onAlphaEntity*` — if the cause is in `TypeaheadDef.crudListenerClassDef`/`entityCrudApiDef.entityDef`, fix and add a regression test)
- [ ] `./gradlew build` green after regeneration

**Verify:** `./gradlew build` → BUILD SUCCESSFUL

**Steps:**
- [ ] **Step 1: Failing renderer test** asserting new signatures.
- [ ] **Step 2: Implement** renderer edits. Target `setFields`:

```kotlin
fun setFields(updater: AlphaEntityUpdater): Int {
    val before = this.entityRepo.findByPrimaryKeyOrNull(<pk>)
    val count = this.entityRepo.setFields(updater)
    if (count > 0 && before != null) {
        val after = this.entityRepo.findByPrimaryKey(<pk>)
        this.alphaCrudNotifier.onEntityUpdated(before, after)
    }
    return count
}
```
(keep the existing `logger`/return structure; read L950-965 first and preserve it.)
- [ ] **Step 3:** Regenerate all affected modules in layer order (domain → dao → repo → service): `./gradlew maiaGeneration` at root, fix any hand-written `CrudListener` implementers (`grep -rn ": .*CrudListener" --include=*.kt libs maia-showcase | grep -v generated`).
- [ ] **Step 4:** `./gradlew build` → PASS. **Commit** `feat(gen)!: CrudListener update event carries before/after; add onBeforeDelete`

---

### Task 7: Refresh dependency model (spec)

**Goal:** For each ES-enabled DTO compute, per contributing entity, the watched fields and reverse path to the root.

**Files:**
- Create: `maia-gen/maia-gen-spec/.../definition/EsRefreshDependencyDef.kt`
- Modify: `SearchableDtoDef.kt` (`val esRefreshDependencies: List<EsRefreshDependencyDef>`)
- Test: `maia-gen/maia-gen-spec/src/test/.../EsRefreshDependencyDefTest.kt`

**Acceptance Criteria:**
- [ ] Root entity: always a dependency (`isRoot = true`), watched = all fields feeding the doc incl. FK join-key fields used by paths
- [ ] Path `customer.region.name` ⇒ dependencies: root (watch `customerId`), Customer (watch `regionId`; reverse hop root.customerId→customer.id), Region (watch `name`; reverse hops root.customerId→customer.id, customer.regionId→region.id)
- [ ] m2m join entity & the other-side entity ⇒ dependencies (reverse via join table)
- [ ] Same entity reached by two paths ⇒ one dependency with merged fields and both reverse paths
- [ ] Each non-root contributing entity requires `crudDef.withCrudListener`; otherwise `IllegalArgumentException` naming the entity and DTO

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test --tests "*EsRefreshDependencyDefTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing tests** for the 5 criteria (entity graph Order→Customer→Region built with spec test helpers).
- [ ] **Step 2: Implement**

```kotlin
class ReverseHop(val fromEntity: EntityDef, val fkField: EntityFieldDef, val toEntity: EntityDef) // fromEntity.fkField -> toEntity.id

class EsRefreshDependencyDef(
    val entityDef: EntityDef,
    val isRoot: Boolean,
    val watchedFields: Set<ClassFieldName>,
    val reversePaths: List<List<ReverseHop>>   // each: ordered from root to this entity
)
```
Walk each `SimpleSearchableDtoFieldDef.fieldPath` from `dtoRootEntityDef`, at each non-leaf segment resolve `ForeignKeyFieldType.foreignKeyFieldDef.foreignEntityDef` (same logic as `SearchableDtoDef.getFieldByPath`), adding the FK field to the current entity's watched set and a `ReverseHop`; leaf field added to the leaf entity's watched set. For `manyToManyJoinEntityDefs`: the join entity (both FK fields watched) and the other-side entity (watched = fields in `PkAndName`, i.e. pk + name field) with a two-hop reverse path.
- [ ] **Step 3: Run → PASS; Commit** `feat(gen-spec): derive EsRefreshDependencyDefs for ES-enabled searchableDto`

---

### Task 8: Reverse-join DAO queries

**Goal:** Generate `findRootIdsVia<Entity>(entityId)` on the searchable DTO DAO for every non-root dependency.

> **Notes from Task 7 review (authoritative):** consume `esRefreshDependencies` / `JoinHop` / `pathsFromRoot` (not `reversePath`). (1) Alias every hop's table by hop index (`h0`, `h1`…) — the same entity can appear twice in one path and in different paths. (2) The root dependency can carry extra non-empty paths; only NON-ROOT dependencies get `findRootIdsVia…`, plus a root entity that is also reached through a non-empty path needs its own method name distinct from the root-id pass-through (decide when implementing: e.g. `findRootIdsViaSelf<Entity>`). (3) Subclass entities share the superclass table: if a hop's target entity has type discriminators, add the discriminator predicate for that hop (reuse `typeDiscriminatorPredicate(column)` already in `SearchableDtoJdbcDaoRenderer`). (4) Joins are inner joins; with multiple `pathsFromRoot` combine per-path selects with `union`. (5) Join dependencies (J) and every `hasCrudListener = false` dependency still get a query only if Task 9 will call it — generate queries for dependencies where `hasCrudListener` is true, plus every other-side/membership entity.

**Files:**
- Modify: `renderers/SearchableDtoJdbcDaoRenderer.kt`, `renderers/SearchableDtoRepoRenderer.kt`
- Test: `renderers/SearchableDtoJdbcDaoRendererTest.kt`

**Acceptance Criteria:**
- [ ] One method per non-root dependency; SQL walks hops root→entity with **inner joins** and filters `<entityTable>.id = :id`, `select distinct root.id`
- [ ] Multiple reverse paths to the same entity ⇒ `union` of the per-path selects
- [ ] Returns `List<DomainId>`; repo passthrough

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*SearchableDtoJdbcDaoRendererTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing test** — for Order→Customer→Region assert text contains `fun findRootIdsViaRegion(id: DomainId): List<DomainId>` and `join ... on ... customer_id` ordering.
- [ ] **Step 2: Implement** — target SQL:

```sql
select distinct o.id from shop.order o
  join shop.customer c on o.customer_id = c.id
  join shop.region r on c.region_id = r.id
where r.id = :id
```
Use aliases per hop (`h0`, `h1`…) to avoid collisions; reuse the schema/table helpers already in this renderer.
- [ ] **Step 3: Run → PASS; Commit** `feat(gen): reverse-join root id queries for ES refresh`

---

### Task 9: Generated EsDoc CrudListeners

**Goal:** One listener bean per (EsDoc version, contributing entity) enlisting refreshes.

> **Design notes carried over from Task 7 (authoritative; they supersede the sketches below):**
> - The model is `SearchableDtoDef.esRefreshDependencies: List<EsRefreshDependencyDef>` with `JoinHop(fromEntity, fromField, toEntity, toField)` and `pathsFromRoot` (root→entity; the root's own dependency contains the empty path). Fields: `isRoot`, `watchedFields`, `refreshOnAnyUpdate`, `isManyToManyMembership`, `hasCrudListener`. It is `by lazy`; read it only when at least one version has `autoRefresh = true`.
> - Many-to-many reality (verified in generated code): Left/Right CrudServices reconcile join rows DIRECTLY through the join repo AFTER `setFields(updater)` fired `onEntityUpdated`, and never call the join entity's notifier. Therefore: (1) root with m2m: `refreshOnAnyUpdate` → enlist `after.id` on every update (runs after commit, so it sees the new membership); (2) other-side entity O: `refreshOnAnyUpdate = true`, and the listener must resolve root ids TWICE — now (event time, pre-reconcile, finds the OLD roots) and after commit (post-reconcile, finds the NEW roots). This needs a collector extension, **Step 0 below**; (3) join entity J: generate a listener only if `hasCrudListener`; nothing emits J events from Left/Right services, so J-only edits are reconciled by `refreshIndex` (document in the generated KDoc and the feature doc).
> - **Step 0 (collector extension, TDD, in libs/maia-elasticsearch):** add `fun enlistRefreshResolved(target: EsRefreshTarget, resolver: () -> Collection<DomainId>)` to `EsRefreshCollector`: the resolver is invoked in `afterCommit` BEFORE the flush, its ids are merged (deduped) with the ids already enlisted for that target; with no active synchronization it resolves and flushes immediately. Resolver exceptions are logged at error and do not stop other targets. Tests: resolver runs after commit not at enlist; resolver ids dedupe with directly enlisted ids; rollback never runs the resolver; no-transaction runs immediately; a throwing resolver doesn't stop another target.
> - Typical generated update handler for a non-root path entity with `refreshOnAnyUpdate = false`: `if (<watched fields differ>) collector.enlistRefresh(indexService, repo.findRootIdsVia<Entity>(after.id))`. For `refreshOnAnyUpdate = true` (m2m O): `collector.enlistRefresh(indexService, repo.findRootIdsVia<Entity>(before.id)); collector.enlistRefreshResolved(indexService) { repo.findRootIdsVia<Entity>(after.id) }`.

**Files:**
- Create: `renderers/SearchableDtoEsDocCrudListenerRenderer.kt`
- Modify: `generator/ServiceLayerModuleGenerator.kt` (or `ElasticServiceModuleGenerator` if that module can see entity listener interfaces — pick the module that depends on both `domain` and `elastic-service`; check `maia-showcase/*/build.gradle.kts`); `EsDocDef`/`SearchableDtoDef` get `esCrudListenerClassDef(entity)` → `<Dto>V<n>EsDoc<Entity>CrudListener`
- Test: `renderers/SearchableDtoEsDocCrudListenerRendererTest.kt`

**Acceptance Criteria:**
- [ ] Skipped entirely when the version's `autoRefresh = false` (read it from `SearchableDtoDef.esVersions`, matching versions to `esDocDefs` by `version == esDocVersion`; `EsDocDef` does not carry it)
- [ ] Root listener: created → enlist `entity.id`; updated → enlist `after.id` iff any watched field differs; deleted → enlist `entity.id` (index service deletes missing)
- [ ] Non-root: updated → iff watched field differs, enlist `repo.findRootIdsVia<Entity>(after.id)`; beforeDelete → enlist `findRootIdsVia<Entity>(entity.id)`; created → no-op except m2m join entity (enlist both sides' root ids via its FK values)
- [ ] Listener bean is `@Service`, depends on `EsRefreshCollector`, the DTO repo and the index service

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "*SearchableDtoEsDocCrudListenerRendererTest"` → PASS

**Steps:**
- [ ] **Step 1: Failing test** asserting generated text for a root and a non-root dependency.
- [ ] **Step 2: Implement** — target output (non-root):

```kotlin
@Service
class OrderSearchV1EsDocCustomerCrudListener(
    private val collector: EsRefreshCollector,
    private val repo: OrderSearchRepo,
    private val indexService: OrderSearchV1IndexService
) : CustomerCrudListener {

    override fun onCustomerEntityCreated(entity: CustomerEntity) {}

    override fun onCustomerEntityUpdated(before: CustomerEntity, after: CustomerEntity) {
        if (before.name != after.name || before.regionId != after.regionId) {
            collector.enlistRefresh(indexService, repo.findRootIdsViaCustomer(after.id))
        }
    }

    override fun onCustomerEntityBeforeDelete(entity: CustomerEntity) {
        collector.enlistRefresh(indexService, repo.findRootIdsViaCustomer(entity.id))
    }

    override fun onCustomerEntityDeleted(entity: CustomerEntity) {}
}
```
Root listener's updated: same diff over root watched fields, then `collector.enlistRefresh(indexService, listOf(after.id))`; its deleted: `collector.enlistRefresh(indexService, listOf(entity.id))`. Compare with `!=` on the entity property types (value classes/enums/Instant all support equals).
- [ ] **Step 3: Run → PASS; Commit** `feat(gen): EsDoc auto-refresh CrudListeners`

---

### Task 10: `esDoc{}` DSL must declare `autoRefresh`

**Goal:** Hand-built EsDocs fail generation unless they explicitly opt out (decision: fail-fast).

**Files:**
- Modify: `maia-gen/maia-gen-spec/.../builders/EsDocDefBuilder.kt` (`fun autoRefresh(enabled: Boolean)`), `EsDocDef.kt` (`val autoRefreshDeclared`), `AbstractSpec.esDoc()`
- Test: `maia-gen-spec/src/test/.../EsDocAutoRefreshDeclarationTest.kt`

**Acceptance Criteria:**
- [ ] `esDoc{}` without `autoRefresh(...)` ⇒ spec build throws, message names the EsDoc and says to call `autoRefresh(false)` (hand-built EsDocs have no join model, so `true` is rejected too: "autoRefresh(true) is only supported for elasticsearchEnabled searchableDto")
- [ ] `autoRefresh(false)` accepted; typeahead EsDocs unaffected
- [ ] `grep -rn "esDoc(" --include=*.kt . | grep -v generated` — update every existing caller in this repo

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test` → PASS

**Steps:** failing test → implement → fix callers → green → **Commit** `feat(gen-spec)!: esDoc{} must declare autoRefresh`

---

### Task 11: Showcase proof + integration test against real ES

**Goal:** End-to-end verification of every decided behavior.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` (enable ES on a DTO with an FK path; if none, add `bravo.someString` style field to a new `Es…` DTO over `Alpha`/`Bravo` — read the join entities near L960–1010 first; ensure referenced entities have `withCrudListener`)
- Possibly modify: Flyway migration for any new entity/DTO columns (only if spec adds entities)
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/es/EsDocAutoRefreshIntegrationTest.kt`
- Modify: test config / `maia-platform` BOM for Testcontainers ES if `AbstractBlackBoxTest` has no ES (check `MaiaShowcaseTestConfiguration.kt`; compose ES service if the project already uses compose for tests)

**Acceptance Criteria (each a test):**
- [ ] root update of a watched field ⇒ doc updated (use Awaitility after commit + index `refresh`)
- [ ] contributing-entity update of a watched field ⇒ every referencing root doc updated; **50 roots ⇒ ≤ 1 bulk call** (spy `EsIndexOps`)
- [ ] join-key change on root ⇒ doc shows new contributing values
- [ ] update of an unwatched field ⇒ **no** ES calls
- [ ] contributing-entity delete under inner join ⇒ affected docs removed; under nullable FK ⇒ doc remains with nulls
- [ ] transaction rolled back ⇒ index unchanged
- [ ] multi-version: V1 and V2 both updated
- [ ] ES failure after commit ⇒ DB write succeeded, error logged, no exception to caller

**Verify:** `./gradlew :maia-showcase:app:test --tests "*EsDocAutoRefreshIntegrationTest"` → PASS

**Steps:**
- [ ] **Step 1:** spec change; regenerate all layers in order (`./gradlew maiaGeneration`); `./gradlew build -x test` green.
- [ ] **Step 2:** write the 8 tests (Arrange via generated `*CrudService`/repos, assert via `ElasticsearchClient.get`).
- [ ] **Step 3:** run → fix generator bugs surfaced (fix in renderers, never in generated files).
- [ ] **Step 4:** full `./gradlew build` → PASS. **Commit** `test(showcase): ES auto-refresh integration`

---

### Task 12: Docs + cleanup

**Files:** `CLAUDE.md` is user-owned — don't edit; add `docs/es-enabled-searchable-dto.md` (DSL usage, refresh semantics, `autoRefresh` opt-out, safety-net job, limitations: single-column id, after-commit loss window).
**Verify:** `./gradlew build` → PASS. **Commit** `docs: ES-enabled searchableDto`

---

## Self-Review

- **Spec coverage:** field-level (T7/T9 watched fields) ✔; CrudNotifier/Listener (T6/T9) ✔; before+after (T6) ✔; always-load before (T6 setFields) ✔; reverse join any depth + m2m (T7/T8) ✔; searchableDto-derived EsDoc + blotters (T2/T3) ✔; after-commit/no outbox (T1) ✔; dedupe+chunk (T1) ✔; delete re-evaluate (T6 BeforeDelete, T5 `refreshByIds` deletes missing, T9) ✔; default-on + opt-out (T2 `autoRefresh`, T9 skip) ✔; hand-built must declare (T10) ✔; versions (T2 multi-version, T9 per version, T11) ✔; real-ES tests (T11) ✔.
- **Type consistency:** `EsRefreshTarget.refreshByIds/deleteById`, `EsRefreshCollector.enlistRefresh`, `EsVersionSpec`, `esDocDefs`/`esDocDef`, `esRefreshDependencies`, `findRootIdsVia<Entity>`, `onXEntityUpdated(before, after)`, `onXEntityBeforeDelete` used consistently.
- **Known soft spots (resolve while executing, stated in-task):** exact JDBC streaming helper name (T4), module hosting the listeners (T9), Testcontainers availability (T11), root-table alias ambiguity for `id` (T4).

## Unresolved Questions

1. Contributing entities without `withCrudListener`: plan fails generation with a clear message (T7). Auto-enable instead?
2. `esDoc{}` DSL `autoRefresh` is required (breaking for downstream apps) — confirm intended.
3. m2m join-row create/delete events: does `CrudService` emit them for join entities today (T9 assumes yes; T11 should cover it if so).
4. Writes bypassing `CrudService` (DAO bulk updates, importers) remain covered only by `refreshIndex`. Acceptable?
5. Default ES mapping per field type (T2): `textAndKeyword` for strings OK?

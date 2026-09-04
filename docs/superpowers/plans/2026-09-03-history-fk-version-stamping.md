# History FK Version Stamping Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a `recordVersionHistory` entity has a `foreignKey()` field pointing at another `recordVersionHistory` entity, stamp the referenced entity's version into the referencing entity's history rows, backed by a real composite DB foreign key to the referenced entity's history table.

**Architecture:** Extend `EntityDef.historyEntityDef` (maia-gen-spec) to synthesize a `<fk>Version: Long` field whenever a FK's target is itself historized. Wire a live version lookup into the generated `history()`/`insertHistory()` DAO functions (maia-gen-generator's `JdbcDaoRenderer`) so the value is captured at the exact instant each history row is written. Extend the DDL/schema-check pipeline (`ExpectedSchemaExtractor`, `CreateTableSqlRenderer`, `maia-gen-schema-check`) to model and render a real composite FK constraint from the history table to the referenced entity's history table's `(id, version)` primary key. Prove it out by regenerating `maia-showcase`'s existing `BravoWithHistory` → `AlphaWithHistory` FK.

**Tech Stack:** Kotlin, the Maia code generator (maia-gen-spec / maia-gen-generator / maia-gen-schema-check modules), JUnit 5 + AssertJ, Postgres/Flyway, Testcontainers.

**User Verification:** NO — no user verification loop was requested; this is a straight implementation of the approved design spec.

**Scope (from the design spec):** Plain `foreignKey()` fields only — many-to-many `leftEntity`/`rightEntity` references are out of scope (follow-up).

**Scope correction (found during Task 1 execution):** the design spec originally treated entity hierarchies with subclasses as out of scope too, with Task 1 rejecting that combination via `ModelDefinitionException`. Task 1's implementer found that the real `maia-showcase` spec already has exactly this case — `OrgUserGroup` (a `recordVersionHistory` subclass of `UserGroup`) has `foreignKey("org", organizationEntityDef)`, where `Organization` inherits `withVersionHistory = true` from `Party`. Rejecting it would permanently break the real app's spec, not just a hypothetical. Task 2 below now includes hierarchy support (mirroring the `hasSubclasses()` loop pattern already present in `JdbcDaoRenderer` for other things), and Task 1 no longer adds a guardrail.

**Blast radius is wider than the worked example:** `field_createdById(...)`/`field_lastModifiedById(...)` (used across most of the showcase) are themselves FK fields pointing at `Party`, which has `recordVersionHistory = true`. So essentially every `recordVersionHistory` entity in the showcase — not just `BravoWithHistory` — will get `createdByVersion`/`lastModifiedByVersion` synthesized once Task 1 lands. This was confirmed by running the full `maia-gen-schema-check` suite against the real showcase spec: `ShowcaseSchemaCheckIntegrationTest` now reports the (expected, not yet regenerated) schema drift across several `*_history` tables, e.g. `user_group_history` missing `org_version`, `bravo_with_history_history` missing `alpha_version`, and several tables missing `created_by_version`/`last_modified_by_version`. This is exactly the drift Task 6 exists to resolve by regenerating and promoting the migration — it's expected, not a defect — but it means Task 6's actual diff will touch many more tables than just `bravo_with_history`/`bravo_with_history_history`.

**Do not commit.** The user is committing all changes themselves at the end. Every task below ends with the change left in the working tree, staged or unstaged — do not run `git commit` at any point while executing this plan.

Reference (design spec): `docs/superpowers/specs/2026-09-03-history-fk-version-stamping-design.md`

**Deviation from the design spec's Testing section:** the spec called out RowMapper changes as a task. Planning research (`EntityRowMapperRenderer.kt:94-101` and `:140`) found `mapRow()` is generated generically from `entityDef.allEntityFieldsSorted` — the new `<fk>Version` field (added in Task 1) is picked up automatically, same as inheritance. Likewise, the history Dao's own `insert()`/`bulkInsert()` SQL (rendered when `JdbcDaoRenderer` runs for the *history* entity itself) is field-generic too, confirmed against the real generated `BravoWithHistoryHistoryDao.kt`. Only the *live* entity's Dao needs hand-editing (Task 2), because `history()`/`insertHistory()`/`bulkInsertHistory()` are the only functions in this renderer that enumerate fields by hand rather than iterating `allEntityFieldsSorted`. No task below touches `EntityRowMapperRenderer.kt`.

---

## Task 1: Synthesize the `<fk>Version` history field

**Goal:** `EntityDef.historyEntityDef` gains a synthetic `<fk>Version: Long` field for every FK field whose target entity has `recordVersionHistory = true` — including FK fields declared directly on a concrete subclass in an entity hierarchy, not just on root entities.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt:469-542` (historyEntityDef), `:1035-1124` (companion factories)
- Test: `maia-gen/maia-gen-spec/src/test/kotlin/org/maiaframework/gen/spec/HistoryFkValidationTest.kt`

**Acceptance Criteria:**
- [ ] A history entity whose live counterpart has a FK to a `recordVersionHistory` entity gets a `<fk>Version` field: type `Long`, not a primary key, table column `<fk_column>_version`, nullability matching the FK field's own nullability.
- [ ] A history entity whose FK target is NOT `recordVersionHistory` gets no such field (existing behavior unchanged).
- [ ] A FK field declared directly on a concrete subclass (not the abstract root) gets the same treatment on that subclass's own `historyEntityDef` — no hierarchy-specific code needed, since `historyEntityDef` is computed per-`EntityDef` instance from that instance's own `entityFieldsNotInherited`.

**Verify:** `./gradlew :maia-gen:maia-gen-spec:test --tests "org.maiaframework.gen.spec.HistoryFkValidationTest"` → all tests pass, including the 3 new ones added below.

**Steps:**

- [ ] **Step 1: Add the new companion factory to `EntityDef.kt`**

Add this next to the existing `versionFieldDef`/`changeTypeFieldDef` companion factories (after `changeTypeFieldDef`, around line 1121):

```kotlin
        fun foreignKeyVersionFieldDef(
            foreignKeyFieldName: ClassFieldName,
            fkNullability: Nullability,
            entityBaseName: EntityBaseName,
            packageName: PackageName
        ): EntityFieldDef {

            val fieldName = foreignKeyFieldName.withSuffix("Version")

            return EntityFieldDef(
                entityBaseName,
                packageName,
                aClassField(
                    fieldName,
                    fieldType = FieldTypes.long
                ) {
                    displayName("${foreignKeyFieldName.value.replaceFirstChar { it.uppercase() }} Version")
                    notCreatableByUser()
                    nullability(fkNullability)
                }.build(),
                TableColumnName(fieldName.toSnakeCase()),
                isDeltaField = IsDeltaField.TRUE,
            )

        }
```

Note the parameter is named `fkNullability`, not `nullability` — inside the `aClassField { ... }` lambda, `this` is the `ClassFieldDef.Builder`, which itself has a `nullability(Nullability): Builder` member function; naming the outer parameter `nullability` would let `nullability(nullability)` resolve ambiguously against the builder's own implicit-receiver scope. `fkNullability` avoids that entirely.

- [ ] **Step 2: Wire it into `historyEntityDef`**

In `EntityDef.kt`, inside the `historyEntityDef` val (around line 474-505), insert a new `.plus(...)` between the existing field-cloning `.map { ... }` and the `.plus(if (isRootEntity) changeTypeFieldDef(...) else null)`:

```kotlin
        val historyFieldDefs = entityFieldsNotInherited
            .asSequence()
            .map { fd ->
                // ... existing per-field clone logic, unchanged ...
            }
            .plus(
                entityFieldsNotInherited
                    .asSequence()
                    .filter { it.foreignKeyFieldDef?.foreignEntityDef?.withVersionHistory?.value == true }
                    .map { fd ->
                        foreignKeyVersionFieldDef(
                            foreignKeyFieldName = fd.classFieldName,
                            fkNullability = fd.nullability,
                            entityBaseName = historyEntityBaseName,
                            packageName = packageName
                        )
                    }
            )
            .plus(if (isRootEntity) changeTypeFieldDef(historyEntityBaseName, packageName) else null)
            .filterNotNull()
            .toList()
```

- [ ] **Step 3: Add tests to `HistoryFkValidationTest.kt`**

Append these three tests to the existing class:

```kotlin
    @Test
    fun `history entity FKing another history entity gets a synthesized version-tracking field`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        val childHistoryFields = spec.child.historyEntityDef!!.allEntityFieldsSorted
        val parentVersionField = childHistoryFields.single { it.classFieldName.value == "parentVersion" }

        assertThat(parentVersionField.isPrimaryKey.value).isFalse()
        assertThat(parentVersionField.nullable).isFalse()
        assertThat(parentVersionField.tableColumnName.value).isEqualTo("parent_version")

    }


    @Test
    fun `history entity FKing a non-history entity gets no synthesized version-tracking field`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val nonDeletable = entity("com.example", "NonDeletable") {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val withHistory = entity("com.example", "WithHistory", recordVersionHistory = true) {
                foreignKey("nonDeletable", nonDeletable) { fieldDisplayName("Non-Deletable") }
            }

        }

        val historyFields = spec.withHistory.historyEntityDef!!.allEntityFieldsSorted

        assertThat(historyFields.map { it.classFieldName.value }).doesNotContain("nonDeletableVersion")

    }


    @Test
    fun `a subclass entity FKing another history entity gets a synthesized version-tracking field on its own history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {

            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { fieldDisplayName("Name") }
            }

            val vehicle = entity("com.example", "Vehicle", recordVersionHistory = true) {
                typeDiscriminator("VEHICLE")
            }

            val car = entity("com.example", "Car", recordVersionHistory = true) {
                superclass(vehicle)
                typeDiscriminator("CAR")
                foreignKey("parent", parent) { fieldDisplayName("Parent") }
            }

        }

        val carHistoryFields = spec.car.historyEntityDef!!.allEntityFieldsSorted
        val parentVersionField = carHistoryFields.single { it.classFieldName.value == "parentVersion" }

        assertThat(parentVersionField.isPrimaryKey.value).isFalse()
        assertThat(parentVersionField.nullable).isFalse()

    }
```

This test exists to prove `historyEntityDef`'s per-instance computation already handles a FK field declared directly on a concrete subclass, without needing any hierarchy-specific code in Steps 1-2 — it should pass without further production-code changes. If it does not, stop and report back rather than improvising a fix, since that would mean the per-instance assumption above is wrong.

- [ ] **Step 4: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-spec:test --tests "org.maiaframework.gen.spec.HistoryFkValidationTest"`
Expected: BUILD SUCCESSFUL, all `HistoryFkValidationTest` tests pass (5 pre-existing + 3 new).

---

## Task 2: Wire the live version lookup into the generated DAO

**Goal:** The generated `<Entity>Dao` for a history-tracked entity with FK field(s) to other history-tracked entities gets the referenced entity's Dao injected, a `findVersionByPrimaryKey` lookup added to that referenced Dao, and the lookup wired into `history()`/`insertHistory()`/`bulkInsertHistory()` so each history row is stamped with the referenced version.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/JdbcDaoRenderer.kt`

**Acceptance Criteria:**
- [ ] Any entity with `withVersionHistory = true` gets a `findVersionByPrimaryKey(id: DomainId): Long` function on its generated Dao (cheap, PK-indexed read against its own live table).
- [ ] An entity with a FK field to a historized entity gets that entity's Dao injected as a constructor dependency, deduplicated per distinct referenced entity.
- [ ] `history()`'s signature gains one extra parameter per historized FK field (`<fk>Version: Long` or `Long?` if the FK is nullable).
- [ ] `insertHistory(entity, version, changeType)` and `bulkInsertHistory(entities, changeType)` look up the referenced version via the injected Dao and pass it through to `history(...)`.
- [ ] This covers BOTH the non-hierarchy (`entityHierarchy.hasSubclasses() == false`) code paths AND the `hasSubclasses()` branches, since the real `maia-showcase` spec already has a historized FK declared on a concrete subclass (`OrgUserGroup.org → Organization`) — see the plan header's "Scope correction" note.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:build` → BUILD SUCCESSFUL (no dedicated renderer unit test exists for `JdbcDaoRenderer` in this codebase today — correctness is proven end-to-end in Task 6 against the regenerated `maia-showcase`).

**Steps:**

- [ ] **Step 1: Add a helper function and two private vals near the existing `foreignKeyTableCount` val (~line 81-91)**

```kotlin
    private fun historizedForeignKeyFieldDefsFor(entityDef: EntityDef): List<EntityFieldDef> = entityDef.allForeignKeyEntityFieldDefs
        .filter { it.foreignKeyFieldDef!!.foreignEntityDef.withVersionHistory.value }


    private val historizedForeignKeyFieldDefs: List<EntityFieldDef> = historizedForeignKeyFieldDefsFor(entityDef)


    private val historizedForeignEntityDefs: List<EntityDef> = (
        if (entityHierarchy.hasSubclasses()) {
            entityHierarchy.concreteEntityDefs.flatMap { historizedForeignKeyFieldDefsFor(it) }
        } else {
            historizedForeignKeyFieldDefs
        }
    ).map { it.foreignKeyFieldDef!!.foreignEntityDef }.distinct()
```

`historizedForeignKeyFieldDefs` (root-only, used by the non-hierarchy branches below) is unchanged in meaning. `historizedForeignEntityDefs` (used only for constructor-arg dedup in Step 2) is now hierarchy-aware: for a hierarchy with subclasses, it must gather FK fields declared on *any* concrete subclass (e.g. `OrgUserGroup.org`), not just ones on the root — `EntityDef.allForeignKeyEntityFieldDefs` on a concrete subclass already includes its own fields plus everything inherited, so iterating `concreteEntityDefs` (not `entityDefs`, which would also include the non-instantiable abstract root and double-count inherited fields) is correct here.

- [ ] **Step 2: Inject the referenced Dao(s) in `init {}`**

Right after the existing block at line 119-121 (`entityDef.historyEntityDef?.let { addConstructorArg(...) }`), add:

```kotlin
        if (entityDef.historyEntityDef != null) {
            historizedForeignEntityDefs.forEach { foreignEntityDef ->
                addConstructorArg(
                    aClassField("${foreignEntityDef.entityBaseName.firstToLower()}Dao", foreignEntityDef.daoFqcn).privat().build()
                )
            }
        }
```

This one block already covers both the hierarchy and non-hierarchy cases, since `historizedForeignEntityDefs` (Step 1) is already hierarchy-aware and deduplicated — no `hasSubclasses()` branching needed here.

- [ ] **Step 3: Add the `findVersionByPrimaryKey` render function**

Add a new private fun, and call it from `renderFunctions()` right after `` `render the findByPrimaryKeyOrNull function`() `` (line 219):

```kotlin
    private fun `render the findVersionByPrimaryKey function`() {

        if (entityDef.withVersionHistory.value == false || entityDef.hasSurrogatePrimaryKey == false) {
            return
        }

        append("""
            |
            |
            |    fun findVersionByPrimaryKey(id: DomainId): Long {
            |
            |        return jdbcOps.queryForLong(
            |            "select version from ${entityDef.schemaAndTableName} where id = :id",
            |            SqlParams().apply {
            |                addValue("id", id)
            |            }
            |        )
            |
            |    }
            |""".trimMargin())

    }
```

(Composite-primary-key referenced entities are out of scope — `foreignKey()` fields are only built against surrogate-PK targets per `ForeignKeyFieldDefBuilder`, so this guard should never actually trigger for a real FK target, but it keeps the function from being generated somewhere nonsensical.)

- [ ] **Step 4: Rewrite `render the insertHistory function`** (currently lines 496-548) — both branches

Replace the whole function body (both the `hasSubclasses()` branch and the `else` branch):

```kotlin
        if (entityHierarchy.hasSubclasses()) {

            entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                val fkFieldDefs = historizedForeignKeyFieldDefsFor(entityDef)
                val versionLookupLines = versionLookupLinesFor(fkFieldDefs)
                val versionArgsSuffix = versionArgsSuffixFor(fkFieldDefs)

                append("""
                    |
                    |
                    |    private fun insertHistory(entity: ${entityDef.entityUqcn}, changeType: ChangeType) {
                    |
                    |        insertHistory(entity, entity.version, changeType)
                    |
                    |    }
                    |
                    |
                    |    private fun insertHistory(entity: ${entityDef.entityUqcn}, version: Long, changeType: ChangeType) {
                    |
                    |""".trimMargin())

                if (versionLookupLines.isNotEmpty()) {
                    appendLine(versionLookupLines)
                    blankLine()
                }

                append("""
                    |        this.historyDao.insert(history(entity, version, changeType$versionArgsSuffix))
                    |
                    |    }
                    |""".trimMargin())

            }

        } else {

            val versionLookupLines = versionLookupLinesFor(historizedForeignKeyFieldDefs)
            val versionArgsSuffix = versionArgsSuffixFor(historizedForeignKeyFieldDefs)

            append("""
                |
                |
                |    private fun insertHistory(entity: ${entityDef.entityUqcn}, changeType: ChangeType) {
                |
                |        insertHistory(entity, entity.version, changeType)
                |
                |    }
                |
                |
                |    private fun insertHistory(entity: ${entityDef.entityUqcn}, version: Long, changeType: ChangeType) {
                |
                |""".trimMargin())

            if (versionLookupLines.isNotEmpty()) {
                appendLine(versionLookupLines)
                blankLine()
            }

            append("""
                |        this.historyDao.insert(history(entity, version, changeType$versionArgsSuffix))
                |
                |    }
                |""".trimMargin())

        }
```

Note `this.historyDao` stays as-is (unqualified, single shared reference) in *both* branches, unchanged from the original — the root's `historyDao` is typed for the root's (possibly abstract) history entity, and every concrete subclass's history entity is a Kotlin subtype of it (`historyEntityDef`'s own `superclassEntityDef` mirrors the live entity's), so passing any concrete subclass's history entity to it already type-checks. Don't "fix" this to use a per-subclass dao — that would be a bigger, unrelated change to existing behavior.

Add these two small private helper functions (used by all three of Steps 4-6, so add them once, e.g. right next to `historizedForeignKeyFieldDefsFor`):

```kotlin
    private fun versionLookupLinesFor(fkFieldDefs: List<EntityFieldDef>): String = fkFieldDefs.joinToString("\n") { fk ->
        val foreignEntityDef = fk.foreignKeyFieldDef!!.foreignEntityDef
        val daoRef = "this.${foreignEntityDef.entityBaseName.firstToLower()}Dao"
        val versionValName = "${fk.classFieldName}Version"
        if (fk.nullable) {
            "        val $versionValName = entity.${fk.classFieldName}?.let { $daoRef.findVersionByPrimaryKey(it) }"
        } else {
            "        val $versionValName = $daoRef.findVersionByPrimaryKey(entity.${fk.classFieldName})"
        }
    }


    private fun versionArgsSuffixFor(fkFieldDefs: List<EntityFieldDef>): String = fkFieldDefs.joinToString("") { ", ${it.classFieldName}Version" }
```

- [ ] **Step 5: Rewrite `render the bulkInsertHistory function`** (currently lines 551-601) — both branches

The `hasSubclasses()` branch's *last* `forEach` (currently lines 576-581) needs the version lookup+args added to its `history(it, it.version + 1, changeType)` call. Replace that inner `forEach` block with:

```kotlin
            entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                val fkFieldDefs = historizedForeignKeyFieldDefsFor(entityDef)

                blankLine()
                appendLine("        val entities${entityDef.typeDiscriminator}: List<${entityDef.entityUqcn}> = entitiesByType[\"${entityDef.typeDiscriminator}\"] as? List<${entityDef.entityUqcn}> ?: emptyList()")

                if (fkFieldDefs.isEmpty()) {
                    appendLine("        val ${entityDef.historyEntityDef!!.entityUqcn.firstToLower()}List = entities${entityDef.typeDiscriminator}.map { history(it, it.version + 1, changeType) }")
                } else {
                    val versionLookupsCsv = fkFieldDefs.joinToString(", ") { fk ->
                        val foreignEntityDef = fk.foreignKeyFieldDef!!.foreignEntityDef
                        val daoRef = "this.${foreignEntityDef.entityBaseName.firstToLower()}Dao"
                        if (fk.nullable) {
                            "it.${fk.classFieldName}?.let { id -> $daoRef.findVersionByPrimaryKey(id) }"
                        } else {
                            "$daoRef.findVersionByPrimaryKey(it.${fk.classFieldName})"
                        }
                    }
                    appendLine("        val ${entityDef.historyEntityDef!!.entityUqcn.firstToLower()}List = entities${entityDef.typeDiscriminator}.map { history(it, it.version + 1, changeType, $versionLookupsCsv) }")
                }

                appendLine("        this.${entityDef.historyEntityDef!!.daoFqcn.uqcn.firstToLower()}.bulkInsert(${entityDef.historyEntityDef!!.entityUqcn.firstToLower()}List)")

            }
```

Leave the surrounding `entitiesByType`-building code (the first `forEach`, right before this one) and everything before/after it in that branch untouched.

The `else` (non-hierarchy) branch: replace with

```kotlin
        } else {

            val versionArgsSuffix = versionArgsCsvSuffixFor(historizedForeignKeyFieldDefs)

            append("""
                |
                |
                |    private fun bulkInsertHistory(entities: List<${entityDef.entityUqcn}>, changeType: ChangeType) {
                |
                |        val historyEntities = entities.map { history(it, it.version, changeType$versionArgsSuffix) }
                |        this.historyDao.bulkInsert(historyEntities)
                |
                |    }
                |""".trimMargin())

        }
```

with one more small helper next to the two from Step 4:

```kotlin
    private fun versionArgsCsvSuffixFor(fkFieldDefs: List<EntityFieldDef>): String = fkFieldDefs.joinToString("") { fk ->
        val foreignEntityDef = fk.foreignKeyFieldDef!!.foreignEntityDef
        val daoRef = "this.${foreignEntityDef.entityBaseName.firstToLower()}Dao"
        val lookup = if (fk.nullable) {
            "it.${fk.classFieldName}?.let { id -> $daoRef.findVersionByPrimaryKey(id) }"
        } else {
            "$daoRef.findVersionByPrimaryKey(it.${fk.classFieldName})"
        }
        ", $lookup"
    }
```

(This duplicates the per-field lookup expression logic from the `hasSubclasses()` branch above rather than sharing it, because that branch needs a comma-joined inline list and this one needs a single trailing-comma-prefixed suffix — trying to unify them isn't worth the indirection for two call sites.)

- [ ] **Step 6: Rewrite `render the history function`** (currently lines 604-667) — both branches

`hasSubclasses()` branch: add `versionParams` per concrete entityDef, using that loop's own `entityDef` (not the outer root one):

```kotlin
            entityHierarchy.concreteEntityDefs.forEach { entityDef ->

                addImportFor(entityDef.historyEntityDef!!.entityFqcn)

                val versionParams = versionParamsFor(historizedForeignKeyFieldDefsFor(entityDef))

                blankLine()
                blankLine()
                appendLine("    private fun history(")
                appendLine("        entity: ${entityDef.entityUqcn},")
                appendLine("        version: Long,")
                appendLine("        changeType: ChangeType$versionParams")
                appendLine("    ): ${entityDef.historyEntityDef!!.entityUqcn} {")
                blankLine()

                if (entityDef.hasSurrogatePrimaryKey) {
                    appendLine("        val id = entity.id")
                }

                entityDef.allEntityFieldsSorted.filterNot { it.isPrimaryKey.isSurrogate || it.classFieldName == ClassFieldName.version }.forEach { fd ->
                    appendLine("        val ${fd.classFieldName} = entity.${fd.classFieldName}")
                }

                EntityRendererHelper.renderCallToEntityConstructor(entityDef.historyEntityDef!!, indentSize = 8, renderer = this)

                blankLine()
                appendLine("    }")

            }
```

`else` (non-hierarchy) branch:

```kotlin
        } else {

            val versionParams = versionParamsFor(historizedForeignKeyFieldDefs)

            blankLine()
            blankLine()
            appendLine("    private fun history(")
            appendLine("        entity: ${this.entityDef.entityUqcn},")
            appendLine("        version: Long,")
            appendLine("        changeType: ChangeType$versionParams")
            appendLine("    ): ${historyEntityDef.entityUqcn} {")
            blankLine()

            if (entityDef.hasSurrogatePrimaryKey) {
                appendLine("        val id = entity.id")
            }

            this.entityDef.allEntityFieldsSorted.filterNot { it.isPrimaryKey.isSurrogate || it.classFieldName == ClassFieldName.version }.forEach { fd ->
                appendLine("        val ${fd.classFieldName} = entity.${fd.classFieldName}")
            }

            EntityRendererHelper.renderCallToEntityConstructor(historyEntityDef, indentSize = 8, renderer = this)

            blankLine()
            appendLine("    }")

        }
```

Last helper, next to the others:

```kotlin
    private fun versionParamsFor(fkFieldDefs: List<EntityFieldDef>): String = fkFieldDefs.joinToString("") { fk ->
        val type = if (fk.nullable) "Long?" else "Long"
        ",\n        ${fk.classFieldName}Version: $type"
    }
```

`EntityRendererHelper.renderCallToEntityConstructor` matches constructor args to the history entity's fields purely by identifier name already in scope — since `<fk>Version` is now a `history()` function parameter, no extra local `val` line is needed for it, in either branch.

- [ ] **Step 7: Build**

Run: `./gradlew :maia-gen:maia-gen-generator:build`
Expected: BUILD SUCCESSFUL

---

## Task 3: Model the composite FK in the expected-schema extractor

**Goal:** `ExpectedSchemaExtractor` recognizes a historized-FK-to-historized-target on a history entity and represents it as a composite FK (both id and version columns together), instead of the old single-column FK to the live table.

**Files:**
- Modify: `../../../maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/schema/expected/ExpectedSchemaExtractor.kt`
- Test: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/schema/ExpectedSchemaExtractorTest.kt`

**Acceptance Criteria:**
- [ ] `ExpectedTableDef` gains a new `compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList()` field (default value preserves every existing positional-constructor call site and test).
- [ ] For a history entity's FK field whose target is also historized: the FK's id column is *excluded* from the flat `foreignKeys` list, and a `ExpectedCompositeForeignKeyDef(columnNames = [fk_id_col, fk_version_col], referencedSchemaAndTable = <target's history table>, referencedColumns = [id_col, version_col])` entry appears in `compositeForeignKeys`.
- [ ] The live (non-history) entity's own FK column is completely unaffected — still a single-column `ExpectedForeignKeyDef` to the live target table, exactly as before.
- [ ] A history entity's FK to a non-historized target is unaffected — still a single-column `ExpectedForeignKeyDef`, exactly as before.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "org.maiaframework.gen.schema.ExpectedSchemaExtractorTest"` → all tests pass, including the new one added below.

**Steps:**

- [ ] **Step 1: Add the new data class and field**

```kotlin
data class ExpectedCompositeForeignKeyDef(
    val columnNames: List<String>,
    val referencedSchemaAndTable: String,
    val referencedColumns: List<String>,
)

data class ExpectedTableDef(
    val schemaAndTableName: String,
    val columns: List<ExpectedColumnDef>,
    val primaryKeyColumns: List<String>,
    val foreignKeys: List<ExpectedForeignKeyDef>,
    val indexes: List<ExpectedIndexDef>,
    val compositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
)
```

- [ ] **Step 2: Replace the `foreignKeys` computation in `extractTable()`** (currently lines 88-100)

```kotlin
        val (historizedFkSqlFields, plainFkSqlFields) = sqlFieldsForColumns
            .filter { it.fieldType is ForeignKeyFieldType }
            .partition {
                baseEntityDef.isHistoryEntity &&
                    (it.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef.withVersionHistory.value
            }

        val foreignKeys = plainFkSqlFields.map { sqlFieldDef ->
            val foreignEntityDef = (sqlFieldDef.fieldType as ForeignKeyFieldType).foreignKeyFieldDef.foreignEntityDef
            ExpectedForeignKeyDef(
                columnName = sqlFieldDef.tableColumnName.value,
                referencedSchemaAndTable = schemaAndTableNameFor(foreignEntityDef),
                referencedColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName.value,
            )
        }

        val compositeForeignKeys = historizedFkSqlFields.map { sqlFieldDef ->
            val fieldType = sqlFieldDef.fieldType as ForeignKeyFieldType
            val foreignEntityDef = fieldType.foreignKeyFieldDef.foreignEntityDef
            val foreignHistoryEntityDef = foreignEntityDef.historyEntityDef!!

            val versionColumnName = fieldType.foreignKeyFieldDef.foreignKeyFieldName.withSuffix("Version").toSnakeCase()
            val foreignIdColumn = foreignEntityDef.primaryKeyFields.first().tableColumnName.value
            val foreignVersionColumn = foreignHistoryEntityDef.allEntityFieldsSorted
                .first { it.classFieldName == ClassFieldName.version }.tableColumnName.value

            ExpectedCompositeForeignKeyDef(
                columnNames = listOf(sqlFieldDef.tableColumnName.value, versionColumnName),
                referencedSchemaAndTable = schemaAndTableNameFor(foreignHistoryEntityDef),
                referencedColumns = listOf(foreignIdColumn, foreignVersionColumn),
            )
        }
```

`ClassFieldName` is already imported in this file. The version column name is derived from the FK's *field* name (`foreignKeyFieldName`, e.g. `"alpha"`), not its table column name (`"alpha_id"`) — mirroring exactly how Task 1's `foreignKeyVersionFieldDef` derives it, so the two stay in lockstep.

- [ ] **Step 3: Pass `compositeForeignKeys` into the returned `ExpectedTableDef`** (currently lines 117-123)

```kotlin
        return ExpectedTableDef(
            schemaAndTableName = schemaAndTableNameFor(baseEntityDef),
            columns = columns,
            primaryKeyColumns = primaryKeyColumns,
            foreignKeys = foreignKeys,
            indexes = indexes,
            compositeForeignKeys = compositeForeignKeys,
        )
```

- [ ] **Step 4: Add a test to `ExpectedSchemaExtractorTest.kt`**

```kotlin
    @Test
    fun `extracts a composite foreign key when a history entity FKs another history entity`() {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent)
            }
        }

        val tables = ExpectedSchemaExtractor().extract(spec.modelDef.rootEntityHierarchies)
        val childHistoryTable = tables.single { it.schemaAndTableName == "test.child_history" }

        assertThat(childHistoryTable.foreignKeys.map { it.columnName }).doesNotContain("parent_id")
        assertThat(childHistoryTable.compositeForeignKeys).containsExactly(
            ExpectedCompositeForeignKeyDef(
                columnNames = listOf("parent_id", "parent_version"),
                referencedSchemaAndTable = "test.parent_history",
                referencedColumns = listOf("id", "version"),
            )
        )

        val childTable = tables.single { it.schemaAndTableName == "test.child" }
        assertThat(childTable.foreignKeys).containsExactly(
            ExpectedForeignKeyDef("parent_id", "test.parent", "id")
        )

    }
```

- [ ] **Step 5: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-generator:test --tests "org.maiaframework.gen.schema.ExpectedSchemaExtractorTest"`
Expected: BUILD SUCCESSFUL, all tests pass.

---

## Task 4: Render the composite FK constraint in the generated DDL

**Goal:** `CreateTableSqlRenderer` emits a table-level `FOREIGN KEY (...) REFERENCES ...(...)` constraint for each `ExpectedTableDef.compositeForeignKeys` entry.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRenderer.kt`
- Test: `maia-gen/maia-gen-generator/src/test/kotlin/org/maiaframework/gen/renderers/CreateTableSqlRendererTest.kt`

**Acceptance Criteria:**
- [ ] After each `CREATE TABLE`'s indexes, one `ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY (...) REFERENCES ...(...);` statement is emitted per composite FK.
- [ ] The inline single-column `REFERENCES` suffix on the FK's id column is *not* rendered for a column now covered by a composite FK (this is automatic — no code change needed, since `foreignKeysByColumnName` is built from `expectedTableDef.foreignKeys`, which Task 3 already excludes that column from).
- [ ] Constraint names are validated with `PostgresIdentifiers.requireValidLength`, matching the existing exclusion-constraint precedent in this same file.

**Verify:** `./gradlew :maia-gen:maia-gen-generator:test --tests "org.maiaframework.gen.renderers.CreateTableSqlRendererTest"` → all tests pass, including the new one added below.

**Steps:**

- [ ] **Step 1: Call the new render step after indexes**

In `` `render CREATE TABLE statement for` `` (`CreateTableSqlRenderer.kt:60-120`), right after the existing `renderIndexes(entityHierarchy, expectedTableDef)` call (line 118), add:

```kotlin
        `render composite foreign key constraints`(expectedTableDef)
```

- [ ] **Step 2: Add the new private fun**

```kotlin
    private fun `render composite foreign key constraints`(expectedTableDef: ExpectedTableDef) {

        expectedTableDef.compositeForeignKeys.forEach { compositeForeignKey ->

            val bareTableName = expectedTableDef.schemaAndTableName.substringAfterLast(".")
            val constraintName = "${bareTableName}_${compositeForeignKey.columnNames.joinToString("_")}_fkey"

            PostgresIdentifiers.requireValidLength(
                constraintName,
                "foreign key constraint name",
                "Shorten the referencing field name on the entity that declares this foreign key."
            )

            val localColumns = compositeForeignKey.columnNames.joinToString(", ")
            val referencedColumns = compositeForeignKey.referencedColumns.joinToString(", ")

            appendLine(
                "ALTER TABLE ${expectedTableDef.schemaAndTableName} ADD CONSTRAINT $constraintName " +
                    "FOREIGN KEY ($localColumns) REFERENCES ${compositeForeignKey.referencedSchemaAndTable}($referencedColumns);"
            )

        }

    }
```

- [ ] **Step 3: Add a test to `CreateTableSqlRendererTest.kt`**

Follow the existing `` `renders a separate history table for a recordVersionHistory entity` `` test's style:

```kotlin
    @Test
    fun `renders a composite foreign key constraint when a history entity FKs another history entity`(@TempDir tempDir: File) {

        val spec = object : AbstractSpec(AppKey("Test")) {
            val parent = entity("com.example", "Parent", recordVersionHistory = true) {
                field("name", FieldTypes.string) { lengthConstraint(max = 50) }
            }
            val child = entity("com.example", "Child", recordVersionHistory = true) {
                foreignKey("parent", parent)
            }
        }

        val renderer = CreateTableSqlRenderer(spec.modelDef.rootEntityHierarchies, "create_tables.sql")
        renderer.renderToDir(tempDir)

        val sql = tempDir.resolve("create_tables.sql").readText()

        assertThat(sql).contains(
            "ALTER TABLE test.child_history ADD CONSTRAINT child_history_parent_id_parent_version_fkey " +
                "FOREIGN KEY (parent_id, parent_version) REFERENCES test.parent_history(id, version);"
        )

        val childHistoryBlock = sql.substringAfter("CREATE TABLE test.child_history (").substringBefore(");")
        assertThat(childHistoryBlock).doesNotContain("REFERENCES test.parent(id)")

    }
```

- [ ] **Step 4: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-generator:test --tests "org.maiaframework.gen.renderers.CreateTableSqlRendererTest"`
Expected: BUILD SUCCESSFUL, all tests pass.

---

## Task 5: Teach the schema-check tool about composite FKs

**Goal:** `SchemaComparator` doesn't false-flag a real composite FK constraint's columns as missing/extra, and `SchemaFixSqlGenerator` can generate the correct composite `ALTER TABLE ... ADD CONSTRAINT` fix-SQL for a missing one.

**Files:**
- Modify: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaComparator.kt`
- Modify: `maia-gen/maia-gen-schema-check/src/main/kotlin/org/maiaframework/gen/schemacheck/SchemaFixSqlGenerator.kt`
- Test: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaComparatorTest.kt`
- Test: `maia-gen/maia-gen-schema-check/src/test/kotlin/org/maiaframework/gen/schemacheck/SchemaFixSqlGeneratorTest.kt`

No changes are needed to `PostgresSchemaIntrospector`/`ActualSchemaModel` — `foreignKeysFor()`'s `unnest(con.conkey, con.confkey)` query already flattens a real multi-column Postgres FK constraint into one `ActualForeignKeyDef` row per column-pair (see the comment at `PostgresSchemaIntrospector.kt:122-129`), which is exactly the shape needed for the presence checks below.

**Acceptance Criteria:**
- [ ] A real composite FK constraint's columns (both the id and version columns) are no longer reported as "extra" just because they're absent from the flat `foreignKeys` list.
- [ ] A composite FK expected but missing from the real DB is reported via a new `missingCompositeForeignKeys` field on `TableDiff`, and counts as an error (`hasErrors`).
- [ ] `SchemaFixSqlGenerator` emits one correct `ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY (a, b) REFERENCES t(x, y);` statement per missing composite FK.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "org.maiaframework.gen.schemacheck.SchemaComparatorTest" --tests "org.maiaframework.gen.schemacheck.SchemaFixSqlGeneratorTest"` → all tests pass, including the new ones added below.

**Steps:**

- [ ] **Step 1: Extend `TableDiff` and the comparison logic in `SchemaComparator.kt`**

Add the import `org.maiaframework.gen.schema.ExpectedCompositeForeignKeyDef`, add a field to `TableDiff`:

```kotlin
    val missingCompositeForeignKeys: List<ExpectedCompositeForeignKeyDef> = emptyList(),
```

and include it in `hasErrors`:

```kotlin
    val hasErrors: Boolean
        get() = status == TableStatus.MISSING
                || missingColumns.isNotEmpty()
                || mismatchedColumns.isNotEmpty()
                || primaryKeyMismatch != null
                || missingForeignKeys.isNotEmpty()
                || missingCompositeForeignKeys.isNotEmpty()
                || missingIndexes.isNotEmpty()
```

In `diffTable()`, replace the FK-column comparison block (currently lines 108-111):

```kotlin
        val actualForeignKeyColumns = actual.foreignKeys.map { it.columnName }.toSet()

        val missingForeignKeys = expected.foreignKeys.filter { it.columnName !in actualForeignKeyColumns }
        val missingCompositeForeignKeys = expected.compositeForeignKeys.filter { composite ->
            composite.columnNames.any { it !in actualForeignKeyColumns }
        }

        val expectedForeignKeyColumns = expected.foreignKeys.map { it.columnName }
            .plus(expected.compositeForeignKeys.flatMap { it.columnNames })
            .toSet()
        val extraForeignKeys = actual.foreignKeys.map { it.columnName }.filter { it !in expectedForeignKeyColumns }
```

and update the `status` computation and the `TableDiff(...)` construction at the end of `diffTable()` to include `missingCompositeForeignKeys` (add it to both the `missingForeignKeys.isEmpty()` conjunction in `status` and as a constructor argument).

- [ ] **Step 2: Add the composite fix-SQL generator function to `SchemaFixSqlGenerator.kt`**

Add the import `org.maiaframework.gen.schema.ExpectedCompositeForeignKeyDef`. Next to the existing `addForeignKeyStatement` (around line 132-137), add:

```kotlin
    private fun addCompositeForeignKeyStatement(schemaAndTableName: String, foreignKey: ExpectedCompositeForeignKeyDef): String {

        val constraintName = "${bareTableName(schemaAndTableName)}_${foreignKey.columnNames.joinToString("_")}_fkey"
        val localColumns = foreignKey.columnNames.joinToString(", ")
        val referencedColumns = foreignKey.referencedColumns.joinToString(", ")

        return "ALTER TABLE $schemaAndTableName ADD CONSTRAINT $constraintName FOREIGN KEY ($localColumns) " +
            "REFERENCES ${foreignKey.referencedSchemaAndTable}($referencedColumns);"

    }
```

Wire it in next to the existing `table.missingForeignKeys.forEach { statements.add(addForeignKeyStatement(table.schemaAndTableName, it)) }` (line 65):

```kotlin
        table.missingCompositeForeignKeys.forEach { statements.add(addCompositeForeignKeyStatement(table.schemaAndTableName, it)) }
```

- [ ] **Step 3: Add a test to `SchemaComparatorTest.kt`**

```kotlin
    @Test
    fun `does not flag a real composite foreign key's columns as extra`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList(),
                compositeForeignKeys = listOf(
                    ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
                ),
            )
        )
        val actual = listOf(
            ActualTableDef(
                "app.child_history",
                emptyList(),
                listOf("id", "version"),
                foreignKeys = listOf(
                    ActualForeignKeyDef("parent_id", "app.parent_history", "id"),
                    ActualForeignKeyDef("parent_version", "app.parent_history", "version"),
                ),
                indexes = emptyList(),
            )
        )

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isFalse()
        assertThat(report.tables.single().extraForeignKeys).isEmpty()
        assertThat(report.tables.single().missingCompositeForeignKeys).isEmpty()

    }


    @Test
    fun `reports a missing composite foreign key as an error`() {

        val expected = listOf(
            ExpectedTableDef(
                "app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList(),
                compositeForeignKeys = listOf(
                    ExpectedCompositeForeignKeyDef(listOf("parent_id", "parent_version"), "app.parent_history", listOf("id", "version"))
                ),
            )
        )
        val actual = listOf(ActualTableDef("app.child_history", emptyList(), listOf("id", "version"), emptyList(), emptyList()))

        val report = comparator.compare(expected, actual)

        assertThat(report.hasErrors).isTrue()
        assertThat(report.tables.single().missingCompositeForeignKeys).hasSize(1)

    }
```

Add the import `org.maiaframework.gen.schema.ExpectedCompositeForeignKeyDef` to the test file.

- [ ] **Step 4: Add a test to `SchemaFixSqlGeneratorTest.kt`**

Check the existing single-column FK fix-SQL test in that file for the exact call pattern used (likely constructs a `TableDiff` and calls a `generate(...)`-style entrypoint), and add an analogous case asserting the generated statement is:

```
ALTER TABLE app.child_history ADD CONSTRAINT child_history_parent_id_parent_version_fkey FOREIGN KEY (parent_id, parent_version) REFERENCES app.parent_history(id, version);
```

- [ ] **Step 5: Run the tests**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "org.maiaframework.gen.schemacheck.SchemaComparatorTest" --tests "org.maiaframework.gen.schemacheck.SchemaFixSqlGeneratorTest"`
Expected: BUILD SUCCESSFUL, all tests pass.

---

## Task 6: Regenerate `maia-showcase` as the proof-of-concept and verify end-to-end

**Goal:** Regenerate `BravoWithHistory`/`AlphaWithHistory` (no spec changes needed — both are already `recordVersionHistory = true` with the FK between them) and confirm the whole pipeline works against a real Postgres schema.

**Files:**
- Regenerate (via `maiaGeneration`, not hand-edited): everything under `maia-showcase/{domain,dao,repo,service,web}/src/generated/`
- Possibly modify: `maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql` (or a new migration file — see Step 3)

**Acceptance Criteria:**
- [ ] `BravoWithHistoryHistoryEntity.kt` has a new `alphaVersion: Long` field.
- [ ] `BravoWithHistoryDao.kt` has `alphaWithHistoryDao: AlphaWithHistoryDao` injected, and `insertHistory`/`bulkInsertHistory` look up and pass through `alphaVersion`.
- [ ] `AlphaWithHistoryDao.kt` has a new `findVersionByPrimaryKey(id: DomainId): Long` function.
- [ ] The regenerated `create_entity_tables.sql` has `alpha_version bigint NOT NULL` on `bravo_with_history_history`, no more `REFERENCES maia.alpha_with_history(id)` on that table's `alpha_id` column, and a new `ALTER TABLE maia.bravo_with_history_history ADD CONSTRAINT ... FOREIGN KEY (alpha_id, alpha_version) REFERENCES maia.alpha_with_history_history(id, version);` line. `bravo_with_history` (the live table) is unchanged.
- [ ] Everything compiles.
- [ ] `ShowcaseSchemaCheckIntegrationTest` passes against the real (migrated) schema — no drift.

**Verify:** `./gradlew :maia-gen:maia-gen-schema-check:test --tests "org.maiaframework.gen.schemacheck.ShowcaseSchemaCheckIntegrationTest"` → BUILD SUCCESSFUL (requires Docker running, for the Testcontainers Postgres instance).

**Steps:**

- [ ] **Step 1: Regenerate the showcase modules**

Run, in the layer order documented in the root `CLAUDE.md` (spec → domain → dao → repo → service → web):

```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration \
    :maia-showcase:repo:maiaGeneration :maia-showcase:service:maiaGeneration :maia-showcase:web:maiaGeneration
```

- [ ] **Step 2: Spot-check the regenerated files against the acceptance criteria above**

Read `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/join/BravoWithHistoryHistoryEntity.kt`, `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/join/BravoWithHistoryDao.kt`, `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/join/AlphaWithHistoryDao.kt`, and `maia-showcase/dao/src/generated/sql/create_entity_tables.sql` and confirm each acceptance criterion above.

- [ ] **Step 3: Promote the DDL change into the Flyway migration**

Diff `maia-showcase/dao/src/generated/sql/create_entity_tables.sql` against `maia-showcase/dao/src/main/resources/db/migration/V003__create_entity_tables.sql`. Check `git log --oneline -- maia-showcase/dao/src/main/resources/db/migration/` for how the most recent schema-affecting change to this showcase was promoted (directly overwriting `V003`, or added as a new versioned migration file) and follow that same convention — this app's migration history is the authority on which approach this project actually uses, not a guess made here.

- [ ] **Step 4: Compile everything**

Run: `./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:dao:compileKotlin :maia-showcase:repo:compileKotlin :maia-showcase:service:compileKotlin :maia-showcase:web:compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Run the schema-check integration test**

Run: `./gradlew :maia-gen:maia-gen-schema-check:test --tests "org.maiaframework.gen.schemacheck.ShowcaseSchemaCheckIntegrationTest"`
Expected: BUILD SUCCESSFUL — `maia-showcase's real Flyway migrations match its real spec` passes, confirming the new composite FK constraint and column are both present in the real migrated DB and match what the generator now expects.

- [ ] **Step 6: Full build sanity check**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

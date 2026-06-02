# History Blotter Generator Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add automatic generation of a full history blotter stack for any entity with `recordVersionHistory = true`, surfaced via a "History" button on its detail view page.

**Architecture:** A new `EntityHistoryBlotterDef` class, computed as a lazy val on `EntityDef` when `withVersionHistory && !isHistoryEntity`, drives all rendering. Seven backend renderers emit Kotlin classes querying `maia.{entity}_history WHERE id = :entityId` via the existing `AgGridSearchModelConverter`. Seven frontend renderers emit Angular components with a component-scoped ag-grid datasource that receives the entity ID as an `@Input()`. Three existing renderers are updated to add the History button and route.

**Tech Stack:** Kotlin (maia-gen-spec, maia-gen-generator), Spring Boot (endpoint), Angular + ag-grid (frontend), Gradle maiaGeneration tasks

**User Verification:** NO

---

## Reference Files (read before starting)

- Spec pattern: `maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDetailViewDef.kt`
- Renderer pattern (Kotlin): `maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/SearchableDtoRepoRenderer.kt`
- Renderer pattern (TS): `maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AgGridDatasourceRenderer.kt`
- Generated DAO reference: `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleBlotterRowDtoDao.kt`
- Generated RowMapper reference: `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleBlotterRowDtoRowMapper.kt`
- Generated Meta reference: `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleBlotterRowDtoMeta.kt`
- Generated TS blotter reference: `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-blotter.ts`
- History entity RowMapper: `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryEntityRowMapper.kt`
- Existing EntityDetailViewPageHtmlRenderer: `maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageHtmlRenderer.kt`
- AnnotationDefs: `maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AnnotationDefs.kt`

---

## Task 1: Spec definition layer

**Goal:** Create `EntityHistoryBlotterDef`, wire it into `EntityDef` and `ModelDef`.

**Files:**
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt`

**Acceptance Criteria:**
- [ ] `EntityDef.historyBlotterDef` is non-null for `HistorySample` entity, null for `HistorySampleHistory` entity
- [ ] `EntityHistoryBlotterDef` exposes the correct 5 column field defs (version, changeType, someString, someInt, lastModifiedTimestampUtc) for HistorySample
- [ ] `ModelDef.entityHistoryBlotterDefs` returns one entry for the showcase model
- [ ] `./gradlew :maia-gen:maia-gen-spec:build` passes

**Verify:** `./gradlew :maia-gen:maia-gen-spec:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EntityHistoryBlotterDef.kt`**

```kotlin
package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.AnnotationDefs
import org.maiaframework.gen.spec.definition.builders.ClassDefBuilder.Companion.aClassDef
import org.maiaframework.gen.spec.definition.lang.AngularComponentNames
import org.maiaframework.gen.spec.definition.lang.ClassType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.PackageName


class EntityHistoryBlotterDef(val entityDef: EntityDef) {


    val historyEntityDef: EntityDef = entityDef.historyEntityDef!!


    val packageName: PackageName = entityDef.packageName


    // e.g. "HistorySampleHistoryBlotter"
    val historyBlotterBaseName: String = "${entityDef.entityBaseName}HistoryBlotter"


    // e.g. "maia.history_sample_history"
    val historyTableSchemaAndTable: String = historyEntityDef.schemaAndTableName


    // Columns: version, changeType, non-FK non-id entity data fields, lastModifiedTimestampUtc
    // Excludes: id (in URL), createdBy/lastModifiedBy (raw FK IDs), createdTimestampUtc
    val blotterColumns: List<EntityFieldDef> = historyEntityDef.entityFieldsNotInherited.filter { fieldDef ->
        val name = fieldDef.classFieldDef.classFieldName.value
        val isFK = fieldDef.classFieldDef.fieldType is ForeignKeyFieldType
        val isEntityId = name == "id"
        val isCreatedTimestamp = name == "createdTimestampUtc"
        !isFK && !isEntityId && !isCreatedTimestamp
    }


    // ── Backend Kotlin names ──────────────────────────────────────────────────

    val rowDtoUqcn = "${historyBlotterBaseName}RowDto"
    val rowMapperUqcn = "${historyBlotterBaseName}RowDtoRowMapper"
    val metaUqcn = "${historyBlotterBaseName}RowDtoMeta"
    val daoUqcn = "${historyBlotterBaseName}RowDtoDao"
    val repoUqcn = "${historyBlotterBaseName}RowDtoRepo"
    val searchServiceUqcn = "${historyBlotterBaseName}RowDtoSearchService"
    val endpointUqcn = "${historyBlotterBaseName}SearchEndpoint"

    val rowDtoClassDef = aClassDef(packageName.uqcn(rowDtoUqcn))
        .ofType(ClassType.DATA_CLASS)
        .withFieldDefsNotInherited(blotterColumns.map { it.classFieldDef })
        .build()

    val rowMapperClassDef = aClassDef(packageName.uqcn(rowMapperUqcn)).build()

    val metaClassDef = aClassDef(packageName.uqcn(metaUqcn)).build()

    val daoClassDef = aClassDef(packageName.uqcn(daoUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()

    val repoClassDef = aClassDef(packageName.uqcn(repoUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REPOSITORY)
        .build()

    val searchServiceClassDef = aClassDef(packageName.uqcn(searchServiceUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_SERVICE)
        .build()

    val endpointClassDef = aClassDef(packageName.uqcn(endpointUqcn))
        .withClassAnnotation(AnnotationDefs.SPRING_REST_CONTROLLER)
        .build()


    // ── Endpoint paths ────────────────────────────────────────────────────────

    private val entityKebab = entityDef.entityBaseName.toKebabCase()

    val searchEndpointPath = "/api/${entityKebab}/{entityId}/history/search"
    val countEndpointPath = "/api/${entityKebab}/{entityId}/history/count"


    // ── Frontend names ────────────────────────────────────────────────────────

    val pageTitle = "${entityDef.entityBaseName.value} History"

    // Route path WITHOUT :id — routes renderer appends /:id
    val routePath = "${entityKebab}/history"

    val blotterComponentNames = AngularComponentNames(packageName, historyBlotterBaseName)
    val blotterPageComponentNames = AngularComponentNames(packageName, "${historyBlotterBaseName}Page")

    val datasourceClassName = "${historyBlotterBaseName}AgGridDatasource"
    val serviceClassName = "${historyBlotterBaseName}Service"
    val tsRowDtoClassName = "${historyBlotterBaseName}RowDto"

    // TypeScript template literal URL with ${this.entityId} (literal TS interpolation)
    // In Kotlin $$"..." strings: $ is literal, $${} is interpolation
    val searchEndpointUrlForTypescript = $$/api/$${entityKebab}/${this.entityId}/history/search"

}
```

- [ ] **Step 2: Add `historyBlotterDef` to `EntityDef.kt`**

Find the block that defines `historyEntityDef` (around line 428). Add immediately after the closing `} else { null }` of `historyEntityDef`:

```kotlin
    val historyBlotterDef: EntityHistoryBlotterDef? =
        if (withVersionHistory.value && !isHistoryEntity) EntityHistoryBlotterDef(this)
        else null
```

- [ ] **Step 3: Add `entityHistoryBlotterDefs` to `ModelDef.kt`**

In `ModelDef.kt`, `entityDefs` is a private val. Add a new computed property after the existing computed properties:

```kotlin
    val entityHistoryBlotterDefs: List<EntityHistoryBlotterDef> =
        entityDefs.mapNotNull { it.historyBlotterDef }
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :maia-gen:maia-gen-spec:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt \
        maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt \
        maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt
git commit -m "feat: add EntityHistoryBlotterDef spec class"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityHistoryBlotterDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-spec:build", "acceptanceCriteria": ["EntityDef.historyBlotterDef non-null for HistorySample", "blotterColumns has 5 entries for HistorySample", "maia-gen-spec builds"], "requiresUserVerification": false}
```

---

## Task 2: Backend domain artifacts

**Goal:** Generate `HistorySampleHistoryBlotterRowDto.kt`, its row mapper, and its meta object. Wire into `DomainModuleGenerator`.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowMapperRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoMetaRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:domain:maiaGeneration` produces `HistorySampleHistoryBlotterRowDto.kt`, `HistorySampleHistoryBlotterRowDtoRowMapper.kt`, `HistorySampleHistoryBlotterRowDtoMeta.kt` in `src/generated/kotlin/main/org/maiaframework/showcase/history/`
- [ ] Generated DTO has fields: `changeType: ChangeType`, `lastModifiedTimestampUtc: Instant`, `someInt: Int`, `someString: String`, `version: Long` (sorted alphabetically as the class builder does)
- [ ] Meta has `fieldNameToColumnName` mapping for all 5 fields
- [ ] `./gradlew :maia-showcase:domain:build` passes

**Verify:** `./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:domain:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EntityHistoryBlotterRowDtoRenderer.kt`**

The DTO is a `data class`. Use `DtoRenderer` which already handles `DATA_CLASS` type — just pass the prepared `ClassDef` from `EntityHistoryBlotterDef.rowDtoClassDef`.

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef

class EntityHistoryBlotterRowDtoRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.rowDtoClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()

        // imports derived from field types
        def.blotterColumns.forEach { col ->
            val fqcn = col.classFieldDef.fieldType.fqcn
            if (fqcn != null && !fqcn.toString().startsWith("kotlin.") && !fqcn.toString().startsWith("java.lang.")) {
                appendLine("import $fqcn")
            }
        }
        blankLine()
        blankLine()

        val fieldLines = def.blotterColumns
            .sortedBy { it.classFieldDef.classFieldName.value }
            .joinToString(",\n") { col ->
                "    val ${col.classFieldDef.classFieldName.value}: ${col.classFieldDef.fieldType.unqualifiedToString}"
            }

        append("""
            |data class ${def.rowDtoUqcn}(
            |$fieldLines
            |) {
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

> **Note:** The existing `DtoRenderer` is a cleaner option. You can alternatively instantiate `DtoRenderer(def.rowDtoClassDef).renderToDir(outputDir)` directly from `DomainModuleGenerator` without a new renderer class — the `ClassDef` already has the field defs and `DATA_CLASS` type. If you take this route, skip creating `EntityHistoryBlotterRowDtoRenderer.kt` entirely.

- [ ] **Step 2: Create `EntityHistoryBlotterRowMapperRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.RowMapperFunctions
import org.maiaframework.gen.spec.definition.lang.EnumFieldType


class EntityHistoryBlotterRowMapperRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.rowMapperClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        val sortedColumns = def.blotterColumns.sortedBy { it.classFieldDef.classFieldName.value }

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()

        // Imports
        sortedColumns.forEach { col ->
            val fqcn = col.classFieldDef.fieldType.fqcn
            if (fqcn != null && !fqcn.toString().startsWith("kotlin.") && !fqcn.toString().startsWith("java.lang.")) {
                appendLine("import $fqcn")
            }
        }
        appendLine("import org.maiaframework.jdbc.MaiaRowMapper")
        appendLine("import org.maiaframework.jdbc.ResultSetAdapter")
        blankLine()
        blankLine()

        appendLine("class ${def.rowMapperUqcn} : MaiaRowMapper<${def.rowDtoUqcn}> {")
        blankLine()
        blankLine()
        appendLine("    override fun mapRow(rsa: ResultSetAdapter): ${def.rowDtoUqcn} {")
        blankLine()

        sortedColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val columnName = col.tableColumnName.value
            val fieldType = col.classFieldDef.fieldType
            val readExpr = when {
                fieldType is EnumFieldType -> "rsa.readEnum(\"$columnName\", ${fieldType.enumDef.uqcn}::class.java)"
                fieldName == "version" -> "rsa.readLong(\"$columnName\")"
                fieldType.unqualifiedToString == "Int" -> "rsa.readInt(\"$columnName\")"
                fieldType.unqualifiedToString == "String" -> "rsa.readString(\"$columnName\")"
                fieldType.unqualifiedToString == "Instant" -> "rsa.readInstant(\"$columnName\")"
                else -> "rsa.read(\"$columnName\") as ${fieldType.unqualifiedToString}"
            }
            appendLine("        val $fieldName = $readExpr")
        }

        blankLine()
        appendLine("        return ${def.rowDtoUqcn}(")
        sortedColumns.forEachIndexed { i, col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val comma = if (i < sortedColumns.size - 1) "," else ""
            appendLine("            $fieldName$comma")
        }
        appendLine("        )")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")
        blankLine()

        return sourceCode.toString()

    }


}
```

- [ ] **Step 3: Create `EntityHistoryBlotterRowDtoMetaRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterRowDtoMetaRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.metaClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        val sortedColumns = def.blotterColumns.sortedBy { it.classFieldDef.classFieldName.value }
        val fieldNames = sortedColumns.map { it.classFieldDef.classFieldName.value }

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()
        appendLine("import org.maiaframework.jdbc.JdbcCompatibleType")
        blankLine()
        blankLine()

        appendLine("object ${def.metaUqcn} {")
        blankLine()
        blankLine()
        appendLine("    fun fieldNameToColumnName(dtoFieldName: String): String {")
        blankLine()
        appendLine("        return when(dtoFieldName) {")

        sortedColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val fullColumn = "${def.historyTableSchemaAndTable}.${col.tableColumnName.value}"
            appendLine("            \"$fieldName\" -> \"$fullColumn\"")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown field name [\$dtoFieldName]. Expected one of $fieldNames\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    fun fieldNameToJdbcType(dtoFieldName: String): JdbcCompatibleType {")
        blankLine()
        appendLine("        return when(dtoFieldName) {")

        sortedColumns.forEach { col ->
            val fieldName = col.classFieldDef.classFieldName.value
            val jdbcType = col.classFieldDef.fieldType.jdbcCompatibleType!!.name
            appendLine("            \"$fieldName\" -> JdbcCompatibleType.$jdbcType")
        }

        appendLine("            else -> throw IllegalArgumentException(\"Unknown field name [\$dtoFieldName]. Expected one of $fieldNames\")")
        appendLine("        }")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")
        blankLine()

        return sourceCode.toString()

    }


}
```

- [ ] **Step 4: Wire into `DomainModuleGenerator.kt`**

Find the `onGenerateSource()` method. Add a call to a new `renderEntityHistoryBlotterDomainArtifacts()` private method:

```kotlin
// In onGenerateSource():
renderEntityHistoryBlotterDomainArtifacts()
```

Add the private method:

```kotlin
private fun renderEntityHistoryBlotterDomainArtifacts() {
    this.modelDef.entityHistoryBlotterDefs.forEach { def ->
        DtoRenderer(def.rowDtoClassDef).renderToDir(this.kotlinOutputDir)
        EntityHistoryBlotterRowMapperRenderer(def).renderToDir(this.kotlinOutputDir)
        EntityHistoryBlotterRowDtoMetaRenderer(def).renderToDir(this.kotlinOutputDir)
    }
}
```

> If you created `EntityHistoryBlotterRowDtoRenderer.kt`, use that instead of `DtoRenderer`. Using `DtoRenderer` directly is preferred.

- [ ] **Step 5: Run and verify**

```bash
./gradlew :maia-showcase:domain:maiaGeneration
```

Check that three files were created:
```
maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDto.kt
maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDtoRowMapper.kt
maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDtoMeta.kt
```

Then build:
```bash
./gradlew :maia-showcase:domain:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowMapperRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoMetaRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt \
        maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/history/
git commit -m "feat: generate history blotter domain artifacts (DTO, RowMapper, Meta)"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowMapperRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoMetaRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt"], "verifyCommand": "./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:domain:build", "acceptanceCriteria": ["HistorySampleHistoryBlotterRowDto.kt generated with 5 fields", "HistorySampleHistoryBlotterRowDtoMeta.kt has fieldNameToColumnName for version/changeType/someString/someInt/lastModifiedTimestampUtc", "domain build passes"], "requiresUserVerification": false}
```

---

## Task 3: Backend DAO and Repo

**Goal:** Generate `HistorySampleHistoryBlotterRowDtoDao.kt` and `HistorySampleHistoryBlotterRowDtoRepo.kt`. Wire into `RepoLayerModuleGenerator`.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoDaoRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRepoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/RepoLayerModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] Generated DAO has `search(entityId, searchModel)` and `count(entityId, searchModel)` methods
- [ ] DAO SQL includes `WHERE ${schemaAndTable}.id = :entityId` before ag-grid filter clause
- [ ] `./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:dao:build` passes

**Verify:** `./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:dao:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EntityHistoryBlotterRowDtoDaoRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterRowDtoDaoRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.daoClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        val sortedColumns = def.blotterColumns.sortedBy { it.classFieldDef.classFieldName.value }
        val table = def.historyTableSchemaAndTable

        val selectCols = sortedColumns.joinToString(",\n") { col ->
            "                $table.${col.tableColumnName.value} as ${col.classFieldDef.classFieldName.value}"
        }

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()
        appendLine("import org.maiaframework.domain.DomainId")
        appendLine("import org.maiaframework.domain.search.AgGridSearchModel")
        appendLine("import org.maiaframework.domain.search.SearchResultPage")
        appendLine("import org.maiaframework.jdbc.JdbcOps")
        appendLine("import org.maiaframework.jdbc.SqlParams")
        appendLine("import org.maiaframework.jdbc.search.AgGridSearchModelConverter")
        appendLine("import org.springframework.stereotype.Repository")
        blankLine()
        blankLine()

        append("""
            |@Repository
            |class ${def.daoUqcn}(
            |    private val jdbcOps: JdbcOps
            |) {
            |
            |
            |    private val dtoRowMapper = ${def.rowMapperUqcn}()
            |
            |
            |    private val searchModelConverter = AgGridSearchModelConverter(
            |        ${def.metaUqcn}::fieldNameToColumnName,
            |        ${def.metaUqcn}::fieldNameToJdbcType
            |    )
            |
            |
            |    fun search(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val agGridClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams)
            |        val offsetAndLimitClause = this.searchModelConverter.buildOffsetAndLimitFor(searchModel)
            |        val orderByClause = this.searchModelConverter.buildOrderByClause(searchModel)
            |
            |        val entityIdClause = "$table.id = :entityId"
            |        val whereClause = if (agGridClause.startsWith("where ")) "${'$'}{agGridClause} and ${'$'}{entityIdClause}" else "where ${'$'}{entityIdClause}"
            |
            |        val sqlForTotalCount = ""${'"'}
            |            select count(*)
            |            from $table
            |            ${'$'}whereClause
            |            ""${'"'}.trimIndent()
            |
            |        val sqlForPage = ""${'"'}
            |            select
            |$selectCols
            |            from $table
            |            ${'$'}whereClause
            |            ${'$'}orderByClause
            |            ${'$'}offsetAndLimitClause
            |            ""${'"'}.trimIndent()
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
            |
            |
            |    fun count(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        val sqlParams = SqlParams()
            |        sqlParams.addValue("entityId", entityId)
            |        val agGridClause = this.searchModelConverter.buildWhereClauseFor(searchModel.filterModel, sqlParams)
            |
            |        val entityIdClause = "$table.id = :entityId"
            |        val whereClause = if (agGridClause.startsWith("where ")) "${'$'}{agGridClause} and ${'$'}{entityIdClause}" else "where ${'$'}{entityIdClause}"
            |
            |        val sqlForTotalCount = ""${'"'}
            |            select count(*)
            |            from $table
            |            ${'$'}whereClause
            |            ""${'"'}.trimIndent()
            |
            |        return this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

> **Note on triple-quotes in Kotlin renderers:** The pattern `""${'"'}` inside a `trimMargin()` block produces a literal `"""` in the generated output. This is the same pattern used throughout the existing renderers.

> **Note on WHERE clause composition:** `AgGridSearchModelConverter.buildWhereClauseFor` returns either an empty string or a clause starting with `where`. The above code prepends the `id = :entityId` constraint. Verify the actual return format by reading `AgGridSearchModelConverter` before committing — adjust the clause composition accordingly. The safe pattern is to always use `WHERE $table.id = :entityId AND (ag-grid sub-clause or 1=1)`.

- [ ] **Step 2: Create `EntityHistoryBlotterRowDtoRepoRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterRowDtoRepoRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.repoClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()
        appendLine("import org.maiaframework.domain.DomainId")
        appendLine("import org.maiaframework.domain.search.AgGridSearchModel")
        appendLine("import org.maiaframework.domain.search.AgGridSearchableException")
        appendLine("import org.maiaframework.domain.search.SearchResultPage")
        appendLine("import org.springframework.stereotype.Repository")
        blankLine()
        blankLine()

        append("""
            |@Repository
            |class ${def.repoUqcn}(
            |    private val dao: ${def.daoUqcn}
            |) {
            |
            |
            |    fun getRows(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        try {
            |            return this.dao.search(entityId, searchModel)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |
            |
            |    fun countRows(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        try {
            |            return this.dao.count(entityId, searchModel)
            |        } catch (e: Exception) {
            |            throw AgGridSearchableException(searchModel, e)
            |        }
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

- [ ] **Step 3: Wire into `RepoLayerModuleGenerator.kt`**

In `onGenerateSource()`, add:
```kotlin
renderEntityHistoryBlotterRepoArtifacts()
```

Add the private method:
```kotlin
private fun renderEntityHistoryBlotterRepoArtifacts() {
    this.modelDef.entityHistoryBlotterDefs.forEach { def ->
        EntityHistoryBlotterRowDtoDaoRenderer(def).renderToDir(this.kotlinOutputDir)
        EntityHistoryBlotterRowDtoRepoRenderer(def).renderToDir(this.kotlinOutputDir)
    }
}
```

- [ ] **Step 4: Verify**

```bash
./gradlew :maia-showcase:dao:maiaGeneration
```

Check generated files exist:
```
maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDtoDao.kt
maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDtoRepo.kt
```

```bash
./gradlew :maia-showcase:dao:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoDaoRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRepoRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/RepoLayerModuleGenerator.kt \
        maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/history/
git commit -m "feat: generate history blotter DAO and Repo"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoDaoRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterRowDtoRepoRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/RepoLayerModuleGenerator.kt"], "verifyCommand": "./gradlew :maia-showcase:dao:maiaGeneration :maia-showcase:dao:build", "acceptanceCriteria": ["DAO generated with search() and count() taking entityId", "WHERE clause includes id = :entityId", "dao build passes"], "requiresUserVerification": false}
```

---

## Task 4: Backend Service and Endpoint

**Goal:** Generate `HistorySampleHistoryBlotterRowDtoSearchService.kt` and `HistorySampleHistoryBlotterSearchEndpoint.kt`. Wire into `ServiceLayerModuleGenerator` and `WebLayerModuleGenerator`.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchServiceRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchEndpointRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/ServiceLayerModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/WebLayerModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] Generated endpoint has `POST ${entityKebab}/{entityId}/history/search` and `POST ${entityKebab}/{entityId}/history/count`
- [ ] `@PathVariable entityId: DomainId` is passed through to the search service and repo
- [ ] `./gradlew :maia-showcase:service:maiaGeneration :maia-showcase:service:build` passes
- [ ] `./gradlew :maia-showcase:web:maiaGeneration :maia-showcase:web:build` passes

**Verify:** `./gradlew :maia-showcase:service:maiaGeneration :maia-showcase:service:build :maia-showcase:web:maiaGeneration :maia-showcase:web:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EntityHistoryBlotterSearchServiceRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterSearchServiceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.searchServiceClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()
        appendLine("import org.maiaframework.domain.DomainId")
        appendLine("import org.maiaframework.domain.search.AgGridSearchModel")
        appendLine("import org.maiaframework.domain.search.SearchResultPage")
        appendLine("import org.springframework.stereotype.Service")
        blankLine()
        blankLine()

        append("""
            |@Service
            |class ${def.searchServiceUqcn}(
            |    private val repo: ${def.repoUqcn}
            |) {
            |
            |
            |    fun search(entityId: DomainId, searchModel: AgGridSearchModel): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.repo.getRows(entityId, searchModel)
            |
            |    }
            |
            |
            |    fun count(entityId: DomainId, searchModel: AgGridSearchModel): Long {
            |
            |        return this.repo.countRows(entityId, searchModel)
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

- [ ] **Step 2: Create `EntityHistoryBlotterSearchEndpointRenderer.kt`**

```kotlin
package org.maiaframework.gen.renderers

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterSearchEndpointRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractSourceFileRenderer() {


    override fun renderedFilePath(): String =
        def.endpointClassDef.fqcn.toString().replace(".", "/") + ".kt"


    override fun renderSource(): String {

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        appendLine("package ${def.packageName}")
        blankLine()
        appendLine("import org.maiaframework.domain.DomainId")
        appendLine("import org.maiaframework.domain.search.AgGridSearchModel")
        appendLine("import org.maiaframework.domain.search.SearchResultPage")
        appendLine("import org.springframework.http.MediaType")
        appendLine("import org.springframework.web.bind.annotation.PathVariable")
        appendLine("import org.springframework.web.bind.annotation.PostMapping")
        appendLine("import org.springframework.web.bind.annotation.RequestBody")
        appendLine("import org.springframework.web.bind.annotation.RestController")
        blankLine()
        blankLine()

        append("""
            |@RestController
            |class ${def.endpointUqcn}(
            |    private val searchService: ${def.searchServiceUqcn}
            |) {
            |
            |
            |    @PostMapping("${def.searchEndpointPath}", produces = [MediaType.APPLICATION_JSON_VALUE])
            |    fun search(
            |        @PathVariable entityId: DomainId,
            |        @RequestBody searchModel: AgGridSearchModel
            |    ): SearchResultPage<${def.rowDtoUqcn}> {
            |
            |        return this.searchService.search(entityId, searchModel)
            |
            |    }
            |
            |
            |    @PostMapping("${def.countEndpointPath}")
            |    fun count(
            |        @PathVariable entityId: DomainId,
            |        @RequestBody searchModel: AgGridSearchModel
            |    ): Long {
            |
            |        return this.searchService.count(entityId, searchModel)
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

- [ ] **Step 3: Wire into `ServiceLayerModuleGenerator.kt`**

In `onGenerateSource()`, add:
```kotlin
renderEntityHistoryBlotterServices()
```

Add the private method:
```kotlin
private fun renderEntityHistoryBlotterServices() {
    this.modelDef.entityHistoryBlotterDefs.forEach { def ->
        EntityHistoryBlotterSearchServiceRenderer(def).renderToDir(this.kotlinOutputDir)
    }
}
```

- [ ] **Step 4: Wire into `WebLayerModuleGenerator.kt`**

In `onGenerateSource()`, add:
```kotlin
renderEntityHistoryBlotterEndpoints()
```

Add the private method:
```kotlin
private fun renderEntityHistoryBlotterEndpoints() {
    this.modelDef.entityHistoryBlotterDefs.forEach { def ->
        EntityHistoryBlotterSearchEndpointRenderer(def).renderToDir(this.kotlinOutputDir)
    }
}
```

- [ ] **Step 5: Verify**

```bash
./gradlew :maia-showcase:service:maiaGeneration :maia-showcase:service:build
./gradlew :maia-showcase:web:maiaGeneration :maia-showcase:web:build
```
Expected: `BUILD SUCCESSFUL` for each.

- [ ] **Step 6: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchServiceRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchEndpointRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/ServiceLayerModuleGenerator.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/WebLayerModuleGenerator.kt \
        maia-showcase/service/src/generated/ \
        maia-showcase/web/src/generated/
git commit -m "feat: generate history blotter service and endpoint"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchServiceRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/EntityHistoryBlotterSearchEndpointRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/ServiceLayerModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/WebLayerModuleGenerator.kt"], "verifyCommand": "./gradlew :maia-showcase:service:build :maia-showcase:web:build", "acceptanceCriteria": ["endpoint POST /api/history-sample/{entityId}/history/search exists", "entityId DomainId path variable passed to service", "service and web builds pass"], "requiresUserVerification": false}
```

---

## Task 5: Frontend artifacts

**Goal:** Generate 7 TypeScript/HTML files for the history blotter. Wire into `AngularUiModuleGenerator`.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterRowDtoTypescriptRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterServiceRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterAgGridDatasourceRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterComponentRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterHtmlRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageComponentRenderer.kt`
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` produces all 7 files under `src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/`
- [ ] Datasource is `@Injectable()` (no `providedIn: 'root'`) with `setEntityId(id: string)` method
- [ ] Blotter component has `@Input() entityId!: string`, `ngOnInit`, and `onGridReady`
- [ ] `./gradlew :maia-showcase:maia-showcase-ui:build` passes (Angular compilation)

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration :maia-showcase:maia-showcase-ui:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EntityHistoryBlotterRowDtoTypescriptRenderer.kt`**

Reference: `TypescriptInterfaceDtoRenderer` or read `HistorySampleBlotterRowDto.ts` for the pattern.

The generated output should be:
```typescript
export interface HistorySampleHistoryBlotterRowDto {
    changeType: string;
    lastModifiedTimestampUtc: string;
    someInt: number;
    someString: string;
    version: number;
}
```

Map Kotlin types to TypeScript: `String` → `string`, `Int`/`Long` → `number`, `Instant` → `string`, enum → `string`.

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType


class EntityHistoryBlotterRowDtoTypescriptRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String =
        "${def.blotterComponentNames.genComponentsBaseDir}/${def.tsRowDtoClassName}.ts"


    override fun renderSource(): String {

        val sortedColumns = def.blotterColumns.sortedBy { it.classFieldDef.classFieldName.value }

        appendLine("// This source was generated by the Maia Framework code generator")
        appendLine("// Renderer class: class ${this::class.qualifiedName}")
        blankLine()
        blankLine()

        appendLine("export interface ${def.tsRowDtoClassName} {")
        sortedColumns.forEach { col ->
            val tsType = when (col.classFieldDef.fieldType) {
                is IntFieldType, is LongFieldType -> "number"
                is InstantFieldType -> "string"
                is EnumFieldType -> "string"
                is StringFieldType -> "string"
                else -> "any"
            }
            appendLine("    ${col.classFieldDef.classFieldName.value}: $tsType;")
        }
        appendLine("}")
        blankLine()

        return sourceCode.toString()

    }


}
```

> **Note on `genComponentsBaseDir`**: Look at `AngularComponentNames` — it has a `genComponentsBaseDir` property derived from the package name. Use that to derive the rendered file path, consistent with other TS renderers.

- [ ] **Step 2: Create `EntityHistoryBlotterServiceRenderer.kt`**

Pattern: `BlotterServiceTypescriptRenderer`. The service calls the search endpoint with `entityId` as a path param.

Generated output target:
```typescript
// history-sample-history-blotter-service.ts
@Injectable({providedIn: 'root'})
export class HistorySampleHistoryBlotterService {

    private readonly http = inject(HttpClient);

    public search(entityId: string, searchModel: any): Observable<SearchResultPage<HistorySampleHistoryBlotterRowDto>> {
        return this.http.post<SearchResultPage<HistorySampleHistoryBlotterRowDto>>(
            `/api/history-sample/${entityId}/history/search`,
            searchModel,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
        );
    }
}
```

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterServiceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String =
        def.blotterComponentNames.serviceRenderedFilePath


    override fun renderSource(): String {

        addImport("@angular/common/http", "HttpClient")
        addImport("@angular/common/http", "HttpHeaders")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("rxjs", "Observable")
        addImport("@maia/maia-ui", "SearchResultPage")
        addImport(def.blotterComponentNames.genComponentsBaseDir.let {
            "${it}/${def.tsRowDtoClassName}"
        }.let { org.maiaframework.gen.spec.definition.lang.TypescriptImport(def.tsRowDtoClassName, "@app/${def.blotterComponentNames.genComponentsBaseDir}/${def.tsRowDtoClassName}") })

        append("""
            |
            |
            |@Injectable({providedIn: 'root'})
            |export class ${def.serviceClassName} {
            |
            |
            |    private httpOptions = {
            |        headers: new HttpHeaders({
            |            'Content-Type': 'application/json'
            |        })
            |    };
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    public search(entityId: string, searchModel: any): Observable<SearchResultPage<${def.tsRowDtoClassName}>> {
            |
            |        return this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(
            |            `${def.searchEndpointUrlForTypescript}`,
            |            searchModel,
            |            this.httpOptions
            |        );
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

> **Note:** For the TypeScript import of `HistorySampleHistoryBlotterRowDto`, check how `BlotterServiceTypescriptRenderer` imports the row DTO — use the same pattern with `TypescriptImport`.

- [ ] **Step 3: Create `EntityHistoryBlotterAgGridDatasourceRenderer.kt`**

The datasource is `@Injectable()` (no `providedIn: 'root'`), has `entityId: string` field and `setEntityId` method, and uses a template literal URL.

Generated output target:
```typescript
// HistorySampleHistoryBlotterAgGridDatasource.ts
@Injectable()
export class HistorySampleHistoryBlotterAgGridDatasource implements IDatasource {

    rowCount?: number = undefined;
    private entityId!: string;

    private readonly http = inject(HttpClient);

    setEntityId(id: string): void {
        this.entityId = id;
    }

    getRows(params: IGetRowsParams): void {
        this.http.post<SearchResultPage<HistorySampleHistoryBlotterRowDto>>(
            `/api/history-sample/${this.entityId}/history/search`,
            params
        ).subscribe({
            next: searchResultPage => params.successCallback(searchResultPage.results, searchResultPage.totalResultCount)
        });
    }
}
```

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef


class EntityHistoryBlotterAgGridDatasourceRenderer(
    private val def: EntityHistoryBlotterDef
) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String =
        "${def.blotterComponentNames.genDir}/${def.datasourceClassName}.ts"


    override fun renderSource(): String {

        addImport("@angular/core", "inject")
        addImport("@angular/core", "Injectable")
        addImport("@angular/common/http", "HttpClient")
        addImport("ag-grid-community", "IDatasource")
        addImport("ag-grid-community", "IGetRowsParams")
        addImport("@maia/maia-ui", "SearchResultPage")
        // TS DTO import — use same pattern as AgGridDatasourceRenderer

        append("""
            |
            |
            |@Injectable()
            |export class ${def.datasourceClassName} implements IDatasource {
            |
            |
            |    rowCount?: number = undefined;
            |
            |
            |    private entityId!: string;
            |
            |
            |    private readonly http = inject(HttpClient);
            |
            |
            |    setEntityId(id: string): void {
            |        this.entityId = id;
            |    }
            |
            |
            |    getRows(params: IGetRowsParams): void {
            |
            |        this.http.post<SearchResultPage<${def.tsRowDtoClassName}>>(
            |            `${def.searchEndpointUrlForTypescript}`,
            |            params
            |        ).subscribe({
            |           next: searchResultPage => params.successCallback(searchResultPage.results, searchResultPage.totalResultCount)
            |        });
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

        return sourceCode.toString()

    }


}
```

> **Note on `genDir` vs `genComponentsBaseDir`**: The `AngularComponentNames` class has both. For TypeScript class files (not component files), look at what `AgGridDatasourceRenderer` uses for its `renderedFilePath()`. Use the same property for consistency.

- [ ] **Step 4: Create `EntityHistoryBlotterComponentRenderer.kt`**

The blotter component:
- Imports `OnInit`, takes `@Input() entityId`
- Provides the datasource in `providers`
- Calls `datasource.setEntityId(entityId)` in `ngOnInit`
- Sets the datasource on the grid in `onGridReady`
- Has ag-grid column defs from `def.blotterColumns`

Generated output target (abridged):
```typescript
@Component({
    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
    providers: [HistorySampleHistoryBlotterAgGridDatasource],
    selector: 'app-history-sample-history-blotter',
    templateUrl: './history-sample-history-blotter.html'
})
export class HistorySampleHistoryBlotter implements OnInit {

    @Input() entityId!: string;

    public columnDefs: ColDef[] = [
        { field: 'version', headerName: 'Version', cellDataType: 'number', filter: true },
        { field: 'changeType', headerName: 'Change Type', cellDataType: 'text', filter: true },
        { field: 'someString', headerName: 'Some String', cellDataType: 'text', filter: true },
        { field: 'someInt', headerName: 'Some Int', cellDataType: 'number', filter: true },
        { field: 'lastModifiedTimestampUtc', headerName: 'Last Modified At', cellDataType: 'text', filter: true },
    ];
    // ... standard ag-grid properties (rowBuffer, rowModelType, etc.) matching HistorySampleBlotter
    
    private readonly datasource = inject(HistorySampleHistoryBlotterAgGridDatasource);

    ngOnInit(): void {
        this.datasource.setEntityId(this.entityId);
    }

    onGridReady(params: GridReadyEvent): void {
        params.api.setGridOption('datasource', this.datasource);
    }
}
```

For the column defs, iterate `def.blotterColumns` sorted by field name. Map `fieldType` to ag-grid `cellDataType`: `Int`/`Long` → `'number'`, all others → `'text'`. Use `col.classFieldDef.displayName?.value ?: col.classFieldDef.classFieldName.value.humanize()` for the header name.

The `headerName` for display: look at how `AgGridBlotterComponentRenderer` derives it from the blotter column display names — follow the same approach.

- [ ] **Step 5: Create `EntityHistoryBlotterHtmlRenderer.kt`**

Generated output:
```html
<div class="h-screen">
    <ag-grid-angular
        style="width: 100%; height: 100%;"
        [columnDefs]="columnDefs"
        [defaultColDef]="defaultColDef"
        [rowBuffer]="rowBuffer"
        [rowSelection]="rowSelection"
        [rowModelType]="rowModelType"
        [cacheBlockSize]="cacheBlockSize"
        [cacheOverflowSize]="cacheOverflowSize"
        [maxConcurrentDatasourceRequests]="maxConcurrentDatasourceRequests"
        [infiniteInitialRowCount]="infiniteInitialRowCount"
        [maxBlocksInCache]="maxBlocksInCache"
        [rowData]="rowData"
        [theme]="agGridTheme"
        (gridReady)="onGridReady($event)"
    ></ag-grid-angular>
</div>
```

No "Add" button (history is read-only).

- [ ] **Step 6: Create `EntityHistoryBlotterPageComponentRenderer.kt`**

The page component reads `:id` from the route and passes it as `[entityId]` to the blotter.

Generated output target:
```typescript
@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout, HistorySampleHistoryBlotter],
    selector: 'app-history-sample-history-blotter-page',
    templateUrl: './history-sample-history-blotter-page.html'
})
export class HistorySampleHistoryBlotterPage {

    private readonly route = inject(ActivatedRoute);

    protected readonly entityId = toSignal(
        this.route.paramMap.pipe(map(p => p.get('id')))
    );
}
```

- [ ] **Step 7: Create `EntityHistoryBlotterPageHtmlRenderer.kt`**

Generated output:
```html
<app-page-layout pageTitle="History Sample History" dataPageId="history_sample_history_blotter">
    @if (entityId(); as id) {
        <app-history-sample-history-blotter [entityId]="id" />
    }
</app-page-layout>
```

- [ ] **Step 8: Wire all 7 renderers into `AngularUiModuleGenerator.kt`**

In `onGenerateSource()`, add:
```kotlin
renderEntityHistoryBlotters()
```

Add the private method:
```kotlin
private fun renderEntityHistoryBlotters() {
    this.modelDef.entityHistoryBlotterDefs.forEach { def ->
        EntityHistoryBlotterRowDtoTypescriptRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterServiceRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterAgGridDatasourceRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterComponentRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterHtmlRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterPageComponentRenderer(def).renderToDir(this.typescriptOutputDir)
        EntityHistoryBlotterPageHtmlRenderer(def).renderToDir(this.typescriptOutputDir)
    }
}
```

- [ ] **Step 9: Verify**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Check all 7 files exist:
```
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/HistorySampleHistoryBlotterRowDto.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-history-blotter-service.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/HistorySampleHistoryBlotterAgGridDatasource.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-history-blotter.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-history-blotter.html
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-history-blotter-page.ts
maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-history-blotter-page.html
```

Then build the Angular app:
```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotter* \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt \
        maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: generate history blotter frontend artifacts"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterRowDtoTypescriptRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterServiceRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterAgGridDatasourceRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterComponentRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageComponentRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityHistoryBlotterPageHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration :maia-showcase:maia-showcase-ui:build", "acceptanceCriteria": ["all 7 TS/HTML files generated", "datasource is @Injectable() not @Injectable({providedIn: root})", "blotter component has @Input() entityId", "Angular build passes"], "requiresUserVerification": false}
```

---

## Task 6: Navigation — History button and route

**Goal:** Update `EntityDetailViewPageHtmlRenderer`, `EntityDetailViewPageComponentRenderer`, and `EntityCrudRoutesRenderer` to add the History button and route when `historyBlotterDef != null`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageComponentRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCrudRoutesRenderer.kt`

**Acceptance Criteria:**
- [ ] Regenerated `history-sample-entity-detail-view-page.html` contains the History button
- [ ] Regenerated `history-sample-entity-detail-view-page.ts` contains `onHistoryClicked()`
- [ ] Regenerated `history-sample-routes.ts` contains `history-sample/history/:id` route
- [ ] Other entities' view pages (e.g. `all-field-types`) are NOT affected (no History button)
- [ ] `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration :maia-showcase:maia-showcase-ui:build` passes

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration :maia-showcase:maia-showcase-ui:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Update `EntityDetailViewPageHtmlRenderer.kt`**

The renderer currently takes `entityDetailViewDef: EntityDetailViewDef` and `blotterPageDef: BlotterPageDef?`. Add an `EntityHistoryBlotterDef?` third parameter:

```kotlin
class EntityDetailViewPageHtmlRenderer(
    private val entityDetailViewDef: EntityDetailViewDef,
    private val blotterPageDef: BlotterPageDef?,
    private val historyBlotterDef: EntityHistoryBlotterDef? = entityDetailViewDef.entityDef.historyBlotterDef,
) : AbstractSourceFileRenderer()
```

In `renderSource()`, after the existing blotter button block (`blotterPageDef?.let { ... }`), add:

```kotlin
        historyBlotterDef?.let {
            append("""
                |    <button matButton aria-label="History" (click)="onHistoryClicked()">
                |        <mat-icon>history</mat-icon>
                |        History
                |    </button>
                |""".trimMargin())
        }
```

> **Note:** The `historyBlotterDef` can be derived directly from `entityDetailViewDef.entityDef.historyBlotterDef` as a default parameter, so callers don't need to change. Verify by reading `AngularUiModuleGenerator.renderEntityDetailViews()` — no change needed there.

- [ ] **Step 2: Update `EntityDetailViewPageComponentRenderer.kt`**

Similarly, add `historyBlotterDef` as a derived parameter. In `renderSourceBody()`, after the `blotterPageDef?.let { ... }` block, add:

```kotlin
        this.historyBlotterDef?.let { historyDef ->

            append("""
                |
                |
                |    onHistoryClicked(): void {
                |        const id = this.entityId();
                |        if (id) {
                |            this.router.navigate(['/${historyDef.routePath}', id]);
                |        }
                |    }
                |""".trimMargin())

        }
```

- [ ] **Step 3: Update `EntityCrudRoutesRenderer.kt`**

Add a `historyBlotterDef` constructor parameter derived from `entityDef.historyBlotterDef`. In `renderSourceBody()`, after `entityDetailViewDef?.let { renderViewRoute(it) }`, add:

```kotlin
        entityDef.historyBlotterDef?.let { renderHistoryRoute(it) }
```

Add the private render function:

```kotlin
    private fun renderHistoryRoute(def: EntityHistoryBlotterDef) {

        append("""
            |    {
            |        path: '${def.routePath}/:id',
            |        loadComponent: () =>
            |            import('./${def.blotterPageComponentNames.componentNameKebab}').then(m => m.${def.blotterPageComponentNames.componentName}),
            |    },
            |""".trimMargin())

    }
```

- [ ] **Step 4: Regenerate UI and verify**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Inspect the generated files:
```bash
grep -A3 "onHistoryClicked\|History" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-entity-detail-view-page.html
grep "history-sample/history" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/history/history-sample-routes.ts
# Should NOT appear in all-field-types:
grep -c "onHistoryClicked" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/all-field-types/all-field-types-entity-detail-view-page.ts
# Expected: 0
```

Then build:
```bash
./gradlew :maia-showcase:maia-showcase-ui:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageHtmlRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageComponentRenderer.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCrudRoutesRenderer.kt \
        maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: add History button and route to generated detail view pages"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityDetailViewPageComponentRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCrudRoutesRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration :maia-showcase:maia-showcase-ui:build", "acceptanceCriteria": ["history-sample-entity-detail-view-page.html has History button", "history-sample-routes.ts has history-sample/history/:id route", "all-field-types detail view page NOT affected", "Angular build passes"], "requiresUserVerification": false}
```

---

## Task 7: Integration test and full app build

**Goal:** Write an integration test for the history search endpoint. Verify the complete app builds.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistoryBlotterEndpointTest.kt`

**Acceptance Criteria:**
- [ ] Test creates a `HistorySampleEntity`, updates it (creating 2 history records), calls `POST /api/history-sample/{id}/history/search`, and asserts 2 results are returned
- [ ] `./gradlew :maia-showcase:app:test --tests "*.HistoryBlotterEndpointTest"` passes
- [ ] `./gradlew :maia-showcase:app:build` passes

**Verify:** `./gradlew :maia-showcase:app:test --tests "*.HistoryBlotterEndpointTest"` → `BUILD SUCCESSFUL`, test passes

**Steps:**

- [ ] **Step 1: Run the full showcase build to confirm nothing is broken**

```bash
./gradlew :maia-showcase:app:build
```

Fix any compilation or wiring issues before writing the test.

- [ ] **Step 2: Create `HistoryBlotterEndpointTest.kt`**

```kotlin
package org.maiaframework.showcase.history

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.maiaframework.domain.DomainId
import org.maiaframework.domain.search.AgGridSearchModel
import org.maiaframework.showcase.AbstractBlackBoxTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import tools.jackson.databind.node.JsonNodeFactory


class HistoryBlotterEndpointTest : AbstractBlackBoxTest() {


    @Autowired
    private lateinit var dao: HistorySampleDao

    @Autowired
    private lateinit var historyDao: HistorySampleHistoryDao

    @Autowired
    private lateinit var restTemplate: TestRestTemplate


    @Test
    fun `history search returns all versions for entity`() {

        // GIVEN a HistorySample entity is created (produces version 1 in history)
        val entity = HistorySampleEntityTestBuilder().build()
        this.dao.insert(entity)
        val entityId = entity.id

        // AND the entity is updated (produces version 2 in history)
        val updater = HistorySampleEntityUpdater.forPrimaryKey(entityId, 1L) {
            someString(entity.someString + "_updated")
            someInt(entity.someInt + 1)
        }
        this.dao.setFields(updater)

        // WHEN we search the history blotter for that entity
        val searchModel = AgGridSearchModel(
            JsonNodeFactory.instance.objectNode(),
            emptyList(),
            0,
            null
        )

        val response = restTemplate.exchange(
            "/api/history-sample/$entityId/history/search",
            HttpMethod.POST,
            HttpEntity(searchModel),
            object : ParameterizedTypeReference<Map<String, Any>>() {}
        )

        // THEN the response is 200 OK
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        // AND it contains 2 history records (CREATE + UPDATE)
        @Suppress("UNCHECKED_CAST")
        val results = response.body!!["results"] as List<*>
        assertThat(results).hasSize(2)

    }


}
```

- [ ] **Step 3: Run the test**

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.history.HistoryBlotterEndpointTest"
```
Expected: test passes, `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistoryBlotterEndpointTest.kt
git commit -m "test: add HistoryBlotterEndpointTest verifying search endpoint"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/history/HistoryBlotterEndpointTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests \"*.HistoryBlotterEndpointTest\"", "acceptanceCriteria": ["test creates entity + update, searches history, asserts 2 results", "app build passes"], "requiresUserVerification": false}
```

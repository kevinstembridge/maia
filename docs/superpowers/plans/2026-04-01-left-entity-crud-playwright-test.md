# Left Entity CRUD Playwright Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a CRUD Playwright test for `LeftEntity` featuring a blotter column that renders associated `RightEntity` names as Material chips, backed by a generator-level two-query many-to-many aggregation.

**Architecture:** Generator produces a `LeftTableDto` with `rightEntities: List<RightPkAndNameDto>`, populated via a second SQL query in the generated DAO. The AG Grid column uses a new `ChipsAgGridCellRendererComponent`. A new `PkAndNameListFieldType` carries the TypeScript import for `RightPkAndNameDto`.

**Tech Stack:** Kotlin/Spring Boot, Angular 21, AG Grid, Angular Material Chips, Playwright, PostgreSQL JDBC

**User Verification:** NO

---

## File Map

### New files
- `libs/maia-ui-workspace/projects/maia-ui/src/lib/components/chips-ag-grid-cell-renderer/chips-ag-grid-cell-renderer.component.ts`
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableManyToManyColumnDef.kt`
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyAggregationDef.kt`
- `maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.ts`
- `maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.html`
- `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftBlotterPage.kt`
- `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftEntityCrudPlaywrightTest.kt`

### Modified files
- `libs/maia-ui-workspace/projects/maia-ui/src/public-api.ts` — export ChipsAgGridCellRendererComponent
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/FieldType.kt` — add `PkAndNameListFieldType`
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AgGridCellRendererDefs.kt` — add `manyToManyChips`
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/SearchableDtoDef.kt` — add `manyToManyAggregationDef`
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableDef.kt` — `manyToManyColumnDef`, `dtoClassFields` extension, `searchableDtoDef` passes aggregation def
- `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/DtoHtmlTableDefBuilder.kt` — add `manyToManyColumn()`
- `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt` — handle `PkAndNameListFieldType`
- `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/DtoHtmlAgGridTableComponentRenderer.kt` — handle `DtoHtmlTableManyToManyColumnDef`
- `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/SearchableDtoJdbcDaoRenderer.kt` — two-query + inline row mapper
- `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DaoLayerModuleGenerator.kt` — skip RowMapper when `manyToManyAggregationDef != null`
- `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt` — `leftEntityDef` enhancements + `leftDtoHtmlTableDef` + `leftCrudDef`
- `maia-showcase/maia-showcase-ui/src/app/app.routes.ts` — add `/left` route
- `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` — add `leftBlotterPage`

---

### Task 1: ChipsAgGridCellRendererComponent in maia-ui

**Goal:** New Angular component that renders a `{name: string}[]` value as read-only `<mat-chip>` elements; exported from maia-ui public API; library rebuilt.

**Files:**
- Create: `libs/maia-ui-workspace/projects/maia-ui/src/lib/components/chips-ag-grid-cell-renderer/chips-ag-grid-cell-renderer.component.ts`
- Modify: `libs/maia-ui-workspace/projects/maia-ui/src/public-api.ts`

**Acceptance Criteria:**
- [ ] Component compiles and exports from `@maia/maia-ui`
- [ ] `maia-ui` builds without errors

**Verify:** `cd libs/maia-ui-workspace && npm run build` → exits 0

**Steps:**

- [ ] **Step 1: Create the component**

```typescript
// libs/maia-ui-workspace/projects/maia-ui/src/lib/components/chips-ag-grid-cell-renderer/chips-ag-grid-cell-renderer.component.ts
import {Component} from '@angular/core';
import {MatChipsModule} from '@angular/material/chips';
import {ICellRendererAngularComp} from 'ag-grid-angular';
import {ICellRendererParams} from 'ag-grid-community';


@Component({
    imports: [MatChipsModule],
    template: `
        <mat-chip-set>
            @for (item of items; track item.name) {
                <mat-chip>{{item.name}}</mat-chip>
            }
        </mat-chip-set>
    `,
})
export class ChipsAgGridCellRendererComponent implements ICellRendererAngularComp {


    items: { name: string }[] = [];


    agInit(params: ICellRendererParams): void {
        this.items = params.value ?? [];
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}
```

- [ ] **Step 2: Export from public-api.ts**

Add to `libs/maia-ui-workspace/projects/maia-ui/src/public-api.ts`:
```typescript
export * from './lib/components/chips-ag-grid-cell-renderer/chips-ag-grid-cell-renderer.component';
```

- [ ] **Step 3: Build maia-ui**

```bash
cd libs/maia-ui-workspace && npm run build
```
Expected: BUILD SUCCESS, dist output updated.

- [ ] **Step 4: Commit**

```bash
git add libs/maia-ui-workspace/projects/maia-ui/src/lib/components/chips-ag-grid-cell-renderer/
git add libs/maia-ui-workspace/projects/maia-ui/src/public-api.ts
git add libs/maia-ui-workspace/dist/
git commit -m "feat: add ChipsAgGridCellRendererComponent to maia-ui"
```

```json:metadata
{"files": ["libs/maia-ui-workspace/projects/maia-ui/src/lib/components/chips-ag-grid-cell-renderer/chips-ag-grid-cell-renderer.component.ts", "libs/maia-ui-workspace/projects/maia-ui/src/public-api.ts"], "verifyCommand": "cd libs/maia-ui-workspace && npm run build", "acceptanceCriteria": ["Component compiles", "maia-ui builds without errors"], "requiresUserVerification": false}
```

---

### Task 2: Generator spec — PkAndNameListFieldType, DtoHtmlTableManyToManyColumnDef, AgGridCellRendererDefs, ManyToManyAggregationDef

**Goal:** Four new/modified spec classes that define the many-to-many column type, its cell renderer, and the aggregation metadata passed to the DAO renderer.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/FieldType.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AgGridCellRendererDefs.kt`
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableManyToManyColumnDef.kt`
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyAggregationDef.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-gen:maia-gen-spec:compileKotlin` passes

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add `PkAndNameListFieldType` to `FieldType.kt`**

Add at the end of `FieldType.kt`, before the closing of the file:

```kotlin
class PkAndNameListFieldType(
    val entityPkAndNameDef: EntityPkAndNameDef
) : FieldType(
    fqcn = Fqcn.LIST,
    bsonCompatibleType = null,
    typescriptCompatibleType = null,
    sqlType = null,
    elasticMappingType = null,
    hazelcastCompatibleType = null,
    parameters = listOf(FieldTypes.byFqcn(entityPkAndNameDef.pkAndNameDtoFqcn)),
    defaultFormFieldValue = "[]"
) {

    override val jdbcCompatibleType: JdbcCompatibleType
        get() = throw IllegalStateException("PkAndNameListFieldType has no JDBC type")

    override fun unwrap(): FieldType = this

}
```

Note: `unqualifiedToString` from the base class will produce `"List<RightPkAndNameDto>"` via the `parameters` list.

- [ ] **Step 2: Add `manyToManyChips` to `AgGridCellRendererDefs.kt`**

```kotlin
val manyToManyChips = AgGridCellRendererDef(
    TypescriptImport("ChipsAgGridCellRendererComponent", "@maia/maia-ui"),
    "ChipsAgGridCellRendererComponent"
)
```

- [ ] **Step 3: Create `DtoHtmlTableManyToManyColumnDef.kt`**

```kotlin
package org.maiaframework.gen.spec.definition

class DtoHtmlTableManyToManyColumnDef(
    val joinEntityDef: EntityDef,
    val rightEntityDef: EntityDef
) : AbstractDtoHtmlTableColumnDef(
    columnHeader = "Right Entities",
    cellRenderer = AgGridCellRendererDefs.manyToManyChips
)
```

- [ ] **Step 4: Create `ManyToManyAggregationDef.kt`**

```kotlin
package org.maiaframework.gen.spec.definition

class ManyToManyAggregationDef(
    val joinEntityDef: EntityDef,
    val rightEntityDef: EntityDef,
    val leftEntityDef: EntityDef
)
```

- [ ] **Step 5: Compile and commit**

```bash
./gradlew :maia-gen:maia-gen-spec:compileKotlin
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/
git commit -m "feat: add PkAndNameListFieldType, DtoHtmlTableManyToManyColumnDef, ManyToManyAggregationDef, manyToManyChips renderer"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/FieldType.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AgGridCellRendererDefs.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableManyToManyColumnDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ManyToManyAggregationDef.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-spec:compileKotlin", "acceptanceCriteria": ["spec module compiles"], "requiresUserVerification": false}
```

---

### Task 3: Wire manyToManyColumn into DtoHtmlTableDef + SearchableDtoDef + Builder

**Goal:** `DtoHtmlTableDefBuilder.manyToManyColumn()` creates the column def; `DtoHtmlTableDef` appends `rightEntities` to the DTO fields and passes `ManyToManyAggregationDef` to `SearchableDtoDef`; `SearchableDtoDef` stores the aggregation def.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/SearchableDtoDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/DtoHtmlTableDefBuilder.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-gen:maia-gen-spec:compileKotlin` passes

**Verify:** `./gradlew :maia-gen:maia-gen-spec:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Add `manyToManyAggregationDef` to `SearchableDtoDef`**

In `SearchableDtoDef.kt`, add the parameter after `manyToManyJoinEntityDefs`:

```kotlin
class SearchableDtoDef(
    ...
    val manyToManyJoinEntityDefs: List<JoinEntityDef>,
    val manyToManyAggregationDef: ManyToManyAggregationDef? = null
)
```

- [ ] **Step 2: Modify `DtoHtmlTableDef.kt`**

The current `private val dtoClassFields` and the `searchableDtoDef` property need updating. Read the current file first (`DtoHtmlTableDef.kt`), then apply:

Replace `private val dtoClassFields = dtoHtmlTableColumnFields.map { it.classFieldDef }` with:

```kotlin
val manyToManyColumnDef: DtoHtmlTableManyToManyColumnDef? =
    dtoHtmlTableColumnDefs.filterIsInstance<DtoHtmlTableManyToManyColumnDef>().firstOrNull()

private val dtoClassFields: List<ClassFieldDef> = run {
    val base = dtoHtmlTableColumnFields.map { it.classFieldDef }
    if (manyToManyColumnDef != null) {
        val pkAndNameDef = manyToManyColumnDef.rightEntityDef.entityPkAndNameDef
        val rightEntitiesFieldDef = ClassFieldDef.aClassField(
            "rightEntities",
            PkAndNameListFieldType(pkAndNameDef)
        ).build()
        base + rightEntitiesFieldDef
    } else {
        base
    }
}
```

Also modify the `searchableDtoDef` property to pass `manyToManyAggregationDef`. Find the block:
```kotlin
SearchableDtoDef(
    searchableDtoDef.dtoRootEntityDef,
    dtoBaseName.withSuffix("Table"),
    moduleName,
    packageName,
    searchableDtoDef.tableName,
    fields,
    searchableDtoDef.lookupDefs,
    searchableDtoDef.withPreAuthorize,
    withGeneratedEndpoint,
    withGeneratedFindAllFunction,
    withGeneratedDto,
    GenerateFindById.FALSE,
    searchModelType,
    searchableDtoDef.withProvidedFieldConverter,
    manyToManyJoinEntityDefs = searchableDtoDef.manyToManyJoinEntityDefs
)
```

Replace `manyToManyJoinEntityDefs = searchableDtoDef.manyToManyJoinEntityDefs` with:

```kotlin
manyToManyJoinEntityDefs = emptyList(),  // table query does NOT join right inline
manyToManyAggregationDef = manyToManyColumnDef?.let {
    ManyToManyAggregationDef(
        joinEntityDef = it.joinEntityDef,
        rightEntityDef = it.rightEntityDef,
        leftEntityDef = searchableDtoDef.dtoRootEntityDef
    )
}
```

Add required imports at top of `DtoHtmlTableDef.kt`:
```kotlin
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.PkAndNameListFieldType
```

- [ ] **Step 3: Add `manyToManyColumn()` to `DtoHtmlTableDefBuilder.kt`**

```kotlin
fun manyToManyColumn(joinEntityDef: EntityDef) {

    val rootEntityDef = requireNotNull(dtoHtmlTableSourceDef.searchableDtoDef) {
        "manyToManyColumn() requires a searchable DTO source"
    }.dtoRootEntityDef

    val rightEntityFieldDef = joinEntityDef.allEntityFieldsSorted
        .firstOrNull { it.foreignKeyFieldDef != null && it.foreignKeyFieldDef!!.foreignEntityDef != rootEntityDef }
        ?: throw IllegalArgumentException(
            "No right entity FK found on join entity ${joinEntityDef.entityBaseName}"
        )

    val rightEntityDef = rightEntityFieldDef.foreignKeyFieldDef!!.foreignEntityDef

    this.columnBuilders.add(object : DefBuilder<DtoHtmlTableManyToManyColumnDef> {
        override fun build() = DtoHtmlTableManyToManyColumnDef(joinEntityDef, rightEntityDef)
    })

}
```

You need to import `EntityDef`. Also, `DefBuilder` is an interface already used by other builders in this file — confirm the existing import covers it.

- [ ] **Step 4: Compile and commit**

```bash
./gradlew :maia-gen:maia-gen-spec:compileKotlin
git add maia-gen/maia-gen-spec/src/
git commit -m "feat: wire manyToManyColumn into DtoHtmlTableDef, SearchableDtoDef, and builder"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/SearchableDtoDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/DtoHtmlTableDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/DtoHtmlTableDefBuilder.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-spec:compileKotlin", "acceptanceCriteria": ["spec compiles"], "requiresUserVerification": false}
```

---

### Task 4: Generator — DAO two-query, TypeScript DTO, AG Grid column

**Goal:** `SearchableDtoJdbcDaoRenderer` generates a second SQL query for many-to-many aggregation and an inline row mapper; `DaoLayerModuleGenerator` skips the separate `RowMapperRenderer` for these defs; `TypescriptInterfaceDtoRenderer` handles `PkAndNameListFieldType`; `DtoHtmlAgGridTableComponentRenderer` emits the chips col def and imports `MatChipsModule`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/SearchableDtoJdbcDaoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DaoLayerModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/DtoHtmlAgGridTableComponentRenderer.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-gen:maia-gen-generator:compileKotlin` passes

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Skip RowMapperRenderer in `DaoLayerModuleGenerator.kt`**

Find:
```kotlin
SearchableDtoJdbcDaoRenderer(it, this.modelDef).renderToDir(this.kotlinOutputDir)
RowMapperRenderer(it.rowMapperDef).renderToDir(this.kotlinOutputDir)
```

Replace with:
```kotlin
SearchableDtoJdbcDaoRenderer(it, this.modelDef).renderToDir(this.kotlinOutputDir)
if (it.manyToManyAggregationDef == null) {
    RowMapperRenderer(it.rowMapperDef).renderToDir(this.kotlinOutputDir)
}
```

- [ ] **Step 2: Inline row mapper in `SearchableDtoJdbcDaoRenderer.kt`**

Replace `renderJsonMapperClassField()` to emit an inline `MaiaRowMapper` when `manyToManyAggregationDef != null`. Change this private function:

```kotlin
private fun renderJsonMapperClassField() {

    if (searchableDtoDef.manyToManyAggregationDef != null) {
        renderInlineDtoRowMapper()
        return
    }

    addImportFor(searchableDtoDef.dtoRowMapperClassDef.fqcn)
    val jsonMapperParameter = if (searchableDtoDef.hasAnyMapFields) "jsonMapper" else ""
    blankLine()
    blankLine()
    appendLine("    private val dtoRowMapper = ${searchableDtoDef.dtoRowMapperClassDef.uqcn}($jsonMapperParameter)")

}


private fun renderInlineDtoRowMapper() {

    addImportFor(Fqcns.MAIA_JDBC_ROW_MAPPER)
    addImportFor(Fqcns.MAIA_RESULT_SET_ADAPTER)

    blankLine()
    blankLine()
    appendLine("    private val dtoRowMapper = MaiaRowMapper<$dtoUqcn> { rsa ->")
    appendLine("        $dtoUqcn(")

    searchableDtoDef.allRowMapperFieldDefs.forEach { rowMapperFieldDef ->
        val fieldName = rowMapperFieldDef.resultSetFieldName
        val readerExpr = RowMapperFunctions.renderRowMapperField(
            rowMapperFieldDef, indentSize = 0, orElseText = "", ::addImportFor
        ).trim()
        appendLine("            $fieldName = $readerExpr,")
    }

    val agg = searchableDtoDef.manyToManyAggregationDef!!
    val rightPkAndNameUqcn = agg.rightEntityDef.entityPkAndNameDef.dtoUqcn
    addImportFor(agg.rightEntityDef.entityPkAndNameDef.pkAndNameDtoFqcn)
    appendLine("            rightEntities = emptyList<$rightPkAndNameUqcn>(),")
    appendLine("        )")
    appendLine("    }")

}
```

Add `import org.maiaframework.gen.spec.definition.RowMapperFunctions` if not already present.

- [ ] **Step 3: Two-query logic in `render function search()`**

In `SearchableDtoJdbcDaoRenderer.kt`, find the `render function search()` private function. After:
```kotlin
val results = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)
```

Change the `results` variable name to `pageResults`, then add the following block before `return SearchResultPage(...)`:

Full replacement of the existing return block. Find these lines in `render function search()`:
```kotlin
        val totalResultCount = this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
        val results = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)
        val endRow = searchModel.endRow
        val limit = if (endRow == null) null else (endRow - searchModel.startRow)

        return SearchResultPage(
            results,
            totalResultCount,
            searchModel.startRow,
            limit
        )
```

Replace with:

```kotlin
        val totalResultCount = this.jdbcOps.queryForLong(sqlForTotalCount, sqlParams)
        val pageResults = this.jdbcOps.queryForList(sqlForPage, sqlParams, this.dtoRowMapper)
        val endRow = searchModel.endRow
        val limit = if (endRow == null) null else (endRow - searchModel.startRow)
```

Then, if `manyToManyAggregationDef != null`, emit the two-query merge. The simplest approach: add a conditional render after computing `pageResults`. Insert after the existing `appendLine("        val results = ...")` replacement block:

```kotlin
        if (searchableDtoDef.manyToManyAggregationDef != null) {
            renderManyToManyMerge()
        }
```

Where `renderManyToManyMerge()` is:

```kotlin
private fun renderManyToManyMerge() {

    val agg = searchableDtoDef.manyToManyAggregationDef!!
    val joinTable = agg.joinEntityDef.schemaAndTableName
    val rightTable = agg.rightEntityDef.schemaAndTableName
    val leftFkCol = agg.joinEntityDef
        .foreignKeyFieldForBaseName(agg.leftEntityDef.entityBaseName).tableColumnName
    val rightFkCol = agg.joinEntityDef
        .foreignKeyFieldForBaseName(agg.rightEntityDef.entityBaseName).tableColumnName
    val rightNameCol = agg.rightEntityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName
    val rightPkAndNameUqcn = agg.rightEntityDef.entityPkAndNameDef.dtoUqcn

    addImportFor(Fqcns.MAIA_DOMAIN_ID)
    addImportFor(Fqcns.MAIA_JDBC_ROW_MAPPER)
    addImportFor(agg.rightEntityDef.entityPkAndNameDef.pkAndNameDtoFqcn)

    appendLine("""
        |
        |        if (pageResults.isEmpty()) {
        |            return SearchResultPage(pageResults, totalResultCount, searchModel.startRow, limit)
        |        }
        |
        |        val leftIds = pageResults.map { it.id.value }
        |        val rightsSqlParams = SqlParams().addValue("leftIds", leftIds)
        |        val rightsSql = \"\"\"
        |            SELECT $joinTable.$leftFkCol as leftId, $rightTable.id as rightId, $rightTable.$rightNameCol as rightName
        |            FROM $joinTable
        |            INNER JOIN $rightTable ON $joinTable.$rightFkCol = $rightTable.id
        |            WHERE $joinTable.$leftFkCol IN (:leftIds)
        |            \"\"\".trimIndent()
        |
        |        val rightPairs = this.jdbcOps.queryForList(rightsSql, rightsSqlParams, MaiaRowMapper { rsa ->
        |            Pair(rsa.readDomainId("leftId"), $rightPkAndNameUqcn(rsa.readDomainId("rightId"), rsa.readString("rightName")))
        |        })
        |        val rightsByLeftId = rightPairs.groupBy({ it.first }, { it.second })
        |
        |        val results = pageResults.map { dto ->
        |            dto.copy(rightEntities = rightsByLeftId[dto.id] ?: emptyList())
        |        }
        |
        |        return SearchResultPage(
        |            results,
        |            totalResultCount,
        |            searchModel.startRow,
        |            limit
        |        )""".trimMargin())

}
```

And change the normal (non-aggregation) `return SearchResultPage(...)` block to use `pageResults` as the results variable:

```kotlin
        return SearchResultPage(
            pageResults,
            totalResultCount,
            searchModel.startRow,
            limit
        )
```

- [ ] **Step 4: `TypescriptInterfaceDtoRenderer.kt` — handle `PkAndNameListFieldType`**

In `init {}`, add after the `if (fieldType is ListFieldType)` block:

```kotlin
if (fieldType is PkAndNameListFieldType) {
    addImport(fieldType.entityPkAndNameDef.pkAndNameDtoTypescriptImport)
}
```

In `renderSourceBody()` `when (fieldType)`, add a branch after `is ListFieldType ->`:
```kotlin
is PkAndNameListFieldType -> appendLine("    ${fieldDef.classFieldName}${nullableClause}: ReadonlyArray<${fieldType.entityPkAndNameDef.dtoUqcn}>;")
```

Add the import at the top:
```kotlin
import org.maiaframework.gen.spec.definition.lang.PkAndNameListFieldType
```

- [ ] **Step 5: `DtoHtmlAgGridTableComponentRenderer.kt` — chips column**

In `init {}`, after `addImport("@angular/material/button", "MatButtonModule")`, add:
```kotlin
if (dtoHtmlTableDef.manyToManyColumnDef != null) {
    addImport("@angular/material/chips", "MatChipsModule")
}
```

In `renderSourceBody()`, find the `@Component` imports array:
```kotlin
appendLine("""
    |@Component({
    |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
```

Replace with a dynamic version:
```kotlin
val extraImports = if (dtoHtmlTableDef.manyToManyColumnDef != null) ", MatChipsModule, ChipsAgGridCellRendererComponent" else ""
appendLine("""
    |@Component({
    |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule$extraImports],
""")
```

In the `columnDefs` loop that iterates `dtoHtmlTableColumnDefs`, add a new case after `is DtoHtmlTableActionColumnDef`:

```kotlin
is DtoHtmlTableManyToManyColumnDef -> appendLine("""
    |        { field: 'rightEntities', headerName: 'Right Entities', cellDataType: 'object', filter: false, sortable: false, cellRenderer: ChipsAgGridCellRendererComponent },
""".trimMargin())
```

Add import at the top of the file:
```kotlin
import org.maiaframework.gen.spec.definition.DtoHtmlTableManyToManyColumnDef
```

- [ ] **Step 6: Compile and commit**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
git add maia-gen/maia-gen-generator/src/
git commit -m "feat: generator two-query many-to-many aggregation, chips column renderer, TypeScript PkAndNameListFieldType support"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/SearchableDtoJdbcDaoRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DaoLayerModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/DtoHtmlAgGridTableComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["generator compiles"], "requiresUserVerification": false}
```

---

### Task 5: Showcase spec changes + maiaGeneration

**Goal:** `leftEntityDef` gains CRUD config; new `leftDtoHtmlTableDef` and `leftCrudDef`; full code generation run produces `LeftTableDto`, `LeftTableDtoDao`, `LeftCrudTableComponent`, etc.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:spec:maiaGeneration` passes
- [ ] `./gradlew :maia-showcase:build` passes (all showcase layers compile and test)

**Verify:** `./gradlew :maia-showcase:build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Modify `leftEntityDef` in `MaiaShowcaseSpec.kt`**

Find (around line 1074):
```kotlin
val leftEntityDef = entity(
    "org.maiaframework.showcase.many_to_many",
    "Left",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE
) {
    field("someInt", FieldTypes.int)
    field("someString", FieldTypes.string) {
        lengthConstraint(max = 100)
    }
}
```

Replace with:
```kotlin
val leftEntityDef = entity(
    "org.maiaframework.showcase.many_to_many",
    "Left",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE
) {
    field("someInt", FieldTypes.int) {
        fieldDisplayName("Some Int")
        editableByUser()
    }
    field("someString", FieldTypes.string) {
        fieldDisplayName("Some String")
        lengthConstraint(max = 100)
        editableByUser()
    }
    crud {
        apis {
            create()
            update()
            delete()
        }
    }
}
```

- [ ] **Step 2: Add `leftDtoHtmlTableDef` and `leftCrudDef` after `leftNotMappedToRightSearchableDtoDef`**

Find `val leftNotMappedToRightSearchableDtoDef = ...` (ends around line 1145). After the closing `}` of that block, add:

```kotlin
val leftDtoHtmlTableDef = dtoHtmlTable(leftSearchableDtoDef, withAddButton = true) {
    columnFromDto("id")
    columnFromDto(dtoFieldName = "tableSomeStringFromLeft", fieldPathInSourceData = "someStringFromLeft")
    columnFromDto(dtoFieldName = "tableSomeIntFromLeft", fieldPathInSourceData = "someIntFromLeft")
    manyToManyColumn(manyToManyJoinEntityDef)
    editActionColumn()
    deleteActionColumn()
}

val leftCrudDef = crudTableDef(leftDtoHtmlTableDef, leftEntityDef.entityCrudApiDef!!)
```

- [ ] **Step 3: Run spec compilation**

```bash
./gradlew :maia-showcase:spec:compileKotlin
```
Expected: BUILD SUCCESSFUL. Fix any compile errors before proceeding.

- [ ] **Step 4: Run full maiaGeneration**

```bash
./gradlew :maia-showcase:spec:maiaGeneration \
          :maia-showcase:domain:maiaGeneration \
          :maia-showcase:dao:maiaGeneration \
          :maia-showcase:service:maiaGeneration \
          :maia-showcase:web:maiaGeneration \
          :maia-showcase:maia-showcase-ui:maiaGeneration
```
Expected: all tasks succeed.

Verify generated files exist:
- `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftTableDto.kt` — should have `rightEntities: List<RightPkAndNameDto>`
- `maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftTableDtoDao.kt` — should contain `rightsSql` and `rightsByLeftId`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many_to_many/LeftTableDto.ts` — should have `rightEntities: ReadonlyArray<RightPkAndNameDto>`

- [ ] **Step 5: Build showcase**

```bash
./gradlew :maia-showcase:build
```
Expected: BUILD SUCCESSFUL (all tests pass).

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/spec/src/ maia-showcase/domain/src/generated/ maia-showcase/dao/src/generated/ maia-showcase/service/src/generated/ maia-showcase/web/src/generated/ maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: showcase leftEntityDef crud config, leftDtoHtmlTableDef with manyToMany column, regenerate"
```

```json:metadata
{"files": ["maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt"], "verifyCommand": "./gradlew :maia-showcase:build", "acceptanceCriteria": ["spec compiles", "maiaGeneration produces LeftTableDto with rightEntities", "generated LeftTableDtoDao has two-query logic", "build passes"], "requiresUserVerification": false}
```

---

### Task 6: Angular left-blotter-page + route

**Goal:** `left-blotter-page.ts`/`.html` page wrapping the generated `LeftCrudTableComponent`; `/left` route added to `app.routes.ts`; Angular build passes.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.ts`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.html`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:maia-showcase-ui:npmBuild` passes

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:npmBuild` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `left-blotter-page.ts`**

```typescript
// maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.ts
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    LeftCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/many_to_many/left-crud-table.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        LeftCrudTableComponent
    ],
    selector: 'app-left-blotter-page',
    templateUrl: './left-blotter-page.html',
})
export class LeftBlotterPage {

}
```

Note: the exact import path for `LeftCrudTableComponent` depends on the generated file location. Check `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many_to_many/` for the actual file name after generation.

- [ ] **Step 2: Create `left-blotter-page.html`**

```html
<app-page-layout pageTitle="Left" dataPageId="left_blotter">
    <app-left-crud-table></app-left-crud-table>
</app-page-layout>
```

Note: verify the selector of the generated `LeftCrudTableComponent` (check the generated `.ts` file).

- [ ] **Step 3: Add route in `app.routes.ts`**

Add after the `bravo` route entry:
```typescript
{
    path: 'left',
    loadComponent: () =>
        import('@app/pages/left-blotter/left-blotter-page').then(
            (m) => m.LeftBlotterPage,
        ),
},
```

- [ ] **Step 4: Build Angular**

```bash
./gradlew :maia-showcase:maia-showcase-ui:npmBuild
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/
git add maia-showcase/maia-showcase-ui/src/app/app.routes.ts
git commit -m "feat: add Angular left-blotter-page and /left route"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.ts", "maia-showcase/maia-showcase-ui/src/app/pages/left-blotter/left-blotter-page.html", "maia-showcase/maia-showcase-ui/src/app/app.routes.ts"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:npmBuild", "acceptanceCriteria": ["Angular build passes", "/left route resolves LeftBlotterPage"], "requiresUserVerification": false}
```

---

### Task 7: LeftBlotterPage + AbstractPlaywrightTest + LeftEntityCrudPlaywrightTest

**Goal:** Kotlin Playwright page object for `/left`; `AbstractPlaywrightTest` gets `leftBlotterPage`; full CRUD + chips assertion test.

**Files:**
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftBlotterPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt`
- Create: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftEntityCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `./gradlew :maia-showcase:app:compileTestKotlin` passes
- [ ] `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftEntityCrudPlaywrightTest"` passes

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftEntityCrudPlaywrightTest"` → BUILD SUCCESSFUL, 1 test passed

**Steps:**

- [ ] **Step 1: Create `LeftBlotterPage.kt`**

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftBlotterPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left",
    "left_blotter"
) {


    fun clickAddButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillCreateForm(
        someInt: String = "42",
        someString: String = "testleft",
    ) {

        page.locator("input[name='someInt']").fill(someInt)
        page.locator("input[name='someString']").fill(someString)

    }


    fun clickSubmitButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))

    }


    fun assertCreateDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun clickEditButtonForFirstRow() {

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"tableSomeStringFromLeft\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val editCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='edit']").first()
        editCell.waitFor()
        editCell.scrollIntoViewIfNeeded()
        editCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun fillEditForm(
        someString: String = "testleft_edited",
    ) {

        page.locator("input[name='someString']").fill(someString)

    }


    fun assertEditDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun assertTableContainsValue(value: String) {

        page.waitForFunction(
            "() => {" +
            "  const vp = document.querySelector('.ag-center-cols-viewport');" +
            "  if (!vp) return false;" +
            "  vp.scrollLeft = 0;" +
            "  return Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes('$value'));" +
            "}"
        )

    }


    fun clickDeleteButtonForFirstRow() {

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 0")

        page.waitForFunction(
            "() => { const c = document.querySelector('.ag-cell[col-id=\"tableSomeStringFromLeft\"]'); " +
            "return c && c.innerText && c.innerText.trim().length > 0; }"
        )

        page.evaluate("document.querySelector('.ag-center-cols-viewport').scrollLeft = 99999")
        val deleteCell = page.locator(".ag-row:not(.ag-row-loading) .ag-cell[col-id='delete']").first()
        deleteCell.waitFor()
        deleteCell.scrollIntoViewIfNeeded()
        deleteCell.click()
        page.locator("mat-dialog-container").waitFor()

    }


    fun clickYesButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Yes")).click()

    }


    fun clickCancelButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Cancel")).click()

    }


    fun assertDeleteDialogClosed() {

        page.locator("mat-dialog-container").waitFor(
            Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN)
        )

    }


    fun assertTableDoesNotContainValue(value: String) {

        page.waitForFunction(
            "(value) => {" +
            "  if (document.querySelector('.ag-overlay-no-rows-center')) return true;" +
            "  if (document.querySelector('.ag-row-loading')) return false;" +
            "  return !Array.from(document.querySelectorAll('.ag-cell'))" +
            "    .some(c => c.innerText && c.innerText.includes(value));" +
            "}",
            value
        )

    }


    fun assertChipVisible(chipText: String) {

        page.waitForFunction(
            "(text) => Array.from(document.querySelectorAll('.ag-cell[col-id=\"rightEntities\"] mat-chip')).some(c => c.textContent && c.textContent.trim() === text)",
            chipText
        )

    }


}
```

- [ ] **Step 2: Add `leftBlotterPage` to `AbstractPlaywrightTest.kt`**

After `protected lateinit var bravoBlotterPage: BravoBlotterPage`, add:
```kotlin
protected lateinit var leftBlotterPage: LeftBlotterPage
```

In `initPlaywrightPage()`, after `bravoBlotterPage = BravoBlotterPage(page, urlHelper)`, add:
```kotlin
leftBlotterPage = LeftBlotterPage(page, urlHelper)
```

Add import at the top:
```kotlin
import org.maiaframework.showcase.testing.pages.LeftBlotterPage
```

- [ ] **Step 3: Create `LeftEntityCrudPlaywrightTest.kt`**

```kotlin
package org.maiaframework.showcase.many_to_many

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.maiaframework.showcase.AbstractPlaywrightTest
import org.springframework.beans.factory.annotation.Autowired


class LeftEntityCrudPlaywrightTest : AbstractPlaywrightTest() {


    @Autowired
    private lateinit var leftEntityDao: LeftEntityDao

    @Autowired
    private lateinit var rightEntityDao: RightEntityDao

    @Autowired
    private lateinit var manyToManyJoinEntityDao: ManyToManyJoinEntityDao


    @BeforeAll
    fun setUp() {

        initAdminUserFixture()

        val rightEntity1 = RightEntityTestBuilder(someString = "aSomeRightValue1").build()
        val rightEntity2 = RightEntityTestBuilder(someString = "aSomeRightValue2").build()
        val leftEntityFixture = LeftEntityTestBuilder(someString = "fixture-left").build()

        fixtures.resetDatabaseState()

        rightEntityDao.insert(rightEntity1)
        rightEntityDao.insert(rightEntity2)
        leftEntityDao.insert(leftEntityFixture)
        manyToManyJoinEntityDao.insert(
            ManyToManyJoinEntityTestBuilder(leftId = leftEntityFixture.id, rightId = rightEntity1.id).build()
        )
        manyToManyJoinEntityDao.insert(
            ManyToManyJoinEntityTestBuilder(leftId = leftEntityFixture.id, rightId = rightEntity2.id).build()
        )

    }


    @BeforeEach
    fun logOut() {

        `log out`()

    }


    @Test
    fun `crud journey`() {

        `log in as admin user`()
        `navigate to the`(leftBlotterPage)

        leftBlotterPage.apply {

            assertChipVisible("aSomeRightValue1")
            assertChipVisible("aSomeRightValue2")

            clickAddButton()
            fillCreateForm(someInt = "99", someString = "testleft")
            clickSubmitButton()
            assertCreateDialogClosed()
            assertTableContainsValue("testleft")

            clickEditButtonForFirstRow()
            fillEditForm("testleft_edited")
            clickSubmitButton()
            assertEditDialogClosed()
            assertTableContainsValue("testleft_edited")

            // Cancel path
            clickDeleteButtonForFirstRow()
            clickCancelButton()
            assertDeleteDialogClosed()
            assertTableContainsValue("testleft_edited")

            // Confirm delete path
            clickDeleteButtonForFirstRow()
            clickYesButton()
            assertDeleteDialogClosed()
            assertTableDoesNotContainValue("testleft_edited")

        }

    }


}
```

Note: check that `LeftEntityDao`, `RightEntityDao`, and `ManyToManyJoinEntityDao` are the correct generated class names. Verify by looking at the generated DAO files in `maia-showcase/dao/src/generated/`.

- [ ] **Step 4: Compile test sources**

```bash
./gradlew :maia-showcase:app:compileTestKotlin
```
Expected: BUILD SUCCESSFUL. Fix any import/class name issues.

- [ ] **Step 5: Run the test**

Ensure the app is running (`./gradlew :maia-showcase:app:bootRun` or via Docker), then:

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.LeftEntityCrudPlaywrightTest"
```
Expected: 1 test passed.

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftBlotterPage.kt
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftEntityCrudPlaywrightTest.kt
git commit -m "feat: LeftBlotterPage, AbstractPlaywrightTest leftBlotterPage, LeftEntityCrudPlaywrightTest"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftBlotterPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftEntityCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests \"org.maiaframework.showcase.many_to_many.LeftEntityCrudPlaywrightTest\"", "acceptanceCriteria": ["test compiles", "crud journey test passes", "assertChipVisible passes for aSomeRightValue1 and aSomeRightValue2"], "requiresUserVerification": false}
```

---

## Open Questions

- None.

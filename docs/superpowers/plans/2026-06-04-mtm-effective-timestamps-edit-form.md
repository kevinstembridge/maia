# MTM Effective Timestamps — Edit Form Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend the generated edit form for entities with timestamped many-to-many associations to use the same mini-form approach as the create form, with pre-population of existing join timestamps from the backend.

**Architecture:** Four-layer change: (1) the update request DTO gains a join-DTO list field instead of bare IDs, and the backend service uses those timestamps; (2) the fetch-for-edit DTO grows a new `{OtherSide}JoinFetchDto` type (id + name + effectiveFrom? + effectiveTo?) for each timestamped association; (3) the frontend edit-form renderers are extended to accept and render `ManyToManyTimestampedFieldDef` entries just like the create renderers; (4) the Playwright tests are updated to interact with join-entry lists instead of chip grids.

**Tech Stack:** Kotlin (Spring Boot generator + spec DSL), Angular 19, Angular Material, Playwright

**User Verification:** NO

---

## File Map

### Modified — `maia-gen/maia-gen-spec`
| File | Change |
|---|---|
| `definition/EntityUpdateApiDef.kt` | Add `timestampedJoinRequestDtosByAssociation` (delegate to create), change `dtoFields` to use join DTO for timestamped M2M |
| `definition/lang/FieldTypes.kt` | Add `JoinFetchDtoFieldType` class + 5 `when` branches + `FieldTypes.joinFetchDto()` |
| `definition/lang/JoinFetchDtoFieldType.kt` | (new) thin wrapper carrying the DTO class FQCN and TypeScript import |
| `definition/JoinFetchDtoDef.kt` | (new) carries `dtoDef`, SQL info (join/name column names), TS rendering info |
| `definition/EntityDef.kt` | Add `joinFetchDtoDefsByAssociation`, change `fetchForEditManyToManyFieldDefs` |
| `definition/RowMapperFieldDef.kt` | Add `joinFetchDtoDef: JoinFetchDtoDef?` to `ManyToManyRowMapperFieldDef` |
| `definition/FetchForEditDtoDef.kt` | Propagate `joinFetchDtoDef` when building `ManyToManyRowMapperFieldDef` |
| `definition/ModelDef.kt` | Add `joinFetchDtoDefs` list |

### Modified — `maia-gen/maia-gen-generator`
| File | Change |
|---|---|
| `renderers/CrudServiceRenderer.kt` | Use `${otherSide}Entities` + `joinDto.effectiveFrom/effectiveTo` for timestamped M2M update |
| `renderers/RowMapperRenderer.kt` | Generate `fetchXxxJoinFetchDtos()` with extended SQL + inline lambda row mapper for timestamped M2M |
| `renderers/AbstractKotlinRenderer.kt` | Handle `JoinFetchDtoFieldType` in `addImportFor` |
| `renderers/ui/TypescriptInterfaceDtoRenderer.kt` | Emit TypeScript for `JoinFetchDtoFieldType` field + list element |
| `generator/DomainModuleGenerator.kt` | Add `render JoinFetchDtos()` to render Kotlin data classes |
| `generator/AngularUiModuleGenerator.kt` | Render TS interfaces for join-fetch DTOs; add `manyToManyTimestampedFieldsForEdit`; update `renderEntityEditPages`; fix `manyToManyChipFieldsForEdit` |
| `renderers/ui/EntityEditReactiveFormHtmlRenderer.kt` | Add `timestampedFields` parameter |
| `renderers/ui/AngularReactiveFormComponentRenderer.kt` | Populate timestamped joins from fetchForEdit DTO in edit ngOnInit |

### Modified — `maia-showcase/app/src/test`
| File | Change |
|---|---|
| `testing/pages/RightManyEditPage.kt` | Replace chip methods with join-entry mini-form methods |
| `testing/pages/LeftManyEditPage.kt` | Same |
| `many_to_many/RightManyCrudPlaywrightTest.kt` | Use new edit-page mini-form methods |
| `many_to_many/LeftManyCrudPlaywrightTest.kt` | Same |

---

## Task 1: UpdateRequestDto + CrudServiceRenderer

**Goal:** For timestamped M2M associations, the update request DTO accepts `${otherSide}Entities: List<{OtherSide}JoinRequestDto>` instead of bare IDs; the backend service uses the provided timestamps when re-creating joins.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityUpdateApiDef.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt`

**Acceptance Criteria:**
- [ ] `RightManyUpdateRequestDto.kt` has `leftEntities: List<LeftJoinRequestDto>` (not `leftEntityIds`)
- [ ] `LeftManyUpdateRequestDto.kt` has `rightEntities: List<RightJoinRequestDto>` (not `rightEntityIds`)
- [ ] `RightManyCrudService.update()` maps `joinDto.effectiveFrom` / `joinDto.effectiveTo` (not `Instant.now()`)
- [ ] Showcase compiles: `./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:service:compileKotlin`

**Verify:** `./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:service:maiaGeneration && grep -A5 "leftEntities" maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyUpdateRequestDto.kt`
Expected: field `leftEntities_raw: List<LeftJoinRequestDto>?`

**Steps:**

- [ ] **Step 1: Add `timestampedJoinRequestDtosByAssociation` to `EntityUpdateApiDef`**

In `EntityUpdateApiDef.kt`, after the `dtoBaseName` declaration, add:

```kotlin
val timestampedJoinRequestDtosByAssociation: Map<ManyToManyEntityDef, RequestDtoDef> by lazy {
    entityDef.entityCrudApiDef?.createApiDef
        ?.timestampedJoinRequestDtosByAssociation
        ?: emptyMap()
}
```

- [ ] **Step 2: Change `dtoFields` M2M section in `EntityUpdateApiDef`**

Find the `.plus(entityDef.manyToManyAssociations.map { ... })` block at the end of `dtoFields` and replace it:

```kotlin
.plus(
    entityDef.manyToManyAssociations.map { manyToManyEntityDef ->
        val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)
        if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
            val joinDtoDef = timestampedJoinRequestDtosByAssociation[manyToManyEntityDef]!!
            RequestDtoFieldDef(
                ClassFieldDef.aClassField(
                    "${otherSide.fieldName}Entities",
                    FieldTypes.list(FieldTypes.requestDto(joinDtoDef))
                ).nullable().build(), null
            )
        } else {
            RequestDtoFieldDef(
                ClassFieldDef.aClassField(
                    "${otherSide.fieldName}EntityIds",
                    FieldTypes.list(FieldTypes.domainId)
                ).nullable().build(), null
            )
        }
    }
)
```

- [ ] **Step 3: Fix `manyToManyFieldNames` set in `CrudServiceRenderer`**

In `CrudServiceRenderer.kt`, in `render update function`, find the `manyToManyFieldNames` set:

```kotlin
// OLD:
val manyToManyFieldNames = apiDef.entityDef.manyToManyAssociations.map { m2m ->
    val otherSide = m2m.otherSideFrom(this.entityDef)
    "${otherSide.fieldName}EntityIds"
}.toSet()

// NEW:
val manyToManyFieldNames = apiDef.entityDef.manyToManyAssociations.map { m2m ->
    val otherSide = m2m.otherSideFrom(this.entityDef)
    if (m2m.entityDef.hasEffectiveTimestamps.value) "${otherSide.fieldName}Entities"
    else "${otherSide.fieldName}EntityIds"
}.toSet()
```

- [ ] **Step 4: Fix the timestamped M2M update block in `CrudServiceRenderer`**

In `render update function`, find the block that starts:
```kotlin
if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value && !manyToManyEntityDef.entityDef.isNotDeletable) {
```

Replace the entire `if/else if/else` chain with:

```kotlin
if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value && !manyToManyEntityDef.entityDef.isNotDeletable) {
    val otherSideDtoFieldName = "${otherSideFieldName}Entities"
    appendLine("        this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id).forEach { join ->")
    appendLine("            this.${joinRepoFieldName}.deleteByPrimaryKey(join.id)")
    appendLine("        }")
    blankLine()
    appendLine("        val new${otherSideFieldNameCapitalized}Joins = editDto.${otherSideDtoFieldName}.map { joinDto ->")
    appendLine("            ${joinEntityClass}.newInstance(effectiveFrom = joinDto.effectiveFrom, effectiveTo = joinDto.effectiveTo, $thisSideFieldName = id, $otherSideFieldName = joinDto.${otherSideFieldName}EntityId)")
    appendLine("        }")
    appendLine("        this.${joinRepoFieldName}.bulkInsert(new${otherSideFieldNameCapitalized}Joins)")
} else if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
    val otherSideDtoFieldName = "${otherSideFieldName}Entities"
    appendLine("        //this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id).forEach { join ->")
    appendLine("        //    this.${joinRepoFieldName}.deleteByPrimaryKey(join.id)")
    appendLine("        //}")
    blankLine()
    appendLine("        //val new${otherSideFieldNameCapitalized}Joins = editDto.${otherSideDtoFieldName}.map { joinDto ->")
    appendLine("        //    ${joinEntityClass}.newInstance(effectiveFrom = joinDto.effectiveFrom, effectiveTo = joinDto.effectiveTo, $thisSideFieldName = id, $otherSideFieldName = joinDto.${otherSideFieldName}EntityId)")
    appendLine("        //}")
    appendLine("        //this.${joinRepoFieldName}.bulkInsert(new${otherSideFieldNameCapitalized}Joins)")
} else {
    val otherSideDtoFieldName = "${otherSideFieldName}EntityIds"
    appendLine("        this.${joinRepoFieldName}.findBy${thisSideFieldNameCapitalized}(id).forEach { join ->")
    appendLine("            this.${joinRepoFieldName}.deleteByPrimaryKey(join.id)")
    appendLine("        }")
    blankLine()
    appendLine("        val new${otherSideFieldNameCapitalized}Joins = editDto.${otherSideDtoFieldName}.map { $otherSideFieldName ->")
    appendLine("            ${joinEntityClass}.newInstance($thisSideFieldName = id, $otherSideFieldName = $otherSideFieldName)")
    appendLine("        }")
    appendLine("        this.${joinRepoFieldName}.bulkInsert(new${otherSideFieldNameCapitalized}Joins)")
}
```

Note: remove `addImportFor<Instant>()` from the first branch (no longer needed there; `Instant.now()` is only used in the updater for `lastModifiedTimestampUtc` which has its own import).

- [ ] **Step 5: Regenerate showcase domain + service and verify**

```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:service:maiaGeneration
```

Expected: BUILD SUCCESSFUL. Then:

```bash
grep -A8 "leftEntities_raw" maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyUpdateRequestDto.kt
```

Expected: `private val leftEntities_raw: List<LeftJoinRequestDto>?`

```bash
grep "joinDto.effectiveFrom" maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt
```

Expected: line containing `effectiveFrom = joinDto.effectiveFrom`

- [ ] **Step 6: Compile**

```bash
./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:service:compileKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityUpdateApiDef.kt \
        maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt \
        maia-showcase/domain/src/generated/ \
        maia-showcase/service/src/generated/
git commit -m "feat: update request DTO and service use join timestamps for timestamped M2M"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityUpdateApiDef.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:service:compileKotlin", "acceptanceCriteria": ["RightManyUpdateRequestDto has leftEntities: List<LeftJoinRequestDto>", "RightManyCrudService.update uses joinDto.effectiveFrom"], "requiresUserVerification": false}
```

---

## Task 2: JoinFetchDtoDef + FetchForEditDto with Timestamps

**Goal:** `{Entity}FetchForEditDto` exposes `List<{OtherSide}JoinFetchDto>` (id, name, effectiveFrom?, effectiveTo?) for timestamped M2M associations; the row mapper sub-query includes `effective_from`/`effective_to` columns from the join table; the Kotlin data class and TypeScript interface are both rendered.

**Files:**
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/JoinFetchDtoDef.kt`
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/JoinFetchDtoFieldType.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/FieldTypes.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/RowMapperFieldDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/FetchForEditDtoDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/AbstractKotlinRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`

**Acceptance Criteria:**
- [ ] `LeftJoinFetchDto.kt` generated with fields `id`, `name`, `effectiveFrom?`, `effectiveTo?`
- [ ] `RightManyFetchForEditDto.leftEntities` is `List<LeftJoinFetchDto>` (not `List<LeftManyPkAndNameDto>`)
- [ ] `RightManyFetchForEditDtoRowMapper` sub-query selects `mtm.effective_from`, `mtm.effective_to`
- [ ] `LeftJoinFetchDto.ts` TypeScript interface generated
- [ ] `RightManyFetchForEditDto.ts` references `LeftJoinFetchDto`
- [ ] Showcase compiles: `./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:dao:compileKotlin`

**Verify:** `./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:maia-showcase-ui:maiaGeneration && cat maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyFetchForEditDtoRowMapper.kt | grep effective`
Expected: lines containing `effective_from` and `effective_to`

**Steps:**

- [ ] **Step 1: Create `JoinFetchDtoDef.kt`**

Create `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/JoinFetchDtoDef.kt`:

```kotlin
package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.builders.DtoDefBuilder
import org.maiaframework.gen.spec.definition.lang.ClassFieldDef
import org.maiaframework.gen.spec.definition.lang.FieldTypes
import org.maiaframework.gen.spec.definition.lang.PackageName
import org.maiaframework.gen.spec.definition.lang.TypescriptImport

class JoinFetchDtoDef(
    packageName: PackageName,
    otherSideDisplayName: String,          // e.g. "Left" (the other side's display name, spaces removed)
    val nameTableColumnName: String,        // e.g. "some_string" — column on the other-side entity table
    val joinEntitySchemaAndTableName: String, // e.g. "maia.left_to_right_many_to_many_join"
    val otherSideIdTableColumnName: String, // e.g. "left_id" — FK column in the join table
    val thisSideIdTableColumnName: String,  // e.g. "right_id" — FK column in the join table
    val otherSideEntitySchemaAndTableName: String, // e.g. "maia.left_many"
) {

    private val dtoBaseName = DtoBaseName("${otherSideDisplayName}Join")

    val dtoDef = DtoDefBuilder(
        packageName,
        dtoBaseName,
        DtoSuffix("FetchDto"),
        listOf(
            ClassFieldDef.aClassField("id", FieldTypes.domainId).build(),
            ClassFieldDef.aClassField("name", FieldTypes.string).build(),
            ClassFieldDef.aClassField("effectiveFrom", FieldTypes.instant).nullable().build(),
            ClassFieldDef.aClassField("effectiveTo", FieldTypes.instant).nullable().build(),
        )
    ).build()

    val uqcn = dtoDef.uqcn

    val fqcn = dtoDef.fqcn

    private val typescriptFilePathWithoutSuffix = "app/gen-components/${packageName.asTypescriptDirs()}/${uqcn}"

    val typescriptImport = TypescriptImport(uqcn.value, "@$typescriptFilePathWithoutSuffix")

    val typescriptRenderedFilePath = "$typescriptFilePathWithoutSuffix.ts"

}
```

- [ ] **Step 2: Create `JoinFetchDtoFieldType.kt`**

Create `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/JoinFetchDtoFieldType.kt`:

```kotlin
package org.maiaframework.gen.spec.definition.lang

import org.maiaframework.gen.spec.definition.JoinFetchDtoDef

class JoinFetchDtoFieldType(val joinFetchDtoDef: JoinFetchDtoDef) : FieldType {
    val uqcn = joinFetchDtoDef.uqcn
    val fqcn = joinFetchDtoDef.fqcn
    val typescriptImport = joinFetchDtoDef.typescriptImport
}
```

- [ ] **Step 3: Add `JoinFetchDtoFieldType` to `FieldTypes.kt`**

In `FieldTypes.kt`, add a factory function alongside `pkAndName`:

```kotlin
fun joinFetchDto(joinFetchDtoDef: JoinFetchDtoDef) = JoinFetchDtoFieldType(joinFetchDtoDef)
```

Add `is JoinFetchDtoFieldType -> false` to each of the five `when` expressions (`canHaveLengthConstraint`, `isNumeric`, `isStringBased`, `isBooleanBased`, `isValueFieldWrapper`). Pattern: add after each `is PkAndNameFieldType -> false` line.

Add the import at the top:
```kotlin
import org.maiaframework.gen.spec.definition.JoinFetchDtoDef
```

- [ ] **Step 4: Add `joinFetchDtoDefsByAssociation` to `EntityDef`**

In `EntityDef.kt`, add a new private property after `fetchForEditManyToManyFieldDefs`:

```kotlin
private val joinFetchDtoDefsByAssociation: Map<ManyToManyEntityDef, JoinFetchDtoDef> by lazy {
    manyToManyAssociations
        .filter { it.entityDef.hasEffectiveTimestamps.value }
        .associateWith { m2m ->
            val otherSide = m2m.otherSideFrom(this)
            JoinFetchDtoDef(
                packageName = this.packageName,
                otherSideDisplayName = otherSide.displayName.replace(" ", ""),
                nameTableColumnName = otherSide.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName,
                joinEntitySchemaAndTableName = m2m.entityDef.schemaAndTableName,
                otherSideIdTableColumnName = m2m.idTableColumnName(otherSide.entityDef),
                thisSideIdTableColumnName = m2m.idTableColumnName(this),
                otherSideEntitySchemaAndTableName = otherSide.entityDef.schemaAndTableName,
            )
        }
}
```

- [ ] **Step 5: Change `fetchForEditManyToManyFieldDefs` in `EntityDef`**

Replace the existing `fetchForEditManyToManyFieldDefs` lazy property:

```kotlin
private val fetchForEditManyToManyFieldDefs: List<ManyToManySearchableDtoFieldDef> by lazy {
    this.manyToManyAssociations.map { manyToManyEntityDef ->
        val otherSide = manyToManyEntityDef.otherSideFrom(this)
        val fieldName = "${otherSide.fieldName}Entities"
        val fieldType = if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
            val joinFetchDtoDef = joinFetchDtoDefsByAssociation[manyToManyEntityDef]!!
            FieldTypes.list(FieldTypes.joinFetchDto(joinFetchDtoDef))
        } else {
            FieldTypes.list(FieldTypes.pkAndName(otherSide.entityDef.entityPkAndNameDef))
        }
        val classFieldDef = aClassField(fieldName, fieldType).build()
        ManyToManySearchableDtoFieldDef(classFieldDef, manyToManyEntityDef, null, Nullability.NOT_NULLABLE)
    }
}
```

Add a public accessor for the join-fetch defs (needed by ModelDef and generator):

```kotlin
val joinFetchDtoDefs: List<JoinFetchDtoDef> by lazy {
    joinFetchDtoDefsByAssociation.values.toList()
}

fun joinFetchDtoDefFor(m2m: ManyToManyEntityDef): JoinFetchDtoDef? = joinFetchDtoDefsByAssociation[m2m]
```

- [ ] **Step 6: Add `joinFetchDtoDef` to `ManyToManyRowMapperFieldDef`**

In `RowMapperFieldDef.kt`, add a `joinFetchDtoDef: JoinFetchDtoDef?` parameter:

```kotlin
class ManyToManyRowMapperFieldDef(
    val manyToManySearchableDtoFieldDef: ManyToManySearchableDtoFieldDef,
    val rootEntityDef: EntityDef,
    val joinFetchDtoDef: JoinFetchDtoDef? = null   // non-null for timestamped associations
) : RowMapperFieldDef( ... ) {
    // existing properties unchanged
}
```

- [ ] **Step 7: Propagate `joinFetchDtoDef` in `FetchForEditDtoDef`**

In `FetchForEditDtoDef.kt`, in the `rowMapperFieldDefs` lambda that builds `ManyToManyRowMapperFieldDef`, look for:

```kotlin
is ManyToManySearchableDtoFieldDef -> ManyToManyRowMapperFieldDef(it, this.dtoRootEntityDef)
```

Change to:

```kotlin
is ManyToManySearchableDtoFieldDef -> {
    val joinFetchDtoDef = this.dtoRootEntityDef
        ?.joinFetchDtoDefFor(it.manyToManyEntityDef)
    ManyToManyRowMapperFieldDef(it, this.dtoRootEntityDef!!, joinFetchDtoDef)
}
```

Note: `this.dtoRootEntityDef` is the parameter of type `EntityDef?`. It may be null for non-edit row mappers, in which case `joinFetchDtoDef` is null and the original behaviour is preserved.

- [ ] **Step 8: Add `joinFetchDtoDefs` to `ModelDef`**

In `ModelDef.kt`, add:

```kotlin
val joinFetchDtoDefs: List<JoinFetchDtoDef> = entityDefs.filter { it.isConcrete }
    .flatMap { it.joinFetchDtoDefs }
    .distinctBy { it.fqcn }
```

- [ ] **Step 9: Update `RowMapperRenderer` for timestamped M2M**

In `RowMapperRenderer.kt`:

**`renderPreClassFields()`**: Add a branch for join-fetch DTOs — they use inline lambdas so no pre-built row mapper field is needed. Only add the `pkAndNameDtoRowMapper` field when `joinFetchDtoDef == null`:

```kotlin
this.rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->
    if (manyToManyRowMapperFieldDef.joinFetchDtoDef == null) {
        val entityPkAndNameDef = manyToManyRowMapperFieldDef.entityPkAndNameDef
        addImportFor(entityPkAndNameDef.rowMapperDef.classDef.fqcn)
        append("""
            |
            |
            |    private val ${manyToManyRowMapperFieldDef.classFieldName}PkAndNameDtoRowMapper = ${entityPkAndNameDef.rowMapperDef.classDef.uqcn}()
            |""".trimMargin())
    }
}
```

**`render function mapRow()`**: Change local variable name based on whether join-fetch is used:

```kotlin
rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->
    val classFieldName = manyToManyRowMapperFieldDef.classFieldName
    if (manyToManyRowMapperFieldDef.joinFetchDtoDef != null) {
        appendLine("        val ${classFieldName}JoinFetchDtoList = fetch${classFieldName.firstToUpper()}JoinFetchDtos(entityId)")
    } else {
        appendLine("        val ${classFieldName}PkAndNameDtoList = fetch${classFieldName.firstToUpper()}PkAndNameDtos(entityId)")
    }
    blankLine()
}
```

Update `fieldNames.add(...)` in the `is ManyToManyRowMapperFieldDef` branch:

```kotlin
is ManyToManyRowMapperFieldDef -> {
    if (rowMapperFieldDef.joinFetchDtoDef != null) {
        fieldNames.add("${rowMapperFieldDef.classFieldName}JoinFetchDtoList")
    } else {
        fieldNames.add("${rowMapperFieldDef.classFieldName}PkAndNameDtoList")
    }
}
```

**`render functions for manyToManyPkAndNameDtos()`**: For timestamped M2M, generate a `fetchXxxJoinFetchDtos` function using an inline lambda row mapper:

```kotlin
private fun `render functions for manyToManyPkAndNameDtos`() {
    val tripleQuote = "\"\"\""

    this.rowMapperDef.manyToManyFieldDefs.forEach { manyToManyRowMapperFieldDef ->

        val joinFetchDtoDef = manyToManyRowMapperFieldDef.joinFetchDtoDef
        if (joinFetchDtoDef != null) {
            // Timestamped: fetch with effective timestamps
            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)
            addImportFor(joinFetchDtoDef.fqcn)

            val classFieldName = manyToManyRowMapperFieldDef.classFieldName
            val otherSideEntity = manyToManyRowMapperFieldDef.otherSideEntity

            append("""
                |
                |
                |    private fun fetch${classFieldName.firstToUpper()}JoinFetchDtos(entityId: DomainId): List<${joinFetchDtoDef.uqcn}> {
                |
                |        return this.jdbcOps.queryForList(
                |            $tripleQuote
                |            select
                |                other.id,
                |                other.${joinFetchDtoDef.nameTableColumnName},
                |                mtm.effective_from,
                |                mtm.effective_to
                |            from ${joinFetchDtoDef.otherSideEntitySchemaAndTableName} other
                |            join ${joinFetchDtoDef.joinEntitySchemaAndTableName} mtm
                |                on other.id = mtm.${joinFetchDtoDef.otherSideIdTableColumnName}
                |            where mtm.${joinFetchDtoDef.thisSideIdTableColumnName} = :entityId
                |            order by other.${joinFetchDtoDef.nameTableColumnName}
                |            $tripleQuote.trimIndent(),
                |            SqlParams().apply {
                |                addValue("entityId", entityId)
                |            },
                |        ) { rsa ->
                |            ${joinFetchDtoDef.uqcn}(
                |                id = rsa.readDomainId("id"),
                |                name = rsa.readString("${joinFetchDtoDef.nameTableColumnName}"),
                |                effectiveFrom = rsa.readInstantOrNull("effective_from"),
                |                effectiveTo = rsa.readInstantOrNull("effective_to"),
                |            )
                |        }
                |
                |    }
                |""".trimMargin())

        } else {
            // Non-timestamped: existing PkAndName logic (unchanged)
            val entityPkAndNameDef = manyToManyRowMapperFieldDef.entityPkAndNameDef
            addImportFor(Fqcns.MAIA_DOMAIN_ID)
            addImportFor(Fqcns.MAIA_SQL_PARAMS)
            addImportFor(entityPkAndNameDef.pkAndNameDtoFqcn)

            val classFieldName = manyToManyRowMapperFieldDef.classFieldName
            val otherSideEntity = manyToManyRowMapperFieldDef.otherSideEntity
            val otherSideIdTableColumnName = manyToManyRowMapperFieldDef.otherSideIdTableColumnName
            val thisSideIdTableColumnName = manyToManyRowMapperFieldDef.thisSideIdTableColumnName
            val manyToManyEntityDef = manyToManyRowMapperFieldDef.manyToManySearchableDtoFieldDef.manyToManyEntityDef

            append("""
                |
                |
                |    private fun fetch${classFieldName.firstToUpper()}PkAndNameDtos(entityId: DomainId): List<${entityPkAndNameDef.pkAndNameDtoFqcn.uqcn}> {
                |
                |        return this.jdbcOps.queryForList(
                |            $tripleQuote
                |            select
                |                other.id,
                |                other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                |            from ${otherSideEntity.entityDef.schemaAndTableName} other
                |            join ${manyToManyEntityDef.entityDef.schemaAndTableName} mtm
                |                on other.id = mtm.${otherSideIdTableColumnName}
                |            where mtm.${thisSideIdTableColumnName} = :entityId
                |            order by other.${otherSideEntity.entityDef.entityPkAndNameDef.nameEntityFieldDef.tableColumnName}
                |            $tripleQuote.trimIndent(),
                |            SqlParams().apply {
                |                addValue("entityId", entityId)
                |            },
                |            this.${classFieldName}PkAndNameDtoRowMapper
                |        )
                |
                |    }
                |""".trimMargin())
        }
    }
}
```

Note: `rsa.readInstantOrNull` — check that `ResultSetAdapter` has this method. If not, use `rsa.readInstantNullable` or whatever the nullable instant reader is named. Look at an existing nullable Instant field in any entity row mapper for the exact method name.

- [ ] **Step 10: Handle `JoinFetchDtoFieldType` in `AbstractKotlinRenderer`**

In `AbstractKotlinRenderer.kt`, in the `addImportFor(FieldType)` function, add after the `is PkAndNameFieldType` case:

```kotlin
is JoinFetchDtoFieldType -> addImportFor(fieldType.fqcn)
```

Add import at the top of the file:
```kotlin
import org.maiaframework.gen.spec.definition.lang.JoinFetchDtoFieldType
```

- [ ] **Step 11: Handle `JoinFetchDtoFieldType` in `TypescriptInterfaceDtoRenderer`**

In `TypescriptInterfaceDtoRenderer.kt`, in the field-type-to-TypeScript-string mapping for list element types, add after `is PkAndNameFieldType -> parameterFieldType.uqcn.value`:

```kotlin
is JoinFetchDtoFieldType -> parameterFieldType.uqcn.value
```

In the imports section (where imports for list element types are added), add after the `is PkAndNameFieldType` case:

```kotlin
is JoinFetchDtoFieldType -> addImport(parameterFieldType.typescriptImport)
```

Add import at the top:
```kotlin
import org.maiaframework.gen.spec.definition.lang.JoinFetchDtoFieldType
```

- [ ] **Step 12: Render Kotlin data classes in `DomainModuleGenerator`**

In `DomainModuleGenerator.kt`, add a new rendering step after `render FetchForEditDtos`:

```kotlin
`render JoinFetchDtos`()
```

Add the function:

```kotlin
private fun `render JoinFetchDtos`() {
    this.modelDef.joinFetchDtoDefs.forEach { renderDto(it.dtoDef) }
}
```

Add it to the `onGenerateSource()` call list (alongside `render FetchForEditDtos()`).

- [ ] **Step 13: Render TypeScript interfaces in `AngularUiModuleGenerator`**

In `AngularUiModuleGenerator.kt`, in `renderEntityDetailsDtos()`, add after the existing loop that renders `manyToManyTimestampedJoinRequestDtoDefs`:

```kotlin
this.modelDef.joinFetchDtoDefs.forEach { joinFetchDtoDef ->
    renderTypescriptInterface(
        renderedFilePath = joinFetchDtoDef.typescriptRenderedFilePath,
        className = joinFetchDtoDef.uqcn,
        fields = joinFetchDtoDef.dtoDef.allFieldsSorted,
        dtoCharacteristics = setOf(DtoCharacteristic.RESPONSE_DTO)
    )
}
```

- [ ] **Step 14: Regenerate showcase and verify**

```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:dao:maiaGeneration :maia-showcase:maia-showcase-ui:maiaGeneration
```

Check for `LeftJoinFetchDto.kt`:
```bash
cat maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftJoinFetchDto.kt
```
Expected: data class with `id`, `name`, `effectiveFrom`, `effectiveTo` fields.

Check `RightManyFetchForEditDto.kt`:
```bash
grep "leftEntities" maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyFetchForEditDto.kt
```
Expected: `val leftEntities: List<LeftJoinFetchDto>`

Check row mapper SQL:
```bash
grep -A20 "fetchLeftEntitiesJoinFetchDtos" maia-showcase/dao/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyFetchForEditDtoRowMapper.kt
```
Expected: SQL with `mtm.effective_from`, `mtm.effective_to`.

Check TypeScript:
```bash
find maia-showcase/maia-showcase-ui/src/generated -name "LeftJoinFetchDto.ts"
```
Expected: file exists.

- [ ] **Step 15: Compile all affected modules**

```bash
./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:dao:compileKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 16: Commit**

```bash
git add maia-gen/ maia-showcase/domain/src/generated/ maia-showcase/dao/src/generated/ maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: FetchForEditDto returns join timestamps for timestamped M2M associations"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/JoinFetchDtoDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/JoinFetchDtoFieldType.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/FieldTypes.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/RowMapperFieldDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/FetchForEditDtoDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/ModelDef.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RowMapperRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/AbstractKotlinRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt"], "verifyCommand": "./gradlew :maia-showcase:domain:compileKotlin :maia-showcase:dao:compileKotlin", "acceptanceCriteria": ["LeftJoinFetchDto.kt generated with 4 fields", "RightManyFetchForEditDto.leftEntities is List<LeftJoinFetchDto>", "Row mapper SQL includes effective_from and effective_to", "LeftJoinFetchDto.ts generated"], "requiresUserVerification": false}
```

---

## Task 3: Edit Form Renderers

**Goal:** The edit form HTML uses mini forms (join-entry list + inline typeahead + date pickers) for timestamped M2M associations, replacing the chip grid; the component TypeScript pre-populates join entries from the fetched DTO timestamps.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditReactiveFormHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

**Acceptance Criteria:**
- [ ] `right-many-entity-edit-form.html` has `.join-entries` div and `.join-mini-form` div (no `mat-chip-grid`)
- [ ] `right-many-entity-edit-form.ts` has `leftJoins` array and `displayLeftEntity` method
- [ ] `right-many-entity-edit-form.ts` `ngOnInit` populates `leftJoins` from `dto.leftEntities`
- [ ] Same for `left-many-entity-edit-form.html` / `.ts`
- [ ] Showcase UI compiles: `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration`

**Verify:** `grep "join-entries\|leftJoins\|dto.leftEntities" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-edit-form.html maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-edit-form.ts`

**Steps:**

- [ ] **Step 1: Fix `manyToManyChipFieldsForEdit` to exclude timestamped associations**

In `AngularUiModuleGenerator.kt`, update `manyToManyChipFieldsForEdit`:

```kotlin
private fun manyToManyChipFieldsForEdit(
    entityDef: EntityDef,
    associations: List<ManyToManyEntityDef>
): List<ManyToManyChipFieldDef> {
    return associations
        .filter { !it.entityDef.hasEffectiveTimestamps.value }   // ← add this filter
        .mapNotNull { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
            ManyToManyChipFieldDef(entityDef, m2m, typeaheadDef)
        }
}
```

- [ ] **Step 2: Add `manyToManyTimestampedFieldsForEdit` to `AngularUiModuleGenerator`**

Add this new private function alongside `manyToManyTimestampedFieldsFor`:

```kotlin
private fun manyToManyTimestampedFieldsForEdit(
    entityDef: EntityDef,
    associations: List<ManyToManyEntityDef>
): List<ManyToManyTimestampedFieldDef> {
    val updateApiDef = entityDef.entityCrudApiDef?.updateApiDef ?: return emptyList()
    return associations
        .filter { it.entityDef.hasEffectiveTimestamps.value }
        .mapNotNull { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
            val joinDtoDef = updateApiDef.timestampedJoinRequestDtosByAssociation[m2m] ?: return@mapNotNull null
            ManyToManyTimestampedFieldDef(entityDef, m2m, typeaheadDef, joinDtoDef)
        }
}
```

- [ ] **Step 3: Update `renderEntityEditPages` in `AngularUiModuleGenerator`**

In `renderEntityEditPages()`, replace the existing body:

```kotlin
val chipFields = manyToManyChipFieldsForEdit(entityEditPageDef.entityDef, entityEditPageDef.entityDef.manyToManyAssociations)
val timestampedFields = manyToManyTimestampedFieldsForEdit(entityEditPageDef.entityDef, entityEditPageDef.entityDef.manyToManyAssociations)

// ... (keep existing angularFormDef construction unchanged) ...

val providerServices = angularFormDef.allTypeaheadDefs.map { it.angularServiceClassName } +
    chipFields.map { it.serviceClassName } +
    timestampedFields.map { it.serviceClassName }

AngularReactiveFormComponentRenderer(
    angularFormDef,
    entityEditPageDef.editFormAngularComponentNames,
    providerServices,
    chipFields,
    timestampedFields   // ← add this
).renderToDir(this.typescriptOutputDir)

EntityEditFormScssRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
EntityEditReactiveFormHtmlRenderer(
    entityEditPageDef.updateApiDef,
    entityEditPageDef.editFormAngularComponentNames,
    chipFields,
    timestampedFields   // ← add this
).renderToDir(this.typescriptOutputDir)
EntityEditFormPageComponentRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
EntityEditPageHtmlRenderer(entityEditPageDef).renderToDir(this.typescriptOutputDir)
```

- [ ] **Step 4: Update `EntityEditReactiveFormHtmlRenderer` to accept `timestampedFields`**

In `EntityEditReactiveFormHtmlRenderer.kt`, add `timestampedFields` parameter and pass to parent:

```kotlin
class EntityEditReactiveFormHtmlRenderer(
    private val apiDef: EntityUpdateApiDef,
    private val componentNamesOverride: AngularComponentNames? = null,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    override val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()  // ← add
) : AbstractCrudReactiveFormHtmlRenderer(
    apiDef.entityDef,
    InlineFormOrDialog.INLINE_FORM
) {
    // ... unchanged ...
}
```

- [ ] **Step 5: Add edit pre-population in `AngularReactiveFormComponentRenderer`**

In `AngularReactiveFormComponentRenderer.kt`, find the `fetchForEditDtoDef` block in the `ngOnInit` section. It currently looks like:

```kotlin
chipFields.forEach { chip ->
    appendLine("                this.${chip.selectedFieldName} = dto.${chip.fetchForEditDtoFieldName}.map(r => ({")
    appendLine("                    ${chip.esDocIdFieldName}: r.${chip.esDocIdFieldName},")
    appendLine("                    ${chip.searchTermFieldName}: r.name,")
    appendLine("                }));")
}
appendLine("                this.loading.set(false);")
```

Add a loop for timestamped fields BEFORE `loading.set(false)`:

```kotlin
chipFields.forEach { chip ->
    appendLine("                this.${chip.selectedFieldName} = dto.${chip.fetchForEditDtoFieldName}.map(r => ({")
    appendLine("                    ${chip.esDocIdFieldName}: r.${chip.esDocIdFieldName},")
    appendLine("                    ${chip.searchTermFieldName}: r.name,")
    appendLine("                }));")
}

// ← ADD THIS BLOCK:
timestampedFields.forEach { field ->
    appendLine("                this.${field.joinsFieldName} = dto.${field.requestDtoFieldName}?.map(e => ({")
    appendLine("                    entityId: e.id,")
    appendLine("                    entityName: e.name,")
    appendLine("                    effectiveFrom: e.effectiveFrom ? new Date(e.effectiveFrom) : null,")
    appendLine("                    effectiveTo: e.effectiveTo ? new Date(e.effectiveTo) : null,")
    appendLine("                })) ?? [];")
}

appendLine("                this.loading.set(false);")
```

This block is only reached when `fetchForEditDtoDef != null`, which is only true for edit forms — so the pre-population is edit-only by construction.

- [ ] **Step 6: Regenerate showcase UI and verify**

```bash
./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration
```

Check HTML:
```bash
grep -c "join-entries\|join-mini-form" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-edit-form.html
```
Expected: 2 (one for each class)

Check no chip grid remains:
```bash
grep "mat-chip-grid" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-edit-form.html
```
Expected: no output

Check TS pre-population:
```bash
grep "dto.leftEntities" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-edit-form.ts
```
Expected: line mapping `dto.leftEntities` to `this.leftJoins`

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/ \
        maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: edit form uses mini forms for timestamped M2M associations"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityEditReactiveFormHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration", "acceptanceCriteria": ["right-many-entity-edit-form.html has join-entries and join-mini-form divs", "right-many-entity-edit-form.ts has leftJoins pre-population from dto.leftEntities", "No mat-chip-grid in edit form HTML"], "requiresUserVerification": false}
```

---

## Task 4: Update Playwright Tests

**Goal:** `RightManyEditPage` and `LeftManyEditPage` interact with join-entry lists instead of chip grids; the Playwright tests exercise add/remove via mini form and pass.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftManyCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `RightManyEditPage` has `assertJoinEntryVisible`, `removeJoinEntry` — no chip methods
- [ ] `LeftManyEditPage` same
- [ ] `RightManyCrudPlaywrightTest` uses new methods; test passes
- [ ] `LeftManyCrudPlaywrightTest` uses new methods; test passes

**Verify:** `./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest" --tests "org.maiaframework.showcase.many_to_many.LeftManyCrudPlaywrightTest"` → 2 tests passed

**Steps:**

- [ ] **Step 1: Rewrite `RightManyEditPage.kt`**

Replace the entire file content:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class RightManyEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/right-many/edit",
    "right_many_edit"
) {


    fun assertJoinEntryVisible(entityName: String) {
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor()
    }


    fun removeJoinEntry(entityName: String) {
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button[type='button']")
            .click()
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))
    }


    fun fillForm(
        someString: String = "testright_edited",
    ) {
        page.locator("input[name='someString']").fill(someString)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
```

- [ ] **Step 2: Rewrite `LeftManyEditPage.kt`**

Replace the entire file content:

```kotlin
package org.maiaframework.showcase.testing.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import org.maiaframework.webtesting.AbstractPage
import org.maiaframework.webtesting.UrlHelper


class LeftManyEditPage(
    private val page: Page,
    urlHelper: UrlHelper
) : AbstractPage(
    page,
    urlHelper,
    "/left-many/edit",
    "left_many_edit"
) {


    fun assertJoinEntryVisible(entityName: String) {
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor()
    }


    fun removeJoinEntry(entityName: String) {
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .locator("button[type='button']")
            .click()
        page.locator(".join-entry")
            .filter(Locator.FilterOptions().setHasText(entityName))
            .waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN))
    }


    fun fillEditForm(
        someString: String = "testleft_edited",
    ) {
        page.locator("input[name='someString']").fill(someString)
    }


    fun clickSubmitButton() {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Submit"))
            .click(Locator.ClickOptions().setForce(true))
    }


}
```

- [ ] **Step 3: Update `RightManyCrudPlaywrightTest`**

In `RightManyCrudPlaywrightTest.kt`, replace the edit-page interaction block:

```kotlin
// OLD:
rightManyEditPage.apply {
    assertOnPage()
    assertChipVisible("left-alpha")
    removeChip("left-alpha")
    fillForm(someString = "testright_edited")
    clickSubmitButton()
}

// NEW:
rightManyEditPage.apply {
    assertOnPage()
    assertJoinEntryVisible("left-alpha")
    removeJoinEntry("left-alpha")
    fillForm(someString = "testright_edited")
    clickSubmitButton()
}
```

- [ ] **Step 4: Update `LeftManyCrudPlaywrightTest`**

In `LeftManyCrudPlaywrightTest.kt`, replace the edit-page interaction block:

```kotlin
// OLD:
leftManyEditPage.apply {
    assertOnPage()
    assertChipVisible("right-alpha")
    assertChipVisible("right-beta")
    removeChip("right-alpha")
    removeChip("right-beta")
    fillEditForm()
    clickSubmitButton()
}

// NEW:
leftManyEditPage.apply {
    assertOnPage()
    assertJoinEntryVisible("right-alpha")
    assertJoinEntryVisible("right-beta")
    removeJoinEntry("right-alpha")
    removeJoinEntry("right-beta")
    fillEditForm()
    clickSubmitButton()
}
```

- [ ] **Step 5: Run Playwright tests**

Make sure the app is running (start it if not: `docker compose -f maia-showcase/compose.yaml up -d` then start the app).

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest" --tests "org.maiaframework.showcase.many_to_many.LeftManyCrudPlaywrightTest" -i 2>&1 | tail -20
```

Expected: `2 tests completed, 0 failures`

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/app/src/test/
git commit -m "test: update edit page Playwright tests for mini-form join entries"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyEditPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftManyEditPage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftManyCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests 'org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest' --tests 'org.maiaframework.showcase.many_to_many.LeftManyCrudPlaywrightTest'", "acceptanceCriteria": ["RightManyCrudPlaywrightTest passes", "LeftManyCrudPlaywrightTest passes"], "requiresUserVerification": false}
```

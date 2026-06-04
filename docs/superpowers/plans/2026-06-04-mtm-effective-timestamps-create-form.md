# ManyToMany Effective Timestamps — Create Form Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a manyToMany join entity has effective timestamps, the generator produces a per-join request DTO, an inline "Add" mini-form with date/time pickers, and a backend that uses user-supplied timestamps instead of `Instant.now()`.

**Architecture:** Six tasks, each committable: (1) new metadata class, (2) DTO model + rendering, (3) backend service, (4) Angular component state/methods, (5) Angular HTML template, (6) Playwright test update. Tasks 2–5 each modify a generator file and then regenerate the showcase to verify the output. Task 6 runs the existing Playwright test suite.

**Tech Stack:** Kotlin, Spring Boot, Angular 19, Angular Material 19, Playwright; Gradle wrapper `./gradlew`.

**User Verification:** NO

---

## File Map

| File | Action | Task |
|---|---|---|
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/ManyToManyTimestampedFieldDef.kt` | Create | 1 |
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityCreateApiDef.kt` | Modify | 2 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt` | Modify | 2 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt` | Modify | 2, 4 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt` | Modify | 2 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt` | Modify | 3 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt` | Modify | 4 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt` | Modify | 4, 5 |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCreateReactiveFormHtmlRenderer.kt` | Modify | 4 |
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt` | Modify | 6 |
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt` | Modify | 6 |

**Showcase generated files (do not edit manually — regenerated each task):**
- `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCreateRequestDto.kt`
- `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftJoinRequestDto.kt` ← new
- `maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/RightManyCreateRequestDto.ts`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/LeftJoinRequestDto.ts` ← new
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.ts`
- `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.html`

---

### Task 1: `ManyToManyTimestampedFieldDef` — new metadata class

**Goal:** Create the metadata class that carries every generated name for the timestamped join pattern.

**Files:**
- Create: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/ManyToManyTimestampedFieldDef.kt`

**Acceptance Criteria:**
- [ ] File compiles cleanly
- [ ] All name-deriving properties match the naming convention established in the design doc (camelCase from `displayName`)

**Verify:** `./gradlew :maia-gen:maia-gen-generator:compileKotlin` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create the file**

```kotlin
package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.ManyToManyEntityDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.TypeaheadDef
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.lang.text.StringFunctions


data class ManyToManyTimestampedFieldDef(
    val entityDef: EntityDef,
    val manyToManyEntityDef: ManyToManyEntityDef,
    val typeaheadDef: TypeaheadDef,
    val joinRequestDtoDef: RequestDtoDef
) {

    private val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)

    val fieldName: String = otherSide.fieldName
    val displayName: String = otherSide.displayName

    val joinsFieldName: String = "${fieldName}Joins"
    val showFormSignalName: String = "show${displayName}JoinForm"
    val addEntityControlName: String = "add${displayName}JoinEntityControl"
    val effectiveFromControlName: String = "add${displayName}JoinEffectiveFromControl"
    val effectiveToControlName: String = "add${displayName}JoinEffectiveToControl"
    val filteredFieldName: String = "filtered${displayName}Entities"
    val filteredIsLoadingFieldName: String = "${filteredFieldName}IsLoading"
    val confirmMethodName: String = "confirmAdd${displayName}Join"
    val cancelMethodName: String = "cancelAdd${displayName}Join"
    val removeMethodName: String = "remove${displayName}Join"
    val joinEntryTypeName: String = "${displayName}JoinEntry"
    val requestDtoFieldName: String = "${fieldName}Entities"
    val joinRequestDtoClassName: String = joinRequestDtoDef.uqcn.value

    val esDocClassName: String = typeaheadDef.esDocDef.dtoDef.uqcn.value
    val serviceClassName: String = typeaheadDef.angularServiceClassName
    val serviceFieldName: String = StringFunctions.firstToLower(serviceClassName)
    val searchTermFieldName: String = typeaheadDef.searchTermFieldName
    val esDocIdFieldName: String = typeaheadDef.esDocIdFieldName

    val serviceImport: TypescriptImport = typeaheadDef.typescriptServiceImport
    val esDocImport: TypescriptImport = typeaheadDef.esDocDef.dtoDef.typescriptDtoImport
    val joinRequestDtoTypescriptImport: TypescriptImport = joinRequestDtoDef.typescriptImport

    val labelText: String = "$displayName Entities"
    val searchPlaceholder: String = "Search $labelText..."
    val autocompleteRefName: String = "${fieldName}JoinEntityAuto"

}
```

- [ ] **Step 2: Verify compile**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/ManyToManyTimestampedFieldDef.kt
git commit -m "feat: add ManyToManyTimestampedFieldDef metadata class"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/ManyToManyTimestampedFieldDef.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:compileKotlin", "acceptanceCriteria": ["File compiles cleanly", "All name-deriving properties match design doc"], "requiresUserVerification": false}
```

---

### Task 2: Join request DTO — spec model + Kotlin/TypeScript rendering

**Goal:** When a manyToMany join entity has effective timestamps, `EntityCreateApiDef` produces a separate `LeftJoinRequestDto` and changes the parent DTO field from `leftEntityIds: List<DomainId>` to `leftEntities: List<LeftJoinRequestDto>`. Both Kotlin and TypeScript files are generated.

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityCreateApiDef.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt`

**Acceptance Criteria:**
- [ ] Generator compiles
- [ ] `LeftJoinRequestDto.kt` is generated with fields `leftEntityId: DomainId`, `effectiveFrom: Instant?`, `effectiveTo: Instant?`
- [ ] `RightManyCreateRequestDto.kt` has `leftEntities: List<LeftJoinRequestDto>` (not `leftEntityIds`)
- [ ] `LeftJoinRequestDto.ts` is generated with `leftEntityId: string`, `effectiveFrom: string | null`, `effectiveTo: string | null`
- [ ] `RightManyCreateRequestDto.ts` has `leftEntities?: ReadonlyArray<LeftJoinRequestDto>`
- [ ] Non-timestamped associations still generate `leftEntityIds: List<DomainId>` (no regression)

**Verify:**
```
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:app:maiaGeneration
```
Then check:
```
grep -n "leftEntities\|leftEntityIds" maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCreateRequestDto.kt
grep -n "leftEntities\|leftEntityIds" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/RightManyCreateRequestDto.ts
cat maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftJoinRequestDto.kt
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/LeftJoinRequestDto.ts
```

**Steps:**

- [ ] **Step 1: Add join DTO computation to `EntityCreateApiDef`**

In `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityCreateApiDef.kt`, add imports and two new lazy properties before `dtoFields`, then modify `dtoFields`:

Add these imports (if not already present):
```kotlin
import org.maiaframework.gen.spec.definition.lang.FieldTypes
```

Add these new lazy properties after the existing `private val angularComponentBaseName` line:

```kotlin
    val timestampedJoinRequestDtosByAssociation: Map<ManyToManyEntityDef, RequestDtoDef> by lazy {
        entityDef.manyToManyAssociations
            .filter { it.entityDef.hasEffectiveTimestamps.value }
            .associateWith { m2m ->
                val otherSide = m2m.otherSideFrom(entityDef)
                RequestDtoDef(
                    dtoBaseName = DtoBaseName("${otherSide.displayName}Join"),
                    packageName = entityDef.packageName,
                    dtoFieldDefs = listOf(
                        RequestDtoFieldDef(
                            ClassFieldDef.aClassField("${otherSide.fieldName}EntityId", FieldTypes.domainId).build(),
                            null
                        ),
                        RequestDtoFieldDef(
                            ClassFieldDef.aClassField("effectiveFrom", FieldTypes.instant).nullable().build(),
                            null
                        ),
                        RequestDtoFieldDef(
                            ClassFieldDef.aClassField("effectiveTo", FieldTypes.instant).nullable().build(),
                            null
                        )
                    ),
                    preAuthorizeExpression = null,
                    moduleName = moduleName
                )
            }
    }

    val manyToManyTimestampedJoinRequestDtoDefs: List<RequestDtoDef> by lazy {
        timestampedJoinRequestDtosByAssociation.values.toList()
    }
```

Modify the existing `.plus(entityDef.manyToManyAssociations.map { ... })` block in `dtoFields` to branch on `hasEffectiveTimestamps`:

Find:
```kotlin
        .plus(
            entityDef.manyToManyAssociations.map { manyToManyEntityDef ->
                val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)
                val classFieldDef = ClassFieldDef.aClassField(
                    "${otherSide.fieldName}EntityIds",
                    FieldTypes.list(FieldTypes.domainId)
                ).nullable().build()
                RequestDtoFieldDef(classFieldDef, null)
            }
        )
```

Replace with:
```kotlin
        .plus(
            entityDef.manyToManyAssociations.map { manyToManyEntityDef ->
                val otherSide = manyToManyEntityDef.otherSideFrom(entityDef)
                if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
                    val joinDtoDef = timestampedJoinRequestDtosByAssociation[manyToManyEntityDef]!!
                    val classFieldDef = ClassFieldDef.aClassField(
                        "${otherSide.fieldName}Entities",
                        FieldTypes.list(FieldTypes.requestDto(joinDtoDef))
                    ).nullable().build()
                    RequestDtoFieldDef(classFieldDef, null)
                } else {
                    val classFieldDef = ClassFieldDef.aClassField(
                        "${otherSide.fieldName}EntityIds",
                        FieldTypes.list(FieldTypes.domainId)
                    ).nullable().build()
                    RequestDtoFieldDef(classFieldDef, null)
                }
            }
        )
```

- [ ] **Step 2: Render join DTOs in `DomainModuleGenerator`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt`, modify `processCrudApiDef` to render join DTOs alongside the main create DTO:

Find:
```kotlin
    private fun processCrudApiDef(entityCrudApiDef: EntityCrudApiDef) {

        entityCrudApiDef.createApiDef?.let { renderRequestDto(it.requestDtoDef) }
```

Replace with:
```kotlin
    private fun processCrudApiDef(entityCrudApiDef: EntityCrudApiDef) {

        entityCrudApiDef.createApiDef?.let { createApiDef ->
            createApiDef.manyToManyTimestampedJoinRequestDtoDefs.forEach { renderRequestDto(it) }
            renderRequestDto(createApiDef.requestDtoDef)
        }
```

- [ ] **Step 3: Fix `TypescriptInterfaceDtoRenderer` for `RequestDtoFieldType` in list element position**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt`, find the `collectionElementFieldType` function and fix the `RequestDtoFieldType` case:

Find:
```kotlin
                is RequestDtoFieldType -> TODO()
```

Replace with:
```kotlin
                is RequestDtoFieldType -> parameterFieldType.requestDtoDef.uqcn.value
```

- [ ] **Step 4: Render join DTO TypeScript interfaces in `AngularUiModuleGenerator`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`, modify `renderRequestDtos()` to also render the join DTO TypeScript interfaces:

Find:
```kotlin
    private fun renderRequestDtos() {

        this.modelDef.requestDtoDefs.forEach { requestDtoDef ->

            renderTypescriptInterface(
                renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath,
                className = requestDtoDef.classDef.uqcn,
                fields = requestDtoDef.classDef.allFieldsSorted,
                dtoCharacteristics = emptySet()
            )

        }

    }
```

Replace with:
```kotlin
    private fun renderRequestDtos() {

        this.modelDef.requestDtoDefs.forEach { requestDtoDef ->

            renderTypescriptInterface(
                renderedFilePath = requestDtoDef.typescriptDtoRenderedFilePath,
                className = requestDtoDef.classDef.uqcn,
                fields = requestDtoDef.classDef.allFieldsSorted,
                dtoCharacteristics = emptySet()
            )

        }

        this.modelDef.entityCrudApiDefs
            .mapNotNull { it.createApiDef }
            .flatMap { it.manyToManyTimestampedJoinRequestDtoDefs }
            .forEach { joinDtoDef ->
                renderTypescriptInterface(
                    renderedFilePath = joinDtoDef.typescriptDtoRenderedFilePath,
                    className = joinDtoDef.uqcn,
                    fields = joinDtoDef.classFieldDefs,
                    dtoCharacteristics = emptySet()
                )
            }

    }
```

- [ ] **Step 5: Verify generator compiles**

```bash
./gradlew :maia-gen:maia-gen-generator:compileKotlin :maia-gen:maia-gen-spec:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Regenerate showcase and verify output**

```bash
./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:app:maiaGeneration
```

Then verify:
```bash
# Should contain leftEntities, not leftEntityIds
grep "leftEntities\|leftEntityIds" maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCreateRequestDto.kt

# Should exist with correct fields
cat maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/LeftJoinRequestDto.kt

# TypeScript
grep "leftEntities\|leftEntityIds" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/RightManyCreateRequestDto.ts
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/LeftJoinRequestDto.ts
```

Expected content of `LeftJoinRequestDto.kt`:
```kotlin
class LeftJoinRequestDto
@JsonCreator constructor(
    @param:JsonProperty("leftEntityId", ...) val leftEntityId_raw: DomainId?,
    @param:JsonProperty("effectiveFrom", ...) val effectiveFrom: Instant?,
    @param:JsonProperty("effectiveTo", ...) val effectiveTo: Instant?
) {
    @get:JsonIgnore
    val leftEntityId: DomainId by lazy { leftEntityId_raw!! }
    ...
}
```

Expected content of `LeftJoinRequestDto.ts`:
```typescript
export interface LeftJoinRequestDto {
    effectiveFrom?: string;
    effectiveTo?: string;
    leftEntityId: string;
}
```

Expected `RightManyCreateRequestDto.ts`:
```typescript
export interface RightManyCreateRequestDto {
    leftEntities?: ReadonlyArray<LeftJoinRequestDto>;
    someInt: number;
    someString: string;
}
```

- [ ] **Step 7: Verify domain module compiles with regenerated code**

```bash
./gradlew :maia-showcase:domain:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityCreateApiDef.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt
git add maia-showcase/domain/src/generated/ maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: generate LeftJoinRequestDto for timestamped manyToMany associations"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EntityCreateApiDef.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/DomainModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/TypescriptInterfaceDtoRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:domain:maiaGeneration :maia-showcase:app:maiaGeneration && ./gradlew :maia-showcase:domain:compileKotlin", "acceptanceCriteria": ["LeftJoinRequestDto.kt generated", "RightManyCreateRequestDto has leftEntities field", "LeftJoinRequestDto.ts generated", "RightManyCreateRequestDto.ts has leftEntities field", "Non-timestamped associations unchanged"], "requiresUserVerification": false}
```

---

### Task 3: `CrudServiceRenderer` — use provided timestamps

**Goal:** The generated `RightManyCrudService.create()` iterates over `leftEntities` and passes `joinDto.effectiveFrom` / `joinDto.effectiveTo` to `LeftToRightManyToManyJoinEntity.newInstance()`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt`

**Acceptance Criteria:**
- [ ] `RightManyCrudService.kt` iterates over `createDto.leftEntities` (not `leftEntityIds`)
- [ ] `newInstance(effectiveFrom = joinDto.effectiveFrom, effectiveTo = joinDto.effectiveTo, ...)` — uses DTO values, not `Instant.now()`
- [ ] `Instant` import removed from this code path (no longer needed)
- [ ] Non-timestamped code path unchanged

**Verify:**
```bash
./gradlew :maia-showcase:service:maiaGeneration
grep -A 10 "leftEntities\|leftEntityIds" maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt
```

**Steps:**

- [ ] **Step 1: Modify the create block in `CrudServiceRenderer`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt`, find the block (around line 167) that generates the effective timestamp join insertion:

Find:
```kotlin
                if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
                    addImportFor<Instant>()
                    appendLine("        createDto.${otherSideDtoFieldName}.forEach { $otherSideFieldName ->")
                    appendLine("            this.${joinRepoFieldName}.insert(")
                    appendLine("                ${joinEntityClass}.newInstance(")
                    appendLine("                    effectiveFrom = Instant.now(),")
                    appendLine("                    effectiveTo = null,")
                    appendLine("                    $thisSideEntityIdFieldName = entity.id,")
                    appendLine("                    $otherSideFieldName = $otherSideFieldName")
                    appendLine("                )")
                    appendLine("            )")
                    appendLine("        }")
```

Replace with:
```kotlin
                if (manyToManyEntityDef.entityDef.hasEffectiveTimestamps.value) {
                    val joinDtoFieldName = "${otherSideFieldName}Entities"
                    appendLine("        createDto.${joinDtoFieldName}.forEach { joinDto ->")
                    appendLine("            this.${joinRepoFieldName}.insert(")
                    appendLine("                ${joinEntityClass}.newInstance(")
                    appendLine("                    effectiveFrom = joinDto.effectiveFrom,")
                    appendLine("                    effectiveTo = joinDto.effectiveTo,")
                    appendLine("                    $thisSideEntityIdFieldName = entity.id,")
                    appendLine("                    $otherSideFieldName = joinDto.${otherSideFieldName}EntityId")
                    appendLine("                )")
                    appendLine("            )")
                    appendLine("        }")
```

Note: also remove the `addImportFor<Instant>()` line from this block. The `Instant` import may still appear in other parts of the generated file; only remove it from this specific timestamped path.

- [ ] **Step 2: Regenerate and verify**

```bash
./gradlew :maia-showcase:service:maiaGeneration
grep -A 12 "forEach" maia-showcase/service/src/generated/kotlin/main/org/maiaframework/showcase/many_to_many/RightManyCrudService.kt
```

Expected output around the create method:
```kotlin
createDto.leftEntities.forEach { joinDto ->
    this.leftToRightManyToManyJoinRepo.insert(
        LeftToRightManyToManyJoinEntity.newInstance(
            effectiveFrom = joinDto.effectiveFrom,
            effectiveTo = joinDto.effectiveTo,
            right = entity.id,
            left = joinDto.leftEntityId
        )
    )
}
```

- [ ] **Step 3: Verify service module compiles**

```bash
./gradlew :maia-showcase:service:compileKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt
git add maia-showcase/service/src/generated/
git commit -m "feat: use provided effective timestamps when creating manyToMany join records"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/CrudServiceRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:service:maiaGeneration && ./gradlew :maia-showcase:service:compileKotlin", "acceptanceCriteria": ["RightManyCrudService iterates leftEntities", "Uses joinDto.effectiveFrom and joinDto.effectiveTo", "Non-timestamped path unchanged"], "requiresUserVerification": false}
```

---

### Task 4: Angular component — split chip/timestamped + component state/methods

**Goal:** `manyToManyChipFieldsFor` excludes timestamped associations; `manyToManyTimestampedFieldsFor` produces `ManyToManyTimestampedFieldDef` instances. The `AngularReactiveFormComponentRenderer` generates join-entry state fields, typeahead wiring, and confirm/cancel/remove methods. The HTML renderers accept `timestampedFields`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCreateReactiveFormHtmlRenderer.kt`

**Acceptance Criteria:**
- [ ] Generator compiles
- [ ] `right-many-entity-create-form.ts` contains `leftJoins`, `showLeftJoinForm`, `addLeftJoinEntityControl`, `addLeftJoinEffectiveFromControl`, `addLeftJoinEffectiveToControl`
- [ ] Component has `confirmAddLeftJoin()`, `cancelAddLeftJoin()`, `removeLeftJoin()` methods
- [ ] Component does NOT have `selectedLeftEntities` or `leftEntitySearchControl` (chip pattern removed for this entity)
- [ ] `onSubmit` maps `leftJoins` to `leftEntities` in the request DTO

**Verify:**
```bash
./gradlew :maia-showcase:app:maiaGeneration
grep -n "leftJoins\|showLeftJoinForm\|confirmAddLeftJoin\|leftEntities" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.ts
```

**Steps:**

- [ ] **Step 1: Split `manyToManyChipFieldsFor` in `AngularUiModuleGenerator`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt`:

Add the import at the top of the file imports section:
```kotlin
import org.maiaframework.gen.renderers.ui.ManyToManyTimestampedFieldDef
```

Find the existing function:
```kotlin
    // TODO MTM: This should live in the model somewhere, not in the generator
    private fun manyToManyChipFieldsFor(
        entityDef: EntityDef,
        associations: List<ManyToManyEntityDef>
    ): List<ManyToManyChipFieldDef> {

        return associations.mapNotNull { m2m ->
            val otherSide = m2m.otherSideFrom(entityDef)
            val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
            ManyToManyChipFieldDef(entityDef, m2m, typeaheadDef)
        }

    }
```

Replace with two functions:
```kotlin
    // TODO MTM: This should live in the model somewhere, not in the generator
    private fun manyToManyChipFieldsFor(
        entityDef: EntityDef,
        associations: List<ManyToManyEntityDef>
    ): List<ManyToManyChipFieldDef> {

        return associations
            .filter { !it.entityDef.hasEffectiveTimestamps.value }
            .mapNotNull { m2m ->
                val otherSide = m2m.otherSideFrom(entityDef)
                val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
                ManyToManyChipFieldDef(entityDef, m2m, typeaheadDef)
            }

    }


    private fun manyToManyTimestampedFieldsFor(
        entityDef: EntityDef,
        associations: List<ManyToManyEntityDef>
    ): List<ManyToManyTimestampedFieldDef> {

        val createApiDef = entityDef.entityCrudApiDef?.createApiDef ?: return emptyList()
        return associations
            .filter { it.entityDef.hasEffectiveTimestamps.value }
            .mapNotNull { m2m ->
                val otherSide = m2m.otherSideFrom(entityDef)
                val typeaheadDef = typeaheadByEntityDef[otherSide.entityDef] ?: return@mapNotNull null
                val joinDtoDef = createApiDef.timestampedJoinRequestDtosByAssociation[m2m] ?: return@mapNotNull null
                ManyToManyTimestampedFieldDef(entityDef, m2m, typeaheadDef, joinDtoDef)
            }

    }
```

- [ ] **Step 2: Thread `timestampedFields` through call sites in `AngularUiModuleGenerator`**

For the create-page call site (around line 423), add `timestampedFields` computation and pass it to renderers. The pattern is: wherever `chipFields` is computed and passed, also compute and pass `timestampedFields`.

Find this block (create-page section):
```kotlin
            val chipFields = manyToManyChipFieldsFor(entityCreatePageDef.entityDef, entityCreatePageDef.entityDef.manyToManyAssociations)
```

Add after it:
```kotlin
            val timestampedFields = manyToManyTimestampedFieldsFor(entityCreatePageDef.entityDef, entityCreatePageDef.entityDef.manyToManyAssociations)
```

Then wherever `chipFields` is passed to `AngularReactiveFormComponentRenderer` in this block, also pass `timestampedFields`. The `AngularReactiveFormComponentRenderer` constructor and `EntityCreateReactiveFormHtmlRenderer` will be updated in steps 3–5 below to accept this new parameter.

Do the same for the edit-page call site (around line 478) and dialog call sites. Pass `emptyList<ManyToManyTimestampedFieldDef>()` for edit/dialog call sites for now (edit form is out of scope).

- [ ] **Step 3: Update `EntityCreateReactiveFormHtmlRenderer` to accept `timestampedFields`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCreateReactiveFormHtmlRenderer.kt`:

Find:
```kotlin
class EntityCreateReactiveFormHtmlRenderer(
    private val entityCreatePageDef: EntityCreatePageDef,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
```

Replace with:
```kotlin
class EntityCreateReactiveFormHtmlRenderer(
    private val entityCreatePageDef: EntityCreatePageDef,
    override val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    override val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractCrudReactiveFormHtmlRenderer(
```

- [ ] **Step 4: Update `AbstractCrudReactiveFormHtmlRenderer` to accept `timestampedFields`**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`:

Add the import:
```kotlin
import org.maiaframework.gen.renderers.ui.ManyToManyTimestampedFieldDef
```

Add `timestampedFields` alongside `chipFields`:
```kotlin
abstract class AbstractCrudReactiveFormHtmlRenderer(
    protected val entityDef: EntityDef,
    private val inlineFormOrDialog: InlineFormOrDialog,
    protected open val chipFields: List<ManyToManyChipFieldDef> = emptyList(),
    protected open val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractSourceFileRenderer() {
```

Add a no-op `renderManyToManyTimestampedFields()` call in `renderSource()` after `renderManyToManyChipFields()`:
```kotlin
        renderManyToManyChipFields()
        renderManyToManyTimestampedFields()
```

Add the stub method (will be implemented fully in Task 5):
```kotlin
    protected open fun renderManyToManyTimestampedFields() {
        // implemented in Task 5
    }
```

- [ ] **Step 5: Update `AngularReactiveFormComponentRenderer` to accept `timestampedFields` and generate state/methods**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`:

Add the `timestampedFields` constructor parameter:
```kotlin
class AngularReactiveFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    formAngularComponentNames: AngularComponentNames,
    providerServices: List<String>,
    private val chipFields: List<ManyToManyChipFieldDef>,
    private val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractAngularComponentRenderer(
```

In `render class fields`, add a call after `render class fields for chip fields`:
```kotlin
        `render class fields for timestamped fields`()
```

Add the new render method:
```kotlin
    private fun `render class fields for timestamped fields`() {

        timestampedFields.forEach { field ->

            addImport(field.esDocImport)
            addImport(field.serviceImport)

            append("""
                |
                |
                |    ${field.joinsFieldName}: {
                |        entityId: string;
                |        entityName: string;
                |        effectiveFrom: Date | null;
                |        effectiveTo: Date | null;
                |    }[] = [];
                |
                |
                |    ${field.showFormSignalName} = signal(false);
                |
                |
                |    ${field.addEntityControlName} = new FormControl<${field.esDocClassName} | null>(null);
                |
                |
                |    ${field.effectiveFromControlName} = new FormControl<Date | null>(null);
                |
                |
                |    ${field.effectiveToControlName} = new FormControl<Date | null>(null);
                |
                |
                |    ${field.filteredFieldName}: ${field.esDocClassName}[] = [];
                |
                |
                |    ${field.filteredIsLoadingFieldName} = signal(false);
                |
                |
                |    ${field.serviceFieldName} = inject(${field.serviceClassName});
                |""".trimMargin())

        }

    }
```

In `render function ngOnInit`, after the existing chip field wiring, add wiring for timestamped fields:
```kotlin
        timestampedFields.forEach { field ->

            addImport("rxjs", "of")
            addImport("rxjs/operators", "catchError")
            addImport("rxjs/operators", "debounceTime")
            addImport("rxjs/operators", "distinctUntilChanged")
            addImport("rxjs/operators", "filter")
            addImport("rxjs/operators", "switchMap")
            addImport("rxjs/operators", "tap")

            append("""
                |
                |        this.${field.addEntityControlName}.valueChanges.pipe(
                |            debounceTime(300),
                |            distinctUntilChanged(),
                |            filter(value => typeof value === 'string'),
                |            tap(() => {
                |                this.${field.filteredFieldName} = [];
                |                this.${field.filteredIsLoadingFieldName}.set(true);
                |            }),
                |            switchMap(value => this.${field.serviceFieldName}.search(value ?? '').pipe(
                |                catchError(err => {
                |                    this.${field.filteredIsLoadingFieldName}.set(false);
                |                    console.error(err);
                |                    return of([]);
                |                })
                |            )),
                |            tap(() => this.${field.filteredIsLoadingFieldName}.set(false))
                |        ).subscribe(res => {
                |            this.${field.filteredFieldName} = res;
                |        });
                |""".trimMargin())

        }
```

Add a new method renderer called from `render chip entity methods` or separately in `renderComponentSource`:
```kotlin
        `render timestamped join methods`()
```

Implement `render timestamped join methods`:
```kotlin
    private fun `render timestamped join methods`() {

        timestampedFields.forEach { field ->

            append("""
                |
                |
                |    ${field.confirmMethodName}(): void {
                |
                |        const entity = this.${field.addEntityControlName}.value;
                |        if (!entity) return;
                |        if (this.${field.joinsFieldName}.some(j => j.entityId === entity.${field.esDocIdFieldName})) return;
                |        this.${field.joinsFieldName}.push({
                |            entityId: entity.${field.esDocIdFieldName},
                |            entityName: entity.${field.searchTermFieldName},
                |            effectiveFrom: this.${field.effectiveFromControlName}.value,
                |            effectiveTo: this.${field.effectiveToControlName}.value,
                |        });
                |        this.${field.addEntityControlName}.reset();
                |        this.${field.effectiveFromControlName}.reset();
                |        this.${field.effectiveToControlName}.reset();
                |        this.${field.filteredFieldName} = [];
                |        this.${field.showFormSignalName}.set(false);
                |
                |    }
                |
                |
                |    ${field.removeMethodName}(index: number): void {
                |
                |        this.${field.joinsFieldName}.splice(index, 1);
                |
                |    }
                |
                |
                |    ${field.cancelMethodName}(): void {
                |
                |        this.${field.addEntityControlName}.reset();
                |        this.${field.effectiveFromControlName}.reset();
                |        this.${field.effectiveToControlName}.reset();
                |        this.${field.filteredFieldName} = [];
                |        this.${field.showFormSignalName}.set(false);
                |
                |    }
                |""".trimMargin())

        }

    }
```

In `render requestDto construction`, add timestamped field mapping before the closing `} as ...` line:
```kotlin
            timestampedFields.forEach { field ->
                appendLine("            ${field.requestDtoFieldName}: this.${field.joinsFieldName}.map(j => ({")
                appendLine("                ${field.fieldName}EntityId: j.entityId,")
                appendLine("                effectiveFrom: j.effectiveFrom?.toISOString() ?? null,")
                appendLine("                effectiveTo: j.effectiveTo?.toISOString() ?? null,")
                appendLine("            })),")
            }
```

Also update `add imports` to conditionally add Material datepicker/timepicker when `timestampedFields` is non-empty:
```kotlin
        if (timestampedFields.isNotEmpty()) {
            `add imports for date and time pickers`()
        }
```

- [ ] **Step 6: Regenerate showcase and verify**

```bash
./gradlew :maia-showcase:app:maiaGeneration
grep -n "leftJoins\|showLeftJoinForm\|confirmAddLeftJoin\|leftEntities\|effectiveFrom" \
  maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.ts
```

Expected: all five names appear in the file.

Also verify no regression for left-many form (does NOT use timestamped pattern):
```bash
grep "selectedLeftEntities\|leftEntitySearchControl" \
  maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/left-many-entity-create-form.ts
```
Expected: still contains chip pattern (left entity create form does not have timestamped join).

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCreateReactiveFormHtmlRenderer.kt
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: generate join-entry state and methods for timestamped manyToMany fields"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/generator/AngularUiModuleGenerator.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/EntityCreateReactiveFormHtmlRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:app:maiaGeneration", "acceptanceCriteria": ["right-many-entity-create-form.ts has leftJoins, showLeftJoinForm, confirmAddLeftJoin", "onSubmit maps leftJoins to leftEntities", "chip pattern not regressed for non-timestamped entities"], "requiresUserVerification": false}
```

---

### Task 5: HTML template — card list + inline mini form

**Goal:** `AbstractCrudReactiveFormHtmlRenderer.renderManyToManyTimestampedFields()` generates a join-entry list, an "Add" button, and an inline mini form with entity typeahead and date/time pickers.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`

**Acceptance Criteria:**
- [ ] `right-many-entity-create-form.html` contains the join-entry `@for` loop, the "Add" button, and the `@if (showLeftJoinForm())` block
- [ ] Effective From and Effective To each have a datepicker + timepicker pair
- [ ] Entity typeahead uses standalone `[formControl]` (not `formControlName`)
- [ ] Chip `mat-form-field` block is absent for `RightMany` (replaced by new pattern)

**Verify:**
```bash
./gradlew :maia-showcase:app:maiaGeneration
grep -n "showLeftJoinForm\|join-mini-form\|effectiveFromPicker\|confirmAddLeftJoin" \
  maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.html
```

**Steps:**

- [ ] **Step 1: Replace the stub `renderManyToManyTimestampedFields()` with the full implementation**

In `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`, replace the stub:

```kotlin
    protected open fun renderManyToManyTimestampedFields() {

        timestampedFields.forEach { field ->

            append("""
                |        <div class="join-entries">
                |            @for (join of ${field.joinsFieldName}; track join.entityId) {
                |                <div class="join-entry">
                |                    <span>{{ join.entityName }}</span>
                |                    <span>{{ join.effectiveFrom | date:'medium' }}</span>
                |                    <span>{{ join.effectiveTo | date:'medium' }}</span>
                |                    <button mat-icon-button type="button" (click)="${field.removeMethodName}(${'$'}index)">
                |                        <mat-icon>delete</mat-icon>
                |                    </button>
                |                </div>
                |            }
                |        </div>
                |        <button mat-stroked-button type="button" (click)="${field.showFormSignalName}.set(true)">
                |            <mat-icon>add</mat-icon> Add ${field.labelText}
                |        </button>
                |        @if (${field.showFormSignalName}()) {
                |            <div class="join-mini-form">
                |                <mat-form-field appearance="outline">
                |                    <mat-label>${field.labelText}</mat-label>
                |                    <input
                |                        [formControl]="${field.addEntityControlName}"
                |                        [matAutocomplete]="${field.autocompleteRefName}"
                |                        placeholder="${field.searchPlaceholder}"
                |                    />
                |                    <mat-autocomplete #${field.autocompleteRefName}="matAutocomplete">
                |                        @if (${field.filteredIsLoadingFieldName}()) {
                |                            <mat-option disabled>Loading...</mat-option>
                |                        }
                |                        @for (option of ${field.filteredFieldName}; track option.${field.esDocIdFieldName}) {
                |                            <mat-option [value]="option">{{ option.${field.searchTermFieldName} }}</mat-option>
                |                        }
                |                    </mat-autocomplete>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveFromPicker${field.fieldName}"
                |                        [formControl]="${field.effectiveFromControlName}" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveFromPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveFromPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective From Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveFromTimepicker${field.fieldName}"
                |                        [formControl]="${field.effectiveFromControlName}" />
                |                    <mat-timepicker #effectiveFromTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveFromTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Date</mat-label>
                |                    <input matInput [matDatepicker]="effectiveToPicker${field.fieldName}"
                |                        [formControl]="${field.effectiveToControlName}" />
                |                    <mat-datepicker-toggle matIconSuffix [for]="effectiveToPicker${field.fieldName}"></mat-datepicker-toggle>
                |                    <mat-datepicker #effectiveToPicker${field.fieldName}></mat-datepicker>
                |                </mat-form-field>
                |                <mat-form-field appearance="outline">
                |                    <mat-label>Effective To Time</mat-label>
                |                    <input matInput [matTimepicker]="effectiveToTimepicker${field.fieldName}"
                |                        [formControl]="${field.effectiveToControlName}" />
                |                    <mat-timepicker #effectiveToTimepicker${field.fieldName}></mat-timepicker>
                |                    <mat-timepicker-toggle matSuffix [for]="effectiveToTimepicker${field.fieldName}"></mat-timepicker-toggle>
                |                </mat-form-field>
                |                <button mat-flat-button type="button" (click)="${field.confirmMethodName}()">Add</button>
                |                <button mat-flat-button type="button" (click)="${field.cancelMethodName}()">Cancel</button>
                |            </div>
                |        }
                |""".trimMargin())

        }

    }
```

The `${field.fieldName}` suffix on picker ref names (`effectiveFromPicker${field.fieldName}`) ensures no name collisions if a form has multiple timestamped associations.

- [ ] **Step 2: Regenerate and verify**

```bash
./gradlew :maia-showcase:app:maiaGeneration
cat maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/many-to-many/right-many-entity-create-form.html
```

Verify the file contains:
- `@for (join of leftJoins; track join.entityId)` 
- `showLeftJoinForm.set(true)`
- `@if (showLeftJoinForm())`
- `[formControl]="addLeftJoinEntityControl"`
- `effectiveFromPickerleft`
- `confirmAddLeftJoin()`
- No `mat-chip-grid` block

- [ ] **Step 3: Angular build check**

```bash
cd maia-showcase/maia-showcase-ui && npx ng build --configuration=development 2>&1 | tail -20
```
Expected: compiled successfully (0 errors)

- [ ] **Step 4: Commit**

```bash
cd /home/kevin/dev/code/maia
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: generate card list and inline mini form for timestamped manyToMany fields"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt"], "verifyCommand": "./gradlew :maia-showcase:app:maiaGeneration && cd maia-showcase/maia-showcase-ui && npx ng build --configuration=development", "acceptanceCriteria": ["right-many-entity-create-form.html has join list loop", "showLeftJoinForm signal controls inline form visibility", "datepicker+timepicker pairs for effectiveFrom and effectiveTo", "confirmAddLeftJoin and cancelAddLeftJoin wired", "Angular build succeeds"], "requiresUserVerification": false}
```

---

### Task 6: Update Playwright test — new create-form interaction

**Goal:** `RightManyCreatePage` exposes the new three-step "Add entity" interaction; the existing CRUD journey test passes end-to-end.

**Files:**
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt`
- Modify: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt`

**Acceptance Criteria:**
- [ ] `searchAndSelectLeftEntity()` is removed
- [ ] Three new methods exist: `clickAddLeftEntityButton()`, `searchAndSelectLeftEntityInMiniForm()`, `clickConfirmAddInMiniForm()`
- [ ] `RightManyCrudPlaywrightTest.crud journey` passes

**Verify:**
```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest.crud journey"
```
Expected: BUILD SUCCESSFUL, 1 test passed

**Steps:**

- [ ] **Step 1: Update `RightManyCreatePage`**

Replace the entire `searchAndSelectLeftEntity` method with three new methods:

```kotlin
    fun clickAddLeftEntityButton() {

        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add Left Entities"))
            .click()

    }


    fun searchAndSelectLeftEntityInMiniForm(searchTerm: String) {

        page.locator("input[placeholder='Search Left Entities...']").fill(searchTerm)
        val option = page.locator("mat-option").filter(Locator.FilterOptions().setHasText(searchTerm))
        option.waitFor()
        option.evaluate("el => el.click()")

    }


    fun clickConfirmAddInMiniForm() {

        page.locator(".join-mini-form button").filter(Locator.FilterOptions().setHasText("Add"))
            .click()

    }
```

- [ ] **Step 2: Update `RightManyCrudPlaywrightTest`**

Find:
```kotlin
            searchAndSelectLeftEntity("left-alpha")
```

Replace with:
```kotlin
            clickAddLeftEntityButton()
            searchAndSelectLeftEntityInMiniForm("left-alpha")
            clickConfirmAddInMiniForm()
```

- [ ] **Step 3: Run the test**

```bash
./gradlew :maia-showcase:app:test --tests "org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest"
```
Expected: BUILD SUCCESSFUL

If the test fails because a locator doesn't match (button text, CSS selector, etc.), inspect the generated HTML to find the correct selector and adjust the page object methods accordingly.

- [ ] **Step 4: Commit**

```bash
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt
git add maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt
git commit -m "test: update RightMany create page for new inline mini form interaction"
```

```json:metadata
{"files": ["maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/RightManyCreatePage.kt", "maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/RightManyCrudPlaywrightTest.kt"], "verifyCommand": "./gradlew :maia-showcase:app:test --tests \"org.maiaframework.showcase.many_to_many.RightManyCrudPlaywrightTest\"", "acceptanceCriteria": ["searchAndSelectLeftEntity removed", "Three new interaction methods present", "CRUD journey test passes"], "requiresUserVerification": false}
```

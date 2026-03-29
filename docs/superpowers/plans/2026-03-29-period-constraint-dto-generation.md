# Period Constraint DTO Generation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the code generator automatically emit `@field:PeriodConstraint` (and `@field:NotBlank` for non-nullable) on Period fields in generated RequestDtos, storing the raw value as `String?` with a `Period.parse()` getter.

**Architecture:** Three-layer change: (1) add the `VALIDATOR_CONSTRAINT_PERIOD` Fqcn constant and `PeriodConstraintDef` to the spec module, (2) hook it into `ClassFieldDef.enrichWithImplicitValidationConstraints()` via an `else if` branch for `PeriodFieldType`, (3) update `RequestDtoRenderer` to store Period fields as `String?` (instead of `Period?`) and render `Period.parse()` getters.

**Tech Stack:** Kotlin, Gradle (`:maia-gen:maia-gen-spec`, `:maia-gen:maia-gen-generator`, `:maia-showcase:domain`)

---

## File Map

| Action | File |
|--------|------|
| Modify | `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt` |
| Create | `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/validation/PeriodConstraintDef.kt` |
| Modify | `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt` |
| Modify | `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RequestDtoRenderer.kt` |
| Re-generate | `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/all_field_types/AllFieldTypesCreateRequestDto.kt` |
| Re-generate | `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/all_field_types/AllFieldTypesUpdate_somePeriodModifiableRequestDto.kt` |
| Re-generate | `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/all_field_types/AllFieldTypesUpdateRequestDto.kt` |

---

### Task 1: Add `VALIDATOR_CONSTRAINT_PERIOD` to `Fqcns.kt`

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt`

- [ ] **Step 1: Add the constant**

Open `Fqcns.kt`. After the line:
```kotlin
val VALIDATOR_CONSTRAINT_NOT_NULL = Fqcn.valueOf("jakarta.validation.constraints.NotNull")
```
Add:
```kotlin
val VALIDATOR_CONSTRAINT_PERIOD = Fqcn.valueOf("org.maiaframework.common.validation.PeriodConstraint")
```

- [ ] **Step 2: Build the spec module to verify compilation**

```bash
./gradlew :maia-gen:maia-gen-spec:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/Fqcns.kt
git commit -m "feat: add VALIDATOR_CONSTRAINT_PERIOD to Fqcns"
```

---

### Task 2: Create `PeriodConstraintDef`

**Files:**
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/validation/PeriodConstraintDef.kt`

- [ ] **Step 1: Create the file**

```kotlin
package org.maiaframework.gen.spec.definition.validation

import org.maiaframework.gen.spec.definition.Fqcns
import org.maiaframework.gen.spec.definition.lang.AnnotationDef


class PeriodConstraintDef private constructor() : AbstractValidationConstraintDef(
    AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_PERIOD)
) {


    companion object {

        val INSTANCE = PeriodConstraintDef()

    }


}
```

Compare to `EnumConstraintDef.kt` and `NotBlankConstraintDef.kt` in the same package for reference on the pattern.

- [ ] **Step 2: Build the spec module**

```bash
./gradlew :maia-gen:maia-gen-spec:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/validation/PeriodConstraintDef.kt
git commit -m "feat: add PeriodConstraintDef"
```

---

### Task 3: Hook `PeriodConstraintDef` into `ClassFieldDef`

**Files:**
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt`

The `enrichWithImplicitValidationConstraints` method currently looks like this (lines ~134–157):

```kotlin
private fun enrichWithImplicitValidationConstraints(
    providedValidationConstraints: SortedSet<AbstractValidationConstraintDef>
): SortedSet<AbstractValidationConstraintDef> {

    val enrichedConstraints = providedValidationConstraints.toMutableSet()

    if (this.fieldType is EnumFieldType) {
        enrichedConstraints.add(EnumConstraintDef(fieldType.fqcn))
    }

    if (this.nullable == false) {

        if (this.fieldType is DomainIdFieldType) {
            enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
        } else if (this.fieldType.isStringBased()) {
            enrichedConstraints.add(NotBlankConstraintDef.INSTANCE)
        } else {
            enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
        }

    }

    return enrichedConstraints.toSortedSet()

}
```

- [ ] **Step 1: Add imports for `PeriodConstraintDef` and `PeriodFieldType`**

`PeriodFieldType` is already imported (it's in the same `lang` package). Add `PeriodConstraintDef` import alongside the other constraint def imports near the top of the file:

```kotlin
import org.maiaframework.gen.spec.definition.validation.PeriodConstraintDef
```

- [ ] **Step 2: Restructure the non-nullable block with an `else if` for Period**

Replace the body of `enrichWithImplicitValidationConstraints` so it reads:

```kotlin
private fun enrichWithImplicitValidationConstraints(
    providedValidationConstraints: SortedSet<AbstractValidationConstraintDef>
): SortedSet<AbstractValidationConstraintDef> {

    val enrichedConstraints = providedValidationConstraints.toMutableSet()

    if (this.fieldType is EnumFieldType) {
        enrichedConstraints.add(EnumConstraintDef(fieldType.fqcn))
    }

    if (this.fieldType is PeriodFieldType) {
        enrichedConstraints.add(PeriodConstraintDef.INSTANCE)
        if (this.nullable == false) {
            enrichedConstraints.add(NotBlankConstraintDef.INSTANCE)
        }
    } else if (this.nullable == false) {

        if (this.fieldType is DomainIdFieldType) {
            enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
        } else if (this.fieldType.isStringBased()) {
            enrichedConstraints.add(NotBlankConstraintDef.INSTANCE)
        } else {
            enrichedConstraints.add(NotNullConstraintDef.INSTANCE)
        }

    }

    return enrichedConstraints.toSortedSet()

}
```

The `else if` ensures Period fields do not also receive `NotNullConstraintDef` from the general non-nullable path.

- [ ] **Step 3: Build the spec module**

```bash
./gradlew :maia-gen:maia-gen-spec:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt
git commit -m "feat: add PeriodConstraint and NotBlank as implicit constraints for Period fields"
```

---

### Task 4: Update `RequestDtoRenderer` for Period fields

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RequestDtoRenderer.kt`

The goal is to make Period fields behave like Enum fields in the DTO: stored as `String?` (raw), with `private` visibility and `_raw` suffix even when nullable, and a `Period.parse()` getter for both nullable and non-nullable fields.

- [ ] **Step 1: Patch `renderConstructorArgs` to handle Period**

Find the `renderConstructorArgs` method. Make two targeted edits:

**Edit A** — replace the `visibility` line:
```kotlin
// Before:
val visibility = if (fieldIsNullable) "" else "private "

// After:
val isPeriod = classField.fieldType is PeriodFieldType
val visibility = if (fieldIsNullable && !isPeriod) "" else "private "
```

**Edit B** — replace the `constructorArgName` / `unwrappedFieldType` / `addImportFor` / `appendLine` block:
```kotlin
// Before:
val constructorArgName = if (fieldIsNullable == false || isUrl) "${fieldName}_raw" else fieldName
val unwrappedFieldType = classField.unWrapIfComplexType()
addImportFor(unwrappedFieldType.fieldType)

appendLine("    $visibility$variableType$constructorArgName: ${unwrappedFieldType.convertToNullable().unqualifiedToString}$commaOrNot")

// After:
val constructorArgName = if (fieldIsNullable == false || isUrl || isPeriod) "${fieldName}_raw" else fieldName
val unwrappedFieldType = if (isPeriod) classField.copy(fieldType = FieldTypes.string) else classField.unWrapIfComplexType()
addImportFor(unwrappedFieldType.fieldType)

appendLine("    $visibility$variableType$constructorArgName: ${unwrappedFieldType.convertToNullable().unqualifiedToString}$commaOrNot")
```

`FieldTypes.string` produces a `StringFieldType` whose `unqualifiedToString` is `String` — this makes the raw constructor arg `String?` for Period fields. The `java.time.Period` import is still added via `addImportFor(classField.fieldType)` in the `init` block, which is needed for the getter.

`FieldTypes.string` produces a `StringFieldType` whose `unqualifiedToString` is `String` — this makes the raw constructor arg `String?` for Period fields. The `java.time.Period` import is still added via `addImportFor(classField.fieldType)` in the `init` block, which is needed for the getter.

- [ ] **Step 2: Update the `renderGetters` dispatch for `PeriodFieldType`**

Find this line inside `renderGetters`:

```kotlin
is PeriodFieldType -> renderGetterIfNonNullableField(field)
```

Replace with:

```kotlin
is PeriodFieldType -> renderGetterForPeriod(field)
```

- [ ] **Step 3: Add `renderGetterForPeriod` method**

Add this private method to `RequestDtoRenderer`, after `renderGetterForEnum` (around line 235):

```kotlin
private fun renderGetterForPeriod(fieldDef: ClassFieldDef) {

    addImportFor(Fqcns.JACKSON_JSON_IGNORE)

    blankLine()
    blankLine()
    appendLine("    @get:JsonIgnore")
    appendLine("    val ${fieldDef.classFieldName}")

    if (fieldDef.nullable) {
        appendLine("        get() = ${fieldDef.classFieldName}_raw?.let { Period.parse(it) }")
    } else {
        appendLine("        get() = ${fieldDef.classFieldName}_raw!!.let { Period.parse(it) }")
    }

}
```

`Period` is unqualified here — the import for `java.time.Period` was added in `init` via `addImportFor(classField.fieldType)`.

- [ ] **Step 4: Build the generator module**

```bash
./gradlew :maia-gen:maia-gen-generator:build
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/RequestDtoRenderer.kt
git commit -m "feat: generate Period fields as String with PeriodConstraint in RequestDtos"
```

---

### Task 5: Re-generate showcase DTOs and verify output

**Files:**
- Re-generate: `maia-showcase/domain/src/generated/**`

- [ ] **Step 1: Delete existing generated domain code**

```bash
./gradlew :maia-showcase:domain:clean
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Run code generation for the showcase domain module**

```bash
./gradlew :maia-showcase:domain:maiaGeneration
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify `AllFieldTypesCreateRequestDto` — Period constructor args**

Open `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/all_field_types/AllFieldTypesCreateRequestDto.kt`.

Confirm these constructor parameters are present:

```kotlin
@field:NotBlank
@field:PeriodConstraint
@param:JsonProperty("somePeriodModifiable", access = JsonProperty.Access.READ_WRITE)
private val somePeriodModifiable_raw: String?,
@param:JsonProperty("somePeriodNullable", access = JsonProperty.Access.READ_WRITE)
@field:PeriodConstraint
private val somePeriodNullable_raw: String?,
```

- [ ] **Step 4: Verify `AllFieldTypesCreateRequestDto` — Period getters**

Confirm these getters are present in the class body:

```kotlin
@get:JsonIgnore
val somePeriodModifiable
    get() = somePeriodModifiable_raw!!.let { Period.parse(it) }


@get:JsonIgnore
val somePeriodNullable
    get() = somePeriodNullable_raw?.let { Period.parse(it) }
```

- [ ] **Step 5: Verify `AllFieldTypesUpdate_somePeriodModifiableRequestDto`**

Open `maia-showcase/domain/src/generated/kotlin/main/org/maiaframework/showcase/all_field_types/AllFieldTypesUpdate_somePeriodModifiableRequestDto.kt`.

Confirm the Period parameter now reads:

```kotlin
@field:NotBlank
@field:PeriodConstraint
@param:JsonProperty("somePeriodModifiable", access = JsonProperty.Access.READ_WRITE)
private val somePeriodModifiable_raw: String?,
```

And the getter:
```kotlin
@get:JsonIgnore
val somePeriodModifiable
    get() = somePeriodModifiable_raw!!.let { Period.parse(it) }
```

- [ ] **Step 6: Commit the re-generated files**

```bash
git add maia-showcase/domain/src/generated/
git commit -m "chore: regenerate showcase domain DTOs with PeriodConstraint"
```

---

### Task 6: Full build verification

- [ ] **Step 1: Run the full project build**

```bash
./gradlew build
```
Expected: `BUILD SUCCESSFUL`

If the build fails due to compilation errors in generated code or tests, examine the error output — likely a type mismatch where consumer code passes `Period` directly to a constructor now expecting `String`. Fix by ensuring callers pass `period.toString()` or `Period.parse(str)` as appropriate.

- [ ] **Step 2: Commit if any fixes were needed**

Only commit if Step 1 required changes. Use:
```bash
git add <affected files>
git commit -m "fix: update callers to pass String for Period DTO fields"
```

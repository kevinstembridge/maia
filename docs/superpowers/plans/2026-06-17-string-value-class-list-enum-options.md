# List<StringValueClass> Enum Options Form Field Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow a `List<StringValueClass>` entity field to declare an enum FQCN via `withOptionsEnum(fqcn: String)` in the spec DSL so the generator renders it as a `mat-select multiple` dropdown in Angular forms.

**Architecture:** A new `EnumOptionsDef` wrapper class derives TypeScript identifiers from a raw FQCN. This is stored on `ClassFieldDef` and flows through `AngularFormFieldDef` into the HTML renderer (`MatFormFieldRenderer`) and the TypeScript component renderer (`AngularReactiveFormComponentRenderer`).

**Tech Stack:** Kotlin (spec/generator), Angular (generated output), Angular Material (`mat-select`).

**User Verification:** NO — no user verification required.

---

## File Map

| File | Change |
|------|--------|
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EnumOptionsDef.kt` | **Create** |
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt` | **Modify** — add `enumOptionsDef` param + `isStringValueClassListWithEnumOptions` computed val |
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/ClassFieldDefBuilder.kt` | **Modify** — add `enumOptionsDef` field, pass in `build()` |
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityFieldDefBuilder.kt` | **Modify** — add `withOptionsEnum(fqcn: String)` DSL function |
| `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AngularFormFieldDef.kt` | **Modify** — add `stringValueClassListEnumOptionsDef` computed property |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt` | **Modify** — add branch + private render function |
| `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt` | **Modify** — two touch points for TypeScript import wiring |
| `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt` | **Modify** — add `withOptionsEnum` to `authorities` field |

---

### Task 1: Create `EnumOptionsDef` and update spec data model

**Goal:** Add the new `EnumOptionsDef` type and propagate it through the spec data model so a `List<StringValueClass>` field can carry an enum FQCN.

**Files:**
- Create: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EnumOptionsDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/ClassFieldDefBuilder.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityFieldDefBuilder.kt`
- Modify: `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AngularFormFieldDef.kt`

**Acceptance Criteria:**
- [ ] `EnumOptionsDef` computes `selectOptionsUqcn` and `selectOptionsTypescriptImport` from a raw `Fqcn`
- [ ] `ClassFieldDef.isStringValueClassListWithEnumOptions` is `true` only when the field type is `ListFieldType<StringValueClassFieldType>` and `enumOptionsDef` is non-null
- [ ] `EntityFieldDefBuilder.withOptionsEnum("some.fqcn.ClassName")` compiles and sets `enumOptionsDef` on the builder
- [ ] `./gradlew :maia-gen:maia-gen-spec:build` passes

**Verify:** `./gradlew :maia-gen:maia-gen-spec:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Create `EnumOptionsDef.kt`**

```kotlin
package org.maiaframework.gen.spec.definition

import org.maiaframework.gen.spec.definition.lang.Fqcn
import org.maiaframework.gen.spec.definition.lang.TypescriptImport
import org.maiaframework.gen.spec.definition.lang.Uqcn

class EnumOptionsDef(val fqcn: Fqcn) {
    private val genComponentsBaseDir = GeneratedTypescriptDir.forPackage(fqcn.packageName)
    val uqcn: Uqcn = fqcn.uqcn
    val selectOptionsUqcn = uqcn.withSuffix("SelectOptions")
    val selectOptionsTypescriptImport = TypescriptImport(
        selectOptionsUqcn.value,
        "@$genComponentsBaseDir/$selectOptionsUqcn"
    )
}
```

- [ ] **Step 2: Add `enumOptionsDef` to `ClassFieldDef`**

In `ClassFieldDef.kt`, add a new constructor parameter after `typeaheadDef`:

```kotlin
val enumOptionsDef: EnumOptionsDef? = null,
```

Add the import at the top of the file:

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

Add the computed val in the class body (after the existing `isEnumList` computed val):

```kotlin
val isStringValueClassListWithEnumOptions: Boolean =
    fieldType is ListFieldType
        && (fieldType as ListFieldType).parameterFieldType is StringValueClassFieldType
        && enumOptionsDef != null
```

- [ ] **Step 3: Add `enumOptionsDef` to `ClassFieldDefBuilder`**

In `ClassFieldDefBuilder.kt`, add the import and a new field:

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

```kotlin
var enumOptionsDef: EnumOptionsDef? = null
```

In the `build()` function, add `enumOptionsDef = enumOptionsDef` to the `ClassFieldDef(...)` call. The constructor call already uses named params so add it alongside the other optional params:

```kotlin
return ClassFieldDef(
    annotationDefs = annotationDefs,
    classFieldName = classFieldName,
    description = description,
    displayName = fieldDisplayName,
    enumOptionsDef = enumOptionsDef,      // ← add this line
    fieldType = fieldType,
    formPlaceholderText = formPlaceholderText,
    isConstructorOnly = isConstructorOnly,
    isCreatableByUser = isCreatableByUser,
    isEditableByUser = isEditableByUser,
    isMasked = isMasked,
    isModifiableBySystem = modifiableBySystem,
    isPrivateProperty = isPrivateProperty,
    isUnique = unique,
    nullability = nullability,
    providedValidationConstraints = validationConstraints.toSortedSet(),
    textCase = textCase,
    typeaheadDef = typeaheadDef,
    valueMappings = valueMappings
)
```

- [ ] **Step 4: Add `withOptionsEnum` to `EntityFieldDefBuilder`**

In `EntityFieldDefBuilder.kt`, add the import:

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

`Fqcn` is already imported. Add the new DSL function after `formPlaceholderText`:

```kotlin
fun withOptionsEnum(fqcn: String) {
    this.classFieldDefBuilder.enumOptionsDef = EnumOptionsDef(Fqcn.valueOf(fqcn))
}
```

- [ ] **Step 5: Add `stringValueClassListEnumOptionsDef` to `AngularFormFieldDef`**

In `AngularFormFieldDef.kt`, add the import:

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

Add the computed property after the `enumListDef` property:

```kotlin
val stringValueClassListEnumOptionsDef: EnumOptionsDef? =
    if (classFieldDef.isStringValueClassListWithEnumOptions) classFieldDef.enumOptionsDef else null
```

- [ ] **Step 6: Build the spec module**

Run: `./gradlew :maia-gen:maia-gen-spec:build`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EnumOptionsDef.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/ClassFieldDefBuilder.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityFieldDefBuilder.kt
git add maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AngularFormFieldDef.kt
git commit -m "feat: add EnumOptionsDef and withOptionsEnum DSL for List<StringValueClass> fields"
```

```json:metadata
{"files": ["maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/EnumOptionsDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/lang/ClassFieldDef.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/ClassFieldDefBuilder.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/builders/EntityFieldDefBuilder.kt", "maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/AngularFormFieldDef.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-spec:build", "acceptanceCriteria": ["EnumOptionsDef computes selectOptionsUqcn and selectOptionsTypescriptImport", "ClassFieldDef.isStringValueClassListWithEnumOptions works correctly", "withOptionsEnum DSL function compiles", "spec module builds successfully"], "requiresUserVerification": false}
```

---

### Task 2: Generator rendering and TypeScript import wiring

**Goal:** Generate the correct `mat-select multiple` HTML and TypeScript class field + import for `List<StringValueClass>` fields that carry an `EnumOptionsDef`.

**Files:**
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt`
- Modify: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt`

**Acceptance Criteria:**
- [ ] `MatFormFieldRenderer.renderFormField()` routes to the new render function when `stringValueClassListEnumOptionsDef != null`
- [ ] The rendered HTML is a `mat-select multiple` iterating over `<EnumName>SelectOptions`
- [ ] The generated Angular component TypeScript imports `<EnumName>SelectOptions` and declares `protected readonly <EnumName>SelectOptions = <EnumName>SelectOptions`
- [ ] Material Select/Option/Tooltip modules are imported when the field is present
- [ ] `./gradlew :maia-gen:maia-gen-generator:build` passes

**Verify:** `./gradlew :maia-gen:maia-gen-generator:build` → `BUILD SUCCESSFUL`

**Steps:**

- [ ] **Step 1: Add new render branch and function in `MatFormFieldRenderer`**

In `renderFormField()`, the current structure is:

```kotlin
} else if (htmlFormField.isEnumList) {

    renderReactiveFormMultiSelectFieldForEnumList(htmlFormField, r, indent)

} else if (htmlFormField.isEnum) {
```

Insert a new branch between `isEnumList` and `isEnum`:

```kotlin
} else if (htmlFormField.isEnumList) {

    renderReactiveFormMultiSelectFieldForEnumList(htmlFormField, r, indent)

} else if (htmlFormField.stringValueClassListEnumOptionsDef != null) {

    renderReactiveFormMultiSelectForStringValueClassList(htmlFormField, r, indent)

} else if (htmlFormField.isEnum) {
```

Add this new private function at the end of the object, after `renderReactiveFormMultiSelectFieldForEnumList`:

```kotlin
private fun renderReactiveFormMultiSelectForStringValueClassList(
    htmlFormField: AngularFormFieldDef,
    r: AbstractSourceRenderer,
    indent: String
) {
    val label = htmlFormField.fieldLabel
    val classFieldDef = htmlFormField.classFieldDef
    val classFieldName = classFieldDef.classFieldName
    val enumOptionsDef = htmlFormField.stringValueClassListEnumOptionsDef!!

    r.append("""
        |$indent<mat-form-field appearance="outline">
        |$indent    <mat-label>$label</mat-label>
        |$indent    <mat-select formControlName="$classFieldName" multiple>
        |$indent        @for ($classFieldName of ${enumOptionsDef.selectOptionsUqcn}; track $classFieldName.name) {
        |$indent            <div [matTooltip]="$classFieldName.description" matTooltipShowDelay="1000">
        |$indent                <mat-option [value]="$classFieldName.name">{{$classFieldName.displayName}}</mat-option>
        |$indent            </div>
        |$indent        }
        |$indent    </mat-select>
        |""".trimMargin())

    r.appendLine("$indent</mat-form-field>")
}
```

Add the import for `EnumOptionsDef` at the top of `MatFormFieldRenderer.kt` (it is needed because the `!!` assertion resolves to `EnumOptionsDef`):

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

- [ ] **Step 2: Add class field declarations in `AngularReactiveFormComponentRenderer`**

Locate the private function `render class fields for enum MatSelect fields` (around line 307). It ends after the `enumFields.plus(listOfEnumFields).distinctBy {...}.forEach {...}` block. Add a new block immediately after the closing brace of that `forEach`:

```kotlin
formGroupFields
    .asSequence()
    .filter { it.isCreatable }
    .mapNotNull { it.stringValueClassListEnumOptionsDef }
    .distinctBy { it.selectOptionsUqcn }
    .forEach { enumOptionsDef ->

        addImport(enumOptionsDef.selectOptionsTypescriptImport)

        append("""
            |
            |
            |    protected readonly ${enumOptionsDef.selectOptionsUqcn} = ${enumOptionsDef.selectOptionsUqcn};
            |""".trimMargin()
        )

    }
```

Add the import at the top of `AngularReactiveFormComponentRenderer.kt`:

```kotlin
import org.maiaframework.gen.spec.definition.EnumOptionsDef
```

- [ ] **Step 3: Add Material module imports in `addImportsForFieldTypes`**

Locate `addImportsForFieldTypes` (around line 1199) and find the `is ListFieldType` branch:

```kotlin
is ListFieldType -> `add imports for ListFieldType`(fieldType)
```

Change it to a block that also handles the new case:

```kotlin
is ListFieldType -> {
    `add imports for ListFieldType`(fieldType)
    angularFormFieldDef.stringValueClassListEnumOptionsDef?.let {
        addImport("@angular/material/select", "MatSelect", isModule = true)
        addImport("@angular/material/select", "MatOption", isModule = true)
        addImport("@angular/material/tooltip", "MatTooltip", isModule = true)
    }
}
```

- [ ] **Step 4: Build the generator module**

Run: `./gradlew :maia-gen:maia-gen-generator:build`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt
git add maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt
git commit -m "feat: render mat-select multiple for List<StringValueClass> fields with enum options"
```

```json:metadata
{"files": ["maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/MatFormFieldRenderer.kt", "maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AngularReactiveFormComponentRenderer.kt"], "verifyCommand": "./gradlew :maia-gen:maia-gen-generator:build", "acceptanceCriteria": ["renderFormField routes to new function when stringValueClassListEnumOptionsDef is non-null", "generated HTML is mat-select multiple", "generated TS imports SelectOptions constant and declares class field", "Material Select/Option/Tooltip modules imported"], "requiresUserVerification": false}
```

---

### Task 3: Wire up the showcase spec and verify end-to-end generation

**Goal:** Use `withOptionsEnum` on the `authorities` field in `MaiaShowcasePartySpec` and confirm the generator produces the correct Angular form files.

**Files:**
- Modify: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`

**Acceptance Criteria:**
- [ ] `authorities` field in `userEntityDef` calls `withOptionsEnum("your.actual.EnumFqcn")`
- [ ] `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` completes successfully
- [ ] `user-entity-edit-form.html` contains `mat-select` with `multiple` for the `authorities` field
- [ ] `user-entity-create-form.html` contains `mat-select` with `multiple` for the `authorities` field
- [ ] `user-entity-edit-form.ts` imports `<EnumName>SelectOptions` and declares `protected readonly <EnumName>SelectOptions = <EnumName>SelectOptions`

**Verify:** `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` → `BUILD SUCCESSFUL`, then grep the generated files as shown below.

**Steps:**

- [ ] **Step 1: Add `withOptionsEnum` to the `authorities` field**

In `MaiaShowcasePartySpec.kt`, locate the `userEntityDef` entity definition and find the `authorities` field block:

```kotlin
field("authorities", fieldListOf(ValueClassDefs.authority)) {
    fieldDisplayName("Authorities")
    editableByUser()
}
```

Add `withOptionsEnum` with the FQCN of whichever enum enumerates the valid authority values. **Substitute the actual FQCN for your project** — the example below uses a placeholder:

```kotlin
field("authorities", fieldListOf(ValueClassDefs.authority)) {
    fieldDisplayName("Authorities")
    editableByUser()
    withOptionsEnum("org.maiaframework.showcase.user.UserAuthorityOption")
}
```

The unqualified class name determines the TypeScript identifiers — in this example, the generator will reference `UserAuthorityOptionSelectOptions` in the Angular form component. A TypeScript file exporting that constant must exist at the corresponding path under `app/gen-components/org/maiaframework/showcase/user/` for the Angular build to succeed (it can be hand-written since it is provided, not generated).

- [ ] **Step 2: Run generation**

Run: `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify generated HTML**

```bash
grep -A5 "authorities" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-entity-edit-form.html
```

Expected output contains: `<mat-select formControlName="authorities" multiple>`

```bash
grep -A5 "authorities" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-entity-create-form.html
```

Expected output contains: `<mat-select formControlName="authorities" multiple>`

- [ ] **Step 4: Verify generated TypeScript**

```bash
grep "SelectOptions" maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/user-entity-edit-form.ts
```

Expected output contains two lines — the import and the class field declaration:
```
import { UserAuthorityOptionSelectOptions } from '@app/gen-components/org/maiaframework/showcase/user/UserAuthorityOptionSelectOptions';
protected readonly UserAuthorityOptionSelectOptions = UserAuthorityOptionSelectOptions;
```

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt
git add maia-showcase/maia-showcase-ui/src/generated/
git commit -m "feat: add withOptionsEnum to showcase authorities field, regenerate"
```

```json:metadata
{"files": ["maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt", "maia-showcase/maia-showcase-ui/src/generated/"], "verifyCommand": "./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration", "acceptanceCriteria": ["authorities field uses withOptionsEnum", "maiaGeneration succeeds", "generated HTML has mat-select multiple for authorities", "generated TS imports SelectOptions constant"], "requiresUserVerification": false}
```

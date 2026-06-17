# Design: Enum Options FQCN for List<StringValueClass> Fields

**Date:** 2026-06-17
**Status:** Approved

## Summary

Add the ability to associate an enum FQCN with a `List<StringValueClass>` entity field via the spec DSL. When the field appears in a generated Angular form it renders as a `mat-select multiple` dropdown — identical to an existing `List<EnumFieldType>` field — sourcing its options from the enum's generated `SelectOptions` constant.

## Motivation

The `User` entity's `authorities` field is typed as `List<Authority>` where `Authority` is a `StringValueClassDef`. There is no generated form control for this field today because the renderer only produces multi-selects for `List<EnumFieldType>`. An enum (`AuthorityEnum` or similar) exists externally that enumerates the valid authority values. We want to declare that association in the spec and have the form render accordingly.

## Design Decisions

- **Association is field-level, not type-level.** The same `StringValueClassDef` can appear in different fields with different (or no) enum options.
- **DSL accepts a raw FQCN string.** The enum may not be defined in the same spec file; accepting an `EnumDef` object would require it to be in scope.
- **New wrapper type `EnumOptionsDef` (Option B).** Derives TypeScript identifiers from the FQCN in one place instead of scattering derivation across the rendering pipeline.
- **Rendering is identical to `isEnumList`.** Same `mat-select multiple` template and same `protected readonly selectOptionsUqcn = selectOptionsUqcn` class field pattern.

## New Type: `EnumOptionsDef`

**File:** `maia-gen-spec/.../definition/EnumOptionsDef.kt`

```kotlin
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

Mirrors the subset of `EnumDef` used by the select-options rendering path. No enum values, no code generation for the enum itself.

## Data Model Changes

### `ClassFieldDef`

- New constructor param: `val enumOptionsDef: EnumOptionsDef? = null`
- New computed val:
  ```kotlin
  val isStringValueClassListWithEnumOptions: Boolean =
      fieldType is ListFieldType
          && (fieldType as ListFieldType).parameterFieldType is StringValueClassFieldType
          && enumOptionsDef != null
  ```

### `ClassFieldDefBuilder`

- New mutable field: `var enumOptionsDef: EnumOptionsDef? = null`
- Passed through in `build()`

### `EntityFieldDefBuilder`

- New DSL function:
  ```kotlin
  fun withOptionsEnum(fqcn: String) {
      this.classFieldDefBuilder.enumOptionsDef = EnumOptionsDef(Fqcn.valueOf(fqcn))
  }
  ```

### Spec usage

```kotlin
field("authorities", fieldListOf(ValueClassDefs.authority)) {
    editableByUser()
    withOptionsEnum("org.maiaframework.showcase.user.AuthorityEnum")
}
```

## Form Rendering

### `AngularFormFieldDef`

New computed property:
```kotlin
val stringValueClassListEnumOptionsDef: EnumOptionsDef? =
    if (classFieldDef.isStringValueClassListWithEnumOptions) classFieldDef.enumOptionsDef else null
```

### `MatFormFieldRenderer.renderFormField()`

New branch inserted before the `isEnum` check:
```kotlin
} else if (htmlFormField.stringValueClassListEnumOptionsDef != null) {
    renderReactiveFormMultiSelectForStringValueClassList(htmlFormField, r, indent)
```

New private function `renderReactiveFormMultiSelectForStringValueClassList` — identical template to `renderReactiveFormMultiSelectFieldForEnumList` substituting `selectOptionsUqcn` from `htmlFormField.stringValueClassListEnumOptionsDef!!.selectOptionsUqcn`.

## TypeScript Import Wiring (`AngularReactiveFormComponentRenderer`)

### `render class fields for enum MatSelect fields`

After the existing `listOfEnumFields` block, add a parallel sequence for fields where `stringValueClassListEnumOptionsDef != null`. Deduplicate by `selectOptionsUqcn` and emit the same pattern:
```kotlin
addImport(enumOptionsDef.selectOptionsTypescriptImport)
append("    protected readonly ${enumOptionsDef.selectOptionsUqcn} = ${enumOptionsDef.selectOptionsUqcn};")
```

### `addImportsForFieldTypes`

In the `is ListFieldType` branch, after delegating to `add imports for ListFieldType`, check `angularFormFieldDef.stringValueClassListEnumOptionsDef` and add Material Select, Option, and Tooltip module imports when present.

## Files Changed

| File | Change |
|------|--------|
| `maia-gen-spec/.../definition/EnumOptionsDef.kt` | New |
| `maia-gen-spec/.../definition/lang/ClassFieldDef.kt` | Add `enumOptionsDef`, `isStringValueClassListWithEnumOptions` |
| `maia-gen-spec/.../definition/builders/ClassFieldDefBuilder.kt` | Add `enumOptionsDef` field, pass in `build()` |
| `maia-gen-spec/.../definition/builders/EntityFieldDefBuilder.kt` | Add `withOptionsEnum(fqcn: String)` |
| `maia-gen-spec/.../definition/AngularFormFieldDef.kt` | Add `stringValueClassListEnumOptionsDef` |
| `maia-gen-generator/.../renderers/ui/MatFormFieldRenderer.kt` | Add branch + render function |
| `maia-gen-generator/.../renderers/ui/AngularReactiveFormComponentRenderer.kt` | Two touch points for import wiring |

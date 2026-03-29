# Period Constraint DTO Generation Design

## Overview

Generated RequestDtos that contain `Period` fields should validate those fields using `@PeriodConstraint`. Currently the generator stores Period fields as `Period?` with `@field:NotNull`, but the desired output (as demonstrated in the manually-edited `AllFieldTypesCreateRequestDto`) is to store them as `String?` with `@field:PeriodConstraint` (and `@field:NotBlank` for non-nullable fields), with a getter that parses via `Period.parse()`.

## Changes

### 1. `Fqcns.kt` — new constant

```kotlin
val VALIDATOR_CONSTRAINT_PERIOD = Fqcn.valueOf("org.maiaframework.common.validation.PeriodConstraint")
```

### 2. `PeriodConstraintDef.kt` — new file

Located at `maia-gen/maia-gen-spec/src/main/kotlin/org/maiaframework/gen/spec/definition/validation/`.

```kotlin
class PeriodConstraintDef private constructor()
    : AbstractValidationConstraintDef(AnnotationDef(Fqcns.VALIDATOR_CONSTRAINT_PERIOD)) {
    companion object { val INSTANCE = PeriodConstraintDef() }
}
```

### 3. `ClassFieldDef.enrichWithImplicitValidationConstraints()`

Add Period handling before the existing non-nullable block. Period fields get `PeriodConstraintDef` always, and `NotBlankConstraintDef` when non-nullable (instead of `NotNullConstraintDef`). The existing non-nullable block is unchanged for all other types.

```kotlin
if (this.fieldType is PeriodFieldType) {
    enrichedConstraints.add(PeriodConstraintDef.INSTANCE)
    if (this.nullable == false) {
        enrichedConstraints.add(NotBlankConstraintDef.INSTANCE)
    }
}
```

The existing `if (this.nullable == false) { ... }` block only runs for non-Period fields.

### 4. `RequestDtoRenderer.renderConstructorArgs()`

Treat Period like Enum: store as `String?`, force `_raw` suffix and `private` visibility regardless of nullability.

- `isPeriod` flag: `classField.fieldType is PeriodFieldType`
- Constructor arg name: `_raw` suffix when `!fieldIsNullable || isUrl || isPeriod`
- Visibility: `private` unless `fieldIsNullable && !isPeriod`
- Unwrapped type: `classField.copy(fieldType = FieldTypes.string)` for Period, else `classField.unWrapIfComplexType()`

The `java.time.Period` import is preserved via `addImportFor(classField.fieldType)` in `init`.

### 5. `RequestDtoRenderer.renderGetters()`

Replace `is PeriodFieldType -> renderGetterIfNonNullableField(field)` with `is PeriodFieldType -> renderGetterForPeriod(field)`.

New `renderGetterForPeriod()` method always renders a getter (unlike `renderGetterIfNonNullableField` which skips nullable fields):

- Non-nullable: `get() = ${fieldDef.classFieldName}_raw!!.let { Period.parse(it) }`
- Nullable: `get() = ${fieldDef.classFieldName}_raw?.let { Period.parse(it) }`

## Desired Output (per `AllFieldTypesCreateRequestDto`)

```kotlin
// non-nullable Period
@field:NotBlank
@field:PeriodConstraint
@param:JsonProperty("somePeriodModifiable", access = JsonProperty.Access.READ_WRITE)
private val somePeriodModifiable_raw: String?,

// nullable Period
@param:JsonProperty("somePeriodNullable", access = JsonProperty.Access.READ_WRITE)
@field:PeriodConstraint
private val somePeriodNullable_raw: String?,

// getters
@get:JsonIgnore
val somePeriodModifiable
    get() = somePeriodModifiable_raw!!.let { Period.parse(it) }

@get:JsonIgnore
val somePeriodNullable
    get() = somePeriodNullable_raw?.let { Period.parse(it) }
```

## Files Changed

| File | Change |
|------|--------|
| `maia-gen/maia-gen-spec/.../Fqcns.kt` | Add `VALIDATOR_CONSTRAINT_PERIOD` |
| `maia-gen/maia-gen-spec/.../validation/PeriodConstraintDef.kt` | New file |
| `maia-gen/maia-gen-spec/.../lang/ClassFieldDef.kt` | Add Period to `enrichWithImplicitValidationConstraints` |
| `maia-gen/maia-gen-generator/.../renderers/RequestDtoRenderer.kt` | Handle Period in constructor args and getters |
| `maia-showcase/domain/src/generated/.../AllFieldTypesCreateRequestDto.kt` | Re-generate (should now match manual edits) |
| Other generated DTOs with Period fields | Re-generate with PeriodConstraint |

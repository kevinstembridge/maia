# EntityFiltersRenderer fix for `effective_range` (design)

## Problem

The `effective_range tstzrange` migration (see
`2026-06-12-effective-timestamps-tstzrange-design.md` and its implementation
plan) updated `CreateTableSqlRenderer`, `JdbcDaoRenderer`, and
`RowMapperRenderer`, but missed `EntityFiltersRenderer`.

For `hasEffectiveTimestamps` entities, `EntityFiltersRenderer` still generates
`effectiveFrom`/`effectiveTo` `FieldFilter` accessors mapped to the raw column
names `effective_from`/`effective_to`. Those columns no longer exist (replaced
by `effective_range`), so any hand-written code using these filters fails at
query time with `PSQLException: column "effective_from" does not exist`.

Confirmed broken (via `LoginPageTest`):

- `maia-showcase/dao/src/main/kotlin/org/maiaframework/showcase/party/PartyEmailAddressDaoHelper.kt`
  - `findBy` (used by `findLoginEmailAddress`/`findOneOrNullLoginEmailAddress`)
  - `findEffectiveLoginEmailAddressesByParty`
  - `findPrimaryEmailAddress`
- `maia-showcase/repo/src/main/kotlin/org/maiaframework/showcase/party/EmailAddressVerificationRepoHelper.kt`
  - `isEmailAddressVerified`

All five use the same "is this record currently effective" pattern:

```kotlin
filters.effectiveFrom lte Instant.now(),
filters.or(
    filters.effectiveTo.isNull(),
    filters.effectiveTo gte Instant.now(),
)
```

(`EmailAddressVerificationRepoHelper` only uses the `effectiveTo` half, since
its query already filters by a specific `emailAddressId`.)

## Scope

This fix applies to all `hasEffectiveTimestamps` entities (5 usages, in
`MaiaShowcasePartySpec.kt` and `MaiaShowcaseSpec.kt`). `hasEffectiveLocalDates`
entities are unaffected — they retain real `effective_from`/`effective_to`
date columns and are not touched by this change.

Out of scope (carried over from the original migration's constraints):
GiST exclusion constraints / overlap-prevention indexes.

## Design

### 1. `EffectiveTimestampRendererHelper` — new column-expression helper

Add:

```kotlin
fun fieldFilterColumnExpression(entityDef: EntityDef, fieldDef: EntityFieldDef): String {

    if (!usesEffectiveRange(entityDef)) {
        return fieldDef.tableColumnName
    }

    return when (fieldDef.classFieldDef.classFieldName.value) {
        "effectiveFrom" -> "lower($EFFECTIVE_RANGE_COLUMN)"
        "effectiveTo" -> "upper($EFFECTIVE_RANGE_COLUMN)"
        else -> fieldDef.tableColumnName
    }

}
```

This centralizes "what SQL expression backs this filter field", mirroring how
`selectStarClause`/`collapseEffectiveColumns` already centralize the DDL/DAO
equivalents.

### 2. `EntityFiltersRenderer` — `effectiveFrom`/`effectiveTo` use `lower`/`upper(effective_range)`

In `renderFieldFunctionsForJdbc`, use the new helper instead of
`fieldDef.tableColumnName` when constructing the `FieldFilter`:

```kotlin
val columnExpression = EffectiveTimestampRendererHelper.fieldFilterColumnExpression(entityDef, fieldDef)
appendLine("            return $returnType(\"$columnExpression\", Types.${fieldDef.fieldType.sqlType}, this.sqlParamCounter) { value -> $valueMappingText }")
```

For `effectiveFrom`/`effectiveTo` on `hasEffectiveTimestamps` entities this
generates e.g. `FieldFilter("lower(effective_range)", Types.TIMESTAMP, ...)`.
All operators (`eq`, `lte`, `gte`, `isNull`, `isNotNull`, etc.) continue to
work syntactically, since `databaseColumnName` is interpolated raw into the
WHERE clause (`"$fieldName <= :param"` → `"lower(effective_range) <= :param"`).

A doc comment is rendered above these two accessors (only for
`hasEffectiveTimestamps` entities):

> Backed by `lower`/`upper(effective_range)`, which are `NULL` for unbounded
> ends. Use `isEffectiveNow()` for point-in-time-effective queries instead of
> combining `lte`/`gte`/`isNull` on these fields — those comparisons return
> `NULL`/false for unbounded bounds even though such rows ARE effective per
> the `effective_range @> :ts` semantics.

### 3. `EntityFiltersRenderer` — new `isEffectiveNow()` filter

In `renderFunctions()`, only when `entityDef.hasEffectiveTimestamps.value`:

```kotlin
fun isEffectiveNow(): $filterUqcn {
    return EffectiveNowFilter()
}
```

In `renderInnerClasses()`, only when `hasEffectiveTimestamps`:

```kotlin
private class EffectiveNowFilter : $filterUqcn {

    override fun whereClause(fieldConverter: $fieldConverterUqcn): String {
        return "${EffectiveTimestampRendererHelper.EFFECTIVE_RANGE_WHERE_CLAUSE}"
    }

    override fun populateSqlParams(sqlParams: SqlParams) {
        // do nothing
    }

}
```

This reuses the existing `EFFECTIVE_RANGE_WHERE_CLAUSE = "effective_range @>
current_timestamp"` constant, already used by `JdbcDaoRenderer` for
`findAllEffective()`/`findEffectiveByX()`. No SQL params are needed.

### 4. Hand-written call site updates

**`PartyEmailAddressDaoHelper.kt`** — in `findBy`,
`findEffectiveLoginEmailAddressesByParty`, `findPrimaryEmailAddress`, replace:

```kotlin
filters.effectiveFrom lte Instant.now(),
filters.or(
    filters.effectiveTo.isNull(),
    filters.effectiveTo gte Instant.now(),
)
```

with:

```kotlin
filters.isEffectiveNow()
```

Remove the now-unused `import java.time.Instant`.

**`EmailAddressVerificationRepoHelper.kt`** — in `isEmailAddressVerified`,
replace:

```kotlin
filters.or(
    filters.effectiveTo.isNull(),
    filters.effectiveTo gte Instant.now()
)
```

with:

```kotlin
filters.isEffectiveNow()
```

Remove the now-unused `import java.time.Instant`.

## Verification

- Regenerate `maia-showcase` via `maiaGeneration` for all affected modules
  (domain layer regenerates `*EntityFilters.kt` for the 5
  `hasEffectiveTimestamps` entities).
- Run `./gradlew clean build`. `LoginPageTest` and the other Playwright CRUD
  tests that depend on login should pass, since `PartyEmailAddressDaoHelper`/
  `EmailAddressVerificationRepoHelper` no longer query nonexistent columns.
- `EffectiveTimestampTest` (already updated earlier this session for the "null
  effectiveFrom = effective since the beginning of time" semantics) should
  continue to pass — `isEffectiveNow()` uses the same
  `effective_range @> current_timestamp` predicate.

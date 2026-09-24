# Props Dashboard: Redundant Override Flag

## Goal

Flag property overrides whose value matches the underlying environment value, so they can be spotted and removed.

## Definition

An override is **redundant** when a DB override exists for the property and `override.propertyValue == environmentValue` (exact string compare).

- No override → not redundant.
- Override exists but `environmentValue` is null (property only exists as an override) → not redundant.

`environmentValue` comes from `ConfigurableEnvironment.getProperty()`. DB overrides are not registered as a Spring property source, so this is the true underlying value.

## Backend

- `libs/maia-props-parent/maia-props-spec/.../PropsSpec.kt` — `propertyDtoDef`: add `field("isRedundant", FieldTypes.boolean)` after `isOverridden`. Regenerate `PropertyResponseDto`.
- `libs/maia-props-parent/maia-props-service/.../PropsManager.kt` — `toPropertyResponseDto`: set `isRedundant = override.propertyValue == environmentValue` in the override branch, `false` in the non-override branch. `getAllProperties` and `setProperty` both use this builder, so both responses carry the flag.

## Frontend (`libs/maia-ui-workspace/projects/maia-props`)

- `models/PropertyResponseDto.ts`: add `isRedundant: boolean`.
- `props-dashboard-page.html`: new `isRedundant` column immediately after "Overridden", header "Redundant", cell text `Redundant` when true, blank otherwise.
- `props-dashboard-page.ts`: add `isRedundant` to `displayedColumns` after `isOverridden`.
- `state/props-dashboard-filtering.spec.ts`: add `isRedundant` to fixtures.

## Testing

- New `PropsManager` unit test in `maia-props-service` covering: override equals environment value (redundant), override differs (not redundant), override with no environment value (not redundant), no override (not redundant).
- Existing maia-props frontend specs pass; library builds.

## Out of Scope

- Filtering by redundant.
- Bulk removal of redundant overrides.

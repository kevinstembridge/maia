# maia-props: mask sensitive property values

## Goal
Never expose the plaintext value of a sensitive property (password/secret/key/token/credential) to the browser — mask it server-side, everywhere a value currently flows to the frontend: the property list, the history audit trail, and the edit form's pre-fill.

## Why server-side, not just UI
Today `GET /props`, `GET /props/{name}/history`, and `POST /props/{name}` all return raw plaintext values, and the edit dialog pre-fills the actual current value into a visible, editable text input. Masking only in Angular would still leak the real value in the network response body (visible via dev tools, or to anyone calling the API directly). The mask must happen before the response leaves the backend.

## Detecting a sensitive property
A property name is sensitive if it contains, case-insensitively, any of: `password`, `secret`, `key`, `token`, `credential`. This mirrors Spring Boot Actuator's own well-known `Sanitizer` default key list (`.*password.*`, `.*secret.*`, `.*key.*`, `.*token.*`, `.*credentials.*`) — a substring match that intentionally over-masks a little (e.g. `server.ssl.key-store` isn't itself secret) in favor of never under-masking a real secret. Implemented as a private helper in `PropsManager`:

```kotlin
private val sensitiveNameFragments = listOf("password", "secret", "key", "token", "credential")

private fun isSensitivePropertyName(propertyName: String): Boolean =
    sensitiveNameFragments.any { propertyName.contains(it, ignoreCase = true) }
```

## Backend changes

### `PropsSpec.kt`
Add a new field to `propertyDtoDef` (the `Property` response DTO — same DSL mechanism already used for `isOverridden`/`isRedundant`):

```kotlin
field("isSensitive", FieldTypes.boolean)
```

Regenerating (`maiaGeneration`, runs automatically as a build dependency of `:test`/`:build` for `maia-props-domain`) produces the updated `PropertyResponseDto.kt` with the new field. `PropertyHistoryItemResponseDto` does NOT get this field — the history dialog only ever displays an already-masked string, with no frontend decision hanging on sensitivity there.

### `PropsManager.kt`
- Add the `isSensitivePropertyName` helper and a `MASKED_VALUE = "******"` constant.
- Add a helper `maskIfSensitive(value: String?, isSensitive: Boolean): String? = if (isSensitive && value != null) MASKED_VALUE else value`.
- In `toPropertyResponseDto`: compute `isSensitive = isSensitivePropertyName(propertyName)` once. The existing `isRedundant = override.propertyValue == environmentValue` comparison keeps using the **raw** values (computed before masking) — redundancy detection must not be affected by masking. Both DTO-construction branches (override present / no override) get `effectiveValue`/`environmentValue` passed through `maskIfSensitive(...)`, and both get the new `isSensitive = isSensitive` field.
- In `getPropertyHistory`: for each history row, if `isSensitivePropertyName(propertyName)`, replace `propertyValue` with `MASKED_VALUE` (it's a non-nullable `String`, always has a value to mask).

### `PropsManagerTest.kt`
Add cases (mirroring the existing `` `override matching environment value is redundant` `` style):
- a sensitive property (e.g. `"db.password"`) with an override → `effectiveValue`/`environmentValue` masked, `isSensitive` true.
- a sensitive property with no override (environment-only value) → `effectiveValue`/`environmentValue` still masked.
- `isRedundant` is still computed correctly for a sensitive property whose override equals its environment value (masking doesn't corrupt the underlying raw comparison).
- a non-sensitive property is unaffected (`isSensitive` false, values pass through unmasked) — regression check against existing tests.
- `getPropertyHistory` masks a sensitive property's historical values but leaves a non-sensitive property's history unmasked.

## Frontend changes

### `PropertyResponseDto.ts`
Add `isSensitive: boolean;`.

### `props-dashboard-page.ts`
In `onEdit(row)`, when `row.isSensitive` is true, pass `currentValue: null` to the edit dialog instead of `row.effectiveValue` — otherwise the form would pre-fill with the literal masked string `"******"`, and submitting without changing it would overwrite the real secret with that placeholder. Also pass a new `isSensitive: row.isSensitive` into `EditPropertyDialogData`. `onAddOverride()` passes `isSensitive: false` (a brand-new override has no prior sensitivity signal to go on, and no prior value to protect).

### `EditPropertyDialog`
`EditPropertyDialogData` gains `isSensitive: boolean`. When `isSensitive` is true and the dialog is in edit mode (not `isAdding`), show a `<mat-hint>` under the Value field: "Current value is hidden. Enter the full value to update it." No change to the form's initialization logic — passing `currentValue: null` already yields a blank, required Value field, exactly like the "Add override" flow.

### `PropsCard`
No change to what's displayed (the masked string already flows through `effectiveValue`/`environmentValue` and renders as-is), but add a small lock icon (`mat-icon` reading `lock`) immediately before the value when `property().isSensitive` is true, so it's visually clear the asterisks are a deliberate mask rather than a literal value.

## Out of scope
- No masking of the `comment` field (free text about the change, not the value itself).
- No client-side sensitivity detection/duplication of the keyword list — the frontend relies entirely on the backend's `isSensitive` flag.
- No change to the "Add override" flow for a brand-new property name (no existing value to protect; `isSensitive` there is informational-only, always `false`).
- No configurability of the sensitive-keyword list (YAGNI — can be added later if a false positive/negative in practice warrants it).

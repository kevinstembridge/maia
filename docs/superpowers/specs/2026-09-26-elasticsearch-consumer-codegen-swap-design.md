# maia-elasticsearch: swap consumers to generated DTOs (Part 2)

## Goal
Replace the 4 hand-written model files in `maia-elasticsearch`'s Angular library with the generated DTOs produced by Part 1 (`IndexStateResponseDto`, `EsIndexHealthResponseDto`, `ManagedEsIndexInfoResponseDto`, `IndexBaseNameAndVersionResponseDto`, imported via the `@app/*` alias), and simplify the consumer code accordingly.

## Trust the types
Frontend and backend are generated from the identical spec (`MaiaElasticsearchSpec.kt`), and none of its fields are `.nullable()`. The backend's Kotlin DTO literally cannot omit `health`/`managedIndexInfo` — a non-nullable Kotlin `val` always has a value. The hand-written frontend model's `health?`/`managedIndexInfo?` optionality was speculative, not backend truth. So this swap removes the corresponding defensive `?.`/`!!` in consumer code rather than preserving it — confirmed by the user explicitly.

## Deletions
- `models/EsIndexHealthDto.ts`, `models/EsIndexName.ts`, `models/EsIndexStateDto.ts`, `models/ManagedEsIndexInfoDto.ts`
- Their 3 corresponding `export * from ...` lines in `public-api.ts` (confirmed: no consumer outside this library imports any of these four types)

## Field mapping (confirmed field-for-field, only the extraneous ones drop)
| Hand-written | Generated | Note |
|---|---|---|
| `EsIndexStateDto.exists/indexName` | `IndexStateResponseDto.exists/indexName` | identical |
| `EsIndexStateDto.health?: EsIndexHealthDto` | `IndexStateResponseDto.health: EsIndexHealthResponseDto` | now non-optional |
| `EsIndexStateDto.managedIndexInfo?: ManagedEsIndexInfoDto` | `IndexStateResponseDto.managedIndexInfo: ManagedEsIndexInfoResponseDto` | now non-optional |
| `EsIndexHealthDto.status` (+ unused `indexName`) | `EsIndexHealthResponseDto.status` | unused field dropped |
| `ManagedEsIndexInfoDto.description/isActiveVersion` | `ManagedEsIndexInfoResponseDto.description/isActiveVersion` | identical |
| `ManagedEsIndexInfoDto.indexName: EsIndexName` | `ManagedEsIndexInfoResponseDto.indexName: IndexBaseNameAndVersionResponseDto` | nested type renamed |
| `EsIndexName.baseName/version` (+ unused `isActiveVersion`) | `IndexBaseNameAndVersionResponseDto.baseName/version` | unused duplicate field dropped |

## Consumer changes
Import swap only (identical field names, just retyped) in: `elastic-indices-api-service.ts`, `elastic-indices-page.ts`, `elastic-indices-page-store.ts`, `create-index-dialog.ts`, `set-index-version-active-dialog.ts`.

Import swap + simplification (drop now-unnecessary null-safety) in:
- `elastic-indices-filtering.ts`: `deriveDisplayStatus`'s `indexStateDto.health?.status?.toLowerCase()` → `indexStateDto.health.status.toLowerCase()`.
- `elastic-index.ts`/`.html`: `index().health!!.status` → `index().health.status`; `index().managedIndexInfo?.description` → `index().managedIndexInfo.description`; `index().managedIndexInfo?.isActiveVersion` → `index().managedIndexInfo.isActiveVersion`; the actions-section guard `index().exists && index().managedIndexInfo && !index().managedIndexInfo?.isActiveVersion` → `index().exists && !index().managedIndexInfo.isActiveVersion` (the `managedIndexInfo &&` truthy check is now always true, so it's removed, not just retyped).

## Test fixture fix (not just a retype — a real pre-existing bug fix)
`elastic-indices-filtering.spec.ts`'s `indexDto()` helper already doesn't match today's hand-written model (`indexExists`/`summary` instead of `exists`/`managedIndexInfo`), forced through with `as EsIndexStateDto`. This is Part 1's confirmed pre-existing test failure. Fixed here since the file must be touched anyway:
- `indexDto()` rebuilt against `IndexStateResponseDto`'s real shape, unsafe cast removed (the object literal now satisfies the type structurally).
- Every call site's `indexExists` → `exists`; every inline `health: {indexName: ..., status: ...}` → `health: {status: ...}` (drop `indexName`, which doesn't exist on `EsIndexHealthResponseDto`).
- One test — **"returns undefined for an existing index with no health status"** (which injected `health: undefined as unknown as ...` to exercise the `?.` branch) — is **deleted**, not retyped: that scenario is no longer expressible once `health` is trusted as always-present and the defensive code is removed. The other "returns undefined" test (an unrecognized status string) still covers the meaningful branch.

## Scope
Single task — the deletions, consumer swaps, and test fixture fix are tightly coupled (the library won't compile with only some of them done), so this isn't split further. Verification is a full `ng build`/`ng test` for `maia-elasticsearch` at the end.

## Out of scope
No change to `maia-props`/`maia-jobs` (Part 1 only wired up their generation, no consumer swap was requested for them). No change to the spec itself beyond what Part 1 already did. No fix to the still-pre-existing, unrelated backend `compileKotlin` package-mismatch bug found in Part 1.

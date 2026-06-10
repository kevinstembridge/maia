# RightMany CRUD Playwright Test - Implement TODOs

## Goal
Implement all TODOs in `RightManyCrudPlaywrightTest`, covering full create/edit/delete journey
including many-to-many join management, view/blotter/history assertions.

## 1. Generator change: editable effectiveFrom/To on existing join entries
- File: `maia-gen/maia-gen-generator/src/main/kotlin/org/maiaframework/gen/renderers/ui/AbstractCrudReactiveFormHtmlRenderer.kt`
  - `renderManyToManyTimestampedFields()`: replace static effectiveFrom/effectiveTo `<span>` with
    `mat-datepicker` + `mat-timepicker` inputs bound via `[(ngModel)]="join.effectiveFrom"` /
    `join.effectiveTo`, mirroring the existing "Add" mini-form pattern but per join-entry row
    (template-driven, not FormControl-based since joins is a plain array).
- No backend change needed: `RightManyCrudService.update()` already deletes & recreates all joins
  from the submitted array on every save.
- Generic change affecting every `manyToManyEntity()` with `withEffectiveTimestamps`.
- Regenerate affected UI modules; spot-check other many-to-many edit forms still render correctly.

## 2. Spec change: rename misleading "rightEntities" field (RightMany side only)
- File: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcaseSpec.kt`
- In `rightManySearchableDtoDef` and `rightManyBlotterDef` only:
  - `manyToManyField("rightEntities", leftToRightManyToManyJoinEntityDef)` ->
    `manyToManyField("leftEntities", leftToRightManyToManyJoinEntityDef)`
  - `columnFromDto("rightEntities") { header("Right Entities") }` ->
    `columnFromDto("leftEntities") { header("Left Entities") }`
- Leave `leftManySearchableDtoDef` / `leftManyBlotterDef` untouched (correctly named already).
- Regenerate; grep for and fix any other references to old field/DTO names
  (e.g. `RightManyBlotterRowDto.rightEntities`, `RightManySearchableDto.rightEntities`).

## 3. Page object additions
- `RightManyViewPage`: method to read someInt/someString/id/version from `.detail-row`/`.detail-value`.
- `RightManyEditPage`: method to edit effectiveFrom/To of an existing `.join-entry` via new pickers.
- `RightManyBlotterPage`: method to assert "Left Entities" chips column contains given names.

## 4. Test rewrite
Implement journey per existing TODO comments:
1. Create right entity with left-1 joined -> verify view fields (someInt, someString, id, version)
2. Verify history blotter shows CREATE row (version 1)
3. Edit: change left-1's effectiveFrom, add left-2 -> verify view + blotter
4. Edit: add left-3, remove left-1 -> verify view + blotter + history (version 2/3 as applicable)
5. Edit: remove all left entities -> verify view + blotter
6. Delete entity -> verify blotter (gone) + history (DELETE row)

Exact version numbers / row counts to be worked out during plan writing based on how many
edits actually change `someInt`/`someString` (history rows are versioned per entity update,
not per join change, since joins aren't part of recordVersionHistory).

## 5. Deferred (out of scope)
History blotter "leftEntities" column TODO - left as documented comment. Requires new generator
support for effective-dated join history (join entity has no `recordVersionHistory`). Separate
future feature.

## Unresolved questions
- None - confirm during plan writing whether intermediate "remove all left entities" step needs
  its own someString change to produce a distinct history row, or whether we just verify blotter/view state without expecting a new history row.

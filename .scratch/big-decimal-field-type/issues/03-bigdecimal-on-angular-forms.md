# 03: BigDecimal on Angular create and edit forms

**What to build:** A user can create and edit an `AllFieldTypes` record's BigDecimal fields in the UI. Generated TypeScript DTOs type them `string`. The form uses a plain text input with a generated pattern validator allowing at most `precision - scale` integer digits and `scale` decimal digits. Required fields default to `"0"`; nullable fields start empty.

**Blocked by:** 02

**Status:** resolved

- [x] Generated TypeScript DTOs type BigDecimal fields as `string`
- [x] Create and edit forms render a text input with the generated pattern validator, using each field's own precision and scale
- [x] Input with too many integer or decimal digits is rejected in the form
- [x] `AllFieldTypesCreatePage` and `AllFieldTypesEditPage` cover the new fields, and the existing Playwright CRUD tests pass headless with no new scenarios

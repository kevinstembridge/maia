# Bravo CRUD Playwright Test — Design

**Date:** 2026-03-30

## Overview

Add a full CRUD Playwright test for `BravoEntity` following the same approach as `AllFieldTypesCrudPlaywrightTest`. Bravo has a foreign key to `AlphaEntity`; the create/edit forms use a typeahead-backed dropdown to select Alpha. This requires spec additions, code generation, a new page object, and the test class itself.

---

## 1. Spec Additions

Add to `MaiaShowcaseSpec.kt` after `bravoSearchableDtoDef`:

```kotlin
val bravoDtoHtmlTableDef = dtoHtmlTable(bravoSearchableDtoDef) {
    columnFromDto(dtoFieldName = "tableStringFromAlpha", fieldPathInSourceData = "dtoStringFromAlpha")
    columnFromDto(dtoFieldName = "tableStringFromBravo", fieldPathInSourceData = "dtoStringFromBravo")
    columnFromDto(dtoFieldName = "createdTimestampUtc", fieldPathInSourceData = "createdTimestampUtc")
}

val bravoCrudDef = crudTableDef(bravoDtoHtmlTableDef, bravoEntityDef.entityCrudApiDef!!)
```

`alphaTypeaheadDef` has already been added by the developer.

---

## 2. Code Generation

Run `maiaGeneration` for the web/app modules. Inspect the generated:
- `bravo-create-dialog.component.html`
- `bravo-edit-dialog.component.html`

to confirm the exact `formControlName` for the Alpha FK field and the `mat-autocomplete` option text format before writing Playwright selectors.

---

## 3. Fixture Setup

Add `anAlpha()` to `Fixtures` (approach B — parallel to `aUser()`), so Alpha fixture data participates in `resetDatabaseState()`. This avoids scattering Alpha DAO calls into individual test classes.

The Bravo test's `@BeforeAll` calls:
```kotlin
fixtures.anAlpha(someString = "alpha-fixture")
fixtures.resetDatabaseState()
```

`anAlpha()` registers to an internal list; `resetDatabaseState()` truncates tables then inserts all registered fixtures — same pattern as `aUser()`.

The `someString = "alpha-fixture"` value is the search term used in `fillCreateForm()`.

---

## 4. BravoBlotterPage

New page object at:
`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/BravoBlotterPage.kt`

Key methods:
- `fillCreateForm()` — calls `selectAlpha("alpha-fixture")`, fills `someInt` and `someString`
- `fillEditForm()` — updates `someString` to `"testbravo_edited"`
- `selectAlpha(searchTerm)` — types into the typeahead input, waits for options panel, clicks the matching option (selectors filled in after inspecting generated HTML)
- Standard inherited methods: `clickAddButton`, `clickSubmitButton`, `clickEditButtonForFirstRow`, `clickDeleteButtonForFirstRow`, `clickYesButton`, `clickCancelButton`, dialog-closed assertions, `assertTableContainsValue`, `assertTableDoesNotContainValue`

`BravoBlotterPage` is registered on `AbstractPlaywrightTest` alongside `allFieldTypesBlotterPage`.

---

## 5. Test Class

`BravoCrudPlaywrightTest` at:
`maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/bravo/BravoCrudPlaywrightTest.kt`

Mirrors `AllFieldTypesCrudPlaywrightTest`:

```
@BeforeAll: initAdminUserFixture → anAlpha("alpha-fixture") → resetDatabaseState
@BeforeEach: log out
@Test crud journey:
  create → edit (assert "testbravo_edited") → delete cancel → delete confirm
```

---

## Sequencing

1. Add spec entries (`bravoDtoHtmlTableDef`, `bravoCrudDef`)
2. Run `maiaGeneration`
3. Inspect generated HTML — fill in `selectAlpha()` selectors
4. Add `anAlpha()` to `Fixtures`
5. Add `BravoBlotterPage`
6. Register `bravoBlotterPage` in `AbstractPlaywrightTest`
7. Add `BravoCrudPlaywrightTest`

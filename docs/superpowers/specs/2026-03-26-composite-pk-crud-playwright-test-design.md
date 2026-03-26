# CompositePrimaryKeyEntity CRUD Playwright Test

## Goal
Add a full-stack CRUD Playwright test for `CompositePrimaryKeyEntity` covering: create → edit → cancel delete → confirm delete, with `version` assertions after each mutation. Also adds a menu item to the main nav.

## Entity
`CompositePrimaryKeyEntity` (package `org.maiaframework.showcase.composite_pk`)

Fields:
- `someString: String` — composite PK, required at create
- `someInt: Int` — composite PK, required at create
- `someModifiableString: String` — **change to `editableByUser()`**, editable in create + edit forms
- `version: Long` — framework-managed (versioned = true)
- `createdTimestampUtc: Instant` — framework-managed

No surrogate `id` field — blotter uses `someString` + `someInt` as identifiers.

## Spec Changes (`MaiaShowcaseSpec.kt`)

1. Change `someModifiableString` from `modifiableBySystem()` to `editableByUser()` in `compositePrimaryKeyEntityDef`.
2. Add `crud { apis(defaultAuthority = partySpec.writeAuthority) { create(); update(); delete() } }`.
3. Add `compositePrimaryKeySearchableDtoDef` — fields: `someString`, `someInt`, `someModifiableString`, `version`, `createdTimestampUtc`.
4. Add `compositePrimaryKeyDtoHtmlTableDef` — columns: `someString`, `someInt`, `someModifiableString`, `version`, `createdTimestampUtc`, `editActionColumn()`, `deleteActionColumn()`.
5. Add `compositePrimaryKeyCrudDef = crudTableDef(compositePrimaryKeyDtoHtmlTableDef, compositePrimaryKeyEntityDef.entityCrudApiDef!!)`.

Note: `fieldDisplayName(...)` must be added to `someString`, `someInt`, and `someModifiableString` (required by generator, as learned from SomeVersioned).

## Code Generation

Run `./gradlew :maia-showcase:*:maiaGeneration` for domain, dao, repo, service, web, maia-showcase-ui modules.

## Angular UI

- New page: `src/app/pages/composite-pk/composite-pk-blotter-page.ts` + `.html`
  - Imports `CompositePrimaryKeyCrudTableComponent` from generated path
  - `dataPageId="composite_pk_blotter"`
- Route `composite_pk` in `app.routes.ts`
- Menu item in `app.html` (gated on `hasReadAuthority()`):
  ```html
  <button mat-menu-item routerLink="/composite_pk">
      <mat-icon>dialpad</mat-icon>
      <span>Composite PK</span>
  </button>
  ```
- Also add missing `some_versioned` menu item (no menu item was added in the previous task).

## Test Infrastructure

- `CompositePkBlotterPage` — page object extending `AbstractPage`, path `/composite_pk`, pageTestId `composite_pk_blotter`
  - `clickAddButton()`, `fillCreateForm(someString, someInt, someModifiableString)`, `fillEditForm(someModifiableString)`, `clickSubmitButton()`, `assertCreateDialogClosed()`, `assertEditDialogClosed()`, `clickEditButtonForFirstRow()`, `clickDeleteButtonForFirstRow()`, `clickYesButton()`, `clickCancelButton()`, `assertDeleteDialogClosed()`, `assertTableContainsValue(value)`, `assertTableDoesNotContainValue(value)`, `assertVersionEquals(expectedVersion: Long)`
- Register `compositePkBlotterPage` in `AbstractPlaywrightTest`

## Test (`CompositePkCrudPlaywrightTest`)

```
@BeforeAll: initAdminUserFixture(); fixtures.resetDatabaseState()
@BeforeEach: log out

crud journey:
  log in as admin
  navigate to compositePkBlotterPage
  clickAddButton → fillCreateForm("abc", "1", "initial") → clickSubmitButton → assertCreateDialogClosed
  assertVersionEquals(1)
  clickEditButtonForFirstRow → fillEditForm("edited") → clickSubmitButton → assertEditDialogClosed
  assertTableContainsValue("edited") → assertVersionEquals(2)
  clickDeleteButtonForFirstRow → clickCancelButton → assertDeleteDialogClosed
  assertTableContainsValue("edited") → assertVersionEquals(2)
  clickDeleteButtonForFirstRow → clickYesButton → assertDeleteDialogClosed
  assertTableDoesNotContainValue("edited")
```

## Constraints
- Composite PK fields (`someString`, `someInt`) appear in create form but not edit form (PKs are immutable after creation).
- `assertVersionEquals` passes `expectedVersion.toInt()` to Playwright's `waitForFunction` (Kotlin Long not supported by JS bridge — pattern established in SomeVersioned test).
- `assertTableContainsValue` scrolls to 0 before checking (leftmost column is `someString`).

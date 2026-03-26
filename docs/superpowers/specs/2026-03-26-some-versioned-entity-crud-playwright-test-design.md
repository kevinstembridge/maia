# SomeVersionedEntity CRUD Playwright Test

## Goal
Add a full-stack CRUD Playwright test for `SomeVersionedEntity`. The test covers: create → edit → cancel delete → confirm delete, with assertions that the `version` field increments correctly after each mutation.

## Entity
`SomeVersionedEntity` (package `org.maiaframework.showcase.versioned`, `versioned = true`)

Fields:
- `someString: String` — editable, max 100
- `someInt: Int` — editable
- `version: Long` — managed by framework, not user-editable

## Spec Changes (`MaiaShowcaseSpec.kt`)

1. Add `deletable = Deletable.TRUE` to `someVersionedEntityDef`.
2. Add `crud { apis(defaultAuthority = partySpec.writeAuthority) { create(); update(); delete() } }`.
3. Add `someVersionedSearchableDtoDef` — fields: `someString`, `someInt`, `version`, `id`, `createdTimestampUtc`.
4. Add `someVersionedDtoHtmlTableDef` — columns: `someString`, `someInt`, `version`, `id`, `createdTimestampUtc`, `editActionColumn()`, `deleteActionColumn()`.
5. Add `someVersionedCrudDef = crudTableDef(someVersionedDtoHtmlTableDef, someVersionedEntityDef.entityCrudApiDef!!)`.

## Code Generation

Run `./gradlew :maia-showcase:maiaGeneration` (or all layer tasks in order) to regenerate domain/dao/repo/service/web/UI.

## Angular UI

- New page: `src/app/pages/some-versioned/some-versioned-blotter-page.ts` + `.html`
  - Imports `SomeVersionedCrudTableComponent` from generated path
- Route `some_versioned` added to `app.routes.ts`

## Test Infrastructure

- `SomeVersionedBlotterPage` — page object extending `AbstractPage`, path `/some_versioned`, element id `some_versioned_blotter`
  - `clickAddButton()`, `fillCreateForm(someString, someInt)`, `fillEditForm(someString)`, `clickSubmitButton()`, `assertCreateDialogClosed()`, `assertEditDialogClosed()`, `clickEditButtonForFirstRow()`, `clickDeleteButtonForFirstRow()`, `clickYesButton()`, `clickCancelButton()`, `assertDeleteDialogClosed()`, `assertTableContainsValue(value)`, `assertTableDoesNotContainValue(value)`, `assertVersionEquals(expectedVersion: Long)`
- Register `someVersionedBlotterPage` in `AbstractPlaywrightTest`

## Test (`SomeVersionedCrudPlaywrightTest`)

```
@BeforeAll: initAdminUserFixture(); fixtures.resetDatabaseState()
@BeforeEach: log out

crud journey:
  log in as admin
  navigate to someVersionedBlotterPage
  clickAddButton → fillCreateForm("hello", "1") → clickSubmitButton → assertCreateDialogClosed
  assertVersionEquals(1)                          // version = 1 after create
  clickEditButtonForFirstRow → fillEditForm("hello_edited") → clickSubmitButton → assertEditDialogClosed → assertTableContainsValue("hello_edited")
  assertVersionEquals(2)                          // version = 2 after first edit
  clickDeleteButtonForFirstRow → clickCancelButton → assertDeleteDialogClosed → assertTableContainsValue("hello_edited")
  assertVersionEquals(2)                          // version unchanged after cancelled delete
  clickDeleteButtonForFirstRow → clickYesButton → assertDeleteDialogClosed → assertTableDoesNotContainValue("hello_edited")
```

## Constraints
- No `someInt` uniqueness issue in test data: use a fixed value (e.g. `"1"`) since DB is reset before each test class.
- The entity has a unique index on `someInt`, so the create form value must be unique across the test run (reset handles this).

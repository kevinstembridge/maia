# LeftSearchable CRUD Playwright Test — Design

## Overview

Add full CRUD (create, update, delete) to `leftSearchableDtoHtmlTableDef` and write a Playwright test that exercises the journey. Upgrades the existing read-only table in-place rather than creating a parallel CRUD-specific table.

---

## Section 1 — Spec Changes (`MaiaShowcaseSpec.kt`)

### `leftEntityDef`

Add `fieldDisplayName`, `editableByUser()` on both fields, and a `crud` block:

```kotlin
val leftEntityDef = entity(
    "org.maiaframework.showcase.many_to_many",
    "Left",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE,
    nameFieldForPkAndNameDto = "someString",
) {
    field("someInt", FieldTypes.int) {
        fieldDisplayName("Some Int")
        editableByUser()
    }
    field("someString", FieldTypes.string) {
        fieldDisplayName("Some String")
        lengthConstraint(max = 100)
        editableByUser()
    }
    crud {
        apis {
            create()
            update()
            delete()
        }
    }
}
```

### `leftSearchableDtoHtmlTableDef`

Upgrade in-place: add `withAddButton = true`, a `someStringFromLeft` column, and action columns:

```kotlin
val leftSearchableDtoHtmlTableDef = dtoHtmlTable(leftSearchableDtoDef, withAddButton = true) {
    columnFromDto("someStringFromLeft", "someString") { header("Some String From Left") }
    columnFromDto("someIntFromLeft", "someInt") { header("Some Int From Left") }
    columnFromDto("rightEntities") { header("Right Entities") }
    editActionColumn()
    deleteActionColumn()
}
```

### New `leftCrudDef`

```kotlin
val leftCrudDef = crudTableDef(leftSearchableDtoHtmlTableDef, leftEntityDef.entityCrudApiDef!!)
```

---

## Section 2 — Angular Page & Route

**New files:**
- `src/app/pages/left-searchable-blotter/left-searchable-blotter-page.ts`
- `src/app/pages/left-searchable-blotter/left-searchable-blotter-page.html`

Mirrors the bravo pattern. Uses the generated `LeftSearchableCrudTableComponent`.

**Route** added to `app.routes.ts`:
```typescript
{
    path: 'left_searchable',
    loadComponent: () =>
        import('@app/pages/left-searchable-blotter/left-searchable-blotter-page').then(
            (m) => m.LeftSearchableBlotterPage,
        ),
},
```

Page layout `dataPageId`: `left_searchable_blotter`.

---

## Section 3 — `AbstractPlaywrightTest`

Add field:
```kotlin
protected lateinit var leftSearchableBlotterPage: LeftSearchableBlotterPage
```

Initialize in `initPlaywrightPage()`:
```kotlin
leftSearchableBlotterPage = LeftSearchableBlotterPage(page, urlHelper)
```

Import: `org.maiaframework.showcase.testing.pages.LeftSearchableBlotterPage`

---

## Section 4 — `LeftSearchableBlotterPage`

Location: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftSearchableBlotterPage.kt`

Bound to route `/left_searchable`, page element ID `left_searchable_blotter`. Mirrors `BravoBlotterPage`.

| Method | Description |
|---|---|
| `clickAddButton()` | Opens create dialog |
| `fillCreateForm(someInt, someString)` | Fills both fields |
| `clickSubmitButton()` | Submits dialog |
| `assertCreateDialogClosed()` | Waits for dialog hidden |
| `clickEditButtonForFirstRow()` | Waits on `someString` col, scrolls to edit col, clicks |
| `fillEditForm(someString)` | Updates someString field |
| `assertEditDialogClosed()` | Waits for dialog hidden |
| `assertTableContainsValue(value)` | Waits until any ag-grid cell contains value |
| `assertTableDoesNotContainValue(value)` | Waits until no ag-grid cell contains value |
| `clickDeleteButtonForFirstRow()` | Scrolls to delete col, clicks |
| `clickYesButton()` | Confirms delete |
| `clickCancelButton()` | Cancels delete |
| `assertDeleteDialogClosed()` | Waits for dialog hidden |

---

## Section 5 — `LeftSearchableCrudPlaywrightTest`

Location: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftSearchableCrudPlaywrightTest.kt`

**`@BeforeAll`**:
- `initAdminUserFixture()`
- `fixtures.resetDatabaseState()`

**`@BeforeEach`**: `log out()`

**`crud journey` test**:
1. `log in as admin user()`
2. `navigate to the(leftSearchableBlotterPage)`
3. `clickAddButton()` → `fillCreateForm(someInt = "42", someString = "testleft")` → `clickSubmitButton()` → `assertCreateDialogClosed()`
4. `assertTableContainsValue("testleft")`
5. `clickEditButtonForFirstRow()` → `fillEditForm("testleft_edited")` → `clickSubmitButton()` → `assertEditDialogClosed()`
6. `assertTableContainsValue("testleft_edited")`
7. Cancel path: `clickDeleteButtonForFirstRow()` → `clickCancelButton()` → `assertDeleteDialogClosed()` → `assertTableContainsValue("testleft_edited")`
8. Confirm path: `clickDeleteButtonForFirstRow()` → `clickYesButton()` → `assertDeleteDialogClosed()` → `assertTableDoesNotContainValue("testleft_edited")`

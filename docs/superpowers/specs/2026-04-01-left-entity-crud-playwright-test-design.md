# LeftEntity CRUD Playwright Test — Design

## Overview

Add a full CRUD Playwright test for `LeftEntity` (many-to-many with `RightEntity`). The blotter displays one row per left entity, with a chips column showing all associated right entity names. This requires: (1) a generator enhancement to support nested many-to-many aggregation in table DTOs, (2) a new `ChipsAgGridCellRendererComponent` in maia-ui, (3) new Angular blotter page + route, (4) Kotlin page object and Playwright test.

---

## Section 1 — Generator Enhancement

### Spec DSL

New method on `DtoHtmlTableDefBuilder`:

```kotlin
fun manyToManyColumn(manyToManyJoinEntityDef: ManyToManyEntityDef)
```

Produces a `DtoHtmlTableManyToManyColumnDef` (distinct from `DtoHtmlTableColumnDef`) so the generator can identify and handle it separately.

### Domain

Generated `LeftTableDto` gains field:

```kotlin
val rightEntities: List<RightPkAndNameDto>
```

`RightPkAndNameDto` is already generated (`id: DomainId`, `name: String`).

TypeScript interface gains:

```typescript
rightEntities: RightPkAndNameDto[]
```

### DAO — Two-Query Approach

`LeftTableDtoDao.search()`:

1. **Page query**: paginated, filterable, sortable query on `maia.left` (existing pattern — no join to `maia.right`)
2. **Rights query**: `SELECT left_id, right.id, right.some_string FROM maia.many_to_many_join INNER JOIN maia.right ON many_to_many_join.right_id = right.id WHERE many_to_many_join.left_id IN (:ids)`
3. **Combine in memory**: group rights by `leftId`, attach each list to its `LeftTableDto` row

If the page returns zero rows, the rights query is skipped.

### Angular — maia-ui

New `ChipsAgGridCellRendererComponent`:
- Accepts `ICellRendererParams` where `value` is `RightPkAndNameDto[]` (or any `{name: string}[]`)
- Renders each item as a `<mat-chip>` (read-only, no click handler)
- Imports `MatChipsModule`

Registered in `AgGridCellRendererDefs` as `manyToManyChips`.

### Angular — Generated Table Component

For a `DtoHtmlTableManyToManyColumnDef`, the renderer emits:
- `cellRenderer: ChipsAgGridCellRendererComponent` in the col def
- `MatChipsModule` added to component `imports`
- `ChipsAgGridCellRendererComponent` added to component `imports`
- `cellDataType: 'object'` (no valueFormatter)

---

## Section 2 — Showcase Spec Changes

### `leftEntityDef` additions

Add `fieldDisplayName`, `editableByUser()` to both fields, plus a `crud {}` block:

```kotlin
val leftEntityDef = entity(
    "org.maiaframework.showcase.many_to_many",
    "Left",
    deletable = Deletable.TRUE,
    allowDeleteAll = AllowDeleteAll.TRUE
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

### New table + CRUD definitions

```kotlin
val leftDtoHtmlTableDef = dtoHtmlTable(leftSearchableDtoDef, withAddButton = true) {
    columnFromDto(dtoFieldName = "tableSomeStringFromLeft", fieldPathInSourceData = "someStringFromLeft")
    columnFromDto(dtoFieldName = "tableSomeIntFromLeft", fieldPathInSourceData = "someIntFromLeft")
    manyToManyColumn(manyToManyJoinEntityDef)
    editActionColumn()
    deleteActionColumn()
}

val leftCrudDef = crudTableDef(leftDtoHtmlTableDef, leftEntityDef.entityCrudApiDef!!)
```

`manyToManyColumn` produces column name `rightEntities`, header `"Right Entities"`.

---

## Section 3 — Angular Page & Routing

- New file: `src/app/pages/left-blotter/left-blotter-page.ts` — uses generated `LeftCrudTableComponent`, mirrors `BravoBlotterPage`
- New file: `src/app/pages/left-blotter/left-blotter-page.html`
- Route `/left` added to `app.routes.ts`
- `AbstractPlaywrightTest` gains `protected lateinit var leftBlotterPage: LeftBlotterPage`, initialized in `initPlaywrightPage()`

---

## Section 4 — Test Infrastructure & Test Journey

### `LeftBlotterPage` (Kotlin page object)

Location: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/LeftBlotterPage.kt`

Bound to route `/left`, page element ID `left_blotter`. Methods:

| Method | Description |
|---|---|
| `clickAddButton()` | Opens create dialog |
| `fillCreateForm(someInt, someString)` | Fills both fields |
| `clickSubmitButton()` | Submits dialog |
| `assertCreateDialogClosed()` | Waits for dialog hidden |
| `clickEditButtonForFirstRow()` | Scrolls to edit action column, clicks |
| `fillEditForm(someString)` | Updates someString field |
| `assertEditDialogClosed()` | Waits for dialog hidden |
| `clickDeleteButtonForFirstRow()` | Scrolls to delete column, clicks |
| `clickYesButton()` | Confirms delete |
| `clickCancelButton()` | Cancels delete |
| `assertDeleteDialogClosed()` | Waits for dialog hidden |
| `assertTableContainsValue(value)` | Waits until any cell contains value |
| `assertTableDoesNotContainValue(value)` | Waits until no cell contains value |
| `assertChipVisible(chipText)` | Waits for a `mat-chip` with matching text in `rightEntities` column |

### `LeftEntityCrudPlaywrightTest`

Location: `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/LeftEntityCrudPlaywrightTest.kt`

**`@BeforeAll`**:
- `initAdminUserFixture()`
- Create `rightEntity1 = RightEntityTestBuilder(someString = "aSomeRightValue1").build()`
- Create `rightEntity2 = RightEntityTestBuilder(someString = "aSomeRightValue2").build()`
- Create `leftEntityFixture = LeftEntityTestBuilder(someString = "fixture-left").build()`
- Create join `ManyToManyJoinEntityTestBuilder(leftId = leftEntityFixture.id, rightId = rightEntity1.id).build()`
- Create join `ManyToManyJoinEntityTestBuilder(leftId = leftEntityFixture.id, rightId = rightEntity2.id).build()`
- `fixtures.resetDatabaseState()` (inserts all of the above)

**`@BeforeEach`**: `log out()`

**`crud journey` test**:
1. `log in as admin user()`
2. `navigate to the(leftBlotterPage)`
3. `assertChipVisible("aSomeRightValue1")` — chips column renders right entity names
4. `assertChipVisible("aSomeRightValue2")`
5. `clickAddButton()` → `fillCreateForm(someInt = "99", someString = "testleft")` → `clickSubmitButton()` → `assertCreateDialogClosed()`
6. `assertTableContainsValue("testleft")`
7. `clickEditButtonForFirstRow()` → `fillEditForm("testleft_edited")` → `clickSubmitButton()` → `assertEditDialogClosed()`
8. `assertTableContainsValue("testleft_edited")`
9. Cancel path: `clickDeleteButtonForFirstRow()` → `clickCancelButton()` → `assertDeleteDialogClosed()` → `assertTableContainsValue("testleft_edited")`
10. Confirm path: `clickDeleteButtonForFirstRow()` → `clickYesButton()` → `assertDeleteDialogClosed()` → `assertTableDoesNotContainValue("testleft_edited")`

---

## Open Questions

- None.

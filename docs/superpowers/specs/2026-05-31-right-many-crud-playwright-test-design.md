# RightMany CRUD Playwright Journey Test

## Goal

A single end-to-end Playwright test covering the full CRUD lifecycle for `RightManyEntity`, including the many-to-many relationship with `LeftManyEntity` and both paths of the two-step delete flow (FK error path and success path).

## Context

`RightManyEntity` (package `org.maiaframework.showcase.many_to_many`) has fields `someInt: Int` and `someString: String`. It has a many-to-many relationship with `LeftManyEntity` via `LeftToRightManyToManyJoinEntity`. The generated UI uses full-page navigation for create and edit (unlike dialog-based entities), and a two-step dialog for delete (FK check first, then confirm).

## Files to Create

| File | Path |
|------|------|
| `RightManyBlotterPage.kt` | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/` |
| `RightManyCreatePage.kt` | same |
| `RightManyEditPage.kt` | same |
| `RightManyViewPage.kt` | same |
| `RightManyCrudPlaywrightTest.kt` | `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/` |

## Files to Modify

- `AbstractPlaywrightTest.kt` — declare and instantiate the 4 new page objects

## Page Objects

### RightManyBlotterPage
- URL: `/right-many-blotter`, pageTestId: `right_many_blotter`
- `clickAddButton()` — clicks Add (triggers Angular navigation to create page, no dialog)
- `clickEditButtonForFirstRow()` — waits for `someString` col content, clicks edit icon cell
- `clickDeleteButtonForFirstRow()` — scrolls to delete col, clicks delete icon cell
- `assertFkCheckDialogShowsError()` — waits for `mat-dialog-container` and asserts error message text visible
- `dismissFkCheckDialog()` — clicks Cancel in FK check dialog, waits for `mat-dialog-container` hidden
- `waitForDeleteDialog()` — waits for `mat-dialog-container` with "Do you want to delete this record?" text
- `clickYesButton()` — clicks Yes in delete dialog
- `clickCancelButton()` — clicks Cancel in delete dialog
- `assertDeleteDialogClosed()` — waits for `mat-dialog-container` hidden
- `assertTableContainsValue(value)` — JS waitForFunction scanning `.ag-cell` text
- `assertTableDoesNotContainValue(value)` — JS waitForFunction confirming absence

### RightManyCreatePage
- URL: `/right-many/create`, pageTestId: `right_many_create`
- `fillForm(someInt, someString)` — fills `input[name='someInt']` and `input[name='someString']`
- `searchAndSelectLeftEntity(searchTerm)` — fills `input[placeholder='Search Left Entities...']`, waits for `mat-option`, JS-clicks to avoid tooltip orphan issue (matches pattern in `LeftManyBlotterPage.searchAndSelectRightEntity`)
- `clickSubmitButton()` — force-clicks Submit button

### RightManyEditPage
- URL: `/right-many/edit/:id` (no direct navigation; reached via blotter edit click), pageTestId: `right_many_edit`
- `assertChipVisible(chipLabel)` — waits for `mat-chip-row` with text
- `removeChip(chipLabel)` — clicks remove button on chip, waits for chip hidden
- `fillForm(someString)` — fills `input[name='someString']`
- `clickSubmitButton()` — force-clicks Submit button

### RightManyViewPage
- pageTestId: `right_many_view`
- No custom methods; `assertOnPage()` (inherited) confirms post-create/edit navigation landed correctly

## Test Setup (`@BeforeAll`)

```kotlin
initAdminUserFixture()
fixtures.resetDatabaseState()
leftToRightManyToManyJoinDao.deleteAll()
rightManyDao.deleteAll()
leftManyDao.deleteAll()
val leftAlpha = LeftManyEntityTestBuilder(someString = "left-alpha").build()
leftManyDao.bulkInsert(listOf(leftAlpha))
esIndexOps.upsert(EsDocHolder(
    id = leftAlpha.id.toString(),
    doc = LeftManyTypeaheadV1EsDoc(id = leftAlpha.id, someString = leftAlpha.someString),
    indexName = leftManyTypeaheadEsIndex.indexName()
))
```

`@BeforeEach`: `log out()`

## Journey Test (`crud journey`)

Single `@Test fun \`crud journey\`()`:

### 1. Create
- Login as admin, navigate to blotter
- `clickAddButton()` → Angular navigates to create page
- `rightManyCreatePage.assertOnPage()`
- `fillForm(someInt = "42", someString = "testright")`
- `searchAndSelectLeftEntity("left-alpha")`
- `clickSubmitButton()` → on success, Angular navigates to view page
- `rightManyViewPage.assertOnPage()`
- `navigate to the(rightManyBlotterPage)`
- `assertTableContainsValue("testright")`

### 2. Delete attempt with FK record
- `clickDeleteButtonForFirstRow()`
- `assertFkCheckDialogShowsError()` — FK check API returns reference exists; dialog stays open
- `dismissFkCheckDialog()` — clicks Cancel, dialog closes

### 3. Edit
- `clickEditButtonForFirstRow()` → Angular navigates to edit page
- `rightManyEditPage.assertOnPage()`
- `assertChipVisible("left-alpha")`
- `removeChip("left-alpha")`
- `fillForm(someString = "testright_edited")`
- `clickSubmitButton()` → navigates to view page
- `rightManyViewPage.assertOnPage()`
- `navigate to the(rightManyBlotterPage)`
- `assertTableContainsValue("testright_edited")`

### 4. Cancel delete
- `clickDeleteButtonForFirstRow()`
- `waitForDeleteDialog()` — FK check passes (no join records), FK dialog auto-closes, delete dialog opens
- `clickCancelButton()`
- `assertDeleteDialogClosed()`
- `assertTableContainsValue("testright_edited")`

### 5. Confirm delete
- `clickDeleteButtonForFirstRow()`
- `waitForDeleteDialog()`
- `clickYesButton()`
- `assertDeleteDialogClosed()`
- `assertTableDoesNotContainValue("testright_edited")`

## Delete Dialog Flow Notes

The delete flow has two dialogs in sequence:
1. `RightManyCheckForeignKeyReferencesDialog` opens on every delete click; calls the FK check API in `ngOnInit`. If references exist it shows an error and waits for Cancel. If no references it calls `dialogRef.close(true)` immediately, which triggers the blotter to open the `RightManyEntityDeleteDialog`.
2. `RightManyEntityDeleteDialog` has Yes/Cancel.

For the FK-error path: `assertFkCheckDialogShowsError()` must wait for the error message to be rendered (the API call is async). For the success path: `waitForDeleteDialog()` must wait for the delete dialog's distinctive text since the FK dialog may briefly appear before auto-closing.

## Autowired Beans in Test

```kotlin
@Autowired lateinit var leftManyDao: LeftManyDao
@Autowired lateinit var rightManyDao: RightManyDao
@Autowired lateinit var leftToRightManyToManyJoinDao: LeftToRightManyToManyJoinDao
@Autowired lateinit var esIndexOps: EsIndexOps
@Autowired lateinit var leftManyTypeaheadEsIndex: LeftManyTypeaheadEsIndex
```

# ManyToManyEffectiveRangeCrudPlaywrightTest Design

## Context

`leftToRightManyToManyJoinEntityDef` is a many-to-many join entity between `LeftManyEntity` and `RightManyEntity` with system-managed effective timestamps (`withEffectiveTimestamps(hasSingleEffectiveRecord = false, managedBy = SYSTEM)`).

System-managed semantics:
- When a join is created, the backend sets `effectiveFrom = now()` and `effectiveTo = null`.
- If an existing join record already exists for the same left/right pair, its `effectiveTo` is set to `now()` before the new one is created.
- The form does **not** show date input fields for system-managed effective timestamps — the user only picks the target entity.

CRUD for this join entity happens through `LeftManyCreatePage` and `LeftManyEditPage` (the mini-form under "Add Right Entities").

## Approach

Extend the existing `LeftManyCreatePage` and `LeftManyEditPage` page objects with new methods scoped to the "Right Entities" timestamped join section. No new page object files. The test class follows the same shape as `LeftManyCrudPlaywrightTest`.

## New Page Object Methods

### `LeftManyCreatePage`

| Method | What it does |
|---|---|
| `clickAddRightJoinEntityButton()` | Clicks "Add Right Entities" stroked button to show the mini-form |
| `searchAndSelectRightJoinEntityInMiniForm(searchTerm)` | Types in the "Search Right Entities..." autocomplete and selects matching option |
| `clickConfirmAddRightJoinInMiniForm()` | Clicks the "Add" flat button inside `.join-mini-form` to confirm |

No date-picker interactions — the form must not display date inputs for system-managed joins.

### `LeftManyEditPage`

| Method | What it does |
|---|---|
| `assertRightJoinEntryVisible(entityName)` | Asserts a `.join-entry` containing `entityName` is visible in the `rightJoins` section |
| `removeRightJoinEntry(entityName)` | Clicks `.join-remove-button` on the matching `.join-entry` and waits for it to disappear |

## Test Class: `ManyToManyEffectiveRangeCrudPlaywrightTest`

**Location:** `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/many_to_many/`

**Injected beans:** `RightManyDao`, `EsIndexOps`, `RightManyTypeaheadEsIndex`

**Fixtures (`@BeforeAll`):**
1. `initAdminUserFixture()`
2. `fixtures.resetDatabaseState()`
3. Insert two `RightMany` entities: `right-gamma`, `right-delta`
4. Upsert both into the `RightManyTypeaheadV1` ES index

**`@BeforeEach`:** `log out()`

### Test: `crud journey`

```
log in as admin → navigate to left-many blotter → click Add

LeftManyCreatePage:
  assertOnPage()
  fillCreateForm()                                  // someInt, someString
  clickAddRightJoinEntityButton()                   // show timestamped mini-form
  searchAndSelectRightJoinEntityInMiniForm("right-gamma")
  clickConfirmAddRightJoinInMiniForm()              // no date fields
  clickSubmitButton()

LeftManyViewPage:
  assertOnPage()
  clickEditButton()

LeftManyEditPage:
  assertOnPage()
  assertRightJoinEntryVisible("right-gamma")
  removeRightJoinEntry("right-gamma")
  fillEditForm()                                    // update someString
  clickSubmitButton()

LeftManyViewPage:
  assertOnPage()

navigate to left-many blotter

LeftManyBlotterPage:
  assertTableContainsValue("testleft_edited")
  clickDeleteButtonForFirstRow()
  clickYesButton()
  assertTableDoesNotContainValue("testleft_edited")
```

## Out of Scope

- Verifying the backend-set `effectiveFrom`/`effectiveTo` timestamps directly (no DB assertion in this test; that belongs in a service-layer test).
- Testing the "close previous join on re-add" backend behavior — covered by backend unit/integration tests.
- Testing the `leftToRightEffectiveRangeEntityDef` (user-managed local-date effective range) — separate test.

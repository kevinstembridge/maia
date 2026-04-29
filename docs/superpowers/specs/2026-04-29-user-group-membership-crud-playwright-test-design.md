# UserGroupMembership CRUD Playwright Test Design

## Goal

Add a Playwright CRUD journey test for the UserGroupMembership blotter page introduced in the `user_group_blotter` branch.

## Key Finding: Incomplete Create Form

The generated create dialog for `UserGroupMembership` only exposes `effectiveFrom` and `effectiveTo` date fields. The `user` and `userGroup` FK fields are absent from the form HTML, so the TypeScript submits them as `null`. The backend (`UserGroupMembershipCreateRequestDto`) marks both `@NotNull`, so every create submission returns a backend validation error. This is the same situation as `UsersCrudPlaywrightTest` (backend rejects create for a different reason). The test handles it by asserting the error and cancelling — not by skipping the path.

The edit dialog has `input[name='userGroup']` and `input[name='user']` text inputs, but `ngOnInit` does not pre-populate them from the fetched DTO. The test fills them in manually using the fixture DomainIds.

There is no delete action (the spec only added `create()` and `update()`).

## Files

| File | Action |
|------|--------|
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/testing/pages/UserGroupMembershipBlotterPage.kt` | Create |
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/user/UserGroupMembershipCrudPlaywrightTest.kt` | Create |
| `maia-showcase/app/src/test/kotlin/org/maiaframework/showcase/AbstractPlaywrightTest.kt` | Modify — add `userGroupMembershipBlotterPage` field and initialise in `initPlaywrightPage()` |

## Page Object: `UserGroupMembershipBlotterPage`

Extends `AbstractPage` with:
- URL path: `/user_group_memberships`
- `dataPageId`: `user_group_memberships_blotter`

Methods (all following the patterns in `LeftSearchableBlotterPage` and `UsersBlotterPage`):

| Method | Behaviour |
|--------|-----------|
| `clickAddButton()` | Click "Add" button, wait for `mat-dialog-container` |
| `clickSubmitButton()` | Force-click "Submit" button (same pattern as other pages) |
| `clickCancelButton()` | Click "Cancel" button |
| `assertCreateDialogClosed()` | Delegates to `assertEditDialogClosed()` |
| `assertEditDialogClosed()` | Wait for `mat-dialog-container` to be hidden |
| `assertDialogShowsError()` | Wait for `mat-dialog-container .alert` |
| `clickEditButtonForFirstRow()` | Wait for `col-id="userDisplayName"` to have text, scroll to edit cell, click, wait for dialog |
| `fillEditForm(userGroupId, userId)` | Wait for `mat-spinner` to hide, fill `input[name='userGroup']`, fill `input[name='user']`, `mouse.move(0,0)`, `Thread.sleep(500)` |
| `assertTableContainsValue(value)` | Standard AG Grid cell scan (same as other pages) |

## Test: `UserGroupMembershipCrudPlaywrightTest`

### Fixtures (`@BeforeAll`)

1. `initAdminUserFixture()` — registers admin user (WRITE authority) with `fixtures`
2. `fixtures.aUser(loginMailVerified = true)` → `memberUser: UserFixture`
3. `fixtures.resetDatabaseState()` — inserts registered users, truncates UserGroup + Membership tables
4. `userGroupDao.insert(UserGroupEntity.newInstance(authorities = emptyList(), description = "Test group", name = "Test Group", systemManaged = false))` → `userGroup`
5. `userGroupMembershipDao.insert(UserGroupMembershipEntity.newInstance(effectiveFrom = null, effectiveTo = null, user = memberUser.userEntity.id, userGroup = userGroup.id))`

Autowired DAOs: `UserGroupDao`, `UserGroupMembershipDao`.

### Test Method: `crud journey`

```
log in as admin user
navigate to userGroupMembershipBlotterPage

// Read
assertTableContainsValue(memberUser.displayName)
assertTableContainsValue(userGroup.name)

// Create (error path — user/userGroup absent from form)
clickAddButton()
clickSubmitButton()
assertDialogShowsError()
clickCancelButton()
assertCreateDialogClosed()

// Edit (success path)
clickEditButtonForFirstRow()
fillEditForm(userGroupId = userGroup.id.toString(), userId = memberUser.userEntity.id.toString())
clickSubmitButton()
assertEditDialogClosed()
assertTableContainsValue(memberUser.displayName)
```

### `AbstractPlaywrightTest` changes

Add alongside the existing blotter page fields:
```kotlin
protected lateinit var userGroupMembershipBlotterPage: UserGroupMembershipBlotterPage
```

Initialise in `initPlaywrightPage()`:
```kotlin
userGroupMembershipBlotterPage = UserGroupMembershipBlotterPage(page, urlHelper)
```

Add import for `UserGroupMembershipBlotterPage`.

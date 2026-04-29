# User Group Membership Blotter — Design

## Goal

Add a page to maia-showcase-ui that displays a blotter of UserGroupMembership records, following the standard Maia spec-driven pattern.

## Spec Changes

File: `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`

### 1. Update `userGroupMembershipSearchableDtoDef`

Add `withGeneratedEndpoint = WithGeneratedEndpoint.TRUE` and `withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE`, plus five new fields:

- `userDisplayName` → `user.displayName`
- `userGroupName` → `userGroup.name`
- `effectiveFrom` (direct entity field)
- `effectiveTo` (direct entity field)

Existing fields retained: `id`, `userId`, `userGroupId`.

### 2. Add `crud` block to `userGroupMembershipEntityDef`

Add a `crud` block inside `userGroupMembershipEntityDef` to enable CRUD API generation (required for `crudBlotter`):

```kotlin
crud {
    apis(defaultAuthority = adminAuthority) {
        create()
        update()
    }
}
```

### 3. Add `userGroupMembershipBlotterDef`

New blotter definition after the searchable DTO, with `withAddButton = true` and `editActionColumn()`:

```kotlin
val userGroupMembershipBlotterDef = blotter(
    userGroupMembershipSearchableDtoDef,
    withAddButton = true,
    withGeneratedDto = WithGeneratedDto.TRUE,
    withGeneratedEndpoint = WithGeneratedEndpoint.TRUE,
    withGeneratedFindAllFunction = WithGeneratedFindAllFunction.TRUE,
) {
    columnFromDto("userDisplayName") { header("User") }
    columnFromDto("userGroupName") { header("Group") }
    columnFromDto("effectiveFrom") { header("Effective From") }
    columnFromDto("effectiveTo") { header("Effective To") }
    columnFromDto("userId") { header("User ID") }
    columnFromDto("userGroupId") { header("Group ID") }
    columnFromDto("id") { header("ID") }
    editActionColumn()
}
```

### 4. Add `userGroupMembershipCrudDef`

```kotlin
val userGroupMembershipCrudDef = crudBlotter(userGroupMembershipBlotterDef, userGroupMembershipEntityDef.entityDef.entityCrudApiDef!!)
```

## Generation Pipeline

Run `maiaGeneration` in layer order:

1. `domain`
2. `dao`
3. `repo`
4. `service`
5. `web`
6. `maia-showcase-ui`

## UI Page

Create `maia-showcase-ui/src/app/pages/user-group-membership-blotter/`:
- `user-group-membership-blotter-page.ts` — thin wrapper around generated `UserGroupMembershipCrudBlotterComponent` inside `PageLayoutComponent`
- `user-group-membership-blotter-page.html` — template

Add route `user_group_memberships` to `app.routes.ts` using lazy-loaded import.

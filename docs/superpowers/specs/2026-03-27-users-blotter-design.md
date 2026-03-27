# Users Blotter Page Design

## Overview

Add a Users blotter page to the maia-showcase app — an ag-grid table listing all users, with Add and Edit action buttons.

## Background

The User entity already has:
- Backend search endpoint: `POST /api/user/search` (generated from `userSearchableDtoDef`)
- Backend CRUD endpoints: create, update (generated from `userEntityDef.crud`)
- Angular create/edit dialogs (generated)
- No delete — the spec defines create and update only

Missing: the Angular table component and the page itself.

## Spec Changes

Add to `maia-showcase/spec/src/main/kotlin/org/maiaframework/showcase/MaiaShowcasePartySpec.kt`, after `userSearchableDtoDef`:

```kotlin
val userDtoHtmlTableDef = dtoHtmlTable(
    userSearchableDtoDef,
    withAddButton = true,
) {
    columnFromDto("displayName")
    columnFromDto("firstName")
    columnFromDto("lastName")
    columnFromDto("createdTimestampUtc")
    editActionColumn()
}

val userCrudDef = crudTableDef(userDtoHtmlTableDef, userEntityDef.entityCrudApiDef!!)
```

Columns: `displayName`, `firstName`, `lastName`, `createdTimestampUtc`. No `encryptedPassword` (sensitive) or `id` (internal).

## Code Generation

Run `./gradlew :maia-showcase:maia-showcase-ui:maiaGeneration` after the spec change. This produces:

- `UserTableService` — calls `POST /api/user/search`
- `UserTableComponent` — ag-grid table
- `UserCrudTableComponent` — wraps the table with Add button and edit action wired to the existing dialogs

All generated to `maia-showcase/maia-showcase-ui/src/generated/typescript/main/app/gen-components/org/maiaframework/showcase/user/`.

## New Files

**`maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.ts`**

```typescript
import {Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {UserCrudTableComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-crud-table.component';

@Component({
    imports: [
        PageLayoutComponent,
        UserCrudTableComponent,
    ],
    selector: 'app-users-blotter-page',
    templateUrl: './users-blotter-page.html',
})
export class UsersBlotterPage {}
```

**`maia-showcase/maia-showcase-ui/src/app/pages/users-blotter/users-blotter-page.html`**

```html
<app-page-layout pageTitle="Users" dataPageId="users_blotter">
    <app-user-crud-table></app-user-crud-table>
</app-page-layout>
```

## Routing

Add to `maia-showcase/maia-showcase-ui/src/app/app.routes.ts`:

```typescript
{
    path: 'users',
    loadComponent: () =>
        import('@app/pages/users-blotter/users-blotter-page').then(
            (m) => m.UsersBlotterPage,
        ),
},
```

## Menu

Add to `maia-showcase/maia-showcase-ui/src/app/app.html` inside `<mat-menu>`:

```html
@if (hasReadAuthority()) {
    <button mat-menu-item routerLink="/users">
        <mat-icon>people</mat-icon>
        <span>Users</span>
    </button>
}
```

## Security

- Viewing the list: no authority required beyond being logged in (search endpoint has no `@PreAuthorize`)
- Add / Edit: `SYS__ADMIN` authority required (enforced by backend `@PreAuthorize` on create/update endpoints)
- The Add button and edit action column render for all users; unauthorised attempts are rejected by the backend

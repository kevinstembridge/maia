# User Icon Menu — Design Spec

**Date:** 2026-06-04
**Branch:** mtm_effective_timestamps
**Scope:** Showcase-specific manual change (not a generator change)

## Problem

The toolbar has a plain text "Logout" button when the user is logged in. There is no way to see the current user's permissions from the UI.

## Solution

Replace the Logout button with an `account_circle` icon-button that opens a dropdown menu containing "My Permissions" and "Logout". "My Permissions" opens a dialog showing the logged-in user's full name and granted authorities.

---

## Layer 1: Toolbar (`app.html` + `app.ts`)

### `app.html`

Replace the existing `@if (isLoggedIn())` block:

```html
@if (isLoggedIn()) {
    <button mat-icon-button [matMenuTriggerFor]="userMenu">
        <mat-icon>account_circle</mat-icon>
    </button>
    <mat-menu #userMenu="matMenu">
        <button mat-menu-item (click)="openMyPermissions()">
            <mat-icon>lock</mat-icon>
            <span>My Permissions</span>
        </button>
        <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
        </button>
    </mat-menu>
} @else {
    <a mat-button routerLink="/login">Login</a>
}
```

The `#userMenu` mat-menu ref name is distinct from the existing `#menu` hamburger menu.

### `app.ts`

- Add `MatDialog` import and injection
- Add `MyPermissionsDialog` import
- Add `openMyPermissions()` method: `this.dialog.open(MyPermissionsDialog)`
- Keep existing `logout()` unchanged
- Add `MyPermissionsDialog` to the component `imports` array

---

## Layer 2: `MyPermissionsDialog` component

### New files

- `src/app/components/my-permissions-dialog/my-permissions-dialog.ts`
- `src/app/components/my-permissions-dialog/my-permissions-dialog.html`

### Component (`my-permissions-dialog.ts`)

```typescript
@Component({
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButtonModule],
    selector: 'app-my-permissions-dialog',
    templateUrl: './my-permissions-dialog.html'
})
export class MyPermissionsDialog {
    private readonly dialogRef = inject(MatDialogRef<MyPermissionsDialog>);
    private readonly currentUserStore = inject(CurrentUserStore);
    protected readonly currentUser = this.currentUserStore.currentUser;

    protected close(): void { this.dialogRef.close(); }
}
```

### Template (`my-permissions-dialog.html`)

- `<h2 mat-dialog-title>My Permissions</h2>`
- `<mat-dialog-content>` containing:
  - `<p>{{ currentUser()?.firstName }} {{ currentUser()?.lastName }}</p>`
  - `<ul>` with `@for (authority of currentUser()?.grantedAuthorities; track authority)` → `<li>{{ authority }}</li>`
- `<mat-dialog-actions>` with a single "Close" `mat-button` calling `close()`

---

## Out of scope

- Showing the username/initials on the icon itself (just the generic icon)
- Styling the permissions list beyond a plain `<ul>`
- Any generator-level changes

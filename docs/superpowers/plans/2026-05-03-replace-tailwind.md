# Replace Tailwind CSS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove Tailwind CSS from maia-showcase-ui and replace all utility class usage with named CSS classes using a hybrid global-stylesheet + component-stylesheet approach.

**Architecture:** A small global `styles.scss` defines two reusable structural patterns (`.centered-page`, `.page-content`). Each component that needs styling gets its own `.scss` file for anything specific to that component. Angular's default `ViewEncapsulation.Emulated` keeps component styles scoped.

**Tech Stack:** Angular 19, SCSS, Angular CLI

**User Verification:** NO

---

## File Map

| File | Action | Responsible For |
|---|---|---|
| `src/styles.scss` | Create | `.centered-page`, `.page-content` global patterns |
| `src/styles.css` | Delete | Replaced by `styles.scss` |
| `angular.json` | Modify | Point styles array at `styles.scss` |
| `src/app/app.scss` | Create | `.toolbar-title`, `.toolbar-spacer` |
| `src/app/app.css` | Delete | Replaced by `app.scss` |
| `src/app/app.ts` | Modify | `styleUrl` → `./app.scss` |
| `src/app/app.html` | Modify | Replace Tailwind classes |
| `src/app/pages/home/home-page.scss` | Create | `.page-heading`, `.page-subtitle` |
| `src/app/pages/home/home-page.ts` | Modify | Add `styleUrl` |
| `src/app/pages/home/home-page.html` | Modify | Replace Tailwind classes |
| `src/app/pages/login/login-page.scss` | Create | `.login-card`, `.login-heading`, `.alert-block`, `.alert` variants |
| `src/app/pages/login/login-page.ts` | Modify | Add `styleUrl` |
| `src/app/pages/login/login-page.html` | Modify | Replace Tailwind classes |
| `src/app/components/page-layout/page-layout.html` | Modify | Replace `mx-3 mt-3` with `.page-content` |
| `package.json` | Modify | Remove `tailwindcss`, `@tailwindcss/postcss`, `postcss` |

---

## Task 1: Global stylesheet and Angular config

**Goal:** Replace `styles.css` with `styles.scss` containing the two global layout patterns, and update `angular.json` to reference it.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/styles.scss`
- Delete: `maia-showcase/maia-showcase-ui/src/styles.css`
- Modify: `maia-showcase/maia-showcase-ui/angular.json`

**Acceptance Criteria:**
- [ ] `src/styles.scss` exists with `.centered-page` and `.page-content`
- [ ] `src/styles.css` is deleted
- [ ] `angular.json` styles array references `src/styles.scss`, not `src/styles.css`
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `src/styles.scss`**

```scss
.centered-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
}

.page-content {
  margin: 0.75rem 0.75rem 0;
}
```

Note: `padding: 2rem` captures the `p-8` that both `home-page.html` and `login-page.html` had on their `<section>` elements.

- [ ] **Step 2: Update `angular.json`**

Find the `"styles"` array in the build configuration (under `projects.maia-showcase-ui.architect.build.options`) and change:

```json
"styles": [
    "src/material-theme.scss",
    "src/styles.css"
]
```

to:

```json
"styles": [
    "src/material-theme.scss",
    "src/styles.scss"
]
```

- [ ] **Step 3: Delete `src/styles.css`**

```bash
rm maia-showcase/maia-showcase-ui/src/styles.css
```

- [ ] **Step 4: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/styles.scss \
        maia-showcase/maia-showcase-ui/angular.json
git rm maia-showcase/maia-showcase-ui/src/styles.css
git commit -m "Add global styles.scss with layout patterns, remove styles.css"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/styles.scss", "maia-showcase/maia-showcase-ui/angular.json", "maia-showcase/maia-showcase-ui/src/styles.css"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["styles.scss exists with .centered-page and .page-content", "angular.json references styles.scss", "ng build passes"], "requiresUserVerification": false}
```

---

## Task 2: Update app component

**Goal:** Replace the `app.css` stylesheet and Tailwind classes in `app.html` with named CSS classes in `app.scss`.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/app.scss`
- Delete: `maia-showcase/maia-showcase-ui/src/app/app.css`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/app.html`

**Acceptance Criteria:**
- [ ] `app.scss` exists with `.toolbar-title` and `.toolbar-spacer`
- [ ] `app.css` is deleted
- [ ] `app.ts` `styleUrl` points to `./app.scss`
- [ ] `app.html` has no Tailwind utility classes
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `src/app/app.scss`**

```scss
.toolbar-title {
  font-size: 1.25rem;
  font-weight: 500;
  margin-left: 8px;

  @media (min-width: 768px) {
    margin-left: 0;
  }
}

.toolbar-spacer {
  flex: 1;
}
```

- [ ] **Step 2: Update `app.ts` styleUrl**

Change:
```typescript
styleUrl: './app.css',
```
to:
```typescript
styleUrl: './app.scss',
```

- [ ] **Step 3: Update `app.html`**

Change:
```html
<span class="ml-2 text-xl font-medium md:ml-0">{{ title() }}</span>

<span class="flex-1" aria-hidden="true"></span>
```
to:
```html
<span class="toolbar-title">{{ title() }}</span>

<span class="toolbar-spacer" aria-hidden="true"></span>
```

- [ ] **Step 4: Delete `src/app/app.css`**

```bash
rm maia-showcase/maia-showcase-ui/src/app/app.css
```

- [ ] **Step 5: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/app.scss \
        maia-showcase/maia-showcase-ui/src/app/app.ts \
        maia-showcase/maia-showcase-ui/src/app/app.html
git rm maia-showcase/maia-showcase-ui/src/app/app.css
git commit -m "Replace Tailwind classes in app component with named CSS classes"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/app.scss", "maia-showcase/maia-showcase-ui/src/app/app.ts", "maia-showcase/maia-showcase-ui/src/app/app.html", "maia-showcase/maia-showcase-ui/src/app/app.css"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["app.scss exists", "app.ts references app.scss", "app.html has no Tailwind classes", "ng build passes"], "requiresUserVerification": false}
```

---

## Task 3: Update home-page component

**Goal:** Create `home-page.scss` and replace Tailwind utility classes in `home-page.html`.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.scss`
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.html`

**Acceptance Criteria:**
- [ ] `home-page.scss` exists with `.page-heading` and `.page-subtitle`
- [ ] `home-page.ts` has `styleUrl: './home-page.scss'`
- [ ] `home-page.html` has no Tailwind utility classes
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `src/app/pages/home/home-page.scss`**

```scss
.page-heading {
  font-size: 1.875rem;
  font-weight: 600;
  margin-top: 1rem;
}

.page-subtitle {
  color: rgb(75, 85, 99);
}
```

- [ ] **Step 2: Update `home-page.ts`**

Add `styleUrl` to the `@Component` decorator:

```typescript
@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.html',
    styleUrl: './home-page.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
```

- [ ] **Step 3: Update `home-page.html`**

Replace the entire file with:

```html
<section class="centered-page" data-page-id="home_page">
    <h1 class="page-heading">Home</h1>
    <p class="page-subtitle">Welcome to the Maia Showcase.</p>
</section>
```

Note: `.centered-page` is a global class defined in `styles.scss`. The `p-8` padding from the original is captured by the `padding: 2rem` already in `.centered-page`.

- [ ] **Step 4: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.scss \
        maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.ts \
        maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.html
git commit -m "Replace Tailwind classes in home-page with named CSS classes"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.scss", "maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.ts", "maia-showcase/maia-showcase-ui/src/app/pages/home/home-page.html"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["home-page.scss exists", "home-page.ts has styleUrl", "home-page.html has no Tailwind classes", "ng build passes"], "requiresUserVerification": false}
```

---

## Task 4: Update login-page component

**Goal:** Create `login-page.scss` and replace Tailwind utility classes in `login-page.html`.

**Files:**
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.scss`
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.html`

**Acceptance Criteria:**
- [ ] `login-page.scss` exists with `.login-card`, `.login-heading`, `.alert-block`, `.alert`, `.alert-warning`, `.alert-error`
- [ ] `login-page.ts` has `styleUrl: './login-page.scss'`
- [ ] `login-page.html` has no Tailwind utility classes
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Create `src/app/pages/login/login-page.scss`**

```scss
.login-card {
  width: 100%;
  max-width: 24rem;
}

.login-heading {
  font-size: 1.875rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.alert-block {
  margin-bottom: 1rem;
}

.alert {
  padding: 1rem;
  border-radius: 4px;
  border: 1px solid;

  &.alert-warning {
    background-color: #fefce8;
    border-color: #facc15;
    color: #92400e;
  }

  &.alert-error {
    background-color: #fef2f2;
    border-color: #f87171;
    color: #991b1b;
  }
}
```

Note: `.alert` goes on the `<p>` element; `.alert-block` goes on the outer `<div>` wrapper that provides bottom margin. The `mb-4` in the original template was on the `<div>`, not the `<p>`.

- [ ] **Step 2: Update `login-page.ts`**

Add `styleUrl` to the `@Component` decorator:

```typescript
@Component({
    selector: 'app-login-form-wrapper',
    imports: [
        LoginForm
    ],
    templateUrl: './login-page.html',
    styleUrl: './login-page.scss'
})
```

- [ ] **Step 3: Update `login-page.html`**

Replace the entire file with:

```html
<section class="centered-page" data-page-id="login_page">
    <div class="login-card">
        <h1 class="login-heading">Log In</h1>

        @if (accountLocked()) {
            <div class="alert-block">
                <p class="alert alert-warning" role="alert">Your email address has not been verified yet.</p>
                <p>Click to <a routerLink="/register/request_email_address_token">request a verification code</a>.</p>
            </div>
        }
        @if (badCredentials()) {
            <div class="alert-block">
                <p class="alert alert-error" role="alert">Incorrect email address or password.</p>
            </div>
        }
        <app-login-form (onFormSubmission)="handleFormSubmission($event)" />
    </div>
</section>
```

- [ ] **Step 4: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.scss \
        maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.ts \
        maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.html
git commit -m "Replace Tailwind classes in login-page with named CSS classes"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.scss", "maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.ts", "maia-showcase/maia-showcase-ui/src/app/pages/login/login-page.html"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["login-page.scss exists with all alert classes", "login-page.ts has styleUrl", "login-page.html has no Tailwind classes", "ng build passes"], "requiresUserVerification": false}
```

---

## Task 5: Update page-layout component

**Goal:** Replace `mx-3 mt-3` Tailwind classes in `page-layout.html` with the global `.page-content` class.

**Files:**
- Modify: `maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html`

**Acceptance Criteria:**
- [ ] `page-layout.html` uses `.page-content` instead of `mx-3 mt-3`
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Update `page-layout.html`**

Change:
```html
<div>
  <div class="mx-3 mt-3">
    <span [attr.data-page-id]="dataPageId">{{pageTitle}}</span>
    <ng-content></ng-content>
  </div>
</div>
```

to:
```html
<div>
  <div class="page-content">
    <span [attr.data-page-id]="dataPageId">{{pageTitle}}</span>
    <ng-content></ng-content>
  </div>
</div>
```

- [ ] **Step 2: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html
git commit -m "Replace Tailwind classes in page-layout with .page-content global class"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/components/page-layout/page-layout.html"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["page-layout.html uses .page-content", "ng build passes"], "requiresUserVerification": false}
```

---

## Task 6: Remove Tailwind dependencies

**Goal:** Remove the three Tailwind npm packages from `package.json` and verify the build still passes.

**Files:**
- Modify: `maia-showcase/maia-showcase-ui/package.json`

**Acceptance Criteria:**
- [ ] `tailwindcss`, `@tailwindcss/postcss`, and `postcss` are absent from `package.json`
- [ ] `node_modules` updated (no Tailwind packages present)
- [ ] `ng build` passes

**Verify:** `cd maia-showcase/maia-showcase-ui && ng build` → BUILD SUCCESSFUL

**Steps:**

- [ ] **Step 1: Remove packages**

```bash
cd maia-showcase/maia-showcase-ui && npm uninstall tailwindcss @tailwindcss/postcss postcss
```

Expected: packages removed from `package.json` and `package-lock.json`.

- [ ] **Step 2: Verify build**

```bash
cd maia-showcase/maia-showcase-ui && ng build
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add maia-showcase/maia-showcase-ui/package.json \
        maia-showcase/maia-showcase-ui/package-lock.json
git commit -m "Remove Tailwind CSS dependencies"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/package.json", "maia-showcase/maia-showcase-ui/package-lock.json"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && ng build", "acceptanceCriteria": ["tailwindcss absent from package.json", "@tailwindcss/postcss absent from package.json", "postcss absent from package.json", "ng build passes"], "requiresUserVerification": false}
```

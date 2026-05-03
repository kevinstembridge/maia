# Replace Tailwind CSS with Named Global CSS Classes

## Goal

Remove Tailwind CSS from `maia-showcase-ui` and replace all utility class usage with a hybrid approach: shared structural patterns in a global stylesheet, component-specific styles in per-component `.scss` files.

## Motivation

Tailwind utility classes baked into component templates couple layout to the component itself, making it impossible for a parent page to embed the component with a different layout. Named CSS classes are more reusable, more readable in large templates, and idiomatic for Angular.

## Scope

Four files currently use Tailwind classes:

| File | Usage |
|---|---|
| `src/app/app.html` | Toolbar layout, title font/weight, responsive margin |
| `src/app/pages/home/home-page.html` | Centered page layout, heading font, muted text color |
| `src/app/pages/login/login-page.html` | Centered page layout, card sizing, alert colors |
| `src/app/components/page-layout/page-layout.html` | Outer page margin |

## Architecture

### Approach: Hybrid (global structural patterns + component stylesheets)

- A small `styles.scss` defines reusable structural patterns used across multiple pages.
- Each component owns its own `.scss` file for anything specific to that component.
- Angular's default `ViewEncapsulation.Emulated` keeps component styles scoped automatically.

---

## Global Stylesheet (`src/styles.scss`)

Replaces the current `src/styles.css` (which contains only `@import "tailwindcss"`).

```scss
// Centered full-height page (used by home-page and login-page)
.centered-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

// Outer page margin wrapper (used by page-layout component)
.page-content {
  margin: 0.75rem 0.75rem 0;
}
```

---

## Component Stylesheets

### `app.scss`

```scss
.toolbar-title {
  font-size: 1.25rem;
  font-weight: 500;
  flex: 1;
  margin-left: 8px;

  @media (min-width: 768px) {
    margin-left: 0;
  }
}
```

### `home-page.scss`

```scss
.page-heading {
  font-size: 1.875rem;
  font-weight: 600;
  margin-top: 16px;
}

.page-subtitle {
  color: #757575;
}
```

### `login-page.scss`

```scss
.login-card {
  width: 100%;
  max-width: 384px;
  margin-bottom: 24px;
}

.alert {
  padding: 16px;
  border-radius: 4px;
  border: 1px solid;
  margin-bottom: 16px;

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

---

## Dependency Removal

Remove from `maia-showcase-ui/package.json`:
- `tailwindcss`
- `@tailwindcss/postcss`
- `postcss`

Update `angular.json` build styles array: replace `src/styles.css` with `src/styles.scss`.

---

## Responsive Support

Responsive layout is handled via standard `@media` queries inside component or global stylesheets. The only current responsive usage is `md:ml-0` on the toolbar title, which maps to `@media (min-width: 768px) { margin-left: 0; }` in `app.component.scss`.

---

## Out of Scope

- Generated components (`src/generated/`) — these use no Tailwind classes.
- `material-theme.scss` — unchanged.
- Any future pages/components not yet created.

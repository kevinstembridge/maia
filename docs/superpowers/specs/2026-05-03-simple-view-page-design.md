# Simple View Page Design

## Overview

Implement `SimpleViewPage` to display the attributes of a `Simple` entity fetched by primary key from the route.

## Route

Already configured in `app.routes.ts`:
```
simple/view/:id
```

## Component

**File:** `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.ts`

- Standalone, `ChangeDetectionStrategy.OnPush`
- Inject `ActivatedRoute` and `SimpleService`
- Single signal derived from route param → service fetch:

```typescript
dto = toSignal(
  inject(ActivatedRoute).paramMap.pipe(
    map(p => p.get('id')!),
    switchMap(id => this.simpleService.findById(id))
  )
)
```

- Signal starts as `undefined` (used as loading guard in template)

## Template

**File:** `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html`

- Wrapped in `<app-page-layout pageTitle="Simple" dataPageId="simple_view">`
- `@if (dto())` guard before rendering fields
- Vertical label/value layout (no separate SCSS file)

Fields displayed (from `SimpleDto`):

| Label | Property |
|---|---|
| Some String | `dto().someString` |
| Created | `dto().createdTimestampUtc` |
| ID | `dto().id` |

## Service

`SimpleService.findById(id: string): Observable<SimpleDto>` — read-only GET at `/api/simple/:id`.

`SimpleCrudService` is NOT used here (its `fetchForEdit` is for edit-intent flows).

## No new files

Only `simple-view-page.ts` and `simple-view-page.html` are modified.

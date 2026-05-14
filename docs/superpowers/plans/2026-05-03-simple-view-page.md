# Simple View Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `CompositePrimaryKeyViewPage` to display a `Simple` entity's attributes fetched by ID from the route.

**Architecture:** Standalone Angular component with `ChangeDetectionStrategy.OnPush`. Uses `toSignal` + `switchMap` on `ActivatedRoute.paramMap` to reactively fetch from `SimpleService.findById`. Template guarded with `@if (dto())`.

**Tech Stack:** Angular 19 signals (`toSignal`), RxJS `switchMap`, Angular `ActivatedRoute`, `PageLayout` component, Angular `TestBed`.

**User Verification:** NO

---

## File Map

| File | Action |
|---|---|
| `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/composite-primary-key-view-page.ts` | Modify — implement component |
| `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html` | Modify — implement template |
| `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.spec.ts` | Create — component tests |

---

### Task 1: Implement CompositePrimaryKeyViewPage component and template

**Goal:** Replace the stub `CompositePrimaryKeyViewPage` with a working component that reads `:id` from the route, fetches the entity, and renders its fields vertically.

**Files:**
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/composite-primary-key-view-page.ts`
- Modify: `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html`
- Create: `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.spec.ts`

**Acceptance Criteria:**
- [ ] Component compiles with no TypeScript errors
- [ ] Route `/simple/view/:id` renders the page with labels and values for `someString`, `createdTimestampUtc`, and `id`
- [ ] Template is blank (no field content) while the signal is `undefined` (before fetch resolves)
- [ ] All tests pass

**Verify:** `cd maia-showcase/maia-showcase-ui && npx ng test --include='**/simple-view-page.spec.ts' --watch=false` → all specs pass

**Steps:**

- [ ] **Step 1: Write the failing spec**

Create `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.spec.ts`:

```typescript
import {TestBed} from '@angular/core/testing';
import {convertToParamMap} from '@angular/router';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {CompositePrimaryKeyViewPage} from './simple-view-page';
import {SimpleService} from '@app/gen-components/org/maiaframework/showcase/simple/simple-service';
import {SimpleDto} from '@app/gen-components/org/maiaframework/showcase/simple/SimpleDto';

describe('CompositePrimaryKeyViewPage', () => {
    const mockDto: SimpleDto = {
        id: 'test-id-123',
        someString: 'Hello World',
        createdTimestampUtc: '2026-01-01T00:00:00Z',
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CompositePrimaryKeyViewPage],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {paramMap: of(convertToParamMap({id: 'test-id-123'}))},
                },
                {
                    provide: SimpleService,
                    useValue: {findById: () => of(mockDto)},
                },
            ],
        }).compileComponents();
    });

    it('should create', () => {
        const fixture = TestBed.createComponent(CompositePrimaryKeyViewPage);
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('should display field values after fetch', async () => {
        const fixture = TestBed.createComponent(CompositePrimaryKeyViewPage);
        await fixture.whenStable();
        const el = fixture.nativeElement as HTMLElement;
        expect(el.textContent).toContain('Hello World');
        expect(el.textContent).toContain('2026-01-01T00:00:00Z');
        expect(el.textContent).toContain('test-id-123');
    });

    it('should call findById with the route id', () => {
        const service = TestBed.inject(SimpleService);
        const spy = spyOn(service, 'findById').and.returnValue(of(mockDto));
        TestBed.createComponent(CompositePrimaryKeyViewPage);
        expect(spy).toHaveBeenCalledWith('test-id-123');
    });
});
```

- [ ] **Step 2: Run tests — expect failure**

```bash
cd maia-showcase/maia-showcase-ui && npx ng test --include='**/simple-view-page.spec.ts' --watch=false
```

Expected: errors because `CompositePrimaryKeyViewPage` is a stub (no `toSignal`, no `dto` signal).

- [ ] **Step 3: Implement the component**

Replace `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/composite-primary-key-view-page.ts` with:

```typescript
import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, switchMap} from 'rxjs';
import {SimpleService} from '@app/gen-components/org/maiaframework/showcase/simple/simple-service';
import {PageLayout} from '@app/components/page-layout/page-layout';

@Component({
    selector: 'app-simple-view',
    templateUrl: './simple-view-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [PageLayout],
    styles: [`
        .simple-view-fields { display: flex; flex-direction: column; gap: 1rem; padding: 1rem 0; }
        .simple-view-field dt { font-weight: 600; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: #6b7280; margin-bottom: 0.25rem; }
        .simple-view-field dd { margin: 0; }
    `],
})
export class CompositePrimaryKeyViewPage {
    private readonly simpleService = inject(SimpleService);

    protected readonly dto = toSignal(
        inject(ActivatedRoute).paramMap.pipe(
            map(p => p.get('id')!),
            switchMap(id => this.simpleService.findById(id))
        )
    );
}
```

- [ ] **Step 4: Implement the template**

Replace `maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html` with:

```html
<app-page-layout pageTitle="Simple" dataPageId="simple_view">
    @if (dto()) {
        <dl class="simple-view-fields">
            <div class="simple-view-field">
                <dt>Some String</dt>
                <dd>{{ dto()!.someString }}</dd>
            </div>
            <div class="simple-view-field">
                <dt>Created</dt>
                <dd>{{ dto()!.createdTimestampUtc }}</dd>
            </div>
            <div class="simple-view-field">
                <dt>ID</dt>
                <dd>{{ dto()!.id }}</dd>
            </div>
        </dl>
    }
</app-page-layout>
```

- [ ] **Step 5: Run tests — expect pass**

```bash
cd maia-showcase/maia-showcase-ui && npx ng test --include='**/simple-view-page.spec.ts' --watch=false
```

Expected: all 3 specs pass (create, display field values, findById called with route id).

- [ ] **Step 6: Commit**

```bash
git add maia-showcase/maia-showcase-ui/src/app/pages/simple-view/composite-primary-key-view-page.ts \
        maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html \
        maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.spec.ts
git commit -m "Implement CompositePrimaryKeyViewPage with signals-based fetch and vertical label/value layout"
```

```json:metadata
{"files": ["maia-showcase/maia-showcase-ui/src/app/pages/simple-view/composite-primary-key-view-page.ts", "maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.html", "maia-showcase/maia-showcase-ui/src/app/pages/simple-view/simple-view-page.spec.ts"], "verifyCommand": "cd maia-showcase/maia-showcase-ui && npx ng test --include='**/simple-view-page.spec.ts' --watch=false", "acceptanceCriteria": ["component compiles with no TypeScript errors", "template renders someString, createdTimestampUtc, and id labels and values", "template is blank before fetch resolves", "all 3 tests pass"], "requiresUserVerification": false}
```

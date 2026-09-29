# Elasticsearch Consumer Codegen Swap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers-extended-cc:subagent-driven-development (recommended) or superpowers-extended-cc:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Delete `maia-elasticsearch`'s 4 hand-written model files and swap every consumer over to the code-generated DTOs produced by Part 1, trusting the generated types' non-optionality (frontend and backend are generated from the identical spec) and simplifying now-unnecessary defensive null-handling accordingly. Also fix a pre-existing, unrelated broken test fixture in the one file that must be touched anyway.

**Architecture:** A single cohesive task — the deletions, consumer swaps, and test fixture fix are tightly coupled (the library won't compile with only some of them done).

**Tech Stack:** Angular 21 (standalone components, signal `input`/`output`), Vitest, TypeScript.

**User Verification:** NO — no human sign-off was requested.

**IMPORTANT — explicit user instruction for this task, continuing from Part 1:** No git worktree, no feature branch, no commits. All work happens directly on `main` in `/home/kevin/dev/code/maia`, left uncommitted when done.

---

## Design reference

`docs/superpowers/specs/2026-09-26-elasticsearch-consumer-codegen-swap-design.md`

## Prerequisite (already done, uncommitted on `main`)

Part 1 (`docs/superpowers/plans/2026-09-26-angular-ui-workspace-codegen-part1.md`) generated the following real files this task imports from:
- `libs/maia-ui-workspace/projects/maia-elasticsearch/src/generated/typescript/main/app/gen-components/org/maiaframework/elasticsearch/index/model/{IndexStateResponseDto,EsIndexHealthResponseDto,ManagedEsIndexInfoResponseDto,IndexBaseNameAndVersionResponseDto}.ts`
- The `@app/*` path alias is already wired into `maia-elasticsearch`'s `tsconfig.lib.json`/`tsconfig.spec.json`.

## Working directory for all commands

`/home/kevin/dev/code/maia/libs/maia-ui-workspace`

---

### Task 1: Delete hand-written models, swap all consumers, fix the broken test fixture

**Goal:** `maia-elasticsearch` builds and tests pass using only the generated DTOs; the 4 hand-written model files no longer exist.

**Files:**
- Delete: `projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexHealthDto.ts`
- Delete: `projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexName.ts`
- Delete: `projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexStateDto.ts`
- Delete: `projects/maia-elasticsearch/src/lib/elastic-indices/models/ManagedEsIndexInfoDto.ts`
- Modify: `projects/maia-elasticsearch/src/public-api.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/services/elastic-indices-api-service.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/elastic-indices-page.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-page-store.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/dialogs/create-index-dialog/create-index-dialog.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/dialogs/set-index-version-active-dialog/set-index-version-active-dialog.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.ts`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/components/elastic-index/elastic-index.html`
- Modify: `projects/maia-elasticsearch/src/lib/elastic-indices/state/elastic-indices-filtering.spec.ts`

**Acceptance Criteria:**
- [ ] All 4 hand-written model files are deleted; nothing in the library imports from `./models/EsIndexHealthDto`, `./models/EsIndexName`, `./models/EsIndexStateDto`, or `./models/ManagedEsIndexInfoDto` anymore
- [ ] Every consumer imports the generated types from `@app/gen-components/org/maiaframework/elasticsearch/index/model/...` instead
- [ ] `elastic-indices-filtering.ts` and `elastic-index.ts`/`.html` no longer use `?.`/`!!` on `health`/`managedIndexInfo` (trusting the now-non-optional generated type), while behavior is otherwise unchanged
- [ ] `elastic-indices-filtering.spec.ts`'s `indexDto()` fixture builds a real, correctly-shaped `IndexStateResponseDto` with no unsafe cast, and every test uses `exists` (not `indexExists`) and a plain `health: {status: ...}` (no `indexName` on health)
- [ ] The "returns undefined for an existing index with no health status" test is removed (the scenario it tested — `health` being absent — is no longer expressible once the defensive code is removed)
- [ ] `npx ng build maia-elasticsearch` succeeds; `npx ng test maia-elasticsearch` passes with no failures

**Verify:** From `libs/maia-ui-workspace`: `npx ng build maia-elasticsearch` → succeeds; `npx ng test maia-elasticsearch` → all tests pass

**Steps:**

- [ ] **Step 1: Delete the 4 hand-written model files**

```bash
rm libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexHealthDto.ts
rm libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexName.ts
rm libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/models/EsIndexStateDto.ts
rm libs/maia-ui-workspace/projects/maia-elasticsearch/src/lib/elastic-indices/models/ManagedEsIndexInfoDto.ts
```

(Run from `/home/kevin/dev/code/maia`, or adjust the relative paths if run from elsewhere — the `models/` directory itself can stay if empty, or be removed too, either is fine.)

- [ ] **Step 2: Update `public-api.ts`**

Find:
```ts
// Public API Surface of maia-elasticsearch
export * from './lib/elastic-indices/models/EsIndexHealthDto';
export * from './lib/elastic-indices/models/ManagedEsIndexInfoDto';
export * from './lib/elastic-indices/models/EsIndexStateDto';
export * from './lib/elastic-indices/services/elastic-indices-api-base-url-token';
export * from './lib/elastic-indices/services/elastic-indices-api-service';
export * from './lib/elastic-indices/state/elastic-indices-page-store';
export * from './lib/elastic-indices/components/elastic-index/elastic-index';
export * from './lib/elastic-indices/dialogs/create-index-dialog/create-index-dialog';
export * from './lib/elastic-indices/dialogs/set-index-version-active-dialog/set-index-version-active-dialog';
export * from './lib/elastic-indices/elastic-indices-page';
```
Replace with:
```ts
// Public API Surface of maia-elasticsearch
export * from './lib/elastic-indices/services/elastic-indices-api-base-url-token';
export * from './lib/elastic-indices/services/elastic-indices-api-service';
export * from './lib/elastic-indices/state/elastic-indices-page-store';
export * from './lib/elastic-indices/components/elastic-index/elastic-index';
export * from './lib/elastic-indices/dialogs/create-index-dialog/create-index-dialog';
export * from './lib/elastic-indices/dialogs/set-index-version-active-dialog/set-index-version-active-dialog';
export * from './lib/elastic-indices/elastic-indices-page';
```

- [ ] **Step 3: Update `elastic-indices-api-service.ts`**

Find:
```ts
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```
Replace with:
```ts
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```
Then replace every remaining occurrence of `EsIndexStateDto` in this file with `IndexStateResponseDto` (two occurrences: `getIndexDefinitions(): Observable<EsIndexStateDto[]>` and `this.http.get<EsIndexStateDto[]>`).

- [ ] **Step 4: Update `elastic-indices-page.ts`**

Find:
```ts
import {EsIndexStateDto} from './models/EsIndexStateDto';
```
Replace with:
```ts
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```
Then replace both remaining occurrences of `EsIndexStateDto` (the `onCreateIndex(dto: EsIndexStateDto)` and `onSetIndexVersionActive(dto: EsIndexStateDto)` parameter types) with `IndexStateResponseDto`.

- [ ] **Step 5: Update `elastic-indices-page-store.ts`**

Find:
```ts
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```
Replace with:
```ts
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```
Then replace every remaining occurrence of `EsIndexStateDto` in this file with `IndexStateResponseDto` (the `indexStateDtos: EsIndexStateDto[]` state field and the three `computed<EsIndexStateDto[]>` type parameters).

- [ ] **Step 6: Update `create-index-dialog.ts`**

Find:
```ts
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
```
Replace with:
```ts
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```
Then replace the remaining occurrence (`@Inject(MAT_DIALOG_DATA) public dto: EsIndexStateDto`) with `IndexStateResponseDto`.

- [ ] **Step 7: Update `set-index-version-active-dialog.ts`**

Same change as Step 6, in this file: find
```ts
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
```
replace with
```ts
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```
and replace the remaining `dto: EsIndexStateDto` occurrence with `IndexStateResponseDto`.

- [ ] **Step 8: Update `elastic-indices-filtering.ts`**

Find:
```ts
import {ParamMap, Params} from '@angular/router';
import {EsIndexStateDto} from '../models/EsIndexStateDto';
```
Replace with:
```ts
import {ParamMap, Params} from '@angular/router';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
```

Then find:
```ts
export function filterBySystemIndices(indices: EsIndexStateDto[], hideSystemIndices: boolean): EsIndexStateDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: EsIndexStateDto[], nameFilter: string): EsIndexStateDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function deriveDisplayStatus(indexStateDto: EsIndexStateDto): DisplayStatus | undefined {
    if (!indexStateDto.exists) {
        return 'not-created';
    }
    const status = indexStateDto.health?.status?.toLowerCase();
    return status === 'green' || status === 'yellow' || status === 'red' ? status : undefined;
}


export function countByDisplayStatus(indices: EsIndexStateDto[]): Record<DisplayStatus, number> {
    const counts: Record<DisplayStatus, number> = {green: 0, yellow: 0, red: 0, 'not-created': 0};
    for (const index of indices) {
        const status = deriveDisplayStatus(index);
        if (status) {
            counts[status]++;
        }
    }
    return counts;
}


export function filterByStatus(indices: EsIndexStateDto[], status: DisplayStatus | null): EsIndexStateDto[] {
    return status === null ? indices : indices.filter((it) => deriveDisplayStatus(it) === status);
}
```
Replace with:
```ts
export function filterBySystemIndices(indices: IndexStateResponseDto[], hideSystemIndices: boolean): IndexStateResponseDto[] {
    return indices.filter((it) => hideSystemIndices === false || it.indexName.startsWith('.') === false);
}


export function filterAndSortByName(indices: IndexStateResponseDto[], nameFilter: string): IndexStateResponseDto[] {
    const normalizedFilter = nameFilter.toLowerCase();
    return indices
        .filter((it) => it.indexName.toLowerCase().includes(normalizedFilter))
        .sort((a, b) => a.indexName.localeCompare(b.indexName));
}


export function deriveDisplayStatus(indexStateDto: IndexStateResponseDto): DisplayStatus | undefined {
    if (!indexStateDto.exists) {
        return 'not-created';
    }
    const status = indexStateDto.health.status.toLowerCase();
    return status === 'green' || status === 'yellow' || status === 'red' ? status : undefined;
}


export function countByDisplayStatus(indices: IndexStateResponseDto[]): Record<DisplayStatus, number> {
    const counts: Record<DisplayStatus, number> = {green: 0, yellow: 0, red: 0, 'not-created': 0};
    for (const index of indices) {
        const status = deriveDisplayStatus(index);
        if (status) {
            counts[status]++;
        }
    }
    return counts;
}


export function filterByStatus(indices: IndexStateResponseDto[], status: DisplayStatus | null): IndexStateResponseDto[] {
    return status === null ? indices : indices.filter((it) => deriveDisplayStatus(it) === status);
}
```
(Note: `deriveDisplayStatus` dropped both `?.` — `health` and `status` are both non-optional on the generated type, per the "trust the types" decision.)

- [ ] **Step 9: Update `elastic-index.ts`**

Find:
```ts
import {Component, computed, input, output} from '@angular/core';
import {EsIndexStateDto} from '../../models/EsIndexStateDto';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<EsIndexStateDto>();

    createIndex = output<EsIndexStateDto>();
    setIndexVersionActive = output<EsIndexStateDto>();
```
Replace with:
```ts
import {Component, computed, input, output} from '@angular/core';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';
import {MatButtonModule} from '@angular/material/button';
import {deriveDisplayStatus, STATUS_COLORS} from '../../state/elastic-indices-filtering';

@Component({
    imports: [MatButtonModule],
    selector: 'maia-elastic-index',
    templateUrl: './elastic-index.html',
    styleUrl: './elastic-index.scss'
})
export class ElasticIndex {

    index = input.required<IndexStateResponseDto>();

    createIndex = output<IndexStateResponseDto>();
    setIndexVersionActive = output<IndexStateResponseDto>();
```
(The rest of the file — `statusColor`, `onCreateIndex`, `onSetIndexVersionActive` — is unchanged.)

- [ ] **Step 10: Update `elastic-index.html`**

Find:
```html
<div class="card">
    <div class="header">
        <span class="name">{{ index().indexName }}</span>
        @if (statusColor(); as color) {
            <span
                class="status-dot"
                [style.background-color]="color"
                [title]="index().health!!.status"
                role="img"
                [attr.aria-label]="index().health!!.status">
            </span>
        }
    </div>
    @if (index().managedIndexInfo?.description) {
        <p class="description">{{ index().managedIndexInfo?.description }}</p>
    }
    @if (index().exists && index().managedIndexInfo?.isActiveVersion) {
        <span class="badge">Active version</span>
    }
    @if (!index().exists) {
        <div class="actions">
            <button mat-flat-button (click)="onCreateIndex()" color="primary">Create...</button>
        </div>
    }
    @if (index().exists && index().managedIndexInfo && !index().managedIndexInfo?.isActiveVersion) {
        <div class="actions">
            <button mat-flat-button (click)="onSetIndexVersionActive()" color="primary">Set as Active Version...</button>
        </div>
    }
</div>
```
Replace with:
```html
<div class="card">
    <div class="header">
        <span class="name">{{ index().indexName }}</span>
        @if (statusColor(); as color) {
            <span
                class="status-dot"
                [style.background-color]="color"
                [title]="index().health.status"
                role="img"
                [attr.aria-label]="index().health.status">
            </span>
        }
    </div>
    @if (index().managedIndexInfo.description) {
        <p class="description">{{ index().managedIndexInfo.description }}</p>
    }
    @if (index().exists && index().managedIndexInfo.isActiveVersion) {
        <span class="badge">Active version</span>
    }
    @if (!index().exists) {
        <div class="actions">
            <button mat-flat-button (click)="onCreateIndex()" color="primary">Create...</button>
        </div>
    }
    @if (index().exists && !index().managedIndexInfo.isActiveVersion) {
        <div class="actions">
            <button mat-flat-button (click)="onSetIndexVersionActive()" color="primary">Set as Active Version...</button>
        </div>
    }
</div>
```

- [ ] **Step 11: Rewrite `elastic-indices-filtering.spec.ts`**

Replace the full contents of the file with:

```ts
import {describe, expect, it} from 'vitest';
import {convertToParamMap} from '@angular/router';
import {
    buildElasticIndicesQueryParams,
    countByDisplayStatus,
    deriveDisplayStatus,
    filterAndSortByName,
    filterBySystemIndices,
    filterByStatus,
    parseElasticIndicesFiltersFromParams
} from './elastic-indices-filtering';
import {IndexStateResponseDto} from '@app/gen-components/org/maiaframework/elasticsearch/index/model/IndexStateResponseDto';


function indexDto(overrides: Partial<IndexStateResponseDto> & {indexName: string}): IndexStateResponseDto {
    return {
        exists: true,
        managedIndexInfo: {
            indexName: {baseName: overrides.indexName, version: 1},
            description: '',
            isActiveVersion: false,
        },
        health: {status: 'green'},
        ...overrides,
    };
}


describe('elastic-indices-filtering', () => {


    describe('filterBySystemIndices()', () => {

        it('returns all indices when hideSystemIndices is false', () => {
            const indices = [indexDto({indexName: '.security'}), indexDto({indexName: 'user-events'})];
            expect(filterBySystemIndices(indices, false)).toEqual(indices);
        });

        it('excludes indices starting with "." when hideSystemIndices is true', () => {
            const visible = indexDto({indexName: 'user-events'});
            const indices = [indexDto({indexName: '.security'}), visible];
            expect(filterBySystemIndices(indices, true)).toEqual([visible]);
        });

    });


    describe('filterAndSortByName()', () => {

        it('returns all indices sorted alphabetically when nameFilter is empty', () => {
            const indices = [indexDto({indexName: 'zeta'}), indexDto({indexName: 'alpha'})];
            expect(filterAndSortByName(indices, '').map((it) => it.indexName)).toEqual(['alpha', 'zeta']);
        });

        it('matches indexName case-insensitively', () => {
            const indices = [indexDto({indexName: 'User-Events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'user').map((it) => it.indexName)).toEqual(['User-Events']);
        });

        it('excludes indices whose name does not contain the filter', () => {
            const indices = [indexDto({indexName: 'user-events'}), indexDto({indexName: 'audit-log'})];
            expect(filterAndSortByName(indices, 'zzz')).toEqual([]);
        });

    });


    describe('deriveDisplayStatus()', () => {

        it('returns "not-created" when the index does not exist, even if health data is present', () => {
            const index = indexDto({indexName: 'a', exists: false, health: {status: 'green'}});
            expect(deriveDisplayStatus(index)).toEqual('not-created');
        });

        it('returns the lowercased health status for an existing index', () => {
            const index = indexDto({indexName: 'a', exists: true, health: {status: 'YELLOW'}});
            expect(deriveDisplayStatus(index)).toEqual('yellow');
        });

        it('returns undefined for an existing index with an unrecognized health status', () => {
            const index = indexDto({indexName: 'a', exists: true, health: {status: 'purple'}});
            expect(deriveDisplayStatus(index)).toBeUndefined();
        });

    });


    describe('countByDisplayStatus()', () => {

        it('counts every display status, including zeros for statuses with no matches', () => {
            const indices = [
                indexDto({indexName: 'a', health: {status: 'GREEN'}}),
                indexDto({indexName: 'b', health: {status: 'green'}}),
                indexDto({indexName: 'c', health: {status: 'red'}}),
                indexDto({indexName: 'd', exists: false})
            ];
            expect(countByDisplayStatus(indices)).toEqual({green: 2, yellow: 0, red: 1, 'not-created': 1});
        });

        it('returns all zeros for an empty list', () => {
            expect(countByDisplayStatus([])).toEqual({green: 0, yellow: 0, red: 0, 'not-created': 0});
        });

    });


    describe('filterByStatus()', () => {

        it('returns all indices unchanged when status is null', () => {
            const indices = [indexDto({indexName: 'a'}), indexDto({indexName: 'b', exists: false})];
            expect(filterByStatus(indices, null)).toEqual(indices);
        });

        it('returns only indices matching the given status', () => {
            const green = indexDto({indexName: 'a', health: {status: 'green'}});
            const red = indexDto({indexName: 'b', health: {status: 'red'}});
            expect(filterByStatus([green, red], 'red')).toEqual([red]);
        });

        it('matches "not-created" against indices that do not exist', () => {
            const missing = indexDto({indexName: 'a', exists: false});
            const existing = indexDto({indexName: 'b'});
            expect(filterByStatus([missing, existing], 'not-created')).toEqual([missing]);
        });

    });


    describe('parseElasticIndicesFiltersFromParams()', () => {

        it('parses all three filters when all params are present', () => {
            const params = convertToParamMap({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
            expect(parseElasticIndicesFiltersFromParams(params)).toEqual({
                nameFilter: 'audit',
                statusFilter: 'red',
                hideSystemIndices: false,
            });
        });

        it('defaults hideSystemIndices to true and the rest to empty/null when no params are present', () => {
            expect(parseElasticIndicesFiltersFromParams(convertToParamMap({}))).toEqual({
                nameFilter: '',
                statusFilter: null,
                hideSystemIndices: true,
            });
        });

        it('falls back to a null statusFilter for an invalid status value', () => {
            const params = convertToParamMap({status: 'purple'});
            expect(parseElasticIndicesFiltersFromParams(params).statusFilter).toBeNull();
        });

    });


    describe('buildElasticIndicesQueryParams()', () => {

        it('includes all params when they differ from their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: 'audit', statusFilter: 'red', hideSystemIndices: false}))
                .toEqual({indexName: 'audit', status: 'red', hideSystemIndices: 'false'});
        });

        it('omits indexName, status, and hideSystemIndices when all filters are at their defaults', () => {
            expect(buildElasticIndicesQueryParams({nameFilter: '', statusFilter: null, hideSystemIndices: true}))
                .toEqual({indexName: null, status: null, hideSystemIndices: null});
        });

    });


});
```

(This drops the old "returns undefined for an existing index with no health status" test entirely — see the design doc's explanation of why: that scenario required `health` to be absent, which is no longer expressible now that the defensive code trusting the non-optional type has been removed. Do not try to preserve it with a cast; that would reintroduce exactly the kind of unsafe-cast fixture this step is fixing.)

- [ ] **Step 12: Run the build and tests**

Run, from `libs/maia-ui-workspace`:
```bash
npx ng build maia-elasticsearch
npx ng test maia-elasticsearch
```
Expected: build succeeds; all tests pass with no failures (there is no fixed "expected count" given in this plan since the exact current count wasn't re-verified after Step 11's one test removal — count the actual `it(...)` blocks in your rewritten file and confirm the test runner reports that same number passing).

- [ ] **Step 13: Do NOT commit**

Leave everything uncommitted, per explicit instruction. Do not run `git add` or `git commit`. Run `git status --short` and report the full output.

---

## Self-Review Notes

- **Spec coverage:** the deletions, every consumer's import swap, the two simplifications (`elastic-indices-filtering.ts`, `elastic-index.ts`/`.html`), and the test fixture fix all map to this single task's steps.
- **Placeholder scan:** none found — every step contains complete code.
- **Type consistency:** `IndexStateResponseDto`/`EsIndexHealthResponseDto`/`ManagedIndexInfoResponseDto`/`IndexBaseNameAndVersionResponseDto` names and the `@app/gen-components/org/maiaframework/elasticsearch/index/model/...` import path are used identically across every file touched.
- **User verification requirement scan:** no human sign-off was requested — NO. The user did give an explicit, binding process instruction (no worktree, no commits) carried over from Part 1, encoded in this task's final step.
- **A note on why this plan removes rather than preserves defensive code:** this was an explicit user correction during design — an earlier draft of this plan proposed keeping `?.`/`!!` "just in case" the generated type's optimism didn't match runtime reality. The user pointed out that frontend and backend share the identical non-nullable spec, so that caution was unfounded, and asked for the simplification instead. This is why Step 11 deletes a test rather than adapting it: the test existed specifically to exercise defensive code that no longer exists.

# AgGrid blotter dateTimeString value formatter

## Problem
AgGrid blotter columns backed by `InstantFieldType` render as `cellDataType: 'dateTimeString'` but show raw ISO strings — no formatting is applied. Other UI (mat-table blotters, detail views) already format Instant fields via `Pipes.INSTANT_PIPE` (`date: 'EEE MMM dd yyyy HH:mm'`). The AgGrid path has no equivalent.

A hand-edited prototype in the generated `left-to-right-complex-blotter.ts`/`.html` proved the fix: an AG Grid `dataTypeDefinitions` override for `dateTimeString`, bound on `<ag-grid-angular [dataTypeDefinitions]="dataTypeDefinitions">`. That prototype hit a separate TS compile bug (literal widening on `baseDataType`/`extendsDataType`) — already fixed and verified (`satisfies DataTypeDefinitions`). This spec is about making the generator emit this automatically so it survives regeneration.

## Scope
- Only `dateTimeString` columns (i.e. `InstantFieldType` fields). `dateString` (`LocalDate`) has no existing formatting convention elsewhere in the codebase — out of scope.
- Grid-wide default only. No new DSL surface — every AgGrid blotter with a dateTimeString column gets this automatically, matching the existing `Pipes.INSTANT_PIPE` format. No per-blotter/per-column override.

## Changes

**`BlotterDef.kt`**
- Add `val hasDateTimeStringColumn: Boolean` — true if any `BlotterColumnDef` has `agGridCellDateType == BlotterColumnDef.AgGridCellDataType.dateTimeString`.

**`AgGridBlotterComponentRenderer.kt`**
- When `hasDateTimeStringColumn`:
  - `addImport("ag-grid-community", "DataTypeDefinitions")`
  - Emit, before `columnDefs`:
    ```ts
    dataTypeDefinitions = {
        dateTimeString: {
            baseDataType: 'dateTimeString',
            extendsDataType: 'dateTimeString',
            valueFormatter: params => {
                if (!params.value) return '';
                const d = new Date(params.value);
                const days = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
                const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
                const pad = (n: number) => String(n).padStart(2, '0');
                return `${days[d.getDay()]} ${months[d.getMonth()]} ${pad(d.getDate())} ${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
            },
        }
    } satisfies DataTypeDefinitions;
    ```

**`AgGridBlotterHtmlRenderer.kt`**
- When `hasDateTimeStringColumn`, emit `[dataTypeDefinitions]="dataTypeDefinitions"` on `<ag-grid-angular>`.

## Verification
- Regenerate `maia-showcase` (`:maia-showcase-ui:maiaGeneration` or equivalent), confirm `left-to-right-complex-blotter.ts`/`.html` match the intent of the manual prototype (discard the hand-edit — generator now produces it).
- `ng build maia-showcase-ui` compiles clean.
- Blotters with no dateTimeString columns are unaffected (no `dataTypeDefinitions` field/binding emitted).

## Unresolved questions
None.

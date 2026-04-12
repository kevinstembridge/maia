# LeftEntity Create/Edit Forms — RightEntity Multi-Select Association

## Goal

Allow users to associate a LeftEntity with zero or more RightEntities directly from the Create and Edit dialogs, using a chips + typeahead control backed by an Elasticsearch index.

## Approach

Option A — full-stack, atomic: `rightEntityIds` travels with every create/update request. The backend reconciles the join table in a single operation. The UI uses a chips + typeahead multi-select powered by a generated `RightTypeaheadDef`.

---

## Backend Design

### 1. Spec — add `RightTypeaheadDef` to `MaiaShowcaseSpec.kt`

```kotlin
val rightTypeaheadDef = typeahead(
    "org.maiaframework.showcase.many_to_many",
    "Right",
    rightEntityDef,
    idFieldName = "rightId",
    sortFieldName = "someString",
    searchTermFieldName = "someString",
    indexVersion = 1
) {
    fieldFromEntity("id", "id", EsDocMappingTypes.keyword)
    fieldFromEntity("someString", "someString", EsDocMappingTypes.searchAsYouType)
}
```

Running `maiaGeneration` produces:
- `RightTypeaheadEsIndex.kt`, `RightTypeaheadService.kt`, `RightTypeaheadEndpoint.kt` (`GET /api/typeahead/right?q=<term>`)
- `RightTypeaheadV1EsDoc.ts`, `right-typeahead-api.service.ts`

### 2. DTOs

**`LeftCreateRequestDto.kt`** — add:
```kotlin
@field:NotNull
@param:JsonProperty("rightEntityIds", access = JsonProperty.Access.READ_WRITE)
private val rightEntityIds_raw: List<DomainId>?
```

**`LeftUpdateRequestDto.kt`** — same addition.

**`LeftFetchForEditDto.kt`** — add:
```kotlin
val rightEntities: List<RightPkAndNameDto>
```

### 3. `LeftFetchForEditDtoRowMapper` — mirror `LeftSearchableDtoRowMapper`

- Accept `JdbcOps` in the constructor (currently takes none)
- Add `private val rightEntitiesPkAndNameDtoRowMapper = RightPkAndNameDtoRowMapper()`
- In `mapRow()`: read `entityId`, run a sub-query joining `maia.right` through `maia.left_to_right_many_to_many_join` using `RightPkAndNameDtoRowMapper`, return fully assembled `LeftFetchForEditDto` with `rightEntities`

Sub-query SQL (same as in `LeftSearchableDtoRowMapper`):
```sql
select other.id, other.some_string
from maia.right other
join maia.left_to_right_many_to_many_join mtm on other.id = mtm.right_id
where mtm.left_id = :entityId
order by other.some_string
```

### 4. `LeftDao` — pass `jdbcOps` to row mapper

Change:
```kotlin
private val fetchForEditDtoRowMapper = LeftFetchForEditDtoRowMapper()
```
To:
```kotlin
private val fetchForEditDtoRowMapper = LeftFetchForEditDtoRowMapper(this.jdbcOps)
```

No other changes to `LeftDao` or `LeftRepo`.

### 5. `LeftCrudService`

**`create()`** — after inserting the entity, bulk-insert one `LeftToRightManyToManyJoinEntity` per `rightEntityId`:
```kotlin
val joins = createDto.rightEntityIds.map { rightId ->
    LeftToRightManyToManyJoinEntity(
        createdTimestampUtc = Instant.now(),
        id = DomainId.newId(),
        lastModifiedTimestampUtc = Instant.now(),
        leftId = entity.id,
        rightId = rightId
    )
}
leftToRightManyToManyJoinRepo.bulkInsert(joins)
```

**`update()`** — delete all existing join rows for the `leftId`, then bulk-insert the new set:
```kotlin
val existingJoins = leftToRightManyToManyJoinRepo.findByLeftId(editDto.id)
existingJoins.forEach { leftToRightManyToManyJoinRepo.deleteByPrimaryKey(it.id) }
val newJoins = editDto.rightEntityIds.map { rightId ->
    LeftToRightManyToManyJoinEntity(
        createdTimestampUtc = Instant.now(),
        id = DomainId.newId(),
        lastModifiedTimestampUtc = Instant.now(),
        leftId = editDto.id,
        rightId = rightId
    )
}
leftToRightManyToManyJoinRepo.bulkInsert(newJoins)
```

**`fetchForEdit()`** — unchanged; mapping now happens in the row mapper.

---

## Frontend Design

### 1. Generated TypeScript DTOs

**`LeftCreateRequestDto.ts`** — add `rightEntityIds: string[]`

**`LeftUpdateRequestDto.ts`** — add `rightEntityIds: string[]`

**`LeftFetchForEditDto.ts`** — add `rightEntities: RightPkAndNameDto[]`

### 2. Generated from `RightTypeaheadDef` (after `maiaGeneration`)

- `RightTypeaheadV1EsDoc.ts` — `{ id: string, someString: string }`
- `right-typeahead-api.service.ts` — `search(term): Observable<RightTypeaheadV1EsDoc[]>` calling `GET /api/typeahead/right`

### 3. Both dialog components — chips + typeahead multi-select

**Imports/providers:**
- Add `MatChipsModule` to `imports`
- Add `RightTypeaheadApiService` to `providers`

**Component state:**
- `selectedRightEntities: RightTypeaheadV1EsDoc[] = []` — the selected chips (source of truth)
- `filteredRightEntities: RightTypeaheadV1EsDoc[] = []` — typeahead suggestions
- `filteredRightEntitiesIsLoading = signal(false)`
- A `@ViewChild` ref to the typeahead text `<input>` for clearing after selection

**`ngOnInit` wiring (both dialogs):**
Wire the text input's `valueChanges` (or an `(input)` event handler) through:
```
debounceTime(300) → filter(string) → tap(clear+loading) → switchMap(search) → tap(loaded)
→ subscribe(res => filteredRightEntities = res)
```

**Edit dialog only — `ngOnInit` after `fetchForEdit`:**
```typescript
selectedRightEntities = dto.rightEntities.map(r => ({ id: r.id, someString: r.name }));
```

**On option selected:**
```typescript
selectedRightEntities.push(option);
rightEntityInput.nativeElement.value = '';
```
Prevent duplicates by checking `id` before pushing.

**On chip removed:**
```typescript
selectedRightEntities = selectedRightEntities.filter(e => e.id !== removed.id);
```

**On submit — map to IDs:**
```typescript
rightEntityIds: this.selectedRightEntities.map(e => e.id)
```

### 4. Template (both dialogs)

```html
<mat-form-field appearance="outline">
    <mat-label>Right Entities</mat-label>
    <mat-chip-grid #chipGrid>
        @for (entity of selectedRightEntities; track entity.id) {
            <mat-chip-row (removed)="removeRightEntity(entity)">
                {{ entity.someString }}
                <button matChipRemove><mat-icon>cancel</mat-icon></button>
            </mat-chip-row>
        }
    </mat-chip-grid>
    <input
        #rightEntityInput
        placeholder="Search Right Entities..."
        [matChipInputFor]="chipGrid"
        [matAutocomplete]="rightEntityAuto"
        (input)="onRightEntityInputChange($event)"
    />
    <mat-autocomplete #rightEntityAuto="matAutocomplete" (optionSelected)="addRightEntity($event)">
        @if (filteredRightEntitiesIsLoading()) {
            <mat-option>Loading...</mat-option>
        }
        @for (option of filteredRightEntities; track option.id) {
            <mat-option [value]="option">{{ option.someString }}</mat-option>
        }
    </mat-autocomplete>
</mat-form-field>
```

---

## Files Changed

### Backend
| File | Change |
|------|--------|
| `maia-showcase/spec/.../MaiaShowcaseSpec.kt` | Add `rightTypeaheadDef` |
| `domain/.../LeftCreateRequestDto.kt` | Add `rightEntityIds` |
| `domain/.../LeftUpdateRequestDto.kt` | Add `rightEntityIds` |
| `domain/.../LeftFetchForEditDto.kt` | Add `rightEntities` |
| `dao/.../LeftFetchForEditDtoRowMapper.kt` | Accept `JdbcOps`, use `RightPkAndNameDtoRowMapper` sub-query |
| `dao/.../LeftDao.kt` | Pass `jdbcOps` to `LeftFetchForEditDtoRowMapper` |
| `service/.../LeftCrudService.kt` | Sync join table on create/update |
| *(generated)* `elasticsearch/.../RightTypeahead*.kt` | New ES index + service |
| *(generated)* `web/.../RightTypeaheadEndpoint.kt` | New endpoint |

### Frontend
| File | Change |
|------|--------|
| `LeftCreateRequestDto.ts` | Add `rightEntityIds` |
| `LeftUpdateRequestDto.ts` | Add `rightEntityIds` |
| `LeftFetchForEditDto.ts` | Add `rightEntities` |
| *(generated)* `RightTypeaheadV1EsDoc.ts` | New |
| *(generated)* `right-typeahead-api.service.ts` | New |
| `left-create-dialog.component.ts` | Chips + typeahead wiring |
| `left-create-dialog.component.html` | Chips + typeahead template |
| `left-edit-dialog.component.ts` | Chips + typeahead wiring + pre-populate |
| `left-edit-dialog.component.html` | Chips + typeahead template |

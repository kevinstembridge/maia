package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.TypeaheadDef

/**
 * Renders a standalone Angular component that wraps a Material form field and autocomplete around
 * the typeahead's API service. Generated forms use it in place of a hand-rolled input, which keeps
 * the search pipeline, the loading state and the "No results found" message in one place.
 *
 * The component can be used in two ways:
 * - as a form control (`formControlName`, `[formControl]`, `[formField]`), where the control's value is the selected
 *   ES doc, or the raw search text while the user is still typing, or null.
 * - in `chips` mode, where the owner keeps the selected entities and passes them in with `[selected]`, and is told
 *   about changes through the `added` and `removed` outputs.
 */
class TypeaheadAngularComponentRenderer(private val typeaheadDef: TypeaheadDef) : AbstractTypescriptRenderer() {


    private val esDocUqcn = typeaheadDef.esDocDef.dtoDef.uqcn.value


    private val searchTermField = typeaheadDef.searchTermFieldName


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "ElementRef")
        addImport("@angular/core", "booleanAttribute")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "input")
        addImport("@angular/core", "output")
        addImport("@angular/core", "signal")
        addImport("@angular/core", "viewChild")
        addImport("@angular/core/rxjs-interop", "takeUntilDestroyed")
        addImport("@angular/forms", "ControlValueAccessor")
        addImport("@angular/forms", "FormControl")
        addImport("@angular/forms", "NgControl")
        addImport("@angular/forms", "ReactiveFormsModule")
        addImport("@angular/material/autocomplete", "MatAutocomplete")
        addImport("@angular/material/autocomplete", "MatAutocompleteSelectedEvent")
        addImport("@angular/material/autocomplete", "MatAutocompleteTrigger")
        addImport("@angular/material/chips", "MatChipsModule")
        addImport("@angular/material/core", "ErrorStateMatcher")
        addImport("@angular/material/core", "MatOption")
        addImport("@angular/material/form-field", "MatFormFieldModule")
        addImport("@angular/material/icon", "MatIconModule")
        addImport("@angular/material/input", "MatInputModule")
        addImport("rxjs", "of")
        addImport("rxjs/operators", "catchError")
        addImport("rxjs/operators", "debounceTime")
        addImport("rxjs/operators", "filter")
        addImport("rxjs/operators", "switchMap")
        addImport("rxjs/operators", "tap")
        addImport(typeaheadDef.typescriptServiceImport)
        addImport(typeaheadDef.esDocDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return typeaheadDef.typescriptComponentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |
            |@Component({
            |    imports: [
            |        MatAutocomplete,
            |        MatAutocompleteTrigger,
            |        MatChipsModule,
            |        MatFormFieldModule,
            |        MatIconModule,
            |        MatInputModule,
            |        MatOption,
            |        ReactiveFormsModule,
            |    ],
            |    providers: [
            |        ${typeaheadDef.angularServiceClassName},
            |    ],
            |    selector: '${typeaheadDef.angularComponentSelector}',
            |    // Lay out as if the form field were a direct child of the host's parent.
            |    styles: [':host { display: contents; }'],
            |    template: `
            |        <mat-form-field appearance="outline">
            |            <mat-label>{{ label() }}</mat-label>
            |            @if (chips()) {
            |                <mat-chip-grid #chipGrid>
            |                    @for (entity of selected(); track entity) {
            |                        <mat-chip-row (removed)="removed.emit(entity)">
            |                            {{ entity.$searchTermField }}
            |                            <button matChipRemove type="button"><mat-icon>cancel</mat-icon></button>
            |                        </mat-chip-row>
            |                    }
            |                </mat-chip-grid>
            |                <input
            |                    #chipInput
            |                    [placeholder]="placeholder()"
            |                    [formControl]="searchControl"
            |                    [matChipInputFor]="chipGrid"
            |                    [matAutocomplete]="auto"
            |                />
            |            } @else {
            |                <input
            |                    matInput
            |                    type="text"
            |                    [placeholder]="placeholder()"
            |                    [formControl]="searchControl"
            |                    [matAutocomplete]="auto"
            |                    [errorStateMatcher]="errorStateMatcher"
            |                    (blur)="onBlur()"
            |                />
            |            }
            |            <mat-autocomplete #auto="matAutocomplete" [displayWith]="displayWith" (optionSelected)="onOptionSelected(${'$'}event)">
            |                @if (isLoading()) {
            |                    <mat-option disabled>Loading...</mat-option>
            |                }
            |                @for (option of options(); track option) {
            |                    <mat-option [value]="option">{{ option.$searchTermField }}</mat-option>
            |                }
            |                @if (options().length === 0 && hasSearched()) {
            |                    <mat-option disabled>No results found</mat-option>
            |                }
            |            </mat-autocomplete>
            |            @if (ngControl?.errors?.['required']) {
            |                <mat-error>{{ label() }} is required.</mat-error>
            |            }
            |        </mat-form-field>
            |    `
            |})
            |export class ${typeaheadDef.angularComponentClassName} implements ControlValueAccessor {
            |
            |
            |    readonly label = input('');
            |
            |
            |    readonly placeholder = input('');
            |
            |
            |    /** Chip mode: show the selected entities as chips inside the field instead of acting as a form control. */
            |    readonly chips = input(false, { transform: booleanAttribute });
            |
            |
            |    /** Chip mode: the entities that are currently selected. */
            |    readonly selected = input<$esDocUqcn[]>([]);
            |
            |
            |    /** Chip mode: emitted when the user picks an entity to add. */
            |    readonly added = output<$esDocUqcn>();
            |
            |
            |    /** Chip mode: emitted when the user removes a chip. */
            |    readonly removed = output<$esDocUqcn>();
            |
            |
            |    /** Emitted when the user picks an entity from the dropdown. */
            |    readonly optionSelected = output<$esDocUqcn>();
            |
            |
            |    protected readonly searchControl = new FormControl<string | $esDocUqcn | null>(null);
            |
            |
            |    protected readonly options = signal<$esDocUqcn[]>([]);
            |
            |
            |    protected readonly isLoading = signal(false);
            |
            |
            |    protected readonly hasSearched = signal(false);
            |
            |
            |    protected readonly ngControl = inject(NgControl, { optional: true, self: true });
            |
            |
            |    private readonly chipInput = viewChild<ElementRef<HTMLInputElement>>('chipInput');
            |
            |
            |    private readonly apiService = inject(${typeaheadDef.angularServiceClassName});
            |
            |
            |    private onChange: (value: string | $esDocUqcn | null) => void = () => {};
            |
            |
            |    private onTouched: () => void = () => {};
            |
            |
            |    /** Shows the error state of the control this component is bound to, not that of the inner search control. */
            |    protected readonly errorStateMatcher: ErrorStateMatcher = {
            |        isErrorState: (_control, form) => {
            |            const control = this.ngControl?.control;
            |            return !!control && control.invalid && (control.touched || !!form?.submitted);
            |        }
            |    };
            |
            |
            |    protected readonly displayWith = (value: string | $esDocUqcn | null): string => {
            |        if (value === null || value === undefined) {
            |            return '';
            |        }
            |        return typeof value === 'string' ? value : (value.$searchTermField ?? '');
            |    };
            |
            |
            |    constructor() {
            |
            |        if (this.ngControl) {
            |            this.ngControl.valueAccessor = this;
            |        }
            |
            |        this.searchControl.valueChanges.pipe(
            |            tap(value => this.onChange(value)),
            |            debounceTime(300),
            |            filter((value): value is string => typeof value === 'string'),
            |            tap(() => {
            |                this.options.set([]);
            |                this.hasSearched.set(false);
            |                this.isLoading.set(true);
            |            }),
            |            switchMap(value => this.apiService.search(value).pipe(
            |                catchError(err => {
            |                    console.error(err);
            |                    return of([] as $esDocUqcn[]);
            |                })
            |            )),
            |            takeUntilDestroyed()
            |        ).subscribe(results => {
            |            this.options.set(results);
            |            this.isLoading.set(false);
            |            this.hasSearched.set(true);
            |        });
            |
            |    }
            |
            |
            |    writeValue(value: string | $esDocUqcn | null): void {
            |
            |        this.searchControl.setValue(value ?? null, { emitEvent: false });
            |
            |        if (value === null || value === undefined || value === '') {
            |            this.options.set([]);
            |            this.isLoading.set(false);
            |            this.hasSearched.set(false);
            |        }
            |
            |    }
            |
            |
            |    registerOnChange(fn: (value: string | $esDocUqcn | null) => void): void {
            |
            |        this.onChange = fn;
            |
            |    }
            |
            |
            |    registerOnTouched(fn: () => void): void {
            |
            |        this.onTouched = fn;
            |
            |    }
            |
            |
            |    setDisabledState(isDisabled: boolean): void {
            |
            |        if (isDisabled) {
            |            this.searchControl.disable({ emitEvent: false });
            |        } else {
            |            this.searchControl.enable({ emitEvent: false });
            |        }
            |
            |    }
            |
            |
            |    protected onBlur(): void {
            |
            |        this.onTouched();
            |
            |    }
            |
            |
            |    protected onOptionSelected(event: MatAutocompleteSelectedEvent): void {
            |
            |        const entity: $esDocUqcn = event.option.value;
            |
            |        if (this.chips()) {
            |
            |            this.added.emit(entity);
            |
            |            const input = this.chipInput();
            |            if (input) {
            |                input.nativeElement.value = '';
            |            }
            |
            |            this.searchControl.setValue('', { emitEvent: false });
            |
            |        } else {
            |
            |            this.optionSelected.emit(entity);
            |
            |        }
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}

package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.FormPurpose
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
import org.maiaframework.gen.spec.definition.lang.JoinFetchDtoFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.LocalDateFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.MapFieldType
import org.maiaframework.gen.spec.definition.lang.ObjectIdFieldType
import org.maiaframework.gen.spec.definition.lang.PeriodFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType
import org.maiaframework.gen.spec.definition.lang.RequestDtoFieldType
import org.maiaframework.gen.spec.definition.lang.SetFieldType
import org.maiaframework.gen.spec.definition.lang.SimpleResponseDtoFieldType
import org.maiaframework.gen.spec.definition.lang.StringFieldType
import org.maiaframework.gen.spec.definition.lang.StringTypeFieldType
import org.maiaframework.gen.spec.definition.lang.StringValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.UrlFieldType
import org.maiaframework.lang.text.StringFunctions


class AngularReactiveFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    formAngularComponentNames: AngularComponentNames,
    providerServices: List<String>,
    private val chipFields: List<ManyToManyChipFieldDef>,
    private val timestampedFields: List<ManyToManyTimestampedFieldDef> = emptyList()
) : AbstractAngularComponentRenderer(
    formAngularComponentNames,
    providerServices
) {


    private val formGroupFields = this.angularFormDef.formModelFields


    private val requestDtoDef = this.angularFormDef.requestDtoDef


    private val allRequestDtoFields = this.requestDtoDef.dtoFieldDefs


    private val typeaheadDefs = this.angularFormDef.allTypeaheadDefs

    private val renderedServiceFieldNames = mutableSetOf<String>()

    init {

        `add imports`()

    }


    override fun renderComponentSource() {

        appendLine("export class $className implements OnInit {")

        `render class fields`()

        `render constructor`()

        `render function ngOnInit`()

        `render TypeaheadResultFormatters`()

        `render chip entity methods`()

        `render timestamped join methods`()

        `render function onSubmit`()

        `render function onCancel`()

        append("""
            |
            |
            |}
            |""".trimMargin())

    }


    private fun `render class fields`() {

        `render class field for entityId`()

        `render class field for delegated form submission output signal`()

        `render class field for onSuccess output signal`()

        `render class field for onCancel output signal`()

        `render class field for onError output signal`()

        `render class field for formService`()

        `render class field for problemDetail`()

        `render class fields for async validators`()

        `render class fields for typeahead fields`()

        `render class fields for typeahead services`()

        `render class fields for enum MatSelect fields`()

        `render class fields for chip fields`()

        `render class fields for timestamped fields`()

        `render class field for formGroup `()

        `render class field for loading signal if fetchForEdit form`()

        `render class field for linked fields`()

        `render class field for dialogRef`()

        `render class field for form context`()

        `render class field for router`()

        `render class fields for multi-field unique indexes`()

    }


    private fun `render class field for entityId`() {

        if (angularFormDef.formPurpose == FormPurpose.edit) {

            addImport("@angular/core", "input")

            append("""
                |
                |
                |    entityId = input.required<${angularFormDef.entityIdInjectType}>();
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for delegated form submission output signal`() {

        if (this.angularFormDef.delegateFormSubmission.value) {

            append("""
                |
                |
                |    readonly onFormSubmission = output<${this.angularFormDef.requestDtoDef.uqcn}>();
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for onSuccess output signal`() {

        if (this.angularFormDef.emitEventOnSuccess.value) {

            append("""
                |
                |
                |    readonly onSuccess = output();
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for onCancel output signal`() {

        append("""
            |
            |
            |    onCancel = output();
            |""".trimMargin()
        )

    }


    private fun `render class field for onError output signal`() {

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |
                |
                |    readonly onErrorEvent = output<any>();
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for formService`() {

        append("""
            |
            |
            |    private readonly formService = inject(${this.angularFormDef.formServiceClassName});
            |""".trimMargin()
        )

    }


    private fun `render class field for problemDetail`() {

        append("""
            |
            |
            |    problemDetail = signal<ProblemDetail | null>(null);
            |""".trimMargin()
        )

    }


    private fun `render class fields for async validators`() {

        this.formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->

            append("""
                |
                |
                |    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});
                |""".trimMargin()
            )

        }

    }


    private fun `render class fields for typeahead fields`() {

        this.typeaheadDefs.forEach { typeaheadDef ->

            val esDocUqcn = typeaheadDef.esDocDef.dtoDef.uqcn.value

            append("""
                |
                |
                |    filtered${typeaheadDef.typeaheadName}: ${esDocUqcn}[] = [];
                |
                |
                |    filtered${typeaheadDef.typeaheadName}IsLoading = signal(false);
                |""".trimMargin()
            )

        }

    }


    private fun `render class fields for typeahead services`() {

        this.typeaheadDefs.forEach { typeaheadDef ->

            val serviceFieldName = StringFunctions.firstToLower(typeaheadDef.angularServiceClassName)

            append("""
                |
                |
                |
                |    private readonly $serviceFieldName = inject(${typeaheadDef.angularServiceClassName});
                |""".trimMargin()
            )

        }

    }


    private fun `render class fields for enum MatSelect fields`() {

        val enumFields = formGroupFields
            .asSequence()
            .filter { it.isCreatable }
            .filter { it.fieldType is EnumFieldType }
            .map { it.fieldType as EnumFieldType }
            .map { it.enumDef }

        val listOfEnumFields = formGroupFields
            .asSequence()
            .filter { it.isCreatable }
            .filter { it.fieldType is ListFieldType }
            .map { it.fieldType as ListFieldType }
            .filter { it.parameterFieldType is EnumFieldType }
            .map { it.parameterFieldType as EnumFieldType }
            .map { it.enumDef }

        enumFields
            .plus(listOfEnumFields)
            .distinctBy { it.selectOptionsUqcn }
            .forEach { enumDef ->

                addImport(enumDef.selectOptionsTypescriptImport)

                append("""
                    |
                    |
                    |    protected readonly ${enumDef.selectOptionsUqcn} = ${enumDef.selectOptionsUqcn};
                    |""".trimMargin()
                )

            }

        formGroupFields
            .asSequence()
            .filter { it.isCreatable }
            .mapNotNull { it.stringValueClassListEnumOptionsDef }
            .distinctBy { it.selectOptionsUqcn }
            .forEach { enumOptionsDef ->

                addImport(enumOptionsDef.selectOptionsTypescriptImport)

                append("""
                    |
                    |
                    |    protected readonly ${enumOptionsDef.selectOptionsUqcn} = ${enumOptionsDef.selectOptionsUqcn};
                    |""".trimMargin()
                )

            }

    }


    private fun `render class fields for chip fields`() {

        chipFields.forEach { chip ->
            append("""
                |
                |
                |    ${chip.selectedFieldName}: ${chip.esDocClassName}[] = [];
                |
                |
                |    ${chip.filteredFieldName}: ${chip.esDocClassName}[] = [];
                |
                |
                |    ${chip.filteredIsLoadingFieldName} = signal(false);
                |
                |
                |    ${chip.searchControlFieldName} = new FormControl('');
                |""".trimMargin()
            )

            if (renderedServiceFieldNames.add(chip.serviceFieldName)) {
                append("""
                    |
                    |
                    |    ${chip.serviceFieldName} = inject(${chip.serviceClassName});
                    |""".trimMargin()
                )
            }

            append("""
                |
                |
                |    @ViewChild('${chip.inputRefName}') ${chip.inputRefName}!: ElementRef<HTMLInputElement>;
                |""".trimMargin()
            )
        }

    }


    private fun `render class fields for timestamped fields`() {

        timestampedFields.forEach { field ->

            addImport(field.esDocImport)
            addImport(field.serviceImport)
            addImport(field.joinRequestDtoTypescriptImport)

            append("""
                |
                |
                |    ${field.joinsFieldName}: {
                |        id: string | null;
                |        entityId: string;
                |        entityName: string;
                |        effectiveFrom: Date | null;
                |        effectiveTo: Date | null;
                |    }[] = [];
                |
                |
                |    ${field.showFormSignalName} = signal(false);
                |
                |
                |    ${field.addEntityControlName} = new FormControl<${field.esDocClassName} | null>(null);
                |
                |
                |    ${field.effectiveFromControlName} = new FormControl<Date | null>(null);
                |
                |
                |    ${field.effectiveToControlName} = new FormControl<Date | null>(null);
                |
                |
                |    ${field.filteredFieldName}: ${field.esDocClassName}[] = [];
                |
                |
                |    ${field.filteredIsLoadingFieldName} = signal(false);
                |""".trimMargin())

            if (renderedServiceFieldNames.add(field.serviceFieldName)) {
                append("""
                    |
                    |
                    |    ${field.serviceFieldName} = inject(${field.serviceClassName});
                    |""".trimMargin())
            }

        }

    }


    private fun `render class field for formGroup `() {

        append("""
            |
            |
            |    formGroup: FormGroup;
            |""".trimMargin()
        )

    }


    private fun `render class field for loading signal if fetchForEdit form`() {

        this.angularFormDef.fetchForEditDtoDef?.let {

            append("""
                |
                |
                |    loading = signal(true);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for linked fields`() {

        this.angularFormDef.formModelFields.filter { it.linksToAField }.forEach { fieldDef ->

            append("""
                |
                |
                |    ${fieldDef.classFieldDef.classFieldName}IsVisible = signal<boolean>(false);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for dialogRef`() {

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {

            append("""
                |
                |
                |
                |    readonly dialogRef = inject(MatDialogRef<${this.angularFormDef.componentNames.componentName}>);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for form context`() {

        this.angularFormDef.context?.let { context ->

            append("""
                |
                |
                |
                |    private readonly context = inject<${context.uqcn}>(MAT_DIALOG_DATA);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field for router`() {

        if (`the form requires a Router`()) {

            addImport("@angular/router", "Router")

            append("""
                |
                |
                |    private readonly router = inject(Router);
                |""".trimMargin()
            )

        }

    }


    private fun `the form requires a Router`(): Boolean {

        // TODO consider other scenarios where the form requires a router, e.g. when the form is used in a dialog
        return this.angularFormDef.onSuccessUrl != null

    }


    private fun `render class fields for multi-field unique indexes`() {

        this.angularFormDef.multiFieldUniqueIndexDefs.forEach { databaseIndexDef ->

            append("""
                |
                |
                |
                |    private readonly ${databaseIndexDef.validatorFieldName} = inject(${databaseIndexDef.validatorName});
                |""".trimMargin()
            )

        }

    }


    private fun `render constructor`() {

        append("""
            |
            |
            |    constructor() {
            |
            |        this.formGroup = new FormGroup(
            |            {
            |""".trimMargin()
        )

        this.formGroupFields.forEach { angularFormFieldDef ->

            FormControlRendererHelper.renderFormControlFor(
                angularFormFieldDef,
                angularFormDef.formPurpose,
                indentSize = 16,
                { line -> appendLine(line) },
                { fieldType -> addImportsFor(fieldType) }
            )
        }

        appendLine("            },")
        appendLine("        );")
        blankLine()
        appendLine("    }")

    }


    private fun `render function ngOnInit`() {

        append("""
            |
            |
            |    ngOnInit() {
            |""".trimMargin())

        this.allRequestDtoFields.filter { it.classFieldDef.typeaheadDef != null }.forEach { requestDtoFieldDef ->

            addImport("rxjs", "of")
            addImport("rxjs/operators", "catchError")
            addImport("rxjs/operators", "debounceTime")
            addImport("rxjs/operators", "filter")
            addImport("rxjs/operators", "map")
            addImport("rxjs/operators", "switchMap")
            addImport("rxjs/operators", "tap")

            val typeaheadDef = requestDtoFieldDef.classFieldDef.typeaheadDef!!
            val filteredFieldName = "filtered${typeaheadDef.typeaheadName.firstToUpper()}"
            val filteredFieldNameIsLoading = "${filteredFieldName}IsLoading"

            append("""
                |
                |        this.formGroup.controls['${typeaheadDef.typeaheadName.firstToLower()}'].valueChanges
                |            .pipe(
                |                debounceTime(300),
                |                filter(value => typeof value === 'string'),
                |                tap(() => {
                |                    this.$filteredFieldName = [];
                |                    this.$filteredFieldNameIsLoading.set(true);
                |                }),
                |                switchMap(value => this.${StringFunctions.firstToLower(typeaheadDef.angularServiceClassName)}.search(value)
                |                    .pipe(
                |                        catchError(err => {
                |                            this.${filteredFieldNameIsLoading}.set(false);
                |                            console.error(err);
                |                            return of([]);
                |                        })
                |                    )
                |                ),
                |                tap(() => this.${filteredFieldNameIsLoading}.set(false))
                |            ).subscribe(res => {
                |                this.${filteredFieldName} = res;
                |            });
                |""".trimMargin())

        }

        chipFields.forEach { chip ->

            addImport("rxjs", "of")
            addImport("rxjs/operators", "catchError")
            addImport("rxjs/operators", "debounceTime")
            addImport("rxjs/operators", "distinctUntilChanged")
            addImport("rxjs/operators", "filter")
            addImport("rxjs/operators", "map")
            addImport("rxjs/operators", "switchMap")
            addImport("rxjs/operators", "tap")

            append("""
                |
                |        this.${chip.searchControlFieldName}.valueChanges.pipe(
                |            debounceTime(300),
                |            distinctUntilChanged(),
                |            filter(value => typeof value === 'string'),
                |            tap(() => {
                |                this.${chip.filteredFieldName} = [];
                |                this.${chip.filteredIsLoadingFieldName}.set(true);
                |            }),
                |            switchMap(value => this.${chip.serviceFieldName}.search(value ?? '').pipe(
                |                catchError(err => {
                |                    this.${chip.filteredIsLoadingFieldName}.set(false);
                |                    console.error(err);
                |                    return of([]);
                |                })
                |            )),
                |            tap(() => this.${chip.filteredIsLoadingFieldName}.set(false))
                |        ).subscribe(res => {
                |            this.${chip.filteredFieldName} = res;
                |        });
                |""".trimMargin())

        }

        timestampedFields.forEach { field ->

            addImport("rxjs", "of")
            addImport("rxjs/operators", "catchError")
            addImport("rxjs/operators", "debounceTime")
            addImport("rxjs/operators", "distinctUntilChanged")
            addImport("rxjs/operators", "filter")
            addImport("rxjs/operators", "switchMap")
            addImport("rxjs/operators", "tap")

            append("""
                |
                |        this.${field.addEntityControlName}.valueChanges.pipe(
                |            debounceTime(300),
                |            distinctUntilChanged(),
                |            filter(value => typeof value === 'string'),
                |            tap(() => {
                |                this.${field.filteredFieldName} = [];
                |                this.${field.filteredIsLoadingFieldName}.set(true);
                |            }),
                |            switchMap(value => this.${field.serviceFieldName}.search(value ?? '').pipe(
                |                catchError(err => {
                |                    this.${field.filteredIsLoadingFieldName}.set(false);
                |                    console.error(err);
                |                    return of([]);
                |                })
                |            )),
                |            tap(() => this.${field.filteredIsLoadingFieldName}.set(false))
                |        ).subscribe(res => {
                |            this.${field.filteredFieldName} = res;
                |        });
                |""".trimMargin())

        }

        if (this.angularFormDef.formModelFields.any { it.linksToAField }) {

            blankLine()
            appendLine("        this.formGroup.valueChanges.subscribe(form => {")

            this.angularFormDef.formModelFields.filter { it.linksToAField }.forEach { formFieldDef ->

                val fieldName = formFieldDef.fieldName
                val fieldLinkedTo = formFieldDef.classFieldDef.fieldLinkedTo!!

                append("""
                    |
                    |            const ${fieldName}IsSelected = form.${formFieldDef.classFieldDef.fieldLinkedTo!!.classFieldName} === ${fieldLinkedTo.fieldType}.OTHER;
                    |
                    |            if (${fieldName}IsSelected) {
                    |                this.formGroup.get('$fieldName').enable();
                    |            } else {
                    |                this.formGroup.get('$fieldName').disable();
                    |            }
                    |
                    |            this.${fieldName}IsVisible.set(${fieldName}IsSelected);
                    |
                    |""".trimMargin())

            }

            appendLine("        });")

        }

        this.angularFormDef.fetchForEditDtoDef?.let { fetchForEditDtoDef ->

            addImport(fetchForEditDtoDef.typescriptImport)

            blankLine()
            appendLine("        this.formService.fetchForEdit(this.entityId()).subscribe({")
            appendLine("            next: (dto: ${fetchForEditDtoDef.uqcn}) => {")
            appendLine("                this.formGroup.patchValue({")

            this.formGroupFields.sortedBy { it.fieldName }.forEach { angularFormFieldDef ->
                val classFieldDef = angularFormFieldDef.classFieldDef
                val formFieldName = angularFormFieldDef.fieldName
                val typeaheadDef = classFieldDef.typeaheadDef

                if (typeaheadDef != null) {
                    val formControlName = typeaheadDef.typeaheadName.firstToLower()
                    appendLine("                    ${formFieldName}: dto.${formFieldName},")
                } else if (classFieldDef.fieldType is ForeignKeyFieldType) {
                    // Skip non-typeahead FK fields — DTO uses a PkAndName object shape
                } else {
                    val fieldName = classFieldDef.classFieldName
                    appendLine("                    ${formFieldName}: dto.${formFieldName},")
                }
            }

            appendLine("                });")

            chipFields.forEach { chip ->
                appendLine("                this.${chip.selectedFieldName} = dto.${chip.fetchForEditDtoFieldName}.map(r => ({")
                appendLine("                    ${chip.esDocIdFieldName}: r.${chip.esDocIdFieldName},")
                appendLine("                    ${chip.searchTermFieldName}: r.name,")
                appendLine("                }));")
            }

            timestampedFields.forEach { field ->
                appendLine("                this.${field.joinsFieldName} = dto.${field.fetchForEditDtoFieldName}?.map(e => ({")
                appendLine("                    id: e.id,")
                appendLine("                    entityId: e.entityId,")
                appendLine("                    entityName: e.name,")
                appendLine("                    effectiveFrom: e.effectiveFrom ? new Date(e.effectiveFrom) : null,")
                appendLine("                    effectiveTo: e.effectiveTo ? new Date(e.effectiveTo) : null,")
                appendLine("                })) ?? [];")
            }

            appendLine("                this.loading.set(false);")
            appendLine("            },")
            appendLine("            error: (err) => {")
            appendLine("                this.problemDetail.set(err.error);")
            appendLine("                this.loading.set(false);")
            appendLine("            }")
            appendLine("        });")

        }

        blankLine()
        appendLine("    }")

    }


    private fun `render TypeaheadResultFormatters`() {

        this.allRequestDtoFields.filter { it.classFieldDef.typeaheadDef != null }.forEach { requestDtoFieldDef ->

            val typeaheadDef = requestDtoFieldDef.classFieldDef.typeaheadDef!!

            blankLine()
            blankLine()
            appendLine("    ${typeaheadDef.typeaheadName.firstToLower()}ResultFormatter = (result: any) => result.${typeaheadDef.searchTermFieldName};")

        }

    }


    private fun `render chip entity methods`() {

        chipFields.forEach { chip ->

            blankLine()
            blankLine()
            append("""
                |    ${chip.addMethodName}(event: MatAutocompleteSelectedEvent): void {
                |
                |        const entity: ${chip.esDocClassName} = event.option.value;
                |        if (!this.${chip.selectedFieldName}.some(e => e.${chip.esDocIdFieldName} === entity.${chip.esDocIdFieldName})) {
                |            this.${chip.selectedFieldName}.push(entity);
                |        }
                |        this.${chip.inputRefName}.nativeElement.value = '';
                |        this.${chip.searchControlFieldName}.setValue('', { emitEvent: false });
                |
                |    }
                |
                |
                |    ${chip.removeMethodName}(entity: ${chip.esDocClassName}): void {
                |
                |        this.${chip.selectedFieldName} = this.${chip.selectedFieldName}.filter(e => e.${chip.esDocIdFieldName} !== entity.${chip.esDocIdFieldName});
                |
                |    }
                |""".trimMargin())

        }

    }


    private fun `render timestamped join methods`() {

        timestampedFields.forEach { field ->

            append("""
                |
                |
                |    ${field.confirmMethodName}(): void {
                |
                |        const entity = this.${field.addEntityControlName}.value;
                |        if (!entity) return;
                |        if (this.${field.joinsFieldName}.some(j => j.entityId === entity.${field.esDocIdFieldName})) return;
                |        this.${field.joinsFieldName}.push({
                |            id: null,
                |            entityId: entity.${field.esDocIdFieldName},
                |            entityName: entity.${field.searchTermFieldName},
                |            effectiveFrom: this.${field.effectiveFromControlName}.value,
                |            effectiveTo: this.${field.effectiveToControlName}.value,
                |        });
                |        this.${field.addEntityControlName}.reset();
                |        this.${field.effectiveFromControlName}.reset();
                |        this.${field.effectiveToControlName}.reset();
                |        this.${field.filteredFieldName} = [];
                |        this.${field.showFormSignalName}.set(false);
                |
                |    }
                |
                |
                |    ${field.removeMethodName}(index: number): void {
                |
                |        this.${field.joinsFieldName}.splice(index, 1);
                |
                |    }
                |
                |
                |    ${field.cancelMethodName}(): void {
                |
                |        this.${field.addEntityControlName}.reset();
                |        this.${field.effectiveFromControlName}.reset();
                |        this.${field.effectiveToControlName}.reset();
                |        this.${field.filteredFieldName} = [];
                |        this.${field.showFormSignalName}.set(false);
                |
                |    }
                |
                |
                |    ${field.displayWithMethodName} = (entity: ${field.esDocClassName} | null): string => {
                |        return entity ? entity.${field.searchTermFieldName} : '';
                |    };
                |""".trimMargin())

        }

    }


    private fun `render function onSubmit`() {

        append("""
            |
            |
            |    onSubmit() {
            |
            |        this.problemDetail.set(null);
            |
            |        if (this.formGroup.invalid) {
            |            return;
            |        }
            |""".trimMargin()
        )

        `render requestDto construction`()

        blankLine()

        if (this.angularFormDef.delegateFormSubmission.value) {

            appendLine("        this.onFormSubmission.emit(requestDto);")

        } else {

            `render formService request`()

        }

        blankLine()
        appendLine("    }")

    }


    private fun `render requestDto construction`() {

        blankLine()
        appendLine("        const requestDto = {")

        this.angularFormDef.context?.let {
            appendLine("            context: this.context,")
        }

        val chipFieldsByDtoName = chipFields.associateBy { it.requestDtoFieldName }
        val timestampedFieldsByDtoName = timestampedFields.associateBy { it.requestDtoFieldName }

        this.allRequestDtoFields.forEach { requestDtoFieldDef ->

            val dtoFieldName = requestDtoFieldDef.classFieldDef.classFieldName
            val typeaheadDef = requestDtoFieldDef.classFieldDef.typeaheadDef
            val chipField = chipFieldsByDtoName[dtoFieldName.value]
            val timestampedField = timestampedFieldsByDtoName[dtoFieldName.value]

            if (chipField != null) {

                appendLine("            ${dtoFieldName}: this.${chipField.selectedFieldName}.map(e => e.${chipField.esDocIdFieldName}),")

            } else if (timestampedField != null) {

                // handled below by timestampedFields loop

            } else if (typeaheadDef == null) {

                if (dtoFieldName == ClassFieldName.context) {

                    if (this.angularFormDef.context == null) {
                        appendLine("            context: this.context,")
                    }

                } else {
                    appendLine("            ${dtoFieldName}: this.formGroup.getRawValue().$dtoFieldName,")
                }

            } else {

                val formGroupFieldName = typeaheadDef.typeaheadName.firstToLower()
                appendLine("            ${dtoFieldName}: this.formGroup.getRawValue().${formGroupFieldName}.${typeaheadDef.esDocIdFieldName},")

            }

        }

        timestampedFields.forEach { field ->
            appendLine("            ${field.requestDtoFieldName}: this.${field.joinsFieldName}.map(j => ({")
            appendLine("                id: j.id,")
            appendLine("                ${field.joinEntityIdFieldName}: j.entityId,")
            appendLine("                effectiveFrom: j.effectiveFrom?.toISOString() ?? null,")
            appendLine("                effectiveTo: j.effectiveTo?.toISOString() ?? null,")
            appendLine("            })),")
        }

        appendLine("        } as ${this.angularFormDef.requestDtoDef.uqcn};")

    }


    private fun `render formService request`() {

        append("""
            |        this.formService.${angularFormDef.onSubmitServiceFunctionName}(requestDto).subscribe({
            |""".trimMargin()
        )

        `render formService request next function`()
        `render formService request error function`()

        appendLine("        });")

    }


    private fun `render formService request next function`() {

        if (this.angularFormDef.onSuccessUrl.isNullOrEmpty() == false) {

            when (this.angularFormDef.formPurpose) {
                FormPurpose.create -> {

                    append("""
                        |            next: (dto: EntityCreatedResponseDto) => {
                        |                this.router.navigate(['${this.angularFormDef.onSuccessUrl}', dto.id]);
                        |            },
                        |""".trimMargin())

                }
                FormPurpose.edit -> {

                    append("""
                        |            next: () => {
                        |                this.router.navigate(['${this.angularFormDef.onSuccessUrl}', this.entityId()]);
                        |            },
                        |""".trimMargin())

                }
                FormPurpose.submit -> {

                    append("""
                        |            next: () => {
                        |                this.router.navigate(['${this.angularFormDef.onSuccessUrl}']);
                        |            },
                        |""".trimMargin())

                }
            }

        } else {

            when (this.angularFormDef.inlineFormOrDialog) {

                InlineFormOrDialog.INLINE_FORM -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |            next: (dto: EntityCreatedResponseDto) => {
                            |                this.onSuccess.emit(dto);
                            |            },
                            |""".trimMargin())

                    } else {

                        append("""
                            |            next: () => {
                            |                // TODO maybe emit an event?
                            |            },
                            |""".trimMargin())

                    }

                }

                InlineFormOrDialog.DIALOG -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |            next: (dto: EntityCreatedResponseDto) => {
                            |                this.onSuccess.emit();
                            |            },
                            |""".trimMargin())

                    } else {

                        append("""
                            |            next: () => {
                            |                this.dialogRef.close(true);
                            |            },
                            |""".trimMargin())

                    }

                }

            }

        }

    }


    private fun `render formService request error function`() {

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |            error: err => {
                |                this.onError.emit(err);
                |            }
                |""".trimMargin())

        } else {

            append("""
                |            error: err => {
                |                this.problemDetail.set(err.error);
                |            }
                |""".trimMargin())

        }

    }


    private fun `render function onCancel`() {

//        if (this.angularFormDef.inlineFormOrDialog != InlineFormOrDialog.DIALOG) {
//            return
//        }

        append("""
            |
            |
            |    onCancelClicked(): void {
            |        this.onCancel.emit();
            |    }
            |""".trimMargin()
        )

    }


    private fun `add imports`() {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "OnInit")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "output")
        addImport("@angular/core", "signal")
        addImport("@angular/forms", "FormControl")
        addImport("@angular/forms", "FormGroup")
        addImport("@angular/forms", "Validators")
        addImport("@angular/forms", "FormsModule", isModule = true)
        addImport("@angular/forms", "ReactiveFormsModule", isModule = true)
        addImport("@angular/material/button", "MatButtonModule", isModule = true)
        addImport("@angular/material/form-field", "MatFormFieldModule", isModule = true)
        addImport("@angular/material/input", "MatInputModule", isModule = true)

        if (this.angularFormDef.fetchForEditDtoDef != null) {
            addImport("@angular/material/progress-spinner", "MatProgressSpinnerModule", isModule = true)
        }

        when (this.angularFormDef.formPurpose) {
            FormPurpose.create -> {
                addImport("@maia/maia-ui", "EntityCreatedResponseDto")
            }
            FormPurpose.edit -> {}
            FormPurpose.submit -> {}
            null -> {}
        }

        addImport(TypescriptImports.problemDetail)
        addImport(requestDtoDef.typescriptImport)
        addImport(angularFormDef.formServiceTypescriptImport)
        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
        }

        addImportsForFieldTypes(formGroupFields)

        this.typeaheadDefs.forEach { typeaheadDef ->
            addImport(typeaheadDef.typescriptServiceImport)
            addImport(typeaheadDef.esDocDef.dtoDef.typescriptDtoImport)
        }

        this.angularFormDef.formModelFields.forEach { angularFieldDef ->
            angularFieldDef.typeaheadRequiredValidatorTypescriptImport?.let { addImport(it) }
        }

        if (chipFields.isNotEmpty()) {
            addImport("@angular/core", "ElementRef")
            addImport("@angular/core", "ViewChild")
            addImport("@angular/material/autocomplete", "MatAutocomplete", isModule = true)
            addImport("@angular/material/autocomplete", "MatAutocompleteSelectedEvent")
            addImport("@angular/material/autocomplete", "MatAutocompleteTrigger", isModule = true)
            addImport("@angular/material/autocomplete", "MatOption", isModule = true)
            addImport("@angular/material/chips", "MatChipsModule", isModule = true)
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
            chipFields.forEach { chip ->
                addImport(chip.serviceImport)
                addImport(chip.esDocImport)
            }
        }

        if (timestampedFields.isNotEmpty()) {
            `add imports for date and time pickers`()
            addImport("@angular/common", "DatePipe", isModule = true)
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
            addImport("@angular/material/autocomplete", "MatAutocomplete", isModule = true)
            addImport("@angular/material/autocomplete", "MatAutocompleteTrigger", isModule = true)
            addImport("@angular/material/autocomplete", "MatOption", isModule = true)
        }

    }


    private fun addImportsForFieldTypes(formGroupFields: List<AngularFormFieldDef>) {

        formGroupFields.filter { it.isCreatable }.forEach { angularFormFieldDef ->

            when (val fieldType = angularFormFieldDef.fieldType) {
                is BooleanFieldType -> `add import for Material Checkbox`()
                is BooleanTypeFieldType -> `add import for Material Checkbox`()
                is BooleanValueClassFieldType -> `add import for Material Checkbox`()
                is DataClassFieldType -> {}
                is DomainIdFieldType -> {}
                is DoubleFieldType -> {}
                is EnumFieldType -> `add imports for Material Select component`(fieldType)
                is EsDocFieldType -> {}
                is ForeignKeyFieldType -> `add imports for Material Autocomplete`(fieldType)
                is FqcnFieldType -> {}
                is JoinFetchDtoFieldType -> TODO("YAGNI?")
                is InstantFieldType -> `add imports for date and time pickers`()
                is IntFieldType -> {}
                is IntTypeFieldType -> {}
                is IntValueClassFieldType -> {}
                is ListFieldType -> {
                    `add imports for ListFieldType`(fieldType)
                    angularFormFieldDef.stringValueClassListEnumOptionsDef?.let {
                        addImport("@angular/material/select", "MatSelect", isModule = true)
                        addImport("@angular/material/select", "MatOption", isModule = true)
                        addImport("@angular/material/tooltip", "MatTooltip", isModule = true)
                    }
                }
                is LocalDateFieldType -> {}
                is LongFieldType -> {}
                is LongTypeFieldType -> {}
                is MapFieldType -> {}
                is ObjectIdFieldType -> {}
                is PeriodFieldType -> {}
                is PkAndNameFieldType -> {}
                is RequestDtoFieldType -> {}
                is SetFieldType -> {}
                is SimpleResponseDtoFieldType -> {}
                is StringFieldType -> {}
                is StringTypeFieldType -> {}
                is StringValueClassFieldType -> {}
                is UrlFieldType -> {}
            }

        }

    }


    private fun `add imports for ListFieldType`(fieldType: ListFieldType) {

        when (val parameterFieldType = fieldType.parameterFieldType) {

            is EnumFieldType -> {

                parameterFieldType.enumDef.selectOptionsTypescriptImport.let {
                    addImport(it)
                }

                addImport("@angular/material/select", "MatSelect", isModule = true)
                addImport("@angular/material/select", "MatOption", isModule = true)
                addImport("@angular/material/tooltip", "MatTooltip", isModule = true)

            }

            else -> {}
        }

    }


    private fun `add imports for Material Select component`(fieldType: EnumFieldType) {

        addImport("@angular/material/select", "MatSelect", isModule = true)
        addImport("@angular/material/select", "MatOption", isModule = true)
        addImport("@angular/material/tooltip", "MatTooltip", isModule = true)

        addImport(fieldType.enumDef.typescriptImport)

        fieldType.enumDef.selectOptionsTypescriptImport.let {
            addImport(it)
        }

    }


    private fun `add imports for Material Autocomplete`(fieldType: ForeignKeyFieldType) {

        addImport("@angular/material/autocomplete", "MatAutocomplete", isModule = true)
        addImport("@angular/material/autocomplete", "MatAutocompleteTrigger", isModule = true)
        addImport("@angular/material/autocomplete", "MatOption", isModule = true)

    }


    private fun `add imports for date and time pickers`() {

        addImport("@angular/material/datepicker", "MatDatepicker", isModule = true)
        addImport("@angular/material/datepicker", "MatDatepickerInput", isModule = true)
        addImport("@angular/material/datepicker", "MatDatepickerToggle", isModule = true)
        addImport("@angular/material/timepicker", "MatTimepicker", isModule = true)
        addImport("@angular/material/timepicker", "MatTimepickerInput", isModule = true)
        addImport("@angular/material/timepicker", "MatTimepickerToggle", isModule = true)

    }


    private fun `add import for Material Checkbox`() {

        addImport("@angular/material/checkbox", "MatCheckbox", isModule = true)

    }


}

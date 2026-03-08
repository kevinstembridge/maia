package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef
import org.maiaframework.lang.text.StringFunctions


class EntityReactiveFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    angularComponentNames: AngularComponentNames
) : AbstractAngularComponentRenderer(angularComponentNames) {


    private val formGroupFields = this.angularFormDef.formModelFields


    private val requestDtoDef = this.angularFormDef.requestDtoDef


    private val allRequestDtoFields = this.requestDtoDef.dtoFieldDefs


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "OnInit")
        addImport("@angular/core", "output")
        addImport("@angular/core", "signal")

        addImport("@angular/forms", "FormControl")
        addImport("@angular/forms", "FormGroup")
        addImport("@angular/forms", "Validators")
        addImport("@angular/forms", "FormsModule", isModule = true)
        addImport("@angular/forms", "ReactiveFormsModule", isModule = true)

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            addImport("@angular/material/dialog", "MatDialog")
            addImport("@angular/material/dialog", "MatDialogRef")
            addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
            addImport("@angular/material/dialog", "MatDialogTitle", isModule = true)
            addImport("@angular/material/dialog", "MatDialogContent", isModule = true)
            addImport("@angular/material/dialog", "MatDialogActions", isModule = true)
        }

        this.angularFormDef.onSuccessUrl?.let {
            addImport("@angular/router", "Router")
        }

        addImport("rxjs", "Observable")
        addImport("rxjs", "Subject")
        addImport("rxjs", "of")
        addImport("rxjs/operators", "catchError")
        addImport("rxjs/operators", "debounceTime")
        addImport("rxjs/operators", "distinctUntilChanged")
        addImport("rxjs/operators", "filter")
        addImport("rxjs/operators", "map")
        addImport("rxjs/operators", "switchMap")
        addImport("rxjs/operators", "tap")
        addImport(this.angularFormDef.formServiceTypescriptImport)
        this.angularFormDef.context?.let { addImport(it.typescriptImport) }
        addImport(this.requestDtoDef.typescriptImport)
        addImport("@angular/material/button", "MatButtonModule", isModule = true)
        addImport("@angular/material/core", "MatOptionModule", isModule = true)
        addImport("@angular/material/autocomplete", "MatAutocompleteModule", isModule = true)
        addImport("@angular/material/input", "MatInputModule", isModule = true)
        addImport("@angular/material/form-field", "MatFormFieldModule", isModule = true)
        addImport(TypescriptImports.problemDetail)

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->
            addImport(typeaheadDef.typescriptServiceImport)
        }

        this.angularFormDef.formModelFields.forEach { angularFieldDef ->
            angularFieldDef.typeaheadRequiredValidatorTypescriptImport?.let { addImport(it) }
        }

        this.angularFormDef.uniqueIndexDefs.forEach { databaseIndexDef ->
            addImport(databaseIndexDef.asyncValidator.asyncValidatorTypescriptImport)
        }

        this.angularFormDef.formModelFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
        }

        this.angularFormDef.fetchForEditDtoDef?.let {
            addImport("@angular/material/progress-spinner", "MatProgressSpinnerModule", isModule = true)
            addImport(it.typescriptImport)
        }

        if (this.angularFormDef.hasAnyMatSelectFields) {

            addImport("@angular/material/select", "MatOption", isModule = true)
            addImport("@angular/material/select", "MatSelect", isModule = true)
            addImport("@angular/material/tooltip", "MatTooltip", isModule = true)

            this.angularFormDef.enumsForMatSelectFields.forEach { enumDef ->
                addImport(enumDef.typescriptImport)
                addImport(enumDef.selectOptionsTypescriptImport)
            }

        }

        if (this.angularFormDef.formModelFields.any { it.hasValidationConstraint(UrlConstraintDef::class.java) }) {
            addImport("@app/validators/CustomValidators", "CustomValidators")
        }

    }


    override fun renderComponentSource() {

        appendLine("export class $className implements OnInit {")

        `render class fields`()

        `render constructor`()

        `render function ngOnInit`()

        `render TypeaheadResultFormatters`()

        `render function onSubmit`()

        `render function onCancel`()

        blankLine()
        blankLine()
        appendLine("}")

    }


    private fun `render class fields`() {

        if (this.angularFormDef.delegateFormSubmission.value) {

            append("""
                |
                |
                |    readonly onFormSubmission = output<${this.angularFormDef.requestDtoDef.uqcn}>();
                |""".trimMargin())

        }

        if (this.angularFormDef.emitEventOnSuccess.value) {

            append("""
                |
                |
                |    readonly onSuccessEvent = output();
                |""".trimMargin())

        }

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |
                |
                |    readonly onErrorEvent = output<any>();
                |""".trimMargin())

        }

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->

            append("""
                |
                |
                |    filtered${typeaheadDef.typeaheadName} = [];
                |
                |
                |    filtered${typeaheadDef.typeaheadName}IsLoading = signal(false);
                |""".trimMargin())

        }

        append("""
            |
            |
            |    problemDetail = signal<ProblemDetail | null>(null);
            |""".trimMargin())

        this.angularFormDef.fetchForEditDtoDef?.let {
            append("""
                |
                |
                |    loading = signal(true);
                |""".trimMargin())
        }

        this.angularFormDef.enumsForMatSelectFields
            .filter { it.withEnumSelectionOptions }
            .distinctBy { it.selectOptionsUqcn }
            .forEach { enumDef ->

            append("""
                |
                |
                |    protected readonly ${enumDef.selectOptionsUqcn} = ${enumDef.selectOptionsUqcn};
                |""".trimMargin())

        }

        append("""
            |
            |
            |    formGroup: FormGroup;
            |""".trimMargin())

        this.angularFormDef.formModelFields.filter { it.linksToAField }.forEach { fieldDef ->

            append("""
                |
                |
                |    ${fieldDef.classFieldDef.classFieldName}IsVisible = signal<boolean>(false);
                |""".trimMargin())

        }

    }


    private fun `render constructor`() {

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            blankLine()
            blankLine()
            appendLine("    readonly dialogRef = inject(MatDialogRef<${this.angularFormDef.componentNames.componentName}>);")
        }

        blankLine()
        blankLine()
        appendLine("    private readonly formService = inject(${this.angularFormDef.formServiceClassName});")

        if (angularFormDef.createOrEdit == CreateOrEdit.edit) {
            blankLine()
            blankLine()
            appendLine("    private readonly entityId = inject<string>(MAT_DIALOG_DATA);")
        }

        this.angularFormDef.context?.let { context ->
            blankLine()
            blankLine()
            appendLine("    private readonly context = inject<${context.uqcn}>(MAT_DIALOG_DATA);")
        }

        this.angularFormDef.onSuccessUrl?.let {
            blankLine()
            blankLine()
            appendLine("    private readonly router = inject(Router);")
        }

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->

            val serviceUqcn = StringFunctions.firstToLower(typeaheadDef.angularServiceClassName)
            blankLine()
            blankLine()
            appendLine("    private readonly $serviceUqcn = inject(${typeaheadDef.angularServiceClassName});")

        }

        this.angularFormDef.multiFieldUniqueIndexDefs.forEach { databaseIndexDef ->
            blankLine()
            blankLine()
            appendLine("    private readonly ${databaseIndexDef.validatorFieldName} = inject(${databaseIndexDef.validatorName});")
        }

        this.angularFormDef.formModelFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            blankLine()
            blankLine()
            appendLine("    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});")
        }

        append("""
            |
            |
            |    constructor() {
            |
            |        this.formGroup = new FormGroup(
            |            {
            |""".trimMargin())

        this.formGroupFields.forEach { angularFormFieldDef ->

            val classFieldDef = angularFormFieldDef.classFieldDef
            val typeaheadDef = classFieldDef.typeaheadDef

            if (typeaheadDef == null) {

                val newFormControl = FormControlRendererHelper.renderFormControlFor(
                    angularFormFieldDef,
                    angularFormDef.createOrEdit
                )

                appendLine("                ${classFieldDef.classFieldName}: $newFormControl,")

            } else {

                val validators = if (classFieldDef.nullable) "" else "${angularFormFieldDef.typeaheadRequiredValidatorFunctionName}()"

                val initialValue = "''"

                appendLine("                ${typeaheadDef.typeaheadName.firstToLower()}: new FormControl($initialValue, { updateOn: 'change', validators: [$validators] }),")
            }

        }

        appendLine("            },")

        if (this.angularFormDef.multiFieldUniqueIndexDefs.isNotEmpty()) {

            appendLine("            {")
            appendLine("                asyncValidators: [")

            this.angularFormDef.multiFieldUniqueIndexDefs.forEach { databaseIndexDef ->
                appendLine("                    this.${databaseIndexDef.validatorFieldName}.validate.bind(this.${databaseIndexDef.validatorFieldName}),")
            }

            appendLine("                ],")
            appendLine("                updateOn: 'blur'")
            appendLine("            }")

        }

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

            blankLine()
            appendLine("        this.formService.fetchForEdit(this.entityId).subscribe({")
            appendLine("            next: (dto: ${fetchForEditDtoDef.uqcn}) => {")
            appendLine("                this.formGroup.patchValue({")

            this.formGroupFields.forEach { angularFormFieldDef ->
                val classFieldDef = angularFormFieldDef.classFieldDef
                val typeaheadDef = classFieldDef.typeaheadDef

                if (typeaheadDef != null) {
                    val formControlName = typeaheadDef.typeaheadName.firstToLower()
                    appendLine("                    ${formControlName}: dto.${formControlName},")
                } else if (classFieldDef.fieldType is ForeignKeyFieldType) {
                    // Skip non-typeahead FK fields — DTO uses a PkAndName object shape
                } else {
                    val fieldName = classFieldDef.classFieldName
                    appendLine("                    ${fieldName}: dto.${fieldName},")
                }
            }

            appendLine("                });")
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
            |
            |""".trimMargin())

        renderRequestDtoConstruction()

        blankLine()

        if (this.angularFormDef.delegateFormSubmission.value) {

            appendLine("        this.onFormSubmission.emit(requestDto);")

        } else {

            `render formService request`()

        }

        blankLine()
        appendLine("    }")

    }


    private fun `render formService request`() {

        append("""
            |        this.formService.${angularFormDef.onSubmitServiceFunctionName}(requestDto)
            |            .subscribe({
            |""".trimMargin()
        )

        `render formService request next function`()
        `render formService request error function`()

        appendLine("            });")

    }


    private fun `render formService request next function`() {

        if (this.angularFormDef.onSuccessUrl.isNullOrEmpty()) {

            when (this.angularFormDef.inlineFormOrDialog) {

                InlineFormOrDialog.INLINE_FORM -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |                next: () => {
                            |                    this.onSuccessEvent.emit();
                            |                },
                            |""".trimMargin())

                    } else {

                        append("""
                            |                next: () => {
                            |                    // TODO maybe emit an event?
                            |                },
                            |""".trimMargin())

                    }

                }

                InlineFormOrDialog.DIALOG -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |                next: () => {
                            |                    this.onSuccessEvent.emit();
                            |                },
                            |""".trimMargin())

                    } else {

                        append("""
                            |                next: () => {
                            |                    this.dialogRef.close(true);
                            |                },
                            |""".trimMargin())

                    }

                }

            }

        } else {

            append("""
                |                next: (_) => {
                |                    this.router.navigate(['${this.angularFormDef.onSuccessUrl}']);
                |                },
                |""".trimMargin())

        }

    }


    private fun `render formService request error function`() {

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |                error: err => {
                |                    this.onErrorEvent.emit(err);
                |                }
                |""".trimMargin())

        } else {

            append("""
                |                error: err => {
                |                    this.problemDetail.set(err.error);
                |                }
                |""".trimMargin())

        }

    }


    private fun renderRequestDtoConstruction() {

        appendLine("        const requestDto = {")

        this.angularFormDef.context?.let {
            appendLine("            context: this.context,")
        }

        this.allRequestDtoFields
            .forEach { requestDtoFieldDef ->

                val dtoFieldName = requestDtoFieldDef.classFieldDef.classFieldName
                val typeaheadDef = requestDtoFieldDef.classFieldDef.typeaheadDef

                if (typeaheadDef == null) {

                    if (dtoFieldName == ClassFieldName.context) {

                        if (this.angularFormDef.context == null) {
                            appendLine("            context: this.context,")
                        }

                    } else {
                        appendLine("            ${dtoFieldName}: this.formGroup.getRawValue().$dtoFieldName,")
                    }

                } else {

                    val formGroupFieldName = typeaheadDef.typeaheadName.firstToLower()
                    appendLine("            ${typeaheadDef.idFieldName}: this.formGroup.getRawValue().${formGroupFieldName}.${typeaheadDef.idFieldName},")

                }

            }

        appendLine("        } as ${this.angularFormDef.requestDtoDef.uqcn};")

    }


    private fun `render function onCancel`() {

        if (this.angularFormDef.inlineFormOrDialog != InlineFormOrDialog.DIALOG) {
            return
        }

        append("""
            |
            |
            |    onCancel(): void {
            |        this.dialogRef.close();
            |    }
            |""".trimMargin())

    }


}

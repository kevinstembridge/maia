package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ClassFieldName
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef
import org.maiaframework.lang.text.StringFunctions


class EntityFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    angularComponentNames: AngularComponentNames
) : AbstractAngularComponentRenderer(angularComponentNames) {


    private val formGroupFields = this.angularFormDef.formModelFields


    private val requestDtoDef = this.angularFormDef.requestDtoDef


    private val allRequestDtoFields = this.requestDtoDef.dtoFieldDefs


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "OnInit")
        addImport("@angular/core", "signal")

        addImport("@angular/forms/signals", "form")
        addImport("@angular/forms/signals", "FormField", isModule = true)
        addImport("@angular/forms/signals", "submit")
        addImport("@angular/forms", "FormsModule", isModule = true)
        addImport("@angular/forms", "ReactiveFormsModule", isModule = true)

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            addImport("@angular/material/dialog", "MatDialog")
            addImport("@angular/material/dialog", "MatDialogRef")
            // TODO probably only needed for Edit forms
//            addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
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
        addImport("@app/gen-components/common/model/ProblemDetail", "ProblemDetail")

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->
            addImport(typeaheadDef.typescriptServiceImport)
        }

        this.angularFormDef.formModelFields.forEach { angularFieldDef ->
            angularFieldDef.typeaheadRequiredValidatorTypescriptImport?.let { addImport(it) }
        }

//        this.angularFormDef.uniqueIndexDefs.forEach { databaseIndexDef ->
//            addImport(databaseIndexDef.asyncValidator.asyncValidatorTypescriptImport)
//        }

        if (this.angularFormDef.uniqueIndexDefs.isNotEmpty()) {
            addImport("@angular/forms/signals", "validateHttp")
        }

//        this.angularFormDef.formModelFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
//            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
//        }

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

        `render class field dialogFormModel`()
        `render class field dialogForm`()
        `render class field problemDetail`()

        if (this.angularFormDef.delegateFormSubmission.value) {

            append("""
                |
                |
                |    @Output() onFormSubmission = new EventEmitter<${this.angularFormDef.requestDtoDef.uqcn}>();
                |""".trimMargin())

        }

        if (this.angularFormDef.emitEventOnSuccess.value) {

            append("""
                |
                |
                |    @Output() onSuccessEvent = new EventEmitter<void>();
                |""".trimMargin())

        }

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |
                |
                |    @Output() onErrorEvent = new EventEmitter<any>();
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

        this.angularFormDef.formModelFields.filter { it.linksToAField }.forEach { fieldDef ->

            append("""
                |
                |
                |    ${fieldDef.classFieldDef.classFieldName}IsVisible = signal<boolean>(false);
                |""".trimMargin())

        }

    }

    private fun `render class field problemDetail`() {
        append(
            """
                |
                |
                |    problemDetail = signal<ProblemDetail | null>(null);
                |""".trimMargin()
        )
    }


    private fun `render class field dialogFormModel`() {

        append("""
            |
            |
            |    dialogFormModel = signal<${this.angularFormDef.requestDtoDef.uqcn}>({
            |""".trimMargin()
        )

        this.angularFormDef.formModelFields.sortedBy { it.fieldName }.forEach { fieldDef ->

            append(
                """
                    |        ${fieldDef.classFieldDef.classFieldName}: '',
                    |""".trimMargin()
            )

        }

        append("""
            |    });
            |""".trimMargin()
        )

    }


    private fun `render class field dialogForm`() {

        blankLine()
        blankLine()

        if (this.angularFormDef.hasAnyValidationConstraints) {
            appendLine("    dialogForm = form(this.dialogFormModel, (schemaPath) => {")
        } else {
            appendLine("    dialogForm = form(this.dialogFormModel)")
        }

        this.angularFormDef.formModelFields.forEach { `render dialogForm field`(it) }

        this.angularFormDef.formModelFields.filter { it.linksToAField }.forEach { fieldDef ->

            append("""
                |        ${fieldDef.classFieldDef.classFieldName}: '',
                ||""".trimMargin()
            )

        }

        appendLine("    });")

    }

    
    private fun `render dialogForm field`(angularFormFieldDef: AngularFormFieldDef) {
        
        val classFieldDef = angularFormFieldDef.classFieldDef
        val classFieldName = classFieldDef.classFieldName
        val fieldLabel = angularFormFieldDef.fieldLabel

        if (classFieldDef.hasAnyValidationConstraints() || classFieldDef.isUnique) {

            if (classFieldDef.hasValidationConstraint(NotNullConstraintDef::class.java)
                || classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)
            ) {

                addImport("@angular/forms/signals", "required")

                appendLine("        required(schemaPath.$classFieldName, { message: '$fieldLabel is required' })")

            }

            if (classFieldDef.hasValidationConstraint(EmailConstraintDef::class.java)) {

                addImport("@angular/forms/signals", "email")

                appendLine("        email(schemaPath.$classFieldName, { message: '$fieldLabel must be valid' })")

            }

            if (classFieldDef.hasValidationConstraint(UrlConstraintDef::class.java)) {
                appendLine("        @if (formGroup.controls['$classFieldName'].hasError('invalidUrl')) {")
                appendLine("            <mat-error>${fieldLabel} must be a valid URL.</mat-error>")
                appendLine("        }")
            }

            classFieldDef.minConstraint?.minValue?.let { minValue ->

                addImport("@angular/forms/signals", "min")

                appendLine("        min(schemaPath.$classFieldName, { message: '$fieldLabel must be at least $minValue.'})")

            }

            classFieldDef.maxConstraint?.maxValue?.let { maxValue ->

                addImport("@angular/forms/signals", "max")

                appendLine("        max(schemaPath.$classFieldName, { message: '$fieldLabel must not be more than $maxValue.'})")

            }

            classFieldDef.lengthConstraint?.min?.let { minLength ->

                addImport("@angular/forms/signals", "minLength")

                appendLine("        minLength(schemaPath.$classFieldName, $minLength)")

            }

            classFieldDef.lengthConstraint?.max?.let { maxLength ->

                addImport("@angular/forms/signals", "maxLength")

                appendLine("        maxLength(schemaPath.$classFieldName, $maxLength)")

            }

            if (classFieldDef.isUnique) {

                addImport("@angular/forms/signals", "validateHttp")

                append("""
                    |        validateHttp(schemaPath.$classFieldName, {
                    |            request: ({value}) => {
                    |                return {
                    |                    url: '${classFieldDef.classFieldName}.huh',
                    |                    body: value(),
                    |                }
                    |            },
                    |            onSuccess: (response: any) => {
                    |                if (response.huh) {
                    |                    return {
                    |                        kind: 'huh',
                    |                        message: 'huh'
                    |                    };
                    |                }
                    |                return null;
                    |            },
                    |            onError: (error) => ({
                    |                kind: 'huh',
                    |                message: 'huh'
                    |            }),
                    |        });
                    |""".trimMargin())

            }

        }

    }


    private fun `render constructor`() {

        append("""
            |
            |
            |    constructor(
            |""".trimMargin())

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            appendLine("        public dialogRef: MatDialogRef<${this.angularFormDef.componentNames.componentName}>,")
        }

        appendLine("        private formService: ${this.angularFormDef.formServiceClassName},")

        if (angularFormDef.createOrEdit == CreateOrEdit.edit) {
            appendLine("        @Inject(MAT_DIALOG_DATA) private dto: any,")
        }

        this.angularFormDef.context?.let { context ->
            appendLine("        @Inject(MAT_DIALOG_DATA) private context: ${context.uqcn},")
        }

        this.angularFormDef.onSuccessUrl?.let {
            appendLine("        private router: Router,")
        }

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->

            val serviceUqcn = StringFunctions.firstToLower(typeaheadDef.angularServiceClassName)
            appendLine("        private $serviceUqcn: ${typeaheadDef.angularServiceClassName},")

        }

        this.angularFormDef.multiFieldUniqueIndexDefs.forEach { databaseIndexDef ->
            appendLine("        private ${databaseIndexDef.validatorFieldName}: ${databaseIndexDef.validatorName},")
        }

//        this.angularFormDef.formModelFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
//            appendLine("        private ${asyncValidatorDef.validatorFieldName}: ${asyncValidatorDef.asyncValidatorName},")
//        }

        if (true) {
            append("""
                |    ) {}
                |""".trimMargin())

            return

        }

        append("""
            |    ) {
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

                val initialValue = when (angularFormDef.createOrEdit) {
                    CreateOrEdit.create -> "''"
                    CreateOrEdit.edit -> typeaheadDef.fieldDefs.map { "${it.fieldName}: dto.${it.fieldName}" }
                        .joinToString(prefix = "{ ", separator = ", ", postfix = " }")

                    null -> "''"
                }

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
            |    async onSubmit(e: Event) {
            |
            |        e.preventDefault();
            |
            |        this.problemDetail.set(null);
            |
            |        await submit(this.dialogForm, async (form) => {
            |
            |""".trimMargin())

        if (this.angularFormDef.delegateFormSubmission.value) {

            appendLine("        this.onFormSubmission.emit(requestDto);")

        } else {

            `render formService request`()

        }

        blankLine()
        appendLine("        });")
        blankLine()
        appendLine("    }")

    }


    private fun `render formService request`() {

        append("""
            |            this.formService.${angularFormDef.onSubmitServiceFunctionName}(form().value())
            |                .subscribe({
            |""".trimMargin()
        )

        `render formService request next function`()
        `render formService request error function`()

        appendLine("                });")

    }


    private fun `render formService request next function`() {

        if (this.angularFormDef.onSuccessUrl.isNullOrEmpty()) {

            when (this.angularFormDef.inlineFormOrDialog) {

                InlineFormOrDialog.INLINE_FORM -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |                    next: () => {
                            |                        this.onSuccessEvent.emit();
                            |                    },
                            |""".trimMargin())

                    } else {

                        append("""
                            |                    next: () => {
                            |                        // TODO maybe emit an event?
                            |                    },
                            |""".trimMargin())

                    }

                }

                InlineFormOrDialog.DIALOG -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {

                        append("""
                            |                    next: () => {
                            |                        this.onSuccessEvent.emit();
                            |                    },
                            |""".trimMargin())

                    } else {

                        append("""
                            |                    next: () => {
                            |                        this.dialogRef.close(true);
                            |                    },
                            |""".trimMargin())

                    }

                }

            }

        } else {

            append("""
                |                    next: (_) => {
                |                        this.router.navigate(['${this.angularFormDef.onSuccessUrl}']);
                |                    },
                |""".trimMargin())

        }

    }


    private fun `render formService request error function`() {

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |                    error: err => {
                |                        this.onErrorEvent.emit(err);
                |                    }
                |""".trimMargin())

        } else {

            append("""
                |                    error: err => {
                |                        this.problemDetail.set(err.error);
                |                    }
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

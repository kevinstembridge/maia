package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.HtmlInputType
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.FormPurpose
import org.maiaframework.gen.spec.definition.flags.InlineFormOrDialog
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.validation.EmailConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotBlankConstraintDef
import org.maiaframework.gen.spec.definition.validation.NotNullConstraintDef
import org.maiaframework.gen.spec.definition.validation.UrlConstraintDef


class AngularSignalFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    angularComponentNames: AngularComponentNames,
    providerServices: List<String>
) : AbstractAngularComponentRenderer(
    angularComponentNames,
    providerServices
) {


    private val requestDtoFieldNames = this.angularFormDef.requestDtoDef.dtoFieldDefs.map { it.classFieldDef.classFieldName }.toSet()


    /** The form model holds only the fields of the request DTO. Read-only fields are displayed from the fetched DTO. */
    private val modelFields = this.angularFormDef.formModelFields.filter { it.classFieldDef.classFieldName in requestDtoFieldNames }


    private val numberFields = modelFields.filter { it.isNumberInput }


    private val typeaheadFields = modelFields.filter { it.isTypeahead }


    private val linkedFields = modelFields.filter { it.linksToAField }


    private val withFetchForEdit = this.angularFormDef.fetchForEditDtoDef != null


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "output")
        addImport("@angular/core", "signal")

        if (withFetchForEdit) {
            addImport("@angular/core", "OnInit")
        }

        if (angularFormDef.formPurpose == FormPurpose.edit || angularFormDef.isEntityCreateForm) {
            addImport("@angular/core", "input")
        }

        if (linkedFields.isNotEmpty()) {
            addImport("@angular/core", "computed")
        }

        addImport("@angular/forms/signals", "form")
        addImport("@angular/forms/signals", "FormField", isModule = true)
        addImport("@angular/forms/signals", "submit")
        addImport("rxjs", "firstValueFrom")

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {
            addImport("@angular/material/dialog", "MatDialog")
            addImport("@angular/material/dialog", "MatDialogRef")
            addImport("@angular/material/dialog", "MatDialogTitle", isModule = true)
            addImport("@angular/material/dialog", "MatDialogContent", isModule = true)
            addImport("@angular/material/dialog", "MatDialogActions", isModule = true)
        }

        if (angularFormDef.context != null) {
            addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
        }

        this.angularFormDef.onSuccessUrl?.let {
            addImport("@angular/router", "Router")
        }

        this.angularFormDef.fetchForEditDtoDef?.let { fetchForEditDtoDef ->
            addImport(fetchForEditDtoDef.typescriptImport)
        }

        if (angularFormDef.formPurpose == FormPurpose.create && angularFormDef.isEntityCreateForm) {
            val createdEntityDef = this.angularFormDef.createdEntityDef
            if (createdEntityDef != null && createdEntityDef.createReturnsPkAndName) {
                addImport(createdEntityDef.entityPkAndNameDef.pkAndNameDtoTypescriptImport)
            } else {
                addImport("@maia/maia-ui", "EntityCreatedResponseDto")
            }
        }

        addImport(this.angularFormDef.formServiceTypescriptImport)
        this.angularFormDef.context?.let { addImport(it.typescriptImport) }
        addImport(this.angularFormDef.requestDtoDef.typescriptImport)
        addImport("@angular/material/button", "MatButtonModule", isModule = true)
        addImport("@angular/material/input", "MatInputModule", isModule = true)
        addImport("@angular/material/form-field", "MatFormFieldModule", isModule = true)

        if (this.angularFormDef.htmlFormFields.any { it.htmlInputType == HtmlInputType.password }) {
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
        }

        if (angularFormDef.readOnlyFormFields.any { it.isUuid && it.fieldType !is ForeignKeyFieldType }) {
            addImport("@maia/maia-ui", "ShortId", isModule = true)
        }

        addImport(TypescriptImports.problemDetail)

        this.angularFormDef.allTypeaheadDefs.forEach { typeaheadDef ->
            addImport(typeaheadDef.typescriptComponentImport)
        }

        if (this.angularFormDef.hasAnyBooleanFields) {
            addImport("@angular/material/checkbox", "MatCheckbox", isModule = true)
        }

        if (this.angularFormDef.hasAnyInstantFields) {
            addImport("@angular/material/datepicker", "MatDatepicker", isModule = true)
            addImport("@angular/material/datepicker", "MatDatepickerInput", isModule = true)
            addImport("@angular/material/datepicker", "MatDatepickerToggle", isModule = true)
            addImport("@angular/material/timepicker", "MatTimepicker", isModule = true)
            addImport("@angular/material/timepicker", "MatTimepickerInput", isModule = true)
            addImport("@angular/material/timepicker", "MatTimepickerToggle", isModule = true)
        }

        if (this.angularFormDef.htmlFormFields.any { it.stringValueClassListEnumOptionsDef != null }) {
            addImport("@angular/material/select", "MatSelect", isModule = true)
            addImport("@angular/material/select", "MatOption", isModule = true)
            addImport("@angular/material/tooltip", "MatTooltip", isModule = true)
        }

        this.angularFormDef.htmlFormFields.mapNotNull { it.stringValueClassListEnumOptionsDef }.forEach {
            addImport(it.selectOptionsTypescriptImport)
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

    }


    override fun renderComponentSource() {

        val implementsOnInit = if (withFetchForEdit) " implements OnInit" else ""

        appendLine("export class $className$implementsOnInit {")

        `render class fields`()
        `render function ngOnInit`()
        `render function onSubmit`()
        `render function onCancel`()

        blankLine()
        blankLine()
        appendLine("}")

    }


    private fun `render class fields`() {

        `render class field entityId`()
        `render class fields for outputs`()
        `render class field formService`()
        `render class field problemDetail`()
        `render class fields for enum MatSelect fields`()
        `render class fields for string value class list options`()
        `render class fields for password visibility toggle`()
        `render class field dialogFormModel`()
        `render class field dialogForm`()
        `render class field loading`()
        `render class fields for linked fields`()
        `render class field dialogRef`()
        `render class field context`()
        `render class field router`()

    }


    private fun `render class field entityId`() {

        if (angularFormDef.formPurpose == FormPurpose.edit) {

            append("""
                |
                |
                |    entityId = input.required<${angularFormDef.entityIdInjectType}>();
                |""".trimMargin()
            )

        }

    }


    private fun `render class fields for outputs`() {

        if (this.angularFormDef.delegateFormSubmission.value) {

            append("""
                |
                |
                |    readonly onFormSubmission = output<${this.angularFormDef.requestDtoDef.uqcn}>();
                |""".trimMargin()
            )

        }

        if (this.angularFormDef.isEntityCreateForm) {

            append("""
                |
                |
                |    readonly onSuccess = output<${this.angularFormDef.createResponseTypeName}>();
                |
                |
                |    /** Set to false when embedding the form to stay on the current page after a successful create. */
                |    readonly navigateOnSuccess = input(true);
                |""".trimMargin()
            )

        } else if (this.angularFormDef.emitEventOnSuccess.value) {

            append("""
                |
                |
                |    readonly onSuccess = output();
                |""".trimMargin()
            )

        }

        if (this.angularFormDef.emitEventOnError.value) {

            append("""
                |
                |
                |    readonly onErrorEvent = output<any>();
                |""".trimMargin()
            )

        }

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.INLINE_FORM) {

            append("""
                |
                |
                |    onCancel = output();
                |""".trimMargin()
            )

        }

    }


    private fun `render class field formService`() {

        append("""
            |
            |
            |    private readonly formService = inject(${this.angularFormDef.formServiceClassName});
            |""".trimMargin()
        )

    }


    private fun `render class field problemDetail`() {

        append("""
            |
            |
            |    problemDetail = signal<ProblemDetail | null>(null);
            |""".trimMargin()
        )

    }


    private fun `render class fields for enum MatSelect fields`() {

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

    }


    private fun `render class fields for string value class list options`() {

        this.angularFormDef.htmlFormFields
            .mapNotNull { it.stringValueClassListEnumOptionsDef }
            .distinctBy { it.selectOptionsUqcn }
            .forEach { enumOptionsDef ->

            append("""
                |
                |
                |    protected readonly ${enumOptionsDef.selectOptionsUqcn} = ${enumOptionsDef.selectOptionsUqcn};
                |""".trimMargin())

        }

    }


    private fun `render class fields for password visibility toggle`() {

        this.angularFormDef.htmlFormFields
            .filter { it.htmlInputType == HtmlInputType.password }
            .forEach { field ->

            append("""
                |
                |
                |    ${field.fieldName}PasswordVisible = signal(false);
                |""".trimMargin())

        }

    }


    private val modelTypeName: String
        get() {
            val dtoType = "Required<${this.angularFormDef.requestDtoDef.uqcn}>"
            // A typeahead holds the selected document (or the text being typed) until it is mapped to its id on submit.
            val overrides = numberFields.map { "${it.fieldName}: number | null" } + typeaheadFields.map { "${it.fieldName}: any" }
            if (overrides.isEmpty()) {
                return dtoType
            }
            val omitted = (numberFields + typeaheadFields).joinToString(" | ") { "'${it.fieldName}'" }
            return "Omit<$dtoType, $omitted> & { ${overrides.joinToString("; ")} }"
        }


    private fun `render class field dialogFormModel`() {

        append("""
            |
            |
            |    dialogFormModel = signal<$modelTypeName>({
            |""".trimMargin()
        )

        modelFields.sortedBy { it.fieldName }.forEach { fieldDef ->

            append(
                """
                    |        ${fieldDef.classFieldDef.classFieldName}: ${fieldDef.classFieldDef.defaultFormFieldValue},
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

        if (modelFields.any { it.hasAnyValidationConstraint() } || linkedFields.isNotEmpty()) {

            appendLine("    dialogForm = form(this.dialogFormModel, (schemaPath) => {")

            modelFields.forEach { `render dialogForm field`(it) }

            linkedFields.forEach { fieldDef ->

                val fieldLinkedTo = fieldDef.classFieldDef.fieldLinkedTo!!

                addImport("@angular/forms/signals", "disabled")

                appendLine("        disabled(schemaPath.${fieldDef.classFieldDef.classFieldName}, ({valueOf}) => valueOf(schemaPath.${fieldLinkedTo.classFieldName}) !== ${fieldLinkedTo.fieldType}.OTHER);")

            }

            appendLine("    });")

        } else {

            appendLine("    dialogForm = form(this.dialogFormModel);")

        }

    }


    private fun `render dialogForm field`(angularFormFieldDef: AngularFormFieldDef) {

        val classFieldDef = angularFormFieldDef.classFieldDef
        val classFieldName = classFieldDef.classFieldName
        val fieldLabel = angularFormFieldDef.fieldLabel

        if (angularFormFieldDef.isTypeahead) {

            if (!classFieldDef.nullable) {

                addImport("@angular/forms/signals", "validate")

                append("""
                    |        validate(schemaPath.$classFieldName, ({value}) => {
                    |            return value() && typeof value() === 'object'
                    |                ? null
                    |                : { kind: 'required', message: '$fieldLabel is required.' };
                    |        });
                    |""".trimMargin())

            }

            return

        }

        if (classFieldDef.hasValidationConstraint(NotNullConstraintDef::class.java)
            || classFieldDef.hasValidationConstraint(NotBlankConstraintDef::class.java)
        ) {

            addImport("@angular/forms/signals", "required")

            appendLine("        required(schemaPath.$classFieldName, { message: '$fieldLabel is required.' });")

        }

        if (classFieldDef.hasValidationConstraint(EmailConstraintDef::class.java)) {

            addImport("@angular/forms/signals", "email")

            appendLine("        email(schemaPath.$classFieldName, { message: '$fieldLabel must be valid.' });")

        }

        if (classFieldDef.hasValidationConstraint(UrlConstraintDef::class.java)) {

            addImport("@angular/forms/signals", "validate")

            append("""
                |        validate(schemaPath.$classFieldName, ({value}) => {
                |            if (!value()) {
                |                return null;
                |            }
                |            try {
                |                new URL(value());
                |                return null;
                |            } catch {
                |                return { kind: 'invalidUrl', message: '$fieldLabel must be a valid URL.' };
                |            }
                |        });
                |""".trimMargin())

        }

        classFieldDef.minConstraint?.minValue?.let { minValue ->

            addImport("@angular/forms/signals", "min")

            appendLine("        min(schemaPath.$classFieldName, $minValue, { message: '$fieldLabel must be at least $minValue.' });")

        }

        classFieldDef.maxConstraint?.maxValue?.let { maxValue ->

            addImport("@angular/forms/signals", "max")

            appendLine("        max(schemaPath.$classFieldName, $maxValue, { message: '$fieldLabel must not be more than $maxValue.' });")

        }

        classFieldDef.lengthConstraint?.min?.let { minLength ->

            addImport("@angular/forms/signals", "minLength")

            appendLine("        minLength(schemaPath.$classFieldName, $minLength, { message: '$fieldLabel must be at least $minLength characters.' });")

        }

        classFieldDef.lengthConstraint?.max?.let { maxLength ->

            addImport("@angular/forms/signals", "maxLength")

            appendLine("        maxLength(schemaPath.$classFieldName, $maxLength, { message: '$fieldLabel must not be more than $maxLength characters.' });")

        }

    }


    private fun `render class field loading`() {

        this.angularFormDef.fetchForEditDtoDef?.let { fetchForEditDtoDef ->

            append("""
                |
                |
                |    loading = signal(true);
                |
                |
                |    /** The fetched DTO, from which the read-only fields are displayed. */
                |    fetchedDto = signal<${fetchForEditDtoDef.uqcn} | null>(null);
                |""".trimMargin()
            )

        }

    }


    private fun `render class fields for linked fields`() {

        linkedFields.forEach { fieldDef ->

            val fieldLinkedTo = fieldDef.classFieldDef.fieldLinkedTo!!

            append("""
                |
                |
                |    ${fieldDef.classFieldDef.classFieldName}IsVisible = computed(() => this.dialogFormModel().${fieldLinkedTo.classFieldName} === ${fieldLinkedTo.fieldType}.OTHER);
                |""".trimMargin())

        }

    }


    private fun `render class field dialogRef`() {

        if (this.angularFormDef.inlineFormOrDialog == InlineFormOrDialog.DIALOG) {

            append("""
                |
                |
                |    readonly dialogRef = inject(MatDialogRef<${this.angularFormDef.componentNames.componentName}>);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field context`() {

        this.angularFormDef.context?.let { context ->

            append("""
                |
                |
                |    private readonly context = inject<${context.uqcn}>(MAT_DIALOG_DATA);
                |""".trimMargin()
            )

        }

    }


    private fun `render class field router`() {

        this.angularFormDef.onSuccessUrl?.let {

            append("""
                |
                |
                |    private readonly router = inject(Router);
                |""".trimMargin()
            )

        }

    }


    private fun `render function ngOnInit`() {

        val fetchForEditDtoDef = this.angularFormDef.fetchForEditDtoDef ?: return

        append("""
            |
            |
            |    ngOnInit() {
            |
            |        this.formService.fetchForEdit(this.entityId()).subscribe({
            |            next: (dto: ${fetchForEditDtoDef.uqcn}) => {
            |                this.fetchedDto.set(dto);
            |                this.dialogFormModel.update(model => ({
            |                    ...model,
            |""".trimMargin()
        )

        modelFields.sortedBy { it.fieldName }.forEach { angularFormFieldDef ->

            val classFieldDef = angularFormFieldDef.classFieldDef
            val formFieldName = angularFormFieldDef.fieldName

            // Non-typeahead FK fields are skipped because the DTO uses a PkAndName object shape.
            if (classFieldDef.typeaheadDef != null || classFieldDef.fieldType !is ForeignKeyFieldType) {
                if (classFieldDef.nullable) {
                    appendLine("                    ${formFieldName}: dto.${formFieldName} ?? model.${formFieldName},")
                } else {
                    appendLine("                    ${formFieldName}: dto.${formFieldName},")
                }
            }

        }

        append("""
            |                }));
            |                this.loading.set(false);
            |            },
            |            error: (err) => {
            |                this.problemDetail.set(err.error);
            |                this.loading.set(false);
            |            }
            |        });
            |
            |    }
            |""".trimMargin()
        )

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
            |            const value = form().value();
            |""".trimMargin())

        `render requestDto construction`()

        if (this.angularFormDef.delegateFormSubmission.value) {

            appendLine("            this.onFormSubmission.emit(requestDto);")

        } else {

            `render formService request`()

        }

        blankLine()
        appendLine("        });")
        blankLine()
        appendLine("    }")

    }


    private fun `render requestDto construction`() {

        val requestDtoType = this.angularFormDef.requestDtoDef.uqcn

        if (typeaheadFields.isEmpty()) {

            appendLine("            const requestDto = value as $requestDtoType;")

        } else {

            appendLine("            const requestDto = {")
            appendLine("                ...value,")

            typeaheadFields.forEach { fieldDef ->

                val typeaheadDef = fieldDef.classFieldDef.typeaheadDef!!

                appendLine("                ${fieldDef.fieldName}: value.${fieldDef.fieldName}.${typeaheadDef.esDocIdFieldName},")

            }

            appendLine("            } as $requestDtoType;")

        }

    }


    private fun `render formService request`() {

        appendLine("")
        appendLine("            try {")
        val assignDto = if (angularFormDef.formPurpose == FormPurpose.create) "const dto = " else ""

        appendLine("                ${assignDto}await firstValueFrom(this.formService.${angularFormDef.onSubmitServiceFunctionName}(requestDto));")

        `render success handling`()

        appendLine("            } catch (err: any) {")

        `render error handling`()

        appendLine("            }")

    }


    private fun `render success handling`() {

        val onSuccessUrl = this.angularFormDef.onSuccessUrl

        if (onSuccessUrl.isNullOrEmpty()) {

            when (this.angularFormDef.inlineFormOrDialog) {

                InlineFormOrDialog.INLINE_FORM -> {

                    if (this.angularFormDef.isEntityCreateForm) {
                        appendLine("                this.onSuccess.emit(dto);")
                    } else if (this.angularFormDef.emitEventOnSuccess.value) {
                        appendLine("                this.onSuccess.emit();")
                    }

                }

                InlineFormOrDialog.DIALOG -> {

                    if (this.angularFormDef.emitEventOnSuccess.value) {
                        appendLine("                this.onSuccess.emit();")
                    } else {
                        appendLine("                this.dialogRef.close(true);")
                    }

                }

            }

            return

        }

        when (this.angularFormDef.formPurpose) {

            FormPurpose.create -> {

                if (this.angularFormDef.isEntityCreateForm) {

                    append("""
                        |                this.onSuccess.emit(dto);
                        |                if (this.navigateOnSuccess()) {
                        |                    this.router.navigate(['$onSuccessUrl', dto.id]);
                        |                }
                        |""".trimMargin())

                } else {

                    appendLine("                this.router.navigate(['$onSuccessUrl', dto.id]);")

                }

            }

            FormPurpose.edit -> appendLine("                this.router.navigate(['$onSuccessUrl', this.entityId()]);")

            else -> appendLine("                this.router.navigate(['$onSuccessUrl']);")

        }

    }


    private fun `render error handling`() {

        if (this.angularFormDef.emitEventOnError.value) {
            appendLine("                this.onErrorEvent.emit(err);")
        } else {
            appendLine("                this.problemDetail.set(err.error);")
        }

    }


    private fun `render function onCancel`() {

        when (this.angularFormDef.inlineFormOrDialog) {

            InlineFormOrDialog.DIALOG -> {

                append("""
                    |
                    |
                    |    onCancel(): void {
                    |        this.dialogRef.close();
                    |    }
                    |""".trimMargin())

            }

            InlineFormOrDialog.INLINE_FORM -> {

                append("""
                    |
                    |
                    |    onCancelClicked(): void {
                    |        this.onCancel.emit();
                    |    }
                    |""".trimMargin())

            }

        }

    }


}

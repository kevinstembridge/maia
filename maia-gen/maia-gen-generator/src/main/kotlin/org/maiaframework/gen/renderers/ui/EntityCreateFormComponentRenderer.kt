package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.InstantFieldType
import org.maiaframework.gen.spec.definition.lang.ListFieldType


class EntityCreateFormComponentRenderer(
    entityCreatePageDef: EntityCreatePageDef
) : AbstractAngularComponentRenderer(entityCreatePageDef.createFormAngularComponentNames) {


    private val createApiDef = entityCreatePageDef.createApiDef
    private val entityDef = entityCreatePageDef.entityDef
    private val formGroupFields = createApiDef.htmlFormFields


    init {

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
        addImport(TypescriptImports.problemDetail)
        addImport(createApiDef.requestDtoDef.typescriptImport)
        addImport(entityDef.crudAngularComponentNames.serviceTypescriptImport)
        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
        }

        addImportsFoFieldTypes(formGroupFields)

    }


    private fun addImportsFoFieldTypes(formGroupFields: List<AngularFormFieldDef>) {

        formGroupFields.filter { it.isEditable }.forEach { angularFormFieldDef ->

            when (val fieldType = angularFormFieldDef.fieldType) {
                is EnumFieldType -> {
                    addImport("@angular/material/select", "MatSelect", isModule = true)
                    addImport("@angular/material/select", "MatOption", isModule = true)
                    addImport("@angular/material/tooltip", "MatTooltip", isModule = true)

                    fieldType.enumDef.selectOptionsTypescriptImport.let {
//                        addImport(it)
                    }
                }

                is InstantFieldType -> {
                    addImport("@angular/material/datepicker", "MatDatepicker", isModule = true)
                    addImport("@angular/material/datepicker", "MatDatepickerInput", isModule = true)
                    addImport("@angular/material/datepicker", "MatDatepickerToggle", isModule = true)
                    addImport("@angular/material/timepicker", "MatTimepicker", isModule = true)
                    addImport("@angular/material/timepicker", "MatTimepickerInput", isModule = true)
                    addImport("@angular/material/timepicker", "MatTimepickerToggle", isModule = true)
                }

                is ListFieldType -> {
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

                else -> {}
            }

        }

    }


    override fun renderComponentSource() {

        appendLine("export class $className implements OnInit {")

        append("""
            |
            |
            |    onSave = output();
            |
            |
            |    onCancel = output();
            |
            |
            |    problemDetail = signal<ProblemDetail | null>(null);
            |
            |
            |    formGroup: FormGroup;
            |
            |
            |    private readonly formService = inject(${entityDef.crudAngularComponentNames.serviceName});
            |""".trimMargin())

        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            blankLine()
            blankLine()
            appendLine("    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});")
        }

        val enumFields = formGroupFields
            .asSequence()
            .filter { it.isEditable }
            .filter { it.fieldType is EnumFieldType }
            .map { it.fieldType as EnumFieldType }
            .map { it.enumDef }

        val listOfEnumFields = formGroupFields
            .asSequence()
            .filter { it.isEditable }
            .filter { it.fieldType is ListFieldType }
            .map { it.fieldType as ListFieldType }
            .filter { it.parameterFieldType is EnumFieldType }
            .map { it.parameterFieldType as EnumFieldType }
            .map { it.enumDef }

        enumFields.plus(listOfEnumFields).distinctBy { it.selectOptionsUqcn }.forEach { enumDef ->

            addImport(enumDef.selectOptionsTypescriptImport)

            blankLine()
            blankLine()
            appendLine("    protected readonly ${enumDef.selectOptionsUqcn} = ${enumDef.selectOptionsUqcn};")

        }

        append("""
            |
            |
            |    constructor() {
            |
            |        this.formGroup = new FormGroup({
            |""".trimMargin())

        formGroupFields.forEach { angularFormFieldDef ->
            FormControlRendererHelper.renderFormControlFor(
                angularFormFieldDef,
                CreateOrEdit.create,
                indentSize = 12,
                { line -> appendLine(line) },
                { fieldType -> addImportsFor(fieldType) }
            )
        }

        appendLine("        });")
        blankLine()
        appendLine("    }")

        append("""
            |
            |
            |    ngOnInit() {
            |
            |    }
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
            |        const requestDto = {
            |""".trimMargin())

        createApiDef.requestDtoDef.dtoFieldDefs.forEach { field ->
            val fieldName = field.classFieldDef.classFieldName
            appendLine("            ${fieldName}: this.formGroup.getRawValue().${fieldName},")
        }

        append("""
            |        } as ${createApiDef.requestDtoDef.uqcn};
            |
            |        this.formService.create(requestDto).subscribe({
            |            next: () => {
            |                this.onSave.emit();
            |            },
            |            error: err => {
            |                this.problemDetail.set(err.error);
            |            },
            |        });
            |
            |    }
            |
            |
            |    onCancelClicked(): void {
            |        this.onCancel.emit();
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}

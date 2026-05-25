package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.RequestDtoDef
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit
import org.maiaframework.gen.spec.definition.lang.BooleanFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanTypeFieldType
import org.maiaframework.gen.spec.definition.lang.BooleanValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.DataClassFieldType
import org.maiaframework.gen.spec.definition.lang.DomainIdFieldType
import org.maiaframework.gen.spec.definition.lang.DoubleFieldType
import org.maiaframework.gen.spec.definition.lang.EnumFieldType
import org.maiaframework.gen.spec.definition.lang.EsDocFieldType
import org.maiaframework.gen.spec.definition.lang.ForeignKeyFieldType
import org.maiaframework.gen.spec.definition.lang.FqcnFieldType
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


class EntityCreateFormComponentRenderer(
    private val requestDtoDef: RequestDtoDef,
    private val formGroupFields: List<AngularFormFieldDef>,
    formAngularComponentNames: AngularComponentNames,
    private val crudAngularComponentNames: AngularComponentNames,
) : AbstractAngularComponentRenderer(formAngularComponentNames) {


    init {

        `add imports`()

    }


    override fun renderComponentSource() {

        appendLine("export class $className implements OnInit {")

        append(
            """
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
            |    private readonly formService = inject(${crudAngularComponentNames.serviceName});
            |""".trimMargin()
        )

        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            blankLine()
            blankLine()
            appendLine("    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});")
        }

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

        enumFields.plus(listOfEnumFields).distinctBy { it.selectOptionsUqcn }.forEach { enumDef ->

            addImport(enumDef.selectOptionsTypescriptImport)

            blankLine()
            blankLine()
            appendLine("    protected readonly ${enumDef.selectOptionsUqcn} = ${enumDef.selectOptionsUqcn};")

        }

        `render constructor`()

        append(
            """
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
            |""".trimMargin()
        )

        requestDtoDef.dtoFieldDefs.forEach { field ->
            val fieldName = field.classFieldDef.classFieldName
            appendLine("            ${fieldName}: this.formGroup.getRawValue().${fieldName},")
        }

        append(
            """
            |        } as ${requestDtoDef.uqcn};
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
            |""".trimMargin()
        )

    }

    private fun `render constructor`() {
        append(
            """
                |
                |
                |    constructor() {
                |
                |        this.formGroup = new FormGroup({
                |""".trimMargin()
        )

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
        addImport(TypescriptImports.problemDetail)
        addImport(requestDtoDef.typescriptImport)
        addImport(crudAngularComponentNames.serviceTypescriptImport)
        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
        }

        addImportsForFieldTypes(formGroupFields)

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
                is InstantFieldType -> `add imports for date and time pickers`()
                is IntFieldType -> {}
                is IntTypeFieldType -> {}
                is IntValueClassFieldType -> {}
                is ListFieldType -> `add imports for ListFieldType`(fieldType)
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

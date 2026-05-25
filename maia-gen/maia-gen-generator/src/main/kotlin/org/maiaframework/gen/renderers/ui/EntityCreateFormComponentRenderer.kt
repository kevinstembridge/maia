package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.AngularComponentNames
import org.maiaframework.gen.spec.definition.AngularFormDef
import org.maiaframework.gen.spec.definition.AngularFormFieldDef
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
import org.maiaframework.lang.text.StringFunctions


class EntityCreateFormComponentRenderer(
    private val angularFormDef: AngularFormDef,
    formAngularComponentNames: AngularComponentNames,
    private val crudAngularComponentNames: AngularComponentNames,
    providerServices: List<String>,
    private val chipFields: List<ManyToManyChipFieldDef>
) : AbstractAngularComponentRenderer(
    formAngularComponentNames,
    providerServices
) {


    private val formGroupFields = this.angularFormDef.formModelFields


    private val requestDtoDef = this.angularFormDef.requestDtoDef


    private val allRequestDtoFields = this.requestDtoDef.dtoFieldDefs


    private val typeaheadDefs = this.angularFormDef.allTypeaheadDefs


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

        `render function onSubmit`()

        `render function onCancel`()

        append("""
            |
            |
            |}
            |""".trimMargin())

    }


    private fun `render class fields`() {

        append("""
            |
            |
            |    onSave = output();
            |
            |
            |    onCancel = output();
            |""".trimMargin()
        )

        `render class field for formService`()

        `render class field for problemDetail`()

        `render class fields for async validators`()

        `render class fields for typeahead fields`()

        `render class fields for typeahead services`()

        `render class fields for enum MatSelect fields`()

        `render class fields for chip fields`()

        `render class field for formGroup `()

    }


    private fun `render class field for formService`() {

        append("""
            |
            |
            |    private readonly formService = inject(${this.crudAngularComponentNames.serviceName});
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

        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->

            append("""
                |
                |
                |    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});
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

    }


    private fun `render class fields for typeahead fields`() {

        typeaheadDefs.forEach { typeaheadDef ->

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

            val serviceUqcn = StringFunctions.firstToLower(typeaheadDef.angularServiceClassName)

            append("""
                |
                |
                |
                |    private readonly $serviceUqcn = inject(${typeaheadDef.angularServiceClassName});
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
                |
                |
                |    @ViewChild('${chip.inputRefName}') ${chip.inputRefName}!: ElementRef<HTMLInputElement>;
                |""".trimMargin()
            )
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


    private fun `render function ngOnInit`() {

        append("""
            |
            |
            |    ngOnInit() {
            |        //TODO
            |    }
            |""".trimMargin())

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


    private fun `render function onSubmit`() {

        append(
            """
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
                |""".trimMargin()
        )

    }


    private fun `render function onCancel`() {

        append("""
            |
            |
            |    onCancelClicked(): void {
            |        this.onCancel.emit();
            |    }
            |""".trimMargin())


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

        this.typeaheadDefs.forEach { typeaheadDef ->
            addImport(typeaheadDef.typescriptServiceImport)
            addImport(typeaheadDef.esDocDef.dtoDef.typescriptDtoImport)
        }

        if (chipFields.isNotEmpty()) {
            addImport("@angular/core", "ElementRef")
            addImport("@angular/core", "ViewChild")
            addImport("@angular/material/autocomplete", "MatAutocompleteSelectedEvent")
            addImport("@angular/material/chips", "MatChipsModule", isModule = true)
            addImport("@angular/material/icon", "MatIconModule", isModule = true)
            chipFields.forEach { chip ->
                addImport(chip.serviceImport)
                addImport(chip.esDocImport)
            }
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

package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.renderers.FormControlRendererHelper
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.TypescriptImports
import org.maiaframework.gen.spec.definition.flags.CreateOrEdit


class EntityEditFormComponentRenderer(
    private val entityEditPageDef: EntityEditPageDef
) : AbstractAngularComponentRenderer(entityEditPageDef.editFormAngularComponentNames) {


    private val updateApiDef = entityEditPageDef.updateApiDef
    private val entityDef = entityEditPageDef.entityDef
    private val formGroupFields = updateApiDef.formGroupFields


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "OnInit")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "input")
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
        addImport(updateApiDef.requestDtoDef.typescriptImport)
        entityDef.fetchForEditDtoDef?.let {
            addImport("@angular/material/progress-spinner", "MatProgressSpinnerModule", isModule = true)
            addImport(it.typescriptImport)
        }
        addImport(entityDef.crudAngularComponentNames.serviceTypescriptImport)
        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            addImport(asyncValidatorDef.asyncValidatorTypescriptImport)
        }

    }


    override fun renderComponentSource() {

        appendLine("export class $className implements OnInit {")

        append("""
            |
            |
            |    entityId = input.required<string>();
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

        entityDef.fetchForEditDtoDef?.let {
            append("""
                |
                |
                |    loading = signal(true);
                |""".trimMargin())
        }

        formGroupFields.mapNotNull { it.asyncValidatorDef }.forEach { asyncValidatorDef ->
            blankLine()
            blankLine()
            appendLine("    private readonly ${asyncValidatorDef.validatorFieldName} = inject(${asyncValidatorDef.asyncValidatorName});")
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
                CreateOrEdit.edit,
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
            |""".trimMargin())

        entityDef.fetchForEditDtoDef?.let { fetchDef ->

            append("""
                |        this.formService.fetchForEdit(this.entityId()).subscribe({
                |            next: (dto: ${fetchDef.uqcn}) => {
                |                this.formGroup.patchValue({
                |""".trimMargin())

            formGroupFields.sortedBy { it.fieldName }.forEach { formField ->
                appendLine("                    ${formField.fieldName}: dto.${formField.fieldName},")
            }

            append("""
                |                });
                |                this.loading.set(false);
                |            },
                |            error: (err) => {
                |                this.problemDetail.set(err.error);
                |                this.loading.set(false);
                |            },
                |        });
                |""".trimMargin())

        }

        append("""
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

        updateApiDef.requestDtoDef.dtoFieldDefs.forEach { field ->
            val fieldName = field.classFieldDef.classFieldName
            appendLine("            ${fieldName}: this.formGroup.getRawValue().${fieldName},")
        }

        append("""
            |        } as ${updateApiDef.requestDtoDef.uqcn};
            |
            |        this.formService.edit(requestDto).subscribe({
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

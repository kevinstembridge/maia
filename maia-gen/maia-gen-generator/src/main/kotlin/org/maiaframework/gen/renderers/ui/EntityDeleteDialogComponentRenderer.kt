package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDeleteApiDef


class EntityDeleteDialogComponentRenderer(private val apiDef: EntityDeleteApiDef) : AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "Inject")
        addImport("@angular/core", "signal")

        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
        addImport("@angular/material/dialog", "MatDialog")
        addImport("@angular/material/dialog", "MatDialogActions")
        addImport("@angular/material/dialog", "MatDialogContent")
        addImport("@angular/material/dialog", "MatDialogRef")
        addImport("@angular/material/dialog", "MatDialogTitle")
        addImport("@angular/material/form-field", "MatFormFieldModule")
        addImport("@maia/maia-ui", "ProblemDetail")

        addImport(apiDef.entityDef.crudAngularComponentNames.serviceTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return apiDef.dialogComponentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |
            |
            |@Component({
            |    imports: [MatDialogTitle, MatDialogContent, MatFormFieldModule, MatDialogActions, MatButtonModule],
            |    selector: '${this.apiDef.angularDialogComponentSelector}',
            |    templateUrl: './${this.apiDef.angularDialogComponentHtmlFileName}'
            |})
            |export class ${this.apiDef.angularDialogComponentName} {
            |
            |
            |    message?: string;
            |
            |
            |    problemDetail = signal<ProblemDetail | null>(null);
            |
            |
            |    constructor(
            |        public dialogRef: MatDialogRef<${this.apiDef.angularDialogComponentName}>,
            |        @Inject(MAT_DIALOG_DATA) private dto: any,
            |        private crudService: ${apiDef.entityDef.crudAngularComponentNames.serviceName}
            |    ) {}
            |
            |
            |    onYes() {
            |        this.crudService.delete(this.dto.id).subscribe({
            |            next: (_) => {
            |                this.dialogRef.close(true);
            |            },
            |            error: (err) => {
            |                this.problemDetail.set(err.error);
            |            }
            |        });
            |    }
            |
            |
            |    onCancel() {
            |        this.dialogRef.close();
            |    }
            |
            |
            |}
        """.trimMargin())

    }


}

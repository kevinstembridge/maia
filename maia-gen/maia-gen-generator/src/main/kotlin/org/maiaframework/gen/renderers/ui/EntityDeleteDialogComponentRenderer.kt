package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDeleteApiDef


class EntityDeleteDialogComponentRenderer(private val apiDef: EntityDeleteApiDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return apiDef.dialogComponentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("""
            |import { Component, Inject, signal } from '@angular/core';
            |import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
            |${this.apiDef.entityDef.crudAngularComponentNames.serviceImportStatement}
            |import { ProblemDetail } from '@app/models/ProblemDetail';
            |import { MatButtonModule } from '@angular/material/button';
            |import { MatFormFieldModule } from '@angular/material/form-field';
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
            |    message: string;
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

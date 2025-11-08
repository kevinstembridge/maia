package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityDef
import org.maiaframework.gen.spec.definition.MaiaGenConstants


class CheckForeignKeyReferencesDialogComponentRenderer(private val entityDef: EntityDef) : AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return entityDef.checkForeignKeyReferencesDialog.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("import { Component, Inject, OnInit } from '@angular/core';")
        appendLine("import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';")
        appendLine("import { of } from 'rxjs';")
        appendLine("import { catchError, tap } from 'rxjs/operators';")
        appendLine(MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_IMPORT_STATEMENT)
        appendLine("import { MessageDetails } from '@app/components/message-panel/message-details';")
        appendLine("import { MatButtonModule } from '@angular/material/button';")
        appendLine("import { MessagePanelComponent } from '@app/components/message-panel/message-panel.component';")
        appendLine("import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';")
        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [MatDialogTitle, MatDialogContent, MatProgressSpinnerModule, MessagePanelComponent, MatDialogActions, MatButtonModule],")
        appendLine("    selector: '${this.entityDef.checkForeignKeyReferencesDialog.componentSelector}',")
        appendLine("    templateUrl: './${this.entityDef.checkForeignKeyReferencesDialog.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${this.entityDef.checkForeignKeyReferencesDialog.componentName} implements OnInit {")
        blankLine()
        appendLine("    checking: boolean;")
        appendLine("    messageDetails = new MessageDetails();")
        blankLine()
        appendLine("    constructor(")
        appendLine("        public dialogRef: MatDialogRef<${this.entityDef.checkForeignKeyReferencesDialog.componentName}>,")
        appendLine("        @Inject(MAT_DIALOG_DATA) private dto: any,")
        appendLine("        private foreignKeyReferenceService: ${MaiaGenConstants.FOREIGN_KEY_REFERENCE_SERVICE_CLASS_NAME}")
        appendLine("    ) {}")
        blankLine()
        blankLine()
        appendLine("    ngOnInit() {")
        blankLine()
        appendLine("        this.foreignKeyReferenceService.check${this.entityDef.entityBaseName}ForReferences(this.dto.id)")
        appendLine("          .pipe(")
        appendLine("            tap(() => this.checking = true),")
        appendLine("            catchError(err => {")
        appendLine("              this.messageDetails.setErrorDetailsFromHttpError(err);")
        appendLine("              return of(null);")
        appendLine("            }),")
        appendLine("            tap(() => this.checking = false)")
        appendLine("          ).subscribe(")
        appendLine("            res => {")
        appendLine("              if (res.exists) {")
        appendLine("                this.messageDetails.setErrorMessage('Foreign key references to entity ' + res.entityKey + ' exist.');")
        appendLine("              } else {")
        appendLine("                this.dialogRef.close(true);")
        appendLine("              }")
        appendLine("            }")
        appendLine("          );")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    onCancel() {")
        appendLine("        this.dialogRef.close();")
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}

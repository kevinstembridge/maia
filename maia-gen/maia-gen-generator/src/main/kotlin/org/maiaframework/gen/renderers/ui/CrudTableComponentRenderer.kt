package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudTableDef

class CrudTableComponentRenderer(
    private val crudTableDef: CrudTableDef,
    private val entityIsReferencedByForeignKeys: Boolean
): AbstractTypescriptRenderer() {


    override fun renderedFilePath(): String {

        return this.crudTableDef.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("import { Component, ViewChild } from '@angular/core';")
        appendLine("import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';")
        appendLine(this.crudTableDef.crudServiceImportStatement)
        appendLine(this.crudTableDef.tableComponentImportStatement)
        this.crudTableDef.createDialogComponentImportStatement?.let { appendLine(it) }
        this.crudTableDef.deleteDialogComponentImportStatement?.let {
            appendLine(it)
            if (this.entityIsReferencedByForeignKeys) {
                appendLine(this.crudTableDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentImportStatement)
            }
        }
        this.crudTableDef.editDialogComponentImportStatement?.let { appendLine(it) }
        appendLine(this.crudTableDef.searchableDtoImportStatement)

        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [${this.crudTableDef.dtoTableComponent.componentName}],")
        appendLine("    selector: '${crudTableDef.crudTableComponentSelector}',")
        appendLine("    templateUrl: './${crudTableDef.crudTableComponentHtmlFileName}'")
        appendLine("})")
        appendLine("export class ${crudTableDef.crudTableComponentClassName} {")
        blankLine()
        appendLine("    @ViewChild(${crudTableDef.dtoTableComponent.componentName}) tableComponent: ${crudTableDef.dtoTableComponent.componentName};")
        blankLine()
        blankLine()
        appendLine("    constructor(")
        appendLine("        private crudService: ${crudTableDef.crudServiceClassName},")
        appendLine("        public dialog: MatDialog")
        appendLine("    ) {}")

        crudTableDef.entityCrudApiDef.createApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    onAddButtonClicked() {")
            blankLine()
            appendLine("        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {")
            appendLine("            width: '400px'")
            appendLine("        });")
            blankLine()
            appendLine("        dialogRef.afterClosed().subscribe((result) => {")
            appendLine("            if (result) {")
            appendLine("                this.tableComponent.reapplyFilters();")
            appendLine("            }")
            appendLine("        });")
            blankLine()
            appendLine("    }")

        }

        crudTableDef.entityCrudApiDef.updateApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    onEdit(dto: ${crudTableDef.dtoHtmlTableDef.dtoUqcn}) {")
            blankLine()
            appendLine("        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {")
            appendLine("            width: '400px',")
            appendLine("            data: dto")
            appendLine("        });")
            blankLine()
            appendLine("        dialogRef.afterClosed().subscribe(result => {")
            appendLine("            if (result) {")
            appendLine("                this.tableComponent.reapplyFilters();")
            appendLine("            }")
            appendLine("        });")
            blankLine()
            appendLine("    }")
        }

        crudTableDef.entityCrudApiDef.deleteApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    onDelete(dto: ${crudTableDef.dtoHtmlTableDef.dtoUqcn}) {")

            if (this.entityIsReferencedByForeignKeys) {
                blankLine()
                appendLine("        const checkForeignKeyReferencesDialogRef = this.dialog.open(${this.crudTableDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentName}, {")
                appendLine("            width: '500px',")
                appendLine("            data: dto")
                appendLine("        });")
                blankLine()
                appendLine("        checkForeignKeyReferencesDialogRef.afterClosed().subscribe(result => {")
                appendLine("            if (result) {")
                appendLine("                this.displayDeleteDialog(dto);")
                appendLine("            }")
                appendLine("        });")
                blankLine()
                appendLine("    }")
                blankLine()
                blankLine()
                appendLine("    private displayDeleteDialog(dto: ${crudTableDef.dtoHtmlTableDef.dtoUqcn}) {")
            }

            appendLine("""
                |
                |        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentName}, {
                |            width: '400px',
                |            data: dto
                |        });
                |
                |        dialogRef.afterClosed().subscribe(result => {
                |            if (result) {
                |                this.tableComponent.reapplyFilters();
                |            }
                |        });
                |
                |    }
            """.trimMargin())

        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}

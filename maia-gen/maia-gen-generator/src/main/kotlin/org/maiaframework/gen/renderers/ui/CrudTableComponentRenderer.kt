package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudTableDef

class CrudTableComponentRenderer(
    private val crudTableDef: CrudTableDef,
    private val entityIsReferencedByForeignKeys: Boolean
): AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "ViewChild")
        addImport("@angular/material/dialog", "MatDialog")
        addImport("@angular/material/dialog", "MatDialogRef")
        addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
        addImport(crudTableDef.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceTypescriptImport)
        addImport(crudTableDef.dtoHtmlTableDef.tableComponent.componentTypescriptImport)
        crudTableDef.entityCrudApiDef.createApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        crudTableDef.entityCrudApiDef.deleteApiDef?.let {
            addImport(it.angularDialogComponentTypescriptImport)
            if (entityIsReferencedByForeignKeys) {
                addImport(crudTableDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentTypescriptImport)
            }
        }
        crudTableDef.entityCrudApiDef.updateApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        addImport(crudTableDef.dtoHtmlTableDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.crudTableDef.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [${this.crudTableDef.dtoTableComponent.componentName}],")
        appendLine("    selector: '${crudTableDef.crudTableComponentSelector}',")
        appendLine("    templateUrl: './${crudTableDef.crudTableComponentHtmlFileName}'")
        appendLine("})")
        appendLine("export class ${crudTableDef.crudTableComponentClassName} {")
        blankLine()
        appendLine("    @ViewChild(${crudTableDef.dtoTableComponent.componentName}) tableComponent!: ${crudTableDef.dtoTableComponent.componentName};")
        blankLine()
        blankLine()
        appendLine("    private readonly crudService = inject(${crudTableDef.crudServiceClassName});")
        blankLine()
        blankLine()
        appendLine("    private readonly dialog = inject(MatDialog);")

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

            val entityDef = crudTableDef.entityCrudApiDef.entityDef
            val entityIdExpression = if (entityDef.hasCompositePrimaryKey) {
                val parts = entityDef.primaryKeyFields.joinToString("/") { "\${dto.${it.classFieldName.value}}" }
                "`$parts`"
            } else {
                "dto.id"
            }

            blankLine()
            blankLine()
            appendLine("    onEdit(dto: ${crudTableDef.dtoHtmlTableDef.dtoUqcn}) {")
            blankLine()
            appendLine("        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {")
            appendLine("            width: '400px',")
            appendLine("            data: $entityIdExpression")
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

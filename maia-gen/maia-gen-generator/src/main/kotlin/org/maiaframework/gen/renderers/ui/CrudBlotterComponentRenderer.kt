package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudBlotterDef

class CrudBlotterComponentRenderer(
    private val crudBlotterDef: CrudBlotterDef,
    private val entityIsReferencedByForeignKeys: Boolean
): AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "ViewChild")
        addImport("@angular/material/dialog", "MatDialog")
        addImport("@angular/material/dialog", "MatDialogRef")
        addImport("@angular/material/dialog", "MAT_DIALOG_DATA")
        addImport(crudBlotterDef.entityCrudApiDef.entityDef.crudAngularComponentNames.serviceTypescriptImport)
        addImport(crudBlotterDef.blotterDef.blotterComponent.componentTypescriptImport)
        crudBlotterDef.entityCrudApiDef.createApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        crudBlotterDef.entityCrudApiDef.deleteApiDef?.let {
            addImport(it.angularDialogComponentTypescriptImport)
            if (entityIsReferencedByForeignKeys) {
                addImport(crudBlotterDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentTypescriptImport)
            }
        }
        crudBlotterDef.entityCrudApiDef.updateApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        addImport(crudBlotterDef.blotterDef.dtoDef.typescriptDtoImport)

    }


    override fun renderedFilePath(): String {

        return this.crudBlotterDef.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [${this.crudBlotterDef.dtoBlotterComponent.componentName}],")
        appendLine("    selector: '${crudBlotterDef.crudBlotterComponentSelector}',")
        appendLine("    templateUrl: './${crudBlotterDef.crudBlotterComponentHtmlFileName}'")
        appendLine("})")
        appendLine("export class ${crudBlotterDef.crudBlotterComponentClassName} {")
        blankLine()
        appendLine("    @ViewChild(${crudBlotterDef.dtoBlotterComponent.componentName}) blotterComponent!: ${crudBlotterDef.dtoBlotterComponent.componentName};")
        blankLine()
        blankLine()
        appendLine("    private readonly crudService = inject(${crudBlotterDef.crudServiceClassName});")
        blankLine()
        blankLine()
        appendLine("    private readonly dialog = inject(MatDialog);")

        crudBlotterDef.entityCrudApiDef.createApiDef?.let { apiDef ->

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
            appendLine("                this.blotterComponent.reapplyFilters();")
            appendLine("            }")
            appendLine("        });")
            blankLine()
            appendLine("    }")

        }

        crudBlotterDef.entityCrudApiDef.updateApiDef?.let { apiDef ->

            val entityDef = crudBlotterDef.entityCrudApiDef.entityDef
            val entityIdExpression = if (entityDef.hasCompositePrimaryKey) {
                val parts = entityDef.primaryKeyFields.joinToString(", ") { "${it.classFieldName.value}: dto.${it.classFieldName.value}" }
                "{$parts}"
            } else {
                "dto.id"
            }

            blankLine()
            blankLine()
            appendLine("    onEdit(dto: ${crudBlotterDef.blotterDef.dtoUqcn}) {")
            blankLine()
            appendLine("        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {")
            appendLine("            width: '400px',")
            appendLine("            data: $entityIdExpression")
            appendLine("        });")
            blankLine()
            appendLine("        dialogRef.afterClosed().subscribe(result => {")
            appendLine("            if (result) {")
            appendLine("                this.blotterComponent.reapplyFilters();")
            appendLine("            }")
            appendLine("        });")
            blankLine()
            appendLine("    }")
        }

        crudBlotterDef.entityCrudApiDef.deleteApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    onDelete(dto: ${crudBlotterDef.blotterDef.dtoUqcn}) {")

            if (this.entityIsReferencedByForeignKeys) {
                blankLine()
                appendLine("        const checkForeignKeyReferencesDialogRef = this.dialog.open(${this.crudBlotterDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentName}, {")
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
                appendLine("    private displayDeleteDialog(dto: ${crudBlotterDef.blotterDef.dtoUqcn}) {")
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
                |                this.blotterComponent.reapplyFilters();
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

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
        addImport("@angular/router", "Router")
        addImport("@angular/material/dialog", "MatDialog")
        addImport(crudBlotterDef.blotterDef.blotterComponent.componentTypescriptImport)
        addImport(crudBlotterDef.blotterDef.dtoDef.typescriptDtoImport)
        crudBlotterDef.entityCrudApiDef.createApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        crudBlotterDef.entityCrudApiDef.deleteApiDef?.let {
            addImport(it.angularDialogComponentTypescriptImport)
            if (entityIsReferencedByForeignKeys) {
                addImport(crudBlotterDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentTypescriptImport)
            }
        }
        if (!crudBlotterDef.blotterDef.hasEditEntityPage) {
            crudBlotterDef.entityCrudApiDef.updateApiDef?.let { addImport(it.angularDialogComponentNames.componentTypescriptImport) }
        }

    }


    override fun renderedFilePath(): String {

        return this.crudBlotterDef.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        append("""
            |
            |@Component({
            |    imports: [${this.crudBlotterDef.dtoBlotterComponent.componentName}],
            |    selector: '${crudBlotterDef.crudBlotterComponentSelector}',
            |    templateUrl: './${crudBlotterDef.crudBlotterComponentHtmlFileName}'
            |})
            |export class ${crudBlotterDef.crudBlotterComponentClassName} {
            |
            |
            |    @ViewChild(${crudBlotterDef.dtoBlotterComponent.componentName}) blotterComponent!: ${crudBlotterDef.dtoBlotterComponent.componentName};
            |
            |
            |    private readonly dialog = inject(MatDialog);
            |""".trimMargin())

        val needsRouter = crudBlotterDef.blotterDef.hasViewActionColumn ||
            (crudBlotterDef.blotterDef.hasEditEntityPage && crudBlotterDef.entityCrudApiDef.updateApiDef != null)

        if (needsRouter) {

            append("""
                |
                |
                |    private readonly router = inject(Router);
                |""".trimMargin())

        }

        crudBlotterDef.entityCrudApiDef.createApiDef?.let { apiDef ->

            append("""
                |
                |
                |    onAddButtonClicked(): void {
                |
                |        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {
                |            width: '400px'
                |        });
                |
                |        dialogRef.afterClosed().subscribe((result) => {
                |            if (result) {
                |                this.blotterComponent.reapplyFilters();
                |            }
                |        });
                |
                |    }
                |""".trimMargin())

        }

        if (crudBlotterDef.blotterDef.hasViewActionColumn) {

            append("""
                |
                |
                |    onView(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {
                |
                |        this.router.navigate(['${crudBlotterDef.blotterDef.searchableDtoDef!!.dtoRootEntityDef.viewEntityUrl}', dto.id]);
                |
                |    }
                |""".trimMargin()
            )

        }

        crudBlotterDef.entityCrudApiDef.updateApiDef?.let { apiDef ->

            val entityDef = crudBlotterDef.entityCrudApiDef.entityDef

            if (crudBlotterDef.blotterDef.hasEditEntityPage) {

                append("""
                    |
                    |
                    |    onEdit(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {
                    |
                    |        this.router.navigate(['${entityDef.editEntityUrl}', dto.id]);
                    |
                    |    }
                    |""".trimMargin())

            } else {

                val entityIdExpression = if (entityDef.hasCompositePrimaryKey) {
                    val parts = entityDef.primaryKeyFieldsSorted.joinToString(", ") { "${it.classFieldName.value}: dto.${it.classFieldName.value}" }
                    "{$parts}"
                } else {
                    "dto.id"
                }

                append("""
                    |
                    |
                    |    onEdit(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {
                    |
                    |        const dialogRef = this.dialog.open(${apiDef.angularDialogComponentNames.componentName}, {
                    |            width: '400px',
                    |            data: $entityIdExpression
                    |        });
                    |
                    |        dialogRef.afterClosed().subscribe(result => {
                    |            if (result) {
                    |                this.blotterComponent.reapplyFilters();
                    |            }
                    |        });
                    |
                    |    }
                    |""".trimMargin())

            }

        }

        crudBlotterDef.entityCrudApiDef.deleteApiDef?.let { apiDef ->

            blankLine()
            blankLine()
            appendLine("    onDelete(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {")

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

            append("""
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
                |""".trimMargin())

        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}

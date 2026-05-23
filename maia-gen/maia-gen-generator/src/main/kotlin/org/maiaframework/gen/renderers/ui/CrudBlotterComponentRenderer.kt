package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.CrudBlotterDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef

class CrudBlotterComponentRenderer(
    private val crudBlotterDef: CrudBlotterDef,
    private val entityIsReferencedByForeignKeys: Boolean,
    private val entityDetailViewDef: EntityDetailViewDef?,
    private val entityEditPageDef: EntityEditPageDef?,
    private val entityCreatePageDef: EntityCreatePageDef?
): AbstractTypescriptRenderer() {


    init {

        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "ViewChild")
        addImport("@angular/router", "Router")
        addImport("@angular/material/dialog", "MatDialog")
        addImport(crudBlotterDef.blotterDef.blotterComponent.componentTypescriptImport)
        addImport(crudBlotterDef.blotterDef.dtoDef.typescriptDtoImport)
        crudBlotterDef.entityCrudApiDef.deleteApiDef?.let {
            addImport(it.angularDialogComponentTypescriptImport)
            if (entityIsReferencedByForeignKeys) {
                addImport(crudBlotterDef.entityCrudApiDef.entityDef.checkForeignKeyReferencesDialog.componentTypescriptImport)
            }
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

        val needsRouter = (entityDetailViewDef != null && crudBlotterDef.blotterDef.hasViewActionColumn)
                || entityEditPageDef != null
                || entityCreatePageDef != null

        if (needsRouter) {

            append("""
                |
                |
                |    private readonly router = inject(Router);
                |""".trimMargin())

        }

        if (entityCreatePageDef != null) {

            append("""
                |
                |
                |    onAddButtonClicked(): void {
                |
                |        this.router.navigate(['${entityCreatePageDef.entityDef.createEntityPageUrl}']);
                |
                |    }
                |""".trimMargin()
            )

        }


        if (entityDetailViewDef != null && crudBlotterDef.blotterDef.hasViewActionColumn) {

            append("""
                |
                |
                |    onView(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {
                |
                |        this.router.navigate(['${crudBlotterDef.blotterDef.searchableDtoDef!!.dtoRootEntityDef.viewEntityPageUrl}', dto.id]);
                |
                |    }
                |""".trimMargin()
            )

        }

        if (entityEditPageDef != null) {

            append("""
                |
                |
                |    onEdit(dto: ${crudBlotterDef.blotterDef.dtoUqcn}): void {
                |
                |        this.router.navigate(['${entityEditPageDef.entityDef.editEntityPageUrl}', dto.id]);
                |
                |    }
                |""".trimMargin())

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

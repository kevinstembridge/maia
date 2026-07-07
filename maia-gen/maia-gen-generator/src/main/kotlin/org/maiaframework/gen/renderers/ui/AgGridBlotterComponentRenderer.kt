package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.ActionName
import org.maiaframework.gen.spec.definition.AgGridCellRendererDefs
import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.BlotterActionColumnDef
import org.maiaframework.gen.spec.definition.BlotterColumnDef
import org.maiaframework.gen.spec.definition.BlotterCompositePkColumnDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.EntityCreatePageDef
import org.maiaframework.gen.spec.definition.EntityDetailViewDef
import org.maiaframework.gen.spec.definition.EntityEditPageDef
import org.maiaframework.gen.spec.definition.Pipes
import org.maiaframework.gen.spec.definition.flags.SearchModelType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType


class AgGridBlotterComponentRenderer(
    private val blotterDef: BlotterDef,
    private val entityDetailViewDef: EntityDetailViewDef?,
    private val entityEditPageDef: EntityEditPageDef?,
    entityCreatePageDef: EntityCreatePageDef?,
    private val entityIsReferencedByForeignKeys: Boolean,
    authoritiesDef: AuthoritiesDef?
) : AbstractTypescriptRenderer() {


    val requiresRouter = (entityDetailViewDef != null && blotterDef.hasViewActionColumn)
            || entityEditPageDef != null
            || entityCreatePageDef != null


    val requiresDialog = blotterDef.hasDeleteActionColumn


    init {

        if (requiresRouter) {
            addImport("@angular/router", "Router")
        }

        blotterDef.blotterColumnDefs
            .mapNotNull { it.cellRenderer }
            .map { it.typescriptImport }
            .toSet()
            .forEach { addImport(it) }

        if (blotterDef.blotterColumnDefs.any { col ->
            col is BlotterColumnDef && col.fieldType is ListFieldType && (col.fieldType as ListFieldType).parameterFieldType is PkAndNameFieldType
        }) {
            addImport(AgGridCellRendererDefs.chips.typescriptImport)
        }

        addImport("@angular/common", "DecimalPipe")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "EnvironmentInjector")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "runInInjectionContext")

        authoritiesDef?.let {
            addImport(it.authServiceTypescriptImport)
            addImport(it.enumDef.typescriptImport)
        }

        addImport(blotterDef.blotterComponent.serviceTypescriptImport)
        addImport(blotterDef.dtoDef.typescriptDtoImport)
        addImport("@angular/material/icon", "MatIconModule")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/forms", "FormsModule")
        addImport("ag-grid-angular", "AgGridAngular")
        addImport("@app/themes/ag-grid-theme", "agGridTheme")

        if (blotterDef.clickableBlotterRowDef != null) {
            addImport("ag-grid-community", "CellClickedEvent")
        }
        addImport("ag-grid-community", "ColDef")
        if (blotterDef.hasDateTimeStringColumn) {
            addImport("ag-grid-community", "DataTypeDefinitions")
            addImport("luxon", "DateTime")
        }
        addImport("ag-grid-community", "FilterModel")
        addImport("ag-grid-community", "GridApi")
        addImport("ag-grid-community", "GridReadyEvent")
        addImport("ag-grid-community", "ICellRendererParams")
        addImport("ag-grid-community", "RowModelType")
        addImport(blotterDef.blotterComponent.agGridDatasourceTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return this.blotterDef.blotterComponent.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        `render @Component decorator`()

        appendLine("export class ${this.blotterDef.blotterComponent.componentName} {")

        `render dataTypeDefinitions field`()

        `render colDefs field`()

        append("""
            |
            |    public defaultColDef: ColDef = {
            |        filter: true,
            |        flex: 1,
            |        floatingFilter: true,
            |        minWidth: 100,
            |        sortable: true
            |    };
            |
            |
            |    public rowBuffer = 0;
            |
            |
            |    public rowSelection = {
            |         mode: 'singleRow' as const,
            |         checkboxes: false,
            |    };
            |
            |
            |    public agGridTheme = agGridTheme;
            |
            |
            |    public rowModelType: RowModelType = 'infinite';
            |
            |
            |    public cacheBlockSize = 100;
            |
            |
            |    public cacheOverflowSize = 2;
            |
            |
            |    public maxConcurrentDatasourceRequests = 1;
            |
            |
            |    public infiniteInitialRowCount = 1000;
            |
            |
            |    public maxBlocksInCache = 10;
            |
            |
            |    public rowData!: ${blotterDef.dtoUqcn}[];
            |
            |
            |    private gridApi!: GridApi<${blotterDef.dtoUqcn}>;
            |
            |
            |    private readonly datasource = inject(${blotterDef.agGridDatasourceClassName});
            |
            |
            |    private readonly authService = inject(AuthService);
            |
            |
            |    private readonly injector = inject(EnvironmentInjector);
            |""".trimMargin()
        )

        `render router field`()

        `render dialog field`()

        `render onGridReady function`()

        `render functions for Add button`()

        `render onView function`()

        `render onEdit function`()

        `render onDelete function`()

        `render handler functions for action columns`()

        `render onCellClicked function for clickable row`()

        append("""
            |
            |
            |    private reapplyFilters() {
            |
            |        runInInjectionContext(this.injector, () => {
            |            this.gridApi.onFilterChanged();
            |        });
            |
            |    }
            |
            |
            |    getFilterModel(): FilterModel {
            |
            |        return this.gridApi.getFilterModel();
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


    private fun `render handler functions for action columns`() {

        this.blotterDef.actionColumnFields
            .filterNot { it.actionName == ActionName.view }
            .filterNot { it.actionName == ActionName.edit }
            .filterNot { it.actionName == ActionName.delete }
            .forEach { actionColumnDef ->
                append(
                    """
                        |
                        |
                        |    on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.blotterDef.dtoUqcn}) {
                        |
                        |        this.${actionColumnDef.actionName}.emit(dto);
                        |
                        |    }
                        |""".trimMargin()
                )
            }

    }


    private fun `render onCellClicked function for clickable row`() {

        this.blotterDef.clickableBlotterRowDef?.let { clickableTableRowDef ->

            blankLine()
            blankLine()
            appendLine("    onCellClicked(event: CellClickedEvent) {")

            clickableTableRowDef.routerNavigationArgs?.let { args ->
                val argsFormatted = args.joinToString(prefix = "[", separator = ", ", postfix = "]")
                appendLine("        this.router.navigate($argsFormatted);")
            }

            appendLine("    }")
        }

    }


    private fun `render @Component decorator`() {

        append(
            """
                |
                |
                |@Component({
                |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
                |    providers: [${this.blotterDef.angularBlotterServiceName}, ${this.blotterDef.agGridDatasourceClassName}, DecimalPipe],
                |    selector: '${this.blotterDef.blotterComponent.componentSelector}',
                |    templateUrl: './${this.blotterDef.blotterComponent.htmlFileName}'
                |""".trimMargin()
        )

        if (this.blotterDef.searchModelType == SearchModelType.MAIA) {
            appendLine("    styleUrls: ['./${this.blotterDef.blotterComponentScssFileName}'],")
        }

        appendLine("})")

    }


    private fun `render dataTypeDefinitions field`() {

        if (!blotterDef.hasDateTimeStringColumn) {
            return
        }

        append(
            $$"""
                |
                |
                |    public dataTypeDefinitions = {
                |        dateTimeString: {
                |            baseDataType: 'dateTimeString',
                |            extendsDataType: 'dateTimeString',
                |            valueFormatter: params => {
                |                if (!params.value) return '';
                |                return DateTime.fromISO(params.value).toFormat('$${Pipes.INSTANT_DATE_FORMAT}');
                |            },
                |        }
                |    } satisfies DataTypeDefinitions;
                |""".trimMargin()
        )

    }


    private fun `render colDefs field`() {

        blankLine()
        blankLine()
        appendLine("    public columnDefs: ColDef[] = [")

        this.blotterDef.blotterColumnDefs.forEach { fieldDef ->
            when (fieldDef) {
                is BlotterActionColumnDef -> renderColDefFor(fieldDef)
                is BlotterColumnDef -> appendLine("        ${renderColDefFor(fieldDef)},")
                is BlotterCompositePkColumnDef -> renderColDefFor(fieldDef)
            }
        }

        appendLine("    ];")

    }


    private fun `render router field`() {

        if (requiresRouter) {
            blankLine()
            blankLine()
            appendLine("    private readonly router = inject(Router);")
        }

    }


    private fun `render dialog field`() {

        if (requiresDialog) {

            addImport("@angular/material/dialog", "MatDialog")

            blankLine()
            blankLine()
            appendLine("    private readonly dialog = inject(MatDialog);")
        }

    }


    private fun `render onGridReady function`() {

        append(
            """
                |
                |
                |    onGridReady(params: GridReadyEvent<${blotterDef.dtoUqcn}>) {
                |
                |        this.gridApi = params.api;
                |        params.api?.setGridOption('datasource', this.datasource);
                |
                |    }
                |""".trimMargin()
        )

    }


    private fun `render functions for Add button`() {

        this.blotterDef.entityCreatePageDef?.let { entityCreatePageDef ->

            append(
                """
                    |
                    |
                    |    get addButtonVisible(): boolean {
                    |
                    |""".trimMargin()
            )

            val authority = entityCreatePageDef.authority

            if (authority != null) {
                appendLine("        return this.authService.currentUserHasThisAuthority(Authority.${authority.name});")
            } else {
                appendLine("        return true;")
            }

            blankLine()
            appendLine("    }")
            blankLine()
            blankLine()
            appendLine("    onAddButtonClicked(): void {")
            blankLine()
            appendLine("        this.router.navigate(['${entityCreatePageDef.createPageUrl}']);")
            blankLine()
            appendLine("    }")
        }

    }


    private fun `render onView function`() {

        if (blotterDef.hasViewActionColumn) {

            check(entityDetailViewDef != null) { "EntityDetailViewDef must be set if blotterDef has view action column. ${blotterDef.blotterComponent.componentName}" }

            append(
                """
                    |
                    |
                    |    private onView(dto: ${blotterDef.dtoUqcn}): void {
                    |
                    |        this.router.navigate(['${entityDetailViewDef.viewPageUrl}', dto.id]);
                    |
                    |    }
                    |""".trimMargin()
            )

        }

    }


    private fun `render onEdit function`() {

        if (blotterDef.hasEditActionColumn) {

            check(entityEditPageDef != null) { "EntityEditPageDef must be set if blotterDef has edit action column. ${blotterDef.blotterComponent.componentName}" }

            append(
                """
                    |
                    |
                    |    private onEdit(dto: ${blotterDef.dtoUqcn}): void {
                    |
                    |        this.router.navigate(['${entityEditPageDef.entityDef.editEntityPageUrl}', dto.id]);
                    |
                    |    }
                    |""".trimMargin()
            )

        }

    }


    private fun `render onDelete function`() {

        if (blotterDef.hasDeleteActionColumn) {

            blankLine()
            blankLine()
            appendLine("    private onDelete(dto: ${blotterDef.dtoUqcn}): void {")

            if (this.entityIsReferencedByForeignKeys) {

                addImport(this.blotterDef.checkForeignKeyReferencesDialogComponentNames.componentTypescriptImport)

                blankLine()
                appendLine("        const checkForeignKeyReferencesDialogRef = this.dialog.open(${this.blotterDef.checkForeignKeyReferencesDialogComponentNames.componentName}, {")
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
                appendLine("    private displayDeleteDialog(dto: ${blotterDef.dtoUqcn}) {")
            }

            addImport(this.blotterDef.angularDeleteDialogComponentNames.componentTypescriptImport)

            append(
                """
                    |
                    |        const dialogRef = this.dialog.open(${this.blotterDef.angularDeleteDialogComponentNames.componentName}, {
                    |            width: '400px',
                    |            data: dto
                    |        });
                    |
                    |        dialogRef.afterClosed().subscribe(result => {
                    |            if (result) {
                    |                this.reapplyFilters();
                    |            }
                    |        });
                    |
                    |    }
                    |""".trimMargin()
            )

        }

    }


    private fun renderColDefFor(blotterColumnDef: BlotterColumnDef): String {

        val attributes = mutableMapOf<String, Any>()

        attributes["field"] = "'${blotterColumnDef.dtoFieldName}'"

        val columnHeader = blotterColumnDef.columnHeader
            ?: throw IllegalStateException(
                "Field '${blotterColumnDef.dtoFieldName}' in table DTO '${blotterDef.dtoBaseName}' has no columnHeader (fieldDisplayName). " +
                "Add a fieldDisplayName to the entity/EsDoc field, or override the header at the columnFromDto() call site."
            )

        attributes["headerName"] = "'${columnHeader.replace("'", "\\'")}'"

        attributes["cellDataType"] = "'${blotterColumnDef.agGridCellDateType.name}'"

        if (blotterColumnDef.isFilterable) {
            attributes["filter"] = true
        }

        if (blotterColumnDef.hide) {
            attributes["hide"] = true
        }

        if (blotterColumnDef.fieldType is ListFieldType) {
            val listFieldType = blotterColumnDef.fieldType as ListFieldType
            if (listFieldType.parameterFieldType is PkAndNameFieldType) {
                attributes["cellRenderer"] = AgGridCellRendererDefs.chips.componentClassName
            } else {
                attributes["valueFormatter"] = "(params) => params.value?.join(', ') ?? ''"
            }
        }

        val keyValues = attributes.map { "${it.key}: ${it.value}" }

        return keyValues.joinToString(prefix = "{ ", separator = ", ", postfix = " }")

    }


    private fun renderColDefFor(fieldDef: BlotterActionColumnDef) {

        append("""
            |        {
            |            field: '${fieldDef.actionName}',
            |            headerName: '${fieldDef.columnHeader}',
            |            width: 100,
            |            maxWidth: 100,
            |            filter: false,
            |            cellRenderer: ${fieldDef.cellRenderer!!.componentClassName},
            |            cellRendererParams: { iconName: '${fieldDef.icon}' },
            |            onCellClicked: event => {
            |                this.on${fieldDef.actionName.firstToUpper()}(event.data);
            |            }
            |        },
            |""".trimMargin())

    }


    private fun renderColDefFor(fieldDef: BlotterCompositePkColumnDef) {

        appendLine("        { field: '${fieldDef.colId}', headerName: '${fieldDef.columnHeader}', filter: ${fieldDef.filter}, hide: ${fieldDef.hide} },")

    }


}

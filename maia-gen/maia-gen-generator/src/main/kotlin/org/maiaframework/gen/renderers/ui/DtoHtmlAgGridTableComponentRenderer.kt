package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableActionColumnDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableColumnDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef
import org.maiaframework.gen.spec.definition.SearchModelType


class DtoHtmlAgGridTableComponentRenderer(
    private val dtoHtmlTableDef: DtoHtmlTableDef,
    private val authoritiesDef: AuthoritiesDef?
) : AbstractTypescriptRenderer() {


    private val requiresRouter = dtoHtmlTableDef.clickableTableRowDef != null


    init {

        if (requiresRouter) {
            addImport("@angular/router", "Router")
        }

        dtoHtmlTableDef.dtoHtmlTableColumnDefs
            .mapNotNull { it.cellRenderer }
            .map { it.typescriptImport }
            .toSet()
            .forEach { addImport(it) }

        addImport("@angular/common", "DecimalPipe")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "output")

        authoritiesDef?.let {
            addImport(it.authServiceTypescriptImport)
            addImport(it.enumDef.typescriptImport)
        }

        addImport(dtoHtmlTableDef.tableComponent.serviceTypescriptImport)
        addImport(dtoHtmlTableDef.dtoDef.typescriptDtoImport)
        addImport("@angular/material/icon", "MatIconModule")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/forms", "FormsModule")
        addImport("ag-grid-angular", "AgGridAngular")
        addImport("@app/themes/ag-grid-theme", "agGridTheme")

        if (dtoHtmlTableDef.clickableTableRowDef != null) {
            addImport("ag-grid-community", "CellClickedEvent")
        }
        addImport("ag-grid-community", "ColDef")
        addImport("ag-grid-community", "FilterModel")
        addImport("ag-grid-community", "GridApi")
        addImport("ag-grid-community", "GridReadyEvent")
        addImport("ag-grid-community", "ICellRendererParams")
        addImport("ag-grid-community", "RowModelType")
        addImport(dtoHtmlTableDef.tableComponent.agGridDatasourceTypescriptImport)

    }


    override fun renderedFilePath(): String {

        return this.dtoHtmlTableDef.tableComponent.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine(
            """
            |
            |
            |@Component({
            |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
            |    providers: [${this.dtoHtmlTableDef.angularTableServiceName}, DecimalPipe],
            |    selector: '${this.dtoHtmlTableDef.tableComponent.componentSelector}',
            |    templateUrl: './${this.dtoHtmlTableDef.tableComponent.htmlFileName}'
        """.trimMargin())

        if (this.dtoHtmlTableDef.searchModelType == SearchModelType.MAIA) {
            appendLine("    styleUrls: ['./${this.dtoHtmlTableDef.tableComponentScssFileName}'],")
        }

        appendLine("})")
        appendLine("export class ${this.dtoHtmlTableDef.tableComponent.componentName} {")

        this.dtoHtmlTableDef.actionColumnFields.forEach { actionColumnDef ->
            blankLine()
            blankLine()
            appendLine("    readonly ${actionColumnDef.actionName} = output<${this.dtoHtmlTableDef.dtoUqcn}>();")
        }

        if (this.dtoHtmlTableDef.addButtonDef != null) {
            blankLine()
            blankLine()
            appendLine("    readonly addButtonClicked = output();")
        }

        blankLine()
        blankLine()
        appendLine("    public columnDefs: ColDef[] = [")

        this.dtoHtmlTableDef.dtoHtmlTableColumnDefs.forEach { fieldDef ->
            when (fieldDef) {
                is DtoHtmlTableColumnDef -> appendLine("        ${renderColDefFor(fieldDef)},")
                is DtoHtmlTableActionColumnDef -> renderColDefFor(fieldDef)
            }
        }

        appendLine("    ];")
        appendLine("""
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
            |         mode: 'multiRow' as const,
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
            |    public rowData!: ${dtoHtmlTableDef.dtoUqcn}[];
            |
            |
            |    private gridApi!: GridApi<${dtoHtmlTableDef.dtoUqcn}>;
            |
            |
            |    private readonly datasource = inject(${dtoHtmlTableDef.agGridDatasourceClassName});""".trimMargin()
        )

        if (requiresRouter) {
            blankLine()
            blankLine()
            appendLine("    private readonly router = inject(Router);")
        }

        append("""
            |
            |
            |    private readonly authService = inject(AuthService);
            |
            |
            |    onGridReady(params: GridReadyEvent<${dtoHtmlTableDef.dtoUqcn}>) {
            |
            |        this.gridApi = params.api;
            |        params.api?.setGridOption('datasource', this.datasource);
            |
            |    }
            |""".trimMargin())

        this.dtoHtmlTableDef.actionColumnFields.forEach { actionColumnDef ->
            appendLine("""
                |
                |
                |    on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.dtoHtmlTableDef.dtoUqcn}) {
                |
                |        this.${actionColumnDef.actionName}.emit(dto);
                |
                |    }
                |""".trimMargin())
        }

        this.dtoHtmlTableDef.addButtonDef?.let { addButtonDef ->

            appendLine("""
                |
                |
                |    get addButtonVisible(): boolean {
                |""".trimMargin())

            if (addButtonDef.authority != null) {
                appendLine("        return this.authService.currentUserHasThisAuthority(Authority.${addButtonDef.authority});")
            } else {
                appendLine("        return true;")
            }

            blankLine()
            appendLine("    }")
            blankLine()
            blankLine()
            appendLine("    onAddButtonClicked() {")
            blankLine()
            appendLine("        this.addButtonClicked.emit();")
            blankLine()
            appendLine("    }")
        }

        this.dtoHtmlTableDef.clickableTableRowDef?.let { clickableTableRowDef ->

            blankLine()
            blankLine()
            appendLine("    onCellClicked(event: CellClickedEvent) {")

            clickableTableRowDef.routerNavigationArgs?.let { args ->
                val argsFormatted = args.joinToString(prefix = "[", separator = ", ", postfix = "]")
                appendLine("        this.router.navigate($argsFormatted);")
            }

            appendLine("    }")
        }

        appendLine("""
            |
            |
            |    reapplyFilters() {
            |
            |        this.gridApi.onFilterChanged();
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
            |}""".trimMargin())

    }


    private fun renderColDefFor(fieldDef: DtoHtmlTableColumnDef): String {

        val attributes = mutableMapOf<String, Any>()

        attributes["colId"] = "'${fieldDef.dtoFieldName}'"

        fieldDef.columnHeader?.let {

            attributes["headerName"] = "'${it.replace("'", "\\'")}'"

        }

        attributes["cellDataType"] = "'${fieldDef.agGridCellDateType.name}'"

        if (fieldDef.isFilterable) {
            attributes["filter"] = true
        }

        val keyValues = attributes.map { "${it.key}: ${it.value}" }

        return keyValues.joinToString(prefix = "{ ", separator = ", ", postfix = " }")

    }


    private fun renderColDefFor(fieldDef: DtoHtmlTableActionColumnDef) {

        appendLine("""
            |        {
            |            colId: '${fieldDef.actionName}',
            |            headerName: '${fieldDef.columnHeader}',
            |            width: 100,
            |            maxWidth: 100,
            |            filter: false,
            |            cellRenderer: ${fieldDef.cellRenderer!!.componentClassName},
            |            cellRendererParams: { iconName: '${fieldDef.icon}' },
            |            onCellClicked: event => {
            |                this.${fieldDef.actionName}.emit(event.data);
            |            }
            |        },""".trimMargin())

    }


}

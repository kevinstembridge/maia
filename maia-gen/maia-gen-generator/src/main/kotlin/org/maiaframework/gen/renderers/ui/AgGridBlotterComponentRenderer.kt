package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.AgGridCellRendererDefs
import org.maiaframework.gen.spec.definition.AuthoritiesDef
import org.maiaframework.gen.spec.definition.BlotterActionColumnDef
import org.maiaframework.gen.spec.definition.BlotterColumnDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.SearchModelType
import org.maiaframework.gen.spec.definition.lang.ListFieldType
import org.maiaframework.gen.spec.definition.lang.PkAndNameFieldType


class AgGridBlotterComponentRenderer(
    private val blotterDef: BlotterDef,
    private val authoritiesDef: AuthoritiesDef?
) : AbstractTypescriptRenderer() {


    private val requiresRouter = blotterDef.clickableBlotterRowDef != null


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
        addImport("@angular/core", "output")

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

        appendLine(
            """
            |
            |
            |@Component({
            |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
            |    providers: [${this.blotterDef.angularBlotterServiceName}, DecimalPipe],
            |    selector: '${this.blotterDef.blotterComponent.componentSelector}',
            |    templateUrl: './${this.blotterDef.blotterComponent.htmlFileName}'
        """.trimMargin())

        if (this.blotterDef.searchModelType == SearchModelType.MAIA) {
            appendLine("    styleUrls: ['./${this.blotterDef.blotterComponentScssFileName}'],")
        }

        appendLine("})")
        appendLine("export class ${this.blotterDef.blotterComponent.componentName} {")

        this.blotterDef.actionColumnFields.forEach { actionColumnDef ->
            blankLine()
            blankLine()
            appendLine("    readonly ${actionColumnDef.actionName} = output<${this.blotterDef.dtoUqcn}>();")
        }

        if (this.blotterDef.addButtonDef != null) {
            blankLine()
            blankLine()
            appendLine("    readonly addButtonClicked = output();")
        }

        blankLine()
        blankLine()
        appendLine("    public columnDefs: ColDef[] = [")

        this.blotterDef.blotterColumnDefs.forEach { fieldDef ->
            when (fieldDef) {
                is BlotterColumnDef -> appendLine("        ${renderColDefFor(fieldDef)},")
                is BlotterActionColumnDef -> renderColDefFor(fieldDef)
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
            |    public rowData!: ${blotterDef.dtoUqcn}[];
            |
            |
            |    private gridApi!: GridApi<${blotterDef.dtoUqcn}>;
            |
            |
            |    private readonly datasource = inject(${blotterDef.agGridDatasourceClassName});
            |
            |
            |    private readonly injector = inject(EnvironmentInjector);""".trimMargin()
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
            |    onGridReady(params: GridReadyEvent<${blotterDef.dtoUqcn}>) {
            |
            |        this.gridApi = params.api;
            |        params.api?.setGridOption('datasource', this.datasource);
            |
            |    }
            |""".trimMargin())

        this.blotterDef.actionColumnFields.forEach { actionColumnDef ->
            appendLine("""
                |
                |
                |    on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.blotterDef.dtoUqcn}) {
                |
                |        this.${actionColumnDef.actionName}.emit(dto);
                |
                |    }
                |""".trimMargin())
        }

        this.blotterDef.addButtonDef?.let { addButtonDef ->

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

        appendLine("""
            |
            |
            |    reapplyFilters() {
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
            |}""".trimMargin())

    }


    private fun renderColDefFor(fieldDef: BlotterColumnDef): String {

        val attributes = mutableMapOf<String, Any>()

        attributes["field"] = "'${fieldDef.dtoFieldName}'"

        val columnHeader = fieldDef.columnHeader
            ?: throw IllegalStateException(
                "Field '${fieldDef.dtoFieldName}' in table DTO '${blotterDef.dtoBaseName}' has no columnHeader (fieldDisplayName). " +
                "Add a fieldDisplayName to the entity/EsDoc field, or override the header at the columnFromDto() call site."
            )

        attributes["headerName"] = "'${columnHeader.replace("'", "\\'")}'"

        attributes["cellDataType"] = "'${fieldDef.agGridCellDateType.name}'"

        if (fieldDef.isFilterable) {
            attributes["filter"] = true
        }

        if (fieldDef.fieldType is ListFieldType) {
            val listFieldType = fieldDef.fieldType as ListFieldType
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

        appendLine("""
            |        {
            |            field: '${fieldDef.actionName}',
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

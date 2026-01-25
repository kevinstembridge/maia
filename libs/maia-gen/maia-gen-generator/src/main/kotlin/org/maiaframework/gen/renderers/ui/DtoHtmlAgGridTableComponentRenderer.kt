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


    override fun renderedFilePath(): String {

        return this.dtoHtmlTableDef.tableComponent.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        val cellClickedEventImportText = if (dtoHtmlTableDef.clickableTableRowDef == null) "" else "CellClickedEvent, "


        if (requiresRouter) {
            appendLine("import { Router } from '@angular/router';")
        }

        this.dtoHtmlTableDef.dtoHtmlTableColumnDefs
            .mapNotNull { it.cellRenderer }
            .map { it.importStatement }
            .toSet()
            .forEach {
                appendLine(it)
            }

        appendLine(
            """
            |import { DecimalPipe } from '@angular/common';
            |import { Component, EventEmitter, Output } from '@angular/core';
            |import { AuthService } from '@app/auth/auth.service';
            |${this.authoritiesDef?.importStatement ?: ""}
            |${this.dtoHtmlTableDef.tableServiceImportStatement}
            |import { ${this.dtoHtmlTableDef.dtoUqcn} } from './${this.dtoHtmlTableDef.dtoUqcn}';
            |import { MatIconModule } from '@angular/material/icon';
            |import { MatButtonModule } from '@angular/material/button';
            |import { FormsModule } from '@angular/forms';
            |import { AgGridAngular } from 'ag-grid-angular';
            |import { agGridTheme } from '@app/themes/ag-grid-theme';
            |import { ${cellClickedEventImportText}ColDef, FilterModel, GridApi, GridReadyEvent, ICellRendererParams, RowModelType } from 'ag-grid-community';
            |${this.dtoHtmlTableDef.agGridDatasourceImportStatement}
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
            appendLine("    @Output() ${actionColumnDef.actionName} = new EventEmitter<${this.dtoHtmlTableDef.dtoUqcn}>();")
        }

        if (this.dtoHtmlTableDef.addButtonDef != null) {
            blankLine()
            blankLine()
            appendLine("    @Output() addButtonClicked = new EventEmitter<void>();")
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
            |         mode: 'multiRow',
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
            |    public themeClass = 'ag-theme-material';
            |
            |
            |    private gridApi!: GridApi<${dtoHtmlTableDef.dtoUqcn}>;
            |
            |
            |    constructor(
            |        private datasource: ${dtoHtmlTableDef.agGridDatasourceClassName},""".trimMargin()
        )

        if (requiresRouter) {
            appendLine("        private router: Router,")
        }

        append("""
            |        private authService: AuthService
            |    ) {}
            |
            |
            |    onGridReady(params: GridReadyEvent<${dtoHtmlTableDef.dtoUqcn}>) {
            |
            |        this.gridApi = params.api;
            |        params.api?.setGridOption('datasource', this.datasource);
            |
            |    }""".trimMargin())

        this.dtoHtmlTableDef.actionColumnFields.forEach { actionColumnDef ->
            appendLine("""
                |
                |
                |    on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.dtoHtmlTableDef.dtoUqcn}) {
                |        this.${actionColumnDef.actionName}.emit(dto);
                |    }""".trimMargin())
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

        attributes["field"] = "'${fieldDef.dtoFieldName}'"

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
            |            field: '${fieldDef.actionName}',
            |            headerName: '${fieldDef.columnHeader}',
            |            width: 50,
            |            maxWidth: 50,
            |            cellRenderer: ${fieldDef.cellRenderer!!.componentClassName},
            |            cellRendererParams: { iconName: '${fieldDef.icon}' },
            |            onCellClicked: event => {
            |                this.${fieldDef.actionName}.emit(event.data);
            |            }
            |        },""".trimMargin())

    }


}

package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.TimelineBlotterDef
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class TimelineBlotterComponentRenderer(
    private val def: TimelineBlotterDef
) : AbstractTypescriptRenderer() {


    private val genDir = GeneratedTypescriptDir.forPackage(def.packageName)


    init {
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "Input")
        addImport("@angular/core", "OnInit")
        addImport("@angular/forms", "FormsModule")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/material/icon", "MatIconModule")
        addImport("ag-grid-angular", "AgGridAngular")
        addImport("@app/themes/ag-grid-theme", "agGridTheme")
        addImport("ag-grid-community", "ColDef")
        addImport("ag-grid-community", "GridApi")
        addImport("ag-grid-community", "GridReadyEvent")
        addImport("ag-grid-community", "RowModelType")
        addImport(TypescriptImport(def.tsRowDtoClassName, "@$genDir/${def.tsRowDtoClassName}"))
        addImport(TypescriptImport(def.datasourceClassName, "@$genDir/${def.datasourceClassName}"))
    }


    override fun renderedFilePath(): String {
        return def.blotterComponentNames.componentRenderedFilePath
    }


    override fun renderSourceBody() {

        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],")
        appendLine("    providers: [${def.datasourceClassName}],")
        appendLine("    selector: '${def.blotterComponentNames.componentSelector}',")
        appendLine("    templateUrl: './${def.blotterComponentNames.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${def.blotterComponentNames.componentName} implements OnInit {")
        blankLine()
        blankLine()
        appendLine("    @Input() entityId!: string;")
        blankLine()
        blankLine()
        appendLine("    public columnDefs: ColDef[] = [")
        appendLine("        { field: 'eventTimestamp', headerName: 'Timestamp', cellDataType: 'text', sort: 'desc' },")
        appendLine("        { field: 'eventType', headerName: 'Event Type', cellDataType: 'text' },")
        appendLine("        { field: 'changeType', headerName: 'Change Type', cellDataType: 'text' },")
        appendLine("        { field: 'version', headerName: 'Version', cellDataType: 'number' },")

        def.entityHistoryColumns.forEach { fieldDef ->
            val fieldName = fieldDef.classFieldDef.classFieldName.value
            val headerName = fieldDef.classFieldDef.displayName?.value ?: fieldName
            val cellDataType = when (fieldDef.classFieldDef.fieldType) {
                is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType,
                is LongFieldType, is LongTypeFieldType -> "number"
                else -> "text"
            }
            appendLine("        { field: '$fieldName', headerName: '$headerName', cellDataType: '$cellDataType' },")
        }

        def.joinDefs.forEach { joinDef ->
            appendLine("        { field: '${joinDef.rightFkDtoFieldName}', headerName: 'Join ID', cellDataType: 'text', hide: true },")
            appendLine("        { field: '${joinDef.displayFieldDtoFieldName}', headerName: 'Join', cellDataType: 'text' },")
        }

        appendLine("    ];")
        blankLine()
        blankLine()
        appendLine("    public defaultColDef: ColDef = {")
        appendLine("        filter: true,")
        appendLine("        flex: 1,")
        appendLine("        floatingFilter: true,")
        appendLine("        minWidth: 100,")
        appendLine("        sortable: true")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    public rowBuffer = 0;")
        blankLine()
        blankLine()
        appendLine("    public rowSelection = {")
        appendLine("         mode: 'singleRow' as const,")
        appendLine("         checkboxes: false,")
        appendLine("    };")
        blankLine()
        blankLine()
        appendLine("    public agGridTheme = agGridTheme;")
        blankLine()
        blankLine()
        appendLine("    public rowModelType: RowModelType = 'infinite';")
        blankLine()
        blankLine()
        appendLine("    public cacheBlockSize = 100;")
        blankLine()
        blankLine()
        appendLine("    public cacheOverflowSize = 2;")
        blankLine()
        blankLine()
        appendLine("    public maxConcurrentDatasourceRequests = 1;")
        blankLine()
        blankLine()
        appendLine("    public infiniteInitialRowCount = 1000;")
        blankLine()
        blankLine()
        appendLine("    public maxBlocksInCache = 10;")
        blankLine()
        blankLine()
        appendLine("    public rowData!: ${def.tsRowDtoClassName}[];")
        blankLine()
        blankLine()
        appendLine("    private gridApi!: GridApi<${def.tsRowDtoClassName}>;")
        blankLine()
        blankLine()
        appendLine("    private readonly datasource = inject(${def.datasourceClassName});")
        blankLine()
        blankLine()
        appendLine("    ngOnInit(): void {")
        appendLine("        this.datasource.setEntityId(this.entityId);")
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("    onGridReady(params: GridReadyEvent<${def.tsRowDtoClassName}>): void {")
        blankLine()
        appendLine("        this.gridApi = params.api;")
        appendLine("        params.api?.setGridOption('datasource', this.datasource);")
        blankLine()
        appendLine("    }")
        blankLine()
        blankLine()
        appendLine("}")

    }


}

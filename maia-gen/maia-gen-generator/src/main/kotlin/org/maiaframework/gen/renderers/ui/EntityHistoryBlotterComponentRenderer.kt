package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.EntityHistoryBlotterDef
import org.maiaframework.gen.spec.definition.GeneratedTypescriptDir
import org.maiaframework.gen.spec.definition.lang.IntFieldType
import org.maiaframework.gen.spec.definition.lang.IntTypeFieldType
import org.maiaframework.gen.spec.definition.lang.IntValueClassFieldType
import org.maiaframework.gen.spec.definition.lang.LongFieldType
import org.maiaframework.gen.spec.definition.lang.LongTypeFieldType
import org.maiaframework.gen.spec.definition.lang.TypescriptImport


class EntityHistoryBlotterComponentRenderer(
    private val def: EntityHistoryBlotterDef
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

        append("""
            |
            |
            |@Component({
            |    imports: [AgGridAngular, FormsModule, MatButtonModule, MatIconModule],
            |    providers: [${def.datasourceClassName}],
            |    selector: '${def.blotterComponentNames.componentSelector}',
            |    templateUrl: './${def.blotterComponentNames.htmlFileName}'
            |})
            |export class ${def.blotterComponentNames.componentName} implements OnInit {
            |
            |
            |    @Input() entityId!: string;
            |
            |
            |    public columnDefs: ColDef[] = [
            |        { field: 'id', headerName: 'ID', cellDataType: 'text', hide: true },
            |""".trimMargin())

        def.blotterColumns.forEach { fieldDef ->
            val fieldName = fieldDef.classFieldDef.classFieldName.value
            val headerName = fieldDef.classFieldDef.displayName?.value ?: fieldName
            val cellDataType = when (fieldDef.classFieldDef.fieldType) {
                is IntFieldType, is IntTypeFieldType, is IntValueClassFieldType,
                is LongFieldType, is LongTypeFieldType -> "number"
                else -> "text"
            }
            appendLine("        { field: '${fieldName}', headerName: '${headerName}', cellDataType: '${cellDataType}' },")
        }

        append("""
            |    ];
            |
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
            |    public rowData!: ${def.tsRowDtoClassName}[];
            |
            |
            |    private gridApi!: GridApi<${def.tsRowDtoClassName}>;
            |
            |
            |    private readonly datasource = inject(${def.datasourceClassName});
            |
            |
            |    ngOnInit(): void {
            |        this.datasource.setEntityId(this.entityId);
            |    }
            |
            |
            |    onGridReady(params: GridReadyEvent<${def.tsRowDtoClassName}>): void {
            |
            |        this.gridApi = params.api;
            |        params.api?.setGridOption('datasource', this.datasource);
            |
            |    }
            |
            |
            |}
            |""".trimMargin())

    }


}

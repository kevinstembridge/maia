package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.BlotterActionColumnDef
import org.maiaframework.gen.spec.definition.BlotterColumnDef
import org.maiaframework.gen.spec.definition.BlotterDef
import org.maiaframework.gen.spec.definition.BlotterEsDocSourceDef
import org.maiaframework.gen.spec.definition.BlotterSearchableDtoSourceDef


class BlotterComponentRenderer(private val blotterDef: BlotterDef) : AbstractTypescriptRenderer() {


    private val searchResultUqcn = when (blotterDef.blotterSourceDef) {
        is BlotterEsDocSourceDef -> "IndexSearchResult"
        is BlotterSearchableDtoSourceDef -> "SearchResultPage"
    }


    init {

        addImport("@angular/common", "DecimalPipe")
        addImport("@angular/common", "NgIf")
        addImport("@angular/common", "AsyncPipe")
        addImport("@angular/common", "DatePipe")

        blotterDef.clickableBlotterRowDef?.let {
            addImport("@angular/router", "Router")
        }

        addImport("@angular/core", "AfterViewInit")
        addImport("@angular/core", "Component")
        addImport("@angular/core", "inject")
        addImport("@angular/core", "output")
        addImport("@angular/core", "QueryList")
        addImport("@angular/core", "ViewChild")
        addImport("@angular/core", "ViewChildren")
        addImport("rxjs", "BehaviorSubject")
        addImport("rxjs", "merge")
        addImport("rxjs", "Observable")
        addImport("rxjs", "Subject")
        addImport("rxjs/operators", "debounceTime")
        addImport("rxjs/operators", "delay")
        addImport("rxjs/operators", "switchMap")
        addImport("rxjs/operators", "tap")
        addImport("@angular/material/paginator", "MatPaginator")
        addImport("@angular/material/paginator", "MatPaginatorModule")
        addImport("@angular/material/sort", "MatSort")
        addImport("@angular/material/sort", "MatSortModule")
        addImport("@app/gen-components/common/model/$searchResultUqcn", searchResultUqcn)
        addImport("@app/gen-components/common/model/SearchModel", "SearchModel")
        addImport("@app/gen-components/common/model/FilterModelItem", "FilterModelItem")
        addImport("@app/gen-components/common/model/SortModelItem", "SortModelItem")
        addImport("@app/auth/auth.service", "AuthService")
        addImport("@app/auth/Authority", "Authority")
        addImport(blotterDef.blotterComponent.serviceTypescriptImport)
        addImport(blotterDef.dtoDef.typescriptDtoImport)
        addImport("@angular/material/icon", "MatIconModule")
        addImport("@angular/material/input", "MatInputModule")
        addImport("@angular/material/table", "MatTableModule")
        addImport("@angular/material/button", "MatButtonModule")
        addImport("@angular/forms", "FormsModule")

    }


    override fun renderedFilePath(): String {

        return this.blotterDef.blotterComponent.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        blankLine()
        appendLine("@Component({")
        appendLine("  imports: [FormsModule, NgIf, MatButtonModule, MatTableModule, MatSortModule, MatInputModule, MatIconModule, MatPaginatorModule, AsyncPipe, DatePipe],")
        appendLine("  providers: [${this.blotterDef.angularBlotterServiceName}, DecimalPipe],")
        appendLine("  selector: '${this.blotterDef.blotterComponent.componentSelector}',")
        appendLine("  styleUrls: ['./${this.blotterDef.blotterComponentScssFileName}'],")
        appendLine("  templateUrl: './${this.blotterDef.blotterComponent.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${this.blotterDef.blotterComponent.componentName} implements AfterViewInit {")
        blankLine()
        appendLine("  @ViewChild(MatPaginator) paginator: MatPaginator;")
        appendLine("  @ViewChild(MatSort) sort: MatSort;")

        this.blotterDef.actionColumnFields.forEach { actionColumnDef ->
            appendLine("  readonly ${actionColumnDef.actionName} = output<${this.blotterDef.dtoUqcn}>();")
        }

        if (this.blotterDef.addButtonDef != null) {
            appendLine("  readonly addButtonClicked = output();")
        }

        blankLine()
        appendLine("  displayedColumns = [")

        this.blotterDef.blotterColumnDefs.forEach { fieldDef ->
            when(fieldDef) {
                is BlotterColumnDef -> appendLine("    '${fieldDef.dtoFieldName}',")
                is BlotterActionColumnDef -> appendLine("    '${fieldDef.actionName}',")
            }
        }

        appendLine("  ];")
        blankLine()
        appendLine("  displayedFilterColumns = [")

        this.blotterDef.blotterColumnDefs.forEach { fieldDef ->
            when (fieldDef) {
                is BlotterColumnDef -> appendLine("    '${fieldDef.dtoFieldName}Filter',")
                is BlotterActionColumnDef -> appendLine("    '${fieldDef.actionName}Filter',")
            }
        }

        appendLine("  ];")
        blankLine()

        this.blotterDef.blotterColumnFields.forEach { fieldDef ->
            appendLine("  private _${fieldDef.dtoFieldName} = null;")
        }

        appendLine("  pageSize: number = 10;")
        appendLine("  private _search$ = new Subject<void>();")
        appendLine("  private _rows$ = new BehaviorSubject<${this.blotterDef.dtoUqcn}[]>([]);")
        appendLine("  private _total$ = new BehaviorSubject<number>(0);")
        appendLine("  private _firstResultIndex = new BehaviorSubject<number>(0);")
        appendLine("  private _lastResultIndex = new BehaviorSubject<number>(0);")
        appendLine("  private sortColumn = '';")
        appendLine("  private sortDirection = '';")
        blankLine()
        appendLine("  public loading: boolean = false;")
        appendLine("  public totalResultCount: number = 0;")
        blankLine()
        blankLine()
        appendLine("  private readonly service = inject(${this.blotterDef.angularBlotterServiceName});")
        this.blotterDef.clickableBlotterRowDef?.let {
            blankLine()
            blankLine()
            appendLine("  private readonly router = inject(Router);")
        }
        blankLine()
        blankLine()
        appendLine("  private readonly authService = inject(AuthService);")
        blankLine()
        blankLine()
        appendLine("  constructor() {")
        blankLine()
        appendLine("    this._search$.pipe(")
        appendLine("      tap(() => this.loading = true),")
        appendLine("      debounceTime(300),")
        appendLine("      switchMap(() => this._search()),")
        appendLine("      tap(() => this.loading = false)")
        appendLine("    ).subscribe(result => {")

        when (this.blotterDef.blotterSourceDef) {
            is BlotterEsDocSourceDef -> {

                appendLine("      this._rows$.next(result.hits);")
                appendLine("      this._total$.next(result.totalHits.count);")
                appendLine("      this.totalResultCount = result.totalHits.count;")
                appendLine("      this._firstResultIndex.next(result.firstResultIndex);")
                appendLine("      this._lastResultIndex.next(result.lastResultIndex);")

            }
            is BlotterSearchableDtoSourceDef -> {

                appendLine("      this._rows$.next(result.results);")
                appendLine("      this._total$.next(result.totalResultCount);")
                appendLine("      this.totalResultCount = result.totalResultCount;")
                appendLine("      this._firstResultIndex.next(result.firstResultIndex);")
                appendLine("      this._lastResultIndex.next(result.lastResultIndex);")

            }

        }

        appendLine("    });")
        blankLine()
        appendLine("    this._search$.next();")
        blankLine()
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  ngAfterViewInit() {")
        blankLine()
        appendLine("    this.sort.sortChange.subscribe((sortData) => {")
        appendLine("      this.paginator.pageIndex = 0;")
        appendLine("      this.sortColumn = sortData.active;")
        appendLine("      this.sortDirection = sortData.direction;")
        appendLine("      this.reapplyFilters();")
        appendLine("    });")
        blankLine()
        appendLine("    this.paginator.page.subscribe(pageData => {")
        appendLine("      this.reapplyFilters();")
        appendLine("    });")
        blankLine()
        appendLine("  }")

        this.blotterDef.actionColumnFields.forEach { actionColumnDef ->
            blankLine()
            blankLine()
            appendLine("  on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.blotterDef.dtoUqcn}) {")
            appendLine("    this.${actionColumnDef.actionName}.emit(dto);")
            appendLine("  }")
        }

        this.blotterDef.addButtonDef?.let { addButtonDef ->

            blankLine()
            blankLine()
            appendLine("  get addButtonVisible(): boolean {")

            if (addButtonDef.authority != null) {
                appendLine("    return this.authService.currentUserHasThisAuthority(Authority.${addButtonDef.authority});")
            } else {
                appendLine("    return true;")
            }

            appendLine("  }")
            blankLine()
            blankLine()
            appendLine("  onAddButtonClicked() {")
            appendLine("    this.addButtonClicked.emit();")
            appendLine("  }")
        }

        blankLine()
        blankLine()
        appendLine("  get rows$() { return this._rows$.asObservable(); }")
        appendLine("  get firstResultIndex$() { return this._firstResultIndex.asObservable(); }")
        appendLine("  get lastResultIndex$() { return this._lastResultIndex.asObservable(); }")
        appendLine("  get total$() { return this._total$.asObservable(); }")

        this.blotterDef.blotterColumnFields.forEach { fieldDef ->
            blankLine()
            blankLine()
            appendLine("  get ${fieldDef.dtoFieldName}() { return this._${fieldDef.dtoFieldName}; }")
            appendLine("  set ${fieldDef.dtoFieldName}(${fieldDef.dtoFieldName}: string) {")
            appendLine("    this._${fieldDef.dtoFieldName} = ${fieldDef.dtoFieldName};")
            appendLine("    this.reapplyFilters();")
            appendLine("  }")
        }

        blankLine()
        blankLine()
        appendLine("  public reapplyFilters() {")
        appendLine("    this._search$.next();")
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private _search(): Observable<${this.searchResultUqcn}<${this.blotterDef.dtoUqcn}>> {")
        appendLine("    return this.service.search(this.buildSearchModel());")
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private buildSearchModel(): SearchModel {")
        blankLine()
        appendLine("    return new SearchModel(")
        appendLine("      this.buildFilterModel(),")
        appendLine("      this.buildSortModel(),")
        appendLine("      this.getStartRow(),")
        appendLine("      this.getEndRow()")
        appendLine("    );")
        blankLine()
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private buildFilterModel(): FilterModelItem[] {")
        blankLine()
        appendLine("    const filterModels = [];")

        this.blotterDef.blotterColumnFields.forEach { fieldDef ->
            blankLine()
            appendLine("    if (this.isNotBlank(this.${fieldDef.dtoFieldName})) {")
            appendLine("      filterModels.push(new FilterModelItem(")
            appendLine("        '${fieldDef.dtoFieldName}',")
            appendLine("        '${fieldDef.filterModelFieldType}',")
            appendLine("        '${fieldDef.filterModelFilterType}',")
            appendLine("        this.${fieldDef.dtoFieldName}")
            appendLine("      ));")
            appendLine("    }")
        }

        blankLine()
        appendLine("    return filterModels;")
        blankLine()
        appendLine("  }")

        blankLine()
        blankLine()
        appendLine("  private buildSortModel(): SortModelItem[] {")
        blankLine()
        appendLine("    const sortModels = [];")
        blankLine()
        appendLine("    if (this.isNotBlank(this.sortColumn) && this.isNotBlank(this.sortDirection)) {")
        appendLine("      sortModels.push(new SortModelItem(this.sortColumn, this.sortDirection));")
        appendLine("    }")
        blankLine()
        appendLine("    return sortModels;")
        blankLine()
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private getStartRow(): number {")
        appendLine("    return (this.paginator.pageIndex) * this.paginator.pageSize;")
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private getEndRow(): number {")
        appendLine("    return this.getStartRow() + this.paginator.pageSize;")
        appendLine("  }")
        blankLine()
        blankLine()
        appendLine("  private isNotBlank(input: string): boolean {")
        appendLine("    return input && input.trim() !== '';")
        appendLine("  }")

        this.blotterDef.clickableBlotterRowDef?.let { clickableTableRowDef ->

            blankLine()
            blankLine()
            appendLine("  onRowClicked(row: ${this.blotterDef.dtoUqcn}) {")

            clickableTableRowDef.routerNavigationArgs?.let { args ->
                val argsFormatted = args.joinToString(prefix = "[", separator = ", ", postfix = "]")
                appendLine("    this.router.navigate($argsFormatted);")
            }

            appendLine("  }")
        }

        blankLine()
        blankLine()
        appendLine("}")

    }


}

package org.maiaframework.gen.renderers.ui

import org.maiaframework.gen.spec.definition.DataSourceType
import org.maiaframework.gen.spec.definition.DtoHtmlTableActionColumnDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableColumnDef
import org.maiaframework.gen.spec.definition.DtoHtmlTableDef


class DtoHtmlTableComponentRenderer(private val dtoHtmlTableDef: DtoHtmlTableDef) : AbstractTypescriptRenderer() {


    private val searchResultUqcn = when (dtoHtmlTableDef.dataSourceType) {
        DataSourceType.ELASTIC_SEARCH -> "IndexSearchResult"
        DataSourceType.DATABASE -> "SearchResultPage"
    }

    override fun renderedFilePath(): String {

        return this.dtoHtmlTableDef.tableComponent.componentRenderedFilePath

    }


    override fun renderSourceBody() {

        appendLine("import { DecimalPipe, NgIf, AsyncPipe, DatePipe } from '@angular/common';")

        this.dtoHtmlTableDef.clickableTableRowDef?.let {
            appendLine("import { Router } from '@angular/router';")
        }

        appendLine("import { AfterViewInit, Component, EventEmitter, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';")
        appendLine("import { BehaviorSubject, merge, Observable, Subject } from 'rxjs';")
        appendLine("import { debounceTime, delay, switchMap, tap } from 'rxjs/operators';")
        appendLine("import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';")
        appendLine("import { MatSort, MatSortModule } from '@angular/material/sort';")
        appendLine("import { $searchResultUqcn } from '@app/gen-components/common/model/$searchResultUqcn';")
        appendLine("import { SearchModel } from '@app/gen-components/common/model/SearchModel';")
        appendLine("import { FilterModelItem } from '@app/gen-components/common/model/FilterModelItem';")
        appendLine("import { SortModelItem } from '@app/gen-components/common/model/SortModelItem';")
        appendLine("import { AuthService } from '@app/auth/auth.service';")
        appendLine("import { Authority } from '@app/auth/Authority';")
        appendLine(this.dtoHtmlTableDef.tableServiceImportStatement)
        appendLine("import { ${this.dtoHtmlTableDef.dtoUqcn} } from './${this.dtoHtmlTableDef.dtoUqcn}';")
        appendLine("import { MatIconModule } from '@angular/material/icon';")
        appendLine("import { MatInputModule } from '@angular/material/input';")
        appendLine("import { MatTableModule } from '@angular/material/table';")
        appendLine("import { MatButtonModule } from '@angular/material/button';")
        appendLine("import { FormsModule } from '@angular/forms';")
        blankLine()
        blankLine()
        appendLine("@Component({")
        appendLine("  imports: [FormsModule, NgIf, MatButtonModule, MatTableModule, MatSortModule, MatInputModule, MatIconModule, MatPaginatorModule, AsyncPipe, DatePipe],")
        appendLine("  providers: [${this.dtoHtmlTableDef.angularTableServiceName}, DecimalPipe],")
        appendLine("  selector: '${this.dtoHtmlTableDef.tableComponent.componentSelector}',")
        appendLine("  styleUrls: ['./${this.dtoHtmlTableDef.tableComponentScssFileName}'],")
        appendLine("  templateUrl: './${this.dtoHtmlTableDef.tableComponent.htmlFileName}'")
        appendLine("})")
        appendLine("export class ${this.dtoHtmlTableDef.tableComponent.componentName} implements AfterViewInit {")
        blankLine()
        appendLine("  @ViewChild(MatPaginator) paginator: MatPaginator;")
        appendLine("  @ViewChild(MatSort) sort: MatSort;")

        this.dtoHtmlTableDef.actionColumnFields.forEach { actionColumnDef ->
            appendLine("  @Output() ${actionColumnDef.actionName} = new EventEmitter<${this.dtoHtmlTableDef.dtoUqcn}>();")
        }

        if (this.dtoHtmlTableDef.addButtonDef != null) {
            appendLine("  @Output() addButtonClicked = new EventEmitter<void>();")
        }

        blankLine()
        appendLine("  displayedColumns = [")

        this.dtoHtmlTableDef.dtoHtmlTableColumnDefs.forEach { fieldDef ->
            when(fieldDef) {
                is DtoHtmlTableColumnDef -> appendLine("    '${fieldDef.dtoFieldName}',")
                is DtoHtmlTableActionColumnDef -> appendLine("    '${fieldDef.actionName}',")
            }
        }

        appendLine("  ];")
        blankLine()
        appendLine("  displayedFilterColumns = [")

        this.dtoHtmlTableDef.dtoHtmlTableColumnDefs.forEach { fieldDef ->
            when (fieldDef) {
                is DtoHtmlTableColumnDef -> appendLine("    '${fieldDef.dtoFieldName}Filter',")
                is DtoHtmlTableActionColumnDef -> appendLine("    '${fieldDef.actionName}Filter',")
            }
        }

        appendLine("  ];")
        blankLine()

        this.dtoHtmlTableDef.dtoHtmlTableColumnFields.forEach { fieldDef ->
            appendLine("  private _${fieldDef.dtoFieldName} = null;")
        }

        appendLine("  pageSize: number = 10;")
        appendLine("  private _search$ = new Subject<void>();")
        appendLine("  private _rows$ = new BehaviorSubject<${this.dtoHtmlTableDef.dtoUqcn}[]>([]);")
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
        appendLine("  constructor(")
        appendLine("    private service: ${this.dtoHtmlTableDef.angularTableServiceName},")
        this.dtoHtmlTableDef.clickableTableRowDef?.let {
            appendLine("    private router: Router,")
        }
        appendLine("    private authService: AuthService")
        appendLine("  ) {")
        blankLine()
        appendLine("    this._search$.pipe(")
        appendLine("      tap(() => this.loading = true),")
        appendLine("      debounceTime(300),")
        appendLine("      switchMap(() => this._search()),")
        appendLine("      tap(() => this.loading = false)")
        appendLine("    ).subscribe(result => {")

        when (this.dtoHtmlTableDef.dataSourceType) {
            DataSourceType.ELASTIC_SEARCH -> {

                appendLine("      this._rows$.next(result.hits);")
                appendLine("      this._total$.next(result.totalHits.count);")
                appendLine("      this.totalResultCount = result.totalHits.count;")
                appendLine("      this._firstResultIndex.next(result.firstResultIndex);")
                appendLine("      this._lastResultIndex.next(result.lastResultIndex);")

            }
            DataSourceType.DATABASE -> {

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

        this.dtoHtmlTableDef.actionColumnFields.forEach { actionColumnDef ->
            blankLine()
            blankLine()
            appendLine("  on${actionColumnDef.actionName.firstToUpper()}(dto: ${this.dtoHtmlTableDef.dtoUqcn}) {")
            appendLine("    this.${actionColumnDef.actionName}.emit(dto);")
            appendLine("  }")
        }

        this.dtoHtmlTableDef.addButtonDef?.let { addButtonDef ->

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

        this.dtoHtmlTableDef.dtoHtmlTableColumnFields.forEach { fieldDef ->
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
        appendLine("  private _search(): Observable<${this.searchResultUqcn}<${this.dtoHtmlTableDef.dtoUqcn}>> {")
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

        this.dtoHtmlTableDef.dtoHtmlTableColumnFields.forEach { fieldDef ->
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

        this.dtoHtmlTableDef.clickableTableRowDef?.let { clickableTableRowDef ->

            blankLine()
            blankLine()
            appendLine("  onRowClicked(row: ${this.dtoHtmlTableDef.dtoUqcn}) {")

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

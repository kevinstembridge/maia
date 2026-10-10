import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AgGridAngular} from 'ag-grid-angular';
import {ColDef, GridReadyEvent, RowModelType, Theme, themeMaterial} from 'ag-grid-community';
import {ToggleHistoryDatasource} from './toggle-history-datasource';
import {ToggleHistoryRow} from './toggle-history-row';


/**
 * Version history of a single feature toggle. The host app registers this at
 * `<TOGGLES_HISTORY_ROUTE>/:id`, guarded by its own authority check.
 */
@Component({
    selector: 'maia-toggle-history-page',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [AgGridAngular],
    providers: [ToggleHistoryDatasource],
    template: `
        <h1>Feature Toggle History</h1>
        <div style="height: calc(100vh - 160px);">
            <ag-grid-angular
                style="width: 100%; height: 100%;"
                [columnDefs]="columnDefs"
                [defaultColDef]="defaultColDef"
                [rowModelType]="rowModelType"
                [cacheBlockSize]="100"
                [maxConcurrentDatasourceRequests]="1"
                [theme]="theme"
                (gridReady)="onGridReady($event)"
            />
        </div>
    `
})
export class ToggleHistoryPage implements OnInit {


    protected readonly theme: Theme = themeMaterial;

    protected readonly rowModelType: RowModelType = 'infinite';

    protected readonly defaultColDef: ColDef = {
        filter: true,
        flex: 1,
        floatingFilter: true,
        minWidth: 100,
        sortable: true,
    };

    protected readonly columnDefs: ColDef<ToggleHistoryRow>[] = [
        {field: 'version', headerName: 'Version', cellDataType: 'number'},
        {field: 'changeType', headerName: 'Change Type'},
        {field: 'featureName', headerName: 'Feature'},
        {field: 'enabled', headerName: 'Enabled'},
        {field: 'activationStrategies', headerName: 'Activation Strategies'},
        {field: 'description', headerName: 'Description'},
        {field: 'comment', headerName: 'Comment'},
        {field: 'contactPerson', headerName: 'Contact Person'},
        {field: 'ticketKey', headerName: 'Ticket'},
        {field: 'infoLink', headerName: 'Info Link'},
        {field: 'reviewDate', headerName: 'Review Date'},
        {field: 'attributes', headerName: 'Attributes'},
        {field: 'lastModifiedByUsername', headerName: 'Last Modified By'},
        {field: 'lastModifiedTimestamp', headerName: 'Last Modified'},
    ];

    private readonly route = inject(ActivatedRoute);
    private readonly datasource = inject(ToggleHistoryDatasource);


    ngOnInit(): void {

        this.datasource.setToggleId(this.route.snapshot.paramMap.get('id')!);

    }


    protected onGridReady(event: GridReadyEvent<ToggleHistoryRow>): void {

        event.api.setGridOption('datasource', this.datasource);

    }


}

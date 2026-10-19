import {Component, provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {AgGridAngular} from 'ag-grid-angular';
import {AllCommunityModule, ColDef, ModuleRegistry} from 'ag-grid-community';
import {UuidAgGridCellRendererComponent} from './uuid-ag-grid-cell-renderer.component';

ModuleRegistry.registerModules([AllCommunityModule]);

const uuid = '123e4567-e89b-12d3-a456-426614174000';

@Component({
    imports: [AgGridAngular],
    template: `<ag-grid-angular style="width: 600px; height: 300px" [columnDefs]="columnDefs" [rowData]="rowData" domLayout="print" />`,
})
class HostComponent {
    columnDefs: ColDef[] = [{field: 'id', cellRenderer: UuidAgGridCellRendererComponent}];
    rowData = [{id: uuid}];
}

describe('UuidAgGridCellRendererComponent in AG Grid', () => {

    const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

    beforeEach(() => {
        TestBed.configureTestingModule({providers: [provideZonelessChangeDetection()]});
    });

    it('shows the short id, then the copy icon after hover, without manual change detection', async () => {
        const fixture = TestBed.createComponent(HostComponent);
        const el = fixture.nativeElement as HTMLElement;
        await fixture.whenStable();
        await sleep(100);

        const text = el.querySelector('.short-id-text');
        expect(text?.textContent?.trim()).toBe('426614174000');

        (el.querySelector('.short-id') as HTMLElement).dispatchEvent(new Event('mouseenter'));
        await sleep(900);
        await fixture.whenStable();

        expect(el.querySelector('.short-id-copy')).not.toBeNull();
    });

});

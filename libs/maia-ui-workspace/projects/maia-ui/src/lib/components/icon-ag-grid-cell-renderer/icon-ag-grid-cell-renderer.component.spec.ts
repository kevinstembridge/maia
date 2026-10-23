import {Component, provideZonelessChangeDetection} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {AgGridAngular} from 'ag-grid-angular';
import {AllCommunityModule, CellClickedEvent, ColDef, ModuleRegistry} from 'ag-grid-community';
import {IconAgGridCellRendererComponent} from './icon-ag-grid-cell-renderer.component';

ModuleRegistry.registerModules([AllCommunityModule]);

@Component({
    imports: [AgGridAngular],
    template: `<ag-grid-angular style="width: 600px; height: 300px" [columnDefs]="columnDefs" [rowData]="rowData" domLayout="print" />`,
})
class HostComponent {
    clicked: string[] = [];

    columnDefs: ColDef[] = [
        {field: 'name'},
        {
            field: 'review',
            cellRenderer: IconAgGridCellRendererComponent,
            cellRendererParams: {iconName: 'rate_review', enabledWhenField: 'eligible'},
            onCellClicked: (event: CellClickedEvent) => {
                if (event.data?.eligible !== true) {
                    return;
                }
                this.clicked.push(event.data.name);
            },
        },
        {
            field: 'plain',
            cellRenderer: IconAgGridCellRendererComponent,
            cellRendererParams: {iconName: 'edit'},
        },
    ];

    rowData = [
        {name: 'enabled-row', eligible: true},
        {name: 'disabled-row', eligible: false},
    ];
}

describe('IconAgGridCellRendererComponent in AG Grid', () => {

    const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

    let fixture: ReturnType<typeof TestBed.createComponent<HostComponent>>;
    let el: HTMLElement;

    const icon = (rowIndex: number, colId: string) =>
        el.querySelector(`[row-index="${rowIndex}"] [col-id="${colId}"] .material-icons`) as HTMLElement;

    beforeEach(async () => {
        TestBed.configureTestingModule({providers: [provideZonelessChangeDetection()]});
        fixture = TestBed.createComponent(HostComponent);
        el = fixture.nativeElement as HTMLElement;
        await fixture.whenStable();
        await sleep(100);
    });

    it('shows an enabled, clickable icon when the row field is true', async () => {
        const enabled = icon(0, 'review');
        expect(enabled.textContent?.trim()).toBe('rate_review');
        expect(enabled.getAttribute('aria-disabled')).toBe('false');
        expect(enabled.classList).not.toContain('icon-disabled');

        enabled.click();
        await sleep(50);
        expect(fixture.componentInstance.clicked).toEqual(['enabled-row']);
    });

    it('shows a greyed-out, aria-disabled icon that does nothing when clicked when the row field is false', async () => {
        const disabled = icon(1, 'review');
        expect(disabled.textContent?.trim()).toBe('rate_review');
        expect(disabled.getAttribute('aria-disabled')).toBe('true');
        expect(disabled.classList).toContain('icon-disabled');
        expect(disabled.style.cursor).toBe('default');
        expect(disabled.style.opacity).toBe('0.38');

        disabled.click();
        await sleep(50);
        expect(fixture.componentInstance.clicked).toEqual([]);
    });

    it('is unchanged for a column without enabledWhenField', () => {
        const plain = icon(1, 'plain');
        expect(plain.getAttribute('aria-disabled')).toBeNull();
        expect(plain.classList).not.toContain('icon-disabled');
        expect(plain.style.cursor).toBe('pointer');
    });

});

import {Component, ChangeDetectionStrategy} from '@angular/core';
import {ICellRendererAngularComp} from 'ag-grid-angular';
import {ICellRendererParams} from 'ag-grid-community';


@Component({
    imports: [],
    template: `
        <div class="chips-container">
            @for (item of items; track item.displayName) {
                <span class="chip-item">{{item.displayName}}</span>
            }
        </div>
    `,
    changeDetection: ChangeDetectionStrategy.Eager,
    styles: [`
        .chips-container { display: flex; flex-wrap: wrap; gap: 4px; align-items: center; height: 100%; }
        .chip-item { background: #e0e0e0; border-radius: 16px; padding: 2px 8px; font-size: 12px; white-space: nowrap; }
    `],
})
export class ChipsAgGridCellRendererComponent implements ICellRendererAngularComp {


    items: { displayName: string }[] = [];


    agInit(params: ICellRendererParams): void {
        this.items = params.value ?? [];
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}

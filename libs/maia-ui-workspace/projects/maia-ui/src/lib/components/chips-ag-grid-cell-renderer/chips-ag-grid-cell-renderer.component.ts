import {Component} from '@angular/core';
import {MatChipsModule} from '@angular/material/chips';
import {ICellRendererAngularComp} from 'ag-grid-angular';
import {ICellRendererParams} from 'ag-grid-community';


@Component({
    imports: [MatChipsModule],
    template: `
        <mat-chip-set>
            @for (item of items; track item.name) {
                <mat-chip>{{item.name}}</mat-chip>
            }
        </mat-chip-set>
    `,
})
export class ChipsAgGridCellRendererComponent implements ICellRendererAngularComp {


    items: { name: string }[] = [];


    agInit(params: ICellRendererParams): void {
        this.items = params.value ?? [];
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}

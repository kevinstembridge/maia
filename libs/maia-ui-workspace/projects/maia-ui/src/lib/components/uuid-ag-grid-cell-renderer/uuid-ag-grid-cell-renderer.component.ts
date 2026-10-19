import {ChangeDetectionStrategy, Component} from '@angular/core';
import {ICellRendererAngularComp} from 'ag-grid-angular';
import {ICellRendererParams} from 'ag-grid-community';
import {ShortId} from '../short-id/short-id';


@Component({
    imports: [ShortId],
    changeDetection: ChangeDetectionStrategy.Eager,
    template: `<maia-short-id [id]="id" />`,
})
export class UuidAgGridCellRendererComponent implements ICellRendererAngularComp {


    id: string | null | undefined;


    agInit(params: ICellRendererParams): void {
        this.id = params.value;
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}

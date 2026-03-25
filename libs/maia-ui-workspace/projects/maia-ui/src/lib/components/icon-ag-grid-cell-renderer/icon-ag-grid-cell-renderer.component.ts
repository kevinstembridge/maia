import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';


interface IconCellRendererParams extends ICellRendererParams {
    iconName: string;
}


@Component({
    imports: [],
    template: `<span class="material-icons" style="cursor:pointer;font-size:20px">{{iconName}}</span>`,
})
export class IconAgGridCellRendererComponent implements ICellRendererAngularComp {


    iconName = '';


    agInit(params: IconCellRendererParams): void {
        this.iconName = params.iconName;
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}

import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';


interface IconCellRendererParams extends ICellRendererParams {
    iconName: string;
}


@Component({
    imports: [MatIconModule, MatIconButton],
    template: `<button mat-icon-button><mat-icon>{{iconName}}</mat-icon></button>`,
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

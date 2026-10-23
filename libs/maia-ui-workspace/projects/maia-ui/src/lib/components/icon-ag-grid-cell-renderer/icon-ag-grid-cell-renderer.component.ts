import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';


interface IconCellRendererParams extends ICellRendererParams {
    iconName: string;
    /** Name of a boolean row field. When set, the icon is enabled only if that field is true for the row. */
    enabledWhenField?: string;
}


@Component({
    imports: [],
    changeDetection: ChangeDetectionStrategy.Eager,
    template: `<span
        class="material-icons"
        [class.icon-disabled]="disabled"
        [attr.aria-disabled]="ariaDisabled"
        [style.cursor]="disabled ? 'default' : 'pointer'"
        [style.opacity]="disabled ? '0.38' : null"
        style="font-size:20px">{{iconName}}</span>`,
})
export class IconAgGridCellRendererComponent implements ICellRendererAngularComp {


    iconName = '';
    disabled = false;
    ariaDisabled: string | null = null;


    agInit(params: IconCellRendererParams): void {
        this.iconName = params.iconName;
        if (params.enabledWhenField) {
            this.disabled = params.data?.[params.enabledWhenField] !== true;
            this.ariaDisabled = String(this.disabled);
        }
    }


    refresh(_params: ICellRendererParams): boolean {
        return false;
    }


}

import {Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    AllFieldTypesCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/all_field_types/all-field-types-crud-table.component';

@Component({
    imports: [
        PageLayoutComponent,
        AllFieldTypesCrudTableComponent
    ],
    selector: 'app-all-field-types-blotter-page',
    templateUrl: './all-field-types-blotter-page.html',
})
export class AllFieldTypesBlotterPage {

}

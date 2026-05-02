import {Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {
    AllFieldTypesCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/all_field_types/all-field-types-crud-blotter.component';

@Component({
    imports: [
        PageLayout,
        AllFieldTypesCrudBlotterComponent
    ],
    selector: 'app-all-field-types-blotter-page',
    templateUrl: './all-field-types-blotter-page.html',
})
export class AllFieldTypesBlotterPage {

}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {
    LeftManySearchableCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/many_to_many/left-many-searchable-crud-blotter.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        LeftManySearchableCrudBlotterComponent
    ],
    selector: 'app-left-many-searchable-blotter-page',
    templateUrl: './left-many-searchable-blotter-page.html',
})
export class LeftManySearchableBlotterPage {

}

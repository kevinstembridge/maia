import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    LeftManySearchableCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/many_to_many/left-many-searchable-crud-blotter.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        LeftManySearchableCrudBlotterComponent
    ],
    selector: 'app-left-many-searchable-blotter-page',
    templateUrl: './left-many-searchable-blotter-page.component.html',
})
export class LeftManySearchableBlotterPage {

}

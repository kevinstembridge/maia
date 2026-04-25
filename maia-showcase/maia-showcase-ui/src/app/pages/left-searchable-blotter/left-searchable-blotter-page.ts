import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    LeftSearchableCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/many_to_many/left-searchable-crud-blotter.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        LeftSearchableCrudBlotterComponent
    ],
    selector: 'app-left-searchable-blotter-page',
    templateUrl: './left-searchable-blotter-page.html',
})
export class LeftSearchableBlotterPage {

}

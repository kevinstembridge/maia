import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {
    LeftManyCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/many-to-many/left-many-crud-blotter';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        LeftManyCrudBlotterComponent
    ],
    selector: 'app-left-many-blotter-page',
    templateUrl: './left-many-blotter-page.html',
})
export class LeftManyBlotterPage {

}

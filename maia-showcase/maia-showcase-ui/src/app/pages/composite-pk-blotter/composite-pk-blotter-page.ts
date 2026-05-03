import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {
    CompositePrimaryKeyCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/composite-pk/composite-primary-key-crud-blotter';

@Component({
    selector: 'app-composite-pk-blotter-page',
    templateUrl: './composite-pk-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        CompositePrimaryKeyCrudBlotterComponent
    ]
})
export class CompositePkBlotterPage {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {
    CompositePrimaryKeyCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-crud-blotter.component';

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

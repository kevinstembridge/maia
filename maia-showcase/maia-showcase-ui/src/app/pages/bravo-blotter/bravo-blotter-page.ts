import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {
    BravoCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/join/bravo-crud-blotter.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        BravoCrudBlotterComponent
    ],
    selector: 'app-bravo-blotter-page',
    templateUrl: './bravo-blotter-page.html',
})
export class BravoBlotterPage {

}

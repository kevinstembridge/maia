import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    BravoCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/join/bravo-crud-blotter.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        BravoCrudBlotterComponent
    ],
    selector: 'app-bravo-blotter-page',
    templateUrl: './bravo-blotter-page.html',
})
export class BravoBlotterPage {

}

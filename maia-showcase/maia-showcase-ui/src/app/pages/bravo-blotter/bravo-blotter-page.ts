import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    BravoCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/join/bravo-crud-table.component';

@Component({
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        BravoCrudTableComponent
    ],
    selector: 'app-bravo-blotter-page',
    templateUrl: './bravo-blotter-page.html',
})
export class BravoBlotterPage {

}

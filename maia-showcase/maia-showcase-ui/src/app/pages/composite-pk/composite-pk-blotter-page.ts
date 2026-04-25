import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    CompositePrimaryKeyCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/composite_pk/composite-primary-key-crud-blotter.component';

@Component({
    selector: 'app-composite-pk-blotter-page',
    templateUrl: './composite-pk-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        CompositePrimaryKeyCrudBlotterComponent
    ]
})
export class CompositePkBlotterPage {}

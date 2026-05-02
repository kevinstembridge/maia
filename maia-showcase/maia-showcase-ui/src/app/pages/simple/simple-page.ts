import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
    SimpleCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/simple/simple-crud-blotter';
import {PageLayout} from '@app/components/page-layout/page-layout';

@Component({
    selector: 'app-simple-page',
    templateUrl: './simple-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        SimpleCrudBlotterComponent,
        PageLayout
    ]
})
export class SimplePage {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
    SimpleCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/simple/simple-crud-blotter.component';
import {PageLayout} from '../../components/page-layout/page-layout';

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

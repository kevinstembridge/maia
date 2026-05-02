import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {
    SomeVersionedCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/versioned/some-versioned-crud-blotter.component';

@Component({
    selector: 'app-some-versioned-blotter-page',
    templateUrl: './some-versioned-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        SomeVersionedCrudBlotterComponent
    ]
})
export class SomeVersionedBlotterPage {}

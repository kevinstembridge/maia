import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {
    SomeVersionedCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/versioned/some-versioned-crud-blotter.component';

@Component({
    selector: 'app-some-versioned-blotter-page',
    templateUrl: './some-versioned-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        SomeVersionedCrudBlotterComponent
    ]
})
export class SomeVersionedBlotterPage {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
    SimpleCrudBlotterComponent
} from '@app/gen-components/org/maiaframework/showcase/simple/simple-crud-blotter.component';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';

@Component({
    selector: 'app-simple-page',
    templateUrl: './simple-page.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        SimpleCrudBlotterComponent,
        PageLayoutComponent
    ]
})
export class SimplePageComponent {}

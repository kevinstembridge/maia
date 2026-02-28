import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
    SimpleCrudTableComponent
} from '@app/gen-components/org/maiaframework/showcase/simple/simple-crud-table.component';

@Component({
    selector: 'app-simple-page',
    templateUrl: './simple-page.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        SimpleCrudTableComponent
    ]
})
export class SimplePageComponent {}

import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
    selector: 'app-simple-page',
    templateUrl: './simple-page.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SimplePageComponent {}
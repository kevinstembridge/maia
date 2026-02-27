import {ChangeDetectionStrategy, Component} from '@angular/core';

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePageComponent {}
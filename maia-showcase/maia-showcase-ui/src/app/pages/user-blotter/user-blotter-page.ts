import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {UserCrudBlotterComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-crud-blotter';

@Component({
    selector: 'app-users-blotter-page',
    templateUrl: './user-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        UserCrudBlotterComponent,
    ]
})
export class UserBlotterPage {}

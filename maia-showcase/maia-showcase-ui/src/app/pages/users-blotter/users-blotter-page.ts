import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {UserCrudBlotterComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-crud-blotter.component';

@Component({
    selector: 'app-users-blotter-page',
    templateUrl: './users-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        UserCrudBlotterComponent,
    ]
})
export class UsersBlotterPage {}

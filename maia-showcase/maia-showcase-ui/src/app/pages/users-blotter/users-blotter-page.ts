import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {UserCrudTableComponent} from '@app/gen-components/org/maiaframework/showcase/user-crud-table.component';

@Component({
    selector: 'app-users-blotter-page',
    templateUrl: './users-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        UserCrudTableComponent,
    ]
})
export class UsersBlotterPage {}

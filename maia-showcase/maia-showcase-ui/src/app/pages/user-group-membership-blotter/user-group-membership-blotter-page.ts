import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayoutComponent} from '@app/components/page-layout/page-layout.component';
import {UserGroupMembershipCrudBlotterComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-group-membership-crud-blotter.component';

@Component({
    selector: 'app-user-group-membership-blotter-page',
    templateUrl: './user-group-membership-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayoutComponent,
        UserGroupMembershipCrudBlotterComponent,
    ]
})
export class UserGroupMembershipBlotterPage {}

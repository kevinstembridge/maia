import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@app/components/page-layout/page-layout';
import {UserGroupMembershipCrudBlotterComponent} from '@app/gen-components/org/maiaframework/showcase/user/user-group-membership-crud-blotter';

@Component({
    selector: 'app-user-group-membership-blotter-page',
    templateUrl: './user-group-membership-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        UserGroupMembershipCrudBlotterComponent,
    ]
})
export class UserGroupMembershipBlotterPage {}

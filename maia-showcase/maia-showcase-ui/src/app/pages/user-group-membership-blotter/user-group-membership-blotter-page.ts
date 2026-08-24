import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {UserGroupMembershipBlotter} from '@app/gen-components/org/maiaframework/showcase/user/user-group-membership-blotter';

@Component({
    selector: 'app-user-group-membership-blotter-page',
    templateUrl: './user-group-membership-blotter-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        PageLayout,
        UserGroupMembershipBlotter,
    ]
})
export class UserGroupMembershipBlotterPage {}

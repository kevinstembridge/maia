import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {JobHistoryPageComponent} from '@maia/maia-jobs';
import {CurrentUserAuthStore} from '../../state/current-user-auth-store';

@Component({
    imports: [PageLayout, JobHistoryPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Job History">
            <maia-job-history-page [canAbandon]="authStore.hasMaiaJobWriteAuthority()" />
        </maia-page-layout>
    `
})
export class JobHistoryPage {

    protected readonly authStore = inject(CurrentUserAuthStore);

}

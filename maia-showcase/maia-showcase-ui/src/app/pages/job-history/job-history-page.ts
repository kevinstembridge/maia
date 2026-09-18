import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {JobHistoryPageComponent} from '@maia/maia-jobs';

@Component({
    imports: [PageLayout, JobHistoryPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Job History">
            <maia-job-history-page />
        </maia-page-layout>
    `
})
export class JobHistoryPage {}

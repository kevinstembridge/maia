import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '@maia/maia-ui';
import {JobsDashboardPageComponent as MaiaJobsDashboardPageComponent} from '@maia/maia-jobs';

@Component({
    imports: [PageLayout, MaiaJobsDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <maia-page-layout pageTitle="Jobs">
            <maia-jobs-dashboard-page />
        </maia-page-layout>
    `
})
export class JobsDashboardPage {}

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {PageLayout} from '../../components/page-layout/page-layout';
import {JobsDashboardPageComponent as MaiaJobsDashboardPageComponent} from '@maia/maia-jobs';

@Component({
    imports: [PageLayout, MaiaJobsDashboardPageComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    template: `
        <app-page-layout pageTitle="Jobs">
            <maia-jobs-dashboard-page />
        </app-page-layout>
    `
})
export class JobsDashboardPage {}
